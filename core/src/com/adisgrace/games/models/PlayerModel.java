package com.adisgrace.games.models;

import com.adisgrace.games.util.GameConstants;

import java.lang.annotation.Target;
import java.util.Random;

public class PlayerModel {
	//TODO: balancing

	private int action_points;
	private float stress;
	private float bitecoin;
	private Random rng;

	private boolean overworked_today;

	public PlayerModel() {
		this.action_points = GameConstants.DAILY_AP;
		this.stress = GameConstants.STARTING_STRESS;
		this.bitecoin = GameConstants.STARTING_BITECOIN;
		this.rng = new Random();

		this.overworked_today = false;
	}

	/**
		Returns number of action points

		@return current AP
	*/
	public int getAP() {
		return this.action_points;
	}

	/**
		Sets number of action points

		@param action_points target amount
	*/
	public void setAP(int action_points) {
		this.action_points = action_points;
	}

	/**
		Reset action points to daily amount
	*/
	public void resetAP() {
		this.action_points = GameConstants.DAILY_AP;
	}

	/**
		Increment action points
	*/
	public void incrementAP(int increment) {
		this.action_points += increment;
	}

	/**
		Decrement action points
	*/
	public void decrementAP(int decrement) {
		this.action_points -= decrement;
		if(this.action_points < 0)
			throw new RuntimeException("Negative AP");
	}

	/**
		Returns amount of stress

		@return current stress
	*/
	public float getStress() {
		return this.stress;
	}

	/**
		Sets amount of stress
	*/
	public void setStress(float stress) {
		this.stress = stress;
	}

	/**
		Increment stress
		Returns true if player is still alive, false if player has lost

		@param increment amount to increment by
		@return is the player alive
	*/
	public boolean incrementStress(float increment) {
		this.stress += increment;
		return (this.stress < GameConstants.MAX_STRESS);
	}

	/**
		Decrement stress, to a minimum of 0

		@param decrement amount to decrement by
	*/
	public void decrementStress(float decrement) {
		this.stress -= decrement;
		this.stress = Math.max(0f, this.stress);
	}

	/**
		Returns amount of bitecoin
	*/
	public float getBitecoin() {
		return this.bitecoin;
	}

	/**
		Increments amount of bitecoin

		@param increment amount to increment by
	*/
	public void incrementBitecoin(float increment) {
		this.bitecoin += increment;
		this.bitecoin = Math.min(this.bitecoin, 999f);
	}

	/**
		Decrements bitecoin
		Returns true if player is still alive, false if player has lost

		@param decrement amount to decrement by
		@return is the player alive
	*/
	public boolean decrementBitecoin(float decrement) {
		this.bitecoin -= decrement;
		return (this.bitecoin >= 0);
	}

	/**
		Returns false if player has lost the game

		@return is the player alive
	*/
	public boolean isLiving() {
		return (this.stress < GameConstants.MAX_STRESS) && ((int)this.bitecoin > 0);
	}

	/**
	 * Moves to next turn, setting AP to daily amount if lower, removing money, and dreaming
	 * @return is the player alive
	 */
	public boolean nextTurn() {
		this.overworked_today = false;

		this.action_points = GameConstants.DAILY_AP;
		this.decrementBitecoin(GameConstants.DAILY_BITECOIN_COST);
//		this.incrementStress((float)rng.nextGaussian() * GameConstants.DREAM_STRESS_STDEV);

//		this.stress = Math.max(0f, this.stress);
		return this.isLiving();
	}

	/**
		Overwork function
		Returns false if player has lost

		@return is the player alive
		@throws RuntimeException if the player has already overworked today
	*/
	public boolean overwork() {
		if(overworked_today)
			throw new RuntimeException("Already overworked today");
		overworked_today = true;
		this.action_points += GameConstants.OVERWORK_AP;
		return this.incrementStress((float)Math.max(GameConstants.OVERWORK_STRESS_MEAN + rng.nextGaussian() * GameConstants.OVERWORK_STRESS_STDEV, 0));
	}

	/**
	 * Returns if player has can overwork
	 *
	 * @return has the player overworked today
	*/
	public boolean canOverwork() {
		return !this.overworked_today;
	}

	/**
	 * Relax function
	 *
	 * @param action_points how many AP is the player spending relaxing
	*/
	public void relax(int action_points) {
		if(action_points > this.action_points)
			throw new RuntimeException("Insufficient AP");
		this.decrementStress(rng.nextInt(6)+5);
		this.decrementAP(action_points);
	}

	public boolean canRelax() {
		return this.action_points > 0;
	}

	/**
	 * Vtube function, make some money selling your virtual body on the internet
	 *
	 * @return the amount of currency earned
	 */
	public float vtube(){
		float amount = (float)(GameConstants.VTUBE_INCOME_MEAN + rng.nextGaussian() * GameConstants.VTUBE_INCOME_STDEV);
		this.incrementBitecoin(amount);
		this.decrementAP(GameConstants.VTUBE_AP_COST);
		return amount;
	}

	/**
	 * @return can the player vtube
	 */
	public boolean canVtube() {
		return this.action_points >= GameConstants.VTUBE_AP_COST;
	}

	/**
	 * Hack function, does not change actual fact state
	 *
	 * @param t			Target which the player wants to hack
	 */
	public void hack(TargetModel t) {
		if (t.getTraits().is_technologically_illiterate()){
			// costs 1 less AP if is_technologically_illiterate
			this.decrementAP(GameConstants.HACK_AP_COST - 1);
		}else{
			this.decrementAP(GameConstants.HACK_AP_COST);
		}
	}

	/**
	 * @return can the player hack
	 *
	 * @param t			Target which the player wants to hack
	 */
	public boolean canHack(TargetModel t) {
		if (t.getTraits().is_technologically_illiterate()){
			// costs 1 less AP if is_technologically_illiterate
			return this.action_points >= (GameConstants.HACK_AP_COST - 1);
		}else {
			return this.action_points >= GameConstants.HACK_AP_COST;
		}
	}

	/**
	 * Scan function, doesn't not change actual fact state
	 * @param san_cost stress damage caused by scanning
	 * @param t			Target which the player wants to scan
	 * @return is the player still alive
	 */
	public boolean scan(float san_cost, TargetModel t){
		if (t.getTraits().is_technologically_illiterate()){
			// costs 1 less AP if is_technologically_illiterate
			this.decrementAP(GameConstants.SCAN_AP_COST - 1);
		}else {
			this.decrementAP(GameConstants.SCAN_AP_COST);
		}
		if(rng.nextInt(100) < GameConstants.SCAN_BITECOIN_CHANCE){
			if(t.getTraits().is_rich())
				this.incrementBitecoin(GameConstants.SCAN_BITECOIN * 2);
			else this.incrementBitecoin(GameConstants.SCAN_BITECOIN);
		}
		return this.incrementStress(san_cost);
	}

	/**
	 * @param t			Target which the player wants to scan
	 *
	 * @return can the player scan
	 */
	public boolean canScan(TargetModel t) {
		if (t.getTraits().is_technologically_illiterate()){
			// costs 1 less AP if is_technologically_illiterate
			return this.action_points >= (GameConstants.SCAN_AP_COST - 1);
		}else {
			return this.action_points >= GameConstants.SCAN_AP_COST;
		}
	}

	/**
	 * Coerce function, only manages playerside attrs, ie AP
	 *
	 * @param t			Target which the player wants to coerce
	 */
	public void coerce(TargetModel t) {
		if (t.getTraits().is_bad_connection()){
			// Costs 1 more AP if target is bad_connection
			this.decrementAP(GameConstants.COERCE_AP_COST + 1);
		}else{
			this.decrementAP(GameConstants.COERCE_AP_COST);
		}
	}

	/**
	 * @return can the player coerce
	 *
	 * param t			Target which the player wants to coerce
	 */
	public boolean canCoerce(TargetModel t) {
		if (t.getTraits().is_bad_connection()){
			// Costs 1 more AP if target is bad_connection
			return this.action_points >= (GameConstants.COERCE_AP_COST + 1);
		}else{
			return this.action_points >= GameConstants.COERCE_AP_COST;
		}
	}

	/**
	 * Harass function, only manages playerside attrs, ie AP
	 *
	 * @param t			Target which the player wants to harass
	 */
	public void harass(TargetModel t) {
		if (t.getTraits().is_bad_connection()){
			// Costs 1 more AP if target is bad_connection
			this.decrementAP(GameConstants.HARASS_AP_COST + 1);
		}else{
			this.decrementAP(GameConstants.HARASS_AP_COST);
		}

		//If off-putting, increase player stress by a random amount
		if(t.getTraits().is_off_putting()){
			this.incrementStress(GameConstants.OFF_PUTTING_CONST);
		}
	}

	/**
	 * @return can the player harass
	 *
	 * @param t			Target which the player wants to harass
	 */
	public boolean canHarass(TargetModel t) {
		if (t.getTraits().is_bad_connection()){
			return this.action_points >= (GameConstants.HARASS_AP_COST + 1);
			// Costs 1 more AP if target is bad_connection
		}else{
			return this.action_points >= GameConstants.HARASS_AP_COST;
		}
	}

	/**
	 * Threaten function, only manages playerside attrs, ie AP
	 *
	 * @param t			Target which the player wants to threaten
	 */
	public void threaten(TargetModel t) {
		if (t.getTraits().is_bad_connection()){
			// Costs 1 more AP if target is bad_connection
			this.decrementAP(GameConstants.THREATEN_AP_COST + 1);
		}else{
			this.decrementAP(GameConstants.THREATEN_AP_COST);
		}
	}

	/**
	 * @return can the player threaten
	 *
	 * @param t			Target which the player wants to threaten
	 */
	public boolean canThreaten(TargetModel t) {
		if (t.getTraits().is_bad_connection()){
			// Costs 1 more AP if target is bad_connection
			return this.action_points >= (GameConstants.THREATEN_AP_COST + 1);
		}else{
			return this.action_points >= GameConstants.THREATEN_AP_COST;
		}
	}

	/**
	 * Expose function, only manages playerside attrs, ie AP
	 *
	 * @param t			Target which the player wants to expose
	 */
	public void expose(TargetModel t) {
		if (t.getTraits().is_bad_connection()){
			// Costs 1 more AP if target is bad_connection
			this.decrementAP(GameConstants.EXPOSE_AP_COST + 1);
		}else {
			this.decrementAP(GameConstants.EXPOSE_AP_COST);
		}
	}

	/**
	 * @return can the player expose
	 *
	 * @param t			Target which the player wants to expose
	 */
	public boolean canExpose(TargetModel t) {
		if (t.getTraits().is_bad_connection()){
			// Costs 1 more AP if target is bad_connection
			return this.action_points >= (GameConstants.EXPOSE_AP_COST + 1);
		}else {
			return this.action_points >= GameConstants.EXPOSE_AP_COST;
		}
	}

	public void gaslight(TargetModel t){
		if(t.getTraits().is_bad_connection()) this.decrementAP(GameConstants.GASLIGHT_AP_COST + 1);
		else this.decrementAP(GameConstants.GASLIGHT_AP_COST);
	}

	/**
	 * @return can the player gaslight
	 *
	 * @param t	Target which the player wants to gaslight
	 */
	public boolean canGaslight(TargetModel t) {
		if (t.getTraits().is_bad_connection()){
			// Costs 1 more AP if target is bad_connection
			return this.action_points >= (GameConstants.GASLIGHT_AP_COST + 1);
		}else {
			return this.action_points >= GameConstants.GASLIGHT_AP_COST;
		}
	}


	public void distract(TargetModel t){
		if(t.getTraits().is_bad_connection()) this.decrementAP(GameConstants.DISTRACT_AP_COST + 1);
		else this.decrementAP(GameConstants.DISTRACT_AP_COST);
	}

	/**
	 * @return can the player gaslight
	 *
	 * @param t	Target which the player wants to gaslight
	 */
	public boolean canDistract(TargetModel t) {
		if (t.getTraits().is_bad_connection()){
			// Costs 1 more AP if target is bad_connection
			return this.action_points >= (GameConstants.DISTRACT_AP_COST + 1);
		}else {
			return this.action_points >= GameConstants.DISTRACT_AP_COST;
		}
	}
}

