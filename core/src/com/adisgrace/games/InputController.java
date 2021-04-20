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

	// Fields to manage specific button presses
	/** Whether each of the WASD buttons (for camera movement) have been pressed */
	private boolean wPressed;
	private boolean aPressed;
	private boolean sPressed;
	private boolean dPressed;
	/** Whether each of the EQ buttons (for camera zoom) have been pressed */
	private boolean ePressed;
	private boolean qPressed;

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

	public void addNodeListener(Node n, final Skin skin, final Runnable actOnNode,
								final LevelController levelController) {
		n.addListener(new ClickListener()
		{
			Label hoverLabel = new Label("N/A", skin);

			@Override
			public void clicked(InputEvent event, float x, float y)
			{
				Actor cbutton = (Actor)event.getListenerActor();
				//System.out.println(cbutton.getName());
				actOnNode.run();
			}

			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {

				Actor cbutton = (Actor)event.getListenerActor();
				String name = cbutton.getName();
				String [] nodeInfo = name.split(",");

				if(nodeInfo.length==1) {
//                        tState = new Label("Target State: " + target.getState(), skin);
//                        tStress.setText("Target Stress: " + Integer.toString(target.getStress()));
//                      tSusp.setText("Target Suspicion: " + Integer.toString(target.getSuspicion()));


					String hoverText = "Target Name: " + name + "\n" +
							"Target Stress: " + levelController.getTargetStress(name) + "\n" +
							"Target Suspicion: " + levelController.getTargetSuspicion(name) + "\n";


					hoverLabel.setText(hoverText);
					hoverLabel.setFontScale(2);

					//Vector2 zeroLoc = b.localToStageCoordinates(new Vector2(0, b.getHeight()));
					Vector2 zeroLoc = new Vector2(Gdx.graphics.getWidth()*.05f, Gdx.graphics.getHeight()*.85f);
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
		});
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
		wPressed = Gdx.input.isKeyPressed(Input.Keys.W);
		aPressed = Gdx.input.isKeyPressed(Input.Keys.A);
		sPressed = Gdx.input.isKeyPressed(Input.Keys.S);
		dPressed = Gdx.input.isKeyPressed(Input.Keys.D);

		// Camera zoom (EQ) controls
		ePressed = Gdx.input.isKeyPressed(Input.Keys.E);
		qPressed = Gdx.input.isKeyPressed(Input.Keys.Q);

		// Clear button (C)
		cPressed = Gdx.input.isKeyJustPressed(Input.Keys.C);

		// Undo button (Z)
		zPressed = Gdx.input.isKeyJustPressed(Input.Keys.Z);
	}
}