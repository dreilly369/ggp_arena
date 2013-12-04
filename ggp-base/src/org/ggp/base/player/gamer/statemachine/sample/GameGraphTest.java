package org.ggp.base.player.gamer.statemachine.sample;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;

public class GameGraphTest {

	@Test
	public void test_ConstructGraph_ReturnGraphOfSizeV() {
		GameGraph graph = new GameGraph(10);
		assertEquals(10, graph.size());
	}

	@Test
	public void test_addEdge_ReturnTrue() {
		GameGraph graph = new GameGraph(10);
		int v = 0;
		int w =1;
		assertEquals(true, graph.addEdge(v,w));
	}

	@Test
	public void test_addEdge_IndexOutOfBOunds_returnFalse() {
		GameGraph graph = new GameGraph(10);
		int v = 0;
		int w =12;//Out of bounds
		//graph.addEdge(v,w);
		assertEquals(false, graph.addEdge(v,w));
	}

	@Test
	public void test_getAdj_ReturnArrayListInts() {
		GameGraph graph = new GameGraph(10);
		int v = 0;
		ArrayList<Integer> right = new ArrayList<Integer>();
		ArrayList<Integer> neighbors = graph.getAdj(v);
		assertEquals(right.getClass(), neighbors.getClass());
	}

	@Test
	public void test_addEdge_AdjIncludesNewEdge() {
		GameGraph graph = new GameGraph(10);
		int v = 0;
		int w = 1;
		graph.addEdge(v, w);
		ArrayList<Integer> neighbors = graph.getAdj(v);
		assertEquals(true, neighbors.contains(w));
	}

	@Test
	public void test_addEdge_OppositeAdjIncludesNewEdge() {
		GameGraph graph = new GameGraph(10);
		int v = 0;
		int w = 1;
		graph.addEdge(v, w);
		ArrayList<Integer> neighbors = graph.getAdj(w);
		assertEquals(true, neighbors.contains(v));
	}

	@Test
	public void test_addEdgeReversedOrder_AdjIncludesNewEdge() {
		GameGraph graph = new GameGraph(10);
		int v = 0;
		int w = 1;
		graph.addEdge(w, v);
		ArrayList<Integer> neighbors = graph.getAdj(w);
		assertEquals(true, neighbors.contains(v));
	}

	@Test
	public void test_addEdgeDuplicate_AdjStaysSame() {
		GameGraph graph = new GameGraph(10);
		int v = 0;
		int w = 1;

		graph.addEdge(v, w);
		int neighborCount1 = graph.getAdj(v).size();
		graph.addEdge(v, w);
		int neighborCount2 = graph.getAdj(v).size();
		assertEquals(neighborCount1, neighborCount2);
	}

	@Test
	public void test_degree_ReturnOne() {
		GameGraph graph = new GameGraph(10);
		int v = 0;
		int w = 1;
		graph.addEdge(v, w);
		int degree = graph.degree(v);
		assertEquals(1, degree);
	}

	@Test
	public void test_degree_ReturnThree() {
		GameGraph graph = new GameGraph(10);
		int v = 0;
		int w = 1;
		int x = 2;
		int y = 3;
		graph.addEdge(v, w);
		graph.addEdge(v, x);
		graph.addEdge(v, y);
		int degree = graph.degree(v);
		assertEquals(3, degree);
	}

	@Test
	public void test_maxDegree_ReturnThree() {
		GameGraph graph = new GameGraph(10);
		int v = 0;
		int w = 1;
		int x = 2;
		int y = 3;
		int z = 0;
		int a = 1;
		int b = 2;
		int c = 3;
		graph.addEdge(v, w);
		graph.addEdge(v, x);
		graph.addEdge(v, y);
		graph.addEdge(w, x);
		graph.addEdge(w, v);
		graph.addEdge(a, c);
		int degree = graph.maxDegree();
		assertEquals(3, degree);
	}

	@Test
	public void test_avgDegree_ReturnAvg() {
		GameGraph graph = new GameGraph(10);
		int v = 0;
		int w = 1;
		int x = 2;
		int y = 3;
		int z = 4;
		int a = 5;
		int b = 6;
		int c = 7;
		int assumed = (3+3+3+2+2+1) / 10;
		graph.addEdge(v, w);// ///
		graph.addEdge(v, x);// ///
		graph.addEdge(v, y);// ///
		graph.addEdge(w, z);// //
		graph.addEdge(w, c);// //
		graph.addEdge(a, b);// /
		int degree = graph.avgDegree();
		assertEquals(assumed, degree);
	}

	@Test
	public void test_countSelfLoops_ReturnTwo() {
		GameGraph graph = new GameGraph(10);
		int v = 0;
		int w = 1;
		int x = 2;
		int y = 3;
		graph.addEdge(v, w);
		graph.addEdge(v, v);
		graph.addEdge(w, w);
		int loops = graph.countSelfLoops();
		assertEquals(2, loops);
	}

	@Test
	public void test_countSelfLoops_ReturnZero() {
		GameGraph graph = new GameGraph(10);
		int v = 0;
		int w = 1;
		int x = 2;
		int y = 3;
		graph.addEdge(v, w);
		int loops = graph.countSelfLoops();
		assertEquals(0, loops);
	}

	@Test
	public void test_selfLoops_ReturnFalse() {
		GameGraph graph = new GameGraph(10);
		int v = 0;
		int w = 1;
		int x = 2;
		int y = 3;
		graph.addEdge(v, w);
		boolean loops = graph.selfLoops(v);
		assertEquals(false, loops);
	}

	@Test
	public void test_selfLoops_ReturnsTrue() {
		GameGraph graph = new GameGraph(10);
		int v = 0;
		int w = 1;
		int x = 2;
		int y = 3;
		graph.addEdge(v, w);
		graph.addEdge(v, v);
		boolean loops = graph.selfLoops(v);
		assertEquals(true, loops);
	}

	@Test
	public void test_hasPathD_pathExists_ReturnsTrue() {
		GameGraph graph = new GameGraph(10);
		int v = 0;
		int w = 1;
		int x = 2;
		graph.addEdge(v, w);
		graph.addEdge(w, x);
		ArrayList<Integer> visited = new ArrayList<Integer>();
		boolean path = graph.hasPathD(v,x,visited);
		assertEquals(true, path);
	}

	@Test
	public void test_hasPathD_pathNotExist_ReturnsFalse() {
		GameGraph graph = new GameGraph(10);
		int v = 0;
		int w = 1;
		int x = 2;
		int y = 3;

		graph.addEdge(v, w);
		graph.addEdge(w, y);

		ArrayList<Integer> visited = new ArrayList<Integer>();
		boolean path = graph.hasPathD(v,x,visited);
		assertEquals(false, path);
	}


	@Test
	public void test_hasPathD_longPathExist_ReturnsTrue() {
		GameGraph graph = new GameGraph(10);
		int v = 0;
		int w = 1;
		int x = 2;
		int y = 3;
		int z = 0;
		int a = 1;
		int b = 2;
		int c = 3;
		graph.addEdge(v, w);
		graph.addEdge(w, x);
		graph.addEdge(x, y);
		graph.addEdge(y, z);
		graph.addEdge(z, c);
		graph.addEdge(c, b);
		graph.addEdge(c, a);
		ArrayList<Integer> visited = new ArrayList<Integer>();
		boolean path = graph.hasPathD(v,a,visited);
		assertEquals(true, path);
	}

	@Test
	public void test_GameGraph_sendArray_ReturnsGraph() {
		int[] hashes = new int[5];
		hashes[0] = 10;
		hashes[1] = 15;
		hashes[2] = 16;
		hashes[3] = 18;
		hashes[4] = 20;
		GameGraph graph = new GameGraph(hashes);
		assertEquals(5, graph.getV());
	}

	@Test
	public void test_addVertex_sendInt_graphVIncreases() {
		GameGraph graph = new GameGraph(10);
		int s1 = graph.getV();
		graph.addVertex(130);
		int s2 = graph.getV();
		assertEquals(s1, s2-1);
	}

	@Test
	public void test_addEdges_sendIntArray_graphEIncreases() {
		int v = 0;
		int w = 1;
		int x = 2;
		int y = 3;

		int[] test = new int[4];
		test[0] = 7;
		test[1] = 8;
		test[2] = 6;
		test[3] = 5;

		GameGraph graph = new GameGraph(10);
		graph.addEdge(v, w);
		graph.addEdge(w, x);
		graph.addEdge(x, y);
		int s1 = graph.getE();
		graph.addEdges(w,test);
		int s2 = graph.getE();
		assertEquals(s1+(test.length*2), s2);
	}


}
