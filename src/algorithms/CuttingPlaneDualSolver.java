package algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import lpWrappers.Configuration;
import lpWrappers.LPSolverException;
import lpWrappers.AMIProblem.BOUNDS_TYPE;
import models.Target;

public class CuttingPlaneDualSolver {
	
	List<Target> targetList;
	int nDefRes;
	
	NoMovementMILPRevise masterProblem;
	
	public NoMovementCuttingPlane cpProblem;
	public NoMovementMaxViolatedConstraint maxViolatedProblem;
	
	int cuttingPlaneIndex;
	
	
	double cuttingPlaneEpsilon = 0.0001;
	
	public CuttingPlaneDualSolver(NoMovementMILPRevise masterProblem, List<Target> targetList, int nDefRes, int cuttingPlaneIndex)///last parameter just for bookkeeping
	{
		this.targetList = targetList;
		this.nDefRes = nDefRes;
		this.masterProblem = masterProblem;
		
		this.cpProblem = new NoMovementCuttingPlane(this.masterProblem, this.targetList, this.nDefRes);
		this.maxViolatedProblem = new NoMovementMaxViolatedConstraint(this.cpProblem, this.targetList, this.nDefRes);
		cpProblem.loadProblem();
		maxViolatedProblem.loadProblem();
		
		this.cuttingPlaneIndex = cuttingPlaneIndex;
	}
	
	public int solve()
	{
		
		
		boolean remainingConstraintsInfeasible = true;
		int numGeneratedConstraints=0;
		
		while(remainingConstraintsInfeasible)
		{
			try {
				cpProblem.solve();
				cpProblem.writeProb("CP_LP");
			} catch (LPSolverException e) {
				// TODO Auto-generated catch block
				System.out.println("Dual problem fail with message: "+e.getMessage());
				e.printStackTrace();
			}
			
			System.out.println("Optimal Solution of Cutting Planes with "+numGeneratedConstraints+" added constraints: "+cpProblem.getLPObjective());
			
			if (cpProblem.getLPObjective()<cuttingPlaneEpsilon)
			{
				System.out.println("Compact variables correspond to optimal defense mixed strategy");
				return 0;//implies that masterProblem does not need to be resolved...the current solution is the optimal solution
			}
			else if (cpProblem.getLPObjective()>=cuttingPlaneEpsilon)
			{
			
			
			
			////update the objective function of maxViolatedConstraint
			for (int i=1;i<=targetList.size();i++)
			{
				for (int j=i+1;j<=targetList.size();j++)
				{
					maxViolatedProblem.setObjectiveCoef(maxViolatedProblem.varMap.get("h" + i + "_" + j), cpProblem.getYduals(i, j) - cpProblem.getZduals(i, j));
				}
			}
			
			try {
				maxViolatedProblem.solve();
				maxViolatedProblem.writeProb("MaxViolation_LP");
			} catch (LPSolverException e) {
				// TODO Auto-generated catch block
				System.out.println("Max violated problem fail with message: "+e.getMessage());
				e.printStackTrace();
			}
			
			System.out.println("Optimal Solution of Max Violated Constraint Problem " + (maxViolatedProblem.getLPObjective() + cpProblem.getLambda()));
	
			
			if (maxViolatedProblem.getLPObjective() + cpProblem.getLambda() <= cuttingPlaneEpsilon)
			{
				remainingConstraintsInfeasible = false;
				continue;
			}
			else///add a constraint to dual of cutting plane
			{
				numGeneratedConstraints++;//one more constraint added to dual
				
				List<Integer> ja = new ArrayList<Integer>();
				List<Double> ar = new ArrayList<Double>();
				
				for (int i=1;i<=targetList.size();i++)
				{
					for (int j=i+1;j<=targetList.size();j++)
					{
						///PRECISION ERROR
						if (maxViolatedProblem.getDefenderCoverage(i)>=1-cuttingPlaneEpsilon && maxViolatedProblem.getDefenderCoverage(j)>=1-cuttingPlaneEpsilon)
						{
							ja.add(cpProblem.varMap.get("y" + i + "_" + j));
							ar.add(1.0);
							
							ja.add(cpProblem.varMap.get("z" + i + "_" + j));
							ar.add(-1.0);
						}
							
					}
				}
				
				ja.add(cpProblem.varMap.get("lambda"));
				ar.add(1.0);
				
				
				cpProblem.addAndSetRow("ConstrGen"+numGeneratedConstraints, BOUNDS_TYPE.UPPER, -Configuration.MM, 0.0);
				cpProblem.rowMap.put("ConstrGen"+numGeneratedConstraints, cpProblem.getNumRows());
				cpProblem.setMatRow(cpProblem.getNumRows(), ja, ar);
				ja.clear();
				ar.clear();
			}//resolve this further constrained cpProblem by going to top of while loop
			
			}
			
		}//end while
		
		//check optimal objective function value of dual
		if (cpProblem.getLPObjective()>=cuttingPlaneEpsilon)//PRECISION ERROR
		{
			//add a cutting plane
			List<Integer> ja = new ArrayList<Integer>();
			List<Double> ar = new ArrayList<Double>();
			
			for (int i=1;i<=targetList.size();i++)
			{
				for (int j=i+1;j<=targetList.size();j++)
				{
					ja.add(masterProblem.varMap.get("x" + i + "_" + j));
					ar.add(cpProblem.getYduals(i, j)-cpProblem.getZduals(i, j));
				}
			}
			
			masterProblem.addAndSetRow("CuttingPlane" + cuttingPlaneIndex, BOUNDS_TYPE.UPPER, -Configuration.MM, - cpProblem.getLambda());
			masterProblem.rowMap.put("CuttingPlane"+cuttingPlaneIndex, masterProblem.getNumRows());
			masterProblem.setMatRow(masterProblem.getNumRows(), ja, ar);
			ja.clear();
			ar.clear();
			
			cuttingPlaneIndex++;//added one more cutting plane
			
			return 1;//implies that masterProblem needs to be resolved with this new constraint
			
		}
		else if (cpProblem.getLPObjective()<cuttingPlaneEpsilon)
		{
			System.out.println("Compact variables correspond to optimal defense mixed strategy");
			return 0;//implies that masterProblem does not need to be resolved...the current solution is the optimal solution
		}
		else
		{
			System.out.println("Well, we did not expect that");
			return -1;///something is wrong...this code should never be reached
		}
		
		
	}

}
