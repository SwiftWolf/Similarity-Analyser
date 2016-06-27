package LCS;

public class Edge {
	private Vertex from;
	private Vertex to;
	private int cost;
	
	public Edge(Vertex from, Vertex to, int cost) {
		this.from = from;
		this.to = to;
		this.cost = cost;
	}

	public Edge(Vertex from, Vertex to) {
		this.from = from;
		this.to = to;
	}

	public String toString() {
		return null;
	}

	public Vertex getFrom() {
		return from;
	}

	public Vertex getTo() {
		return to;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	public int getCost() {
		return cost;
	}
	
	public void reverseEdge(){
        if(from.removeEdge(this)){
            to.addEdge(this);
           
            Vertex tmp = from;
            from = to;
            to = tmp;
        }
    }
}
