package uci.pan.smote.parallel.input;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

public class ConfigInputFormat extends
		InputFormat<NullWritable, SmoteRunConfig> {

	@Override
	public RecordReader<NullWritable, SmoteRunConfig> createRecordReader(
			InputSplit split, TaskAttemptContext context) {
		return new ConfigRecordReader();
	}

	@Override
	public List<InputSplit> getSplits(JobContext context) {
		Configuration conf = context.getConfiguration();
		try {
			List<InputSplit> splits = new ArrayList<InputSplit>();
			// String rootFolder = conf.get("smote.rootPath");

			String rootFolder = conf.get("base.path");
			int numFolds = conf.getInt("num.folds", 1);
			int ks[] = {1, 5, 9, 15};
			int rates[] = {100, 200, 300, 400, 500};
			
			ConfigInputSplit cis;

			// build a list of tasks for mappers
			for (int f = 1; f <= numFolds; f++) { //1->3 = CrossValidation sets
			//for (int f = 0; f <= 0; f++) { //0 == train-test
				for (int k: ks) {
					for (int rate: rates) {
						cis = mkInputSplit(rootFolder, "train_" + f + ".arff",
								"valid_" + f + ".arff", k, rate);
						splits.add(cis);
					}
				}
			}
			return splits;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	// configs only has one config, but if we want to add more, it's possible
	public ConfigInputSplit mkInputSplit(String rootFolder,
			String trainFilename, String testFilename, int smoteK, int smoteRate) {
		// System.out.println("I made a split: k="+smoteK + " - r="+smoteRate);
		SmoteRunConfig fsc = new SmoteRunConfig();
		fsc.trainFilePath = rootFolder + trainFilename;
		fsc.testFilePath = rootFolder + testFilename;
		fsc.smoteK = smoteK;
		fsc.smoteRate = smoteRate;
		ConfigInputSplit inputsplit = new ConfigInputSplit();
		List<SmoteRunConfig> configs = new ArrayList<SmoteRunConfig>();
		configs.add(fsc);
		inputsplit.setConfigs(configs);
		return inputsplit;

	}

}
