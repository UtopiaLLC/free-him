import java.util.List;
import java.util.ArrayList;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.Vector2;

public class WorldModel {

	// Player object
	private Player player;

	// Map of targets and their names
	private Map<String, Target> targets;

	// Map of visible factnodes
	private Map<String, Array<String>> to_display;

	// Map of interactable (hackable or scannable) factnodes 
	private Map<String, Array<String>> to_interact;

	// Map from factnode identifiers to descriptions
	// Modified for combos
	private Map<String, Map<String, String>> summary;

	// Map of target world coordinates
	private Map<String, Vector2> targetCoords;

	// Number of days elapsed from start of game
	private int n_days;

	/** Enumeration representing the game's current state */
	protected enum GAMESTATE{
		/** Player has won the game */
		WIN,
		/** Player had lost the game */
		LOSE,
		/** Player is still playing the game. Welcome to the game */
		ONGOING
	};

	public World(Array<String> targetJsons, Map<String, Vector2> targetCoords) {
		player = new Player();
		this.targetCoords = targetCoords;
		targets = new Array<TargetModel>();

		to_display = new Map<String, List<String>>();
		to_interact = new Map<String, List<String>>();
		for(Target t : targetJsons) {
			targets.add(t.getName(), t);
			summary.add(t.getName(), new Map<String, String>());
			to_display.set(t.getName(), new Array<String>());
			// to_interact.addAll(t.getFirstNodes());

			for(String fact: t.getFirstNodes()){
				to_display.get(t.getName()).add(fact);
				summary.get(t.getName()).add(fact, t.getSummary(fact));
			}
		}

		n_days = 0;
	}

	/**
	 * Returns the current state of this world.
	 * 
	 * State can be: WIN, LOSE, ONGOING
	 *
	 * @return the world's current state
	 */
	public GAMESTATE getGameState(){
		for(Target t: targets){
			if(t.getState() == TargetModel.TargetState.GAMEOVER):
				return GAMESTATE.LOSE;
		}

		if(!player.isLiving()){
			return GAMESTATE.LOSE;
		}else{
			boolean allDefeated = true;
			for(Target t: targets){
				if(t.getState() != TargetModel.TargetState.DEFEATED){
					allDefeated = false;
				}
			}

			if(allDefeated == true){
				return GAMESTATE.WIN;
			}
		}

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


	/************************************************* PLAYER METHODS *************************************************/

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


	

	public boolean scan(String fact) {}

}

