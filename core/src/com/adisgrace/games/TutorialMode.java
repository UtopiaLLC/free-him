package com.adisgrace.games;

import com.adisgrace.games.util.AssetDirectory;
import com.adisgrace.games.util.GameConstants;
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
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class TutorialMode implements Screen {

    /** The actual assets to be loaded */
    private AssetDirectory directory;

    private Label playButton;
    private Image nextButton;
    private Image prevButton;
    private Label numPic;

    private Texture background;
    private Texture torch;

    private Stage stage;

    private GameCanvas canvas;

    private Array<Image> tutorialImages;
    private Image tutorialImage;



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

    private int currentImage;

    private boolean buttonAdded;
    private boolean canStart;


    public TutorialMode(AssetDirectory directory, Array<String> tutorialPaths) {

        canStart = false;

        canvas = new GameCanvas();
        this.directory = directory;
        ExtendViewport viewport = new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        viewport.setCamera(canvas.getCamera());
        stage = new Stage(viewport);

        Gdx.input.setInputProcessor(stage);

        torch = directory.getEntry("torch", Texture.class);
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


        playButton = new Label("Start", GameConstants.SELECTION_SKIN, "VCR");
        playButton.setPosition(Gdx.graphics.getWidth()/2 - playButton.getWidth()/2,
                Gdx.graphics.getHeight()*.05f- playButton.getHeight()/2);

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
        //stage.addActor(playButton);

        tutorialImages = new Array<>();
        if(tutorialPaths.size > 0) {
            for(int i = 0; i < tutorialPaths.size; i++) {
                tutorialImage = new Image(directory.getEntry(tutorialPaths.get(i), Texture.class));
                tutorialImage.setPosition(Gdx.graphics.getWidth() / 2 - tutorialImage.getWidth() / 2,
                        Gdx.graphics.getHeight() * .1f + playButton.getHeight() / 2 + 50f);
                tutorialImages.add(tutorialImage);
                currentImage = 0;
                tutorialImage = tutorialImages.get(currentImage);
                stage.addActor(tutorialImage);
            }

        } else {
            stage.addActor(playButton);
            
        }


        nextButton = new Image(directory.getEntry("Tutorial:Next", Texture.class));
        nextButton.setPosition(Gdx.graphics.getWidth()*.67f - nextButton.getWidth()/2,
                Gdx.graphics.getHeight()*.15f- nextButton.getHeight()/2);

        nextButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                System.out.println("Clicked");
                if(currentImage+1 < tutorialImages.size) {
                    currentImage++;
                    numPic.setText(currentImage+1+"/"+tutorialImages.size);
                    tutorialImage.remove();
                    tutorialImage = tutorialImages.get(currentImage);
                    stage.addActor(tutorialImage);
                } else {
                    if(!canStart) {
                        canStart = true;
                        stage.addActor(playButton);
                    }
                }

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
        stage.addActor(nextButton);

        prevButton = new Image(directory.getEntry("Tutorial:Prev", Texture.class));
        prevButton.setPosition(Gdx.graphics.getWidth()*.33f - prevButton.getWidth()/2,
                Gdx.graphics.getHeight()*.15f- prevButton.getHeight()/2);

        prevButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                System.out.println("Clicked");
                if(currentImage > 0) {
                    currentImage--;
                    numPic.setText(currentImage+1+"/"+tutorialImages.size);
                    tutorialImage.remove();
                    tutorialImage = tutorialImages.get(currentImage);
                    stage.addActor(tutorialImage);
                }

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
        stage.addActor(prevButton);

        background = directory.getEntry("background", Texture.class);

        numPic = new Label("1/"+tutorialImages.size, GameConstants.SELECTION_SKIN, "tutorial-text");
        numPic.setPosition(Gdx.graphics.getWidth()*.5f - numPic.getWidth()/2,
                Gdx.graphics.getHeight()*.17f- numPic.getHeight()/2);
        stage.addActor(numPic);

        //Label tutorialTex = new Label(tutorialText, GameConstants.SELECTION_SKIN, "tutorial-text");
//        if(tutorialText.length() > 30) {
//            tutorialTex.setWidth(600);
//        }
//        tutorialTex.setWrap(true);
//        tutorialTex.setPosition(Gdx.graphics.getWidth() / 2 - tutorialTex.getWidth()/2, Gdx.graphics.getHeight()*.5f - tutorialTex.getHeight());
//        stage.addActor(tutorialTex);
        //Gdx.input.setInputProcessor();

    }


    public void exitLoad(){
        listener.exitScreen(this, 0);
    }

    @Override
    public void show() {
        active = true;
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
        canvas.draw(background, 0, 0);
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
                Gdx.graphics.getHeight() * .80f - reg.getRegionHeight()/2, reg.getRegionWidth()/2,
                reg.getRegionHeight() / 2, reg.getRegionWidth(), reg.getRegionHeight(), .75f, .75f, 0);
    }

    @Override
    public void render(float v) {
        draw();
    }

    @Override
    public void resize(int i, int i1) {
        stage.getViewport().update(i,i1, true);
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

    }
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }
}
