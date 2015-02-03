package uci.pan.smote.parallel.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import weka.core.Instances;

public class HadoopUtil {

	public static Instances getInstancesFromFile(String inputUri)
			throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(HDFSUtils.getInputStream(inputUri)));
		Instances data = new Instances(reader);
		reader.close();

		return data;
	}

	public static void addConfigurationFromPath(Configuration conf, String path) throws Exception {
        FileSystem fs = FileSystem.get(new URI(path), conf);
        File localCopyOfPath = File.createTempFile("resources", "tmp");
        fs.copyToLocalFile(new Path(path), new Path(localCopyOfPath.getAbsolutePath()));
        conf.addResource(localCopyOfPath.toURI().toURL());
    }
	
}
