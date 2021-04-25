package com.adisgrace.games;

import com.adisgrace.games.models.TargetModel;
import com.adisgrace.games.util.ButtonFactory;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;
import java.util.Map;

public class UIController {
    /** The skin used for displaying neon UI elements */
    private Skin skin;
    /** The ImageButton for threaten, to be initialized with given texture */
    private ImageButton threaten;
    /** Whether the threaten button has been checked */
    private boolean threaten_checked = false;
    /** The ImageButton for expose, to be initialized with given texture */
    private ImageButton expose;
    /** Whether the expose button has been checked */
    private boolean expose_checked = false;
    /** The ImageButton for overwork, to be initialized with given texture */
    private ImageButton overwork;
    /** Whether the overwork button has been checked */
    private boolean overwork_checked = false;
    /** The ImageButton for otherJobs, to be initialized with given texture */
    private ImageButton otherJobs;
    /** Whether the otherJobs button has been checked */
    private boolean otherJobs_checked = false;
    /** The ImageButton for relax, to be initialized with given texture */
    private ImageButton relax;
    /** Whether the relax button has been checked */
    private boolean relax_checked = false;

    public UIController(Skin skin) {
        this.skin = skin;
    }

    /**
     * This method creates a skill bar using threaten, expose, overwork, otherJobs, relac
     * @param toolbar table that will encapsulate all other tables
     * @return the skillBar table
     */
    public Table createSkillBarTable(Table toolbar) {
        Table skillBar = new Table();
        skillBar.setSize(toolbar.getWidth()*.60f, toolbar.getHeight()*.3f);
        skillBar.setBackground(new TextureRegionDrawable(new TextureRegion(
                new Texture(Gdx.files.internal("UI/SkillBar_2.png")))));
        // numSkills is equal to the number of skill buttons + 1
        int numSkills = 5+1;
        float pad = skillBar.getWidth() / 60f;
        skillBar.add(threaten).width(skillBar.getWidth()/numSkills).height(skillBar.getHeight()).padRight(pad).align(Align.bottom);
        skillBar.add(expose).width(skillBar.getWidth()/numSkills).height(skillBar.getHeight()).padRight(pad).align(Align.bottom);
        skillBar.add(overwork).width(skillBar.getWidth()/numSkills).height(skillBar.getHeight()).padRight(pad).align(Align.bottom);
        skillBar.add(otherJobs).width(skillBar.getWidth()/numSkills).height(skillBar.getHeight()).padRight(pad).align(Align.bottom);
        skillBar.add(relax).width(skillBar.getWidth()/numSkills).height(skillBar.getHeight()).padRight(pad).align(Align.bottom);
        return skillBar;
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

    /**
     * Creates a dialog box for the notebook with [s] at a reasonably-sized height and width
     * @param s the string displayed
     * @param targetName name of the target
     * @param levelController controller instance for the level
     */
    public void createNotebookDialog(String s,  String targetName, LevelController levelController) {
        Dialog dialog = new Dialog("Notebook", skin) {
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
        dialog.setMovable(true);

        //Get all fact summaries that can potentially be displayed
        Map<String, String> factSummaries = levelController.getNotes(targetName);
        //This will store the fact ids of all the scanned facts
        Array<String> scannedFacts = new Array<>();

        Table table = dialog.getContentTable();
        if (factSummaries.keySet().size() == 0) {
            scannedFacts.add("No facts scanned yet!");
        }
        for (String fact_ : factSummaries.keySet()) {
            if (factSummaries.containsKey(fact_))
                scannedFacts.add(factSummaries.get(fact_));
        }
        table.setFillParent(false);

        table.row();
        for (int i = 0; i < scannedFacts.size; i++) {
            Label k = new Label(scannedFacts.get(i), skin);
            k.setFontScale(1.3f);
            k.setWrap(true);
            table.add(k).prefWidth(350);
            table.row();
        }

        dialog.button("Ok", true); //sends "true" as the result
        dialog.key(Input.Keys.ENTER, true); //sends "true" when the ENTER key is pressed
        dialog.show(GameController.toolbarStage);
        GameController.nodeFreeze = true;
    }

    /**
     * This method adds a dialog to the stage that allows a user to select a specific target's notebook
     * @param s
     * @param targets
     * @param levelController
     */
    public void createNotebookTargetSelector(String s, final Array<TargetModel> targets,
                                             final LevelController levelController) {
        Dialog dialog = new Dialog("Notebook", skin) {
            public void result(Object obj) {
                GameController.nodeFreeze = false;

                if(obj.getClass() == Boolean.class) {
                    return;
                }

                createNotebookDialog("Notebook:", targets.get((int)obj).getName(), levelController);
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
        dialog.setMovable(true);

        for(int i = 0; i < targets.size; i++) {
            dialog.button(targets.get(i).getName(), i);
        }
        dialog.button("Cancel", true); //sends "true" as the result
        dialog.show(GameController.toolbarStage);
    }

    /**
     * This method creates a threaten button with given textures for it's original status, when the cursor is hovering
     * above it and when it is clicked.
     *
     * @return      ImageButton for threaten.
     */
    public ImageButton createThreaten(InputController ic, final Runnable confirmFunction){
//        threaten = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(
//                Gdx.files.internal("skills/threaten_up.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
//                Gdx.files.internal("skills/threaten_down.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
//                Gdx.files.internal("skills/threaten_select.png")))));
//        threaten.setTransform(true);
//        threaten.setScale(1f);
        threaten = ButtonFactory.makeImageButton(
                "skills/threaten_up.png",
                "skills/threaten_down.png",
                "skills/threaten_select.png");
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
//        expose = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(
//                Gdx.files.internal("skills/expose_up.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
//                Gdx.files.internal("skills/expose_down.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
//                Gdx.files.internal("skills/expose_select.png")))));
//        expose.setTransform(true);
//        expose.setScale(1f);
        expose = ButtonFactory.makeImageButton(
                "skills/expose_up.png",
                "skills/expose_down.png",
                "skills/expose_select.png");
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
//        overwork = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(
//                Gdx.files.internal("skills/overwork_up.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
//                Gdx.files.internal("skills/overwork_down.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
//                Gdx.files.internal("skills/overwork_select.png")))));
//        overwork.setTransform(true);
//        overwork.setScale(1f);
        overwork = ButtonFactory.makeImageButton(
                "skills/overwork_up.png",
                "skills/overwork_down.png",
                "skills/overwork_select.png");
        final Label overworkLabel = new Label("Overwork: Gains 2 AP, but Increases Stress", skin);
        final String s = "overwork";
        overwork.addListener(ic.getButtonListener(
                new Runnable() {
                    @Override
                    public void run() {
                        toolbarOnClick(overwork, overwork_checked,"overwork",
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
//        otherJobs = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(
//                Gdx.files.internal("skills/otherjobs_up.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
//                Gdx.files.internal("skills/otherjobs_down.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
//                Gdx.files.internal("skills/otherjobs_select.png")))));
//        otherJobs.setTransform(true);
//        otherJobs.setScale(1f);
        otherJobs = ButtonFactory.makeImageButton(
                "skills/otherjobs_up.png",
                "skills/otherjobs_down.png",
                "skills/otherjobs_select.png");
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
//        relax = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(
//                Gdx.files.internal("skills/relax_up.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
//                Gdx.files.internal("skills/relax_down.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
//                Gdx.files.internal("skills/relax_select.png")))));
//        relax.setTransform(true);
//        relax.setScale(1f);
        relax = ButtonFactory.makeImageButton(
                "skills/relax_up.png",
                "skills/relax_down.png",
                "skills/relax_select.png");
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
     * This method allows you to select a fact to threaten or expose someone.
     *
     * Very similar to a notebook, except every fact has a listener that allows you to click and choose a fact
     *
     * If a fact has been used to threaten, it will not appear in the display for threaten
     *
     * If a fact has been used to expose, it will not appear in the display for threaten and expose
     *
     *
     * @param s the text that is displayed above the facts to select
     */
    public void getBlackmailFact(String s, String targetName, Array<String> exposedFacts, Array<String> threatenedFacts,
                                 LevelController levelController) {
        GameController.blackmailDialog = new Dialog("Notebook", skin) {
            public void result(Object obj) {
                //to activate the node clicking once more
                GameController.nodeFreeze = false;
                GameController.activeVerb = GameController.ActiveVerb.NONE;
            }
        };
        TextureRegion tRegion = new TextureRegion(new Texture(Gdx.files.internal("skins/background.png")));
        TextureRegionDrawable drawable = new TextureRegionDrawable(tRegion);

        GameController.blackmailDialog.setBackground(drawable);
        GameController.blackmailDialog.getBackground().setMinWidth(500);
        GameController.blackmailDialog.getBackground().setMinHeight(500);
        Label l = new Label( s, skin );
        //scale sizing based on the amount of text
        if(s.length() > 50) {
            l.setFontScale(1.5f);
        }else {
            l.setFontScale(2f);
        }
        l.setWrap( true );
        GameController.blackmailDialog.setMovable(true);
        //Add the text to the center of the dialog box
        GameController.blackmailDialog.getContentTable().add( l ).prefWidth( 350 );
        //Get all fact summaries that can potentially be displayed
        Map<String, String> factSummaries = levelController.getNotes(targetName);

        //This will store all mappings from summaries to a fact name
        Map<String, String> summaryToFacts = new HashMap<>();
        //This will store the fact ids of all the scanned facts

        final Array<String> scannedFacts = new Array<>();

        Table table = GameController.blackmailDialog.getContentTable();
        if (factSummaries.keySet().size() == 0) {
            scannedFacts.add("No facts scanned yet!");
        }
        for (String fact_ : factSummaries.keySet()) {
            //Should not add empty fact summaries
            if (factSummaries.containsKey(fact_))
                scannedFacts.add(factSummaries.get(fact_));
            //Add to both scannedFacts and summaryToFacts
            summaryToFacts.put(factSummaries.get(fact_), fact_);
        }
        table.setFillParent(false);

        table.row();
        //Now, parse through all scannedFacts to see which are eligible for display
        for (int i = 0; i < scannedFacts.size; i++) {
            final int temp_i = i;
            //this should ALWAYS be overwritten in the code underneath
            Label k = new Label("No facts", skin);
            if(GameController.activeVerb == GameController.ActiveVerb.EXPOSE ){
                //If a scanned fact has already been exposed, we can't expose it again
                if (exposedFacts.contains(scannedFacts.get(temp_i), false) ) {
                    continue;
                } else {
                    //Else we can display it
                    k = new Label(scannedFacts.get(i), skin);
                }
            } else if(GameController.activeVerb == GameController.ActiveVerb.THREATEN){
                //If a scanned fact has already been used to threaten, we can't use it to threaten again
                if (threatenedFacts.contains(scannedFacts.get(temp_i), false) ) {
                    continue;
                } else {
                    //Else we can display it
                    k = new Label(scannedFacts.get(i), skin);
                }
            }
            k.setWrap(true);
            //Add a listener that can be reachable via the name format "target_name,fact_id"
            k.setName(targetName + "," + summaryToFacts.get(scannedFacts.get(i)));
            k.addListener(getBlackmailFactListener(levelController, scannedFacts, temp_i));
            table.add(k).prefWidth(350);
            table.row();
        }

        GameController.blackmailDialog.button("Cancel", true); //sends "true" as the result
        GameController.blackmailDialog.key(Input.Keys.ENTER, true); //sends "true" when the ENTER key is pressed
        GameController.blackmailDialog.show(GameController.toolbarStage);
        //Make sure nothing else is able to be clicked while blackmail dialog is shown
        GameController.nodeFreeze = true;
    }

    private ClickListener getBlackmailFactListener(final LevelController levelController,
                                                   final Array<String> scannedFacts, final int temp_i){
        return new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Actor cbutton = (Actor)event.getListenerActor();
                String[] info = cbutton.getName().split(",");
                switch (GameController.activeVerb) {
                    case HARASS:
                    case THREATEN:
                        //Threaten the target
                        levelController.threaten(info[0], info[1]);
                        GameController.activeVerb = GameController.ActiveVerb.NONE;
                        createDialogBox("You threatened the target!");
                        //Add this fact to the list of facts used to threaten
                        GameController.threatenedFacts.add(scannedFacts.get(temp_i));
                        break;
                    case EXPOSE:
                        //Expose the target
                        levelController.expose(info[0], info[1]);
                        GameController.activeVerb = GameController.ActiveVerb.NONE;
                        createDialogBox("You exposed the target!");
                        //Add this fact to the list of facts used to expose
                        GameController.exposedFacts.add(scannedFacts.get(temp_i));
                        //Add this fact to the list of facts used to threaten
                        GameController.threatenedFacts.add(scannedFacts.get(temp_i));
                        break;
                    default:
                        System.out.println("This shouldn't be happening.");
                }
                GameController.blackmailDialog.hide();
            }
        };
    }

}