package com.adisgrace.games.models;

import java.util.*;

import com.badlogic.gdx.utils.Array;

/**
 * "Traits" representation.
 *
 * A trait is the trait of a target, which have different outcomes when performing actions.
 *
 * Traits is stored as ArrayLists of TraitModel.Trait, and is stored as an array of all lowercase strings in the json.
 */
public class TraitModel {
    /** Enumeration representing the trait of a connector */
    public enum Trait{
        /** decreases the Paranoia stat of all targets in level by 1 */
        PARANOIAC,
        // Implementation in LevelModel.nextDay + targetModel.reduce_paranoia

        /** enemy decreases the stress of all targets in level over time */
        THERAPIST,
        // Implementation in WorldModel.nextTurn + targetModel.therapy

        /** spreads percentage of their suspicion to all targets in level */
        GOSSIP,
        // Implementation in WorldModel.nextTurn + targetModel.spread_gossip/receive_gossip

        /** interacting with them (blackmail skills) increases your stress */
        OFF_PUTTING,
        // Implemented by Tony Zhang

        /** after their suspicion is first raised, it automatically rises by a small amount every turn */
        NATURALLY_SUSPICIOUS,
        // Implemented by Tony Zhang

        /** you get more money when performing actions that get you money on them */
        RICH,

        /** hacking games are harder and/or failure has a greater penalty (cooldown) */
        TECHNOLOGICALLY_LITERATE,

        // cannot be implemented at the moment

        /** blackmail skills used against this target cost an extra AP */
        BAD_CONNECTION,
        // stuff changed includes LevelController.harass, WorldModel.harass, PlayerModel.harass\canHarass
        // PlayerModel.threaten/canThreaten, LevelController.threaten, Worldmodel.threaten
        // WorldModel.expose, LevelController.expose, PlayerModel.coerce/canCoerce

        /** hacking is easier/costs 1 less AP */
        TECHNOLOGICALLY_ILLITERATE,
        // stuff changed includes PlayerModel.hack/canHack/scan/canScan  LevelController.hack/scan
        // WorldModel.hack/scan

        /** blackmail options deal more stress damage */
        SENSITIVE
        // Implemented by Tony Zhang
    }

    /** How much stress the therapist heals */
    public static final int HEALING_CONST = 3;
    /** What percentage stress is being spread by the target */
    public static final float GOSSIP_CONST = .05f;
    /** How much stress off-putting targets deal*/
    public static final float OFF_PUTTING_CONST = 5;
    /** How much suspicion a target goes up by every turn for naturally suspicious*/
    public static final float NATURALLY_SUSPICIOUS_CONST = 5;
    /** Stress multiplier for sensitive targets*/
    public static final float SENSITIVE_MULTIPLIER = 1.15f;
    /** Traits of a target, stored as an arrayList of Traits */
    private ArrayList<Trait> traits;

    /**
     * Constructor for a traitModel. Saves the traits as an array.
     *
     * @param t     Traits of a target as an array of strings.
     */
    public TraitModel(Array<String> t){
        // Initializing traits with for-each loop
        traits = new ArrayList<>();
        for (String s: t) {
            switch (s){
                case "paranoiac":
                    traits.add(Trait.PARANOIAC);
                    break;
                case "therapist":
                    traits.add(Trait.THERAPIST);
                    break;
                case "gossip":
                    traits.add(Trait.GOSSIP);
                    break;
                case "off_putting":
                    traits.add(Trait.OFF_PUTTING);
                    break;
                case "naturally_suspicious":
                    traits.add(Trait.NATURALLY_SUSPICIOUS);
                    break;
                case "rich":
                    traits.add(Trait.RICH);
                    break;
                case "technologically_literate":
                    traits.add(Trait.TECHNOLOGICALLY_LITERATE);
                    break;
                case "bad_connection":
                    traits.add(Trait.BAD_CONNECTION);
                    break;
                case "technologically_illiterate":
                    traits.add(Trait.TECHNOLOGICALLY_ILLITERATE);
                    break;
                case "sensitive":
                    traits.add(Trait.SENSITIVE);
                    break;
            }
        }
    }

    /**
     * Empty constructor
     * */
    public TraitModel(){
        traits = new ArrayList<>();
    }

    /**
     * Returns whether the specific target is paranoiac.
     * */
    public boolean is_paranoiac(){
        return traits.contains(Trait.PARANOIAC);
    }

    /**
     * Returns whether the specific target is therapist.
     * */
    public boolean is_therapist(){
        return traits.contains(Trait.THERAPIST);
    }

    /**
     * Returns whether the specific target is gossip.
     * */
    public boolean is_gossip(){
        return traits.contains(Trait.GOSSIP);
    }

    /**
     * Returns whether the specific target is off_putting.
     * */
    public boolean is_off_putting(){
        return traits.contains(Trait.OFF_PUTTING);
    }

    /**
     * Returns whether the specific target is naturally_suspicious.
     * */
    public boolean is_naturally_suspicious(){
        return traits.contains(Trait.NATURALLY_SUSPICIOUS);
    }

    /**
     * Returns whether the specific target is rich.
     * */
    public boolean is_rich(){
        return traits.contains(Trait.RICH);
    }

    /**
     * Returns whether the specific target is technologically_literate.
     * */
    public boolean is_technologically_literate(){
        return traits.contains(Trait.TECHNOLOGICALLY_LITERATE);
    }

    /**
     * Returns whether the specific target is bad_connection.
     * */
    public boolean is_bad_connection(){
        return traits.contains(Trait.BAD_CONNECTION);
    }

    /**
     * Returns whether the specific target is technologically_illiterate.
     * */
    public boolean is_technologically_illiterate(){
        return traits.contains(Trait.TECHNOLOGICALLY_ILLITERATE);
    }

    /**
     * Returns whether the specific target is sensitive.
     * */
    public boolean is_sensitive(){
        return traits.contains(Trait.SENSITIVE);
    }
}
