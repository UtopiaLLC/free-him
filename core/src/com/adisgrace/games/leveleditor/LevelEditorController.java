package com.adisgrace.games.leveleditor;

import java.util.HashMap;

import com.adisgrace.games.FactNode;
import com.adisgrace.games.GameCanvas;
import com.adisgrace.games.GameController;
import com.adisgrace.games.WorldModel;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
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
    /** Canvas is the primary view class of the game */
    private GameCanvas canvas;
    /** View camera */
    private OrthographicCamera camera;
    /** CurrentZoom controls how much the camera is zoomed in or out */
    private float currentZoom;
    /** Stage generation buttons are drawn on */
    Stage toolstage;
    /** Stage where nodes and grids are drawn*/
    Stage nodeStage;

    /** Hashmap of all the images added */
    HashMap<String,Image> images;
    /** Array of all the buttons added */
    Array<Button> buttons;

    /** Hashmap of nodes, each key is the name and each value is the corresponding FactNode */
    /** Hashmap of targets, each key is the target name and each value is an array of nodes that are associated with that target */
    /** Connector subclass with two fields: coordinates and type */


    /** The count of the next image that is added */
    int imgCount;
    /** acceleration accumulators for camera movement */
    private int left_acc, right_acc, up_acc, down_acc;
    /** time taken for camera to accelerate to max speed */
    private int acceleration_speed = 40;

    /** Vector cache to avoid initializing vectors every time */
    private Vector2 vec;

    /** Dimensions of map tile */
    private static final int TILE_HEIGHT = 256;
    private static final int TILE_WIDTH = 444;
    /** Constants for the y-offset for different node types */
    private static final float LOCKED_OFFSET = 114.8725f;

    /**
     * Creates a new level editor controller. This initializes the UI and sets up the isometric
     * grid.
     */
    public LevelEditorController() {
        // Create canvas and set view and zoom
        canvas = new GameCanvas();

        // Set up camera
        ExtendViewport viewport = new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        viewport.setCamera(canvas.getCamera());
        currentZoom = canvas.getCamera().zoom;
        canvas.getCamera().zoom = 1.5f;

        // Create stage for nodes and tile with isometric grid
        nodeStage = new Stage(viewport);
        //canvas.setIsometricSize(3,3);
        canvas.drawIsometricGrid(nodeStage, 1, 1);

        // Create tool stage for buttons
        createToolStage();

        // Initialize hashmap of images
        images = new HashMap<String, Image>();
        // Initialize vector cache
        vec = new Vector2();
    }

    /**
     * Creates and fills the stage with buttons to be used in creating a level.
     *
     * These include:
     * - A button to create new nodes.
     */
    public void createToolStage(){
        ExtendViewport toolbarViewPort = new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        toolstage = new Stage(toolbarViewPort);

        // Handle inputs from both stages with a Multiplexer
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(toolstage);
        inputMultiplexer.addProcessor(nodeStage);

        Gdx.input.setInputProcessor(inputMultiplexer);

        // Create buttons

        // Create a button to add new images
        Drawable drawable = new TextureRegionDrawable(new Texture(Gdx.files.internal("skills/overwork.png")));
        ImageButton button = new ImageButton(drawable);
        button.setTransform(true);
        button.setScale(0.4f);
        //button.setPosition(500, 500);
        // Add listener to button
        button.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                System.out.println("Button Pressed");
                addImage();
            }
        });
        toolstage.addActor(button);

        // Arrange buttons in order using a Table
        Table toolbar = new Table();
        toolbar.right();
        toolbar.setSize(.25f*Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        toolbar.addActor(button);
        toolstage.addActor(toolbar);
    }

    /**
     * Adds an image of something to the stage.
     *
     * Called when one of the image-adding buttons is pressed. Each image is given a name that is a number
     * of increasing value, so that no names are repeated in a single level editor session.
     */
    public void addImage() {
        // Create image
        final Image im = new Image(new Texture(Gdx.files.internal("node/N_LockedIndividual_2.png")));
        nodeStage.addActor(im);
        im.setPosition(-(im.getWidth() - TILE_WIDTH) / 2, ((TILE_HEIGHT / 2) - LOCKED_OFFSET) * 2);
        im.setOrigin(0, 0);

        // Set name of image
        String name = String.valueOf(imgCount);
        im.setName(name);
        // Add image to images
        images.put(name, im);
        imgCount++;

        // Add drag listener to image that updates position on drag
        im.addListener((new DragListener() {
            public void touchDragged (InputEvent event, float x, float y, int pointer) {
                // When dragging, snaps image center to cursor
                float dx = x-im.getWidth()*0.5f;
                float dy = y-im.getHeight()*0.25f;
                im.setPosition(im.getX() + dx, im.getY() + dy);
            }
        }));
        // Add drag listener that snaps to grid when drag ends
        im.addListener((new DragListener() {
            public void dragStop (InputEvent event, float x, float y, int pointer) {
                // Get coordinates of center of image
                float newX = im.getX()+ x-im.getWidth()*0.5f;
                float newY = im.getY()+ y-im.getHeight()*0.25f;
                // Get location that image should snap to
                newX = newX - (newX % (TILE_WIDTH / 2));
                newY = newY - (newY % (TILE_HEIGHT / 2));

                // If tries to snap to intersection between lines, snap to the grid cell
                // above the intersection instead
                if (Math.abs((int)(newX / (TILE_WIDTH / 2)) % 2) != Math.abs((int)(newY / (TILE_HEIGHT / 2)) % 2)) {
                    newY += TILE_HEIGHT / 2;
                }

                // Account for difference between tile width and sprite width
                newX -= (im.getWidth() - TILE_WIDTH) / 2;
                newY += ((TILE_HEIGHT / 2) - LOCKED_OFFSET) * 2;

                im.setPosition(newX, newY);
            }
        }));
    }

    /**
     * Helper function that converts coordinates from world space to isometric space.
     *
     * @param coords   Coordinates in world space to transform
     * @return         Given coordinates in isometric space
     */
    private Vector2 worldToIsometric(Vector2 coords) {
        float tempx = coords.x;
        float tempy = coords.y;
        coords.x = (float)Math.sqrt(3)/2 * tempx - 0.5f * tempy;
        coords.y = (float)Math.sqrt(3)/2 * tempx + 0.5f * tempy;

        return coords;
    }

    /**
     * Helper function that converts coordinates from isometric space to world space.
     *
     * @param coords   Coordinates in isometric space to transform
     * @return         Given coordinates in world space
     */
    private Vector2 isometricToWorld(Vector2 coords) {
        float tempx = coords.x;
        float tempy = coords.y;
        coords.x = 0.57735f * tempx - tempy;
        coords.y = 0.57735f * tempx + tempy;

        return coords;
    }

    @Override
    public void show() {
    }

    @Override
    /**
     * renders the game display at consistent time steps
     */
    public void render(float delta) {

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

    @Override
    public void resize(int width, int height) {

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

    /************************************************* CAMERA MOVEMENT *************************************************/

    /**
     * Moves the camera based on the Input Keys
     * Also allows for zooming + scales the movement and bounds based on zooming
     *
     */
    public void moveCamera() {
        camera = canvas.getCamera();
        currentZoom = camera.zoom;
        if(Gdx.input.isKeyPressed(Input.Keys.W)) {
            camera.translate(0, 12*currentZoom*cameraSpeed(0)/acceleration_speed);
        }if(Gdx.input.isKeyPressed(Input.Keys.A)) {
            camera.translate(-12*currentZoom*cameraSpeed(2)/acceleration_speed, 0);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.S)) {
            camera.translate(0, -12*currentZoom*cameraSpeed(1)/acceleration_speed);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.D)) {
            camera.translate(12*currentZoom*cameraSpeed(3)/acceleration_speed, 0);
        }

        if(Gdx.input.isKeyPressed(Input.Keys.E)) {
            camera.zoom = (.99f)*currentZoom;
        } if(Gdx.input.isKeyPressed(Input.Keys.Q)) {
            camera.zoom = (1.01f)*currentZoom;
        }

        if(camera.zoom > 4.0f) {
            camera.zoom = 4.0f;
        }
        if(camera.zoom < 1.0f) {
            camera.zoom = 1.0f;
        }

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

    /************************************************* CONNECTORS *************************************************/
    /**
     * Connector inner class that represents part of a line between two nodes or a target and a node.
     *
     * Each connector has two parts: isometric coordinates of the grid that the connector is in, and a string
     * representing the directions that the connector points. The string contains some combination of the four letters
     * "ENSW," arranged in alphabetical order. The letters represent the following directions on the isometric grid:
     *      E = +x direction
     *      N = +y direction
     *      S = -y direction
     *      W = -x direction
     * where +x is the direction that would be the lower right on the screen, and +y is the direction that would be
     * upper right on the screen.
     *
     * For example, a connector with type "ENSW" would be a four-way connector. A connector with type "NS" would be
     * a straight line that runs from the upper right to lower right side of the grid tile, when viewed on a screen.
     */
    class Connector {
        /** The coordinates of the connector in isometric space */
        int xcoord;
        int ycoord;
        /** The string of directions representing the type of connector */
        String type;

        /**
         * Constructor for a connector. Saves the location and the type.
         *
         * @param x     The x-coordinate of the Connector in isometric space.
         * @param y     The y-coordinate of the Connector in isometric space.
         * @param t     The type of the Connector, represented as a string of "ENSW" with the directions that the
         *              Connector runs in.
         */
        public Connector(int x, int y, String t) {
            xcoord = x;
            ycoord = y;
            type = t;
        }

        /**
         * Constructor for a connector. Saves the location and the type.
         *
         * @param coords    The coordinates of the Connector in isometric space.
         * @param t         The type of the Connector, represented as a string of "ENSW" with the directions that
         *                  the Connector runs in.
         */
        public Connector(Vector2 coords, String t) {
            xcoord = (int)coords.x;
            ycoord = (int)coords.y;
            type = t;
        }
    }
}
