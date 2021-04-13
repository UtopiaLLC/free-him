package com.adisgrace.games.leveleditor;

import com.badlogic.gdx.math.Vector2;

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
    /** The coordinates of the connector in isometric space */
    int xcoord;
    int ycoord;
    /** The string of directions representing the type of connector */
    String type;

    /** Paths for each type of connector */
    public static final String C_NORTH = "leveleditor/connectors/C_N_1.png";
    public static final String C_EAST = "leveleditor/connectors/C_E_1.png";
    public static final String C_SOUTH = "leveleditor/connectors/C_S_1.png";
    public static final String C_WEST = "leveleditor/connectors/C_W_1.png";

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
}