package uci.pan.smote.parallel.input;

import java.io.IOException;
import java.io.DataInput;
import java.io.DataOutput;
import java.util.ArrayList;
import java.util.List;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputSplit;

public class ConfigInputSplit extends InputSplit implements Writable {
	// each mapper is responsible to run a list of configs
	private List<SmoteRunConfig> configs;

	public void setConfigs(List<SmoteRunConfig> configs) {
		this.configs = configs;
	}

	public ConfigInputSplit() {
	}

	public ConfigInputSplit(List<SmoteRunConfig> configs) {
		this.configs = configs;
	}

	// to say how complex is the job by mappers
	@Override
	public long getLength() {
		if (configs == null) {
			return 0;
		}
		int sum = 0;
		for (SmoteRunConfig sc : configs) {
			sum += sc.smoteRate;
		}
		return sum;
	}

	// pass jobs to machines that have the data local
	@Override
	public String[] getLocations() throws IOException {
		return new String[] {};
	}

	// deserialize
	@Override
	public void readFields(DataInput in) throws IOException {
		int count = in.readInt();
		configs = new ArrayList<SmoteRunConfig>(count);
		for (int i = 0; i < count; i++) {
			SmoteRunConfig config = new SmoteRunConfig();
			config.readFields(in);
			configs.add(config);
		}
	}

	// serialize
	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(configs.size());
		for (int i = 0; i < configs.size(); i++) {
			SmoteRunConfig config = configs.get(i);
			config.write(out);
		}
	}

	public List<SmoteRunConfig> getConfigs() {
		return this.configs;
	}
}
