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
        targetCoords = scaleNodeCoordinates(targetCoords, 100f, 250f);

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
     * @param multiply
     * @return targetCoords with scaling, in world coordinates
     */
    private Vector2 scaleNodeCoordinates(Vector2 targetCoords, float add, float multiply){
        targetCoords.add(targetCoords.x * multiply, targetCoords.y * multiply);
        targetCoords.add(add, add);

        for (Vector2 node: nodeCoords) {
            node.add(node.x * multiply, node.y * multiply);
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
            Vector2 pos = convertToIsometric(nodeCoords.get(i));
            button.setPosition(pos.x, pos.y);
            button.setName(target.getName()+","+targetNodes.get(i));

            imageNodes.put(target.getName()+","+targetNodes.get(i), button);
            stage.addActor(button);
        }

         tRegion = new TextureRegion(new Texture(Gdx.files.internal("targetmale/green.png")));
         drawable = new TextureRegionDrawable(tRegion);
         ImageButton button = new ImageButton(drawable); //Set the button up
         Vector2 pos = convertToIsometric(targetCoords);
         button.setPosition(pos.x, pos.y);
         button.setName(target.getName());
         imageNodes.put(target.getName(), button);
         stage.addActor(button);


    }

    /**
     * Converts world coordinates to isometric coordinates
     * @param worldCoords
     * @return the isometric coordinates
     */
    public Vector2 convertToIsometric(Vector2 worldCoords) {
        float oneOne = (float)Math.sqrt(3)/2;
        float oneTwo = (float)Math.sqrt(3)/2;
        float twoOne = (float)-1/2;
        float twoTwo = (float)1/2;
        Vector2 ans = new Vector2();
        ans.x = oneOne * worldCoords.x + oneTwo * worldCoords.y;
        ans.y = twoOne * worldCoords.x + twoTwo * worldCoords.y;

        return ans;
    }




}
