package com.adisgrace.games.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

/**
 * Connector inner class that represents part of a line between two nodes or a target and a node.
 *
 * Each connector has two parts: isometric coordinates of the grid that the connector is in, and a string
 * representing the directions that the connector points. The string contains some combination of the four letters
 * "ENSW," arranged in alphabetical order. The letters represent the following directions on the isometric grid:
 *      E = +x direction
 *      N = +y direction
 *      S = -y direction
 *      W = -x direction
 * where +x is the direction that would be the lower right on the screen, and +y is the direction that would be
 * upper right on the screen.
 *
 * For example, a connector with type "ENSW" would be a four-way connector. A connector with type "NS" would be
 * a straight line that runs from the upper right to lower right side of the grid tile, when viewed on a screen.
 */
public class Connector {
    /** Enumeration representing the direction components of a connector */
    public enum Direction {
        /** N = +y direction */
        N,
        /** E = +x direction */
        E,
        /** S = -y direction */
        S,
        /** W = -x direction */
        W
    };

    /** The coordinates of the connector in isometric space */
    public int xcoord;
    public int ycoord;
    /** The string of directions representing the type of connector */
    public String type;

    /** Paths for each type of connector */
    private static final String C_NORTH = "leveleditor/connectors/C_N_2.png";
    private static final String C_EAST = "leveleditor/connectors/C_E_2.png";
    private static final String C_SOUTH = "leveleditor/connectors/C_S_2.png";
    private static final String C_WEST = "leveleditor/connectors/C_W_2.png";

    /** Textures for each type of connector */
    public static final Texture TX_NORTH = new Texture(Gdx.files.internal(C_NORTH)),
            TX_EAST = new Texture(Gdx.files.internal(C_EAST)), TX_SOUTH = new Texture(Gdx.files.internal(C_SOUTH)),
            TX_WEST = new Texture(Gdx.files.internal(C_WEST));

    /** Constants for steps taken in the various direction a connector can go */
    private static final Vector2 N_STEP = new Vector2(0,1);
    private static final Vector2 E_STEP = new Vector2(1,0);
    private static final Vector2 S_STEP = new Vector2(0,-1);
    private static final Vector2 W_STEP = new Vector2(-1,0);

    /**
     * Constructor for a connector. Saves the location and the type.
     *
     * @param x     The x-coordinate of the Connector in isometric space.
     * @param y     The y-coordinate of the Connector in isometric space.
     * @param t     The type of the Connector, represented as a string of "ENSW" with the directions that the
     *              Connector runs in.
     */
    public Connector(int x, int y, String t) {
        xcoord = x;
        ycoord = y;
        type = t;
    }

    /**
     * Constructor for a connector. Saves the location and the type.
     *
     * @param coords    The coordinates of the Connector in isometric space.
     * @param t         The type of the Connector, represented as a string of "ENSW" with the directions that
     *                  the Connector runs in.
     */
    public Connector(Vector2 coords, String t) {
        xcoord = (int)coords.x;
        ycoord = (int)coords.y;
        type = t;
    }

    /**
     * Constructor for a connector. Saves the location and the type.
     *
     * @param coords    The coordinates of the Connector in isometric space.
     * @param d         The direction of a single-type connector.
     */
    public Connector(Vector2 coords, Direction d) {
        xcoord = (int)coords.x;
        ycoord = (int)coords.y;
        type = dirToString(d);

    }

    /**
     * Adds a direction to the type of this connector.
     *
     * @param d     The direction of the connector to add to the type.
     */
    public void addDirToType(Direction d) {
        type += dirToString(d);
    }

    /**
     * Returns the texture for a connector with the given direction.
     *
     * @param dir   A connector direction to get the texture for
     * @return      The texture for a connector with the given direction
     */
    public static Texture getTexture(Direction dir) {
        switch(dir) {
            case N:
                return TX_NORTH;
            case E:
                return TX_EAST;
            case S:
                return TX_SOUTH;
            case W:
                return TX_WEST;
            default:
                throw new RuntimeException("Connector can only be NESW");
        }
    }

    /**
     * Returns the texture for a connector with the given direction.
     *
     * @param dir   A connector direction to get the texture for, as a String
     * @return      The texture for a connector with the given direction
     */
    public static Texture getTexture(String dir) {
        return getTexture(toDir(dir));
    }

    /**
     * Converts a Direction to the corresponding string.
     *
     * @param dir   The direction of the connector to convert to a string.
     * @return      The direction as a string.
     */
    public static String dirToString(Direction dir) {
        switch(dir) {
            case N:
                return "N";
            case E:
                return "E";
            case S:
                return "S";
            case W:
                return "W";
        }
        // Should never get here
        throw new RuntimeException("Direction is not a direction");
    }

    /**
     * Converts a string to the corresponding Direction.
     *
     * @param dir   The string representing a direction to convert to a Direction.
     * @return      The string as a direction.
     */
    public static Direction toDir(String dir) {
        switch(dir) {
            case "N":
                return Direction.N;
            case "E":
                return Direction.E;
            case "S":
                return Direction.S;
            case "W":
                return Direction.W;
        }
        // Should never get here
        throw new RuntimeException("String is not a direction");
    }

    /**
     * Converts a char to the corresponding Direction.
     *
     * @param dir   The char representing a direction to convert to a Direction.
     * @return      The char as a direction.
     */
    public static Direction toDir(char dir) {
        return toDir(String.valueOf(dir));
    }

    /**
     * Returns the direction opposite to the one that is given.
     *
     * @param dir   A direction
     * @return      The opposite direction
     */
    public static Direction oppositeDir(Direction dir) {
        switch(dir) {
            case N:
                return Direction.S;
            case E:
                return Direction.W;
            case S:
                return Direction.N;
            case W:
                return Direction.E;
        }
        // Should never get here
        throw new RuntimeException("Direction is not a direction");
    }



    /**
     * Returns a step in a direction based on the direction given in a connector.
     *
     * @param d     One of the characters in "NESW," representing a connector direction
     * @return      The vector that steps in the direction given by the input
     */
    public static Vector2 getDirVec(char d) {
        // Get directional step based on character
        switch(d) {
            case 'N':
                return N_STEP;
            case 'E':
                return E_STEP;
            case 'S':
                return S_STEP;
            case 'W':
                return W_STEP;
            default:
                throw new RuntimeException("Can only take in NESW");
        }
    }
}