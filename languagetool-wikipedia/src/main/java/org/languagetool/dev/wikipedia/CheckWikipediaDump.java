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
package org.languagetool.dev.wikipedia;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.Rule;
import org.xml.sax.SAXException;

/**
 * Command-line tool that checks texts from Wikipedia (download "pages-articles.xml.bz2" from
 * http://download.wikimedia.org/backup-index.html, e.g.
 * http://download.wikimedia.org/dewiki/latest/dewiki-latest-pages-articles.xml.bz2)
 * and stores the result in a database.
 * 
 * @author Daniel Naber
 */
public class CheckWikipediaDump {

  private CheckWikipediaDump() {
    // no public constructor
  }
  
  public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
    final CheckWikipediaDump prg = new CheckWikipediaDump();
    ensureCorrectUsageOrExit(args);
    File propFile = null;
    if (!"-".equals(args[0])) {
      propFile = new File(args[0]);
      if (!propFile.exists() || propFile.isDirectory()) {
        throw new IOException("File not found or isn't a file: " + propFile.getAbsolutePath());
      }
    }
    final String languageCode = args[2];
    final Set<String> disabledRuleIds = new HashSet<String>();
    if (!"-".equals(args[1])) {
      final File disabledRulesPropFile = new File(args[1]);
      if (!disabledRulesPropFile.exists() || disabledRulesPropFile.isDirectory()) {
        throw new IOException("File not found or isn't a file: " + disabledRulesPropFile.getAbsolutePath());
      }
      final Properties disabledRules = new Properties();
      FileInputStream stream = new FileInputStream(disabledRulesPropFile);
      try {
        disabledRules.load(stream);
        addDisabledRules("all", disabledRuleIds, disabledRules);
        addDisabledRules(languageCode, disabledRuleIds, disabledRules);
      } finally {
        stream.close();
      }
    }
    final int maxArticles = Integer.parseInt(args[5]);
    final int maxErrors = Integer.parseInt(args[6]);
    String[] ruleIds = null;
    if (!"-".equals(args[4])) {
      ruleIds = args[4].split(",");
    }
    prg.run(propFile, disabledRuleIds, languageCode, args[3], ruleIds, maxArticles, maxErrors);
  }

  private static void addDisabledRules(String languageCode, Set<String> disabledRuleIds, Properties disabledRules) {
    final String disabledRulesString = disabledRules.getProperty(languageCode);
    if (disabledRulesString != null) {
      final String[] ids = disabledRulesString.split(",");
      disabledRuleIds.addAll(Arrays.asList(ids));
    }
  }

  private static void ensureCorrectUsageOrExit(String[] args) {
    if (args.length != 7) {
      System.err.println("Usage: CheckWikipediaDump <propertyFile> <rulePropertyFile> <language> <filename> <ruleIds> <maxArticles> <maxErrors>");
      System.err.println("  propertyFile      a file to set database access properties. Use '-' to print results to stdout.");
      System.err.println("  rulePropertyFile  a file to set rules which should be disabled per language (e.g. en=RULE1,RULE2 or all=RULE3,RULE4). Use '-' to ignore.");
      System.err.println("  language          language code like 'en' or 'de'");
      System.err.println("  filename          path to unpacked Wikipedia XML dump;");
      System.err.println("                    dumps are available from http://dumps.wikimedia.org/backup-index.html");
      System.err.println("  ruleIds           comma-separated list of rule-ids to activate. Use '-' to activate the default rules.");
      System.err.println("  maxArticles       maximum number of articles to check, 0 for no limit");
      System.err.println("  maxErrors         stop when reaching this many errors, 0 for no limit");
      System.exit(1);
    }
  }

  private void run(File propFile, Set<String> disabledRules, String langCode, String xmlFileName, String[] ruleIds, int maxArticles, int maxErrors)
      throws IOException, SAXException, ParserConfigurationException {
    //final long startTime = System.currentTimeMillis();
    final File file = new File(xmlFileName);
    if (!file.exists() || !file.isFile()) {
      throw new IOException("File doesn't exist or isn't a file: " + xmlFileName);
    }
    final Language lang = Language.getLanguageForShortName(langCode);
    final JLanguageTool languageTool = new JLanguageTool(lang);
    languageTool.activateDefaultPatternRules();
    if (ruleIds != null) {
      enableSpecifiedRules(ruleIds, languageTool);
    } else {
      applyRuleDeactivation(languageTool, disabledRules);
    }
    disableSpellingRules(languageTool);
    final Date dumpDate = getDumpFileDate(file);
    System.out.println("Dump date: " + dumpDate + ", language: " + langCode);
    System.out.println("Article limit: " + (maxArticles > 0 ? maxArticles : "no limit"));
    System.out.println("Error limit: " + (maxErrors > 0 ? maxErrors : "no limit"));
    BaseWikipediaDumpHandler xmlHandler = null;
    try {
      if (propFile != null) {
        xmlHandler = new DatabaseDumpHandler(languageTool, dumpDate, langCode, propFile, lang);
      } else {
        xmlHandler = new OutputDumpHandler(languageTool, dumpDate, langCode, lang);
      }
      xmlHandler.setMaximumArticles(maxArticles);
      xmlHandler.setMaximumErrors(maxErrors);
      final SAXParserFactory factory = SAXParserFactory.newInstance();
      final SAXParser saxParser = factory.newSAXParser();
      saxParser.parse(file, xmlHandler);
    } catch (ErrorLimitReachedException e) {
      System.out.println(e);
    } catch (ArticleLimitReachedException e) {
      System.out.println(e);
    } finally {
      if (xmlHandler != null) {
        final float matchesPerDoc = (float)xmlHandler.getRuleMatchCount() / xmlHandler.getArticleCount();
        System.out.printf(lang + ": %d total matches\n", xmlHandler.getRuleMatchCount());
        System.out.printf(lang + ": ø%.2f rule matches per document\n", matchesPerDoc);
        //System.out.printf(lang + ": %s total runtime\n", getRunTime(startTime));
        xmlHandler.close();
      }
    }
  }

  private void enableSpecifiedRules(String[] ruleIds, JLanguageTool languageTool) {
    for (Rule rule : languageTool.getAllRules()) {
      languageTool.disableRule(rule.getId());
    }
    for (String ruleId : ruleIds) {
      languageTool.enableRule(ruleId);
    }
    for (Rule rule : languageTool.getAllRules()) {
      if (rule.isDefaultOff()) {
        languageTool.enableDefaultOffRule(rule.getId());
      }
    }
    for (String ruleId : ruleIds) {
      boolean found = false;
      for (Rule rule : languageTool.getAllRules()) {
        if (rule.getId().equals(ruleId)) {
            found = true;
            break;
        }
      }
      if (!found) {
        System.out.println("WARNING: Could not find rule '" + ruleId + "'");
      }
    }
    System.out.println("Only these rules are enabled: " + Arrays.toString(ruleIds));
  }

  private void applyRuleDeactivation(JLanguageTool languageTool, Set<String> disabledRules) throws IOException {
    // disabled via config file, usually to avoid too many false alarms:
    for (String disabledRuleId : disabledRules) {
      languageTool.disableRule(disabledRuleId);
    }
    System.out.println("These rules are disabled: " + languageTool.getDisabledRules());
  }

  private void disableSpellingRules(JLanguageTool languageTool) {
    final List<Rule> allActiveRules = languageTool.getAllActiveRules();
    for (Rule rule : allActiveRules) {
      if (rule.isSpellingRule()) {
        languageTool.disableRule(rule.getId());
      }
    }
    System.out.println("All spelling rules are disabled");
  }

  private Date getDumpFileDate(File file) throws IOException {
    final String filename = file.getName();
    final String[] parts = filename.split("-");
    if (parts.length < 3) {
      throw new IOException("Unexpected filename format: " + file.getName() + ", must be like ??wiki-????????-pages-articles.xml");
    }
    final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    try {
      return sdf.parse(parts[1]);
    } catch (ParseException e) {
      throw new IOException("Unexpected date format '" + parts[1] + "', must be yyyymmdd", e);
    }
  }

  private String getRunTime(long startTime) {
    final long runtime = System.currentTimeMillis() - startTime;
    return String.format("%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(runtime),
            TimeUnit.MILLISECONDS.toSeconds(runtime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(runtime))
    );
  }

}
