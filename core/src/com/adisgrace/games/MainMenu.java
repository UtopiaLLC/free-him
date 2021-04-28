package com.adisgrace.games;

import com.adisgrace.games.util.ScreenListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

/**
 * This class handles the main menu of the game that is shown on startup. It shows the Play, Settings,
 * and Credits options, and launches the game on Play.
 */
public class MainMenu implements Screen {
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;
    /** Canvas is the primary view class of the game */
    private final GameCanvas canvas;
    /** Stage to set up main menu UI */
    Stage stage;
    /** Array of buttons in the main menu */
    private Array<ImageButton> menuButtons = new Array<>();

    /** Assets for the main menu */
    private static final Texture TITLE_ASSET = new Texture(Gdx.files.internal("mainmenu/MM_Title_1.png"));
    private static final Texture[] MENU_BUTTON_ASSETS = new Texture[]{
            new Texture(Gdx.files.internal("mainmenu/MM_Play_1.jpg")),
            new Texture(Gdx.files.internal("mainmenu/MM_Settings_1.jpg")),
            new Texture(Gdx.files.internal("mainmenu/MM_Credits_1.jpg"))
    };

    /** Constants for locations of buttons */
    /** Height of top of menu, relative to canvas height */
    private static final float MENU_HEIGHT = 0.37f;
    /** Button scale */
    private static final float BUTTON_SCALE = 0.8f;

    /*********************************************** CONSTRUCTOR ***********************************************/
    /**
     * Constructor for a main menu.
     */
    public MainMenu() {
        // Create canvas and set view and zoom
        canvas = new GameCanvas();
        // Set up camera
        ExtendViewport viewport = new ExtendViewport(canvas.getWidth(), canvas.getHeight());

        // Create stage for grid and tile with isometric grid
        stage = new Stage(viewport);

        // Handle inputs with a Multiplexer
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(inputMultiplexer);

        // Place buttons
        // Initialize variables to use
        ImageButton button;
        float height = MENU_HEIGHT;

        // Go through array of assets for menu buttons and create a button for each
        for (Texture buttonTex : MENU_BUTTON_ASSETS) {
            button = new ImageButton(new TextureRegionDrawable(buttonTex));
            // Set button position to be halfway across the screen horizontally
            button.setPosition(0.5f * canvas.getWidth() - (buttonTex.getWidth() * BUTTON_SCALE/ 2),
                    height * canvas.getHeight());
            button.setTransform(true);
            button.setScale(BUTTON_SCALE);
            stage.addActor(button);
            menuButtons.add(button);

            // Decrement height modifier
            height -= 0.1;
        }

        // Add listener to play button to start game on click
        // TODO: in the final product this should go to the loading mode, not start the game
        button = menuButtons.get(0);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                exit();
            }
        });

        // TODO: currently the Settings and Credits buttons do nothing
    }

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    /**
     * Exits this screen.
     */
    private void exit() {
        listener.exitScreen(this,0);
    }

    /*********************************************** SCREEN METHODS ***********************************************/
    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);

        // Draw background image
        canvas.begin();
        canvas.draw(TITLE_ASSET, 0, 0, 1280, 720);
        canvas.end();

        // Draw buttons
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        canvas.resize();

        // Keep buttons in the same place when resizing
        stage.getViewport().update(width,height,true);
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
