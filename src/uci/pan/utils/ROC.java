package uci.pan.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ROC {

	/** 
	 * Yasser's original function
	 * prob: prediction vector
	 * labels: actual labels
	 * indices: pass null
	 * size: how many cases to look at
	 * totalPos: number of pos cases in actual labels
	 * totalneg: number of neg cases in actual labels
	 * returns AUC
	 */
	
   public static double GetFastROC(double[] prob, double[] labels, int[] indices, int size, int totalPositive,
           int totalNegative) {
       List<DoubleDoublePair> sortedProb = new ArrayList<DoubleDoublePair>();
       if (indices == null) {
           for (int d = 0; d < size; d++) {
               sortedProb.add(new DoubleDoublePair(prob[d], labels[d]));
           }
       } else {
           for (int d = 0; d < size; d++) {
               sortedProb.add(new DoubleDoublePair(prob[d], labels[indices[d]]));
           }
       }
       Collections.sort(sortedProb);

       double fp = 0;
       double tp = 0;
       double fpPrev = 0;
       double tpPrev = 0;
       double area = 0;
       double fPrev = Double.MIN_VALUE;

       int i = 0;
       while (i < sortedProb.size()) {
           DoubleDoublePair pair = sortedProb.get(i);
           double curF = pair.key;
           if (curF != fPrev) {
               area += Math.abs(fp - fpPrev) * ((tp + tpPrev) / 2.0);
               fPrev = curF;
               fpPrev = fp;
               tpPrev = tp;
           }
           double label = pair.value;
           if (label == +1) {
               tp++;
           } else {
               fp++;
           }
           i++;
       }
       area += Math.abs(totalNegative - fpPrev) * ((totalPositive + tpPrev) / 2.0);
       area /= ((double) totalPositive * totalNegative);
       return area;
   }


	//return the AUC based on sensitivity and 1-specificity
   //in others words, make a rieman sum with y-axis=TP rate and x-axis=FP rate  
	public static String GetFastPerfStats(double[] prob, double[] labels,
			int size, int totalPositive, int totalNegative) {
		List<DoubleDoublePair> sortedProb = new ArrayList<DoubleDoublePair>();
		for (int d = 0; d < size; d++) {
			sortedProb.add(new DoubleDoublePair(prob[d], labels[d]));
		}

		Collections.sort(sortedProb);

		double fp = 0;
		double tp = 0;
		double fpPrev = 0;
		double tpPrev = 0;
		double area = 0;
		double fPrev = Double.MIN_VALUE;

		int i = 0;
		while (i < sortedProb.size()) {
			DoubleDoublePair pair = sortedProb.get(i);
			double curF = pair.key;
			if (curF != fPrev) {
				area += Math.abs(fp - fpPrev) * ((tp + tpPrev) / 2.0);
				fPrev = curF;
				fpPrev = fp;
				tpPrev = tp;
			}
			double label = pair.value;
			if (label == +1) {
				tp++;
			} else {
				fp++;
			}
			i++;
		}
		area += Math.abs(totalNegative - fpPrev)
				* ((totalPositive + tpPrev) / 2.0);
		area /= ((double) totalPositive * totalNegative);
		// get TP FP FN and TN, and return them all with AUC
		int mytp = 0, myfp = 0, myfn = 0, mytn = 0;
		for (i = 0; i < prob.length; i++) {
			if (labels[i] == +1) {
				if (Math.round(prob[i]) == 0)
					myfn++;
				else if (Math.round(prob[i]) == 1)
					mytp++;
			} else if (labels[i] == 0) {
				if (Math.round(prob[i]) == 0)
					mytn++;
				else if (Math.round(prob[i]) == 1)
					myfp++;
			}
		}
		String output = mytp + "," + myfn + "," + mytn + "," + myfp + "," + area;
		return output;
	}
	   
	   
	// return the area under curve of the precision-recall ROC curve
	//params:
	// prob = my label (double between 0 and 1)
	// labels = actual labels (0 OR 1)
	// size = how many data cases
	// totalPositive = number of actual positives
	// totalNegative = number of actual negatives
	/*
	 * this code might have bugs / not be accurate
	 */
	public static double GetFastAUCPrec(double[] prob, double[] labels,
			int size, int totalPositive, int totalNegative) {
		// sort by prob, ie my label
		List<DoubleDoublePair> sortedProb = new ArrayList<DoubleDoublePair>();
		for (int d = 0; d < size; d++) {
			sortedProb.add(new DoubleDoublePair(prob[d], labels[d]));
           }
	       
	       Collections.sort(sortedProb);

	       double fp = 0;
	       double tp = 0;
	       double fpPrev = 0;
	       double tpPrev = 0;
	       double area = 0;
	       double fPrev = Double.MIN_VALUE;
	       
	       int i = 0;
	       //for each data case (taken in ascending order of their predicted label)
	       while (i < sortedProb.size()) {
	           DoubleDoublePair pair = sortedProb.get(i);
	           double curF = pair.key;
	           //when there is a bump in the curve
	           if (curF != fPrev) {
	        	   //add (bump width) * (average bump height) to the area 
	               area += Math.abs(fp - fpPrev) * ((tp + tpPrev) / 2.0);
	               fPrev = curF;
	               fpPrev = fp;
	               tpPrev = tp;
	           }
	           double actualLabel = pair.value;
	           if (actualLabel == +1) {
	               tp++;
	           } else {
	               fp++;
	           }
	           i++;
	       }
	       // add the last data case (eventual) contribution
	       area += Math.abs(totalNegative - fpPrev) * ((totalPositive + tpPrev) / 2.0);
	       //normalize by both axis denominators
	       area /= ((double) totalPositive * (tp+fp));
	      
	       return area;
	   }
   
   private static class DoubleDoublePair implements Comparable<DoubleDoublePair> {
       public double key;
       public double value;

       public DoubleDoublePair(double key, double value) {
           this.key = key;
           this.value = value;
       }

       public int compareTo(DoubleDoublePair o) {
           if (this.key > o.key) {
               return -1;
           } else if (this.key < o.key) {
               return 1;
           }
           return 0;
       }
   }
}