package uci.pan.smote.parallel.input;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

public class ConfigRecordReader extends RecordReader<NullWritable, SmoteRunConfig> {
	private ConfigInputSplit split;
	private int pos;
	private int count;

	public ConfigRecordReader() {		
	}

	@Override
	public void initialize(InputSplit split, TaskAttemptContext context) throws IOException, InterruptedException {
		this.split = (ConfigInputSplit) split;
		this.pos = -1;
		this.count = this.split.getConfigs().size();
	}

	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
		pos++;
		return (pos < count);		
	}

	@Override
	public NullWritable getCurrentKey() throws IOException, InterruptedException {
		return NullWritable.get();
	}

	@Override
	public SmoteRunConfig getCurrentValue() throws IOException, InterruptedException {
		return split.getConfigs().get(pos);
	}
	
	@Override
	public void close() {
	}

	@Override
	public float getProgress() {
		if (pos < 0) {
			return 0.0f;
		}
		return ((float) pos) / count;
	}

}
