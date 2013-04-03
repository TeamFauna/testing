import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Collections;
import java.io.IOException;
import java.util.Hashtable;
import java.util.HashSet;

class Pipair {

    int tSupport = 3;
    float tConfidence = 0.65f;

    class Pair {
        private String _aName;
        private String _bName;
        private int _support = 0;
        private float _aWithoutB = 0;

        public Pair(String aName, String bName) {
            _aName = aName;
            _bName = bName;
        }

        public void strengthen() {
            _support++;
        }

        public void weaken() {
            _aWithoutB++;
        }

        public int getSupport() {
            return _support;
        }

        public float getConfidence() {
            return (float)_support / _aWithoutB;
        }

        public String toString() {
            return "TODO";
        }
    }

    class Violation {
        private String _caller;
        private String _violator;
        private Pair _pair;

        public Violation(String caller, String violator, Pair pair) {
            _caller = caller;
            _violator = violator;
            _pair = pair;
        }

        public String toString() {
            return "TODO";
        }
    }

    public Hashtable<String,ArrayList<String>> parseFile(String fileName) {
        Runtime rt = Runtime.getRuntime();
        try {
            Process pr = rt.exec("opt -print-callgraph " + fileName);
        } catch (IOException e) {
        }
        return new Hashtable<String,ArrayList<String>>();
    }

    public Hashtable<String,ArrayList<Pair>> getInvariantPairs(Hashtable<String,ArrayList<String>> cg) {
        Hashtable<String,ArrayList<Pair>> pairs = new Hashtable<String,ArrayList<Pair>>();

        Enumeration funcs = cg.elements();
        while (funcs.hasMoreElements()) {
            ArrayList<String> calls = (ArrayList<String>)funcs.nextElement();
            
            // Remove duplicates from calls
            HashSet<String> callSet = new HashSet(calls);
            calls = new ArrayList(callSet);

            for (int i = 0; i < calls.size(); i++) {
                for (int j = i + 1; j < calls.size(); j++) {
                    // For both possible pairs...

                    // Check for existing pair

                    // If existing pair, increment it

                    // If not, create it
                }

                ArrayList<Pair> existingPairs = pairs.get(calls.get(i));
                for (int k = 0; k < existingPairs.size(); k++) {
                    existingPairs.get(k).weaken();
                }
            }
        }
        
        return pairs;
    }

    public ArrayList<Violation> getViolations(Hashtable<String,ArrayList<String>> cg,
                                              Hashtable<String,ArrayList<Pair>> invariants) {
        //if (p.getSupport() > tSupport && p.getConfidence() > tConfidence) {
        return new ArrayList<Violation>(); // TODO
    }

    public void run(String cgFile) {
        Hashtable<String,ArrayList<String>> cg = parseFile(cgFile);
        Hashtable<String,ArrayList<Pair>> invariants = getInvariantPairs(cg);
        ArrayList<Violation> violations = getViolations(cg, invariants);
        printViolations(violations);
    }

    public void printViolations(ArrayList<Violation> violations) {
        Enumeration e = Collections.enumeration(violations);
        while (e.hasMoreElements()) {
            Violation v = (Violation)e.nextElement();
            System.out.println(v);
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
