package com.adisgrace.games;

import com.adisgrace.games.models.PlayerModel;
import com.adisgrace.games.models.TargetModel;
import com.adisgrace.games.models.WorldModel;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
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
        /**  Linked to hack and scan commands*/
        OVERWORK,
        OTHER_JOBS,
        RELAX,
        NONE
    };

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
    private TargetModel target;
    /** world is a variable that links to all the models in the project */
    private WorldModel world;
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

    private ImageButton harass;
    private boolean harass_checked = false;
    private ImageButton threaten;
    private boolean threaten_checked = false;
    private ImageButton expose;
    private boolean expose_checked = false;
    private ImageButton overwork;
    private boolean overwork_checked = false;
    private ImageButton otherJobs;
    private boolean otherJobs_checked = false;
    private ImageButton relax;
    private boolean relax_checked = false;

    private PlayerModel player;

    private boolean ended = false;
    private boolean nodeFreeze = false;

    private Dialog blackmailDialog;
    private boolean getRidOfBlackmail;
    private ProgressBar stressBar;
    private Label bitecoinAmount;

    private ShapeRenderer shapeRenderer;

    private static final float MINWORLDWIDTH = 1280;
    private static final float MINWORLDHEIGHT = 720;

    public GameController() {
        canvas = new GameCanvas();
//        Gdx.graphics.getWidth();
        ExtendViewport viewport = new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        viewport.setCamera(canvas.getCamera());
        currentZoom = canvas.getCamera().zoom;
        stage = new Stage(viewport);
        canvas.getCamera().zoom = 1.5f;

        // Create and store targets in array
        Array<String> targetJsons = new Array<>();
        targetJsons.add("PatrickWestfield.json");

        // Create new WorldModel with given target JSONs
        world = new WorldModel(targetJsons);
        player = world.getPlayer();
        activeVerb = ActiveVerb.NONE;

        // Setting a target
        target = world.getTarget("Patrick Westfield");

        //instantiating target and expose lists
        threatenedFacts = new Array<String>();
        exposedFacts = new Array<String>();

        // Creating Nodes
        nodeView = new NodeView(stage, target, world);
        imageNodes = nodeView.getImageNodes();
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
        Array<String> displayedNodes= world.getDisplayedNodes().get(target.getName());
        for(String str : displayedNodes) {
            stage.addActor(imageNodes.get(target.getName()+","+str));
        }
        stage.addActor(imageNodes.get(target.getName()));

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

        if (activeVerb == ActiveVerb.NONE && hoverVerb == ActiveVerb.NONE){
            unCheck();
        }

        // Was supposed to freeze nodes and stop action if game ended or if popup window exists... Didn't work yet.
        if(!ended) {
            moveCamera();
            toolbarStage.act(delta);
            if(!nodeFreeze) {
                stage.act(delta);
            } else {

            }

        }
//        canvas.begin();
//        canvas.end();


        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1, 1, 1, 1);
        Vector2 v1;
        Vector2 v2;

        //Vertical lines
        float maxVertical = 8f * stage.getHeight();
        float minVertical = -10f * stage.getHeight();
        float maxHorizontal = 7f * stage.getWidth();
        float minHorizontal = -10f * stage.getWidth();
        float incrementV = (maxVertical - minVertical) / 26;
//        float incrementH = (maxHorizontal - minHorizontal) / 60;

        for (float i = minVertical; i < maxVertical; i = i+incrementV){
            v1 = convertToIsometric(new Vector2(i, 10f * stage.getHeight()));
            v2 = convertToIsometric(new Vector2(i, -10f * stage.getHeight()));
            shapeRenderer.line(v1.x, v1.y, v2.x, v2.y);
        }
//
//        Vector2 v1 = convertToIsometric(new Vector2(stage.getWidth()/2, 10f * stage.getHeight()));
//        Vector2 v2 = convertToIsometric(new Vector2(stage.getWidth()/2, -10f * stage.getHeight()));
//        shapeRenderer.line(v1.x, v1.y, v2.x, v2.y);
//
//        v1 = convertToIsometric(new Vector2(stage.getWidth()/4, 10f * stage.getHeight()));
//        v2 = convertToIsometric(new Vector2(stage.getWidth()/4, -10f * stage.getHeight()));
//        shapeRenderer.line(v1.x, v1.y, v2.x, v2.y);
//
//        v1 = convertToIsometric(new Vector2(3 * stage.getWidth()/4, 10f * stage.getHeight()));
//        v2 = convertToIsometric(new Vector2(3 * stage.getWidth()/4, -10f * stage.getHeight()));
//        shapeRenderer.line(v1.x, v1.y, v2.x, v2.y);

        //Horizontal lines
        for (float i = minHorizontal; i < maxHorizontal; i = i+incrementV){
            v1 = convertToIsometric(new Vector2(10f * stage.getWidth(), i));
            v2 = convertToIsometric(new Vector2(-10f * stage.getWidth(), i));
            shapeRenderer.line(v1.x, v1.y, v2.x, v2.y);
        }

//        v1 = convertToIsometric(new Vector2(10f * stage.getWidth(), stage.getHeight()/2));
//        v2 = convertToIsometric(new Vector2(-10f * stage.getWidth(), stage.getHeight()/2));
//        shapeRenderer.line(v1.x, v1.y, v2.x, v2.y);


        shapeRenderer.end();

        stage.getViewport().apply();
        stage.draw();

        toolbarStage.getViewport().apply();
        toolbarStage.draw();
        updateStats();


        if(world.getGameState() == WorldModel.GAMESTATE.LOSE && !ended) {
            createDialogBox("YOU LOSE!");
            ended = true;

        } else if (world.getGameState() == WorldModel.GAMESTATE.WIN && !ended) {
            createDialogBox("You Win!");
            ended = true;
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

    private ImageButton createHarass(){
        harass = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/harass_up.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/harass_down.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/harass_select.png")))));
        harass.setTransform(true);
        harass.setScale(1f);
        harass.addListener(new ClickListener()
        {
            Label  harassLabel = new Label("Harass: 2 AP", skin);

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
                    harassLabel.setX(event.getStageX());
                    harassLabel.setY(event.getStageY());
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

    private ImageButton createThreaten(){
        threaten = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/threaten_up.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/threaten_down.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/threaten_select.png")))));
        threaten.setTransform(true);
        threaten.setScale(1f);
        threaten.addListener(new ClickListener()
        {
            Label  threatenLabel = new Label("Threaten: 2 AP, select an evidence which you want to use against your target, increases stress.", skin);

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
                    threatenLabel.setX(event.getStageX());
                    threatenLabel.setY(event.getStageY());
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
    private ImageButton createExpose(){
        expose = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/expose_up.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/expose_down.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/expose_select.png")))));
        expose.setTransform(true);
        expose.setScale(1f);
        expose.addListener(new ClickListener()
            {

                Label  exposeLabel = new Label("Expose: 2 AP, select an evidence and expose it to everyone, increases a moderate amount of stress.", skin);

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
                        exposeLabel.setX(event.getStageX());
                        exposeLabel.setY(event.getStageY());
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

    private ImageButton createOverwork(){
        overwork = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/overwork_up.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/overwork_down.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/overwork_select.png")))));
        overwork.setTransform(true);
        overwork.setScale(1f);
        overwork.addListener(new ClickListener()
        {
            Label  overworkLabel = new Label("Overwork: Gains AP, Increases Stress", skin);

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
                    overworkLabel.setX(event.getStageX());
                    overworkLabel.setY(event.getStageY());
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

    private ImageButton createOtherJobs(){
        otherJobs = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/otherjobs_up.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/otherjobs_down.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/otherjobs_select.png")))));
        otherJobs.setTransform(true);
        otherJobs.setScale(1f);
        otherJobs.addListener(new ClickListener()
        {
            Label  otherJobLabel = new Label("Other Jobs: 3 AP, gain some bitecoins through working", skin);

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
                    otherJobLabel.setX(event.getStageX());
                    otherJobLabel.setY(event.getStageY());
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

    private ImageButton createRelax(){
        relax = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/relax_up.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/relax_down.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/relax_select.png")))));
        relax.setTransform(true);
        relax.setScale(1f);
        relax.addListener(new ClickListener()
        {
            Label  relaxLabel = new Label("Relax: Decreases Stress with AP", skin);

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
                    relaxLabel.setX(event.getStageX());
                    relaxLabel.setY(event.getStageY());
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
                world.nextTurn();
            }
        });
        return end;
    }

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
     * createToolbar creates a fixed toolbar with buttons linked to each of the player skills
     */
    public void createToolbar() {
        // Move to an outside class eventually
        ExtendViewport toolbarViewPort = new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        toolbarStage = new Stage(toolbarViewPort);
        skin = new Skin(Gdx.files.internal("skins/neon-ui-updated.json"));

        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(toolbarStage);
        inputMultiplexer.addProcessor(stage);

        Gdx.input.setInputProcessor(inputMultiplexer);

        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();



        TextureRegion tRegion = new TextureRegion(new Texture(Gdx.files.internal("skins/background.png")));
        TextureRegionDrawable drawable = new TextureRegionDrawable(tRegion);
        //toolbar.setBackground(drawable);


        createHarass();
        createExpose();
        createRelax();
        createOtherJobs();
        createOverwork();
        createThreaten();

        ImageButton end = createEndDay();
        ImageButton settings = createSettings();
        ImageButton notebook = createNotebook();

        Table toolbar = new Table();
        toolbar.bottom();
        toolbar.setSize(width, .25f*height);
        Table leftSide = new Table();
        Table skillBar = new Table();
        Table rightSide = new Table();

        leftSide.setSize(toolbar.getWidth()*.25f, toolbar.getHeight());
        skillBar.setSize(toolbar.getWidth()*.60f, toolbar.getHeight());
        rightSide.setSize(toolbar.getWidth()*.05f, toolbar.getHeight()/8);

        stressBar = new ProgressBar(0f, 100f, 1f, true, skin, "synthwave");
        stressBar.setValue(player.getStress());
        leftSide.add(stressBar).left().width(75).height(244);


        Stack bitecoinStack = new Stack();

        Image bitecoinCounter = new Image(new TextureRegionDrawable(new TextureRegion(
                new Texture("UI/BitecoinCounter.png"))));
        bitecoinAmount = new Label(Integer.toString((int)player.getBitecoin()), skin, "bitcoin");

        bitecoinStack.add(bitecoinCounter);
        bitecoinStack.add(bitecoinAmount);

        leftSide.add(bitecoinStack);


        int numSkills = 6+1;

        float pad = skillBar.getWidth() / 60f;
        System.out.println(pad);
        //skillBar.add(harass).width(skillBar.getWidth()/numSkills).height(skillBar.getHeight()).padRight(pad).align(Align.bottom);
        skillBar.add(threaten).width(skillBar.getWidth()/numSkills).height(skillBar.getHeight()).padRight(pad).align(Align.bottom);
        skillBar.add(expose).width(skillBar.getWidth()/numSkills).height(skillBar.getHeight()).padRight(pad).align(Align.bottom);
        skillBar.add(overwork).width(skillBar.getWidth()/numSkills).height(skillBar.getHeight()).padRight(pad).align(Align.bottom);
        skillBar.add(otherJobs).width(skillBar.getWidth()/numSkills).height(skillBar.getHeight()).padRight(pad).align(Align.bottom);
        skillBar.add(relax).width(skillBar.getWidth()/numSkills).height(skillBar.getHeight()).padRight(pad).align(Align.bottom);

        end.align(Align.bottomRight);
        rightSide.add(end).width(rightSide.getWidth()).height(/*rightSide.getHeight*/70f).align(Align.center);
        rightSide.row();
        rightSide.add(notebook).width(rightSide.getWidth()).height(/*rightSide.getHeight*/100f).align(Align.center);
        rightSide.row();
        rightSide.add(settings).width(rightSide.getWidth()).height(/*rightSide.getHeight*/100f).align(Align.center);

        toolbar.add(leftSide).left().width(.25f*toolbar.getWidth()).height(.10f*toolbar.getHeight()).align(Align.top);
        toolbar.add(skillBar).width(.6f*toolbar.getWidth()).height(.10f*toolbar.getWidth()).align(Align.bottom);
        toolbar.add(rightSide).right().width(.15f*toolbar.getWidth()).height(.10f*toolbar.getHeight()).align(Align.top);

        toolbarStage.addActor(toolbar);
        toolbarStage.addActor(createStats());
    }

    private Vector2 convertToIsometric(Vector2 worldCoords) {
        float oneOne = (float)Math.sqrt(3)/2;
        float oneTwo = (float)Math.sqrt(3)/2;
        float twoOne = (float)-1/2;
        float twoTwo = (float)1/2;
        Vector2 ans = new Vector2();
        ans.x = oneOne * worldCoords.x + oneTwo * worldCoords.y;
        ans.y = twoOne * worldCoords.x + twoTwo * worldCoords.y;

        return ans;
    }


    private Table createStats() {
        Table stats = new Table();
        stress = new Label("Player Stress: " + Integer.toString((int)(world.getPlayer().getStress())), skin);
        stress.setFontScale(2);
        ap = new Label("AP: " + Integer.toString(world.getPlayer().getAP()), skin);
        ap.setFontScale(2);
        tStress = new Label("Target Stress: " + Integer.toString(target.getStress()), skin);
        tStress.setFontScale(2);
        tSusp = new Label("Target Suspicion: " + Integer.toString(target.getSuspicion()), skin);
        tSusp.setFontScale(2);
        money = new Label ("Bitecoin: " + Integer.toString((int)world.getPlayer().getBitecoin()), skin);
        money.setFontScale(2);
        tState = new Label("Target State: " + target.getState(), skin);
        tState.setFontScale(2);

        stats.top();
        stats.setFillParent(true);

        //stats.add(stress).expandX().padTop(20);
        stats.add(tState).expandX().padTop(20);
        //stats.add(money).expandX().padTop(20);
        stats.add(tStress).expandX().padTop(20);
        stats.add(tSusp).expandX().padTop(20);
        stats.row();
        stats.add(ap).expandX().padTop(10);
        return stats;
    }


    /**
     * Moves the camera based on the Input Keys
     * Also allows for zooming + scales the movement and bounds based on zooming
     *
     */
    public void moveCamera() {
        camera = canvas.getCamera();
        currentZoom = camera.zoom;
        if(Gdx.input.isKeyPressed(Input.Keys.W)) {
            camera.translate(0, 12*currentZoom*cameraSpeed(0)/acceleration_speed);
        }if(Gdx.input.isKeyPressed(Input.Keys.A)) {
            camera.translate(-12*currentZoom*cameraSpeed(2)/acceleration_speed, 0);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.S)) {
            camera.translate(0, -12*currentZoom*cameraSpeed(1)/acceleration_speed);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.D)) {
            camera.translate(12*currentZoom*cameraSpeed(3)/acceleration_speed, 0);
        }

        if(Gdx.input.isKeyPressed(Input.Keys.E)) {
            camera.zoom = (.99f)*currentZoom;
        } if(Gdx.input.isKeyPressed(Input.Keys.Q)) {
            camera.zoom = (1.01f)*currentZoom;
        }

        if(camera.zoom > 4.0f) {
            camera.zoom = 4.0f;
        }
        if(camera.zoom < 1.0f) {
            camera.zoom = 1.0f;
        }

        float camX = camera.position.x;
        float camY = camera.position.y;

        Vector2 camMin = new Vector2(-1500f, -1500f);//(camera.viewportWidth/2, camera.viewportHeight/2);
        camMin.scl(camera.zoom/2); //bring to center and scale by the zoom level
        Vector2 camMax = new Vector2(1500f, 1500f);
        camMax.sub(camMin); //bring to center

        //keep camera within borders
        camX = Math.min(camMax.x, Math.max(camX, camMin.x));
        camY = Math.min(camMax.y, Math.max(camY, camMin.y));

        camera.position.set(camX, camY, camera.position.z);

        camera.update();
    }

    /**
     * Returns the camera speed given the direction, calculated with accumulated acceleration
     * @param direction
     */
    public float cameraSpeed(int direction){
        // 0 = up, 1 = down, 2 = left, 3 = right
        float speed = 0f;
        //acceleration_speed = 40;
        switch (direction){
            case 0:
                if (up_acc == 0) clearSpeedRev(0);
                up_acc += 1;
                speed = up_acc > acceleration_speed ? acceleration_speed : up_acc;
                break;
            case 1:
                if (down_acc == 0) clearSpeedRev(1);
                down_acc += 1;
                speed = down_acc > acceleration_speed ? acceleration_speed : down_acc;
                break;
            case 2:
                if (left_acc == 0) clearSpeedRev(2);
                left_acc += 1;
                speed = left_acc > acceleration_speed ? acceleration_speed : left_acc;
                break;
            case 3:
                if (right_acc == 0) clearSpeedRev(3);
                right_acc += 1;
                speed = right_acc > acceleration_speed ? acceleration_speed : right_acc;
                break;
        }
        return speed;
    }

    /**
     * Clears camera speed in all directions
     * @param
     */
    private void clearSpeed(){
        up_acc = 0;
        down_acc = 0;
        left_acc = 0;
        right_acc = 0;
        return;
    }

    /**
     * Clears camera speed in reverse directions
     * @param
     */
    private void clearSpeedRev(int direction){
        switch (direction){
            case 0:
                down_acc = 0;
            case 1:
                up_acc = 0;
            case 2:
                right_acc = 0;
            case 3:
                left_acc = 0;
        }
        return;
    }

    /**
     * Controls what actions the game needs to take on a specified node based on the
     * activeVerb that was clicked
     * @param nodeName
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
//                    world.hack(nodeInfo[0], nodeInfo[1]);
//                    world.scan(nodeInfo[0], nodeInfo[1]);
//                    createDialogBox(world.viewFact(nodeInfo[0], nodeInfo[1]));
                    switch (world.interactionType(nodeInfo[0], nodeInfo[1])) {
                        case HACK:
                            if(world.getPlayer().canHack()) {
                                button.setStyle(new ImageButton.ImageButtonStyle(null, null, null,
                                        new TextureRegionDrawable(new TextureRegion(new Texture(
                                                Gdx.files.internal("node/red.png")))), null, null));
                                createDialogBox(world.interact(nodeInfo[0], nodeInfo[1]));
                                reloadDisplayedNodes();
                            } else {
                                createDialogBox("Insufficient AP to hack this node.");
                            }
                            break;
                        case SCAN:
                            if(world.getPlayer().canScan()) {
                                button.setStyle(new ImageButton.ImageButtonStyle(null, null, null,
                                        new TextureRegionDrawable(new TextureRegion(new Texture(
                                                Gdx.files.internal("node/blue.png")))), null, null));
                                createDialogBox(world.interact(nodeInfo[0], nodeInfo[1]));
                                reloadDisplayedNodes();
                            } else {
                                createDialogBox("Insufficient AP to scan this node.");
                            }
                            break;
                        case VIEWFACT:
                            createDialogBox(world.interact(nodeInfo[0], nodeInfo[1]));
                            break;
                    }

                }
                break;
            case HARASS:
                if(isTarget) {
                    if(world.getPlayer().canHarass()) {
                        world.harass(target.getName());
                        createDialogBox("You harassed the target! They seem incredibly disturbed and got a bit stressed.");
                        activeVerb = ActiveVerb.NONE;
                    }
                    else {
                        createDialogBox("Insufficient AP to harass the target.");
                        activeVerb = ActiveVerb.NONE;
                    }
                }
                //activeVerb = ActiveVerb.NONE;
                break;
            case THREATEN:
                if(isTarget) {
                    if(world.getPlayer().canThreaten()) {
                        getBlackmailFact("Select a fact to threaten the target with.");
                    }
                    else {
                        createDialogBox("Insufficient AP to threaten the target.");
                        activeVerb = ActiveVerb.NONE;
                    }
                }
                //activeVerb = ActiveVerb.NONE;
                break;
            case EXPOSE:
                if(isTarget) {
                    if(isTarget) {
                        if(world.getPlayer().canExpose()) {
                            getBlackmailFact("Select a fact to expose the target with.");
                        }
                        else {
                            createDialogBox("Insufficient AP to expose the target.");
                            activeVerb = ActiveVerb.NONE;
                        }
                    }
                }
                //activeVerb = ActiveVerb.NONE;
                break;
            default:
                System.out.println("You shall not pass");
                break;
        }
    }

    /**
     * Creates a dialog box with [s] at a reasonably-sized height and width
     * @param s
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
     * Creates a dialog box with [s] at a reasonably-sized height and width
     * @param s
     */
    public void createNotebook(String s) {
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

        Map<String, String> factSummaries = world.viewFactSummaries(target.getName());
        Array<String> scannedFacts = new Array<>();

        Table table = dialog.getContentTable();
        if (factSummaries.keySet().size() == 0) {
            scannedFacts.add("No facts scanned yet!");
        }
        for (String fact_ : factSummaries.keySet()) {
            if (!world.viewFactSummary(target.getName(), fact_).equals(""))
                scannedFacts.add(world.viewFactSummary(target.getName(), fact_));
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

    /**
     *
     * @param s
     */
    public void getBlackmailFact(String s) {
        blackmailDialog = new Dialog("Notebook", skin) {
            public void result(Object obj) {
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
        if(s.length() > 50) {
            l.setFontScale(1.5f);
        }else {
            l.setFontScale(2f);
        }
        l.setWrap( true );
        blackmailDialog.setMovable(true);
        blackmailDialog.getContentTable().add( l ).prefWidth( 350 );
        Map<String, String> factSummaries = world.viewFactSummaries(target.getName());
        Map<String, String> summaryToFacts = new HashMap<String, String>();
        final Array<String> scannedFacts = new Array<>();

        Table table = blackmailDialog.getContentTable();
        if (factSummaries.keySet().size() == 0) {
            scannedFacts.add("No facts scanned yet!");
        }
        for (String fact_ : factSummaries.keySet()) {
            if(!world.viewFactSummary(target.getName(), fact_).equals(""))
                scannedFacts.add(world.viewFactSummary(target.getName(), fact_));
            summaryToFacts.put(world.viewFactSummary(target.getName(), fact_), fact_);
        }
        table.setFillParent(false);

        table.row();
        for (int i = 0; i < scannedFacts.size; i++) {
            final int temp_i = i;
            //this should ALWAYS be overwritten in the code underneath
            Label k = new Label("No facts", skin);
            if(activeVerb == ActiveVerb.EXPOSE ){
                if (exposedFacts.contains(scannedFacts.get(temp_i), false) ) {
                    continue;
                } else {
                    k = new Label(scannedFacts.get(i), skin);
                }
            } else if(activeVerb == ActiveVerb.THREATEN){
                if (threatenedFacts.contains(scannedFacts.get(temp_i), false) ) {
                    continue;
                } else {
                    k = new Label(scannedFacts.get(i), skin);
                }
            }
            k.setWrap(true);
            k.setName(target.getName() + "," + summaryToFacts.get(scannedFacts.get(i)));
            k.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Actor cbutton = (Actor)event.getListenerActor();
                    String[] info = cbutton.getName().split(",");
                    getRidOfBlackmail = true;
                    switch (activeVerb) {
                        case HARASS:

                        case THREATEN:
                            world.threaten(info[0], info[1]);
                            activeVerb = ActiveVerb.NONE;
                            createDialogBox("You threatened the target!");
                            threatenedFacts.add(scannedFacts.get(temp_i));
                            break;
                        case EXPOSE:
                            world.expose(info[0], info[1]);
                            activeVerb = ActiveVerb.NONE;
                            createDialogBox("You exposed the target!");
                            exposedFacts.add(scannedFacts.get(temp_i));
                            threatenedFacts.add(scannedFacts.get(temp_i));
                            break;
                        default:
                            System.out.println("This shouldn't be happening.");
                    }
                }
            });
            table.add(k).prefWidth(350);
            table.row();
        }

        blackmailDialog.button("Cancel", true); //sends "true" as the result
        blackmailDialog.key(Input.Keys.ENTER, true); //sends "true" when the ENTER key is pressed
        blackmailDialog.show(toolbarStage);
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
        Array<String> displayedNodes= world.getDisplayedNodes().get(target.getName());
        for(String str : displayedNodes) {
            stage.addActor(imageNodes.get(target.getName()+","+str));
        }

    }

    /**
     * Displays a dialog based on what active verb was clicked.
     * @param s
     */
    public void callConfirmFunction(String s) {
        switch(s) {
            case "overwork":
                if(world.getPlayer().canOverwork()) {
                    world.overwork();
                    createDialogBox("You overworked yourself and gained 2 AP at the cost of your sanity...");
                } else {
                    createDialogBox("You cannot overwork anymore today!");
                }
                break;
            case "relax":
                if(world.getPlayer().canRelax()) {
                    world.relax(1);
                    createDialogBox("You rested for 1 AP and decreased your stress!");
                } else {
                    createDialogBox("Insufficient AP to relax.");
                }
                break;
            case "otherJobs":
                if(world.getPlayer().canVtube()) {
                    world.vtube();
                    createDialogBox("You did some other jobs and earned some more bitecoin for yourself!");
                } else {
                    createDialogBox("Insufficient AP to do other jobs");
                }
                break;
            case "notebook":
                createNotebook("Notebook:");
                break;
            default:
                System.out.println("You shall not pass");
        }
    }

    /**
     * Updates the stats HUD with current values
     */
    public void updateStats(){
        stress.setText("Player Stress: " + Integer.toString((int)world.getPlayer().getStress()));
        stressBar.setValue(player.getStress());
        bitecoinAmount.setText(Integer.toString((int)player.getBitecoin()));
        ap.setText("AP: " + Integer.toString(world.getPlayer().getAP()));
        tStress.setText("Target Stress: " + Integer.toString(target.getStress()));
        tSusp.setText("Target Suspicion: " + Integer.toString(target.getSuspicion()));
        money.setText("Bitecoin: " + Integer.toString((int)world.getPlayer().getBitecoin()));
    }
}
