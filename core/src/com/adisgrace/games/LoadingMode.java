package com.adisgrace.games;

import com.adisgrace.games.util.AssetDirectory;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class LoadingMode implements Screen {

    /** Internal assets for this loading screen */
    private AssetDirectory internal;
    /** The actual assets to be loaded */
    private AssetDirectory assets;

    private ImageButton playButton;
    private Texture background;

    private Stage stage;

    /** Whether or not this player mode is still active */
    private boolean active;



    public LoadingMode() {
        internal = new AssetDirectory("loading.json");
        internal.loadAssets();
        internal.finishLoading();

        playButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(
                internal.getEntry("play", Texture.class))));
        background = internal.getEntry("background", Texture.class);



        //Gdx.input.setInputProcessor();

        assets = new AssetDirectory("assets.json");
        assets.loadAssets();
        active = true;

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float v) {

    }

    @Override
    public void resize(int i, int i1) {

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
