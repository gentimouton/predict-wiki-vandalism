package uci.pan.bagging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import uci.pan.utils.Config;
import uci.pan.utils.IO;
import uci.pan.utils.PRAUC;
import uci.pan.utils.ROC;
import uci.pan.utils.Utils;
import weka.core.Attribute;
import weka.core.Instances;

public class BaggingRunner {

	static String outputFileName = Config.OUTPUT_FILE_PATH + "-"
	+ Config.NUM_TREES + "trees.txt";
	static StringBuilder output;

	// if fold == 0, then it's the full trainset and the testset
	private static void processSet(int foldID) throws Exception {
		String path = Config.BASE_PATH;
		Instances trainSet = Utils.getInstancesFromFile(path + "train_"+ foldID + ".arff");
		Instances testSet = Utils.getInstancesFromFile(path + "valid_"+ foldID + ".arff");

		String className = Config.CLASS_NAME;
		// define which attribute is the label
		Attribute labelAttr = trainSet.attribute(className);
		trainSet.setClass(labelAttr);
		testSet.setClass(labelAttr);
		run(trainSet, testSet, Config.NUM_TREES, foldID);
	}

	private static void run(Instances trainSet, Instances testSet, int numTrees, int foldId) throws Exception {
		// how much to vary the decision threshold
		//does not impact AUC, just sensitivity and specificity
		double offset = 0;

		// normal RF

		RandomForestWithPrefixEvaluation nrf = new RandomForestWithPrefixEvaluation();
		buildAndTestRF(nrf, trainSet, testSet, "NRF", foldId, numTrees, 100,offset);
		nrf = null;

		// bag size = 2*minority size
		int smallBagSizePercent = 200 * getMinoritySize(trainSet)
		/ trainSet.numInstances();
		RandomForestWithPrefixEvaluation rbrfSmall = new RoughlyBalancedRandomForest();
		String rbrfSmallName = "RBRF_SMALL";
		buildAndTestRF(rbrfSmall, trainSet, testSet, rbrfSmallName, foldId,numTrees, smallBagSizePercent, offset);
		rbrfSmall = null;
		RandomForestWithPrefixEvaluation brfSmall = new BreimanBalancedRandomForest();
		String brfSmallName = "BRF_SMALL";
		buildAndTestRF(brfSmall, trainSet, testSet, brfSmallName, foldId,numTrees, smallBagSizePercent, offset);
		brfSmall = null;

		// bag size = train size
		RandomForestWithPrefixEvaluation rbrfSame = new RoughlyBalancedRandomForest();
		String rbrfSameName = "RBRF_SAME";
		buildAndTestRF(rbrfSame, trainSet, testSet, rbrfSameName, foldId,numTrees, 100, offset);
		rbrfSame = null;
		RandomForestWithPrefixEvaluation brfSame = new BreimanBalancedRandomForest();
		String brfSameName = "BRF_SAME";
		buildAndTestRF(brfSame, trainSet, testSet, brfSameName, foldId,numTrees, 100, offset);
		brfSame = null;

		// bag size = 2*train size
		RandomForestWithPrefixEvaluation rbrfDouble = new RoughlyBalancedRandomForest();
		String rbrfDoubleName = "RBRF_DOUBLE";
		buildAndTestRF(rbrfDouble, trainSet, testSet, rbrfDoubleName, foldId,numTrees, 200, offset);
		rbrfDouble = null;
		RandomForestWithPrefixEvaluation brfDouble = new BreimanBalancedRandomForest();
		String brfDoubleName = "BRF_DOUBLE";
		buildAndTestRF(brfDouble, trainSet, testSet, brfDoubleName, foldId,	numTrees, 200, offset);
		brfDouble = null;

	}

	private static void buildAndTestRF(RandomForestWithPrefixEvaluation rf,
			Instances trainSet, Instances testSet, String name, int foldId,
			int numTrees, int bagSizePercent, double offset) throws Exception {
		rf.setNumTrees(numTrees);
		rf.setBagSizePercent(bagSizePercent);
		long start = System.currentTimeMillis();
		rf.buildClassifier(trainSet, null); //second argument is context, only used in mapred, unused in bagging
		System.out.println("Build time for " + name + ": "
				+ (System.currentTimeMillis() - start));
		start = System.currentTimeMillis();
		dumpStats(rf, testSet, numTrees, offset, name, foldId);
		System.out.println("Test time for " + name + ": "
				+ (System.currentTimeMillis() - start));
	}

	// return the number of datacases in minority
	private static int getMinoritySize(Instances dataset) {
		int count = 0;
		for (int i = 0; i <= dataset.numInstances() - 1; i++) {
			if ((int) dataset.instance(i).classValue() == 1) // 1 = minority
				count++;
		}
		return count;
	}

	// stores AUC in file
	private static void dumpStats(RandomForestWithPrefixEvaluation rf,
			Instances testSet, int numTrees, double offset, String name,
			int foldId) throws Exception {
		output = new StringBuilder();
		int numPos = 0, numNeg = 0;
		double[] actualLabels = new double[testSet.numInstances()];
		// get numpos, numneg and actualLabels
		for (int i = 0; i < testSet.numInstances(); i++) {
			actualLabels[i] = testSet.get(i).classValue();
			if (actualLabels[i] == 1)
				numPos++;
			else
				numNeg++;
		}
		// build table with integers for each tree, to be randomized
		int numRepeats = Config.NUM_REPEATS;
		List<Integer> allNumbers = new ArrayList<Integer>(numTrees);
		for (int i = 0; i < numTrees; i++) {
			allNumbers.add(i);
		}

		// only log the trees that are powers of 2
		List<Integer> klist = Arrays.asList(1,2,4,8,16,32,64,128,256,512,1024,2048,4096,8192);

		// build numRepeats RFs of size numTrees so that we can get std dev of our model 
		for (int r = 0; r < numRepeats; r++) {
			double[] sumLabels = new double[testSet.numInstances()];
			double[] predLabels = new double[testSet.numInstances()];
			Collections.shuffle(allNumbers);
			// for each tree in the forest
			for (int k = 1; k <= numTrees; k++) {
				// classify all instances
				for (int i = 0; i < testSet.numInstances(); i++) {
					sumLabels[i] += rf.classify(testSet.get(i), allNumbers.get(k-1));
					// predLabels[i] = sumLabels[i] / k ;
					predLabels[i] = offset + sumLabels[i] / k;
				}
				if(klist.contains(k)) {
					String stats = ROC.GetFastPerfStats(predLabels, actualLabels, predLabels.length, numPos, numNeg);
					double pred2[] = Arrays.copyOf(predLabels, predLabels.length);
					double actual2[] = Arrays.copyOf(actualLabels, actualLabels.length);
					double prauc = PRAUC.getPrecRecallRoc(pred2, actual2, numPos);
					output.append(name + "," + foldId + "," + (r + 1) + "," + k + "," + stats + "," + prauc + "\n");
				} //end if
			} //end for k
		} //end for r
		IO.writeToExistingFile(output.toString(), outputFileName, "UTF-8");
	}

	public static void main(String[] args) throws Exception {
		String s = "Type" + "," + "Fold" + "," + "Run" + "," + "Trees"
		+ "," + "TP" + "," + "FN" + "," + "TN" + "," + "FP" + "," + "ROCAUC" + "," 
		+ "PRAUC" + "\n";
		IO.writeToNewFile(s, outputFileName, "UTF-8");

		System.out.println("Running the 7 RF with " + Config.NUM_TREES + " trees. Printing results in " + outputFileName);
		for (int i = 0; i <= Config.NUM_FOLDS; i++) {
			System.out.println("== Fold#" + i + " ==");
			processSet(i);
		}
		System.out.println("Done.");
	}

}
