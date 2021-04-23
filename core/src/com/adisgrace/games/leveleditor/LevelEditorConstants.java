package com.adisgrace.games.leveleditor;

import com.adisgrace.games.util.Connector;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * Static file of constants, including assets, for the level editor, stored separately for convenience.
 */
public final class LevelEditorConstants {
    /** Order of connectors (N,E,S,W) */
    public static final Connector.Direction[] CONN_ORDER = {Connector.Direction.N, Connector.Direction.E, Connector.Direction.S, Connector.Direction.W};
    public static final String[] CONN_NAME_ORDER = {"N","E","S","W"};
    /** Array of all textures for nodes */
    public static final Texture[] NODE_TEXTURES = new Texture[]{
            new Texture(Gdx.files.internal("leveleditor/N_TargetMaleIndividualLow_1.png")),
            new Texture(Gdx.files.internal("leveleditor/N_UnlockedIndividualLow_1.png")),
            new Texture(Gdx.files.internal("leveleditor/N_LockedIndividualLow_1.png")),
            new Texture(Gdx.files.internal("leveleditor/N_TargetMaleIndividual_1.png")),
            new Texture(Gdx.files.internal("leveleditor/N_UnlockedIndividual_1.png")),
            new Texture(Gdx.files.internal("leveleditor/N_LockedIndividual_2.png"))
    };
    /** Array of all TextureRegionDrawables for nodes */
    public static final TextureRegionDrawable[] NODE_TRDS = new TextureRegionDrawable[]{
            new TextureRegionDrawable(NODE_TEXTURES[0]),
            new TextureRegionDrawable(NODE_TEXTURES[1]),
            new TextureRegionDrawable(NODE_TEXTURES[2]),
            new TextureRegionDrawable(NODE_TEXTURES[3]),
            new TextureRegionDrawable(NODE_TEXTURES[4]),
            new TextureRegionDrawable(NODE_TEXTURES[5])
    };
    /** Array of TextureRegionDrawables of all node creation buttons, in order */
    public static final TextureRegionDrawable[] ADD_NODE_TRD_ORDER = new TextureRegionDrawable[]{
            new TextureRegionDrawable(new Texture(Gdx.files.internal("leveleditor/buttons/LE_AddNodeTarget_1.png"))),
            new TextureRegionDrawable(new Texture(Gdx.files.internal("leveleditor/buttons/LE_AddNodeUnlocked_1.png"))),
            new TextureRegionDrawable(new Texture(Gdx.files.internal("leveleditor/buttons/LE_AddNodeLocked_1.png")))
    };
    /** Array of TextureRegionDrawables of all mode changing buttons, in order */
    public static final TextureRegionDrawable[] CHANGE_MODE_TRD_ORDER = new TextureRegionDrawable[]{
            new TextureRegionDrawable(new Texture(Gdx.files.internal("leveleditor/buttons/LE_MoveMode_1.png"))),
            new TextureRegionDrawable(new Texture(Gdx.files.internal("leveleditor/buttons/LE_EditMode_1.png"))),
            new TextureRegionDrawable(new Texture(Gdx.files.internal("leveleditor/buttons/LE_DeleteMode_1.png"))),
            new TextureRegionDrawable(new Texture(Gdx.files.internal("leveleditor/buttons/LE_DrawMode_1.png"))),
            new TextureRegionDrawable(new Texture(Gdx.files.internal("leveleditor/buttons/LE_SaveLevel_1.png")))
    };

    /** Predefined stress levels for FactNodes */
    public enum StressRating{
        NONE, LOW, MED, HIGH
    }

    /** Order of stress rating buttons (None, Low, Medium, High) */
    public static final StressRating[] SR_ORDER = {StressRating.NONE, StressRating.LOW, StressRating.MED, StressRating.HIGH};
    public static final String[] SR_NAME_ORDER = {"None", "Low", "Medium", "High"};
    /** Array of TextureRegionDrawables of all stress rating buttons, in order */
    public static final TextureRegionDrawable[] SR_TRD_ORDER = new TextureRegionDrawable[]{
            new TextureRegionDrawable(new Texture(Gdx.files.internal("leveleditor/buttons/LE_NodeNone_1.png"))),
            new TextureRegionDrawable(new Texture(Gdx.files.internal("leveleditor/buttons/LE_NodeLow_1.png"))),
            new TextureRegionDrawable(new Texture(Gdx.files.internal("leveleditor/buttons/LE_NodeMed_1.png"))),
            new TextureRegionDrawable(new Texture(Gdx.files.internal("leveleditor/buttons/LE_NodeHigh_1.png"))),
    };
    /** TextureRegionDrawable for blank stress rating button */
    public static final TextureRegionDrawable SR_TRD_BLANK = new TextureRegionDrawable(
            new Texture(Gdx.files.internal("leveleditor/buttons/LE_NodeBlank_1.png")));

    /** Dimensions of map tile */
    public static final float TILE_HEIGHT = 256.0f;
    public static final float TILE_WIDTH = 444.0f;
    /** Constant for the y-offset for different node types */
    public static final float LOCKED_OFFSET = 114.8725f;

    /** Scale of the buttons in the toolbar */
    public static final float BUTTON_SCALE = 0.5f;
    /** Width of buttons in the toolbar in pixels */
    public static final int BUTTON_WIDTH = 100;
    /** Gap between two buttons in pixels */
    public static final int BUTTON_GAP = 60;
    /** How far to the right the toolbar should be offset from the left edge of the screen, in pixels */
    public static final int TOOLBAR_X_OFFSET = 10;
    /** How far down the toolbar should be offset from the top edge of the screen, in pixels */
    public static final int TOOLBAR_Y_OFFSET = 60;
}
