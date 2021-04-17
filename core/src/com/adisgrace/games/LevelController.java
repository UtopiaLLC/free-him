package com.adisgrace.games;

//should handle all interactions with the LevelModel

import com.adisgrace.games.models.LevelModel;
import com.adisgrace.games.models.PlayerModel;
import com.adisgrace.games.models.TargetModel;
import com.adisgrace.games.util.Connector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.*;



public class LevelController {

    private LevelModel levelModel;
    private PlayerModel player;



    public LevelController(String levelJson){
        levelModel = new LevelModel(levelJson);
        player = new PlayerModel();

    }


    /**
     *
     * @return the current state of the level
     */
    public LevelModel.LevelState getLevelState(){
        return levelModel.getLevelState();
    }

    /**
     *
     * @return  how much money is made with other jobs, returns -1 upon failure
     */
    public float otherJobs(){
        if(player.canVtube()){
            return player.vtube();
        }
        return -1;
    }

    /**
     * Hack function, decreases player AP and hacks target fact
     * May throw runtime exceptions if provided invalid inputs
     * @param target target to hack
     * @param fact particular node id of target to hack
     * @return an int reflecting the result of the hack
     */
    public int hack(String target, String fact){
        Random rng = new Random();

        if(!levelModel.getTargets().containsKey(target))
            return -1;
        if(!levelModel.getVisibleFacts().get(target).contains(fact, false)
                || levelModel.getHackedFacts().get(target).contains(fact, false))
            return -2;
        if(!player.canHack())
            return -3;
        player.hack();
        if(rng.nextDouble() < 0.2){
            levelModel.getTargets().get(target).addSuspicion(25);
            return -4;
        }
        levelModel.getHackedFacts().get(target).add(fact);
        return 1;
    }

    /**
     * Scan function, decreases player AP and logs and returns scan results
     * May throw runtime exceptions if provided invalid inputs
     * @param target target to scan
     * @param fact particular node id of target to scan
     * @return levelModel.getContents() of scanned node, <b>NOT CURRENT GAMESTATE</b>
     */
    public boolean scan(String target, String fact){
        if(!levelModel.getTargets().containsKey(target))
            return false;
        if(!levelModel.getVisibleFacts().get(target).contains(fact, false)
                || !levelModel.getHackedFacts().get(target).contains(fact, false)
                || levelModel.getSummaries().get(target).containsKey(fact))
            return false;
        if(!player.canScan())
            return false;
        player.scan(0f); // Stress cost for scanning is unimplemented
        levelModel.getSummaries().get(target).put(fact, levelModel.getTargets().get(target).getSummary(fact));
        levelModel.getContents().get(target).put(fact, levelModel.getTargets().get(target).getContent(fact));
        // combo checking
        //Array<String> facts_known = new Array<String>((String[]) levelModel.getSummaries().keySet().toArray());
        Array<String> facts_known = new Array<String>();
        for(String key : levelModel.getSummaries().keySet()) {
            facts_known.add(key);
        }
        int factsSize = facts_known.size;
        for(int i = 0; i < factsSize; i++) {
            String fact_ = facts_known.get(i);
            if (levelModel.getTargets().get(target).checkForCombo(fact_, facts_known)) {
                levelModel.getSummaries().get(target).put(fact, levelModel.getTargets().get(target).getSummary(fact_));
                levelModel.getContents().get(target).put(fact, levelModel.getTargets().get(target).getContent(fact_));
            }
        }
        levelModel.getExposableFacts().get(target).add(fact);
        levelModel.getVisibleFacts().get(target).addAll(levelModel.getTargets().get(target).getChildren(fact));
        return true;
    }

    /**
     * Advances to next day
     *
     * @return gamestate after napping
     */
    public LevelModel.LevelState endDay() {
        player.nextTurn();
        for(TargetModel t : levelModel.getTargets().values()){
            t.nextTurn();
        }
        levelModel.nextDay();
        return levelModel.getLevelState();
    }

    /**
     *	Harass a target
     *
     *	@param target 	name of target
     *	@return 			the gamestate after this action
     */
    public LevelModel.LevelState harass(String target){
        // Get harass damage and inflict on target
        int stressDmg = levelModel.getTargets().get(target).harass();
        levelModel.getTargets().get(target).addStress(stressDmg);
        player.harass();
        return levelModel.getLevelState();
    }

    /**
     * Expose function, increases target stress greatly and reduces AP, but renders the fact unusable
     * @param target target of attack
     * @param fact fact to expose
     * @return amount of stress increase on target
     */
    public LevelModel.LevelState expose(String target, String fact){
        if(!levelModel.getTargets().containsKey(target))
            throw new RuntimeException("Invalid target");
        if(!levelModel.getContents().get(target).containsKey(fact))
            throw new RuntimeException("Node has not been scanned");
        if(!player.canExpose())
            throw new RuntimeException("Insufficient AP to expose");
        if(!levelModel.getExposableFacts().get(target).contains(fact, false))
            throw new RuntimeException("This fact has already been exposed");
        player.expose();
        levelModel.getExposableFacts().get(target).removeValue(fact, false);
        int stressDamage = levelModel.getTargets().get(target).expose(fact);
        levelModel.getTargets().get(target).addStress(stressDamage);
        return levelModel.getLevelState();
    }

    /**
     * Threaten function, increases target stress and reduces AP
     * @param target target to threaten
     * @param fact fact to threaten target over
     * @return amount of stress increase on target
     */
    public LevelModel.LevelState threaten(String target, String fact){
        if(!levelModel.getTargets().containsKey(target))
            throw new RuntimeException("Invalid target");
        if(!levelModel.getContents().get(target).containsKey(fact))
            throw new RuntimeException("Node has not been scanned");
        if(!player.canThreaten())
            throw new RuntimeException("Insufficient AP to threaten");
        if(!levelModel.getExposableFacts().get(target).contains(fact, false))
            throw new RuntimeException("This fact has already been exposed");
        player.threaten();
        int stressDamage = levelModel.getTargets().get(target).threaten(fact);
        levelModel.getTargets().get(target).addStress(stressDamage);
        return levelModel.getLevelState();
    }


    /**
     *	Calls the player's overwork function
     *
     *	@return whether overwork was successful
     */
    public boolean overwork(){
        if(player.canOverwork()){
            player.overwork();
            return true;
        }
        return false;
    }

    /**
    * Relax function, relaxes 1 AP at a time
    *
    * @return whether relax was successful
    */
    public boolean relax(){
        if(player.canRelax()){
            player.relax(1);
            return true;
        }
        return false;
    }

    /**
     * Returns target stress
     *
     * @param target name of the target
     * @return the stress of that target
     */
    public int getTargetStress(String target){
        return levelModel.getTarget(target).getStress();
    }

    /**
     * Returns target suspicion
     *
     * @param target name of the target
     * @return the suspicion of the target
     */
    public int getTargetSuspicion(String target){
        return levelModel.getTarget(target).getSuspicion();
    }


    /**
     *
     * @return the amount of AP a player has remaining
     */
    public int getAP(){
        return player.getAP();
    }

    /**
     *
     * @return how much stress the player has
     */
    public float getPlayerStress(){
        return player.getStress();
    }

    /**
     *
     * @return how much currency the user has
     */
    public float getPlayerCurrency(){
        return player.getBitecoin();
    }

    /**
     * Returns the notes for a specific target
     *
     * @param target name of the target
     * @return All facts that the player has discovered about the target
     */
    public Map<String, String> getNotes(String target){
        return levelModel.getSummaries().get(target);
    }

    /**
     * Returns the position of the target
     *
     * @param target name of the target
     * @return  position of the target in isometric coordinates
     */
    public Vector2 getTargetPos(String target){
        System.out.println(target);
        System.out.println(levelModel.getTarget(target));
       //return new Vector2(levelModel.getTarget(target).getX(),levelModel.getTarget(target).getY());
        int [] targetLoc = levelModel.getTargetLoc(target);

        return new Vector2(targetLoc[0], targetLoc[1]);
    }

    /**
     * Returns the names and locations of all visible facts pertaining to target
     *
     * @param target the name of the target
     * @return A hashmap of facts connected to their isometric coordinates
     */
    public HashMap<String, Vector2> getFactLocations(String target){
        HashMap<String, Vector2> factLocations = new HashMap<>();
        TargetModel t = levelModel.getTarget(target);

        for(String f: levelModel.getVisibleFacts(target)){
            factLocations.put(f, t.getNodeCoords(f));
        }

        return factLocations;
    }

    /**
     * returns target models
     *
     * @return all existing target models in levelModel
     */
    public Map<String, TargetModel> getTargetModels(){
        return levelModel.getTargets();
    }

    /**
     *
     * @return returns all connectors from visible nodes
     */
    public Array<Connector> getAllVisibleConnectors(){
        Array<Connector> connections = new Array<>();
        Set<String> targets = levelModel.getTargets().keySet();
        for(String target: targets){
            //add connectors originating from target
            Array<Vector2> coordinates = levelModel.getTarget(target).getFirstConnectorCoords();
            Array<String> types = levelModel.getTarget(target).getFirstConnectorTypes();
            for(int i = 0; i < coordinates.size; i++){
                Vector2 coordinate = coordinates.get(i);
                connections.add(new Connector((int)coordinate.x, (int)coordinate.y, types.get(i)));
            }

            for(String fact: levelModel.getVisibleFacts(target)){
                //add connectors origination from all visible nodes
                connections.addAll(getConnectorsOf(target, fact));
            }
        }

        return connections;

    }

    /**
     * Helper function returning connectors origination from fact
     *
     * @param target name of the target
     * @param fact name of the fact
     * @return all connectors originating from the fact
     */
    public Array<Connector> getConnectorsOf(String target, String fact){
        Array<Connector> connections = new Array<>();
        Array<Vector2> coordinates = levelModel.getTarget(target).getConnectorCoordsOf(fact);
        Array<String> types = levelModel.getTarget(target).getConnectorTypesOf(fact);

        for(int i = 0; i < coordinates.size; i++){
            Vector2 coordinate = coordinates.get(i);
            connections.add(new Connector((int)coordinate.x, (int)coordinate.y, types.get(i)));
        }

        return connections;
    }

    /**
     * Checks the current state of a fact, whether it's locked, scannable, or viewable
     *
     * @param target name of the target
     * @param fact name of the fact
     * @return returns whether a node is currently locked (returns 1), scannable (returns 2), or viewable (returns 3)
     */
    public int getCurrentNodeState(String target, String fact){
        if(levelModel.getContents().get(target).containsKey(fact))
            return 1; //viewable
        if(levelModel.getHackedFacts().get(target).contains(fact, false))
            return 2; //scannable
        else
            return 3; //locked
    }

    /**
     *
     * @param target name of the target
     * @param fact identifier of the fact
     * @return the
     */
    public String viewFact(String target, String fact){
        return levelModel.getContents().get(target).get(fact);
    }

    /**
     *
     * @param target name of the target
     * @return visible nodes of target
     */
    public Array<String> getVisibleNodes(String target){
        return levelModel.getVisibleFacts(target);
    }



}
