// TODO collapse + counter, changed rules, new rules which are commented out

import java.util.ArrayList;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;

public class ltdiff {
  
  public static void main(String[] args) {
    
    String lang = args[0];
    
    ArrayList<String[]> rules_added = new ArrayList<String[]>();
    ArrayList<String[]> rules_removed = new ArrayList<String[]>();
    
    // keep every added and removed rule in mind
    
    try {
    
      BufferedReader in = new BufferedReader(new FileReader("grammar_"+lang+".xml.diff"));
      String line;
      
      // loop through all lines
      while((line = in.readLine()) != null) {
        if(line.contains("id=\"") && line.contains("name=\"")) {
          String[] r = new String[2];
          r[0] = line; // id
          r[1] = line; // name
          
          r[0] = r[0].replaceAll(".*id=\"","").replaceAll("\".*","");
          
          r[1] = r[1].replaceAll(".*name=\"","").replaceAll("\".*","");
          
          if(line.startsWith("+"))
            rules_added.add(r);
          else if(line.startsWith("-"))
            rules_removed.add(r);
        }
      } // while(readLine)
      
      in.close();
      
    } catch (IOException e) {
      System.err.println("Error: " + e.getMessage());
    }
    
    // sort by rule name
    
    Collections.sort(rules_added, new Comparator<String[]>() {
      public int compare(String[] strings, String[] otherStrings) {
        return strings[1].compareTo(otherStrings[1]);
      }
    });
    
    Collections.sort(rules_removed, new Comparator<String[]>() {
      public int compare(String[] strings, String[] otherStrings) {
        return strings[1].compareTo(otherStrings[1]);
      }
    });
    
    // create html file containing the tr elements
    
    try {
    
      FileWriter fstream = new FileWriter("changes_"+lang+".html");
      BufferedWriter out = new BufferedWriter(fstream);
    
      for (int j = 0; j < rules_added.size(); j++) {
        boolean ignore = false;
        for(int k = 0; k < rules_removed.size(); k++)
          if(rules_removed.get(k)[0].equals(rules_added.get(j)[0])) ignore = true;
        if(!ignore) out.write("<tr class=\"new\"><td>4NEWRULE</td><td>" + rules_added.get(j)[1] + "</td></tr>\n");
      }
      
      for (int j = 0; j < rules_removed.size(); j++) {
        boolean ignore = false;
        for(int k = 0; k < rules_added.size(); k++)
          if(rules_added.get(k)[0].equals(rules_removed.get(j)[0])) ignore = true;
        if(!ignore) out.write("<tr class=\"removed\"><td>5REMOVEDRULE</td><td>" + rules_removed.get(j)[1] + "</td></tr>\n");
      }
      
      out.close();
      
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
    }
    
    System.exit(0);
  }
}