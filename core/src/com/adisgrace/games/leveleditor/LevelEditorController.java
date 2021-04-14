package com.adisgrace.games.leveleditor;

import com.adisgrace.games.*;
import com.adisgrace.games.util.Connector;
import com.adisgrace.games.util.Connector.*;
import com.adisgrace.games.leveleditor.LevelEditorModel.StressRating;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;

/**
 * Class for handling the level editor.
 *
 * All information about the level editor itself is stored in LevelEditorModel
 * the contents of which are rendered every frame. The background is done in
 * GameCanvas.
 */
public class LevelEditorController implements Screen {
    /** Enumeration representing the current mode of the level editor */
    public enum Mode {
        /** Move Mode: nodes can be freely moved around onscreen */
        MOVE,
        /** Edit Mode: nodes can be clicked on, after which their contents can be edited */
        EDIT,
        /** Delete Mode: nodes can be clicked on, which deletes them */
        DELETE,
        /** Draw Mode: connections can be drawn between nodes */
        DRAW
    }

    /** Canvas is the primary view class of the game */
    private final GameCanvas canvas;
    /** Gets player input */
    private final InputController input;
    /** Controller for view camera for node map */
    private final CameraController camera;

    /** Stage where grid is drawn */
    //Stage gridStage;
    /** Stage where nodes and connectors are drawn */
    Stage nodeStage;
    /** Stage where buttons are drawn on */
    Stage toolstage;

    /** Array of all the images added */
    Array<Image> images;
    /** Hashmap of node names to their stress ratings */
    ArrayMap<String, StressRating> nodeSRs;
    /** TODO: tracking connectors */
    /** Image representing the current node that is being clicked on */
    Image selectedNode;
    /** Image representing stress rating of current node being clicked on */
    Image nodeStressRating;

    /** Current mode of the level editor */
    private Mode editorMode;
    /** Label style to use for text labels */
    Label.LabelStyle lstyle;

    /** The count of the next image that is added */
    int imgCount;

    /** Vector cache to avoid initializing vectors every time */
    private Vector2 vec;

    /** Dimensions of map tile */
    private static final float TILE_HEIGHT = 256.0f;
    private static final float TILE_WIDTH = 444.0f;
    /** Constants for the y-offset for different node types */
    private static final float LOCKED_OFFSET = 114.8725f;

    /** Scale of the buttons in the toolbar */
    private static final float BUTTON_SCALE = 0.5f;
    /** Width of buttons in the toolbar in pixels */
    private static final int BUTTON_WIDTH = 100;
    /** Gap between two buttons in pixels */
    private static final int BUTTON_GAP = 60;
    /** How far to the right the toolbar should be offset from the left edge of the screen, in pixels */
    private static final int TOOLBAR_X_OFFSET = 10;
    /** How far down the toolbar should be offset from the top edge of the screen, in pixels */
    private static final int TOOLBAR_Y_OFFSET = 60;

    /** Array of modes */
    private static final Mode[] MODE_ORDER = {Mode.MOVE, Mode.EDIT, Mode.DELETE, Mode.DRAW};

    /** Order of connectors (N,E,S,W) */
    private static final Direction[] CONN_ORDER = {Direction.N, Direction.E, Direction.S, Direction.W};
    private static final String[] CONN_NAME_ORDER = {"N","E","S","W"};
    /** Array of all textures for nodes */
    private static final Texture[] NODE_TEXTURES = new Texture[]{
            new Texture(Gdx.files.internal("leveleditor/N_TargetMaleIndividualLow_1.png")),
            new Texture(Gdx.files.internal("leveleditor/N_UnlockedIndividualLow_1.png")),
            new Texture(Gdx.files.internal("leveleditor/N_LockedIndividualLow_1.png")),
            new Texture(Gdx.files.internal("leveleditor/N_TargetMaleIndividual_1.png")),
            new Texture(Gdx.files.internal("leveleditor/N_UnlockedIndividual_1.png")),
            new Texture(Gdx.files.internal("leveleditor/N_LockedIndividual_2.png"))
    };
    /** Array of all TextureRegionDrawables for nodes */
    private static final TextureRegionDrawable[] NODE_TRDS = new TextureRegionDrawable[]{
            new TextureRegionDrawable(NODE_TEXTURES[0]),
            new TextureRegionDrawable(NODE_TEXTURES[1]),
            new TextureRegionDrawable(NODE_TEXTURES[2]),
            new TextureRegionDrawable(NODE_TEXTURES[3]),
            new TextureRegionDrawable(NODE_TEXTURES[4]),
            new TextureRegionDrawable(NODE_TEXTURES[5])
    };
    /** Array of TextureRegionDrawables of all node creation buttons, in order */
    private static final TextureRegionDrawable[] ADD_NODE_TRD_ORDER = new TextureRegionDrawable[]{
            new TextureRegionDrawable(new Texture(Gdx.files.internal("leveleditor/buttons/LE_AddNodeTarget_1.png"))),
            new TextureRegionDrawable(new Texture(Gdx.files.internal("leveleditor/buttons/LE_AddNodeUnlocked_1.png"))),
            new TextureRegionDrawable(new Texture(Gdx.files.internal("leveleditor/buttons/LE_AddNodeLocked_1.png")))
    };
    /** Array of TextureRegionDrawables of all mode changing buttons, in order */
    private static final TextureRegionDrawable[] CHANGE_MODE_TRD_ORDER = new TextureRegionDrawable[]{
            new TextureRegionDrawable(new Texture(Gdx.files.internal("leveleditor/buttons/LE_MoveMode_1.png"))),
            new TextureRegionDrawable(new Texture(Gdx.files.internal("leveleditor/buttons/LE_EditMode_1.png"))),
            new TextureRegionDrawable(new Texture(Gdx.files.internal("leveleditor/buttons/LE_DeleteMode_1.png"))),
            new TextureRegionDrawable(new Texture(Gdx.files.internal("leveleditor/buttons/LE_DrawMode_1.png")))
    };
    /** Order of stress rating buttons (None, Low, Medium, High) */
    private static final StressRating[] SR_ORDER = {StressRating.NONE, StressRating.LOW,
            StressRating.MED, StressRating.HIGH};
    private static final String[] SR_NAME_ORDER = {"None", "Low", "Medium", "High"};
    /** Array of TextureRegionDrawables of all stress rating buttons, in order */
    private static final TextureRegionDrawable[] SR_TRD_ORDER = new TextureRegionDrawable[]{
            new TextureRegionDrawable(new Texture(Gdx.files.internal("leveleditor/buttons/LE_NodeNone_1.png"))),
            new TextureRegionDrawable(new Texture(Gdx.files.internal("leveleditor/buttons/LE_NodeLow_1.png"))),
            new TextureRegionDrawable(new Texture(Gdx.files.internal("leveleditor/buttons/LE_NodeMed_1.png"))),
            new TextureRegionDrawable(new Texture(Gdx.files.internal("leveleditor/buttons/LE_NodeHigh_1.png"))),
    };
    /** TextureRegionDrawable for blank stress rating button */
    private static final TextureRegionDrawable SR_TRD_BLANK = new TextureRegionDrawable(
            new Texture(Gdx.files.internal("leveleditor/buttons/LE_NodeBlank_1.png")));


    /************************************************* CONSTRUCTOR *************************************************/
    /**
     * Creates a new level editor controller. This initializes the UI and sets up the isometric
     * grid.
     */
    public LevelEditorController() {
        // Create canvas and set view and zoom
        canvas = new GameCanvas();
        // Get singleton instance of player input controller
        input = InputController.getInstance();

        // Set up camera
        ExtendViewport viewport = new ExtendViewport(canvas.getWidth(), canvas.getHeight());
        camera = new CameraController(input,canvas);
        camera.setViewport(viewport);

        // Create stage for grid and tile with isometric grid
        nodeStage = new Stage(viewport);
        //canvas.setIsometricSize(3,3);
        canvas.drawIsometricGrid(nodeStage, 5, 5);

        // Create tool stage for buttons
        createToolStage();

        // Initialize array of images
        images = new Array<>();
        // Initialize hashmap of tracked node stress ratings
        nodeSRs = new ArrayMap<>();
        // Initialize vector caches
        vec = new Vector2();

        // Start editor mode in Move Mode
        editorMode = Mode.MOVE;

        // Create label style to use
        BitmapFont font = new BitmapFont();
        lstyle = new Label.LabelStyle(font, Color.CYAN);
    }

    /*********************************************** HELPER FUNCTIONS ***********************************************/

    /**
     * Helper function that converts coordinates from world space to isometric space.
     *
     * @param coords   Coordinates in world space to transform
     */
    private void worldToIsometric(Vector2 coords) {
        float tempx = coords.x;
        float tempy = coords.y;
        coords.x = 0.57735f * tempx - tempy;
        coords.y = 0.57735f * tempx + tempy;
    }

    /**
     * Helper function that gets the center of an isometric grid tile nearest to the given coordinates.
     *
     * Called when snapping an image to the center of a grid tile.
     *
     * The nearest isometric center is just stored in the vector cache [vec].
     *
     * @param x     x-coordinate of the location we want to find the nearest isometric center to
     * @param y     y-coordinate of the location we want to find the nearest isometric center to
     */
    private void nearestIsoCenter(float x, float y){
        // Transform world coordinates to isometric space
        vec.set(x,y);
        worldToIsometric(vec);
        x = vec.x;
        y = vec.y;

        // Find the nearest isometric center
        x = Math.round(x / TILE_HEIGHT);
        y = Math.round(y / TILE_HEIGHT);

        // Transform back to world space
        vec.set(x * (0.5f * TILE_WIDTH) + y * (0.5f * TILE_WIDTH),
                -x * (0.5f * TILE_HEIGHT) + y * (0.5f * TILE_HEIGHT));
    }

    /**
     * Helper function that finds the index of a value in the array.
     *
     * Returns the index of the given value in the array, or -1 if the value was not found.
     *
     * @param val       Value in array
     * @param arr       Array that value belongs to
     * @return          Index of the value in the array
     */
    private <T>int find(T val, T[] arr) {
        // Loop through array until given value is found in the array
        for (int k=0; k<arr.length; k++) {
            if (arr[k].equals(val)) {return k;}
        }
        // Return -1 if value was not found
        return -1;
    }

    /**
     * Helper function that returns the index to the next value in the array.
     *
     * When called with an array of connector types as input, it returns the character representing
     * the connector to rotate to next. The order goes from North -> East -> South -> West -> North.
     *
     * When called with an array of node stress rating button types as input, it returns the name of
     * the next button type. The order goes from None -> Low -> Med -> High -> None.
     *
     * @param curr      Current entry in the array.
     * @param order     Array of objects representing the order.
     * @return          Index of the entry that is next in the array order
     */
    private <T>int nextEntry(T curr, T[] order) {
        // Find current entry's index
        int k = find(curr,order);
        // Raise exception if not found
        if (k<0) {
            throw new RuntimeException("Entry is not in array");
        }
        // Return index of next entry in order
        return (find(curr,order) + 1) % order.length;
    }

    /**
     * Helper function that changes the editor mode to the given mode.
     *
     * @param mode      Mode to change the editor to
     */
    private void changeEditorMode(Mode mode) {
        // Deselect any selected nodes
        if (selectedNode != null) {
            // First digit of node name gives the node type, so set node image to be low version of itself
            selectedNode.setDrawable(NODE_TRDS[Character.getNumericValue(selectedNode.getName().charAt(0))]);
        }
        selectedNode = null;

        // Revert stress rating button to blank
        nodeStressRating.setDrawable(SR_TRD_BLANK);

        // Change mode
        editorMode = mode;
    }

    /************************************************** TOOLBAR **************************************************/

    /**
     * Creates and fills the stage with buttons to be used in creating a level.
     *
     * These include:
     * - A button to create a new target.
     * - A button to create a new unlocked node.
     * - A button to create a new locked node.
     */
    private void createToolStage(){
        // Creates toolbar viewport and camera
        FitViewport toolbarViewPort = new FitViewport(canvas.getWidth(), canvas.getHeight());
        toolstage = new Stage(toolbarViewPort);

        // Handle inputs from both stages with a Multiplexer
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(toolstage);
        inputMultiplexer.addProcessor(nodeStage);
        Gdx.input.setInputProcessor(inputMultiplexer);

        // Create and place toolbar to hold all the buttons
        Table toolbar = new Table();
        toolbar.right();
        toolbar.setSize(.25f*canvas.getWidth(),canvas.getHeight());

        // Add all buttons to toolbar
        createNodeButtons(toolbar);
        createModeButtons(toolbar);
        createNodeSRButton(toolbar);

        // Add filled toolbar to stage
        toolstage.addActor(toolbar);
    }

    /**
     * TODO: combine createNodeButtons and createModeButtons into a single function
     * Can take in a boolean for whether to do Node or Mode
     */

    /**
     * Function that adds the buttons used to create nodes to the stage and toolbar.
     *
     * These include:
     * - A button to create a new target.
     * - A button to create a new unlocked node.
     * - A button to create a new locked node.
     *
     * @param toolbar       The toolbar that the buttons are stored in.
     */
    private void createNodeButtons(Table toolbar) {
        // Create "add node" buttons
        // Height of first button
        int height = (int)camera.getHeight() - TOOLBAR_Y_OFFSET;
        // Initialize other variables for button creation
        Drawable drawable;
        ImageButton button;

        // Loop through and create each button
        for (int k=0; k<ADD_NODE_TRD_ORDER.length; k++) {
            // Create and place button
            drawable = ADD_NODE_TRD_ORDER[k];
            button = new ImageButton(drawable);
            button.setTransform(true);
            button.setScale(BUTTON_SCALE);
            button.setPosition(TOOLBAR_X_OFFSET, height);

            // Set node type that this button will create
            final int nodeType = k;

            // Add listeners to button, changing depending on which node the button creates
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {addNode(nodeType);}
            });

            // Add button to stage
            toolstage.addActor(button);
            // Arrange buttons in order using a Table
            toolbar.addActor(button);
            // Increment height
            height -= BUTTON_GAP;
        }
    }

    /**
     * Function that adds the buttons used to change modes to the stage and toolbar.
     *
     * These include:
     * - A button to change to Move Mode, where nodes can be moved around.
     * - A button to change to Edit Mode, where the contents of nodes can be edited.
     * - A button to change to Delete Mode, where nodes can be deleted.
     *
     * @param toolbar       The toolbar that the buttons are stored in.
     */
    private void createModeButtons(Table toolbar) {
        // Create mode change buttons
        // Height of first button
        int height = (int)camera.getHeight() - TOOLBAR_Y_OFFSET;
        // Right offset of mode buttons
        int xloc = canvas.getWidth() - TOOLBAR_X_OFFSET - BUTTON_WIDTH;
        // Initialize other variables for button creation
        Drawable drawable;
        ImageButton button;

        // Loop through and create each button
        for (int k=0; k<CHANGE_MODE_TRD_ORDER.length; k++) {
            // Create and place button
            drawable = CHANGE_MODE_TRD_ORDER[k];
            button = new ImageButton(drawable);
            button.setTransform(true);
            button.setScale(BUTTON_SCALE);
            button.setPosition(xloc, height);

            final Mode newMode = MODE_ORDER[k];

            // TODO: some kind of text that shows the mode
            // Add listeners to button, changing depending on which node the button creates
            // Changes the editor mode to the one determined by the button
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {changeEditorMode(newMode);}
            });

            // Add button to stage
            toolstage.addActor(button);
            // Arrange buttons in order using a Table
            toolbar.addActor(button);
            // Increment height
            height -= BUTTON_GAP;
        }
    }

    /**
     * Function that creates the button that can be used to set the target stress damage of a node
     * and places it in the stage and toolbar.
     *
     * This is just one button that cycles between the options.
     *
     * @param toolbar       The toolbar that the buttons are stored in.
     */
    private void createNodeSRButton(Table toolbar) {
        // Initialize other variables for button creation
        Drawable drawable;

        // Create and place button, initialized at Blank
        drawable = SR_TRD_BLANK;
        nodeStressRating = new Image(drawable);
        nodeStressRating.setScale(BUTTON_SCALE);
        nodeStressRating.setPosition(canvas.getWidth() / 2f - 50, 10);

        // Set name to current status of button
        nodeStressRating.setName("None");

        // Add listeners to button, changing depending on which node the button creates
        nodeStressRating.addListener((new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                // Only do something if clicked while in Edit Mode and a node is selected
                if (editorMode == Mode.EDIT && selectedNode != null) {
                    // Set the appearance and name to be the next button
                    int next = nextEntry(nodeStressRating.getName(), SR_NAME_ORDER);
                    nodeStressRating.setName(SR_NAME_ORDER[next]);
                    nodeStressRating.setDrawable(SR_TRD_ORDER[next]);

                    // Change the stress rating of the selected node accordingly
                    String name = selectedNode.getName();
                    nodeSRs.put(name,SR_ORDER[next]);
                }
            }
        }));

        // Add button to stage
        toolstage.addActor(nodeStressRating);
        // Put button in toolbar Table
        toolbar.addActor(nodeStressRating);
    }

    /*********************************************** NODES ***********************************************/

    /**
     * Adds a draggable node of the given type to the stage.
     *
     * Called when one of the node-adding buttons is pressed. Each image is given a name that is a number
     * of increasing value, so that no names are repeated in a single level editor session.
     *
     * This function also contains all the behaviors of each node and what it does when it is interacted
     * with.
     *
     * @param nodeType      0: target, 1: unlocked, 2: locked
     */
    private void addNode(int nodeType) {
        // Create image
        final Image im = new Image(NODE_TEXTURES[nodeType]);
        nodeStage.addActor(im);
        im.setPosition(-(im.getWidth() - TILE_WIDTH) / 2, ((TILE_HEIGHT / 2) - LOCKED_OFFSET) * 2);
        im.setOrigin(0, 0);

        // Set name of image, which is the node type, the string "Node," and a unique number
        String name = nodeType + "Node" + imgCount;
        im.setName(name);
        // Add image to images
        images.add(im);
        imgCount++;

        // Add node to ArrayMap of stress ratings, initialized at an SR of None
        nodeSRs.put(name,StressRating.NONE);

        // Get relevant low and high textures for this node
        final TextureRegionDrawable nodeLow = NODE_TRDS[nodeType];
        final TextureRegionDrawable nodeHigh = NODE_TRDS[nodeType+3];

        // Add listeners, which change their behavior depending on the editor mode

        // Add drag listener that does something during a drag
        im.addListener((new DragListener() {
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                // Only do this if editor mode is Move
                // Updates image position on drag
                if (editorMode == Mode.MOVE) {
                    // When dragging, snaps image center to cursor
                    float dx = x - im.getWidth() * 0.5f;
                    float dy = y - im.getHeight() * 0.25f;
                    im.setPosition(im.getX() + dx, im.getY() + dy);

                    // Change to high version of asset
                    im.setDrawable(nodeHigh);
                }
            }
        }));

        // Add drag listener that does something when a drag ends
        im.addListener((new DragListener() {
            public void dragStop(InputEvent event, float x, float y, int pointer) {
                // Only do this if editor mode is Move
                // Snap to center of nearby isometric grid
                if (editorMode == Mode.MOVE) {
                    // Get coordinates of center of image
                    float newX = im.getX() + x - im.getWidth() * 0.5f;
                    float newY = im.getY() + y - im.getHeight() * 0.25f;

                    // Get location that image should snap to
                    nearestIsoCenter(newX, newY);
                    // Retrieve from vector cache
                    newX = vec.x;
                    newY = vec.y;

                    // Account for difference between tile width and sprite width
                    newX -= (im.getWidth() - TILE_WIDTH) / 2;
                    newY += ((TILE_HEIGHT / 2) - LOCKED_OFFSET) * 2;

                    im.setPosition(newX, newY);

                    // Change back to low version of asset
                    im.setDrawable(nodeLow);
                }
            }
        }));

        // Add click listener that does something when the node is clicked
        im.addListener((new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
            // Different behavior on clicked depending on editor mode
            switch (editorMode) {
                // In Edit Mode, allow node stress rating to be set
                case EDIT:
                    // If a node was previously selected, revert it to deselected
                    if (selectedNode != null) {
                        // First digit of node name gives the node type, so set node image to be low version of itself
                        selectedNode.setDrawable(NODE_TRDS[Character.getNumericValue(selectedNode.getName().charAt(0))]);
                    }
                    // Change clicked node to the node that was just clicked
                    selectedNode = im;
                    // Change to high version of asset to indicate it's been selected
                    im.setDrawable(new TextureRegionDrawable(nodeHigh));

                    // Change the appearance and name of the stress rating button to reflect the SR of this node
                    int ind = find(nodeSRs.get(im.getName()),SR_ORDER);
                    nodeStressRating.setDrawable(SR_TRD_ORDER[ind]);
                    nodeStressRating.setName(SR_NAME_ORDER[ind]);

                    break;
                // In Delete Mode, delete the node
                case DELETE:
                    im.remove();
                    break;
                default:
                    break;
            }
            }
        }));
    }

    /*********************************************** CONNECTORS ***********************************************/

    /**
     * Adds a connector to the grid tile at the given coordinates.
     *
     * Called when right-clicking anywhere in Draw Mode. Coordinates given are in screen space.
     *
     * @param x     Screen space x-coordinate of the location to add the connector at
     * @param y     Screen space y-coordinate of the location to add the connector at
     */
    public void addConnector(float x, float y) {
        // Convert mouse position from screen to world coordinates
        vec = camera.screenToWorld(x,y);
        x = vec.x;
        y = vec.y;

        // Create connector image, defaulting to the North connector
        final Image im = new Image(new Texture(Gdx.files.internal(Connector.getAssetPath(Direction.N))));
        nodeStage.addActor(im);

        // Set name of image, which defaults to "N"
        im.setName("N");
        // Set scale
        im.setScale(0.5f);

        // TODO: put this back when isometric grid is moved to the canvas, not the stage
        // Always move to the back
        //im.toBack();

        // Get nearest isometric center to where the mouse clicked and fetch from vector cache
        nearestIsoCenter(x,y);
        x = vec.x;
        y = vec.y;
        // Place connector at nearest isometric center
        im.setPosition(x - (TILE_WIDTH / 4), y - (TILE_HEIGHT / 4));
        im.setOrigin(0, 0);
        // Add image to images
        images.add(im);

        // Add listeners, which change their behavior depending on the editor mode
        // Add click listener that does something when the connector is left-clicked
        im.addListener((new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
            // Different behavior on clicked depending on editor mode
            switch (editorMode) {
                // In Draw Mode, rotate the connector
                case DRAW:
                    // Set the appearance and name to be the next connector
                    int nextConn = nextEntry(im.getName(), CONN_NAME_ORDER);
                    im.setName(CONN_NAME_ORDER[nextConn]);
                    im.setDrawable(new TextureRegionDrawable(
                            new Texture(Gdx.files.internal(
                                    // Path to connector asset
                                    Connector.getAssetPath(CONN_ORDER[nextConn])
                            ))));
                    break;
                // In Delete Mode, delete the connector
                case DELETE:
                    im.remove();
                    break;
                default:
                    break;
            }
            }
        }));
    }

    /*********************************************** SCREEN METHODS ***********************************************/

    @Override
    public void show() {
    }

    /**
     * Helper function that clears all images in the level.
     *
     * Called if the clear button "C" is pressed.
     */
    private void clearLevel() {
        // Delete all images
        images.clear();
        // Clear saved data about node stress ratings
        nodeSRs.clear();
        // Reset image count
        imgCount = 0;
    }

    /**
     * Helper function that undos the creation of the last image.
     *
     * Called if the undo button "Z" is pressed.
     */
    private void undo() {
        // Do nothing if no images have been created
        if (images.size <= 0) {return;}
        // Remove last created image from array of created images
        Image lastIm = images.pop();
        // If last created image had its stress rating tracked, remove that as well
        if(nodeSRs.containsKey(lastIm.getName())) {
            nodeSRs.removeIndex(nodeSRs.indexOfKey(lastIm.getName()));
        }
        // Actually remove image from screen
        lastIm.remove();

    }

    @Override
    /**
     * renders the game display at consistent time steps
     */
    public void render(float delta) {
        // PREUPDATE
        // If C button is pressed, clear the level
        if (input.didClear()) {clearLevel();}
        // If Z button is pressed, delete last object that was created
        if (input.didUndo()) {undo();}

        // Handle making connectors if in Draw Mode and a connector was made
        if (editorMode == Mode.DRAW && input.didRightClick()) {
            addConnector(input.getX(), input.getY());
        }

        // Move camera
        canvas.clear();
        camera.moveCamera();

        // UPDATE
        // Draw objects on canvas
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        nodeStage.act(delta);
        toolstage.act(delta);

        nodeStage.draw();
        toolstage.draw();
    }

    @Override
    /**
     * Ensures that the game world appears at the same scale, even when resizing
     */
    public void resize(int width, int height) {
        // Keep game world at the same scale even when resizing
        nodeStage.getViewport().update(width,height,true);
        camera.resize(width, height);

        // Keep toolbar in the same place when resizing
        toolstage.getViewport().update(width,height,true);

        canvas.resize();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }


}
