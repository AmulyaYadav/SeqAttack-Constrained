package algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lpWrappers.Configuration;
import lpWrappers.MIProblem;
import models.Target;

// Note this is for two attacks only
public class FreeMovementMILP extends MIProblem{
	List<Target> targetList;
	int nRes;
	
	HashMap<String, Integer> varMap; // for MILP, column variables
	HashMap<String, Integer> rowMap;	// for MILP, row constraints
	public FreeMovementMILP(List<Target> targetList, int nRes) {
		this.targetList = targetList;
		this.nRes = nRes;
		
		this.varMap = new HashMap<String, Integer>();
		this.rowMap = new HashMap<String, Integer>();
	}
	
	public List<Double> computeUSSE(Target attackedTarget, boolean isSuccess) {
		List<Double> playerUSSE = new ArrayList<Double>();
		List<Target> tempTargetList = new ArrayList<Target>(this.targetList);
		tempTargetList.remove(attackedTarget.getTargetID() - 1);
		SSE sse = null;
		if(isSuccess) sse =  new SSE(tempTargetList, this.nRes);
		else sse = new SSE(tempTargetList, this.nRes - 1);
		
		Map<Target, Double> optDefStrategy = sse.origami();
		playerUSSE.add(sse.getOptDefU(optDefStrategy));
		playerUSSE.add(sse.getOptAttU(optDefStrategy));
		return playerUSSE;
	}
	
	@Override
	protected void setProblemType() {
		// TODO Auto-generated method stub
		this.setProblemName("FreeMovementMILP");
		this.setProblemType(PROBLEM_TYPE.MIP, OBJECTIVE_TYPE.MAX);
	}
    
	@Override
	protected void setColBounds() {
		// TODO Auto-generated method stub
		int idx = 1;
		addAndSetColumn("v", BOUNDS_TYPE.FREE, -Configuration.MM, Configuration.MM, VARIABLE_TYPE.CONTINUOUS, 1);
		varMap.put("v", idx);
		idx++;
		
		addAndSetColumn("r", BOUNDS_TYPE.FREE, -Configuration.MM, Configuration.MM, VARIABLE_TYPE.CONTINUOUS, 0);
		varMap.put("r", idx);
		idx++;
		
		for(Target t : this.targetList) {
			addAndSetColumn("x" + t.getTargetID(), BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.CONTINUOUS, 0);
			varMap.put("x" + t.getTargetID(), idx);
			idx++;
		}
		
		for(Target t : this.targetList) {
			addAndSetColumn("h" + t.getTargetID(), BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.INTEGER, 0);
			varMap.put("h" + t.getTargetID(), idx);
			idx++;
		}
	}

	@Override
	protected void setRowBounds() {
		// TODO Auto-generated method stub
		// Compute player utilities at second attack
		List<List<Double>> playerUSuccessList = new ArrayList<List<Double>>();
		List<List<Double>> playerUNotSuccessList = new ArrayList<List<Double>>();
		for(Target t : this.targetList) {
			List<Double> playerUSuccess = computeUSSE(t, true);
			List<Double> playerUNotSuccess = computeUSSE(t, false);
			playerUSuccessList.add(playerUSuccess);
			playerUNotSuccessList.add(playerUNotSuccess);
		}
		
		// Generate constraints
		// Constraints on defender's utility
		List<Integer> ja = new ArrayList<Integer>();
		List<Double> ar = new ArrayList<Double>();
		double M = Configuration.MM * 1.0;
		
		for(Target t : this.targetList) {
			int tID = t.getTargetID();
			double defReward = t.getPayoffStructure().getDefenderReward();
			double defPen = t.getPayoffStructure().getDefenderPenalty();
			ja.add(varMap.get("v"));
			ar.add(1.0);
			
			ja.add(varMap.get("x" + tID));
			ar.add(-defReward + defPen + playerUSuccessList.get(tID - 1).get(0) - playerUNotSuccessList.get(tID - 1).get(0));
			
			ja.add(varMap.get("h" + tID));
			ar.add(M);
			
			
			addAndSetRow("defUUBConstr" + tID, BOUNDS_TYPE.UPPER, -M, M + defPen + playerUSuccessList.get(tID - 1).get(0));
			rowMap.put("defUUBConstr" + tID, this.getNumRows());
			this.setMatRow(this.getNumRows(), ja, ar);
			ja.clear();
			ar.clear();
		}
		
		// Constraints on attacker's utility
		for(Target t : this.targetList) {
			int tID = t.getTargetID();
			double attReward = t.getPayoffStructure().getAdversaryReward();
			double attPen = t.getPayoffStructure().getAdversaryPenalty();
			
			ja.add(varMap.get("r"));
			ar.add(1.0);
			
			ja.add(varMap.get("x" + tID));
			ar.add(-attPen + attReward + playerUSuccessList.get(tID - 1).get(1) - playerUNotSuccessList.get(tID - 1).get(1));
			
			addAndSetRow("attULBConstr" + tID, BOUNDS_TYPE.LOWER, attReward + playerUSuccessList.get(tID - 1).get(1), M);
			rowMap.put("attULBConstr" + tID, this.getNumRows());
			this.setMatRow(this.getNumRows(), ja, ar);
			ja.clear();
			ar.clear();
		}
		
		for(Target t : this.targetList) {
			int tID = t.getTargetID();
			double attReward = t.getPayoffStructure().getAdversaryReward();
			double attPen = t.getPayoffStructure().getAdversaryPenalty();
			
			ja.add(varMap.get("r"));
			ar.add(1.0);
			
			ja.add(varMap.get("x" + tID));
			ar.add(-attPen + attReward + playerUSuccessList.get(tID - 1).get(1) - playerUNotSuccessList.get(tID - 1).get(1));
			
			ja.add(varMap.get("h" + tID));
			ar.add(M);
			
			addAndSetRow("attUUBConstr" + tID, BOUNDS_TYPE.UPPER, -M, attReward + playerUSuccessList.get(tID - 1).get(1) + M);
			rowMap.put("attUUBConstr" + tID, this.getNumRows());
			this.setMatRow(this.getNumRows(), ja, ar);
			ja.clear();
			ar.clear();
		}
		
		// Constraints on attacked target variable
		for(Target t : this.targetList) {
			int tID = t.getTargetID();
			ja.add(varMap.get("h" + tID));
			ar.add(1.0);
		}
		addAndSetRow("attAttackConstr", BOUNDS_TYPE.FIXED, 1, 1);
		rowMap.put("attAttackConstr", this.getNumRows());
		this.setMatRow(this.getNumRows(), ja, ar);
		ja.clear();
		ar.clear();
		
		// Constraints on defense
		for(Target t : this.targetList) {
			int tID = t.getTargetID();
			ja.add(varMap.get("x" + tID));
			ar.add(1.0);
		}
		addAndSetRow("defenseConstr", BOUNDS_TYPE.FIXED, this.nRes, this.nRes);
		rowMap.put("defenseConstr", this.getNumRows());
		this.setMatRow(this.getNumRows(), ja, ar);
		ja.clear();
		ar.clear();
	}

	@Override
	protected void generateData() {
		// TODO Auto-generated method stub
		
	}
	
	public double getDefOptU() {
		return this.getColumnPrimal(this.varMap.get("v"));
	}
	public double getAttOptU() {
		return this.getColumnPrimal(this.varMap.get("r"));
	}
	
	public Target getFirstAttackTarget() {
		Target attackTarget = null;
		for(Target t : this.targetList) {
			int tID = t.getTargetID();
			double temp = this.getColumnPrimal(this.varMap.get("h" + tID));
			if(temp > 0.5)
			{
				attackTarget = t;
				break;
			}
		}
		return attackTarget;
	}
    public double getOptDefCov(Target t) {
    	return this.getColumnPrimal(this.varMap.get("x" + t.getTargetID()));
    }
    public double getOptDefCov(int tID) {
    	return this.getColumnPrimal(this.varMap.get("x" + tID));
    }
    public Map<Target, Double> getOptDefStrategy() {
    	Map<Target, Double> defStrategy = new HashMap<Target, Double>();
    	for(Target t : this.targetList) {
    		double defCov = getOptDefCov(t);
    		defStrategy.put(t, defCov);
    	}
    	return defStrategy;
    }
    
    public double getOptDefURecompute() {
    	Target firstAttack = getFirstAttackTarget();
    	double defReward = firstAttack.getPayoffStructure().getDefenderReward();
    	double defPen = firstAttack.getPayoffStructure().getDefenderPenalty();
    	
    	
    	
    	double defCov = getOptDefCov(firstAttack);
    	double defImmediateEU = defCov * (defReward - defPen) + defPen;
    	double defFutureEU = (1-defCov) * this.computeUSSE(firstAttack, true).get(0)
    							+ defCov * this.computeUSSE(firstAttack, false).get(0);
    	return defImmediateEU + defFutureEU;
    }
    
    public double getOptAttURecompute() {
    	Target firstAttack = getFirstAttackTarget();
    	double attReward = firstAttack.getPayoffStructure().getAdversaryReward();
    	double attPen = firstAttack.getPayoffStructure().getAdversaryPenalty();
    	
    	
    	
    	double defCov = getOptDefCov(firstAttack);
    	double attImmediateEU = defCov * (attPen - attReward) + attReward;
    	double attFutureEU = (1-defCov) * this.computeUSSE(firstAttack, true).get(1)
    							+ defCov * this.computeUSSE(firstAttack, false).get(1);
    	return attImmediateEU + attFutureEU;
    }
    
	public void end() {
		super.end();
		varMap.clear();
		rowMap.clear();
	}
}
