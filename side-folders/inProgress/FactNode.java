import java.util.Random;
import java.util.ArrayList;

/**
 * A model class representing a ship.
 *
 * This class has more interesting methods other than setters and getters, but is
 * still a passive model.
 */
public class FactNode {
	/** String representing the shorthand name for a FactNode, used to refer to the node and as the dictionary key in TargetModel */
	private String name;

	/** String representing the device/account name ex. “Patrick’s Social Media Account.” */
	private String title;

	/** String representing info that is shown when the node is scanned. ex. “Two weeks ago, Patrick posted…” */
	private String content;

	/** String representing a summary of the important fact in the content. ex. “Patrick’s favorite color is red” */
	private String summary;

	/** List of names of nodes that are made visible when the current FactNode is scanned. */
	private ArrayList<String> children;

	// TODO: remove visible, scanned, exposed and relevant functions if not used
	
	/** Boolean representing whether the current FactNode has been made visible yet. */
	//private boolean visible;

	/** Boolean representing whether the current FactNode has been scanned yet. */
	//private boolean scanned;

	/** Boolean representing whether the current Fact has been exposed yet. */
	//private boolean exposed;

	/** X- and Y-coordinates of this FactNode relative to the target */
	private int nodeX;
	private int nodeY;

	/** Filepath to location of asset for this node */
	private String assetPath;

	/** Integer representing the amount of stress damage exposing this fact deals to the parent Target. */
	private int targetStressDmg;
	
	/** Integer representing the amount of stress damage scanning this Node deals to the player. */
	private int playerStressDmg;

	/**
	 * Creates a FactNode with the given parameters.
	 *
	 * @param n 		Name
	 * @param t			Title
	 * @param c			Content
	 * @param s			Summary
	 * @param cdren		List of node's children's names
	 * @param x			X-coordinate
	 * @param y			Y-coordinate
	 * @param aPath		Filepath to node's assets
	 * @param tsDmg		Target stress damage
	 * @param psDmg		Player stress damage
	 */
	public FactNode(String n, String t, String c, String s, ArrayList<String> cdren, int x, int y, String aPath, int tsDmg, int psDmg) {
		name = n;
		title = t;
		content = c;
		summary = s;
		children = cdren;
		nodeX = x;
		nodeY = y;
		assetPath = aPath;
		targetStressDmg = tsDmg;
		playerStressDmg = psDmg;
	}

	/**
	 * Returns list of names of this node's children
	 *
	 * @return list of node's children's names
	 */
	public ArrayList<String> getChildren(){
		return children;
	}

	/**
	 * Sets the visibility of the FactNode
	 *
	 * param value the visibility of the FactNode
	 *
	public void setVisible(boolean value){
		visible = value;
	}

	/**
	 * Returns the visibility of the FactNode
	 *
	 * return the visibility of the FactNode
	 *
	public boolean getVisible(){
		return visible;
	}

	/**
	 * Returns whether the current FactNode has been scanned yet
	 *
	 * return whether the current FactNode has been scanned yet
	 *
	public boolean getScanned(){
		return scanned;
	}

	/**
	 * Sets whether the FactNode has been scanned
	 *
	 * param value whether the FactNode has been scanned
	 *
	public void setScanned(boolean value){
		scanned = value;
	}

	/**
	 * Returns whether the current Fact has been exposed yet
	 *
	 * return whether the current Fact has been exposed yet
	 *
	public boolean getExposed(){
		return exposed;
	}

	/**
	 * Sets whether the Fact has been exposed
	 *
	 * param value whether the Fact has been exposed
	 *
	public void setExposed(boolean value){
		exposed = value;
	}
	*/

	/**
	 * Returns the name of the FactNode
	 *
	 * @return the name of the FactNode
	 */
	public String getName(){
		return name;
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
	 * Returns the x-coordinate of the FactNode
	 *
	 * @return the x-coordinate of the FactNode
	 */
	public int getX(){
		return nodeX;
	}

	/**
	 * Returns the y-coordinate of the FactNode
	 *
	 * @return the y-coordinate of the FactNode
	 */
	public int getY(){
		return nodeY;
	}

	/**
	 * Returns the filepath to the location of the assets for the FactNode
	 *
	 * @return filepath to FactNode's assets location
	 */
	public String getAssetPath(){
		return assetPath;
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