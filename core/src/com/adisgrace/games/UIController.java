package com.adisgrace.games;

import com.adisgrace.games.models.TargetModel;
import com.adisgrace.games.util.AssetDirectory;
import com.adisgrace.games.util.ButtonFactory;
import com.adisgrace.games.util.GameConstants;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
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

    private AssetDirectory directory;



    public UIController(Skin skin, AssetDirectory directory) {
        this.skin = skin;
        this.directory = directory;
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
                directory.getEntry("UI:SkillBar", Texture.class))));

        // numSkills is equal to the number of skill buttons + 1
        int numSkills = 7+1;
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
        relax.setChecked(false);
        GameController.activeVerb = GameController.ActiveVerb.NONE;
    }

    /**
     * This helper method sets all buttons in toolbar to their correct states based on activeVerg
     */
    public void refreshButtons(){
        harass.setChecked(false);
        expose.setChecked(false);
        otherJobs.setChecked(false);
        overwork.setChecked(false);
        distract.setChecked(false);
        gaslight.setChecked(false);
        relax.setChecked(false);
        switch(GameController.activeVerb){
            case GASLIGHT:
                gaslight.setChecked(true);
                break;
            case DISTRACT:
                distract.setChecked(true);
                break;
            case EXPOSE:
                expose.setChecked(true);
                break;
            case HARASS:
                harass.setChecked(true);
                break;
            default: break;
        }
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
            case DISTRACT:
                if(GameController.activeVerb == av)
                    GameController.activeVerb = GameController.ActiveVerb.NONE;
                else GameController.activeVerb = av;
                refreshButtons();
//                if (!button.isChecked()){
//                    unCheck();
//                    GameController.activeVerb = av;
//                    button.setChecked(true);
//                }else{
//                    unCheck();
//                }
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
//            System.out.println(buttonLabel.toString());
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
                GameConstants.CLICK_ON.play(GameConstants.global_sound);
                if((boolean)obj) {
                    confirmFunction.run();
                }
            }
        };
        TextureRegion tRegion = new TextureRegion(new Texture(Gdx.files.internal("skins/win-95.png")));
        TextureRegionDrawable drawable = new TextureRegionDrawable(tRegion);
        dialog.setBackground(drawable);
        dialog.getBackground().setMinWidth(300);
        dialog.getBackground().setMinHeight(300);
        Label l = new Label(s, skin, "dialog-box");
        l.setFontScale(1);
        l.setWrap( true );
        l.setColor(Color.BLACK);
        dialog.getContentTable().add( l ).prefWidth( 250 );
        float bottomPad = getDialogButtonBottomPadding(300);
        dialog.button("Yes", true).pad(0f,0f,bottomPad,0f); //sends "true" as the result
        dialog.button("No", false).pad(0f,0f,bottomPad,0f);  //sends "false" as the result
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
                GameConstants.CLICK_ON.play(GameConstants.global_sound);
            }
        };
        TextureRegion tRegion = new TextureRegion(new Texture(Gdx.files.internal("skins/win-95.png")));
        TextureRegionDrawable drawable = new TextureRegionDrawable(tRegion);
        dialog.setBackground(drawable);
//        dialog.setLayoutEnabled(false);
        dialog.getBackground().setMinWidth(GameConstants.DIALOG_WIDTH);
        dialog.getBackground().setMinHeight(GameConstants.DIALOG_HEIGHT);
        Label l = new Label( s, skin, "hover-text");
        l.setColor(Color.BLACK);
        if(s.length() > 350) {
            l.setFontScale(1.0f);
        }
        else if(s.length() > 75) {
            l.setFontScale(1.25f);
        }else {
            l.setFontScale(2f);
        }
        l.setWrap( true );
        //Centers the dialog
        dialog.getContentTable().add( l ).prefWidth( GameConstants.DIALOG_PREF_WIDTH );

        float bottomPad = getDialogButtonBottomPadding(GameConstants.DIALOG_HEIGHT);
        dialog.button("Ok", true).pad(0f,0f,bottomPad,0f); //sends "true" as the result
        dialog.key(Input.Keys.ENTER, true); //sends "true" when the ENTER key is pressed
        dialog.show(GameController.toolbarStage);
        GameController.nodeFreeze = true;
    }

    /**
     * Creates a dialog box with [s] at a reasonably-sized height and width
     * @param s the title of the dialog box
     * @param fact the description of the fact
     */
    public void createDialogBoxFact(String s, String fact) {
        Dialog dialog = new Dialog("", skin) {
            public void result(Object obj) {
                GameController.nodeFreeze = false;
                GameConstants.CLICK_ON.play(GameConstants.global_sound);
            }
        };
        TextureRegion tRegion = new TextureRegion(new Texture(Gdx.files.internal("skins/win-95.png")));
        TextureRegionDrawable drawable = new TextureRegionDrawable(tRegion);
        dialog.setBackground(drawable);
        dialog.getBackground().setMinWidth(GameConstants.DIALOG_WIDTH);
        dialog.getBackground().setMinHeight(GameConstants.DIALOG_HEIGHT);

        Label title = new Label( s, skin, "dialog-box");
        title.setColor(Color.BLACK);
        title.setFontScale(1.5f);
        dialog.addActor(title);
        title.setPosition(50,GameConstants.DIALOG_HEIGHT-90);
        title.setWrap(false);

        Label k = new Label(fact, skin, "dialog-box");
        k.setWrap(true);
        k.setWidth(GameConstants.DIALOG_WIDTH-125);
//        k.setPosition(75,75,Align.topLeft);
        k.setPosition(75, GameConstants.DIALOG_HEIGHT-90-(100), Align.topLeft);
        dialog.addActor(k);


        float bottomPad = getDialogButtonBottomPadding(GameConstants.DIALOG_HEIGHT);
        dialog.button("Ok", true).pad(0f,0f,bottomPad,0f); //sends "true" as the result
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
        Dialog dialog = new Dialog("", skin) {
            public void result(Object obj) {
                GameController.nodeFreeze = false;
                GameConstants.CLICK_ON.play(GameConstants.global_sound);
            }
        };
        TextureRegion tRegion = new TextureRegion(new Texture(Gdx.files.internal("skins/win-95.png")));
        TextureRegionDrawable drawable = new TextureRegionDrawable(tRegion);
        dialog.setBackground(drawable);
        dialog.getBackground().setMinWidth(GameConstants.DIALOG_WIDTH);
        dialog.getBackground().setMinHeight(GameConstants.DIALOG_HEIGHT);
//        dialog.setLayoutEnabled(false);

        Label title = new Label( s, skin, "dialog-box");
        title.setColor(Color.BLACK);
        title.setFontScale(1.5f);
        dialog.addActor(title);
        title.setPosition(50,GameConstants.DIALOG_HEIGHT-90);
        dialog.setMovable(true);

        //Get all fact summaries that can potentially be displayed
        Map<String, String> factSummaries = levelController.getNotes(targetName);
        //This will store the fact ids of all the scanned facts
        Array<String> scannedFacts = new Array<>();

        if (factSummaries.keySet().size() == 0) {
            scannedFacts.add("No facts scanned yet!");
        }
        for (String fact_ : factSummaries.keySet()) {
            if (factSummaries.containsKey(fact_) && levelController.getTargetModels().get(targetName).getStressRating(fact_) != GameConstants.StressRating.NONE)
                scannedFacts.add(factSummaries.get(fact_) + " " +
                        GameConstants.stressRatingToIndicator(levelController.getTargetModels().get(targetName).getStressRating(fact_)));
        }


        int lines = 1;
        for (int i = 0; i < scannedFacts.size; i++) {
            if(scannedFacts.get(i).length() > 0) {
                lines += ((scannedFacts.get(i).length() + 64 + 1) / 64);
                Label k = new Label(scannedFacts.get(i), skin, "dialog-box-border");
                k.setWrap(true);
                k.setWidth(GameConstants.DIALOG_WIDTH - 125);
                k.setPosition(75, GameConstants.DIALOG_HEIGHT - 90 - (25 * lines));
                k.setHeight(((scannedFacts.get(i).length() + 64 + 1) / 64) * 25f);
                lines++;
                dialog.addActor(k);
            }
        }
//        ScrollPane scrollPane = new ScrollPane(dialog, skin);
//        dialog.addActor(scrollPane);
        float bottomPad = getDialogButtonBottomPadding(GameConstants.DIALOG_HEIGHT);
        dialog.setLayoutEnabled(true);
        dialog.button("Ok", true).pad(0f,0f,bottomPad,0f); //sends "true" as the result
        dialog.key(Input.Keys.ENTER, true); //sends "true" when the ENTER key is pressed
        dialog.show(GameController.toolbarStage);

        GameController.nodeFreeze = true;
    }

    /**
     * This method adds a dialog to the stage that allows a user to select a setting action
     * @param s
     * @param goToMainMenu
     * @param restartLevel
     */
    public void createWinLevelSelector(String s, final Runnable goToMainMenu,
                                       final Runnable restartLevel,
                                       final Runnable nextLevel) {
        Dialog dialog = new Dialog("", skin) {
            public void result(Object obj) {
                GameController.nodeFreeze = false;

                if(obj.getClass() == Boolean.class) {
                    return;
                }

                if ((int)obj == 1){
                    goToMainMenu.run();
                } else if ((int)obj == 2) {
                    restartLevel.run();
                } else if ((int)obj == 3) {
                    nextLevel.run();
                }
            }
        };

        TextureRegion tRegion = new TextureRegion(new Texture(Gdx.files.internal("skins/win-95.png")));
        TextureRegionDrawable drawable = new TextureRegionDrawable(tRegion);
        dialog.setBackground(drawable);
        dialog.getBackground().setMinWidth(GameConstants.DIALOG_WIDTH);
        dialog.getBackground().setMinHeight(GameConstants.DIALOG_HEIGHT);
        Label l = new Label( s, skin, "dialog-box");
        l.setColor(Color.BLACK);
        if(s.length() > 50) {
            l.setFontScale(1.5f);
        }else {
            l.setFontScale(2f);
        }
        l.setWrap( true );
        dialog.getContentTable().add( l ).prefWidth( GameConstants.DIALOG_PREF_WIDTH );
        dialog.setMovable(true);

        float bottomPad = getDialogButtonBottomPadding(GameConstants.DIALOG_HEIGHT);

        dialog.button("Main Menu", 1).pad(0f,0f,bottomPad,0f);
        dialog.button("Restart Level", 2).pad(0f,0f,bottomPad,0f);
        dialog.button("Next Level", 3).pad(0f,0f,bottomPad,0f); //sends "true" as the result
        dialog.show(GameController.toolbarStage);
    }

    /**
     * This method adds a dialog to the stage that allows a user to select a setting action
     * @param s
     * @param goToMainMenu
     * @param restartLevel
     */
    public void createSettingsSelector(String s, final Runnable goToMainMenu,
                                       final Runnable restartLevel) {
        Dialog dialog = new Dialog("", skin) {
            public void result(Object obj) {
                GameController.nodeFreeze = false;

                if(obj.getClass() == Boolean.class) {
                    return;
                }

                if ((int)obj == 1){
                    goToMainMenu.run();
                } else if ((int)obj == 2) {
                    restartLevel.run();
                }
            }
        };

        TextureRegion tRegion = new TextureRegion(new Texture(Gdx.files.internal("skins/win-95.png")));
        TextureRegionDrawable drawable = new TextureRegionDrawable(tRegion);
        dialog.setBackground(drawable);
        dialog.getBackground().setMinWidth(GameConstants.DIALOG_WIDTH);
        dialog.getBackground().setMinHeight(GameConstants.DIALOG_HEIGHT);
        Label l = new Label( s, skin, "dialog-box");
        l.setColor(Color.BLACK);
        if(s.length() > 50) {
            l.setFontScale(1.5f);
        }else {
            l.setFontScale(2f);
        }
        l.setWrap( true );
        dialog.getContentTable().add( l ).prefWidth( GameConstants.DIALOG_PREF_WIDTH );
        dialog.setMovable(true);

        float bottomPad = getDialogButtonBottomPadding(GameConstants.DIALOG_HEIGHT);

        dialog.button("Main Menu", 1).pad(0f,0f,bottomPad,0f);
        dialog.button("Restart Level", 2).pad(0f,0f,bottomPad,0f);
        dialog.show(GameController.toolbarStage);
    }

    /**
     * This method adds a dialog to the stage that allows a user to select a specific target's notebook
     * @param s
     * @param targets
     * @param levelController
     */
    public void createNotebookTargetSelector(String s, final Array<TargetModel> targets,
                                             final LevelController levelController) {
        Dialog dialog = new Dialog("", skin) {
            public void result(Object obj) {
                GameConstants.CLICK_ON.play(GameConstants.global_sound);
                GameController.nodeFreeze = false;

                if(obj.getClass() == Boolean.class) {
                    return;
                }

                createNotebookDialog("Notebook:", targets.get((int)obj).getName(), levelController);
            }
        };
        
        TextureRegion tRegion = new TextureRegion(new Texture(Gdx.files.internal("skins/win-95.png")));
        TextureRegionDrawable drawable = new TextureRegionDrawable(tRegion);
        dialog.setBackground(drawable);
        dialog.getBackground().setMinWidth(GameConstants.DIALOG_WIDTH);
        dialog.getBackground().setMinHeight(GameConstants.DIALOG_HEIGHT);
        Label l = new Label( s, skin, "dialog-box");
        //l.setColor(Color.BLACK);
        if(s.length() > 50) {
            l.setFontScale(1.5f);
        }else {
            l.setFontScale(2f);
        }
        l.setWrap( true );
        dialog.getContentTable().add( l ).prefWidth( GameConstants.DIALOG_PREF_WIDTH );
        dialog.setMovable(true);

        float bottomPad = getDialogButtonBottomPadding(GameConstants.DIALOG_HEIGHT);
        for(int i = 0; i < targets.size; i++) {
            dialog.button(targets.get(i).getName(), i).pad(0f,0f,bottomPad,0f);
        }
        dialog.button("Cancel", true).pad(0f,0f,bottomPad,0f); //sends "true" as the result
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
                directory.getEntry("Skills:HarassDown", Texture.class),
                directory.getEntry("Skills:HarassUp", Texture.class),
                directory.getEntry("Skills:HarassSelect", Texture.class));
        final Label  harassLabel = createHoverLabel(GameController.getHoverText(GameController.ActiveVerb.HARASS));
        final String s = "harass";
        harass.addListener(ic.getButtonListener(
                new Runnable() {
                    @Override
                    public void run() {
                        GameConstants.SKILL_ACTIVE.play(0.2f * GameConstants.global_sound);
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
                directory.getEntry("Skills:ExposeDown", Texture.class),
                directory.getEntry("Skills:ExposeUp", Texture.class),
                directory.getEntry("Skills:ExposeSelect", Texture.class));
        final Label exposeLabel = createHoverLabel(GameController.getHoverText(GameController.ActiveVerb.EXPOSE));
        final String s = "expose";
        expose.addListener(ic.getButtonListener(
                new Runnable() {
                    @Override
                    public void run() {
                        GameConstants.SKILL_ACTIVE.play(.2f * GameConstants.global_sound);
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
                directory.getEntry("Skills:DistractDown", Texture.class),
                directory.getEntry("Skills:DistractUp", Texture.class),
                directory.getEntry("Skills:DistractSelect", Texture.class));
        final Label distractLabel = createHoverLabel(GameController.getHoverText(GameController.ActiveVerb.DISTRACT));
        final String s = "distract";
        distract.addListener(ic.getButtonListener(
                new Runnable() {
                    @Override
                    public void run() {
                        GameConstants.SKILL_ACTIVE.play(.2f * GameConstants.global_sound);
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
                directory.getEntry("Skills:GaslightDown", Texture.class),
                directory.getEntry("Skills:GaslightUp", Texture.class),
                directory.getEntry("Skills:GaslightSelect", Texture.class));
        final Label gaslightLabel = createHoverLabel(GameController.getHoverText(GameController.ActiveVerb.GASLIGHT));
        final String s = "gaslight";
        gaslight.addListener(ic.getButtonListener(
                new Runnable() {
                    @Override
                    public void run() {
                        GameConstants.SKILL_ACTIVE.play(.2f * GameConstants.global_sound);
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
                directory.getEntry("Skills:OverworkDown", Texture.class),
                directory.getEntry("Skills:OverworkUp", Texture.class),
                directory.getEntry("Skills:OverworkSelect", Texture.class));
        final Label overworkLabel = createHoverLabel(GameController.getHoverText(GameController.ActiveVerb.OVERWORK));
        final String s = "overwork";
        overwork.addListener(ic.getButtonListener(
                new Runnable() {
                    @Override
                    public void run() {
                        toolbarOnClick(overwork,s,GameController.ActiveVerb.OVERWORK, confirmFunction);
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
                directory.getEntry("Skills:OtherJobsDown", Texture.class),
                directory.getEntry("Skills:OtherJobsUp", Texture.class),
                directory.getEntry("Skills:OtherJobsSelect", Texture.class));
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
        Label l = new Label(s, skin, "hover-gray");
        l.setWrap(true);
        l.setHeight(70);
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
                directory.getEntry("Skills:RelaxDown", Texture.class),
                directory.getEntry("Skills:RelaxUp", Texture.class),
                directory.getEntry("Skills:RelaxSelect", Texture.class));
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
        GameController.blackmailDialog = new Dialog("", skin) {
            public void result(Object obj) {
                //to activate the node clicking once more
                GameController.nodeFreeze = false;
                GameController.activeVerb = GameController.ActiveVerb.NONE;
            }
        };
        TextureRegion tRegion = new TextureRegion(new Texture(Gdx.files.internal("skins/win-95.png")));
        TextureRegionDrawable drawable = new TextureRegionDrawable(tRegion);

        GameController.blackmailDialog.setBackground(drawable);
        GameController.blackmailDialog.getBackground().setMinWidth(GameConstants.DIALOG_WIDTH);
        GameController.blackmailDialog.getBackground().setMinHeight(GameConstants.DIALOG_HEIGHT);

        Label title = new Label( s, skin, "dialog-box");
        title.setColor(Color.BLACK);
        title.setFontScale(1.5f);
        GameController.blackmailDialog.addActor(title);
        title.setPosition(50,GameConstants.DIALOG_HEIGHT-90);
        title.setWrap(false);
        GameController.blackmailDialog.setMovable(true);
        //Get all fact summaries that can potentially be displayed
        Map<String, String> factSummaries = levelController.getNotes(targetName);
        //This will store all mappings from summaries to a fact name
        Map<String, String> summaryToFacts = new HashMap<>();
        //This will store the fact ids of all the scanned facts
        final Array<String> scannedFacts = new Array<>();

        if (factSummaries.keySet().size() == 0) {
            scannedFacts.add("No facts scanned yet!");
        }
        for (String fact_ : factSummaries.keySet()) {
            if (factSummaries.containsKey(fact_))
                if (levelController.getTargetModels().get(targetName).getStressRating(fact_) != GameConstants.StressRating.NONE
                    || factSummaries.get(fact_).length() > 0)
                    scannedFacts.add(factSummaries.get(fact_) + " " +
                            GameConstants.stressRatingToIndicator(levelController.getTargetModels().get(targetName).getStressRating(fact_)));
            //Add to both scannedFacts and summaryToFacts
            summaryToFacts.put(factSummaries.get(fact_) + " " +
                    GameConstants.stressRatingToIndicator(levelController.getTargetModels().get(targetName).getStressRating(fact_)), fact_);
        }


        addEligibleBlackmailFacts(scannedFacts, summaryToFacts, targetName, levelController, factSummaries);
        float bottomPad = getDialogButtonBottomPadding(GameConstants.DIALOG_HEIGHT);
        GameController.blackmailDialog.button("Cancel", true).pad(0f,0f,bottomPad,0f);; //sends "true" as the result
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
     * @param levelController level controller instance
     * @param factSummaries map from fact name to fact summaries
     */
    private void addEligibleBlackmailFacts(Array<String> scannedFacts, Map<String, String> summaryToFacts,String targetName,
                                           LevelController levelController, Map<String, String> factSummaries) {
        //Now, parse through all scannedFacts to see which are eligible for display

        int lines = 2;
        for (int i = 0; i < scannedFacts.size; i++) {
            final int temp_i = i;
            //this should ALWAYS be overwritten in the code underneath
            Label k = new Label("No facts", skin, "dialog-box-border");
            lines += ((scannedFacts.get(i).length() + 64 + 1) / 64);

            String factIDAndSummaryKey = summaryToFacts.get(scannedFacts.get(temp_i)) + scannedFacts.get(temp_i);
//            System.out.println("here: " + factIDAndSummaryKey);
            if(GameController.activeVerb == GameController.ActiveVerb.EXPOSE ){
                //If a scanned fact has already been exposed, we can't expose it again
                if (GameController.exposedFacts.contains(factIDAndSummaryKey, false) ) {
                    continue;
                } else {
                    //Else we can display it
                    k = new Label(scannedFacts.get(i), skin, "dialog-box-border");
                }
            } else if(GameController.activeVerb == GameController.ActiveVerb.HARASS){

                //If a scanned fact has already been used to threaten, we can't use it to threaten again
                if (GameController.threatenedFacts.contains(factIDAndSummaryKey, false) ) {
                    continue;
                } else {
                    //Else we can display it
                    k = new Label(scannedFacts.get(i), skin, "dialog-box-border");
                }
            }
            //Add a listener that can be reachable via the name format "target_name,fact_id"
            if(factSummaries.keySet().size() != 0) {
                k.setWidth(GameConstants.DIALOG_WIDTH - 125);
                k.setPosition(75, GameConstants.DIALOG_HEIGHT - 90 - (25 * lines));
                k.setHeight(((scannedFacts.get(i).length() + 64 + 1) / 64) * 25f);
                lines++;
                k.setName(targetName + "," + summaryToFacts.get(scannedFacts.get(i)));
                k.addListener(getBlackmailFactListener(levelController, factIDAndSummaryKey));
            }
            //k.setColor(Color.BLACK);
            k.setWrap(true);
            k.setWidth(GameConstants.DIALOG_WIDTH-125);
            k.setPosition(75, GameConstants.DIALOG_HEIGHT-90-(50*(temp_i+1)));
            GameController.blackmailDialog.addActor(k);

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
                        //Harass the target
                        int stressDamage = levelController.harass(info[0], info[1]);
                        GameController.activeVerb = GameController.ActiveVerb.NONE;
                        createDialogBox("You harassed the target!");
                        //Add this fact to the list of facts used to threaten
                        if (stressDamage < 0) {
                            GameController.threatenedFacts.add(factIDAndSummary);
                        }
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

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor){

            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor fromActor){

            }

        };
    }

    /**
     * Called when a node is hovered over.
     *
     * @param target        Target that the node belongs to
     * @param buttonLabel   I assume the label with the name of the node?
     * @param node          Node that is being hovered over
     */
    public void nodeOnEnter(TargetModel target, Label buttonLabel, Node node) {
        // Add label with name of node? to stage
        GameController.toolbarStage.addActor(buttonLabel);

        // Change node to lit up version
        node.changeColor(target.getColorState()-1);

        // Indicate that node is being hovered over
        node.setHover(true);

        // Play sound
        GameConstants.NODE_HOVER.stop();
        GameConstants.NODE_HOVER.play(.25f * GameConstants.global_sound);
    }

    /**
     * Called when a hover over a node stops.
     *
     * @param target        Target that the node belongs to
     * @param buttonLabel   I assume the label with the name of the node?
     * @param node          Node that is being hovered over
     */
    public void nodeOnExit(TargetModel target, Label buttonLabel, Node node) {
        buttonLabel.remove();
        node.changeColor(target.getColorState());

        // Indicate that node is no longer being hovered over
        node.setHover(false);
    }

    private float getDialogButtonBottomPadding(int wh) {
        if (wh >= 400) return 40f;
        return 20f;
    }

}
