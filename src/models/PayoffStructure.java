package models;

/**
 * This class represents the Payoff Structure associated with a Target.
 * A single instance of this class belongs to exactly one Target. Sharing among different instances of 
 * Targets is discouraged (if you change one instance of a shared payoff structure, you change it for
 * ALL targets, for better or worse).
 * @author Ben Ford
 */
public class PayoffStructure {

	private double defenderReward;
	private double defenderPenalty;
	private double adversaryReward;
	private double adversaryPenalty;

	public PayoffStructure(double defenderReward, double defenderPenalty, double adversaryReward, double adversaryPenalty) 
	{
		this.defenderReward = defenderReward;
		this.defenderPenalty = defenderPenalty;
		this.adversaryReward = adversaryReward;
		this.adversaryPenalty = adversaryPenalty;
	}
	public void setAdversaryReward(double reward)
	{
		this.adversaryReward = reward;
	}
	
	public double getDefenderReward() {
		return defenderReward;
	}

	public double getDefenderPenalty() {
		return defenderPenalty;
	}

	public double getAdversaryReward() {
		return adversaryReward;
	}

	public double getAdversaryPenalty() {
		return adversaryPenalty;
	}
	
	/**
	 * Format: Defender Reward, Defender Penalty \n Adversary Reward, Adversary Penalty
	 */
	public String toString()
	{
		String returnString = "";
		
		returnString += "Defender Reward: " + defenderReward + "\t";
		returnString += "Defender Penalty: " + defenderPenalty + "\n";
		returnString += "Adversary Reward: " + adversaryReward + "\t";
		returnString += "Adversary Penalty: " + adversaryPenalty;
		
		return returnString;
	}
}
