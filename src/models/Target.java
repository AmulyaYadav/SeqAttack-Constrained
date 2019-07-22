package models;



/**
 * This class shall track the payoff structure and probability of coverage for a single target.
 * Note that a single Target instance belongs to exactly one GameRound object.
 * @author Ben Ford
 */
public class Target implements Comparable<Object>{

	private int id;
	private PayoffStructure payoffs;
//	private Float coverageProbability;//Valid values: [0,1]
	/**
	 * @param targetID The unique ID of the target.
	 */
	public Target(int id, String targetID)
	{
//		coverageProbability = null;
		this.id = id;
	}
	
	/**
	 * @param payoffStructure The associated PayoffStructure object for this target.
	 */
	public Target(int id, PayoffStructure payoffs)
	{
		this.payoffs = payoffs;
//		coverageProbability = null;
		this.id = id;
	}
	
	/**
	 * @param payoffStructure The associated PayoffStructure object for this target.
	 * @param coverageProbability The coverage probability for this target.
	 * @throws ValueOutOfBoundsException
	 */
	
//	public float getCoverageProbability() {
//		return coverageProbability;
//	}
//	
//	public void setCoverageProbability(Float coverageProbability) {
//		this.coverageProbability = coverageProbability;
//	}
	
	public int getTargetID() {
		return id;
	}

	public void setTargetID(int id) {
		this.id = id;
	}
	
	public void setPayoffStructure(PayoffStructure payoffs)
	{
		this.payoffs = payoffs;
	}
	
	public PayoffStructure getPayoffStructure()
	
	{
		return payoffs;
	}
//	public void setAttPayoffInterval(PayoffInt attPayoffInterval)
//	{
//		this.attPayoffInterval = attPayoffInterval;
//	}
//	public PayoffInt getAttPayoffInterval()
//	{
//		return this.attPayoffInterval;
//	}
	/**
	 * Format: Payoff Structure \n Coverage Probability
	 */

//	@Override
//	public int compareTo(Object o) {
//		// TODO Auto-generated method stub
//		return this.getPayoffStructure().getAdversaryReward().;
//	}
	@Override
	  public int compareTo(Object o) {
			int result = 0;
	        if(this.getTargetID() != ((Target) o).getTargetID()) {
	        	double attReward = this.getPayoffStructure().getAdversaryReward();
	        	double rewardtoCompare = ((Target) o).getPayoffStructure().getAdversaryReward();
	            result = (attReward < rewardtoCompare ? -1 : (attReward == rewardtoCompare ? 0 : 1));
	        }
	        return  result;
	 }
	
}
