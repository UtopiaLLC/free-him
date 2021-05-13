package com.adisgrace.games;

import com.adisgrace.games.util.AssetDirectory;
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
	/** Primary game controller for the game (CONTROLLER CLASS) */
	private LevelSelection levelSelection;
	/** AssetManager to load game assets (textures, sounds, etc.) */
	AssetDirectory directory;
	
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
		setScreen(null);
		game.dispose();
		mainmenu.dispose();

		// Unload all of the resources
		if (directory != null) {
			directory.unloadAssets();
			directory.dispose();
			directory = null;
		}
		super.dispose();
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
			directory = loading.getAssets();
			mainmenu = new MainMenu(directory);
			mainmenu.setScreenListener(this);
			setScreen(mainmenu);

			loading.dispose();
			loading = null;
		}
		else if (screen == mainmenu) {
			// Create primary game controller
			levelSelection = new LevelSelection();
			setScreen(levelSelection);

			mainmenu.dispose();
			mainmenu = null;
		}
		else if (screen == levelSelection) {
			// Create primary game controller
			game = new GameController(directory);
			setScreen(game);

			levelSelection.dispose();
			levelSelection = null;
		}
		else if (screen == game) {
			if(exitCode == 1) {

			} else if(exitCode == 2){

			}
		}
	}
}
