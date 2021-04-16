package com.adisgrace.games.leveleditor;

import com.adisgrace.games.util.Connector;
import com.adisgrace.games.models.FactNode;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;

import java.awt.image.AreaAveragingScaleFilter;
import java.io.*;
import java.util.*;

public class LevelEditorModel {

    private String level_name;
    private int level_width = 0, level_height = 0;

    /** Map from target names to the Target object  */
    private Map<String, Target> targets;
    private Map<String, FactNode> factnodes;
    /** Map of parent name (key) to a map of child name (key) to the connectors between them, ordered
     * from parent to child */
    private Map<String, Map<String, Array<Connector>>> connections;

    /** Map of locations to the connector at that location */
    private HashMap<Vector2, Connector> connectorsAtCoords;
    /** Map of locations to the FactNode at that location */
    private HashMap<Vector2, FactNode> factnode_from_pos;
    /** Hashmap of nodes that are discovered but not yet explored, for use in making connections */
    private Array<String> discoveredNodes;
    /** Array of locations that have been visited, for use in making connections */
    private Array<Vector2> visited;

    /** Constants for steps taken in the various direction a connector can go */
    private static final Vector2 N_STEP = new Vector2(0,1);
    private static final Vector2 E_STEP = new Vector2(1,0);
    private static final Vector2 S_STEP = new Vector2(0,-1);
    private static final Vector2 W_STEP = new Vector2(-1,0);

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

    /** Inner class representing the information about a target */
    private class Target {
        /** Isometric coordinates representing target's location */
        float x;
        float y;
        /** Name of the target */
        String name;
        /** Paranoia stat of target */
        int paranoia;
        /** Maximum stress of target */
        int maxStress;

        /**
         * Constructor for a Target with the specified attributes.
         *
         * @param x             x-coordinate of target in isometric space
         * @param y             y-coordinate of target in isometric space
         * @param name          must be unique in the level
         * @param paranoia      must be positive, smaller is better
         * @param maxStress     must be positive
         */
        private Target(float x, float y, String name, int paranoia, int maxStress) {
            // Throws an exception if a target name is not unique
            if(targets.containsKey(name))
                throw new RuntimeException("Target names must be unique; there is an existing target "
                        + name);
            // Set attributes
            this.x = x;
            this.y = y;
            this.name = name;
            this.paranoia = paranoia;
            this.maxStress = maxStress;
        }

        /**
         * Constructor for a Target at 0,0 with the specified attributes.
         *
         * @param name          must be unique in the level
         * @param paranoia      must be positive, smaller is better
         * @param maxStress     must be positive
         */
        private Target(String name, int paranoia, int maxStress) {
            // Throws an exception if a target name is not unique
            if(targets.containsKey(name))
                throw new RuntimeException("Target names must be unique; there is an existing target "
                        + name);
            // Set attributes
            this.x = 0f;
            this.y = 0f;
            this.name = name;
            this.paranoia = paranoia;
            this.maxStress = maxStress;
        }
    }

    /**
     * Constructor.
     */
    public LevelEditorModel(){
        targets = new HashMap<String, Target>();
        factnodes = new HashMap<String, FactNode>();
        connections = new HashMap<String, Map<String, Array<Connector>>>();

        connectorsAtCoords = new HashMap<>();
        factnode_from_pos = new HashMap<>();
        discoveredNodes = new Array<>();
    }

    /**
     * Makes a target with the specified attributes and stores it in the model.
     *
     * @param name          must be unique in the level
     * @param paranoia      must be positive, smaller is better
     * @param maxStress     must be positive
     * @param pos           coordinates in isometric space
     */
    public void make_target(String name, int paranoia, int maxStress, Vector2 pos) {
        Target tar = new Target(pos.x,pos.y,name,paranoia,maxStress);
        targets.put(name,tar);
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
     * @param targetName    Name of target
     * @return              Target object
     */
    private Target getTarget(String targetName){
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
     * @param b     generic bool to allow this to overload the other make_connection
     */
    public void make_connection(String parentName, String childName, Array<Connector> path, boolean b){
        if(!connections.containsKey(parentName))
            connections.put(parentName, new HashMap<String, Array<Connector>>());
        connections.get(parentName).put(childName, path);
    }

    /**
     * Creates a connection from a parent (target or factnode) to a child (factnode)
     * @param parentName name of the parent element
     * @param childName name of the child element
     * @param path list of locations the connection passes through ordered from parent to child
     */
    public void make_connection(String parentName, String childName, Array<Vector2> path){
        // For each location in the path, get the connector and store in an array of connectors
        Array<Connector> connArr = new Array<>();
        for (Vector2 pos : path) {
            connArr.add(connectorsAtCoords.get(pos));
        }
        make_connection(parentName,childName,connArr,true);
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
     * Takes in an array of connectors and creates connection paths between targets and nodes
     * accordingly.
     *
     * @param mapConnectors    Array of connectors in the map
     */
    public void make_connections(Array<Connector> mapConnectors) {
        // Fill hashmap of nodes at each given position
        factnode_from_pos.clear();
        for(FactNode fn : factnodes.values()){
            factnode_from_pos.put(new Vector2(fn.getX(), fn.getY()), fn);
        }
        // Fill hashmap of connectors at each given position
        connectorsAtCoords.clear();
        for(Connector c : mapConnectors) {
            connectorsAtCoords.put(new Vector2(c.xcoord,c.ycoord), c);
        }
        // Initialize array of locations that have been visited already
        visited = new Array<>();

        // Current tile we are looking at
        Vector2 currLoc;
        // Connector at current location
        Connector currConn;
        // Name of node
        String nodeName;

        // Start at each target as the root of the graph
        for(Target target : targets.values()){
            // Reset array of discovered nodes
            discoveredNodes.clear();

            // Get target location and the connector on top of it
            currLoc = new Vector2(target.x,target.y);
            currConn = connectorsAtCoords.get(currLoc);

            // Mark current location as visited
            visited.add(currLoc);

            // Iterate through directions in connector
            for (int k = 0; k < currConn.type.length(); k++) {
                // Get the new location given by the connector direction
                Vector2 loc = new Vector2(currLoc.x,currLoc.y);
                loc.add(getDir(currConn.type.charAt(k)));
                // If new location is unvisited
                if (!visited.contains(loc,false)) {
                    // Start traveling down the path given by this direction
                    travelDownPath(loc, new Array<Vector2>(), target.name);
                }
            }

            // While there are still discovered nodes, keep searching
            while (discoveredNodes.size > 0) {
                // Get first discovered node and start exploring from there
                nodeName = discoveredNodes.pop();
                currLoc = new Vector2(factnodes.get(nodeName).getX(),factnodes.get(nodeName).getY());
                currConn = connectorsAtCoords.get(currLoc);

                // Iterate through directions in connector
                for (int k = 0; k < currConn.type.length(); k++) {
                    // Get the new location given by the connector direction
                    Vector2 loc = new Vector2(currLoc.x,currLoc.y);
                    loc.add(getDir(currConn.type.charAt(k)));
                    // If new location is unvisited
                    if (!visited.contains(loc,false)) {
                        // Start traveling down the path given by this direction
                        travelDownPath(loc, new Array<Vector2>(), nodeName);
                    }
                }
            }

        }

    }

    /**
     * Recursive function call to travel down a path
     */
    private void travelDownPath(Vector2 nextLoc, Array<Vector2> pathSoFar, String parent) {
        // Visit next location
        visited.add(nextLoc);
        // Add this location to the path so far
        pathSoFar.add(nextLoc);

        // End case: next tile has a node in it
        if (factnode_from_pos.containsKey(nextLoc)) {
            // End the search
            // Get child name
            String child = factnode_from_pos.get(nextLoc).getNodeName();
            // Add path to map of connections
            make_connection(parent,child,pathSoFar);
            // Add node to discovered nodes
            discoveredNodes.add(child);
            System.out.println("Connection made");
            return;
        }
        // If connector is not in next tile
        if (!connectorsAtCoords.containsKey(nextLoc)) {
            // TODO: fix why this sometimes happens
            //throw new RuntimeException("No connector in next tile, this is impossible");
            return;
        }

        // Get connector in next tile
        Connector nextConn = connectorsAtCoords.get(nextLoc);

        // Iterate through directions in connector
        for (int k = 0; k < nextConn.type.length(); k++) {
            // Get the new location given by the connector direction
            Vector2 loc = new Vector2(nextLoc.x,nextLoc.y);
            loc.add(getDir(nextConn.type.charAt(k)));
            // If new location is unvisited
            if (!visited.contains(loc,false)) {
                // Start traveling down the path given by this direction
                travelDownPath(loc, new Array<Vector2>(), parent);
            }
        }
    }

    /**
     * Helper function that returns a step in a direction based on the direction
     * given in a connector.
     *
     * @param d     One of the characters in "NESW," representing a connector direction
     * @return      The vector that steps in the direction given by the input
     */
    private Vector2 getDir(char d) {
        // Get directional step based on character
        switch(d) {
            case 'N':
                return N_STEP;
            case 'E':
                return E_STEP;
            case 'S':
                return S_STEP;
            case 'W':
                return W_STEP;
            default:
                throw new RuntimeException("Can only take in NESW");
        }
    }

    /**
     * Automatically detects and creates connections from an array of Connector objects
     * @param connections_ Array of connections on the map
    public void make_connections(Array<Connector> connections_){
        // Create and fill hashmap of nodes at each given position
        Map<Vector2,FactNode> factnode_from_pos = new HashMap<>();
        for(FactNode fn : factnodes.values()){
            factnode_from_pos.put(new Vector2(fn.getX(), fn.getY()), fn);
        }
        // Create and fill hashmap of connectors at each given position
        Map<Vector2,Connector> connector_from_pos = new HashMap<>();
        for(Connector c : connections_) {
            connector_from_pos.put(new Vector2(c.xcoord,c.ycoord), c);
        }

        // Initialize caches
        Set<Vector2> seen;
        Array<Array<Vector2>> border;
        Array<Vector2> path;
        Array<Vector2> path2;
        Array<Connector> path3;
        Vector2 vec, vec2;
        String dirs, newdirs = "";

        // For each target in the level
        for(Map<String,Object> target : targets.values()){
            // Initialize path from the target
            path = new Array<>();
            // Start at target location
            path.add((Vector2)target.get("pos"));
            // Initialize tracker for which tiles have been visited
            // Used to ensure that the search doesn't backtrack
            seen = new HashSet<>();
            // Mark tile containing target as visited
            seen.add((Vector2)target.get("pos"));
            // Contains all the potentially valid paths that have been found so far
            border = new Array<>();
            border.add(path);

            // Continue until no more valid paths remain
            while(!border.isEmpty()){
                // Check last valid path that remains
                path = border.pop();
                // Mark last tile in path as visited
                seen.add(path.peek());
                // If it's an actual path that leads to a FactNode
                if(path.size > 1 && factnode_from_pos.containsKey(path.peek())){
                    path3 = new Array<>();
                    dirs = "";
                    vec = path.first();
                    Vector2 pos;
                    for(int i = 1; i < path.size; i++){
                        pos = path.get(i);
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

                // Check last tile of path for connectors, and if there is one, add it accordingly
                addToPath(path, connector_from_pos, border);
            }
        }
    }

    /**
     *
     */

    /**
     * Helper function that checks if there is a connector at the end of this path,
     * and if so, creates a new path with the connector appended to it and adds the
     * new path to the stack of valid paths.
     *
     * @param path          Path to check the end of for a connector
     * @param connectors    Map of connectors at their locations
     * @param border        Stack of valid paths so far
    private void addToPath(Array<Vector2> path, Map<Vector2,Connector> connectors, Array<Array<Vector2>> border) {
        // Initialize vector cache
        Vector2 vec;
        // Initialize new path
        Array<Vector2> newPath;


        if(connectors.get(path.peek()).type.indexOf('N') >= 0){
            vec = new Vector2(path.peek().x, path.peek().y+1);
            if(!seen.contains(vec)){
                newPath = new Array<>(path);
                newPath.add(vec);
                border.add(newPath);
            }
        }
        if(connectors.get(path.peek()).type.indexOf('S') >= 0){
            vec = new Vector2(path.peek().x, path.peek().y-1);
            if(!seen.contains(vec)) {
                newPath = new Array<>(path);
                newPath.add(vec);
                border.add(newPath);
            }
        }
        if(connectors.get(path.peek()).type.indexOf('E') >= 0){
            vec = new Vector2(path.peek().x+1, path.peek().y);
            if(!seen.contains(vec)) {
                newPath = new Array<>(path);
                newPath.add(vec);
                border.add(newPath);
            }
        }
        if(connectors.get(path.peek()).type.indexOf('W') >= 0){
            vec = new Vector2(path.peek().x-1, path.peek().y);
            if(!seen.contains(vec)) {
                newPath = new Array<>(path);
                newPath.add(vec);
                border.add(newPath);
            }
        }
    }
    */

    /**
     * Writes level to a json with the given filename
     * Targets are written to jsons named after their names, ie John Smith -> JohnSmith.json
     * @param filename name of level file (not including .json file extension)
     */
    public void make_level_json(String filename) throws IOException{
        System.out.println("started saving level");
        BufferedWriter out;
        out = new BufferedWriter(new FileWriter(filename + ".json"));
        String targetlist = "", targetpositions = "";
        for(Target target : targets.values()) {
            targetlist += ", \"" + target.name + "\"";
            targetpositions += ", [" + (int)(target.x) + ", " + (int)(target.y) + "]";
        }
        targetlist = "[" + targetlist.substring(2) + "]";
        targetpositions = "[" + targetpositions.substring(2) + "]";
        out.write("{\n" +
                "\t\"name\": \"" + filename + "\",\n" +
                "\t\"dims\": [" + level_width + ", " + level_height + "],\n" +
                "\t\"targets\": " + targetlist + ",\n" +
                "\t\"targetLocs\": " + targetpositions + "\n}"
        );
        out.flush();
        out.close();
        for(String targetname : targets.keySet())
            make_target_json(targetname);
        System.out.println("finished saving level");
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
        Set<String> t;
        while(!border.isEmpty()){
            parent = border.pop();
            if(!connections.containsKey(parent))
                continue;
            t = connections.get(parent).keySet();
            facts.addAll(new Array(t.toArray()));
            border.addAll(new Array(t.toArray()));
        }
        Array<FactNode> factnodes_ = new Array<>();
        for(String factname : facts)
            factnodes_.add(factnodes.get(factname));
        return factnodes_;
    }

    /**
     * Writes a target to a json
     * Output file has the same name as the target, ie John Smith -> JohnSmith.json
     * @param targetName name of target to compile a json for
     */
    public void make_target_json(String targetName) throws IOException{
        System.out.printf("started saving target " + targetName);
        BufferedWriter out;
        out = new BufferedWriter(new FileWriter(targetName.replaceAll(" ","") + ".json"));
        Array<FactNode> factnodes_ = get_target_facts(targetName);

        int targetx = (int)((targets.get(targetName)).x);
        int targety = (int)((targets.get(targetName)).y);

        String firstnodes = "";
        if(connections.containsKey(targetName)) {
            for (String child : connections.get(targetName).keySet()) {
                firstnodes += ", \"" + child + "\"";
            }
            firstnodes = "[" + firstnodes.substring(2) + "]";
        } else firstnodes = "[]";

        String firstconnections = "";
        String firstconnectiontypes = "";
        String strcache1 = "", strcache2 = "";
        Array<Connector> connection;
        if(connections.containsKey(targetName)) {
            for (String child : connections.get(targetName).keySet()) {
                connection = connections.get(targetName).get(child);
                strcache1 = "";
                strcache2 = "";
                for (Connector c : connection) {
                    strcache1 += ", [" + (c.xcoord - targetx) + "," + (c.ycoord - targetx) + "]";
                    strcache2 += ", \"" + c.type + "\"";
                }
                firstconnections += ", [" + strcache1.substring(2) + "]";
                firstconnectiontypes += ", [" + strcache2.substring(2) + "]";
            }
            firstconnections = "[" + firstconnections.substring(2) + "]";
            firstconnectiontypes = "[" + firstconnectiontypes.substring(2) + "]";
        } else {
            firstconnections = "[]";
            firstconnectiontypes = "[]";
        }

        String pod = "", nodeinfo, connections_, connectiontypes;
        for(FactNode fact : get_target_facts(targetName)){
            nodeinfo = ",\n{\n" +
                    "\t\t\"nodeName\": \"" + fact.getNodeName() + "\",\n" +
                    "\t\t\"title\": \"" + fact.getTitle() + "\",\n" +
                    "\t\t\"coords\": [" + (fact.getX()-targetx) + "," + (fact.getY()-targety) + "],\n" +
                    "\t\t\"locked\": " + fact.getLocked() + ",\n" +
                    "\t\t\"content\": \"" + fact.getContent() + "\",\n" +
                    "\t\t\"summary\": \"" + fact.getSummary() + "\",\n";

            strcache1 = "";
            if(connections.containsKey(fact.getNodeName())) {
                for (String childname : connections.get(fact.getNodeName()).keySet()) {
                    strcache1 += ", \"" + childname + "\"";
                }
                strcache1 = "[" + strcache1.substring(2) + "]";
            } else strcache1 = "[]";
            nodeinfo += "\t\t\"children\": " + strcache1 + ",\n" +
                    "\t\t\"targetStressDamage\": " + fact.getTargetStressDmg() + ",\n" +
                    "\t\t\"playerStressDamage\": " + fact.getPlayerStressDmg() + ",\n";

            connections_ = "";
            connectiontypes = "";
            if(connections.containsKey(fact.getNodeName())) {
                for (String childname : connections.get(fact.getNodeName()).keySet()) {
                    strcache1 = "";
                    strcache2 = "";
                    for (Connector c : connections.get(fact.getNodeName()).get(childname)) {
                        strcache1 += ", [" + (c.xcoord - targetx) + "," + (c.ycoord - targety) + "]";
                        strcache2 += ", \"" + c.type + "\"";
                    }
                    connections_ += ", [" + strcache1.substring(2) + "]";
                    connectiontypes += ", [" + strcache2.substring(2) + "]";
                }
                connections_ = "[" + connections_.substring(2) + "]";
                connectiontypes = "[" + connectiontypes.substring(2) + "]";
            } else {
                connections_ = "[]";
                connectiontypes = "[]";
            }

            nodeinfo += "\t\t\"connectorCoords\": " + connections_ + ",\n" +
                    "\t\t\"connectorTypes\": " + connectiontypes + "\n\t}";

            pod += nodeinfo;
        }
        if(pod.length() > 0)
            pod = "\t[\n" + pod.substring(2) + "\n\t]";
        else pod = "\t[]";

        out.write("{\n" +
                "\t\"targetName\": \"" + targets.get(targetName).name + "\",\n" +
                "\t\"paranoia\": " + targets.get(targetName).paranoia + ",\n" +
                "\t\"maxStress\": " + targets.get(targetName).maxStress + ",\n" +
                "\t\"firstNodes\": " + firstnodes + ",\n" +
                "\t\"firstConnectors\": " + firstconnections + ",\n" +
                "\t\"firstConnectorTypes\": " + firstconnectiontypes + ",\n" +
                "\t\"pod\": " + pod + ",\n" +
                "\t\"combos\": " + "[]" + "\n}"
        );

        out.flush();
        out.close();

        System.out.println("finished saving target " + targetName);
    }
}