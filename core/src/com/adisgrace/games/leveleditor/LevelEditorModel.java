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

/**
 * Class for handling the save/load functionality of the level editor.
 *
 * Can take a level and create the corresponding JSON, and can take a
 * JSON and construct the corresponding level.
 */
public class LevelEditorModel {
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
        int paranoia;
        /** Maximum stress of target */
        int maxStress;
        /** Traits of the target */
        Array<Trait> traits;

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
    private class ConnectorTile extends LevelTile {
        /** Direction of the connector */
        Direction dir;

        /**
         * Constructor for a ConnectorTile with the specified attributes.
         *
         * @param x             x-coordinate of connector in isometric space
         * @param y             y-coordinate of connector in isometric space
         * @param im            Connector image
         * @param dir           Direction of connector as a Direction
         */
        ConnectorTile(float x, float y, Image im, Direction dir) {
            this.x = x;
            this.y = y;
            this.im = im;
            this.dir = dir;
        }

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

    /*********************************************** SAVE AND LOAD ***********************************************/

    /** Values of placeholder constants */
    private static final String TARGET_NAME = "Torchlight Employee";
    private static final int TARGET_PARANOIA = 3;
    private static final int TARGET_MAXSTRESS = 100;
    private static final LevelEditorConstants.StressRating PS_DMG = LevelEditorConstants.StressRating.NONE;

    /**
     * Saves the level, producing a JSON from the existing model.
     */
    public void saveLevel() {
        // TODO: if there are overlapping connectors, delete the extras

        // Create a LevelEditorParser
        LevelEditorParser parser = new LevelEditorParser();

        // Get filename from user input in terminal
        // TODO: get user input not from terminal
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter desired level name: ");
        String fname = scan.nextLine();

        // Instantiate count of targets in level
        int targetCount = 1;

        // Go through each grid tile that contains LevelTiles
        String c;
        LevelTile lt;
        String connector;
        Array<Connector> connectors = new Array<>();
        for (Vector2 pos : levelMap.keys()) {
            connector = "";
            // For each LevelTile in this grid tile
            for (String tilename : levelMap.get(pos)) {
                // Get identifier that can be used to identify type of tile
                c = String.valueOf(tilename.charAt(0));
                // Get the actual LevelTile
                lt = levelTiles.get(tilename);

                // Initialize subclasses to potentially cast to
                NodeTile nt;
                TargetTile tt;
                ConnectorTile ct;
                switch (c) {
                    case "0": // TARGET
                        // Cast to TargetTile
                        tt = (TargetTile) lt;
                        // Make target accordingly
                        parser.make_target(fname+ " " + TARGET_NAME + " " + targetCount, TARGET_PARANOIA, 100, pos);
                        targetCount++;
                        break;
                    case "1": // UNLOCKED NODE
                        // Cast to NodeTile
                        nt = (NodeTile) lt;
                        // Make unlocked node accordingly
                        parser.make_factnode(nt.im.getName(), nt.targetSR, PS_DMG, false, pos);
                        break;
                    case "2": // LOCKED NODE
                        // Cast to NodeTile
                        nt = (NodeTile) lt;
                        // Make locked node accordingly
                        parser.make_factnode(nt.im.getName(), nt.targetSR, PS_DMG, true, pos);
                        break;
                    default: // CONNECTOR
                        // Cast to ConnectorTile
                        ct = (ConnectorTile) lt;
                        // Add the direction to the connector string
                        connector += c;
                        break;
                }
            }
            // Store new connector in array of connectors
            connectors.add(new Connector(pos,connector));
        }
        // Pass all connectors into the model
        parser.make_connections(connectors);

        // Make JSON
        try {
            // Don't need to include ".json"
            parser.make_level_json(fname);
        }
        catch(IOException e) {
            System.out.println("make_level_json failed");
        }

        System.out.println("Level " + fname + " Save Complete");
    }
}