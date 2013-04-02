import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Collections;
import java.io.IOException;

class Pipair {

    class Pair {
        String aName;
        String bName;
        int support;
        float confidence;

        public String toString() {
            return "TODO";
        }
    }

    class CallGraph {
        public void parseFile(String fileName) {
            Runtime rt = Runtime.getRuntime();
            try {
                Process pr = rt.exec("opt -print-callgraph " + fileName);
            } catch (IOException e) {
            }
        }

        public ArrayList<Pair> getPairs() {
            return new ArrayList<Pair>(); // TODO
        }
    }

    int tSupport = 3;
    float tConfidence = 0.65f;

    public void run(String cgFile) {
        CallGraph cg = new CallGraph();
        cg.parseFile(cgFile);
        ArrayList<Pair> results = cg.getPairs();
        printResults(results);
        System.out.println(tSupport);
        System.out.println(tConfidence);
    }

    public void printResults(ArrayList<Pair> results) {
        Enumeration e = Collections.enumeration(results);
        while (e.hasMoreElements()) {
            Pair p = (Pair)e.nextElement();
            if (p.support > tSupport && p.confidence > tConfidence) {
                System.out.println("bug: " + e.nextElement());
            }
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: ./pipair <bitcode file> <T SUPPORT> <T CONFIDENCE>,");
            System.exit(0);
        }

        Pipair prog = new Pipair();
        if (args.length >= 2) {
            prog.tSupport = Integer.parseInt(args[1]);
        }
        if (args.length >= 3) {
            prog.tConfidence = Float.parseFloat(args[2]);
        }
        prog.run(args[0]);
    }
}
