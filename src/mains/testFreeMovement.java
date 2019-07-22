package mains;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

import algorithms.FreeMovementMILP;
import algorithms.SSE;
import algorithms.SSEMultiAttack;
import lpWrappers.LPSolverException;
import models.PayoffStructure;
import models.Target;

public class testFreeMovement {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			lpWrappers.Configuration.loadLibrariesCplex();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//server run
		int numTargets1 = Integer.parseInt(args[0]);
		int cov1 = Integer.parseInt(args[2]);
		int numRes1 = Integer.parseInt(args[1]);
		
		////local run
		//int numTargets1 = 20;
		//int cov1 = 0;
		//int numRes1 = 5;
		int payoffIndex = 0;
		int nAttack = 2;
		
		try {
		//server run
		BufferedWriter writer = new BufferedWriter(new FileWriter("/storage/home/a/auy212/work/work/freeMovOutput/"+numTargets1+"T"+numRes1+"R"+cov1+"c.csv"));
		//local run
		//BufferedWriter writer = new BufferedWriter(new FileWriter("/Users/auy212/"+numTargets1+"T"+numRes1+"R"+cov1+"c.csv"));
		
		for(int numTargets = numTargets1; numTargets <= numTargets1; numTargets += 10) // number of targets
		{
			for(int numRes = numRes1; numRes <= numRes1; numRes += 5){
				System.out.println("Resources: " + numRes);
			for(int cov = cov1; cov <= cov1; cov += 2)
			{
				System.out.println("Covariance: " + cov);
				Vector<int[]> payoffs = loadData(numTargets, cov);
				
				for(payoffIndex = 0; payoffIndex < 5; payoffIndex++)
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
						e.printStackTrace();
					}
					
					long end = System.currentTimeMillis();
					
					double timeSI = (end-start)/1000;
					
					sseMulti.writeProb("./lpsi");
					double optDefUsi = sseMulti.getOptDefU();
					double optAttUsi = sseMulti.getOptAttU();
					
					start = System.currentTimeMillis();
					
					FreeMovementMILP sseSE = new FreeMovementMILP(targetList, numRes);
					sseSE.loadProblem();
					try {
						sseSE.solve();
					} catch (LPSolverException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					end = System.currentTimeMillis();
					
					double timeSE = (end-start)/1000;
					
					double optDefUse = sseSE.getDefOptU();
					double optAttUse = sseSE.getAttOptU();
					
					
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
					System.out.println("Opt Def U Sim: " + optDefUsi + "\t Opt Att U Sim: " + optAttUsi +"\t Time: "+ timeSI);
					System.out.println("--------------------------------------------------------------");
					writer.write(optDefUse+"," +optAttUse+","+timeSE+","+optDefUsi+","+optAttUsi+","+timeSI);
					writer.newLine();
					sseMulti.end();
					sseSE.end();
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