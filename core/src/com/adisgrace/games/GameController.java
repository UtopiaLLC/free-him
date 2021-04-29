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
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
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
        DISTRACT,
        GASLIGHT,
        /** Linked to relaxing to reduce stress*/
        RELAX,
        /** Linked to no action being selected */
        NONE
    };
    public static String getHoverText(ActiveVerb activeVerb){
        switch (activeVerb){
            //case HARASS: return "Harass: Harass your target to slightly increase their stress for 2 AP";
            case HARASS: return "Harass: Harass your target with a \n fact to blackmail to increase their stress " +
                    "for 2 AP";
            case EXPOSE: return "Expose: Expose your target's fact to the public\n for large stress damage" +
                    " for 3 AP";
            case OVERWORK: return "Overwork: Gains 2 AP, but Increases Stress";
            case OTHER_JOBS: return "Other Jobs: Make Money with 3 AP";
            case RELAX: return "Relax: Decreases Stress for 1 AP";
            case GASLIGHT: return "Gaslight: Plant seeds of doubt in your target's mind, making\n them less sure of themselves" +
                    " for 2 AP";
            case DISTRACT: return "Distract: For 2 AP, divert your target's attention, letting you work free of their interference.";
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
    /** apImages is the images for ap shown on the right toolbar*/
    private Image[] apImages;
    /** current amount of AP displayed*/
    private Table displayedAP;
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
    public static Array<String> exposedFacts;
    /** list of facts used to threaten the target*/
    public static Array<String> threatenedFacts;
    /** model for player stats and actions */
    //private PlayerModel player;
    /** flag for when game ended*/
    private boolean ended = false;
    /** flag for when all nodes need to not be clicked anymore*/
    public static boolean nodeFreeze = false;
    /** dialog box for blackmail commands*/
    public static Dialog blackmailDialog;
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

    private Array<TargetModel.TargetState> targetStates;

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

        skin = new Skin(Gdx.files.internal("skins/neon-ui-updated.json"));
        uiController = new UIController(skin);
        NodeView.loadAnimations();
        ic = new InputController();

        loadLevel(0);

        NorthConnector = new Texture(Gdx.files.internal(Connector.getAssetPath("N")));
        SouthConnector = new Texture(Gdx.files.internal(Connector.getAssetPath("S")));
        WestConnector = new Texture(Gdx.files.internal(Connector.getAssetPath("W")));
        EastConnector = new Texture(Gdx.files.internal(Connector.getAssetPath("E")));


        cameraController = new CameraController(ic, canvas);
        displayedAP = new Table();
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
        updateNodeColors();
        updateStats();

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

    public void updateNodeColors() {
        for(int i = 0; i < targets.size; i++) {
            if(targets.get(i).getState() != targetStates.get(i)) {
                TargetModel target = targets.get(i);
                TargetModel.TargetState state = target.getState();
                int colorState = getColorTypeFromState(state);

                for(String fact : target.getNodes()){
                    Node node = imageNodes.get(target.getName()+","+fact);
                    node.changeColor(colorState);
                }
                imageNodes.get(target.getName()).changeColor(colorState);
                targetStates.set(i, state);

                System.out.println("CHANGE STATE");
                System.out.println(state);

            }
        }
    }

    private int getColorTypeFromState(TargetModel.TargetState state ) {
        int colorState;
        switch (state) {
            case UNAWARE:
                colorState = 1;
                break;
            case SUSPICIOUS:
                colorState = 3;
                break;
            case PARANOID:
                colorState = 5;
                break;
            case THREATENED:
                colorState = 7;
                break;
            case DEFEATED:
                colorState = 11;
                break;
            default:
                colorState = 9;
                break;
        }
        return colorState;
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
            final String[] nodeInfo = b.getName().split(",");

            String s;
            if(nodeInfo.length == 1) {
                s =  "\nTarget Name: " + b.getName() + "\n" +
                        "Target Stress: " + levelController.getTargetStress(b.getName()) + "\n" +
                        "Target Suspicion: " + levelController.getTargetSuspicion(b.getName()) + "\n";
            } else {
                s = levelController.getTargetModels().get(nodeInfo[0]).getTitle(nodeInfo[1]);
            }
            final Label nodeLabel = uiController.createHoverLabel(s);

            nodeLabel.setFontScale(2);
            if(nodeInfo.length == 1) {
                Vector2 zeroLoc = new Vector2(Gdx.graphics.getWidth() * .02f, Gdx.graphics.getHeight() * .60f);
                nodeLabel.setX(zeroLoc.x);
                nodeLabel.setY(zeroLoc.y);
                nodeLabel.setWidth(450f);
                nodeLabel.setHeight(300f);
            } else {
                Vector2 zeroLoc = new Vector2(Gdx.graphics.getWidth() * .02f, Gdx.graphics.getHeight() * .85f);
                nodeLabel.setX(zeroLoc.x);
                nodeLabel.setY(zeroLoc.y);
                nodeLabel.setWidth(300f);
                nodeLabel.setHeight(100f);
            }
            b.addListener(ic.getButtonListener(
                    new Runnable() {
                        @Override
                        public void run() {
                            actOnNode(b.getName(), b);
                        }
                    }, new Runnable() {
                        @Override
                        public void run() {
                            uiController.nodeOnEnter(
                                    getColorTypeFromState(levelController.getTargetModels().get(nodeInfo[0]).getState()),
                                    nodeLabel, b);
                        }
                    },
                    new Runnable() {
                        @Override
                        public void run() {
                            uiController.nodeOnExit(
                                    getColorTypeFromState(levelController.getTargetModels().get(nodeInfo[0]).getState()),
                                    nodeLabel, b);
                        }
                    }));
            //Adds enter and exit listeners to each node button
            //b.addListener(ic.addNodeListenerEnterExit(skin, levelController));
            button.remove();
        }
    }

    /**
     * This method switches the level based on the number inputted
     * @param newLevel the level that the game needs to be switched to
     */
    public void loadLevel(int newLevel) {
        levelController = levelControllers.get(newLevel);

        stage.clear();
        targetStates = new Array<>();
        activeVerb = ActiveVerb.NONE;

        targets = new Array<>();
        for (TargetModel t: levelController.getTargetModels().values()){
            targets.add(t);
            targetStates.add(t.getState());
        }

        //instantiating target and expose lists
        threatenedFacts = new Array<>();
        exposedFacts = new Array<>();
        canvas.beginDebug();
        canvas.drawIsometricGrid(stage, nodeWorldWidth, nodeWorldHeight);
        canvas.endDebug();

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

        addNodeListeners(imageNodes);

        NorthConnector = new Texture(Gdx.files.internal(Connector.getAssetPath("N")));
        SouthConnector = new Texture(Gdx.files.internal(Connector.getAssetPath("S")));
        WestConnector = new Texture(Gdx.files.internal(Connector.getAssetPath("W")));
        EastConnector = new Texture(Gdx.files.internal(Connector.getAssetPath("E")));


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
     * This method creates an AP image which reflects how much AP the player has left
     * @return Array of images representing how much AP the player has
     */
    private Image[] createAP(){
        Texture apTexture = new Texture(Gdx.files.internal("UI/APCounter.png"));
        TextureRegion[][] apSplitTextures = new TextureRegion(apTexture).split(apTexture.getWidth()/9, apTexture.getHeight());
        Image[] ap = new Image[apSplitTextures[0].length];
        for(int i = 0; i < ap.length; i++){
            ap[i] = new Image(apSplitTextures[0][i]);
        }

        return ap;
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
        uiController.createHarass(ic, createConfirmRunnable("harass"));
        uiController.createDistract(ic, createConfirmRunnable("distract"));
        uiController.createGaslight(ic, createConfirmRunnable("gaslight"));

        ImageButton end = createEndDay();
        ImageButton settings = createSettings();
        ImageButton notebook = createNotebook();
        apImages = createAP();

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
        displayedAP.setSize(toolbar.getWidth()*.05f, toolbar.getHeight()/8);
        displayedAP.add(apImages[levelController.getAP()]).width(displayedAP.getWidth()).height(/*rightSide.getHeight*/55f).align(Align.center);

        toolbar.add(leftSide).left().width(.25f*toolbar.getWidth()).height(.10f*toolbar.getHeight()).align(Align.top);
        toolbar.add(skillBar).width(.6f*toolbar.getWidth()).height(.10f*toolbar.getWidth()).align(Align.bottom);
        toolbar.add(displayedAP).right().width(.15f*toolbar.getWidth()).height(.20f*toolbar.getHeight()).align(Align.top);
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
                if(isTarget) {
                    if(levelController.getAP() >= PlayerModel.THREATEN_AP_COST) {
                        uiController.getBlackmailFact("Select a fact to threaten the target with.", nodeInfo[0],
                                levelController);
                    }
                    else {
                        uiController.createDialogBox("Insufficient AP to threaten the target.");
                        activeVerb = ActiveVerb.NONE;
                    }
                }
                break;
            case THREATEN:
                break;
            case EXPOSE:
                if(isTarget) {
                    if(isTarget) {
                        if(levelController.getAP() >= PlayerModel.EXPOSE_AP_COST) {
                            uiController.getBlackmailFact("Select a fact to expose the target with.", nodeInfo[0],
                                    levelController);
                        }
                        else {
                            uiController.createDialogBox("Insufficient AP to expose the target.");
                            activeVerb = ActiveVerb.NONE;
                        }
                    }
                }
                break;
            case GASLIGHT:
                if (isTarget) {
                    if(levelController.getAP() >= PlayerModel.GASLIGHT_AP_COST) {
                        if(levelController.gaslight(nodeInfo[0]))
                            uiController.createDialogBox("You manage to convince them that you're a figment of their imagination.");
                        else
                            uiController.createDialogBox("You fail to gaslight them, and only further arouse their suspicions.");
                    }
                    else {
                        uiController.createDialogBox("Insufficient AP to gaslight the target.");
                        activeVerb = ActiveVerb.NONE;
                    }
                }
            case DISTRACT:
                if(isTarget){
                    if(levelController.getAP() >= PlayerModel.DISTRACT_AP_COST){
                        if(levelController.distract(nodeInfo[0])){
                            uiController.createDialogBox("You manage to distract your target. They won't have time to deal with you for a while.");
                        }else{
                            uiController.createDialogBox("You fail to distract them, and only further arouse their suspicions.");
                        }
                    }else{
                        uiController.createDialogBox("Insufficient AP to distract the target.");
                        activeVerb = ActiveVerb.NONE;
                    }
                }
                break;
            default:
                System.out.println("You shall not pass");
                break;
        }
    }

    public void addConnections(String target, String fact){
        ArrayMap<String, Array<Connector>> connectors = levelController.getConnectorsOf(target, fact);
        //Vector2 connectorCoords = new Vector2();
        Vector2 targetCoords = levelController.getTargetPos(target);
        for(int i = 0; i < connectors.size; i++){
            Array<Connector> firstConnectors = connectors.getValueAt(i);
            //draw each individual connector on the path
            for(Connector connector : firstConnectors) {
                Vector2 connectorCoords = new Vector2();
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
                uiController.createNotebookTargetSelector("Select a target to view facts for.", targets, levelController);
                break;
            case "distract":
                break;
            default:
                System.out.println("You shall not pass");
        }
    }

    /**
     * Updates the stats HUD with current values
     */
    public void updateStats(){
        stressBar.setValue(levelController.getPlayerStress());
        bitecoinAmount.setText(Integer.toString((int)levelController.getPlayerCurrency()));
        ap.setText("AP: " + Integer.toString(levelController.getAP()));
        displayedAP.reset();
        displayedAP.add(apImages[levelController.getAP()]).width(displayedAP.getWidth()).height(/*rightSide.getHeight*/70f).align(Align.center);
    }
}
