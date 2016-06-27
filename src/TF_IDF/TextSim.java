package TF_IDF;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* Defines a 2D array of doubles to hold the term frequency counts for each term in each program
 * and scans the list of terms in each program to calculate the frequencies. 
 * */
public class TextSim {

	HashMap<String, Integer> termIndex;
	double[][] tfTable;
	int[] dfTable;

	int[] termLengths;
	
	int count = 0;
	int maxP;
	int maxTermCount;
	int numTerms;

	public TextSim(int maxP, int maxTermCount) {
		this.maxTermCount = maxTermCount;
		this.maxP = maxP;
		numTerms = 0;
		termIndex = new HashMap<String, Integer>(maxTermCount);
		tfTable = new double[maxTermCount][maxP];
		dfTable = new int[maxTermCount];
		
		termLengths = new int[maxTermCount];
	}

	/*
	 * Calls method to set up dfTable. Modifies tfTable values into tfidf
	 * values. Calculates cosine similarity between each pair of programs and
	 * stores the result in the passed in results array.
	 */
	public void bowSim(String[] results, String[] listOfFileLabels) {
		double idf = 0;
		double ab = 0;
		double b2 = 0;
		double a2 = 0;

		// Set up df table
		df();

		/*
		 * Calculates the IDF for the term and multiples it into each term
		 * frequency value in the table to produce the corresponding TF-IDF
		 */
		for (int i = 0; i < numTerms; i++) {
			idf = 0;
			// if df value is 1 then we don't care as it can't be similar to
			// anything
			if (dfTable[i] > 1) {
				/*
				 * Decrementing maxP and df by 1 on the grounds that one program
				 * represents the query against the rest which are the
				 * collection
				 **/
				idf = Math.log((maxP - 1) / (dfTable[i] - 1));
				for (int j = 0; j < maxP; j++) {
					// tfTable turns into the tfidf table
					tfTable[i][j] *= idf;
				}
			}
		}

		/*
		 * pA and pB are the actual indices in the TF-Table of the programs
		 * being compared AND the indices in the results matrix They never take
		 * the values of the upper half of the results matrix or the diagonal
		 */
		for (int pA = 0; pA < maxP; pA++) {
			// add labels
			results[pA] = listOfFileLabels[pA + 1];
			for (int pB = 0; pB < pA; pB++) {
				ab = 0;
				b2 = 0;
				a2 = 0;
				double cosSim = 0;
				for (int i = 0; i < numTerms; i++) {
					double tfidfA = tfTable[i][pA];
					double tfidfB = tfTable[i][pB];
					if (tfidfA != 0 || tfidfB != 0) {
						ab += tfidfA * tfidfB;
						a2 += Math.pow(tfidfA, 2);
						b2 += Math.pow(tfidfB, 2);
					}
				}
				if (ab != 0 && a2 != 0 && b2 != 0) {
					cosSim = ab / (Math.sqrt(a2) * Math.sqrt(b2));
				} else {
					cosSim = 0;
				}
				results[pA] += " " + String.format("%.0f", cosSim * 100);
			}
		}
	}

	public void nwsSim(String[] results, String[] listOfFileLabels) {
		double idf = 0;
		double ab = 0;
		double b2 = 0;
		double a2 = 0;

		df();

		for (int i = 0; i < numTerms; i++) {
			idf = 0;
			// if df value is 1 then we don't care as it can't be similar to
			// anything
			if (dfTable[i] > 1) {
				idf = Math.log((maxP - 1) / (dfTable[i] - 1));
				for (int j = 0; j < maxP; j++) {
					if (tfTable[i][j] != 0) {
						tfTable[i][j] =  idf * termLengths[i];
					}
				}
			}
		}

		for (int pA = 0; pA < maxP; pA++) {
			results[pA] = listOfFileLabels[pA + 1];
			for (int pB = 0; pB < pA; pB++) {
				ab = 0;
				b2 = 0;
				a2 = 0;
				double cosSim = 0;
				for (int i = 0; i < numTerms; i++) {
					double tfidfA = tfTable[i][pA];
					double tfidfB = tfTable[i][pB];
					
					if (tfidfA != 0 || tfidfB != 0) {
						ab += tfidfA * tfidfB;
						a2 += Math.pow(tfidfA, 2);
						b2 += Math.pow(tfidfB, 2);
					}
				}
				if (ab != 0 && a2 != 0 && b2 != 0) {
					cosSim = ab / (Math.sqrt(a2) * Math.sqrt(b2));
				} else {
					cosSim = 0;
				}
				results[pA] += " " + String.format("%.0f", cosSim * 100);
			}
		}
	}

	public void tf(List<String> termList, int p) {
		for (String term : termList) {
			int ti = getIndex(term);
			tfTable[ti][p] = tfTable[ti][p] + 1;
		}
	}

	/*
	 * Uses the initial term frequency values in tfTable to obtain the document
	 * frequency for each term.
	 */
	public void df() {
		for (int i = 0; i < numTerms; i++) {
			for (int j = 0; j < maxP; j++) {
				if (tfTable[i][j] != 0) {
					dfTable[i] += 1;
				}
			}
		}
	}

	public Integer getIndex(String term) {
		Integer ti = termIndex.getOrDefault(term, null);
		if (ti == null) {
			termIndex.put(term, numTerms);
			ti = numTerms;
			numTerms++;
			if (numTerms > maxTermCount) {
				System.err.println("Too many words in corpus");
			}
		}
		termLengths[ti] = term.length();
		return ti;
	}
}
