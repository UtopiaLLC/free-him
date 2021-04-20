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
 * Traits is stored as strings.
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

}
