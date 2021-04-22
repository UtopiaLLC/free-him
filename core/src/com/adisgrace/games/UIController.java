package com.adisgrace.games;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class UIController {
    private Skin skin;

    /** The ImageButton for threaten, to be initialized with given texture */
    public static ImageButton threaten;
    /** Whether the threaten button has been checked */
    private boolean threaten_checked = false;
    /** The ImageButton for expose, to be initialized with given texture */
    public static ImageButton expose;
    /** Whether the expose button has been checked */
    private boolean expose_checked = false;
    /** The ImageButton for overwork, to be initialized with given texture */
    public static ImageButton overwork;
    /** Whether the overwork button has been checked */
    private boolean overwork_checked = false;
    /** The ImageButton for otherJobs, to be initialized with given texture */
    public static ImageButton otherJobs;
    /** Whether the otherJobs button has been checked */
    private boolean otherJobs_checked = false;
    /** The ImageButton for relax, to be initialized with given texture */
    public static ImageButton relax;
    /** Whether the relax button has been checked */
    private boolean relax_checked = false;

    public UIController(Skin skin) {
        this.skin = skin;
    }

    /**
     * This method creates a threaten button with given textures for it's original status, when the cursor is hovering
     * above it and when it is clicked.
     *
     * @return      ImageButton for threaten.
     */
    public ImageButton createThreaten(InputController ic, final Runnable confirmFunction){
        threaten = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/threaten_up.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/threaten_down.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/threaten_select.png")))));
        threaten.setTransform(true);
        threaten.setScale(1f);
        final Label  threatenLabel = new Label("Threaten: Threaten your target with a \n fact to blackmail to increase their stress " +
                "for 2 AP", skin);
        final String s = "threaten";
        threaten.addListener(ic.getButtonListener(
                new Runnable() {
                    @Override
                    public void run() {
                        toolbarOnClick(threaten, threaten_checked, s, GameController.ActiveVerb.THREATEN, confirmFunction);
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        toolbarOnEnter(threaten, threatenLabel, GameController.ActiveVerb.THREATEN);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        toolbarOnExit(threaten, threatenLabel, GameController.ActiveVerb.THREATEN);
                    }
                }));
        return threaten;
    }

    /**
     * This method creates a expose button with given textures for it's original status, when the cursor is hovering
     * above it and when it is clicked.
     *
     * @return      ImageButton for expose.
     */
    public ImageButton createExpose(InputController ic, final Runnable confirmFunction){
        expose = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/expose_up.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/expose_down.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/expose_select.png")))));
        expose.setTransform(true);
        expose.setScale(1f);
        final Label exposeLabel = new Label("Expose: Expose your target's fact to the public\n for large stress damage" +
                " for 3 AP", skin);
        final String s = "expose";
        expose.addListener(ic.getButtonListener(
                new Runnable() {
                    @Override
                    public void run() {
                        toolbarOnClick(expose, expose_checked,s, GameController.ActiveVerb.EXPOSE,  confirmFunction);
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        toolbarOnEnter(expose, exposeLabel, GameController.ActiveVerb.EXPOSE);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        toolbarOnExit(expose, exposeLabel, GameController.ActiveVerb.EXPOSE);
                    }
                }));
        return expose;
    }

    /**
     * This method creates a overwork button with given textures for it's original status, when the cursor is hovering
     * above it and when it is clicked.
     *
     * @return      ImageButton for overwork.
     */
    public ImageButton createOverwork(InputController ic, final Runnable confirmFunction){
        overwork = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/overwork_up.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/overwork_down.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/overwork_select.png")))));
        overwork.setTransform(true);
        overwork.setScale(1f);
        final Label overworkLabel = new Label("Overwork: Gains 2 AP, but Increases Stress", skin);
        final String s = "overwork";
        overwork.addListener(ic.getButtonListener(
                new Runnable() {
                    @Override
                    public void run() {
                        toolbarOnClick(overwork, expose_checked,"overwork",
                                GameController.ActiveVerb.OVERWORK, confirmFunction);
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        toolbarOnEnter(overwork, overworkLabel, GameController.ActiveVerb.OVERWORK);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        toolbarOnExit(overwork, overworkLabel, GameController.ActiveVerb.OVERWORK);
                    }
                }));
        return overwork;
    }

    /**
     * This method creates a otherjobs button with given textures for it's original status, when the cursor is hovering
     * above it and when it is clicked.
     *
     * @return      ImageButton for otherjobs.
     */
    public ImageButton createOtherJobs(InputController ic, final Runnable confirmFunction){
        otherJobs = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/otherjobs_up.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/otherjobs_down.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/otherjobs_select.png")))));
        otherJobs.setTransform(true);
        otherJobs.setScale(1f);
        final Label otherJobLabel = new Label("Other Jobs: Make Money with 3 AP", skin);
        final String s = "other jobs";
        otherJobs.addListener(ic.getButtonListener(
                new Runnable() {
                    @Override
                    public void run() {
                        toolbarOnClick(otherJobs, otherJobs_checked,s, GameController.ActiveVerb.OTHER_JOBS, confirmFunction);
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        toolbarOnEnter(otherJobs, otherJobLabel, GameController.ActiveVerb.OTHER_JOBS);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        toolbarOnExit(otherJobs, otherJobLabel, GameController.ActiveVerb.OTHER_JOBS);
                    }
                }));
        return otherJobs;
    }

    /**
     * This method creates a relax button with given textures for it's original status, when the cursor is hovering
     * above it and when it is clicked.
     *
     * @return      ImageButton for relax.
     */
    public ImageButton createRelax(InputController ic, final Runnable confirmFunction){
        relax = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/relax_up.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/relax_down.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/relax_select.png")))));
        relax.setTransform(true);
        relax.setScale(1f);
        final Label  relaxLabel = new Label("Relax: Decreases Stress with 1 AP", skin);
        final String s = "relax";
        relax.addListener(ic.getButtonListener(
                new Runnable() {
                    @Override
                    public void run() {
                        toolbarOnClick(relax, relax_checked,"relax", GameController.ActiveVerb.RELAX, confirmFunction);
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        toolbarOnEnter(relax, relaxLabel, GameController.ActiveVerb.RELAX);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        toolbarOnExit(relax, relaxLabel, GameController.ActiveVerb.RELAX);
                    }
                }));
        return relax;
    }

    /**
     * This helper method sets all buttons in toolbar to their unchecked/original states
     */
    public void unCheck(){
        threaten_checked = false;
        expose_checked = false;
        otherJobs_checked = false;
        overwork_checked = false;
        relax_checked = false;
        threaten.setChecked(false);
        expose.setChecked(false);
        otherJobs.setChecked(false);
        overwork.setChecked(false);
        relax.setChecked(false);
        GameController.activeVerb = GameController.ActiveVerb.NONE;
    }

    /**
     * This method is to be run when a toolbar button gets clicked. This method
     * changes the active verb based on the button that was clicked and changes the UI
     * of the button to reflect the fact that it has been selected.
     * @param button the button that was clicked
     * @param buttonChecked the flag for whether or not the button has been selected
     * @param s the name of the skill that was clicked
     * @param av the active verb of the skill that was clicked
     */
    public void toolbarOnClick(ImageButton button, boolean buttonChecked, final String s,
                                GameController.ActiveVerb av, Runnable confirmFunction) {
        switch(av) {
            case THREATEN:
            case EXPOSE:
                if (buttonChecked == false){
                    unCheck();
                    GameController.activeVerb = av;
                    buttonChecked = true;
                    button.setChecked(true);
                }else{
                    unCheck();
                }
                break;
            case OVERWORK:
            case OTHER_JOBS:
            case RELAX:
                unCheck();
                confirmDialog("Are you sure you want to "+s+"?", confirmFunction);
            default:
                break;
        }

    }

    /**
     * This method adds a label whenever the user hovers over the skill bar.
     * @param button the toolbar button that was hovered over
     * @param buttonLabel the label that needs to be displayed
     * @param av the active verb corresponding to the skill bar
     */
    public void toolbarOnEnter(ImageButton button, Label buttonLabel,GameController.ActiveVerb av) {
        if(GameController.activeVerb != av){
            Vector2 zeroLoc = button.localToStageCoordinates(new Vector2(0, button.getHeight()));
            System.out.println(buttonLabel.toString());
            buttonLabel.setX(zeroLoc.x);
            buttonLabel.setY(zeroLoc.y);
            GameController.toolbarStage.addActor(buttonLabel);
            button.setChecked(true);
        }
    }

    /**
     * This method removes a label whenever the user moves away from the skill bar.
     * @param button the toolbar button that was hovered over
     * @param buttonLabel the label that needs to be removed from being displayed
     * @param av the active verb corresponding to the skill bar
     */
    public void toolbarOnExit(ImageButton button, Label buttonLabel, GameController.ActiveVerb av) {
        buttonLabel.remove();
        if (GameController.activeVerb!=av)button.setChecked(false);
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
