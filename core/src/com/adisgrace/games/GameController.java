package com.adisgrace.games;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
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

    public GameController() {
        canvas = new GameCanvas();
        ExtendViewport viewport = new ExtendViewport(1280, 720);
        viewport.setCamera(canvas.getCamera());
        currentZoom = canvas.getCamera().zoom;
        stage = new Stage(viewport);

        createToolbar();

        target = new TargetModel("PatrickWestfield.json");

        Array<String> targetJsons = new Array<>();
        targetJsons.add("PatrickWestfield.json");

        Map<String, Vector2> targetCoords = new HashMap<>();
        targetCoords.put("Patrick Westfield", new Vector2(0f, 0f));
        world = new WorldModel(targetJsons, targetCoords);
        activeVerb = ActiveVerb.NONE;

        nodeView = new NodeView(stage, target, world);


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

        TextButton harass = new TextButton("Harass", skin, "default");
        harass.setTransform(true);
        harass.setScale(2.0f);
        harass.addListener(new ClickListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                System.out.println("You harassed me!");
                activeVerb = ActiveVerb.HARASS;
            }
        });
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
                System.out.println("You overworked me!");
                activeVerb = ActiveVerb.OVERWORK;
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
                System.out.println("You rested me!");
                activeVerb = ActiveVerb.REST;
            }
        });

        toolbar.add(harass).expandX().padBottom(30);
        toolbar.add(threaten).expandX().padBottom(30);
        toolbar.add(expose).expandX().padBottom(30);
        toolbar.add(overwork).expandX().padBottom(30);
        toolbar.add(rest).expandX().padBottom(30);

        toolbarStage.addActor(toolbar);
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

        Vector2 camMin = new Vector2(-1000f, -1000f);//(camera.viewportWidth/2, camera.viewportHeight/2);
        camMin.scl(camera.zoom/2); //bring to center and scale by the zoom level
        Vector2 camMax = new Vector2(1000f, 1000f);
        camMax.sub(camMin); //bring to center

        //keep camera within borders
        camX = Math.min(camMax.x, Math.max(camX, camMin.x));
        camY = Math.min(camMax.y, Math.max(camY, camMin.y));

        camera.position.set(camX, camY, camera.position.z);

        camera.update();
    }


}
