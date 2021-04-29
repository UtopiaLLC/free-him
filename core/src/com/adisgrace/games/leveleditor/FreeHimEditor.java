package com.adisgrace.games.leveleditor;

import com.adisgrace.games.util.ScreenListener;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;


//THIS IS GDXROOT
public class FreeHimEditor extends Game implements ScreenListener {
    /** Level editor start screen (CONTROLLER CLASS) */
    private LevelEditorStart start;
    /** Primary controller for the level editor (CONTROLLER CLASS) */
    private LevelEditorController editor;

    @Override
    public void create () {
        // Create level editor start screen and initialize as first screen
        start = new LevelEditorStart();
        start.setScreenListener(this);
        setScreen(start);
    }

    @Override
    public void dispose () {

    }

    /**
     * The given screen has made a request to exit its player mode.
     *
     * The value exitCode can be used to implement menu options. The
     * following exitCodes give the following options:
     * [0] Create a new level
     * [1] Load in a previously-saved level
     *
     * @param screen   The screen requesting to exit
     * @param exitCode The state of the screen upon exit
     */
    public void exitScreen(Screen screen, int exitCode) {
        // If the current screen is the start screen and we want to
        // create a new level, do that
        if (screen == start && exitCode == 0) {
            // Create primary level editor controller
            editor = new LevelEditorController();
            setScreen(editor);

            start.dispose();
            start = null;
        }
        // If the current screen is the start screen and we want to
        // load a previously-saved level, do that
        else if (screen == start && exitCode == 1) {
            // Create primary level editor controller
            editor = new LevelEditorController(start.getLevelToLoad());
            setScreen(editor);

            start.dispose();
            start = null;
        }
    }
}