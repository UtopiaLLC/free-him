package com.adisgrace.games.leveleditor;

import com.adisgrace.games.models.FactNode;
import com.adisgrace.games.models.TargetModel;
import com.adisgrace.games.util.Connector;
import com.adisgrace.games.util.Connector.Direction;
import static com.adisgrace.games.leveleditor.LevelEditorConstants.*;
import com.adisgrace.games.leveleditor.LevelEditorModel.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.io.*;
import java.util.*;

/**
 * Class for handling the save/load functionality of the level editor.
 *
 * Can take a level and create the corresponding JSON, and can take a
 * JSON and construct the corresponding level.
 */
public class LevelEditorParser {
    /** Level dimensions */
    private int level_width, level_height;
    /** Map from target names to the TargetTiles  */
    private Map<String, TargetTile> targets;
    private Map<String, NodeTile> nodes;
    /** Map of parent name (key) to a map of child name (key) to the connectors between them, ordered
     * from parent to child */
    private Map<String, Map<String, Array<Connector>>> connections;

    /** Map of locations to the connector at that location */
    private HashMap<Vector2, Connector> connectorsAtCoords;
    /** Map of locations to the NodeTile at that location */
    private HashMap<Vector2, NodeTile> nodesAtCoords;
    /** Hashmap of nodes that are discovered but not yet explored, for use in making connections */
    private Array<String> discoveredNodes;
    /** Array of locations that have been visited, for use in making connections */
    private Array<Vector2> visited;

    /**
     * Helper function that converts a stress rating to its integer equivalent.
     *
     * @param sr    Stress rating to convert
     * @return      Integer value of the given stress rating
     */
    private int stressRatingToInt(StressRating sr){
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
     * Helper function that converts an integer to its stress rating equivalent.
     *
     * Just in case, this will accept ranges of integers for low/medium/high.
     *
     * @param stress    Integer value to convert
     * @return          Stress rating of the given integer value
     */
    private StressRating intToStressRating(int stress){
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

    /**
     * Constructor for a LevelEditorParser.
     */
    public LevelEditorParser(){
        targets = new HashMap<>();
        nodes = new HashMap<>();
        connections = new HashMap<>();

        connectorsAtCoords = new HashMap<>();
        nodesAtCoords = new HashMap<>();
        discoveredNodes = new Array<>();
    }

    /**
     * Resets the parser so it can be used to parse a new level.
     */
    private void reset() {
        targets.clear();
        nodes.clear();
        connections.clear();
        connectorsAtCoords.clear();
        nodesAtCoords.clear();
        discoveredNodes.clear();
    }

    /**
     * Saves the given level as a JSON and returns whether the save was successful.
     *
     * @param model     The model of the level to save.
     * @return          Whether the level save was successful.
     */
    public boolean saveLevel(LevelEditorModel model) {
        reset();

        // TODO: if there are overlapping connectors, delete the extras

        // Get relevant data from the model
        ArrayMap<String, LevelTile> levelTiles = model.getLevelTiles();
        ArrayMap<Vector2, Array<String>> levelMap = model.getLevelMap();
        level_width = model.getLevelWidth();
        level_height = model.getLevelHeight();

        // Go through each grid tile that contains LevelTiles
        String c;
        LevelTile lt;
        String connector;
        Array<Connector> connectors = new Array<>();
        // For each occupied grid tile in the map
        for (Vector2 pos : levelMap.keys()) {
            connector = "";
            // For each LevelTile in this grid tile
            for (String tilename : levelMap.get(pos)) {
                // Get identifier that can be used to identify type of tile
                c = String.valueOf(tilename.charAt(0));
                // Get the actual LevelTile
                lt = levelTiles.get(tilename);

                // Initialize subclasses to potentially cast to
                NodeTile nt;
                TargetTile tt;
                switch (c) {
                    case "0": // TARGET
                        // Cast to TargetTile
                        tt = (TargetTile) lt;
                        // Add to array of targets
                        targets.put(tt.im.getName(), tt);
                        break;
                    case "1": // UNLOCKED NODE
                    case "2": // LOCKED NODE
                        // Cast to NodeTile
                        nt = (NodeTile) lt;
                        // Add to nodes and nodesAtCoords
                        nodes.put(nt.im.getName(), nt);
                        nodesAtCoords.put(pos, nt);
                        break;
                    default: // CONNECTOR
                        // Add the direction to the connector string
                        connector += c;
                        break;
                }
            }
            // Store new connector in array of connectors
            connectors.add(new Connector(pos,connector));
        }
        // Pass all connectors into the model
        make_connections(connectors);

        // Make JSON
        try {
            // Don't need to include ".json"
            make_level_json(model.getLevelName());
            System.out.println("Level " + model.getLevelName() + " Save Complete");
            return true;
        }
        catch(IOException e) {
            System.out.println("make_level_json failed");
            return false;
        }
    }

    /************************************************* CONNECTIONS *************************************************/

    /**
     * Creates a connection from a parent (target or factnode) to a child (factnode)
     * @param parentName name of the parent element
     * @param childName name of the child element
     * @param path LibGDX array of connectors the connection passes through ordered from parent to child
     */
    public void make_connection(String parentName, String childName, Array<Connector> path){
        if(!connections.containsKey(parentName))
            connections.put(parentName, new HashMap<String, Array<Connector>>());
        connections.get(parentName).put(childName, path);
    }

    /**
     * Creates a connection from a parent (target or factnode) to a child (factnode)
     * @param parentName    name of the parent element
     * @param childName     name of the child element
     * @param path          Map of coordinates in the path and the connectors at those coordinates
     */
    public void make_connection(String parentName, String childName, ArrayMap<Vector2,Connector> path){
        // Convert from arraymap to array
        Array<Connector> connArr = new Array<>();
        for (int k = 0; k < path.size; k++) {
            connArr.add(path.getValueAt(k));
        }
        make_connection(parentName, childName, connArr);
    }

    /**
     * Takes in an array of connectors and creates connection paths between targets and nodes
     * accordingly.
     *
     * @param mapConnectors    Array of connectors in the map
     */
    public void make_connections(Array<Connector> mapConnectors) {
        // Fill hashmap of nodes at each given position
        nodesAtCoords.clear();
        for(NodeTile fn : nodes.values()){
            nodesAtCoords.put(new Vector2(fn.x, fn.y), fn);
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

        // Start at each target as the root of a graph
        for(TargetTile target : targets.values()){
            // Reset array of discovered nodes
            discoveredNodes.clear();

            // Get target location and the connector on top of it
            currLoc = new Vector2(target.x,target.y);
            currConn = connectorsAtCoords.get(currLoc);

            // Mark current location as visited
            visited.add(currLoc);

            // Evaluate travel through this connector starting at the target, with no paths so far
            travelThroughConnector(currConn,new ArrayMap<Vector2,Connector>(),target.im.getName());

            // While there are still discovered nodes, keep searching
            while (discoveredNodes.size > 0) {
                // Get first discovered node and start exploring from there
                nodeName = discoveredNodes.pop();
                currLoc = new Vector2(nodes.get(nodeName).x,nodes.get(nodeName).y);
                currConn = connectorsAtCoords.get(currLoc);

                // Evaluate travel for each starting connector for paths from non-target nodes
                travelThroughConnector(currConn,new ArrayMap<Vector2,Connector>(),nodeName);
            }
        }

        System.out.println("Connections made");

    }

    /**
     * Helper function that adds a direction to a path. If there is already a direction at that location,
     * modifies the connector at that location to account for all directions there.
     *
     * @param pathSoFar     Path so far
     * @param loc           Location at which to add a direction to the path
     * @param dir           Direction to add to the path at the given location
     */
    private void addToPath(ArrayMap<Vector2, Connector> pathSoFar, Vector2 loc, Direction dir) {
        // Initialize connector
        Connector conn;

        // If there is already a direction at that location
        if (pathSoFar.containsKey(loc)) {
            // Create new connector so as not to cause issues with other paths using the same connector
            conn = new Connector(pathSoFar.get(loc).xcoord,pathSoFar.get(loc).ycoord,pathSoFar.get(loc).type);
            // Update the existing connector to have an additional direction
            conn.addDirToType(dir);
        }
        // Otherwise, just add new connector with that direction at that location
        else {
            conn = new Connector(loc,dir);
        }
        pathSoFar.put(loc,conn);
    }

    /**
     * Helper function that handles traveling down all potential possible directions
     * given by a connector.
     *
     * @param currConn  The connector to evaluate travel through.
     * @param parent    The name of the parent that this connector is on a path from.
     */
    private void travelThroughConnector(Connector currConn, ArrayMap<Vector2, Connector> pathSoFar, String parent) {
        // Get location of connector
        Vector2 currLoc = new Vector2(currConn.xcoord, currConn.ycoord);
        // Get type of connector
        String type = currConn.type;
        // Initialize caches
        Vector2 newLoc;
        char dir;
        ArrayMap<Vector2, Connector> newPathSoFar;

        // Iterate through directions of starting connector in same tile as target
        for (int k = 0; k < type.length(); k++) {
            // Get the new location given by the connector direction
            dir = type.charAt(k);
            newLoc = new Vector2(currLoc.x,currLoc.y);
            newLoc.add(Connector.getDirVec(dir));
            // If new location is unvisited
            if (!visited.contains(newLoc,false)) {
                // Create a new path containing this direction as the last step so far
                newPathSoFar = new ArrayMap<>(pathSoFar);
                addToPath(newPathSoFar, currLoc, Connector.toDir(dir));

                // Start traveling down the path given by this direction
                travelDownPath(newLoc,Connector.toDir(dir), newPathSoFar, parent);
            }
        }
    }

    /**
     * Recursive function call to travel down a path, creating connections between nodes.
     *
     * @param nextLoc       Next location to travel to, in isometric coordinates
     * @param arrivalDir    Direction from the previous location that led to this one
     * @param pathSoFar     The path so far, in connectors and their corresponding locations
     * @param parent        The name of the parent node that this path started at
     */
    private void travelDownPath(Vector2 nextLoc, Direction arrivalDir, ArrayMap<Vector2,Connector> pathSoFar, String parent) {
        // Visit next location
        visited.add(nextLoc);
        // Add next location to the path so far, arriving through the opposite of the arrivalDir
        // Ex. leaving the last location from the North arrives in this location from the South
        addToPath(pathSoFar, nextLoc, Connector.oppositeDir(arrivalDir));

        // End case: next tile has a node in it
        if (nodesAtCoords.containsKey(nextLoc)) {
            // End the search
            // Get child name
            String child = nodesAtCoords.get(nextLoc).im.getName();
            // Add path to map of connections
            make_connection(parent,child,pathSoFar);
            // Add node to discovered nodes
            discoveredNodes.add(child);
            return;
        }
        // If connector is not in next tile
        if (!connectorsAtCoords.containsKey(nextLoc)) {
            throw new RuntimeException("No connector in next tile, this is impossible");
        }

        // Get connector in next tile
        Connector nextConn = connectorsAtCoords.get(nextLoc);

        // Travel through the connector in the next tile
        travelThroughConnector(nextConn, pathSoFar, parent);
    }

    /******************************************** JSON PARSING: SAVE LEVEL ********************************************/

    /**
     * Writes level to a json with the given filename
     * Targets are written to jsons named after their names, ie John Smith -> JohnSmith.json
     * @param filename name of level file (not including .json file extension)
     */
    public void make_level_json(String filename) throws IOException{
        System.out.println("started saving level");
        BufferedWriter out;
        out = new BufferedWriter(new FileWriter("levels/" + filename + ".json"));
        String targetlist = "", targetpositions = "";
        for(TargetTile target : targets.values()) {
            targetlist += ", \"" + target.name.replaceAll(" ","") + ".json" + "\"";
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
     * Returns an array containing all NodeTiles that are descendants of a target.
     * MUST BE CALLED AFTER make_connections !
     * @param targetName    name of target
     * @return              array of NodeTile objects
     */
    private Array<NodeTile> get_target_facts(String targetName){
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
        Array<NodeTile> childNodes = new Array<>();
        for(String factname : facts)
            childNodes.add(nodes.get(factname));
        return childNodes;
    }

    /**
     * Writes a target to a json
     * Output file has the same name as the target, ie John Smith -> JohnSmith.json
     * @param targetName name of target to compile a json for
     */
    public void make_target_json(String targetName) throws IOException{
        // Get actual target name, as opposed to the one used for internal referencing
        String realTargetName = targets.get(targetName).name;

        System.out.printf("started saving target " + realTargetName);
        BufferedWriter out;
        out = new BufferedWriter(new FileWriter("levels/targets/" + realTargetName.replaceAll(" ","") + ".json"));
        Array<NodeTile> childNodes = get_target_facts(targetName);

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
                    strcache1 += ", [" + (c.xcoord - targetx) + "," + (c.ycoord - targety) + "]";
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
        for(NodeTile fact : childNodes){
            nodeinfo = ",\n{\n" +
                    "\t\t\"nodeName\": \"" + fact.im.getName() + "\",\n" +
                    "\t\t\"title\": \"" + fact.title + "\",\n" +
                    "\t\t\"coords\": [" + (fact.x-targetx) + "," + (fact.y-targety) + "],\n" +
                    "\t\t\"locked\": " + fact.locked + ",\n" +
                    "\t\t\"content\": \"" + fact.content + "\",\n" +
                    "\t\t\"summary\": \"" + fact.summary + "\",\n";

            strcache1 = "";
            if(connections.containsKey(fact.im.getName())) {
                for (String childname : connections.get(fact.im.getName()).keySet()) {
                    strcache1 += ", \"" + childname + "\"";
                }
                strcache1 = "[" + strcache1.substring(2) + "]";
            } else strcache1 = "[]";
            nodeinfo += "\t\t\"children\": " + strcache1 + ",\n" +
                    "\t\t\"targetStressDamage\": " + stressRatingToInt(fact.targetSR) + ",\n" +
                    "\t\t\"playerStressDamage\": " + stressRatingToInt(fact.playerSR) + ",\n";

            connections_ = "";
            connectiontypes = "";
            if(connections.containsKey(fact.im.getName())) {
                for (String childname : connections.get(fact.im.getName()).keySet()) {
                    strcache1 = "";
                    strcache2 = "";
                    for (Connector c : connections.get(fact.im.getName()).get(childname)) {
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

        out.write("\t{\n" +
                "\t\"targetName\": \"" + targets.get(targetName).name + "\",\n" +
                "\t\"paranoia\": " + targets.get(targetName).paranoia + ",\n" +
                "\t\"maxStress\": " + targets.get(targetName).maxStress + ",\n" +
                "\t\"traits\": " + targets.get(targetName).traitsAsString() + ",\n" +
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

    /******************************************** JSON PARSING: LOAD LEVEL ********************************************/
    /**
     * Returns the LevelEditorModel constructed from the level with the given filename.
     *
     * The model is then displayed in the level editor for further editing.
     *
     * @param levelfile The filename of the level to load into the level editor.
     * @return          A LevelEditorModel of the level file.
     */
    public LevelEditorModel loadLevel(String levelfile) {
        // If levelfile is missing the .json file extension, add it
        if (!levelfile.contains(".json")) levelfile += ".json";

        // Initialize model for level that's being loaded
        LevelEditorModel model = new LevelEditorModel();

        // Create JSON reader to parse through the level JSON
        JsonValue leveljson = new JsonReader().parse(Gdx.files.internal("levels/" + levelfile));

        // Get and store level name
        model.setLevelName(leveljson.get("name").asString());
        // Get and store level dimensions
        int[] dims = leveljson.get("dims").asIntArray();
        model.setLevelDimensions(dims[0], dims[1]);

        // Get iterator for locations of targets in level
        JsonValue locations = leveljson.get("targetLocs");
        JsonValue.JsonIterator itr = locations.iterator();
        // Initialize int array for location of each target in level
        int[] loc;

        // Go through array of targets, stored as filenames in the JSON
        String[] targetJsons = leveljson.get("targets").asStringArray();
        for(String targetfile : targetJsons){
            // Get location of target
            loc = itr.next().asIntArray();
            // Parse each target and load into level
            model = parseTarget(loc[0], loc[1], targetfile, model);
        }

        // Return filled model
        return model;
    }

    /**
     * Helper function that parses the connectors in a JSON and adds them to the given level model.
     *
     * This function takes in the connector coordinates and types as JsonValues.
     *
     * Note that the connector must add the target's coordinates to the stored location to get the true
     * isometric coordinates.
     *
     * @param targetX           x-coordinate of target that this connector belongs to
     * @param targetY           y-coordinate of target tat this connector belongs to
     * @param connectorCoords   JSON array of coordinates of connectors.
     * @param connectorTypes    JSON array of types of connectors.
     * @param model             The level model to add the connectors to.
     * @return                  The level model with the connectors added.
     */
    private LevelEditorModel parseConnectors(int targetX, int targetY, JsonValue connectorCoords,
                                             JsonValue connectorTypes, LevelEditorModel model) {
        // Create iterators for connector coordinates and types
        JsonValue.JsonIterator coordItr = connectorCoords.iterator();
        JsonValue.JsonIterator typeItr = connectorTypes.iterator();

        // Initialize int array for location of a connector
        int[] loc;
        // Initialize iterator for subarray of coordinates
        JsonValue.JsonIterator citr;
        // Initialize iterator for subarray of types
        JsonValue.JsonIterator titr;
        // Go through each connector given and add it to the model
        while (coordItr.hasNext()) {
            // This is now an array of int arrays
            citr = coordItr.next().iterator();
            // This is now an array of types
            titr = typeItr.next().iterator();
            // Go through subarrays of coordinates/types
            while (citr.hasNext()) {
                // Get connector location
                loc = citr.next().asIntArray();
                // Load connector into level model
                model.loadConnector(loc[0] + targetX, loc[1] + targetY, titr.next().asString());
            }
        }

        return model;
    }

    /**
     * Helper function that loads a target into the level editor, parsing a target JSON
     * with the given name and storing the data in the given model.
     *
     * @param x             The x-coordinate of the target in the level.
     * @param y             The y-coordinate of the target in the level.
     * @param targetfile    The name of the file that the target's data is stored in.
     * @param model         The model to store the parsed target data in.
     * @return              The model with the target's data included.
     */
    private LevelEditorModel parseTarget(int x, int y, String targetfile, LevelEditorModel model) {
        // Get parser for JSON
        JsonValue json = new JsonReader().parse(Gdx.files.internal("levels/targets/" + targetfile));
        // Initialize iterator for arrays
        JsonValue.JsonIterator itr;

        // Get main properties of target and load into the level editor
        model.loadTarget(x, y, json.getString("targetName"), json.getInt("paranoia"), json.getInt("maxStress"));

        // Load first connectors of this target into the level editor
        model = parseConnectors(x, y, json.get("firstConnectors"), json.get("firstConnectorTypes"), model);

        // Get nodes
        JsonValue nodesArr = json.get("pod");
        itr = nodesArr.iterator();

        // Initializations for parsing of each node
        // The node itself as a JSON object
        JsonValue node;
        // Int array for location of node
        int[] loc;

        // Iterate through nodes in pod and load them into the level as NodeTiles
        while (itr.hasNext()) {
            // Get next node
            node = itr.next();
            // Get coordinates
            loc = node.get("coords").asIntArray();
            // Load node into level
            model.loadNode(loc[0]+x, loc[1]+y, node.getString("nodeName"), node.getString("title"),
                    node.getBoolean("locked"), node.getString("content"), node.getString("summary"),
                    intToStressRating(node.getInt("targetStressDamage")),
                    intToStressRating(node.getInt("playerStressDamage")));

            // Now, need to handle connectors that are the children of this node
            model = parseConnectors(x, y, node.get("connectorCoords"), node.get("connectorTypes"), model);
        }

        return model;

        /**
        // TODO: get combos working when they're added to the level editor
        // Get combos
        // Initializations
        combos = new Array<TargetModel.Combo>();
        JsonValue combosArr = json.get("combos");
        itr = combosArr.iterator();
        TargetModel.Combo combo;
        Array<String> relatedFacts = new Array<>();

        // Iterate through combos, create each as a Combo, then add to array of combos
        while (itr.hasNext()) {
            node = itr.next();

            // Get related facts
            nodeArr = node.get("relatedFacts");
            nodeItr = nodeArr.iterator();
            relatedFacts.clear();
            while (nodeItr.hasNext()) {relatedFacts.add(nodeItr.next().asString());}
            relatedFacts.sort();

            // Construct and store combo
            combo = new TargetModel.Combo(new Array<>(relatedFacts), node.getString("overwrite"),
                    node.getString("comboSummary"), node.getInt("comboStressDamage"));
            combos.add(combo);
        }
         */
    }
}