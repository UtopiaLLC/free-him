//package com.adisgrace.games.leveleditor;
//
//import com.adisgrace.games.*;
//import com.adisgrace.games.util.Connector;
//import com.adisgrace.games.util.Connector.*;
//import com.adisgrace.games.leveleditor.LevelEditorModel.StressRating;
//
//import com.badlogic.gdx.Gdx;
//import com.badlogic.gdx.InputMultiplexer;
//import com.badlogic.gdx.Screen;
//import com.badlogic.gdx.graphics.Color;
//import com.badlogic.gdx.graphics.GL20;
//import com.badlogic.gdx.graphics.Texture;
//import com.badlogic.gdx.graphics.g2d.BitmapFont;
//import com.badlogic.gdx.math.Vector2;
//import com.badlogic.gdx.scenes.scene2d.Actor;
//import com.badlogic.gdx.scenes.scene2d.InputEvent;
//import com.badlogic.gdx.scenes.scene2d.Stage;
//import com.badlogic.gdx.scenes.scene2d.ui.*;
//import com.badlogic.gdx.scenes.scene2d.utils.*;
//import com.badlogic.gdx.utils.Array;
//import com.badlogic.gdx.utils.ArrayMap;
//import com.badlogic.gdx.utils.viewport.ExtendViewport;
//import com.badlogic.gdx.utils.viewport.FitViewport;
//
//import java.io.IOException;
//import java.util.Scanner;
//
///**
// * Class for handling the level editor.
// *
// * All information about the level editor itself is stored in LevelEditorModel
// * the contents of which are rendered every frame. The background is done in
// * GameCanvas.
// */
//public class LevelEditorController implements Screen {
//    /** Enumeration representing the current mode of the level editor */
//    public enum Mode {
//        /** Move Mode: nodes can be freely moved around onscreen */
//        MOVE,
//        /** Edit Mode: nodes can be clicked on, after which their contents can be edited */
//        EDIT,
//        /** Delete Mode: nodes can be clicked on, which deletes them */
//        DELETE,
//        /** Draw Mode: connections can be drawn between nodes */
//        DRAW
//    }
//
//    /** Canvas is the primary view class of the game */
//    private final GameCanvas canvas;
//    /** Gets player input */
//    private final InputController input;
//    /** Controller for view camera for node map */
//    private final CameraController camera;
//
//    /** Stage where nodes and connectors are drawn */
//    Stage nodeStage;
//    /** Stage where buttons are drawn on */
//    Stage toolstage;
//
//    /** Hashmap of node names to their LevelTiles */
//    ArrayMap<String, LevelTile> levelTiles;
//    /** Hashmap of coordinates and the names of the objects at that location */
//    ArrayMap<Vector2, Array<String>> levelMap;
//
//    /** Image representing the current node that is being clicked on */
//    Image selectedNode;
//    /** Image representing stress rating of current node being clicked on */
//    Image nodeStressRating;
//
//    /** Current mode of the level editor */
//    private Mode editorMode;
//    /** Label style to use for text labels */
//    Label.LabelStyle lstyle;
//    /** TextField to enter target name */
//    TextField targetNamePrompt;
//
//    /** The count of the next image that is added */
//    int imgCount;
//
//    /** Vector cache to avoid initializing vectors every time */
//    private Vector2 vec;
//
//    /** Dimensions of map tile */
//    private static final float TILE_HEIGHT = 256.0f;
//    private static final float TILE_WIDTH = 444.0f;
//    /** Constant for the y-offset for different node types */
//    private static final float LOCKED_OFFSET = 114.8725f;
//
//    /** Scale of the buttons in the toolbar */
//    private static final float BUTTON_SCALE = 0.5f;
//    /** Width of buttons in the toolbar in pixels */
//    private static final int BUTTON_WIDTH = 100;
//    /** Gap between two buttons in pixels */
//    private static final int BUTTON_GAP = 60;
//    /** How far to the right the toolbar should be offset from the left edge of the screen, in pixels */
//    private static final int TOOLBAR_X_OFFSET = 10;
//    /** How far down the toolbar should be offset from the top edge of the screen, in pixels */
//    private static final int TOOLBAR_Y_OFFSET = 60;
//
//    /** Array of modes */
//    private static final Mode[] MODE_ORDER = {Mode.MOVE, Mode.EDIT, Mode.DELETE, Mode.DRAW};
//
//    /** Order of connectors (N,E,S,W) */
//    private static final Direction[] CONN_ORDER = {Direction.N, Direction.E, Direction.S, Direction.W};
//    private static final String[] CONN_NAME_ORDER = {"N","E","S","W"};
//    /** Array of all textures for nodes */
//    private static final Texture[] NODE_TEXTURES = new Texture[]{
//            new Texture(Gdx.files.internal("leveleditor/N_TargetMaleIndividualLow_1.png")),
//            new Texture(Gdx.files.internal("leveleditor/N_UnlockedIndividualLow_1.png")),
//            new Texture(Gdx.files.internal("leveleditor/N_LockedIndividualLow_1.png")),
//            new Texture(Gdx.files.internal("leveleditor/N_TargetMaleIndividual_1.png")),
//            new Texture(Gdx.files.internal("leveleditor/N_UnlockedIndividual_1.png")),
//            new Texture(Gdx.files.internal("leveleditor/N_LockedIndividual_2.png"))
//    };
//    /** Array of all TextureRegionDrawables for nodes */
//    private static final TextureRegionDrawable[] NODE_TRDS = new TextureRegionDrawable[]{
//            new TextureRegionDrawable(NODE_TEXTURES[0]),
//            new TextureRegionDrawable(NODE_TEXTURES[1]),
//            new TextureRegionDrawable(NODE_TEXTURES[2]),
//            new TextureRegionDrawable(NODE_TEXTURES[3]),
//            new TextureRegionDrawable(NODE_TEXTURES[4]),
//            new TextureRegionDrawable(NODE_TEXTURES[5])
//    };
//    /** Array of TextureRegionDrawables of all node creation buttons, in order */
//    private static final TextureRegionDrawable[] ADD_NODE_TRD_ORDER = new TextureRegionDrawable[]{
//            new TextureRegionDrawable(new Texture(Gdx.files.internal("leveleditor/buttons/LE_AddNodeTarget_1.png"))),
//            new TextureRegionDrawable(new Texture(Gdx.files.internal("leveleditor/buttons/LE_AddNodeUnlocked_1.png"))),
//            new TextureRegionDrawable(new Texture(Gdx.files.internal("leveleditor/buttons/LE_AddNodeLocked_1.png")))
//    };
//    /** Array of TextureRegionDrawables of all mode changing buttons, in order */
//    private static final TextureRegionDrawable[] CHANGE_MODE_TRD_ORDER = new TextureRegionDrawable[]{
//            new TextureRegionDrawable(new Texture(Gdx.files.internal("leveleditor/buttons/LE_MoveMode_1.png"))),
//            new TextureRegionDrawable(new Texture(Gdx.files.internal("leveleditor/buttons/LE_EditMode_1.png"))),
//            new TextureRegionDrawable(new Texture(Gdx.files.internal("leveleditor/buttons/LE_DeleteMode_1.png"))),
//            new TextureRegionDrawable(new Texture(Gdx.files.internal("leveleditor/buttons/LE_DrawMode_1.png"))),
//            new TextureRegionDrawable(new Texture(Gdx.files.internal("leveleditor/buttons/LE_SaveLevel_1.png")))
//    };
//    /** Order of stress rating buttons (None, Low, Medium, High) */
//    private static final StressRating[] SR_ORDER = {StressRating.NONE, StressRating.LOW,
//            StressRating.MED, StressRating.HIGH};
//    private static final String[] SR_NAME_ORDER = {"None", "Low", "Medium", "High"};
//    /** Array of TextureRegionDrawables of all stress rating buttons, in order */
//    private static final TextureRegionDrawable[] SR_TRD_ORDER = new TextureRegionDrawable[]{
//            new TextureRegionDrawable(new Texture(Gdx.files.internal("leveleditor/buttons/LE_NodeNone_1.png"))),
//            new TextureRegionDrawable(new Texture(Gdx.files.internal("leveleditor/buttons/LE_NodeLow_1.png"))),
//            new TextureRegionDrawable(new Texture(Gdx.files.internal("leveleditor/buttons/LE_NodeMed_1.png"))),
//            new TextureRegionDrawable(new Texture(Gdx.files.internal("leveleditor/buttons/LE_NodeHigh_1.png"))),
//    };
//    /** TextureRegionDrawable for blank stress rating button */
//    private static final TextureRegionDrawable SR_TRD_BLANK = new TextureRegionDrawable(
//            new Texture(Gdx.files.internal("leveleditor/buttons/LE_NodeBlank_1.png")));
//
//
//    /************************************************* CONSTRUCTOR *************************************************/
//    /**
//     * Creates a new level editor controller. This initializes the UI and sets up the isometric
//     * grid.
//     */
//    public LevelEditorController() {
//        // Create canvas and set view and zoom
//        canvas = new GameCanvas();
//        // Get singleton instance of player input controller
//        input = InputController.getInstance();
//
//        //canvas.setIsometricSize(4, 4);
//
//        // Set up camera
//        ExtendViewport viewport = new ExtendViewport(canvas.getWidth(), canvas.getHeight());
//        camera = new CameraController(input,canvas);
//        camera.setViewport(viewport);
//
//        // Create stage for grid and tile with isometric grid
//        nodeStage = new Stage(viewport);
//
//        // Create tool stage for buttons
//        createToolStage();
//
//        // Initialize hashmap of level tiles
//        levelTiles = new ArrayMap<>();
//        // Initialize map of level tiles at coordinates
//        levelMap = new ArrayMap<>();
//        // Initialize vector caches
//        vec = new Vector2();
//
//        // Start editor mode in Move Mode
//        editorMode = Mode.MOVE;
//
//        // Create label style to use
//        BitmapFont font = new BitmapFont();
//        lstyle = new Label.LabelStyle(font, Color.CYAN);
//    }
//
//    /************************************************** LEVELTILE ***************************************************/
//    /** Inner class representing a level tile at an isometric coordinate */
//    private class LevelTile {
//        /** Isometric coordinates representing tile's location */
//        float x;
//        float y;
//        /** Stress rating of the tile, if any */
//        StressRating sr;
//        /** The image itself stored at the tile */
//        Image im;
//
//        /**
//         * Constructor for a LevelTile.
//         */
//        private LevelTile(float x, float y, Image im, StressRating sr) {
//            this.x = x;
//            this.y = y;
//            this.im = im;
//            this.sr = sr;
//        }
//
//    }
//
//    /**
//     * Helper function that adds the tile with the given name to the given location
//     * in the level map. Given location is in isometric space.
//     *
//     * This function does not deal with levelTiles at all.
//     *
//     * @param name  Name of the image to add to the map.
//     * @param x     x-coordinate of location to add tile at.
//     * @param y     y-coordinate of location to add tile at.
//     */
//    private void addToMap(String name, float x, float y) {
//        vec.set(x,y);
//        // If coordinate already exists, add to existing array there
//        if (levelMap.containsKey(vec)) {
//            levelMap.get(vec).add(name);
//        }
//        // Otherwise, make a new one
//        else {
//            Array<String> arr = new Array<String>();
//            arr.add(name);
//            Vector2 newVec = new Vector2(vec.x,vec.y);
//            levelMap.put(newVec,arr);
//        }
//    }
//
//    /**
//     * Helper function that adds the tile with the given properties to the given location
//     * in the level. Given location is in isometric space.
//     *
//     * This adds the node to levelTiles and levelMap.
//     *
//     * @param im    Image representing the tile's appearance.
//     * @param x     x-coordinate of location to add tile at.
//     * @param y     y-coordinate of location to add tile at.
//     * @param sr    Stress rating of the tile.
//     */
//    private void addToLevel(Image im, float x, float y, StressRating sr) {
//        // Create corresponding LevelTile
//        LevelTile lt = new LevelTile(x,y,im,sr);
//        // Add to levelTiles
//        levelTiles.put(im.getName(),lt);
//        // Map to location in level map
//        addToMap(im.getName(),x,y);
//    }
//
//    /**
//     * Helper function that removes the tile with the given name from the level map.
//     *
//     * This function does not deal with levelTiles at all.
//     *
//     * @param name  Name of the image to remove from the map.
//     */
//    private void removeFromMap(String name) {
//        // Get the node's coordinates from levelTiles
//        LevelTile lt = levelTiles.get(name);
//        vec.set(lt.x,lt.y);
//        // Remove node from levelMap using coordinates
//        Array<String> nodes = levelMap.get(vec);
//        nodes.removeValue(name,false);
//        Vector2 newVec = new Vector2(vec.x,vec.y);
//        levelMap.put(newVec, nodes);
//    }
//
//    /**
//     * Helper function that removes the tile with the given name from the level.
//     *
//     * This removes the node from levelTiles and levelMap.
//     *
//     * @param name  Name of the image to remove from the map.
//     */
//    private void removeFromLevel(String name) {
//        removeFromMap(name);
//        levelTiles.removeKey(name);
//    }
//
//    /**
//     * Helper function that updates the levelTile with the given name so that
//     * it contains the given value.
//     *
//     * Given coordinates must be in isometric space.
//     *
//     * @param name      Name of LevelTile to modify.
//     * @param x         x-coordinate to change the LevelTile to.
//     * @param y         y-coordinate to change the LevelTile to.
//     */
//    private void updateLevelTile(String name, float x, float y) {
//        // Update the map itself as well to account for the change in location
//        removeFromMap(name);
//        addToMap(name,x,y);
//
//        // Update the array of level tiles
//        LevelTile lt = levelTiles.get(name);
//        lt.x = x;
//        lt.y = y;
//        levelTiles.put(name,lt);
//    }
//
//    /**
//     * Helper function that updates the levelTile with the given name so that
//     * it contains the given value.
//     *
//     * @param name      Name of LevelTile to modify.
//     * @param stress    StressRating to change the level tile's stress rating to.
//     */
//    private void updateLevelTile(String name, StressRating stress) {
//        LevelTile lt = levelTiles.get(name);
//        lt.sr = stress;
//        levelTiles.put(name,lt);
//    }
//
//    /**
//     * Helper function that updates the LevelTile with the given name to have
//     * a new name.
//     *
//     * @param name      Name of LevelTile to modify.
//     * @param newName   Name to change the LevelTile's name to.
//     */
//    private void updateLevelTile(String name, String newName) {
//        // Rename tile in levelMap
//        removeFromMap(name);
//        addToMap(newName,levelTiles.get(name).x,levelTiles.get(name).y);
//
//        // Rename tile in levelTiles
//        levelTiles.setKey(levelTiles.indexOfKey(name), newName);
//    }
//
//
//
//    /*************************************************** HELPERS ****************************************************/
//    /**
//     * Helper function that converts coordinates from world space to isometric space.
//     *
//     * @param coords   Coordinates in world space to transform
//     */
//    private void worldToIsometric(Vector2 coords) {
//        float tempx = coords.x;
//        float tempy = coords.y;
//        coords.x = 0.57735f * tempx - tempy;
//        coords.y = 0.57735f * tempx + tempy;
//    }
//
//    /**
//     * Helper function that converts coordinates from isometric space to world space.
//     *
//     * @param coords   Coordinates in isometric space to transform
//     */
//    private void isometricToWorld(Vector2 coords) {
//        float tempx = coords.x;
//        float tempy = coords.y;
//        coords.x = tempx * (0.5f * TILE_WIDTH) + tempy * (0.5f * TILE_WIDTH);
//        coords.y = -tempx * (0.5f * TILE_HEIGHT) + tempy * (0.5f * TILE_HEIGHT);
//    }
//
//    /**
//     * Helper function that gets the center of an isometric grid tile nearest to the given coordinates.
//     *
//     * Called when snapping an image to the center of a grid tile.
//     *
//     * The nearest isometric center is just stored in the vector cache [vec], in isometric space.
//     *
//     * @param x     x-coordinate of the location we want to find the nearest isometric center to
//     * @param y     y-coordinate of the location we want to find the nearest isometric center to
//     */
//    private void nearestIsoCenter(float x, float y){
//        // Transform world coordinates to isometric space
//        vec.set(x,y);
//        worldToIsometric(vec);
//        x = vec.x;
//        y = vec.y;
//
//        // Find the nearest isometric center
//        x = Math.round(x / TILE_HEIGHT);
//        y = Math.round(y / TILE_HEIGHT);
//
//        // Return in isometric space
//        vec.set(x,y);
//    }
//
//    /**
//     * Helper function that finds the index of a value in the array.
//     *
//     * Returns the index of the given value in the array, or -1 if the value was not found.
//     *
//     * @param val       Value in array
//     * @param arr       Array that value belongs to
//     * @return          Index of the value in the array
//     */
//    private <T>int find(T val, T[] arr) {
//        // Loop through array until given value is found in the array
//        for (int k=0; k<arr.length; k++) {
//            if (arr[k].equals(val)) {return k;}
//        }
//        // Return -1 if value was not found
//        return -1;
//    }
//
//    /**
//     * Helper function that returns the index to the next value in the array.
//     *
//     * When called with an array of connector types as input, it returns the character representing
//     * the connector to rotate to next. The order goes from North -> East -> South -> West -> North.
//     *
//     * When called with an array of node stress rating button types as input, it returns the name of
//     * the next button type. The order goes from None -> Low -> Med -> High -> None.
//     *
//     * @param curr      Current entry in the array.
//     * @param order     Array of objects representing the order.
//     * @return          Index of the entry that is next in the array order
//     */
//    private <T>int nextEntry(T curr, T[] order) {
//        // Find current entry's index
//        int k = find(curr,order);
//        // Raise exception if not found
//        if (k<0) {
//            throw new RuntimeException("Entry is not in array");
//        }
//        // Return index of next entry in order
//        return (find(curr,order) + 1) % order.length;
//    }
//
//    /**
//     * Helper function that changes the editor mode to the given mode.
//     *
//     * @param mode      Mode to change the editor to
//     */
//    private void changeEditorMode(Mode mode) {
//        // Deselect any selected nodes
//        if (selectedNode != null) {
//            // First digit of node name gives the node type, so set node image to be low version of itself
//            selectedNode.setDrawable(NODE_TRDS[Character.getNumericValue(selectedNode.getName().charAt(0))]);
//        }
//        selectedNode = null;
//
//        // Revert stress rating button to blank
//        nodeStressRating.setDrawable(SR_TRD_BLANK);
//
//        // Change mode
//        editorMode = mode;
//    }
//
//    /************************************************** TOOLBAR **************************************************/
//
//    /**
//     * Creates and fills the stage with buttons to be used in creating a level.
//     *
//     * These include:
//     * - A button to create a new target.
//     * - A button to create a new unlocked node.
//     * - A button to create a new locked node.
//     */
//    private void createToolStage(){
//        // Creates toolbar viewport and camera
//        FitViewport toolbarViewPort = new FitViewport(canvas.getWidth(), canvas.getHeight());
//        toolstage = new Stage(toolbarViewPort);
//
//        // Handle inputs from both stages with a Multiplexer
//        InputMultiplexer inputMultiplexer = new InputMultiplexer();
//        inputMultiplexer.addProcessor(toolstage);
//        inputMultiplexer.addProcessor(nodeStage);
//        Gdx.input.setInputProcessor(inputMultiplexer);
//
//        // Create and place toolbar to hold all the buttons
//        Table toolbar = new Table();
//        toolbar.right();
//        toolbar.setSize(.25f*canvas.getWidth(),canvas.getHeight());
//
//        // Add all buttons to toolbar
//        createNodeButtons(toolbar);
//        createModeButtons(toolbar);
//        createNodeSRButton(toolbar);
//
//        // Add filled toolbar to stage
//        toolstage.addActor(toolbar);
//
//        // Create text field for prompting for target name
//        //TextField
//    }
//
//    /**
//     * TODO: combine createNodeButtons and createModeButtons into a single function
//     * Can take in a boolean for whether to do Node or Mode
//     */
//
//    /**
//     * Function that adds the buttons used to create nodes to the stage and toolbar.
//     *
//     * These include:
//     * - A button to create a new target.
//     * - A button to create a new unlocked node.
//     * - A button to create a new locked node.
//     * - A button to bring all nodes to the front, which is thematically similar.
//     *
//     * @param toolbar       The toolbar that the buttons are stored in.
//     */
//    private void createNodeButtons(Table toolbar) {
//        // Create "add node" buttons
//        // Height of first button
//        int height = (int)camera.getHeight() - TOOLBAR_Y_OFFSET;
//        // Initialize other variables for button creation
//        Drawable drawable;
//        ImageButton button;
//
//        // Loop through and create each button
//        for (int k=0; k<ADD_NODE_TRD_ORDER.length; k++) {
//            // Create and place button
//            drawable = ADD_NODE_TRD_ORDER[k];
//            button = new ImageButton(drawable);
//            button.setTransform(true);
//            button.setScale(BUTTON_SCALE);
//            button.setPosition(TOOLBAR_X_OFFSET, height);
//
//            // Set node type that this button will create
//            final int nodeType = k;
//
//            // Add listeners to button, changing depending on which node the button creates
//            button.addListener(new ChangeListener() {
//                @Override
//                public void changed(ChangeEvent event, Actor actor) {
//                    addNode(nodeType);
//                }
//            });
//
//            // Add button to stage
//            toolstage.addActor(button);
//            // Arrange buttons in order using a Table
//            toolbar.addActor(button);
//            // Increment height
//            height -= BUTTON_GAP;
//        }
//    }
//
//    /**
//     * Function that adds the buttons used to change modes to the stage and toolbar.
//     *
//     * These include:
//     * - A button to change to Move Mode, where nodes can be moved around.
//     * - A button to change to Edit Mode, where the contents of nodes can be edited.
//     * - A button to change to Delete Mode, where nodes can be deleted.
//     * - A button to save the level, which is not the same, but it looks good here.
//     *
//     * @param toolbar       The toolbar that the buttons are stored in.
//     */
//    private void createModeButtons(Table toolbar) {
//        // Create mode change buttons
//        // Height of first button
//        int height = (int)camera.getHeight() - TOOLBAR_Y_OFFSET;
//        // Right offset of mode buttons
//        int xloc = canvas.getWidth() - TOOLBAR_X_OFFSET - BUTTON_WIDTH;
//        // Initialize other variables for button creation
//        Drawable drawable;
//        ImageButton button;
//
//        // Loop through and create each button
//        for (int k=0; k<CHANGE_MODE_TRD_ORDER.length; k++) {
//            // Create and place button
//            drawable = CHANGE_MODE_TRD_ORDER[k];
//            button = new ImageButton(drawable);
//            button.setTransform(true);
//            button.setScale(BUTTON_SCALE);
//            button.setPosition(xloc, height);
//
//            // For the actual mode creation buttons, do that
//            if (k < CHANGE_MODE_TRD_ORDER.length - 1) {
//                final Mode newMode = MODE_ORDER[k];
//
//                // TODO: some kind of text that shows the mode
//                // Add listeners to button, changing depending on which node the button creates
//                // Changes the editor mode to the one determined by the button
//                button.addListener(new ChangeListener() {
//                    @Override
//                    public void changed(ChangeEvent event, Actor actor) {
//                        changeEditorMode(newMode);
//                    }
//                });
//            }
//            // But make the save level button differently
//            else {
//                button.addListener(new ChangeListener() {
//                    @Override
//                    public void changed(ChangeEvent event, Actor actor) {
//                        saveLevel();
//                        //System.out.println("Level Saved");
//                    }
//                });
//            }
//
//            // Add button to stage
//            toolstage.addActor(button);
//            // Arrange buttons in order using a Table
//            toolbar.addActor(button);
//            // Increment height
//            height -= BUTTON_GAP;
//        }
//    }
//
//    /**
//     * Function that creates the button that can be used to set the target stress damage of a node
//     * and places it in the stage and toolbar.
//     *
//     * This is just one button that cycles between the options.
//     *
//     * @param toolbar       The toolbar that the buttons are stored in.
//     */
//    private void createNodeSRButton(Table toolbar) {
//        // Initialize other variables for button creation
//        Drawable drawable;
//
//        // Create and place button, initialized at Blank
//        drawable = SR_TRD_BLANK;
//        nodeStressRating = new Image(drawable);
//        nodeStressRating.setScale(BUTTON_SCALE);
//        nodeStressRating.setPosition(canvas.getWidth() / 2f - 50, 10);
//
//        // Set name to current status of button
//        nodeStressRating.setName("None");
//
//        // Add listeners to button, changing depending on which node the button creates
//        nodeStressRating.addListener((new ClickListener() {
//            public void clicked(InputEvent event, float x, float y) {
//                // Only do something if clicked while in Edit Mode and a node is selected
//                if (editorMode == Mode.EDIT && selectedNode != null) {
//                    // Set the appearance and name to be the next button
//                    int next = nextEntry(nodeStressRating.getName(), SR_NAME_ORDER);
//                    nodeStressRating.setName(SR_NAME_ORDER[next]);
//                    nodeStressRating.setDrawable(SR_TRD_ORDER[next]);
//
//                    // Change the stress rating of the selected node accordingly
//                    updateLevelTile(selectedNode.getName(),SR_ORDER[next]);
//                }
//            }
//        }));
//
//        // Add button to stage
//        toolstage.addActor(nodeStressRating);
//        // Put button in toolbar Table
//        toolbar.addActor(nodeStressRating);
//    }
//
//    /*********************************************** NODES ***********************************************/
//
//    /**
//     * Adds a draggable node of the given type to the stage.
//     *
//     * Called when one of the node-adding buttons is pressed. Each image is given a name that is a number
//     * of increasing value, so that no names are repeated in a single level editor session.
//     *
//     * This function also contains all the behaviors of each node and what it does when it is interacted
//     * with.
//     *
//     * @param nodeType      0: target, 1: unlocked, 2: locked
//     */
//    private void addNode(int nodeType) {
//        // If target, prompt for name before creating node
//        if (nodeType == 0) {
//            // TODO: prompt for target name
//        }
//
//        // Create image
//        final Image im = new Image(NODE_TEXTURES[nodeType]);
//        nodeStage.addActor(im);
//        im.setPosition(-(im.getWidth() - TILE_WIDTH) / 2, ((TILE_HEIGHT / 2) - LOCKED_OFFSET) * 2);
//        im.setOrigin(0, 0);
//
//        // Set name of image, which is the node type, the string "Node," and a unique number
//        String name = nodeType + "Node" + imgCount;
//        im.setName(name);
//        imgCount++;
//
//        // Add to level, initialized with a stress rating of None
//        addToLevel(im,0,0,StressRating.NONE);
//
//        // Get relevant low and high textures for this node
//        final TextureRegionDrawable nodeLow = NODE_TRDS[nodeType];
//        final TextureRegionDrawable nodeHigh = NODE_TRDS[nodeType+3];
//
//        // Add listeners, which change their behavior depending on the editor mode
//
//        // Add drag listener that does something during a drag
//        im.addListener((new DragListener() {
//            public void touchDragged(InputEvent event, float x, float y, int pointer) {
//                // Only do this if editor mode is Move
//                // Updates image position on drag
//                if (editorMode == Mode.MOVE) {
//                    // When dragging, snaps image center to cursor
//                    float dx = x - im.getWidth() * 0.5f;
//                    float dy = y - im.getHeight() * 0.25f;
//                    im.setPosition(im.getX() + dx, im.getY() + dy);
//
//                    // Change to high version of asset
//                    im.setDrawable(nodeHigh);
//                }
//            }
//        }));
//
//        // Add drag listener that does something when a drag ends
//        im.addListener((new DragListener() {
//            public void dragStop(InputEvent event, float x, float y, int pointer) {
//                // Only do this if editor mode is Move
//                // Snap to center of nearby isometric grid
//                if (editorMode == Mode.MOVE) {
//                    // Get coordinates of center of image
//                    float newX = im.getX() + x - im.getWidth() * 0.5f;
//                    float newY = im.getY() + y - im.getHeight() * 0.25f;
//
//                    // Get location that image should snap to
//                    nearestIsoCenter(newX, newY);
//                    newX = vec.x;
//                    newY = vec.y;
//
//                    // Update LevelTile with new isometric location
//                    updateLevelTile(im.getName(), newX, newY);
//
//                    // Convert to world space
//                    vec.set(newX,newY);
//                    isometricToWorld(vec);
//                    // Retrieve from vector cache
//                    newX = vec.x;
//                    newY = vec.y;
//
//                    // Account for difference between tile width and sprite width
//                    newX -= (im.getWidth() - TILE_WIDTH) / 2;
//                    newY += ((TILE_HEIGHT / 2) - LOCKED_OFFSET) * 2;
//
//                    im.setPosition(newX, newY);
//
//                    // Change back to low version of asset
//                    im.setDrawable(nodeLow);
//                }
//            }
//        }));
//
//        // Add click listener that does something when the node is clicked
//        im.addListener((new ClickListener() {
//            public void clicked(InputEvent event, float x, float y) {
//            // Different behavior on clicked depending on editor mode
//            switch (editorMode) {
//                // In Edit Mode, allow node stress rating to be set
//                case EDIT:
//                    // If a node was previously selected, revert it to deselected
//                    if (selectedNode != null) {
//                        // First digit of node name gives the node type, so set node image to be low version of itself
//                        selectedNode.setDrawable(NODE_TRDS[Character.getNumericValue(selectedNode.getName().charAt(0))]);
//                    }
//                    // Change clicked node to the node that was just clicked
//                    selectedNode = im;
//                    // Change to high version of asset to indicate it's been selected
//                    im.setDrawable(new TextureRegionDrawable(nodeHigh));
//
//                    // Change the appearance and name of the stress rating button to reflect the SR of this node
//                    int ind = find(levelTiles.get(im.getName()).sr,SR_ORDER);
//                    nodeStressRating.setDrawable(SR_TRD_ORDER[ind]);
//                    nodeStressRating.setName(SR_NAME_ORDER[ind]);
//
//                    break;
//                // In Delete Mode, delete the node
//                case DELETE:
//                    removeFromLevel(im.getName());
//                    im.remove();
//                    break;
//                default:
//                    break;
//            }
//            }
//        }));
//    }
//
//    /*********************************************** CONNECTORS ***********************************************/
//    /**
//     * Adds a connector to the grid tile at the given coordinates.
//     *
//     * Called when right-clicking anywhere in Draw Mode. Coordinates given are in screen space.
//     *
//     * @param x     Screen space x-coordinate of the location to add the connector at
//     * @param y     Screen space y-coordinate of the location to add the connector at
//     */
//    public void addConnector(float x, float y) {
//        // Convert mouse position from screen to world coordinates
//        vec = camera.screenToWorld(x,y);
//        x = vec.x;
//        y = vec.y;
//
//        // Create connector image, defaulting to the North connector
//        final Image im = new Image(new Texture(Gdx.files.internal(Connector.getAssetPath(Direction.N))));
//        nodeStage.addActor(im);
//
//        // Set name of connector, which defaults to "N". The first letter is the connector, the second
//        // is a unique identifier.
//        im.setName("N" + imgCount);
//        imgCount++;
//        // Set scale
//        im.setScale(0.5f);
//
//        // Get nearest isometric center to where the mouse clicked
//        nearestIsoCenter(x, y);
//        x = vec.x;
//        y = vec.y - 1; // For some reason this is consistently off by 1, so we take care of that this way
//
//        // Add connector to level
//        addToLevel(im,x,y,StressRating.NONE);
//
//        // Convert to world space
//        vec.set(x,y+1); // Don't know why it needs to be y+1, but it does
//        isometricToWorld(vec);
//        // Retrieve from vector cache
//        x = vec.x;
//        y = vec.y;
//
//        // Place connector at nearest isometric center
//        im.setPosition(x - (TILE_WIDTH / 4), y - (TILE_HEIGHT / 4));
//        im.setOrigin(0, 0);
//
//        // Add listeners, which change their behavior depending on the editor mode
//        // Add click listener that does something when the connector is left-clicked
//        im.addListener((new ClickListener() {
//            public void clicked(InputEvent event, float x, float y) {
//            // Different behavior on clicked depending on editor mode
//            switch (editorMode) {
//                // In Draw Mode, rotate the connector
//                case DRAW:
//                    // Set the appearance and name to be the next connector
//                    int nextConn = nextEntry(String.valueOf(im.getName().charAt(0)), CONN_NAME_ORDER);
//                    String name = CONN_NAME_ORDER[nextConn] + im.getName().substring(1);
//                    updateLevelTile(im.getName(),name);
//                    im.setName(name);
//                    im.setDrawable(new TextureRegionDrawable(
//                            new Texture(Gdx.files.internal(
//                                    // Path to connector asset
//                                    Connector.getAssetPath(CONN_ORDER[nextConn])
//                            ))));
//                    break;
//                // In Delete Mode, delete the connector
//                case DELETE:
//                    removeFromLevel(im.getName());
//                    im.remove();
//                    break;
//                default:
//                    break;
//            }
//            }
//        }));
//    }
//
//    /*********************************************** SCREEN METHODS ***********************************************/
//
//    @Override
//    public void show() {
//    }
//
//    /**
//     * Clears all images in the level.
//     *
//     * Called if the clear button "C" is pressed.
//     */
//    private void clearLevel() {
//        // Remove all images from level
//        for (LevelTile lt : levelTiles.values()) {
//            lt.im.remove();
//        }
//        // Clear saved data about level tiles
//        levelTiles.clear();
//        levelMap.clear();
//        // Reset image count
//        imgCount = 0;
//    }
//
//    /**
//     * Undos the creation of the last image.
//     *
//     * Called if the undo button "Z" is pressed.
//     */
//    private void undo() {
//        int size = levelTiles.size;
//        // Do nothing if no images have been created
//        if (size <= 0) {return;}
//        // Remove last created image from array of created images and from level map
//        Image lastIm = levelTiles.getValueAt(size - 1).im;
//        removeFromLevel(lastIm.getName());
//        // Actually remove image from screen
//        lastIm.remove();
//
//    }
//
//    @Override
//    /**
//     * Renders the game display at consistent time steps.
//     */
//    public void render(float delta) {
//        // PREUPDATE
//        // If C button is pressed, clear the level
//        if (input.didClear()) {clearLevel();}
//        // If Z button is pressed, delete last object that was created
//        if (input.didUndo()) {undo();}
//
//        // Handle making connectors if in Draw Mode and a connector was made
//        if (editorMode == Mode.DRAW && input.didRightClick()) {
//            addConnector(input.getX(), input.getY());
//        }
//
//        // Move camera
//        canvas.clear();
//        camera.moveCamera();
//
//        // UPDATE
//
//        // Draw objects on canvas
//        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//        nodeStage.act(delta);
//        toolstage.act(delta);
//
//        canvas.drawIsometricGrid(nodeStage, 15, 15);
//
//        nodeStage.draw();
//        toolstage.draw();
//    }
//
//    @Override
//    /**
//     * Ensures that the game world appears at the same scale, even when resizing
//     */
//    public void resize(int width, int height) {
//        // Keep game world at the same scale even when resizing
//        //nodeStage.getViewport().update(width,height,true);
//        camera.resize(width, height);
//
//        // Keep toolbar in the same place when resizing
//        //toolstage.getViewport().update(width,height,true);
//    }
//
//    @Override
//    public void pause() {
//
//    }
//
//    @Override
//    public void resume() {
//
//    }
//
//    @Override
//    public void hide() {
//
//    }
//
//    @Override
//    public void dispose() {
//
//    }
//
//    /*********************************************** SAVE AND LOAD ***********************************************/
//
//    /** Values of placeholder constants */
//    private static final String TARGET_NAME = "Torchlight Employee";
//    private static final int TARGET_PARANOIA = 3;
//    private static final int TARGET_MAXSTRESS = 100;
//    private static final StressRating PS_DMG = StressRating.NONE;
//
//    /**
//     * Saves the level, constructing a model and producing a JSON.
//     */
//    private void saveLevel() {
//        // TODO: if there are overlapping connectors, delete the extras
//
//        // Create a LevelEditorModel
//        LevelEditorModel model = new LevelEditorModel();
//
//        // Get filename from user input in terminal
//        // TODO: get user input not from terminal
//        Scanner scan = new Scanner(System.in);
//        System.out.println("Enter desired level name: ");
//        String fname =  scan.nextLine();
//
//        // Instantiate count of targets in level
//        int targetCount = 1;
//
//        // Go through each grid tile that contains LevelTiles
//        String c;
//        LevelTile lt;
//        String connector;
//        Array<Connector> connectors = new Array<>();
//        for (Vector2 pos : levelMap.keys()) {
//            connector = "";
//            // For each LevelTile in this grid tile
//            for (String tilename : levelMap.get(pos)) {
//                // Get identifier that can be used to identify type of tile
//                c = String.valueOf(tilename.charAt(0));
//                // Get the actual LevelTile
//                lt = levelTiles.get(tilename);
//                switch (c) {
//                    case "0": // TARGET NODE
//                        // Make target accordingly
//                        model.make_target(fname+ " " + TARGET_NAME + " " + targetCount, TARGET_PARANOIA, 100, pos);
//                        targetCount++;
//                        break;
//                    case "1": // UNLOCKED NODE
//                        // Make unlocked node accordingly
//                        model.make_factnode(lt.im.getName(), lt.sr, PS_DMG, false, pos);
//                        break;
//                    case "2": // LOCKED NODE
//                        // Make locked node accordingly
//                        model.make_factnode(lt.im.getName(), lt.sr, PS_DMG, true, pos);
//                        break;
//                    default: // CONNECTOR
//                        // Add the direction to the connector string
//                        connector += c;
//                        break;
//                }
//            }
//            // Store new connector in array of connectors
//            connectors.add(new Connector(pos,connector));
//        }
//        // Pass all connectors into the model
//        model.make_connections(connectors);
//
//        // Make JSON
//        try {
//            // Don't need to include ".json"
//            model.make_level_json(fname);
//        }
//        catch(IOException e) {
//            System.out.println("make_level_json failed");
//        }
//
//        System.out.println("Level " + fname + " Save Complete");
//    }
//}
