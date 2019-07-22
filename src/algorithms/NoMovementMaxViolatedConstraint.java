package algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lpWrappers.Configuration;
import lpWrappers.MIProblem;
import lpWrappers.AMIProblem.BOUNDS_TYPE;
import lpWrappers.AMIProblem.OBJECTIVE_TYPE;
import lpWrappers.AMIProblem.PROBLEM_TYPE;
import lpWrappers.AMIProblem.VARIABLE_TYPE;
import models.Target;

public class NoMovementMaxViolatedConstraint extends MIProblem{
	
	List<Target> targetList;
	int nDefRes;
	
	HashMap<String, Integer> varMap; // for MILP, column variables
	HashMap<String, Integer> rowMap;	// for MILP, row constraints
	
	HashMap<String, Double> masterOptimalVars;
	
	public NoMovementMaxViolatedConstraint(NoMovementCuttingPlane masterProblem, List<Target> targetList, int nDefRes) {
		super();
		this.targetList = targetList;
		this.nDefRes = nDefRes;
		this.varMap = new HashMap<String, Integer>();
		this.rowMap = new HashMap<String, Integer>();
		
		
		this.masterOptimalVars = new HashMap<String, Double>();
		/*for (Target t : targetList) {
			int tID = t.getTargetID();
			for (Target tprime : targetList) {
				int tprimeID = tprime.getTargetID();
				if (tID < tprimeID) {
					masterOptimalVars.put("y" + tID + "_" + tprimeID, masterProblem.getYduals(tID, tprimeID));
					masterOptimalVars.put("z" + tID + "_" + tprimeID, masterProblem.getZduals(tID, tprimeID));
				}
			}
		}*/
	}

	@Override
	protected void setProblemType() {
		// TODO Auto-generated method stub
		this.setProblemName("NoMovementMaxViolatedConstraint");
		this.setProblemType(PROBLEM_TYPE.MIP, OBJECTIVE_TYPE.MAX);
	}

	@Override
	protected void setColBounds() {
		// TODO Auto-generated method stub
		int idx = 1;
		

		// Binary variables which denote pure strategy corresponding to max violated constraint
		for (Target t : targetList) {
			addAndSetColumn("h" + t.getTargetID(), BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.INTEGER, 0);
			varMap.put("h" + t.getTargetID(), idx);
			idx++;
		}
		
		// h_ij dual variables
		for (Target t : targetList) {
			int tID = t.getTargetID();
			for (Target tprime : targetList) {
				int tprimeID = tprime.getTargetID();
				if (tID < tprimeID) {
					addAndSetColumn("h" + tID + "_" + tprimeID, BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.INTEGER, 0);
					//addAndSetColumn("h" + tID + "_" + tprimeID, BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.INTEGER, masterOptimalVars.get("y" + tID + "_" + tprimeID) - masterOptimalVars.get("z" + tID + "_" + tprimeID));
					varMap.put("h" + tID + "_" + tprimeID, idx);
					idx++;
				}
			}
		}
	}

	@Override
	protected void setRowBounds() {
		// TODO Auto-generated method stub
		List<Integer> ja = new ArrayList<Integer>();
		List<Double> ar = new ArrayList<Double>();
		for (Target t : targetList) {
			ja.add(varMap.get("h" + t.getTargetID()));
			ar.add(1.0);
		}
		
		addAndSetRow("BudgetConstraint", BOUNDS_TYPE.FIXED, nDefRes, nDefRes);
		rowMap.put("BudgetConstraint", this.getNumRows());
		this.setMatRow(this.getNumRows(), ja, ar);
		ja.clear();
		ar.clear();
		
		
		/*for(Target t : targetList) {
			int tID = t.getTargetID();
			for(Target tprime : targetList) {
				int tprimeID = tprime.getTargetID();
				if (tprimeID > tID) {
					ja.add(varMap.get("h" + tID + "_" + tprimeID));
					ar.add(1.0);
					
					ja.add(varMap.get("h" + tID));
					ar.add(-1.0);
					
					addAndSetRow("DefResPairUB1" + tID + "_" + tprimeID, BOUNDS_TYPE.UPPER, -Configuration.MM, 0.0);
					rowMap.put("DefResPairUB1" + tID + "_" + tprimeID, this.getNumRows());
					this.setMatRow(this.getNumRows(), ja, ar);
					ja.clear();
					ar.clear();
				}
			}
		}
		
		for(Target t : targetList) {
			int tID = t.getTargetID();
			for(Target tprime : targetList) {
				int tprimeID = tprime.getTargetID();
				if (tprimeID > tID) {
					ja.add(varMap.get("h" + tID + "_" + tprimeID));
					ar.add(1.0);
					
					ja.add(varMap.get("h" + tprimeID));
					ar.add(-1.0);
					
					addAndSetRow("DefResPairUB2" + tID + "_" + tprimeID, BOUNDS_TYPE.UPPER, -Configuration.MM, 0.0);
					rowMap.put("DefResPairUB2" + tID + "_" + tprimeID, this.getNumRows());
					this.setMatRow(this.getNumRows(), ja, ar);
					ja.clear();
					ar.clear();
				}
			}
		}*/
		
		
		
		for(Target t : targetList) {
			int tID = t.getTargetID();
			for(Target tprime : targetList) {
				int tprimeID = tprime.getTargetID();
				if (tprimeID > tID) {
					ja.add(varMap.get("h" + tID + "_" + tprimeID));
					ar.add(1.0);
					
					ja.add(varMap.get("h" + tID));
					ar.add(-1.0);
					
					addAndSetRow("DefResPairUB1" + tID + "_" + tprimeID, BOUNDS_TYPE.UPPER, -Configuration.MM, 0.0);
					rowMap.put("DefResPairUB1" + tID + "_" + tprimeID, this.getNumRows());
					this.setMatRow(this.getNumRows(), ja, ar);
					ja.clear();
					ar.clear();
				}
			}
		}
		
		
		for(Target t : targetList) {
			int tID = t.getTargetID();
			for(Target tprime : targetList) {
				int tprimeID = tprime.getTargetID();
				if (tprimeID > tID) {
					ja.add(varMap.get("h" + tID + "_" + tprimeID));
					ar.add(1.0);
					
					ja.add(varMap.get("h" + tprimeID));
					ar.add(-1.0);
					
					addAndSetRow("DefResPairUB2" + tID + "_" + tprimeID, BOUNDS_TYPE.UPPER, -Configuration.MM, 0.0);
					rowMap.put("DefResPairUB2" + tID + "_" + tprimeID, this.getNumRows());
					this.setMatRow(this.getNumRows(), ja, ar);
					ja.clear();
					ar.clear();
				}
			}
		}
		
		for(Target t : targetList) {
			int tID = t.getTargetID();
			for(Target tprime : targetList) {
				int tprimeID = tprime.getTargetID();
				if (tprimeID > tID) {
					ja.add(varMap.get("h" + tID + "_" + tprimeID));
					ar.add(-1.0);
					
					ja.add(varMap.get("h" + tprimeID));
					ar.add(1.0);
					
					ja.add(varMap.get("h" + tID));
					ar.add(1.0);
					
					addAndSetRow("DefResPairUB3" + tID + "_" + tprimeID, BOUNDS_TYPE.UPPER, -Configuration.MM, 1.0);
					rowMap.put("DefResPairUB3" + tID + "_" + tprimeID, this.getNumRows());
					this.setMatRow(this.getNumRows(), ja, ar);
					ja.clear();
					ar.clear();
				}
			}
		}
		
	}

	@Override
	protected void generateData() {
		// TODO Auto-generated method stub
		
	}
	
	public double getDefenderCoverage(int targetID) {
		/////////////////////////POTENTIAL PRECISION FLOW ERROR
		return this.getColumnPrimal(varMap.get("h" + targetID));
	}
	
	public void end() {
		super.end();
		varMap.clear();
		rowMap.clear();
	}

}
