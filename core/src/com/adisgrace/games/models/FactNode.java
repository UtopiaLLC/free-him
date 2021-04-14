package com.adisgrace.games.models;

import com.badlogic.gdx.utils.Array;

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

	/** List of names of nodes that are made visible when the current FactNode is scanned. */
	private Array<String> children;

	/** X- and Y-coordinates of this FactNode relative to the target */
	private int nodeX;
	private int nodeY;

	/** Boolean representing whether or not the node is locked. */
	private boolean locked;

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
	 * @param l	Whether the node is locked or not
	 * @param tsDmg		Target stress damage
	 * @param psDmg		Player stress damage
	 */
	public FactNode(String n, String t, String c, String s, Array<String> cdren, int x, int y, boolean l, int tsDmg, int psDmg) {
		nodeName = n;
		title = t;
		content = c;
		summary = s;
		children = cdren;
		nodeX = x;
		nodeY = y;
		locked = l;
		targetStressDmg = tsDmg;
		playerStressDmg = psDmg;
	}

	/**
	 * Returns list of names of this node's children
	 *
	 * @return list of node's children's names
	 */
	public Array<String> getChildren(){
		return children;
	}

	/**
	 * Returns whether the FactNode is locked or not
	 */
	public boolean getLocked(){
		return locked;
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
}