/*
 * InputController.java
 *
 * This class buffers in input from the devices and converts it into its
 * semantic meaning. If your game had an option that allows the player to
 * remap the control keys, you would store this information in this class.
 * That way, the main GameEngine does not have to keep track of the current
 * key mapping.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package com.adisgrace.games;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Class for reading player input. 
 *
 * This supports both a keyboard and X-Box controller. In previous solutions, we only 
 * detected the X-Box controller on start-up.  This class allows us to hot-swap in
 * a controller via the new XBox360Controller class.
 */
public class InputController {
	// Sensitivity for moving crosshair with gameplay


	/** The singleton instance of the input controller */
	private static InputController theController = null;
	
	/** 
	 * Return the singleton instance of the input controller
	 *
	 * @return the singleton instance of the input controller
	 */
	public static InputController getInstance() {
		if (theController == null) {
			theController = new InputController();
		}
		return theController;
	}

	// Fields to manage specific button presses
	/** Whether each of the WASD buttons (for camera movement) have been pressed */
	private boolean wPressed;
	private boolean aPressed;
	private boolean sPressed;
	private boolean dPressed;
	/** Whether each of the EQ buttons (for camera zoom) have been pressed */
	private boolean ePressed;
	private boolean qPressed;
	
	/** How much did we move horizontally? */
	private float horizontal;
	/** How much did we move vertically? */
	private float vertical;
	/** The crosshair position (for raddoll) */
	private Vector2 crosshair;
	/** The crosshair cache (for using as a return value) */
	private Vector2 crosscache;
	/** For the gamepad crosshair control */
	private float momentum;

	
	/**
	 * Returns the amount of sideways movement. 
	 *
	 * -1 = left, 1 = right, 0 = still
	 *
	 * @return the amount of sideways movement. 
	 */
	public float getHorizontal() {
		return horizontal;
	}
	
	/**
	 * Returns the amount of vertical movement. 
	 *
	 * -1 = down, 1 = up, 0 = still
	 *
	 * @return the amount of vertical movement. 
	 */
	public float getVertical() {
		return vertical;
	}
	
	/**
	 * Returns the current position of the crosshairs on the screen.
	 *
	 * This value does not return the actual reference to the crosshairs position.
	 * That way this method can be called multiple times without any fair that 
	 * the position has been corrupted.  However, it does return the same object
	 * each time.  So if you modify the object, the object will be reset in a
	 * subsequent call to this getter.
	 *
	 * @return the current position of the crosshairs on the screen.
	 */
	public Vector2 getCrossHair() {
		return crosscache.set(crosshair);
	}


	/******************************************** CAMERA CONTROL GETTERS ********************************************/

	/**
	 * Returns true if the W key was pressed.
	 *
	 * @return true if the W key was pressed.
	 */
	public boolean didUp() {return wPressed;}

	/**
	 * Returns true if the D key was pressed.
	 *
	 * @return true if the D key was pressed.
	 */
	public boolean didRight() {return dPressed;}

	/**
	 * Returns true if the A key was pressed.
	 *
	 * @return true if the A key was pressed.
	 */
	public boolean didLeft() {return aPressed;}

	/**
	 * Returns true if the S key was pressed.
	 *
	 * @return true if the S key was pressed.
	 */
	public boolean didDown() {return sPressed;}

	/**
	 * Returns true if the E key was pressed.
	 *
	 * @return true if the E key was pressed.
	 */
	public boolean didZoomIn() {return ePressed;}

	/**
	 * Returns true if the Q key was pressed.
	 *
	 * @return true if the Q key was pressed.
	 */
	public boolean didZoomOut() {return qPressed;}
	
	/**
	 * Creates a new input controller
	 * 
	 * The input controller attempts to connect to the X-Box controller at device 0,
	 * if it exists.  Otherwise, it falls back to the keyboard control.
	 */
	public InputController() {

		crosshair = new Vector2();
		crosscache = new Vector2();
	}

	/**
	 * Reads the input for the player and converts the result into game logic.
	 *
	 * The method provides both the input bounds and the drawing scale.  It needs
	 * the drawing scale to convert screen coordinates to world coordinates.  The
	 * bounds are for the crosshair.  They cannot go outside of this zone.
	 *
	 * @param bounds The input bounds for the crosshair.
	 * @param scale  The drawing scale
	 */
	public void readInput(Rectangle bounds, Vector2 scale) {
		// Check to see if a GamePad is connected
		//readKeyboard(bounds, scale, false);
	}

	/**
	 * Reads the input for the player and converts the result into game logic.
	 *
	 * The method provides both the input bounds and the drawing scale.  It needs
	 * the drawing scale to convert screen coordinates to world coordinates.  The
	 * bounds are for the crosshair.  They cannot go outside of this zone.
	 */
	public void readInput() {
		// Check to see if a GamePad is connected
		readKeyboard();
	}

	/**
	 * Reads input from the keyboard.
	 *
	 * This controller reads from the keyboard regardless of whether or not an X-Box
	 * controller is connected.  However, if a controller is connected, this method
	 * gives priority to the X-Box controller.
	 */
	private void readKeyboard() {
		// Camera movement (WASD) controls
		wPressed = Gdx.input.isKeyPressed(Input.Keys.W);
		aPressed = Gdx.input.isKeyPressed(Input.Keys.A);
		sPressed = Gdx.input.isKeyPressed(Input.Keys.S);
		dPressed = Gdx.input.isKeyPressed(Input.Keys.D);

		// Camera zoom (EQ) controls
		ePressed = Gdx.input.isKeyPressed(Input.Keys.E);
		qPressed = Gdx.input.isKeyPressed(Input.Keys.Q);
	}
	
	/**
	 * Clamp the cursor position so that it does not go outside the window
	 *
	 * While this is not usually a problem with mouse control, this is critical 
	 * for the gamepad controls.
	 */
	private void clampPosition(Rectangle bounds) {
		crosshair.x = Math.max(bounds.x, Math.min(bounds.x+bounds.width, crosshair.x));
		crosshair.y = Math.max(bounds.y, Math.min(bounds.y+bounds.height, crosshair.y));
	}
}