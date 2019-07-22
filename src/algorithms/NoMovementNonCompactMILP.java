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


public class NoMovementNonCompactMILP extends MIProblem{
	List<Target> targetList;
	
	ArrayList<ArrayList<Integer>>combinationList = new ArrayList<ArrayList<Integer>>();
	int nDefRes;
	
	HashMap<String, Integer> varMap; // for MILP, column variables
	HashMap<String, Integer> rowMap;	// for MILP, row constraints

	public NoMovementNonCompactMILP(List<Target> targetList, int nDefRes) {
		super();
//		this.redirectOutput(null);
		this.targetList = targetList;
		this.nDefRes = nDefRes;
		this.varMap = new HashMap<String, Integer>();
		this.rowMap = new HashMap<String, Integer>();
		//int actionInd=0;
		int nTarget=targetList.size();
        ArrayList<Integer> tempvec = new ArrayList<Integer>();
        for (int i=0;i<nTarget;i++)
	    {
	        if (i>nTarget-1-nDefRes)
	            tempvec.add(1);
	        else
	            tempvec.add(0);
	    }
	    boolean end=false;
	    while(!end)
	    {
	        ArrayList<Integer> newvec = new ArrayList<Integer>();
	        for (int i=0;i<tempvec.size();i++)
	            newvec.add(tempvec.get(i));
	        combinationList.add(newvec);
	        //actionInd++;
	        ////generate next binary string with k bits set
	        boolean nextPresent=false;
	        for (int i=tempvec.size()-1;i>=1;i--)
	        {
	            if (tempvec.get(i)==1&&tempvec.get(i-1)==0)
	            {
	                nextPresent=true;
	                tempvec.set(i,0);
	                tempvec.set(i-1, 1);
	                int numOnesAfterPoint = 0;
	                for (int j=i+1;j<tempvec.size();j++)
	                    if (tempvec.get(j)==1)
	                        numOnesAfterPoint++;
	                
	                for (int j=tempvec.size()-1;j>=i+1;j--)
	                {
	                    if (numOnesAfterPoint>0)
	                    {
	                        tempvec.set(j,1);
	                        numOnesAfterPoint--;
	                    }
	                    else
	                        tempvec.set(j, 0);
	                }
	                break;
	            }
	            else
	                continue;
	        }
	        
	        if (!nextPresent)
	            end=true;
	    }
		System.out.println(combinationList);	
		//System.out.println(targetList);
    }


/*
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


public class NoMovementMILPRevise extends MIProblem{
	List<Target> targetList;
	int nDefRes;
	
	HashMap<String, Integer> varMap; // for MILP, column variables
	HashMap<String, Integer> rowMap;	// for MILP, row constraints

	public NoMovementMILPRevise(List<Target> targetList, int nDefRes) {
		super();
//		this.redirectOutput(null);
		this.targetList = targetList;
		this.nDefRes = nDefRes;
		this.varMap = new HashMap<String, Integer>();
		this.rowMap = new HashMap<String, Integer>();
	}
	
*/	


	protected void setProblemType() {
		// TODO Auto-generated method stub
		this.setProblemName("NoMovementMILP");
		this.setProblemType(PROBLEM_TYPE.MIP, OBJECTIVE_TYPE.MAX);
	}
	@Override
	protected void setColBounds() {
		// TODO Auto-generated method stub
		int idx = 1;
		int nTarget=targetList.size();
		// Defender objectives
		addAndSetColumn("v", BOUNDS_TYPE.FREE, -Configuration.MM, Configuration.MM, VARIABLE_TYPE.CONTINUOUS, 1);
		varMap.put("v", idx);
		idx++;
		
		// Attacker objectives
		addAndSetColumn("r", BOUNDS_TYPE.FREE, -Configuration.MM, Configuration.MM, VARIABLE_TYPE.CONTINUOUS, 0);
		varMap.put("r", idx);
		idx++;
		
		//New Defender coverage probability
		int size=combinationList.size();
		    for (int i=0;i<size;i++){
		       // if (combinationList.get(i).get(j)==1){
		        	addAndSetColumn("x" + i, BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.CONTINUOUS, 0);
					varMap.put("x" +i, idx);
					idx++;   
		       // }		        
		    }		    
		
		
		
		
//		// New Defender coverage probability at each combinationlist
//		for (Target t : targetList) {
//			addAndSetColumn("x" + t.getTargetID(), BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.CONTINUOUS, 0);
//			varMap.put("x" + t.getTargetID(), idx);
//			idx++;
//		}
//		
	/*
		// Defender coverage probability at each target
		for (Target t : targetList) {
			addAndSetColumn("x" + t.getTargetID(), BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.CONTINUOUS, 0);
			varMap.put("x" + t.getTargetID(), idx);
			idx++;
		}
		*/
		    
		    
		// Defender coverage probability at each pair of target
//		for (Target t : targetList) {
//			int tID = t.getTargetID();
//			for (Target tprime : targetList) {
//				int tprimeID = tprime.getTargetID();
//				if (tID < tprimeID) {
//					addAndSetColumn("x" + tID + "_" + tprimeID, BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.CONTINUOUS, 0);
//					varMap.put("x" + tID + "_" + tprimeID, idx);
//					idx++;
//				}
//			}
//		}
//		
		
	
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
		int size=combinationList.size();
		double M = Configuration.MM * 1.0;
		
		for (int u=0;u<size;u++){	
			for (Target i : targetList) {
				int iID = i.getTargetID();
				double iDefReward = i.getPayoffStructure().getDefenderReward();
				double iDefPenalty = i.getPayoffStructure().getDefenderPenalty();
				if (combinationList.get(u).get(iID)==1) {
					ja.add(varMap.get("x" + u));
					ar.add(iDefReward - iDefPenalty);
				}
				ja.add(1);
				ar.add(iDefPenalty);
				
				for (Target j : targetList) {
					int jID = j.getTargetID();
					double jDefReward = j.getPayoffStructure().getDefenderReward();
					double jDefPenalty = j.getPayoffStructure().getDefenderPenalty();
				
					for (Target k : targetList) {
						int kID = k.getTargetID();
						double kDefReward = k.getPayoffStructure().getDefenderReward();
						double kDefPenalty = k.getPayoffStructure().getDefenderPenalty();
						if (jID != iID && kID != iID) {
							if (combinationList.get(u).get(iID)==1 && combinationList.get(u).get(jID)==1 ){
							
								ja.add(varMap.get("x"+ u));
								ar.add(jDefReward - jDefPenalty);
							}
							if (combinationList.get(u).get(iID)==1) {
								ja.add(varMap.get("x" + u));
								ar.add(jDefPenalty);
							}
							if (combinationList.get(u).get(iID)==0 && combinationList.get(u).get(kID)==1 ){
								ja.add(varMap.get("x"+ u));
								ar.add(kDefReward-kDefPenalty);
							}
							if (combinationList.get(u).get(iID)==0) {
								ja.add(varMap.get("x" + u));
								ar.add(kDefPenalty);
							}
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
	}
		
				
//				
//	protected void setDefUConstraint() {
//		List<Integer> ja = new ArrayList<Integer>();
//		List<Double> ar = new ArrayList<Double>();
//		double M = Configuration.MM * 1.0;
//		for(Target i : targetList) {
//			int iID = i.getTargetID();
//			double iDefReward = i.getPayoffStructure().getDefenderReward();
//			double iDefPenalty = i.getPayoffStructure().getDefenderPenalty();
//			for (Target j : targetList) {
//				int jID = j.getTargetID();
//				double jDefReward = j.getPayoffStructure().getDefenderReward();
//				double jDefPenalty = j.getPayoffStructure().getDefenderPenalty();
//				for(Target k : targetList) {
//					int kID = k.getTargetID();
//					double kDefReward = k.getPayoffStructure().getDefenderReward();
//					double kDefPenalty = k.getPayoffStructure().getDefenderPenalty();
//					if (jID != iID && kID != iID) {
//						ja.add(varMap.get("x" + iID));
//						ar.add(iDefReward - iDefPenalty + jDefPenalty - kDefPenalty);
//						
//						if (jID == kID) {
//							ja.add(varMap.get("x" + jID));
//							ar.add(jDefReward - jDefPenalty);
//						}
//						else {
//							if (iID < jID) {
//								ja.add(varMap.get("x" + iID + "_" + jID));
//							}
//							else {
//								ja.add(varMap.get("x" + jID + "_" + iID));
//							}
//							ar.add(jDefReward - jDefPenalty);
//							
//							ja.add(varMap.get("x" + kID));
//							ar.add(kDefReward - kDefPenalty);
//							
//							if(iID < kID) {
//								ja.add(varMap.get("x" + iID + "_" + kID));
//							}
//							else {
//								ja.add(varMap.get("x" + kID + "_" + iID));
//							}
//							ar.add(kDefPenalty - kDefReward);
//						}
//						
//						ja.add(varMap.get("h" + iID));
//						ar.add(-M);
//						ja.add(varMap.get("qProtected" + jID));
//						ar.add(-M);
//						ja.add(varMap.get("qUnProtected" + kID));
//						ar.add(-M);
//						
//						ja.add(varMap.get("v"));
//						ar.add(-1.0);
//						
//						addAndSetRow("defUConstr" + iID + "_" + jID + "_" + kID, BOUNDS_TYPE.LOWER, -iDefPenalty - kDefPenalty - 3 * M, M);
//						rowMap.put("defUConstr" + iID + "_" + jID + "_" + kID, this.getNumRows());
//						this.setMatRow(this.getNumRows(), ja, ar);
//						ja.clear();
//						ar.clear();
//					}
//				}
//			}
//		}
//	}
//	
	
	
	protected void setAttUConstraint() {
		int size=combinationList.size();
		List<Integer> ja = new ArrayList<Integer>();
		List<Double> ar = new ArrayList<Double>();
		double M = Configuration.MM * 1.0;
		// Lower bound
		for (int u=0;u<size;u++){	
			for (Target i : targetList) {
				int iID = i.getTargetID();
				double iAttReward = i.getPayoffStructure().getDefenderReward();
				double iAttPenalty = i.getPayoffStructure().getDefenderPenalty();
				if (combinationList.get(u).get(iID)==1) {
					ja.add(varMap.get("x" + u));
					ar.add( iAttPenalty-iAttReward );
					}
				ja.add(1);
				ar.add(iAttReward);
				
				for (Target j : targetList) {
					int jID = j.getTargetID();
					double jAttReward = j.getPayoffStructure().getDefenderReward();
					double jAttPenalty = j.getPayoffStructure().getDefenderPenalty();
					for (Target k : targetList) {
						int kID = k.getTargetID();
						double kAttReward = k.getPayoffStructure().getDefenderReward();
						double kAttPenalty = k.getPayoffStructure().getDefenderPenalty();
						if (jID != iID && kID != iID) {
							if (combinationList.get(u).get(iID)==1 && combinationList.get(u).get(jID)==1 ){
								
								ja.add(varMap.get("x"+ u));
								ar.add(jAttPenalty-jAttReward);
							}
							if (combinationList.get(u).get(iID)==1) {
								ja.add(varMap.get("x" + u));
								ar.add(jAttReward);
								}
							if (combinationList.get(u).get(iID)==0 && combinationList.get(u).get(kID)==1 ){
								ja.add(varMap.get("x"+ u));
								ar.add(kAttPenalty - kAttReward);
							}
							if (combinationList.get(u).get(iID)==0) {
								ja.add(varMap.get("x" + u));
								ar.add(kAttReward);
							}
							ja.add(varMap.get("r"));
							ar.add(-1.0);
							addAndSetRow("attULBConstr" + iID + "_" + jID + "_" + kID, BOUNDS_TYPE.UPPER, -M, -kAttReward);
							rowMap.put("attULBConstr" + iID + "_" + jID + "_" + kID, this.getNumRows());
							this.setMatRow(this.getNumRows(), ja, ar);
							ja.clear();
							ar.clear();
						}
					}
				}
			}
		}
	
	
		
		
		
		
		
	
	
//	protected void setAttUConstraint() {
//		List<Integer> ja = new ArrayList<Integer>();
//		List<Double> ar = new ArrayList<Double>();
//		double M = Configuration.MM * 1.0;
//		// Lower bound
//		for(Target i : targetList) {
//			int iID = i.getTargetID();
//			double iAttReward = i.getPayoffStructure().getAdversaryReward();
//			double iAttPenalty = i.getPayoffStructure().getAdversaryPenalty();
//			for(Target j : targetList) {
//				int jID = j.getTargetID();
//				double jAttReward = j.getPayoffStructure().getAdversaryReward();
//				double jAttPenalty = j.getPayoffStructure().getAdversaryPenalty();
//				for(Target k : targetList) {
//					int kID = k.getTargetID();
//					double kAttReward = k.getPayoffStructure().getAdversaryReward();
//					double kAttPenalty = k.getPayoffStructure().getAdversaryPenalty();
//					if (iID != jID && iID != kID) {
//						ja.add(varMap.get("x" + iID));
//						ar.add(iAttPenalty - iAttReward + jAttReward - kAttReward);
//						if(jID == kID) {
//							ja.add(varMap.get("x" + jID));
//							ar.add(jAttPenalty - jAttReward);
//						} 
//						else {
//							if (iID < jID) {
//								ja.add(varMap.get("x" + iID + "_" + jID));
//							}
//							else {
//								ja.add(varMap.get("x" + jID + "_" + iID));
//							}
//							ar.add(jAttPenalty - jAttReward);
//							
//							if (iID < kID) {
//								ja.add(varMap.get("x" + iID + "_" + kID));
//							}
//							else {
//								ja.add(varMap.get("x" + kID + "_" + iID));
//							}
//							ar.add(kAttReward - kAttPenalty);
//							
//							ja.add(varMap.get("x" + kID));
//							ar.add(kAttPenalty - kAttReward);
//						}
//						
//						ja.add(varMap.get("r"));
//						ar.add(-1.0);
//						addAndSetRow("attULBConstr" + iID + "_" + jID + "_" + kID, BOUNDS_TYPE.UPPER, -M, -kAttReward);
//						rowMap.put("attULBConstr" + iID + "_" + jID + "_" + kID, this.getNumRows());
//						this.setMatRow(this.getNumRows(), ja, ar);
//						ja.clear();
//						ar.clear();
//					}
//				}
//			}
//		}
		
//		
		//upperbound
		for (int u=0;u<size;u++){
			for (Target i : targetList) {
				int iID = i.getTargetID();
				double iAttReward = i.getPayoffStructure().getDefenderReward();
				double iAttPenalty = i.getPayoffStructure().getDefenderPenalty();
				if (combinationList.get(u).get(iID)==1) {
					ja.add(varMap.get("x" + u));
					ar.add( iAttPenalty-iAttReward );
					}
				ja.add(1);
				ar.add(iAttReward);
				
				for (Target j : targetList) {
					int jID = j.getTargetID();
					double jAttReward = j.getPayoffStructure().getDefenderReward();
					double jAttPenalty = j.getPayoffStructure().getDefenderPenalty();
					for (Target k : targetList) {
						int kID = k.getTargetID();
						double kAttReward = k.getPayoffStructure().getDefenderReward();
						double kAttPenalty = k.getPayoffStructure().getDefenderPenalty();
						if (jID != iID && kID != iID) {
							if (combinationList.get(u).get(iID)==1 && combinationList.get(u).get(jID)==1 ){
								
								ja.add(varMap.get("x"+ u));
								ar.add(jAttPenalty-jAttReward);
							}
							if (combinationList.get(u).get(iID)==1) {
								ja.add(varMap.get("x" + u));
								ar.add(jAttReward);
								}
							if (combinationList.get(u).get(iID)==0 && combinationList.get(u).get(kID)==1 ){
								ja.add(varMap.get("x"+ u));
								ar.add(kAttPenalty - kAttReward);
							}
							if (combinationList.get(u).get(iID)==0) {
								ja.add(varMap.get("x" + u));
								ar.add(kAttReward);
							}
							ja.add(varMap.get("h" + iID));
							ar.add(-M);
							ja.add(varMap.get("qProtected" + jID));
							ar.add(-M);
							ja.add(varMap.get("qUnProtected" + kID));
							ar.add(-M);
							
							ja.add(varMap.get("r"));
							ar.add(-1.0);
							addAndSetRow("attUUBConstr" + iID + "_" + jID + "_" + kID, BOUNDS_TYPE.LOWER, -3 * M - kAttReward, M);
							rowMap.put("attUUBConstr" + iID + "_" + jID + "_" + kID, this.getNumRows());
							this.setMatRow(this.getNumRows(), ja, ar);
							ja.clear();
							ar.clear();
						}
					}
				}
			}
		}
	}
		
	
	
	
		
//		// Upper bound
//		
//		for(Target i : targetList) {
//			int iID = i.getTargetID();
//			double iAttReward = i.getPayoffStructure().getAdversaryReward();
//			double iAttPenalty = i.getPayoffStructure().getAdversaryPenalty();
//			for(Target j : targetList) {
//				int jID = j.getTargetID();
//				double jAttReward = j.getPayoffStructure().getAdversaryReward();
//				double jAttPenalty = j.getPayoffStructure().getAdversaryPenalty();
//				for(Target k : targetList) {
//					int kID = k.getTargetID();
//					double kAttReward = k.getPayoffStructure().getAdversaryReward();
//					double kAttPenalty = k.getPayoffStructure().getAdversaryPenalty();
//					if (iID != jID && iID != kID) {
//						ja.add(varMap.get("x" + iID));
//						ar.add(iAttPenalty - iAttReward + jAttReward - kAttReward);
//						if(jID == kID) {
//							ja.add(varMap.get("x" + jID));
//							ar.add(jAttPenalty - jAttReward);
//						} 
//						else {
//							if (iID < jID) {
//								ja.add(varMap.get("x" + iID + "_" + jID));
//							}
//							else {
//								ja.add(varMap.get("x" + jID + "_" + iID));
//							}
//							ar.add(jAttPenalty - jAttReward);
//							
//							if (iID < kID) {
//								ja.add(varMap.get("x" + iID + "_" + kID));
//							}
//							else {
//								ja.add(varMap.get("x" + kID + "_" + iID));
//							}
//							ar.add(kAttReward - kAttPenalty);
//							
//							ja.add(varMap.get("x" + kID));
//							ar.add(kAttPenalty - kAttReward);
//						}
//						
//						ja.add(varMap.get("h" + iID));
//						ar.add(-M);
//						ja.add(varMap.get("qProtected" + jID));
//						ar.add(-M);
//						ja.add(varMap.get("qUnProtected" + kID));
//						ar.add(-M);
//						
//						ja.add(varMap.get("r"));
//						ar.add(-1.0);
//						addAndSetRow("attUUBConstr" + iID + "_" + jID + "_" + kID, BOUNDS_TYPE.LOWER, -3 * M - kAttReward, M);
//						rowMap.put("attUUBConstr" + iID + "_" + jID + "_" + kID, this.getNumRows());
//						this.setMatRow(this.getNumRows(), ja, ar);
//						ja.clear();
//						ar.clear();
//					}
//				}
//			}
//		}
//	}
//	
	
	
	protected void setAttackedTargetConstraint() {
		int size=combinationList.size();
		List<Integer> ja = new ArrayList<Integer>();
		List<Double> ar = new ArrayList<Double>();
		for  (int u=0;u<size;u++){
			ja.add(varMap.get("x" + u));
			ar.add(1.0);
			}
		addAndSetRow("SumProb", BOUNDS_TYPE.FIXED, 1.0, 1.0);
		rowMap.put("SumProb", this.getNumRows());
		
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
	
	
//	protected void setDefResourceAllocationConstraint() {
//		List<Integer> ja = new ArrayList<Integer>();
//		List<Double> ar = new ArrayList<Double>();
//		for (Target t : targetList) {
//			ja.add(varMap.get("x" + t.getTargetID()));
//			ar.add(1.0);
//		}
//		addAndSetRow("DefResSingle", BOUNDS_TYPE.FIXED, this.nDefRes, this.nDefRes);
//		rowMap.put("DefResSingle", this.getNumRows());
//		this.setMatRow(this.getNumRows(), ja, ar);
//		ja.clear();
//		ar.clear();
//		
//		for (Target t : targetList) {
//			int tID = t.getTargetID();
//			for(Target tprime : targetList) {
//				int tprimeID = tprime.getTargetID();
//				if (tprimeID > tID) {
//					ja.add(varMap.get("x" + tID + "_" + tprimeID));
//					ar.add(1.0);
//					
//				} else if (tprimeID < tID) {
//					ja.add(varMap.get("x" + tprimeID + "_" + tID));
//					ar.add(1.0);
//				}
//			}
//			ja.add(varMap.get("x" + tID));
//			ar.add(-(nDefRes - 1) * 1.0);
//			addAndSetRow("DefResPair", BOUNDS_TYPE.FIXED, 0.0, 0.0);
//			rowMap.put("DefResPair", this.getNumRows());
//			this.setMatRow(this.getNumRows(), ja, ar);
//			ja.clear();
//			ar.clear();
//		}
//		
//		for(Target t : targetList) {
//			int tID = t.getTargetID();
//			for(Target tprime : targetList) {
//				int tprimeID = tprime.getTargetID();
//				if (tprimeID > tID) {
//					ja.add(varMap.get("x" + tID + "_" + tprimeID));
//					ar.add(1.0);
//					
//					ja.add(varMap.get("x" + tID));
//					ar.add(-1.0);
//					
//					addAndSetRow("DefResPairUB1" + tID + "_" + tprimeID, BOUNDS_TYPE.UPPER, -Configuration.MM, 0.0);
//					rowMap.put("DefResPairUB1" + tID + "_" + tprimeID, this.getNumRows());
//					this.setMatRow(this.getNumRows(), ja, ar);
//					ja.clear();
//					ar.clear();
//				}
//			}
//		}
//		
//		for(Target t : targetList) {
//			int tID = t.getTargetID();
//			for(Target tprime : targetList) {
//				int tprimeID = tprime.getTargetID();
//				if (tprimeID > tID) {
//					ja.add(varMap.get("x" + tID + "_" + tprimeID));
//					ar.add(1.0);
//					
//					ja.add(varMap.get("x" + tprimeID));
//					ar.add(-1.0);
//					
//					addAndSetRow("DefResPairUB2" + tID + "_" + tprimeID, BOUNDS_TYPE.UPPER, -Configuration.MM, 0.0);
//					rowMap.put("DefResPairUB2" + tID + "_" + tprimeID, this.getNumRows());
//					this.setMatRow(this.getNumRows(), ja, ar);
//					ja.clear();
//					ar.clear();
//				}
//			}
//		}
//	}
	@Override
	protected void setRowBounds() {
		// TODO Auto-generated method stub
		setDefUConstraint();
		setAttUConstraint();
		setAttackedTargetConstraint();
		//setDefResourceAllocationConstraint();
	}
	
	///////TO BE REVISED/////////////////
	
	public double getDefenderCoverage(int targetID) {
		return this.getColumnPrimal(varMap.get("x" + targetID));
	}
/*	public double getDefenderCoverage(Target t) {
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
	}*/
	
	public double getDefU() {
		return this.getColumnPrimal(varMap.get("v"));
		
	}
	
/*	public Map<Set<Target>, Double> getMixedStrategy() {
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
	}*/
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
