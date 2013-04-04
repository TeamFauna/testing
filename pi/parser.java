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
    /* Take a bc file and retrieve the callgraph using the opt command
     * 
     * If we want to do interprocedural analysis, expand the callgraph to n levels
     */
    public static Hashtable<String,ArrayList<String>> parseFile(String fileName, int levels) {
        // This table will point "Functions" to a list of their "calls"
        Hashtable<String, ArrayList<String>> table = new Hashtable<String,ArrayList<String>>();
        
        Runtime rt = Runtime.getRuntime();
        try {
            //Print the callgraph to stderr, disable stdout because it's all junk
            Process pr = rt.exec("opt -print-callgraph -disable-output " + fileName);
            InputStream st = pr.getErrorStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(st));
            String line = null;


            int state = 0; //0 - Empty Line, 1 - Call graph
            String current = null;
            while ((line = in.readLine()) != null) { 

              //System.out.println(line + " " + line.length());

              switch (state) {
                case(1): //We are in a state that already has a "function" look for the "calls"
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
                case(0): //Look for a "function"
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
            for (int l = 0; l < levels; l++) {
              //We need a callgraph that won't change as we expand functions
              Hashtable<String,ArrayList<String>> staticTable = deepCopy(table);

              Enumeration funcs = table.elements();
              //Loop through all the functions to expand their calls
              while (funcs.hasMoreElements()) {
                ArrayList<String> calls = (ArrayList<String>)funcs.nextElement();
                ArrayList<String> originalCalls = (ArrayList<String>)calls.clone();

                //Loop through the calls and expand the functions they contain
                for (int i = 0; i < originalCalls.size(); i++) {
                  String expandFunc = originalCalls.get(i);

                  ArrayList<String> funcsToBeAdded = staticTable.get(expandFunc);

                  //If there are no functions to expand, don't remove the original call from the function
                  if (funcsToBeAdded.size() > 0) {
                    calls.remove(expandFunc);
                  }

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
        //System.out.println(table);
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
