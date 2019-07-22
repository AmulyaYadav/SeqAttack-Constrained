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

public class NoMovementCuttingPlane extends MIProblem{
	
	List<Target> targetList;
	int nDefRes;
	
	public HashMap<String, Integer> varMap; // for LP, column variables
	public HashMap<String, Integer> rowMap;	// for LP, row constraints
	
	HashMap<String, Double> masterOptimalVars;
	
	public NoMovementCuttingPlane(NoMovementMILPRevise masterProblem, List<Target> targetList, int nDefRes) {
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
		this.setProblemName("NoMovementCuttingPlane");
		this.setProblemType(PROBLEM_TYPE.LP, OBJECTIVE_TYPE.MAX);
	}

	@Override
	protected void setColBounds() {
		// TODO Auto-generated method stub
		int idx = 1;
		
		// lambda
		addAndSetColumn("lambda", BOUNDS_TYPE.FREE, -Configuration.MM, Configuration.MM, VARIABLE_TYPE.CONTINUOUS, 1);
		varMap.put("lambda", idx);
		idx++;
		
		
		// y_ij dual variables
		for (Target t : targetList) {
			int tID = t.getTargetID();
			for (Target tprime : targetList) {
				int tprimeID = tprime.getTargetID();
				if (tID < tprimeID) {
					addAndSetColumn("y" + tID + "_" + tprimeID, BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.CONTINUOUS, 0.0);
					//addAndSetColumn("y" + tID + "_" + tprimeID, BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.CONTINUOUS, masterOptimalVars.get("x" + tID + "_" + tprimeID));//last term is coefficient in objective of dual
					varMap.put("y" + tID + "_" + tprimeID, idx);
					idx++;
				}
			}
		}
		
		// z_ij dual variables
		for (Target t : targetList) {
			int tID = t.getTargetID();
			for (Target tprime : targetList) {
				int tprimeID = tprime.getTargetID();
				if (tID < tprimeID) {
					addAndSetColumn("z" + tID + "_" + tprimeID, BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.CONTINUOUS, 0.0);
					//addAndSetColumn("z" + tID + "_" + tprimeID, BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.CONTINUOUS, -1*masterOptimalVars.get("x" + tID + "_" + tprimeID));//last term is coefficient in objective of dual
					varMap.put("z" + tID + "_" + tprimeID, idx);
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
				
				if (tID < tprimeID)
				{
					ja.add(varMap.get("y" + tID + "_" + tprimeID));
					ar.add(1.0);
					
					ja.add(varMap.get("z" + tID + "_" + tprimeID));
					ar.add(1.0);
					
					addAndSetRow("SumConstraint_"+tprimeID+"_"+tID, BOUNDS_TYPE.UPPER, -Configuration.MM, 1.0);
					rowMap.put("SumConstraint_"+tprimeID+"_"+tID, this.getNumRows());
					/*System.out.println(this.getNumRows());
					for (int i=0;i<ja.size();i++)
						System.out.print(ja.get(i)+" ");
					System.out.println();
					for (int i=0;i<ar.size();i++)
						System.out.print(ar.get(i)+" ");*/
						
					this.setMatRow(this.getNumRows(), ja, ar);
				}
				
				
				ja.clear();
				ar.clear();
				
/*				if (tprimeID > tID) {
					ja.add(varMap.get("y" + tID + "_" + tprimeID));
					ar.add(1.0);
					
					ja.add(varMap.get("z" + tID + "_" + tprimeID));
					ar.add(1.0);	
				}
*/
			}
		}
		
		//addAndSetRow("SumConstraint", BOUNDS_TYPE.UPPER, -Configuration.MM, 1.0);
		//rowMap.put("SumConstraint", this.getNumRows());
		//this.setMatRow(this.getNumRows(), ja, ar);
		//ja.clear();
		//ar.clear();
	}
	
	public double getYduals(int tID, int tprimeID) {
		if (tID < tprimeID)
			return this.getColumnPrimal(varMap.get("y" + tID + "_" + tprimeID));
		else
			return this.getColumnPrimal(varMap.get("y" + tprimeID + "_" + tID));
	}	
	
	public double getYduals(Target t, Target tprime) {
		int tID = t.getTargetID();
		int tprimeID = tprime.getTargetID();
		if (tID < tprimeID)
			return this.getColumnPrimal(varMap.get("y" + tID + "_" + tprimeID));
		else
			return this.getColumnPrimal(varMap.get("y" + tprimeID + "_" + tID));
	}
	
	public double getZduals(int tID, int tprimeID) {
		if (tID < tprimeID)
			return this.getColumnPrimal(varMap.get("z" + tID + "_" + tprimeID));
		else
			return this.getColumnPrimal(varMap.get("z" + tprimeID + "_" + tID));
	}	
	
	public double getZduals(Target t, Target tprime) {
		int tID = t.getTargetID();
		int tprimeID = tprime.getTargetID();
		if (tID < tprimeID)
			return this.getColumnPrimal(varMap.get("z" + tID + "_" + tprimeID));
		else
			return this.getColumnPrimal(varMap.get("z" + tprimeID + "_" + tID));
	}
	
	public double getLambda() {
		return this.getColumnPrimal(varMap.get("lambda"));
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
