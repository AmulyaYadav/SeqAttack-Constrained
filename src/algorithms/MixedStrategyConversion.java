package algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

import lpWrappers.Configuration;
import lpWrappers.MIProblem;
import models.Target;

public class MixedStrategyConversion extends MIProblem{
	Set<Target> targetSet;
	int nRes;
	HashMap<Set<Target>, Double> pairCovProbs;
	
	Set<Set<Target>> pureStrategySet;
	
	HashMap<String, Integer> varMap; // for MILP, column variables
	HashMap<String, Integer> rowMap;	// for MILP, row constraints
	
	public MixedStrategyConversion(Set<Target> targetSet, int nRes, HashMap<Set<Target>, Double> pairCovProbs) {
		this.targetSet = targetSet;
		this.nRes = nRes;
		this.pairCovProbs = pairCovProbs;
		
		pureStrategySet = Sets.combinations(targetSet, nRes);
		
		this.varMap = new HashMap<String, Integer>();
		this.rowMap = new HashMap<String, Integer>();
	}

	@Override
	protected void setProblemType() {
		// TODO Auto-generated method stub
		this.setProblemName("MixedStrategyConvergion");
		this.setProblemType(PROBLEM_TYPE.LP, OBJECTIVE_TYPE.MIN);
	}

	@Override
	protected void setColBounds() {
		// TODO Auto-generated method stub
		int idx = 1;
		
		// Variables for probability of pure strategies
		for(Set<Target> pureStrategy : pureStrategySet) {
			String var = "x";
			for (Target t : pureStrategy) {
				var = var + "_" + t.getTargetID();
			}
			addAndSetColumn(var, BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.CONTINUOUS, 0);
			varMap.put(var, idx);
			idx++;
		}
		
		// Variable for objective
		addAndSetColumn("d", BOUNDS_TYPE.FREE, -Configuration.MM, Configuration.MM, VARIABLE_TYPE.CONTINUOUS, 1.0);
		varMap.put("d", idx);
		idx++;
	}

	@Override
	protected void setRowBounds() {
		// TODO Auto-generated method stub
		// Constraints on pure strategies
		List<Integer> ja = new ArrayList<Integer>();
		List<Double> ar = new ArrayList<Double>();
		for(java.util.Map.Entry<Set<Target>, Double> entry : this.pairCovProbs.entrySet()) {
			double cov = entry.getValue();
			Set<Target> targetPair = entry.getKey();
			for(Set<Target> pureStrategy : pureStrategySet) {
				if(Sets.difference(targetPair, pureStrategy).isEmpty()) {
					String var = "x";
					for (Target t : pureStrategy) {
						var = var + "_" + t.getTargetID();
					}
					ja.add(varMap.get(var));
					ar.add(1.0);
				}
			}
			ja.add(varMap.get("d"));
			ar.add(1.0);
			String temp = "constrLB1";
			for(Target t : targetPair) {
				temp = temp + "_" + t.getTargetID();
			}
			addAndSetRow(temp, BOUNDS_TYPE.LOWER, cov, Configuration.MM);
			rowMap.put(temp, this.getNumRows());
			this.setMatRow(this.getNumRows(), ja, ar);
			ja.clear();
			ar.clear();
		}
		
		for(java.util.Map.Entry<Set<Target>, Double> entry : this.pairCovProbs.entrySet()) {
			double cov = entry.getValue();
			Set<Target> targetPair = entry.getKey();
			for(Set<Target> pureStrategy : pureStrategySet) {
				if(Sets.difference(targetPair, pureStrategy).isEmpty()) {
					String var = "x";
					for (Target t : pureStrategy) {
						var = var + "_" + t.getTargetID();
					}
					ja.add(varMap.get(var));
					ar.add(-1.0);
				}
			}
			ja.add(varMap.get("d"));
			ar.add(1.0);
			String temp = "constrLB2";
			for(Target t : targetPair) {
				temp = temp + "_" + t.getTargetID();
			}
			addAndSetRow(temp, BOUNDS_TYPE.LOWER, -cov, Configuration.MM);
			rowMap.put(temp, this.getNumRows());
			this.setMatRow(this.getNumRows(), ja, ar);
			ja.clear();
			ar.clear();
		}
		
		// Mixed strategy constraints
		for(Set<Target> pureStrategy : pureStrategySet) {
			String var = "x";
			for (Target t : pureStrategy) {
				var = var + "_" + t.getTargetID();
			}
			ja.add(varMap.get(var));
			ar.add(1.0);
		}
		addAndSetRow("MixedStrategyConstr", BOUNDS_TYPE.FIXED, 1.0, 1.0);
		rowMap.put("MixedStrategyConstr", this.getNumRows());
		this.setMatRow(this.getNumRows(), ja, ar);
		ja.clear();
		ar.clear();
	}
	
	public double getDistance() {
		return this.getColumnPrimal(varMap.get("d"));
	}
	
	public double getProb(Set<Target> pureStrategy) {
		String var = "x";
		for (Target t : pureStrategy) {
			var = var + "_" + t.getTargetID();
		}
		return this.getColumnPrimal(varMap.get(var));
	}
	public Map<Set<Target>, Double> getMixedStrategy() {
		Map<Set<Target>, Double> mixedStrategy = new HashMap<Set<Target>, Double>();
		for(Set<Target> pureStrategy : pureStrategySet) {
			mixedStrategy.put(pureStrategy, getProb(pureStrategy));
		}
		
		return mixedStrategy;
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
