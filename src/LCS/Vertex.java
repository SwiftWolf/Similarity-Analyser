package LCS;

import java.util.ArrayList;

public class Vertex {

	int size = 0;
	ArrayList<Edge> adjacency = null;

	public Vertex(int size) {
		this.size = size;
	}

	public Edge addEdge(Vertex to) {
		if (adjacency == null) {
			adjacency = new ArrayList<Edge>(size);
		}
		Edge edge = new Edge(this, to);
		adjacency.add(edge);
		return edge;
	}

	public Edge addEdge(Edge edge) {
		if (adjacency == null) {
			adjacency = new ArrayList<Edge>(size);
		}
		adjacency.add(edge);

		return edge;
	}

	public boolean removeEdge(Edge edge) {
		if (adjacency != null) {
			return adjacency.remove(edge);
		}
		return false;
	}

	public ArrayList<Edge> getAdjList() {
		return adjacency;
	}

	public Edge getFirstAdjacent() {
		if (adjacency != null && adjacency.size() != 0)
			return adjacency.get(0);

		return null;
	}
}
