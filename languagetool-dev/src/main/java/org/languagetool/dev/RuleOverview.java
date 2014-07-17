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

import org.apache.commons.lang.StringUtils;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.language.Contributor;
import org.languagetool.tools.StringTools;
import org.languagetool.tools.Tools;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Command line tool to list supported languages and their number of rules.
 * 
 * @author Daniel Naber
 */
@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
public final class RuleOverview {

  private static final List<String> LANGUAGES_WITH_NEW_MAINTAINER_NEED = 
          Arrays.asList("en", "ja", "is", "sv", "lt", "ro", "ml");
  private static final List<String> LANGUAGES_WITH_CO_MAINTAINER_NEED = 
          Arrays.asList("da", "be", "zh", "gl");

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
    System.out.println("<table class=\"tablesorter sortable\" style=\"width: auto\">");
    System.out.println("<thead>");
    System.out.println("<tr>");
    System.out.println("  <th valign='bottom' width=\"200\">Language</th>");
    System.out.println("  <th valign='bottom' align=\"left\" width=\"60\">XML<br/>rules</th>");
    System.out.println("  <th></th>");
    System.out.println("  <th align=\"left\" width=\"60\">Java<br/>rules</th>");
    System.out.println("  <th align=\"left\" width=\"60\">False<br/>friends</th>");
    //System.out.println("  <th valign='bottom' width=\"65\">Auto-<br/>detected</th>");
    System.out.println("  <th valign='bottom' align=\"left\">Rule Maintainers</th>");
    System.out.println("</tr>");
    System.out.println("</thead>");
    System.out.println("<tbody>");
    final List<Language> sortedLanguages = getSortedLanguages();

    //setup false friends counting
    final String falseFriendFile = JLanguageTool.getDataBroker().getRulesDir() + File.separator + "false-friends.xml";
    final String falseFriendRules = StringTools.readStream(Tools.getStream(falseFriendFile), "utf-8")
      .replaceAll("(?s)<!--.*?-->", "")
      .replaceAll("(?s)<rules.*?>", "");

    int overallJavaCount = 0;
    int langSpecificWebsiteCount = 0;
    for (final Language lang : sortedLanguages) {
      if (lang.isVariant()) {
        continue;
      }
      System.out.print("<tr>");
      final String langCode = lang.getShortName();
      final File langSpecificWebsite = new File(webRoot, langCode);
      final List<String> variants = getVariants(sortedLanguages, lang);
      String variantsText = "";
      if (variants.size() > 0) {
        variantsText = "<br/><span class='langVariants'>Variants for: " + StringUtils.join(variants, ", ") + "</span>";
      }
      if (langSpecificWebsite.isDirectory()) {
        System.out.print("<td valign=\"top\"><a href=\"../" + langCode + "/\">" + lang.getName() + "</a>" + variantsText + "</td>");
        langSpecificWebsiteCount++;
      } else {
        System.out.print("<td valign=\"top\">" + lang.getName() + " " + variantsText + "</td>");
      }

      int allRules = countRulesForLanguage(lang);
      if (allRules == 0) {
        System.out.println("<td valign=\"top\" align=\"right\">0</td>");
      } else {
        final String ruleBase = "https://github.com/languagetool-org/languagetool/blob/master/languagetool-language-modules/"
                + langCode + "/src/main/resources/org/languagetool/rules/";
        System.out.print("<td valign=\"top\" align=\"right\">" + allRules + "</td>");
        System.out.print("<td valign=\"top\" align=\"right\">" +
            //"<a href=\"" + ruleBase + langCode + "/grammar.xml" + "\">Show</a> / " +
            "<a href=\"http://community.languagetool.org/rule/list?lang=" + langCode + "\">Browse</a>,&nbsp;" +
            "<a href=\"" + ruleBase + langCode + "/grammar.xml\">XML</a>" +
            "</td>");
      }

      // count Java rules:
      final File dir = new File("../languagetool-language-modules/" + langCode + "/src/main/java" +
              JLanguageTool.getDataBroker().getRulesDir() + "/" + langCode);
      if (!dir.exists()) {
        System.out.print("<td valign=\"top\" align=\"right\">0</td>");
      } else {
        final File[] javaRules = dir.listFiles(new JavaFilter(lang.getName()));
        final int javaCount = javaRules.length;
        if (javaCount > 0) {
          final String sourceCodeLink = 
                  "https://github.com/languagetool-org/languagetool/blob/master/languagetool-language-modules/"
                  + langCode + "/src/main/java/org/languagetool/rules/" 
                  + langCode + "/";
          System.out.print("<td valign=\"top\" align=\"right\"><a href=\"" + sourceCodeLink + "\">" + javaCount + "</a></td>");
        } else {
          System.out.print("<td valign=\"top\" align=\"right\">" + javaCount + "</td>");
        }
        overallJavaCount++;
      }

      // false friends:
      final int count = countFalseFriendRules(falseFriendRules, lang);
      System.out.print("<td valign=\"top\" align=\"right\">" + count + "</td>");
      //System.out.print("<td valign=\"top\">" + (isAutoDetected(lang.getShortName()) ? "yes" : "-") + "</td>");
      
      // maintainer information:
      final StringBuilder maintainerInfo = getMaintainerInfo(lang);
      final String maintainerText;
      if (langCode.equals("pt")) {
        maintainerText = " - <span class='maintainerNeeded'><a href='http://wiki.languagetool.org/tasks-for-language-maintainers'>Looking for a maintainer for Brazilian Portuguese</a></span>";
      } else if (LANGUAGES_WITH_NEW_MAINTAINER_NEED.contains(langCode)) {
        maintainerText = " - <span class='maintainerNeeded'><a href='http://wiki.languagetool.org/tasks-for-language-maintainers'>Looking for new maintainer</a></span>";
      } else if (LANGUAGES_WITH_CO_MAINTAINER_NEED.contains(langCode)) {
        maintainerText = " - <span class='maintainerNeeded'><a href='http://wiki.languagetool.org/tasks-for-language-maintainers'>Looking for co-maintainer</a></span>";
      } else {
        maintainerText = "";
      }
      System.out.print("<td valign=\"top\" align=\"left\">" + maintainerInfo.toString() + maintainerText + "</td>");
      
      System.out.println("</tr>");    
    }
      
    if (overallJavaCount == 0) {
      throw new RuntimeException("No Java rules found - start this script from the languagetool-standalone directory");
    }
    if (langSpecificWebsiteCount == 0) {
      throw new RuntimeException("No language specific websites found - please let the web root parameter " +
              "point to the 'www' directory (current value: '" + webRoot + "')");
    }

    System.out.println("</tbody>");
    System.out.println("</table>");
  }

  private int countRulesForLanguage(Language lang) throws IOException {
    List<String> ruleFileNames = lang.getRuleFileNames();
    int count = 0;
    for (String ruleFileName : ruleFileNames) {
      final URL url = this.getClass().getResource(ruleFileName);
      if (url != null) {
        String xmlRules = StringTools.readStream(Tools.getStream(ruleFileName), "utf-8");
        xmlRules = xmlRules.replaceAll("(?s)<!--.*?-->", "");
        xmlRules = xmlRules.replaceAll("(?s)<rules.*?>", "");
        count += countXmlRules(xmlRules);
        count += countXmlRuleGroupRules(xmlRules);
      }
    }
    return count;
  }

  private List<String> getVariants(List<Language> allLanguages, Language lang) {
    List<String> variants = new ArrayList<>();
    for (Language sortedLanguage : allLanguages) {
      if (sortedLanguage.isVariant() && lang.getShortName().equals(sortedLanguage.getShortName())) {
        variants.add(sortedLanguage.getName().replaceAll(".*\\((.*?)\\).*", "$1").trim());
      }
    }
    return variants;
  }

  private List<Language> getSortedLanguages() {
    final List<Language> sortedLanguages = Arrays.asList(Language.REAL_LANGUAGES);
    Collections.sort(sortedLanguages, new Comparator<Language>() {
      @Override
      public int compare(Language o1, Language o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
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

  /*private boolean isAutoDetected(String code) {
    if (LanguageIdentifier.getSupportedLanguages().contains(code)) {
      return true;
    }
    final Set<String> additionalCodes = new HashSet<String>(Arrays.asList(LanguageIdentifierTools.ADDITIONAL_LANGUAGES));
    if (additionalCodes.contains(code)) {
      return true;
    }
    return false;
  }*/

  private class JavaFilter implements FileFilter {

    private final String langName;

    public JavaFilter(String langName) {
      this.langName = langName;
    }

    @Override
    public boolean accept(final File f) {
      final String filename = f.getName();
      final boolean isAbstractTopClass = filename.endsWith(langName + "Rule.java");
      final boolean isSpellerClass = filename.endsWith("SpellerRule.java");
      return filename.endsWith(".java") && !isAbstractTopClass && !isSpellerClass;
    }

  }

}
