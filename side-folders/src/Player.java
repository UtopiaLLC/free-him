public class Player {

	private final int DAILY_AP = 6;

	private final float STARTING_STRESS = 15;
	private final float MAX_STRESS = 100;

	private final float STARTING_BITECOIN = 30;
	private final float DAILY_BITECOIN = -10;

	private int action_points;
	private float stress;
	private float bitecoin;

	public Player() {
		this.action_points = DAILY_AP;
		this.stress = STARTING_STRESS;
		this.bitecoin = STARTING_BITECOIN;
	}

	/**
		Returns number of action points
	*/
	public int getAP() {
		return this.action_points;
	}

	/**
		Sets number of action points
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
	}

	/**
		Returns amount of stress
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
	*/
	public boolean incrementStress(float increment) {
		this.stress += increment;
		return (this.stress < MAX_STRESS);
	}

	/**
		Decrement stress, to a minimum of 0
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
	*/
	public void incrementBitecoin(float increment) {
		this.bitecoin += increment;
	}

	/**
		Decrements bitecoin
		Returns true if player is still alive, false if player has lost
	*/
	public boolean decrementBitecoin(float decrement) {
		this.bitecoin -= decrement;
		return (this.bitecoin >= 0);
	}

	/**
		Returns false if player has lost the game
	*/
	public boolean isLiving() {
		return (this.stress < MAX_STRESS) && (this.bitecoin >= 0);
	}
}
