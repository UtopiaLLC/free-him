package com.adisgrace.games.leveleditor;

import com.adisgrace.games.util.Connector;
import com.adisgrace.games.util.Connector.Direction;
import com.adisgrace.games.leveleditor.LevelEditorConstants.StressRating;
import com.adisgrace.games.models.TraitModel.Trait;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;

import java.io.IOException;
import java.util.Scanner;

import static com.adisgrace.games.leveleditor.LevelEditorConstants.DEFAULT_MAX_STRESS;
import static com.adisgrace.games.leveleditor.LevelEditorConstants.DEFAULT_PARANOIA;

/**
 * Class for handling the save/load functionality of the level editor.
 *
 * Can take a level and create the corresponding JSON, and can take a
 * JSON and construct the corresponding level.
 */
public class LevelEditorModel {
    /** Name of the level */
    private String levelName;
    /** Hashmap of target/node names to their LevelTiles */
    private ArrayMap<String, LevelTile> levelTiles;
    /** Hashmap of coordinates and the names of the objects at that location */
    private ArrayMap<Vector2, Array<String>> levelMap;

    /** Vector cache to avoid initializing vectors every time */
    private Vector2 vec = new Vector2();

    /*********************************************** INNER CLASSES ***********************************************/

    /** Inner class representing a level tile at an isometric location */
    public class LevelTile {
        /** Isometric coordinates representing tile's location */
        protected float x;
        protected float y;
        /** The image itself stored at the tile */
        protected Image im;
    }

    /** Inner class representing a target as the level tile at an isometric location */
    public class TargetTile extends LevelTile {
        /** Name of the target */
        String name;
        /** Paranoia stat of target */
        int paranoia = DEFAULT_PARANOIA;
        /** Maximum stress of target */
        int maxStress = DEFAULT_MAX_STRESS;
        /** Traits of the target */
        Array<Trait> traits = new Array<>();

        /**
         * Constructor for a Target with the specified attributes.
         *
         * @param x             x-coordinate of target in isometric space
         * @param y             y-coordinate of target in isometric space
         * @param im            target image
         */
        TargetTile(float x, float y, Image im) {
            this.x = x;
            this.y = y;
            this.im = im;
        }
    }

    /** Inner class representing a node as the level tile at an isometric location */
    public class NodeTile extends LevelTile {
        /** Title of the node */
        String title;
        /** Whether or not node is locked */
        boolean locked;
        /** Content of the node */
        String content;
        /** Summary of the node */
        String summary;
        /** Target stress rating of the node, representing target stress damage */
        StressRating targetSR = StressRating.NONE;
        /** Player stress rating of the node, representing player stress damage */
        StressRating playerSR = StressRating.NONE;

        /**
         * Constructor for a NodeTile with the specified attributes.
         *
         * @param x             x-coordinate of node in isometric space
         * @param y             y-coordinate of node in isometric space
         * @param im            Node image
         * @param locked        Whether or not this node is locked
         */
        NodeTile(float x, float y, Image im, boolean locked) {
            this.x = x;
            this.y = y;
            this.im = im;
            this.locked = locked;
        }
    }

    /** Inner class representing a connector as the level tile at an isometric location */
    public class ConnectorTile extends LevelTile {
        /** Direction of the connector */
        Direction dir;

        /**
         * Constructor for a ConnectorTile with the specified attributes.
         *
         * @param x             x-coordinate of connector in isometric space
         * @param y             y-coordinate of connector in isometric space
         * @param im            Connector image
         * @param dir           Direction of connector as a string
         */
        ConnectorTile(float x, float y, Image im, String dir) {
            this.x = x;
            this.y = y;
            this.im = im;
            this.dir = Connector.toDir(dir);
        }
    }


    /*********************************************** CONSTRUCTOR ***********************************************/

    /**
     * Constructor for a LevelEditorModel.
     */
    public LevelEditorModel() {
        // Initialize hashmap of level tiles
        levelTiles = new ArrayMap<>();
        // Initialize map of level tiles at coordinates
        levelMap = new ArrayMap<>();
    }

    /*********************************************** GETTERS ***********************************************/
    /**
     * Returns the TargetTile with the given name.
     *
     * The given name must be that of a target, as we cast to TargetTile.
     * @param name      Name of target
     * @return          TargetTile of the target with the given name
     */
    public TargetTile getTargetTile(String name) {
        return (TargetTile) levelTiles.get(name);
    }

    /**
     * Returns the NodeTile with the given name.
     *
     * The given name must be that of a target, as we cast to TargetTile.
     * @param name      Name of target
     * @return          TargetTile of the target with the given name
     */
    public NodeTile getNodeTile(String name) {
        return (NodeTile) levelTiles.get(name);
    }

    /**
     * Returns the hashmap of target/node names to their LevelTiles.
     *
     * @return  Hashmap of target/node names to their LevelTiles.
     */
    public ArrayMap<String, LevelTile> getLevelTiles() {
        return levelTiles;
    }

    /**
     * Returns the hashmap of coordinates and the names of the objects at that location.
     *
     * @return  Hashmap of coordinates and the names of the objects at that location.
     */
    public ArrayMap<Vector2, Array<String>> getLevelMap() {
        return levelMap;
    }

    /**
     * Returns the name of the level.
     *
     * @return  Name of the level.
     */
    public String getLevelName() {
        return levelName;
    }

    /**
     * Sets the level name to the given name.
     *
     * @param name  Name to set the level's name to.
     */
    public void setLevelName(String name) {
        levelName = name;
    }

    /*********************************************** ADD/REMOVE TO LEVEL ***********************************************/

    /**
     * Adds the tile with the given name to the given location in the level map.
     * Given location is in isometric space.
     *
     * This function does not deal with levelTiles at all.
     *
     * @param name  Name of the image to add to the map.
     * @param x     x-coordinate of location to add tile at.
     * @param y     y-coordinate of location to add tile at.
     */
    private void addToMap(String name, float x, float y) {
        vec.set(x,y);
        // If coordinate already exists, add to existing array there
        if (levelMap.containsKey(vec)) {
            levelMap.get(vec).add(name);
        }
        // Otherwise, make a new one
        else {
            Array<String> arr = new Array<>();
            arr.add(name);
            Vector2 newVec = new Vector2(vec.x,vec.y);
            levelMap.put(newVec,arr);
        }
    }

    /**
     * Adds the tile with the given properties to the given location in the level.
     * Given location is in isometric space.
     *
     * This adds the node to levelTiles and levelMap.
     *
     * @param im    Image representing the tile's appearance.
     * @param x     x-coordinate of location to add tile at.
     * @param y     y-coordinate of location to add tile at.
     */
    public void addToLevel(Image im, float x, float y) {
        // Initialize level tile
        LevelTile lt;
        // Get identifier that can be used to identify type of tile
        String tilename = im.getName();
        String c = String.valueOf(tilename.charAt(0));
        // Create corresponding level tile depending on type
        switch (c) {
            case "0": // TARGET
                // Make target accordingly
                lt = new TargetTile(x,y,im);
                break;
            case "1": // UNLOCKED NODE
                // Make unlocked node accordingly
                lt = new NodeTile(x,y,im,false);
                break;
            case "2": // LOCKED NODE
                // Make locked node accordingly
                lt = new NodeTile(x,y,im,true);
                break;
            default: // CONNECTOR
                // Make connector accordingly (first character is direction)
                lt = new ConnectorTile(x,y,im,c);
                break;
        }
        // Add to levelTiles
        levelTiles.put(im.getName(),lt);
        // Map to location in level map
        addToMap(im.getName(),x,y);
    }

    /**
     * Removes the tile with the given name from the level map.
     *
     * This function does not deal with levelTiles at all.
     *
     * @param name  Name of the image to remove from the map.
     */
    public void removeFromMap(String name) {
        // Get the node's coordinates from levelTiles
        LevelTile lt = levelTiles.get(name);
        vec.set(lt.x,lt.y);
        // Remove node from levelMap using coordinates
        Array<String> nodes = levelMap.get(vec);
        nodes.removeValue(name,false);
        Vector2 newVec = new Vector2(vec.x,vec.y);
        levelMap.put(newVec, nodes);
    }

    /**
     * Removes the tile with the given name from the level.
     *
     * This removes the node from levelTiles and levelMap.
     *
     * @param name  Name of the image to remove from the map.
     */
    public void removeFromLevel(String name) {
        removeFromMap(name);
        levelTiles.removeKey(name);
    }

    /*********************************************** MODIFY LEVEL ***********************************************/

    /**
     * Updates the levelTile with the given name so that it is moved to the given location.
     *
     * Given coordinates must be in isometric space.
     *
     * @param name      Name of LevelTile to modify.
     * @param x         x-coordinate to change the LevelTile to.
     * @param y         y-coordinate to change the LevelTile to.
     */
    public void updateLevelTileLocation(String name, float x, float y) {
        // Update the map itself as well to account for the change in location
        removeFromMap(name);
        addToMap(name,x,y);

        // Update the array of level tiles
        LevelTile lt = levelTiles.get(name);
        lt.x = x;
        lt.y = y;
        levelTiles.put(name,lt);
    }

    /**
     * Updates the target stress rating of the node with the given name.
     *
     * Must be a valid node name.
     *
     * @param name      Name of LevelTile to modify.
     * @param targetSR  StressRating to change the node's target stress rating to.
     */
    public void updateNodeTargetStressRating(String name, StressRating targetSR) {
        NodeTile lt = (NodeTile) levelTiles.get(name);
        lt.targetSR = targetSR;
        levelTiles.put(name,lt);
    }

    /**
     * Updates the player stress rating of the node with the given name.
     *
     * Must be a valid node name.
     *
     * @param name      Name of LevelTile to modify.
     * @param playerSR  StressRating to change the node's player stress rating to.
     */
    public void updateNodePlayerStressRating(String name, StressRating playerSR) {
        NodeTile lt = (NodeTile) levelTiles.get(name);
        lt.playerSR = playerSR;
        levelTiles.put(name,lt);
    }

    /**
     * Updates the name of the target with the given name.
     *
     * Must be a valid target name.
     *
     * @param name      Name of LevelTile to modify.
     * @param tname     Name to change the target's name to.
     */
    public void updateTargetName(String name, String tname) {
        TargetTile tt = (TargetTile) levelTiles.get(name);
        tt.name = tname;
        levelTiles.put(name,tt);
    }

    /**
     * Updates the Target Paranoia of the node with the given name.
     *
     * Must be a valid node name.
     *
     * @param name      Name of LevelTile to modify.
     * @param targetParanoia  targetParanoia to change the target's targetParanoia to.
     */
    public void updateTargetParanoia(String name, int targetParanoia) {
        TargetTile lt = (TargetTile) levelTiles.get(name);
        lt.paranoia = targetParanoia;
        levelTiles.put(name,lt);
    }

    /**
     * Updates the Target MaxStress of the node with the given name.
     *
     * Must be a valid node name.
     *
     * @param name      Name of LevelTile to modify.
     * @param targetMaxStress  targetMaxStress to change the target's targetMaxStress to.
     */
    public void updateTargetMaxStress(String name, int targetMaxStress) {
        TargetTile lt = (TargetTile) levelTiles.get(name);
        lt.maxStress = targetMaxStress;
        levelTiles.put(name,lt);
    }

    /**
     * Updates the title of the node with the given name.
     *
     * Must be a valid node name.
     *
     * @param name      Name of LevelTile to modify.
     * @param title     Title to change the node's title to.
     */
    public void updateNodeTitle(String name, String title) {
        NodeTile lt = (NodeTile) levelTiles.get(name);
        lt.title = title;
        levelTiles.put(name,lt);
    }

    /**
     * Updates the content of the node with the given name.
     *
     * Must be a valid node name.
     *
     * @param name      Name of LevelTile to modify.
     * @param content  content to change the Node's content to.
     */
    public void updateNodeContent(String name, String content) {
        NodeTile lt = (NodeTile) levelTiles.get(name);
        lt.content = content;
        levelTiles.put(name,lt);
    }

    /**
     * Updates the summary of the node with the given name.
     *
     * Must be a valid node name.
     *
     * @param name      Name of LevelTile to modify.
     * @param summary  summary to change the Node's summary to.
     */
    public void updateNodeSummary(String name, String summary) {
        NodeTile lt = (NodeTile) levelTiles.get(name);
        lt.summary = summary;
        levelTiles.put(name,lt);
    }

    /**
     * Updates the Target Traits of the node with the given name.
     *
     * Must be a valid node name.
     *
     * @param name      Name of LevelTile to modify.
     * @param targetTraits  targetTraits to change the target's Traits to.
     */
    public void updateTargetTraits(String name, Array targetTraits) {
        TargetTile lt = (TargetTile) levelTiles.get(name);
        // If any of the selected traits is None, clear all target traits
        if (targetTraits.contains("none",false)) {
            lt.traits.clear();
        }
        else {
            lt.traits = targetTraits;
        }
        levelTiles.put(name,lt);
    }

    /**
     * Updates the LevelTile with the given name to have a new name.
     *
     * @param name      Name of LevelTile to modify.
     * @param newName   Name to change the LevelTile's name to.
     */
    public void updateLevelTileName(String name, String newName) {
        // Rename tile in levelMap
        removeFromMap(name);
        addToMap(newName,levelTiles.get(name).x,levelTiles.get(name).y);
        // Rename tile in levelTiles
        levelTiles.setKey(levelTiles.indexOfKey(name), newName);
    }

    /*********************************************** CLEAR AND UNDO ***********************************************/

    /**
     * Clears everything in the level.
     */
    public void clear() {
        // Remove all images from level
        for (LevelTile lt : levelTiles.values()) {
            lt.im.remove();
        }
        // Clear saved data about level tiles
        levelTiles.clear();
        levelMap.clear();
    }

    /**
     * Undoes the creation of the last image.
     *
     * Called if the undo button "Z" is pressed.
     */
    public void undo() {
        int size = levelTiles.size;
        // Do nothing if no images have been created
        if (size <= 0) {return;}
        // Remove last created image from array of created images and from level map
        Image lastIm = levelTiles.getValueAt(size - 1).im;
        removeFromLevel(lastIm.getName());
        // Actually remove image from screen
        lastIm.remove();
    }
}