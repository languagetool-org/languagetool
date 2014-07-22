/* LanguageTool, a natural language style checker 
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.dumpcheck;

import org.apache.commons.cli.*;
import org.apache.commons.lang.StringUtils;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.MultiThreadedJLanguageTool;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Checks texts from one or more {@link org.languagetool.dev.dumpcheck.SentenceSource}s.
 * @since 2.4
 */
public class SentenceSourceChecker {

  private SentenceSourceChecker() {
    // no public constructor
  }

  public static void main(String[] args) throws IOException {
    final SentenceSourceChecker prg = new SentenceSourceChecker();
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
    final int maxArticles = Integer.parseInt(commandLine.getOptionValue("max-sentences", "0"));
    final int maxErrors = Integer.parseInt(commandLine.getOptionValue("max-errors", "0"));
    String[] ruleIds = commandLine.hasOption('r') ? commandLine.getOptionValue('r').split(",") : null;
    String[] categoryIds = commandLine.hasOption("also-enable-categories") ?
                           commandLine.getOptionValue("also-enable-categories").split(",") : null;
    String[] fileNames = commandLine.getOptionValues('f');
    File languageModelDir = commandLine.hasOption("languagemodel") ?
                            new File(commandLine.getOptionValue("languagemodel")) : null;
    prg.run(propFile, disabledRuleIds, languageCode, Arrays.asList(fileNames), ruleIds, categoryIds, maxArticles, maxErrors, languageModelDir);
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
                    "The file needs to set the properties dbUrl ('jdbc:...'), dbUser, and dbPassword. " +
                    "It can optionally define the batchSize for insert statements, which defaults to 1.")
            .create("d"));
    options.addOption(OptionBuilder.withLongOpt("rule-properties").withArgName("file").hasArg()
            .withDescription("A file to set rules which should be disabled per language (e.g. en=RULE1,RULE2 or all=RULE3,RULE4)")
            .create());
    options.addOption(OptionBuilder.withLongOpt("rule-ids").withArgName("id").hasArg()
            .withDescription("comma-separated list of rule-ids to activate")
            .create("r"));
    options.addOption(OptionBuilder.withLongOpt("also-enable-categories").withArgName("categories").hasArg()
            .withDescription("comma-separated list of categories to activate, additionally to rules activated anyway")
            .create());
    options.addOption(OptionBuilder.withLongOpt("file").withArgName("file").hasArg()
            .withDescription("an unpacked Wikipedia XML dump; (must be named *.xml, dumps are available from http://dumps.wikimedia.org/backup-index.html) " +
                    "or a Tatoeba CSV file filtered to contain only one language (must be named tatoeba-*). You can specify this option more than once.")
            .isRequired()
            .create("f"));
    options.addOption(OptionBuilder.withLongOpt("max-sentences").withArgName("number").hasArg()
            .withDescription("maximum number of sentences to check")
            .create());
    options.addOption(OptionBuilder.withLongOpt("max-errors").withArgName("number").hasArg()
            .withDescription("maximum number of errors, stop when finding more")
            .create());
    options.addOption(OptionBuilder.withLongOpt("languagemodel").withArgName("indexDir").hasArg()
            .withDescription("directory with a '3grams' sub directory that contains an ngram index")
            .create());
    try {
      CommandLineParser parser = new GnuParser();
      return parser.parse(options, args);
    } catch (org.apache.commons.cli.ParseException e) {
      System.err.println("Error: " + e.getMessage());
      HelpFormatter formatter = new HelpFormatter();
      formatter.setWidth(80);
      formatter.setSyntaxPrefix("Usage: ");
      formatter.printHelp(SentenceSourceChecker.class.getSimpleName() + " [OPTION]... --file <file> --language <code>", options);
      System.exit(1);
    }
    return null;
  }

  private void run(File propFile, Set<String> disabledRules, String langCode, List<String> fileNames, String[] ruleIds,
                   String[] additionalCategoryIds, int maxSentences, int maxErrors, File languageModelDir) throws IOException {
    final Language lang = Language.getLanguageForShortName(langCode);
    final JLanguageTool languageTool = new MultiThreadedJLanguageTool(lang);
    languageTool.activateDefaultPatternRules();
    if (languageModelDir != null) {
      languageTool.activateLanguageModelRules(languageModelDir);
    }
    if (ruleIds != null) {
      enableOnlySpecifiedRules(ruleIds, languageTool);
    } else {
      applyRuleDeactivation(languageTool, disabledRules);
    }
    activateAdditionalCategories(additionalCategoryIds, languageTool);
    disableSpellingRules(languageTool);
    System.out.println("Working on: " + StringUtils.join(fileNames, ", "));
    System.out.println("Sentence limit: " + (maxSentences > 0 ? maxSentences : "no limit"));
    System.out.println("Error limit: " + (maxErrors > 0 ? maxErrors : "no limit"));

    ResultHandler resultHandler = null;
    int ruleMatchCount = 0;
    int sentenceCount = 0;
    try {
      if (propFile != null) {
        resultHandler = new DatabaseHandler(propFile, maxSentences, maxErrors);
      } else {
        resultHandler = new StdoutHandler(maxSentences, maxErrors);
      }
      MixingSentenceSource mixingSource = MixingSentenceSource.create(fileNames, lang);
      while (mixingSource.hasNext()) {
        Sentence sentence = mixingSource.next();
        List<RuleMatch> matches = languageTool.check(sentence.getText());
        resultHandler.handleResult(sentence, matches, lang);
        sentenceCount++;
        ruleMatchCount += matches.size();
      }
    } catch (ErrorLimitReachedException | DocumentLimitReachedException e) {
      System.out.println(e);
    } finally {
      if (resultHandler != null) {
        final float matchesPerSentence = (float)ruleMatchCount / sentenceCount;
        System.out.printf(lang + ": %d total matches\n", ruleMatchCount);
        System.out.printf(lang + ": ø%.2f rule matches per sentence\n", matchesPerSentence);
        try {
          resultHandler.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  private void enableOnlySpecifiedRules(String[] ruleIds, JLanguageTool languageTool) {
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
    warnOnNonExistingRuleIds(ruleIds, languageTool);
    System.out.println("Only these rules are enabled: " + Arrays.toString(ruleIds));
  }

  private void warnOnNonExistingRuleIds(String[] ruleIds, JLanguageTool languageTool) {
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
  }

  private void applyRuleDeactivation(JLanguageTool languageTool, Set<String> disabledRules) {
    // disabled via config file, usually to avoid too many false alarms:
    for (String disabledRuleId : disabledRules) {
      languageTool.disableRule(disabledRuleId);
    }
    System.out.println("These rules are disabled: " + languageTool.getDisabledRules());
  }

  private void activateAdditionalCategories(String[] additionalCategoryIds, JLanguageTool languageTool) {
    if (additionalCategoryIds != null) {
      for (String categoryId : additionalCategoryIds) {
        for (Rule rule : languageTool.getAllRules()) {
          if (rule.getCategory().getName().equals(categoryId)) {
            System.out.println("Activating " + rule.getId() + " in category " + categoryId);
            languageTool.enableDefaultOffRule(rule.getId());
          }
        }
      }
    }
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

}
