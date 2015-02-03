package uci.pan.bagging;

import org.apache.hadoop.mapreduce.Mapper.Context;

import weka.core.Instances;

public class BalancedBagging extends BaggingWithPrefixEvaluation {

	
	public BalancedBagging(@SuppressWarnings("rawtypes") Context c) {
		super(c);		
	}


	private static final long serialVersionUID = 1L;

	
	protected int imbaRatePercent = 50; //default: balance in set
	
	
	public Instances getDataFromClass(Instances data, int label) {
		// create empty set
		Instances resultInstances = new Instances(data, -1);
		for (int i = 0; i <= data.numInstances() - 1; i++) {
			if ((int) data.instance(i).classValue() == label)
				resultInstances.add(data.instance(i)); // size of result increased automatically
		}
		return resultInstances;
	}
	

	public void setImbaRatePercent(int ratePercent) {
		imbaRatePercent = ratePercent;
	}

	
}
