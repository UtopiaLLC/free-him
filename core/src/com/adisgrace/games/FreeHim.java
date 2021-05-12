package com.adisgrace.games;

import com.adisgrace.games.util.ScreenListener;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


//THIS IS GDXROOT
public class FreeHim extends Game implements ScreenListener {

	/** Player mode for the loading screen (CONTROLLER CLASS) */
	private LoadingMode loading;

	/** Player mode for the main menu screen (CONTROLLER CLASS) */
	private MainMenu mainmenu;
	/** Primary game controller for the game (CONTROLLER CLASS) */
	private GameController game;
	
	@Override
	public void create () {
		// Create main menu and set as starting screen
		loading = new LoadingMode();
		loading.setScreenListener(this);
		setScreen(loading);
//		mainmenu = new MainMenu();
//		mainmenu.setScreenListener(this);
//		setScreen(mainmenu);
	}

	@Override
	public void dispose () {

	}

	/**
	 * The given screen has made a request to exit its player mode.
	 *
	 * The value exitCode can be used to implement menu options.
	 *
	 * @param screen   The screen requesting to exit
	 * @param exitCode The state of the screen upon exit
	 */
	public void exitScreen(Screen screen, int exitCode) {
		// If the current screen is the main menu and exitScreen is
		// called, start the game
		if(screen == loading) {
			mainmenu = new MainMenu();
			mainmenu.setScreenListener(this);
			setScreen(mainmenu);

			loading.dispose();
			loading = null;
		}
		if (screen == mainmenu) {
			// Create primary game controller
			game = new GameController();
			setScreen(game);

			mainmenu.dispose();
			mainmenu = null;
		}
	}
}
