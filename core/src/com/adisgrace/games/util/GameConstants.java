package com.adisgrace.games.util;

public final class GameConstants {
    /************************************************* LevelModel **************************************************/

    public static final int DEFAULT_LEVEL_DIM = 20;

    /************************************************* TraitModel **************************************************/

    /** How much stress the therapist heals */
    public static final int HEALING_CONST = 3;
    /** What percentage stress is being spread by the target */
    public static final float GOSSIP_CONST = .30f;
    /** How much stress off-putting targets deal*/
    public static final float OFF_PUTTING_CONST = 5;
    /** How much suspicion a target goes up by every turn for naturally suspicious*/
    public static final float NATURALLY_SUSPICIOUS_CONST = 5;
    /** Stress multiplier for sensitive targets*/
    public static final float SENSITIVE_MULTIPLIER = 1.15f;

    /************************************************* TargetModel **************************************************/

    /** Constant for inverse Paranoia check, made every (INV_PARANOIA_CONSTANT - paranoia) turns */
    public static final int INV_PARANOIA_CONSTANT = 7;
    /** Constants for low/medium/high suspicion */
    public static final int SUSPICION_LOW = 15;
    public static final int SUSPICION_MED = 25;
    public static final int SUSPICION_HIGH = 35;
    /** Constant for multiplier that stress damage is multiplied by for expose */
    public static final float EXPOSE_MULTIPLIER = 2.5f;

    /************************************************* NodeView **************************************************/

    /** Dimensions of map tile */
    public static final int TILE_HEIGHT = 256;
    public static final int TILE_WIDTH = 444;

    public static final float ADD = 0;
    public static final float SCALE_X = 444;
    public static final float SCALE_Y = 256;
    public static final float LOCKED_OFFSET = 114.8725f;

    /************************************************* PlayerModel **************************************************/

    public static final int DAILY_AP = 6;
    public static final int HACK_AP_COST = 2;
    public static final int SCAN_AP_COST = 2;
    public static final int THREATEN_AP_COST = 2;
    public static final int COERCE_AP_COST = 2;
    public static final int HARASS_AP_COST = 2;
    public static final int EXPOSE_AP_COST = 3;
    public static final int GASLIGHT_AP_COST = 2;
    public static final int DISTRACT_AP_COST = 2;

    public static final float STARTING_STRESS = 15;
    public static final float MAX_STRESS = 100;
    public static final float DREAM_STRESS_STDEV = 5;

    public static final float STARTING_BITECOIN = 30;
    public static final float DAILY_BITECOIN_COST = 10;
    public static final float SCAN_BITECOIN_CHANCE = 10;
    public static final float SCAN_BITECOIN = 10;

    public static final int OVERWORK_AP = 2;
    public static final float OVERWORK_STRESS_MEAN = 15;
    public static final float OVERWORK_STRESS_STDEV = 3;

    public static final float RELAX_STRESS_MEAN = 6;
    public static final float RELAX_STRESS_STDEV = 2;

    public static final float VTUBE_INCOME_MEAN = 20;
    public static final float VTUBE_INCOME_STDEV = 5;
    public static final int VTUBE_AP_COST = 3;

    /************************************************* MainMenu **************************************************/

    /** Constants for locations of buttons */
    /** Height of top of menu, relative to canvas height */
    public static final float MENU_HEIGHT = 0.37f;
    /** Button scale */
    public static final float BUTTON_SCALE = 0.8f;

    /************************************************* UIController **************************************************/

    public static final int DIALOG_WIDTH = 890;
    public static final int DIALOG_HEIGHT = 500;
    public static final int DIALOG_PREF_WIDTH = 350;

    /************************************************* GameController **************************************************/

    public static final int NODE_WORLD_WIDTH = 30;
    public static final int NODE_WORLD_HEIGHT = 30;

    public static final int SCREEN_WIDTH = 1280, SCREEN_HEIGHT = 720;
    public static final int RIGHT_SIDE_HEIGHT = 199;

    /************************************************* STRESS RATING **************************************************/

    /* Predefined stress ratings for FactNodes */
    public enum StressRating {
        NONE, LOW, MED, HIGH
    }

    /* Constants for stress rating values */
    public static final int SR_NONE = 0, SR_LOW = 5, SR_MED = 10, SR_HIGH = 20;

    /**
     * Helper function that converts a stress rating to its integer equivalent.
     *
     * @param sr    Stress rating to convert
     * @return      Integer value of the given stress rating
     */
    public static int stressRatingToInt(StressRating sr){
        switch (sr){
            case NONE:
                return SR_NONE;
            case LOW:
                return SR_LOW;
            case MED:
                return SR_MED;
            case HIGH:
                return SR_HIGH;
            default:
                throw new RuntimeException("Invalid StressRating passed " + sr.toString());
        }
    }

    /**
     * Helper function that converts a stress rating to a string that indicates its value.
     *
     * @param sr    Stress rating to convert
     * @return      String indicator of the given stress rating
     */
    public static String stressRatingToIndicator(StressRating sr){
        switch (sr){
            case NONE:
                return "";
            case LOW:
                return "(+)";
            case MED:
                return "(++)";
            case HIGH:
                return "(+++)";
            default:
                throw new RuntimeException("Invalid StressRating passed " + sr.toString());
        }
    }

    /**
     * Helper function that converts an integer to its stress rating equivalent.
     *
     * Just in case, this will accept ranges of integers for low/medium/high.
     *
     * @param stress    Integer value to convert
     * @return          Stress rating of the given integer value
     */
    public static StressRating intToStressRating(int stress){
        // If out of range
        if (stress < 0 || stress > ((SR_HIGH - SR_MED) / 2) + SR_HIGH) {
            throw new RuntimeException("Invalid stress value " + stress + " passed");
        }
        // stress = 0, so stress rating of NONE
        else if (stress == 0) {return StressRating.NONE;}
        // 0 < stress < halfway between LOW and MED, so stress rating of LOW
        else if (stress < (SR_LOW + SR_MED) / 2) {return StressRating.LOW;}
        // Halfway between LOW and MED < stress < halfway between MED and HIGH, so stress rating of MED
        else if (stress < (SR_MED + SR_HIGH) / 2) {return StressRating.MED;}
        // Anything else as long as it's not out of range, so stress rating of HIGH
        else {return StressRating.HIGH;}
    }
}
