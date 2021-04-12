package com.adisgrace.games.leveleditor;

import java.util.HashMap;

import com.adisgrace.games.FactNode;
import com.adisgrace.games.GameCanvas;
import com.adisgrace.games.GameController;
import com.adisgrace.games.WorldModel;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class LevelEditorController implements Screen {
    /** Canvas is the primary view class of the game */
    private GameCanvas canvas;
    /** View camera */
    private OrthographicCamera camera;
    /** CurrentZoom controls how much the camera is zoomed in or out */
    private float currentZoom;
    /** Stage that everything is shown on */
    Stage stage;

    /** Hashtable of all the images added */
    HashMap<String,Image> images;
    /** The count of the next image that is added */
    int imgCount;
    /** acceleration accumulators for camera movement */
    private int left_acc, right_acc, up_acc, down_acc;
    /** time taken for camera to accelerate to max speed */
    private int acceleration_speed = 40;

    public LevelEditorController() {
        // Create canvas and set view and zoom
        canvas = new GameCanvas();

        // Set up camera
        ExtendViewport viewport = new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        viewport.setCamera(canvas.getCamera());
        currentZoom = canvas.getCamera().zoom;
        canvas.getCamera().zoom = 1.5f;

        // Create stage
        stage = new Stage();
        Gdx.input.setInputProcessor(stage);
        canvas.drawIsometricGrid(stage);

        // Create a button to add new images
        Drawable drawable = new TextureRegionDrawable(new Texture(Gdx.files.internal("skills/overwork.png")));
        ImageButton button = new ImageButton(drawable);
        stage.addActor(button);
        button.setPosition(500,500);
        // Add listener to button
        button.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                System.out.println("Button Pressed");
                addImage();
            }
        });

        // Initialize hashmap of images
        images = new HashMap<String, Image>();
    }

    public void addImage() {
        // Create image
        final Image im = new Image(new Texture(Gdx.files.internal("node/blue.png")));
        stage.addActor(im);
        im.setPosition(0, 0);
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
                float dy = y-im.getHeight()*0.5f;
                im.setPosition(im.getX() + dx, im.getY() + dy);
            }
        }));
        // Add drag listener that snaps to grid when drag ends
        im.addListener((new DragListener() {
            public void dragStop (InputEvent event, float x, float y, int pointer) {
                System.out.println("Snap");

                float newX = im.getX() + x-im.getWidth()*0.5f;
                float newY = im.getY() + y-256.0f;


                im.setPosition(newX - (newX % 222), newY - (newY % 128));
            }
        }));
    }

    @Override
    public void show() {
    }

    /**
     * Snaps an image to the center of the nearest isometric grid cell.
     *
     * The image is snapped to the grid cell that the mouse cursor is over.
     * It may not be super reliable, but it mostly will work.
     *
     * The grid cells are arranged so that (0,0) is the center of a grid cell.
     *
     * @param im        Image to snap to grid
     * @param mouseX    x-coordinate of mouse cursor
     * @param mouseY    y-coordinate of mouse cursor
     */
    public void snapToGrid(Image im, float mouseX, float mouseY) {
        // Round down
        int x = (int)(Gdx.input.getX() / 444);
        int y = (int)(Gdx.input.getX() / 256);

        float dx = mouseX-im.getWidth()*0.5f;
        float dy = mouseY-im.getHeight()*0.5f;
        im.setPosition(im.getX() + dx, im.getY() + dy);
    }

    @Override
    /**
     * renders the game display at consistent time steps
     */
    public void render(float delta) {
        canvas.clear();

        // Move camera
        moveCamera();

        // Draw objects on canvas
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
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
}
