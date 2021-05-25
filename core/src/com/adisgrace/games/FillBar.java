package com.adisgrace.games;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class FillBar extends Group {

    private Image outline;
    private TextureRegionDrawable fill_asset;
    private Image fill;

    /** Proportion of bar that is filled, between 0 and 1 */
    private float fill_amount;
    /** Pixel distances from 0% fill to border and 100% fill to border */
    private int empty_border, full_border;

    /**
     * Creates a progress bar actor with the specified attributes and 0% fill.
     *
     * @param outline asset for empty progress bar
     * @param fill asset for progress bar fill, must be the same size as {outline}
     * @param offsets are bottom/top offsets from the bottom/top of the fill asset or absolute values?
     * @param bottom distance from 0% fill and the bottom of the fill asset if offsets, else pixel height of 0% fill
     * @param top distance from 100% fill and the top of the fill asset if offsets, else pixel height of 100% fill
     */
    public FillBar(Texture outline, Texture fill, boolean offsets, int bottom, int top){
        this(outline, fill, offsets, bottom, top, 0f);
    }

    /**
     * Creates a progress bar actor with the specified attributes.
     *
     * @param outline asset for empty progress bar
     * @param fill asset for progress bar fill, must be the same size as {outline}
     * @param offsets are bottom/top offsets from the bottom/top of the fill asset or absolute values?
     * @param bottom distance from 0% fill and the bottom of the fill asset if offsets, else pixel height of 0% fill
     * @param top distance from 100% fill and the top of the fill asset if offsets, else pixel height of 100% fill
     * @param fill_amount proportion of bar that is currently filled
     */
    public FillBar(Texture outline, Texture fill, boolean offsets, int bottom, int top, float fill_amount){
        super();
        this.empty_border = bottom;
        this.full_border = offsets ? top : fill.getHeight() - top;
        this.fill_amount = fill_amount;
        int empty = full_border + (int)((1 - this.fill_amount) * (fill.getHeight() - this.full_border - this.empty_border));
        this.fill_asset = new TextureRegionDrawable(new TextureRegion(fill, 0, empty,
                fill.getWidth(), fill.getHeight()));
        this.fill = new Image(this.fill_asset);
        this.fill.setY(-empty);
        this.outline = new Image(new TextureRegion(outline));
        this.addActor(this.fill);
        this.addActor(this.outline);
    }

    public float getFillAmount() {
        return fill_amount;
    }

    public void setFillAmount(float fill_amount) {
        this.fill_amount = Math.min(Math.max(fill_amount,0.f),1.f);
        int empty = full_border + (int)((1 - this.fill_amount) * (fill.getHeight() - this.full_border - this.empty_border));
        fill_asset.getRegion().setRegion(0, empty, (int)fill.getWidth(), (int)fill.getHeight());
//        this.fill.setDrawable(fill_asset);
        this.fill.setY(-empty);
    }
}
