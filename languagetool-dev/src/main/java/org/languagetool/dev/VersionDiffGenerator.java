/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */

package org.languagetool.dev;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * Generates an HTML report of added, deleted and modified rules between versions of LanguageTool.
 * See ltdiff.bash.
 * @author Markus Brenneis
 */
class VersionDiffGenerator {
  
  public static void main(String[] args) throws IOException {
    final VersionDiffGenerator generator = new VersionDiffGenerator();
    generator.makeDiff(args[0]);
  }
  
  private void makeDiff(String lang) throws IOException {
    
    final List<Rule> oldRules = new ArrayList<Rule>(); // rules in old grammar.xml
    final List<Rule> newRules = new ArrayList<Rule>(); // rules in new grammar.xml
    final List<Rule> modifiedRules = new ArrayList<Rule>();
    
    for (int i = 0; i < 2; i++) {

      final List<Rule> rules;
      if (i == 0) {
        rules = oldRules;
      } else {
        rules = newRules;
      }
    
      final Scanner scanner = new Scanner(new FileReader(i == 0 ? "tools/ltdiff/old" : "tools/ltdiff/new"));

      Rule r = new Rule();
      
      // loop through all lines
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
      
        if (line.contains("id=\"") && line.contains("rule")) {
          
          if (!line.contains("name=\"")) { // merge with the following line if the name is there (e.g. sk)
            line += scanner.nextLine();
          }
          
          if (r.numberOfExamples() > 0) {
            rules.add(r);
            r = new Rule();
          }
          
          r.id = line;
          r.name = line;
          
          r.id = r.id.replaceAll(".*id=\"","").replaceAll("\".*","");
          r.name = r.name.replaceAll(".*name=\"","").replaceAll("\".*","");
          
          for (Rule rule : rules) { // ensure that the name is unique
            if (r.name.equals(rule.name)) {
              r.name += " ";
            }
          }
          
        } else if (line.contains("type=\"correct\"")) {
          
          while (!line.contains("</example>")) { // merge with the following line(s) if the example continues there
            line += scanner.nextLine();
          }
          r.correct.add(line.replaceAll("marker","b").replaceAll(".*<example.*?>","").replaceAll("</example>.*",""));
          
        } else if (line.contains("type=\"incorrect\"")) {
        
          while (!line.contains("</example>")) {
            line += scanner.nextLine();
          }
          r.incorrect.add(line.replaceAll("marker","b").replaceAll(".*<example.*?>","").replaceAll("</example>.*",""));
          
        }
        
      } // while(readLine)
      
      scanner.close();
      
    }
      
    // sort rules by name
    Collections.sort(oldRules);
    Collections.sort(newRules);
    
    // create html file containing the tr elements
    
    final FileWriter fileWriter = new FileWriter("changes_" + lang + ".html");
    final BufferedWriter out = new BufferedWriter(fileWriter);

    for (Rule newRule1 : newRules) {

      boolean found = false;

      for (int j = 0; j < oldRules.size() && !found; j++) {

        if (newRule1.id.equals(oldRules.get(j).id) || newRule1.name.equals(oldRules.get(j).name)) {

          found = true;

          if (newRule1.numberOfExamples() > oldRules.get(j).numberOfExamples()) { // if the new rules has more examples, it is considered to be improved

            final Rule r = newRule1;

            for (int k = 0; k < r.correct.size(); k++) { // remove examples which already exist in old rule

              for (int l = 0; l < oldRules.get(j).correct.size() && r.correct.size()>0; l++) {

                if (r.correct.get(k).equals(oldRules.get(j).correct.get(l))) {

                  r.correct.remove(k);
                  if (k > 0) k--;

                } // if examples equal

              } // for each old correct example

            } // for each new correct example

            for (int k = 0; k < r.incorrect.size(); k++) { // remove examples which already exist in old rule

              for (int l = 0; l < oldRules.get(j).incorrect.size() && r.incorrect.size()>0; l++) {

                if (r.incorrect.get(k).equals(oldRules.get(j).incorrect.get(l))) {

                  r.incorrect.remove(k);
                  if (k > 0) k--;

                } // if examples equal

              } // for each old incorrect example

            } // for each new incorrect example
            
            // remove correct examples which have a related incorrect example
            // (users probably want to see correct examples only when false positives are fixed)
            r.removeCorrectExamplesWithRelatedIncorrectExample();

            modifiedRules.add(r);

          } // if new rules has more examples

        } // if new rule is not new

      } // for each old rule

      if (!found) {
        out.write("<tr class=\"new\"><td>4NEWRULE</td><td>" + newRule1.name + newRule1.getExamples(false) + "</td></tr>\n");
      }

    } // for each new rule

    for (Rule modifiedRule : modifiedRules) {
      out.write("<tr class=\"modified\"><td>6IMPROVEDRULE</td><td>" + modifiedRule.name + modifiedRule.getExamples(true) + "</td></tr>\n");
    }

    for (Rule oldRule : oldRules) {
      boolean found = false;
      for (Rule newRule : newRules) {
        if (newRule.id.equals(oldRule.id) || newRule.name.equals(oldRule.name)) {
          found = true;
        }
      }
      if (!found && !oldRule.name.contains("<")) {
        out.write("<tr class=\"removed\"><td>5REMOVEDRULE</td><td>" + oldRule.name + "</td></tr>\n");
      }
    }
    
    out.close();
  }

  class Rule implements Comparable<Rule> {

    private final List<String> correct = new ArrayList<String>();
    private final List<String> incorrect = new ArrayList<String>();

    private String name = "";
    private String id;
    
    int numberOfExamples() {
      return correct.size() + incorrect.size();
    }
    
    String getExamples(boolean all) {
      
      String s = "<div>";

      for (String anIncorrect : incorrect) {
        s += "<span>7FINDERR</span>" + anIncorrect + "<br/>";
      }
      
      if (all) {
        for (String aCorrect : correct) {
          s += "<span>8FINDNOTERR</span>" + aCorrect + "<br/>";
        }
      }
      
      s = s.substring(0, s.length() - 5) + "</div>";
          
      return s;
      
    }
    
    /**
     * removes correct examples for which an incorrect example which only differs in the part between &lt;b&gt;&lt;/b&gt; exists
     */
    public void removeCorrectExamplesWithRelatedIncorrectExample() {
      
      for(int i = 0; i < correct.size(); i++) {
        
        boolean found = false;
        
        for(int j = 0; j < incorrect.size() && !found; j++) {
          
          if(correct.get(i).startsWith(incorrect.get(j).substring(0, incorrect.get(j).indexOf("<b>")+3))
          && correct.get(i).endsWith(incorrect.get(j).substring(incorrect.get(j).indexOf("</b>")))) {
            
            correct.remove(i);
            i--;
            found=true;
            
          }
          
        }
        
      }
      
    }
    
    @Override
    public int compareTo(Rule r) {
      return this.name.compareTo(r.name);
    }
  }

}