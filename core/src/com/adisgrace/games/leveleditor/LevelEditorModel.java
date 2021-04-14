package com.adisgrace.games.leveleditor;

import com.adisgrace.games.util.Connector;
import com.adisgrace.games.models.FactNode;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LevelEditorModel {

    private String level_name;
    private int level_width, level_height;

    private Map<String, Map<String, Object>> targets;
    private Map<String, FactNode> factnodes;
    //    private Set<Connector> connections;
    private Map<String, Map<String, Array<Connector>>> connections;

    /** Predefined stress levels for FactNodes */
    public enum StressRating{
        NONE, LOW, MED, HIGH
    }
    private int stressRating_to_int(StressRating sr){
        switch (sr){
            case NONE:
                return 0;
            case LOW:
                return 5;
            case MED:
                return 10;
            case HIGH:
                return 20;
            default:
                throw new RuntimeException("Invalid StressRating passed " + sr.toString());
        }
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
     * fieldToEdit accepts "paranoia" (int), "pos" (Vector2), "posX" (int), "posY" (int),
     * "maxStress" (StressRating), and "targetName" (str).
     * @param targetName name of target to edit
     * @param fieldToEdit name of the attribute to be changed
     * @param newFieldValue new value of attribute
     */
    public void edit_target(String targetName, String fieldToEdit, Object newFieldValue){
        if(!targets.containsKey(targetName))
            throw new RuntimeException("Invalid target passed " + targetName);
        switch(fieldToEdit){
            case "paranoia":
            case "pos":
                targets.get(targetName).put(fieldToEdit, newFieldValue);
                break;
            case "maxStress":
                int maxStress = 50 + 5 * stressRating_to_int((StressRating)newFieldValue);
                targets.get(targetName).put(fieldToEdit, maxStress);
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
     * Changes a particular target's paranoia and maxStress.
     * @param targetName name of target to edit
     * @param paranoia must be positive, smaller is better
     * @param maxStress accepts LOW, MED, and HIGH
     */
    public void edit_target(String targetName, int paranoia, StressRating maxStress){
        if(!targets.containsKey(targetName))
            throw new RuntimeException("Invalid target passed " + targetName);
        Map<String, Object> target = targets.get(targetName);
        target.put("paranoia", paranoia);
        int maxStress_ = 50 + 5 * stressRating_to_int(maxStress);
        target.put("maxStress", maxStress_);
    }

    /**
     * Deletes a target.
     * @param targetName name of target to be deleted
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
        int tsDmg_ = stressRating_to_int(tsDmg);
        // converts StressRating tsDmg to integer value tsDmg
        // also assigns summary/contents based on tsDmg
        switch(tsDmg){
            case NONE:
                contents = "You learn something entirely innocuous about your target.";
                summary = "Nothing!";
                break;
            case LOW:
                contents = "You crawl their web presence and find a few very embarrassing photos.";
                summary = "Photos";
                break;
            case MED:
                contents = "You dig through their history and discover a few citations or arrests some 10+ years ago.";
                summary = "History";
                break;
            case HIGH:
                contents = "You discover their involvement in some quite recent felonies that if exposed, would be prosecuted.";
                summary = "Criminality";
                break;
            default:
                throw new RuntimeException("Invalid tsDmg passed");
        }
        // converts StressRating psDmg to integer value psDmg
        int psDmg_ = stressRating_to_int(psDmg);

        //TODO implement locked in FactNode
        //FactNode factNode = new FactNode(factName, "untitled fact", contents, summary, new Array<String>(),
        //        (int)coords.x, (int)coords.y, "asset_path", tsDmg_, psDmg_, locked);
        //factnodes.put(factName, factNode);
    }

    /**
     * Edits a particular attribute of a factnode.
     * This does not check whether or not newFieldValue is a valid value of fieldToEdit.
     * Accepts fields "title" (str), "content" (str), "summary" (str), "posX" (int), "posY" (int),
     * pos (Vector2), "locked" (bool), "tsDmg" (StressRating), and "psDmg" (StressRating).
     * @param factName name of factnode to edit
     * @param fieldToEdit name of the attribute to be changed
     * @param newFieldValue new value of attribute
     */
    public void edit_factnode(String factName, String fieldToEdit, Object newFieldValue){
        if(!factnodes.containsKey(factName))
            throw new RuntimeException("Invalid factnode passed " + factName);
        FactNode fn = factnodes.get(factName);
        switch(fieldToEdit){
            case "title":
                fn.setTitle(newFieldValue.toString());
                break;
            case "content":
                fn.setContent(newFieldValue.toString());
                break;
            case "summary":
                fn.setSummary(newFieldValue.toString());
                break;
            case "posX":
                fn.setX((Integer)newFieldValue);
                break;
            case "posY":
                fn.setY((Integer)newFieldValue);
                break;
            case "pos":
                fn.setX((int)((Vector2)newFieldValue).x);
                fn.setY((int)((Vector2)newFieldValue).y);
                break;
            case "locked":
                fn.setLocked((Boolean)newFieldValue);
                break;
            case "tsDmg":
                fn.setTargetStressDmg(stressRating_to_int((StressRating)newFieldValue));
                String contents, summary;
                switch((StressRating)newFieldValue){
                    case NONE:
                        contents = "You learn something entirely innocuous about your target.";
                        summary = "Nothing!";
                        break;
                    case LOW:
                        contents = "You crawl their web presence and find a few very embarrassing photos.";
                        summary = "Photos";
                        break;
                    case MED:
                        contents = "You dig through their history and discover a few citations or arrests some 10+ years ago.";
                        summary = "History";
                        break;
                    case HIGH:
                        contents = "You discover their involvement in some quite recent felonies that if exposed, would be prosecuted.";
                        summary = "Criminality";
                        break;
                    default:
                        throw new RuntimeException("Invalid tsDmg passed");
                }
                fn.setContent(contents);
                fn.setSummary(summary);
                break;
            case "psDmg":
                fn.setPlayerStressDmg(stressRating_to_int((StressRating)newFieldValue));
                break;
            default:
                throw new RuntimeException("Invalid field passed " + fieldToEdit);
        }
    }

    /**
     * Remove a factnode.
     * @param factName name of the factnode to be removed
     */
    public void delete_factnode(String factName){
        if(!factnodes.containsKey(factName))
            throw new RuntimeException("Invalid factnode passed " + factName);
        factnodes.remove(factName);
    }

    /**
     * Access a factnode's contents.
     * @param factName name of the factnode
     * @return pointer to factnode
     */
    public FactNode getFactNode(String factName){
        if(!factnodes.containsKey(factName))
            throw new RuntimeException("Invalid factnode passed " + factName);
        return factnodes.get(factName);
    }

    /**
     * Creates a connection from a parent (target or factnode) to a child (factnode)
     * @param parentName name of the parent element
     * @param childName name of the child element
     * @param path list of connectors the connection passes through ordered from parent to child
     */
    public void makeConnection(String parentName, String childName, Array<Connector> path){
        if(!connections.containsKey(parentName))
            connections.put(parentName, new HashMap<String, Array<Connector>>());
        connections.get(parentName).put(childName, path);
    }

    /**
     * Remove a connection between a parent and a child
     * @param parentName
     * @param childName
     */
    public void delete_connection(String parentName, String childName){
        if(!connections.containsKey(parentName) || !connections.get(parentName).containsKey(childName))
            throw new RuntimeException("No such connection between " + parentName + " and " + childName);
        connections.get(parentName).remove(childName);
    }


}
