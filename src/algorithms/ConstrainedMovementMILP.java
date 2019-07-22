package algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import lpWrappers.Configuration;
import lpWrappers.LPSolverException;
import lpWrappers.MIProblem;
import models.Target;


public class ConstrainedMovementMILP extends MIProblem{
	List<Target> targetList;
	int nDefRes;
	
	HashMap<String, Integer> varMap; // for MILP, column variables
	HashMap<String, Integer> rowMap;	// for MILP, row constraints

	public ConstrainedMovementMILP(List<Target> targetList, int nDefRes) {
		super();
//		this.redirectOutput(null);
		this.targetList = targetList;
		this.nDefRes = nDefRes;
		this.varMap = new HashMap<String, Integer>();
		this.rowMap = new HashMap<String, Integer>();
		
		///Code to set time limits on MILP Computation
		/*try {
			cplex.setParam(IloCplex.DoubleParam.TiLim, 300.00);
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
	protected void setProblemType() {
		// TODO Auto-generated method stub
		this.setProblemName("ConstrainedMILP");
		this.setProblemType(PROBLEM_TYPE.MIP, OBJECTIVE_TYPE.MAX);
	}
	@Override
	protected void setColBounds() {
		// TODO Auto-generated method stub
		int idx = 1;
		
		// Defender objectives
		addAndSetColumn("v", BOUNDS_TYPE.FREE, -Configuration.MM, Configuration.MM, VARIABLE_TYPE.CONTINUOUS, 1);
		varMap.put("v", idx);
		idx++;
		
		// Attacker objectives
		addAndSetColumn("r", BOUNDS_TYPE.FREE, -Configuration.MM, Configuration.MM, VARIABLE_TYPE.CONTINUOUS, 0);
		varMap.put("r", idx);
		idx++;
		
		// Defender coverage probability at each target
		for (Target t : targetList) {
			addAndSetColumn("x" + t.getTargetID(), BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.CONTINUOUS, 0);
			varMap.put("x" + t.getTargetID(), idx);
			idx++;
		}
		
		// Defender coverage probability at each pair of target x_{i,j}
		for (Target t : targetList) {
			int tID = t.getTargetID();
			for (Target tprime : targetList) {
				int tprimeID = tprime.getTargetID();
				if (tID < tprimeID) {
					addAndSetColumn("x" + tID + "_" + tprimeID, BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.CONTINUOUS, 0);
					varMap.put("x" + tID + "_" + tprimeID, idx);
					idx++;
				}
			}
		}
		
		// Defender coverage probability at each pair of target x_{-i,j}
				for (Target t : targetList) {
					int tID = t.getTargetID();
					for (Target tprime : targetList) {
						int tprimeID = tprime.getTargetID();
						if (tID < tprimeID) {
							addAndSetColumn("x-" + tID + "_" + tprimeID, BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.CONTINUOUS, 0);
							varMap.put("x" + tID + "_" + tprimeID, idx);
							idx++;
						}
					}
				}
		
		// Binary variables to indicate attacked targets
		for (Target t : targetList) {
			addAndSetColumn("h" + t.getTargetID(), BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.INTEGER, 0);
			varMap.put("h" + t.getTargetID(), idx);
			idx++;
		}
		
		for (Target t : targetList) {
			int tID = t.getTargetID();
			addAndSetColumn("qProtected" + tID, BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.INTEGER, 0);
			varMap.put("qProtected" + tID, idx);
			idx++;
		}
		
		for (Target t : targetList) {
			int tID = t.getTargetID();
			addAndSetColumn("qUnProtected" + tID, BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.INTEGER, 0);
			varMap.put("qUnProtected" + tID, idx);
			idx++;
		}
	}
	
	protected void setDefUConstraint() {
		List<Integer> ja = new ArrayList<Integer>();
		List<Double> ar = new ArrayList<Double>();
		double M = Configuration.MM * 1.0;
		for(Target i : targetList) {
			int iID = i.getTargetID();
			double iDefReward = i.getPayoffStructure().getDefenderReward();
			double iDefPenalty = i.getPayoffStructure().getDefenderPenalty();
			for (Target j : targetList) {
				int jID = j.getTargetID();
				double jDefReward = j.getPayoffStructure().getDefenderReward();
				double jDefPenalty = j.getPayoffStructure().getDefenderPenalty();
				for(Target k : targetList) {
					int kID = k.getTargetID();
					double kDefReward = k.getPayoffStructure().getDefenderReward();
					double kDefPenalty = k.getPayoffStructure().getDefenderPenalty();
					if (jID != iID && kID != iID) {
						ja.add(varMap.get("x" + iID));
						ar.add(iDefReward - iDefPenalty + jDefPenalty - kDefPenalty);
						
						if (iID < jID) {
							ja.add(varMap.get("x" + iID + "_" + jID));
						}
						else {
							ja.add(varMap.get("x" + jID + "_" + iID));
						}
						ar.add(jDefReward - jDefPenalty);
						
						
						//for x_{-i,j} variables, x_{-i,j} != x_{-j,i}. Hence, this if condition is not needed
						//if (iID < kID) {
							ja.add(varMap.get("x-" + iID + "_" + kID));
						//}
						//else {
						//	ja.add(varMap.get("x-" + jID + "_" + kID));
						//}
						ar.add(kDefReward - kDefPenalty);
						
						
						ja.add(varMap.get("h" + iID));
						ar.add(-M);
						ja.add(varMap.get("qProtected" + jID));
						ar.add(-M);
						ja.add(varMap.get("qUnProtected" + kID));
						ar.add(-M);
						
						ja.add(varMap.get("v"));
						ar.add(-1.0);
						
						addAndSetRow("defUConstr" + iID + "_" + jID + "_" + kID, BOUNDS_TYPE.LOWER, -iDefPenalty - kDefPenalty - 3 * M, M);
						rowMap.put("defUConstr" + iID + "_" + jID + "_" + kID, this.getNumRows());
						this.setMatRow(this.getNumRows(), ja, ar);
						ja.clear();
						ar.clear();
					}
				}
			}
		}
	}
	
	protected void setAttUConstraint() {
		List<Integer> ja = new ArrayList<Integer>();
		List<Double> ar = new ArrayList<Double>();
		double M = Configuration.MM * 1.0;
		// Lower bound
		for(Target i : targetList) {
			int iID = i.getTargetID();
			double iAttReward = i.getPayoffStructure().getAdversaryReward();
			double iAttPenalty = i.getPayoffStructure().getAdversaryPenalty();
			for(Target j : targetList) {
				int jID = j.getTargetID();
				double jAttReward = j.getPayoffStructure().getAdversaryReward();
				double jAttPenalty = j.getPayoffStructure().getAdversaryPenalty();
				for(Target k : targetList) {
					int kID = k.getTargetID();
					double kAttReward = k.getPayoffStructure().getAdversaryReward();
					double kAttPenalty = k.getPayoffStructure().getAdversaryPenalty();
					if (iID != jID && iID != kID) {
						ja.add(varMap.get("x" + iID));
						ar.add(iAttPenalty - iAttReward + jAttReward - kAttReward);
						
				
						if (iID < jID) {
							ja.add(varMap.get("x" + iID + "_" + jID));
						}
						else {
							ja.add(varMap.get("x" + jID + "_" + iID));
						}
						ar.add(jAttPenalty - jAttReward);
							
						//if (iID < kID) {
							ja.add(varMap.get("x-" + iID + "_" + kID));
						//}
						//else {
						//	ja.add(varMap.get("x-" + kID + "_" + iID));
						//}
						ar.add(kAttPenalty - kAttReward);
							
						
						ja.add(varMap.get("r"));
						ar.add(-1.0);
						addAndSetRow("attULBConstr" + iID + "_" + jID + "_" + kID, BOUNDS_TYPE.UPPER, -M, -kAttReward - iAttReward);///CHECK THIS
						rowMap.put("attULBConstr" + iID + "_" + jID + "_" + kID, this.getNumRows());
						this.setMatRow(this.getNumRows(), ja, ar);
						ja.clear();
						ar.clear();
					}
				}
			}
		}
		
		// Upper bound
		
		for(Target i : targetList) {
			int iID = i.getTargetID();
			double iAttReward = i.getPayoffStructure().getAdversaryReward();
			double iAttPenalty = i.getPayoffStructure().getAdversaryPenalty();
			for(Target j : targetList) {
				int jID = j.getTargetID();
				double jAttReward = j.getPayoffStructure().getAdversaryReward();
				double jAttPenalty = j.getPayoffStructure().getAdversaryPenalty();
				for(Target k : targetList) {
					int kID = k.getTargetID();
					double kAttReward = k.getPayoffStructure().getAdversaryReward();
					double kAttPenalty = k.getPayoffStructure().getAdversaryPenalty();
					if (iID != jID && iID != kID) {
						ja.add(varMap.get("x" + iID));
						ar.add(iAttPenalty - iAttReward + jAttReward - kAttReward);
						
						if (iID < jID) {
							ja.add(varMap.get("x" + iID + "_" + jID));
						}
						else {
							ja.add(varMap.get("x" + jID + "_" + iID));
						}
						ar.add(jAttPenalty - jAttReward);
							
						//if (iID < kID) {
							ja.add(varMap.get("x-" + iID + "_" + kID));
						//}
						//else {
						//	ja.add(varMap.get("x" + kID + "_" + iID));
						//}
						ar.add(kAttPenalty - kAttReward);
						
						
						ja.add(varMap.get("h" + iID));
						ar.add(-M);
						ja.add(varMap.get("qProtected" + jID));
						ar.add(-M);
						ja.add(varMap.get("qUnProtected" + kID));
						ar.add(-M);
						
						ja.add(varMap.get("r"));
						ar.add(-1.0);
						addAndSetRow("attUUBConstr" + iID + "_" + jID + "_" + kID, BOUNDS_TYPE.LOWER, -3 * M - kAttReward - iAttReward, M);///CHECK THIS
						rowMap.put("attUUBConstr" + iID + "_" + jID + "_" + kID, this.getNumRows());
						this.setMatRow(this.getNumRows(), ja, ar);
						ja.clear();
						ar.clear();
					}
				}
			}
		}
	}
	
	protected void setAttackedTargetConstraint() {
		List<Integer> ja = new ArrayList<Integer>();
		List<Double> ar = new ArrayList<Double>();
		for (Target t : targetList) {
			ja.add(varMap.get("h" + t.getTargetID()));
			ar.add(1.0);
		}
		addAndSetRow("FirstAttack", BOUNDS_TYPE.FIXED, 1.0, 1.0);
		rowMap.put("FirstAttack", this.getNumRows());
		this.setMatRow(this.getNumRows(), ja, ar);
		ja.clear();
		ar.clear();
		
		for (Target t : targetList) {
			int tID = t.getTargetID();
			ja.add(varMap.get("qProtected" + tID ));
			ar.add(1.0);

		}
		addAndSetRow("SecondAttackProtected", BOUNDS_TYPE.FIXED, 1.0, 1.0);
		rowMap.put("SecondAttackProtected", this.getNumRows());
		this.setMatRow(this.getNumRows(), ja, ar);
		ja.clear();
		ar.clear();
		
		for (Target t : targetList) {
			int tID = t.getTargetID();
			ja.add(varMap.get("qUnProtected" + tID));
			ar.add(1.0);

		}
		addAndSetRow("SecondAttackUnProtected", BOUNDS_TYPE.FIXED, 1.0, 1.0);
		rowMap.put("SecondAttackUnProtected", this.getNumRows());
		this.setMatRow(this.getNumRows(), ja, ar);
		ja.clear();
		ar.clear();
		
		for (Target t : targetList) {
			int tID = t.getTargetID();
			ja.add(varMap.get("qProtected" + tID));
			ar.add(1.0);
			ja.add(varMap.get("h" + tID));
			ar.add(1.0);
			addAndSetRow("AttackProtected", BOUNDS_TYPE.UPPER, -Configuration.MM, 1.0);
			rowMap.put("AttackProtected", this.getNumRows());
			this.setMatRow(this.getNumRows(), ja, ar);
			ja.clear();
			ar.clear();
			
		}
		for (Target t : targetList) {
			int tID = t.getTargetID();
			ja.add(varMap.get("qUnProtected" + tID));
			ar.add(1.0);
			ja.add(varMap.get("h" + tID));
			ar.add(1.0);
			addAndSetRow("AttackUnProtected", BOUNDS_TYPE.UPPER, -Configuration.MM, 1.0);
			rowMap.put("AttackUnProtected", this.getNumRows());
			this.setMatRow(this.getNumRows(), ja, ar);
			ja.clear();
			ar.clear();
			
		}
	}
	protected void setDefResourceAllocationConstraint() {
		List<Integer> ja = new ArrayList<Integer>();
		List<Double> ar = new ArrayList<Double>();
		for (Target t : targetList) {
			ja.add(varMap.get("x" + t.getTargetID()));
			ar.add(1.0);
		}
		addAndSetRow("DefResSingle", BOUNDS_TYPE.FIXED, this.nDefRes, this.nDefRes);
		rowMap.put("DefResSingle", this.getNumRows());
		this.setMatRow(this.getNumRows(), ja, ar);
		ja.clear();
		ar.clear();
		
		for (Target t : targetList) {
			int tID = t.getTargetID();
			for(Target tprime : targetList) {
				int tprimeID = tprime.getTargetID();
				if (tprimeID > tID) {
					ja.add(varMap.get("x" + tID + "_" + tprimeID));
					ar.add(1.0);
					
				} else if (tprimeID < tID) {
					ja.add(varMap.get("x" + tprimeID + "_" + tID));
					ar.add(1.0);
				}
			}
			ja.add(varMap.get("x" + tID));
			ar.add(-(nDefRes - 1) * 1.0);
			addAndSetRow("DefResPair", BOUNDS_TYPE.FIXED, 0.0, 0.0);
			rowMap.put("DefResPair", this.getNumRows());
			this.setMatRow(this.getNumRows(), ja, ar);
			ja.clear();
			ar.clear();
		}
		
		for(Target t : targetList) {
			int tID = t.getTargetID();
			for(Target tprime : targetList) {
				int tprimeID = tprime.getTargetID();
				//for x_{-i,j} variables, x_{-i,j} != x_{-j,i}. Hence, this if condition is not needed
				//if (tprimeID > tID) {
				ja.add(varMap.get("x-" + tID + "_" + tprimeID));
				ar.add(1.0);
			}
					
			ja.add(varMap.get("x" + tID));
			ar.add((nDefRes - 1) * 1.0);
					
			addAndSetRow("DefResPairNegVar" + tID, BOUNDS_TYPE.FIXED, nDefRes - 1 , nDefRes - 1);
			rowMap.put("DefResPairNegVar" + tID, this.getNumRows());
			this.setMatRow(this.getNumRows(), ja, ar);
			ja.clear();
			ar.clear();
		}
		
		for(Target t : targetList) {
			int tID = t.getTargetID();
			for(Target tprime : targetList) {
				int tprimeID = tprime.getTargetID();
				if (tprimeID > tID) {
					ja.add(varMap.get("x" + tID + "_" + tprimeID));
					ar.add(1.0);
					
					ja.add(varMap.get("x" + tprimeID));
					ar.add(-1.0);
					
					addAndSetRow("DefResPairUB2" + tID + "_" + tprimeID, BOUNDS_TYPE.UPPER, -Configuration.MM, 0.0);
					rowMap.put("DefResPairUB2" + tID + "_" + tprimeID, this.getNumRows());
					this.setMatRow(this.getNumRows(), ja, ar);
					ja.clear();
					ar.clear();
				}
			}
		}
	}
	@Override
	protected void setRowBounds() {
		// TODO Auto-generated method stub
		setDefUConstraint();
		setAttUConstraint();
		setAttackedTargetConstraint();
		setDefResourceAllocationConstraint();
	}
	
	public double getDefenderCoverage(int targetID) {
		return this.getColumnPrimal(varMap.get("x" + targetID));
	}
	public double getDefenderCoverage(Target t) {
		return this.getColumnPrimal(varMap.get("x" + t.getTargetID()));
	}
	public double getDefenderCoveragePair(int tID, int tprimeID) {
		if (tID < tprimeID)
			return this.getColumnPrimal(varMap.get("x" + tID + "_" + tprimeID));
		else
			return this.getColumnPrimal(varMap.get("x" + tprimeID + "_" + tID));
	}	
	
	public double getDefenderCoveragePair(Target t, Target tprime) {
		int tID = t.getTargetID();
		int tprimeID = tprime.getTargetID();
		if (tID < tprimeID)
			return this.getColumnPrimal(varMap.get("x" + tID + "_" + tprimeID));
		else
			return this.getColumnPrimal(varMap.get("x" + tprimeID + "_" + tID));
	}
	
	public double getDefU() {
		return this.getColumnPrimal(varMap.get("v"));
		
	}
	
	public double getAttU() {
		return this.getColumnPrimal(varMap.get("r"));
		
	}
	
	public Map<Set<Target>, Double> getMixedStrategy() {
		Set<Target> targetSet = new HashSet<Target>(this.targetList);
		HashMap<Set<Target>, Double> pairCovProbs = new HashMap<Set<Target>, Double>();
		for(Target t : targetList) {
			int tID = t.getTargetID();
			for(Target tprime : targetList) {
				int tprimeID = tprime.getTargetID();
				if (tprimeID > tID) {
					Set<Target> pair = new HashSet<Target>();
					pair.add(t);
					pair.add(tprime);
					pairCovProbs.put(pair, getDefenderCoveragePair(tID, tprimeID));
				}
			}
		}
		
		MixedStrategyConversion mixedStrategyConvergion = new MixedStrategyConversion(targetSet, nDefRes, pairCovProbs);
		mixedStrategyConvergion.loadProblem();
		try {
			mixedStrategyConvergion.solve();
		} catch (LPSolverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mixedStrategyConvergion.writeProb("TestMixedStrategyConversion");
		System.out.println("Distance: " + mixedStrategyConvergion.getDistance());
		Map<Set<Target>, Double> mixedStrategy = mixedStrategyConvergion.getMixedStrategy();
		mixedStrategyConvergion.end();
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
