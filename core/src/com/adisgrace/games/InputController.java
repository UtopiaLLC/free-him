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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/**
 * Class for reading player input. 
 *
 * This supports both a keyboard and X-Box controller. In previous solutions, we only 
 * detected the X-Box controller on start-up.  This class allows us to hot-swap in
 * a controller via the new XBox360Controller class.
 */
public class InputController {
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

	/** Whether or not to ignore input */
	private boolean ignoreInput = false;

	// Fields to manage specific button presses
	/** Whether each of the WASD buttons (for camera movement) have been pressed */
	private boolean wPressed;
	private boolean aPressed;
	private boolean sPressed;
	private boolean dPressed;
	/** Whether each of the EQ buttons (for camera zoom) have been pressed */
	private boolean ePressed;
	private boolean qPressed;

	private boolean leftPressed;
	private boolean rightPressed;

	/** Whether right-click was just pressed */
	private boolean rightClicked;
	/** Whether left-click was just pressed */
	private boolean leftClicked;

	/** Whether C, the clear button, has been pressed */
	private boolean cPressed;

	/** Whether Z, the undo button, has been pressed */
	private boolean zPressed;

	/** Mouse coordinates */
	private float mouseX;
	private float mouseY;

	/**
	 * Sets whether input should be ignored. Used to disable other
	 * inputs when a text field is being written to in the LevelEditor.
	 *
	 * @param ignoreInput	Whether input should be ignored
	 */
	public void shouldIgnoreInput (boolean ignoreInput) {
		this.ignoreInput = ignoreInput;
	}

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
	 * Returns true if the left key was pressed.
	 *
	 * @return true if the left key was pressed.
	 */
	public boolean didLeftArrow() {return leftPressed;}

	/**
	 * Returns true if the right key was pressed.
	 *
	 * @return true if the right key was pressed.
	 */
	public boolean didRightArrow() {return rightPressed;}


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
	 * Returns true if right click is pressed and was not previously pressed.
	 *
	 * @return true if right click is pressed and was not previously pressed.
	 */
	public boolean didRightClick() {return rightClicked;}

	/**
	 * Returns true if left click is pressed and was not previously pressed.
	 *
	 * @return true if left click is pressed and was not previously pressed.
	 */
	public boolean didLeftClick() {return leftClicked;}

	/**
	 * Returns true if the C key was pressed.
	 *
	 * @return true if the C key was pressed.
	 */
	public boolean didClear() {return cPressed;}

	/**
	 * Returns true if the Z key was pressed.
	 *
	 * @return true if the Z key was pressed.
	 */
	public boolean didUndo() {return zPressed;}

	/**
	 * Returns the current mouse x-coordinate.
	 *
	 * @return current mouse x-coordinate.
	 */
	public float getX() {return mouseX;}

	/**
	 * Returns the current mouse y-coordinate.
	 *
	 * @return current mouse y-coordinate.
	 */
	public float getY() {return mouseY;}

	/**
	 * Creates a new input controller
	 *
	 * The input controller attempts to connect to the X-Box controller at device 0,
	 * if it exists.  Otherwise, it falls back to the keyboard control.
	 */
	public InputController() {
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
	 */
	public void readInput() {
		// Get mouse coordinates
		mouseX = Gdx.input.getX();
		mouseY = Gdx.input.getY();

		// See if mouse buttons are being clicked
		rightClicked = Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT);
		leftClicked = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);

		// Read from keyboard
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
		wPressed = Gdx.input.isKeyPressed(Input.Keys.W) && !ignoreInput;
		aPressed = Gdx.input.isKeyPressed(Input.Keys.A) && !ignoreInput;
		sPressed = Gdx.input.isKeyPressed(Input.Keys.S) && !ignoreInput;
		dPressed = Gdx.input.isKeyPressed(Input.Keys.D) && !ignoreInput;

		// Camera zoom (EQ) controls
		ePressed = Gdx.input.isKeyPressed(Input.Keys.E) && !ignoreInput;
		qPressed = Gdx.input.isKeyPressed(Input.Keys.Q) && !ignoreInput;

		// Clear button (C)
		cPressed = Gdx.input.isKeyJustPressed(Input.Keys.C) && !ignoreInput;

		// Undo button (Z)
		zPressed = Gdx.input.isKeyJustPressed(Input.Keys.Z) && !ignoreInput;

		leftPressed = Gdx.input.isKeyJustPressed(Input.Keys.LEFT);
		rightPressed = Gdx.input.isKeyJustPressed(Input.Keys.RIGHT);

	}

	/**
	 * This method inputs parameters to create a click listener for nodes.
	 *
	 * This method only adds the enter and exit listeners to the nodes for **GameController-specific** uses.
	 *
	 * @param skin the skin of the labels
	 * @param levelController the levelController used in the gameplay controller
	 * @return the ClickListener for nodes
	 */
	public ClickListener addNodeListenerEnterExit(final Skin skin, final LevelController levelController) {
		return new ClickListener() {
			Label hoverLabel = new Label("N/A", skin);

			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {

				Actor cbutton = (Actor) event.getListenerActor();
				String name = cbutton.getName();
				String[] nodeInfo = name.split(",");

				if (nodeInfo.length == 1) {
					String hoverText = "Target Name: " + name + "\n" +
							"Target Stress: " + levelController.getTargetStress(name) + "\n" +
							"Target Suspicion: " + levelController.getTargetSuspicion(name) + "\n";
					hoverLabel.setText(hoverText);
					hoverLabel.setFontScale(2);
					Vector2 zeroLoc = new Vector2(Gdx.graphics.getWidth() * .05f, Gdx.graphics.getHeight() * .85f);
					hoverLabel.setX(zeroLoc.x);
					hoverLabel.setY(zeroLoc.y);

					GameController.toolbarStage.addActor(hoverLabel);

				}

			}

			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				super.exit(event, x, y, pointer, toActor);
				hoverLabel.remove();
			}
		};
	}

	/**
	 * Returns a ClickListener that can take in any function to be run. Returns a listener with clicked, enter, exit
	 * implemented.
	 *
	 * @param onClick Method to call on click
	 * @param onEnter Method to call on enter
	 * @param onExit Method to call on exit
	 * @return ClickListener using associated functions
	 */
	public ClickListener getButtonListener(final Runnable onClick, final Runnable onEnter, final Runnable onExit){
		return new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y){
				onClick.run();
			}

			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor){
				onEnter.run();
			}

			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor){
				onExit.run();
			}
		};
	}

	/**
	 * Returns a ClickListener that can take in any function to be run. Used for when user clicks a button
	 * @param onClick Method to call on click
	 * @return ClickListener using associated functions
	 */
	public ClickListener getButtonListener(final Runnable onClick){
		return new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y){
				onClick.run();
			}
		};
	}

	/**
	 * Returns a ClickListener that can take in any function to be run. Used for when user hovers over a button
	 * @param onEnter Method to call on enter
	 * @return ClickListener using associated functions
	 */
	public ClickListener getButtonListenerEnter(final Runnable onEnter){
		return new ClickListener(){
			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor){
				onEnter.run();
			}
		};
	}

	/**
	 * Returns a ClickListener that can take in any function to be run. Used for when user stops hovering over a button
	 * @param onExit Method to call on exit
	 * @return ClickListener using associated functions
	 */
	public ClickListener getButtonListenerExit(final Runnable onExit){
		return new ClickListener(){
			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor fromActor){
				onExit.run();
			}
		};
	}
}