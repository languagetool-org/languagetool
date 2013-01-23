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
package org.languagetool.dev;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.tika.language.LanguageIdentifier;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.language.Contributor;
import org.languagetool.tools.LanguageIdentifierTools;
import org.languagetool.tools.StringTools;
import org.languagetool.tools.Tools;

/**
 * Command line tool to list supported languages and their number of rules.
 * 
 * @author Daniel Naber
 */
public final class RuleOverview {

  public static void main(final String[] args) throws IOException {
    if (args.length != 1) {
      System.out.println("Usage: " + RuleOverview.class.getName() + " <webRoot>");
      System.exit(1);
    }
    final RuleOverview prg = new RuleOverview();
    prg.run(new File(args[0]));
  }
  
  private RuleOverview() {
    // no public constructor
  }
  
  private void run(File webRoot) throws IOException {
    System.out.println("<b>Rules in LanguageTool " + JLanguageTool.VERSION + "</b><br />");
    System.out.println("Date: " + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "<br /><br />\n");
    System.out.println("<table class=\"tablesorter sortable\">");
    System.out.println("<thead>");
    System.out.println("<tr>");
    System.out.println("  <th valign='bottom' width=\"70\">Language</th>");
    System.out.println("  <th valign='bottom' align=\"left\" width=\"60\">XML<br/>rules</th>");
    System.out.println("  <th width=\"120\"></th>");
    System.out.println("  <th align=\"left\" width=\"60\">Java<br/>rules</th>");
    System.out.println("  <th align=\"left\" width=\"60\">False<br/>friends</th>");
    System.out.println("  <th valign='bottom' width=\"65\">Auto-<br/>detected</th>");
    System.out.println("  <th valign='bottom' align=\"left\">Rule Maintainers</th>");
    System.out.println("</tr>");
    System.out.println("</thead>");
    System.out.println("<tbody>");
    final List<String> sortedLanguages = getSortedLanguages();

    //setup false friends counting
    final String falseFriendFile = JLanguageTool.getDataBroker().getRulesDir() + File.separator + "false-friends.xml";
    final URL falseFriendUrl = this.getClass().getResource(falseFriendFile);
    final String falseFriendRules = StringTools.readFile(Tools.getStream(falseFriendFile))
      .replaceAll("(?s)<!--.*?-->", "")
      .replaceAll("(?s)<rules.*?>", "");

    int overallJavaCount = 0;
    int langSpecificWebsiteCount = 0;
    for (final String langName : sortedLanguages) {
      final Language lang = Language.getLanguageForName(langName);
      if (lang.isVariant()) {
        continue;
      }
      System.out.print("<tr>");
      final File langSpecificWebsite = new File(webRoot, lang.getShortName());
      if (langSpecificWebsite.isDirectory()) {
        System.out.print("<td valign=\"top\"><a href=\"../" + lang.getShortName() + "/\">" + lang.getName() + "</a></td>");
        langSpecificWebsiteCount++;
      } else {
        System.out.print("<td valign=\"top\">" + lang.getName() + "</td>");
      }
      //FIXME: this does not work for en-GB and en-US
      final String xmlFile = JLanguageTool.getDataBroker().getRulesDir() + File.separator + lang.getShortName() + File.separator + "grammar.xml";
      final URL url = this.getClass().getResource(xmlFile);    
      if (url == null) {
        System.out.println("<td valign=\"top\" align=\"right\">0</td>");
      } else {
        // count XML rules:
        String xmlRules = StringTools.readFile(Tools.getStream(xmlFile));
        xmlRules = xmlRules.replaceAll("(?s)<!--.*?-->", "");
        xmlRules = xmlRules.replaceAll("(?s)<rules.*?>", "");
        final int count = countXmlRules(xmlRules);
        final int countInRuleGroup = countXmlRuleGroupRules(xmlRules);
        final String ruleBase = "http://languagetool.svn.sourceforge.net/viewvc/languagetool/trunk/JLanguageTool/src/main/resources/org/languagetool/rules/";
        System.out.print("<td valign=\"top\" align=\"right\">" + (count + countInRuleGroup) + "</td>");
        System.out.print("<td valign=\"top\" align=\"right\">" +
            "<a href=\"" + ruleBase + lang.getShortName() + "/grammar.xml" + "\">Show</a> / " +
            "<a href=\"" + ruleBase + lang.getShortName() + "/grammar.xml?content-type=text%2Fplain" + "\">XML</a> / " +
            "<a href=\"http://community.languagetool.org/rule/list?lang=" + lang.getShortName() + "\">Browse</a>" +
            "</td>");
      }

      // count Java rules:
      final File dir = new File("src/main/java" +
              JLanguageTool.getDataBroker().getRulesDir() + "/" + lang.getShortName());
      if (!dir.exists()) {
        System.out.print("<td valign=\"top\" align=\"right\">0</td>");
      } else {
        final File[] javaRules = dir.listFiles(new JavaFilter(langName));
        final int javaCount = javaRules.length;
        if (javaCount > 0) {
          final String sourceCodeLink = 
                  "http://languagetool.svn.sourceforge.net/viewvc/languagetool/trunk/JLanguageTool/src/main/java/org/languagetool/rules/" 
                  + lang.getShortName() + "/";
          System.out.print("<td valign=\"top\" align=\"right\"><a href=\"" + sourceCodeLink + "\">" + javaCount + "</a></td>");
        } else {
          System.out.print("<td valign=\"top\" align=\"right\">" + javaCount + "</td>");
        }
        overallJavaCount++;
      }

      // false friends
      if (falseFriendUrl == null) {
        System.out.println("<td valign=\"top\" align=\"right\">0</td>");
      } else {
        final int count = countFalseFriendRules(falseFriendRules, lang);
        System.out.print("<td valign=\"top\" align=\"right\">" + count + "</td>");

        System.out.print("<td valign=\"top\">" + (isAutoDetected(lang.getShortName()) ? "yes" : "-") + "</td>");
        
        // maintainer information:
        final StringBuilder maintainerInfo = getMaintainerInfo(lang);
        System.out.print("<td valign=\"top\" align=\"left\">" + maintainerInfo.toString() + "</td>");
      }
      
      System.out.println("</tr>");    
    }
      
    if (overallJavaCount == 0) {
      throw new RuntimeException("No Java rules found - start this script from the LanguageTool directory so " +
              "that the sources are at 'src/main/java/org/languagetool/rules'");
    }
    if (langSpecificWebsiteCount == 0) {
      throw new RuntimeException("No language specific websites found - please let the web root parameter " +
              "point to the 'www' directory (current value: '" + webRoot + "')");
    }

    System.out.println("</tbody>");
    System.out.println("</table>");
  }

  private List<String> getSortedLanguages() {
    final List<String> sortedLanguages = new ArrayList<String>();
    for (Language element : Language.LANGUAGES) {
      if (element == Language.DEMO) {
        continue;
      }
      sortedLanguages.add(element.getName());
    }
    Collections.sort(sortedLanguages);
    return sortedLanguages;
  }

  private int countXmlRules(String xmlRules) {
    int pos = 0;
    int count = 0;
    while (true) {
      pos = xmlRules.indexOf("<rule ", pos + 1);
      if (pos == -1) {
        break;
      }
      count++;
    }
    return count;
  }

  private int countXmlRuleGroupRules(String xmlRules) {
    int pos = 0;
    int countInRuleGroup = 0;
    while (true) {
      pos = xmlRules.indexOf("<rule>", pos + 1);
      if (pos == -1) {
        break;
      }
      countInRuleGroup++;
    }
    return countInRuleGroup;
  }

  private int countFalseFriendRules(String falseFriendRules, Language lang) {
    int pos = 0;
    int count = 0;
    while (true) {
      pos = falseFriendRules.indexOf("<pattern lang=\"" + lang.getShortName(), pos + 1);
      if (pos == -1) {
        break;
      }
      count++;
    }
    return count;
  }

  private StringBuilder getMaintainerInfo(Language lang) {
    final StringBuilder maintainerInfo = new StringBuilder();
    if (lang.getMaintainers() != null) {
      for (Contributor contributor : lang.getMaintainers()) {
        if (!StringTools.isEmpty(maintainerInfo.toString())) {
          maintainerInfo.append(", ");
        }
        if (contributor.getUrl() != null) {
          maintainerInfo.append("<a href=\"");
          maintainerInfo.append(contributor.getUrl());
          maintainerInfo.append("\">");
        }
        maintainerInfo.append(contributor.getName());
        if (contributor.getUrl() != null) {
          maintainerInfo.append("</a>");
        }
        if (contributor.getRemark() != null) {
          maintainerInfo.append("&nbsp;(" + contributor.getRemark() + ")");
        }
      }
    }
    return maintainerInfo;
  }

  private boolean isAutoDetected(String code) {
    if (LanguageIdentifier.getSupportedLanguages().contains(code)) {
      return true;
    }
    final Set<String> additionalCodes = new HashSet<String>(Arrays.asList(LanguageIdentifierTools.ADDITIONAL_LANGUAGES));
    if (additionalCodes.contains(code)) {
      return true;
    }
    return false;
  }

}

class JavaFilter implements FileFilter {

  private final String langName;

  public JavaFilter(String langName) {
    this.langName = langName;
  }

  public boolean accept(final File f) {
    final String filename = f.getName();
    final boolean isAbstractTopClass = filename.endsWith(langName + "Rule.java");
    if (filename.endsWith(".java") && !isAbstractTopClass) {
      return true;
    }
    return false;
  }

}
