package com.adisgrace.games;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.Vector2;
import java.util.Random;

public class WorldModel {

	// Player object
	private PlayerModel player;

	// Map of targets and their names
	private Map<String, TargetModel> targets;

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

	// Map of target world coordinates
	private Map<String, Vector2> targetCoords;

	// Number of days elapsed from start of game
	private int n_days;

	// Random number generator, only used for hacking success chance
	private Random rng;

	/** Enumeration representing the game's current state */
	protected enum GAMESTATE{
		/** Player has won the game */
		WIN,
		/** Player had lost the game */
		LOSE,
		/** Player is still playing the game. Welcome to the game */
		ONGOING
	};

	/**
	 * Constructs a WorldModel from a list of targets and their positions on the displayed map.
	 * @param targetJsons Array of target json filenames
	 * @param targetCoords Map of target_name->coords for exactly the targets described by the jsons
	 */
	public WorldModel(Array<String> targetJsons, Map<String, Vector2> targetCoords) {
		player = new PlayerModel();
		this.targetCoords = new HashMap<String, Vector2>(targetCoords);
		targets = new HashMap<String, TargetModel>();
		to_display = new HashMap<String, Array<String>>();
		hackednodes = new HashMap<String, Array<String>>();
		exposablenodes = new HashMap<String, Array<String>>();
		summaries = new HashMap<String, Map<String, String>>();
		contents = new HashMap<String, Map<String, String>>();
//        TargetModel target;
		for(String t : targetJsons) {
			this.addTarget(t);
//		    target = new TargetModel(t);
//			targets.put(t, target);
//			summaries.put(t, new HashMap<String, String>());
//			contents.put(t, new HashMap<String, String>());
//			hackednodes.put(t, new Array<String>());
//			exposablenodes.put(t, new Array<String>());
//			to_display.put(t, new Array<String>());
//			// to_interact.addAll(t.getFirstNodes());
//			for(String fact: target.getFirstNodes()){
//				to_display.get(t).add(fact);
//				// summary.get(t).put(fact, target.getSummary(fact));
//			}
		}
		n_days = 0;
		rng = new Random();
	}

	/**
	 * Returns the current state of this world.
	 *
	 * State can be: WIN, LOSE, ONGOING
	 *
	 * @return the world's current state
	 */
	public GAMESTATE getGameState(){
        if(!player.isLiving())
            return GAMESTATE.LOSE;

		for(TargetModel t: targets.values()){
			if(t.getState() == TargetModel.TargetState.GAMEOVER)
				return GAMESTATE.LOSE;
		}

        boolean allDefeated = true;
        for(TargetModel t: targets.values()){
            if(t.getState() != TargetModel.TargetState.DEFEATED){
                allDefeated = false;
            }
        }
        if(allDefeated)
            return GAMESTATE.WIN;

		return GAMESTATE.ONGOING;
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
	 * Returns the world coordinates for a given `target`.
	 *
	 * @param targetName 	The string name of the selected target
	 * @return 				Vector world coordinates of the target's origin
	 */
	public Vector2 getWorldCoordinates(String targetName){
		return targetCoords.get(targetName);
	}

	/**
	 * Returns the world coordinates for a given target fact.
	 *
	 * @param targetName 	The string name of the selected target
	 * @param fact			The specific fact belonging to `targetName`
	 * @return 				Vector world coordinates of the target fact's origin
	 */
	public Vector2 getWorldCoordinates(String targetName, String fact){
		return targets.get(targetName).getNodeCoords(fact).cpy().add(targetCoords.get(targetName));
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
		if(!targetCoords.containsKey(t))
			targetCoords.put(t, new Vector2());
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
		targetCoords.put(t, coords.cpy());
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

	/************************************************* PLAYER METHODS *************************************************/

	/**
	 * Advances to next day
	 *
	 * @return gamestate after napping
	 */
	public GAMESTATE nextTurn() {
		player.nextTurn();
		for(TargetModel t : targets.values()){
			t.nextTurn();
		}
		n_days++;
		return this.getGameState();
	}

	/**
	*	Calls the player's overwork function
	*
	*	@return the gamestate after this action
	*/
	public GAMESTATE overwork() {
		if(!player.overwork()){
			return GAMESTATE.LOSE;
		}
		return GAMESTATE.ONGOING;
	}

	/**
	*	Calls the player's vtube function
	*
	*	@return the gamestate after this action
	*/
	public GAMESTATE vtube(){
		player.vtube();
		return GAMESTATE.ONGOING;
	}

	/**
	*	Calls the player's vtube function
	*
	*	@param ap 		the amount of action points that the player decides to spend on relaxing
	*	@return 		the gamestate after this action
	*/
	public GAMESTATE relax(int ap){
		player.relax(ap);
		return GAMESTATE.ONGOING;
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
		return targets.get(targetname).threaten(fact);
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
		return targets.get(targetname).expose(fact);
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


