//package com.adisgrace.games.leveleditor;
//
//import com.adisgrace.games.util.Connector;
//import com.adisgrace.games.util.Connector.Direction;
//import com.adisgrace.games.models.FactNode;
//import com.badlogic.gdx.math.Vector2;
//import com.badlogic.gdx.utils.Array;
//import com.badlogic.gdx.utils.ArrayMap;
//
//import java.awt.image.AreaAveragingScaleFilter;
//import java.io.*;
//import java.util.*;
//
//public class LevelEditorModel {
//
//    private String level_name;
//    private int level_width = 0, level_height = 0;
//
//    /** Map from target names to the Target object  */
//    private Map<String, Target> targets;
//    private Map<String, FactNode> factnodes;
//    /** Map of parent name (key) to a map of child name (key) to the connectors between them, ordered
//     * from parent to child */
//    private Map<String, Map<String, Array<Connector>>> connections;
//
//    /** Map of locations to the connector at that location */
//    private HashMap<Vector2, Connector> connectorsAtCoords;
//    /** Map of locations to the FactNode at that location */
//    private HashMap<Vector2, FactNode> factnode_from_pos;
//    /** Hashmap of nodes that are discovered but not yet explored, for use in making connections */
//    private Array<String> discoveredNodes;
//    /** Array of locations that have been visited, for use in making connections */
//    private Array<Vector2> visited;
//
//    /** Predefined stress levels for FactNodes */
//    public enum StressRating{
//        NONE, LOW, MED, HIGH
//    }
//    private int stressRating_to_int(StressRating sr){
//        switch (sr){
//            case NONE:
//                return 0;
//            case LOW:
//                return 5;
//            case MED:
//                return 10;
//            case HIGH:
//                return 20;
//            default:
//                throw new RuntimeException("Invalid StressRating passed " + sr.toString());
//        }
//    }
//
//    /** Inner class representing the information about a target */
//    private class Target {
//        /** Isometric coordinates representing target's location */
//        float x;
//        float y;
//        /** Name of the target */
//        String name;
//        /** Paranoia stat of target */
//        int paranoia;
//        /** Maximum stress of target */
//        int maxStress;
//
//        /**
//         * Constructor for a Target with the specified attributes.
//         *
//         * @param x             x-coordinate of target in isometric space
//         * @param y             y-coordinate of target in isometric space
//         * @param name          must be unique in the level
//         * @param paranoia      must be positive, smaller is better
//         * @param maxStress     must be positive
//         */
//        private Target(float x, float y, String name, int paranoia, int maxStress) {
//            // Throws an exception if a target name is not unique
//            if(targets.containsKey(name))
//                throw new RuntimeException("Target names must be unique; there is an existing target "
//                        + name);
//            // Set attributes
//            this.x = x;
//            this.y = y;
//            this.name = name;
//            this.paranoia = paranoia;
//            this.maxStress = maxStress;
//        }
//
//        /**
//         * Constructor for a Target at 0,0 with the specified attributes.
//         *
//         * @param name          must be unique in the level
//         * @param paranoia      must be positive, smaller is better
//         * @param maxStress     must be positive
//         */
//        private Target(String name, int paranoia, int maxStress) {
//            // Throws an exception if a target name is not unique
//            if(targets.containsKey(name))
//                throw new RuntimeException("Target names must be unique; there is an existing target "
//                        + name);
//            // Set attributes
//            this.x = 0f;
//            this.y = 0f;
//            this.name = name;
//            this.paranoia = paranoia;
//            this.maxStress = maxStress;
//        }
//    }
//
//    /**
//     * Constructor.
//     */
//    public LevelEditorModel(){
//        targets = new HashMap<String, Target>();
//        factnodes = new HashMap<String, FactNode>();
//        connections = new HashMap<String, Map<String, Array<Connector>>>();
//
//        connectorsAtCoords = new HashMap<>();
//        factnode_from_pos = new HashMap<>();
//        discoveredNodes = new Array<>();
//    }
//
//    /**
//     * Makes a target with the specified attributes and stores it in the model.
//     *
//     * @param name          must be unique in the level
//     * @param paranoia      must be positive, smaller is better
//     * @param maxStress     must be positive
//     * @param pos           coordinates in isometric space
//     */
//    public void make_target(String name, int paranoia, int maxStress, Vector2 pos) {
//        Target tar = new Target(pos.x,pos.y,name,paranoia,maxStress);
//        targets.put(name,tar);
//    }
//
//    /**
//     * Deletes a target.
//     * @param targetName name of target to be deleted
//     */
//    public void delete_target(String targetName){
//        if(!targets.containsKey(targetName))
//            throw new RuntimeException("Invalid target passed " + targetName);
//        targets.remove(targetName);
//    }
//
//    /**
//     * Gets a map containing a target's attributes.
//     * This returns a live version of the target and any changes will be reflected in the model.
//     * @param targetName    Name of target
//     * @return              Target object
//     */
//    private Target getTarget(String targetName){
//        return targets.get(targetName);
//    }
//
//    /**
//     * Creates a factnode with the specified attributes
//     * @param factName must be unique within
//     * @param tsDmg
//     * @param psDmg
//     * @param locked
//     * @param coords
//     */
//    public void make_factnode(String factName, StressRating tsDmg, StressRating psDmg, boolean locked, Vector2 coords){
//        String summary;
//        String contents;
//        int tsDmg_ = stressRating_to_int(tsDmg);
//        // converts StressRating tsDmg to integer value tsDmg
//        // also assigns summary/contents based on tsDmg
//        switch(tsDmg){
//            case NONE:
//                contents = "You learn something entirely innocuous about your target.";
//                summary = "Nothing!";
//                break;
//            case LOW:
//                contents = "You crawl their web presence and find a few very embarrassing photos.";
//                summary = "Photos";
//                break;
//            case MED:
//                contents = "You dig through their history and discover a few citations or arrests some 10+ years ago.";
//                summary = "History";
//                break;
//            case HIGH:
//                contents = "You discover their involvement in some quite recent felonies that if exposed, would be prosecuted.";
//                summary = "Criminality";
//                break;
//            default:
//                throw new RuntimeException("Invalid tsDmg passed");
//        }
//        // converts StressRating psDmg to integer value psDmg
//        int psDmg_ = stressRating_to_int(psDmg);
//
//        FactNode factNode = new FactNode(factName, "untitled fact", contents, summary, new Array<String>(),
//                (int)coords.x, (int)coords.y, locked, tsDmg_, psDmg_, new Array<Array<Vector2>>(),
//                new Array<Array<String>>());
//        factnodes.put(factName, factNode);
//    }
//
//    /**
//     * Edits a particular attribute of a factnode.
//     * This does not check whether or not newFieldValue is a valid value of fieldToEdit.
//     * Accepts fields "title" (str), "content" (str), "summary" (str), "posX" (int), "posY" (int),
//     * pos (Vector2), "locked" (bool), "tsDmg" (StressRating), and "psDmg" (StressRating).
//     * @param factName name of factnode to edit
//     * @param fieldToEdit name of the attribute to be changed
//     * @param newFieldValue new value of attribute
//     */
//    public void edit_factnode(String factName, String fieldToEdit, Object newFieldValue){
//        if(!factnodes.containsKey(factName))
//            throw new RuntimeException("Invalid factnode passed " + factName);
//        FactNode fn = factnodes.get(factName);
//        switch(fieldToEdit){
//            case "title":
//                fn.setTitle(newFieldValue.toString());
//                break;
//            case "content":
//                fn.setContent(newFieldValue.toString());
//                break;
//            case "summary":
//                fn.setSummary(newFieldValue.toString());
//                break;
//            case "posX":
//                fn.setX((Integer)newFieldValue);
//                break;
//            case "posY":
//                fn.setY((Integer)newFieldValue);
//                break;
//            case "pos":
//                fn.setX((int)((Vector2)newFieldValue).x);
//                fn.setY((int)((Vector2)newFieldValue).y);
//                break;
//            case "locked":
//                fn.setLocked((Boolean)newFieldValue);
//                break;
//            case "tsDmg":
//                fn.setTargetStressDmg(stressRating_to_int((StressRating)newFieldValue));
//                String contents, summary;
//                switch((StressRating)newFieldValue){
//                    case NONE:
//                        contents = "You learn something entirely innocuous about your target.";
//                        summary = "Nothing!";
//                        break;
//                    case LOW:
//                        contents = "You crawl their web presence and find a few very embarrassing photos.";
//                        summary = "Photos";
//                        break;
//                    case MED:
//                        contents = "You dig through their history and discover a few citations or arrests some 10+ years ago.";
//                        summary = "History";
//                        break;
//                    case HIGH:
//                        contents = "You discover their involvement in some quite recent felonies that if exposed, would be prosecuted.";
//                        summary = "Criminality";
//                        break;
//                    default:
//                        throw new RuntimeException("Invalid tsDmg passed");
//                }
//                fn.setContent(contents);
//                fn.setSummary(summary);
//                break;
//            case "psDmg":
//                fn.setPlayerStressDmg(stressRating_to_int((StressRating)newFieldValue));
//                break;
//            default:
//                throw new RuntimeException("Invalid field passed " + fieldToEdit);
//        }
//    }
//
//    /**
//     * Remove a factnode.
//     * @param factName name of the factnode to be removed
//     */
//    public void delete_factnode(String factName){
//        if(!factnodes.containsKey(factName))
//            throw new RuntimeException("Invalid factnode passed " + factName);
//        factnodes.remove(factName);
//    }
//
//    /**
//     * Access a factnode's contents.
//     * @param factName name of the factnode
//     * @return pointer to factnode
//     */
//    public FactNode getFactNode(String factName){
//        if(!factnodes.containsKey(factName))
//            throw new RuntimeException("Invalid factnode passed " + factName);
//        return factnodes.get(factName);
//    }
//
//    /**
//     * Creates a connection from a parent (target or factnode) to a child (factnode)
//     * @param parentName name of the parent element
//     * @param childName name of the child element
//     * @param path LibGDX array of connectors the connection passes through ordered from parent to child
//     */
//    public void make_connection(String parentName, String childName, Array<Connector> path){
//        if(!connections.containsKey(parentName))
//            connections.put(parentName, new HashMap<String, Array<Connector>>());
//        connections.get(parentName).put(childName, path);
//    }
//
//    /**
//     * Creates a connection from a parent (target or factnode) to a child (factnode)
//     * @param parentName    name of the parent element
//     * @param childName     name of the child element
//     * @param path          Map of coordinates in the path and the connectors at those coordinates
//     */
//    public void make_connection(String parentName, String childName, ArrayMap<Vector2,Connector> path){
//        // Convert from arraymap to array
//        Array<Connector> connArr = new Array<>();
//        for (int k = 0; k < path.size; k++) {
//            connArr.add(path.getValueAt(k));
//        }
//        make_connection(parentName, childName, connArr);
//    }
//
//
//    /**
//     * Remove a connection between a parent and a child
//     * @param parentName
//     * @param childName
//     */
//    public void delete_connection(String parentName, String childName){
//        if(!connections.containsKey(parentName) || !connections.get(parentName).containsKey(childName))
//            throw new RuntimeException("No such connection between " + parentName + " and " + childName);
//        connections.get(parentName).remove(childName);
//    }
//
//    /**
//     * Takes in an array of connectors and creates connection paths between targets and nodes
//     * accordingly.
//     *
//     * @param mapConnectors    Array of connectors in the map
//     */
//    public void make_connections(Array<Connector> mapConnectors) {
//        // Fill hashmap of nodes at each given position
//        factnode_from_pos.clear();
//        for(FactNode fn : factnodes.values()){
//            factnode_from_pos.put(new Vector2(fn.getX(), fn.getY()), fn);
//        }
//        // Fill hashmap of connectors at each given position
//        connectorsAtCoords.clear();
//        for(Connector c : mapConnectors) {
//            connectorsAtCoords.put(new Vector2(c.xcoord,c.ycoord), c);
//        }
//        // Initialize array of locations that have been visited already
//        visited = new Array<>();
//
//        // Current tile we are looking at
//        Vector2 currLoc;
//        // Connector at current location
//        Connector currConn;
//        // Name of node
//        String nodeName;
//
//        // Start at each target as the root of a graph
//        for(Target target : targets.values()){
//            // Reset array of discovered nodes
//            discoveredNodes.clear();
//
//            // Get target location and the connector on top of it
//            currLoc = new Vector2(target.x,target.y);
//            currConn = connectorsAtCoords.get(currLoc);
//
//            // Mark current location as visited
//            visited.add(currLoc);
//
//            // Evaluate travel through this connector starting at the target, with no paths so far
//            travelThroughConnector(currConn,new ArrayMap<Vector2,Connector>(),target.name);
//
//            // While there are still discovered nodes, keep searching
//            while (discoveredNodes.size > 0) {
//                // Get first discovered node and start exploring from there
//                nodeName = discoveredNodes.pop();
//                currLoc = new Vector2(factnodes.get(nodeName).getX(),factnodes.get(nodeName).getY());
//                currConn = connectorsAtCoords.get(currLoc);
//
//                // Evaluate travel for each starting connector for paths from non-target nodes
//                travelThroughConnector(currConn,new ArrayMap<Vector2,Connector>(),nodeName);
//            }
//        }
//
//        System.out.println("Connections made");
//
//    }
//
//    /**
//     * Helper function that adds a direction to a path. If there is already a direction at that location,
//     * modifies the connector at that location to account for all directions there.
//     *
//     * @param pathSoFar     Path so far
//     * @param loc           Location at which to add a direction to the path
//     * @param dir           Direction to add to the path at the given location
//     */
//    private void addToPath(ArrayMap<Vector2, Connector> pathSoFar, Vector2 loc, Direction dir) {
//        // Initialize connector
//        Connector conn;
//
//        // If there is already a direction at that location
//        if (pathSoFar.containsKey(loc)) {
//            // Create new connector so as not to cause issues with other paths using the same connector
//            conn = new Connector(pathSoFar.get(loc).xcoord,pathSoFar.get(loc).ycoord,pathSoFar.get(loc).type);
//            // Update the existing connector to have an additional direction
//            conn.addDirToType(dir);
//        }
//        // Otherwise, just add new connector with that direction at that location
//        else {
//            conn = new Connector(loc,dir);
//        }
//        pathSoFar.put(loc,conn);
//    }
//
//    /**
//     * Helper function that handles traveling down all potential possible directions
//     * given by a connector.
//     *
//     * @param currConn  The connector to evaluate travel through.
//     * @param parent    The name of the parent that this connector is on a path from.
//     */
//    private void travelThroughConnector(Connector currConn, ArrayMap<Vector2, Connector> pathSoFar, String parent) {
//        // Get location of connector
//        Vector2 currLoc = new Vector2(currConn.xcoord, currConn.ycoord);
//        // Get type of connector
//        String type = currConn.type;
//        // Initialize caches
//        Vector2 newLoc;
//        char dir;
//        ArrayMap<Vector2, Connector> newPathSoFar;
//
//        // Iterate through directions of starting connector in same tile as target
//        for (int k = 0; k < type.length(); k++) {
//            // Get the new location given by the connector direction
//            dir = type.charAt(k);
//            newLoc = new Vector2(currLoc.x,currLoc.y);
//            newLoc.add(Connector.getDirVec(dir));
//            // If new location is unvisited
//            if (!visited.contains(newLoc,false)) {
//                // Create a new path containing this direction as the last step so far
//                newPathSoFar = new ArrayMap<>(pathSoFar);
//                addToPath(newPathSoFar, currLoc, Connector.toDir(dir));
//
//                // Start traveling down the path given by this direction
//                travelDownPath(newLoc,Connector.toDir(dir), newPathSoFar, parent);
//            }
//        }
//    }
//
//    /**
//     * Recursive function call to travel down a path, creating connections between nodes.
//     *
//     * @param nextLoc       Next location to travel to, in isometric coordinates
//     * @param arrivalDir    Direction from the previous location that led to this one
//     * @param pathSoFar     The path so far, in connectors and their corresponding locations
//     * @param parent        The name of the parent node that this path started at
//     */
//    private void travelDownPath(Vector2 nextLoc, Direction arrivalDir, ArrayMap<Vector2,Connector> pathSoFar, String parent) {
//        // Visit next location
//        visited.add(nextLoc);
//        // Add next location to the path so far, arriving through the opposite of the arrivalDir
//        // Ex. leaving the last location from the North arrives in this location from the South
//        addToPath(pathSoFar, nextLoc, Connector.oppositeDir(arrivalDir));
//
//        // End case: next tile has a node in it
//        if (factnode_from_pos.containsKey(nextLoc)) {
//            // End the search
//            // Get child name
//            String child = factnode_from_pos.get(nextLoc).getNodeName();
//            // Add path to map of connections
//            make_connection(parent,child,pathSoFar);
//            // Add node to discovered nodes
//            discoveredNodes.add(child);
//            return;
//        }
//        // If connector is not in next tile
//        if (!connectorsAtCoords.containsKey(nextLoc)) {
//            throw new RuntimeException("No connector in next tile, this is impossible");
//        }
//
//        // Get connector in next tile
//        Connector nextConn = connectorsAtCoords.get(nextLoc);
//
//        // Travel through the connector in the next tile
//        travelThroughConnector(nextConn, pathSoFar, parent);
//    }
//
//    /**
//     * Writes level to a json with the given filename
//     * Targets are written to jsons named after their names, ie John Smith -> JohnSmith.json
//     * @param filename name of level file (not including .json file extension)
//     */
//    public void make_level_json(String filename) throws IOException{
//        System.out.println("started saving level");
//        BufferedWriter out;
//        out = new BufferedWriter(new FileWriter("levels/" + filename + ".json"));
//        String targetlist = "", targetpositions = "";
//        for(Target target : targets.values()) {
//            targetlist += ", \"" + target.name.replaceAll(" ","") + ".json" + "\"";
//            targetpositions += ", [" + (int)(target.x) + ", " + (int)(target.y) + "]";
//        }
//        targetlist = "[" + targetlist.substring(2) + "]";
//        targetpositions = "[" + targetpositions.substring(2) + "]";
//        out.write("{\n" +
//                "\t\"name\": \"" + filename + "\",\n" +
//                "\t\"dims\": [" + level_width + ", " + level_height + "],\n" +
//                "\t\"targets\": " + targetlist + ",\n" +
//                "\t\"targetLocs\": " + targetpositions + "\n}"
//        );
//        out.flush();
//        out.close();
//        for(String targetname : targets.keySet())
//            make_target_json(targetname);
//        System.out.println("finished saving level");
//    }
//
//    /**
//     * Returns an array containing all factnodes that are descendants of a target.
//     * MUST BE CALLED AFTER make_connections !
//     * @param targetName name of target
//     * @return array of factnode objects
//     */
//    private Array<FactNode> get_target_facts(String targetName){
//        Array<String> facts = new Array<>();
//        Array<String> border = new Array<>();
//        border.add(targetName);
//        String parent;
//        Set<String> t;
//        while(!border.isEmpty()){
//            parent = border.pop();
//            if(!connections.containsKey(parent))
//                continue;
//            t = connections.get(parent).keySet();
//            facts.addAll(new Array(t.toArray()));
//            border.addAll(new Array(t.toArray()));
//        }
//        Array<FactNode> factnodes_ = new Array<>();
//        for(String factname : facts)
//            factnodes_.add(factnodes.get(factname));
//        return factnodes_;
//    }
//
//    /**
//     * Writes a target to a json
//     * Output file has the same name as the target, ie John Smith -> JohnSmith.json
//     * @param targetName name of target to compile a json for
//     */
//    public void make_target_json(String targetName) throws IOException{
//        System.out.printf("started saving target " + targetName);
//        BufferedWriter out;
//        out = new BufferedWriter(new FileWriter("levels/targets/" + targetName.replaceAll(" ","") + ".json"));
//        Array<FactNode> factnodes_ = get_target_facts(targetName);
//
//        int targetx = (int)((targets.get(targetName)).x);
//        int targety = (int)((targets.get(targetName)).y);
//
//        String firstnodes = "";
//        if(connections.containsKey(targetName)) {
//            for (String child : connections.get(targetName).keySet()) {
//                firstnodes += ", \"" + child + "\"";
//            }
//            firstnodes = "[" + firstnodes.substring(2) + "]";
//        } else firstnodes = "[]";
//
//        String firstconnections = "";
//        String firstconnectiontypes = "";
//        String strcache1 = "", strcache2 = "";
//        Array<Connector> connection;
//        if(connections.containsKey(targetName)) {
//            for (String child : connections.get(targetName).keySet()) {
//                connection = connections.get(targetName).get(child);
//                strcache1 = "";
//                strcache2 = "";
//                for (Connector c : connection) {
//                    strcache1 += ", [" + (c.xcoord - targetx) + "," + (c.ycoord - targetx) + "]";
//                    strcache2 += ", \"" + c.type + "\"";
//                }
//                firstconnections += ", [" + strcache1.substring(2) + "]";
//                firstconnectiontypes += ", [" + strcache2.substring(2) + "]";
//            }
//            firstconnections = "[" + firstconnections.substring(2) + "]";
//            firstconnectiontypes = "[" + firstconnectiontypes.substring(2) + "]";
//        } else {
//            firstconnections = "[]";
//            firstconnectiontypes = "[]";
//        }
//
//        String pod = "", nodeinfo, connections_, connectiontypes;
//        for(FactNode fact : get_target_facts(targetName)){
//            nodeinfo = ",\n{\n" +
//                    "\t\t\"nodeName\": \"" + fact.getNodeName() + "\",\n" +
//                    "\t\t\"title\": \"" + fact.getTitle() + "\",\n" +
//                    "\t\t\"coords\": [" + (fact.getX()-targetx) + "," + (fact.getY()-targety) + "],\n" +
//                    "\t\t\"locked\": " + fact.getLocked() + ",\n" +
//                    "\t\t\"content\": \"" + fact.getContent() + "\",\n" +
//                    "\t\t\"summary\": \"" + fact.getContent() + "\",\n"; //////////////////////TODO
//
//            strcache1 = "";
//            if(connections.containsKey(fact.getNodeName())) {
//                for (String childname : connections.get(fact.getNodeName()).keySet()) {
//                    strcache1 += ", \"" + childname + "\"";
//                }
//                strcache1 = "[" + strcache1.substring(2) + "]";
//            } else strcache1 = "[]";
//            nodeinfo += "\t\t\"children\": " + strcache1 + ",\n" +
//                    "\t\t\"targetStressDamage\": " + fact.getTargetStressDmg() + ",\n" +
//                    "\t\t\"playerStressDamage\": " + fact.getPlayerStressDmg() + ",\n";
//
//            connections_ = "";
//            connectiontypes = "";
//            if(connections.containsKey(fact.getNodeName())) {
//                for (String childname : connections.get(fact.getNodeName()).keySet()) {
//                    strcache1 = "";
//                    strcache2 = "";
//                    for (Connector c : connections.get(fact.getNodeName()).get(childname)) {
//                        strcache1 += ", [" + (c.xcoord - targetx) + "," + (c.ycoord - targety) + "]";
//                        strcache2 += ", \"" + c.type + "\"";
//                    }
//                    connections_ += ", [" + strcache1.substring(2) + "]";
//                    connectiontypes += ", [" + strcache2.substring(2) + "]";
//                }
//                connections_ = "[" + connections_.substring(2) + "]";
//                connectiontypes = "[" + connectiontypes.substring(2) + "]";
//            } else {
//                connections_ = "[]";
//                connectiontypes = "[]";
//            }
//
//            nodeinfo += "\t\t\"connectorCoords\": " + connections_ + ",\n" +
//                    "\t\t\"connectorTypes\": " + connectiontypes + "\n\t}";
//
//            pod += nodeinfo;
//        }
//        if(pod.length() > 0)
//            pod = "\t[\n" + pod.substring(2) + "\n\t]";
//        else pod = "\t[]";
//
//        out.write("{\n" +
//                "\t\"targetName\": \"" + targets.get(targetName).name + "\",\n" +
//                "\t\"paranoia\": " + targets.get(targetName).paranoia + ",\n" +
//                "\t\"maxStress\": " + targets.get(targetName).maxStress + ",\n" +
//                "\t\"firstNodes\": " + firstnodes + ",\n" +
//                "\t\"firstConnectors\": " + firstconnections + ",\n" +
//                "\t\"firstConnectorTypes\": " + firstconnectiontypes + ",\n" +
//                "\t\"pod\": " + pod + ",\n" +
//                "\t\"combos\": " + "[]" + "\n}"
//        );
//
//        out.flush();
//        out.close();
//
//        System.out.println("finished saving target " + targetName);
//    }
//}