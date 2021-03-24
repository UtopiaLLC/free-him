import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Random;
import com.badlogic.gdx.math.Vector2;

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
	/**	Targets connected to the current target */
	private ArrayList<String> neighbors;
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
	/** Current state of target */
	private TargetState state;
	/** Dictionary representing the nodes that are in the same pod as the target, where a node can be accessed with its name */
	private Dictionary<String, FactNode> podDict;
	/** Array of node names representing the nodes that are immediately visible when the target first becomes available */
	private ArrayList<String> firstNodes;

	/**
	 * TODO: implement data structure for combos
	 * 
	 * A combo has four main properties:
	 * - relatedFacts: a list of FactNode names representing the facts that form a combo
	 * - overwrite: a String representing the FactNode name whose data should be overwritten when the combo is completed
	 * - comboSummary: a String representing the new summary that the summary of [overwrite] should be replaced with
	 * - comboStressDamage: an integer representing the new stress damage that the stress damage of [overwrite] should be replaced with
	 * 
	 * You can implement this however, but I think it might be easiest to make an inner class called Combo with these properties, then
	 * have an array of combos be a field of TargetModel.
	 */

	/** Constant for inverse Paranoia check, made every (INV_PARANOIA_CONSTANT - paranoia) turns */
	private static final int INV_PARANOIA_CONSTANT = 5;
	/** Instance of Random class, to be used whenever a random number is needed */
	private Random rand;
	/** Vector2 buffer, to be used instead of creating a new Vector2 every time */
	private Vector2 vec;

	/************************************************* TARGET CONSTRUCTOR *************************************************/

	/**
	 * Creates a new Target with the given JSON data.
	 * 
	 * All information about a target and their respective pod is stored in a JSON, that is
	 * passed into this constructor, where it is parsed. The relevant FactNodes are also
	 * created here.
	 *
	 * @param data		The JSON with all the target's data.
	 */
	public TargetModel() {
		// TODO

		countdown = paranoia;
		rand = new Random();
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
	 * Returns the names of this target's neighbors.
	 * 
	 * The neighbors of a target are the other targets that are connected to this one.
	 * Suspicion is contagious and can spread between adjacent targets. Names are formatted
	 * as first then last name, with a space in between.
	 *
	 * @return array of neighboring targets' names.
	 */
	public ArrayList<String> getNeighbors() {
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
		stress += s;
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
	 * @param s		Amount by which to increase target's suspicion
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
	public ArrayList<String> getFirstNodes() {
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
	public String[] getNodes() {
		// TODO: VSCode says this doesn't work for me
		String[podDict.size()] result;
		// Iterate through the list of FactNodes
		int index = 0;
		for(String key:podDict.keySet()){
			FactNode factNode = getFactNode(fact);
			result[index] = factNode.getName();
			index++;
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
	 * Returns the filepath to the location of the map assets for the node.
	 * 
	 * The filepath leads to a folder where all the frames for a node are stored.
	 * 
	 * @param name   	Name of the fact whose node's assets we want
	 * @return 			Filepath to folder of node assets
	 */
	public String getNodeAssetPath(String name) {
		return getFactNode(name).getAssetPath();
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
	 * Returns list of names of child nodes for the given node.
	 * 
	 * A given node's children are the nodes that are made visible when the given node is scanned.
	 * 
	 * @param name   	Name of the node whose children we want
	 * @return 			String[] of the names of children of the given node
	 */
	public String[] getChildren(String name) {
		// TODO: VSCode says this doesn't work for me
		FactNode factNode = getFactNode(name);
		// Get children of FactNode
		ArrayList<FactNode> children = factNode.getChildren();
		String[children.size()] result;
		// Filling up the result with names of children
		for(int i = 0; i < children.size(); i++){
			result[i] = children.get(i).getName();
		}
		return result;
	}

	/************************************************* SKILL METHODS *************************************************/

	/**
	 * Used to threaten the target with the fact stored at the given node.
	 * 
	 * If the fact can be used to threaten, moves the target to the Threatened state, resets the
	 * countdown to the next Paranoia check, increases stress accordingly, and returns the damage
	 * dealt by the fact. Otherwise, does nothing and returns 0.
	 * 
	 * @param fact	Name of the node where the threatening fact is stored
	 * @return 		Amount of stress damage dealt by threatening with fact
	 */
	public int threaten(String fact) {
		int stressDmg = getFactNode(fact).getTargetStressDmg();
		// If fact deals nonzero threaten damage
		if (stressDmg != 0) {
			// Move target to threatened
			state = TargetState.THREATENED;
			// Reset countdown to next Paranoia check
			countdown = paranoia;
		}
		// Return amount of stress damage dealt (can be 0)
		return stressDmg;
	}

	/**
	 * Used to expose a fact about the target, which is stored at the given node.
	 * 
	 * Returns the amount of stress damage dealt. If the fact deals nonzero stress
	 * damage, moves target to Paranoid.
	 * 
	 * @param fact	Name of the node where the to-be-exposed fact is stored
	 * @return 		Amount of stress damage dealt by exposing with fact
	 */
	public int expose(String fact) {
		FactNode factNode = getFactNode(fact);
		int stressDmg = factNode.getTargetStressDmg();
		// If exposing deals nonzero damage
		if (stressDmg != 0) {
			// Move target to Paranoid and reset countdown
			state = TargetState.PARANOID;
			countdown = paranoia;
		}
		// Return amount of expose damage dealt (can be 0)
		return stressDmg;
	}

	/************************************************* COMBO METHODS *************************************************/

	/**
	 * Checks if a given fact is part of a combo with any other of the given facts, and if so, updates
	 * the relevant information accordingly.
	 * 
	 * If a fact is part of a combo, then the node whose name is the "overwrite" property of the combo will
	 * have its summary and stress damage replaced with the summary and stress damage stored in the combo, and
	 * this function will return true. Otherwise, nothing happens and the function returns false.
	 * 
	 * If a fact is part of multiple combos, the combo that should be kept is the one with the most number of
	 * related facts that form that combo. For example, if a fact is part of a two-fact combo and a three-fact
	 * combo, the three-fact combo will be the one that applies.
	 * 
	 * @param newDmg	New value to set the stress damage of a fact to
	 * @param name		Name of fact to set the stress damage of
	 */
	public boolean checkForCombo(String name, ArrayList<String> facts) {
		// TODO
		// Note that if a fact is in fact part of a combo, after replacing the fact's summary and stress
		// damage with that of the combo, the combo should be deleted from this target. If there are multiple
		// combos that contain the fact, all the combos that are the same length or less should be deleted.

		// For example, say we have facts A, B, and C, with the combos AB, AC, and ABC, and A is "overwrite" for all three.
		// checkForCombo(A, [B]): A's summary and stressDmg are replaced with those of combo AB. Combos AB and AC are deleted.
		// checkForCombo(A, [B, C]): A's summary and stressDmg are replaced with those of combo ABC. Combos AB, AC, and ABC are deleted.
		return false;
	}
}