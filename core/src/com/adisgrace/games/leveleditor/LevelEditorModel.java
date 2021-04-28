package com.adisgrace.games.leveleditor;

import com.adisgrace.games.util.Connector;
import com.adisgrace.games.util.Connector.Direction;
import static com.adisgrace.games.leveleditor.LevelEditorConstants.*;
import com.adisgrace.games.models.TraitModel.Trait;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;

/**
 * Class for handling the save/load functionality of the level editor.
 *
 * Can take a level and create the corresponding JSON, and can take a
 * JSON and construct the corresponding level.
 */
public class LevelEditorModel {
    /** Name of the level */
    private String levelName;
    /** Dimensions of the level */
    public int levelWidth, levelHeight;
    /** Hashmap of target/node names to their LevelTiles */
    private ArrayMap<String, LevelTile> levelTiles = new ArrayMap<>();
    /** Hashmap of coordinates and the names of the objects at that location */
    private ArrayMap<Vector2, Array<String>> levelMap = new ArrayMap<>();

    /** Vector cache to avoid initializing vectors every time */
    private Vector2 vec = new Vector2();

    /*********************************************** CONSTRUCTOR ***********************************************/

    /**
     * Constructor for a LevelEditorModel.
     */
    public LevelEditorModel() {
    }

    /*********************************************** INNER CLASSES ***********************************************/
    /** Enum representing the type of a tile */
    public enum TileType {TARGET, NODE, CONNECTOR};

    /** Inner class representing a level tile at an isometric location */
    public class LevelTile {
        /** Isometric coordinates representing tile's location */
        protected float x;
        protected float y;
        /** The image itself stored at the tile */
        protected Image im;
        /** The type of the tile */
        protected TileType tileType;
    }

    /** Inner class representing a target as the level tile at an isometric location */
    public class TargetTile extends LevelTile {
        /** Name of the target */
        String name = "";
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
         * @param im            Target image
         */
        TargetTile(float x, float y, Image im) {
            this.x = x;
            this.y = y;
            this.im = im;

            tileType = TileType.TARGET;
        }

        /**
         * Constructor for a Target with the specified attributes.
         *
         * This constructor is called when loading a target into the level.
         *
         * @param x             x-coordinate of target in isometric space
         * @param y             y-coordinate of target in isometric space
         * @param name          Target's name
         * @param paranoia      Target's paranoia stat
         * @param maxStress     Target's max stress
         */
        TargetTile(float x, float y, String name, int paranoia, int maxStress) {
            this.x = x;
            this.y = y;
            this.name = name;
            this.paranoia = paranoia;
            this.maxStress = maxStress;

            // Set target image to the default target image
            im = new Image(NODE_TEXTURES[TARGET_LOW]);
            // Ensure image name is in correct format
            im.setName("0" + name);
            // Set scale
            im.setScale(0.5f);

            tileType = TileType.TARGET;
        }

        /**
         * Returns this target's traits converted into a single string.
         *
         * This is used when writing the traits to the JSON when saving the level.
         *
         * @return  Target's traits converted into a single string.
         */
        public String traitsAsString() {
            String finalString = "[";
            for (Trait trait : traits) {
                finalString += "\"" + TRAIT_OPTIONS_STRINGS[find(trait, TRAIT_OPTIONS)] + "\", ";
            }
            // If at least one trait was saved, trim the last comma
            if (finalString.length() > 3) {
                finalString = finalString.substring(0,finalString.length() - 2);
            }
            return finalString + "]";
        }
    }

    /** Inner class representing a node as the level tile at an isometric location */
    public class NodeTile extends LevelTile {
        /** Title of the node */
        String title = "";
        /** Whether or not node is locked */
        boolean locked = false;
        /** Content of the node */
        String content = "";
        /** Summary of the node */
        String summary = "";
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

            tileType = TileType.NODE;
        }

        /**
         * Constructor for a NodeTile with the specified attributes.
         *
         * Note that we know the node name will be unique among all nodes in the level
         * because it had to be when the level was first created.
         *
         * @param x             x-coordinate of node in isometric space
         * @param y             y-coordinate of node in isometric space
         * @param name          Node name, as in its unique identifier in the level
         * @param title         Title of the node
         * @param locked        Whether or not this node is locked
         * @param content       Content of the node
         * @param summary       Summary of the node
         * @param targetSR      Target stress rating of node
         * @param playerSR      Player stress rating of node
         */
        NodeTile(float x, float y, String name, String title, boolean locked, String content, String summary,
                 StressRating targetSR, StressRating playerSR) {
            this.x = x;
            this.y = y;
            this.title = title;
            this.locked = locked;
            this.content = content;
            this.summary = summary;
            this.targetSR = targetSR;
            this.playerSR = playerSR;

            // Set image to the default image for the relevant node
            im = new Image(NODE_TEXTURES[locked ? LOCKED_LOW : UNLOCKED_LOW]);
            // Set image name to be node name
            im.setName(name);
            // Set scale
            im.setScale(0.5f);

            tileType = TileType.NODE;
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

            tileType = TileType.CONNECTOR;
        }

        /**
         * Constructor for a ConnectorTile with the specified attributes.
         *
         * @param x             x-coordinate of connector in isometric space
         * @param y             y-coordinate of connector in isometric space
         * @param dir           Direction of connector as a string
         */
        ConnectorTile(float x, float y, String dir) {
            this.x = x;
            this.y = y;
            this.dir = Connector.toDir(dir);

            // Create image based on direction
            im = new Image(Connector.getTexture(this.dir));
            // Set image name to be direction
            im.setName(dir);
            // Set scale
            im.setScale(0.5f);

            tileType = TileType.CONNECTOR;
        }
    }

    /*********************************************** LOAD INTO LEVEL ***********************************************/
    /**
     * Loads a target with the given parameters into the level.
     *
     * @param x         x-coordinate of target in level
     * @param y         y-coordinate of target in level
     * @param name      Name of the target to load into level
     * @param paranoia  Paranoia stat of target to load into level
     * @param maxStress Max stress of target to load into level
     */
    public void loadTarget(int x, int y, String name, int paranoia, int maxStress) {
        // Create the TargetTile and add to the level
        addToLevel(new TargetTile(x, y, name, paranoia, maxStress));
    }

    /**
     * Loads a node with the given parameters into the level.
     *
     * Note that we can use the node's name as is since we know is unique among all nodes in the level,
     * because it had to be when the level was first created.
     *
     * @param x             x-coordinate of node in isometric space
     * @param y             y-coordinate of node in isometric space
     * @param name          Name of the node to load into level
     * @param title         Title of the node to load into level
     * @param locked        Whether or not this node is locked
     * @param content       Content of the node to load into level
     * @param summary       Summary of the node to load into level
     * @param targetSR      Target stress rating of node to load into level
     * @param playerSR      Player stress rating of node to load into level
     */
    public void loadNode(int x, int y, String name, String title, boolean locked, String content, String summary,
                         StressRating targetSR, StressRating playerSR) {
        // Create the NodeTile and add to the level
        addToLevel(new NodeTile(x, y, name, title, locked, content, summary, targetSR, playerSR));
    }

    /**
     * Loads a connector with the given parameters into the level.
     *
     * In this case, a Connector with a multidirectional type is broken up into a ConnectorTile for
     * each direction.
     *
     * @param x     x-coordinate of the connector
     * @param y     y-coordinate of the connector
     * @param type  Type of the connector
     */
    public void loadConnector(int x, int y, String type) {
        // Initialize string for each direction in this connector
        String dir;

        // Create a new connector for each direction in the type
        for (int k=0; k<type.length(); k++) {
            dir = String.valueOf(type.charAt(k));
            // Create the ConnectorTile and add to the level
            addToLevel(new ConnectorTile(x,y,dir));
        }
    }

    /*********************************************** GETTERS/SETTERS ***********************************************/
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

    /**
     * Returns the width of the level.
     *
     * @return  Width of the level.
     */
    public int getLevelWidth() {return levelWidth;}

    /**
     * Returns the height of the level.
     *
     * @return  Height of the level.
     */
    public int getLevelHeight() {return levelHeight;}

    /**
     * Sets the dimensions of the level to the given dimensions.
     *
     * @param width     The width to set the level width to.
     * @param height    The height to set the level height to.
     */
    public void setLevelDimensions(int width, int height) {
        levelWidth = width;
        levelHeight = height;
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
     * Adds the tile to the level.
     *
     * This adds the tile to levelTiles and levelMap.
     *
     * @param lt    LevelTile to add to the level.
     */
    public void addToLevel(LevelTile lt) {
        // Add to levelTiles
        levelTiles.put(lt.im.getName(),lt);
        // Map to location in level map
        addToMap(lt.im.getName(),lt.x,lt.y);
    }

    /**
     * Adds the tile with the given properties to the given location in the level.
     * Given location is in isometric space.
     *
     * This adds the tile to levelTiles and levelMap.
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
        addToLevel(lt);
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
