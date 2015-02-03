package uci.pan.bagging;
import java.util.Random;

import org.apache.hadoop.mapreduce.Mapper.Context;

import weka.core.Instances;

public class BreimanBalancedBagging extends BalancedBagging {

	public BreimanBalancedBagging(@SuppressWarnings("rawtypes") Context c) {
		super(c);
		// TODO Auto-generated constructor stub
	}

	private static final long serialVersionUID = 1L;

	protected synchronized Instances getTrainingSet(int iteration)
			throws Exception {
		Instances bagData = resampleBinomialWithoutWeights(m_data, m_random);
		return bagData;
		
	}

	//this method creates a bag based on negative binomial distribution 
	public final Instances resampleBinomialWithoutWeights(Instances originalData, Random random) {
				
		// get positives and negatives
		Instances posData = getDataFromClass(originalData, 1);
		Instances negData = getDataFromClass(originalData, 0);
		
		Random r = new Random(2L);
		//int nPos = posData.numInstances();
		//int bagSize = 2*nPos;
		int bagSize = (int) Math.round(originalData.size()*getBagSizePercent()/100);
		Instances newData = new Instances(originalData, bagSize);
		
		int dataToPick = 0; //the index of the datacase to add
		double coinResult = 0;
		//for each datacase in newbag, 
		for (int i=0; i<bagSize; i++) {
			//flip a coin
			coinResult = r.nextDouble(); //binary class
			if (coinResult <= ((double) imbaRatePercent/100)) { //if head, add positive datacase
				dataToPick = (int) Math.floor(random.nextDouble()*posData.numInstances());
				newData.add(posData.instance(dataToPick));
			}
			else { //if tails: add negative datacase
				dataToPick = (int) Math.floor(random.nextDouble()*negData.numInstances());
				newData.add(negData.instance(dataToPick));
			}
		}
		return newData;
	}




}
