package com.adisgrace.games;

//should handle all interactions with the LevelModel

import com.adisgrace.games.models.LevelModel;
import com.adisgrace.games.models.PlayerModel;
import com.adisgrace.games.models.TargetModel;
import com.adisgrace.games.models.TraitModel;
import com.adisgrace.games.util.Connector;
import com.adisgrace.games.util.GameConstants;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;

import java.util.*;
import java.util.logging.Level;


public class LevelController {

    public static double MINION_DAMAGE = 0.2;

    public enum CauseOfDeath{
        STRESS,
        BITECOIN,
        TARGET,
        NONE
    }

    private LevelModel levelModel;
    private PlayerModel player;

    private int n_rows, n_cols;

    private Random rng;

    public LevelController(String levelJson){
        levelModel = new LevelModel(levelJson);
        n_rows = levelModel.getHeight();
        n_cols = levelModel.getWidth();
        player = new PlayerModel();

        rng = new Random();
    }

    /**
     * Width of level map
     * @return number of columns in level grid
     */
    public int getWidth() {
        return n_cols;
    }

    /**
     * Height of level map
     * @return number of rows in level grid
     */
    public int getHeight() {
        return n_rows;
    }

    /**
     *
     * @return the current state of the level
     */
    public LevelModel.LevelState getLevelState(){
        if(!player.isLiving()) return LevelModel.LevelState.LOSE;
        return levelModel.getLevelState();
    }

    /**
     * Returns the reason the player lost the game, or NONE if the game is won or ongoing
     * @return CauseOfDeath.NONE, CauseOfDeath.BITECOIN, CauseOfDeath.STRESS, or CauseOfDeath.TARGET
     */
    public CauseOfDeath getCauseOfDeath(){
        if(getLevelState() != LevelModel.LevelState.LOSE)
            return CauseOfDeath.NONE;
        if(player.getBitecoin() <= 0)
            return CauseOfDeath.BITECOIN;
        if(player.getStress() >= GameConstants.MAX_STRESS)
            return CauseOfDeath.STRESS;
        return CauseOfDeath.TARGET;
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
        if(!levelModel.getTargets().containsKey(target))
            return -1;
        if(!player.canHack(levelModel.getTargets().get(target))) // pass target to playerModel since traits affect AP cost
            return -3;
        player.hack(levelModel.getTargets().get(target)); // pass target to playerModel since traits affect AP cost
//        if(rng.nextDouble() < 0.2){
        levelModel.getTarget(target).unlock();
        levelModel.getHackedFacts().get(target).add(fact);

        // Increase target suspicion accordingly
        levelModel.getTargets().get(target).unlock();
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
        if(!player.canScan(levelModel.getTargets().get(target))) // pass target to playerModel since traits affect AP cost
            return false;
        player.scan(levelModel.getTarget(target).getStressCost(fact), levelModel.getTargets().get(target)); // Stress cost for scanning is unimplemented
        // pass target to playerModel since traits affect AP cost
        levelModel.getSummaries().get(target).put(fact, levelModel.getTargets().get(target).getSummary(fact));
        levelModel.getContents().get(target).put(fact, levelModel.getTargets().get(target).getContent(fact));
        // combo checking
        //Array<String> facts_known = new Array<String>((String[]) levelModel.getSummaries().keySet().toArray());
        Array<String> facts_known = new Array<String>();
//        System.out.println(levelModel.getSummaries().get(target).keySet());
        for(String key : levelModel.getSummaries().get(target).keySet()) {
            facts_known.add(key);
        }
        int factsSize = facts_known.size;
        for(int i = 0; i < factsSize; i++) {
            String fact_ = facts_known.get(i);
            if (levelModel.getTargets().get(target).checkForCombo(fact_, facts_known)) {
//                System.out.println("combo activated");
                levelModel.getSummaries().get(target).put(fact_, levelModel.getTarget(target).getSummary(fact_));
                levelModel.getContents().get(target).put(fact_, levelModel.getTarget(target).getContent(fact_));
            }
        }
        levelModel.getExposableFacts().get(target).add(fact);

        // Increase target suspicion accordingly
        levelModel.getTargets().get(target).scan();
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
            System.out.println(t.nextTurn());
        }
        levelModel.nextDay();
        return levelModel.getLevelState();
    }

    /**
     * Attempt to gaslight a target
     * @param target name of target
     * @return true if the attempt was successful
     */
    public boolean gaslight(String target){
        boolean success = rng.nextInt(100) > levelModel.getTarget(target).getSuspicion();
        levelModel.getTarget(target).gaslight(success);
        player.gaslight(levelModel.getTarget(target));
        return success;
    }

    /**
     * Returns whether or not a player is able to gaslight
     * @return whether or not player can gaslight
     */
    public boolean canGaslight(String target) {
        return player.canGaslight(levelModel.getTarget(target));
    }

    /**
     * Attempt to distract a target
     * @param target name of the target
     * @return true if the attempt was successful
     */
    public boolean distract(String target){
        player.distract(levelModel.getTarget(target));
        return levelModel.getTarget(target).distract();
    }

    /**
     * Returns whether or not a player is able to distract
     * @return whether or not player can expose
     */
    public boolean canDistract(String target) {
        return player.canDistract(levelModel.getTarget(target));
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
        if(!player.canExpose(levelModel.getTargets().get(target)))  // pass target to playerModel since traits affect AP cost
            throw new RuntimeException("Insufficient AP to expose");
        if(!levelModel.getExposableFacts().get(target).contains(fact, false))
            throw new RuntimeException("This fact has already been exposed");
        player.expose(levelModel.getTargets().get(target));  // pass target to playerModel since traits affect AP cost
        levelModel.getExposableFacts().get(target).removeValue(fact, false);
        int stressDamage = levelModel.getTargets().get(target).expose(fact);
        levelModel.getTargets().get(target).addStress(stressDamage);
        if(!levelModel.isBoss(target) && levelModel.getTarget(target).getState() == TargetModel.TargetState.DEFEATED){
            for(String boss : levelModel.getBosses())
                levelModel.getTarget(boss).addStress((int)(MINION_DAMAGE * levelModel.getTarget(target).getMaxStress()));
        }
        return levelModel.getLevelState();
    }

    /**
     * Returns whether or not a player is able to expose
     * @return whether or not player can expose
     */
    public boolean canExpose(String target) {
        return player.canExpose(levelModel.getTarget(target));
    }

    /**
     * Harass function, increases target stress and reduces AP
     * @param target target to threaten
     * @param fact fact to threaten target over
     * @return amount of stress increase on target
     */
    public int harass(String target, String fact){
        if(!levelModel.getTargets().containsKey(target))
            throw new RuntimeException("Invalid target");
        if(!levelModel.getContents().get(target).containsKey(fact))
            throw new RuntimeException("Node has not been scanned");
        if(!player.canThreaten(levelModel.getTargets().get(target)))  // pass target to playerModel since traits affect AP cost
            throw new RuntimeException("Insufficient AP to threaten");
        if(!levelModel.getExposableFacts().get(target).contains(fact, false))
            throw new RuntimeException("This fact has already been exposed");
        player.threaten(levelModel.getTargets().get(target)); // pass target to playerModel since traits affect AP cost
        int stressDamage = levelModel.getTargets().get(target).harass(fact);
        if (stressDamage >= 0) {
            levelModel.getTargets().get(target).addStress(stressDamage);
            if(!levelModel.isBoss(target) && levelModel.getTarget(target).getState() == TargetModel.TargetState.DEFEATED){
                for(String boss : levelModel.getBosses())
                    levelModel.getTarget(boss).addStress((int)(MINION_DAMAGE * levelModel.getTarget(target).getMaxStress()));
            }
        }
        return stressDamage;
    }

    /**
     * Returns whether or not a player is able to threaten
     * @return whether or not player can threaten
     */
    public boolean canHarass(String target) {
        return player.canThreaten(levelModel.getTarget(target));
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
     * Returns whether or not a player is able to overwork
     * @return whether or not player can overwork
     */
    public boolean canOverwork() {
        return player.canOverwork();
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
     * Returns whether or not a player is able to relax
     * @return whether or not player can relax
     */
    public boolean canRelax() {
        return player.canRelax();
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
//        System.out.println(target);
//        System.out.println(levelModel.getTarget(target));
       //return new Vector2(levelModel.getTarget(target).getX(),levelModel.getTarget(target).getY());
        int [] targetLoc = levelModel.getTargetLoc(target);

        return new Vector2(targetLoc[0], targetLoc[1]);
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
     * @param target name of the target
     * @return all facts directly connected to targets and their corresponding paths
     */
    public ArrayMap<String, Array<Connector>> getConnectorsOf(String target){
        return levelModel.getTarget(target).getFirstNodes();
    }

    /**
     *
     * @param target name of the target
     * @param fact name of the fact belonging to the target
     * @return all nodes that directly stem from fact as well as their paths from fact
     */
    public ArrayMap<String, Array<Connector>> getConnectorsOf(String target, String fact){
        return levelModel.getTarget(target).getChildren(fact);
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
     * @return the fact
     */
    public String viewFact(String target, String fact){
        return levelModel.getContents().get(target).get(fact);
    }

    /**
     *
     * @param target name of the target
     * @param fact identifier of the fact
     * @return the locked status of a node
     */
    public boolean getLocked(String target, String fact) {
        return levelModel.getTarget(target).getLocked(fact);
    }

    /**
     * returns the current state of a target
     * @param target name of the target
     * @return the target state of the target
     */
    public TargetModel.TargetState getTargetState(String target){return levelModel.getTarget(target).getState();}

    /**
     * returns the traits of a target
     * @param target name of the target
     * @return the target traits of the target
     */
    public ArrayList<TraitModel.Trait> getTargetTraits(String target){return levelModel.getTarget(target).getTraits().get_traits();}

    /**
     * the total amount of stress rating types within the entire subtree where fact is the parent
     * @param target Name of the target
     * @param fact Name of the fact that belongs to the target
     * @return {NONE, LOW, MED, HIGH}
     */
    public int[] getStressRatings(String target, String fact){
        return levelModel.getStressRatings(target, fact);
    }

    public boolean isMale(String target) {
        return levelModel.getTarget(target).isMale();
    }

    public int getDaysLeft() {
        return levelModel.getDaysLeft();
    }

    public String getTutorialText() {
        return levelModel.getTutorialText();
    }

    public String getDefeatMessage(String target) {
        return levelModel.getTarget(target).getDefeatMessage();
    }




}
