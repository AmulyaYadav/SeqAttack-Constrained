package mains;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

import algorithms.SSE;
import models.PayoffStructure;
import models.Target;

public class testSSE {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			lpWrappers.Configuration.loadLibrariesCplex();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		int numTargets = 8;
		int cov = 0;
		int numRes = 7;
		int numPayoffs = 2;
		int payoffIndex = 0;

		
		for(numTargets = 8; numTargets <= 8; numTargets += 20) // number of targets
		{
			for(cov = 0; cov <= 0; cov += 2)
			{
				System.out.println("Covariance: " + cov);
				Vector<int[]> payoffs = loadData(numTargets, cov);
				
				for(payoffIndex = 0; payoffIndex < 1; payoffIndex++)
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
					SSE sse = new SSE(targetList, numRes);
					Map<Target, Double> optDefStrategy = sse.origami();
					double optDefU = sse.getOptDefU(optDefStrategy);
					double optAttU = sse.getOptAttU(optDefStrategy);
					
					// Printing out solution
					for(Target t : targetList) {
						System.out.print(t.getPayoffStructure().getDefenderReward() + "\t");
					}
					System.out.println();
					for(Target t : targetList) {
						System.out.print(t.getPayoffStructure().getDefenderPenalty() + "\t");
					}
					System.out.println();
					for(Target t : targetList) {
						System.out.print(t.getPayoffStructure().getAdversaryReward() + "\t");
					}
					System.out.println();
					for(Target t : targetList) {
						System.out.print(t.getPayoffStructure().getAdversaryPenalty() + "\t");
					}
					System.out.println();
					
					for(Target t : targetList) {
						System.out.print(optDefStrategy.get(t) + "\t");
					}
					System.out.println();
					
					System.out.println("Opt Def U: " + optDefU + "\t opt Att U: " + optAttU);
				}
			}
		}
	}
	public static Vector<int[]> loadData(int numTargets, int cov)
	{
		String payoffFile;
		if (cov < 10)
			payoffFile = "/Users/thanhnguyen/Documents/WORKS/SEQUENTIAL_ATTACK/PAYOFF/" + numTargets + "Target/inputr-0." + cov + "00000.csv";
		else
			payoffFile = "/Users/thanhnguyen/Documents/WORKS/SEQUENTIAL_ATTACK/PAYOFF/" + numTargets + "Target/inputr-1.000000.csv";
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
