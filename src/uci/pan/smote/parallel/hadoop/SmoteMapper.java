package uci.pan.smote.parallel.hadoop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import uci.pan.bagging.RandomForestWithPrefixEvaluation;
import uci.pan.bagging.SmoteRandomForest;
import uci.pan.smote.parallel.input.MyCounters;
import uci.pan.smote.parallel.input.SmoteRunConfig;
import uci.pan.smote.parallel.input.SmoteTreeConfig;
import uci.pan.smote.parallel.util.HadoopUtil;
import uci.pan.utils.PRAUC;
import uci.pan.utils.ROC;
import weka.core.Attribute;
import weka.core.Instances;

public class SmoteMapper extends
		Mapper<NullWritable, SmoteRunConfig, SmoteTreeConfig, Text> {

	@Override
	public void map(NullWritable key, SmoteRunConfig config, final Context context)
			throws IOException, InterruptedException {

		String trainFilePath = config.trainFilePath;
		
		String testFilePath = config.testFilePath;
		
		// int numRuns = value.getNumRuns();
		int smoteK = config.smoteK;
		int smoteRate = config.smoteRate;
		// only log the trees that are powers of 2
		List<Integer> tlist = Arrays.asList(1,2,4,8,16,32,64,128,256,512,1024,2048);
		
		try {
			// get train and test data
			Configuration conf = context.getConfiguration();
			String className = conf.get("smote.class.name");
			int numTrees = conf.getInt("num.trees", 1);
			int numRuns = conf.getInt("num.repeats", 1);
			Instances trainSet = HadoopUtil.getInstancesFromFile(trainFilePath);
			Instances testSet = HadoopUtil.getInstancesFromFile(testFilePath);
			Attribute labelAttr = trainSet.attribute(className);
			trainSet.setClass(labelAttr);
			testSet.setClass(labelAttr);

			
			// how much to vary the decision threshold
			// does not impact AUC, just sensitivity and specificity
			double offset = 0;

			
			// build a smoted forest
			RandomForestWithPrefixEvaluation rf = new SmoteRandomForest();
			rf.setSeed(smoteRate+smoteK); //just in case
			((SmoteRandomForest)rf).setSmoteK(smoteK);
			((SmoteRandomForest)rf).setSmoteRate(smoteRate);
			rf.setNumTrees(numTrees);
			rf.setBagSizePercent(100);
			rf.buildClassifier(trainSet, context);//2nd argument is context for Map Reduce job, ie not serial case like here

			System.out.println("Classifier built. Now testing.");
			// get numpos, numneg and actualLabels
			int numPos = 0, numNeg = 0;
			double[] actualLabels = new double[testSet.numInstances()];
			for (int i = 0; i < testSet.numInstances(); i++) {
				actualLabels[i] = testSet.get(i).classValue();
				if (actualLabels[i] == 1)
					numPos++;
				else
					numNeg++;
			}

			// build an array containing 1 int per tree, to be randomized
			List<Integer> allNumbers = new ArrayList<Integer>(numTrees);
			for (int i = 0; i < numTrees; i++) {
				allNumbers.add(i);
			}
			double pred2[];
			double actual2[];
			double prauc;
			// build numRepeats RFs of size numTrees so that we can get std dev of our model 
			for (int r = 1; r <= numRuns; r++) {
				double[] sumLabels = new double[testSet.numInstances()];
				double[] predLabels = new double[testSet.numInstances()];
				Collections.shuffle(allNumbers);
				// for each tree in the forest
				for (int t = 1; t <= numTrees; t++) {
					// mapred specific
					if(context != null) {
						context.getCounter(MyCounters.MAPPER_TREE_TESTED_COUNTER).increment(1);
						context.progress();
					}
					// classify all instances
					for (int i = 0; i < testSet.numInstances(); i++) {
						sumLabels[i] += rf.classify(testSet.get(i), allNumbers.get(t-1));
						predLabels[i] = offset + sumLabels[i] / t;
					}
					if(tlist.contains(t)) {
						String stats = ROC.GetFastPerfStats(predLabels, actualLabels, predLabels.length, numPos, numNeg);
						pred2 = Arrays.copyOf(predLabels, predLabels.length);
						actual2 = Arrays.copyOf(actualLabels, actualLabels.length);
						prauc = PRAUC.getPrecRecallRoc(pred2, actual2, numPos);
						context.write(new SmoteTreeConfig(smoteK, smoteRate, t), new Text(stats + "," + prauc));
					} //end if
				} //end for k
			} //end for r
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
