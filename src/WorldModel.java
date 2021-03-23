import java.util.List;
import java.util.ArrayList;

public class WorldModel {

	private Player player;
	private List<Target> targets;

	// List of infonodes that can be displayed
	private List<InfoNode> to_display;
	// List of infonodes that can be interacted with (hacked or scanned)
	private List<InfoNode> to_interact;

	private int n_days;

	public World() {
		player = new Player();

		targets = TargetFactory.initialize();

		to_display = new ArrayList<InfoNode>();
		to_interact = new ArrayList<InfoNode>();
		for(Target t : targets) {
			to_display.addAll(t.displayableNodes());
			to_interact.addAll(t.interactableNodes());
		}

		n_days = 0;
	}

}
