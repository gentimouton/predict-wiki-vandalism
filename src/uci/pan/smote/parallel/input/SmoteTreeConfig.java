package uci.pan.smote.parallel.input;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

public class SmoteTreeConfig implements WritableComparable<SmoteTreeConfig> {
	public int numTrees;
	public int smoteK;
	public int smoteRate;
	
	public SmoteTreeConfig() {
	}
	
	// SmoteTreeConfig is just a SmoteRunConfig with the number of trees in it
	public SmoteTreeConfig(int k, int rate, int numTrees) {
		this.numTrees = numTrees;
		this.smoteK = k;
		this.smoteRate = rate;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(this.smoteK);
		out.writeInt(this.smoteRate);
		out.writeInt(this.numTrees);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		this.smoteK = in.readInt();
		this.smoteRate = in.readInt();
		this.numTrees = in.readInt();
	}

	@Override
	public int compareTo(SmoteTreeConfig o) {
		if (this.smoteK < o.smoteK)
			return -1;
		else if (this.smoteK == o.smoteK) {
			if (this.smoteRate < o.smoteRate)
				return -1;
			else {
				if (this.smoteRate == o.smoteRate)
					return (this.numTrees - o.numTrees);
			}
		}
		return 1;
	}

	@Override
	public String toString() {
		String result = this.smoteK + "," + this.smoteRate + ","
				+ this.numTrees;
		return result;
	}

}
