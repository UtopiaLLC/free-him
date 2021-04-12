package com.adisgrace.games.leveleditor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;

public class DragTest extends GdxTest {

    Stage stage;

    public void create () {
        stage = new Stage();
        Gdx.input.setInputProcessor(stage);

        // Create image
        final Image im = new Image(new Texture(Gdx.files.internal("badlogic.jpg")));
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

    public void render () {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    public void resize (int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose () {
        stage.dispose();
    }
}
