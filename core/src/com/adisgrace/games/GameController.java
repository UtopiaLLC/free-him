package com.adisgrace.games;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class GameController implements Screen {
    private GameCanvas canvas;
    private Stage stage;
    private Skin skin;

    //  private NodeMap map;
    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;
    private float currentZoom;


    public GameController() {
        canvas = new GameCanvas();
        FitViewport viewport = new FitViewport(1280, 720);
        viewport.setCamera(canvas.getCamera());
        currentZoom = canvas.getCamera().zoom;

        skin = new Skin(Gdx.files.internal("skins/neon-ui.json"));
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);

        Table toolbar = new Table();
        toolbar.setFillParent(true);

        stage.addActor(toolbar);

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {

        canvas.clear();

        moveCamera();
        canvas.begin();
        canvas.draw(new Texture(Gdx.files.internal("badlogic.jpg")),10f, 10f) ;
        canvas.draw(new Texture(Gdx.files.internal("badlogic.jpg")),500f, 2000f) ;
        canvas.draw(new Texture(Gdx.files.internal("badlogic.jpg")),1500f, 2000f) ;
        canvas.draw(new Texture(Gdx.files.internal("badlogic.jpg")),2500f, 2500f) ;
        canvas.draw(new Texture(Gdx.files.internal("badlogic.jpg")),1250f, 1000f) ;



        canvas.end();
        stage.act(delta);
        stage.draw();

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

        Vector2 camMin = new Vector2(camera.viewportWidth/2, camera.viewportHeight/2);
        camMin.scl(camera.zoom/2); //bring to center and scale by the zoom level
        Vector2 camMax = new Vector2(3000f, 3000f);
        camMax.sub(camMin); //bring to center

        //keep camera within borders
        camX = Math.min(camMax.x, Math.max(camX, camMin.x));
        camY = Math.min(camMax.y, Math.max(camY, camMin.y));

        camera.position.set(camX, camY, camera.position.z);

        camera.update();
    }
}
