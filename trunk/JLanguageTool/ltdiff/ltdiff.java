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

class rule implements Comparable<rule> {
  public String id;
  public String name;
  public ArrayList<String> correct = new ArrayList<String>();
  public ArrayList<String> incorrect = new ArrayList<String>();
  
  public int numberOfExamples() {
    return correct.size()+incorrect.size();
  }
  
  public String examples(boolean all) {
    
    String s = "<div>";
    
    for (int i=0; i<incorrect.size(); i++) {
      s += "<span>7FINDERR</span>" + incorrect.get(i) + "<br/>";
    }
    
    if (all) {
      for (int i=0; i<correct.size(); i++) {
      s += "<span>8FINDNOTERR</span>" + correct.get(i) + "<br/>";
      }
    }
    
    s = s.substring(0,s.length()-5) + "</div>";
        
    return s;
    
  }
  
  @Override
  public int compareTo(rule r) {
    return this.name.compareTo(r.name);
  }
}

public class ltdiff {
  
  public static void main(String[] args) {
    
    String lang = args[0];
    
    ArrayList<rule> old_rules = new ArrayList<rule>(); // rules in old grammar.xml
    ArrayList<rule> new_rules = new ArrayList<rule>(); // rules in new grammar.xml
    ArrayList<rule> modified_rules = new ArrayList<rule>();
    
    try {
      
      for (int i=0; i<2; i++) {
        
        ArrayList<rule> rules;
        
        if(i==0)
          rules = old_rules;
        else
          rules = new_rules;
      
        BufferedReader in = new BufferedReader(new FileReader(i==0 ? "old" : "new"));
        String line;
        
        rule r = new rule();
        
        // loop through all lines
        while ((line = in.readLine()) != null) {
        
          if (line.contains("id=\"") && line.contains("rule")) {
            
            if (!line.contains("name=\"")) // merge with the following line if the name is there (e.g. sk)
              line += in.readLine();
            
            if (r.correct.size() > 0) {
              rules.add(r);
              r = new rule();
            }
            
            r.id = line;
            r.name = line;
            
            r.id = r.id.replaceAll(".*id=\"","").replaceAll("\".*","");
            r.name= r.name.replaceAll(".*name=\"","").replaceAll("\".*","");
            
            for (int j = 0; j < rules.size(); j++) { // ensure that the name is unique
              if (r.name.equals(rules.get(j).name)) {
                r.name += " ";
              }
            }
            
          } else if (line.contains("type=\"correct\"")) {
            
            while (!line.contains("</example>")) // merge with the following line(s) if the example continues there
              line += in.readLine();
            r.correct.add(line.replaceAll("marker","b").replaceAll(".*<example.*?>","").replaceAll("</example>.*",""));
            
          } else if (line.contains("type=\"incorrect\"")) {
          
            while (!line.contains("</example>"))
              line += in.readLine();
            r.incorrect.add(line.replaceAll("marker","b").replaceAll(".*<example.*?>","").replaceAll("</example>.*",""));
            
          }
          
        } // while(readLine)
        
        in.close();
        
      }
      
    } catch (IOException e) {
      System.err.println("Error 1: " + e.getMessage());
    }
    
    // sort rules by name
    Collections.sort(old_rules);
    Collections.sort(new_rules);
    
    // create html file containing the tr elements
    
    try {
    
      FileWriter fstream = new FileWriter("changes_"+lang+".html");
      BufferedWriter out = new BufferedWriter(fstream);
    
      for (int i = 0; i < new_rules.size(); i++) {
      
        boolean found = false;
        
        for (int j = 0; j < old_rules.size() && !found; j++) {
        
          if (new_rules.get(i).id.equals(old_rules.get(j).id) || new_rules.get(i).name.equals(old_rules.get(j).name)) {
            
            found = true;
            
            if (new_rules.get(i).numberOfExamples() > old_rules.get(j).numberOfExamples()) { // if the new rules has more examples, it is considered to be improved
            
              rule r = new_rules.get(i);
              
              for (int k = 0; k < r.correct.size(); k++) { // remove examples which already exist in old rule
              
                for (int l = 0; l < old_rules.get(j).correct.size(); l++) {
                
                  if (r.correct.get(k).equals(old_rules.get(j).correct.get(l))) {
                  
                    r.correct.remove(k);
                    if (k>0) k--;
                    
                  } // if examples equal
                  
                } // for each old correct example
                
              } // for each new correct example
              
              for (int k = 0; k < r.incorrect.size(); k++) { // remove examples which already exist in old rule
              
                for (int l = 0; l < old_rules.get(j).incorrect.size(); l++) {
                
                  if (r.incorrect.get(k).equals(old_rules.get(j).incorrect.get(l))) {
                  
                    r.incorrect.remove(k);
                    if (k>0) k--;
                    
                  } // if examples equal
                  
                } // for each old incorrect example
                
              } // for each new incorrect example
              
              modified_rules.add(r);
              
            } // if new rules has more examples
            
          } // if new rule is not new
          
        } // for each old rule
        
        if (!found)
          out.write("<tr class=\"new\"><td>4NEWRULE</td><td>" + new_rules.get(i).name + new_rules.get(i).examples(false) + "</td></tr>\n");
        
      } // for each new rule
      
      for (int i=0; i < modified_rules.size(); i++) {
        out.write("<tr class=\"modified\"><td>6IMPROVEDRULE</td><td>" + modified_rules.get(i).name + modified_rules.get(i).examples(true) + "</td></tr>\n");
      }
      
      for (int i = 0; i < old_rules.size(); i++) {
        boolean found = false;
        for (int j = 0; j < new_rules.size(); j++)
          if (new_rules.get(j).id.equals(old_rules.get(i).id) || new_rules.get(j).name.equals(old_rules.get(i).name))
            found = true;
        if (!found)
          out.write("<tr class=\"removed\"><td>5REMOVEDRULE</td><td>" + old_rules.get(i).name + "</td></tr>\n");
      }
      
      out.close();
      
    } catch (Exception e) {
      System.err.println("Error 2: " + e.getMessage());
    }
    
    System.exit(0);
  }
}