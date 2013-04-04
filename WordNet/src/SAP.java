import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: et33027
 * Date: 3/28/13
 * Time: 5:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class SAP {

    private Digraph dg;
    private BreadthFirstDirectedPaths bfs;
    private BreadthFirstDirectedPaths bfsR;
    private Integer vertexAmongSet;
    private Integer vertexAmongMultipleSet;

    // constructor takes a digraph (not necessarily a DAG)
    public SAP(Digraph G) {
        this.dg = new Digraph(G);
    }

    // length of shortest ancestral path between v and w; -1 if no such path
    public int length(int v, int w) {
        if (v < 0 || v >= this.dg.V() || w < 0 || w >= this.dg.V()) throw new IndexOutOfBoundsException();
        if (v == w)
            return 0;

        bfs = new BreadthFirstDirectedPaths(this.dg, v);
        bfsR = new BreadthFirstDirectedPaths(this.dg, w);

        int minLength = -1;
        for (int i=0; i<dg.V(); i++) {
            if (bfs.hasPathTo(i) && bfsR.hasPathTo(i)) {
                if (minLength == -1) {
                    minLength = bfs.distTo(i) + bfsR.distTo(i);
                    vertexAmongSet = i;
                }
                else {
                    if (bfs.distTo(i) + bfsR.distTo(i) < minLength){
                        minLength = bfs.distTo(i) + bfsR.distTo(i);
                        vertexAmongSet = i;
                    }
                }
            }
        }

        return minLength;
    }

    // a common ancestor of v and w that participates in a shortest ancestral path; -1 if no such path
    public int ancestor(int v, int w) {
        if (v < 0 || v >= this.dg.V() || w < 0 || w >= this.dg.V()) throw new IndexOutOfBoundsException();
        if (v == w)
            return v;
        bfs = new BreadthFirstDirectedPaths(this.dg,v);
        if (length(v, w) != -1) {
            return vertexAmongSet;
        }

        return -1;
    }

    // length of shortest ancestral path between any vertex in v and any vertex in w; -1 if no such path
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        for (Integer a : v) {
            if (a < 0 || a >= this.dg.V()) throw new IndexOutOfBoundsException();
        }
        for (Integer b : w) {
            if (b < 0 || b >= this.dg.V()) throw new IndexOutOfBoundsException();
        }

        for (Integer c:v) {
            for (Integer d:w){
                if (c == d) {
                    return 0;
                }
            }
        }

        bfs = new BreadthFirstDirectedPaths(this.dg,v);
        bfsR = new BreadthFirstDirectedPaths(this.dg, w);

        int minLength = -1;
        for (int i=0; i<dg.V(); i++) {
            if (bfs.hasPathTo(i) && bfsR.hasPathTo(i)) {
                if (minLength == -1) {
                    minLength = bfs.distTo(i) + bfsR.distTo(i);
                    vertexAmongMultipleSet = i;
                }
                else {
                    if (bfs.distTo(i) + bfsR.distTo(i) < minLength){
                        minLength = bfs.distTo(i) + bfsR.distTo(i);
                        vertexAmongMultipleSet = i;
                    }
                }
            }
        }
        return minLength;
    }

    // a common ancestor that participates in shortest ancestral path; -1 if no such path
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        for (Integer a : v) {
            if (a < 0 || a >= this.dg.V()) throw new IndexOutOfBoundsException();
        }
        for (Integer b : w) {
            if (b < 0 || b >= this.dg.V()) throw new IndexOutOfBoundsException();
        }
        for (Integer c:v) {
            for (Integer d:w){
                if (c == d) {
                    return c;
                }
            }
        }
        bfs = new BreadthFirstDirectedPaths(this.dg,v);
        if (length(v, w) != -1) {
            return vertexAmongMultipleSet;
        }

        return -1;
    }

    // for unit testing of this class (such as the one below)
    public static void main(String[] args) {
        In in = new In("testData/digraph3.txt");
        Digraph G = new Digraph(in);
        SAP sap = new SAP(G);
        List<Integer> v = new ArrayList<Integer>();
        v.add(13);
        v.add(14);
        List<Integer> w = new ArrayList<Integer>();
        w.add(7);
        w.add(9);
        int length = sap.length(v,w);
        int ancestor = sap.ancestor(v, w);
        StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
//        while (!StdIn.isEmpty()) {
//            int v = StdIn.readInt();
//            int w = StdIn.readInt();
//            int length   = sap.length(v, w);
//            int ancestor = sap.ancestor(v, w);
//            StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
//        }
    }
}
