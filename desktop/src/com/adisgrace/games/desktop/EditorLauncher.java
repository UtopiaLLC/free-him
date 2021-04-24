package com.adisgrace.games.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.adisgrace.games.leveleditor.FreeHimEditor;

public class EditorLauncher {
    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

        config.width = 1280;
        config.height = 720;

        new LwjglApplication(new FreeHimEditor(), config);
    }
}
