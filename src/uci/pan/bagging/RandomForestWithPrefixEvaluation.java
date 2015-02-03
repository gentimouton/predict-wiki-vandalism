package uci.pan.bagging;

import org.apache.hadoop.mapreduce.Mapper.Context;

import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.RandomTree;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

public class RandomForestWithPrefixEvaluation extends RandomForest {

	private static final long serialVersionUID = 1L;
	protected int bagSizePercent = 100; 
	// default: bag is same size as fold
	protected int bagImbaRatePercent = 50; 
	// default: 50 enforce perfect balance in bags
	
	
	public void buildClassifier(Instances data, @SuppressWarnings("rawtypes") Context context) throws Exception {
        // can classifier handle the data?
        getCapabilities().testWithFail(data);
        // remove instances with missing class
        data = new Instances(data);
        data.deleteWithMissingClass();

        m_bagger = new BaggingWithPrefixEvaluation(context);
        //((BaggingWithPrefixEvaluation) m_bagger).setContext(context); //for mapred
        	
        
        //if we compare forests to each other, this should be done to be fair
        m_bagger.setBagSizePercent(bagSizePercent); 

        RandomTree rTree = new RandomTree();

        // set up the random tree options
        m_KValue = m_numFeatures;
        if (m_KValue < 1) m_KValue = (int) Utils.log2(data.numAttributes())+1;
        rTree.setKValue(m_KValue);
        rTree.setMaxDepth(getMaxDepth());

        // set up the bagger and build the forest
        m_bagger.setClassifier(rTree);
        m_bagger.setSeed(m_randomSeed);
        m_bagger.setNumIterations(m_numTrees);
        m_bagger.setCalcOutOfBag(false);
        m_bagger.buildClassifier(data);
}


	// prefix of length K <=> look at K first trees
	public void setBaggerPrefix(int prefixK) {
		((BaggingWithPrefixEvaluation) m_bagger).setPrefix(prefixK);
	}

	public double classify(Instance instance, int treeIndex) throws Exception {
		return ((BaggingWithPrefixEvaluation) m_bagger).classify(instance, treeIndex);
	}

	public void setBagSizePercent(int sizePercent) {
		bagSizePercent = sizePercent;
	}

	public void setBagImbaRate(int ratePercent) {
		bagImbaRatePercent = ratePercent;
	}
}
