package com.adisgrace.games.models;

import com.adisgrace.games.util.Connector;
import com.adisgrace.games.util.GameConstants;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;

/**
 * A model class representing a ship.
 *
 * This class has more interesting methods other than setters and getters, but is
 * still a passive model.
 */
public class FactNode {
	/** String representing the shorthand name for a FactNode, used to refer to the node and as the dictionary key in TargetModel */
	private String nodeName;

	/** String representing the device/account name ex. “Patrick’s Social Media Account.” */
	private String title;

	/** String representing info that is shown when the node is scanned. ex. “Two weeks ago, Patrick posted…” */
	private String content;

	/** String representing a summary of the important fact in the content. ex. “Patrick’s favorite color is red” */
	private String summary;

	/** Hashmap of children names to the paths of connectors leading to them. The children are the nodes that appear
	 * when this node is scanned. */
	private ArrayMap<String, Array<Connector>> children;

	/** X- and Y-coordinates of this FactNode relative to the target */
	private int nodeX;
	private int nodeY;

	/** stress rating of this specific fact node*/
	private GameConstants.StressRating stressRating;

	/** represents the amount of {NONE, LOW, MED, HIGH} stress ratings that belong in a subtree where this node is the top*/
	private int[] stressRatingsInSubTree = {0, 0, 0, 0};

	/** true if stressRatingsInSubTree accurately represents the contents of the entire subtree, false if not*/
	private boolean subTreeProcessed;

	/** Boolean representing whether or not the node is locked. */
	private boolean locked;

	/** Integer representing the amount of stress damage exposing this fact deals to the parent Target. */
	private int targetStressDmg;

	/** Integer representing the amount of stress damage scanning this Node deals to the player. */
	private int playerStressDmg;

	/************************************************* CONSTRUCTOR *************************************************/

	/**
	 * Creates a FactNode with the given parameters.
	 *
	 * @param name 		Name
	 * @param title		Title
	 * @param content	Content
	 * @param summary	Summary
	 * @param children  Children of the node
	 * @param x			X-coordinate
	 * @param y			Y-coordinate
	 * @param locked	Whether the node is locked or not
	 * @param tsDmg		Target stress damage
	 * @param psDmg		Player stress damage
	 */
	public FactNode(String name, String title, String content, String summary, ArrayMap<String, Array<Connector>> children,
					int x, int y, boolean locked, int tsDmg, int psDmg) {
		nodeName = name;
		this.title = title;
		this.content = content;
		this.summary = summary;
		this.children = children;
		nodeX = x;
		nodeY = y;
		this.locked = locked;
		targetStressDmg = tsDmg;
		playerStressDmg = psDmg;

		//reads targetstressdmg and assigns the corresponding stress rating, and then tallies itself in the subtree ratings
		if(targetStressDmg < 5){
			stressRating = GameConstants.StressRating.NONE;
			stressRatingsInSubTree[0]++;
		}else if(targetStressDmg < 10){
			stressRating = GameConstants.StressRating.LOW;
			stressRatingsInSubTree[1]++;
		}else if(targetStressDmg < 15){
			stressRating = GameConstants.StressRating.MED;
			stressRatingsInSubTree[2]++;
		}else{
			stressRating = GameConstants.StressRating.HIGH;
			stressRatingsInSubTree[3]++;
		}

		//if there are no children, then stressRatingsInSubTree already full represents this subtree
		subTreeProcessed = children.isEmpty();

	}

	/************************************************* GETTERS/SETTERS *************************************************/

	/**
	 * Returns hashmap of child names and the corresponding paths to those children.
	 *
	 * @return hashmap of this node's children's names and the corresponding paths to those children
	 */
	public ArrayMap<String, Array<Connector>> getChildren(){
		return children;
	}

	/**
	 * Returns whether the FactNode is locked or not
	 */
	public boolean getLocked(){
		return locked;
	}

	/**
	 * Set whether the FactNode is locked
	 * @param locked locked state
	 */
	public void setLocked(boolean locked){
		this.locked = locked;
	}

	/**
	 * Returns the name of the FactNode
	 *
	 * @return the name of the FactNode
	 */
	public String getNodeName(){
		return nodeName;
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
	 * Sets the content of the FactNode
	 * @param content the content of the FactNode
	 */
	public void setContent(String content){
		this.content = content;
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
	 * Sets the summary of the FactNode
	 *
	 * @param sum      the new summary of the FactNode
	 */
	public void setSummary(String sum){
		summary = sum;
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

	/**
	 * Sets the summary of the FactNode
	 *
	 * @param dmg      the new TargetStressDmg of the FactNode
	 */
	public void setTargetStressDmg(int dmg){
		targetStressDmg = dmg;
	}

	/**
	 * Returns the stress rating of the Factnode
	 * @return the stress rating for damange that would be dealt to the target
	 */
	public GameConstants.StressRating getStressRating(){
		return stressRating;
	}

	/**
	 * Returns the number of each stress rating that exists within this subtree
	 * @return {NONE, LOW, MED, HIGH}
	 */
	public int[] getSubTreeStressRatings(){
		return stressRatingsInSubTree;
	}

	/**
	 * Returns whether this subtree of nodes has been processed to have the correct set of stress ratings
	 * @return true if the subtree is processed
	 */
	public boolean isSubTreeProcessed(){
		return subTreeProcessed;
	}

}