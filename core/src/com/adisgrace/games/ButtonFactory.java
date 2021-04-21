package com.adisgrace.games;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class ButtonFactory {
    /**
     *
     * @param up
     * @param down
     * @param checked
     * @param hoverText
     * @param stage
     * @return
     */
    public static ImageButton makeInteractToolbarButton(
            Stage stage,
            TextureRegionDrawable up, TextureRegionDrawable down, TextureRegionDrawable checked,
            String hoverText, Skin skin
    ){
        ImageButton button = new ImageButton(up, down, checked);
        button.setTransform(true);
        button.setScale(1);

        Label hoverLabel = new Label(hoverText, skin);


        return button;
    }
}
