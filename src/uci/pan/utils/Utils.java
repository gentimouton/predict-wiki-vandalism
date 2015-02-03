package uci.pan.utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;


public class Utils {
	
	public static Instances getInstancesFromFile(String inputFilename) throws IOException
	 {
		BufferedReader reader = new BufferedReader(
				new FileReader(inputFilename));
		Instances data = new Instances(reader);
		reader.close();
		return data;
	}


	public static void splitIntoFolds() throws IOException {
		String path = Config.BASE_PATH;
		Instances trainset = Utils.getInstancesFromFile(path + "wtrain_0.arff");
		//trainset.deleteAttributeAt(trainset.attribute("editid").index());
		
		Random r = new Random();
		trainset.randomize(r); //shuffles Instances randomly
		Instances[] folds = new Instances[Config.NUM_FOLDS];
		//init folds
		for (int i=0; i<folds.length; i++) {
			folds[i] = new Instances(trainset, -1); //empty dataset
		}
		//fill folds
		for (int i=0; i<trainset.numInstances(); i++) {
			folds[i%Config.NUM_FOLDS].add(trainset.get(i));
		}
		//merge folds to get trainsets and validsets into arff files
		Instances trainTemp = new Instances(trainset, -1); //at step i, stores train_i.arff
		ArffSaver saver = new ArffSaver();
		
		for (int i=1; i<=Config.NUM_FOLDS; i++) {
			// flush folds[i] into valid_i.arff
			saver.setInstances(folds[i-1]);
			saver.setFile(new File(Config.BASE_PATH+"wvalid_"+i+".arff"));
			saver.writeBatch();
			// flush all other folds into train_i.arff
			trainTemp.delete();
			for (int j=1; j<=Config.NUM_FOLDS; j++) {
				if(j != i) {
					trainTemp.addAll(folds[j-1]);
				}
			}
			saver.setInstances(trainTemp);
			saver.setFile(new File(Config.BASE_PATH+"wtrain_"+i+".arff"));
			saver.writeBatch();
		}
	}
	
	
	public static void convertCsvToArff(String csvFile, String arffFile) throws Exception {
		CSVLoader loader = new CSVLoader();
		ArffSaver saver = new ArffSaver();
		// load CSV
		loader.setSource(new File(Config.BASE_PATH + csvFile));
		Instances data = loader.getDataSet();
		// save ARFF
		saver.setInstances(data);
		saver.setFile(new File(Config.BASE_PATH + arffFile));
		saver.writeBatch();

	}
	
	public static void splitTrainTest(int ratioInTrain) throws IOException {
		String path = Config.BASE_PATH;
		Instances trainset = Utils.getInstancesFromFile(path + "wall.arff");
		ArffSaver saver = new ArffSaver();
		//trainset.deleteAttributeAt(trainset.attribute("editid").index());
		Random r = new Random();
		trainset.randomize(r); //shuffles Instances randomly
		Instances[] folds = new Instances[2]; //train and test

		//init folds
		for (int i=0; i<folds.length; i++) {
			folds[i] = new Instances(trainset, -1); //empty dataset
		}
		//fill train
		int imax = (int) trainset.numInstances()*ratioInTrain/100; 
		for (int i=0; i<imax; i++) {
			folds[0].add(trainset.get(i));
		}
		//store as arff
		saver.setInstances(folds[0]);
		saver.setFile(new File(Config.BASE_PATH+"wtrain_0.arff"));
		saver.writeBatch();
		
		//fill test
		for (int i=imax; i<trainset.numInstances(); i++) {
			folds[1].add(trainset.get(i));
		}
		//store as arff
		saver.setInstances(folds[1]);
		saver.setFile(new File(Config.BASE_PATH+"wvalid_0.arff"));
		saver.writeBatch();
		
		
		
	}
		
	public static void main (String args[]) throws Exception {
		convertCsvToArff(Config.BASE_PATH + "wow-short.csv", Config.BASE_PATH + "wow-short.arff");
		//splitTrainTest(70); //train = 70%, test=30%
		//splitIntoFolds();
	}
}
