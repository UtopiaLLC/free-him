package com.uiteam.game;

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

    private Vector2 position;
    private Animation<TextureRegion> topAnimation;
    private TextureRegion nodeBaseReg;

    private int nodeType;
    private NodeState nodeState;

    public Image nodeTop;
    public Image nodeBase;


    float stateTime;


    public Node(float x, float y, String name, int type, NodeState state) {

        position = new Vector2(x, y);
//        topAnimation = nodeTopAnim;
//        this.nodeBaseReg = nodeBase;
        super.setName(name);

        nodeType = type;
        nodeState = state;

        changeTextures(nodeState, nodeType);

        setBounds(sprite.getX(), sprite.getY(), sprite.getWidth(), sprite.getHeight());
        setTouchable(Touchable.enabled);

        stateTime = 0f;
    }

    public void changeColor(int type) {

    }

    public void changeState(NodeState state) {

    }

    private void changeTextures(NodeState nodeState, int nodeType) {
        switch(nodeState) {
            case LOCKED:
                topAnimation = null;
                nodeBaseReg = NodeView.getLockedNode(nodeType);
                break;
            case UNSCANNED:
                topAnimation = NodeView.getUnscannedNode(nodeType);
                //nodeBaseReg = NodeView.getLockedNode(nodeType);
            case SCANNED:
                topAnimation = NodeView.getScannedNode(nodeType);
                //nodeBaseReg = NodeView.getLockedNode(nodeType);
            case TARGET:
                //topAnimation = NodeView.getTargetNode(nodeType);
                nodeBaseReg = NodeView.getLockedNode(nodeType);


        }
    }


    @Override
    public void act(float delta) {
        super.act(delta);

        stateTime += delta;
        reg = walkAnimation.getKeyFrame(stateTime,true);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
//        sprite.draw(batch);


    }
}

//        addListener(new InputListener() {
//            @Override
//            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
//                super.enter(event, x, y, pointer, fromActor);
//                setTexture(new Texture(Gdx.files.internal("Globe_icon_hover.png")));
//            }
//
//            @Override
//            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
//                super.exit(event, x, y, pointer, toActor);
//                setTexture(new Texture(Gdx.files.internal("Globe_icon.png")));
//            }
//        });
