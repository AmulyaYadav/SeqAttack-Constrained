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

public class ConstrainedCuttingPlane extends MIProblem{
	
	List<Target> targetList;
	int nDefRes;
	
	public HashMap<String, Integer> varMap; // for LP, column variables
	public HashMap<String, Integer> rowMap;	// for LP, row constraints
	
	HashMap<String, Double> masterOptimalVars;
	
	public ConstrainedCuttingPlane(ConstrainedMovementMILP masterProblem, List<Target> targetList, int nDefRes) {
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
					masterOptimalVars.put("x" + tID + "_" + tprimeID, masterProblem.getDefenderCoveragePair(tID, tprimeID));
				}
				else if (tprimeID < tID) {
						masterOptimalVars.put("x" + tprimeID + "_" + tID, masterProblem.getDefenderCoveragePair(tprimeID, tID));
				}
			}
		}*/
	}
	
	@Override
	protected void setProblemType() {
		// TODO Auto-generated method stub
		this.setProblemName("ConstrainedCuttingPlane");
		this.setProblemType(PROBLEM_TYPE.LP, OBJECTIVE_TYPE.MAX);
	}

	@Override
	protected void setColBounds() {
		// TODO Auto-generated method stub
		int idx = 1;
		
		// lambda
		addAndSetColumn("gamma", BOUNDS_TYPE.FREE, -Configuration.MM, Configuration.MM, VARIABLE_TYPE.CONTINUOUS, 1);
		varMap.put("gamma", idx);
		idx++;
		
		
		
		///objective function coefficients for all these variables will be set during runtime
		// lambda^1_ij dual variables
		for (Target t : targetList) {
			int tID = t.getTargetID();
			for (Target tprime : targetList) {
				int tprimeID = tprime.getTargetID();
				if (tID != tprimeID) {
					addAndSetColumn("lambda^1" + tID + "_" + tprimeID, BOUNDS_TYPE.LOWER, 0, Configuration.MM, VARIABLE_TYPE.CONTINUOUS, 0);
					//addAndSetColumn("y" + tID + "_" + tprimeID, BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.CONTINUOUS, 0.0);
					//addAndSetColumn("y" + tID + "_" + tprimeID, BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.CONTINUOUS, masterOptimalVars.get("x" + tID + "_" + tprimeID));//last term is coefficient in objective of dual
					varMap.put("lambda^1" + tID + "_" + tprimeID, idx);
					idx++;
					
					addAndSetColumn("lambda^2" + tID + "_" + tprimeID, BOUNDS_TYPE.LOWER, 0, Configuration.MM, VARIABLE_TYPE.CONTINUOUS, 0);
					//addAndSetColumn("y" + tID + "_" + tprimeID, BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.CONTINUOUS, 0.0);
					//addAndSetColumn("y" + tID + "_" + tprimeID, BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.CONTINUOUS, masterOptimalVars.get("x" + tID + "_" + tprimeID));//last term is coefficient in objective of dual
					varMap.put("lambda^2" + tID + "_" + tprimeID, idx);
					idx++;
					
					addAndSetColumn("lambda^3" + tID + "_" + tprimeID, BOUNDS_TYPE.LOWER, 0, Configuration.MM, VARIABLE_TYPE.CONTINUOUS, 0);
					//addAndSetColumn("y" + tID + "_" + tprimeID, BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.CONTINUOUS, 0.0);
					//addAndSetColumn("y" + tID + "_" + tprimeID, BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.CONTINUOUS, masterOptimalVars.get("x" + tID + "_" + tprimeID));//last term is coefficient in objective of dual
					varMap.put("lambda^3" + tID + "_" + tprimeID, idx);
					idx++;
					
					addAndSetColumn("lambda^4" + tID + "_" + tprimeID, BOUNDS_TYPE.LOWER, 0, Configuration.MM, VARIABLE_TYPE.CONTINUOUS, 0);
					//addAndSetColumn("y" + tID + "_" + tprimeID, BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.CONTINUOUS, 0.0);
					//addAndSetColumn("y" + tID + "_" + tprimeID, BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.CONTINUOUS, masterOptimalVars.get("x" + tID + "_" + tprimeID));//last term is coefficient in objective of dual
					varMap.put("lambda^4" + tID + "_" + tprimeID, idx);
					idx++;
				}
			}
		}
		
		
	}

	@Override
	protected void setRowBounds() {
		
		
		for(Target t : targetList) {
			int tID = t.getTargetID();
			for(Target tprime : targetList) {
				int tprimeID = tprime.getTargetID();
				
				// TODO Auto-generated method stub
				List<Integer> ja = new ArrayList<Integer>();
				List<Double> ar = new ArrayList<Double>();
				
				if (tID != tprimeID)
				{
					ja.add(varMap.get("lambda^1" + tID + "_" + tprimeID));
					ar.add(1.0);
					
					ja.add(varMap.get("lambda^2" + tID + "_" + tprimeID));
					ar.add(1.0);
					
					addAndSetRow("SumConstraint_"+tprimeID+"_"+tID, BOUNDS_TYPE.UPPER, -Configuration.MM, 1.0);
					rowMap.put("SumConstraint_"+tprimeID+"_"+tID, this.getNumRows());
					this.setMatRow(this.getNumRows(), ja, ar);
				}
				
				
				ja.clear();
				ar.clear();
				
				if (tID != tprimeID)
				{
					ja.add(varMap.get("lambda^3" + tID + "_" + tprimeID));
					ar.add(1.0);
					
					ja.add(varMap.get("lambda^4" + tID + "_" + tprimeID));
					ar.add(1.0);
					
					addAndSetRow("SumConstraint^2_"+tprimeID+"_"+tID, BOUNDS_TYPE.UPPER, -Configuration.MM, 1.0);
					rowMap.put("SumConstraint^2_"+tprimeID+"_"+tID, this.getNumRows());
					this.setMatRow(this.getNumRows(), ja, ar);
				}
			}
		}
	}
	
	public double getLambda1duals(int tID, int tprimeID) {
		return this.getColumnPrimal(varMap.get("lambda^1" + tID + "_" + tprimeID));
	}	
	
	public double getLambda2duals(int tID, int tprimeID) {
		return this.getColumnPrimal(varMap.get("lambda^2" + tID + "_" + tprimeID));
	}	
	
	public double getLambda3duals(int tID, int tprimeID) {
		return this.getColumnPrimal(varMap.get("lambda^3" + tID + "_" + tprimeID));
	}	
	
	public double getLambda4duals(int tID, int tprimeID) {
		return this.getColumnPrimal(varMap.get("lambda^4" + tID + "_" + tprimeID));
	}	
	
	public double getGamma() {
		return this.getColumnPrimal(varMap.get("gamma"));
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
