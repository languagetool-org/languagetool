/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package de.danielnaber.languagetool.dev;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.tools.StringTools;

/**
 * Command line tool to list supported languages and their number of rules.
 * 
 * @author Daniel Naber
 */
public final class RuleOverview {

  public static void main(final String[] args) throws IOException {
    final RuleOverview prg = new RuleOverview();
    prg.run();
  }
  
  private RuleOverview() {
    // no constructor
  }
  
  private void run() throws IOException {
    System.out.println("<b>Rules in LanguageTool " + JLanguageTool.VERSION + "</b><br />");
    System.out.println("Date: " + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "<br /><br />\n");
    System.out.println("<table>");
    System.out.println("<tr>");
    System.out.println("  <th></th>");
    System.out.println("  <th align=\"right\">XML rules</th>");
    System.out.println("  <th>&nbsp;&nbsp;</th>");
    System.out.println("  <th align=\"right\">Java rules</th>");
    System.out.println("  <th>&nbsp;&nbsp;</th>");
    System.out.println("  <th align=\"right\">False friends<th><td>" +
        " (<a href=\"http://languagetool.cvs.sourceforge.net/*checkout*/languagetool/" +
        "JLanguageTool/src/rules/false-friends.xml\">show</a>)</td>");
    System.out.println("</tr>");
    final List<String> sortedLanguages = new ArrayList<String>();
    for (Language element : Language.LANGUAGES) {
      if (element == Language.DEMO) {
        continue;
      }
      sortedLanguages.add(element.getName());
    }
    Collections.sort(sortedLanguages);

    //setup false friends counting
    final String ffFile = "/rules" + File.separator + "false-friends.xml";
    final java.net.URL ffurl = this.getClass().getResource(ffFile);    
    String ffRules = StringTools.readFile(this.getClass().getResourceAsStream(ffFile));
    ffRules = ffRules.replaceAll("(?s)<!--.*?-->", "");
    ffRules = ffRules.replaceAll("(?s)<rules.*?>", "");

    
    for (final String langName : sortedLanguages) {
      final Language lang = Language.getLanguageForName(langName);
      System.out.print("<tr>");
      System.out.print("<td>" + lang.getName() + "</td>");
      final String xmlFile = "/rules" + File.separator + lang.getShortName() + File.separator + "grammar.xml";
      final java.net.URL url = this.getClass().getResource(xmlFile);    
      if (url == null) {
        System.out.println("<td align=\"right\">0</td>");
      } else {
        // count XML rules:
        String xmlRules = StringTools.readFile(this.getClass().getResourceAsStream(xmlFile));
        xmlRules = xmlRules.replaceAll("(?s)<!--.*?-->", "");
        xmlRules = xmlRules.replaceAll("(?s)<rules.*?>", "");
        int pos = 0;
        int count = 0;
        while (pos != -1) {
          pos = xmlRules.indexOf("<rule ", pos + 1);          
          if (pos == -1) {
            break;
          }          
          count++;
        }
        pos = 0;
        int countInRulegroup = 0;
        while (pos != -1) {
          pos = xmlRules.indexOf("<rule>", pos + 1);          
          if (pos == -1) {
            break;
          }          
          countInRulegroup++;
        }
        System.out.print("<td align=\"right\">" + (count + countInRulegroup) +
            " (<a href=\"http://languagetool.cvs.sourceforge.net/*checkout*/languagetool/" +
            "JLanguageTool/src/rules/" + lang.getShortName() + "/grammar.xml\">show</a>)" + "</td>");
      }
      System.out.print("<td></td>");

      System.out.print("<td></td>");
      // count Java rules:
      final File dir = new File("src/java/de/danielnaber/languagetool/rules/" + lang.getShortName());
      if (!dir.exists()) {
        System.out.print("<td align=\"right\">0</td>");
      } else {
        final File[] javaRules = dir.listFiles(new JavaFilter());
        final int javaCount = javaRules.length-1;   // minus 1: one is always "<Language>Rule.java"
        System.out.print("<td align=\"right\">" + javaCount + "</td>");
      }

//    false friends
      System.out.println("<td></td><td></td>"); 
      if (ffurl == null) {
        System.out.println("<td align=\"right\">0</td>");
      } else {
        // count XML rules:
        int pos = 0;
        int count = 0;
        while (pos != -1) {
          pos = ffRules.indexOf("<pattern lang=\""+ lang.getShortName(), pos + 1);          
          if (pos == -1) {
            break;
          }          
          count++;
        }
        System.out.print("<td align=\"left\">" + (count) +
            "</td>");
      }
      
      System.out.println("</tr>");    
    }

    System.out.println("</table>");    
  }

}

class JavaFilter implements FileFilter {

  public boolean accept(final File f) {
    if (f.getName().endsWith(".java")) {
      return true;
    }
    return false;
  }

}
