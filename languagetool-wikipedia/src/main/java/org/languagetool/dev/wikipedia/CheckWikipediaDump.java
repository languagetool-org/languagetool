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

import org.apache.commons.cli.*;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.MultiThreadedJLanguageTool;
import org.languagetool.dev.dumpcheck.ArticleLimitReachedException;
import org.languagetool.dev.dumpcheck.ErrorLimitReachedException;
import org.languagetool.rules.Rule;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Command-line tool that checks texts from Wikipedia (download "pages-articles.xml.bz2" from
 * http://download.wikimedia.org/backup-index.html, e.g.
 * http://download.wikimedia.org/dewiki/latest/dewiki-latest-pages-articles.xml.bz2)
 * and stores the result in a database.
 * 
 * @author Daniel Naber
 * @deprecated use {@link org.languagetool.dev.dumpcheck.SentenceSourceChecker} instead (deprecated since 2.4)
 */
@Deprecated
public class CheckWikipediaDump {

  private CheckWikipediaDump() {
    // no public constructor
  }
  
  public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
    System.err.println("*** Note: this class has been deprecated - please use option 'check-data' instead");
    final CheckWikipediaDump prg = new CheckWikipediaDump();
    final CommandLine commandLine = ensureCorrectUsageOrExit(args);
    File propFile = null;
    if (commandLine.hasOption('d')) {
      propFile = new File(commandLine.getOptionValue('d'));
      if (!propFile.exists() || propFile.isDirectory()) {
        throw new IOException("File not found or isn't a file: " + propFile.getAbsolutePath());
      }
    }
    final String languageCode = commandLine.getOptionValue('l');
    final Set<String> disabledRuleIds = new HashSet<>();
    if (commandLine.hasOption("rule-properties")) {
      final File disabledRulesPropFile = new File(commandLine.getOptionValue("rule-properties"));
      if (!disabledRulesPropFile.exists() || disabledRulesPropFile.isDirectory()) {
        throw new IOException("File not found or isn't a file: " + disabledRulesPropFile.getAbsolutePath());
      }
      final Properties disabledRules = new Properties();
      try (FileInputStream stream = new FileInputStream(disabledRulesPropFile)) {
        disabledRules.load(stream);
        addDisabledRules("all", disabledRuleIds, disabledRules);
        addDisabledRules(languageCode, disabledRuleIds, disabledRules);
      }
    }
    final int maxArticles = Integer.parseInt(commandLine.getOptionValue("max-articles", "0"));
    final int maxErrors = Integer.parseInt(commandLine.getOptionValue("max-errors", "0"));
    String[] ruleIds = null;
    if (commandLine.hasOption('r')) {
      ruleIds = commandLine.getOptionValue('r').split(",");
    }
    prg.run(propFile, disabledRuleIds, languageCode, commandLine.getOptionValue('f'), ruleIds, maxArticles, maxErrors);
  }

  private static void addDisabledRules(String languageCode, Set<String> disabledRuleIds, Properties disabledRules) {
    final String disabledRulesString = disabledRules.getProperty(languageCode);
    if (disabledRulesString != null) {
      final String[] ids = disabledRulesString.split(",");
      disabledRuleIds.addAll(Arrays.asList(ids));
    }
  }

  @SuppressWarnings("AccessStaticViaInstance")
  private static CommandLine ensureCorrectUsageOrExit(String[] args) {
    Options options = new Options();
    options.addOption(OptionBuilder.withLongOpt("language").withArgName("code").hasArg()
            .withDescription("language code like 'en' or 'de'")
            .isRequired()
            .create("l"));
    options.addOption(OptionBuilder.withLongOpt("db-properties").withArgName("file").hasArg()
            .withDescription("A file to set database access properties. If not set, the output will be written to STDOUT. " +
                    "The file needs to set dbDriver (fully qualified driver class), dbUrl ('jdbc:...'), dbUser, and dbPassword.")
            .create("d"));
    options.addOption(OptionBuilder.withLongOpt("rule-properties").withArgName("file").hasArg()
            .withDescription("A file to set rules which should be disabled per language (e.g. en=RULE1,RULE2 or all=RULE3,RULE4)")
            .create());
    options.addOption(OptionBuilder.withLongOpt("rule-ids").withArgName("id").hasArg()
            .withDescription("comma-separated list of rule-ids to activate")
            .create("r"));
    options.addOption(OptionBuilder.withLongOpt("file").withArgName("xmlfile").hasArg()
            .withDescription("an unpacked Wikipedia XML dump; dumps are available from http://dumps.wikimedia.org/backup-index.html")
            .isRequired()
            .create("f"));
    options.addOption(OptionBuilder.withLongOpt("max-articles").withArgName("number").hasArg()
            .withDescription("maximum number of articles to check")
            .create());
    options.addOption(OptionBuilder.withLongOpt("max-errors").withArgName("number").hasArg()
            .withDescription("maximum number of errors, stop when finding more")
            .create());
    try {
      CommandLineParser parser = new GnuParser();
      return parser.parse(options, args);
    } catch (org.apache.commons.cli.ParseException e) {
      System.err.println("Error: " + e.getMessage());
      HelpFormatter formatter = new HelpFormatter();
      formatter.setWidth(80);
      formatter.setSyntaxPrefix("Usage: ");
      formatter.printHelp(CheckWikipediaDump.class.getSimpleName() + " [OPTION]... --file <xmlfile> --language <code>", options);
      System.exit(1);
    }
    return null;
  }

  private void run(File propFile, Set<String> disabledRules, String langCode, String xmlFileName, String[] ruleIds, int maxArticles, int maxErrors)
      throws IOException, SAXException, ParserConfigurationException {
    //final long startTime = System.currentTimeMillis();
    final File file = new File(xmlFileName);
    if (!file.exists() || !file.isFile()) {
      throw new IOException("File doesn't exist or isn't a file: " + xmlFileName);
    }
    final Language lang = Language.getLanguageForShortName(langCode);
    final JLanguageTool languageTool = new MultiThreadedJLanguageTool(lang);
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
    } catch (ErrorLimitReachedException | ArticleLimitReachedException e) {
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

  private void applyRuleDeactivation(JLanguageTool languageTool, Set<String> disabledRules) {
    // disabled via config file, usually to avoid too many false alarms:
    for (String disabledRuleId : disabledRules) {
      languageTool.disableRule(disabledRuleId);
    }
    System.out.println("These rules are disabled: " + languageTool.getDisabledRules());
  }

  private void disableSpellingRules(JLanguageTool languageTool) {
    final List<Rule> allActiveRules = languageTool.getAllActiveRules();
    for (Rule rule : allActiveRules) {
      if (rule.isDictionaryBasedSpellingRule()) {
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
