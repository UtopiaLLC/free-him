package com.adisgrace.games.leveleditor;

import com.adisgrace.games.FactNode;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;
import java.util.Map;

public class LevelEditorModel {

    private String level_name;
    private int level_width, level_height;

    private Map<String, Map<String, Object>> targets;
    private Map<String, FactNode> factnodes;
    private Map<String, Map<String, Array<Connector>>> connections;

    /** Predefined stress levels for FactNodes */
    public enum StressRating{
        NONE, LOW, MED, HIGH
    }

    /**
     * Constructor.
     */
    public LevelEditorModel(){
        targets = new HashMap<String, Map<String, Object>>();
        factnodes = new HashMap<String, FactNode>();
        connections = new HashMap<String, Map<String, Array<Connector>>>();
    }

    /**
     * Makes a new target at 0,0 with the specified attributes.
     * @param targetName must be unique in the level
     * @param paranoia must be positive, smaller is better
     * @param maxStress must be positive
     */
    public void make_target(String targetName, int paranoia, int maxStress){
        if(targets.containsKey(targetName))
            throw new RuntimeException("Target names must be unique; there is an existing target "
                    + targetName);
        HashMap<String, Object> target = new HashMap<String, Object>();
        target.put("targetName", targetName);
        target.put("paranoia", paranoia);
        target.put("maxStress", maxStress);
        target.put("posX", 0);
        target.put("posY", 0);
        targets.put(targetName, target);
    }

    /**
     * Makes a new target at the specified position with the specified attributes.
     * @param targetName must be unique in the level
     * @param paranoia must be positive, smaller is better
     * @param maxStress must be positive
     * @param pos coordinates
     */
    public void make_target(String targetName, int paranoia, int maxStress, Vector2 pos){
        HashMap<String, Object> target = new HashMap<String, Object>();
        target.put("targetName", targetName);
        target.put("paranoia", paranoia);
        target.put("maxStress", maxStress);
        target.put("pos", pos);
        targets.put(targetName, target);
    }

    /**
     * Edits a particular attribute of a target.
     * This does not check whether or not newFieldValue is a valid value of fieldToEdit.
     * @param targetName name of target to edit
     * @param fieldToEdit name of the attribute to be changed
     * @param newFieldValue new value of attribute
     */
    public void edit_target(String targetName, String fieldToEdit, Object newFieldValue){
        if(!targets.containsKey(targetName))
            throw new RuntimeException("Invalid target passed " + targetName);
        switch(fieldToEdit){
            case "paranoia":
            case "maxStress":
            case "pos":
                targets.get(targetName).put(fieldToEdit, newFieldValue);
                break;
            case "posX":
                ((Vector2)(targets.get(targetName).get("pos"))).x = (Integer) newFieldValue;
            case "posY":
                ((Vector2)(targets.get(targetName).get("pos"))).y = (Integer) newFieldValue;
            case "targetName":
                if(targets.containsKey(newFieldValue.toString()))
                    throw new RuntimeException("Target names must be unique; there is an existing target "
                            + newFieldValue.toString());
                Map<String, Object> target = targets.get(targetName);
                targets.remove(targetName);
                targets.put(newFieldValue.toString(), target);
            default:
                throw new RuntimeException("Invalid field name passed");
        }
    }

    /**
     * Changes a particular target's paranoia and maxStress.
     * @param targetName name of target to edit
     * @param paranoia must be positive, smaller is better
     * @param maxStress must be positive
     */
    public void edit_target(String targetName, int paranoia, int maxStress){
        if(!targets.containsKey(targetName))
            throw new RuntimeException("Invalid target passed " + targetName);
        Map<String, Object> target = targets.get(targetName);
        target.put("paranoia", paranoia);
        target.put("maxStress", maxStress);
    }

    /**
     * Deletes a target.
     * @param targetName
     */
    public void delete_target(String targetName){
        if(!targets.containsKey(targetName))
            throw new RuntimeException("Invalid target passed " + targetName);
        targets.remove(targetName);
    }

    /**
     * Gets a map containing a target's attributes.
     * This returns a live version of the target and any changes will be reflected in the model.
     * @param targetName
     * @return Map with keys "targetName" (String), "paranoia" (int), "maxStress" (int), "posX" (int), "posY" (int)
     */
    public Map<String, Object> getTarget(String targetName){
        return targets.get(targetName);
    }

    /**
     * Creates a factnode with the specified attributes
     * @param factName must be unique within
     * @param tsDmg
     * @param psDmg
     * @param locked
     * @param coords
     */
    public void make_factnode(String factName, StressRating tsDmg, StressRating psDmg, boolean locked, Vector2 coords){
        String summary;
        String contents;
        int tsDmg_;
        // converts StressRating tsDmg to integer value tsDmg
        // also assigns summary/contents based on tsDmg
        switch(tsDmg){
            case NONE:
                contents = "You learn something entirely innocuous about your target.";
                summary = "Nothing!";
                tsDmg_ = 0;
                break;
            case LOW:
                contents = "You crawl their web presence and find a few very embarrassing photos.";
                summary = "Photos";
                tsDmg_ = 5;
                break;
            case MED:
                contents = "You dig through their history and discover a few citations or arrests some 10+ years ago.";
                summary = "History";
                tsDmg_ = 10;
                break;
            case HIGH:
                contents = "You discover their involvement in some quite recent felonies that if exposed, would be prosecuted.";
                summary = "Criminality";
                tsDmg_ = 20;
                break;
            default:
                throw new RuntimeException("Invalid tsDmg passed");
        }
        // converts StressRating psDmg to integer value psDmg
        int psDmg_;
        switch(psDmg){
            case NONE:
                psDmg_ = 0;
                break;
            case LOW:
                psDmg_ = 5;
                break;
            case MED:
                psDmg_ = 10;
                break;
            case HIGH:
                psDmg_ = 20;
                break;
            default:
                throw new RuntimeException("Invalid psDmg passed");
        }
        //TODO implement locked in FactNode
        FactNode factNode = new FactNode(factName, "untitled fact", contents, summary, new Array<String>(),
                (int)coords.x, (int)coords.y, "asset_path", tsDmg_, psDmg_, locked);
        factnodes.put(factName, factNode);
    }

    /**
     * Edits a particular attribute of a factnode.
     * This does not check whether or not newFieldValue is a valid value of fieldToEdit.
     * @param factName name of factnode to edit
     * @param fieldToEdit name of the attribute to be changed
     * @param newFieldValue new value of attribute
     */
    public void edit_factnode(String factName, String fieldToEdit, Object newFieldValue){
        switch(fieldToEdit){
            //TODO continue
//            case
        }
    }



}
