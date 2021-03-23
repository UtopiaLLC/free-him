import java.util.Random;
import java.util.ArrayList;

/**
 * A model class representing a ship.
 *
 * This class has more interesting methods other than setters and getters, but is
 * still a passive model.
 */
public class FactNode {
	/** String representing the device/account name ex. “Patrick’s Social Media Account.” */
	private String title;

	/** String representing info that is shown when the node is scanned. ex. “Two weeks ago, Patrick posted…” */
	private String content;

	/** String representing a summary of the important fact in the content. ex. “Patrick’s favorite color is red” */
	private String summary;

	/** ArrayList<FactNode> representing the nodes that are made visible when the current FactNode is scanned. */
	private ArrayList<FactNode> children;

	/** Boolean representing whether the current FactNode has been made visible yet. */
	private boolean visible;

	/** Boolean representing whether the current FactNode has been scanned yet. */
	private boolean scanned;

	/** Integer representing the amount of stress damage exposing this fact deals to the parent Target. */
	private int targetStressDmg;
	
	/** Integer representing the amount of stress damage scanning this Node deals to the player. */
	private int playerStressDmg;

	/**
	 * Create FactNode with given json.
	 *
	 * @param path The filepath of the json to parse.
	 */
	public FactNode(String path) {
		// Not implemented
	}

	/**
	 * Sets the visibility of the FactNode
	 *
	 * @param value the visibility of the FactNode
	 */
	public void setVisible(boolean value){
		visible = value;
	}

	/**
	 * Returns the visibility of the FactNode
	 *
	 * @return the visibility of the FactNode
	 */
	public boolean getVisible(){
		return visible;
	}

	/**
	 * Returns whether the current FactNode has been scanned yet
	 *
	 * @return whether the current FactNode has been scanned yet
	 */
	public boolean getScanned(){
		return scanned;
	}

	/**
	 * Sets whether the FactNode has been scanned
	 *
	 * @param value whether the FactNode has been scanned
	 */
	public void setScanned(boolean value){
		scanned = value;
	}

	/**
	 * Returns the title of the FactNode
	 *
	 * @return the title of the FactNode
	 */
	public String getTitle(){
		return title;
	}

	/**
	 * Returns the content of the FactNode
	 *
	 * @return the content of the FactNode
	 */
	public String getContent(){
		return content;
	}
	
	/**
	 * Returns the summary of the FactNode
	 *
	 * @return the summary of the FactNode
	 */
	public String getSummary(){
		return summary;
	}

	/**
	 * Returns the children of the FactNode
	 *
	 * @return the children of the FactNode
	 */
	public ArrayList<FactNode> getChildren(){
		return children;
	}

	/**
	 * Returns the amount of stress damage scanning this Node deals to the player
	 *
	 * @return the amount of stress damage scanning this Node deals to the player
	 */
	public int getPlayerStressDmg(){
		return playerStressDmg;
	}

	/**
	 * Returns the amount of stress damage exposing this fact deals to the parent Target
	 *
	 * @return the amount of stress damage exposing this fact deals to the parent Target
	 */
	public int getTargetStressDmg(){
		return targetStressDmg;
	}
}