package com.adisgrace.games.models;

import java.util.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonReader;

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
        paranoiac, // Implementation in LevelModel constructor + targetModel.reduce_paranoia
        /** enemy decreases the stress of all targets in level over time */
        therapist, // Implementation in WorldModel.nextTurn + targetModel.therapy
        /** spreads percentage of their suspicion to all targets in level */
        gossip,
        /** interacting with them (blackmail skills) increases your stress */
        off_putting,
        /** after their suspicion is first raised, it automatically rises by a small amount every turn */
        naturally_suspicious,
        /** you get more money when performing actions that get you money on them */
        rich,
        /** hacking games are harder and/or failure has a greater penalty (cooldown) */
        technologically_literate,
        /** skills used against this target cost an extra AP */
        bad_connection,
        /** hacking is easier/costs less AP */
        technologically_illiterate,
        /** blackmail options deal more stress damage */
        sensitive
    }

    /** How much stress the therapist heals */
    public static final int HEALING_CONST = 3;
    /** Traits of a target, stored as an arrayList of Traits */
    private ArrayList<Trait> traits;

    /**
     * Constructor for a traitModel. Saves the traits as an array.
     *
     * @param t     Traits of a target as an array of strings.
     */
    public TraitModel(Array<String> t){
        // Initializing traits with for-each loop
        for (String s: t) {
            switch (s){
                case "paranoiac":
                    traits.add(Trait.paranoiac);
                    break;
                case "therapist":
                    traits.add(Trait.therapist);
                    break;
                case "gossip":
                    traits.add(Trait.gossip);
                    break;
                case "off_Putting":
                    traits.add(Trait.off_putting);
                    break;
                case "naturally_suspicious":
                    traits.add(Trait.naturally_suspicious);
                    break;
                case "rich":
                    traits.add(Trait.rich);
                    break;
                case "technologically_literate":
                    traits.add(Trait.technologically_literate);
                    break;
                case "bad_connection":
                    traits.add(Trait.bad_connection);
                    break;
                case "technologically_illiterate":
                    traits.add(Trait.technologically_illiterate);
                    break;
                case "sensitive":
                    traits.add(Trait.sensitive);
                    break;
            }
        }
    }

    /**
     * Returns whether the specific target is paranoiac.
     * */
    public boolean is_paranoiac(){
        return traits.contains(Trait.paranoiac);
    }

    /**
     * Returns whether the specific target is therapist.
     * */
    public boolean is_therapist(){
        return traits.contains(Trait.therapist);
    }

    /**
     * Returns whether the specific target is gossip.
     * */
    public boolean is_gossip(){
        return traits.contains(Trait.gossip);
    }

    /**
     * Returns whether the specific target is off_putting.
     * */
    public boolean is_off_putting(){
        return traits.contains(Trait.off_putting);
    }

    /**
     * Returns whether the specific target is naturally_suspicious.
     * */
    public boolean is_naturally_suspicious(){
        return traits.contains(Trait.naturally_suspicious);
    }

    /**
     * Returns whether the specific target is rich.
     * */
    public boolean is_rich(){
        return traits.contains(Trait.rich);
    }

    /**
     * Returns whether the specific target is technologically_literate.
     * */
    public boolean is_technologically_literate(){
        return traits.contains(Trait.technologically_literate);
    }

    /**
     * Returns whether the specific target is bad_connection.
     * */
    public boolean is_bad_connection(){
        return traits.contains(Trait.bad_connection);
    }

    /**
     * Returns whether the specific target is technologically_illiterate.
     * */
    public boolean is_technologically_illiterate(){
        return traits.contains(Trait.technologically_illiterate);
    }

    /**
     * Returns whether the specific target is sensitive.
     * */
    public boolean is_sensitive(){
        return traits.contains(Trait.sensitive);
    }
}
