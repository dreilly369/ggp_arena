package org.ggp.base.player.gamer.statemachine.sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GameGraph {
	private int V = 0;
	private int E = 0;
	private Map<Integer, ArrayList<Integer>> adj;

	public GameGraph(int v){
		//Create an empty graph with v vertices
		HashMap<Integer, ArrayList<Integer>> map = new HashMap<Integer,ArrayList<Integer>>();
		for(int i=0;i<v;i++){
			ArrayList<Integer> empty = new ArrayList<Integer>();
			map.put(i, empty);
		}
		this.adj = map;
		this.V = v;
	}

	public GameGraph(int[] v){
		//Create an empty graph with v vertices
		HashMap<Integer, ArrayList<Integer>> map = new HashMap<Integer,ArrayList<Integer>>();
		for(int i=0;i<v.length;i++){
			ArrayList<Integer> empty = new ArrayList<Integer>();
			map.put(v[i], empty);
		}
		this.adj = map;
		this.V = v.length;
	}

	public void addVertex(int vertex){
		ArrayList<Integer> empty = new ArrayList<Integer>();
		this.adj.put(vertex, empty);
		this.V = this.adj.size();
	}

	public void addAllVertices(int[] vertices){
		HashMap<Integer, ArrayList<Integer>> map = new HashMap<Integer,ArrayList<Integer>>();
		for(int i=0;i<vertices.length;i++){
			ArrayList<Integer> empty = new ArrayList<Integer>();
			map.put(vertices[i], empty);
		}
		this.adj = map;
		this.V = this.adj.size();
	}

	public int degree(int v){
		return this.getAdj(v).size();
	}

	public int size() {
		return this.V;
	}

	public boolean addEdge(int v, int w) {
		if(v > this.V || w > this.V){
			return false;
		}

		ArrayList<Integer> vs = this.adj.get(v);
		if(!vs.contains(w)){
			vs.add(w);
			this.adj.put(v, vs);
			this.E++;
		}
		return true;
	}

	public boolean addNewVerticesWithEdgesToParent(int p, int[] w) {
		if(!adj.containsKey(p)){
			throw new IndexOutOfBoundsException();
		}
		ArrayList<Integer> vs = this.adj.get(p);
		//For each new vertex add it to the list and add an edge from it's parent
		for(int i=0;i<w.length;i++){
			this.addVertex(w[i]);//Adding it to the adjacency map
			ArrayList<Integer> ws = this.adj.get(w[i]);//Should be empty
			if(!vs.contains(w[i])){//It shouldn't but for safety
				vs.add(w[i]);
				this.adj.put(p, vs);
				this.addEdge(p, w[i]);
				this.E++;
			}
			if(!ws.contains(p)){//This should run every time
				ws.add(p);
				this.adj.put(w[i], ws);
				this.E++;
			}
		}
		return true;
	}

	public boolean addEdges(int p, int[] w) {
		if(!adj.containsKey(p)){
			throw new IndexOutOfBoundsException();
		}

		ArrayList<Integer> vs = this.adj.get(p);
		for(int i=0;i<w.length;i++){
			ArrayList<Integer> ws = this.adj.get(w[i]);
			if(!vs.contains(w[i])){
				vs.add(w[i]);
				this.adj.put(p, vs);
				this.E++;
			}
			if(!ws.contains(p)){
				ws.add(p);
				this.adj.put(w[i], ws);
				this.E++;
			}
		}

		return true;
	}

	public ArrayList<Integer> getAdj(int v) {
		// TODO Auto-generated method stub
		return this.adj.get(v);
	}

	public int maxDegree() {
		int mxSeen = 0;
		for(int i = 0; i < this.adj.size();i++){
			int curr = this.adj.get(i).size();
			if(curr > mxSeen){
				mxSeen = curr;
			}
		}
		return mxSeen;
	}

	public int avgDegree() {
		return this.E/this.V;
	}

	public int countSelfLoops() {
		// TODO Auto-generated method stub
		int count = 0;
		for(Integer v : this.adj.keySet()){
			ArrayList<Integer> neighbors = this.getAdj(v);
			for(Integer n : neighbors){
				if(n == v){
					count++;
				}
			}
		}
		return count;
	}

	public boolean selfLoops(int v) {
		ArrayList<Integer> neighbors = this.getAdj(v);
		for(Integer n : neighbors){
			if(n == v){
				return true;
			}
		}
		return false;
	}

	public boolean hasPathD(int v, int x, ArrayList<Integer> visited) {
		//Depth First search implementation for finding any path between v and x
		visited.add(v);
		ArrayList<Integer> neighbors = this.getAdj(v);
		if(neighbors.contains(x)){
			return true;
		}

		boolean path = false;
		//Recursively check the neighbors
		for(Integer step : neighbors){
			if(!visited.contains(step)){
				path = hasPathD(step, x, visited);
			}
		}
		return path;
	}

	public int getV() {
		return this.V;
	}

	public int getE() {
		return this.E;
	}
}
