package com.adisgrace.games.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import com.adisgrace.games.util.Connector;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonReader;

/**
 * "Enemy" representation.
 * 
 * A target is one of the NPCs that the player is tasked with defeating.
 *
 * All information about individual targets is stored in a JSON, which is used to construct a
 * TargetModel.
 */

public class TargetModel {
	/** Enumeration representing the target's current state */
    public enum TargetState {
        /** Target is unaware of player, makes Paranoia checks against suspicion to move to SUSPICIOUS */
        UNAWARE,
        /** Target is suspicious of player, interacting with them before next inverse Paranoia check moves to PARANOID */
        SUSPICIOUS,
        /** Target is paranoid and scared, will go to GAMEOVER on next Paranoia check */
		PARANOID,
		/** Target is aware of player and feels threatened, will go to PARANOID next Paranoia check */
		THREATENED,
		/** Target's stress has reached maxStress and the target is no longer in play */
		DEFEATED,
		/** Target has gone to police, marking a game over for the player */
		GAMEOVER
    };

	/** The target's name */
	private String name;
	/** The target's location in isometric space */
	private int locX, locY;
	/** The target's traits */
	private TraitModel traits = new TraitModel();
	/** Target's maximum stress level (when stress reaches that point, target has been revenged) */
	private int maxStress;
	/** Turns before the target makes a Paranoia check. Possible values are from 0 to INV_PARANOIA_CONSTANT. */
	private int paranoia;
	/** Dictionary representing the nodes that are in the same pod as the target, where a node can be accessed with its name */
	private HashMap<String, FactNode> podDict;
	/** Hashmap of nodes that are first shown when the level begins, mapped to the corresponding paths that lead to them. */
	private ArrayMap<String, Array<Connector>> firstNodes;
	/** Array of Target combos */
	private Array<Combo> combos;

	/** Target's current stress level */
	private int stress;
	/** Target's current suspicion (maxes out at 100) */
	private int suspicion;
	/** Current state of target */
	private TargetState state;
	/** Number of turns remaining before next Paranoia check */
	private int countdown;

	/** Amount of suspicion reduced on a successful gaslight attempt */
	private int gaslight_reduction;
	/** Boolean which is true if paranoia deducted from other targets */
	private boolean paranoiac_used = false;
	/** Turns where the target does nothing every turn*/
	private int distractedTurns;
	/** % chance that a distract will fail*/
	private int distractFailChance;
	/** Whether this target has had their suspicion raised before*/
	private static boolean naturallySuspiciousCheck;

	/** Instance of Random class, to be used whenever a random number is needed */
	private Random rand;

	/** Constant for inverse Paranoia check, made every (INV_PARANOIA_CONSTANT - paranoia) turns */
	private static final int INV_PARANOIA_CONSTANT = 5;
	/** Constants for low/medium/high suspicion */
	private static final int SUSPICION_LOW = 5;
	private static final int SUSPICION_MED = 10;
	private static final int SUSPICION_HIGH = 15;
	/** Constant for multiplier that stress damage is multiplied by for expose */
	private static final float EXPOSE_MULTIPLIER = 2.5f;

	/************************************************* TARGET CONSTRUCTOR *************************************************/

	/**
	 * Creates a new Target with the given JSON data.
	 *
	 * The argument taken in should be a JSON filename in the format "FirstLast.json" and not
	 * the JSON itself.
	 * 
	 * All information about a target and their respective pod is stored in a JSON, that is
	 * passed into this constructor, where it is parsed. The relevant FactNodes are also
	 * created here.
	 *
	 * @param targetJson		Name of the JSON with all the target's data.
	 */
	public TargetModel(String targetJson) {
		// Get parser for JSON
		JsonValue json = new JsonReader().parse(Gdx.files.internal("levels/targets/" + targetJson));

		// Get main properties of target
		name = json.getString("targetName");
		paranoia = json.getInt("paranoia");
		maxStress = json.getInt("maxStress");

		gaslight_reduction = json.getInt("gaslightReduction", 8);

		// Initialize iterator for arrays
		JsonValue.JsonIterator itr;

		// Get firstNodes
		firstNodes = mapChildrenToPaths(json.get("firstNodes"), json.get("firstConnectors"),
				json.get("firstConnectorTypes"));

		// Get nodes
		JsonValue nodesArr = json.get("pod");
		itr = nodesArr.iterator();

		// Initializations for parsing of each node
		// Dictionary of node names mapped to the relevant FactNode object
		podDict = new HashMap<>();
		// Array cache to be used in node parsing
		JsonValue nodeArr;
		// The node itself as a JSON object
		JsonValue node;
		// Coordinates of node
		int nodeX;
		int nodeY;
		// Children of node, mapped to the paths to them
		ArrayMap<String, Array<Connector>> children = new ArrayMap<>();
		// Node converted to a FactNode
		FactNode fn;
		// Name of node
		String nodeName;
		// Iterator used to iterate through lists within a node
		JsonValue.JsonIterator nodeItr;

		// Iterate through nodes in pod and map them to their names in podDict
		while (itr.hasNext()) {
			// Get next node
			node = itr.next();

			// Get name
			nodeName = node.getString("nodeName");
			// Get coordinates
			nodeArr = node.get("coords");
			nodeItr = nodeArr.iterator();
			nodeX = nodeItr.next().asInt();
			nodeY = nodeItr.next().asInt();

			// Construct children with children names and paths to those children
			children = mapChildrenToPaths(node.get("children"), node.get("connectorCoords"), node.get("connectorTypes"));

			// Create FactNode
			fn = new FactNode(nodeName, node.getString("title"), node.getString("content"),
					node.getString("summary"), children, nodeX, nodeY, node.getBoolean("locked"),
					node.getInt("targetStressDamage"), node.getInt("playerStressDamage"));

			// Store FactNode in podDict, mapped to name
			podDict.put(nodeName, fn);
		}

		// Get combos
		// Initializations
		combos = new Array<Combo>();
		JsonValue combosArr = json.get("combos");
		itr = combosArr.iterator();
		Combo combo;
		Array<String> relatedFacts = new Array<>();

		// Iterate through combos, create each as a Combo, then add to array of combos
		while (itr.hasNext()) {
			node = itr.next();

			// Get related facts
			nodeArr = node.get("relatedFacts");
			nodeItr = nodeArr.iterator();
			relatedFacts.clear();
			while (nodeItr.hasNext()) {relatedFacts.add(nodeItr.next().asString());}
			relatedFacts.sort();

			// Construct and store combo
			combo = new Combo(new Array<>(relatedFacts), node.getString("overwrite"),
					node.getString("comboSummary"), node.getInt("comboStressDamage"));
			combos.add(combo);
		}

		// Initialize other values
		stress = 0;
		suspicion = 0;
		naturallySuspiciousCheck = false;
		state = TargetState.UNAWARE;
		distractedTurns = 0;
		distractFailChance = 0;
		countdown = paranoia;
		rand = new Random();
	}

	/**
	 * Helper function that constructs a hashmap of child names mapped to the path to that child.
	 *
	 * @param childNames		Names of the child nodes
	 * @param childPathCoords	Coordinates of the connectors in the paths to each child
	 * @param childPathTypes	Types of the connectors in the paths to each child
	 * @return					Hashmap of child names mapped to the path to them
	 */
	private ArrayMap<String, Array<Connector>> mapChildrenToPaths(JsonValue childNames, JsonValue childPathCoords,
																  JsonValue childPathTypes) {
		// First, get names of child nodes
		JsonValue.JsonIterator itr = childNames.iterator();
		// Now get paths that lead to those child nodes
		Array<Array<Connector>> paths = readPaths(childPathCoords, childPathTypes);
		// Construct and store hashmap of child nodes mapped to the paths to them
		int k=0;
		ArrayMap<String, Array<Connector>> childrenToPaths = new ArrayMap<>();
		while (itr.hasNext()){
			childrenToPaths.put(itr.next().asString(), paths.get(k));
			k++;
		}

		return childrenToPaths;
	}

	/**
	 * Helper function that reads a JSON value for connector coords and returns them in the correct format
	 *
	 * @param connectorCoordsArr	The JSON value to parse
	 * @return						The JSON value, correctly parsed
	 */
	private Array<Array<Connector>> readPaths(JsonValue connectorCoordsArr, JsonValue connectorTypesArr) {
		// Initialize iterators for connector coordinates
		JsonValue.JsonIterator coordPathItr = connectorCoordsArr.iterator();
		JsonValue.JsonIterator coordConnItr;
		// Initialize JSON array of coordinates in a path
		JsonValue coordPathArr;
		// Initialize array for a single coordinate
		int[] coordArr;

		// Initialize iterators for connector types
		JsonValue.JsonIterator typePathItr = connectorTypesArr.iterator();
		JsonValue.JsonIterator typeConnItr;
		// Initialize JSON array of connector types in a path
		JsonValue typePathArr;

		// Initialize array of connectors representing a path
		Array<Connector> path;
		// Initialize array of all paths that are read
		Array<Array<Connector>> allPaths = new Array<>();

		// Iterate through JSON array of paths
		while (coordPathItr.hasNext()){
			// Create new path
			path = new Array<>();

			// Go through connectors in in path
			coordPathArr = coordPathItr.next();
			coordConnItr = coordPathArr.iterator();
			typePathArr = typePathItr.next();
			typeConnItr = typePathArr.iterator();
			while (coordConnItr.hasNext()) {
				// Get coordinate from JSON
				coordArr = coordConnItr.next().asIntArray();
				// Construct connector and store in path
				path.add(new Connector(coordArr[0],coordArr[1],typeConnItr.next().asString()));
			}

			// Add completed path to array of paths
			allPaths.add(path);
		}

		return allPaths;
	}

	/************************************************* TARGET METHODS *************************************************/

	/**
	 * Returns the name of this target.
	 * 
	 * The target's name is represented as a first and last name with a space in between.
	 *
	 * @return the target's name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the current stress level of this target.
	 * 
	 * Stress can only be a value from 0 to maxStress.
	 *
	 * @return the target's current stress level.
	 */
	public int getStress() {
		return stress;
	}

	/**
	 * Returns the x-coordinate of the target's location.
	 *
	 * Coordinates are in world coordinates. Node coordinates treat this coordinate as the center.
	 *
	 * @return the target's x-coordinate.
	 */
	public int getX() {
		return locX;
	}

	/**
	 * Returns the y-coordinate of the target's location.
	 *
	 * Coordinates are in world coordinates. Node coordinates treat this coordinate as the center.
	 *
	 * @return the target's y-coordinate.
	 */
	public int getY() {
		return locY;
	}

	/**
	 * Returns the maximum stress level of this target.
	 * 
	 * When the target's stress reaches maxStress, the target becomes Defeated.
	 *
	 * @return the target's maximum possible stress.
	 */
	public int getMaxStress() {
		return maxStress;
	}

	/**
	 * Returns the current suspicion level of this target.
	 * 
	 * Suspicion is always within the range 0-100.
	 *
	 * @return the target's current suspicion.
	 */
	public int getSuspicion() {
		return suspicion;
	}

	/**
	 * Returns the current state of this target.
	 * 
	 * State can be: UNAWARE, SUSPICIOUS, PARANOID, THREATENED, DEFEATED, GAMEOVER
	 *
	 * @return the target's current state
	 */
	public TargetState getState(){
		return this.state;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// All additional getters and setters for traits usage will be here

	/**
	 * Returns the traits of this target.
	 *
	 * @return the target's traits
	 */
	public TraitModel getTraits(){
		return this.traits;
	}

	/**
	 * Reduce the paranoia of a target by a certain amount
	 *
	 * @param delta 		The paranoia amount to subtract from the target
	 * */
	public void reduce_paranoia(int delta){
		paranoia -= delta;
		if(paranoia < 0) paranoia = 0;
	}

	/**
	 * Sets the paranoiac_used variable to desired value
	 *
	 * @param delta 		The paranoiac_used attribute to set to
	 * */
	public void set_paranoiac_used(boolean delta){
		paranoiac_used = delta;
	}

	/**
	 * Gets the paranoiac_used variable
	 * */
	public boolean get_paranoiac_used(){
		return paranoiac_used;
	}

	/**
	 * Reduce the stress of a target by a certain amount with therapy
	 * */
	public void therapy(){
		stress -= TraitModel.HEALING_CONST;
		if (stress < 0) stress = 0;
	}

	/**
	 * The amount of suspicion give to other targets from this target, at least 1
	 * */
	public int spread_gossip(){
		return Math.max((int)(suspicion*TraitModel.GOSSIP_CONST), 1);
	}

	/**
	 * The amount of suspicion received from other targets from this target
	 *
	 * @param r			The amount of suspicion received from gossip
	 * */
	public void receive_gossip(int r){
		suspicion += r;
	}

	/**
	 * Increases the target's stress by the given amount.
	 * 
	 * Returns true if the target is still active after adding the stress, and false otherwise,
	 * in which case the new stress is greater than or equal to maxStress and the target becomes
	 * Defeated.
	 *
	 * @param s		Amount by which to increase target's stress
	 * @return		Whether target is still active and undefeated after adding stress
	 */
	public boolean addStress(int s) {
		//multiplies stress if target is sensitive
		stress += (this.traits.is_sensitive()?TraitModel.SENSITIVE_MULTIPLIER:1)*s;
		// If target's stress reaches or passes their maxStress, they're defeated
		if (stress >= maxStress) {
			state = TargetState.DEFEATED;
			return false;
		}
		return true;
	}

	/**
	 * Increases the target's suspicion by the given amount.
	 *
	 * Suspicion is always within the range 0-100.
	 *
	 * @param sus		Amount by which to increase target's suspicion
	 */
	public void addSuspicion(int sus) {
		suspicion += sus;
		// Clamp suspicion to the range 0-100
		if (suspicion < 0) {suspicion = 0;}
		else if (suspicion > 100) {suspicion = 100;}
	}

	/**
	 * Decreases the target's suspicion on a successful gaslight attempt,
	 * or increases it on a failed one.
	 * @param success Was the attempt successful?
	 */
	public void gaslight(boolean success) {
		if (success) addSuspicion(-gaslight_reduction);
		else addSuspicion(gaslight_reduction/2);
	}

	/**
	 * Upon successful distract, target is frozen for the duration of paranoia.
	 * Every successful distract lowers the chance for another successful distract by 25%
	 * @return whether the distract attempt was successful or not
	 */
	public boolean distract(){
		boolean success = rand.nextInt(100) > distractFailChance;
		if(success){
			distractedTurns += 25;
			traits.freeze();
			distractedTurns = paranoia;
		}else{
			addSuspicion(SUSPICION_LOW);
		}
		return success;
	}

	/**
	 * Handles target AI behavior at the end of each round, then returns the target's state in the next round.
	 * This function should be called for every target at the end of every round.
	 * 
	 * At the end of the round, decrement countdown. If countdown reaches 0, then it's time for the next Paranoia
	 * check, and handle that depending on current state. Returns the target's state at the top of the next round.
	 *
	 * @return 	Target's state at the top of the next round
	 */
	public TargetState nextTurn() {
		// If target is in state GameOver or Defeated, preemptively do nothing
		if (state == TargetState.GAMEOVER || state == TargetState.DEFEATED) {return state;}

		// If the target is distracted, do nothing this turn
		if(distractedTurns > 0){
			distractedTurns--;
			return state;
		}

		//unfreezes traits that were frozen upon a successful distract
		traits.unfreeze();

		countdown--;
		// If not time for next Paranoia check, return
		if (countdown != 0) {return state;}

		//if target is naturally suspicious and has raised suspicion before, increase suspicion
		if(this.getTraits().is_naturally_suspicious() && naturallySuspiciousCheck){
			suspicion+=TraitModel.NATURALLY_SUSPICIOUS_CONST;
		}

		// Otherwise, handle Paranoia check depending on state
		switch(state) {
			case UNAWARE:
				// Roll against suspicion
				if (rand.nextInt(100) < suspicion) {
					// If suspicion check succeeds, move target to Suspicious
					state = TargetState.SUSPICIOUS;
					// Reset countdown for inverse Paranoia check
					countdown = INV_PARANOIA_CONSTANT - paranoia;
				}
				break;
			case SUSPICIOUS:
				// Move back to Unaware
				state = TargetState.UNAWARE;
				// Reset countdown for Paranoia check
				countdown = paranoia;
				break;
			case PARANOID:
				// Game over
				state = TargetState.GAMEOVER;
				break;
			case THREATENED:
				// Move to Paranoid
				state = TargetState.PARANOID;
				// Reset countdown for Paranoia check
				countdown = paranoia;
				break;
			default:
				// If GameOver or Defeated, do nothing
		}
		// Return new state
		return state;
	}	

	/************************************************* FACTNODE METHODS *************************************************/

	/**
	 * Helper function that returns the FactNode in podDict with the given name as the key.
	 * 
	 * @param nodeName	Name of the node to get
	 * @return 			FactNode with the given name
	 */
	private FactNode getFactNode(String nodeName) {
		return podDict.get(nodeName);
	}

	/**
	 * Returns a hashmap of the first nodes for this target, mapped to the paths to them.
	 * 
	 * The first nodes are the ones that are visible immediately after the target is spawned in. This DOES NOT
	 * return the names of all the nodes in the pod.
	 * 
	 * Nodes are accounts, devices, etc. that are associated with the target and give more information about
	 * the target upon scanning. A node's name is an internal keyword used to identify a node, which can be
	 * passed into other TargetModel methods to achieve different things.
	 *
	 * For each node name as key, the value is an array of connectors forming the path to the key node. These
	 * connectors are in order from the parent to the child.
	 *
	 * @return	Hashmap of names of the first nodes in the target's pod, mapped to the paths that lead to them
	 */
	public ArrayMap<String, Array<Connector>> getFirstNodes() {
		return firstNodes;
	}

	/**
	 * Returns list of the names of all the facts in the current target's pod.
	 * 
	 * Nodes are accounts, devices, etc. that are associated with the target and give more information about
	 * the target upon scanning. A node's name is an internal keyword used to identify a node, which can be
	 * passed into other TargetModel methods to achieve different things.
	 * 
	 * @return 		Array of names of all the nodes in the target's pod.
	 */
	public Array<String> getNodes() {
		Array<String> result = new Array<>();
		// Iterate through the list of FactNodes
		for(String key:podDict.keySet()){
			FactNode factNode = getFactNode(key);
			result.add(factNode.getNodeName());
		}
		return result;
	}

	/**
	 * Returns the title of the node that the given fact is stored in.
	 * 
	 * The title of a fact's node is what should be displayed when hovering over the node.
	 * 
	 * Ex. "King County Court Records," "Torchlight Personnel File", etc.
	 * 
	 * @param name   	Name of the fact whose node's title we want
	 * @return 			Title of the node that the given fact is stored at
	 */
	public String getTitle(String name) {
		return getFactNode(name).getTitle();
	}

	/**
	 * Returns the coordinates of a given node.
	 * 
	 * A node's coordinates are relative to the target's location, where the target is at the origin.
	 * 
	 * @param name   	Name of the fact whose node's location we want
	 * @return 			Coordinates of the node, relative to the target's location
	 */
	public Vector2 getNodeCoords(String name) {
		FactNode fn = getFactNode(name);
		Vector2 nodeVec = new Vector2(fn.getX(), fn.getY());
		return nodeVec;
	}

	/**
	 * Returns the locked status of a given node.
	 *
	 * @param fact	Name of the fact whose locked information we want
	 * @return	boolean of whether node is locked or not
	 */
	public boolean getLocked(String fact) {
		return podDict.get(fact).getLocked();
	}

	/**
	 * Returns the content stored in the node with the given name.
	 * 
	 * The content of the node is what is shown to the player when the node is scanned, representing the information
	 * stored within that node. It is also what is shown if the player revisits the node to read it again.
	 * 
	 * @param name	Name of the node whose content we want
	 * @return 		Content stored at the given node
	 */
	public String getContent(String name) {
		return getFactNode(name).getContent();
	}

	/**
	 * Returns the summary stored in the node with the given name.
	 *
	 * The summary is what is stored in the player's notebook, so they can see a more concise version of the facts
	 * they've learned without having to reread each node.
	 *
	 * @param name	Name of the node whose summary we want
	 * @return 		Summary stored at the given node
	 */
	public String getSummary(String name) {
		return getFactNode(name).getSummary();
	}

	/**
	 * Returns the player stress damage of the node with the given name.
	 *
	 * @param name	Name of the node whose summary we want
	 * @return 		Stress damage
	 */
	public int getStressCost(String name) {
		return getFactNode(name).getPlayerStressDmg();
	}

	/**
	 * Returns hashmap of names of child nodes for the given node, mapped to the paths from the given
	 * node to them.
	 *
	 * A given node's children are the nodes that are made visible when the given node is scanned.
	 *
	 * As there is no situation where you would want the names of the children but not also the paths leading to them,
	 * this function is what should be called whenever a node is scanned and the children are needed.
	 *
	 * @param name  Name of the node whose children we want
	 * @return		Hashmap of names of child nodes for the given node, mapped to the paths from the given node to them
	 */
	public ArrayMap<String, Array<Connector>> getChildren(String name) {
		return getFactNode(name).getChildren();
	}

	/************************************************* SKILL METHODS *************************************************/

	/**
	 * Helper function that gets a random integer within the percentile range of the input.
	 *
	 * Returns a random integer in the range [val - (range/100)*val, val + (range/100)*val].
	 *
	 * @param val	Value to get within range of
	 * @param range Percentage of val that is the range to select an integer from
	 * @return 		Random integer within range
	 */
	private int randInRange(int val, int range) {
		int bound = (int)(((float)range / 100f) * val);
		return (val + rand.nextInt(bound * 2) - bound);
	}

	/**
	 * Used to threaten the target with the fact stored at the given node.
	 * 
	 * If the fact can be used to threaten, moves the target to the Threatened state, resets the
	 * countdown to the next Paranoia check, increases stress accordingly, deals the damage to
	 * the target accordingly. Otherwise, does nothing. Returns the amount of damage dealt, which
	 * can be used to calculate diminishing returns.
	 *
	 * Suspicion is increased by a low amount regardless.
	 * 
	 * @param fact	Name of the node where the threatening fact is stored
	 * @return 		Amount of damage dealt to target (can be 0)
	 */
	public int harass(String fact) {
		int stressDmg = getFactNode(fact).getTargetStressDmg();
		// Increase target's suspicion by a low amount
		suspicion += randInRange(SUSPICION_LOW, 25);
		naturallySuspiciousCheck = true;
		// If fact deals threaten damage above a critical threshold
		if (stressDmg > 5) {
			// Deal stress damage to target
			addStress(stressDmg);
			// Move target to threatened
			//TODO: threaten has become harass, and harass does not change target state
//			state = TargetState.THREATENED;
			// Reset countdown to next Paranoia check
			countdown = paranoia;
		}
		// Return amount of damage dealt
		return stressDmg;
	}

	/**
	 * Used to expose a fact about the target, which is stored at the given node.
	 * 
	 * If the fact deals nonzero stress damage, deals the damage to the target and
	 * moves target to Paranoid. Otherwise, does nothing. Returns the amount of
	 * damage dealt, which can be used to calculate diminishing returns.
	 *
	 * Suspicion is increased by a medium amount regardless.
	 * 
	 * @param fact	Name of the node where the to-be-exposed fact is stored
	 * @return 		Amount of damage dealt to the target (can be 0)
	 */
	public int expose(String fact) {
		FactNode factNode = getFactNode(fact);
		int stressDmg = factNode.getTargetStressDmg();
		// Increase target's suspicion by a medium amount
		suspicion += randInRange(SUSPICION_MED, 25);
		naturallySuspiciousCheck = true;
		// If exposing deals nonzero damage
		if (stressDmg != 0) {
			// Deal damage to target
			addStress(stressDmg);
			// Move target to Paranoid
			//TODO: match to action outcomes
			state = TargetState.PARANOID;
			// Reset countdown to next Paranoia check
			countdown = paranoia;
		}
		// Return amount of damage dealt, multiplied by expose multiplier
		return (int)(stressDmg * EXPOSE_MULTIPLIER);
	}

	/************************************************* COMBO METHODS *************************************************/

	/**
	 * A combo has four main properties:
	 * - relatedFacts: a list of FactNode names representing the facts that form a combo
	 * - overwrite: a String representing the FactNode name whose data should be overwritten when the combo is completed
	 * - comboSummary: a String representing the new summary that the summary of [overwrite] should be replaced with
	 * - comboStressDamage: an integer representing the new stress damage that the stress damage of [overwrite] should be replaced with
	 *
	 * You can implement this however, but I think it might be easiest to make an inner class called Combo with these properties, then
	 * have an array of combos be a field of TargetModel.
	 */
	private class Combo{
		/** A list of FactNode names representing the facts that form a combo */
		private Array<String> relatedFacts;

		/** A String representing the FactNode name whose data should be overwritten when the combo is completed */
		private String overwrite;

		/** A String representing the new summary that the summary of [overwrite] should be replaced with */
		private String comboSummary;

		 /** An integer representing the new stress damage that the stress damage of [overwrite] should be replaced with */
		private int comboStressDamage;

		/** An integer representing the length of the combo */
		private int length;

		/**
		 * Creates a new Combo with the given data.
		 *
		 *
		 * @param facts		A list of FactNode names representing the facts that form a combo.
		 * @param ow        A String representing the FactNode name whose data should be overwritten when the combo is completed
		 * @param cs        A String representing the new summary that the summary of [overwrite] should be replaced with
		 * @param cd        An integer representing the new stress damage that the stress damage of [overwrite] should be replaced with
		 */
		public Combo(Array<String> facts, String ow, String cs, int cd) {
			relatedFacts = facts;
			overwrite = ow;
			comboSummary = cs;
			comboStressDamage = cd;
			length = facts.size;
		}

		/**
		 * Creates a new Combo with the given data.
		 */
		public Combo() {
			length = 0;
		}

		/**
		 * Returns the facts that form the given combo.
		 *
		 * The facts are represented as an ArrayList of strings.
		 *
		 * @return the facts that form the given combo.
		 */
		public Array<String> getFacts() {
			return relatedFacts;
		}

		/**
		 * Returns the fact whose data should be overwritten when the combo is completed
		 *
		 * @return A String representing the FactNode name whose data should be overwritten when the combo is completed
		 */
		public String getOverwrite() {
			return overwrite;
		}

		/**
		 * Completes the combo by setting the summary and stress damage of the node whose name is the "overwrite"
		 * property of the combo with the summary and stress damage stored in the combo.
		 */
		private void activate(){
			FactNode activated = getFactNode(overwrite);
			activated.setSummary(comboSummary);
			activated.setTargetStressDmg(comboStressDamage);
		}
	}

	/**
	 * Checks if a given fact is part of a combo with any other of the given facts, and if so, updates
	 * the relevant information accordingly.
	 * 
	 * If a fact is part of a combo, then the node whose name is the "overwrite" property of the combo will
	 * have its summary and stress damage replaced with the summary and stress damage stored in the combo, and
	 * this function will return true. Otherwise, nothing happens and the function returns false.
	 * 
	 * If a fact is part of multiple combos that overwrite the same node, the combo that is applied is the longest
	 * one. If a fact is part of multiple combos that overwrite different nodes, then all unique combos are applied
	 * (if some overwrite the same node, pick the longest).
	 * 
	 * @param name	    Given fact, checks if a given fact is part of a combo with any other of the given facts.
	 * @param facts		Other given facts.
	 */
	public boolean checkForCombo(String name, Array<String> facts) {
		// Note that if a fact is in fact part of a combo, after replacing the fact's summary and stress
		// damage with that of the combo, the combo should be deleted from this target. If there are multiple
		// combos that contain the fact, all the combos that are the same length or less should be deleted.

		// For example, say we have facts A, B, and C, with the combos AB, AC, and ABC, and A is "overwrite" for all three.
		// checkForCombo(A, [B]): A's summary and stressDmg are replaced with those of combo AB. Combos AB and AC are deleted.
		// checkForCombo(A, [B, C]): A's summary and stressDmg are replaced with those of combo ABC. Combos AB, AC, and ABC are deleted.

		// Also, an example for multiple combos: say we have facts A, B, and C with combos AC and BC, and "overwrite" is the first node in the combo.
		// checkForCombo(C, [A, B]): A and B's summary and stressDmg are replaced with those of combos AC and BC, respectively. AB and BC are both deleted.
		// Another example: combos AC, BC, BCD, first node is the "overwrite".
		// checkForCombo(C, [A, B, D]): A and B's summary/stressDmg are replaced with those of combos AC and BCD, respectively. AB, BC, and BCD are all deleted.

		// create bag of facts to check for combos
		facts.add(name);

		// flag which indicates if at least one combo has been activated
		boolean flag = false;

		// Iterate over each fact and check if it's overwritten
		for (String f: facts){
			// Iterate over each combo and check if it's activated
			Combo longest = new Combo();
			for (Combo combo: combos){
				// Only checks combos whose overwrite = f
				if (combo.getOverwrite() == f){
					boolean isCombo = true;
					// Checks if all facts in a combo are included
					for (String comboFact: combo.getFacts()){
						if (!facts.contains(comboFact,true)) isCombo = false;
					}
					// Removes shorter combos
					if (isCombo){
						if (combo.length > longest.length){
							// longer combo which overwrites f found, replaces and removes shorter combo
							combos.removeValue(longest,true);
							longest = combo;
						}else {
							// longer combo which overwrites f already made, removes shorter combo
							combos.removeValue(combo,true);
						}
					}
				}
			}
			// activates combo and sets flag to true if there exists a combo which activates f
			if (longest.length >0){
				longest.activate();
				flag = true;
			}
			// remove activated combo
			combos.removeValue(longest,true);
		}
		return flag;
	}
}