package com.adisgrace.games.leveleditor;

import com.adisgrace.games.util.Connector;
import com.adisgrace.games.models.FactNode;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.awt.image.AreaAveragingScaleFilter;
import java.io.*;
import java.util.*;

public class LevelEditorModel {

    private String level_name;
    private int level_width = 0, level_height = 0;

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
        FactNode factNode = new FactNode(factName, "untitled fact", contents, summary, new Array<String>(),
                (int)coords.x, (int)coords.y, locked, tsDmg_, psDmg_, new Array<int[]>(), new Array<String>());
        factnodes.put(factName, factNode);
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
    public void make_connection(String parentName, String childName, Array<Connector> path){
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

    /**
     * Automatically detects and creates connections from an array of Connector objects
     * @param connections_ Array of connections on the map
     */
    public void make_connections(Array<Connector> connections_){
//        Map<Vector2,Map<String,Object>> target_from_pos = new HashMap<>();
//        for(Map<String,Object> t : targets.values()){
//            target_from_pos.put((Vector2) t.get("pos"), t);
//        }
        Map<Vector2,FactNode> factnode_from_pos = new HashMap<>();
        for(FactNode fn : factnodes.values()){
            factnode_from_pos.put(new Vector2(fn.getX(), fn.getY()), fn);
        }
        Map<Vector2,Connector> connector_from_pos = new HashMap<>();
        for(Connector c : connections_) {
            connector_from_pos.put(new Vector2(c.xcoord,c.ycoord), c);
        }

        Set<Vector2> seen;
        Array<Array<Vector2>> border;
        Array<Vector2> path;
        Array<Vector2> path2;
        Array<Connector> path3;
        Vector2 vec, vec2;
        String dirs, newdirs = "";
        for(Map<String,Object> target : targets.values()){
            path = new Array<>();
            path.add((Vector2)target.get("pos"));
            seen = new HashSet<>();
            seen.add((Vector2)target.get("pos"));
            border = new Array<>();
            border.add(path);
            while(!border.isEmpty()){
                path = border.pop();
                seen.add(path.peek());
                if(path.size > 1 && factnode_from_pos.containsKey(path.peek())) {
                    path3 = new Array<>();
                    dirs = "";
                    vec = path.first();
                    for(Vector2 pos : path){
                        vec2 = new Vector2(pos);
                        vec2.sub(vec);
                        if(vec2.y == 1) {
                            dirs += "N";
                            newdirs = "S";
                        }
                        else if(vec2.y == -1) {
                            dirs += "S";
                            newdirs = "N";
                        }
                        else if(vec2.x == 1) {
                            dirs += "E";
                            newdirs = "W";
                        }
                        else if(vec2.y == -1) {
                            dirs += "W";
                            newdirs = "E";
                        }
                        path3.add(new Connector((int)pos.x, (int)pos.y, dirs));
                        dirs = newdirs;
                    }
                    make_connection(
                            (target.get("pos").equals(path.first())
                                    ? target.get("targetName").toString()
                                    : factnode_from_pos.get(path.first()).getNodeName()),
                            factnode_from_pos.get(path.peek()).getNodeName(),
                            path3
                    );
                    path2 = new Array<>();
                    path2.add(path.peek());
                    border.add(path2);
                }
                if(connector_from_pos.get(path.peek()).type.indexOf('N') >= 0){
                    vec = new Vector2(path.peek().x, path.peek().y+1);
                    if(!seen.contains(vec)){
                        path2 = new Array<>(path);
                        path2.add(vec);
                        border.add(path2);
                    }
                }
                if(connector_from_pos.get(path.peek()).type.indexOf('S') >= 0){
                    vec = new Vector2(path.peek().x, path.peek().y-1);
                    if(!seen.contains(vec)) {
                        path2 = new Array<>(path);
                        path2.add(vec);
                        border.add(path2);
                    }
                }
                if(connector_from_pos.get(path.peek()).type.indexOf('E') >= 0){
                    vec = new Vector2(path.peek().x+1, path.peek().y);
                    if(!seen.contains(vec)) {
                        path2 = new Array<>(path);
                        path2.add(vec);
                        border.add(path2);
                    }
                }
                if(connector_from_pos.get(path.peek()).type.indexOf('W') >= 0){
                    vec = new Vector2(path.peek().x-1, path.peek().y);
                    if(!seen.contains(vec)) {
                        path2 = new Array<>(path);
                        path2.add(vec);
                        border.add(path2);
                    }
                }
            }
        }
    }

    /**
     * Writes level to a json with the given filename
     * Targets are written to jsons named after their names, ie John Smith -> JohnSmith.json
     * @param filename name of level file (not including .json file extension)
     */
    public void make_level_json(String filename) throws IOException{
        BufferedWriter out;
        out = new BufferedWriter(new FileWriter(filename + ".json"));
        String targetlist = "", targetpositions = "";
        for(Map<String,Object> target : targets.values()) {
            targetlist += ", \"" + target.get("targetName") + "\"";
            targetpositions += ", [" + (int)(((Vector2)target.get("pos")).x) + ", " +
                    (int)(((Vector2)target.get("pos")).y) + "]";
        }
        targetlist = "[" + targetlist.substring(2) + "]";
        targetpositions = "[" + targetpositions.substring(2) + "]";
        out.write("{\n" +
                "\t\"name\": " + filename + ",\n" +
                "\t\"dims\": [" + level_width + ", " + level_height + "],\n" +
                "\t\"targets\": " + targetlist + ",\n" +
                "\t\"targetLocs\": " + targetpositions + "\n}"
        );
        out.flush();
        out.close();
        for(String targetname : targets.keySet())
            make_target_json(targetname);
    }

    /**
     * Returns an array containing all factnodes that are descendants of a target.
     * MUST BE CALLED AFTER make_connections !
     * @param targetName name of target
     * @return array of factnode objects
     */
    private Array<FactNode> get_target_facts(String targetName){
        Array<String> facts = new Array<>();
        Array<String> border = new Array<>();
        border.add(targetName);
        String parent;
        while(!border.isEmpty()){
            parent = border.pop();
            facts.addAll(new Array(connections.get(parent).keySet().toArray()));
            border.addAll(new Array(connections.get(parent).keySet().toArray()));
        }
        Array<FactNode> factnodes_ = new Array<>();
        for(String factname : facts)
            factnodes_.add(factnodes.get(factname));
        return factnodes_;
    }

    /**
     * Writes a target to a json
     * Output file has the same name as the target, ie John Smith -> JohnSmith.json
     */
    public void make_target_json(String targetName) throws IOException{
        BufferedWriter out;
        out = new BufferedWriter(new FileWriter(targetName.replaceAll(" ","") + ".json"));
        Array<FactNode> factnodes_ = get_target_facts(targetName);

        int targetx = (int)((Vector2)(targets.get(targetName)).get("pos")).x;
        int targety = (int)((Vector2)(targets.get(targetName)).get("pos")).y;

        String firstnodes = "";
        for(String child : connections.get(targetName).keySet()) {
            firstnodes += ", \"" + child + "\"";
        }
        firstnodes = "[" + firstnodes.substring(2) + "]";

        String firstconnections = "";
        String firstconnectiontypes = "";
        String strcache1 = "", strcache2 = "";
        Array<Connector> connection;
        for(String child : connections.get(targetName).keySet()){
            connection = connections.get(targetName).get(child);
            strcache1 = "";
            strcache2 = "";
            for(Connector c : connection){
                strcache1 += ", [" + (c.xcoord-targetx) + "," + (c.ycoord-targetx) + "]";
                strcache2 += ", \"" + c.type + "\"";
            }
            firstconnections += ", [" + strcache1.substring(2) + "]";
            firstconnectiontypes += ", [" + strcache2.substring(2) + "]";
        }
        firstconnections = "[" + firstconnections.substring(2) + "]";
        firstconnectiontypes = "[" + firstconnectiontypes.substring(2) + "]";

        String pod = "", nodeinfo, connections_, connectiontypes;
        for(FactNode fact : get_target_facts(targetName)){
            nodeinfo = "\t{\n" +
                    "\t\t\"nodeName\": \"" + fact.getNodeName() + "\",\n" +
                    "\t\t\"title\": \"" + fact.getTitle() + "\",\n" +
                    "\t\t\"coords\": [" + (fact.getX()-targetx) + "," + (fact.getY()-targety) + "],\n" +
                    "\t\t\"locked\": " + fact.getLocked() + ",\n" +
                    "\t\t\"content\": " + fact.getContent() + ",\n" +
                    "\t\t\"summary\": " + fact.getSummary() + ",\n";

            strcache1 = "";
            for(String childname : connections.get(fact.getNodeName()).keySet()){
                strcache1 += ", \"" + childname + "\"";
            }
            strcache1 = "[" + strcache1.substring(2) + "]";
            nodeinfo += "\t\t\"children\": " + strcache1 + ",\n" +
                    "\t\t\"targetStressDamage\": " + fact.getTargetStressDmg() + ",\n" +
                    "\t\t\"playerStressDamage\": " + fact.getPlayerStressDmg() + ",\n";

            connections_ = "";
            connectiontypes = "";
            for(String childname : connections.get(fact.getNodeName()).keySet()){
                strcache1 = "";
                strcache2 = "";
                for(Connector c : connections.get(fact.getNodeName()).get(childname)){
                    strcache1 += ", [" + (c.xcoord-targetx) + "," + (c.ycoord-targety) + "]";
                    strcache2 += ", \"" + c.type + "\"";
                }
                connections_ += ", [" + strcache1.substring(2) + "]";
                connectiontypes += ", [" + strcache2.substring(2) + "]";
            }
            connections_ = "[" + connections_.substring(2) + "]";
            connectiontypes = "[" + connectiontypes.substring(2) + "]";

            nodeinfo += "\t\t\"connectorCoords\": " + connections_ + ",\n" +
                    "\t\t\"connectorTypes\": " + connectiontypes + "\n\t}";

            pod += nodeinfo;
        }

        out.write("{\n" +
                "\t\"targetName\": \"" + targets.get(targetName).get("targetName") + "\",\n" +
                "\t\"paranoia\": " + targets.get(targetName).get("paranoia") + ",\n" +
                "\t\"maxStress\": " + targets.get(targetName).get("maxStress") + ",\n" +
                "\t\"firstNodes\": " + firstnodes + ",\n" +
                "\t\"firstConnectors\": " + firstconnections + ",\n" +
                "\t\"firstConnectorTypes\": " + firstconnectiontypes + ",\n" +
                "\t\"pod\": " + pod + ",\n" +
                "\t\"combos\": " + "[]" + "\n}"
        );

        out.flush();
        out.close();
    }
}