package LCS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

// See https://people.mpi-inf.mpg.de/~mehlhorn/LEDAbook.html chapter 7 pp.144 
public class BipartiteMatching {

	/*
	 * list<edge> MAX_WEIGHT_BIPARTITE_MATCHING_T(graph& G, const list<node>& A,
	 * const list<node>& B,const edge_array<NT>& c, node_array<NT>& pot) (?)
	 */
	public ArrayList<Edge> maxWeightBipartieMatching(ArrayList<Vertex> A, ArrayList<Vertex> B, ArrayList<Edge> c,
			int treeSize1, int treeSize2) {

		// list<edge> result
		ArrayList<Edge> result = new ArrayList<Edge>(treeSize2);

		// node_array<bool> free
		HashMap<Vertex, Boolean> free = new HashMap<Vertex, Boolean>(treeSize1 + treeSize2);
		// node_array<edge> pred(G,nil)
		HashMap<Vertex, Edge> pred = new HashMap<Vertex, Edge>(treeSize1 + treeSize2);
		// node_array<NT> dist(G,0);
		HashMap<Vertex, Integer> dist = new HashMap<Vertex, Integer>(treeSize1 + treeSize2);
		// node_array<NT> pot)
		HashMap<Vertex, Integer> pot = new HashMap<Vertex, Integer>(treeSize1 + treeSize2);

		// node_pq<NT> PQ(G);
		FibHeap PQ = new FibHeap(treeSize1);

		// Initialize
		for (Vertex a : A) {
			// if (free[a]) augment(G,a,c,pot,free,pred,dist,PQ); (?)
			free.put(a, true); // a is free.
			pred.put(a, (Edge) null); // No predecessor
			dist.put(a, 0); // distance is zero
			pot.put(a, 0); // potential is zero
		}

		// Initialize
		for (Vertex b : B) {
			free.put(b, true);
			pred.put(b, (Edge) null);
			dist.put(b, 0);
			pot.put(b, 0);
		}

		int C = 0;

		// forall_edges(e,G)
		for (Edge e : c) {
			// if (c[e] > C)
			if (e.getCost() > C) {
				// C = c[e]
				C = e.getCost();
			}
		}

		// forall(a,A)
		for (Vertex a : A) {
			// pot[a] = C
			pot.put(a, C);
		}
		// forall(a,A)
		for (Vertex a : A) {
			// if (free[a])
			if (free.get(a)) {
				// augment(G,a,c,pot,free,pred,dist,PQ);
				augment(a, pot, free, pred, dist, PQ);
			}
		}

		// forall(b,B)
		for (Vertex b : B) {
			// forall_out_edges(e,b)
			Edge e = b.getFirstAdjacent();

			if (e != null) {
				// result.append(e);
				result.add(e);
			}
		}

		// forall(e,result)
		for (Edge e : result) {
			// G.rev_edge(e);
			e.reverseEdge();
		}
		return result;

	}

	// inline void augment(graph& G, node a, const edge_array<NT>& c,
	// node_array<NT>& pot, node_array<bool>& free, node_array<edge>& pred,
	// node_array<NT>& dist, node_pq<NT>& PQ)
	public void augment(Vertex a, HashMap<Vertex, Integer> pot, HashMap<Vertex, Boolean> free,
			HashMap<Vertex, Edge> pred, HashMap<Vertex, Integer> dist, FibHeap PQ) {

		// dist[a] = 0;
		dist.put(a, 0);

		// node best_node_in_A = a
		Vertex bestNodeInA = a;

		// NT minA = pot[a];
		int minA = pot.get(a);

		// NT Delta;
		int delta;

		// stack<node> RA
		Stack<Vertex> RA = new Stack<Vertex>();

		// RA.push(a)
		RA.push(a);

		// stack<node> RB;
		Stack<Vertex> RB = new Stack<Vertex>();

		// node a1 = a
		Vertex a1 = a;

		// relax all edges out of a1
		ArrayList<Edge> adjEdges = a1.getAdjList();

		// if ( adj_edges == nil )
		if (adjEdges == null) {
			return;
		}

		// forall_adj_edges(e,a1)
		for (Edge e : adjEdges) {

			// node b = G.target(e)
			Vertex b = e.getTo();

			// NT db = dist[a1] + (pot[a1] + pot[b] - c[e])
			int db = dist.get(a1) + (pot.get(a1) + pot.get(b) - e.getCost());

			// dist[b] = db
			dist.put(b, db);

			// pred[b] = e
			pred.put(b, e);

			// RB.push(b);
			RB.push(b);

			// PQ.insert(b,db);
			PQ.insert(b, db);
		}

		while (true) {
			// select from PQ the node b with minimal distance db

			// node b
			Vertex b;

			// NT db
			int db = 0;

			// if (PQ.empty())
			if (PQ.empty()) {
				// b = nil;
				b = null;
			} else {
				// b = PQ.del_min();
				b = (Vertex) PQ.extractMin();
				// db = dist[b]
				db = dist.get(b);
			}

			// distinguish three cases
			// if ( b == nil || db >= minA )
			if (b == null || db >= minA) {
				// Delta = minA;
				delta = minA;

				// augmentation by path to best node in A
				augmentPathToBestNodeInA(bestNodeInA, pred);

				free.put(a, false);
				free.put(bestNodeInA, true);
				
				// The holy break
				break;
			} else {
				// if ( free[b] )
				if (free.get(b)) {

					// Delta = db;
					delta = db;

					// augmentation by path to b
					augmentPathToBestNodeInA(b, pred);
					free.put(a, false);
					free.put(b, false);

					break;

				} else {
					// continue shortest-path computation
					// e = G.first_adj_edge(b);
					Edge e = b.getFirstAdjacent();

					// node a1 = G.target(e);
					a1 = e.getTo();

					// pred[a1] = e;
					pred.put(a1, e);

					// RA.push(a1);
					RA.push(a1);

					// dist[a1] = db;
					dist.put(a1, db);

					// if (db + pot[a1] < minA)
					if (db + pot.get(a1) < minA) {
						// best_node_in_A = a1;
						bestNodeInA = a1;

						// minA = db + pot[a1];
						minA = db + pot.get(a1);
					}

					// relax all edges out of a1
					adjEdges = a1.getAdjList();

					// forall_adj_edges(e1,a1)
					for (Edge e1 : adjEdges) {
						// node b = G.target(e);
						b = e1.getTo();

						// NT db = dist[a1] + (pot[a1] + pot[b] - c[e1]);
						db = dist.get(a1) + (pot.get(a1) + pot.get(b) - e1.getCost());

						// if ( pred[b] == nil )
						if (pred.get(b) == null) {

							// dist[b] = db;
							dist.put(b, db);

							// pred[b] = e1;
							pred.put(b, e1);

							// RB.push(b);
							RB.push(b);

							// PQ.insert(b,db);
							PQ.insert(b, db);
						} else {
							// if ( db < dist[b] )
							if (db < dist.get(b)) {
								// dist[b] = db;
								dist.put(b, db);

								// pred[b] = e1;
								pred.put(b, e1);
								// PQ.decrease_p(b,db);
								PQ.decreaseKey(b, db);

							}
						}
					}
				}
			}
		}
		//-------------------REMOVE
		// augment: potential update and reinit

				// Update the potential and remove the nodes from the stack
				while (!RA.empty()) {
					Vertex tmp = RA.pop();
					pred.put(tmp, null);
					int potChange = delta - dist.get(tmp);

					if (potChange <= 0) {
						continue;
					}

					pot.put(tmp, (pot.get(tmp) - potChange));

				}

				// Update the potential and remove the nodes from the stack.
				while (!RB.empty()) {
					Vertex tmp = RB.pop();
					pred.put(tmp, (Edge) null);
					
					// if b is in the heap
					if (PQ.member(tmp)) {
						PQ.delete(tmp);
					}

					int potChange = delta - dist.get(tmp);

					if (potChange <= 0) {
						continue;
					}

					pot.put(tmp, (pot.get(tmp) + potChange));

				}

		
		//----------------------REMOVE------------------
		

	}

	// inline void augment_path_to(graph& G, node v,const node_array<edge>&
	// pred)
	public void augmentPathToBestNodeInA(Vertex v, HashMap<Vertex, Edge> pred) {
		// edge e = pred[v];
		Edge e = pred.get(v);

		// while (e)
		while (e != null) {
			// G.rev_edge(e);
			e.reverseEdge();
			// e = pred[G.target(e)]; // not source (!!!)
			e = pred.get(e.getTo());
		}
	}
}
