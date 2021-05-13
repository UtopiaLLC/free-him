package com.adisgrace.games;

import com.adisgrace.games.util.AssetDirectory;
import com.adisgrace.games.util.ScreenListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class LoadingMode implements Screen {

    /** Internal assets for this loading screen */
    private AssetDirectory internal;
    /** The actual assets to be loaded */
    private AssetDirectory assets;

    private ImageButton playButton;
    private Texture background;
    private Texture torch;

    private Stage stage;

    private GameCanvas canvas;

    /** Whether or not this player mode is still active */
    private boolean active;
    /** Current progress (0 to 1) of the asset manager */
    private float progress;
    /** The amount of time to devote to loading assets (as opposed to on screen hints, etc.) */
    private int budget;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    Animation<TextureRegion> torchAnimation;
    private float stateTime;
    private TextureRegion reg;

    private boolean buttonAdded;


    public LoadingMode() {

        canvas = new GameCanvas();
        budget = 1;
        ExtendViewport viewport = new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        viewport.setCamera(canvas.getCamera());
        stage = new Stage(viewport);

        Gdx.input.setInputProcessor(stage);

        internal = new AssetDirectory("loading.json");
        internal.loadAssets();
        internal.finishLoading();

        torch = internal.getEntry("torch", Texture.class);
        TextureRegion[][] regions = new TextureRegion(torch).split(
                torch.getWidth() / 6,
                torch.getHeight() / 1);

        TextureRegion[] torchFrames = new TextureRegion[6];

        for (int i = 0; i < 6; i++) {
            //combined = GameCanvas.combineTextures(regions[j][i], node_regions[0][j]);
            //spinFrames[i] = new TextureRegion(combined);
            torchFrames[i] = new TextureRegion(regions[0][i]);
        }
        torchAnimation = new Animation<TextureRegion>(0.25f, torchFrames);
        torchAnimation.setPlayMode(Animation.PlayMode.LOOP);

        playButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(
                internal.getEntry("play", Texture.class))));
        playButton.setPosition(Gdx.graphics.getWidth()/2 - internal.getEntry("play", Texture.class).getWidth()/2,
                Gdx.graphics.getHeight()*.35f- internal.getEntry("play", Texture.class).getHeight()/2);

        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                System.out.println("Clicked");
                exitLoad();
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                System.out.println("Hovered");
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
            }
        });
        buttonAdded = false;
        background = internal.getEntry("background", Texture.class);



        //Gdx.input.setInputProcessor();

        assets = new AssetDirectory("assets.json");
        assets.loadAssets();
        active = true;
        NodeView.loadAnimations();

    }

    public AssetDirectory getAssets() {
        return assets;
    }

    public void exitLoad(){
        listener.exitScreen(this, 0);
    }

    @Override
    public void show() {
        active = true;
    }

    /**
     * Update the status of this player mode.
     *
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     *
     * @param delta Number of seconds since last animation frame
     */
    private void update(float delta) {
        assets.update(budget);
        this.progress = assets.getProgress();

        if (progress >= 1.0f) {
            if(!buttonAdded) {
                this.progress = 1.0f;
                //playButton = internal.getEntry("play",Texture.class);
                stage.addActor(playButton);
            }
        }
    }

    /**
     * Draw the status of this player mode.
     *
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     */
    private void draw() {
        canvas.clear();
        canvas.begin();
        //canvas.draw(background, 0, 0);
        drawTorch(canvas);
        canvas.end();

        stage.act();
        stage.draw();


    }

    public void drawTorch(GameCanvas canvas) {
        stateTime += Gdx.graphics.getDeltaTime();
        reg = torchAnimation.getKeyFrame(stateTime, true);

        //Color tint = (pressState == 1 ? Color.GRAY: Color.WHITE);
//        canvas.draw(reg,Gdx.graphics.getWidth()/2, Gdx.graphics.getWidth() * .75,
//                reg.getWidth()/2, reg.getHeight()/2,
//                , 0, 1, 1, 0);
        canvas.getSpriteBatch().draw(reg,Gdx.graphics.getWidth()/2 - reg.getRegionWidth()/2,
                Gdx.graphics.getHeight() * .75f - reg.getRegionHeight()/2, reg.getRegionWidth()/2,
                reg.getRegionHeight() / 2, reg.getRegionWidth(), reg.getRegionHeight(), .75f, .75f, 0);
    }

    @Override
    public void render(float v) {
        if (active) {
            update(v);
            draw();

        }
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
        active = false;
    }

    @Override
    public void dispose() {
        internal.unloadAssets();
        internal.dispose();
    }
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }
}
