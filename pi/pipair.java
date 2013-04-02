import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Collections;

class Pair {
    String aName;
    String bName;
    int support;
    float confidence;

    public String toString() {
        return "TODO";
    }
}

class Pipair {

    static final int T_SUPPORT = 3;
    static final float T_CONFIDENCE = 0.65f;

    public void run() {
        ArrayList<Pair> results = new ArrayList<Pair>();
        printResults(results);
    }

    public void printResults(ArrayList<Pair> results) {
        Enumeration e = Collections.enumeration(results);
        while (e.hasMoreElements()) {
            Pair p = (Pair)e.nextElement();
            if (p.support > T_SUPPORT && p.confidence > T_CONFIDENCE) {
                System.out.println("bug: " + e.nextElement());
            }
        }
    }

    public static void main(String[] args) {
        Pipair prog = new Pipair();
        prog.run();
    }
}
