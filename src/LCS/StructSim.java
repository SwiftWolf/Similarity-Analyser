package LCS;

import javacc.SimpleNode;

/* Defines an array of AST's and provides access methods to store and recall them.
 * And calls the pair-wise comparison on the structure of the trees and returns
 * their similarity score in the form of a String array. 
 * */
public class StructSim {
	SimpleNode[] astArray; // holds the array of ASTs
	
	int maxP; // the number of programs being compared
	
	// Sets the size of the AST array to be number of programs to be compared
	public StructSim(int maxP) {
		this.maxP = maxP;
		astArray = new SimpleNode[maxP];	
	}
	
	// Inserts AST tree at index p
	public void storeAST(SimpleNode tree, int p){
		astArray[p] = tree;
	}
	
	// Returns AST at index p
	public SimpleNode recallAST(int p) {
		return astArray[p];
	}
	
	/* Calls the simASTLCD structural analysis method once for each pair of ASTs in astArray,
	 * ignoring the diagonal cases, i.e. n(n-1) / 2 times. Writes the resulting pairwise
	 * similarity scores as string representations of integers, into the supplied String array.
	 * */
	public void structAnalyser(String[] results, String[] listOfFileLabels){
		SimpleNode tree1, tree2;
		// i (row)
		for (int i = 0; i < maxP; i++) {
			// j (column)
			results[i] = listOfFileLabels[i + 1];
			for (int j = 0; j < i; j++) {
				tree1 = recallAST(i);
				tree2 = recallAST(j);
				
				tree1.reduceTreeSize();
				tree2.reduceTreeSize();
				
				tree1.sizeSubtree();
				tree2.sizeSubtree();
				
				ASTLCS simASTLCS = new ASTLCS(tree1, tree2);
				double astLcsResult = simASTLCS.main();				
				results[i] += " " + String.format("%.0f", astLcsResult * 100);
			}
		}
	}
}
