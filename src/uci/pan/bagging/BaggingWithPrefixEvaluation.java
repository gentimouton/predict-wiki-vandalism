package uci.pan.bagging;

import org.apache.hadoop.mapreduce.Mapper.Context;

import uci.pan.smote.parallel.input.MyCounters;
import weka.classifiers.Classifier;
import weka.classifiers.meta.Bagging;
import weka.core.Instance;
import weka.core.Instances;

public class BaggingWithPrefixEvaluation extends Bagging {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("rawtypes")
	private Context context;

	public BaggingWithPrefixEvaluation(@SuppressWarnings("rawtypes") Context c) {
		this.context = c;
	}
	// returns the classification of instance by tree#treeIndex
	public double classify(Instance instance, int treeIndex) throws Exception {
		//System.out.println("baggingwithprefix.classify()");
		return m_Classifiers[treeIndex].classifyInstance(instance);
	}

	public void setPrefix(int prefixK) {
		m_NumIterations = prefixK;
	}

	// mapred specific
	public void setContext(@SuppressWarnings("rawtypes") Context context) {
		this.context = context;
	}

	@SuppressWarnings("unchecked")
	protected synchronized void buildClassifiers() throws Exception {		

		for (int i = 0; i < m_Classifiers.length; i++) {
			// mapred specific
			if(this.context != null) {
				this.context.getCounter(MyCounters.MAPPER_TREE_BUILT_COUNTER).increment(1);
				this.context.progress();
			}

			if (m_numExecutionSlots > 1) {
				final Classifier currentClassifier = m_Classifiers[i];
				final int iteration = i;
				if (m_Debug) {
					System.out.print("Training classifier (" + (i + 1) + ")");
				}
				Runnable newTask = new Runnable() {
					public void run() {
						try {
							Instances trainingSet = getTrainingSet(iteration);
							//System.out.println("number of instances in train set: " + trainingSet.numInstances());
							currentClassifier.buildClassifier(trainingSet);
							completedClassifier(iteration, true);
						} catch (Exception ex) {
							ex.printStackTrace();
							completedClassifier(iteration, false);
						}
					}
				};

				// launch this task
				m_executorPool.execute(newTask);
			} else {
				Instances trainingSet = getTrainingSet(i);
				//System.out.println("Number of instances in train set: " + trainingSet.numInstances());
				m_Classifiers[i].buildClassifier(trainingSet);
			}
		}

		if (m_numExecutionSlots > 1
				&& m_completed + m_failed < m_Classifiers.length) {
			block(true);
		}
	}

	private synchronized void block(boolean tf) {
		if (tf) {
			try {
				wait();
			} catch (InterruptedException ex) {
			}
		} else {
			notifyAll();
		}
	}
}
