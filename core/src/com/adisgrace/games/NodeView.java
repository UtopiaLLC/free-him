package com.adisgrace.games;

import com.adisgrace.games.models.TargetModel;
import com.adisgrace.games.models.WorldModel;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;
import java.util.Map;

public class NodeView {
    /** stage is a Scene2d scene graph that contains all hierarchies of Scene2d Actors */
    private Stage stage;
    /** nodeCoords contains all world coordinates for each fact node and target node */
    private Array<Vector2> nodeCoords;
    /** imageNodes contains all ImageButtons for each fact node and target node
     *  The key is a string concatenation of [targetName,factNodeName] or [targetName]
     *  The value is the corresponding ImageButton
     */
    private Map<String, ImageButton> imageNodes;

    /** Dimensions of map tile */
    private static final int TILE_HEIGHT = 256;
    private static final int TILE_WIDTH = 444;

    private static final float ADD = 0;
    private static final float SCALE_X = 444/2f;
    private static final float SCALE_Y = 256/2f;
    private static final float LOCKED_OFFSET = 114.8725f;

    public NodeView(Stage stage, TargetModel target, WorldModel world) {
        this.stage = stage;
        nodeCoords = new Array<>();
        Vector2 targetCoords = world.getWorldCoordinates(target.getName());
        Array<String> targetNodes = target.getNodes();
        for (String nodeName: targetNodes ){
            Vector2 node = target.getNodeCoords(nodeName);
            node.x = node.x + targetCoords.x;
            node.y = node.y + targetCoords.y;
            nodeCoords.add(node);
        }
        targetCoords = scaleNodeCoordinates(targetCoords, ADD, SCALE_X, SCALE_Y);

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
    public Map<String, ImageButton> getImageNodes() {
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
        TextureRegion tRegion = new TextureRegion(new Texture(Gdx.files.internal("node/green.png")));
        TextureRegionDrawable drawable = new TextureRegionDrawable(tRegion);
        for (int i = 0; i < targetNodes.size; i++) {
            assert(targetNodes.size == nodeCoords.size);

            ImageButton button = new ImageButton(drawable); //Set the button up
            Vector2 pos = nearestIsoCenter(nodeCoords.get(i).x, nodeCoords.get(i).y);
            // Account for difference between tile width and sprite width
            pos.x -= (tRegion.getTexture().getWidth() - TILE_WIDTH) / 2;
            pos.y += ((TILE_HEIGHT / 2) - LOCKED_OFFSET) * 2;

            button.setPosition(pos.x, pos.y);
            button.setName(target.getName()+","+targetNodes.get(i));

            imageNodes.put(target.getName()+","+targetNodes.get(i), button);
            stage.addActor(button);
        }

         tRegion = new TextureRegion(new Texture(Gdx.files.internal("targetmale/green.png")));
         drawable = new TextureRegionDrawable(tRegion);
         ImageButton button = new ImageButton(drawable); //Set the button up
         Vector2 pos = nearestIsoCenter(targetCoords.x, targetCoords.y);
         pos.x -= (tRegion.getTexture().getWidth() - TILE_WIDTH) / 2;
         pos.y += ((TILE_HEIGHT / 2) - LOCKED_OFFSET) * 2;
         button.setPosition(pos.x, pos.y);
         button.setName(target.getName());
         imageNodes.put(target.getName(), button);
         stage.addActor(button);


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
    private Vector2 nearestIsoCenter(float x, float y){
        // Transform world coordinates to isometric space
        Vector2 vec = new Vector2(x,y);
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




}
