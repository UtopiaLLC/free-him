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
     * @param upAssetPath (Relative or absolute) path to asset for unclicked button
     * @param downAssetPath (Relative or absolute) path to asset for button when clicking
     * @param checkedAssetPath (Relative or absolute) path to asset for button when selected
     * @return
     */
    public static ImageButton makeImageButton(String upAssetPath, String downAssetPath, String checkedAssetPath){
        ImageButton button = new ImageButton(
                new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal(upAssetPath)))),
                new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal(downAssetPath)))),
                new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal(checkedAssetPath))))
        );
        button.setTransform(true);
        button.setScale(1);
        return button;
    }

    /**
     * Creates and returns an ImageButton with the linked assets and adds a
     * ClickListener using the passed functions.
     * @param upAssetPath (Relative or absolute) path to asset for unclicked button
     * @param downAssetPath (Relative or absolute) path to asset for button when clicking
     * @param checkedAssetPath (Relative or absolute) path to asset for button when selected
     * @param onClick Method to run when clicked
     * @return
     */
    public static ImageButton makeImageButton(String upAssetPath, String downAssetPath, String checkedAssetPath,
                                              Runnable onClick){
        ImageButton button = makeImageButton(upAssetPath, downAssetPath, checkedAssetPath);
        button.addListener(InputController.getInstance().getButtonListener(onClick));
        return button;
    }

    /**
     * Creates and returns an ImageButton with the linked assets and adds a
     * ClickListener using the passed functions.
     * @param upAssetPath (Relative or absolute) path to asset for unclicked button
     * @param downAssetPath (Relative or absolute) path to asset for button when clicking
     * @param checkedAssetPath (Relative or absolute) path to asset for button when selected
     * @param onClick Method to run when clicked
     * @param onEnter Method to run when mouse enters button
     * @param onExit Method to run when mouse leaves button
     * @return
     */
    public static ImageButton makeImageButton(String upAssetPath, String downAssetPath, String checkedAssetPath,
                                              Runnable onClick, Runnable onEnter, Runnable onExit){
        ImageButton button = makeImageButton(upAssetPath, downAssetPath, checkedAssetPath);
        button.addListener(InputController.getInstance().getButtonListener(onClick, onEnter, onExit));
        return button;
    }
}
