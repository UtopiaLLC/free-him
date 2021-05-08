package com.adisgrace.games.models;

import java.util.*;

import com.adisgrace.games.util.GameConstants;
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
    public enum LevelState{
        ONGOING,
        WIN,
        LOSE,
        TIMEOUT
    }

    // Name of the Level
    private String name;

    /** How many days can this level last, loses the game if days run out */
    private int daysLeft = 10; // needs to be properly initialized

    // Player object
    private PlayerModel player;

    // Map of targets and their names
    private Map<String, TargetModel> targets;

    // Locations of each target in isometric coordinates
    private Map<String, int[]> targetLocs;

    // Set of names of boss targets in the level
    private Set<String> bosses;

    // Multiplier for suspicion spread from nonbosses to bosses
    // Every nonboss adds (suspicion)*SUSPICION_SPREAD suspicion to every boss
    private static double SUSPICION_SPREAD = 0.2;

    // Map of visible factnodes
    private Map<String, Array<String>> visibleFacts;

    // Map of hacked factnodes
    private Map<String, Array<String>> hackedFacts;

    // Map of exposable factnodes
    private Map<String, Array<String>> exposableFacts;

    // Map of interactable (hackable or scannable) factnodes
    //private Map<String, Array<String>> to_interact;

    //Map from factnode identifiers to shortened descriptions
    //Modified for combos
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
    }

    /**
     * Constructs a LevelModel from a list of targets.
     *
     * INVARIANT: Assumes that the levelJson string is in the form of levels/level/levelJson,
     * where the targets are stored in the separate folder levels/level/targets
     * @param levelJson Array of target json filenames
     */
    public LevelModel(String levelJson) {

        player = new PlayerModel();
        targets = new HashMap<String, TargetModel>();
        visibleFacts = new HashMap<String, Array<String>>();
        hackedFacts = new HashMap<String, Array<String>>();
        exposableFacts = new HashMap<String, Array<String>>();
        summaries = new HashMap<String, Map<String, String>>();
        contents = new HashMap<String, Map<String, String>>();
//        TargetModel target;

        JsonValue json = new JsonReader().parse(Gdx.files.internal(levelJson));
        String[] targetJsons = json.get("targets").asStringArray();
        name = json.get("name").asString();
        // TODO: Uncomment this line, and change "daysLeft" to whatever it is in the level json
        //daysLeft = json.get("daysLeft").asInt();

        if (json.get("dims") != null) {
            int[] dims = json.get("dims").asIntArray();
            n_cols = dims[0];
            n_rows = dims[1];
        } else {
            n_cols = GameConstants.DEFAULT_LEVEL_DIM;
            n_rows = GameConstants.DEFAULT_LEVEL_DIM;
        }

        //binds each target string to a location in the level
        JsonValue locations = json.get("targetLocs");
        JsonValue.JsonIterator itr = locations.iterator();
        targetLocs = new HashMap<>();

        //binds each target string to a targetModel
        //This for loop assumes that there is an equal amount of targets and targetLocations

        //splits the level json path into its components so that we can reconstruct it for targets
        String[] splitBySlash = levelJson.split("/");
        for(String targetJson: targetJsons){
//            targets.put(t.getName(), t);
            TargetModel t = addTarget(splitBySlash[0] + "/" + splitBySlash[1] + "/" + "targets/" + targetJson);
            targetLocs.put(t.getName(), itr.next().asIntArray());
            for(String fact: t.getNodes()) {
                if(!t.getLocked(fact)) {
                    hackedFacts.get(t.getName()).add(fact);
                }
            }
        }

        if(json.get("bosses") != null && json.get("bosses").asStringArray().length > 0)
            bosses = new HashSet<>(Arrays.asList(json.get("bosses").asStringArray()));
        else bosses = new HashSet<>(targets.keySet());

        n_days = 0;
        rng = new Random();
    }

    public Map<String, TargetModel> getTargets() {
        return targets;
    }

    public Map<String, int[]> getTargetLocs() {
        return targetLocs;
    }

    public Map<String, Array<String>> getHackedFacts() {
        return hackedFacts;
    }

    public Map<String, Array<String>> getExposableFacts() {
        return exposableFacts;
    }

    public Map<String, Array<String>> getVisibleFacts() {
        return visibleFacts;
    }

    public Map<String, Map<String, String>> getSummaries() {
        return summaries;
    }

    public Map<String, Map<String, String>> getContents() {
        return contents;
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
        return n_cols;
    }

    /**
     * Returns the height of this level
     */
    public int getHeight() {
        return n_rows;
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
     * Returns the world coordinates for a given `target`.
     *
     * @param targetName 	The string name of the selected target
     * @return 				Vector world coordinates of the target's origin
     */
    public Vector2 getWorldCoordinates(String targetName){
        return new Vector2(targets.get(targetName).getX(),targets.get(targetName).getY());
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


    /**
     * Returns the current state of this world.
     *
     * State can be: WIN, LOSE, ONGOING
     *
     * @return the world's current state
     */

    public LevelState getLevelState(){
        // check if player lost by running out of time
        if (daysLeft <= 0)
            return LevelState.TIMEOUT;

        if(!player.isLiving())
            return LevelState.LOSE;

        for(String t: bosses){
            if(targets.get(t).getState().equals(TargetModel.TargetState.GAMEOVER))
                return LevelState.LOSE;
        }

        boolean allDefeated = true;
        for(TargetModel t: targets.values()){
            if(t.getState() != TargetModel.TargetState.DEFEATED){
                allDefeated = false;
            }
        }
        if(allDefeated)
            return LevelState.WIN;

        return LevelState.ONGOING;
    }

    public void nextDay(){
        n_days++;
        // reduce time left by 1 day
        daysLeft--;
        // Implements target trait : paranoiac
        // iterate over all targets to see if any is paranoiac
        for (String targetname : targets.keySet()){
            TargetModel t = targets.get(targetname);
            if(!bosses.contains(targetname) && t.getState() != TargetModel.TargetState.DEFEATED){
                for(String bossname : bosses){
                    //targets.get(bossname).addSuspicion((int)(t.getSuspicion() * SUSPICION_SPREAD));
                }
            }
            if (t.getTraits().is_paranoiac()){
                //If a target is paranoiac and is alive and paranoiac_used is false, reduce paranoia of all targets in level by 1
                if (t.getState() != TargetModel.TargetState.DEFEATED && t.get_paranoiac_used() == false){
                    for (TargetModel tt : targets.values()){
                        tt.reduce_paranoia(1);
                    }
                    // set paranoiac_used to true after target ability used
                    t.set_paranoiac_used(true);
                }
                //If a target is paranoiac and is dead and paranoiac_used is true, buff paranoia of all targets in level by 1
                if (t.getState() == TargetModel.TargetState.DEFEATED && t.get_paranoiac_used() == true){
                    for (TargetModel tt : targets.values()){
                        tt.reduce_paranoia(-1);
                    }
                    // set paranoiac_used to true after target ability used
                    t.set_paranoiac_used(false);
                }
            }
            if (t.getTraits().is_gossip()){
                // if target is gossip and is alivespread suspicion to all other targets
                if (t.getState() != TargetModel.TargetState.DEFEATED){
                    for (TargetModel tt : targets.values()){
                        // target does not spread suspicion to itself
                        if (tt != t){
                            tt.receive_gossip(t.spread_gossip());
                        }
                    }
                }
            }
        }
    }



    /**
     * Returns the visible facts for a given `target`.
     *
     * @param targetName 	The string name of the selected target
     * @return 				Array of fact IDs that are visible to the player
     */
    public Array<String> getVisibleFacts(String targetName){
        return visibleFacts.get(targetName);
    }

    /**
     * Returns the exposable facts for a given `target`.
     *
     * @param targetName    The string name of the selected target
     * @return 				Array of fact IDs that are scanned and not exposed
     */
    public Set<String> getExposableFacts(String targetName){
        return exposableFacts.keySet();
    }






    /************************************************* CONTROLLER METHODS *************************************************/

    /**
     * Adds target to model.
     * New target world coords are 0,0, but does not change existing coordinates.
     * @param targetJson name of json file containing target data
     */
    private TargetModel addTarget(String targetJson){
        TargetModel target = new TargetModel(targetJson);
        String t = target.getName();
        targets.put(t, target);
        summaries.put(t, new HashMap<String, String>());
        contents.put(t, new HashMap<String, String>());
        hackedFacts.put(t, new Array<String>());
        exposableFacts.put(t, new Array<String>());
        return target;
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
        return visibleFacts;
    }

    /**
     * Returns list of targets
     * @return target_id list
     */
    public Array<String> getTargetSet() {
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

    /**
     * Returns whether a target is a boss
     * @param targetname name of target
     * @return true if target is a boss for this level
     */
    public boolean isBoss(String targetname){
        return bosses.contains(targetname);
    }

    /**
     * Returns the bosses of the level
     * @return set of boss names
     */
    public Set<String> getBosses(){
        return bosses;
    }

    /************************************************* PLAYER METHODS *************************************************/


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
     *	@param ap 		the amount of action points that the player decides to spend on relaxing
     *	@return 		the gamestate after this action
     */
    public LevelModel.GAMESTATE relax(int ap){
        player.relax(ap);
        return LevelModel.GAMESTATE.ONGOING;
    }

    /**
     * Returns all facts connected to a given fact
     * @param targetName name of target who "owns" fact to be viewed
     * @param fact fact identifier
     * @return an Array of facts connected to fact
     */
    public Array<String> getConnections(String targetName, String fact){
        TargetModel target = targets.get(targetName);
        return new Array(target.getChildren(fact).keys);
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
     * Coerce function, <b>unimplemented</b>
     * @param targetname target to coerce
     * @param fact fact to coerce target over
     */
    public void coerce(String targetname, String fact){
        return;
    }
}
