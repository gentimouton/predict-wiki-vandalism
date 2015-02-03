package uci.pan.bagging;

import java.util.Random;

import org.apache.hadoop.mapreduce.Mapper.Context;

import weka.core.Instances;

public class RoughlyBalancedBagging extends BalancedBagging {


	public RoughlyBalancedBagging(@SuppressWarnings("rawtypes") Context c) {
		super(c);
		// TODO Auto-generated constructor stub
	}

	private static final long serialVersionUID = 1L;
	 
	
	protected synchronized Instances getTrainingSet(int iteration)
	throws Exception {

		//int bagSize = m_data.numInstances() * m_BagSizePercent / 100;
		
		Instances bagData = resampleNegBinomialWithoutWeights(m_data, m_random);
		
		//bagData.size() can be larger or smaller than m_data 
		
		/*
		if (m_CalcOutOfBag) {
			m_inBag[iteration] = new boolean[m_data.numInstances()];
			bagData = resampleNegBinomialWithoutWeights(m_data, m_random,
					m_inBag[iteration]);
		} else {
			bagData = resampleNegBinomialWithoutWeights(m_data, m_random, null); 
			if (bagSize < m_data.numInstances()) {
				bagData.randomize(m_random);
				Instances newBagData = new Instances(bagData, 0, bagSize);
				bagData = newBagData;
			}
		}
		*/
		return bagData;
	}

	//create a bag based on negative binomial distribution 
	public final Instances resampleNegBinomialWithoutWeights(Instances originalData, Random random) {
		
		Instances posData = getDataFromClass(originalData, 1);
		Instances negData = getDataFromClass(originalData, 0);
		int halfN = (int) Math.round(originalData.size()*getBagSizePercent()/200);
		double bias = (double) imbaRatePercent/100;
		
		int Nneg = (int) Math.round(getValueFromNegBinomialDist(halfN, bias));
		int Npos = (int) Math.round(originalData.size()*getBagSizePercent()/100-Nneg); //enforce Npos+Nneg = data.size()*bagSizeRatio 
		Instances newData = new Instances(originalData, Npos + Nneg);
		
		// add Nneg negative instances to new bag
		int dataToPick; // the index of the datacase to add
		for (int i = 0; i < Nneg; i++) {
			dataToPick = (int) Math.floor(random.nextDouble()*negData.numInstances());
			newData.add(negData.instance(dataToPick));
		}
		// add Npos positives
		for (int i = 0; i < Npos; i++) {
			dataToPick = (int) Math.floor(random.nextDouble()*posData.numInstances());
			newData.add(posData.instance(dataToPick));
		}

		return newData;
	}


	public int getValueFromNegBinomialDist(int n, double q) {
		int m = 0;
		Random r = new Random(); // random number generator
		double p = r.nextDouble(); // get a random proba in [0,1]
		// cdf stores cdf(m)
		double cdf = 1 - regularizedIncompleteBetaFunction(m + 1, n, q); 

		// increase m until cdf(m, n, q) matches p
		while (cdf < p) {
			m++;
			//System.out.println((m+1) + n);
			cdf = 1 - regularizedIncompleteBetaFunction(m + 1, n, q);
		}
		return m;
	}

	// for the relationship between ibeta and regularized ibeta, 
	// see http://en.wikipedia.org/wiki/Regularized_incomplete_beta_function#Incomplete_beta_function
	public double regularizedIncompleteBetaFunction(double a, double b, double x) {
		return (weka.core.Statistics.incompleteBeta(a, b, x) / weka.core.Statistics.incompleteBeta(a, b, 1));
	}



}
