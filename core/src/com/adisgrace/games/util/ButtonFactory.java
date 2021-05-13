package com.adisgrace.games.util;

import com.adisgrace.games.InputController;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class ButtonFactory {

    /**
     * Creates and returns an ImageButton with the linked assets.
     * This does NOT attach a listener; that needs to be done elsewhere with button.addListener.
     * @param upAsset asset for unclicked button
     * @param downAsset asset for button when clicking
     * @param checkedAsset asset for button when selected
     * @return
     */
    public static ImageButton makeImageButton(Texture upAsset, Texture downAsset, Texture checkedAsset){
        ImageButton button = new ImageButton(
                new TextureRegionDrawable(new TextureRegion(upAsset)),
                new TextureRegionDrawable(new TextureRegion(downAsset)),
                new TextureRegionDrawable(new TextureRegion(checkedAsset))
        );
        button.setTransform(true);
        button.setScale(1);
        return button;
    }

    /**
     * Creates and returns an ImageButton with the linked assets and adds a
     * ClickListener using the passed functions.
     * @param upAsset asset for unclicked button
     * @param downAsset asset for button when clicking
     * @param checkedAsset asset for button when selected
     * @param onClick Method to run when clicked
     * @return
     */
    public static ImageButton makeImageButton(Texture upAsset, Texture downAsset, Texture checkedAsset,
                                              Runnable onClick){
        ImageButton button = makeImageButton(upAsset, downAsset, checkedAsset);
        button.addListener(InputController.getInstance().getButtonListener(onClick));
        return button;
    }

    /**
     * Creates and returns an ImageButton with the linked assets and adds a
     * ClickListener using the passed functions.
     * @param upAsset asset for unclicked button
     * @param downAsset asset for button when clicking
     * @param checkedAsset asset for button when selected
     * @param onClick Method to run when clicked
     * @param onEnter Method to run when mouse enters button
     * @param onExit Method to run when mouse leaves button
     * @return
     */
    public static ImageButton makeImageButton(Texture upAsset, Texture downAsset, Texture checkedAsset,
                                              Runnable onClick, Runnable onEnter, Runnable onExit){
        ImageButton button = makeImageButton(upAsset, downAsset, checkedAsset);
        button.addListener(InputController.getInstance().getButtonListener(onClick, onEnter, onExit));
        return button;
    }
}
