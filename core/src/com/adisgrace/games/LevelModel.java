package com.adisgrace.games;

import java.util.HashMap;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonReader;

/**
 * "Level" representation.
 *
 * A level is one level consisting of multiple targets and their corresponding nodes.
 *
 * All information about individual levels is stored in a JSON, which is used to construct a
 * LevelModel.
 */

public class LevelModel {
    /** Each type is a string, where the string contains some combination of the four letters “N,” “E,” “S,” “W” */
    public enum Connector{
        /** +x direction */
        E,
        /** -x direction */
        W,
        /** -y direction */
        S,
        /** +y direction */
        N
    }
    /** The level's name */
    private String name;
    /** Pair of integers representing dimensions of level, stored as array of 2 ints. Automatic initialized as zeros */
    private int[] dims = new int[2];
    /** Array of targets in level, where each string is the name of a JSON where the target info is stored. */
    private Array<String> targets;
    /** Array of locations of targets in level, in the same order as [targets]. In isometric coordinates. */
    private Array<int[]> targetLocs;
    /** Array of locations of connections in level. In isometric coordinates. */
    private Array<int[]> connectorCoords;
    /** Array of types of the connectors in a level, where each type is an enum. */
    private Array<Connector> connectorTypes;


    /************************************************* Level CONSTRUCTOR **********************************************/
    public LevelModel(String levelJson){
        // placeholder
    }

    /**
     * Returns the name of this level.
     *
     * The level's name is represented as a first and last name with a space in between.
     *
     * @return the level's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the dimensions of this level.
     *
     * Pair of integers representing dimensions of level, stored as array of 2 ints.
     *
     * @return the dimensions of this level.
     */
    public int[] getDims() {
        return dims;
    }

    /**
     * Returns the targets included in this level.
     *
     * Array of targets in level, where each string is the name of a JSON where the target info is stored.
     *
     * @return the targets included in this level.
     */
    public Array<String> getTargets(){ return targets; }

    /**
     * Returns the locations of targets included in this level.
     *
     * Array of target locations in isometric coordinates, where each entry is an int array of size 2. targetLocs[i]
     * is the the location of targets[i].
     *
     * @return the target locations in isometric coordinates.
     */
    public Array<int[]> getTargetLocs(){ return targetLocs; }

    /**
     * Returns the connector coordinates included in this level.
     *
     * Array of connector coordinates in this level, where each entry is an int[2] which stores the connector's location
     * in isometric coordinates.
     *
     * @return the connector coordinates in this level.
     */
    public Array<int[]> getConnectorCoords(){ return connectorCoords; }

    /**
     * Returns the connector types for connectors included in this level.
     *
     * Array of connector types in this level, where each entry is an Connector which stores the connector's type,
     * connectorTypes[i] is the type of connector[i].
     *
     * @return the connector types in this level.
     */
    public Array<Connector> getConnectorTypes(){ return connectorTypes; }
}
