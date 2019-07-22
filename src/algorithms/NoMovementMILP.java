package algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lpWrappers.Configuration;
import lpWrappers.LPSolverException;
import lpWrappers.MIProblem;
import models.Target;


public class NoMovementMILP extends MIProblem{
	List<Target> targetList;
	int nDefRes;
	
	HashMap<String, Integer> varMap; // for MILP, column variables
	HashMap<String, Integer> rowMap;	// for MILP, row constraints

	public NoMovementMILP(List<Target> targetList, int nDefRes) {
		super();
//		this.redirectOutput(null);
		this.targetList = targetList;
		this.nDefRes = nDefRes;
		this.varMap = new HashMap<String, Integer>();
		this.rowMap = new HashMap<String, Integer>();
	}
	protected void setProblemType() {
		// TODO Auto-generated method stub
		this.setProblemName("NoMovementMILP");
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
		addAndSetColumn("vUnProtected", BOUNDS_TYPE.FREE, -Configuration.MM, Configuration.MM, VARIABLE_TYPE.CONTINUOUS, 1);
		varMap.put("vUnProtected", idx);
		idx++;
		addAndSetColumn("vProtected", BOUNDS_TYPE.FREE, -Configuration.MM, Configuration.MM, VARIABLE_TYPE.CONTINUOUS, 1);
		varMap.put("vProtected", idx);
		idx++;
		
		// Attacker objectives
		addAndSetColumn("r", BOUNDS_TYPE.FREE, -Configuration.MM, Configuration.MM, VARIABLE_TYPE.CONTINUOUS, 0);
		varMap.put("r", idx);
		idx++;
		for (Target t : targetList) {
			addAndSetColumn("rUnProtected" + t.getTargetID(), BOUNDS_TYPE.FREE, -Configuration.MM, Configuration.MM, VARIABLE_TYPE.CONTINUOUS, 0);
			varMap.put("rUnProtected" + t.getTargetID(), idx);
			idx++;
		}
		for (Target t : targetList) {
			addAndSetColumn("rProtected" + t.getTargetID(), BOUNDS_TYPE.FREE, -Configuration.MM, Configuration.MM, VARIABLE_TYPE.CONTINUOUS, 0);
			varMap.put("rProtected" + t.getTargetID(), idx);
			idx++;
		}
		
		// Defender coverage probability at each target
		for (Target t : targetList) {
			addAndSetColumn("x" + t.getTargetID(), BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.CONTINUOUS, 0);
			varMap.put("x" + t.getTargetID(), idx);
			idx++;
		}
		
		// Defender coverage probability at each pair of target
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
		
		// Binary variables to indicate attacked targets
		for (Target t : targetList) {
			addAndSetColumn("h" + t.getTargetID(), BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.INTEGER, 0);
			varMap.put("h" + t.getTargetID(), idx);
			idx++;
		}
		
		for (Target t : targetList) {
			int tID = t.getTargetID();
			for (Target tprime : targetList) {
				int tprimeID = tprime.getTargetID();
				if (tID != tprimeID) {
					addAndSetColumn("qProtected" + tID + "_" + tprimeID, BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.INTEGER, 0);
					varMap.put("qProtected" + tID + "_" + tprimeID, idx);
					idx++;
				}
			}
		}
		
		for (Target t : targetList) {
			int tID = t.getTargetID();
			for (Target tprime : targetList) {
				int tprimeID = tprime.getTargetID();
				if (tID != tprimeID) {
					addAndSetColumn("qUnProtected" + tID + "_" + tprimeID, BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.INTEGER, 0);
					varMap.put("qUnProtected" + tID + "_" + tprimeID, idx);
					idx++;
				}
			}
		}
	}
	
	protected void setDefUConstraint() {
		List<Integer> ja = new ArrayList<Integer>();
		List<Double> ar = new ArrayList<Double>();
		for (Target t : targetList) {
			double defReward = t.getPayoffStructure().getDefenderReward();
			double defPenalty = t.getPayoffStructure().getDefenderPenalty();
			
			ja.add(varMap.get("x" + t.getTargetID()));
			ar.add(defReward - defPenalty);
			ja.add(varMap.get("v"));
			ar.add(-1.0);
			ja.add(varMap.get("h" + t.getTargetID()));
			ar.add(-Configuration.MM * 1.0);
			
			
			addAndSetRow("defUConstr" + t.getTargetID(), BOUNDS_TYPE.LOWER, -Configuration.MM - defPenalty, Configuration.MM);
			rowMap.put("defUConstr" + t.getTargetID(), this.getNumRows());
			this.setMatRow(this.getNumRows(), ja, ar);
			ja.clear();
			ar.clear();
		}
		
		// Protected
		for (Target t : targetList) {
			int tID = t.getTargetID();
			for(Target tprime : targetList) {
				int tprimeID = tprime.getTargetID();
				if (tprimeID != tID) {
					double defReward = tprime.getPayoffStructure().getDefenderReward();
					double defPenalty = tprime.getPayoffStructure().getDefenderPenalty();
					if (tprimeID > tID) {
						ja.add(varMap.get("x" + tID + "_" + tprimeID));
					}
					else {
						ja.add(varMap.get("x" + tprimeID + "_" + tID));
					}
					ar.add(defReward - defPenalty);
					
					ja.add(varMap.get("x" + tID));
					ar.add(defPenalty);
					
					ja.add(varMap.get("h" + tID));
					ar.add(-Configuration.MM * 1.0);
					
					ja.add(varMap.get("qProtected" + tID + "_" + tprimeID));
					ar.add(-Configuration.MM * 1.0);
					
					
					ja.add(varMap.get("vProtected" ));
					ar.add(-1.0);
					
					
					addAndSetRow("defUProtectedConstr" + tID + "_" + tprimeID, BOUNDS_TYPE.LOWER, -2 * Configuration.MM, Configuration.MM);
					rowMap.put("defUProtectedConstr" + tID + "_" + tprimeID, this.getNumRows());
					this.setMatRow(this.getNumRows(), ja, ar);
					ja.clear();
					ar.clear();
				}
			}
		}
		
		// Unprotected
		for (Target t : targetList) {
			int tID = t.getTargetID();
			for(Target tprime : targetList) {
				int tprimeID = tprime.getTargetID();
				if (tprimeID != tID) {
					double defReward = tprime.getPayoffStructure().getDefenderReward();
					double defPenalty = tprime.getPayoffStructure().getDefenderPenalty();
					if (tprimeID > tID) {
						ja.add(varMap.get("x" + tID + "_" + tprimeID));
					}
					else {
						ja.add(varMap.get("x" + tprimeID + "_" + tID));
					}
					ar.add(-(defReward - defPenalty));
					
					ja.add(varMap.get("x" + tprimeID));
					ar.add(defReward - defPenalty);
					
					ja.add(varMap.get("x" + tID));
					ar.add(-defPenalty);
					
					ja.add(varMap.get("h" + tID));
					ar.add(-Configuration.MM * 1.0);
					
					ja.add(varMap.get("qUnProtected" + tID + "_" + tprimeID));
					ar.add(-Configuration.MM * 1.0);
					
					
					ja.add(varMap.get("vUnProtected" ));
					ar.add(-1.0);
					
					
					addAndSetRow("defUUnProtectedConstr" + tID + "_" + tprimeID, BOUNDS_TYPE.LOWER, -2 * Configuration.MM - defPenalty, Configuration.MM);
					rowMap.put("defUUnProtectedConstr" + tID + "_" + tprimeID, this.getNumRows());
					this.setMatRow(this.getNumRows(), ja, ar);
					ja.clear();
					ar.clear();
				}
			}
		}
	}
	
	protected void setAttUConstraint() {
		List<Integer> ja = new ArrayList<Integer>();
		List<Double> ar = new ArrayList<Double>();
		for (Target t : targetList) {
			double attReward = t.getPayoffStructure().getAdversaryReward();
			double attPenalty = t.getPayoffStructure().getAdversaryPenalty();
			
			ja.add(varMap.get("x" + t.getTargetID()));
			ar.add(attPenalty - attReward);
			
			ja.add(varMap.get("r"));
			ar.add(-1.0);
			
			ja.add(varMap.get("rProtected" + t.getTargetID()));
			ar.add(1.0);
			
			ja.add(varMap.get("rUnProtected" + t.getTargetID()));
			ar.add(1.0);
			
			addAndSetRow("attUConstrLB" + t.getTargetID(), BOUNDS_TYPE.UPPER, -Configuration.MM, -attReward);
			rowMap.put("attUConstrLB" + t.getTargetID(), this.getNumRows());
			this.setMatRow(this.getNumRows(), ja, ar);
			ja.clear();
			ar.clear();
		}
		
		for (Target t : targetList) {
			double attReward = t.getPayoffStructure().getAdversaryReward();
			double attPenalty = t.getPayoffStructure().getAdversaryPenalty();
			
			ja.add(varMap.get("x" + t.getTargetID()));
			ar.add(attPenalty - attReward);
			
			ja.add(varMap.get("r"));
			ar.add(-1.0);
			
			ja.add(varMap.get("rProtected" + t.getTargetID()));
			ar.add(1.0);
			
			ja.add(varMap.get("rUnProtected" + t.getTargetID()));
			ar.add(1.0);
			
			ja.add(varMap.get("h" + t.getTargetID()));
			ar.add(-Configuration.MM * 1.0);
			
			addAndSetRow("attUConstrUB" + t.getTargetID(), BOUNDS_TYPE.LOWER, -Configuration.MM - attReward, Configuration.MM);
			rowMap.put("attUConstrUB" + t.getTargetID(), this.getNumRows());
			this.setMatRow(this.getNumRows(), ja, ar);
			ja.clear();
			ar.clear();
		}
		
		// Protected
		for (Target t : targetList) {
			int tID = t.getTargetID();
			for(Target tprime : targetList) {
				int tprimeID = tprime.getTargetID();
				if (tprimeID != tID) {
					double attReward = tprime.getPayoffStructure().getAdversaryReward();
					double attPenalty = tprime.getPayoffStructure().getAdversaryPenalty();
					ja.add(varMap.get("rProtected" + tID));
					ar.add(-1.0);
					
					if (tID < tprimeID) {
						ja.add(varMap.get("x" + tID + "_" + tprimeID));
					}
					else {
						ja.add(varMap.get("x" + tprimeID + "_" + tID));
					}
					ar.add(attPenalty - attReward);
					
					ja.add(varMap.get("x" + tID));
					ar.add(attReward);
					
					addAndSetRow("attUProtectedConstrLB" + tID + "_" + tprimeID, BOUNDS_TYPE.UPPER, -Configuration.MM, 0);
					rowMap.put("attUProtectedConstrLB" + tID + "_" + tprimeID, this.getNumRows());
					this.setMatRow(this.getNumRows(), ja, ar);
					ja.clear();
					ar.clear();
				}
			}
		}
		
		for (Target t : targetList) {
			int tID = t.getTargetID();
			for(Target tprime : targetList) {
				int tprimeID = tprime.getTargetID();
				if (tprimeID != tID) {
					double attReward = tprime.getPayoffStructure().getAdversaryReward();
					double attPenalty = tprime.getPayoffStructure().getAdversaryPenalty();
					ja.add(varMap.get("rProtected" + tID));
					ar.add(-1.0);
					
					if (tID < tprimeID) {
						ja.add(varMap.get("x" + tID + "_" + tprimeID));
					}
					else {
						ja.add(varMap.get("x" + tprimeID + "_" + tID));
					}
					ar.add(attPenalty - attReward);
					
					ja.add(varMap.get("x" + tID));
					ar.add(attReward);
					
					ja.add(varMap.get("qProtected" + tID + "_" + tprimeID));
					ar.add(-Configuration.MM * 1.0);
					
					addAndSetRow("attUProtectedConstrUB" + tID + "_" + tprimeID, BOUNDS_TYPE.LOWER, -Configuration.MM, Configuration.MM);
					rowMap.put("attUProtectedConstrUB" + tID + "_" + tprimeID, this.getNumRows());
					this.setMatRow(this.getNumRows(), ja, ar);
					ja.clear();
					ar.clear();
				}
			}
		}
		
		// Unprotected
		for (Target t : targetList) {
			int tID = t.getTargetID();
			for(Target tprime : targetList) {
				int tprimeID = tprime.getTargetID();
				if (tprimeID != tID) {
					double attReward = tprime.getPayoffStructure().getAdversaryReward();
					double attPenalty = tprime.getPayoffStructure().getAdversaryPenalty();
					ja.add(varMap.get("rUnProtected" + tID));
					ar.add(-1.0);
					
					if (tID < tprimeID) {
						ja.add(varMap.get("x" + tID + "_" + tprimeID));
					}
					else {
						ja.add(varMap.get("x" + tprimeID + "_" + tID));
					}
					ar.add(-attPenalty + attReward);
					
					ja.add(varMap.get("x" + tprimeID));
					ar.add(attPenalty - attReward);
					
					ja.add(varMap.get("x" + tID));
					ar.add(-attReward);
					
					addAndSetRow("attUUnProtectedConstrLB" + tID + "_" + tprimeID, BOUNDS_TYPE.UPPER, -Configuration.MM, -attReward);
					rowMap.put("attUUnProtectedConstrLB" + tID + "_" + tprimeID, this.getNumRows());
					this.setMatRow(this.getNumRows(), ja, ar);
					ja.clear();
					ar.clear();
				}
			}
		}
		
		for (Target t : targetList) {
			int tID = t.getTargetID();
			for(Target tprime : targetList) {
				int tprimeID = tprime.getTargetID();
				if (tprimeID != tID) {
					double attReward = tprime.getPayoffStructure().getAdversaryReward();
					double attPenalty = tprime.getPayoffStructure().getAdversaryPenalty();
					ja.add(varMap.get("rUnProtected" + tID));
					ar.add(-1.0);
					
					if (tID < tprimeID) {
						ja.add(varMap.get("x" + tID + "_" + tprimeID));
					}
					else {
						ja.add(varMap.get("x" + tprimeID + "_" + tID));
					}
					ar.add(-attPenalty + attReward);
					
					ja.add(varMap.get("x" + tprimeID));
					ar.add(attPenalty - attReward);
					
					ja.add(varMap.get("x" + tID));
					ar.add(-attReward);
					
					ja.add(varMap.get("qUnProtected" + tID + "_" + tprimeID));
					ar.add(-1.0 * Configuration.MM);
					
					addAndSetRow("attUUnProtectedConstrUB" + tID + "_" + tprimeID, BOUNDS_TYPE.LOWER, -Configuration.MM - attReward, Configuration.MM);
					rowMap.put("attUUnProtectedConstrUB" + tID + "_" + tprimeID, this.getNumRows());
					this.setMatRow(this.getNumRows(), ja, ar);
					ja.clear();
					ar.clear();
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
			for (Target tprime : targetList) {
				int tprimeID = tprime.getTargetID();
				if (tprimeID != tID) {
					ja.add(varMap.get("qProtected" + tID + "_" + tprimeID));
					ar.add(1.0);
				}
			}
			addAndSetRow("SecondAttackProtected" + tID, BOUNDS_TYPE.FIXED, 1.0, 1.0);
			rowMap.put("SecondAttackProtected" + tID, this.getNumRows());
			this.setMatRow(this.getNumRows(), ja, ar);
			ja.clear();
			ar.clear();
		}
		
		for (Target t : targetList) {
			int tID = t.getTargetID();
			for (Target tprime : targetList) {
				int tprimeID = tprime.getTargetID();
				if (tprimeID != tID) {
					ja.add(varMap.get("qUnProtected" + tID + "_" + tprimeID));
					ar.add(1.0);
				}
			}
			addAndSetRow("SecondAttackUnProtected" + tID, BOUNDS_TYPE.FIXED, 1.0, 1.0);
			rowMap.put("SecondAttackUnProtected" + tID, this.getNumRows());
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
				if (tprimeID > tID) {
					ja.add(varMap.get("x" + tID + "_" + tprimeID));
					ar.add(1.0);
					
					ja.add(varMap.get("x" + tID));
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
		double v = this.getColumnPrimal(varMap.get("v"));
		double vProtected = this.getColumnPrimal(varMap.get("vProtected"));
		double vUnProtected = this.getColumnPrimal(varMap.get("vUnProtected"));
		return v + vProtected + vUnProtected;
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
