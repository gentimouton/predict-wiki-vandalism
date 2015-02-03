package uci.pan.smote.parallel.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import uci.pan.smote.parallel.input.ConfigInputFormat;
import uci.pan.smote.parallel.input.SmoteTreeConfig;
import uci.pan.smote.parallel.util.HadoopUtil;

public class SmoteRunner extends Configured implements Tool {

	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = new Configuration();

		// add your libraries here
		//DistributedCache.addCacheFile(new URI("/user/thomas/ml.properties"), conf);
		//conf.addResource("file:///home/thomas/pan/ml.properties");
		
		HadoopUtil.addConfigurationFromPath(conf, "hdfs:///user/hadoop/properties.xml");
		DistributedCache.addFileToClassPath(new Path("/user/hadoop/lib/weka.jar"), conf);
		DistributedCache.addFileToClassPath(new Path("/user/hadoop/lib/SMOTE.jar"), conf);
		
		Job job = new Job(conf, "RF-SMOTE");
		job.setJarByClass(SmoteRunner.class);

		job.setMapperClass(SmoteMapper.class);
		job.setReducerClass(SmoteReducer.class);

		job.setInputFormatClass(ConfigInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		job.setOutputKeyClass(SmoteTreeConfig.class);
		job.setOutputValueClass(Text.class);

		FileOutputFormat.setOutputPath(job, new Path(args[args.length-1]));

		return (job.waitForCompletion(true) ? 0 : 1);
	}

	public static void main(String[] args) {
		int res;
		try {
			res = ToolRunner.run(new Configuration(), new SmoteRunner(),
					args);
			System.exit(res);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}