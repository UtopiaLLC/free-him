package com.adisgrace.games;

import com.adisgrace.games.models.*;
import com.adisgrace.games.util.Connector;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
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
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

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
    public static String getHoverText(ActiveVerb activeVerb){
        switch (activeVerb){
            case HARASS: return "Harass: Harass your target to slightly increase their stress for 2 AP";
            case THREATEN: return "Threaten: Threaten your target with a \n fact to blackmail to increase their stress " +
                    "for 2 AP";
            case EXPOSE: return "Expose: Expose your target's fact to the public\n for large stress damage" +
                    " for 3 AP";
            case OVERWORK: return "Overwork: Gains 2 AP, but Increases Stress";
            case OTHER_JOBS: return "Other Jobs: Make Money with 3 AP";
            case RELAX: return "Relax: Decreases Stress with 1 AP";
            default: throw new RuntimeException("Invalid ActiveVerb passed " + activeVerb.toString());
        }
    };
    public static String getName(ActiveVerb activeVerb){
        switch (activeVerb){
            case HARASS: return "Harass";
            case THREATEN: return "Threaten";
            case EXPOSE: return "Expose";
            case OVERWORK: return "Overwork";
            case OTHER_JOBS: return "Other Jobs";
            case RELAX: return "Relax";
            default: return "None";
        }
    }

    private Array<String> levelJsons;
    private Array<LevelController> levelControllers;

    private final int THREATEN_AP_COST = 2;
    private final int EXPOSE_AP_COST = 3;

    /** canvas is the primary view class of the game */
    private GameCanvas canvas;
    /** stage is a Scene2d scene graph that contains all hierarchies of Scene2d Actors */
    public static Stage stage;
    /** stage is a Scene2d scene graph that contains all hierarchies of Scene2d Actors specifically for the toolbar and
     * the HUD.
     */
    public static Stage toolbarStage;
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
    public static ActiveVerb activeVerb;
    /** hoverVerb is the verb that is currently being hovered over by the cursor */
    private ActiveVerb hoverVerb;
    /** nodeView is the view class that exposes all nodes in the map */
    private NodeView nodeView;
    /** imageNodes contains all ImageButtons for each fact node and target node */
    private Map<String, Node> imageNodes;
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
    /** model for player stats and actions */
    //private PlayerModel player;
    /** flag for when game ended*/
    private boolean ended = false;
    /** flag for when all nodes need to not be clicked anymore*/
    public static boolean nodeFreeze = false;
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

    private UIController uiController;
    /** controller for input operations*/
    private InputController ic;

    private Music music;

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

    private Array<Connector> visibleConnectors;

    private Texture NorthConnector;
    private Texture SouthConnector;
    private Texture EastConnector;
    private Texture WestConnector;

    private Animation<TextureRegion> NorthConnectorAnimation;
    private Animation<TextureRegion> SouthConnectorAnimation;
    private Animation<TextureRegion> EastConnectorAnimation;
    private Animation<TextureRegion> WestConnectorAnimation;

    //private Image north, east, south, west;


    public GameController() {
        canvas = new GameCanvas();
        ExtendViewport viewport = new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        viewport.setCamera(canvas.getCamera());
        currentZoom = canvas.getCamera().zoom;
        stage = new Stage(viewport);
        canvas.getCamera().zoom = 1.5f;

        //TODO: write function to parse folder of level jsons
        levelJsons = new Array<>();
        levelJsons.add("sample-level-1.json");
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

        skin = new Skin(Gdx.files.internal("skins/neon-ui-updated.json"));
        uiController = new UIController(skin);

        // Creating Nodes
        imageNodes = new HashMap<>();

        NodeView.loadAnimations();
        for (TargetModel target: targets) {
            Vector2 targetCoords = levelController.getTargetPos(target.getName());
            Array<String> targetNodes = target.getNodes();
            Array<Boolean> lockedNodes = new Array<>();
            for (String nodeName: targetNodes ){
                lockedNodes.add(levelController.getLocked(target.getName(), nodeName));
            }
            nodeView = new NodeView(stage, target, targetNodes, targetCoords, lockedNodes);
            imageNodes.putAll(nodeView.getImageNodes());
        }

        ic = new InputController();
        addNodeListeners(imageNodes);



        NorthConnector = new Texture(Gdx.files.internal(Connector.getAssetPath("N")));
        SouthConnector = new Texture(Gdx.files.internal(Connector.getAssetPath("S")));
        WestConnector = new Texture(Gdx.files.internal(Connector.getAssetPath("W")));
        EastConnector = new Texture(Gdx.files.internal(Connector.getAssetPath("E")));

//        north = new Image(NorthConnector);
//        south = new Image(SouthConnector);
//        west = new Image(WestConnector);
//        east = new Image(EastConnector);


        //visibleConnectors = levelController.getAllVisibleConnectors();

        //This draws all the primary connections that are visible at the beginning of the game
        Vector2 connectorCoords = new Vector2();
        for(TargetModel target: targets){
            Vector2 targetCoords = levelController.getTargetPos(target.getName());
            ArrayMap<String, Array<Connector>> firstConnections = levelController.getConnectorsOf(target.getName());

            //for each target, extract the path from target to each individual node
            for(int i = 0; i < firstConnections.size; i++){
                Array<Connector> firstConnectors = firstConnections.getValueAt(i);
                //draw each individual connector on the path
                for(Connector connector : firstConnectors) {
                    connectorCoords.set(connector.xcoord, connector.ycoord);
                    connectorCoords.add(targetCoords);
                    connectorCoords = isometricToWorld(connectorCoords);
                    if(connector.type.contains("E")) {
                        Image east = new Image(EastConnector);
                        east.setPosition(connectorCoords.x, connectorCoords.y);
                        stage.addActor(east);
                    }if(connector.type.contains("W")) {
                        Image west = new Image(WestConnector);
                        west.setPosition(connectorCoords.x, connectorCoords.y);
                        stage.addActor(west);
                    }
                    if(connector.type.contains("N")) {
                        Image north = new Image(NorthConnector);
                        north.setPosition(connectorCoords.x, connectorCoords.y);
                        stage.addActor(north);
                    }
                    if(connector.type.contains("S")) {
                        Image south = new Image(SouthConnector);
                        south.setPosition(connectorCoords.x, connectorCoords.y);
                        stage.addActor(south);
                    }
                }

                String fact = firstConnections.getKeyAt(i);
                stage.addActor(imageNodes.get(target.getName()+","+fact));
            }
            stage.addActor(imageNodes.get(target.getName()));
        }

        cameraController = new CameraController(ic, canvas);
        createToolbar();
        shapeRenderer = new ShapeRenderer();

        music = Gdx.audio.newMusic(Gdx.files.internal("music/Moonlit_Skyline.mp3"));
        music.setVolume(0.1f);
        music.setLooping(true);
        music.play();

        NorthConnectorAnimation = connectorAnimation(NorthConnector);
        SouthConnectorAnimation = connectorAnimation(SouthConnector);
        EastConnectorAnimation = connectorAnimation(EastConnector);
        WestConnectorAnimation = connectorAnimation(WestConnector);

        ConnectorActor connector = new ConnectorActor(SouthConnectorAnimation,
                isometricToWorld(new Vector2(5f, 5f)));
        stage.addActor(connector);

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

        // If no action is currently selected, and the cursor is not hovering above any button, then remove any effects
        if (activeVerb == ActiveVerb.NONE && hoverVerb == ActiveVerb.NONE){
            uiController.unCheck();
        }

        if(!ended) {
            cameraController.moveCamera();
            toolbarStage.act(delta);
            if(!nodeFreeze) {
                stage.act(delta);
            }
        }

        canvas.drawIsometricGrid(stage,nodeWorldWidth,nodeWorldHeight);
        stage.getViewport().apply();
        stage.draw();
        toolbarStage.getViewport().apply();
        toolbarStage.draw();

        if(levelController.getLevelState() == LevelModel.LevelState.LOSE && !ended) {
            uiController.createDialogBox("YOU LOSE!");
            ended = true;
            //switchLevel(0);

        } else if (levelController.getLevelState() == LevelModel.LevelState.WIN && !ended) {
            uiController.createDialogBox("You Win!");
            ended = true;
            //switchLevel(currentLevel+1);
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

    /**
     * Delegates responsibility of adding click listeners to all
     * the nodes to the InputController
     * @param imageNodes map from node id to node
     */
    private void addNodeListeners(Map<String,Node> imageNodes) {
        for(final Node button : imageNodes.values()) { // Node Click Listeners
            final Node b = button;
            //Adds click listener to each node button
            b.addListener(ic.getButtonListener(new Runnable() {
                @Override
                public void run() {
                    actOnNode(b.getName(), b);
                }
            }));
            //Adds enter and exit listeners to each node button
            b.addListener(ic.addNodeListenerEnterExit(skin, levelController));
            button.remove();
        }
    }

    /**
     * This method switches the level based on the number inputted
     * @param newLevel the level that the game needs to be switched to
     */
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
            Vector2 targetCoords = levelController.getTargetPos(target.getName());
            Array<String> targetNodes = target.getNodes();
            Array<Boolean> lockedNodes = new Array<>();
            for (String nodeName: targetNodes ){
                lockedNodes.add(levelController.getLocked(target.getName(), nodeName));
            }
            nodeView = new NodeView(stage, target, targetNodes, targetCoords, lockedNodes);
            imageNodes.putAll(nodeView.getImageNodes());
        }

        for(Node button : imageNodes.values()) { // Node Click Listeners
            final Node b = button;
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
//        for (TargetModel target: targets) {
//            Array<String> displayedNodes= levelController.getVisibleNodes(target.getName());
//            for(String str : displayedNodes) {
//                stage.addActor(imageNodes.get(target.getName()+","+str));
//            }
//            stage.addActor(imageNodes.get(target.getName()));
//        }

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
                uiController.createDialogBox("You end the day after a long battle of psychological warfare.");
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
                uiController.createDialogBox("You clicked something that hasn't been implemented yet.");
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
                uiController.confirmDialog("Are you sure you want to open notebook?", new Runnable(){
                    @Override
                    public void run() {
                        callConfirmFunction(s);
                    }
                });
            }
        });
        return notebook;
    }

    /**
     * Creates a runnable that runs the callConfirmFunction method
     * @param s
     * @return the runnable associated with callConfirmFunction
     */
    private Runnable createConfirmRunnable(final String s) {
        return new Runnable(){
            @Override
            public void run() {
                callConfirmFunction(s);
            }
        };
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

        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(toolbarStage);
        inputMultiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(inputMultiplexer);

        stressBar = new ProgressBar(0f, 100f, 1f, true, skin, "synthwave");
        stressBar.setValue(levelController.getPlayerStress());

        uiController.createExpose(ic, createConfirmRunnable("expose"));
        uiController.createRelax(ic, createConfirmRunnable("relax"));
        uiController.createOtherJobs(ic, createConfirmRunnable("other jobs"));
        uiController.createOverwork(ic, createConfirmRunnable("overwork"));
        uiController.createThreaten(ic, createConfirmRunnable("threaten"));
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
        Table skillBar = uiController.createSkillBarTable(toolbar);
        Table rightSide = createRightsideTable(toolbar, end, notebook, settings);

        toolbar.add(leftSide).left().width(.25f*toolbar.getWidth()).height(.10f*toolbar.getHeight()).align(Align.top);
        toolbar.add(skillBar).width(.6f*toolbar.getWidth()).height(.10f*toolbar.getWidth()).align(Align.bottom);
        toolbar.add(rightSide).right().width(.15f*toolbar.getWidth()).height(.10f*toolbar.getHeight()).align(Align.top);
        return toolbar;
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
     * Helper function that converts coordinates from isometric space to world space.
     *
     * @param coords   Coordinates in isometric space to transform
     */
    private Vector2 isometricToWorld(Vector2 coords) {
        float tempx = coords.x;
        float tempy = coords.y;
        coords.x = tempx * (0.5f * TILE_WIDTH) + tempy * (0.5f * TILE_WIDTH);
        coords.y = -tempx * (0.5f * TILE_HEIGHT) + tempy * (0.5f * TILE_HEIGHT);

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

        stats.top();
        stats.setFillParent(true);

        stats.add(ap).expandX().padTop(10);
        return stats;
    }

    /**
     * Controls what actions the game needs to take on a specified node based on the
     * activeVerb that was clicked
     * @param nodeName name of target and fact in the form "target_name,fact_id"
     * @param button the node button
     */
    public void actOnNode(String nodeName, Node button) {
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
                            if(hack == -1 || hack == -2) {
                                System.out.println("HACK IS NOT WORKING: " + hack);
                                System.exit(1);
                            }
                            if(hack == 1) {

                                button.changeState(Node.NodeState.UNSCANNED);

                                uiController.createDialogBox("You hacked the node successfully!");


                            } else if(hack == -3) {
                                uiController.createDialogBox("Insufficient AP to hack this node.");
                            } else if(hack == -4) {
                                uiController.createDialogBox("You failed to hack the node!");
                            }
                            break;
                        case 2://scannable
                            boolean success = levelController.scan(nodeInfo[0], nodeInfo[1]);
                            if(success) {

                                button.changeState(Node.NodeState.SCANNED);


                                addConnections(nodeInfo[0], nodeInfo[1]);
                                uiController.createDialogBox(levelController.viewFact(nodeInfo[0], nodeInfo[1]));

                            } else {
                                uiController.createDialogBox("Insufficient AP to scan this node.");
                            }
                            break;
                        case 1://viewable
                            uiController.createDialogBox(levelController.viewFact(nodeInfo[0], nodeInfo[1]));
                            break;
                    }

                }
                break;
            case HARASS:

                break;
            case THREATEN:
                if(isTarget) {
                    if(levelController.getAP() >= THREATEN_AP_COST) {
                        getBlackmailFact("Select a fact to threaten the target with.", nodeInfo[0]);
                    }
                    else {
                        uiController.createDialogBox("Insufficient AP to threaten the target.");
                        activeVerb = ActiveVerb.NONE;
                    }

//                    if(levelController.getTargetStress(nodeInfo[0]) >=40) {
//                        Texture target_Look = new Texture("node/N_TargetMale_1.png");
//                        TextureRegion[][] regions = new TextureRegion(target_Look).split(
//                                target_Look.getWidth() / 6,
//                                target_Look.getHeight() / 2);
//                        TextureRegion tRegion = regions[3][0];
//
//                        Texture node_base = new Texture("node/N_TargetBase_1.png");
//                        TextureRegion[][] node_regions = new TextureRegion(node_base).split(
//                                node_base.getWidth() / 6,
//                                node_base.getHeight() / 2);
//
//                        Texture combined = GameCanvas.combineTextures(tRegion, node_regions[3][0]);
//
//                        TextureRegionDrawable drawable = new TextureRegionDrawable(new TextureRegion(combined));
//                        button.setStyle(new ImageButton.ImageButtonStyle(null, null, null,
//                                drawable, null, null));
//                    }
                }
                break;
            case EXPOSE:
                if(isTarget) {
                    if(isTarget) {
                        if(levelController.getAP() >= EXPOSE_AP_COST) {
                            getBlackmailFact("Select a fact to expose the target with.", nodeInfo[0]);
                        }
                        else {
                            uiController.createDialogBox("Insufficient AP to expose the target.");
                            activeVerb = ActiveVerb.NONE;
                        }
//                        if(levelController.getTargetStress(nodeInfo[0]) >=40) {
//                            Texture target_Look = new Texture("node/N_TargetMale_1.png");
//                            TextureRegion[][] regions = new TextureRegion(target_Look).split(
//                                    target_Look.getWidth() / 6,
//                                    target_Look.getHeight() / 2);
//                            TextureRegion tRegion = regions[3][0];
//
//                            Texture node_base = new Texture("node/N_TargetBase_1.png");
//                            TextureRegion[][] node_regions = new TextureRegion(node_base).split(
//                                    node_base.getWidth() / 6,
//                                    node_base.getHeight() / 2);
//
//                            Texture combined = GameCanvas.combineTextures(tRegion, node_regions[3][0]);
//
//                            TextureRegionDrawable drawable = new TextureRegionDrawable(new TextureRegion(combined));
//                            button.setStyle(new ImageButton.ImageButtonStyle(null, null, null,
//                                    drawable, null, null));
//                        }
                    }
                }
                break;
            default:
                System.out.println("You shall not pass");
                break;
        }
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
            if(factSummaries.keySet().size() != 0) {
                k.setWrap(true);
                //Add a listener that can be reachable via the name format "target_name,fact_id"
                k.setName(targetName + "," + summaryToFacts.get(scannedFacts.get(i)));
                k.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        Actor cbutton = (Actor) event.getListenerActor();
                        String[] info = cbutton.getName().split(",");
                        getRidOfBlackmail = true;
                        switch (activeVerb) {
                            case HARASS:

                            case THREATEN:
                                //Threaten the target
                                levelController.threaten(info[0], info[1]);
                                activeVerb = ActiveVerb.NONE;
                                uiController.createDialogBox("You threatened the target!");
                                //Add this fact to the list of facts used to threaten
                                threatenedFacts.add(scannedFacts.get(temp_i));
                                break;
                            case EXPOSE:
                                //Expose the target
                                levelController.expose(info[0], info[1]);
                                activeVerb = ActiveVerb.NONE;
                                uiController.createDialogBox("You exposed the target!");
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
            }

            table.add(k).prefWidth(350);
            table.row();
        }

        blackmailDialog.button("Cancel", true); //sends "true" as the result
        blackmailDialog.key(Input.Keys.ENTER, true); //sends "true" when the ENTER key is pressed
        blackmailDialog.show(toolbarStage);
        //Make sure nothing else is able to be clicked while blackmail dialog is shown
        nodeFreeze = true;
    }

    public void addConnections(String target, String fact){
        ArrayMap<String, Array<Connector>> connectors = levelController.getConnectorsOf(target, fact);
        Vector2 connectorCoords = new Vector2();
        Vector2 targetCoords = levelController.getTargetPos(target);
        for(int i = 0; i < connectors.size; i++){
            Array<Connector> firstConnectors = connectors.getValueAt(i);
            //draw each individual connector on the path
            for(Connector connector : firstConnectors) {
                connectorCoords.set(connector.xcoord, connector.ycoord).add(targetCoords);

                connectorCoords = isometricToWorld(connectorCoords);
                if(connector.type.contains("E")) {
                    System.out.println("East");
                    ConnectorActor east = new ConnectorActor(EastConnectorAnimation, connectorCoords);
                    east.setPosition(connectorCoords.x, connectorCoords.y);
                    stage.addActor(east);
                }if(connector.type.contains("W")) {
                    System.out.println("West");
                    ConnectorActor west = new ConnectorActor(WestConnectorAnimation, connectorCoords);
                    west.setPosition(connectorCoords.x, connectorCoords.y);
                    stage.addActor(west);
                }
                if(connector.type.contains("N")) {
                    System.out.println("North");
                    ConnectorActor north = new ConnectorActor(NorthConnectorAnimation, connectorCoords);
                    north.setPosition(connectorCoords.x, connectorCoords.y);
                    stage.addActor(north);
                }
                if(connector.type.contains("S")) {
                    System.out.println("South");
                    ConnectorActor south = new ConnectorActor(SouthConnectorAnimation, connectorCoords);
                    south.setPosition(connectorCoords.x, connectorCoords.y);
                    stage.addActor(south);
                }

                System.out.println("-------------");

            }

            String newFact = connectors.getKeyAt(i);
            stage.addActor(imageNodes.get(target+","+newFact));
        }
    }

    public Animation<TextureRegion> connectorAnimation(Texture tex) {

        TextureRegion[] connectorFrames = new TextureRegion[50];
        int counter = 0;

        for(int i = 2; i <= 100; i+=2) {

            float percentile = i/100f;
            float coordScaler = (1 - percentile) / 2;

            connectorFrames[counter] = new TextureRegion(tex,
                    (int)(tex.getWidth() * coordScaler),
                    (int)(tex.getHeight() * coordScaler),
                    (int)(tex.getWidth() * percentile),
                    (int)(tex.getHeight() * percentile));

            counter++;
        }

        Animation<TextureRegion> edgeAnimation = new Animation<TextureRegion>(0.025f, connectorFrames);
        edgeAnimation.setPlayMode(Animation.PlayMode.NORMAL);
        return edgeAnimation;
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
                    uiController.createDialogBox("You overworked yourself and gained 2 AP at the cost of your sanity...");
                } else {
                    uiController.createDialogBox("You cannot overwork anymore today!");
                }
                break;
            case "relax":
                success = levelController.relax();

                if(success) {
                    uiController.createDialogBox("You relaxed for 1 AP and decreased your stress!");
                } else {
                    uiController.createDialogBox("Insufficient AP to relax.");
                }
                break;
            case "other jobs":
            case "otherJobs":
                float money = levelController.otherJobs();
                if(money != -1f) {

                    uiController.createDialogBox("You did some other jobs and earned some " + Float.toString(money) +  " bitecoin for yourself!");
                } else {
                    uiController.createDialogBox("Insufficient AP to do other jobs");
                }
                break;
            case "notebook":
                //createNotebook("Notebook:");
                uiController.createNotebookTargetSelector("Select a target to view facts for.", targets, levelController);
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
