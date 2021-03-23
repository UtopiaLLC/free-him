import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Random;

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

	/** Constant for inverse Paranoia check, made every (INV_PARANOIA_CONSTANT - paranoia) turns */
	private static final int INV_PARANOIA_CONSTANT = 5;
	/** Instance of Random class, to be used whenever a random number is needed */
	private Random rand;

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
	 * Returns the list of nodes in this target's pod.
	 * 
	 * Nodes are accounts, devices, etc. that are associated with the target and give
	 * more information about the target upon scanning.
	 *
	 * @return array of names in this target's pod.
	 */
	public ArrayList<String> getFirstNodes() {
		return firstNodes;
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

	/**
	 * SKILL FUNCTIONS
	 */

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
	 * Marks fact as exposed, and returns the amount of stress damage dealt. If the
	 * fact deals nonzero stress damage, moves target to Paranoid.
	 * 
	 * @param fact	Name of the node where the to-be-exposed fact is stored
	 * @return 		Amount of stress damage dealt by exposing with fact
	 */
	public int expose(String fact) {
		FactNode factNode = getFactNode(fact);
		// Set fact to exposed
		factNode.setExposed(true);
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

	/**
	 * TODO: a whole bunch of getters for a FactNode in podDict
	 * 
	 * getChildren(string nodeName), returns names of children nodes
	 * getTitle(string nodeName), returns title of node
	 * scan
	 * - return content, summary
	 * getNodes(), returns array of names
	 */


}