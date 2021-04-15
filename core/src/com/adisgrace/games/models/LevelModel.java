package com.adisgrace.games.models;

import java.util.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonReader;

/**
 * "Level" representation.
 *
 * A level is one level consisting of multiple targets and their corresponding nodes.
 *
 * All information about individual levels is stored in a JSON, which is used to construct a
 * LevelModel.
 */

public class LevelModel {
    // Name of the Level
    private String name;

    //dimensions of the world
    private int width;
    private int height;

    // Player object
    private PlayerModel player;

    // Map of targets and their names
    private Map<String, TargetModel> targets;

    // Locations of each target in isometric coordinates
    private Map<String, int[]> targetLocs;

    // Map of visible factnodes
    private Map<String, Array<String>> to_display;

    // Map of hacked factnodes
    private Map<String, Array<String>> hackednodes;

    // Map of exposable factnodes
    private Map<String, Array<String>> exposablenodes;

    // Map of interactable (hackable or scannable) factnodes
//	private Map<String, Array<String>> to_interact;

    // Map from factnode identifiers to shortened descriptions
    // Modified for combos
    private Map<String, Map<String, String>> summaries;


    // Map from factnode identifiers to full descriptions
    // Modified for combos
    private Map<String, Map<String, String>> contents;

    // Number of days elapsed from start of game
    private int n_days;

    // Random number generator, only used for hacking success chance
    private Random rng;

    // Dimensions of level grid
    private int n_rows, n_cols;

    /** Enumeration representing the game's current state */
    protected enum GAMESTATE{
        /** Player has won the game */
        WIN,
        /** Player had lost the game */
        LOSE,
        /** Player is still playing the game. Welcome to the game */
        ONGOING
    };

    /** Enum representing different verbs */
    protected enum Verb {
        VIEWFACT,
        SCAN,
        HACK,
        RELAX,
        NEXTTURN,
        OVERWORK,
        THREATEN,
        VTUBE
    }

    /**
     * Constructs a LevelModel from a list of targets.
     * @param levelJson Array of target json filenames
     */
    public LevelModel(String levelJson) {

        player = new PlayerModel();
        targets = new HashMap<String, TargetModel>();
        //to_display = new HashMap<String, Array<String>>();
        //hackednodes = new HashMap<String, Array<String>>();
        //exposablenodes = new HashMap<String, Array<String>>();
        //summaries = new HashMap<String, Map<String, String>>();
        //contents = new HashMap<String, Map<String, String>>();
//        TargetModel target;

        //TODO parse json file ${levelName}.json into list of strings targetJsons + other level properties
        JsonValue json = new JsonReader().parse(Gdx.files.internal("levels/" + levelJson));
        String[] targetJsons = json.get("targets").asStringArray();

        name = json.get("name").asString();

        int[] dims = json.get("dims").asIntArray();
        width = dims[0];
        height = dims[1];

        //binds each target string to a targetModel
        for(String target: targetJsons){
            targets.put(target, new TargetModel(target));
        }

        //binds each target string to a location in the level
        JsonValue locations = json.get("targetLocs");
        JsonValue.JsonIterator itr = locations.iterator();

        //This for loop assumes that there is an equal amount of targets and targetLocations
        for(String target: targetJsons){targetLocs.put(target, itr.next().asIntArray());}

        n_days = 0;
        rng = new Random();
    }

    /**
     * Returns the name of the level
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the width of this level
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of this level
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the queried target location
     *
     * @param target    the name of the target
     */
    public int[] getTargetLoc(String target) {
        return targetLocs.get(target);
    }

    /**
     * Returns the current state of this world.
     *
     * State can be: WIN, LOSE, ONGOING
     *
     * @return the world's current state
     */
    public LevelModel.GAMESTATE getGameState(){
        if(!player.isLiving())
            return LevelModel.GAMESTATE.LOSE;

        for(TargetModel t: targets.values()){
            if(t.getState() == TargetModel.TargetState.GAMEOVER)
                return LevelModel.GAMESTATE.LOSE;
        }

        boolean allDefeated = true;
        for(TargetModel t: targets.values()){
            if(t.getState() != TargetModel.TargetState.DEFEATED){
                allDefeated = false;
            }
        }
        if(allDefeated)
            return LevelModel.GAMESTATE.WIN;

        return LevelModel.GAMESTATE.ONGOING;
    }



    /**
     * Returns the visible facts for a given `target`.
     *
     * @param targetName 	The string name of the selected target
     * @return 				Array of fact IDs that are visible to the player
     */
    public Array<String> getVisibleFacts(String targetName){
        return to_display.get(targetName);
    }

    /**
     * Returns the exposable facts for a given `target`.
     *
     * @param targetName    The string name of the selected target
     * @return 				Array of fact IDs that are scanned and not exposed
     */
    public Set<String> getExposableFacts(String targetName){
        return exposablenodes.keySet();
    }

    /**
     * Returns the world coordinates for a given `target`.
     *
     * @param targetName 	The string name of the selected target
     * @return 				Vector world coordinates of the target's origin
     */
    public Vector2 getWorldCoordinates(String targetName){
        return new Vector2(targetLocs.get(targetName)[0], targetLocs.get(targetName)[1]);
    }

    /**
     * Returns the world coordinates for a given target fact.
     *
     * @param targetName 	The string name of the selected target
     * @param fact			The specific fact belonging to `targetName`
     * @return 				Vector world coordinates of the target fact's origin
     */
    public Vector2 getWorldCoordinates(String targetName, String fact){
        return targets.get(targetName).getNodeCoords(fact).cpy().add(getWorldCoordinates(targetName));
    }

    /************************************************* CONTROLLER METHODS *************************************************/

    /**
     * Adds target to model.
     * New target world coords are 0,0, but does not change existing coordinates.
     * @param t name of json file containing target data
     */
    public void addTarget(String t){
        TargetModel target = new TargetModel(t);
        t = target.getName();
        targets.put(t, target);
        summaries.put(t, new HashMap<String, String>());
        contents.put(t, new HashMap<String, String>());
        hackednodes.put(t, new Array<String>());
        exposablenodes.put(t, new Array<String>());
        to_display.put(t, new Array<String>());
        for(String fact: target.getFirstNodes()){
            to_display.get(t).add(fact);
        }
    }

    /**
     * Adds target to model.
     * @param t name of json file containing target data
     * @param coords world coordinates of target
     */
    public void addTarget(String t, Vector2 coords){
        TargetModel target = new TargetModel(t);
        t = target.getName();
        targets.put(t, target);
        summaries.put(t, new HashMap<String, String>());
        contents.put(t, new HashMap<String, String>());
        hackednodes.put(t, new Array<String>());
        exposablenodes.put(t, new Array<String>());
        to_display.put(t, new Array<String>());
        for(String fact: target.getFirstNodes()){
            to_display.get(t).add(fact);
        }
    }

    /**
     * General function that handles clicking on a node.
     * Hacks or scans node if appropriate and returns an empty string.
     * If node has already been scanned, returns node contents.
     * @param targetname  target to interact with
     * @param fact particular node id of target to interact with
     * @return empty string if hacking or scanning, else fact contents
     */
    public String interact(String targetname, String fact){
        if(!targets.containsKey(targetname))
            throw new RuntimeException("Invalid target");
        if(!to_display.get(targetname).contains(fact, false))
            throw new RuntimeException("Fact not discovered");
        if(contents.get(targetname).containsKey(fact))
            return this.viewFact(targetname, fact);
        if(hackednodes.get(targetname).contains(fact, false))
            return this.scan(targetname, fact);
        else
        if(this.hack(targetname, fact))
            return "You successfully hack the node.";
        else return ("You fail to hack the node. " + targetname + " begins to catch wind of your activities.");
    }

    /**
     * Interaction that would result from calling interact(targetname,fact)
     * Returns 0 for hack, 1 for scan, and 2 for viewFact
     * @param targetname  target that would be interacted with
     * @param fact particular node id of target that would be interacted with
     * @return int representing resulting verb of interaction
     */
    public LevelModel.Verb interactionType(String targetname, String fact){
        if(contents.get(targetname).containsKey(fact))
            return LevelModel.Verb.VIEWFACT;
        if(hackednodes.get(targetname).contains(fact, false))
            return LevelModel.Verb.SCAN;
        else
            return LevelModel.Verb.HACK;
    }

    /**
     * Returns PlayerModel
     * @return player
     */
    public PlayerModel getPlayer() {
        return player;
    }

    /**
     * Returns map containing displayed nodes.
     * @return map of target_id -> node_id list
     */
    public Map<String, Array<String>> getDisplayedNodes() {
        return to_display;
    }

    /**
     * Returns list of targets
     * @return target_id list
     */
    public Array<String> getTargets() {
        return new Array<String>((String[])targets.keySet().toArray());
    }

    /**
     * Returns a particular TargetModel
     * @param targetname name of target
     * @return attached TargetModel
     */
    public TargetModel getTarget(String targetname){
        return targets.get(targetname);
    }

    /************************************************* PLAYER METHODS *************************************************/

    /**
     * Advances to next day
     *
     * @return gamestate after napping
     */
    public LevelModel.GAMESTATE nextTurn() {
        player.nextTurn();
        for(TargetModel t : targets.values()){
            t.nextTurn();
            //TODO is this the way we want to implement traits?
        }
        n_days++;
        return this.getGameState();
    }

    /**
     *	Calls the player's overwork function
     *
     *	@return the gamestate after this action
     */
    public LevelModel.GAMESTATE overwork() {
        if(!player.overwork()){
            return LevelModel.GAMESTATE.LOSE;
        }
        return LevelModel.GAMESTATE.ONGOING;
    }

    /**
     *	Calls the player's vtube function
     *
     *	@return the gamestate after this action
     */
    public LevelModel.GAMESTATE vtube(){
        player.vtube();
        return LevelModel.GAMESTATE.ONGOING;
    }

    /**
     *	Calls the player's vtube function
     *
     *	@param ap 		the amount of action points that the player decides to spend on relaxing
     *	@return 		the gamestate after this action
     */
    public LevelModel.GAMESTATE relax(int ap){
        player.relax(ap);
        return LevelModel.GAMESTATE.ONGOING;
    }

    /**
     *	Harass a target
     *
     *	@param targetname 	name of target
     *	@return 			the gamestate after this action
     */
    public LevelModel.GAMESTATE harass(String targetname){
        // Get harass damage and inflict on target
        int stressDmg = targets.get(targetname).harass();
        targets.get(targetname).addStress(stressDmg);
        player.harass();
        return this.getGameState();
    }

    /**
     * Hack function, decreases player AP and hacks target fact
     * May throw runtime exceptions if provided invalid inputs
     * @param targetname target to hack
     * @param fact particular node id of target to hack
     * @return was the hack successful?
     */
    public boolean hack(String targetname, String fact){
        if(!targets.containsKey(targetname))
            throw new RuntimeException("Invalid target");
        if(!to_display.get(targetname).contains(fact, false)
                || hackednodes.get(targetname).contains(fact, false))
            throw new RuntimeException("Node is undiscovered, or has already been hacked");
        if(!player.canHack())
            throw new RuntimeException("Insufficient AP to hack");
        player.hack();
        if(rng.nextDouble() < 0.2){
            System.out.println("Suspicion before " + targets.get(targetname).getSuspicion());
            targets.get(targetname).addSuspicion(25);
            System.out.println("Suspicion after " + targets.get(targetname).getSuspicion());
            return false;
        }
        hackednodes.get(targetname).add(fact);
        return true;
    }

    /**
     * Scan function, decreases player AP and logs and returns scan results
     * May throw runtime exceptions if provided invalid inputs
     * @param targetname target to scan
     * @param fact particular node id of target to scan
     * @return contents of scanned node, <b>NOT CURRENT GAMESTATE</b>
     */
    public String scan(String targetname, String fact){
        if(!targets.containsKey(targetname))
            throw new RuntimeException("Invalid target");
        if(!to_display.get(targetname).contains(fact, false)
                || !hackednodes.get(targetname).contains(fact, false)
                || summaries.get(targetname).containsKey(fact))
            throw new RuntimeException("Node is undiscovered or unhacked, or has already been scanned");
        if(!player.canScan())
            throw new RuntimeException("Insufficient AP to scan");
        player.scan(0f); // Stress cost for scanning is unimplemented
        summaries.get(targetname).put(fact, targets.get(targetname).getSummary(fact));
        contents.get(targetname).put(fact, targets.get(targetname).getContent(fact));
        // combo checking
        //Array<String> facts_known = new Array<String>((String[]) summaries.keySet().toArray());
        Array<String> facts_known = new Array<String>();
        for(String key : summaries.keySet()) {
            facts_known.add(key);
        }
        int factsSize = facts_known.size; //////////////////////////////
        for(int i = 0; i < factsSize; i++) {
            String fact_ = facts_known.get(i);
            if (targets.get(targetname).checkForCombo(fact_, facts_known)) {
                summaries.get(targetname).put(fact, targets.get(targetname).getSummary(fact_));
                contents.get(targetname).put(fact, targets.get(targetname).getContent(fact_));
            }
        }
        exposablenodes.get(targetname).add(fact);
        to_display.get(targetname).addAll(targets.get(targetname).getChildren(fact));
        return targets.get(targetname).getContent(fact);
    }

    /**
     * Returns all facts connected to a given fact
     * @param targetName name of target who "owns" fact to be viewed
     * @param fact fact identifier
     * @return an Array of facts connected to fact
     */
    public Array<String> getConnections(String targetName, String fact){
        TargetModel target = targets.get(targetName);
        return target.getChildren(fact);
    }

    /**
     * View contents of a known fact
     * @param targetname name of target who "owns" fact to be viewed
     * @param fact fact identifier
     * @return contents of fact
     */
    public String viewFact(String targetname, String fact){
        if(!targets.containsKey(targetname))
            throw new RuntimeException("Invalid target");
        if(!contents.get(targetname).containsKey(fact))
            throw new RuntimeException("Contents of node unknown");
        return contents.get(targetname).get(fact);
    }

    /**
     * Gets map fact_id->fact_contents containing all facts known about target
     * @param targetname
     * @return Map from fact ids to fact contents
     */
    public Map<String, String> viewFacts(String targetname){
        Map<String, String> out = new HashMap<String, String>();
        for(String fact_ : contents.get(targetname).keySet())
            out.put(fact_, contents.get(targetname).get(fact_));
        return out;
    }

    /**
     * View summary of a known fact
     * @param targetname name of target who "owns" fact to be viewed
     * @param fact fact identifier
     * @return summary of fact
     */
    public String viewFactSummary(String targetname, String fact){
        if(!targets.containsKey(targetname))
            throw new RuntimeException("Invalid target");
        if(!summaries.get(targetname).containsKey(fact))
            throw new RuntimeException("Contents of node unknown");
        return summaries.get(targetname).get(fact);
    }

    /**
     * Gets map fact_id->fact_summary containing all facts known about target
     * @param targetname
     * @return Map from fact ids to fact summaries
     */
    public Map<String, String> viewFactSummaries(String targetname){
        Map<String, String> out = new HashMap<String, String>();
        for(String fact_ : summaries.get(targetname).keySet())
            out.put(fact_, summaries.get(targetname).get(fact_));
        return out;
    }

    /**
     * Threaten function, increases target stress and reduces AP
     * @param targetname target to threaten
     * @param fact fact to threaten target over
     * @return amount of stress increase on target
     */
    public int threaten(String targetname, String fact){
        if(!targets.containsKey(targetname))
            throw new RuntimeException("Invalid target");
        if(!contents.get(targetname).containsKey(fact))
            throw new RuntimeException("Node has not been scanned");
        if(!player.canThreaten())
            throw new RuntimeException("Insufficient AP to threaten");
        if(!exposablenodes.get(targetname).contains(fact, false))
            throw new RuntimeException("This fact has already been exposed");
        player.threaten();
        int stressDamage = targets.get(targetname).threaten(fact);
        targets.get(targetname).addStress(stressDamage);
        return stressDamage;
    }

    /**
     * Expose function, increases target stress greatly and reduces AP, but renders the fact unusable
     * @param targetname target of attack
     * @param fact fact to expose
     * @return amount of stress increase on target
     */
    public int expose(String targetname, String fact){
        if(!targets.containsKey(targetname))
            throw new RuntimeException("Invalid target");
        if(!contents.get(targetname).containsKey(fact))
            throw new RuntimeException("Node has not been scanned");
        if(!player.canExpose())
            throw new RuntimeException("Insufficient AP to expose");
        if(!exposablenodes.get(targetname).contains(fact, false))
            throw new RuntimeException("This fact has already been exposed");
        player.expose();
        exposablenodes.get(targetname).removeValue(fact, false);
        int stressDamage = targets.get(targetname).expose(fact);
        targets.get(targetname).addStress(stressDamage);
        return stressDamage;
    }

    /**
     * Coerce function, <b>unimplemented</b>
     * @param targetname target to coerce
     * @param fact fact to coerce target over
     */
    public void coerce(String targetname, String fact){
        return;
    }
}
