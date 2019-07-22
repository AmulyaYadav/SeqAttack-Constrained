package algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lpWrappers.Configuration;
import lpWrappers.MIProblem;
import models.Target;

public class SSEMultiAttack extends MIProblem{
	List<Target> targetList;
	int nRes;
	int nAttack;
	
	HashMap<String, Integer> varMap; // for MILP, column variables
	HashMap<String, Integer> rowMap;	// for MILP, row constraints
	
	public SSEMultiAttack(List<Target> targetList, int nRes, int nAttack) {
		this.targetList = targetList;
		this.nRes = nRes;
		this.nAttack = nAttack;
		
		this.varMap = new HashMap<String, Integer>();
		this.rowMap = new HashMap<String, Integer>();
	}

	@Override
	protected void setProblemType() {
		// TODO Auto-generated method stub
		this.setProblemName("SSEMultiAttack");
		this.setProblemType(PROBLEM_TYPE.MIP, OBJECTIVE_TYPE.MAX);
	}

	@Override
	protected void setColBounds() {
		// TODO Auto-generated method stub
		int idx = 1;
		for(int i = 0; i < this.nAttack; i++) {
			addAndSetColumn("v" + (i + 1), BOUNDS_TYPE.FREE, -Configuration.MM, Configuration.MM, VARIABLE_TYPE.CONTINUOUS, 1);
			varMap.put("v" + (i + 1), idx);
			idx++;
		}
		for(Target t : this.targetList) {
			addAndSetColumn("x" + t.getTargetID(), BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.CONTINUOUS, 0);
			varMap.put("x" + t.getTargetID(), idx);
			idx++;
		}
		
		for(int i = 0; i < this.nAttack; i++) {
			for(Target t : this.targetList) {
				addAndSetColumn("h" + t.getTargetID() + "_" + (i + 1), BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.INTEGER, 0);
				varMap.put("h" + t.getTargetID() + "_" + (i + 1), idx);
				idx++;
			}
		}
		for(int i = 0; i < this.nAttack; i++) {
			addAndSetColumn("r" + (i + 1), BOUNDS_TYPE.FREE, -Configuration.MM, Configuration.MM, VARIABLE_TYPE.CONTINUOUS, 0);
			varMap.put("r" + (i + 1), idx);
			idx++;
		}
	}

	@Override
	protected void setRowBounds() {
		// TODO Auto-generated method stub
		// Constraints on attacker's utility
		List<Integer> ja = new ArrayList<Integer>();
		List<Double> ar = new ArrayList<Double>();
		double M = Configuration.MM * 1.0;
		for (int i = 0; i < this.nAttack; i++) {
			for(Target t : this.targetList) {
				int tID = t.getTargetID();
				double attReward = t.getPayoffStructure().getAdversaryReward();
				double attPen = t.getPayoffStructure().getAdversaryPenalty();
				// r_i <= x_t * (P^a_t - R^a_t) + R^a_t + (1 - h_it) * M
				ja.add(varMap.get("r" + (i + 1)));
				ar.add(1.0);
				ja.add(varMap.get("x" + tID));
				ar.add(-attPen + attReward);
				ja.add(varMap.get("h" + tID + "_" + (i + 1)));
				ar.add(M);
				
				addAndSetRow("attUUBConstr" + tID + "_" + (i + 1), BOUNDS_TYPE.UPPER, -M, attReward + M);
				rowMap.put("attUUBConstr" + tID + "_" + (i + 1), this.getNumRows());
				this.setMatRow(this.getNumRows(), ja, ar);
				ja.clear();
				ar.clear();
			}
		}
		
		for (int i = 0; i < this.nAttack; i++) {
			for(Target t : this.targetList) {
				int tID = t.getTargetID();
				double attReward = t.getPayoffStructure().getAdversaryReward();
				double attPen = t.getPayoffStructure().getAdversaryPenalty();
				// r_i >= x_t * (P^a_t - R^a_t) + R^a_t - (sum_j h_jt) * M
				ja.add(varMap.get("r" + (i + 1)));
				ar.add(1.0);
				ja.add(varMap.get("x" + tID));
				ar.add(-attPen + attReward);
				for(int j = 0; j < this.nAttack; j++) {
					ja.add(varMap.get("h" + tID + "_" + (j + 1)));
					ar.add(M);
				}
				
				addAndSetRow("attULBConstr" + tID + "_" + (i + 1), BOUNDS_TYPE.LOWER, attReward, M);
				rowMap.put("attULBConstr" + tID + "_" + (i + 1), this.getNumRows());
				this.setMatRow(this.getNumRows(), ja, ar);
				ja.clear();
				ar.clear();
			}
		}
		// Constraints on defender's utility
		for(int i = 0; i < this.nAttack; i++) {
			for(Target t : this.targetList) {
				int tID = t.getTargetID();
				double defReward = t.getPayoffStructure().getDefenderReward();
				double defPen = t.getPayoffStructure().getDefenderPenalty();
				// v_i <= x_t(R^d_t - P^d_t) + P^d_t + (1- h_it) M
				ja.add(varMap.get("v" + (i + 1)));
				ar.add(1.0);
				ja.add(varMap.get("x" + tID));
				ar.add(-defReward + defPen);
				ja.add(varMap.get("h" + tID + "_" + (i + 1)));
				ar.add(M);
				
				addAndSetRow("defUUBConstr" + tID + "_" + (i + 1), BOUNDS_TYPE.UPPER, -M, M + defPen);
				rowMap.put("defUUBConstr" + tID + "_" + (i + 1), this.getNumRows());
				this.setMatRow(this.getNumRows(), ja, ar);
				ja.clear();
				ar.clear();
			}
		}
		
		// Constraint on attacks
		for(int i = 0; i < this.nAttack; i++) {
			// sum_t h_it = 1
			for(Target t : this.targetList) {
				ja.add(varMap.get("h" + t.getTargetID() + "_" + (i + 1)));
				ar.add(1.0);
			}
			addAndSetRow("attAttackConstr1" + (i + 1), BOUNDS_TYPE.FIXED, 1.0, 1.0);
			varMap.put("attAttackConstr1" + (i + 1), this.getNumRows());
			this.setMatRow(this.getNumRows(), ja, ar);
			ja.clear();
			ar.clear();
		}
		for(Target t : this.targetList) {
			// sum_i h_it <= 1;
			for(int i = 0; i < this.nAttack; i++) {
				ja.add(varMap.get("h" + t.getTargetID() + "_" + (i + 1)));
				ar.add(1.0);
			}
			addAndSetRow("attAttackConstr2" + t.getTargetID(), BOUNDS_TYPE.UPPER, -M, 1.0);
			varMap.put("attAttackConstr2" + t.getTargetID(), this.getNumRows());
			this.setMatRow(this.getNumRows(), ja, ar);
			ja.clear();
			ar.clear();
		}
		// Constraint on defense
		// sum_t x_t <= nRes
		for(Target t : this.targetList) {
			ja.add(varMap.get("x" + t.getTargetID()));
			ar.add(1.0);
		}
		addAndSetRow("defResConstr", BOUNDS_TYPE.FIXED, this.nRes, this.nRes);
//		addAndSetRow("defResConstr", BOUNDS_TYPE.UPPER, -M, this.nRes);
		varMap.put("defResConstr", this.getNumRows());
		this.setMatRow(this.getNumRows(), ja, ar);
		ja.clear();
		ar.clear();
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
    public double getOptDefU() {
    	return this.getLPObjective();
    }
    public Set<Target> getOptAttacks() {
    	Set<Target> attackSet = new HashSet<Target>();
    	for(int i = 0; i < this.nAttack; i++) {
    		for(Target t : this.targetList) {
    			double isAttack = this.getColumnPrimal(this.varMap.get("h" + t.getTargetID() + "_" + (i + 1)));
    			if (isAttack > 0.5) {
    				attackSet.add(t);
    			}
    		}
    	}
    	return attackSet;
    }
    
    public double getOptDefURecompute() {
    	double defU = 0.0;
    	Set<Target> attackSet = this.getOptAttacks();
    	for(Target t : attackSet) {
    		double defCov = this.getOptDefCov(t);
    		double defReward = t.getPayoffStructure().getDefenderReward();
    		double defPen = t.getPayoffStructure().getDefenderPenalty();
    		double defEU = defCov * (defReward - defPen) + defPen;
    		defU += defEU;
    	}
    	return defU;
    }
    
    public double getOptAttU() {
    	double attU = 0.0;
    	Set<Target> attackSet = this.getOptAttacks();
    	for(Target t : attackSet) {
    		double defCov = this.getOptDefCov(t);
    		double attReward = t.getPayoffStructure().getAdversaryReward();
    		double attPen = t.getPayoffStructure().getAdversaryPenalty();
    		double attEU = defCov * (attPen - attReward) + attReward;
    		attU += attEU;
    	}
    	return attU;
    }
    
    public boolean isAttackCorrect() {
    	boolean yes = true;
    	Set<Target> attackSet = this.getOptAttacks();
    	double maxAttEU = Double.NEGATIVE_INFINITY;
    	for(Target t : this.targetList) {
    		if(!attackSet.contains(t)) {
    			double defCov = this.getOptDefCov(t);
    			double attReward = t.getPayoffStructure().getAdversaryReward();
    			double attPen = t.getPayoffStructure().getAdversaryPenalty();
    			double attEU = defCov * (attPen - attReward) + attReward;
    			if(maxAttEU < attEU) maxAttEU = attEU;
    		}
    	}
    	for(Target t : attackSet) {
    		double defCov = this.getOptDefCov(t);
			double attReward = t.getPayoffStructure().getAdversaryReward();
			double attPen = t.getPayoffStructure().getAdversaryPenalty();
			double attEU = defCov * (attPen - attReward) + attReward;
			if (attEU < maxAttEU) {
				yes = false;
				break;
			}
    	}
    	return yes;
    }
	@Override
	protected void generateData() {
		// TODO Auto-generated method stub
		
	}
	public void end() {
		super.end();
		varMap.clear();
		rowMap.clear();
	}
}
