import java.util.List;
import java.util.ArrayList;

public class WorldModel {

	// Player object
	private Player player;

	// List of visible targets
	private List<Target> targets;

	// List of visible factnodes
	private List<String> to_display;

	// List of interactable (hackable or scannable) factnodes 
	private List<String> to_interact;

	// Map from factnode identifiers to descriptions
	// Modified for combos
	private Map<String, String> summary;

	// Number of days elapsed from start of game
	private int n_days;

	public World() {
		player = new Player();

		targets = TargetFactory.initialize();

		to_display = new ArrayList<String>();
		to_interact = new ArrayList<String>();
		for(Target t : targets) {
			to_display.addAll(t.getFirstNodes());
			to_interact.addAll(t.getFirstNodes());
		}

		n_days = 0;
	}



	/**
		Skill functions
		Return true if game is still active, false if win or loss
	*/

	public boolean overwork() {

	}

	public boolean 

	public boolean scan(String fact) {}

}

