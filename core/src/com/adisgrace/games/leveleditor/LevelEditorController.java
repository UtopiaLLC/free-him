package com.adisgrace.games.leveleditor;

import com.adisgrace.games.*;
import com.adisgrace.games.util.Connector;
import com.adisgrace.games.util.Connector.*;
import static com.adisgrace.games.leveleditor.LevelEditorConstants.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.SnapshotArray;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.io.IOException;
import java.util.Scanner;

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

    /** Model for level created in the level editor */
    private LevelEditorModel model;

    /** Canvas is the primary view class of the game */
    private final GameCanvas canvas;
    /** Gets player input */
    private final InputController input;
    /** Controller for view camera for node map */
    private final CameraController camera;

    /** Stage where nodes and connectors are drawn */
    Stage nodeStage;
    /** Stage where buttons are drawn on */
    Stage toolStage;

    /** Table for target edit form */
    Table targetForm = new Table();
    /** Table for node edit form */
    Table nodeForm = new Table();

    /** Image representing the current node that is being clicked on */
    Image selectedNode;
    /** If a prior selected node was deselected in favor of selecting a new node, this is the new node */
    Image newSelectedNode;

    /** Current mode of the level editor, initialized as Move mode */
    private Mode editorMode = Mode.MOVE;

    /** Skin for TextFields and TextAreas */
    Skin skin = new Skin(Gdx.files.internal("skins/neon-ui-updated.json"));

    /** The count of the next image that is added */
    int imgCount;

    /** Vector cache to avoid initializing vectors every time */
    private Vector2 vec = new Vector2();

    /** Array of modes */
    private static final Mode[] MODE_ORDER = {Mode.MOVE, Mode.EDIT, Mode.DELETE, Mode.DRAW};

    /*************************************************** HELPERS ****************************************************/
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
     * Helper function that converts coordinates from isometric space to world space.
     *
     * @param coords   Coordinates in isometric space to transform
     */
    private void isometricToWorld(Vector2 coords) {
        float tempx = coords.x;
        float tempy = coords.y;
        coords.x = tempx * (0.5f * TILE_WIDTH) + tempy * (0.5f * TILE_WIDTH);
        coords.y = -tempx * (0.5f * TILE_HEIGHT) + tempy * (0.5f * TILE_HEIGHT);
    }

    /**
     * Helper function that gets the center of an isometric grid tile nearest to the given coordinates.
     *
     * Called when snapping an image to the center of a grid tile.
     *
     * The nearest isometric center is just stored in the vector cache [vec], in isometric space.
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

        // Return in isometric space
        vec.set(x,y);
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
     * Helper function that compares equality between two "nodes," represented
     * as named images.
     *
     * If the names are the same, then they are equal. If they are both null,
     * then they are equal. Otherwise, they are not equal.
     *
     * @param im1   First node to compare equality of
     * @param im2   Second node to compare equality of
     * @return      Whether or not the two nodes are equal
     */
    private boolean nodeEquals(Image im1, Image im2) {
        // If both are null, then they're equal
        if (im1 == null && im2 == null) {return true;}
        // If only one is null, then they're not equal
        if (im1 == null || im2 == null) {return false;}
        // If both share the same name, then they're equal
        if (im1.getName().equals(im2.getName())) {return true;}
        // Otherwise, they are not equal
        return false;
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
        // Deselect any currently-selected nodes
        if (selectedNode != null) {deselectNode();}

        // Change mode
        editorMode = mode;
    }

    /************************************************* CONSTRUCTOR *************************************************/
    /**
     * Creates a new level editor controller. This initializes the UI and sets up the isometric
     * grid.
     */
    public LevelEditorController() {
        // Create model for level created in level editor
        model = new LevelEditorModel();

        // Create canvas and set view and zoom
        canvas = new GameCanvas();
        // Get singleton instance of player input controller
        input = InputController.getInstance();

        //canvas.setIsometricSize(4, 4);

        // Set up camera
        ExtendViewport viewport = new ExtendViewport(canvas.getWidth(), canvas.getHeight());
        camera = new CameraController(input,canvas);
        camera.setViewport(viewport);

        // Create stage for grid and tile with isometric grid
        nodeStage = new Stage(viewport);
        // Ensure that if something that isn't a node is clicked, lose focus
        nodeStage.getRoot().addCaptureListener(new InputListener() {
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                if (!(event.getTarget() instanceof Image)) {
                    newSelectedNode = null;
                }
                return false;
            }
        });

        // Create tool stage for buttons
        createToolStage();

        // Preemptively add node and target forms to the stage
        toolStage.addActor(targetForm);
        toolStage.addActor(nodeForm);
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
        toolStage = new Stage(toolbarViewPort);

        // Handle inputs from both stages with a Multiplexer
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(toolStage);
        inputMultiplexer.addProcessor(nodeStage);
        Gdx.input.setInputProcessor(inputMultiplexer);

        // Create and place toolbar to hold all the buttons
        Table toolbar = new Table();
        toolbar.right();
        toolbar.setSize(.25f*canvas.getWidth(),canvas.getHeight());

        // Add toolbar to stage
        toolStage.addActor(toolbar);

        // Add all buttons to toolbar
        createNodeButtons(toolbar);
        createModeButtons(toolbar);

        // Set stage to lose focus when clicking on someplace not in a form
        toolStage.getRoot().addCaptureListener(new InputListener() {
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                // If not a TextField or SelectBox, lose focus
                if (!(event.getTarget() instanceof TextField) && !(event.getTarget() instanceof SelectBox)
                && !(event.getTarget() instanceof TextArea))
                    toolStage.setKeyboardFocus(null);
                return false;
            }
        });
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
     * - A button to bring all nodes to the front, which is thematically similar.
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
                public void changed(ChangeEvent event, Actor actor) {
                    addNode(nodeType);
                }
            });

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
     * - A button to save the level, which is not the same, but it looks good here.
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

            // For the actual mode creation buttons, do that
            if (k < CHANGE_MODE_TRD_ORDER.length - 1) {
                final Mode newMode = MODE_ORDER[k];

                // TODO: some kind of text that shows the mode
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
                        model.saveLevel();
                        //System.out.println("Level Saved");
                    }
                });
            }

            // Arrange buttons in order using a Table
            toolbar.addActor(button);
            // Increment height
            height -= BUTTON_GAP;
        }
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
        imgCount++;

        // Add to level (stress rating will automatically initialize to None)
        model.addToLevel(im,0,0);

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
                    newX = vec.x;
                    newY = vec.y;

                    // Update LevelTile with new isometric location
                    model.updateLevelTileLocation(im.getName(), newX, newY);

                    // Convert to world space
                    vec.set(newX,newY);
                    isometricToWorld(vec);
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
                // In Edit Mode, select the node
                case EDIT:
                    // If a different node was previously selected
                    if (!nodeEquals(selectedNode, im)) {
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

        // Set name of connector, which defaults to "N". The first letter is the connector, the second
        // is a unique identifier.
        im.setName("N" + imgCount);
        imgCount++;
        // Set scale
        im.setScale(0.5f);

        // Get nearest isometric center to where the mouse clicked
        nearestIsoCenter(x, y);
        x = vec.x;
        y = vec.y - 1; // For some reason this is consistently off by 1, so we take care of that this way

        // Add connector to level
        model.addToLevel(im,x,y);

        // Convert to world space
        vec.set(x,y+1); // Don't know why it needs to be y+1, but it does
        isometricToWorld(vec);
        // Retrieve from vector cache
        x = vec.x;
        y = vec.y;

        // Place connector at nearest isometric center
        im.setPosition(x - (TILE_WIDTH / 4), y - (TILE_HEIGHT / 4));
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
                    model.updateLevelTileName(im.getName(),name);
                    im.setName(name);
                    im.setDrawable(new TextureRegionDrawable(
                            new Texture(Gdx.files.internal(
                                    // Path to connector asset
                                    Connector.getAssetPath(CONN_ORDER[nextConn])
                            ))));
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
     * Helper function that returns a new FocusListener that disables keyboard input when a text field
     * is being used.
     *
     * @return new FocusListener that disables keyboard input when a text field is being used.
     */
    FocusListener newIgnoreInputFocusListener() {
        return new FocusListener() {
            public void keyboardFocusChanged(FocusListener.FocusEvent event, Actor actor, boolean focused) {
                // Ignores keyboard input for camera control when typing in a text box
                input.shouldIgnoreInput(focused);
            }
        };
    }

    /**
     * Creates the form for writing target information for the given target and places it in the toolStage.
     *
     * This function takes in a target, which would be the selected node if the selected node is a target.
     *
     * The entries include, for targets specifically:
     * [0] A TextField to enter the target name.
     * [1] A TextField to enter the target's paranoia stat.
     * [2] A SelectBox dropdown menu to select target traits (multiple options can be selected).
     * [3] A TextField to enter the target's maximum stress.
     *
     * @param target    The target that this form handles the information for
     */
    private void createTargetForm(Image target) {
        // Place table to contain target form entries
        targetForm.left();
        targetForm.bottom();
        targetForm.setSize(.25f*canvas.getWidth(),canvas.getHeight());

        // Target Name
        TextField targetName = new TextField("", skin);
        targetName.setMessageText("Target Name");
        targetName.setPosition(FORM_X_OFFSET,FORM_Y_OFFSET + 3 * FORM_GAP);
        // Initialize with target name
        // TODO: Apparently this line is not working
        targetName.setText(target.getName().substring(1));
        // Add listener to disable keyboard input when the field is selected
        targetName.addListener(newIgnoreInputFocusListener());
        // Arrange in table
        targetForm.addActor(targetName);

        // Target Paranoia
        TextField targetParanoia = new TextField("", skin);
        targetParanoia.setMessageText("Target Paranoia");
        targetParanoia.setPosition(FORM_X_OFFSET,FORM_Y_OFFSET + 2 * FORM_GAP);
        // Initialize with target paranoia
        // TODO: Apparently this line is not working
        targetParanoia.setText(String.valueOf(model.getTargetTile(target.getName()).paranoia));
        // Add listener to disable keyboard input when the field is selected
        targetParanoia.addListener(newIgnoreInputFocusListener());
        // Arrange in table
        targetForm.addActor(targetParanoia);

        // Target Traits
        SelectBox targetTraits = new SelectBox(skin);
        targetTraits.setItems(TRAIT_OPTIONS);
        targetTraits.setPosition(FORM_X_OFFSET,FORM_Y_OFFSET + FORM_GAP);
        // Add listener to disable keyboard input when the field is selected
        targetTraits.addListener(newIgnoreInputFocusListener());
        // Arrange in table
        targetForm.addActor(targetTraits);

        // Target Max Stress
        TextField targetMaxStress = new TextField("", skin);
        targetMaxStress.setMessageText("Target Max Stress");
        targetMaxStress.setPosition(FORM_X_OFFSET,FORM_Y_OFFSET - 1 * FORM_GAP);
        // Initialize with target max stress
        targetMaxStress.setText(String.valueOf(model.getTargetTile(target.getName()).maxStress));
        // Add listener to disable keyboard input when the field is selected
        targetMaxStress.addListener(newIgnoreInputFocusListener());
        // Arrange in table
        targetForm.addActor(targetMaxStress);
    }

    /**
     * Creates the forms for writing target/node information and places them in the toolStage.
     *
     * These include, for nodes specifically:
     * - A TextField to enter the node title.
     * - A TextArea to write the node content (what's seen when scanned).
     * - A TextArea to write the node summary (what goes into the notebook).
     * - A SelectBox dropdown menu to select the node's target stress rating (only one option can be selected).
     * - A SelectBox dropdown menu to select the node's player stress rating (only two options can be selected).
     * */
    private void createNodeForm(Image node) {
        // Place table to contain node form entries
        nodeForm.left();
        nodeForm.bottom();
        nodeForm.setSize(.25f*canvas.getWidth(),canvas.getHeight());

        // Node Title
        TextField nodeTitle = new TextField("", skin);
        // TODO: Make textboxes reusable, currently it resets all values of the node when opened again
        if (model.getNodeTile(node.getName()).title != null && model.getNodeTile(node.getName()).title != ""){
            nodeTitle.setMessageText(model.getNodeTile(node.getName()).title);
        }else {
            nodeTitle.setMessageText("Node Title");
        }
        nodeTitle.setPosition(FORM_X_OFFSET,FORM_Y_OFFSET + 5 * FORM_GAP);
        // Initialize with node title
        // TODO: Apparently this line is not working
        nodeTitle.setText(String.valueOf(model.getNodeTile(node.getName()).title));
        // Add listener to disable keyboard input when the field is selected
        nodeTitle.addListener(newIgnoreInputFocusListener());
        // Arrange in table
        nodeForm.addActor(nodeTitle);

        // Node Content
        TextArea nodeContent = new TextArea("", skin);
        nodeContent.setMessageText("Node Content");
        nodeContent.setPosition(FORM_X_OFFSET,FORM_Y_OFFSET + 4 * FORM_GAP);
        // Initialize with node content
        // TODO: Apparently this line is not working
        nodeTitle.setText(String.valueOf(model.getNodeTile(node.getName()).content));
        // Add listener to disable keyboard input when the field is selected
        nodeContent.addListener(newIgnoreInputFocusListener());
        // Arrange in table
        nodeForm.addActor(nodeContent);

        // Node Summary
        TextArea nodeSummary = new TextArea("", skin);
        nodeSummary.setMessageText("Node Summary");
        nodeSummary.setPosition(FORM_X_OFFSET,FORM_Y_OFFSET + 3 * FORM_GAP);
        // Initialize with node summary
        // TODO: Apparently this line is not working
        nodeTitle.setText(String.valueOf(model.getNodeTile(node.getName()).summary));
        // Add listener to disable keyboard input when the field is selected
        nodeSummary.addListener(newIgnoreInputFocusListener());
        // Arrange in table
        nodeForm.addActor(nodeSummary);

        // Target Stress Rating
        SelectBox targetStressRating = new SelectBox(skin);
        targetStressRating.setItems(SR);
        targetStressRating.setPosition(FORM_X_OFFSET,FORM_Y_OFFSET + 2 * FORM_GAP);
        // Add listener to disable keyboard input when the field is selected
        targetStressRating.addListener(newIgnoreInputFocusListener());
        // Arrange in table
        nodeForm.addActor(targetStressRating);

        // Player Stress Rating
        SelectBox playerStressRating = new SelectBox(skin);
        playerStressRating.setItems(SR);
        playerStressRating.setPosition(FORM_X_OFFSET,FORM_Y_OFFSET + 1 * FORM_GAP);
        // Add listener to disable keyboard input when the field is selected
        playerStressRating.addListener(newIgnoreInputFocusListener());
        // Arrange in table
        nodeForm.addActor(playerStressRating);
    }

    /**
     * Saves the information in the given form as that of the given target/node.
     *
     * This should be done by editing the properties of the LevelTile with
     * the name im.getName() (cast to TargetTile/NodeTile as needed, you can
     * determine which to cast to with the first digit of im.getName() - 0 for
     * target, 1 or 2 for node).
     *
     * To access all the form entries, you can do getCells() for the Table, then
     * iterate through the cells and do getActor for each, which should give
     * you the textField/textArea/selectBox in the order you added them to the Table.
     *
     * @param form      The Table containing all the target/node information.
     * @param im        The Image of the target/node.
     */
    private void saveForm(Table form, Image im) {
        int nodetype = Character.getNumericValue(im.getName().charAt(0));
        SnapshotArray cells = form.getChildren();
        if (nodetype == 0){ // case image to targetTile
            LevelEditorModel.TargetTile target = model.getTargetTile(im.getName());
            target.name = String.valueOf(((TextField)form.getChild(0)).getText());
            target.paranoia = Integer.parseInt(String.valueOf(((TextField)form.getChild(1)).getText()));
            target.traits = ((SelectBox)form.getChild(2)).getItems();
            target.maxStress = Integer.parseInt(String.valueOf(((TextField)form.getChild(3)).getText()));
            //System.out.println(target.name);
        } else if (nodetype == 1 || nodetype == 2){ // cast image to targetNode
            LevelEditorModel.NodeTile node = model.getNodeTile(im.getName());
            node.title = String.valueOf(((TextField)cells.get(0)).getText());
            node.content = String.valueOf(((TextArea)cells.get(1)).getText());
            node.summary = String.valueOf(((TextArea)cells.get(2)).getText());
            node.targetSR = (StressRating) ((SelectBox)cells.get(3)).getItems().get(0);
            node.playerSR = (StressRating) ((SelectBox)cells.get(4)).getItems().get(0);
        }
    }

    /*********************************************** SCREEN METHODS ***********************************************/
    @Override
    public void show() {
    }

    /**
     * Clears all images in the level.
     *
     * Called if the clear button "C" is pressed.
     */
    private void clearLevel() {
        model.clear();
        // Reset image count
        imgCount = 0;
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
        // If the currently selected and to-be-selected nodes are different
        if (!nodeEquals(selectedNode, newSelectedNode)) {
            // Initialize variable for node type
            int nodeType;

            // If a node was actually previously selected, save the form data for it
            if (selectedNode != null) {
                // Determine if previously selected node was a target or a node
                nodeType = Character.getNumericValue(selectedNode.getName().charAt(0));
                // If was a target, save contents of targetForm for that target and clear form
                if (nodeType == 0) {
                    createTargetForm(selectedNode);
                    saveForm(targetForm, selectedNode);
                    targetForm.clear();
                }
                // If was a node, save contents of nodeForm for that node and clear form
                else {
                    createNodeForm(selectedNode);
                    saveForm(nodeForm, selectedNode);
                    nodeForm.clear();
                }
            }

            // If a node is actually going to be selected next, create a form for it
            if (newSelectedNode != null) {
                // Determine if next selected node will be a target or a node
                nodeType = Character.getNumericValue(newSelectedNode.getName().charAt(0));
                // If it's a target, create a target form
                if (nodeType == 0) {
                    createTargetForm(newSelectedNode);
                }
                // If it's a node, create a node form
                else {
                    createNodeForm(newSelectedNode);
                }
            }

            // Deselect the currently selected node
            deselectNode();
        }

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
        if (input.didClear()) {clearLevel();}
        // If Z button is pressed, delete last object that was created
        if (input.didUndo()) {model.undo();}

        // Handle making connectors if in Draw Mode and a connector was made
        if (editorMode == Mode.DRAW && input.didRightClick()) {
            addConnector(input.getX(), input.getY());
        }

        // Move camera
        canvas.clear();
        camera.moveCamera();

        // If in Edit Mode, determine if edit forms need to be handled
        if (editorMode == Mode.EDIT) {
            displayEditForms();
        }

        // UPDATE

        // Draw objects on canvas
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        nodeStage.act(delta);
        toolStage.act(delta);

        canvas.drawIsometricGrid(nodeStage, 10, 10);

        nodeStage.draw();
        toolStage.draw();
    }

    @Override
    /**
     * Ensures that the game world appears at the same scale, even when resizing
     */
    public void resize(int width, int height) {
        // Keep game world at the same scale even when resizing
        //nodeStage.getViewport().update(width,height,true);
        camera.resize(width, height);

        // Keep toolbar in the same place when resizing
        //toolstage.getViewport().update(width,height,true);
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
