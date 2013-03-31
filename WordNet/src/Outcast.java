import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: et33027
 * Date: 3/28/13
 * Time: 5:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class Outcast {

    private WordNet wn;

    // constructor takes a WordNet object
    public Outcast(WordNet wordnet) {
        this.wn = wordnet;
    }

    // given an array of WordNet nouns, return an outcast
    public String outcast(String[] nouns) {
        List<Integer> distances = new ArrayList<Integer>(nouns.length);
        for (int i=0; i<nouns.length; i++) {
//            System.out.println("*******"+nouns[i]+"*******");
            distances.add(0);
            for (int j=0; j<nouns.length; j++) {
                if (j==i) continue;
                distances.set(i, distances.get(i) + wn.distance(nouns[i], nouns[j]));
            }
//            System.out.println("***Total distance:"+distances.get(i));
        }
        int maxDistance = distances.get(0);
        String outcast = nouns[0];
        for (int a=1; a<distances.size(); a++) {
            if (distances.get(a) > maxDistance) {
                maxDistance = distances.get(a);
                outcast = nouns[a];
            }
        }
        return outcast;
    }

    // for unit testing of this class (such as the one below)
    public static void main(String[] args) {
        WordNet wordnet = new WordNet("testData/synsets.txt","testData/hypernyms.txt");
        Outcast outcast = new Outcast(wordnet);
//        for (int t = 2; t < args.length; t++) {
            String[] nouns = In.readStrings("testData/outcast11.txt");
            StdOut.println("outcast11.txt: " + outcast.outcast(nouns));
//        }
    }
}
