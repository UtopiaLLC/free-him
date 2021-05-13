package com.adisgrace.games;

import com.adisgrace.games.NodeView;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;

public class Node extends Group {

    public enum NodeState {
        LOCKED,
        UNSCANNED,
        SCANNED,
        TARGET
    };

    private final int NODE_TOP_OFFSET = 10;
    private final float OFFSET_INCREMENT = 1f;
    private float offset;
    private int counter;
    private boolean up;


    private Vector2 position;
    private Animation<TextureRegion> topAnimation;
    private TextureRegion nodeBaseReg;
    private TextureRegion topRegion;

    private int nodeType;
    private NodeState nodeState;
    
    private float stateTime;
    private TextureRegion reg;
    private boolean isFemale;
    private boolean isBoss;


    private int colorBoi = 0;

    public Node(float x, float y, String name, int type, NodeState state) {

        position = new Vector2(x, y);
//        topAnimation = nodeTopAnim;
//        this.nodeBaseReg = nodeBase;
        super.setName(name);

        nodeType = type;
        nodeState = state;
        isFemale = false;

        changeTextures(nodeState, nodeType);

        setBounds(x, y, nodeBaseReg.getRegionWidth(), nodeBaseReg.getRegionHeight());
        setTouchable(Touchable.enabled);

        stateTime = 0f;
        offset = 0f;
        counter = 0;
        up = true;
        reg = nodeBaseReg;
    }

    public void isFemale(boolean female) {
        isFemale = female;
        changeTextures(nodeState, nodeType);
    }

    public void isBoss(boolean boss) {
        isBoss = boss;
        changeTextures(nodeState, nodeType);
    }

    public void changeColor(int type) {
        nodeType = type;
        changeTextures(nodeState, type);
    }

    /**
     * Changes the state of this node, which also changes the color
     * @param state the new nodestate
     */
    public void changeState(NodeState state) {
        nodeState = state;
        changeTextures(state, nodeType);
    }

    private void changeTextures(NodeState nodeState, int nodeType) {
        switch(nodeState) {
            case LOCKED:
                topAnimation = null;
                topRegion = null;
                nodeBaseReg = NodeView.getLockedNode(nodeType);
                break;
            case UNSCANNED:
                topAnimation = NodeView.getUnscannedNode(nodeType);
                topRegion = null;
                nodeBaseReg = NodeView.getNodeBase(nodeType);
                offset = 0f;
                break;
            case SCANNED:
                topAnimation = NodeView.getScannedNode(nodeType);
                topRegion = null;
                nodeBaseReg = NodeView.getNodeBase(nodeType);
                offset = 0f;
                break;
            case TARGET:
                //topAnimation = NodeView.getTargetNode(nodeType);
                topAnimation = null;
                if (isBoss) {
                    topRegion = NodeView.getTargetBossNode(nodeType);
                }
                else if(isFemale) {
                    topRegion = NodeView.getTargetFemaleNode(nodeType);
                } else {
                    topRegion = NodeView.getTargetNode(nodeType);
                }
                nodeBaseReg = NodeView.getTargetBase(nodeType);
                break;
        }
    }


    @Override
    public void act(float delta) {
        super.act(delta);

    }

    /**
     * draws the node object onto the screen
     * @param batch libGDX batch object
     * @param parentAlpha alpha value to draw
     */
    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        if(nodeState != NodeState.TARGET && nodeState != NodeState.LOCKED) {
            stateTime += Gdx.graphics.getDeltaTime();
            reg = topAnimation.getKeyFrame(stateTime, true);
        } else if(nodeState == NodeState.TARGET){
          reg = topRegion;
        } else {
            reg = nodeBaseReg;
        }

        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        if(nodeState != NodeState.TARGET && nodeState != NodeState.LOCKED) {
            if(counter%10==0){
                offset+=(up?1:-1)*OFFSET_INCREMENT;
            }
            batch.draw(nodeBaseReg, getX(), getY(), getWidth() / 2, getHeight() / 2, getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
            batch.draw(reg, getX(), getY() + offset, getWidth() / 2, getHeight() / 2, getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
            if(counter++>=100){
                counter = 0;
                up = !up;
            }
        } else if(nodeState == NodeState.TARGET) {
            if(counter%10==0){
                offset+=(up?1:-1)*OFFSET_INCREMENT;
            }
            batch.draw(nodeBaseReg, getX(), getY(), getWidth() / 2, getHeight() / 2, getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
            batch.draw(reg, getX(), getY() + offset, getWidth() / 2, getHeight() / 2, getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
            if(counter++>=100){
                counter = 0;
                up = !up;
            }
        } else {
            batch.draw(reg, getX(), getY(), getWidth() / 2, getHeight() / 2, getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
            //batch.draw(reg, getX(), getY(), getWidth() / 2, getHeight() / 2, getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
        }

    }
}

