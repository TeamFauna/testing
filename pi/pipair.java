import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Collections;
import java.io.IOException;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.math.RoundingMode;

class Pipair {

    int tSupport = 3;
    float tConfidence = 0.65f;
    NumberFormat numf = NumberFormat.getNumberInstance();

    public static String getPairName(String a, String b) {
        if (a.compareTo(b) > 0) {
            String temp = a;
            a = b;
            b = temp;
        }
        return a + ":" + b;
    }

    class SupportGraph {
        Hashtable<String,Integer> supports = new Hashtable<String,Integer>();
        HashSet<String> allNames = new HashSet<String>();

        private void parseFromCallGraph(Hashtable<String,ArrayList<String>> cg) {
            Enumeration funcs = cg.elements();
            while (funcs.hasMoreElements()) {
                ArrayList<String> calls = (ArrayList<String>)funcs.nextElement();
                calls = removeDuplicateCalls(calls);

                for (int i = 0; i < calls.size(); i++) {
                    allNames.add(calls.get(i));
                    for (int j = i + 1; j < calls.size(); j++) {
                        String name = Pipair.getPairName(calls.get(i),
                                                         calls.get(j));
                        createOrIncrementSupport(name);
                    }
                    createOrIncrementSupport(calls.get(i));
                }
            }
        }

        private ArrayList<String> removeDuplicateCalls(ArrayList<String> calls) {
            HashSet<String> callSet = new HashSet<String>(calls);
            calls = new ArrayList<String>(callSet);
            return calls;
        }

        private void createOrIncrementSupport(String name) {
            Integer curSing = supports.get(name);
            if (curSing == null) {
                supports.put(name, 1);
            } else {
                supports.put(name, new Integer(curSing + 1));
            }
        }
    }


    public Hashtable<String,ArrayList<String>> parseFile(String fileName) {
        Runtime rt = Runtime.getRuntime();
        Hashtable<String, ArrayList<String>> table = new Hashtable<String,ArrayList<String>>();
        try {
            Process pr = rt.exec("opt -print-callgraph -disable-output " + fileName);
            InputStream st = pr.getErrorStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(st));
            String line = null;


            int state = 0; //0 - Empty Line, 1 - Call graph
            String current = null;
            while ((line = in.readLine()) != null) { 

              //System.out.println(line + " " + line.length());

              switch (state) {
                case(1):
                  if (line.matches("(.*)CS<0x[0-9a-f]*> calls function(.*)")) {
                    String[] slist = line.split("\'");
                    String func = slist[1];
                    ArrayList<String> curList = table.get(current);
                    curList.add(func);
                    //System.out.println(func);
                    break;
                  }
                case(0):
                  if (line.startsWith("Call graph node for function")) {
                    
                    String[] slist = line.split("\'");
                    current = slist[1];
                    ArrayList<String> nlist = new ArrayList<String>();
                    table.put(current,nlist);
                    state = 1;
                    //System.out.println(current);
                    break;
                  }
                default:
                  if (line.length() == 0) { 
                    state = 0;
                    //System.out.println("");
                  }
                  break;
              }

            }

        } catch (IOException e) {
        }
        return table;
    }

    public void findAndPrintViolations(Hashtable<String,ArrayList<String>> cg,
                                       SupportGraph sg) {
        Enumeration<String> cgKeySet = cg.keys();
        while (cgKeySet.hasMoreElements()) {
            String caller = (String)cgKeySet.nextElement();
            ArrayList<String> callsL = (ArrayList<String>)cg.get(caller);
            HashSet<String> calls = new HashSet<String>(callsL);

            Iterator i = calls.iterator();
            while (i.hasNext()) {
                String f = (String)i.next();
                printInvariantsForFunction(caller, f, sg, calls);
            }
        }
    }

    private void printInvariantsForFunction(String caller,
                                            String f1,
                                            SupportGraph sg,
                                            HashSet<String> calls) {
        Iterator<String> i = sg.allNames.iterator();
        while (i.hasNext()) {
            String f2 = i.next();
            String key = Pipair.getPairName(f1, f2);
            int pairSupport = sg.supports.get(key).intValue();
            int singleSupport = sg.supports.get(f1).intValue();
            float confidence = (float)pairSupport/singleSupport;

            if (confidence > tConfidence && pairSupport > tSupport) {
                if (!calls.contains(f2)) {
                    printViolation(caller, f1, f2, pairSupport,
                                   confidence);
                }
            }
        }
    }
    
    public void printViolation(String caller, String f1, String f2,
                               int support, float confidence) {
        System.out.println("bug: " + f1 + " in " + caller + ", " +
                           "pair: (" +
                           f1 + " " + f2 + "), support: " +
                           support + ", confidence: " +
                           numf.format(confidence * 100.0) + "%");
    }

    public void run(String cgFile) {
        numf.setMaximumFractionDigits(2);
        numf.setMinimumFractionDigits(2);
        numf.setRoundingMode(RoundingMode.HALF_EVEN);

        Hashtable<String,ArrayList<String>> cg = parseFile(cgFile);
        SupportGraph sg = new SupportGraph();
        sg.parseFromCallGraph(cg);
        findAndPrintViolations(cg, sg);
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
