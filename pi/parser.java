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
    public static int levels = 0;

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
                    if (!curList.contains(func)) { 
                      curList.add(func);
                    }
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
            
            //If we are expanding functions n levels deep, we do it here
            for (int l = 0; l < Parser.levels; l++) {
              Hashtable<String,ArrayList<String>> staticTable = deepCopy(table);

              Enumeration funcs = table.elements();

              while (funcs.hasMoreElements()) {
                ArrayList<String> calls = (ArrayList<String>)funcs.nextElement();

                ArrayList<String> originalCalls = (ArrayList<String>)calls.clone();

                for (int i = 0; i < originalCalls.size(); i++) {
                  String expandFunc = originalCalls.get(i);

                  ArrayList<String> funcsToBeAdded = staticTable.get(expandFunc);

                  for (int j = 0; j < funcsToBeAdded.size(); j++) {
                    String funcToBeAdded = (String)funcsToBeAdded.get(j);
                    if (!calls.contains(funcToBeAdded)) {
                      calls.add(funcToBeAdded);
                    }
                  }
                }
              }
            }
        } catch (IOException e) {
        }
        return table;
    }

    public static Hashtable<String,ArrayList<String>> deepCopy(Hashtable<String,ArrayList<String>> iniTable) {
      Hashtable<String,ArrayList<String>> nTable = new Hashtable<String,ArrayList<String>>();

      Enumeration funcs = iniTable.keys();
      
      while (funcs.hasMoreElements()) { 
        String func = (String)funcs.nextElement();
        ArrayList<String> iniCalls = iniTable.get(func); 

        ArrayList<String> nCalls = (ArrayList<String>)iniCalls.clone();

        nTable.put(func, nCalls);
      }
      
      return nTable;

    }
}
