package com.adisgrace.games;

import com.adisgrace.games.NodeView;
import com.adisgrace.games.util.GameConstants;
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

import static com.adisgrace.games.util.GameConstants.*;

public class Node extends Group {

    public enum NodeState {
        LOCKED,
        UNSCANNED,
        SCANNED,
        TARGET
    };

    /** Name of target that this node belongs to */
    private String targetName;
    /** Name of node */
    private String nodeName;

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

    /** Whether the node is being hovered over */
    private boolean hover = false;

    /** Subtree info of this node (number of child nodes with each stress rating) */
    private int[] subtreeInfo;

    /**
     * Constructor for Node view class (I think)
     *
     * @param x     x-coordinate of node
     * @param y     y-coordinate of node
     * @param name  Name of parent target that this node belongs to
     * @param type  Type of node, whatever that means
     * @param state Whether the node is locked, unscanned, scanned, or a target
     */
    public Node(float x, float y, String name, int type, NodeState state) {
        super.setName(name);

        // Store name of parent target
        targetName = name;

        nodeConstructor(x, y, type, state);
    }

    /**
     * Constructor for Node view class (I think)
     *
     * @param x             x-coordinate of node
     * @param y             y-coordinate of node
     * @param targetName    Name of parent target that this node belongs to
     * @param nodeName      Name of this node
     * @param type          Type of node, whatever that means
     * @param state         Whether the node is locked, unscanned, scanned, or a target
     */
    public Node(float x, float y, String targetName, String nodeName, int type, NodeState state) {
        super.setName(targetName+","+nodeName);

        // Store name of parent target and node itself
        this.targetName = targetName;
        this.nodeName = nodeName;

        nodeConstructor(x, y, type, state);
    }

    /**
     * Helper constructor for Node view class (I think)
     *
     * @param x     x-coordinate of node
     * @param y     y-coordinate of node
     * @param type  Type of node, whatever that means
     * @param state Whether the node is locked, unscanned, scanned, or a target
     */
    private void nodeConstructor(float x, float y, int type, NodeState state) {
        position = new Vector2(x, y);

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

    /**
     * Sets whether or not the current node is being hovered over.
     *
     * @param hover     Whether the current node is being hovered over
     */
    public void setHover(boolean hover) {
        this.hover = hover;
    }

    /**
     * Sets the subtree info of this node.
     *
     * @param subtreeInfo   Int array of number of nodes of each stress rating in this node's subtree
     */
    public void setSubtreeInfo(int[] subtreeInfo) {
        this.subtreeInfo = subtreeInfo;
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
        if (nodeState != NodeState.TARGET && nodeState != NodeState.LOCKED) {
            stateTime += Gdx.graphics.getDeltaTime();
            reg = topAnimation.getKeyFrame(stateTime, true);
        } else if (nodeState == NodeState.TARGET) {
            reg = topRegion;
        } else {
            reg = nodeBaseReg;
        }

        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        if (nodeState != NodeState.TARGET && nodeState != NodeState.LOCKED) {
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

        // If node is scanned and is currently being hovered over, draw node subtree info icons
        if (nodeState == NodeState.SCANNED && hover) {
            batch.draw(SUBTREE_ICON_TEXTURES[0][(nodeType / 2) - (nodeType % 2)], getX(), getY(), getWidth() / 2, getHeight() / 2, getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
            // Display numbers for subtree info
            // Low
            batch.draw(SUBTREE_TEXT_TEXTURES[0][subtreeInfo[1]], getX() - SUBTREE_TEXT_WIDTH / 2f + LOW_X,
                    getY() - SUBTREE_TEXT_HEIGHT / 2f + LOW_Y, SUBTREE_TEXT_WIDTH, SUBTREE_TEXT_HEIGHT);
            // Medium
            batch.draw(SUBTREE_TEXT_TEXTURES[0][subtreeInfo[2]], getX() - SUBTREE_TEXT_WIDTH / 2f + MED_X,
                    getY() - SUBTREE_TEXT_HEIGHT / 2f + MED_Y, SUBTREE_TEXT_WIDTH, SUBTREE_TEXT_HEIGHT);
            // High
            batch.draw(SUBTREE_TEXT_TEXTURES[0][subtreeInfo[3]], getX() - SUBTREE_TEXT_WIDTH / 2f + HIGH_X,
                    getY() - SUBTREE_TEXT_HEIGHT / 2f + HIGH_Y, SUBTREE_TEXT_WIDTH, SUBTREE_TEXT_HEIGHT);
        }

    }
}

