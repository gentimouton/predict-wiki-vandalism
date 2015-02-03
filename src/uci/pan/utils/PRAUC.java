package uci.pan.utils;


/**
 * PR-AUC code by Sara
 * 
 */
public class PRAUC {

	public static double getPrecRecallRoc(double[] predictions,
			double[] labels, int totalPositives) {
		sortPredictions(predictions, labels);
		int tie = mkPessimistic(predictions, labels);
		// System.out.print(tie+","); to be fixed

		if(totalPositives == 0) {
			System.out.println("Warning: there were 0 positives in the set given to prec-recall AUC.");
			return (-1);
		}
			
		double recall_step = 1.0 / totalPositives;
		int positives_sofar = 0;
		double[] precisions = new double[totalPositives + 1];

		precisions[0] = 1.0;

		for (int i = 0; i < predictions.length; i++) {
			if (labels[i] == 1.0) {
				positives_sofar++;
				precisions[positives_sofar] = positives_sofar
				/ (double) (i + 1);
			}
		}
		double auc = calcAUC(recall_step, precisions);
		return auc;
	}

	public static double calcAUC(double recall_step, double[] precisions) {
		double auc = 0.0;
		double xdiff = recall_step;

		for (int i = 1; i < precisions.length; i++) {
			double rectangle_auc = (precisions[i] * xdiff);
			double triangle_auc = (xdiff * 0.5 * Math.abs(precisions[i - 1]
			                                                         - precisions[i]));
			auc = auc + rectangle_auc + triangle_auc;
		}
		return auc;
	}

	public static void sortPredictions(double[] predictions, double[] labels) {

		for (int j = 0; j < predictions.length; j++) {
			for (int i = 0; i < predictions.length - 1; i++) {
				if (predictions[i] < predictions[i + 1]) {
					swap(predictions, i, i + 1);
					swap(labels, i, i + 1);
				}

			}
		}
	}

	public static int mkPessimistic(double[] predictions, double[] labels) {
		int tie = 0;
		for (int i = 0; i < predictions.length - 1; i++) {
			if (predictions[i] == predictions[i + 1]
			                                  && labels[i] > labels[i + 1]) {
				swap(predictions, i, i + 1);
				swap(labels, i, i + 1);
				tie++;
			}
		}
		return tie;
	}

	public static void swap(double[] input, int i, int j) {
		double help = input[i + 1];
		input[i + 1] = input[i];
		input[i] = help;
	}

	public static void main(String[] args) {
		double[] predictions1 = new double[] {0.4, 0.5, 0.6};
		double[] labels1 = new double[] {1.0, 1.0, 1.0};

		// getPrecRecallRoc(predictions0, labels0, 6);
		System.out.println(getPrecRecallRoc(predictions1, labels1, 3));
	}

}


