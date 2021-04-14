package com.adisgrace.games.leveleditor;

import com.adisgrace.games.leveleditor.LevelEditorController;
import com.adisgrace.games.util.ScreenListener;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


//THIS IS GDXROOT
public class FreeHimTest extends Game implements ScreenListener {


    @Override
    public void create () {
        LevelEditorController screen = new LevelEditorController();
        setScreen(screen);

        //TargetModel target = new TargetModel("PatrickWestfield.json");
    }

    @Override
    public void dispose () {

    }

    @Override
    public void exitScreen(Screen screen, int exitCode) {

    }
}
