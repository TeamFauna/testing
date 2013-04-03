package fauna.testing;
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

class Parser {
    public static Hashtable<String,ArrayList<String>> parseFile(String fileName) {
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
}
