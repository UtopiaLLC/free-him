package com.adisgrace.games;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.HashMap;
import java.util.Map;

public class GameController implements Screen {

    /** Enumeration representing the active verb applied via toolbar */
    public enum ActiveVerb {
        /**  */
        HARASS,
        /**  */
        THREATEN,
        /**  */
        EXPOSE,
        /**  */
        OVERWORK,
        /**  */
        REST,
        /**  */
        NONE
    };

    private GameCanvas canvas;
    private Stage stage;
    private Stage toolbarStage;
    private Skin skin;

    //  private NodeMap map;
    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;
    private float currentZoom;
    TargetModel target;
    WorldModel world;
    private ActiveVerb activeVerb;
    private NodeView nodeView;
    private Map<String, ImageButton> imageNodes;

    // Label Stats
    Label stress;
    Label ap;
    Label tStress;
    Label tSusp;

    public GameController() {
        canvas = new GameCanvas();
        ExtendViewport viewport = new ExtendViewport(1280, 720);
        viewport.setCamera(canvas.getCamera());
        currentZoom = canvas.getCamera().zoom;
        stage = new Stage(viewport);

        target = new TargetModel("PatrickWestfield.json");

        Array<String> targetJsons = new Array<>();
        targetJsons.add("PatrickWestfield.json");

        Map<String, Vector2> targetCoords = new HashMap<>();
        targetCoords.put("Patrick Westfield", new Vector2(0f, 0f));
        world = new WorldModel(targetJsons, targetCoords);
        activeVerb = ActiveVerb.NONE;

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
    public void render(float delta) {

        canvas.clear();

        moveCamera();
//        canvas.begin();
//        canvas.end();
        stage.act(delta);
        stage.draw();
        toolbarStage.act(delta);
        toolbarStage.draw();
        updateStats();

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
    // Move to an outside class eventually
    public void createToolbar() {
        ExtendViewport toolbarViewPort = new ExtendViewport(1280, 720);
        toolbarStage = new Stage(toolbarViewPort);
        skin = new Skin(Gdx.files.internal("skins/neon-ui.json"));

        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(stage);
        inputMultiplexer.addProcessor(toolbarStage);
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
        TextButton threaten = new TextButton("Threaten", skin, "default");
        threaten.setTransform(true);
        threaten.setScale(2.0f);
        threaten.addListener(new ClickListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                System.out.println("You threatened me!");
                activeVerb = ActiveVerb.THREATEN;

            }
        });
        TextButton expose = new TextButton("Expose", skin, "default");
        expose.setTransform(true);
        expose.setScale(2.0f);
        expose.addListener(new ClickListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                System.out.println("You exposed me!");
                activeVerb = ActiveVerb.EXPOSE;
            }
        });
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
        TextButton rest = new TextButton("Rest", skin, "default");
        rest.setTransform(true);
        rest.setScale(2.0f);
        rest.addListener(new ClickListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                final String s = "rest";
                confirmDialog("Are you sure you want to rest?", s);
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
                createDialogBox("You ended the day after a long battle of psychological warfare.");
                world.nextTurn();
            }
        });

        //toolbar.add(harass).expandX().padBottom(30);
        toolbar.add(threaten).expandX().padBottom(30);
        toolbar.add(expose).expandX().padBottom(30);
        toolbar.add(overwork).expandX().padBottom(30);
        toolbar.add(rest).expandX().padBottom(30);
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

        stats.top();
        stats.setFillParent(true);

        stats.add(stress).expandX().padTop(20);
        stats.add(ap).expandX().padTop(20);
        stats.add(tStress).expandX().padTop(20);
        stats.add(tSusp).expandX().padTop(20);


        toolbarStage.addActor(toolbar);
        toolbarStage.addActor(stats);
    }

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

    public void actOnNode(String nodeName) {
        String[] nodeInfo = nodeName.split(",");
        boolean isTarget = false;
        if(nodeInfo.length == 1) {
            isTarget = true;
        }
        switch (activeVerb) {
            case NONE:
                if(!isTarget) {
//                    world.hack(nodeInfo[0], nodeInfo[1]);
//                    world.scan(nodeInfo[0], nodeInfo[1]);
//                    createDialogBox(world.viewFact(nodeInfo[0], nodeInfo[1]));
                    if(world.getPlayer().canHack() && world.getPlayer().canScan()) {
                        createDialogBox(world.interact(nodeInfo[0], nodeInfo[1]));
                        reloadDisplayedNodes();
                    } else {
                        createDialogBox("Insufficient AP to hack or scan this node.");
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

    public void createDialogBox(String s) {
        Dialog dialog = new Dialog("", skin);
        dialog.getBackground().setMinWidth(500);
        dialog.getBackground().setMinHeight(500);
        Label l = new Label( s, skin );
        l.setFontScale(2);
        l.setWrap( true );
        dialog.getContentTable().add( l ).prefWidth( 350 );
        dialog.button("Ok", true); //sends "true" as the result
        dialog.key(Input.Keys.ENTER, true); //sends "true" when the ENTER key is pressed
        dialog.show(toolbarStage);
    }

    public void confirmDialog(String s, final String function) {
        Dialog dialog = new Dialog("Are you sure?", skin) {
            public void result(Object obj) {
                if((boolean)obj) {
                    callConfirmFunction(function);
                } else {

                }
            }
        };
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

    public void reloadDisplayedNodes() {
        Array<String> displayedNodes= world.getDisplayedNodes().get(target.getName());
        for(String str : displayedNodes) {
            stage.addActor(imageNodes.get(target.getName()+","+str));
        }

    }

    public void callConfirmFunction(String s) {
        switch(s) {
            case "overwork":
                world.overwork();
                createDialogBox("You overworked yourself and gained 2 AP at the cost of your sanity...");
                break;
            case "rest":
                world.relax(1);
                createDialogBox("You rested for 1 AP and decreased your stress!");
                break;
            default:
                System.out.println("You shall not pass");
        }
    }

    public void updateStats(){
        stress.setText("Player Stress: " + Float.toString(world.getPlayer().getStress()));
        ap.setText("AP: " + Float.toString(world.getPlayer().getAP()));
        tStress.setText("Target Stress: " + Float.toString(target.getStress()));
        tSusp.setText("Target Suspicion: " + Float.toString(target.getSuspicion()));
    }
}
