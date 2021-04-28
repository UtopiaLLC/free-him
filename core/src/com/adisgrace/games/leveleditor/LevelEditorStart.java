package com.adisgrace.games.leveleditor;

import com.adisgrace.games.GameCanvas;
import static com.adisgrace.games.leveleditor.LevelEditorConstants.*;

import com.adisgrace.games.util.ScreenListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.SerializationException;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

/**
 * This class is the starting screen of the level editor, and will prompt the user to either make a
 * new level or load in an old one.
 *
 * An exit code of 0 indicates that the level editor for a new level should be created, whereas an
 * exit code of 1 indicates that a previously-saved level should be loaded into the level editor.
 */
public class LevelEditorStart implements Screen {
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;
    /** Canvas is the primary view class of the game */
    private final GameCanvas canvas;
    /** Stage to set up main menu UI */
    Stage stage;

    /** Text field where the name of the level to load should be entered */
    TextField levelToLoadField;
    /** If a level is to be loaded, store the name here */
    String levelToLoad;
    /** Label stating that the level does not exist */
    Label errorlabel;

    /** Assets for the main menu */
    private static final Texture TITLE_ASSET = new Texture(Gdx.files.internal("mainmenu/MM_Title_1.png"));

    /*********************************************** CONSTRUCTOR ***********************************************/
    /**
     * Constructor for a LevelEditorStart.
     */
    public LevelEditorStart() {
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

        // Label saying that this is the level editor
        Label label = FormFactory.newLabel("(THE LEVEL EDITOR)", 465f);
        label.setX((SCREEN_WIDTH / 2f) - (label.getWidth() / 2));
        stage.addActor(label);

        // New Level button
        TextButton newLevel = new TextButton("CREATE NEW LEVEL", skin);
        newLevel.setPosition((SCREEN_WIDTH / 2f) - (3 * newLevel.getWidth() / 2), 300);
        stage.addActor(newLevel);
        // Exit with exit code 0
        newLevel.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                exit(0);
            }
        });

        // Load Level button
        TextButton loadLevel = new TextButton("LOAD SAVED LEVEL", skin);
        loadLevel.setPosition((SCREEN_WIDTH / 2f) + (newLevel.getWidth() / 2), 300);
        stage.addActor(loadLevel);
        // Exit with exit code 1
        loadLevel.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Only exit if level to load is successfully stored, meaning it's a valid level that exists
                if (storeLevelToLoad()) exit(1);
            }
        });

        // TextField to put in name of level to load
        levelToLoadField = FormFactory.newTextField("Name of Level to Load", 225, 250,
                "",false);
        levelToLoadField.setX((SCREEN_WIDTH / 2f) - (levelToLoadField.getWidth() / 2));
        levelToLoadField.setAlignment(1);
        stage.addActor(levelToLoadField);

        // Label that lets the user know if the input level doesn't exist
        errorlabel = FormFactory.newLabel("Error: level does not exist", levelToLoadField.getY() + 30);
        errorlabel.setX((SCREEN_WIDTH / 2f) - (errorlabel.getWidth() / 2));
        // Keep it hidden until the user tries to load a level that doesn't exist
        errorlabel.setVisible(false);
        stage.addActor(errorlabel);
    }

    /**
     * Stores the name of the level to load written in the text field, and returns whether or not it is
     * a valid level.
     *
     * @return  Whether the currently-entered level exists.
     */
    private boolean storeLevelToLoad() {
        levelToLoad = levelToLoadField.getText();

        // Test parsing to see if the file exists
        String levelfile = levelToLoad;
        // If levelfile is missing the .json file extension, add it
        if (!levelfile.contains(".json")) levelfile += ".json";
        // If successful, file exists
        try {
            new JsonReader().parse(Gdx.files.internal("levels/" + levelfile));
            return true;
        }
        // If unsuccessful, file doesn't exist
        catch (SerializationException se) {
            // Make error message visible
            errorlabel.setVisible(true);
            return false;
        }
    }

    /**
     * Returns the name of the level to load into the level editor.
     *
     * @return  The name of the level to load into the level editor.
     */
    public String getLevelToLoad() {return levelToLoad;}

    /*********************************************** EXIT METHODS ***********************************************/

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    /**
     * Exits this screen with the given exit code.
     *
     * @param exitCode  Exit code that denotes what to do after exiting.
     */
    private void exit(int exitCode) {
        listener.exitScreen(this,exitCode);
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
        canvas.draw(TITLE_ASSET, -20, 0, SCREEN_WIDTH+20, SCREEN_HEIGHT);
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
