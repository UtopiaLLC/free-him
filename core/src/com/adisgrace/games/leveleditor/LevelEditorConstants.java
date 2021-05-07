package com.adisgrace.games.leveleditor;

import com.adisgrace.games.util.Connector;
import static com.adisgrace.games.util.GameConstants.*;
import com.adisgrace.games.models.TraitModel.Trait;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import java.io.File;

/**
 * Static file of constants, including assets, for the level editor, stored separately for convenience.
 */
public final class LevelEditorConstants {
    /** Screen dimensions */
    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;

    /** Skin for Scene2D elements */
    public static final Skin skin = new Skin(Gdx.files.internal("skins/neon-ui.json"));

    /** Order of connectors (N,E,S,W) */
    public static final Connector.Direction[] CONN_ORDER = {Connector.Direction.N, Connector.Direction.E, Connector.Direction.S, Connector.Direction.W};
    public static final String[] CONN_NAME_ORDER = {"N","E","S","W"};
    /** Constants to get the correct indices for node textures */
    public static final int TARGET_LOW = 0, UNLOCKED_LOW = 1, LOCKED_LOW = 2, TARGET_HIGH = 3, UNLOCKED_HIGH = 4, LOCKED_HIGH = 5;
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
            new TextureRegionDrawable(NODE_TEXTURES[TARGET_LOW]),
            new TextureRegionDrawable(NODE_TEXTURES[UNLOCKED_LOW]),
            new TextureRegionDrawable(NODE_TEXTURES[LOCKED_LOW]),
            new TextureRegionDrawable(NODE_TEXTURES[TARGET_HIGH]),
            new TextureRegionDrawable(NODE_TEXTURES[UNLOCKED_HIGH]),
            new TextureRegionDrawable(NODE_TEXTURES[LOCKED_HIGH])
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

    /** How far the form entries should be from the left side of the screen */
    public static final int FORM_X_OFFSET = 40;
    /** How far the topmost form entry should be from the top edge of the screen */
    public static final int FORM_Y_OFFSET = 250;
    /** How far the form entries should be spaced apart vertically */
    public static final int FORM_GAP = 30;

    /** Array of target traits available as options, but as strings */
    public static final String[] TRAIT_OPTIONS_STRINGS = {"paranoiac", "therapist", "gossip", "off_putting",
            "naturally_suspicious", "rich", "technologically_literate", "bad_connection", "technologically_illiterate",
            "sensitive"};
    /** Array of target traits available as options */
    public static final Trait[] TRAIT_OPTIONS = {Trait.PARANOIAC, Trait.THERAPIST, Trait.GOSSIP,
            Trait.OFF_PUTTING, Trait.NATURALLY_SUSPICIOUS, Trait.RICH, Trait.TECHNOLOGICALLY_LITERATE,
            Trait.BAD_CONNECTION, Trait.TECHNOLOGICALLY_ILLITERATE, Trait.SENSITIVE};

    /** Width of a target/node form, in terms of percentage of screen width */
    public static final float FORM_WIDTH = 0.23f;

    /** Default values for certain stats */
    public static final int DEFAULT_PARANOIA = 3;
    public static final int DEFAULT_MAX_STRESS = 50;

    /** Directory where levels are stored */
    public static final File LEVEL_DIRECTORY = new File("levels/");

    /** Generic messages for title/content/summary of generic nodes */
    public static final String GENERIC_TITLE = "Generic Node";
    public static final String[] GENERIC_CONTENT = {
        "There's nothing of use here... you'll have to keep digging.",
        "You find some information that might be a little bit useful.",
        "You find some information that you think will be relatively useful.",
        "You find a bombshell that you're certain was meant to stay secret. You can definitely use this."
    };
    public static final String[] GENERIC_SUMMARIES = {"", "Minor Secret", "Medium Secret", "Major Secret"};

    /*************************************************** FUNCTIONS ****************************************************/
    /**
     * Helper function that compares equality between two "nodes," represented as named images. Returns
     * if the nodes are not equal.
     *
     * Two nodes are equal if they are both null or if both aren't null and their names are the same.
     * Otherwise, they are not equal.
     *
     * @param im1 First node to compare equality of
     * @param im2 Second node to compare equality of
     * @return Whether or not the two nodes are not equal
     */
    public static boolean nodeNotEquals(Image im1, Image im2) {
        // Not equal if only one is null, or if both aren't null and the names aren't equal
        return (im1 != null && im2 == null) || (im1 == null && im2 != null) ||
                ((im1 != null && im2 != null) && !im1.getName().equals(im2.getName()));
    }

    /**
     * Returns the generic node content for a given target stress rating.
     *
     * @param targetSR  The target stress rating of the node
     * @return          The generic node content for the given target stress rating
     */
    public static String getGenericContent(StressRating targetSR) {
        return GENERIC_CONTENT[find(targetSR, SR_ORDER)];
    }

    /**
     * Returns the generic node summary for a given target stress rating.
     *
     * @param targetSR  The target stress rating of the node
     * @return          The generic node summary for the given target stress rating
     */
    public static String getGenericSummary(StressRating targetSR) {
        return GENERIC_SUMMARIES[find(targetSR, SR_ORDER)];
    }

    /********************************************* ISOMETRIC CONVERSIONS **********************************************/
    /**
     * Helper function that converts coordinates from world space to isometric space.
     *
     * @param coords Coordinates in world space to transform
     */
    public static void worldToIsometric(Vector2 coords) {
        float tempx = coords.x;
        float tempy = coords.y;
        coords.x = 0.57735f * tempx - tempy;
        coords.y = 0.57735f * tempx + tempy;
    }

    /**
     * Helper function that converts coordinates from isometric space to world space.
     *
     * @param coords Coordinates in isometric space to transform
     */
    public static void isometricToWorld(Vector2 coords) {
        float tempx = coords.x;
        float tempy = coords.y;
        coords.x = tempx * (0.5f * TILE_WIDTH) + tempy * (0.5f * TILE_WIDTH);
        coords.y = -tempx * (0.5f * TILE_HEIGHT) + tempy * (0.5f * TILE_HEIGHT);
    }

    /**
     * Helper function that gets the center of an isometric grid tile nearest to the given coordinates.
     * Returns the vector of the nearest isometric grid tile in isometric space.
     *
     * Called when snapping an image to the center of a grid tile.
     *
     * @param coords    World coordinates of the location we want to find the nearest isometric center to
     * @return          Isometric coordinate of nearest isometric grid tile
     */
    public static void nearestIsoCenter(Vector2 coords) {
        // Transform world coordinates to isometric space
        worldToIsometric(coords);

        // Find the nearest isometric center and return in isometric space
        // I don't know why you divide x by the height and not the width, but for some reason it
        // works and dividing x by width doesn't
        coords.set(Math.round(coords.x / TILE_HEIGHT), Math.round(coords.y / TILE_HEIGHT));
    }

    /********************************************** ARRAY ORDER FINDING ***********************************************/
    /**
     * Finds the index of a value in the array.
     *
     * Returns the index of the given value in the array, or -1 if the value was not found.
     *
     * @param val       Value in array
     * @param arr       Array that value belongs to
     * @return          Index of the value in the array
     */
    public static <T>int find(T val, T[] arr) {
        // Loop through array until given value is found in the array
        for (int k=0; k<arr.length; k++) {
            if (arr[k].equals(val)) {return k;}
        }
        // Return -1 if value was not found
        return -1;
    }

    /**
     * Helper function that returns the index to the next value in the array.
     * <p>
     * When called with an array of connector types as input, it returns the character representing
     * the connector to rotate to next. The order goes from North -> East -> South -> West -> North.
     * <p>
     * When called with an array of node stress rating button types as input, it returns the name of
     * the next button type. The order goes from None -> Low -> Med -> High -> None.
     *
     * @param curr  Current entry in the array.
     * @param order Array of objects representing the order.
     * @return Index of the entry that is next in the array order
     */
    public static <T> int nextEntry(T curr, T[] order) {
        // Find current entry's index
        int k = find(curr, order);
        // Raise exception if not found
        if (k < 0) {
            throw new RuntimeException("Entry is not in array");
        }
        // Return index of next entry in order
        return (find(curr, order) + 1) % order.length;
    }
}
