package com.adisgrace.games;

import com.adisgrace.games.models.*;
import com.adisgrace.games.util.Connector;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;

public class GameController implements Screen {

    /** Enumeration representing the active verb applied via toolbar */
    public enum ActiveVerb {
        /**  Linked to Harass mode; Harass needs to be applied to a node after it has been clicked */
        HARASS,
        /**  Linked to Threaten mode; Threaten needs to be applied to a node after it has been clicked */
        THREATEN,
        /**  Linked to Expose mode; Expose needs to be applied to a node after it has been clicked */
        EXPOSE,
        /**  Linked to overworking commands*/
        OVERWORK,
        /** Linked to running more jobs to get bitecoin*/
        OTHER_JOBS,
        /** Linked to relaxing to reduce stress*/
        RELAX,
        /** Linked to no action being selected */
        NONE
    };

    private Array<String> levelJsons;
    private Array<LevelController> levelControllers;

    private final int THREATEN_AP_COST = 2;
    private final int EXPOSE_AP_COST = 3;

    /** canvas is the primary view class of the game */
    private GameCanvas canvas;
    /** stage is a Scene2d scene graph that contains all hierarchies of Scene2d Actors */
    private Stage stage;
    /** stage is a Scene2d scene graph that contains all hierarchies of Scene2d Actors specifically for the toolbar and
     * the HUD.
     */
    private Stage toolbarStage;
    /** skin is the button skin for use in the toolbar */
    private Skin skin;
    /** camera controls panning the node map */
    private OrthographicCamera camera;
    /** currentZoom controls how much the camera is zoomed in or out */
    private float currentZoom;
    /** target is the specific target being attacked */
    private Array<TargetModel> targets;
    /** world is a variable that links to all the models in the project */
    //private WorldModel world;
    LevelController levelController;
    /** activeVerb is the state of the toolbar buttons i.e. which button clicked or not clicked */
    private ActiveVerb activeVerb;
    /** hoverVerb is the verb that is currently being hovered over by the cursor */
    private ActiveVerb hoverVerb;
    /** nodeView is the view class that exposes all nodes in the map */
    private NodeView nodeView;
    /** imageNodes contains all ImageButtons for each fact node and target node */
    private Map<String, ImageButton> imageNodes;
    /** stress is the dialog label for stress */
    private Label stress;
    /** ap is the dialog label for ap */
    private Label ap;
    /** tStress is the dialog label for tStress */
    private Label tStress;
    /** tSusp is the dialog label for tSusp */
    private Label tSusp;
    /** money is the dialog label for money */
    private Label money;
    /** money is the dialog label for target state */
    private Label tState;
    /** acceleration accumulators for camera movement */
    private int left_acc, right_acc, up_acc, down_acc;
    /** time taken for camera to accelerate to max speed */
    private int acceleration_speed = 40;
    /** list of facts used to expose the target*/
    private Array<String> exposedFacts;
    /** list of facts used to threaten the target*/
    private Array<String> threatenedFacts;

    /** The ImageButton for harass, to be initialized with given texture */
    private ImageButton harass;
    /** Whether the harass button has been checked */
    private boolean harass_checked = false;
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
    /** model for player stats and actions */
    //private PlayerModel player;
    /** flag for when game ended*/
    private boolean ended = false;
    /** flag for when all nodes need to not be clicked anymore*/
    private boolean nodeFreeze = false;
    /** dialog box for blackmail commands*/
    private Dialog blackmailDialog;
    /** flag for when blackmail operations are complete*/
    private boolean getRidOfBlackmail;
    /** progress bar that tracks the stress level of players*/
    private ProgressBar stressBar;
    /** label for the amount of bitecoin a player has*/
    private Label bitecoinAmount;
    /** shapeRenderer for grid lines, may not be needed anymore*/
    private ShapeRenderer shapeRenderer;
    /** controller for camera operations*/
    private CameraController cameraController;

    /** The smallest width the game window can take */
    private static final float MINWORLDWIDTH = 1280;
    /** The smallest height the game window can take */
    private static final float MINWORLDHEIGHT = 720;

    private static final int nodeWorldWidth = 15;
    private static final int nodeWorldHeight = 15;

    /** Dimensions of map tile */
    private static final int TILE_HEIGHT = 256;
    private static final int TILE_WIDTH = 444;

    private int currentLevel;

    public GameController() {
        canvas = new GameCanvas();
        ExtendViewport viewport = new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        viewport.setCamera(canvas.getCamera());
        currentZoom = canvas.getCamera().zoom;
        stage = new Stage(viewport);
        canvas.getCamera().zoom = 1.5f;

        //TODO: write function to parse folder of level jsons
        levelJsons = new Array<>();
        levelJsons.add("testlevel.json");
        levelControllers = new Array<>();

        for(String s : levelJsons) {
            levelControllers.add(new LevelController(s));
        }

        levelController = levelControllers.get(0);

        // Create and store targets in array
//        Array<String> targetJsons = new Array<>();
//        targetJsons.add("PatrickWestfield.json");

        // Create new WorldModel with given target JSONs
        //world = new WorldModel(targetJsons);
        activeVerb = ActiveVerb.NONE;

        // Setting a target
//        target = world.getTarget("Patrick Westfield");
        targets = new Array<>();
        for (TargetModel t: levelController.getTargetModels().values()){
            targets.add(t);
        }

        //instantiating target and expose lists
        threatenedFacts = new Array<String>();
        exposedFacts = new Array<String>();
        canvas.beginDebug();
        canvas.drawIsometricGrid(stage, nodeWorldWidth, nodeWorldHeight);
        canvas.endDebug();

        // Creating Nodes
        imageNodes = new HashMap<>();
        for (TargetModel target: targets) {
            nodeView = new NodeView(stage, target, levelController);
            imageNodes.putAll(nodeView.getImageNodes());
        }

        for(ImageButton button : imageNodes.values()) { // Node Click Listeners
            final ImageButton b = button;
            button.addListener(new ClickListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Actor cbutton = (Actor)event.getListenerActor();
                    //System.out.println(cbutton.getName());
                    actOnNode(cbutton.getName(), b);
                }
            });
            button.remove();
        }

        // Adding all visible nodes
        for (TargetModel target: targets) {
            Array<String> displayedNodes= levelController.getVisibleNodes(target.getName());
            for(String str : displayedNodes) {
                stage.addActor(imageNodes.get(target.getName()+","+str));
            }
            stage.addActor(imageNodes.get(target.getName()));
        }

        Array<Connector> visibleConnectors = levelController.getAllVisibleConnectors();
        canvas.begin();
        for(Connector connector : visibleConnectors) {
            String type = connector.type;
            for (int i = 0; i < type.length(); i++) {
                String dir = type.substring(i, i+1);
                canvas.draw(new Texture(Gdx.files.internal(Connector.getAssetPath(dir))),
                        connector.xcoord, connector.ycoord);
            }
        }

        canvas.end();


        InputController ic = new InputController();
        cameraController = new CameraController(ic, canvas);
        createToolbar();
        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void show() {

    }

    @Override
    /**
     * renders the game display at consistent time steps
     */
    public void render(float delta) {

        canvas.clear();
        canvas.drawIsometricGrid(stage,nodeWorldWidth,nodeWorldHeight);

        Array<Connector> visibleConnectors = levelController.getAllVisibleConnectors();
        canvas.begin();
        for(Connector connector : visibleConnectors) {
            String type = connector.type;
            for (int i = 0; i < type.length(); i++) {
                String dir = type.substring(i, i+1);
                canvas.draw(new Texture(Gdx.files.internal(Connector.getAssetPath(dir))),
                        connector.xcoord, connector.ycoord);
            }
        }

        canvas.end();

        // If no action is currently selected, and the cursor is not hovering above any button, then remove any effects
        if (activeVerb == ActiveVerb.NONE && hoverVerb == ActiveVerb.NONE){
            unCheck();
        }

        if(!ended) {
            cameraController.moveCamera();
            toolbarStage.act(delta);
            if(!nodeFreeze) {
                stage.act(delta);
            }
        }

        stage.getViewport().apply();
        stage.draw();
        toolbarStage.getViewport().apply();
        toolbarStage.draw();
        updateStats();

        if(levelController.getLevelState() == LevelModel.LevelState.LOSE && !ended) {
            createDialogBox("YOU LOSE!");
            switchLevel(0);

        } else if (levelController.getLevelState() == LevelModel.LevelState.WIN && !ended) {
            createDialogBox("You Win!");
            switchLevel(currentLevel+1);
        }

        if(getRidOfBlackmail) {
            blackmailDialog.hide();
            getRidOfBlackmail = false;
        }

    }

    @Override
    public void resize(int width, int height) {
        //access viewports and change size
        toolbarStage.getViewport().update(width,height);
        stage.getViewport().update(width,height, true);

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

    public void switchLevel(int newLevel) {
        levelController = levelControllers.get(newLevel);
        activeVerb = ActiveVerb.NONE;

        targets = (Array<TargetModel>) levelController.getTargetModels().values();

        //instantiating target and expose lists
        threatenedFacts = new Array<String>();
        exposedFacts = new Array<String>();

        stage.clear();

        // Creating Nodes
        imageNodes = new HashMap<>();
        for (TargetModel target: targets) {
            nodeView = new NodeView(stage, target, levelController);
            imageNodes.putAll(nodeView.getImageNodes());
        }

        for(ImageButton button : imageNodes.values()) { // Node Click Listeners
            final ImageButton b = button;
            button.addListener(new ClickListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Actor cbutton = (Actor)event.getListenerActor();
                    //System.out.println(cbutton.getName());
                    actOnNode(cbutton.getName(), b);
                }
            });
            button.remove();
        }

        // Adding all visible nodes
        for (TargetModel target: targets) {
            Array<String> displayedNodes= levelController.getVisibleNodes(target.getName());
            for(String str : displayedNodes) {
                stage.addActor(imageNodes.get(target.getName()+","+str));
            }
            stage.addActor(imageNodes.get(target.getName()));
        }

    }

    /**
     * This helper method sets all buttons in toolbar to their unchecked/original states
     */
    private void unCheck(){
        harass_checked = false;
        threaten_checked = false;
        expose_checked = false;
        otherJobs_checked = false;
        overwork_checked = false;
        relax_checked = false;
        harass.setChecked(false);
        threaten.setChecked(false);
        expose.setChecked(false);
        otherJobs.setChecked(false);
        overwork.setChecked(false);
        relax.setChecked(false);
        activeVerb = ActiveVerb.NONE;
    }

    /**
     * This method creates a harass button with given textures for it's original status, when the cursor is hovering
     * above it and when it is clicked.
     *
     * @return      ImageButton for harass.
     */
    private ImageButton createHarass(){
        harass = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/harass_up.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/harass_down.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/harass_select.png")))));
        harass.setTransform(true);
        harass.setScale(1f);
        harass.addListener(new ClickListener()
        {
            Label  harassLabel = new Label("Harass: Harass your target to slightly increase their stress for 2 AP", skin);

            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                if (harass_checked == false){
                    unCheck();
                    activeVerb = ActiveVerb.HARASS;
                    harass_checked = true;
                    harass.setChecked(true);
                }else{
                    unCheck();
                }
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor){
                if(activeVerb != ActiveVerb.HARASS){
                    Vector2 zeroLoc = harass.localToStageCoordinates(new Vector2(0, harass.getHeight()));
                    harassLabel.setX(zeroLoc.x);
                    harassLabel.setY(zeroLoc.y);
                    toolbarStage.addActor(harassLabel);
                    hoverVerb = ActiveVerb.HARASS;
                    harass.setChecked(true);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor){
                harassLabel.remove();
                hoverVerb = ActiveVerb.NONE;
                if (activeVerb!=ActiveVerb.HARASS)harass.setChecked(false);
            }
        });
        return harass;
    }

    /**
     * This method creates a threaten button with given textures for it's original status, when the cursor is hovering
     * above it and when it is clicked.
     *
     * @return      ImageButton for threaten.
     */
    private ImageButton createThreaten(){
        threaten = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/threaten_up.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/threaten_down.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/threaten_select.png")))));
        threaten.setTransform(true);
        threaten.setScale(1f);
        threaten.addListener(new ClickListener()
        {

            Label  threatenLabel = new Label("Threaten: Threaten your target with a \n fact to blackmail to increase their stress " +
                    "for 2 AP", skin);

            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                if (threaten_checked == false){
                    unCheck();
                    activeVerb = ActiveVerb.THREATEN;
                    threaten_checked = true;
                    threaten.setChecked(true);
                }else{
                    unCheck();
                }
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor){
                if(activeVerb != ActiveVerb.THREATEN){
                    Vector2 zeroLoc = threaten.localToStageCoordinates(new Vector2(0, threaten.getHeight()));
                    threatenLabel.setX(zeroLoc.x);
                    threatenLabel.setY(zeroLoc.y);
                    toolbarStage.addActor(threatenLabel);
                    hoverVerb = ActiveVerb.THREATEN;
                    threaten.setChecked(true);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor){
                threatenLabel.remove();
                hoverVerb = ActiveVerb.NONE;
                if (activeVerb!=ActiveVerb.THREATEN)threaten.setChecked(false);
            }
        });
        return threaten;
    }

    /**
     * This method creates a expose button with given textures for it's original status, when the cursor is hovering
     * above it and when it is clicked.
     *
     * @return      ImageButton for expose.
     */
    private ImageButton createExpose(){
        expose = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/expose_up.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/expose_down.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/expose_select.png")))));
        expose.setTransform(true);
        expose.setScale(1f);
        expose.addListener(new ClickListener()
        {

            Label  exposeLabel = new Label("Expose: Expose your target's fact to the public\n for large stress damage" +
                    " for 3 AP", skin);

            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                if (expose_checked == false){
                    unCheck();
                    activeVerb = ActiveVerb.EXPOSE;
                    expose_checked = true;
                    expose.setChecked(true);
                }else{
                    unCheck();
                }
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor){
                if(activeVerb != ActiveVerb.EXPOSE){
                    Vector2 zeroLoc = expose.localToStageCoordinates(new Vector2(0, expose.getHeight()));
                    exposeLabel.setX(zeroLoc.x);
                    exposeLabel.setY(zeroLoc.y);
                    toolbarStage.addActor(exposeLabel);
                    hoverVerb = ActiveVerb.EXPOSE;
                    expose.setChecked(true);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor){
                exposeLabel.remove();
                hoverVerb = ActiveVerb.NONE;
                if (activeVerb!=ActiveVerb.EXPOSE)expose.setChecked(false);
            }
        });
        return expose;
    }

    /**
     * This method creates a overwork button with given textures for it's original status, when the cursor is hovering
     * above it and when it is clicked.
     *
     * @return      ImageButton for overwork.
     */
    private ImageButton createOverwork(){
        overwork = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/overwork_up.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/overwork_down.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/overwork_select.png")))));
        overwork.setTransform(true);
        overwork.setScale(1f);
        overwork.addListener(new ClickListener()
        {
            Label  overworkLabel = new Label("Overwork: Gains 2 AP, but Increases Stress", skin);

            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                unCheck();
                final String s = "overwork";
                confirmDialog("Are you sure you want to overwork?", s);
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor){

                if(activeVerb != ActiveVerb.OVERWORK){
                    Vector2 zeroLoc = overwork.localToStageCoordinates(new Vector2(0, overwork.getHeight()));
                    overworkLabel.setX(zeroLoc.x);
                    overworkLabel.setY(zeroLoc.y);
                    toolbarStage.addActor(overworkLabel);
                    hoverVerb = ActiveVerb.OVERWORK;
                    overwork.setChecked(true);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor){
                overworkLabel.remove();
                hoverVerb = ActiveVerb.NONE;
                if (activeVerb!=ActiveVerb.OVERWORK)overwork.setChecked(false);
            }
        });
        return overwork;
    }

    /**
     * This method creates a otherjobs button with given textures for it's original status, when the cursor is hovering
     * above it and when it is clicked.
     *
     * @return      ImageButton for otherjobs.
     */
    private ImageButton createOtherJobs(){
        otherJobs = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/otherjobs_up.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/otherjobs_down.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/otherjobs_select.png")))));
        otherJobs.setTransform(true);
        otherJobs.setScale(1f);
        otherJobs.addListener(new ClickListener()
        {

            Label  otherJobLabel = new Label("Other Jobs: Make Money with 3 AP", skin);

            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                unCheck();
                final String s = "otherJobs";
                confirmDialog("Are you sure you want to do other jobs?", s);
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor){
                if(activeVerb != ActiveVerb.OTHER_JOBS){
                    Vector2 zeroLoc = otherJobs.localToStageCoordinates(new Vector2(0, otherJobs.getHeight()));
                    otherJobLabel.setX(zeroLoc.x);
                    otherJobLabel.setY(zeroLoc.y);
                    toolbarStage.addActor(otherJobLabel);
                    hoverVerb = ActiveVerb.OTHER_JOBS;
                    otherJobs.setChecked(true);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor){
                otherJobLabel.remove();
                hoverVerb = ActiveVerb.NONE;
                if (activeVerb!=ActiveVerb.OTHER_JOBS)otherJobs.setChecked(false);
            }
        });
        return otherJobs;
    }

    /**
     * This method creates a relax button with given textures for it's original status, when the cursor is hovering
     * above it and when it is clicked.
     *
     * @return      ImageButton for relax.
     */
    private ImageButton createRelax(){
        relax = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/relax_up.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/relax_down.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/relax_select.png")))));
        relax.setTransform(true);
        relax.setScale(1f);
        relax.addListener(new ClickListener()
        {
            Label  relaxLabel = new Label("Relax: Decreases Stress with 1 AP", skin);

            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                unCheck();
                final String s = "relax";
                confirmDialog("Are you sure you want to relax?", s);
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor){
                if(activeVerb != ActiveVerb.RELAX){
                    Vector2 zeroLoc = relax.localToStageCoordinates(new Vector2(0, relax.getHeight()));
                    relaxLabel.setX(zeroLoc.x);
                    relaxLabel.setY(zeroLoc.y);;
                    toolbarStage.addActor(relaxLabel);
                    hoverVerb = ActiveVerb.RELAX;
                    relax.setChecked(true);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor){
                relaxLabel.remove();
                hoverVerb = ActiveVerb.NONE;
                if (activeVerb!=ActiveVerb.RELAX)relax.setChecked(false);
            }
        });
        return relax;
    }

    /**
     * This method creates a EndDay button with given textures for it's original status, when the cursor is hovering
     * above it and when it is clicked.
     *
     * @return      ImageButton for EndDay.
     */
    private ImageButton createEndDay(){
        ImageButton end = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("UI/EndDay.png")))));
        end.setTransform(true);
        end.setScale(1f);
        end.addListener(new ClickListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                createDialogBox("You end the day after a long battle of psychological warfare.");
                levelController.endDay();
            }
        });
        return end;
    }

    /**
     * This method creates a Settings button with given textures for it's original status, when the cursor is hovering
     * above it and when it is clicked.
     *
     * @return      ImageButton for Settings.
     */
    private ImageButton createSettings(){
        ImageButton settings = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("UI/Settings.png")))));
        settings.setTransform(true);
        settings.setScale(1f);
        settings.addListener(new ClickListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                createDialogBox("You clicked something that hasn't been implemented yet.");
            }
        });
        return settings;
    }

    /**
     * This method creates a Notebook button with given textures for it's original status, when the cursor is hovering
     * above it and when it is clicked.
     *
     * @return      ImageButton for Notebook.
     */
    private ImageButton createNotebook(){
        ImageButton notebook = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("UI/Notebook.png")))));
        notebook.setTransform(true);
        notebook.setScale(1f);
        notebook.addListener(new ClickListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                final String s = "notebook";
                confirmDialog("Are you sure you want to open notebook?", s);
            }
        });
        return notebook;
    }

    /**
     * CreateToolbar creates a fixed toolbar with buttons linked to each of the player skills.
     *
     * It has three tables, one for the left side of the toolbar, one for the right side, and another for the
     * skill bar.
     *
     * This function also adds a input multiplexer with each stage. The toolbar has higher priority for input
     *
     */
    public void createToolbar() {
        ExtendViewport toolbarViewPort = new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        toolbarStage = new Stage(toolbarViewPort);
        skin = new Skin(Gdx.files.internal("skins/neon-ui-updated.json"));

        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(toolbarStage);
        inputMultiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(inputMultiplexer);

        stressBar = new ProgressBar(0f, 100f, 1f, true, skin, "synthwave");
        stressBar.setValue(levelController.getPlayerStress());

        createHarass();
        createExpose();
        createRelax();
        createOtherJobs();
        createOverwork();
        createThreaten();
        ImageButton end = createEndDay();
        ImageButton settings = createSettings();
        ImageButton notebook = createNotebook();

        Table toolbar = createToolbarTable(end, settings, notebook);
        toolbarStage.addActor(toolbar);
        toolbarStage.addActor(createStats());
    }

    /**
     * This method creates a toolbar table with the leftside, rightside, and skill bar tables embedded inside.
     *
     * @param end ImageButton for end day
     * @param notebook ImageButton for notebook
     * @param settings ImageButton for settings
     * @return the toolbar table
     */
    private Table createToolbarTable(ImageButton end, ImageButton notebook, ImageButton settings) {
        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();

        Table toolbar = new Table();
        toolbar.bottom();
        toolbar.setSize(width, .25f*height);

        Table leftSide = createLeftsideTable(toolbar);
        Table skillBar = createSkillBarTable(toolbar);
        Table rightSide = createRightsideTable(toolbar, end, notebook, settings);

        toolbar.add(leftSide).left().width(.25f*toolbar.getWidth()).height(.10f*toolbar.getHeight()).align(Align.top);
        toolbar.add(skillBar).width(.6f*toolbar.getWidth()).height(.10f*toolbar.getWidth()).align(Align.bottom);
        toolbar.add(rightSide).right().width(.15f*toolbar.getWidth()).height(.10f*toolbar.getHeight()).align(Align.top);
        return toolbar;
    }

    /**
     * This method creates a skill bar using threaten, expose, overwork, otherJobs, relac
     * @param toolbar table that will encapsulate all other tables
     * @return the skillBar table
     */
    private Table createSkillBarTable(Table toolbar) {
        Table skillBar = new Table();
        skillBar.setSize(toolbar.getWidth()*.60f, toolbar.getHeight());
        int numSkills = 6+1;
        float pad = skillBar.getWidth() / 60f;
        //skillBar.add(harass).width(skillBar.getWidth()/numSkills).height(skillBar.getHeight()).padRight(pad).align(Align.bottom);
        skillBar.add(threaten).width(skillBar.getWidth()/numSkills).height(skillBar.getHeight()).padRight(pad).align(Align.bottom);
        skillBar.add(expose).width(skillBar.getWidth()/numSkills).height(skillBar.getHeight()).padRight(pad).align(Align.bottom);
        skillBar.add(overwork).width(skillBar.getWidth()/numSkills).height(skillBar.getHeight()).padRight(pad).align(Align.bottom);
        skillBar.add(otherJobs).width(skillBar.getWidth()/numSkills).height(skillBar.getHeight()).padRight(pad).align(Align.bottom);
        skillBar.add(relax).width(skillBar.getWidth()/numSkills).height(skillBar.getHeight()).padRight(pad).align(Align.bottom);
        return skillBar;
    }

    /**
     * This method creates the right side table with end day, notebook, settings
     * @param toolbar table that will encapsulate all other tables
     * @param end ImageButton for end
     * @param notebook ImageButton for notebook
     * @param settings ImageButton for settings
     * @return the right side table
     */
    private Table createRightsideTable(Table toolbar, ImageButton end, ImageButton notebook, ImageButton settings) {
        Table rightSide = new Table();
        rightSide.setSize(toolbar.getWidth()*.05f, toolbar.getHeight()/8);
        rightSide.add(end).width(rightSide.getWidth()).height(/*rightSide.getHeight*/70f).align(Align.center);
        rightSide.row();
        rightSide.add(notebook).width(rightSide.getWidth()).height(/*rightSide.getHeight*/100f).align(Align.center);
        rightSide.row();
        rightSide.add(settings).width(rightSide.getWidth()).height(/*rightSide.getHeight*/100f).align(Align.center);
        return rightSide;

    }

    /**
     * This method creates the left side table, which will have the bitecoin counter and the progress bar
     * @param toolbar the table that will encapsulate all other tables
     * @return the left side table
     */
    private Table createLeftsideTable(Table toolbar) {
        Table leftSide = new Table();
        leftSide.setSize(toolbar.getWidth()*.25f, toolbar.getHeight());
        leftSide.add(stressBar).left().width(75).height(244);
        leftSide.add(createBitecoinStack());
        return leftSide;
    }

    /**
     * This method creates a Stack UI element that has the bitecoin numbers and the UI drawable
     * @return the bitecoin stack
     */
    private Stack createBitecoinStack(){
        Stack bitecoinStack = new Stack();

        Image bitecoinCounter = new Image(new TextureRegionDrawable(new TextureRegion(
                new Texture("UI/BitecoinCounter.png"))));
        bitecoinAmount = new Label(Integer.toString((int)levelController.getPlayerCurrency()), skin, "bitcoin");

        bitecoinStack.add(bitecoinCounter);
        bitecoinStack.add(bitecoinAmount);
        return bitecoinStack;

    }

    /**
     * Helper function that converts coordinates from world space to isometric space.
     *
     * @param coords   Coordinates in world space to transform
     * @return         Given coordinates in isometric space
     */
    private Vector2 worldToIsometric(Vector2 coords) {
        float tempx = coords.x;
        float tempy = coords.y;
        coords.x = 0.57735f * tempx - tempy;
        coords.y = 0.57735f * tempx + tempy;

        return coords;
    }

    /**
     * Helper function that gets the center of an isometric grid tile nearest to the given coordinates.
     *
     * Called when snapping an image to the center of a grid tile.
     *
     * The nearest isometric center is just stored in the vector cache [vec].
     *
     * @param x     x-coordinate of the location we want to find the nearest isometric center to
     * @param y     y-coordinate of the location we want to find the nearest isometric center to
     */
    private Vector2 nearestIsoCenter(Vector2 vec, float x, float y){
        // Transform world coordinates to isometric space
        vec.set(x,y);
        vec = worldToIsometric(vec);
        x = vec.x;
        y = vec.y;

        // Find the nearest isometric center
        x = Math.round(x / TILE_HEIGHT);
        y = Math.round(y / TILE_HEIGHT);

        // Transform back to world space
        vec.set(x * (0.5f * TILE_WIDTH) + y * (0.5f * TILE_WIDTH),
                -x * (0.5f * TILE_HEIGHT) + y * (0.5f * TILE_HEIGHT));

        return vec;
    }


    /**
     * Creates a stats table that appears as the HUD
     * @return the stats table
     */
    private Table createStats() {
        Table stats = new Table();
        stress = new Label("Player Stress: " + Integer.toString((int)(levelController.getPlayerStress())), skin);
        stress.setFontScale(2);
        ap = new Label("AP: " + Integer.toString(levelController.getAP()), skin);
        ap.setFontScale(2);
//        tStress = new Label("Target Stress: " + Integer.toString(target.getStress()), skin);
//        tStress.setFontScale(2);
//        tSusp = new Label("Target Suspicion: " + Integer.toString(target.getSuspicion()), skin);
//        tSusp.setFontScale(2);
//        money = new Label ("Bitecoin: " + Integer.toString((int)world.getPlayer().getBitecoin()), skin);
//        money.setFontScale(2);
//        tState = new Label("Target State: " + target.getState(), skin);
//        tState.setFontScale(2);

        stats.top();
        stats.setFillParent(true);

        //stats.add(stress).expandX().padTop(20);
//        stats.add(tState).expandX().padTop(20);
        //stats.add(money).expandX().padTop(20);
//        stats.add(tStress).expandX().padTop(20);
//        stats.add(tSusp).expandX().padTop(20);
        stats.add(ap).expandX().padTop(10);
        return stats;
    }

    /**
     * Controls what actions the game needs to take on a specified node based on the
     * activeVerb that was clicked
     * @param nodeName name of target and fact in the form "target_name,fact_id"
     * @param button the node button
     */
    public void actOnNode(String nodeName, ImageButton button) {
        String[] nodeInfo = nodeName.split(",");
        boolean isTarget = false;
        if(nodeInfo.length == 1) {
            isTarget = true;
        }
        if(ended || nodeFreeze) {
            return;
        }
        switch (activeVerb) {
            case NONE:
                if(!isTarget) {
                    switch (levelController.getCurrentNodeState(nodeInfo[0], nodeInfo[1])) {
                        case 3: //locked
                            int hack = levelController.hack(nodeInfo[0], nodeInfo[1]);
                            if(hack <= -1) {
                                System.out.println("HACK IS NOT WORKING");
                                System.exit(1);
                            }
                            if(hack == 1) {
                                Texture node = new Texture("node/N_UnscannedNode_1.png");
                                TextureRegion[][] regions = new TextureRegion(node).split(
                                        node.getWidth() / 10,
                                        node.getHeight() / 6);
                                TextureRegion tRegion = regions[0][0];

                                Texture node_base = new Texture("node/N_NodeBase_1.png");
                                TextureRegion[][] node_regions = new TextureRegion(node_base).split(
                                        node_base.getWidth() / 6,
                                        node_base.getHeight() / 2);

                                Texture combined = GameCanvas.combineTextures(tRegion, node_regions[0][0]);

                                TextureRegionDrawable drawable = new TextureRegionDrawable(new TextureRegion(combined));

                                button.setStyle(new ImageButton.ImageButtonStyle(null, null, null,
                                        drawable, null, null));

                                createDialogBox("You hacked the node successfully!");


                                reloadDisplayedNodes();
                            } else if(hack == -3) {
                                createDialogBox("Insufficient AP to hack this node.");
                            } else if(hack == -4) {
                                createDialogBox("You failed to hack the node!");
                            }
                            break;
                        case 2://scannable
                            boolean success = levelController.scan(nodeInfo[0], nodeInfo[1]);
                            if(success) {
                                Texture node = new Texture("node/N_ScannedNode_2.png");
                                TextureRegion[][] regions = new TextureRegion(node).split(
                                        node.getWidth() / 10,
                                        node.getHeight() / 6);
                                TextureRegion tRegion = regions[0][0];

                                Texture node_base = new Texture("node/N_NodeBase_1.png");
                                TextureRegion[][] node_regions = new TextureRegion(node_base).split(
                                        node_base.getWidth() / 6,
                                        node_base.getHeight() / 2);

                                Texture combined = GameCanvas.combineTextures(tRegion, node_regions[0][0]);
                                TextureRegionDrawable drawable = new TextureRegionDrawable(new TextureRegion(combined));
                                button.setStyle(new ImageButton.ImageButtonStyle(null, null, null,
                                        drawable, null, null));
                                createDialogBox(levelController.viewFact(nodeInfo[0], nodeInfo[1]));
                                reloadDisplayedNodes();
                            } else {
                                createDialogBox("Insufficient AP to scan this node.");
                            }
                            break;
                        case 1://viewable
                            createDialogBox(levelController.viewFact(nodeInfo[0], nodeInfo[1]));
                            break;
                    }

                }
                break;
            case HARASS:
//                if(isTarget) {
//                    if(world.getPlayer().canHarass()) {
//                        world.harass(target.getName());
//                        createDialogBox("You harassed the target! They seem incredibly disturbed and got a bit stressed.");
//                        activeVerb = ActiveVerb.NONE;
//                    }
//                    else {
//                        createDialogBox("Insufficient AP to harass the target.");
//                        activeVerb = ActiveVerb.NONE;
//                    }
//                }
                break;
            case THREATEN:
                if(isTarget) {
                    if(levelController.getAP() >= THREATEN_AP_COST) {
                        getBlackmailFact("Select a fact to threaten the target with.", nodeInfo[0]);
                    }
                    else {
                        createDialogBox("Insufficient AP to threaten the target.");
                        activeVerb = ActiveVerb.NONE;
                    }
                }
                break;
            case EXPOSE:
                if(isTarget) {
                    if(isTarget) {
                        if(levelController.getAP() >= EXPOSE_AP_COST) {
                            getBlackmailFact("Select a fact to expose the target with.", nodeInfo[0]);
                        }
                        else {
                            createDialogBox("Insufficient AP to expose the target.");
                            activeVerb = ActiveVerb.NONE;
                        }
                    }
                }
                break;
            default:
                System.out.println("You shall not pass");
                break;
        }
    }


    /**
     * Creates a dialog box with [s] at a reasonably-sized height and width
     * @param s the string displayed
     */
    public void createDialogBox(String s) {
        Dialog dialog = new Dialog("", skin) {
            public void result(Object obj) {
                nodeFreeze = false;
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
        dialog.show(toolbarStage);
        nodeFreeze = true;
    }

    /**
     * Creates a dialog box for the notebook with [s] at a reasonably-sized height and width
     * @param s the string displayed
     */
    public void createNotebook(String s, String targetName) {
        Dialog dialog = new Dialog("Notebook", skin) {
            public void result(Object obj) {
                nodeFreeze = false;
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
        dialog.show(toolbarStage);
        nodeFreeze = true;
    }

    public void createNotebookTargetSelector(String s) {
        Dialog dialog = new Dialog("Notebook", skin) {
            public void result(Object obj) {
                nodeFreeze = false;

                if(obj.getClass() == Boolean.class) {
                    return;
                }

                createNotebook("Notebook:", targets.get((int)obj).getName());
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
        dialog.show(toolbarStage);
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
    public void getBlackmailFact(String s, String targetName) {
        blackmailDialog = new Dialog("Notebook", skin) {
            public void result(Object obj) {
                //to activate the node clicking once more
                nodeFreeze = false;
                activeVerb = ActiveVerb.NONE;
            }
        };
        TextureRegion tRegion = new TextureRegion(new Texture(Gdx.files.internal("skins/background.png")));
        TextureRegionDrawable drawable = new TextureRegionDrawable(tRegion);

        blackmailDialog.setBackground(drawable);
        blackmailDialog.getBackground().setMinWidth(500);
        blackmailDialog.getBackground().setMinHeight(500);
        Label l = new Label( s, skin );
        //scale sizing based on the amount of text
        if(s.length() > 50) {
            l.setFontScale(1.5f);
        }else {
            l.setFontScale(2f);
        }
        l.setWrap( true );
        blackmailDialog.setMovable(true);
        //Add the text to the center of the dialog box
        blackmailDialog.getContentTable().add( l ).prefWidth( 350 );
        //Get all fact summaries that can potentially be displayed
        Map<String, String> factSummaries = levelController.getNotes(targetName);

        //This will store all mappings from summaries to a fact name
        Map<String, String> summaryToFacts = new HashMap<>();
        //This will store the fact ids of all the scanned facts

        final Array<String> scannedFacts = new Array<>();

        Table table = blackmailDialog.getContentTable();
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
            if(activeVerb == ActiveVerb.EXPOSE ){
                //If a scanned fact has already been exposed, we can't expose it again
                if (exposedFacts.contains(scannedFacts.get(temp_i), false) ) {
                    continue;
                } else {
                    //Else we can display it
                    k = new Label(scannedFacts.get(i), skin);
                }
            } else if(activeVerb == ActiveVerb.THREATEN){
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
            k.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Actor cbutton = (Actor)event.getListenerActor();
                    String[] info = cbutton.getName().split(",");
                    getRidOfBlackmail = true;
                    switch (activeVerb) {
                        case HARASS:

                        case THREATEN:
                            //Threaten the target
                            levelController.threaten(info[0], info[1]);
                            activeVerb = ActiveVerb.NONE;
                            createDialogBox("You threatened the target!");
                            //Add this fact to the list of facts used to threaten
                            threatenedFacts.add(scannedFacts.get(temp_i));
                            break;
                        case EXPOSE:
                            //Expose the target
                            levelController.expose(info[0], info[1]);
                            activeVerb = ActiveVerb.NONE;
                            createDialogBox("You exposed the target!");
                            //Add this fact to the list of facts used to expose
                            exposedFacts.add(scannedFacts.get(temp_i));
                            //Add this fact to the list of facts used to threaten
                            threatenedFacts.add(scannedFacts.get(temp_i));
                            break;
                        default:
                            System.out.println("This shouldn't be happening.");
                    }
                }
            });
            //Add the displayed fact to the middle of the blackmail dialog
            table.add(k).prefWidth(350);
            table.row();
        }

        blackmailDialog.button("Cancel", true); //sends "true" as the result
        blackmailDialog.key(Input.Keys.ENTER, true); //sends "true" when the ENTER key is pressed
        blackmailDialog.show(toolbarStage);
        //Make sure nothing else is able to be clicked while blackmail dialog is shown
        nodeFreeze = true;
    }

    /**
     * Displays a dialog box where the user can confirm whether or not they want
     * to proceed with a particular action
     * @param s
     * @param function
     */
    public void confirmDialog(String s, final String function) {
        Dialog dialog = new Dialog("", skin) {
            public void result(Object obj) {
                if((boolean)obj) {
                    callConfirmFunction(function);
                } else {

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
        dialog.show(toolbarStage);

    }

    /**
     * Display all fact nodes of a target node that is visible
     */
    public void reloadDisplayedNodes() {
        for(TargetModel target : targets) {
            Array<String> displayedNodes= levelController.getVisibleNodes(target.getName());
            for(String str : displayedNodes) {
                stage.addActor(imageNodes.get(target.getName()+","+str));
            }
        }

    }

    /**
     * Displays a dialog based on what active verb was clicked.
     * @param s
     */
    public void callConfirmFunction(String s) {
        boolean success;
        switch(s) {
            case "overwork":
                success = levelController.overwork();
                if(success) {
                    createDialogBox("You overworked yourself and gained 2 AP at the cost of your sanity...");
                } else {
                    createDialogBox("You cannot overwork anymore today!");
                }
                break;
            case "relax":
                success = levelController.relax();

                if(success) {
                    createDialogBox("You rested for 1 AP and decreased your stress!");
                } else {
                    createDialogBox("Insufficient AP to relax.");
                }
                break;
            case "otherJobs":
                float money = levelController.otherJobs();
                if(money != -1f) {

                    createDialogBox("You did some other jobs and earned some " + Float.toString(money) +  " bitecoin for yourself!");
                } else {
                    createDialogBox("Insufficient AP to do other jobs");
                }
                break;
            case "notebook":
                //createNotebook("Notebook:");
                createNotebookTargetSelector("Select a target to view facts for.");
                break;
            default:
                System.out.println("You shall not pass");
        }
    }

    /**
     * Updates the stats HUD with current values
     */
    public void updateStats(){
        //stress.setText("Player Stress: " + Integer.toString((int)world.getPlayer().getStress()));
        stressBar.setValue(levelController.getPlayerStress());
        bitecoinAmount.setText(Integer.toString((int)levelController.getPlayerCurrency()));
        ap.setText("AP: " + Integer.toString(levelController.getAP()));
//        tStress.setText("Target Stress: " + Integer.toString(target.getStress()));
//        tSusp.setText("Target Suspicion: " + Integer.toString(target.getSuspicion()));
//        money.setText("Bitecoin: " + Integer.toString((int)world.getPlayer().getBitecoin()));
    }
}
