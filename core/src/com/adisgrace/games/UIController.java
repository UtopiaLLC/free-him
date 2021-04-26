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

import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;

public class UIController {
    /** The skin used for displaying neon UI elements */
    private Skin skin;
    /** The ImageButton for harass, to be initialized with given texture */
    private ImageButton harass;
    /** The ImageButton for expose, to be initialized with given texture */
    private ImageButton expose;
    /** The ImageButton for distract, to be initialized with given texture */
    private ImageButton distract;
    /** The ImageButton for gaslight, to be initialized with given texture */
    private ImageButton gaslight;
    /** The ImageButton for overwork, to be initialized with given texture */
    private ImageButton overwork;
    /** The ImageButton for otherJobs, to be initialized with given texture */
    private ImageButton otherJobs;
    /** The ImageButton for relax, to be initialized with given texture */
    private ImageButton relax;

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
        int numSkills = 6+1;
        float pad = skillBar.getWidth() / 60f;
        skillBar.add(harass).width(skillBar.getWidth()/numSkills).height(skillBar.getHeight()).padRight(pad).align(Align.bottom);
        skillBar.add(expose).width(skillBar.getWidth()/numSkills).height(skillBar.getHeight()).padRight(pad).align(Align.bottom);
        skillBar.add(distract).width(skillBar.getWidth()/numSkills).height(skillBar.getHeight()).padRight(pad).align(Align.bottom);
        skillBar.add(gaslight).width(skillBar.getWidth()/numSkills).height(skillBar.getHeight()).padRight(pad).align(Align.bottom);
        skillBar.add(overwork).width(skillBar.getWidth()/numSkills).height(skillBar.getHeight()).padRight(pad).align(Align.bottom);
        skillBar.add(otherJobs).width(skillBar.getWidth()/numSkills).height(skillBar.getHeight()).padRight(pad).align(Align.bottom);
        skillBar.add(relax).width(skillBar.getWidth()/numSkills).height(skillBar.getHeight()).padRight(pad).align(Align.bottom);
        return skillBar;
    }

    /**
     * This helper method sets all buttons in toolbar to their unchecked/original states
     */
    public void unCheck(){
        harass.setChecked(false);
        expose.setChecked(false);
        otherJobs.setChecked(false);
        overwork.setChecked(false);
        distract.setChecked(false);
        gaslight.setChecked(false);
        GameController.activeVerb = GameController.ActiveVerb.NONE;
    }

    /**
     * This method is to be run when a toolbar button gets clicked. This method
     * changes the active verb based on the button that was clicked and changes the UI
     * of the button to reflect the fact that it has been selected.
     * @param button the button that was clicked
     * @param s the name of the skill that was clicked
     * @param av the active verb of the skill that was clicked
     */
    public void toolbarOnClick(ImageButton button, final String s,
                                GameController.ActiveVerb av, Runnable confirmFunction) {
        switch(av) {
            case HARASS:
            case THREATEN:
            case EXPOSE:
            case GASLIGHT:
                if (!button.isChecked()){
                    unCheck();
                    GameController.activeVerb = av;
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
    public ImageButton createHarass(InputController ic, final Runnable confirmFunction){
        harass = ButtonFactory.makeImageButton(
                "skills/harass_up.png",
                "skills/harass_down.png",
                "skills/harass_select.png");
        final Label  harassLabel = createHoverLabel(GameController.getHoverText(GameController.ActiveVerb.HARASS));
        final String s = "harass";
        harass.addListener(ic.getButtonListener(
                new Runnable() {
                    @Override
                    public void run() {
                        toolbarOnClick(harass, s, GameController.ActiveVerb.HARASS, confirmFunction);
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        toolbarOnEnter(harass, harassLabel, GameController.ActiveVerb.HARASS);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        toolbarOnExit(harass, harassLabel, GameController.ActiveVerb.HARASS);
                    }
                }));
        return harass;
    }

    /**
     * This method creates a expose button with given textures for it's original status, when the cursor is hovering
     * above it and when it is clicked.
     *
     * @return      ImageButton for expose.
     */
    public ImageButton createExpose(InputController ic, final Runnable confirmFunction){
        expose = ButtonFactory.makeImageButton(
                "skills/expose_up.png",
                "skills/expose_down.png",
                "skills/expose_select.png");
        final Label exposeLabel = createHoverLabel(GameController.getHoverText(GameController.ActiveVerb.EXPOSE));
        final String s = "expose";
        expose.addListener(ic.getButtonListener(
                new Runnable() {
                    @Override
                    public void run() {
                        toolbarOnClick(expose, s, GameController.ActiveVerb.EXPOSE,  confirmFunction);
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
     * This method creates a distract button with given textures for it's original status, when the cursor is hovering
     * above it and when it is clicked.
     *
     * @return      ImageButton for distract.
     */
    public ImageButton createDistract(InputController ic, final Runnable confirmFunction){
        distract = ButtonFactory.makeImageButton( //TODO
                "skills/expose_up.png",
                "skills/expose_down.png",
                "skills/expose_select.png");
        final Label distractLabel = createHoverLabel(GameController.getHoverText(GameController.ActiveVerb.DISTRACT));
        final String s = "distract";
        distract.addListener(ic.getButtonListener(
                new Runnable() {
                    @Override
                    public void run() {
                        toolbarOnClick(distract, s, GameController.ActiveVerb.DISTRACT,  confirmFunction);
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        toolbarOnEnter(distract, distractLabel, GameController.ActiveVerb.DISTRACT);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        toolbarOnExit(expose, distractLabel, GameController.ActiveVerb.DISTRACT);
                    }
                }));
        return distract;
    }

    /**
     * This method creates a gaslight button with given textures for its original status, when the cursor is hovering
     * above it and when it is clicked.
     *
     * @return      ImageButton for expose.
     */
    public ImageButton createGaslight(InputController ic, final Runnable confirmFunction){
        gaslight = ButtonFactory.makeImageButton( //TODO
                "skills/expose_down.png",
                "skills/expose_select.png",
                "skills/expose_up.png");
        final Label gaslightLabel = createHoverLabel(GameController.getHoverText(GameController.ActiveVerb.GASLIGHT));
        final String s = "gaslight";
        gaslight.addListener(ic.getButtonListener(
                new Runnable() {
                    @Override
                    public void run() {
                        toolbarOnClick(gaslight, s, GameController.ActiveVerb.GASLIGHT,  confirmFunction);
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        toolbarOnEnter(gaslight, gaslightLabel, GameController.ActiveVerb.GASLIGHT);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        toolbarOnExit(gaslight, gaslightLabel, GameController.ActiveVerb.GASLIGHT);
                    }
                }));
        return gaslight;
    }

    /**
     * This method creates a overwork button with given textures for it's original status, when the cursor is hovering
     * above it and when it is clicked.
     *
     * @return      ImageButton for overwork.
     */
    public ImageButton createOverwork(InputController ic, final Runnable confirmFunction){
        overwork = ButtonFactory.makeImageButton(
                "skills/overwork_up.png",
                "skills/overwork_down.png",
                "skills/overwork_select.png");
        final Label overworkLabel = createHoverLabel(GameController.getHoverText(GameController.ActiveVerb.OVERWORK));
        final String s = "overwork";
        overwork.addListener(ic.getButtonListener(
                new Runnable() {
                    @Override
                    public void run() {
                        toolbarOnClick(overwork,s,
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
        otherJobs = ButtonFactory.makeImageButton(
                "skills/otherjobs_up.png",
                "skills/otherjobs_down.png",
                "skills/otherjobs_select.png");
        final Label otherJobLabel = createHoverLabel(GameController.getHoverText(GameController.ActiveVerb.OTHER_JOBS));
        final String s = "other jobs";
        otherJobs.addListener(ic.getButtonListener(
                new Runnable() {
                    @Override
                    public void run() {
                        toolbarOnClick(otherJobs, s, GameController.ActiveVerb.OTHER_JOBS, confirmFunction);
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

    public Label createHoverLabel(String s) {
//        Dialog dialog = new Dialog("", skin );
//        TextureRegion tRegion = new TextureRegion(new Texture(Gdx.files.internal("skins/win-95.png")));
//        TextureRegionDrawable drawable = new TextureRegionDrawable(tRegion);
//
//        dialog.setHeight(400);
//        dialog.setBackground(drawable);

        Label l = new Label(s, skin, "win-95");
        l.setWrap(true);
        l.setHeight(100);

        return l;
    }

    /**
     * This method creates a relax button with given textures for it's original status, when the cursor is hovering
     * above it and when it is clicked.
     *
     * @return      ImageButton for relax.
     */
    public ImageButton createRelax(InputController ic, final Runnable confirmFunction){
        relax = ButtonFactory.makeImageButton(
                "skills/relax_up.png",
                "skills/relax_down.png",
                "skills/relax_select.png");
        final Label  relaxLabel = createHoverLabel(GameController.getHoverText(GameController.ActiveVerb.RELAX));
        final String s = "relax";
        relax.addListener(ic.getButtonListener(
                new Runnable() {
                    @Override
                    public void run() {
                        toolbarOnClick(relax, s, GameController.ActiveVerb.RELAX, confirmFunction);
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
    public void getBlackmailFact(String s, String targetName, LevelController levelController) {
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
//        Label l = new Label( s, skin );
        Label l = new Label(s, skin, "win-95");
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

        addEligibleBlackmailFacts(scannedFacts, summaryToFacts, targetName, table, levelController, factSummaries);

        GameController.blackmailDialog.button("Cancel", true); //sends "true" as the result
        GameController.blackmailDialog.key(Input.Keys.ENTER, true); //sends "true" when the ENTER key is pressed
        GameController.blackmailDialog.show(GameController.toolbarStage);
        //Make sure nothing else is able to be clicked while blackmail dialog is shown
        GameController.nodeFreeze = true;
    }

    /**
     * This method parses through all the scannedFacts of a single target to see which facts are eligible for display
     *
     * Eligibility is determined by these criteria
     * 1) Scanned fact has not been exposed
     * 2) Scanned fact has not been used to threaten
     *
     * @param scannedFacts array of fact summaries that were scanned
     * @param summaryToFacts map from fact summaries to fact id
     * @param targetName name of target
     * @param table instance of Table used in blackmail notebook instance
     * @param levelController level controller instance
     * @param factSummaries map from fact name to fact summaries
     */
    private void addEligibleBlackmailFacts(Array<String> scannedFacts, Map<String, String> summaryToFacts,String targetName,
                                           Table table, LevelController levelController, Map<String, String> factSummaries) {
        //Now, parse through all scannedFacts to see which are eligible for display
        for (int i = 0; i < scannedFacts.size; i++) {
            final int temp_i = i;
            //this should ALWAYS be overwritten in the code underneath
            Label k = new Label("No facts", skin);
            String factIDAndSummaryKey = summaryToFacts.get(scannedFacts.get(temp_i)) + scannedFacts.get(temp_i);
            System.out.println("here: " + factIDAndSummaryKey);
            if(GameController.activeVerb == GameController.ActiveVerb.EXPOSE ){
                //If a scanned fact has already been exposed, we can't expose it again
                if (GameController.exposedFacts.contains(factIDAndSummaryKey, false) ) {
                    continue;
                } else {
                    //Else we can display it
                    k = new Label(scannedFacts.get(i), skin);
                }
            } else if(GameController.activeVerb == GameController.ActiveVerb.HARASS){

                //If a scanned fact has already been used to threaten, we can't use it to threaten again
                if (GameController.threatenedFacts.contains(factIDAndSummaryKey, false) ) {
                    continue;
                } else {
                    //Else we can display it
                    k = new Label(scannedFacts.get(i), skin);
                }
            }
            k.setWrap(true);
            //Add a listener that can be reachable via the name format "target_name,fact_id"
            if(factSummaries.keySet().size() != 0) {
                k.setName(targetName + "," + summaryToFacts.get(scannedFacts.get(i)));
                k.addListener(getBlackmailFactListener(levelController, factIDAndSummaryKey));
            }
            table.add(k).prefWidth(350);
            table.row();
        }
    }

    /**
     * This method returns a click listener for each fact displayed in a blackmail dialog.
     * @param levelController
     * @param factIDAndSummary
     * @return
     */
    private ClickListener getBlackmailFactListener(final LevelController levelController, final String factIDAndSummary){
        return new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Actor cbutton = (Actor)event.getListenerActor();
                String[] info = cbutton.getName().split(",");
                switch (GameController.activeVerb) {
                    case THREATEN:
                        break;
                    case HARASS:
                        //Threaten the target
                        levelController.threaten(info[0], info[1]);
                        GameController.activeVerb = GameController.ActiveVerb.NONE;
                        createDialogBox("You harassed the target!");
                        //Add this fact to the list of facts used to threaten
                        GameController.threatenedFacts.add(factIDAndSummary);

                        break;
                    case EXPOSE:
                        //Expose the target
                        levelController.expose(info[0], info[1]);
                        GameController.activeVerb = GameController.ActiveVerb.NONE;
                        createDialogBox("You exposed the target!");
                        //Add this fact to the list of facts used to expose
                        GameController.exposedFacts.add(factIDAndSummary);
                        //Add this fact to the list of facts used to threaten
                        GameController.threatenedFacts.add(factIDAndSummary);
                        break;
                    default:
                        System.out.println("This shouldn't be happening.");
                }
                GameController.blackmailDialog.hide();
            }
        };
    }

    public void nodeOnEnter(int colorState, Label buttonLabel, Node node) {

//        Vector2 zeroLoc = node.localToStageCoordinates(new Vector2(0, node.getHeight()));
//        buttonLabel.setX(zeroLoc.x);
//        buttonLabel.setY(zeroLoc.y);
        GameController.toolbarStage.addActor(buttonLabel);

        node.changeColor(colorState-1);
    }

    public void nodeOnExit(int colorState, Label buttonLabel, Node node) {
        buttonLabel.remove();
        node.changeColor(colorState);
    }

}
