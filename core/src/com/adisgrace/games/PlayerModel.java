import java.util.Random;

public class PlayerModel {

	//TODO: 
	private final int DAILY_AP = 6;

	private final float STARTING_STRESS = 15;
	private final float MAX_STRESS = 100;

	private final float STARTING_BITECOIN = 30;
	private final float DAILY_BITECOIN = -10;

	private final int OVERWORK_AP = 2;
	private final float OVERWORK_STRESS_MEAN = 15;
	private final float OVERWORK_STRESS_STDEV = 3;

	private final float RELAX_STRESS_MEAN = 6;
	private final float RELAX_STRESS_STDEV = 2;

	private final float VTUBE_INCOME_MEAN = 20;
	private final float VTUBER_INCOME_STDEV = 5;

	private int action_points;
	private float stress;
	private float bitecoin;
	private Random rng;

	private boolean overworked_today;

	public Player() {
		this.action_points = DAILY_AP;
		this.stress = STARTING_STRESS;
		this.bitecoin = STARTING_BITECOIN;
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
		this.action_points = DAILY_AP;
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
			throw new RuntimeException('Negative AP');
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
		return (this.stress < MAX_STRESS);
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
		return (this.stress < MAX_STRESS) && (this.bitecoin >= 0);
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
		this.action_points += OVERWORK_AP;
		return this.incrementStress(Math.max(OVERWORK_STRESS_MEAN + rng.nextGaussian() * OVERWORK_STRESS_STDEV, 0));
	}

	/**
		Returns if player has already overworked today

		@return has the player overworked today
	*/
	public boolean overworkedToday() {
		return this.overworked_today;
	}

	/**
		Relax function

		@param action_points how many AP is the player spending relaxing
	*/
	public void relax(int action_points) {
		this.decrementStress(action_points * RELAX_STRESS_MEAN 
			+ rng.nextGaussian() * RELAX_STRESS_STDEV / action_points);
	}

	/**
		Vtube function, make some money selling your virtual body on the internet
	*/
	public void vtube(){
		this.incrementBitecoin(VTUBE_INCOME_MEAN + rng.nextGaussian() * RELAX_STRESS_STDEV);
	}

	/**
		Scan function
		This only handles stress damage, not facts known

		@param fact FactNode being scanned
		@return is the player alive
	*/
	public boolean scan(FactNode fact) {
		return 
	}


}

