package com.adisgrace.games.models;

import java.util.HashMap;
import java.util.Random;

import com.adisgrace.games.util.Connector;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
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
	/** The target's traits */
	private TraitModel traits = new TraitModel();
	/** The target's location in world coordinates */
	private int locX;
	private int locY;
	/**	Targets connected to the current target */
	private Array<String> neighbors;
	/** Target's current stress level */
	private int stress;
	/** Target's maximum stress level (when stress reaches that point, target has been revenged) */
	private int maxStress;
	/** Target's current suspicion (maxes out at 100) */
	private int suspicion;
	/** Turns before the target makes a Paranoia check. Possible values are from 0 to INV_PARANOIA_CONSTANT. */
	private int paranoia;
	/** Number of turns remaining before next Paranoia check */
	private int countdown;
	/** Whether this target has had their suspicion raised before*/
	private static boolean naturallySuspiciousCheck;
	/** Current state of target */
	private TargetState state;
	/** Dictionary representing the nodes that are in the same pod as the target, where a node can be accessed with its name */
	private HashMap<String, FactNode> podDict;
	/** Array of node names representing the nodes that are immediately visible when the target first becomes available */
	private Array<String> firstNodes;

	/** The paths that are first shown when the level begins. Each path corresponds to the node in firstNodes at the same index and is
	 * in isometric coordinates. */
	private Array<Array<Vector2>> firstConnectorPaths;
	/** The connector types for the nodes that are immediately visible when the level begins, index corresponds to the node in firstNodes */
	private Array<Array<String>> firstConnectorTypes;

	/** Array of Target combos */
	private Array<Combo> combos;

	/** Constant for inverse Paranoia check, made every (INV_PARANOIA_CONSTANT - paranoia) turns */
	private static final int INV_PARANOIA_CONSTANT = 5;

	/** Constants for low/medium/high suspicion */
	private static final int SUSPICION_LOW = 5;
	private static final int SUSPICION_MED = 10;
	private static final int SUSPICION_HIGH = 15;
	/** Instance of Random class, to be used whenever a random number is needed */
	private Random rand;

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
		// STORE ALL ARRAYS ALPHABETICALLY

		// Get parser for JSON
		JsonValue json = new JsonReader().parse(Gdx.files.internal("levels/targets/" + targetJson));

		// Get main properties of target
		name = json.getString("targetName");
		paranoia = json.getInt("paranoia");
		maxStress = json.getInt("maxStress");

		// Initialize iterator for arrays
		JsonValue.JsonIterator itr;
		JsonValue.JsonIterator itr2;

		// Get neighbors
//		neighbors = new Array<String>();
//		JsonValue neighborArr = json.get("neighbors");
//		itr = neighborArr.iterator();
//		// Iterate through neighbors and add to ArrayList of neighbors
//		while (itr.hasNext()) {neighbors.add(itr.next().asString());}
//		// Sort alphabetically
//		neighbors.sort();

		// Get firstNodes
		firstNodes = new Array<>();
		JsonValue firstNodesArr = json.get("firstNodes");
		itr = firstNodesArr.iterator();
		while (itr.hasNext()){firstNodes.add(itr.next().asString());}


		// Get firstConnectorPaths
		// Get array of paths, where each path is an array of coordinates, where each coordinate is
		// an array of exactly 2 ints representing isometric coordinates
		firstConnectorPaths = readConnectorCoords(json.get("firstConnectors"));

		// Get firstConnectorTypes
		// Get array of paths, where each path is an array of coordinates, where each coordinate is
		// an array of exactly 2 ints representing isometric coordinates
		firstConnectorTypes = readConnectorTypes(json.get("firstConnectorTypes"));


//		// Get target coordinates
//		neighborArr = json.get("loc");
//		itr = neighborArr.iterator();
//		locX = itr.next().asInt();
//		locY = itr.next().asInt();

		// Get nodes
		JsonValue nodesArr = json.get("pod");
		itr = nodesArr.iterator();
		// Get number of firstNodes
		//int firstNodesCount = json.getInt("firstNodesCount");

		// Initializations
		podDict = new HashMap<String, FactNode>();
		//firstNodes = new Array<String>();
		JsonValue nodeArr;
		JsonValue node;
		int nodeX;
		int nodeY;
		Array<String> children = new Array<String>();
		Array<Array<Vector2>> connectorCoords = new Array<>();
		Array<Array<String>> connectorTypes = new Array<>();
		FactNode fn;
		String nodeName;
		JsonValue.JsonIterator nodeItr;

		// Iterate through nodes in pod and map them to their names in podDict
		while (itr.hasNext()) {
			node = itr.next();

			// Get name
			nodeName = node.getString("nodeName");
			// Get coordinates
			nodeArr = node.get("coords");
			nodeItr = nodeArr.iterator();
			nodeX = nodeItr.next().asInt();
			nodeY = nodeItr.next().asInt();

			// Get connectorCoords
			connectorCoords = readConnectorCoords(node.get("connectorCoords"));

			// Get connectorTypes
			connectorTypes = readConnectorTypes(node.get("connectorTypes"));

			// Get children
			nodeArr = node.get("children");
			nodeItr = nodeArr.iterator();
			children.clear();
			while (nodeItr.hasNext()) {children.add(nodeItr.next().asString());}
			children.sort();

			// Create FactNode
			fn = new FactNode(nodeName, node.getString("title"), node.getString("content"),
					node.getString("summary"), new Array<String>(children), nodeX, nodeY,
					node.getBoolean("locked"), node.getInt("targetStressDamage"),
					node.getInt("playerStressDamage"), connectorCoords, connectorTypes);

			// Store FactNode in podDict, mapped to name
			podDict.put(nodeName, fn);
		}
		firstNodes.sort();

		// Get combos
		// Initializations
		combos = new Array<Combo>();
		JsonValue combosArr = json.get("combos");
		itr = combosArr.iterator();
		Combo combo;

		// Iterate through combos, create each as a Combo, then add to array of combos
		while (itr.hasNext()) {
			node = itr.next();

			// Get related facts
			nodeArr = node.get("relatedFacts");
			nodeItr = nodeArr.iterator();
			children.clear();
			while (nodeItr.hasNext()) {children.add(nodeItr.next().asString());}
			children.sort();

			// Construct and store combo
			combo = new Combo(new Array<String>(children), node.getString("overwrite"),
					node.getString("comboSummary"), node.getInt("comboStressDamage"));
			combos.add(combo);
		}

		// Initialize other values
		stress = 0;
		suspicion = 0;
		naturallySuspiciousCheck = false;
		state = TargetState.UNAWARE;
		countdown = paranoia;
		rand = new Random();
	}

	/**
	 * Helper function that reads a JSON value for connector coords and returns them in the correct format
	 *
	 * @param connectorCoordsArr	The JSON value to parse
	 * @return						The JSON value, correctly parsed
	 */
	private Array<Array<Vector2>> readConnectorCoords(JsonValue connectorCoordsArr) {
		JsonValue.JsonIterator itr;
		JsonValue.JsonIterator itr2;
		// Initialize JSON array of coordinates in a path
		JsonValue pathArr;
		// Initialize array for a single coordinate
		int[] coordArr;
		// Initialize actual array for coordinates in a path
		Array<Vector2> path;

		// Initialize actual array of paths
		Array<Array<Vector2>> connectorPaths = new Array<>();

		// Iterate through JSON array of paths
		itr = connectorCoordsArr.iterator();
		while (itr.hasNext()){
			// Gets array of coordinates in a path
			// Reset coords
			path = new Array<>();

			// Go through coords in path
			pathArr = itr.next();
			itr2 = pathArr.iterator();
			while (itr2.hasNext()) {
				// Get coordinate from JSON
				coordArr = itr2.next().asIntArray();
				// Save coordinate
				path.add(new Vector2(coordArr[0],coordArr[1]));
			}

			// Add completed path to array of paths
			connectorPaths.add(path);
		}

		return connectorPaths;
	}

	/**
	 * Helper function that reads a JSON value for connector types and returns them in the correct format
	 *
	 * @param connectorTypesArr	The JSON value to parse
	 * @return					The JSON value, correctly parsed
	 */
	private Array<Array<String>> readConnectorTypes(JsonValue connectorTypesArr) {
		JsonValue.JsonIterator itr;
		JsonValue.JsonIterator itr2;
		// Initialize JSON array of coordinates in a path
		JsonValue pathArr;
		// Initialize actual array for connector types in a path
		Array<String> typesPath;
		// Initialize connectorTypes
		Array<Array<String>> connectorTypes = new Array<>();

		// Iterate through JSON array of connector types
		itr = connectorTypesArr.iterator();
		while (itr.hasNext()){
			// Gets array of types in a path
			// Reset types path
			typesPath = new Array<>();

			// Go through types in path
			pathArr = itr.next();
			itr2 = pathArr.iterator();
			while (itr2.hasNext()) {
				// Get type from JSON and store in the path
				typesPath.add(itr2.next().asString());
			}

			// Add completed path to array of paths
			connectorTypes.add(typesPath);
		}

		return connectorTypes;
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
	 * Returns the first connector coordinates of the target.
	 *
	 * Array of locations of first connections in isometric coordinates.
	 *
	 * @return the target's first connector coordinates.
	 */
	public Array<Vector2> getFirstConnectorCoords(){
		// TODO: doesn't discriminate currently between which path goes to which node, which makes animations impossible
		Array<Vector2> allCoords = new Array<>();
		// Got through each coordinate in firstConnectorPaths and add to an array of all coordinates
		for (Array<Vector2> path : firstConnectorPaths) {
			for (Vector2 coord : path) {
				allCoords.add(coord);
			}
		}
		return allCoords;
	}

	/**
	 * Returns the first connector types of the target.
	 *
	 * Array of types of first connections.
	 *
	 * @return the target's first connector types.
	 */
	public Array<String> getFirstConnectorTypes(){
		// TODO: doesn't discriminate currently between which path goes to which node, which makes animations impossible
		Array<String> allTypes = new Array<>();
		// Got through each coordinate in firstConnectorTypes and add to an array of all types
		for (Array<String> path : firstConnectorTypes) {
			for (String type : path) {
				allTypes.add(type);
			}
		}
		return allTypes;
	}

	/**
	 * Returns coordinates of connectors coming from fact
	 *
	 * @param fact the name of the fact
	 * @return a list of coordinates that represent connections out of fact
	 */
	public Array<Vector2> getConnectorCoordsOf(String fact){
		FactNode f = getFactNode(fact);
		return f.getConnectorCoords();
	}

	/**
	 * Returns the orientations of the connectors coming from fact
	 *
	 * @param fact the name of the fact
	 * @return	a list of types that represent connections out of fact, in the same order as getConnectorCoordsOf(FactNode fact)
	 */
	public Array<String> getConnectorTypesOf(String fact){
		FactNode f = getFactNode(fact);
		return f.getConnectorTypes();
	}

	/**
	 * Returns the connectors going from a FactNode to a given child
	 *
	 * @param node		Name of the FactNode to find the path to a child for
	 * @param child		Name of a child to get the path to
	 * @return			An array of connectors from the given node to the given child
	 */
	public Array<Connector> getPath(String node, String child){
		return getFactNode(node).getPathToChild(child);
	}


	/**
	 * Returns the names of this target's neighbors.
	 * 
	 * The neighbors of a target are the other targets that are connected to this one.
	 * Suspicion is contagious and can spread between adjacent targets. Names are formatted
	 * as first then last name, with a space in between.
	 *
	 * @return array of neighboring targets' names.
	 */
	public Array<String> getNeighbors() {
		return neighbors;
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
	 * Returns the list of names of the first nodes in this target's pod.
	 * 
	 * The first nodes are the ones that are visible immediately after the target is spawned in. This DOES NOT
	 * return the names of all the nodes in the pod.
	 * 
	 * Nodes are accounts, devices, etc. that are associated with the target and give more information about
	 * the target upon scanning. A node's name is an internal keyword used to identify a node, which can be
	 * passed into other TargetModel methods to achieve different things.
	 *
	 * @return		Array of names of the first nodes in this target's pod.
	 */
	public Array<String> getFirstNodes() {
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

//	/**
//	 * Returns the filepath to the location of the map assets for the node.
//	 *
//	 * The filepath leads to a folder where all the frames for a node are stored.
//	 *
//	 * @param name   	Name of the fact whose node's assets we want
//	 * @return 			Filepath to folder of node assets
//	 */
//	public String getNodeAssetPath(String name) {
//		return getFactNode(name).getAssetPath();
//	}

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
	 * Returns list of names of child nodes for the given node.
	 * 
	 * A given node's children are the nodes that are made visible when the given node is scanned.
	 * 
	 * @param name   	Name of the node whose children we want
	 * @return 			List of the names of children of the given node
	 */
	public Array<String> getChildren(String name) {
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
	 * Used to harass the target.
	 *
	 * Target stress and suspicion are increased by a low amount.
	 *
	 * Returns the amount of damage dealt.
	 *
	 * @return	Amount of damage to be dealt to the target
	 */
	public int harass() {
		// Increase target's suspicion by a low amount
		suspicion += randInRange(SUSPICION_LOW, 50);
		naturallySuspiciousCheck = true;
		// Return low amount of stress damage to deal to target
		return randInRange(5, 50);
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
	public int threaten(String fact) {
		int stressDmg = getFactNode(fact).getTargetStressDmg();
		// Increase target's suspicion by a low amount
		suspicion += randInRange(SUSPICION_LOW, 25);
		naturallySuspiciousCheck = true;
		// If fact deals threaten damage above a critical threshold
		if (stressDmg > 5) {
			// Deal stress damage to target
			addStress(stressDmg);
			// Move target to threatened
			state = TargetState.THREATENED;
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
			state = TargetState.PARANOID;
			// Reset countdown to next Paranoia check
			countdown = paranoia;
		}
		// Return amount of damage dealt
		return stressDmg;
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