package TF_IDF;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.body.VariableDeclaratorId;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class Terminator extends VoidVisitorAdapter<Object> {
	ArrayList<String> bowTerms;
	ArrayList<String> nwsTerms;

	public Terminator() {
		bowTerms = new ArrayList<String>();
		nwsTerms = new ArrayList<String>();
	}

	public void visit(ClassOrInterfaceDeclaration className, Object arg) {
		nwsTerms.add(className.getName());
		super.visit(className, arg);
	}

	public void visit(MethodDeclaration methodName, Object arg) {
		nwsTerms.add(methodName.getName());
		super.visit(methodName, arg);
	}

	public void visit(VariableDeclaratorId varName, Object arg) {
		nwsTerms.add(varName.getName());
		super.visit(varName, arg);
	}

	public ArrayList<String> getBowTerms() {
		return bowTerms;
	}

	public ArrayList<String> getNwsTerms() {
		return nwsTerms;
	}

	/*
	 * commentParser Called once per program, gets all the comments in the given
	 * program and stores them in a list. Then for each comment element (either
	 * single line comment, or multi line comment) in the list, passes that
	 * element to the comment handler.
	 */
	public void commentParser(CompilationUnit cu) {
		List<Comment> commentList = cu.getComments();

		if (!commentList.isEmpty()) {
			// Take for each comment in a given file
			for (Comment comm : commentList) {
				commentLineHandler(comm);
			}
		}
	}

	/*
	 * Given a comment element, commentHandler converts it to a string and then
	 * splits on the new lines. For each split line it passes that line to the
	 * commentLine method. commentLine returns a cleaned string. Provided that
	 * the string length is not equal to 0, the string is then added to the cleanComment list
	 */
	public void commentLineHandler(Comment comment) {
		String genericComment = comment.getContent();
		for (String splitLine : genericComment.split("\n")) {
			String cleanComment = commentLine(splitLine);

			if (cleanComment.length() > 0) {
				for (String word : cleanComment.split(" ")) {
					bowTerms.add(word);
				}
				// single-word line comments are not being added to the nwsTerms list
				if (cleanComment.split(" ").length > 1) {
					nwsTerms.add(cleanComment);
				}
			}
		}
	}

	/*
	 * Takes a single line of raw comment as a string and returns either a pure
	 * alphanumeric string of words separated by single spaces, stripped of
	 * leading and trailing white space; or an empty string, if the raw comment
	 * contained no alphanumeric characters
	 */
	public String commentLine(String rawLine) {
		// Leaves only alphanumeric characters and spaces
		String cleanLine = rawLine.replaceAll("[^\\p{Alnum} ]", "");
		// Replaces all whitespace with a single space and removes leading and
		// trailing whitespace
		cleanLine = cleanLine.replaceAll("\\s+", " ").trim();

		return cleanLine;
	}

	public void print() {
		System.out.println(getBowTerms());
	}
}