import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Collections;
import java.io.IOException;
import java.util.Hashtable;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

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

    public Hashtable<String,ArrayList<String>> parseFile(String fileName) {
        Runtime rt = Runtime.getRuntime();
        Hashtable<String, ArrayList<String>> table = new Hashtable<String,ArrayList<String>>();
        try {
            Process pr = rt.exec("opt -print-callgraph " + fileName);
            System.out.println("test2");
            InputStream st = pr.getErrorStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(st));
            String line = null;


            int state = 0; //0 - Empty Line, 1 - Call graph
            String current = null;
            while ((line = in.readLine()) != null) { 

              System.out.println(line + " " + line.length());

              switch (state) {
                case(0):
                  if (line.startsWith("Call graph node for function")) {
                    
                    String[] slist = line.split("\'");
                    current = slist[1];
                    ArrayList<String> nlist = new ArrayList<String>();
                    table.put(current,nlist);
                    state = 1;
                    System.out.println(current);
                    break;
                  }
                case(1):
                  System.out.println("STATE 1");
                  if (line.matches("(.*)CS<0x[0-9a-f]*> calls function(.*)")) {
                    System.out.println("STATE 2jkashdfjkdlshfgasjklf");
                    String[] slist = line.split("\'");
                    String func = slist[1];
                    ArrayList<String> curList = table.get(current);
                    curList.add(func);
                    System.out.println(func);
                    break;
                  }
                default:
                  if (line.length() == 0) { 
                    state = 0;
                  }
                  break;
              }

            }

        } catch (IOException e) {
        }
        return null;
    }

    public ArrayList<Pair> getPairs() {
        return new ArrayList<Pair>(); // TODO
    }

    int tSupport = 3;
    float tConfidence = 0.65f;

    public void run(String cgFile) {
        Hashtable<String, ArrayList<String>> table = parseFile(cgFile);
       // printResults(results);
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
