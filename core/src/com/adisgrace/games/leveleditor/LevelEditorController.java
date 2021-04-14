package com.adisgrace.games.leveleditor;

import com.adisgrace.games.*;
import com.adisgrace.games.util.Connector;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

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
    };

    /** Canvas is the primary view class of the game */
    private GameCanvas canvas;
    /** Gets player input */
    private InputController input;
    /** View camera for node map */
    private OrthographicCamera camera;

    /** CurrentZoom controls how much the camera is zoomed in or out */
    private float currentZoom;
    /** Stage where nodes and grids are drawn*/
    Stage nodeStage;
    /** Stage where buttons are drawn on */
    Stage toolstage;

    /** Array of all the images added */
    Array<Image> images;

    /** World bounds (not the same as canvas bounds) */
    Rectangle bounds;

    /** Current mode of the level editor */
    private Mode editorMode;

    /** The count of the next image that is added */
    int imgCount;
    /** acceleration accumulators for camera movement */
    private int left_acc, right_acc, up_acc, down_acc;
    /** time taken for camera to accelerate to max speed */
    private int acceleration_speed = 40;

    /** Vector caches to avoid initializing vectors every time */
    private Vector2 vec;
    private Vector3 vec3;

    /** Dimensions of map tile */
    private static final int TILE_HEIGHT = 256;
    private static final int TILE_WIDTH = 444;
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

    /** Textures for nodes */
    private Texture targetLow;
    private Texture targetHigh;
    private Texture unlockedLow;
    private Texture unlockedHigh;
    private Texture lockedLow;
    private Texture lockedHigh;


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
        camera = canvas.getCamera();
        viewport.setCamera(camera);
        currentZoom = camera.zoom;
        camera.zoom = 1.5f;
        //viewport.setScreenPosition(-2*canvas.getWidth(),-2*canvas.getHeight());

        // Create stage for nodes and tile with isometric grid
        nodeStage = new Stage(viewport);
        //canvas.setIsometricSize(3,3);
        canvas.drawIsometricGrid(nodeStage, 3, 3);

        // Create tool stage for buttons
        createToolStage();

        // Initialize hashmap of images
        images = new Array<Image>();
        // Initialize vector caches
        vec = new Vector2();
        vec3 = new Vector3();

        // Start editor mode in Move Mode
        editorMode = Mode.MOVE;

        // Create all textures for nodes
        targetLow = new Texture(Gdx.files.internal("leveleditor/N_TargetMaleIndividualLow_1.png"));
        targetHigh = new Texture(Gdx.files.internal("leveleditor/N_TargetMaleIndividual_1.png"));
        unlockedLow = new Texture(Gdx.files.internal("leveleditor/N_UnlockedIndividualLow_1.png"));
        unlockedHigh = new Texture(Gdx.files.internal("leveleditor/N_UnlockedIndividual_1.png"));
        lockedLow = new Texture(Gdx.files.internal("leveleditor/N_LockedIndividualLow_1.png"));
        lockedHigh = new Texture(Gdx.files.internal("leveleditor/N_LockedIndividual_2.png"));
    }

    /**
     * Helper function that creates and fills the stage with buttons to be used in creating a level.
     *
     * These include:
     * - A button to create a new target.
     * - A button to create a new unlocked node.
     * - A button to create a new locked node.
     */
    private void createToolStage(){
        // Creates toolbar viewport and camera
        ExtendViewport toolbarViewPort = new ExtendViewport(canvas.getWidth(), canvas.getHeight());
        toolstage = new Stage(toolbarViewPort);

        System.out.println(canvas.getHeight());
        System.out.println(canvas.getWidth());

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
        toolbar = createNodeButtons(toolbar);
        toolbar = createModeButtons(toolbar);

        // Add filled toolbar to stage
        toolstage.addActor(toolbar);
    }

    /**
     * Helper function that adds the buttons used to create nodes to the stage.
     *
     * Returns the toolbar with these node-creation buttons included.
     *
     * These include:
     * - A button to create a new target.
     * - A button to create a new unlocked node.
     * - A button to create a new locked node.
     *
     * @param toolbar       The toolbar that the buttons are stored in.
     * @return              The toolbar with the node-creation buttons added.
     */
    private Table createNodeButtons(Table toolbar) {
        // Create "add node" buttons
        // Save the paths to all the node assets (must be final to work in lambda expression)
        final String target = "leveleditor/N_TargetMaleIndividual_1.png";
        final String unlocked = "leveleditor/N_UnlockedIndividual_1.png";
        final String locked = "leveleditor/N_LockedIndividual_2.png";
        // Paths to all button assets
        String[] buttonAssets = {"leveleditor/buttons/LE_AddNodeTarget_1.png",
                "leveleditor/buttons/LE_AddNodeUnlocked_1.png",
                "leveleditor/buttons/LE_AddNodeLocked_1.png"};
        // Height of first button
        int height = (int)camera.viewportHeight - TOOLBAR_Y_OFFSET;
        // Initialize other variables for button creation
        Drawable drawable;
        ImageButton button;

        // Loop through and create each button
        for (int k=0; k<buttonAssets.length; k++) {
            // Create and place button
            drawable = new TextureRegionDrawable(new Texture(Gdx.files.internal(buttonAssets[k])));
            button = new ImageButton(drawable);
            button.setTransform(true);
            button.setScale(BUTTON_SCALE);
            button.setPosition(TOOLBAR_X_OFFSET, height);

            // Add listeners to button, changing depending on which node the button creates
            if (k==0) {
                // ADD TARGET
                button.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {addNode(target,0);}
                });
            } else if (k==1) {
                // ADD LOCKED
                button.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {addNode(unlocked,1);}
                });
            } else if (k==2) {
                // ADD UNLOCKED
                button.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {addNode(locked,2);}
                });
            }

            // Add button to stage
            toolstage.addActor(button);
            // Arrange buttons in order using a Table
            toolbar.addActor(button);
            // Increment height
            height -= BUTTON_GAP;
        }

        return toolbar;
    }

    /**
     * Helper function that adds the buttons used to change modes to the stage.
     *
     * Returns the toolbar with these mode-changing buttons included.
     *
     * These include:
     * - A button to change to Move Mode, where nodes can be moved around.
     * - A button to change to Edit Mode, where the contents of nodes can be edited.
     * - A button to change to Delete Mode, where nodes can be deleted.
     *
     * @param toolbar       The toolbar that the buttons are stored in.
     * @return              The toolbar with the mode-changing buttons added.
     */
    private Table createModeButtons(Table toolbar) {
        // Create mode change buttons
        // Paths to all button assets
        String[] buttonAssets = {"leveleditor/buttons/LE_MoveMode_1.png",
                "leveleditor/buttons/LE_EditMode_1.png",
                "leveleditor/buttons/LE_DeleteMode_1.png",
                "leveleditor/buttons/LE_DrawMode_1.png"};
        // Height of first button
        int height = (int)camera.viewportHeight - TOOLBAR_Y_OFFSET;
        // Right offset of mode buttons
        int xloc = canvas.getWidth() - TOOLBAR_X_OFFSET - BUTTON_WIDTH;
        // Initialize other variables for button creation
        Drawable drawable;
        ImageButton button;

        // Loop through and create each button
        for (int k=0; k<buttonAssets.length; k++) {
            // Create and place button
            drawable = new TextureRegionDrawable(new Texture(Gdx.files.internal(buttonAssets[k])));
            button = new ImageButton(drawable);
            button.setTransform(true);
            button.setScale(BUTTON_SCALE);
            button.setPosition(xloc, height);

            // TODO: some kind of text that shows the mode
            // Add listeners to button, changing depending on which node the button creates
            if (k==0) {
                // CHANGE TO MOVE MODE
                button.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {editorMode = Mode.MOVE;}
                });
            } else if (k==1) {
                // CHANGE TO EDIT MODE
                button.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {editorMode = Mode.EDIT;}
                });
            } else if (k==2) {
                // CHANGE TO DELETE MODE
                button.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {editorMode = Mode.DELETE;}
                });
            } else if (k==3) {
                // CHANGE TO DRAW MODE
                button.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {editorMode = Mode.DRAW;}
                });
            }

            // Add button to stage
            toolstage.addActor(button);
            // Arrange buttons in order using a Table
            toolbar.addActor(button);
            // Increment height
            height -= BUTTON_GAP;
        }

        return toolbar;
    }

    /*********************************************** ADDING TO LEVEL ***********************************************/

    /**
     * Helper function that converts coordinates from world space to isometric space.
     *
     * @param coords   Coordinates in world space to transform
     * @return         Given coordinates in isometric space
     */
    private Vector2 worldToIsometric(Vector2 coords) {
        float tempx = coords.x;
        float tempy = coords.y;
        coords.x = 0.57735f * tempx - tempy;
        coords.y = 0.57735f * tempx + tempy;

        return coords;
    }

    /**
     * Helper function that converts coordinates from isometric space to world space.
     *
     * @param coords   Coordinates in isometric space to transform
     * @return         Given coordinates in world space
     */
    private Vector2 isometricToWorld(Vector2 coords) {
        // TODO: FIX
        float tempx = coords.x;
        float tempy = coords.y;
        //coords.x = (float)((Math.sqrt(3)/2) * tempx - (0.5f * tempy));
        //coords.y = (float)((Math.sqrt(3)/2) * tempx + (0.5f * tempy));
        coords.x = (float)((Math.sqrt(3)/2) * tempx + ((Math.sqrt(3)/2) * tempy));
        coords.y = (float)(-0.5f * tempx + (0.5f * tempy));

        return coords;
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
        // TODO: have this return the isometric coordinates of the nearest iso center

        // Transform world coordinates to isometric space
        vec.set(x,y);
        vec = worldToIsometric(vec);
        x = vec.x;
        y = vec.y;

        // Find the nearest isometric center
        x = Math.round(x / TILE_HEIGHT);
        y = Math.round(y / TILE_HEIGHT);

        // Transform back to world space
        vec.set(x * (0.5f * TILE_WIDTH) + y * (0.5f * TILE_WIDTH),-x * (0.5f * TILE_HEIGHT) + y * (0.5f * TILE_HEIGHT));
        //vec = isometricToWorld(vec);

        /**
        // Get location that image should snap to
        x = x - (x % (TILE_WIDTH / 2));
        y = y - (y % (TILE_HEIGHT / 2));

        // If tries to snap to intersection between lines, snap to the grid cell
        // above the intersection instead
        if (Math.abs((int) (x / (TILE_WIDTH / 2)) % 2) != Math.abs((int) (y / (TILE_HEIGHT / 2)) % 2)) {
            y += TILE_HEIGHT / 2;
        }
         */
    }

    /*********************************************** NODES ***********************************************/

    /**
     * Adds a draggable node with the appearance of the asset at the given path to the stage.
     *
     * Called when one of the node-adding buttons is pressed. Each image is given a name that is a number
     * of increasing value, so that no names are repeated in a single level editor session.
     *
     * @param pathHigh      Path to the file where the node's high light asset is stored
     * @param nodeType      0: target, 1: unlocked, 2: locked
     */
    private void addNode(String pathHigh, int nodeType) {
        // Create image
        final Image im = new Image(new Texture(Gdx.files.internal(pathHigh)));
        nodeStage.addActor(im);
        im.setPosition(-(im.getWidth() - TILE_WIDTH) / 2, ((TILE_HEIGHT / 2) - LOCKED_OFFSET) * 2);
        im.setOrigin(0, 0);

        // Set name of image, which is the string "Node" and a number
        String name = "Node" + String.valueOf(imgCount);
        im.setName(name);
        // Add image to images
        images.add(im);
        imgCount++;

        // Add listeners, which change their behavior depending on the editor mode

        // Add drag listener that does something during a drag (one for each asset)
        switch(nodeType) {
            case 0: // Target
                im.addListener((new DragListener() {
                    public void touchDragged(InputEvent event, float x, float y, int pointer) {
                        // Only do this if editor mode is Move
                        // Updates image position on drag
                        if (editorMode == Mode.MOVE) {
                            // When dragging, snaps image center to cursor
                            float dx = x - im.getWidth() * 0.5f;
                            float dy = y - im.getHeight() * 0.25f;
                            im.setPosition(im.getX() + dx, im.getY() + dy);

                            // Change to low version of asset
                            im.setDrawable(new TextureRegionDrawable(targetLow));
                        }
                    }
                }));
                break;
            case 1: // Unlocked
                im.addListener((new DragListener() {
                    public void touchDragged(InputEvent event, float x, float y, int pointer) {
                        // Only do this if editor mode is Move
                        // Updates image position on drag
                        if (editorMode == Mode.MOVE) {
                            // When dragging, snaps image center to cursor
                            float dx = x - im.getWidth() * 0.5f;
                            float dy = y - im.getHeight() * 0.25f;
                            im.setPosition(im.getX() + dx, im.getY() + dy);

                            // Change to low version of asset
                            im.setDrawable(new TextureRegionDrawable(unlockedLow));
                        }
                    }
                }));
                break;
            case 2: // Locked
                im.addListener((new DragListener() {
                    public void touchDragged(InputEvent event, float x, float y, int pointer) {
                        // Only do this if editor mode is Move
                        // Updates image position on drag
                        if (editorMode == Mode.MOVE) {
                            // When dragging, snaps image center to cursor
                            float dx = x - im.getWidth() * 0.5f;
                            float dy = y - im.getHeight() * 0.25f;
                            im.setPosition(im.getX() + dx, im.getY() + dy);

                            // Change to low version of asset
                            im.setDrawable(new TextureRegionDrawable(lockedLow));
                        }
                    }
                }));
                break;
        }

        // Add drag listener that does something when a drag ends (one for each asset)
        switch(nodeType) {
            case 0: // Target
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

                            // Change back to high version of asset
                            im.setDrawable(new TextureRegionDrawable(targetHigh));
                        }
                    }
                }));
                break;
            case 1: // Unlocked
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

                            // Change back to high version of asset
                            im.setDrawable(new TextureRegionDrawable(unlockedHigh));
                        }
                    }
                }));
                break;
            case 2: // Locked
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

                            // Change back to high version of asset
                            im.setDrawable(new TextureRegionDrawable(lockedHigh));
                        }
                    }
                }));
                break;
        }

        // Add click listener that does something when the node is clicked
        im.addListener((new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                // Different behavior on clicked depending on editor mode
                switch (editorMode) {
                    // In Edit Mode, open up the node submenu
                    case EDIT:
                        System.out.println("Edit");
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
        //System.out.println("Screen space: (" + String.valueOf(x) + ", " + String.valueOf(y) + ")");

        // Convert mouse position from screen to world coordinates
        vec3.set(x,y,0);
        camera.unproject(vec3);
        x = vec3.x;
        y = vec3.y;

        //System.out.println("World space: (" + String.valueOf(x) + ", " + String.valueOf(y) + ")");

        // Create connector image, defaulting to the South connector
        final Image im = new Image(new Texture(Gdx.files.internal(Connector.C_NORTH)));
        nodeStage.addActor(im);

        // Set name of image, which defaults to "N"
        im.setName("N");
        // Set scale
        im.setScale(0.5f);

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
                        // Set the appearance to be the next connector
                        String nextConn = rotateConnector(im.getName());
                        im.setName(nextConn);
                        im.setDrawable(new TextureRegionDrawable(
                                new Texture(Gdx.files.internal(
                                        getAssetPath(nextConn)
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

    /**
     * Helper function that returns which connector to rotate to next.
     *
     * Returns the character representing the connector to rotate to next.
     *
     * The order goes from North -> East -> South -> West -> North.
     *
     * @param conn   String representing current direction of connector
     * @return       String representing the path to the asset where the rotated connector is
     */
    private String rotateConnector(String conn) {
        switch(conn) {
            case "N":
                return "E";
            case "E":
                return "S";
            case "S":
                return "W";
            case "W":
                return "N";
            default:
                throw new RuntimeException("Connector can only be NESW");
        }
    }

    /**
     * Helper function that returns the path to the asset of the connector given by the input.
     *
     * @param t     The type of the Connector, represented as a string of "NESW" with the directions that
     *              the Connector runs in.
     */
    public String getAssetPath(String t) {
        switch(t) {
            case "N":
                return Connector.C_NORTH;
            case "E":
                return Connector.C_EAST;
            case "S":
                return Connector.C_SOUTH;
            case "W":
                return Connector.C_WEST;
            default:
                throw new RuntimeException("Connector can only be NESW");
        }
    }

    /*********************************************** SCREEN METHODS ***********************************************/

    @Override
    public void show() {
    }

    @Override
    /**
     * renders the game display at consistent time steps
     */
    public void render(float delta) {
        // If C button is pressed, clear the level
        if (input.didClear()) {
            clearLevel();
        }

        // If Z button is pressed, delete last object that was created
        if (input.didUndo()) {
            images.pop().remove();
        }

        // Handle making connectors if in Draw Mode and a connector was made
        if (editorMode == Mode.DRAW && input.didRightClick()) {
            addConnector(input.getX(), input.getY());
        }

        // Move camera
        canvas.clear();
        moveCamera();

        // Draw objects on canvas
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        nodeStage.act(delta);
        nodeStage.draw();
        toolstage.act(delta);
        toolstage.draw();

    }

    /**
     * Helper function that clears all images in the level
     */
    private void clearLevel() {
        // Delete all images
        for (Image im : images) {
            im.remove();
        }
        // Reset image count
        imgCount = 0;
    }

    @Override
    /**
     * Ensures that the game world appears at the same scale, even when resizing
     */
    public void resize(int width, int height) {
        // Keep game world at the same scale even when resizing
        nodeStage.getViewport().update(width,height,true);
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.position.set(width/2f, height/2f, 0);

        // Keep toolbar in the same place when resizing
        toolstage.getViewport().update(width,height,true);

        canvas.resize();

        System.out.println(canvas.getHeight());
        System.out.println(canvas.getWidth());
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

    /************************************************* CAMERA *************************************************/

    /**
     * Handles camera movement and zoom based on user input.
     *
     * Also adjusts the world scale based on zoom.
     */
    private void moveCamera() {
        // Check for new input
        input.readInput();
        // Set current camera zoom
        currentZoom = camera.zoom;

        // Move camera if one of the WASD keys are pressed
        if(input.didUp()) {
            camera.translate(0, 12*currentZoom*cameraSpeed(0)/acceleration_speed);
        }
        if(input.didLeft()) {
            camera.translate(-12*currentZoom*cameraSpeed(2)/acceleration_speed, 0);
        }
        if(input.didDown()) {
            camera.translate(0, -12*currentZoom*cameraSpeed(1)/acceleration_speed);
        }
        if(input.didRight()) {
            camera.translate(12*currentZoom*cameraSpeed(3)/acceleration_speed, 0);
        }

        // Zoom in/out if E/Q keys are pressed
        if(input.didZoomIn()) {
            camera.zoom = (.99f)*currentZoom;
        }
        if(input.didZoomOut()) {
            camera.zoom = (1.01f)*currentZoom;
        }

        // Clamp zoom between set values
        if(camera.zoom > 4.0f) {
            camera.zoom = 4.0f;
        }
        if(camera.zoom < 1.0f) {
            camera.zoom = 1.0f;
        }

        // Scale world by zoom
        float camX = camera.position.x;
        float camY = camera.position.y;

        Vector2 camMin = new Vector2(-1500f, -1500f);//(camera.viewportWidth/2, camera.viewportHeight/2);
        camMin.scl(camera.zoom/2); //bring to center and scale by the zoom level
        Vector2 camMax = new Vector2(1500f, 1500f);
        camMax.sub(camMin); //bring to center

        //keep camera within borders
        camX = Math.min(camMax.x, Math.max(camX, camMin.x));
        camY = Math.min(camMax.y, Math.max(camY, camMin.y));

        camera.position.set(camX, camY, camera.position.z);

        camera.update();
    }

    /**
     * Returns the camera speed given the direction, calculated with accumulated acceleration
     * @param direction
     */
    public float cameraSpeed(int direction){
        // 0 = up, 1 = down, 2 = left, 3 = right
        float speed = 0f;
        //acceleration_speed = 40;
        switch (direction){
            case 0:
                if (up_acc == 0) clearSpeedRev(0);
                up_acc += 1;
                speed = up_acc > acceleration_speed ? acceleration_speed : up_acc;
                break;
            case 1:
                if (down_acc == 0) clearSpeedRev(1);
                down_acc += 1;
                speed = down_acc > acceleration_speed ? acceleration_speed : down_acc;
                break;
            case 2:
                if (left_acc == 0) clearSpeedRev(2);
                left_acc += 1;
                speed = left_acc > acceleration_speed ? acceleration_speed : left_acc;
                break;
            case 3:
                if (right_acc == 0) clearSpeedRev(3);
                right_acc += 1;
                speed = right_acc > acceleration_speed ? acceleration_speed : right_acc;
                break;
        }
        return speed;
    }

    /**
     * Clears camera speed in reverse directions
     * @param
     */
    private void clearSpeedRev(int direction){
        switch (direction){
            case 0:
                down_acc = 0;
            case 1:
                up_acc = 0;
            case 2:
                right_acc = 0;
            case 3:
                left_acc = 0;
        }
        return;
    }
}
