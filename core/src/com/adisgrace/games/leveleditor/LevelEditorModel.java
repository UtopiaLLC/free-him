package com.adisgrace.games.leveleditor;

import com.adisgrace.games.util.Connector;
import com.adisgrace.games.util.Connector.Direction;
import static com.adisgrace.games.leveleditor.LevelEditorConstants.*;
import static com.adisgrace.games.util.GameConstants.*;
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
    /** Time limit of level */
    private int timeLimit;
    /** Hashmap of target/node names to their LevelTiles */
    private ArrayMap<String, LevelTile> levelTiles = new ArrayMap<>();
    /** Hashmap of coordinates and the names of the objects at that location */
    private ArrayMap<Vector2, Array<String>> levelMap = new ArrayMap<>();
    /** Count of number of images in this model */
    public int imgCount;

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
        /** Whether or not the target is generic */
        boolean isGeneric = true;
        /** Whether or not the target is male (true) or female (false) */
        boolean isMale;

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
         * @param isGeneric     Whether the target is generic
         * @param isMale        Whether the target is male
         */
        TargetTile(float x, float y, String name, int paranoia, int maxStress, boolean isGeneric, boolean isMale) {
            this.x = x;
            this.y = y;
            this.name = name;
            this.paranoia = paranoia;
            this.maxStress = maxStress;
            this.isGeneric = isGeneric;
            this.isMale = isMale;

            // Set target image to the default target image
            im = new Image(NODE_TEXTURES[TARGET_LOW]);
            // Ensure image name is in correct format
            im.setName("0" + name + imgCount);
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
        boolean locked;
        /** Content of the node */
        String content = "";
        /** Summary of the node */
        String summary = "";
        /** Target stress rating of the node, representing target stress damage */
        StressRating targetSR = StressRating.NONE;
        /** Player stress rating of the node, representing player stress damage */
        StressRating playerSR = StressRating.NONE;
        /** Whether or not the node is generic */
        boolean isGeneric = true;

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
         * @param x             x-coordinate of node in isometric space
         * @param y             y-coordinate of node in isometric space
         * @param im            Node image
         * @param locked        Whether or not this node is locked
         * @param isGeneric     Whether or not this node is generic
         */
        NodeTile(float x, float y, Image im, boolean locked, boolean isGeneric) {
            this.x = x;
            this.y = y;
            this.im = im;
            this.locked = locked;
            this.isGeneric = isGeneric;

            tileType = TileType.NODE;
        }

        /**
         * Constructor for a NodeTile with the specified attributes.
         *
         * Note that we do not reuse the name, in case targets from different levels are combined
         * and their networks have nodes that share the same names.
         *
         * @param x             x-coordinate of node in isometric space
         * @param y             y-coordinate of node in isometric space
         * @param title         Title of the node
         * @param locked        Whether or not this node is locked
         * @param content       Content of the node
         * @param summary       Summary of the node
         * @param targetSR      Target stress rating of node
         * @param playerSR      Player stress rating of node
         * @param isGeneric     Whether or not this node is generic
         */
        NodeTile(float x, float y, String title, boolean locked, String content, String summary,
                 StressRating targetSR, StressRating playerSR, boolean isGeneric) {
            this.x = x;
            this.y = y;
            this.title = title;
            this.locked = locked;
            this.content = content;
            this.summary = summary;
            this.targetSR = targetSR;
            this.playerSR = playerSR;
            this.isGeneric = isGeneric;

            // Set image to the default image for the relevant node
            im = new Image(NODE_TEXTURES[locked ? LOCKED_LOW : UNLOCKED_LOW]);
            // Set image name to be the node type, the string "Node," and a unique number
            // Can't reuse the old name in case multiple targets have nodes with the same name
            im.setName((locked ? 2 : 1) + "Node" + imgCount);

            tileType = TileType.NODE;
        }

        /**
         * Returns the title of the node, accounting for whether or not it's generic.
         *
         * If the node is generic, returns the relevant generic title rather than the saved custom title.
         *
         * @return  The title of this node
         */
        public String getTitle() {
            return getTitle(false, this.isGeneric);
        }

        /**
         * Returns the title of the node, accounting for whether or not it's generic and whether the output
         * will be written to a JSON.
         *
         * If the node is generic, returns the relevant generic title rather than the saved custom title.
         *
         * If this function is being called to write to the saved JSON, makes it JSON-friendly first.
         *
         * @param forSave   Whether this function is being called so the string can be written to a JSON.
         * @param isGeneric Whether this node is generic (based on whether the parent target is)
         * @return          The title of this node
         */
        public String getTitle(boolean forSave, boolean isGeneric) {
            String tempTitle = title;
            // Format quotes correctly if it's being returned to write to the JSON
            if (forSave) tempTitle.replaceAll("\"", "\\\\\"");
            return isGeneric ? GENERIC_TITLE : tempTitle;
        }

        /**
         * Returns the content of the node, accounting for whether or not it's generic.
         *
         * If the node is generic, returns the relevant generic content rather than the saved custom content.
         *
         * @return  The content of this node
         */
        public String getContent() {
            return getContent(false, this.isGeneric);
        }

        /**
         * Returns the content of the node, accounting for whether or not it's generic and whether the output
         * will be written to a JSON.
         *
         * If the node is generic, returns the relevant generic content rather than the saved custom content.
         *
         * If this function is being called to write to the saved JSON, makes it JSON-friendly first.
         *
         * @param forSave   Whether this function is being called so the string can be written to a JSON.
         * @param isGeneric Whether this node is generic (based on whether the parent target is)
         * @return          The content of this node
         */
        public String getContent(boolean forSave, boolean isGeneric) {
            String tempContent = content;
            // Formats newlines and quotes correctly for the JSON
            if (forSave) {
                tempContent.replaceAll("\r\n", System.lineSeparator()).replaceAll("\n", System.lineSeparator()).
                        replaceAll("\"", "\\\\\"");
            }
            return isGeneric ? getGenericContent(targetSR) : tempContent;
        }

        /**
         * Returns the summary of the node, accounting for whether or not it's generic.
         *
         * If the node is generic, returns the relevant generic summary rather than the saved custom summary.
         *
         * @return  The summary of this node
         */
        public String getSummary() {
            return getSummary(false, this.isGeneric);
        }

        /**
         * Returns the summary of the node, accounting for whether or not it's generic and whether the output
         * will be written to a JSON.
         *
         * If the node is generic, returns the relevant generic summary rather than the saved custom summary.
         *
         * If this function is being called to write to the saved JSON, makes it JSON-friendly first.
         *
         * @param forSave   Whether this function is being called so the string can be written to a JSON.
         * @param isGeneric Whether this node is generic (based on whether the parent target is)
         * @return          The summary of this node
         */
        public String getSummary(boolean forSave, boolean isGeneric) {
            String tempSummary = summary;
            // Formats newlines and quotes correctly for the JSON
            if (forSave) tempSummary.replaceAll("\n", "\\\\n").replaceAll("\"", "\\\\\"");
            return isGeneric ? getGenericSummary(targetSR) : tempSummary;
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
            // Set image name to be direction plus unique identifier
            im.setName(dir + imgCount);
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
     * @param isGeneric Whether the target is generic
     * @param isMale    Whether the target is male (true) or female (false)
     */
    public void loadTarget(int x, int y, String name, int paranoia, int maxStress, boolean isGeneric, boolean isMale) {
        // Create the TargetTile and add to the level
        addToLevel(new TargetTile(x, y, name, paranoia, maxStress, isGeneric, isMale));
    }

    /**
     * Loads a node with the given parameters into the level.
     *
     * Note that we can use the node's name as is since we know is unique among all nodes in the level,
     * because it had to be when the level was first created.
     *
     * @param x             x-coordinate of node in isometric space
     * @param y             y-coordinate of node in isometric space
     * @param title         Title of the node to load into level
     * @param locked        Whether or not this node is locked
     * @param content       Content of the node to load into level
     * @param summary       Summary of the node to load into level
     * @param targetSR      Target stress rating of node to load into level
     * @param playerSR      Player stress rating of node to load into level
     * @param isGeneric     Whether the node is generic
     */
    public void loadNode(int x, int y, String title, boolean locked, String content, String summary,
                         StressRating targetSR, StressRating playerSR, boolean isGeneric) {
        // Create the NodeTile and add to the level
        addToLevel(new NodeTile(x, y, title, locked, content, summary, targetSR, playerSR, isGeneric));
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
     * Returns the time limit of the level.
     *
     * @return  Time limit of the level.
     */
    public int getLevelTimeLimit() {
        return timeLimit;
    }

    /**
     * Sets the level time limit to the given time limit.
     *
     * @param timeLimit  Time limit to set the level's time limit to.
     */
    public void setLevelTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

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
        // Increment image count
        imgCount++;
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
     * If it's a target/node, will default to generic.
     *
     * @param im    Image representing the tile's appearance.
     * @param x     x-coordinate of location to add tile at.
     * @param y     y-coordinate of location to add tile at.
     */
    public void addToLevel(Image im, float x, float y) {
        addToLevel(im,x,y,true);
    }

    /**
     * Adds the tile with the given properties to the given location in the level.
     * Given location is in isometric space.
     *
     * This adds the tile to levelTiles and levelMap.
     *
     * @param im        Image representing the tile's appearance.
     * @param x         x-coordinate of location to add tile at.
     * @param y         y-coordinate of location to add tile at.
     * @param isGeneric Whether this node/target should be generic.
     */
    public void addToLevel(Image im, float x, float y, boolean isGeneric) {
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
                lt = new NodeTile(x,y,im,false, isGeneric);
                break;
            case "2": // LOCKED NODE
                // Make locked node accordingly
                lt = new NodeTile(x,y,im,true, isGeneric);
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

    /*********************************************** UPDATE TARGET ***********************************************/

    /**
     * Updates the name of the target with the given name.
     *
     * Must be a valid target name.
     *
     * @param name      Name of target to modify.
     * @param tname     Name to change the target's name to.
     */
    public void updateTargetName(String name, String tname) {
        TargetTile tt = (TargetTile) levelTiles.get(name);
        tt.name = tname;
    }

    /**
     * Updates the Paranoia of the target with the given name.
     *
     * Takes in a String value for paranoia and handles accordingly if the string is empty.
     *
     * Must be a valid target name.
     *
     * @param name              Name of target to modify.
     * @param targetParanoia    targetParanoia to change the target's targetParanoia to, as a string.
     */
    public void updateTargetParanoia(String name, String targetParanoia) {
        // Initialize integer value for paranoia
        int paranoia;
        // If field was empty, set paranoia to default
        if (targetParanoia.equals("")) paranoia = DEFAULT_PARANOIA;
        // Otherwise, convert to int and use that
        else paranoia = Integer.parseInt(targetParanoia);

        TargetTile lt = (TargetTile) levelTiles.get(name);
        lt.paranoia = paranoia;
    }

    /**
     * Updates the MaxStress of the target with the given name.
     *
     * Takes in a String for max stress and handles accordingly if the string is empty.
     *
     * Must be a valid target name.
     *
     * @param name              Name of target to modify.
     * @param targetMaxStress   targetMaxStress to change the target's targetMaxStress to, as a string.
     */
    public void updateTargetMaxStress(String name, String targetMaxStress) {
        // Initialize integer value for max stress
        int maxStress;
        // If field was empty, set max stress to default
        if (targetMaxStress.equals("")) maxStress = DEFAULT_MAX_STRESS;
        // Otherwise, convert to int and use that
        else maxStress = Integer.parseInt(targetMaxStress);

        TargetTile lt = (TargetTile) levelTiles.get(name);
        lt.maxStress = maxStress;
    }

    /**
     * Updates the Traits of the target with the given name.
     *
     * Must be a valid target name.
     *
     * @param name          Name of target to modify.
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
    }

    /**
     * Updates the generic status of the target with the given name.
     *
     * Must be a valid target name.
     *
     * @param name      Name of target to modify.
     * @param isGeneric Whether the given target is generic or not.
     */
    public void updateTargetIsGeneric(String name, boolean isGeneric) {
        TargetTile lt = (TargetTile) levelTiles.get(name);
        lt.isGeneric = isGeneric;
    }

    /*********************************************** UPDATE NODE ***********************************************/

    /**
     * Updates the title of the node with the given name.
     *
     * Must be a valid node name.
     *
     * @param name      Name of node to modify.
     * @param title     Title to change the node's title to.
     */
    public void updateNodeTitle(String name, String title) {
        NodeTile nt = (NodeTile) levelTiles.get(name);
        nt.title = title;
    }

    /**
     * Updates the locked status of the node with the given value.
     *
     * Must be a valid node name.
     *
     * @param name      Name of node to modify.
     * @param locked    Whether this node is locked.
     */
    public void updateNodeLocked(String name, boolean locked) {
        NodeTile nt = (NodeTile) levelTiles.get(name);
        nt.locked = locked;
    }

    /**
     * Updates the content of the node with the given name.
     *
     * If the node is generic, does not overwrite what is currently saved.
     *
     * Must be a valid node name.
     *
     * @param name      Name of node to modify.
     * @param content   content to change the Node's content to.
     */
    public void updateNodeContent(String name, String content) {
        NodeTile nt = (NodeTile) levelTiles.get(name);
        if (nt.isGeneric) return;
        nt.content = content;
    }

    /**
     * Updates the summary of the node with the given name.
     *
     * If the node is generic, does not overwrite what is currently saved.
     *
     * Must be a valid node name.
     *
     * @param name      Name of node to modify.
     * @param summary   Summary to change the Node's summary to.
     */
    public void updateNodeSummary(String name, String summary) {
        NodeTile nt = (NodeTile) levelTiles.get(name);
        if (nt.isGeneric) return;
        nt.summary = summary;
    }

    /**
     * Updates the target stress rating of the node with the given name.
     *
     * Must be a valid node name.
     *
     * @param name      Name of node to modify.
     * @param targetSR  StressRating to change the node's target stress rating to.
     */
    public void updateNodeTargetStressRating(String name, StressRating targetSR) {
        NodeTile nt = (NodeTile) levelTiles.get(name);
        nt.targetSR = targetSR;
    }

    /**
     * Updates the player stress rating of the node with the given name.
     *
     * Must be a valid node name.
     *
     * @param name      Name of node to modify.
     * @param playerSR  StressRating to change the node's player stress rating to.
     */
    public void updateNodePlayerStressRating(String name, StressRating playerSR) {
        NodeTile nt = (NodeTile) levelTiles.get(name);
        nt.playerSR = playerSR;
    }

    /**
     * Updates the generic status of the node with the given name.
     *
     * Must be a valid node name.
     *
     * @param name      Name of node to modify.
     * @param isGeneric Whether the given node is generic or not.
     */
    public void updateNodeIsGeneric(String name, boolean isGeneric) {
        NodeTile nt = (NodeTile) levelTiles.get(name);
        nt.isGeneric = isGeneric;
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
