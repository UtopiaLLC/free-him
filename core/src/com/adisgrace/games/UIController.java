package com.adisgrace.games;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class UIController {
    private Skin skin;

    public UIController(Skin skin) {
        this.skin = skin;
    }

    /**
     * Displays a dialog box where the user can confirm whether or not they want
     * to proceed with a particular action
     * @param s
     * @param confirmFunction
     */
    public void confirmDialog(String s, final Runnable confirmFunction) {
        Dialog dialog = new Dialog("", skin) {
            public void result(Object obj) {
                if((boolean)obj) {
                    confirmFunction.run();
//                    callConfirmFunction(function);
                }
            }
        };
        TextureRegion tRegion = new TextureRegion(new Texture(Gdx.files.internal("skins/background.png")));
        TextureRegionDrawable drawable = new TextureRegionDrawable(tRegion);
        dialog.setBackground(drawable);
        dialog.getBackground().setMinWidth(300);
        dialog.getBackground().setMinHeight(300);
        Label l = new Label( s, skin );
        l.setFontScale(2);
        l.setWrap( true );
        dialog.getContentTable().add( l ).prefWidth( 250 );
        dialog.button("Yes", true); //sends "true" as the result
        dialog.button("No", false);  //sends "false" as the result
        dialog.show(GameController.toolbarStage);

    }


    /**
     * Creates a dialog box with [s] at a reasonably-sized height and width
     * @param s the string displayed
     */
    public void createDialogBox(String s) {
        Dialog dialog = new Dialog("", skin) {
            public void result(Object obj) {
                GameController.nodeFreeze = false;
            }
        };
        TextureRegion tRegion = new TextureRegion(new Texture(Gdx.files.internal("skins/background.png")));
        TextureRegionDrawable drawable = new TextureRegionDrawable(tRegion);
        dialog.setBackground(drawable);
        dialog.getBackground().setMinWidth(500);
        dialog.getBackground().setMinHeight(500);
        Label l = new Label( s, skin );
        if(s.length() > 50) {
            l.setFontScale(1.5f);
        }else {
            l.setFontScale(2f);
        }
        l.setWrap( true );
        dialog.getContentTable().add( l ).prefWidth( 350 );
        dialog.button("Ok", true); //sends "true" as the result
        dialog.key(Input.Keys.ENTER, true); //sends "true" when the ENTER key is pressed
        dialog.show(GameController.toolbarStage);
        GameController.nodeFreeze = true;
    }

}
