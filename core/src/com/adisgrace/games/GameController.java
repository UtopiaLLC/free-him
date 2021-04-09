package com.adisgrace.games;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSorter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;

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

    private boolean ended = false;
    private boolean nodeFreeze = false;

    private Dialog blackmailDialog;
    private boolean getRidOfBlackmail;

    private static final float MINWORLDWIDTH = 300; //1280
    private static final float MINWORLDHEIGHT = 400; //720

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
        activeVerb = ActiveVerb.NONE;

        // Setting a target
        target = world.getTarget("Patrick Westfield");

        // Creating Nodes
        nodeView = new NodeView(stage, target, world);
        imageNodes = nodeView.getImageNodes();
        for(ImageButton button : imageNodes.values()) { // Node Click Listeners
            button.addListener(new ClickListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Actor cbutton = (Actor)event.getListenerActor();
                    //System.out.println(cbutton.getName());
                    actOnNode(cbutton.getName());
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
//        toolbarStage.setViewport(new ExtendViewport(width, height));
//        stage.setViewport(new ExtendViewport(width, height));
        toolbarStage.getViewport().update(width,height);
        stage.getViewport().update(width,height, true);

    }

//    public void resize (int width, int height) {
//        Vector2 size = Scaling.fit.apply(300, 400, width, height);
//        int viewportX = (int)(width - size.x) / 2;
//        int viewportY = (int)(height - size.y) / 2;
//        int viewportWidth = (int)size.x;
//        int viewportHeight = (int)size.y;
//        Gdx.gl.glViewport(viewportX, viewportY, viewportWidth, viewportHeight);
//        ExtendViewport view = new ExtendViewport(800, 480, true, viewportX, viewportY, viewportWidth, viewportHeight);
//        stage.setViewport();
//    }

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
     * createToolbar creates a fixed toolbar with buttons linked to each of the player skills
     */
    public void createToolbar() {
        // Move to an outside class eventually
        ExtendViewport toolbarViewPort = new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        toolbarStage = new Stage(toolbarViewPort);
        skin = new Skin(Gdx.files.internal("skins/neon-ui.json"));

        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(toolbarStage);
        inputMultiplexer.addProcessor(stage);

        Gdx.input.setInputProcessor(inputMultiplexer);

        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();




        TextureRegion tRegion = new TextureRegion(new Texture(Gdx.files.internal("skins/background.png")));
        TextureRegionDrawable drawable = new TextureRegionDrawable(tRegion);
        //toolbar.setBackground(drawable);


        ImageButton harass = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/overwork.png")))));
        harass.setTransform(true);
        harass.setScale(.25f);
        harass.addListener(new ClickListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                activeVerb = ActiveVerb.HARASS;
            }
        });
        ImageButton threaten = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/overwork.png")))));
        threaten.setTransform(true);
        threaten.setScale(.25f);
        threaten.addListener(new ClickListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {

                activeVerb = ActiveVerb.THREATEN;

            }
        });
        ImageButton expose = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/overwork.png")))));
        expose.setTransform(true);
        expose.setScale(.25f);
        expose.addListener(new ClickListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                activeVerb = ActiveVerb.EXPOSE;
            }
        });
        ImageButton overwork = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/overwork.png")))));
        overwork.setTransform(true);
        overwork.setScale(.25f);
        overwork.addListener(new ClickListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                final String s = "overwork";
                confirmDialog("Are you sure you want to overwork?", s);
            }
        });
        ImageButton otherJobs = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/overwork.png")))));
        otherJobs.setTransform(true);
        otherJobs.setScale(.25f);
        otherJobs.addListener(new ClickListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                final String s = "otherJobs";
                confirmDialog("Are you sure you want to do other jobs?", s);
            }
        });
        ImageButton relax = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/overwork.png")))));
        relax.setTransform(true);
        relax.setScale(.25f);
        relax.addListener(new ClickListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                final String s = "relax";
                confirmDialog("Are you sure you want to relax?", s);
            }
        });
        ImageButton end = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/overwork.png")))));
        end.setTransform(true);
        end.setScale(.5f);
        end.addListener(new ClickListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                createDialogBox("You end the day after a long battle of psychological warfare.");
                world.nextTurn();
            }
        });
        ImageButton settings = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/overwork.png")))));
        settings.setTransform(true);
        settings.setScale(.5f);
        settings.addListener(new ClickListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                createDialogBox("You clicked something that hasn't been implemented yet.");
            }
        });
        ImageButton notebook = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(
                Gdx.files.internal("skills/overwork.png")))));
        notebook.setTransform(true);
        notebook.setScale(.5f);
        notebook.addListener(new ClickListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                final String s = "notebook";
                confirmDialog("Are you sure you want to open notebook?", s);
            }
        });


        Table toolbar = new Table();
        toolbar.bottom();
        toolbar.setSize(width, .25f*height);
        Table leftSide = new Table();
        Table skillBar = new Table();
        Table rightSide = new Table();

        leftSide.setSize(toolbar.getWidth()*.25f, toolbar.getHeight());
        skillBar.setSize(toolbar.getWidth()*.60f, toolbar.getHeight());
        rightSide.setSize(toolbar.getWidth()*.05f, toolbar.getHeight()/8);

        ProgressBar stressBar = new ProgressBar(0f, 100f, 1f, true, skin);
        stressBar.setValue(50);
        leftSide.add(stressBar).left();

        int numSkills = 6;

        skillBar.add(harass).width(skillBar.getWidth()/numSkills);
        skillBar.add(threaten).width(skillBar.getWidth()/numSkills);
        skillBar.add(expose).width(skillBar.getWidth()/numSkills);
        skillBar.add(overwork).width(skillBar.getWidth()/numSkills);
        skillBar.add(otherJobs).width(skillBar.getWidth()/numSkills);
        skillBar.add(relax).width(skillBar.getWidth()/numSkills);

        end.align(Align.bottomRight);
        rightSide.add(end).width(rightSide.getWidth());
//        rightSide.row();
        rightSide.add(notebook).width(rightSide.getWidth());
//        rightSide.row();
        rightSide.add(settings).width(rightSide.getWidth());

        toolbar.add(leftSide).left().width(.25f*toolbar.getWidth());
        toolbar.add(skillBar).width(.6f*toolbar.getWidth());
        toolbar.add(rightSide).right().width(.15f*toolbar.getWidth());
        rightSide.debug();

        toolbarStage.addActor(toolbar);
        toolbarStage.addActor(createStats());
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

        stats.top();
        stats.setFillParent(true);

        stats.add(stress).expandX().padTop(20);
        stats.add(ap).expandX().padTop(20);
        stats.add(money).expandX().padTop(20);
        stats.row();
        stats.add(tStress).expandX().padTop(10);
        stats.add(tSusp).expandX().padTop(10);
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
            camera.translate(0, 12*currentZoom);
        }if(Gdx.input.isKeyPressed(Input.Keys.A)) {
            camera.translate(-12*currentZoom, 0);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.S)) {
            camera.translate(0, -12*currentZoom);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.D)) {
            camera.translate(12*currentZoom, 0);
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
     * Controls what actions the game needs to take on a specified node based on the
     * activeVerb that was clicked
     * @param nodeName
     */
    public void actOnNode(String nodeName) {
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
                                createDialogBox(world.interact(nodeInfo[0], nodeInfo[1]));
                                reloadDisplayedNodes();
                            } else {
                                createDialogBox("Insufficient AP to hack this node.");
                            }
                            break;
                        case SCAN:
                            if(world.getPlayer().canScan()) {
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
                        createDialogBox("Harass isn't implemented yet... thank Brian :)");
                        activeVerb = ActiveVerb.NONE;
                    }
                    else {
                        createDialogBox("Insufficient AP to harass the target.");
                        activeVerb = ActiveVerb.NONE;
                    }
                }
                break;
            case THREATEN:
                if(isTarget) {
                    if(world.getPlayer().canThreaten()) {
                        getBlackmailFact("Select a fact to threaten the player with.");
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
                        if(world.getPlayer().canExpose()) {
                            getBlackmailFact("Select a fact to expose the player with.");
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
//        table.align(Align.topLeft);
//        ScrollPane sp = new ScrollPane(table);
//        sp.setScrollingDisabled(true, false);
//        sp.setOverscroll(false, false);
//        sp.setFillParent(true);
//        dialog.addActor(sp);

//        dialog.getTitleTable().align(Align.right );

//                add(new Label("Notebook", skin)).center();

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
        Array<String> scannedFacts = new Array<>();

        Table table = blackmailDialog.getContentTable();
        if (factSummaries.keySet().size() == 0) {
            scannedFacts.add("No facts scanned yet!");
        }
        for (String fact_ : factSummaries.keySet()) {
            scannedFacts.add(world.viewFactSummary(target.getName(), fact_));
            summaryToFacts.put(world.viewFactSummary(target.getName(), fact_), fact_);
        }
        table.setFillParent(false);

        table.row();
        for (int i = 0; i < scannedFacts.size; i++) {
            Label k = new Label(scannedFacts.get(i), skin);
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
                            break;
                        case EXPOSE:
                            world.expose(info[0], info[1]);
                            activeVerb = ActiveVerb.NONE;
                            createDialogBox("You exposed the target!");
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
//                createDialogBox("You opened the notebook!");
                createNotebook("You opened the notebook!");
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
        ap.setText("AP: " + Integer.toString(world.getPlayer().getAP()));
        tStress.setText("Target Stress: " + Integer.toString(target.getStress()));
        tSusp.setText("Target Suspicion: " + Integer.toString(target.getSuspicion()));
        money.setText("Bitecoin: " + Integer.toString((int)world.getPlayer().getBitecoin()));
    }
}
