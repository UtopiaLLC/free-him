package com.adisgrace.games;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class ConnectorActor extends Actor {

    private Animation<TextureRegion> animation;
    private Vector2 position;

    private float stateTime;
    private TextureRegion reg;

    /** Dimensions of connector Texture */
    private final int TILE_HEIGHT = 256;
    private final int TILE_WIDTH = 444;

    public ConnectorActor(Animation<TextureRegion> animation, Vector2 position) {
        this.animation = animation;
        this.position = position;


        setBounds(position.x, position.y,
                animation.getKeyFrame(0).getRegionWidth(),
                animation.getKeyFrame(0).getRegionHeight());

        stateTime = 0;
    }

    public boolean animationComplete() {
        return animation.isAnimationFinished(stateTime);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        stateTime += Gdx.graphics.getDeltaTime();
        reg = animation.getKeyFrame(stateTime, true);
        setBounds(position.x, position.y,
                reg.getRegionWidth(),
                reg.getRegionHeight());

        int x_offset = TILE_WIDTH - (int)getWidth();
        int y_offset = TILE_HEIGHT - (int)getHeight();
//        x_offset = 0;
//        y_offset = 0;

        batch.draw(reg, position.x + (x_offset/2), position.y + (y_offset/2),
                getWidth() / 2, getHeight() / 2, getWidth(), getHeight(),
                getScaleX(), getScaleY(), getRotation());

    }


}
