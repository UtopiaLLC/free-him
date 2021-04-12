package com.adisgrace.games.leveleditor;

import com.adisgrace.games.GameCanvas;
import com.adisgrace.games.GameController;
import com.adisgrace.games.WorldModel;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
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
    /** canvas is the primary view class of the game */
    private GameCanvas canvas;
    /** currentZoom controls how much the camera is zoomed in or out */
    private float currentZoom;

    Stage stage;

    private ShapeRenderer shapeRenderer;

    public LevelEditorController() {

        shapeRenderer = new ShapeRenderer();

        // Create canvas and set view and zoom
        canvas = new GameCanvas();
        ExtendViewport viewport = new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        viewport.setCamera(canvas.getCamera());
        currentZoom = canvas.getCamera().zoom;
        canvas.getCamera().zoom = 1.5f;

        // Create stage
        stage = new Stage();
        Gdx.input.setInputProcessor(stage);

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
    }

    public void addImage() {
        // Create image
        final Image im = new Image(new Texture(Gdx.files.internal("node/blue.png")));
        stage.addActor(im);
        im.setPosition(0, 0);
        im.setOrigin(0, 0);

        // Add drag listener to image that updates position on drag
        im.addListener((new DragListener() {
            public void touchDragged (InputEvent event, float x, float y, int pointer) {
                // When dragging, snaps image center to cursor
                float dx = x-im.getWidth()*0.5f;
                float dy = y-im.getHeight()*0.5f;
                im.setPosition(im.getX() + dx, im.getY() + dy);
            }
        }));
    }

    @Override
    public void show() {
    }

    @Override
    /**
     * renders the game display at consistent time steps
     */
    public void render(float delta) {
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
}
