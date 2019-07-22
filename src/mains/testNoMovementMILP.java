package mains;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

import algorithms.CuttingPlaneDualSolver;
import algorithms.FreeMovementMILP;
import algorithms.NoMovementMILPRevise;
import algorithms.SSEMultiAttack;
import lpWrappers.AMIProblem.STATUS_TYPE;
import lpWrappers.LPSolverException;
import models.PayoffStructure;
import models.Target;

public class testNoMovementMILP {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			lpWrappers.Configuration.loadLibrariesCplex();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		///server run
		//int numTargets1 = Integer.parseInt(args[0]);
		//int cov1 = Integer.parseInt(args[2]);
		//int numRes1 = Integer.parseInt(args[1]);
		//int numInstances = Integer.parseInt(args[3]);
		
		
		////local run
		int numTargets1 = 40;
		int cov1 = 10;
		int numRes1 = 5;
		int numInstances=3;
		int payoffIndex = 0;
		int nAttack = 2;
		
		try {
			//server run
			//BufferedWriter writer = new BufferedWriter(new FileWriter("/storage/home/a/auy212/work/work/freeMovOutput/"+numTargets1+"T"+numRes1+"R"+cov1+"c.csv"));
			//local run
			BufferedWriter writer = new BufferedWriter(new FileWriter("/Users/auy212/"+numTargets1+"T"+numRes1+"R"+cov1+"c.csv"));


		
		for(int numTargets = numTargets1; numTargets <= numTargets1; numTargets += 10) // number of targets
		{
			for(int numRes = numRes1; numRes <= numRes1; numRes += 5){
				System.out.println("Resources: " + numRes);
			for(int cov = cov1; cov <= cov1; cov += 2)
			{
				System.out.println("Covariance: " + cov);
				Vector<int[]> payoffs = loadData(numTargets, cov);
				
				for(payoffIndex = 0; payoffIndex < numInstances; payoffIndex++)
				{
					int[] payoff = payoffs.get(payoffIndex);
					List<Target> targetList = new ArrayList<Target>();
					for(int id = 0; id < numTargets; id++)
					{	
						double attReward = payoff[id + 2 * numTargets];
						double attPenalty = payoff[id + 3 * numTargets];
						double defReward = payoff[id];
						double defPenalty = payoff[id + numTargets];
						
						PayoffStructure payoffStructure = new PayoffStructure(defReward, defPenalty, attReward, attPenalty);
						Target t = new Target(id + 1, payoffStructure);
						targetList.add(t);
						
					}
					
					long start = System.currentTimeMillis();
					SSEMultiAttack sseMulti = new SSEMultiAttack(targetList, numRes, nAttack);
					sseMulti.loadProblem();
					try {
						sseMulti.solve();
					} catch (LPSolverException e) {
						// TODO Auto-generated catch block
						System.out.println("Simultaneous Attack Fail with Message: "+e.getMessage());
						e.printStackTrace();
					}
					
					long end = System.currentTimeMillis();
					
					double timeSI = (end-start)/1000;
					
					sseMulti.writeProb("./lpsi");
//					Map<Target, Double> optDefStrategy = sseMulti.getOptDefStrategy();
					double optDefUsi = sseMulti.getOptDefU();
//					double optDefUCheck= sseMulti.getOptDefURecompute();
					double optAttUsi = sseMulti.getOptAttU();
					
					start = System.currentTimeMillis();
					
					NoMovementMILPRevise noMovementMILP = new NoMovementMILPRevise(targetList, numRes);
					noMovementMILP.loadProblem();
					
					CuttingPlaneDualSolver cpDualProblem = new CuttingPlaneDualSolver(noMovementMILP, targetList, numRes, 0);
					
					
					int moreCuttingPlanesFound = 1;
					
					int numPlanesFound=-1;///bookkeeping
					
					/*while(moreCuttingPlanesFound==1)
					{*/
						numPlanesFound++;
						try {
							noMovementMILP.solve();
							noMovementMILP.writeProb("TestNoMovementMILP");
						} catch (LPSolverException e) {
							// TODO Auto-generated catch block
							System.out.println("No Movement Fail with Message: "+e.getMessage() + " Status:" + noMovementMILP.getSolveStatus());
							if (noMovementMILP.getSolveStatus()==STATUS_TYPE.INFEASIBLE)
							{
								System.out.println("System infeasible");
							}
							e.printStackTrace();
						}
						
						/*System.out.println("Expected Defender Utility after adding " + numPlanesFound+ " planes: " + noMovementMILP.getLPObjective());
						
						///update objective of cutting plane dual problem
						for (int i=1;i<=numTargets;i++)
						{
							for (int j=i+1;j<=numTargets;j++)
							{
								cpDualProblem.cpProblem.setObjectiveCoef(cpDualProblem.cpProblem.varMap.get("y" + i + "_" + j) , noMovementMILP.getDefenderCoveragePair(i, j));
								cpDualProblem.cpProblem.setObjectiveCoef(cpDualProblem.cpProblem.varMap.get("z" + i + "_" + j) , -1*noMovementMILP.getDefenderCoveragePair(i, j));
							}
						}
						
						///remove all added constraints from past case
						
						moreCuttingPlanesFound = cpDualProblem.solve();*/
					//}
					
					end = System.currentTimeMillis();
					double timeSE = (end-start)/1000;
					
					double optDefUse= noMovementMILP.getDefU();
					double optAttUse= noMovementMILP.getAttU();
					
					
					ArrayList<Double> coverageVector = new ArrayList<Double>();
					for (int i=1;i<=numTargets;i++)
						coverageVector.add(noMovementMILP.getDefenderCoverage(i));
					////for only two attacks
					
					double bestResponseAtt=-10000;
					Target besti=null, bestj=null;
					for (Target i: targetList)
					{
						for (Target j: targetList)
						{
							if (j.getTargetID()>i.getTargetID())
							{
								double attU=0.0;
								double defCov = coverageVector.get(i.getTargetID()-1);
						    	double attReward = i.getPayoffStructure().getAdversaryReward();
						    	double attPen = i.getPayoffStructure().getAdversaryPenalty();
						    	double attEU = defCov * (attPen - attReward) + attReward;
						    	attU += attEU;
						    	
						    	defCov = coverageVector.get(j.getTargetID()-1);
						    	attReward = j.getPayoffStructure().getAdversaryReward();
						    	attPen = j.getPayoffStructure().getAdversaryPenalty();
						    	attEU = defCov * (attPen - attReward) + attReward;
						    	attU += attEU;
						    	
						    	if (attU>bestResponseAtt)
						    	{
						    		bestResponseAtt=attU;
						    		besti=i;
						    		bestj=j;
						    	}
							}
						}
					}
					
					///works only if you have atleast two resources and there are atmost two attacks going on
					double bestResponseDefender = coverageVector.get(besti.getTargetID()-1)*besti.getPayoffStructure().getDefenderReward() + (1-coverageVector.get(besti.getTargetID()-1))*besti.getPayoffStructure().getDefenderPenalty() + coverageVector.get(bestj.getTargetID()-1)*bestj.getPayoffStructure().getDefenderReward() + (1-coverageVector.get(bestj.getTargetID()-1))*bestj.getPayoffStructure().getDefenderPenalty();
					
					
					
					
					// Printing out solution
//					for(Target t : targetList) {
//						System.out.print(t.getPayoffStructure().getDefenderReward() + "\t");
//					}
//					System.out.println();
//					for(Target t : targetList) {
//						System.out.print(t.getPayoffStructure().getDefenderPenalty() + "\t");
//					}
//					System.out.println();
//					for(Target t : targetList) {
//						System.out.print(t.getPayoffStructure().getAdversaryReward() + "\t");
//					}
//					System.out.println();
//					for(Target t : targetList) {
//						System.out.print(t.getPayoffStructure().getAdversaryPenalty() + "\t");
//					}
//					System.out.println();
//					
//					for(Target t : targetList) {
//						System.out.print(optDefStrategy.get(t) + "\t");
//					}
//					System.out.println();
					
//					System.out.println("Opt Def U: " + optDefU + "\t Opt Def U Check: " + optDefUCheck + "\t opt Att U: " + optAttU);
					System.out.println("Opt Def U Seq: " + optDefUse + "\t Opt Att U Seq: " + optAttUse +"\t Time: "+ timeSE);
					System.out.println("Opt Def U Sim: " + optDefUsi + "\t Opt Att U Sim: " + optAttUsi +"\t Time: "+timeSI);
					System.out.println("--------------------------------------------------------------");
					writer.write(optDefUse+"," +optAttUse+","+timeSE+","+optDefUsi+","+optAttUsi+","+timeSI + ", "+bestResponseDefender+", "+bestResponseAtt);
					writer.newLine();
					sseMulti.end();
					noMovementMILP.end();
				}
			}
			System.out.println("**************************************************************");
			}
		}
		writer.close();
		}
		catch(IOException e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static Vector<int[]> loadData(int numTargets, int cov)
	{
		String payoffFile;
		if (cov < 10)
			payoffFile = "/Users/auy212/Downloads/work/PAYOFF/" + numTargets + "Target/inputr-0." + cov + "00000.csv";
			//payoffFile = "/storage/home/a/auy212/work/work/PAYOFF/" + numTargets + "Target/inputr-0." + cov + "00000.csv";
		else
			payoffFile = "/Users/auy212/Downloads/work/PAYOFF/" + numTargets + "Target/inputr-1.000000.csv";
			//payoffFile = "/storage/home/a/auy212/work/work/PAYOFF/" + numTargets + "Target/inputr-1.000000.csv";
		Vector<int[]> payoffs = new Vector<int[]>();
		FileReader fin = null;
		Scanner src = null;
		try {
			fin = new FileReader(payoffFile);
			src = new Scanner(fin);
			while (src.hasNext()) {
				String line = src.nextLine();
				String[] values = line.split(",");
				int[] payoff = new int[4 * numTargets];
				for (int i = 0; i < 4 * numTargets; i++)
					payoff[i] = Integer.parseInt(values[i]);
				payoffs.add(payoff);

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Couldn't open file for reading.");
			e.printStackTrace();
		}
		src.close();
		return payoffs;
	}

}