package uci.pan.smote.serial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import uci.pan.bagging.RandomForestWithPrefixEvaluation;
import uci.pan.bagging.SmoteRandomForest;
import uci.pan.utils.Config;
import uci.pan.utils.IO;
import uci.pan.utils.PRAUC;
import uci.pan.utils.ROC;
import uci.pan.utils.Utils;
import weka.core.Attribute;
import weka.core.Instances;

public class SmoteRunner {

	static String outputFileName = Config.OUTPUT_FILE_PATH + "-"
	+ Config.NUM_TREES + "trees.txt";
	static StringBuilder output;

	private static void processFold(int foldID, List<Integer> kList, List<Integer> rList) throws Exception {
		String path = Config.BASE_PATH;
		Instances trainSet = Utils.getInstancesFromFile(path + "train_" + foldID + ".arff");
		Instances testSet = Utils.getInstancesFromFile(path + "valid_" + foldID + ".arff");
		// define which attribute is the label
		Attribute labelAttr = trainSet.attribute(Config.CLASS_NAME);
		trainSet.setClass(labelAttr);
		testSet.setClass(labelAttr);

		// how much to vary the decision threshold
		// does not impact AUC, just sensitivity and specificity
		double offset = 0;
		int numTrees = Config.NUM_TREES; //10k max
		
		// for each rate and each k, build smoted forests
		RandomForestWithPrefixEvaluation rf = null;
		for(int r: rList) {
			for (int k : kList) {	
				rf = new SmoteRandomForest();
				rf.setSeed(r+k); //just in case
				((SmoteRandomForest)rf).setSmoteK(k);
				((SmoteRandomForest)rf).setSmoteRate(r);
				rf.setNumTrees(numTrees);
				rf.setBagSizePercent(100);
				// build smoted RF
				long start = System.currentTimeMillis();
				rf.buildClassifier(trainSet, null);//2nd argument is context for Map Reduce job, ie not serial case like here
				long trainTime = System.currentTimeMillis() - start;
				System.out.println("Time taken to train Smote("+k+"NN, " +r+"%): " + trainTime);
				start = System.currentTimeMillis();
				dumpStats(rf, testSet, foldID, offset, numTrees, k, r);
				long testTime = System.currentTimeMillis() - start;
				System.out.println("Time taken to test: " + testTime);			
				rf = null;
			} //end for k
		} // end for r
	}

	

	// stores various stats in file: #trees, foldId, TP, FP, FN, TN, AUC, ...
	private static void dumpStats(RandomForestWithPrefixEvaluation rf,
			Instances testSet, int foldId, double offset, int numTrees, int k,
			int rate) throws Exception {

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
		List<Integer> allNumbers = new ArrayList<Integer>(numTrees);
		for (int i = 0; i < numTrees; i++) {
			allNumbers.add(i);
		}

		// only log the trees that are powers of 2
		List<Integer> tlist = Arrays.asList(1,2,4,8,16,32,64,128,256,512,1024,2048);
		double pred2[];
		double actual2[];
		double prauc;
		// build numRepeats RFs of size numTrees so that we can get std dev of our model 
		for (int r = 1; r <= Config.NUM_REPEATS; r++) {
			double[] sumLabels = new double[testSet.numInstances()];
			double[] predLabels = new double[testSet.numInstances()];
			Collections.shuffle(allNumbers);
			// for each tree in the forest
			for (int t = 1; t <= numTrees; t++) {
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
					output.append(foldId + "\t" + r + "\t" + t + "\t" + k + "\t" + rate + "\t" + stats + "\t" + prauc + "\n");
				} //end if
			} //end for k
		} //end for r
		IO.writeToExistingFile(output.toString(), outputFileName, "UTF-8");
	}



	public static void main(String[] args) throws Exception {
		String s = "F" + "\t" + "Run" + "\t" + "t" + "\t" + "k" + "\t" + "r" 
		+ "\t" + "TP" + "\t" + "FN" + "\t" + "TN" + "\t" + "FP" + "\t" + "ROC-AUC" + "\t" 
		+ "PR-AUC" + "\n";
		IO.writeToNewFile(s, outputFileName, "UTF-8");

		//List<Integer> smoteKs = Arrays.asList(1,3,5,9,13,19,25,33);
		List<Integer> smoteKs = Arrays.asList(5, 21);
		//List<Integer> smoteRates = Arrays.asList(0,100,300,600,1300);
		List<Integer> smoteRates = Arrays.asList(200, 600);

		System.out.println("Running NRF + SMOTE with " + Config.NUM_TREES + " trees.");
		System.out.println("Using up to " + smoteKs.get(smoteKs.size()-1) + "NN and up to " + smoteRates.get(smoteRates.size()-1) + "% oversampling.");
		System.out.println("Printing results in " + outputFileName);

		for (int i = 0; i <= Config.NUM_FOLDS; i++) {
			System.out.println("== Fold#" + i + " ==");
			processFold(i, smoteKs, smoteRates);
		}
		System.out.println("Done.");

	}
}
