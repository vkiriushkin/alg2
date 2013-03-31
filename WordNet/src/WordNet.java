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
    private List<List<String>> synList;
    private Set<String> nounsSet;

    // constructor takes the name of the two input files
    public WordNet(String synsets, String hypernyms) {
        boolean isRooted = false;
        nounsSet = new TreeSet<String>();

        //read synsets file and fill array
        In synIn = new In(synsets);
        synList = new ArrayList<List<String>>();
        while (synIn.hasNextLine()) {
            List<String> params = Arrays.asList(synIn.readLine().split("\\,"));
            synList.add(params);
            nounsSet.addAll(Arrays.asList(params.get(1).split("\\s+")));
        }

        //read hypernums file and create Digraph
        In hypIn = new In(hypernyms);
        hDigraph = new Digraph(synList.size());
        while (hypIn.hasNextLine()) {
            List<String> connectedVertices = Arrays.asList(hypIn.readLine().split("\\,"));
            if (connectedVertices.size() == 1)
                isRooted = true;

            for (int i=1; i<connectedVertices.size(); i++) {
                hDigraph.addEdge(Integer.parseInt(connectedVertices.get(0)),Integer.parseInt(connectedVertices.get(i)));
            }
        }

        if (!isRooted)
            throw new IllegalArgumentException();
    }



    // returns all WordNet nouns
    public Iterable<String> nouns() {
        return nounsSet;
    }

    // is the word a WordNet noun?
    public boolean isNoun(String word) {
        return nounsSet.contains(word);
    }

    // distance between nounA and nounB (defined below)
    public int distance(String nounA, String nounB) {
        if (!isNoun(nounA) || !isNoun(nounB))
            throw new IllegalArgumentException();
        int nounAId = -1;
        int nounBId = -1;

        //find ids for nounA and nounB inside list of synset
        List<Integer> nounASynsetIds = new ArrayList<Integer>();
        List<Integer> nounBSynsetIds = new ArrayList<Integer>();
        for (int i=0; i<synList.size(); i++) {
            List<String> synset = Arrays.asList(synList.get(i).get(1).split("\\s+"));
            for (String s : synset) {
                if (s.equals(nounA))
                    nounASynsetIds.add(i);
                if (s.equals(nounB))
                    nounBSynsetIds.add(i);
            }
        }

//        System.out.println("Ids for "+nounA+": ");
//        for (Integer a:nounASynsetIds)
//            System.out.print(a + " ");
//        System.out.println("");
//        System.out.println("Ids for "+nounB+": ");
//        for (Integer b:nounBSynsetIds)
//            System.out.print(b+" ");

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
//        System.out.println("");
//        System.out.println("Distance between "+nounA+" and "+nounB+":"+distance);
        return distance;
    }

    // a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
    // in a shortest ancestral path (defined below)
    public String sap(String nounA, String nounB) {
        if (!isNoun(nounA) || !isNoun(nounB))
            throw new IllegalArgumentException();

        int nounAId = -1;
        int nounBId = -1;

        //find ids for nounA and nounB inside list of synset
        List<Integer> nounASynsetIds = new ArrayList<Integer>();
        List<Integer> nounBSynsetIds = new ArrayList<Integer>();
        for (int i=0; i<synList.size(); i++) {
            List<String> synset = Arrays.asList(synList.get(i).get(1).split("\\s+"));
            for (String s : synset) {
                if (s.equals(nounA))
                    nounASynsetIds.add(i);
                if (s.equals(nounB))
                    nounBSynsetIds.add(i);
            }
        }

        SAP sap = new SAP(hDigraph);
        int ancestor;
        if (nounASynsetIds.size() == 1 && nounBSynsetIds.size() == 1) {
            ancestor = sap.ancestor(nounASynsetIds.get(0),nounBSynsetIds.get(0));
        } else {
            ancestor = sap.ancestor(nounASynsetIds, nounBSynsetIds);
        }

        return synList.get(ancestor).get(1);
    }

    // for unit testing of this class
    public static void main(String[] args) {
        WordNet wn = new WordNet("testData/synsets.txt","testData/hypernyms.txt");
        System.out.println("Digraph size:" + wn.hDigraph.V());
        System.out.println("List size:" + wn.synList.size());
        System.out.println("Set of nouns size:" + wn.nounsSet.size());
        System.out.println("***Testing if nouns exist***");
        System.out.println("Checking: 'shoulder_holster'. Expected: true. Actual: " + wn.isNoun("shoulder_holster"));
        System.out.println("Checking: 'fashioning'. Expected: true. Actual: " + wn.isNoun("fashioning"));
        System.out.println("Checking: 'wheatfield'. Expected: true. Actual: " + wn.isNoun("wheatfield"));
        System.out.println("Checking: 'hipopotam'. Expected: false. Actual: " + wn.isNoun("hipopotam"));
        System.out.println("Checking: 'vkiriushkin'. Expected: false. Actual: " + wn.isNoun("vkiriushkin"));
    }
}
