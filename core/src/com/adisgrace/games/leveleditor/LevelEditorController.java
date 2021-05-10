package com.adisgrace.games.leveleditor;

import com.adisgrace.games.*;
import com.adisgrace.games.util.Connector;
import com.adisgrace.games.util.Connector.*;
import static com.adisgrace.games.leveleditor.LevelEditorConstants.*;
import static com.adisgrace.games.util.GameConstants.*;

import com.adisgrace.games.util.GameConstants;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
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
    /**
     * Enumeration representing the current mode of the level editor
     */
    private enum Mode {
        /**
         * Move Mode: nodes can be freely moved around onscreen
         */
        MOVE,
        /**
         * Edit Mode: nodes can be clicked on, after which their contents can be edited
         */
        EDIT,
        /**
         * Delete Mode: nodes can be clicked on, which deletes them
         */
        DELETE,
        /**
         * Draw Mode: connections can be drawn between nodes
         */
        DRAW
    }

    /** Model for level created in the level editor */
    private final LevelEditorModel model;
    /** Parser to use to convert models to JSONs */
    private final LevelEditorParser parser = new LevelEditorParser();
    /** Canvas is the primary view class of the game */
    private final GameCanvas canvas = new GameCanvas();
    /** Gets player input */
    private final InputController input = InputController.getInstance();
    /** Controller for view camera for node map */
    private final CameraController camera = new CameraController(input, canvas);

    /** Stage where nodes and connectors are drawn */
    private Stage nodeStage;
    /** Stage where buttons are drawn on */
    private Stage toolStage;

    /**
     * Table for target edit form
     */
    private final Table targetForm = new Table();
    /**
     * Table for node edit form
     */
    private final Table nodeForm = new Table();
    /**
     * Background for the forms
     */
    private final Image formBG = new Image(SR_TRD_BLANK);

    /**
     * Image representing the current node that is being clicked on
     */
    private Image selectedNode;
    /**
     * If a prior selected node was deselected in favor of selecting a new node, this is the new node
     */
    private Image newSelectedNode;
    /**
     * If the form background was clicked
     */
    private boolean wasFormBGClicked = false;
    /** If the most recent node was set to generic/not generic. Checking off one target/node as generic should
     * then make all subsequent nodes created generic, and vice versa for not generic. */
    private boolean wasLastGeneric = true;

    /**
     * Current mode of the level editor, initialized as Move mode
     */
    private Mode editorMode = Mode.MOVE;
    /** Label indicating the current mode of the level editor */
    private Label editorModeLabel;

    /**
     * Vector cache to avoid initializing vectors every time
     */
    private Vector2 vec = new Vector2();

    /**
     * Array of modes
     */
    private static final Mode[] MODE_ORDER = {Mode.MOVE, Mode.EDIT, Mode.DELETE, Mode.DRAW};

    /**
     * TextField that holds the name of the level being worked on
     */
    private TextField levelName;
    /**
     * TextFields that hold the dimensions of the level being worked on
     */
    private TextField levelDimX;
    private TextField levelDimY;

    /************************************************* CONSTRUCTOR *************************************************/

    /**
     * Creates a new level editor controller.
     *
     * This constructor is only called when the level editor is opened for a new level.
     */
    public LevelEditorController() {
        // Create a new model
        model = new LevelEditorModel();
        // Initialize the rest of the level editor
        initializeLevelEditor();
    }

    /**
     * Creates a new level editor controller.
     *
     * This constructor is only called when the level editor is opened for a previously-saved level.
     *
     * @param levelfile     The filename of the previously-saved level to load into the level editor.
     */
    public LevelEditorController(String levelfile) {
        // Create a model based on a previously-saved level file
        model = parser.loadLevel(levelfile);
        // Initialize the rest of the level editor
        initializeLevelEditor();

        // Populate the level based on the model
        repopulateLevel();
    }

    /**
     * Helper function that draws all the tiles that are stored in the model.
     *
     * Called when loading a previously-created level into the level editor.
     */
    private void repopulateLevel() {
        // Get the level data from the model
        ArrayMap<String, LevelEditorModel.LevelTile> levelTiles = model.getLevelTiles();
        ArrayMap<Vector2, Array<String>> levelMap = model.getLevelMap();
        // Initialize vector to hold each location
        Vector2 loc;
        // Initialize array to hold the names of the tiles at each location
        Array<String> tilesAtLoc;
        // Initialize level tile for tile at location
        LevelEditorModel.LevelTile lt;

        // Go through the level map and place whatever is at each location at that location
        for (int k=0; k<levelMap.size; k++) {
            loc = levelMap.getKeyAt(k);
            tilesAtLoc = levelMap.getValueAt(k);
            // Go through each tile at this location
            for (int i=0; i<tilesAtLoc.size; i++) {
                // Get the tile at this location
                lt = levelTiles.get(tilesAtLoc.get(i));
                // Depending on its type, add a different image to the level
                switch (lt.tileType) {
                    case TARGET:
                        // Add target to level
                    case NODE:
                        // Add node to level
                        addNode((int)loc.x, (int)loc.y, lt.im);
                        break;
                    case CONNECTOR:
                        // Add connector to level
                        addConnector((int)loc.x, (int)loc.y, lt.im);
                        break;
                }
            }
        }

        // Set the level name and dimensions to what was read
        levelName.setText(model.getLevelName());
        levelDimX.setText(String.valueOf(model.getLevelWidth()));
        levelDimY.setText(String.valueOf(model.getLevelHeight()));
    }

    /**
     * Initializes the UI and sets up the isometric grid.
     */
    private void initializeLevelEditor() {
        // Set the input controller to be what to ignore input from when in forms
        FormFactory.setInputController(input);
        // Set up camera
        ExtendViewport viewport = new ExtendViewport(canvas.getWidth(), canvas.getHeight());
        camera.setViewport(viewport);

        // Create stage for grid and tile with isometric grid
        nodeStage = new Stage(viewport);
        // Ensure that if something that isn't a node is clicked, lose focus
        nodeStage.getRoot().addCaptureListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                // If thing that is clicked isn't a node
                if (!(event.getTarget() instanceof Image)) {
                    newSelectedNode = null;
                }
                return false;
            }
        });

        // Create tool stage for buttons
        createToolStage();

        // Preemptively add form background to the stage and make semi-transparent, but keep hidden
        formBG.setVisible(false);
        toolStage.addActor(formBG);
        formBG.setColor(0.3f, 0.3f, 0.3f, 0.8f);
        formBG.setName("background");

        // Preemptively add node and target forms to the stage
        toolStage.addActor(targetForm);
        toolStage.addActor(nodeForm);

        // Create the overlays that give/set info about the level being worked on
        createLevelInfoOverlays();
    }

    /**
     * Helper function that creates the overlays that give/set info about the level being worked on and
     * adds them to the tool stage, also placing them accordingly.
     *
     * This includes:
     * - A TextField to input the level name.
     * - A TextField to input the level width, and the relevant Label.
     * - A TextField to input the level height, and the relevant Label.
     * - A Label that tells the user what the editor mode is.
     */
    private void createLevelInfoOverlays() {
        // Add text field for level name at the bottom of the screen
        levelName = FormFactory.newTextField("Level Name", 10 + FORM_GAP,
                FORM_WIDTH * canvas.getWidth(), "My Level");
        levelName.setX((GameConstants.SCREEN_WIDTH / 2f) - 0.5f * levelName.getWidth());
        // Align text to center
        levelName.setAlignment(1);
        toolStage.addActor(levelName);

        // Right below, put two text fields for dimensions of the screen
        levelDimX = FormFactory.newTextField("Level Width", 10,
                FORM_WIDTH * canvas.getWidth() / 4, "20");
        levelDimY = FormFactory.newTextField("Level Height", 10,
                FORM_WIDTH * canvas.getWidth() / 4, "20");
        // Add to stage
        toolStage.addActor(levelDimX);
        toolStage.addActor(levelDimY);
        // Create labels for what these text fields are and add to stage
        Label levelDimXLabel = new Label("Width", skin);
        Label levelDimYLabel = new Label("Height", skin);
        toolStage.addActor(levelDimXLabel);
        toolStage.addActor(levelDimYLabel);

        // Place at bottom of screen
        levelDimX.setX((GameConstants.SCREEN_WIDTH / 2f) - levelDimX.getWidth());
        levelDimY.setX((GameConstants.SCREEN_WIDTH / 2f) + levelDimX.getWidth());
        // Align to center
        levelDimX.setAlignment(1);
        levelDimY.setAlignment(1);
        // Place labels next to the relevant fields
        levelDimXLabel.setPosition((GameConstants.SCREEN_WIDTH / 2f) - 1.5f * levelDimX.getWidth() - levelDimXLabel.getWidth() / 2, 15);
        levelDimYLabel.setPosition((GameConstants.SCREEN_WIDTH / 2f) + 0.5f * levelDimY.getWidth() - levelDimYLabel.getWidth() / 2, 15);

        // Add label indicating what the current editor mode is
        editorModeLabel = FormFactory.newLabel("MOVE MODE", GameConstants.SCREEN_HEIGHT - 40);
        editorModeLabel.setAlignment(1);
        editorModeLabel.setX((GameConstants.SCREEN_WIDTH / 2f) - editorModeLabel.getWidth()/2);
        toolStage.addActor(editorModeLabel);
    }

    /************************************************** TOOLBAR **************************************************/

    /**
     * Creates and fills the stage with buttons to be used in creating a level.
     * <p>
     * These include:
     * - A button to create a new target.
     * - A button to create a new unlocked node.
     * - A button to create a new locked node.
     */
    private void createToolStage() {
        // Creates toolbar viewport and camera
        FitViewport toolbarViewPort = new FitViewport(canvas.getWidth(), canvas.getHeight());
        toolStage = new Stage(toolbarViewPort);

        // Handle inputs from both stages with a Multiplexer
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(toolStage);
        inputMultiplexer.addProcessor(nodeStage);
        Gdx.input.setInputProcessor(inputMultiplexer);

        // Add all interface buttons to the screen
        createInterfaceButtons();

        // Set stage to lose focus when clicking on someplace not in a form
        toolStage.getRoot().addCaptureListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                // If not part of a form, lose focus
                if (!(event.getTarget() instanceof TextField || event.getTarget() instanceof SelectBox
                        || event.getTarget() instanceof TextArea))
                    toolStage.setKeyboardFocus(null);
                // If the form background was clicked or a label was clicked, don't lose focus
                if ((event.getTarget() instanceof Image && event.getTarget().getName() != null
                        && event.getTarget().getName().equals("background")) || event.getTarget() instanceof Label)
                    wasFormBGClicked = true;
                return false;
            }
        });
    }

    /**
     * Helper function that changes the editor mode to the given mode.
     *
     * @param mode Mode to change the editor to
     */
    private void changeEditorMode(Mode mode) {
        // Save and clear any edit forms that are shown
        saveAndClearForm(targetForm);
        saveAndClearForm(nodeForm);

        // Hide form background
        formBG.setVisible(false);

        // Deselect any currently-selected nodes
        if (selectedNode != null) {
            deselectNode();
            newSelectedNode = null;
        }

        // Change label based on new mode
        switch (mode) {
            case EDIT:
                editorModeLabel.setText("EDIT MODE");
                break;
            case DRAW:
                editorModeLabel.setText("DRAW MODE");
                break;
            case MOVE:
                editorModeLabel.setText("MOVE MODE");
                break;
            case DELETE:
                editorModeLabel.setText("DELETE MODE");
                break;
        }

        // Change mode
        editorMode = mode;
    }

    /**
     * Function that adds the interface buttons to the stage, placing/displaying them and setting
     * what they do when clicked.
     *
     * On the left for nodes, these include:
     * - A button to create a new target.
     * - A button to create a new unlocked node.
     * - A button to create a new locked node.
     *
     * On the right for editor modes, these include:
     * - A button to change to Move Mode, where nodes can be moved around.
     * - A button to change to Edit Mode, where the contents of nodes can be edited.
     * - A button to change to Delete Mode, where nodes can be deleted.
     * - A button to change to Draw Mode, where connectors can be added.
     * - A button to save the level, which is not the same, but it looks good here.
     */
    private void createInterfaceButtons() {
        // Height of first button
        int height = (int) camera.getHeight() - TOOLBAR_Y_OFFSET;
        // Right offset of mode buttons
        int xlocMode = canvas.getWidth() - TOOLBAR_X_OFFSET - BUTTON_WIDTH;
        // Initialize other variables for button creation
        Drawable drawable;
        ImageButton button;

        // Loop through and create each button
        for (int k = 0; k < CHANGE_MODE_TRD_ORDER.length; k++) {
            // If there are still node buttons left to create, create and place node button
            if (k < ADD_NODE_TRD_ORDER.length) {
                // Create and place node button
                drawable = ADD_NODE_TRD_ORDER[k];
                button = new ImageButton(drawable);
                button.setTransform(true);
                button.setScale(GameConstants.BUTTON_SCALE);
                button.setPosition(TOOLBAR_X_OFFSET, height);

                // Set node type that this button will create
                final int nodeType = k;

                // Add listeners to button, changing depending on which node the button creates
                button.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        addNode(nodeType);
                    }
                });

                // Add button to stage
                toolStage.addActor(button);
            }

            // Create and place mode button
            drawable = CHANGE_MODE_TRD_ORDER[k];
            button = new ImageButton(drawable);
            button.setTransform(true);
            button.setScale(GameConstants.BUTTON_SCALE);
            button.setPosition(xlocMode, height);

            // For the actual mode creation buttons, do that
            if (k < CHANGE_MODE_TRD_ORDER.length - 1) {
                final Mode newMode = MODE_ORDER[k];

                // Add listeners to button, changing depending on which node the button creates
                // Changes the editor mode to the one determined by the button
                button.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        changeEditorMode(newMode);
                    }
                });
            }
            // But make the save level button differently
            else {
                button.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        // Get level name from relevant text box
                        model.setLevelName(levelName.getText());

                        // If any forms are open, save and clear those
                        saveAndClearForm(targetForm);
                        saveAndClearForm(nodeForm);
                        // Hide form background
                        formBG.setVisible(false);

                        // Save the level
                        boolean didLevelSave = parser.saveLevel(model);
                        // Set label to indicate whether or not level save was successful
                        editorModeLabel.setText(didLevelSave ? "Level Saved Successfully" : "Failed to Save Level");
                    }
                });
            }

            // Add button to stage
            toolStage.addActor(button);
            // Increment height
            height -= BUTTON_GAP;
        }
    }

    /*********************************************** NODES ***********************************************/

    /**
     * Adds a draggable node of the given type to the stage.
     *
     * Called only when one of the node-adding buttons is pressed. Each image is given a name that is a number
     * of increasing value, so that no names are repeated in a single level editor session.
     *
     * This function also contains all the behaviors of each node and what it does when it is interacted
     * with.
     *
     * @param nodeType 0: target, 1: unlocked, 2: locked
     */
    private void addNode(int nodeType) {
        // Create image for the node
        Image im = new Image(NODE_TEXTURES[nodeType]);
        // Set node name, which is the node type, the string "Node," and a unique number
        im.setName(nodeType + "Node" + model.imgCount);

        // Add node to level editor
        addNode(0,0, im, true);
    }

    /**
     * Adds a draggable node of the given type to the stage.
     *
     * Called only when repopulating the level editor with saved level data.
     *
     * This function also contains all the behaviors of each node and what it does when it is interacted
     * with.
     *
     * Note that the node type is given by the first digit of the image name as follows:
     * - 0: target
     * - 1: unlocked
     * - 2: locked
     *
     * @param x     x-coordinate of node, in isometric coordinates
     * @param y     y-coordinate of node, in isometric coordinates
     * @param image Image representing the node
     */
    private void addNode(int x, int y, Image image) {
        addNode(x,y,image,false);
    }

    /**
     * Adds a draggable node of the given type to the stage.
     *
     * Called when one of the node-adding buttons is pressed or when populating the level editor with
     * saved level data. When creating a new node, each image is given a name that is a number of increasing
     * value, so that no names are repeated in a single level editor session.
     *
     * This function also contains all the behaviors of each node and what it does when it is interacted
     * with.
     *
     * Note that the node type is given by the first digit of the image name as follows:
     * - 0: target
     * - 1: unlocked
     * - 2: locked
     *
     * @param x     x-coordinate of node, in isometric coordinates
     * @param y     y-coordinate of node, in isometric coordinates
     * @param image Image representing the node
     * @param isNew Whether this node is new, meaning it isn't being loaded in from a saved level
     */
    private void addNode(int x, int y, Image image, boolean isNew) {
        // Create image
        final Image im = image;
        nodeStage.addActor(im);

        // Convert from isometric to world space
        vec.set(x,y);
        isometricToWorld(vec);
        x = (int)vec.x;
        y = (int)vec.y;
        // Account for difference between tile width and sprite width
        x -= (im.getWidth() - GameConstants.TILE_WIDTH) / 2;
        y += ((GameConstants.TILE_HEIGHT / 2) - GameConstants.LOCKED_OFFSET) * 2;
        // Place node
        im.setPosition(x,y);
        im.setOrigin(0, 0);

        // If node is new, add it to the model
        if (isNew) model.addToLevel(im, 0, 0, wasLastGeneric);

        // Get node type
        int nodeType = Character.getNumericValue(im.getName().charAt(0));

        // Get relevant low and high textures for this node
        final TextureRegionDrawable nodeLow = NODE_TRDS[nodeType];
        final TextureRegionDrawable nodeHigh = NODE_TRDS[nodeType + 3];

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
                    vec.set(newX, newY);
                    nearestIsoCenter(vec);
                    newX = vec.x;
                    newY = vec.y;
                    // Update LevelTile with new isometric location
                    model.updateLevelTileLocation(im.getName(), newX, newY);
                    // Convert to world space
                    vec.set(newX, newY);
                    isometricToWorld(vec);
                    // Retrieve from vector cache
                    newX = vec.x;
                    newY = vec.y;
                    // Account for difference between tile width and sprite width
                    newX -= (im.getWidth() - GameConstants.TILE_WIDTH) / 2;
                    newY += ((GameConstants.TILE_HEIGHT / 2) - GameConstants.LOCKED_OFFSET) * 2;
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
                    // In Edit Mode, select the node
                    case EDIT:
                        // If a different node was previously selected
                        if (nodeNotEquals(selectedNode, im)) {
                            newSelectedNode = im;
                        }
                        // Change to high version of asset to indicate it's been selected
                        im.setDrawable(new TextureRegionDrawable(nodeHigh));
                        break;
                    // In Delete Mode, delete the node
                    case DELETE:
                        model.removeFromLevel(im.getName());
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
     * Adds a connector to the grid tile at the given screen coordinates.
     *
     * Called only when right-clicking anywhere in Draw Mode. Coordinates given are in screen space.
     *
     * @param x Screen space x-coordinate of the location that was clicked to add the connector at
     * @param y Screen space y-coordinate of the location that was clicked to add the connector at
     */
    private void addConnector(float x, float y) {
        // Create connector image, defaulting to the North connector
        Image im = new Image(Connector.getTexture(Direction.N));
        // Set scale
        im.setScale(0.5f);
        // Set name of connector, which defaults to "N". The first letter is the connector, the second
        // is a unique identifier.
        im.setName("N" + model.imgCount);

        // Convert mouse position from screen to world coordinates
        vec = camera.screenToWorld(x, y);
        x = vec.x;
        y = vec.y;
        // Get nearest isometric center to where the mouse clicked
        vec.set(x,y);
        nearestIsoCenter(vec);
        x = vec.x;
        y = vec.y - 1; // For some reason this is consistently off by 1, so we take care of that this way

        // Add connector to level editor
        addConnector(x,y,im,true);
    }

    /**
     * Adds a connector to the grid tile at the given isometric coordinates.
     *
     * Called only when initially populating the level editor with saved level data.
     *
     * @param x     Isometric x-coordinate of the location to add the connector at
     * @param y     Isometric y-coordinate of the location to add the connector at
     * @param image Image of the connector being placed
     */
    private void addConnector(float x, float y, Image image) {
        addConnector(x,y,image,false);
    }

    /**
     * Adds a connector to the grid tile at the given isometric coordinates.
     *
     * Called when right-clicking anywhere in Draw Mode or when populating the level editor with saved
     * level data.
     *
     * @param x     Isometric x-coordinate of the location to add the connector at
     * @param y     Isometric y-coordinate of the location to add the connector at
     * @param image Image of the connector being placed
     * @param isNew Whether this connector is new, meaning it isn't being loaded in from a saved level
     */
    private void addConnector(float x, float y, Image image, boolean isNew) {
        // Make image final
        final Image im = image;
        // Add to stage
        nodeStage.addActor(im);

        // If connector is new, add it to the model
        if (isNew) model.addToLevel(im, x, y);

        // Convert to world space
        vec.set(x, y + 1); // Don't know why it needs to be y+1, but it does
        isometricToWorld(vec);
        // Retrieve from vector cache
        x = vec.x;
        y = vec.y;
        // Place connector at nearest isometric center
        im.setPosition(x - (GameConstants.TILE_WIDTH / 4), y - (GameConstants.TILE_HEIGHT / 4));
        im.setOrigin(0, 0);

        // Add listeners, which change their behavior depending on the editor mode
        // Add click listener that does something when the connector is left-clicked
        im.addListener((new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                // Different behavior on clicked depending on editor mode
                switch (editorMode) {
                    // In Draw Mode, rotate the connector
                    case DRAW:
                        // Set the appearance and name to be the next connector
                        int nextConn = nextEntry(String.valueOf(im.getName().charAt(0)), CONN_NAME_ORDER);
                        String name = CONN_NAME_ORDER[nextConn] + im.getName().substring(1);
                        model.updateLevelTileName(im.getName(), name);
                        im.setName(name);
                        im.setDrawable(new TextureRegionDrawable(Connector.getTexture(CONN_ORDER[nextConn])));
                        break;
                    // In Delete Mode, delete the connector
                    case DELETE:
                        model.removeFromLevel(im.getName());
                        im.remove();
                        break;
                    default:
                        break;
                }
            }
        }));
    }

    /*********************************************** EDIT MODE FORMS ***********************************************/

    /**
     * Creates the form for writing target information for the given target and places it in the toolStage.
     * <p>
     * This function takes in a target, which would be the selected node if the selected node is a target.
     * <p>
     * The entries include, for targets specifically:
     * [1] A CheckBox for whether or not the target is generic.
     * [3] A TextField to enter the target name.
     * [5] A TextField to enter the target's paranoia stat.
     * [7] A TextField to enter the target's maximum stress.
     * [9] A SelectBox dropdown menu to select target traits (multiple options can be selected).
     *
     * @param target The target that this form handles the information for
     * @param height Height of each entry in the form, in terms of pixels from the bottom
     * @param width  Width of each entry in the form
     */
    private void createTargetForm(Image target, float height, float width) {
        // Get TargetTile for target this form is for
        LevelEditorModel.TargetTile tt = model.getTargetTile(target.getName());

        // Place table to contain target form entries
        targetForm.left();
        targetForm.bottom();
        targetForm.setSize(FORM_WIDTH * GameConstants.SCREEN_WIDTH, height);

        // FORM LABEL
        targetForm.addActor(FormFactory.newLabel("TARGET DATA", height));
        // CHECKBOX FOR WHETHER TARGET IS GENERIC
        final CheckBox generic = FormFactory.newCheckBox("Generic", height, tt.isGeneric);
        targetForm.addActor(generic);
        height -= FORM_GAP;
        // Listener for what to do when checkbox is changed
        generic.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                // Set global generic setting based on what was checked last
                wasLastGeneric = generic.isChecked();
            }});

        // TARGET NAME
        targetForm.addActor(FormFactory.newLabel("Name", height));
        height -= FORM_GAP;
        targetForm.addActor(FormFactory.newTextField("Target Name", height, width, tt.name));
        height -= FORM_GAP;

        // TARGET PARANOIA
        targetForm.addActor(FormFactory.newLabel("Paranoia", height));
        height -= FORM_GAP;
        targetForm.addActor(FormFactory.newTextField("Target Paranoia", height, width, String.valueOf(tt.paranoia)));
        height -= FORM_GAP;

        // TARGET MAX STRESS
        targetForm.addActor(FormFactory.newLabel("Max Stress", height));
        height -= FORM_GAP;
        targetForm.addActor(
                FormFactory.newTextField("Target Max Stress", height, width, String.valueOf(tt.maxStress)));
        height -= FORM_GAP;

        // TARGET TRAITS
        targetForm.addActor(FormFactory.newLabel("Traits (hold CTRL to deselect)", height));
        height -= 8 * FORM_GAP;
        // Set selected target traits to be what's already selected
        targetForm.addActor(FormFactory.newListBox(TRAIT_OPTIONS, height, width, tt.traits));
    }

    /**
     * Creates the forms for writing target/node information and places them in the toolStage.
     * <p>
     * These include, for nodes specifically:
     * [1] A CheckBox for whether or not the node is generic.
     * [3] A TextField to enter the node title.
     * [5] A TextArea to write the node content (what's seen when scanned).
     * [7] A TextArea to write the node summary (what goes into the notebook).
     * [9] A SelectBox dropdown menu to select the node's target stress rating (only one option can be selected).
     * [11] A SelectBox dropdown menu to select the node's player stress rating (only one option can be selected).
     *
     * @param node   The node that this form handles the information for
     * @param height Height of each entry in the form, in terms of pixels from the bottom
     * @param width  Width of each entry in the form
     */
    private void createNodeForm(Image node, float height, float width) {
        // Get NodeTile for node this form is for
        LevelEditorModel.NodeTile nt = model.getNodeTile(node.getName());

        // Place table to contain node form entries
        nodeForm.left();
        nodeForm.bottom();
        nodeForm.setSize(FORM_WIDTH * GameConstants.SCREEN_WIDTH, height);

        // FORM LABEL
        nodeForm.addActor(FormFactory.newLabel("NODE DATA", height));

        // CHECKBOX FOR WHETHER NODE IS GENERIC
        final CheckBox generic = FormFactory.newCheckBox("Generic", height, nt.isGeneric);
        nodeForm.addActor(generic);

        height -= FORM_GAP;

        // NODE TITLE
        nodeForm.addActor(FormFactory.newLabel("Title", height));
        height -= FORM_GAP;
        final TextField title = FormFactory.newTextField("Node Title", height, width, nt.getTitle());
        nodeForm.addActor(title);
        height -= FORM_GAP;

        // NODE CONTENT
        nodeForm.addActor(FormFactory.newLabel("Content", height));
        height -= 3 * FORM_GAP + 10;
        final TextArea content = FormFactory.newTextArea("Node Content", height, width, nt.getContent(), 3);
        nodeForm.addActor(content);
        height -= FORM_GAP;

        // NODE SUMMARY
        nodeForm.addActor(FormFactory.newLabel("Summary", height));
        height -= 2 * FORM_GAP + 10;
        final TextArea summary = FormFactory.newTextArea("Node Summary", height, width, nt.getSummary(), 2);
        nodeForm.addActor(summary);
        height -= FORM_GAP;

        // NODE TARGET STRESS RATING
        nodeForm.addActor(FormFactory.newLabel("Target Stress Rating", height));
        height -= 1.5f * FORM_GAP;
        final SelectBox targetSR = FormFactory.newSelectBox(SR_ORDER, height, width, nt.targetSR);
        nodeForm.addActor(targetSR);
        height -= FORM_GAP;

        // NODE PLAYER STRESS RATING
        nodeForm.addActor(FormFactory.newLabel("Player Stress Rating", height));
        height -= 1.5f * FORM_GAP;
        nodeForm.addActor(FormFactory.newSelectBox(SR_ORDER, height, width, nt.playerSR));


        // Handle behavior of form when generic checkbox is checked off (down here so as to be able to access other
        // parts of the form)

        // Node name as a final
        final String nodeName = node.getName();

        // Listener for what to do when generic checkbox is changed
        generic.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                boolean isGeneric = generic.isChecked();
                // Set global generic setting based on what was checked last
                wasLastGeneric = isGeneric;

                // Set content, summary, and title to be disabled if generic, or enabled otherwise
                title.setDisabled(isGeneric);
                content.setDisabled(isGeneric);
                summary.setDisabled(isGeneric);

                // If went from not generic to generic, save any previously-entered data
                if (isGeneric) {
                    model.updateNodeTitle(nodeName, title.getText());
                    model.updateNodeContent(nodeName, content.getText());
                    model.updateNodeSummary(nodeName, summary.getText());
                }

                // If it is generic, display a generic message that indicates stress rating rather
                // than custom content/summary. If not, set it to the stored text.
                LevelEditorModel.NodeTile nt = model.getNodeTile(nodeName);
                StressRating sr = (StressRating) targetSR.getSelected();
                content.setText(isGeneric ? getGenericContent(sr) : nt.content);
                summary.setText(isGeneric ? getGenericSummary(sr) : nt.summary);
                // Same for title, except title is always the same and doesn't depend on stress rating for generics
                title.setText(isGeneric ? GENERIC_TITLE : nt.title);
            }
        });

        // Listener for what to do when target stress rating is changed
        targetSR.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                boolean isGeneric = generic.isChecked();
                // If node is generic, update content/summary each time the stress rating changes
                StressRating sr = (StressRating) targetSR.getSelected();
                content.setText(isGeneric ? getGenericContent(sr) : model.getNodeTile(nodeName).content);
                summary.setText(isGeneric ? getGenericSummary(sr) : model.getNodeTile(nodeName).summary);
            }
        });
    }


    /**
     * Saves the information in the given form as that of the given target/node.
     * <p>
     * This should be done by editing the properties of the LevelTile with
     * the name im.getName() (cast to TargetTile/NodeTile as needed, you can
     * determine which to cast to with the first digit of im.getName() - 0 for
     * target, 1 or 2 for node).
     *
     * @param form The Table containing all the target/node information.
     * @param im   The Image of the target/node.
     */
    private void saveForm(Table form, Image im) {
        String name = im.getName();
        int nodetype = Character.getNumericValue(name.charAt(0));

        // If node is a Target
        if (nodetype == 0) {
            // Save whether or not target is generic
            model.updateTargetIsGeneric(name, ((CheckBox) form.getChild(1)).isChecked());

            // Save name
            model.updateTargetName(name, ((TextField) form.getChild(3)).getText());

            // Save paranoia
            model.updateTargetParanoia(name, String.valueOf(((TextField) form.getChild(5)).getText()));

            // Save max stress
            model.updateTargetMaxStress(name, String.valueOf(((TextField) form.getChild(7)).getText()));

            // Save traits
            ArraySelection selection = ((List) form.getChild(9)).getSelection();
            model.updateTargetTraits(name, selection.toArray());
        }
        // If node is Unlocked or Locked
        else if (nodetype == 1 || nodetype == 2) {
            // Save whether or not node is generic
            model.updateNodeIsGeneric(name, ((CheckBox) form.getChild(1)).isChecked());

            // Save title (note that the title is different from the name)
            model.updateNodeTitle(name, ((TextField) form.getChild(3)).getText());

            // Save content
            model.updateNodeContent(name, ((TextField) form.getChild(5)).getText());

            // Save summary
            model.updateNodeSummary(name, ((TextField) form.getChild(7)).getText());

            // Save target stress rating
            model.updateNodeTargetStressRating(name, (StressRating) ((SelectBox) form.getChild(9)).getSelected());

            // Save player stress rating
            model.updateNodePlayerStressRating(name, (StressRating) ((SelectBox) form.getChild(11)).getSelected());
        }
    }

    /**
     * Helper function that saves and clears a form for the currently selected node.
     * <p>
     * Called when a node is deselected, or when the editor mode changes.
     *
     * @param form The form to save and clear.
     */
    private void saveAndClearForm(Table form) {
        // If form is not showing, do nothing
        if (!form.hasChildren()) return;
        saveForm(form, selectedNode);
        form.clear();
    }

    /*********************************************** SCREEN METHODS ***********************************************/

    @Override
    public void show() {
    }

    /**
     * Clears all images in the level.
     * <p>
     * Called if the clear button "C" is pressed.
     */
    private void clearLevel() {
        model.clear();
        // Reset image count
        model.imgCount = 0;
    }

    /**
     * Helper function that deselects the currently-selected node.
     */
    private void deselectNode() {
        // If no node is currently selected, do nothing
        if (selectedNode == null) return;
        // First digit of node name gives the node type, so set node image to be low version of itself
        selectedNode.setDrawable(NODE_TRDS[Character.getNumericValue(selectedNode.getName().charAt(0))]);
        // Actually deselect node
        selectedNode = null;
    }

    /**
     * Handles displaying of forms depending on the node that's selected, as well
     * as saves the information of a node that was deselected.
     */
    private void displayEditForms() {
        // If it was only the form background that was clicked, set the new selected node to be the same as the old
        if (wasFormBGClicked) newSelectedNode = selectedNode;

        // If the currently selected and to-be-selected nodes are different
        if (nodeNotEquals(selectedNode, newSelectedNode)) {
            // Hide form background
            formBG.setVisible(false);

            // Initialize variable for node type
            int nodeType;

            // If a node was actually previously selected, save the form data for it
            if (selectedNode != null) {
                // Determine if previously selected node was a target or a node
                nodeType = Character.getNumericValue(selectedNode.getName().charAt(0));
                // If was a target, save contents of targetForm for that target and clear form
                if (nodeType == 0) {
                    saveAndClearForm(targetForm);
                }
                // If was a node, save contents of nodeForm for that node and clear form
                else {
                    saveAndClearForm(nodeForm);
                }
            }

            // If a node is actually going to be selected next, create a form for it
            if (newSelectedNode != null) {
                // Initialize variable for height of each entry in the form
                float height = GameConstants.SCREEN_HEIGHT - FORM_Y_OFFSET;
                // Initialize variable for width of each entry in the form
                float width = FORM_WIDTH * GameConstants.SCREEN_WIDTH;

                // Align and show background
                formBG.setSize(width + FORM_X_OFFSET, height + FORM_GAP);
                formBG.setPosition(FORM_X_OFFSET / 2, FORM_X_OFFSET - FORM_GAP);
                formBG.setVisible(true);

                // Determine if next selected node will be a target or a node
                nodeType = Character.getNumericValue(newSelectedNode.getName().charAt(0));
                // If it's a target, create a target form
                if (nodeType == 0) {
                    createTargetForm(newSelectedNode, height, width);
                }
                // If it's a node, create a node form
                else {
                    createNodeForm(newSelectedNode, height, width);
                }
            }

            // Deselect the currently selected node
            deselectNode();
        }

        wasFormBGClicked = false;

        // Regardless, set the new selected node as the new currently selected node
        selectedNode = newSelectedNode;
    }

    @Override
    /**
     * Renders the game display at consistent time steps.
     */
    public void render(float delta) {
        // PREUPDATE
        // If C button is pressed, clear the level
        if (input.didClear()) {
            clearLevel();
        }
        // If Z button is pressed, delete last object that was created
        if (input.didUndo()) {
            model.undo();
        }

        // Handle making connectors if in Draw Mode and a connector was made
        if (editorMode == Mode.DRAW && input.didRightClick()) {
            addConnector(input.getX(), input.getY());
        }

        // If in Edit Mode, determine if edit forms need to be handled
        if (editorMode == Mode.EDIT) {
            displayEditForms();
        }

        // Resize background canvas based on input level dimensions
        try {
            model.setLevelDimensions(Integer.parseInt(levelDimX.getText()), Integer.parseInt(levelDimY.getText()));
        }
        catch (NumberFormatException nfe) {
            model.setLevelDimensions(0, 0);
        }

        // UPDATE

        // Move camera
        canvas.clear();
        camera.moveCamera();

        // Act on stages
        nodeStage.act(delta);
        toolStage.act(delta);

        // Draw objects on canvas
        canvas.drawIsometricGrid(model.getLevelWidth(), model.getLevelHeight());
        nodeStage.draw();
        toolStage.draw();
    }

    @Override
    /**
     * Ensures that the game world appears at the same scale, even when resizing
     */
    public void resize(int width, int height) {
        // Keep game world at the same scale even when resizing
        camera.resize(width, height);

        // Keep nodes in the same place when resizing
        nodeStage.getViewport().update(width,height,true);

        // Keep toolbar in the same place when resizing
        toolStage.getViewport().update(width,height,true);
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