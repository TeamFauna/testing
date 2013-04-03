import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Collections;
import java.io.IOException;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

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

        public String getTarget() {
            return _bName;
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
        private Pair _pair;

        public Violation(String caller, Pair pair) {
            _caller = caller;
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

    public Hashtable<String,Hashtable<String,Pair>>
        getInvariantPairs(Hashtable<String,ArrayList<String>> cg) {

        Hashtable<String,Hashtable<String,Pair>> pairs = getAllInvariantPairs(cg);
        rejectWeakPairs(pairs);
        return pairs;
    }

    private Hashtable<String,Hashtable<String,Pair>> getAllInvariantPairs(Hashtable<String,ArrayList<String>> cg) {
        Hashtable<String,Hashtable<String,Pair>> pairs =
            new Hashtable<String,Hashtable<String,Pair>>();

        Enumeration funcs = cg.elements();
        while (funcs.hasMoreElements()) {
            ArrayList<String> calls = (ArrayList<String>)funcs.nextElement();
            removeDuplicateCalls(calls);

            for (int i = 0; i < calls.size(); i++) {
                for (int j = i + 1; j < calls.size(); j++) {
                    createOrStrengthenPair(pairs, calls.get(i), calls.get(j));
                    createOrStrengthenPair(pairs, calls.get(j), calls.get(i));
                }

                Hashtable<String,Pair> existingPairs = pairs.get(calls.get(i));
                for (int k = 0; k < existingPairs.size(); k++) {
                    existingPairs.get(k).weaken();
                }
            }
        }

        return pairs;
    }

    private void
        rejectWeakPairs(Hashtable<String,Hashtable<String,Pair>> pairs) {

        Enumeration pairLists = pairs.elements();
        while (pairLists.hasMoreElements()) {
            Hashtable<String,Pair> pairList = (Hashtable<String,Pair>)pairLists.nextElement();
            Enumeration callPairs = pairList.elements();
            while (callPairs.hasMoreElements()) {
                Pair p = (Pair)callPairs.nextElement();
                if (p.getSupport() > tSupport &&
                    p.getConfidence() > tConfidence) {
                    pairList.remove(p);
                }
            }
        }
    }

    private void createOrStrengthenPair(Hashtable<String,Hashtable<String,Pair>>
                                       pairs,
                                       String f1, String f2) {
        Hashtable<String,Pair> funcPairs = pairs.get(f1);
        if (funcPairs == null) {
            funcPairs = new Hashtable<String,Pair>();
            pairs.put(f1, funcPairs);
        }
        Pair p = funcPairs.get(f2);
        if (p == null) {
            p = new Pair(f1, f2);
            funcPairs.put(f2, p);
        }
        p.strengthen();
    }

    private void removeDuplicateCalls(ArrayList<String> calls) {
        HashSet<String> callSet = new HashSet(calls);
        calls = new ArrayList(callSet);
    }

    public ArrayList<Violation>
        getViolations(Hashtable<String,ArrayList<String>> cg,
                      Hashtable<String,Hashtable<String,Pair>> invariants) {

        ArrayList<Violation> violations = new ArrayList<Violation>();

        HashSet<Map.Entry<String,ArrayList<String>>> cgSet = (HashSet<Map.Entry<String,ArrayList<String>>>)cg.entrySet();
        Iterator functions = cgSet.iterator();
        while (functions.hasNext()) {
            Map.Entry<String,ArrayList<String>> entry = (Map.Entry<String,ArrayList<String>>)functions.next();
            String functionName = (String)entry.getKey();
            ArrayList<String> callsL = (ArrayList<String>)entry.getValue();
            HashSet<String> calls = new HashSet(callsL);

            Iterator i = calls.iterator();
            while (i.hasNext()) {
                Hashtable invariantsForCall = invariants.get(i.next());
                Enumeration pairs = invariantsForCall.elements();
                while (pairs.hasMoreElements()) {
                    Pair invariant = (Pair)pairs.nextElement();
                    if (!calls.contains(invariant.getTarget())) {
                        violations.add(new Violation(functionName,
                                                      invariant));
                    }
                }
            }
        }

        return violations;
    }

    public void run(String cgFile) {
        Hashtable<String,ArrayList<String>> cg = parseFile(cgFile);
        Hashtable<String,Hashtable<String,Pair>> invariants =
            getInvariantPairs(cg);
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
