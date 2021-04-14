package com.adisgrace.games.leveleditor;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Vector2;

public class GestureDetectorTest extends GdxTest implements ApplicationListener {
    Texture texture;
    Texture t2;
    SpriteBatch batch;
    OrthographicCamera camera;
    CameraController controller;
    GestureDetector gestureDetector;

    int width;
    int height;

    class CameraController implements GestureListener {
        float velX, velY;
        float initialScale = 1;

        public boolean touchDown (float x, float y, int pointer, int button) {
            initialScale = camera.zoom;
            return false;
        }

        @Override
        public boolean tap (float x, float y, int count, int button) {
            Gdx.app.log("GestureDetectorTest", "tap at " + x + ", " + y + ", count: " + count);

            // Check if click is on asset
            //if ()

            return false;
        }

        @Override
        public boolean longPress (float x, float y) {
            Gdx.app.log("GestureDetectorTest", "long press at " + x + ", " + y);
            return false;
        }

        @Override
        public boolean fling (float velocityX, float velocityY, int button) {
            return false;
        }

        @Override
        public boolean pan (float x, float y, float deltaX, float deltaY) {
            // Gdx.app.log("GestureDetectorTest", "pan at " + x + ", " + y);
            camera.position.add(-deltaX * camera.zoom, deltaY * camera.zoom, 0);
            return false;
        }

        @Override
        public boolean panStop (float x, float y, int pointer, int button) {
            Gdx.app.log("GestureDetectorTest", "pan stop at " + x + ", " + y);
            return false;
        }

        @Override
        public boolean zoom (float originalDistance, float currentDistance) {
            float ratio = originalDistance / currentDistance;
            camera.zoom = initialScale * ratio;
            System.out.println(camera.zoom);
            return false;
        }

        @Override
        public boolean pinch (Vector2 initialFirstPointer, Vector2 initialSecondPointer, Vector2 firstPointer, Vector2 secondPointer) {
            return false;
        }

        public void update () {
        }

        @Override
        public void pinchStop () {
        }
    }

    @Override
    public void create () {
        texture = new Texture(Gdx.files.internal("badlogic.jpg"));
        t2 = texture = new Texture(Gdx.files.internal("badlogic.jpg"));

        width = texture.getWidth();
        height = texture.getHeight();

        batch = new SpriteBatch();
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        controller = new CameraController();
        gestureDetector = new GestureDetector(20, 40, 0.5f, 2, 0.15f, controller);
        Gdx.input.setInputProcessor(gestureDetector);
    }

    @Override
    public void render () {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        controller.update();
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(texture, 0, 0, texture.getWidth(), texture.getHeight());
        batch.draw(t2, 30, 30, t2.getWidth(), t2.getHeight());
        batch.end();
    }

    @Override
    public void dispose () {
        texture.dispose();
        batch.dispose();
    }
}