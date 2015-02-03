package uci.pan.bagging;
import org.apache.hadoop.mapreduce.Mapper.Context;

import weka.classifiers.trees.RandomTree;
import weka.core.Instances;
import weka.core.Utils;


public class BreimanBalancedRandomForest extends RandomForestWithPrefixEvaluation {
	
	private static final long serialVersionUID = 1L;

	public void buildClassifier(Instances data, @SuppressWarnings("rawtypes") Context context) throws Exception {

		    // can classifier handle the data?
		    getCapabilities().testWithFail(data);

		    // remove instances with missing class
		    data = new Instances(data);
		    data.deleteWithMissingClass();
		    
		    m_bagger = new BreimanBalancedBagging(null);
		    m_bagger.setBagSizePercent(bagSizePercent);
		    ((BalancedBagging) m_bagger).setImbaRatePercent(bagImbaRatePercent);
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

}
