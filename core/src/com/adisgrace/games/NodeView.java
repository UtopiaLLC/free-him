package com.adisgrace.games;

import com.adisgrace.games.models.LevelController;
import com.adisgrace.games.models.TargetModel;
import com.adisgrace.games.models.WorldModel;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;

import javax.swing.plaf.TextUI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class NodeView {
    /** stage is a Scene2d scene graph that contains all hierarchies of Scene2d Actors */
    private Stage stage;
    /** nodeCoords contains all world coordinates for each fact node and target node */
    private Array<Vector2> nodeCoords;
    /** imageNodes contains all ImageButtons for each fact node and target node
     *  The key is a string concatenation of [targetName,factNodeName] or [targetName]
     *  The value is the corresponding ImageButton
     */
    private Map<String, Node> imageNodes;

    /** Dimensions of map tile */
    private static final int TILE_HEIGHT = 256;
    private static final int TILE_WIDTH = 444;

    private static final float ADD = 0;
    private static final float SCALE_X = 444;
    private static final float SCALE_Y = 256;
    private static final float LOCKED_OFFSET = 114.8725f;

    public NodeView(Stage stage, TargetModel target, LevelController levelController) {
        this.stage = stage;
        nodeCoords = new Array<>();
        Vector2 targetCoords = levelController.getTargetPos(target.getName());

        System.out.println(levelController.getTargetPos(target.getName()));
        Array<String> targetNodes = target.getNodes();
        for (String nodeName: targetNodes ){
            Vector2 node = target.getNodeCoords(nodeName);
            node.x = node.x + targetCoords.x;
            node.y = node.y + targetCoords.y;
            nodeCoords.add(node);
        }
        //targetCoords = scaleNodeCoordinates(targetCoords, ADD, SCALE_X, SCALE_Y);

        imageNodes = new HashMap<>();
        createImageNodes(target, targetNodes, targetCoords);
    }

    /**
     * Gets the map of all ImageNodes.
     *
     * imageNodes contains all ImageButtons for each fact node and target node
     *      The key is a string concatenation of [targetName,factNodeName] or [targetName].
     *      The value is the corresponding ImageButton.
     *
     * @return imageNodes
     */
    public Map<String, Node> getImageNodes() {
        return imageNodes;
    }

    /**
     * Scales world node coordinates to have more distance between each node.
     * @param targetCoords
     * @param add
     * @param multiplyX
     * @param multiplyY
     * @return targetCoords with scaling, in world coordinates
     */
    private Vector2 scaleNodeCoordinates(Vector2 targetCoords, float add, float multiplyX, float multiplyY){
        targetCoords.add(targetCoords.x * multiplyX, targetCoords.y * multiplyY);
        targetCoords.add(add, add);

        for (Vector2 node: nodeCoords) {
            node.add(node.x * multiplyX, node.y * multiplyY);
            node.add(add, add);
        }
        return targetCoords;
    }

    /**
     * Adds information about ImageButtons to the imageNodes Map
     * @param target
     * @param targetNodes
     * @param targetCoords
     */
    private void createImageNodes(TargetModel target, Array<String> targetNodes, Vector2 targetCoords) {

        for (int i = 0; i < targetNodes.size; i++) {
            assert(targetNodes.size == nodeCoords.size);

            //ImageButton button = new ImageButton(NodeView.getLockedNode(0)); //Set the button up
            Vector2 pos = isometricToWorld(nodeCoords.get(i));
            // Account for difference between tile width and sprite width
            pos.x -= (NodeView.getLockedNode(0).getRegionWidth() - TILE_WIDTH) / 2;
            pos.y += ((TILE_HEIGHT / 2) - LOCKED_OFFSET) * 2;

            Node node = new Node(pos.x, pos.y, target.getName()+","+targetNodes.get(i), 0, Node.NodeState.LOCKED);
            imageNodes.put(target.getName()+","+targetNodes.get(i), node);
            stage.addActor(node);
        }

        //ImageButton button = new ImageButton(NodeView.getTargetNode(0)); //Set the button up
        Vector2 pos = isometricToWorld(targetCoords);
        pos.x -= (NodeView.getTargetNode(0).getTexture().getWidth() - TILE_WIDTH) / 2;
        pos.y += ((TILE_HEIGHT / 2) - LOCKED_OFFSET) * 2;

        Node targetNode = new Node(pos.x, pos.y, target.getName(), 0, Node.NodeState.TARGET);
        imageNodes.put(target.getName(), targetNode);
        stage.addActor(targetNode);


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
    private Vector2 nearestIsoCenter(float x, float y){
        // Transform world coordinates to isometric space
        Vector2 vec = new Vector2(x,y);
        vec = isometricToWorld(vec);
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

    public static Array<TextureRegion> lockedNodes;
    public static Array<Animation> unscannedNodes;
    public static Array<Animation> scannedNodes;
    public static Array<TextureRegion> targetNodes;
    public static Array<TextureRegion> nodeBases;


    public static class AnimatedDrawable extends BaseDrawable {

        private Animation<TextureRegion> animation;
        private TextureRegion keyFrame;
        private float stateTime = 0;

        public AnimatedDrawable(Animation<TextureRegion> animation){

            this.animation = animation;
            TextureRegion key = animation.getKeyFrame(0f);

            this.setLeftWidth(key.getRegionWidth()/2);
            this.setRightWidth(key.getRegionWidth()/2);
            this.setTopHeight(key.getRegionHeight()/2);
            this.setBottomHeight(key.getRegionHeight()/2);
            this.setMinWidth(key.getRegionWidth());
            this.setMinHeight(key.getRegionHeight());

        }

        @Override
        public void draw(Batch batch, float x, float y, float width, float height){

            stateTime += Gdx.graphics.getDeltaTime();
            keyFrame = animation.getKeyFrame(stateTime, true);

            batch.draw(keyFrame, x,y, keyFrame.getRegionWidth(), keyFrame.getRegionHeight());
        }
    }

    public static void loadAnimations() {


        // Locked Nodes

        lockedNodes = new Array<>();
        Texture lockedNode = new Texture("node/N_LockedNode_1.png");
        TextureRegion[][] regions = new TextureRegion(lockedNode).split(
                lockedNode.getWidth() / 6,
                lockedNode.getHeight() / 2);

        TextureRegion tRegion = regions[0][0];
        TextureRegionDrawable drawable = new TextureRegionDrawable(tRegion);

        for(int i = 0; i < 6; i++) {
            tRegion = regions[0][i];
            lockedNodes.add(tRegion);

            tRegion = regions[1][i];
            drawable = new TextureRegionDrawable(tRegion);
            lockedNodes.add(tRegion);
        }

        System.out.println("Locked nodes done!");
        // Unscanned Nodes
        unscannedNodes = new Array<>();

        Texture node = new Texture("node/N_UnscannedNode_1.png");
        regions = new TextureRegion(node).split(
                node.getWidth() / 10,
                node.getHeight() / 6);

        Texture node_base = new Texture("node/N_NodeBase_1.png");
        TextureRegion[][] node_regions = new TextureRegion(node_base).split(
                node_base.getWidth() / 6,
                node_base.getHeight() / 2);

        Texture combined;
        TextureRegion[] spinFrames = new TextureRegion[10];

        for(int j = 0; j < 6; j++) {
            for (int i = 0; i < 10; i++) {
                //combined = GameCanvas.combineTextures(regions[j][i], node_regions[0][j]);
                //spinFrames[i] = new TextureRegion(combined);
                spinFrames[i] = new TextureRegion(regions[j][i]);
            }

            Animation<TextureRegion> spinAnimation = new Animation<TextureRegion>(0.025f, spinFrames);
            spinAnimation.setPlayMode(Animation.PlayMode.LOOP);

            unscannedNodes.add(spinAnimation);


            for (int i = 0; i < 10; i++) {
                //combined = GameCanvas.combineTextures(regions[j][i], node_regions[1][j]);
                //spinFrames[i] = new TextureRegion(combined);
                spinFrames[i] = new TextureRegion(regions[j][i]);
            }

            spinAnimation = new Animation<TextureRegion>(0.025f, spinFrames);
            unscannedNodes.add(spinAnimation);

        }
        System.out.println("Unscanned nodes done!");
        //Scanned Nodes!
        scannedNodes = new Array<>();
        node = new Texture("node/N_ScannedNode_2.png");
        regions = new TextureRegion(node).split(
                node.getWidth() / 10,
                node.getHeight() / 6);

        node_base = new Texture("node/N_NodeBase_1.png");
        node_regions = new TextureRegion(node_base).split(
                node_base.getWidth() / 6,
                node_base.getHeight() / 2);

        spinFrames = new TextureRegion[10];

        for(int j = 0; j < 6; j++) {

            for (int i = 0; i < 10; i++) {
                //combined = GameCanvas.combineTextures(regions[j][i], node_regions[0][j]);
                //spinFrames[i] = new TextureRegion(combined);
                spinFrames[i] = new TextureRegion(regions[j][i]);
            }

            Animation<TextureRegion> spinAnimation = new Animation<TextureRegion>(0.025f, spinFrames);
            spinAnimation.setPlayMode(Animation.PlayMode.LOOP);

            scannedNodes.add(spinAnimation);

            for (int i = 0; i < 10; i++) {
                //combined = GameCanvas.combineTextures(regions[j][i], node_regions[1][j]);
                //spinFrames[i] = new TextureRegion(combined);
                spinFrames[i] = new TextureRegion(regions[j][i]);
            }

            spinAnimation = new Animation<TextureRegion>(0.025f, spinFrames);
            scannedNodes.add(spinAnimation);

        }

        System.out.println("Scanned nodes done!");
        // Targets

        targetNodes = new Array<>();

        Texture target_Look = new Texture("node/N_TargetMale_1.png");
        regions = new TextureRegion(target_Look).split(
                target_Look.getWidth() / 6,
                target_Look.getHeight() / 2);

        node_base = new Texture("node/N_TargetBase_1.png");
        node_regions = new TextureRegion(node_base).split(
                node_base.getWidth() / 6,
                node_base.getHeight() / 2);

        for(int i = 0; i < 6; i++) {
            combined = GameCanvas.combineTextures(regions[0][i], node_regions[0][i]);
            //drawable = new TextureRegionDrawable(new TextureRegion(combined));
            targetNodes.add(new TextureRegion(combined));

            combined = GameCanvas.combineTextures(regions[1][i], node_regions[1][i]);
            //drawable = new TextureRegionDrawable(new TextureRegion(combined));
            targetNodes.add(new TextureRegion(combined));

        }


        System.out.println("Target nodes done!");

        nodeBases = new Array<>();

        node_base = new Texture("node/N_NodeBase_1.png");
        node_regions = new TextureRegion(node_base).split(
                node_base.getWidth() / 6,
                node_base.getHeight() / 2);

        for(int i = 0; i < 6; i++) {
            nodeBases.add(new TextureRegion(node_regions[0][i]));

            nodeBases.add(new TextureRegion(node_regions[1][i]));

        }




    }

    /**
     *
     * @param type where:
     *             0 = Green, Litup
     *             1 = Green, Dimmed
     *             2 = Yellow, Litup
     *             3 = Yellow, Dimmed
     *             4 = Red, Litup
     *             5 = Red, Dimmed
     *             6 = Orange, Litup
     *             7 = Orange, Dimmed
     *             8 = Purple, Litup
     *             9 = Purple, Dimmed
     *             10 = Grey, Litup
     *             11 = Grey, Dimmed
     *
     * @return Returns the node drawable based on the type specified
     */
    public static TextureRegion getLockedNode(int type) {
        return lockedNodes.get(type);
    }

    /**
     *
     * @param type where:
     *             0 = Green, Litup
     *             1 = Green, Dimmed
     *             2 = Yellow, Litup
     *             3 = Yellow, Dimmed
     *             4 = Red, Litup
     *             5 = Red, Dimmed
     *             6 = Orange, Litup
     *             7 = Orange, Dimmed
     *             8 = Purple, Litup
     *             9 = Purple, Dimmed
     *             10 = Grey, Litup
     *             11 = Grey, Dimmed
     *
     * @return Returns the node drawable based on the type specified
     */
    public static Animation<TextureRegion> getUnscannedNode(int type) {
        return unscannedNodes.get(type);
    }

    /**
     *
     * @param type where:
     *             0 = Green, Litup
     *             1 = Green, Dimmed
     *             2 = Yellow, Litup
     *             3 = Yellow, Dimmed
     *             4 = Red, Litup
     *             5 = Red, Dimmed
     *             6 = Orange, Litup
     *             7 = Orange, Dimmed
     *             8 = Purple, Litup
     *             9 = Purple, Dimmed
     *             10 = Grey, Litup
     *             11 = Grey, Dimmed
     *
     * @return Returns the node drawable based on the type specified
     */
    public static Animation<TextureRegion> getScannedNode(int type) {
        return scannedNodes.get(type);
    }

    /**
     *
     * @param type where:
     *             0 = Green, Litup
     *             1 = Green, Dimmed
     *             2 = Yellow, Litup
     *             3 = Yellow, Dimmed
     *             4 = Red, Litup
     *             5 = Red, Dimmed
     *             6 = Orange, Litup
     *             7 = Orange, Dimmed
     *             8 = Purple, Litup
     *             9 = Purple, Dimmed
     *             10 = Grey, Litup
     *             11 = Grey, Dimmed
     *
     * @return Returns the node drawable based on the type specified
     */
    public static TextureRegion getTargetNode(int type) {
        return targetNodes.get(type);
    }

    /**
     *
     * @param type where:
     *             0 = Green, Litup
     *             1 = Green, Dimmed
     *             2 = Yellow, Litup
     *             3 = Yellow, Dimmed
     *             4 = Red, Litup
     *             5 = Red, Dimmed
     *             6 = Orange, Litup
     *             7 = Orange, Dimmed
     *             8 = Purple, Litup
     *             9 = Purple, Dimmed
     *             10 = Grey, Litup
     *             11 = Grey, Dimmed
     *
     * @return Returns the node drawable based on the type specified
     */
    public static TextureRegion getNodeBase(int type) {
        return nodeBases.get(type);
    }

}
