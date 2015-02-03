package uci.pan.smote.parallel.input;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.WritableComparable;

public class SmoteRunConfig implements WritableComparable<SmoteRunConfig> {

	public String trainFilePath;
	public String testFilePath;
	public int smoteK;
	public int smoteRate;


	public SmoteRunConfig() {
	}
	
	@Override
	public void write(DataOutput out) throws IOException {
		out.writeUTF(trainFilePath);
		out.writeUTF(testFilePath);
		out.writeInt(smoteK);
		out.writeInt(smoteRate);

	}

	@Override
	public void readFields(DataInput in) throws IOException {
		trainFilePath = in.readUTF();
		testFilePath = in.readUTF();
		smoteK = in.readInt();
		smoteRate = in.readInt();
	}

	@Override
	public int compareTo(SmoteRunConfig o) {
		if (this.smoteK < o.smoteK)
			return -1;
		else if (this.smoteK == o.smoteK) {
			return (this.smoteRate - o.smoteRate);
		}
		return 1;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(trainFilePath + "," + testFilePath + "," + this.smoteK
				+ "," + this.smoteRate);
		return result.toString();
	}

}
