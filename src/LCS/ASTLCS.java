package LCS;

import java.util.ArrayList;
import java.util.HashMap;
import javacc.SimpleNode;
import javacc.JavaParserTreeConstants;

public class ASTLCS {

	// Store of all possible matchings
	public HashMap<SimpleNode, ArrayList<SimpleNode>> hashMapB = null;
	// Actual Mappings
	public HashMap<SimpleNode, SimpleNode> hashMapM = null;
	public SimpleNode tree1, tree2;
	public double results;

	public ASTLCS(SimpleNode tree1, SimpleNode tree2) {
		// We always find the mapping between the smallest tree and the largest
		// tree. May need to switch the root nodes.
		if (tree1.getSize() <= tree2.getSize()) {
			this.tree1 = tree1;
			this.tree2 = tree2;
		} else {
			this.tree1 = tree2;
			this.tree2 = tree1;
		}
		hashMapB = new HashMap<SimpleNode, ArrayList<SimpleNode>>(tree1.getSize());
		hashMapM = new HashMap<SimpleNode, SimpleNode>(tree1.getSize());
	}

	@SuppressWarnings("finally")
	public float main() {
		if (tree1.getId() == tree2.getId()) {
			int res = topDownUnorderedMaxCommonSubtreeIso(tree1, tree2);
			hashMapM.put(tree1, tree2);

			ArrayList<SimpleNode> list = new ArrayList<SimpleNode>(tree1.getSize());
			tree1.preorderTraversal(list);
			list.remove(0);

			try {
				for (SimpleNode node : list) {
					// has the node been checked?
					if (!node.checked) {
						if (hashMapB.containsKey(node)) {
							for (SimpleNode w : hashMapB.get(node)) {
								if (hashMapM.get(node.getParent()) == w.getParent()) {
									SimpleNode vp1 = node.getParent();
									SimpleNode wp2 = w.getParent();

									if (!(node.getId() == JavaParserTreeConstants.JJTBLOCK
											&& vp1.getId() == JavaParserTreeConstants.JJTMETHODDECLARATION)
											&& !(w.getId() == JavaParserTreeConstants.JJTBLOCK
													&& wp2.getId() == JavaParserTreeConstants.JJTMETHODDECLARATION)) {
										hashMapM.put(node, w);
										break;
									}

									else {
										ArrayList<SimpleNode> vList = new ArrayList<SimpleNode>(node.getSize());
										node.preorderTraversal(vList);

										ArrayList<SimpleNode> wList = new ArrayList<SimpleNode>(w.getSize());
										w.preorderTraversal(wList);

										// Go through the nodes in the preoder
										// traversal of the subtree rooted at v
										for (SimpleNode v1 : vList) {
											// Set check to true. Do not want to
											// handle this node again.
											v1.checked = true;

											// If v1 is in the B map
											if (hashMapB.containsKey(v1)) {
												for (SimpleNode w1 : hashMapB.get(v1)) {
													int index;
													// Check if w1 is a member
													// of the subtree rooted at
													// w
													if ((index = wList.indexOf(w1)) != -1) {
														// If yes, map it to v1
														hashMapM.put(v1, w1);
														break;
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			} finally {
				float similarity = sim(tree1.getSize(), tree2.getSize(), res);
				return similarity;
			}
		}
		return 0;
	}

	// ASTSIM-LCS(Node r1, Node r2, Map<Node, List<Node>> B)
	public int topDownUnorderedMaxCommonSubtreeIso(SimpleNode tree1, SimpleNode tree2) {

		// if label(r1) /= label(r2) then
		if (tree1.getId() != tree2.getId()) {
			// return 0
			return 0;
		}

		// if isLeafNode(r1) or isLeafNode(r2) then
		if (tree1.isLeaf() || tree2.isLeaf()) {
			// return 1
			if (tree1.getId() == tree2.getId()) {
				return 1;
			} else {
				return 0;
			}
		}

		SimpleNode parentTree1 = tree1.getParent();
		SimpleNode parentTree2 = tree2.getParent();

		int result;
		// if label(r1) = label(r2) = BLOCK and label(parent(r1)) =
		// label(parent(r2)) = METHOD-DECLARATION then
		if (tree1.getId() == JavaParserTreeConstants.JJTBLOCK && tree2.getId() == JavaParserTreeConstants.JJTBLOCK
				&& parentTree1.getId() == JavaParserTreeConstants.JJTMETHODDECLARATION
				&& parentTree2.getId() == JavaParserTreeConstants.JJTMETHODDECLARATION) {

			// result <- LCS(v1, v2, B)
			int tmpResult = lcs(tree1, tree2);
			// Return result - Requires being passed out of the if stmt
			result = tmpResult;

		} else {

			int tr1Size = tree1.numOfChildren();
			int tr2Size = tree2.numOfChildren();

			// Nodes to vertex
			// Map<Node, Vertex> T1G
			HashMap<SimpleNode, Vertex> T1G = new HashMap<SimpleNode, Vertex>(tr1Size);

			// Nodes to vertex
			// Map<Node, Vertex> T2G
			HashMap<SimpleNode, Vertex> T2G = new HashMap<SimpleNode, Vertex>(tr2Size);

			// Vertex to nodes
			// Map<Vertex, Node> GT
			HashMap<Vertex, SimpleNode> GT = new HashMap<Vertex, SimpleNode>(tr1Size + tr2Size);

			// List<Vertex> U
			ArrayList<Vertex> U = new ArrayList<Vertex>(tr1Size);

			// List<Edge> edges
			ArrayList<Edge> edges = new ArrayList<Edge>(tr1Size * tr2Size);

			// for all v1 in children(r1) do
			for (SimpleNode v1 : tree1.getChildren()) {

				// Create new vertex v
				Vertex v = new Vertex(tr2Size);

				// Insert v into U
				U.add(v);

				// GT[v] <- v1
				GT.put(v, v1);

				// T1G[v1] <- v
				T1G.put(v1, v);
			}

			// List<Vertex> W
			ArrayList<Vertex> W = new ArrayList<Vertex>(tr2Size);

			// for all v2 in children(r2) do
			for (SimpleNode v2 : tree2.getChildren()) {
				// Create new vertex w
				Vertex w = new Vertex(tr1Size);

				// Insert w into W
				W.add(w);

				// GT[w] <- v2
				GT.put(w, v2);

				// T2G[v2] <- w
				T2G.put(v2, w);

			}

			ArrayList<Edge> list = null;

			// for all v1 in children(r1) do
			for (SimpleNode v1 : tree1.getChildren()) {

				// for all v2 in children(r2) do
				for (SimpleNode v2 : tree2.getChildren()) {

					// Max subtree between v1 and v2
					// result <- ASTSIMLCS(v1,v2,B)
					int tmpResult = topDownUnorderedMaxCommonSubtreeIso(v1, v2);

					// if result /= 0 then
					if (tmpResult != 0) {

						// Create new Edge e = (T1G[v1],T2G[v2])
						Vertex v = T1G.get(v1);
						Edge e = v.addEdge(T2G.get(v2));

						// e.weight <- result
						e.setCost(tmpResult);

						// Insert e into edges
						edges.add(e);
					}
				}
			}

			// matchedEdges <- MaxWeightBipatiteMatching(U,W,edges)
			BipartiteMatching bipartite = new BipartiteMatching();
			list = bipartite.maxWeightBipartieMatching(U, W, edges, tr1Size, tr2Size);

			// result <- 1
			int tmpResult = 1;

			// for all e in matchedEdges do
			for (Edge e : list) {

				ArrayList<SimpleNode> nodeList = null;

				// Vertex v1 <- source(e), where v1 in U
				SimpleNode v1 = GT.get(e.getFrom());

				// Vertex v2 <- target(e), where v2 in W
				SimpleNode v2 = GT.get(e.getTo());

				// if B[GT[v1]] /= null then
				if (hashMapB.containsKey(v1)) {
					// list <- B[GT[v1]]
					nodeList = hashMapB.get(v1);
				} else {
					nodeList = new ArrayList<SimpleNode>(100);
				}

				// Insert GT[v2] into list
				nodeList.add(v2);

				// B[GT[v1]] <- list
				hashMapB.put(v1, nodeList);

				// result <- result + e.weight
				tmpResult += e.getCost();

			}
			result = tmpResult;
		}
		return result;
	}

	// LCS(Node r1, Node r2, Map<Node, List<Node>> B)
	public int lcs(SimpleNode subTree1, SimpleNode subTree2) {

		// Preorder traversal of the subtree rooted at r1 (r2)
		ArrayList<SimpleNode> subtreePreOrd1 = new ArrayList<SimpleNode>(subTree1.getSize());
		ArrayList<SimpleNode> subtreePreOrd2 = new ArrayList<SimpleNode>(subTree2.getSize());

		subTree1.preorderTraversal(subtreePreOrd1);
		subTree2.preorderTraversal(subtreePreOrd2);

		// m (n)<- Size of the subtree rooted at r1(r2)
		int m = subTree1.getSize();
		int n = subTree2.getSize();

		// Array <INT, INT> c
		int c[][] = new int[m + 1][n + 1];

		// for i = 0 to m do - c(i, 0) <- 0
		for (int i = 0; i <= m; i++) {
			c[i][0] = 0;
		}

		// for j = 0 to n do - c(j, 0) <- 0
		for (int j = 0; j <= n; j++) {
			c[0][j] = 0;
		}

		// for i = 1 to m do
		for (int i = 1; i <= m; i++) {
			// for j = 1 to n do
			for (int j = 1; j <= n; j++) {

				// v1 <- (i-1)-th element in subtree1
				SimpleNode i1 = subtreePreOrd1.get(i - 1);
				// v2 <- (j-1)-th element in subtree2
				SimpleNode j1 = subtreePreOrd2.get(j - 1);

				SimpleNode p1 = i1.getParent();
				SimpleNode p2 = j1.getParent();

				// if(label(v1) = label(v2)) and ((label(parent(p1) =
				// label(parent(p2)) or label(v1) = BLOCK)
				if ((i1.getId() == j1.getId() && ((p1.getId() == p2.getId()))
						|| i1.getId() == JavaParserTreeConstants.JJTBLOCK)) {
					// c(i,j) = c(i-1, j-1) + 1
					c[i][j] = c[i - 1][j - 1] + 1;
				} else {
					// c(i,j) = max(c(i,j-1) , c(i-1, j))
					c[i][j] = Math.max(c[i][j - 1], c[i - 1][j]);
				}
			}
		}

		// Map <Node, Node> tmp
		HashMap<SimpleNode, SimpleNode> tmp = new HashMap<SimpleNode, SimpleNode>(subTree1.getSize());

		// i <- m
		int i = m;
		// j <- n
		int j = n;

		// Find the actual alignment between the subtrees
		// while i > 0 and j > 0 do
		while (i > 0 && j > 0) {
			// v1 <- (i-1)-th element in subtree1
			SimpleNode i1 = subtreePreOrd1.get(i - 1);
			// v2 <- (j-1)-th element in subtree2
			SimpleNode j1 = subtreePreOrd2.get(j - 1);

			SimpleNode p1 = i1.getParent();
			SimpleNode p2 = j1.getParent();

			// if(label(v1) = label(v2)) and ((label(parent(p1) =
			// label(parent(p2)) or label(v1) = BLOCK)
			if ((i1.getId() == j1.getId() && ((p1.getId() == p2.getId()))
					|| i1.getId() == JavaParserTreeConstants.JJTBLOCK)) {
				// tmp[v1] <- v2
				tmp.put(i1, j1);

				// i <- i - 1
				i--;

				// j <- j - 1
				j--;

				// else if (c(i,j-1) > c(i-1, j))
			} else if (c[i][j - 1] > c[i - 1][j]) {
				// j <- j - 1
				j--;
			} else
				// i <- i - 1
				i--;
		}

		// result <- 0
		int result = 0;

		// For the final alignments
		// Map <Node, Node> alignment
		HashMap<SimpleNode, SimpleNode> alignment = new HashMap<SimpleNode, SimpleNode>(subTree1.getSize());

		// for all v in subtree1 do
		for (SimpleNode node : subtreePreOrd1) {
			// if tmp[v] /= nil then
			if (tmp.containsKey(node)) {
				// w <- tmp[v]
				SimpleNode w = tmp.get(node);

				// if v = r1 then
				if (node == subTree1) {
					// alignment[v] <- w
					alignment.put(node, w);
				} else {
					// p1 <- parent(v)
					SimpleNode p1 = node.getParent();
					// p1 <- parent(W)
					SimpleNode p2 = w.getParent();

					// if alignment[p1] = p2 then
					if (alignment.containsKey(p1) && p2 == alignment.get(p1)) {
						// alignment[v] <- w
						alignment.put(node, w);
						// label(v) = BLOCK
					} else if (node.getId() == JavaParserTreeConstants.JJTBLOCK) {

						SimpleNode[] nodes = node.getChildren();

						// nodes must not be empty
						if (nodes != null) {
							// for all v1 in children(v) do
							for (SimpleNode node1 : nodes) {
								// if tmp[v1] /= nil then
								if (tmp.containsKey(node1)) {
									// w1 <- tmp[v1]
									SimpleNode w1 = tmp.get(node1);

									// if parent(w1) = w then
									if (w1 == w.getParent()) {
										// alighment[v] <- w
										alignment.put(node, w);
										break;
									}
								}
							}
						}
					}

				}
				// if alignment[v] = w then
				if (alignment.containsKey(node)) {
					// List <Node> List
					ArrayList<SimpleNode> list;

					// if B[v] /= nil then
					if (hashMapB.containsKey(node)) {
						// list <- B[v]
						list = hashMapB.get(node);
					} else {
						list = new ArrayList<SimpleNode>(100);
					}

					// Insert alignment[v] into list
					list.add(w);
					// B[v] <- list
					hashMapB.put(node, list);

					// result <- result + 1
					result += 1;
				}
			}
		}
		return result;
	}

	private float sim(int size1, int size2, int s) {
		System.out.println(s);
		return (float) (2 * s) / (float) (size1 + size2);
	}
}
