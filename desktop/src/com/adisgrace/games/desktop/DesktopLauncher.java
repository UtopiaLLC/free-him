package com.adisgrace.games.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.adisgrace.games.FreeHim;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1280;
		config.height = 720;
		config.title = "Free Him";


//		config.width = 300;
//		config.height = 400;

		new LwjglApplication(new FreeHim(), config);
	}
}
