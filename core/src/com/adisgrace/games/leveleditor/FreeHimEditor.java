package com.adisgrace.games.leveleditor;

import com.adisgrace.games.util.ScreenListener;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;


//THIS IS GDXROOT
public class FreeHimEditor extends Game implements ScreenListener {


    @Override
    public void create () {
        LevelEditorController screen = new LevelEditorController();
        setScreen(screen);
    }

    @Override
    public void dispose () {

    }

    @Override
    public void exitScreen(Screen screen, int exitCode) {

    }
}