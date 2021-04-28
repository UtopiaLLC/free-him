package com.adisgrace.games;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;

public class CameraController {
    /** acceleration accumulators for camera movement */
    private int left_acc, right_acc, up_acc, down_acc;
    /** Gets player input */
    private InputController input;
    /** View camera for node map */
    private OrthographicCamera camera;
    /** CurrentZoom controls how much the camera is zoomed in or out */
    private float currentZoom;

    /** Vector caches to avoid initializing vectors every time */
    private Vector2 vec = new Vector2();
    private Vector3 vec3 = new Vector3();

    /** time taken for camera to accelerate to max speed */
    private final static int ACCELERATION_SPEED = 40;

    /**
     * Constructor for a camera controller.
     */
    public CameraController(InputController input, GameCanvas canvas) {
        this.input = input;

        // Set up camera
        camera = canvas.getCamera();

        // Set initial zoom
        currentZoom = camera.zoom;
        camera.zoom = 2f;
    }

    /**
     * Sets the camera viewport to the given viewport.
     *
     * @param viewport      Viewport to set the camera's viewport to.
     */
    public void setViewport(Viewport viewport) {
        viewport.setCamera(camera);
    }

    /**
     * Returns the height of the camera viewport.
     *
     * @return  Height of the camera viewport.
     */
    public float getHeight() {
        return camera.viewportHeight;
    }

    /**
     * Returns the width of the camera viewport.
     *
     * @return  Width of the camera viewport.
     */
    public float getWidth() {
        return camera.viewportWidth;
    }

    /**
     * Converts a point from screen space to world space coordinates.
     *
     * @param x   x-coordinate in screen space
     * @param y   y-coordinate in screen space
     * @return    Coordinates converted to world space
     */
    public Vector2 screenToWorld(float x, float y) {
        vec3.set(x,y,0);
        camera.unproject(vec3);
        vec.set(vec3.x,vec3.y);
        return vec;
    }

    /**
     * Adjusts the viewport to ensure that the game world appears to be the same
     * size, even when resizing.
     *
     * Should be called whenever the screen is resized.
     *
     * @param width     Screen width
     * @param height    Screen height
     */
    public void resize(float width, float height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.position.set(width/2f, height/2f, 0);
    }

    /**
     * Handles camera movement and zoom based on user input.
     *
     * Also adjusts the world scale based on zoom.
     */
    public void moveCamera() {
        // Check for new input
        input.readInput();
        // Set current camera zoom
        currentZoom = camera.zoom;

        // Move camera if one of the WASD keys are pressed
        if(input.didUp()) {
            camera.translate(0, 12*currentZoom*cameraSpeed(0)/ACCELERATION_SPEED);
        }
        if(input.didLeft()) {
            camera.translate(-12*currentZoom*cameraSpeed(2)/ACCELERATION_SPEED, 0);
        }
        if(input.didDown()) {
            camera.translate(0, -12*currentZoom*cameraSpeed(1)/ACCELERATION_SPEED);
        }
        if(input.didRight()) {
            camera.translate(12*currentZoom*cameraSpeed(3)/ACCELERATION_SPEED, 0);
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
    private float cameraSpeed(int direction){
        // 0 = up, 1 = down, 2 = left, 3 = right
        float speed = 0f;
        //acceleration_speed = 40;
        switch (direction){
            case 0:
                if (up_acc == 0) clearSpeedRev(0);
                up_acc += 1;
                speed = up_acc > ACCELERATION_SPEED ? ACCELERATION_SPEED : up_acc;
                break;
            case 1:
                if (down_acc == 0) clearSpeedRev(1);
                down_acc += 1;
                speed = down_acc > ACCELERATION_SPEED ? ACCELERATION_SPEED : down_acc;
                break;
            case 2:
                if (left_acc == 0) clearSpeedRev(2);
                left_acc += 1;
                speed = left_acc > ACCELERATION_SPEED ? ACCELERATION_SPEED : left_acc;
                break;
            case 3:
                if (right_acc == 0) clearSpeedRev(3);
                right_acc += 1;
                speed = right_acc > ACCELERATION_SPEED ? ACCELERATION_SPEED : right_acc;
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
