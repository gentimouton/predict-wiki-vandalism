package uci.pan.bagging;
import org.apache.hadoop.mapreduce.Mapper.Context;

import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.instance.SMOTE;

public class SmoteBagging extends BalancedBagging {

	public SmoteBagging(@SuppressWarnings("rawtypes") Context c) {
		super(c);		
	}

	private static final long serialVersionUID = 1L;

	public int smoteK;
	public int smoteRate;

	public int getSmoteK() {
		return smoteK;
	}

	public void setSmoteK(int smoteK) {
		this.smoteK = smoteK;
	}

	public int getSmoteRate() {
		return smoteRate;
	}

	public void setSmoteRate(int smoteRate) {
		this.smoteRate = smoteRate;
	}

	// make a bag and apply SMOTE on it
	protected synchronized Instances getTrainingSet(int iteration)
	throws Exception {
		//make a bag with replacement
		Instances bagData = m_data.resampleWithWeights(m_random);

		SMOTE s = new SMOTE();
		try {
			s.setRandomSeed(iteration);//otherwise, all bags have the same smoted set
			s.setClassValue("last");
			s.setInputFormat(bagData);
			s.setNearestNeighbors(this.smoteK);
			s.setPercentage(this.smoteRate);
			Instances smotedBag = Filter.useFilter(bagData, s);
			//System.out.println("Successfully SMOTEd("+this.smoteK+"NN, "+this.smoteRate+"%).");
			//System.out.println("Number of positives in bag: " + this.getDataFromClass(smotedBag, 1).numInstances());
			return smotedBag;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}


}

