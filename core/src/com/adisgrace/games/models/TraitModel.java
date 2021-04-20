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
        paranoiac,
        /** enemy decreases the stress of all targets in level over time */
        therapist,
        /** spreads percentage of their suspicion to all targets in level */
        gossip,
        /** interacting with them (blackmail skills) increases your stress */
        off_Putting,
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
                    traits.add(Trait.off_Putting);
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
    public boolean isParanoiac(){
        return traits.contains(Trait.paranoiac);
    }
}
