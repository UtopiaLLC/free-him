package com.adisgrace.games;

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
import com.badlogic.gdx.utils.Array;
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
        /**  Linked to Overwork mode; Overwork needs to be applied */
        OVERWORK,
        /**  Linked to Relax mode; Relax needs to be applied */
        REST,
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
    TargetModel target;
    /** world is a variable that links to all the models in the project */
    WorldModel world;
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

    public GameController() {
        canvas = new GameCanvas();
        ExtendViewport viewport = new ExtendViewport(1280, 720);
        viewport.setCamera(canvas.getCamera());
        currentZoom = canvas.getCamera().zoom;
        stage = new Stage(viewport);

        // Create and store targets in array
        Array<String> targetJsons = new Array<>();
        targetJsons.add("PatrickWestfield.json");

        // Create new WorldModel with given target JSONs
        world = new WorldModel(targetJsons);
        activeVerb = ActiveVerb.NONE;

        target = world.getTarget("Patrick Westfield");

        nodeView = new NodeView(stage, target, world);
        imageNodes = nodeView.getImageNodes();
        for(ImageButton button : imageNodes.values()) {
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

        stage.draw();

        toolbarStage.draw();
        updateStats();

        if(world.getGameState() == WorldModel.GAMESTATE.LOSE && !ended) {
            createDialogBox("YOU LOSE!");
            ended = true;

        } else if (world.getGameState() == WorldModel.GAMESTATE.WIN && !ended) {
            createDialogBox("You Win!");
            ended = true;
        }

    }

    @Override
    public void resize(int width, int height) {

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
     * createToolbar creates a fixed toolbar with buttons linked to each of the player skills
     */
    public void createToolbar() {
        // Move to an outside class eventually
        ExtendViewport toolbarViewPort = new ExtendViewport(1280, 720);
        toolbarStage = new Stage(toolbarViewPort);
        skin = new Skin(Gdx.files.internal("skins/neon-ui.json"));

        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(toolbarStage);
        inputMultiplexer.addProcessor(stage);

        Gdx.input.setInputProcessor(inputMultiplexer);

        Table toolbar = new Table();
        toolbar.bottom();
        toolbar.setFillParent(true);

//        TextButton harass = new TextButton("Harass", skin, "default");
//        harass.setTransform(true);
//        harass.setScale(2.0f);
//        harass.addListener(new ClickListener()
//        {
//            @Override
//            public void clicked(InputEvent event, float x, float y)
//            {
//                System.out.println("You harassed me!");
//                activeVerb = ActiveVerb.HARASS;
//            }
//        });
//        TextButton threaten = new TextButton("Threaten", skin, "default");
//        threaten.setTransform(true);
//        threaten.setScale(2.0f);
//        threaten.addListener(new ClickListener()
//        {
//            @Override
//            public void clicked(InputEvent event, float x, float y)
//            {
//                System.out.println("You threatened me!");
//                activeVerb = ActiveVerb.THREATEN;
//
//            }
//        });
//        TextButton expose = new TextButton("Expose", skin, "default");
//        expose.setTransform(true);
//        expose.setScale(2.0f);
//        expose.addListener(new ClickListener()
//        {
//            @Override
//            public void clicked(InputEvent event, float x, float y)
//            {
//                System.out.println("You exposed me!");
//                activeVerb = ActiveVerb.EXPOSE;
//            }
//        });
        TextButton overwork = new TextButton("Overwork", skin, "default");
        overwork.setTransform(true);
        overwork.setScale(2.0f);
        overwork.addListener(new ClickListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                final String s = "overwork";
                confirmDialog("Are you sure you want to overwork?", s);
            }
        });
        TextButton otherJobs = new TextButton("Do other jobs", skin, "default");
        otherJobs.setTransform(true);
        otherJobs.setScale(2.0f);
        otherJobs.addListener(new ClickListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                final String s = "otherJobs";
                confirmDialog("Are you sure you want to do other jobs?", s);
            }
        });
        TextButton relax = new TextButton("Relax", skin, "default");
        relax.setTransform(true);
        relax.setScale(2.0f);
        relax.addListener(new ClickListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                final String s = "relax";
                confirmDialog("Are you sure you want to relax?", s);
            }
        });
        TextButton end = new TextButton("End Day", skin, "default");
        end.setTransform(true);
        end.setScale(2.0f);
        end.addListener(new ClickListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                createDialogBox("You end the day after a long battle of psychological warfare.");
                world.nextTurn();
            }
        });

        //toolbar.add(harass).expandX().padBottom(30);
//        toolbar.add(threaten).expandX().padBottom(10);
//        toolbar.add(expose).expandX().padBottom(10);
        toolbar.add(overwork).expandX().padBottom(10);
        toolbar.add(otherJobs).expandX().padBottom(10);
        toolbar.add(relax).expandX().padBottom(10);
        toolbar.add(end).expandX().padBottom(10).padRight(20);

        Table stats = new Table();
        stress = new Label("Player Stress: " + Float.toString(world.getPlayer().getStress()), skin);
        stress.setFontScale(2);
        ap = new Label("AP: " + Float.toString(world.getPlayer().getAP()), skin);
        ap.setFontScale(2);
        tStress = new Label("Target Stress: " + Float.toString(target.getStress()), skin);
        tStress.setFontScale(2);
        tSusp = new Label("Target Suspicion: " + Float.toString(target.getSuspicion()), skin);
        tSusp.setFontScale(2);
        money = new Label ("Bitecoin: " + Float.toString(world.getPlayer().getBitecoin()), skin);
        money.setFontScale(2);

        stats.top();
        stats.setFillParent(true);

        stats.add(stress).expandX().padTop(20);
        stats.add(ap).expandX().padTop(20);
        stats.add(money).expandX().padTop(20);
        stats.row();
        stats.add(tStress).expandX().padTop(10);
        stats.add(tSusp).expandX().padTop(10);



        toolbarStage.addActor(toolbar);
        toolbarStage.addActor(stats);
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
            camera.translate(0, 50*currentZoom);
        }if(Gdx.input.isKeyPressed(Input.Keys.A)) {
            camera.translate(-50*currentZoom, 0);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.S)) {
            camera.translate(0, -50*currentZoom);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.D)) {
            camera.translate(50*currentZoom, 0);
        }

        if(Gdx.input.isKeyPressed(Input.Keys.E)) {
            camera.zoom = (.9f)*currentZoom;
        } if(Gdx.input.isKeyPressed(Input.Keys.Q)) {
            camera.zoom = (1.1f)*currentZoom;
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
            case THREATEN:
                if(isTarget) {

                }

                break;
            case EXPOSE:
                if(isTarget) {
                    System.out.println(activeVerb);
                    activeVerb = ActiveVerb.NONE;
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
                world.overwork();
                createDialogBox("You overworked yourself and gained 2 AP at the cost of your sanity...");
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
            default:
                System.out.println("You shall not pass");
        }
    }

    /**
     * Updates the stats HUD with current values
     */
    public void updateStats(){
        stress.setText("Player Stress: " + Float.toString(world.getPlayer().getStress()));
        ap.setText("AP: " + Float.toString(world.getPlayer().getAP()));
        tStress.setText("Target Stress: " + Float.toString(target.getStress()));
        tSusp.setText("Target Suspicion: " + Float.toString(target.getSuspicion()));
        money.setText("Bitecoin: " + Float.toString(world.getPlayer().getBitecoin()));
    }
}
