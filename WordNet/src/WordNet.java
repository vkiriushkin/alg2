import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: et33027
 * Date: 3/28/13
 * Time: 5:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class WordNet {

    private Digraph hDigraph;
    private ST<String, List<String>> synSet;

    // constructor takes the name of the two input files
    public WordNet(String synsets, String hypernyms) {

        int verticesWithZeroOutDegree = 0;
        //read synsets file and fill array
        In synIn = new In(synsets);
        synSet = new ST<String, List<String>>();
        int index = 0;
        while (synIn.hasNextLine()) {
            List<String> params = Arrays.asList(synIn.readLine().split("\\,"));
            for (String key : params.get(1).split("\\s+")) {
                if (synSet.contains(key))
                    synSet.get(key).add(params.get(0));
                else {
                    List<String> values = new ArrayList<String>();
                    values.add(String.valueOf(index));
                    values.add(params.get(1));
                    values.add(params.get(0));
                    synSet.put(key,values);
                }
            }
            index++;
        }

        //read hypernums file and create Digraph
        In hypIn = new In(hypernyms);
        hDigraph = new Digraph(synSet.size());
        while (hypIn.hasNextLine()) {
            List<String> connectedVertices = Arrays.asList(hypIn.readLine().split("\\,"));
            for (int i=1; i<connectedVertices.size(); i++) {
                hDigraph.addEdge(Integer.parseInt(connectedVertices.get(0)),Integer.parseInt(connectedVertices.get(i)));
            }
        }


        DirectedCycle dc = new DirectedCycle(hDigraph);
        for (int v=0; v<hDigraph.V();v++) {
            if (!hDigraph.adj(v).iterator().hasNext()){
                verticesWithZeroOutDegree++;
            }
        }
        if (!dc.hasCycle() && verticesWithZeroOutDegree !=1)
            throw new IllegalArgumentException();
    }

    // returns all WordNet nouns
    public Iterable<String> nouns() {
        return synSet.keys();
    }

    // is the word a WordNet noun?
    public boolean isNoun(String word) {
        return synSet.contains(word);
    }

    // distance between nounA and nounB (defined below)
    public int distance(String nounA, String nounB) {
        if (!isNoun(nounA) || !isNoun(nounB))
            throw new IllegalArgumentException();
        int nounAId = -1;
        int nounBId = -1;

        //find ids for nounA and nounB inside list of synset
        List<String> nounASynsetValues = synSet.get(nounA);
        List<String> nounBSynsetValues = synSet.get(nounB);

        List<Integer> nounASynsetIds = new ArrayList<Integer>();
        for (int a=2;a<nounASynsetValues.size();a++) {
            nounASynsetIds.add(Integer.parseInt(nounASynsetValues.get(a)));
        }
        List<Integer> nounBSynsetIds = new ArrayList<Integer>();
        for (int a=2;a<nounBSynsetValues.size();a++) {
            nounBSynsetIds.add(Integer.parseInt(nounBSynsetValues.get(a)));
        }

        SAP sap = new SAP(hDigraph);
        int distance;
        if (nounASynsetIds.size() == 1 && nounBSynsetIds.size() == 1) {
            distance = sap.length(nounASynsetIds.get(0),nounBSynsetIds.get(0));
        } else {
            if (nounASynsetIds.size() > nounBSynsetIds.size())
                distance = sap.length(nounASynsetIds, nounBSynsetIds);
            else
                distance = sap.length(nounBSynsetIds, nounASynsetIds);
        }
        return distance;
    }

    // a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
    // in a shortest ancestral path (defined below)
    public String sap(String nounA, String nounB) {
        if (!isNoun(nounA) || !isNoun(nounB))
            throw new IllegalArgumentException();

        //find ids for nounA and nounB inside list of synset
        List<String> nounASynsetValues = synSet.get(nounA);
        List<String> nounBSynsetValues = synSet.get(nounB);

        List<Integer> nounASynsetIds = new ArrayList<Integer>();
        for (int a=2;a<nounASynsetValues.size();a++) {
            nounASynsetIds.add(Integer.parseInt(nounASynsetValues.get(a)));
        }
        List<Integer> nounBSynsetIds = new ArrayList<Integer>();
        for (int a=2;a<nounBSynsetValues.size();a++) {
            nounBSynsetIds.add(Integer.parseInt(nounBSynsetValues.get(a)));
        }

        SAP sap = new SAP(hDigraph);
        int ancestor;
        if (nounASynsetIds.size() == 1 && nounBSynsetIds.size() == 1) {
            ancestor = sap.ancestor(nounASynsetIds.get(0),nounBSynsetIds.get(0));
        } else {
            ancestor = sap.ancestor(nounASynsetIds, nounBSynsetIds);
        }

        for (String s:synSet.keys()) {
            if (Integer.parseInt(synSet.get(s).get(0)) == ancestor) {
                return synSet.get(s).get(1);
            }
        }

        return null;
    }

    // for unit testing of this class
    public static void main(String[] args) {
        WordNet wn = new WordNet("testData/synsets3.txt","testData/hypernymsInvalidTwoRoots.txt");
        Stopwatch sw = new Stopwatch();
//        System.out.println("Digraph size:" + wn.hDigraph.V());
//        System.out.println("List size:" + wn.synSet.size());
//        System.out.println("Set of nouns size:" + wn.nounsSet.size());
//        System.out.println("***Testing if nouns exist***");
//        System.out.println("Checking: 'shoulder_holster'. Expected: true. Actual: " + wn.isNoun("shoulder_holster"));
//        System.out.println("Checking: 'fashioning'. Expected: true. Actual: " + wn.isNoun("fashioning"));
//        System.out.println("Checking: 'wheatfield'. Expected: true. Actual: " + wn.isNoun("wheatfield"));
//        System.out.println("Checking: 'hipopotam'. Expected: false. Actual: " + wn.isNoun("hipopotam"));
//        System.out.println("Checking: 'vkiriushkin'. Expected: false. Actual: " + wn.isNoun("vkiriushkin"));
//        for (int i=0; i<200; i++) {
//            int random1 = StdRandom.uniform(0,wn.hDigraph.V());
//            int random2 = StdRandom.uniform(0,wn.hDigraph.V());
//            List<String> keys = new ArrayList<String>();
//            for (String s: wn.synSet.keys())
//                keys.add(s);
//            String nounA = keys.get(random1);
//            String nounB = keys.get(random2);
//            wn.distance(nounA,nounB);
//            wn.sap(nounA,nounB);
////            System.out.println("Distance between "+nounA+" and "+nounB+":"+wn.distance(nounA,nounB));
//        }

        String nounA = "bola_tie";
        String nounB = "Wiesenthal";

        System.out.println(wn.distance(nounA,nounB));
        System.out.println(wn.sap(nounA,nounB));
        System.out.println(sw.elapsedTime());
    }
}
