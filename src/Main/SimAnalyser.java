package Main;
/* Reads a set of java files and compares them pairwise for structural and textual similarity.
 * Asks the user for the parent directory containing the "AnonCW" directory (previously created
 * by the SimPrep program) containing the java files to be compared.
 * The output from the SimAnalyser is written to the parent directory supplied by the user,
 * in the format required by the SimViewer.
 * */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import javacc.JavaParser;
import javacc.SimpleNode;
import LCS.StructSim;
import TF_IDF.Terminator;
import TF_IDF.TextSim;

import com.github.javaparser.JavaParser_Javaparser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;

@SuppressWarnings("serial")
public class SimAnalyser extends JPanel {
	public static String newline = System.getProperty("line.separator");
	
	int numberOfFiles;
	String[] listOfFiles;
	String[] listOfFileStems;

	String[] structResults;
	String[] nwsTermResults;
	String[] bowTermResults;

	StructSim structSim;
	TextSim nwsSim, bowSim;

	public static void main(String[] args) throws Exception {
		SimAnalyser sa = new SimAnalyser();
		sa.displayFileChooser();
	}

	public void displayFileChooser() throws IOException {
		JFileChooser fc = new JFileChooser();

		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Java Files", "java");
		fc.setFileFilter(filter);

		int returnVal = fc.showOpenDialog(SimAnalyser.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			Path path = Paths.get(fc.getSelectedFile().getAbsolutePath());
			try {
				processScore(path);
			} catch (IOException e) {
				System.err.println(e);
			}
		}
	}

	public void processScore(Path path) throws IOException {
		String parentStem = path.getParent().toString();
		String stem = path.toString().replace("\\", "/");
		stem = stem + "/";
		File folder = new File(stem);

		// String array of the file names being handled
		listOfFiles = folder.list();

		// Number of files being handled
		numberOfFiles = listOfFiles.length;

		// Maximum expected terms in the corpus of programs
		final int maxTermCount = 10000;

		/* The 3 sets of results will go into separate string arrays with one
		 * row of each result matrix being represent as a single string. And
		 * each string will be prefixed with it's respective program label. */
		int resultSize = numberOfFiles; 
		structResults = new String[resultSize];
		nwsTermResults = new String[resultSize];
		bowTermResults = new String[resultSize];

		listOfFileStems = new String[listOfFiles.length];

		structSim = new StructSim(numberOfFiles);

		nwsSim = new TextSim(numberOfFiles, maxTermCount);
		bowSim = new TextSim(numberOfFiles, maxTermCount);

		CompilationUnit cu = null;

		for (int p = 0; p < numberOfFiles; p++) {
			// Load file for ASTLCS, returns AST
			SimpleNode tree = JavaParser.parseFile(stem + listOfFiles[p]);

			// Check that tree has been created
			if (tree == null) {
				System.err.println("The tree produced by the " + p
						+ "th program is not valid");
			} else {
				tree.reduceTreeSize();
				// Save current tree to array
				structSim.storeAST(tree, p);
			}

			FileInputStream file = new FileInputStream(stem + listOfFiles[p]);
			try {
				cu = JavaParser_Javaparser.parse(file);
			} catch (ParseException e) {
				e.printStackTrace();
			} finally {
				file.close();
			}

			Terminator terminator = new Terminator();

			/*
			 * Obtains identifiers (Class names, method names and variable
			 * declarations) for the given file and stores them in terminator
			 */
			terminator.visit(cu, null);

			/*
			 * Obtains the comments for the given file, processes them and
			 * cleans them, and then stores them in terminator
			 */
			terminator.commentParser(cu);

			// terminator.print();

			bowSim.tf(terminator.getBowTerms(), p);
			nwsSim.tf(terminator.getNwsTerms(), p);
		}

		// Writing results to file
		listOfFileStems = new String[numberOfFiles + 1];
		listOfFileStems[0] = "Files";
		// Strip the .java sufix from all the file names for use in the table
		for (int i = 1; i < numberOfFiles + 1; i++) {
			listOfFileStems[i] = listOfFiles[i - 1].substring(0,
					listOfFiles[i - 1].lastIndexOf("."));
		}

		structSim.structAnalyser(structResults, listOfFileStems);
		bowSim.bowSim(bowTermResults, listOfFileStems);
		nwsSim.nwsSim(nwsTermResults, listOfFileStems);

		writeToFile(parentStem);

	}

	// Save results to file - used for graph display
	public void writeToFile(String stem) {
		stem = stem + "\\";
		BufferedWriter structOutput, nwsOutput, bowOutput = null;

		try {
			structOutput = new BufferedWriter(new FileWriter(stem
					+ "structResults.txt"));
			nwsOutput = new BufferedWriter(new FileWriter(stem
					+ "NWSResults.txt"));
			bowOutput = new BufferedWriter(new FileWriter(stem
					+ "BOWResults.txt"));

			structOutput.write(String.valueOf(numberOfFiles));
			structOutput.newLine();
			structOutput.write(cleanOutput(listOfFileStems, false));
			structOutput.newLine();
			structOutput.write(cleanOutput(structResults, true));

			nwsOutput.write(String.valueOf(numberOfFiles));
			nwsOutput.newLine();
			nwsOutput.write(cleanOutput(listOfFileStems, false));
			nwsOutput.newLine();
			nwsOutput.write(cleanOutput(nwsTermResults, true));

			bowOutput.write(String.valueOf(numberOfFiles));
			bowOutput.newLine();
			bowOutput.write(cleanOutput(listOfFileStems, false));
			bowOutput.newLine();
			bowOutput.write(cleanOutput(bowTermResults, true));

			structOutput.flush();
			nwsOutput.flush();
			bowOutput.flush();

			structOutput.close();
			nwsOutput.close();
			bowOutput.close();

		} catch (IOException e) {
			System.err.println(e);
		}
	}

	public String cleanOutput(String[] res, boolean isResult) {
		String cleanResults = Arrays.toString(res);
		cleanResults = cleanResults.replace("[", "");
		cleanResults = cleanResults.replace("]", "");

		if (isResult) {
			cleanResults = cleanResults.replace(", ", newline);
		} else {
			cleanResults = cleanResults.replace(", ", " ");
		}
		return cleanResults;
	}
}
