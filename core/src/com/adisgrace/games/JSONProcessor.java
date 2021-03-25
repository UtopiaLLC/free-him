import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
  
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

public class JSONProcessor {
    public static void main(String[] args) throws Exception 
    {
        // Get parser for JSON
        Object obj = new JSONParser().parse(new FileReader("./targets/PatrickWestfield.json"));
        // Cast to JSONObject
        JSONObject json = (JSONObject) obj;
          
        // Get main properties of target
        String targetName = (String) json.get("targetName");
        int paranoia = ((Long) json.get("paranoia")).intValue();
        int maxStress = ((Long) json.get("maxStress")).intValue();

        // Initialize iterator for arrays
        Iterator itr;
        
        // Get neighbors and convert to JSONArray
        JSONArray neighborsArr = (JSONArray) json.get("neighbors");
        itr = neighborsArr.iterator();
        ArrayList<String> neighbors = new ArrayList<String>();
        // Iterate through neighbors and add to ArrayList of neihgbors
        while (itr.hasNext()) {neighbors.add((String) itr.next());}
        // Sort alphabetically
        neighbors.sort(null);

        System.out.println(targetName);
        System.out.println(paranoia);
        System.out.println(maxStress);
        System.out.println(neighbors);

        // Get number of firstNodes
        int firstNodesCount = ((Long) json.get("firstNodesCount")).intValue();
        // Get nodes/firstNodes
        JSONArray nodesArr = (JSONArray) json.get("pod");
        //Iterator nodeItr = nodesArr.iterator();
        itr = nodesArr.iterator();

        // Initializations
        HashMap<String, FactNode> podDict = new HashMap<String, FactNode>();
        ArrayList<String> firstNodes = new ArrayList<String>();
        JSONArray nodeArr;
        JSONObject node;
        int nodeX;
        int nodeY;
        ArrayList<String> children = new ArrayList<String>();
        FactNode fn;
        String nodeName;
        Iterator nodeItr;

        // Iterate through nodes in pod and map them to their names in podDict
        while (itr.hasNext()) {
            node = (JSONObject) itr.next();

            // Get name
            nodeName = (String) node.get("nodeName");
            // Get coordinates
            nodeArr = (JSONArray) node.get("coords");
            nodeItr = nodeArr.iterator();
            nodeX = ((Long) nodeItr.next()).intValue();
            nodeY = ((Long) nodeItr.next()).intValue();
            // Get children
            nodeArr = (JSONArray) node.get("children");
            nodeItr = nodeArr.iterator();
            while (nodeItr.hasNext()) {children.add((String) nodeItr.next());}
            children.sort(null);
            children.clear();
            
            // If node is one of the first nodes, add it to firstNodes
            if (firstNodesCount > 0) {
                firstNodes.add(nodeName);
                firstNodesCount--;
            }
            
            // Create FactNode
            fn = new FactNode(nodeName, (String) node.get("title"), (String) node.get("content"),
            (String) node.get("summary"), children, nodeX, nodeY, (String) node.get("assets"),
            ((Long) node.get("targetStressDamage")).intValue(), ((Long) node.get("playerStressDamage")).intValue());

            // Store FactNode in podDict, mapped to name
            podDict.put(nodeName, fn);
        }
        firstNodes.sort(null);

        // Iterate through combos in list

        // When there are lists, sort alphabetically
        System.out.println("test");
        
    }
}
