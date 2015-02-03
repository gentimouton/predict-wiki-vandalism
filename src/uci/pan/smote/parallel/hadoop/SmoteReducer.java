package uci.pan.smote.parallel.hadoop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import uci.pan.smote.parallel.input.MyCounters;
import uci.pan.smote.parallel.input.SmoteTreeConfig;

public class SmoteReducer extends
		Reducer<SmoteTreeConfig, Text, SmoteTreeConfig, Text> {

	private class Stats {
		public int tp;
		public int fp;
		public int fn;
		public int tn;
		public double auc;

		public Stats(int tp, int fn, int tn, int fp, double auc) {
			this.tp = tp;
			this.tn = tn;
			this.fp = fp;
			this.fn = fn;
			this.auc = auc;
		}
	}

	private HashMap<SmoteTreeConfig, ArrayList<Stats>> results = new HashMap<SmoteTreeConfig, ArrayList<Stats>>();

	@Override
	public void reduce(SmoteTreeConfig key, Iterable<Text> values,
			Context context) throws IOException, InterruptedException {

		// keyin = SmoteRunConfig(smoteK, smoteRate, train, test)
		// valuein: string "\t" separated of numTrees,tp,fn,tn,fp,auc
		// keyout: SmoteTreeConfig = SmoteRunConfig with numTrees in it
		// valueOut: String containing average of each stats for all runs and
		//int k = key.smoteK;
		//int rate = key.smoteRate;
		//int numTrees = 0;
		int tp, tn, fp, fn;
		double auc;
		Stats s;
		ArrayList<Stats> statsList;
		//SmoteTreeConfig stc;
		for (Text statsText : values) {
			context.write(key, statsText);
			/*
			String stats[] = statsText.toString().split("\t");
			// build a reducer keyout
			//numTrees = Integer.parseInt(stats[0]);
			//stc = new SmoteTreeConfig(k, rate, numTrees);
			// add the stats to the corresponding config
			tp = Integer.parseInt(stats[0]);
			fn = Integer.parseInt(stats[1]);
			tn = Integer.parseInt(stats[2]);
			fp = Integer.parseInt(stats[3]);
			auc = Double.parseDouble(stats[4]);
			
			s = new Stats(tp, fn, tn, fp, auc);
			if (results.containsKey(key)) {
				statsList = results.get(key);
			} else {
				statsList = new ArrayList<SmoteReducer.Stats>();
			}
			statsList.add(s);
			results.put(key, statsList);
			*/
		}
		
		/*
		// when all keyIn have been received, valueOut is stats average
		double tpAvg, fnAvg, tnAvg, fpAvg;
		double tpSum = 0, fnSum = 0, tnSum = 0, fpSum = 0;
		double aucSum = 0;
		double aucAvg;
		int count = 0; // how many configs of this kind there are
		// normally, there should be numFolds * numRuns, but you never know ...
		Set<SmoteTreeConfig> configSet = results.keySet();
		for (SmoteTreeConfig config : configSet) {
			tpSum = fnSum = tnSum = fpSum = 0;
			aucSum = 0;
			//numTrees = config.numTrees;
			// rate and k are unchanged
			statsList = results.get(config);
			count = statsList.size();
			for (Stats stat : statsList) {
				tpSum += stat.tp;
				fpSum += stat.fp;
				tnSum += stat.tn;
				fnSum += stat.fn;
				aucSum += stat.auc;
			}
			// now get all the averages
			tpAvg =  tpSum / count;
			fnAvg = fnSum / count;
			tnAvg = tnSum / count;
			fpAvg = fpSum / count;
			aucAvg = aucSum / count;
			String output = Double.toString(tpAvg) + "\t"
					+ Double.toString(fnAvg) + "\t" + Double.toString(tnAvg)
					+ "\t" + Double.toString(fpAvg) + "\t"
					+ Double.toString(aucAvg);
			// and send them all
			context.write(config, new Text(output));
		}
		*/
		context.getCounter(MyCounters.REDUCER_INVOCATION_COUNTER).increment(1);

	}

}
