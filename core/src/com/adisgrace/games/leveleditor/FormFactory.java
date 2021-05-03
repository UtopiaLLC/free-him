package com.adisgrace.games.leveleditor;

import com.adisgrace.games.InputController;
import com.adisgrace.games.models.TraitModel;
import static com.adisgrace.games.leveleditor.LevelEditorConstants.*;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.utils.Array;

import static com.adisgrace.games.leveleditor.LevelEditorConstants.FORM_GAP;
import static com.adisgrace.games.leveleditor.LevelEditorConstants.FORM_X_OFFSET;

/**
 * Factory class that constructs form entries for use in the level editor.
 */
public final class FormFactory {
    /** Input controller to ignore input with */
    private static InputController input;

    /**
     * Sets the input controller to ignore input from to the given input controller.
     *
     * @param input InputController to potentially ignore input from
     */
    public static void setInputController(InputController input) {
        FormFactory.input = input;
    }

    /**
     * Helper function that returns a new FocusListener that disables keyboard input when a text field
     * is being used.
     *
     * @return new FocusListener that disables keyboard input when a text field is being used.
     */
    private static FocusListener newIgnoreInputFocusListener() {
        return new FocusListener() {
            public void keyboardFocusChanged(FocusListener.FocusEvent event, Actor actor, boolean focused) {
                // Ignores keyboard input for camera control when typing in a text box
                input.shouldIgnoreInput(focused);
            }
        };
    }

    /**
     * Creates and returns a TextField/TextArea with the given parameters.
     *
     * @param name              Name of the field
     * @param height            Height at which the field is placed on the screen
     * @param width             Width of the field
     * @param initialText       What to initially fill the field with
     * @param isArea            Whether the field should actually be a TextArea
     * @param doDisableInput    Whether or not to disable keyboard input when this text field/area is selected
     * @return                  The constructed TextField or TextArea
     */
    private static TextField newTextFieldOrArea(String name, float height, float width, String initialText,
                                                boolean isArea, boolean doDisableInput) {
        // Create text field, or text area if that's what's asked for
        TextField field = isArea ? new TextArea("", skin) : new TextField("", skin);

        // Set name of field
        field.setMessageText(name);
        // Set position and dimensions of field
        field.setPosition(FORM_X_OFFSET, height);
        field.setWidth(width);
        // Initialize contents of field if there are contents to initialize with
        if (!initialText.equals("null")) field.setText(initialText);
        // Add listener to disable keyboard input when the field is selected, if so desired
        if (doDisableInput) field.addListener(newIgnoreInputFocusListener());

        return field;
    }

    /**
     * Creates and returns a TextField with the given parameters.
     *
     * @param name          Name of the field
     * @param height        Height at which the field is placed on the screen
     * @param width         Width of the field
     * @param initialText   What to initially fill the field with
     * @return              The constructed TextField
     */
    public static TextField newTextField(String name, float height, float width, String initialText) {
        return newTextFieldOrArea(name, height, width, initialText, false, true);
    }

    /**
     * Creates and returns a TextField with the given parameters.
     *
     * @param name              Name of the field
     * @param height            Height at which the field is placed on the screen
     * @param width             Width of the field
     * @param initialText       What to initially fill the field with
     * @param doDisableInput    Whether or not to disable keyboard input when this text field/area is selected
     * @return                  The constructed TextField
     */
    public static TextField newTextField(String name, float height, float width, String initialText,
                                         boolean doDisableInput) {
        return newTextFieldOrArea(name, height, width, initialText, false, doDisableInput);
    }

    /**
     * Creates and returns a TextArea with the given parameters.
     *
     * @param name          Name of the field
     * @param height        Height at which the field is placed on the screen
     * @param width         Width of the field
     * @param initialText   What to initially fill the field with
     * @param boxHeight     Height of the box itself
     * @return              The constructed TextArea
     */
    public static TextArea newTextArea(String name, float height, float width, String initialText, int boxHeight) {
        TextArea area = (TextArea) newTextFieldOrArea(name, height, width, initialText, true, true);
        // Set text box height
        area.setHeight(boxHeight * FORM_GAP);
        return area;
    }

    /**
     * Creates and returns a SelectBox with the given parameters.
     *
     * @param options   The backing array for the SelectBox, giving the options to select from
     * @param height    Height at which the SelectBox is placed on the screen
     * @param width     Width of the SelectBox
     * @param selected  Which of the given options is already selected, if any
     * @return          The constructed SelectBox
     */
    public static SelectBox newSelectBox(Object[] options, float height, float width, Object selected) {
        SelectBox box = new SelectBox(skin);
        box.setItems(options);
        box.setPosition(FORM_X_OFFSET, height);
        box.setWidth(width);
        // Only set as selected if something has been selected
        if (selected != null) {
            box.setSelected(selected);
        }
        // Add listener to disable keyboard input when the field is selected
        box.addListener(newIgnoreInputFocusListener());

        return box;
    }

    /**
     * Creates and returns a List with the given parameters.
     * <p>
     * This function in particular is only used to create the list to pick target traits from.
     *
     * @param options   The backing array for the List, giving the options to select from
     * @param height    Height at which the List is placed on the screen
     * @param width     Width of the List
     * @param selected  Which of the given options is already selected, if any
     * @return          The constructed List
     */
    public static List newListBox(Object[] options, float height, float width, Array<TraitModel.Trait> selected) {
        List box = new List(skin);
        box.setItems(options);
        box.setPosition(FORM_X_OFFSET, height);
        box.setHeight(7.5f * FORM_GAP);
        box.setWidth(width);
        // Add listener to disable keyboard input when the field is selected
        box.addListener(newIgnoreInputFocusListener());

        // Clear the default selection
        box.getSelection().clear();

        // Select the previously-selected options
        box.getSelection().addAll(selected);

        // Ensure multiple options can be selected
        box.getSelection().setMultiple(true);
        // Ensure no options can be selected
        box.getSelection().setRequired(false);
        // Doesn't clear the selection when selecting a new option
        box.getSelection().setToggle(true);

        return box;
    }

    /**
     * Creates and returns a new Label with the given parameters.
     *
     * @param labelName The text to write in the label
     * @param height    The vertical height at which to place the label
     * @return          The constructed Label
     */
    public static Label newLabel(String labelName, float height) {
        Label label = new Label(labelName, skin);
        label.setPosition(FORM_X_OFFSET, height);
        return label;
    }

    /**
     * Creates and returns a new CheckBox with the given parameters.
     *
     * @param name      The text to label the check box with
     * @param height    The vertical height at which the place the check box
     * @return          The constructed CheckBox
     */
    public static CheckBox newCheckBox(String name, float height) {
        CheckBox checkBox = new CheckBox(name, skin);
        checkBox.setPosition(FORM_X_OFFSET + 215, height-2);
        return checkBox;
    }
}