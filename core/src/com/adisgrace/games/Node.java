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

    private Vector2 position;
    private Animation<TextureRegion> topAnimation;
    private TextureRegion nodeBaseReg;

    private int nodeType;
    private NodeState nodeState;
    
    private float stateTime;
    private TextureRegion reg;

    private int colorBoi = 0;

    public Node(float x, float y, String name, int type, NodeState state) {

        position = new Vector2(x, y);
//        topAnimation = nodeTopAnim;
//        this.nodeBaseReg = nodeBase;
        super.setName(name);

        nodeType = type;
        nodeState = state;

        changeTextures(nodeState, nodeType);

        setBounds(x, y, nodeBaseReg.getRegionWidth(), nodeBaseReg.getRegionHeight());
        setTouchable(Touchable.enabled);

        stateTime = 0f;

        reg = nodeBaseReg;
    }

    public void changeColor(int type) {
        nodeType = type;
        changeTextures(nodeState, type);
    }

    public void changeState(NodeState state) {
        nodeState = state;
        changeTextures(state, nodeType);
    }

    private void changeTextures(NodeState nodeState, int nodeType) {
        switch(nodeState) {
            case LOCKED:
                topAnimation = null;
                nodeBaseReg = NodeView.getLockedNode(nodeType);
                break;
            case UNSCANNED:
                topAnimation = NodeView.getUnscannedNode(nodeType);
                nodeBaseReg = NodeView.getNodeBase(nodeType);
                break;
            case SCANNED:
                topAnimation = NodeView.getScannedNode(nodeType);
                nodeBaseReg = NodeView.getNodeBase(nodeType);
                break;
            case TARGET:
                //topAnimation = NodeView.getTargetNode(nodeType);
                topAnimation = null;
                nodeBaseReg = NodeView.getTargetNode(nodeType);
                break;
        }
    }


    @Override
    public void act(float delta) {
        super.act(delta);

    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

//        if(Gdx.graphics.getDeltaTime() > 0.015) {
//            colorBoi++;
//            if(colorBoi > 11) {
//                colorBoi = 0;
//            }
//            changeColor(colorBoi);
//            System.out.println(colorBoi);
//        }

        if(nodeState != NodeState.TARGET && nodeState != NodeState.LOCKED) {
            stateTime += Gdx.graphics.getDeltaTime();
            reg = topAnimation.getKeyFrame(stateTime, true);
        } else {
            reg = nodeBaseReg;
        }

        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        if(nodeState != NodeState.TARGET && nodeState != NodeState.LOCKED) {
            batch.draw(nodeBaseReg, getX(), getY(), getWidth() / 2, getHeight() / 2, getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
            batch.draw(reg, getX(), getY(), getWidth() / 2, getHeight() / 2, getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
        } else {
            batch.draw(reg, getX(), getY(), getWidth() / 2, getHeight() / 2, getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
            //batch.draw(reg, getX(), getY(), getWidth() / 2, getHeight() / 2, getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
        }
    }
}

