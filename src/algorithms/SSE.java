package algorithms;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import models.Target;

public class SSE {
	List<Target> targetList;
	int nRes;
	double epsilon = 1e-4;
	public SSE(List<Target> targetList, int nRes) {
		this.targetList = targetList;
		this.nRes = nRes;
	}
	
	public Map<Target, Double> origami() {
		Map<Target, Double> optDefStrategy = new HashMap<Target, Double>();
		int nTarget = this.targetList.size();
		List<Target> sortedTargetList = new ArrayList<Target>(this.targetList);
		Collections.sort(sortedTargetList, Collections.reverseOrder());
		double[] payoff = new double[nTarget];
		double[] coverage = new double[nTarget];
		double[] addedCov = new double[nTarget];
		double[] ratio = new double[nTarget];
		int idx = 0;
		for (Target t : sortedTargetList) {
			payoff[idx] = t.getPayoffStructure().getAdversaryReward();
			coverage[idx] = 0;
			idx++;
		}
		
		double left = (double) this.nRes;
		int next = 1; 
		double covBound = Double.NEGATIVE_INFINITY;
		while(next < nTarget) {
			for (idx = 0; idx < next; idx++) {
				Target t = sortedTargetList.get(idx);
				double attReward = t.getPayoffStructure().getAdversaryReward();
				double attPenalty = t.getPayoffStructure().getAdversaryPenalty();
				addedCov[idx] = (payoff[next] - attReward) / (attPenalty - attReward) - coverage[idx];
				if(coverage[idx] + addedCov[idx] >= 1) {
					covBound = Math.max(covBound, attPenalty);
				}
				coverage[idx] += addedCov[idx];
				left -= addedCov[idx];
			}
			if(covBound != Double.NEGATIVE_INFINITY || left < 0) {
				break;
			}
			next++;
		}
		double sumTemp = 0.0;
		for(idx = 0; idx < next; idx++) {
			Target t = sortedTargetList.get(idx);
			double attReward = t.getPayoffStructure().getAdversaryReward();
			double attPenalty = t.getPayoffStructure().getAdversaryPenalty();
			ratio[idx] = 1 / (attReward - attPenalty);
			sumTemp += ratio[idx];
		}
		for(idx = 0; idx < next; idx++) {
			Target t = sortedTargetList.get(idx);
			double attPenalty = t.getPayoffStructure().getAdversaryPenalty();
			coverage[idx] += (ratio[idx] * left) / sumTemp;
			if(coverage[idx] >= 1) {
				covBound = Math.max(covBound, attPenalty);
			}
		}
		if(covBound != Double.NEGATIVE_INFINITY) {
			for(idx = 0; idx < next; idx++) {
				Target t = sortedTargetList.get(idx);
				double attReward = t.getPayoffStructure().getAdversaryReward();
				double attPenalty = t.getPayoffStructure().getAdversaryPenalty();
				coverage[idx] = (covBound - attReward) / (attPenalty - attReward);
			}
		}
		idx = 0;
		for(Target t : sortedTargetList) {
			optDefStrategy.put(t, coverage[idx]);
			idx++;
		}
		return optDefStrategy;
	}
	public double getOptDefU(Map<Target, Double> defStrategy) {
		double optDefU = Double.NEGATIVE_INFINITY;
		double optAttU = Double.NEGATIVE_INFINITY;
		for(Entry<Target, Double> entry : defStrategy.entrySet()) {
			Target t  = entry.getKey();
			double defCov = entry.getValue();
			double attReward = t.getPayoffStructure().getAdversaryReward();
			double attPen = t.getPayoffStructure().getAdversaryPenalty();
			double attEU = defCov * (attPen - attReward) + attReward;
			if (optAttU < attEU) optAttU = attEU;
		}
		
		for(Entry<Target, Double> entry : defStrategy.entrySet()) {
			Target t  = entry.getKey();
			double defCov = entry.getValue();
			double attReward = t.getPayoffStructure().getAdversaryReward();
			double attPen = t.getPayoffStructure().getAdversaryPenalty();
			double attEU = defCov * (attPen - attReward) + attReward;
			if (attEU > optAttU - this.epsilon) { // this target belongs to attack set
				double defReward = t.getPayoffStructure().getDefenderReward();
				double defPen = t.getPayoffStructure().getDefenderPenalty();
				double defEU = defCov * (defReward - defPen) + defPen;
				if(optDefU < defEU) {
					optDefU = defEU;
				}
			}
		}
		
		return optDefU;
	}
	public double getOptAttU(Map<Target, Double> defStrategy) {
		double optAttU = Double.NEGATIVE_INFINITY;
		for(Entry<Target, Double> entry : defStrategy.entrySet()) {
			Target t  = entry.getKey();
			double defCov = entry.getValue();
			double attReward = t.getPayoffStructure().getAdversaryReward();
			double attPen = t.getPayoffStructure().getAdversaryPenalty();
			double attEU = defCov * (attPen - attReward) + attReward;
			if (optAttU < attEU) optAttU = attEU;
		}
		return optAttU;
	}
}
