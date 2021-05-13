package com.adisgrace.games;

import com.adisgrace.games.util.AssetDirectory;
import com.adisgrace.games.leveleditor.LevelEditorConstants;
import com.adisgrace.games.util.GameConstants;
import com.adisgrace.games.util.ScreenListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

/**
 * This class handles the main menu of the game that is shown on startup. It shows the Play, Settings,
 * and Credits options, and launches the game on Play.
 */
public class MainMenu implements Screen {
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;
    /** Canvas is the primary view class of the game */
    private GameCanvas canvas = new GameCanvas();
    /** Stage to set up main menu UI */
    Stage stage;
    /** Table for main menu buttons */
    Table menuButtons = new Table();
    /** Table for settings */
    Table settings = new Table();
    /** Which image to show as the background */
    Texture background;
    /** Back button for when in a submenu and need to go back */
    ImageButton back;

    AssetDirectory directory;

    private static final String TRD_BACK_BUTTON = "MainMenu:Back";
    /** Assets for the main menu */
    private static final String TITLE_ASSET = "MainMenu:Title";
    /** Assets for the main menu */
    private static final String CREDIT_ASSET = "MainMenu:CreditsScreen";
    private static final String[] MENU_BUTTON_ASSETS = new String[]{
            "MainMenu:Play",
            "MainMenu:Settings",
            "MainMenu:Credits"
    };

    /*********************************************** CONSTRUCTOR ***********************************************/
    /**
     * Constructor for a main menu.
     */

    public MainMenu(final AssetDirectory directory) {
        // Create canvas and set view and zoom
        canvas = new GameCanvas();

        this.directory = directory;
        // Set up camera

        ExtendViewport viewport = new ExtendViewport(canvas.getWidth(), canvas.getHeight());

        // Create stage for grid and tile with isometric grid
        stage = new Stage(viewport);
        // Add table for menu buttons to stage
        stage.addActor(menuButtons);
        // Add table for settings to stage
        stage.addActor(settings);

        // Handle inputs with a Multiplexer
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(inputMultiplexer);

        background = directory.getEntry(TITLE_ASSET, Texture.class);

        // Create and place back button, initialized as hidden
        createBackButton();

        // Create main menu buttons
        createMainMenu();

        // Create settings options, initialized as hidden
        createSettings();

        // Handle what to do when each of the main menu buttons is clicked

        // Add listener to play button to start game on click
        // TODO: in the final product this should go to the loading mode, not start the game
        menuButtons.getChild(0).addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                exit();
            }
        });

        // TODO: currently the Settings button does nothing
        // When Settings button is clicked, show settings screen
        menuButtons.getChild(1).addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Hide main menu
                menuButtons.setVisible(false);
                // Show settings options
                settings.setVisible(true);
                // Show back button
                back.setVisible(true);
            }
        });

        // When Credits button is clicked, show Credits screen
        menuButtons.getChild(2).addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Hide main menu
                menuButtons.setVisible(false);
                // Show credits as background
                background = directory.getEntry(CREDIT_ASSET, Texture.class);
                // Show back button
                back.setVisible(true);
            }
        });
    }

    /**
     * Creates and arranges the settings on the screen.
     */
    private void createSettings() {
        // Create labels for the sliders and add to table
        final Label musicLabel = new Label("Music Volume", LevelEditorConstants.skin);
        final Label sfxLabel = new Label("Sound Effects Volume", LevelEditorConstants.skin);
        settings.addActor(musicLabel);
        settings.addActor(sfxLabel);

        // Create sliders for music and sound effects volume
        final Slider musicVolume = new Slider(0,100,1,false, LevelEditorConstants.skin);
        final Slider sfxVolume = new Slider(0,100,1,false, LevelEditorConstants.skin);
        settings.addActor(musicVolume);
        settings.addActor(sfxVolume);
        // Initialize sliders at 100% volume
        musicVolume.setValue(100);
        sfxVolume.setValue(100);

        // Create labels for values of sliders and add to table
        final Label musicValLabel = new Label("100", LevelEditorConstants.skin);
        final Label sfxValLabel = new Label("100", LevelEditorConstants.skin);
        settings.addActor(musicValLabel);
        settings.addActor(sfxValLabel);

        // Add listeners to sliders so that they update the value labels when the sliders change
        musicVolume.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                musicValLabel.setText((int) (musicVolume.getVisualPercent() * 100));
            }
        });
        sfxVolume.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                sfxValLabel.setText((int) (sfxVolume.getVisualPercent() * 100));
            }
        });

        // Prepare to start arranging things on screen
        float height = (GameConstants.MENU_HEIGHT + 0.3f) * canvas.getHeight();

        // Place Settings title above just to make it clear what the page is
        Image settingsTitle = new Image(directory.getEntry(MENU_BUTTON_ASSETS[1], Texture.class));
        settingsTitle.setScale(1.7f);
        settingsTitle.setPosition(canvas.getWidth() / 2f - settingsTitle.getWidth() * settingsTitle.getScaleX() / 2f, height);
        settings.addActor(settingsTitle);

        height -= 0.28f * canvas.getHeight();

        // Place sliders at center of screen along with accompanying label descriptors and values
        // Music volume
        musicLabel.setPosition(canvas.getWidth() / 2f - musicLabel.getWidth() - 20, height + 2);
        musicVolume.setPosition(canvas.getWidth() / 2f, height);
        musicValLabel.setPosition(musicVolume.getX() + musicVolume.getWidth() + 20, height + 2);

        height -= 0.07f * canvas.getHeight();

        // Sound effects volume
        sfxLabel.setPosition(canvas.getWidth() / 2f - sfxLabel.getWidth() - 20, height + 2);
        sfxVolume.setPosition(canvas.getWidth() / 2f, height);
        sfxValLabel.setPosition(sfxVolume.getX() + sfxVolume.getWidth() + 20, height + 2);

        // Initialize settings as hidden
        settings.setVisible(false);
    }

    /**
     * Creates and arranges the main menu on the screen.
     */
    private void createMainMenu() {
        // Initialize variables to use for main menu buttons
        ImageButton button;
        float height = GameConstants.MENU_HEIGHT;

        // Go through array of assets for menu buttons and create a button for each
        for (String buttonString : MENU_BUTTON_ASSETS) {
            button = new ImageButton(new TextureRegionDrawable(directory.getEntry(buttonString, Texture.class)));
            // Set button position to be halfway across the screen horizontally
            button.setPosition(0.5f * canvas.getWidth() - (button.getWidth() * GameConstants.BUTTON_SCALE/ 2),
                    height * canvas.getHeight());
            button.setTransform(true);
            button.setScale(GameConstants.BUTTON_SCALE);
            menuButtons.addActor(button);

            // Decrement height modifier
            height -= 0.1;
        }
    }

    /**
     * Creates and places the back button in the upper left corner of the screen.
     */
    private void createBackButton() {
        // Create and place back button for when in a submenu, but don't show it yet
        back = new ImageButton(new TextureRegionDrawable(directory.getEntry(TRD_BACK_BUTTON, Texture.class)));
        back.setTransform(true);
        back.setScale(0.7f);
        back.setPosition(35,GameConstants.SCREEN_HEIGHT - 70);
        back.setVisible(false);
        stage.addActor(back);

        // When clicked, this button returns you to the main menu
        back.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Show main menu
                menuButtons.setVisible(true);
                // Show title screen as background
                background = directory.getEntry(TITLE_ASSET, Texture.class);
                // Hide back button
                back.setVisible(false);
                // Hide settings if they aren't already hidden
                settings.setVisible(false);
            }
        });
    }

    /*********************************************** SCREEN METHODS ***********************************************/
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

    @Override
    public void show() {
        // Handle inputs with a Multiplexer
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);

        // Draw background image
        canvas.begin();

        canvas.draw(background, 0, 0, 1280, 720);
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
