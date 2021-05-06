package com.adisgrace.games.util;

public final class GameConstants {

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
