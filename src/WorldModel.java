import java.util.List;
import java.util.ArrayList;

public class WorldModel {

	private Player player;
	private List<Target> targets;

	// List of factnodes that can be displayed
	private List<FactNode> to_display;
	// List of factnodes that can be interacted with (hacked or scanned)
	private List<FactNode> to_interact;

	private int n_days;

	public World() {
		player = new Player();

		targets = TargetFactory.initialize();

		to_display = new ArrayList<FactNode>();
		to_interact = new ArrayList<FactNode>();
		for(Target t : targets) {
			for(String fact_id : t.firstNodes()) {
				to_display.add(t.getFactNode());
				to_interact.add(t.getFactNode());
			}
		}

		n_days = 0;
	}

}
