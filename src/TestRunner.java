import uci.pan.utils.ROC;

public class TestRunner {

	public static void main(String args[]) {
		double[] labels = {1,1,0,1,1,1,0,0,1,0};
		double[] prob = {0.9,0.8,0.7,0.6,0.55,0.54,0.53,0.52,0.51,0.505};
		
		double auc = ROC.GetFastAUCPrec(prob, labels, 10, 6, 4);
		System.out.println(auc);
		
	}
}
