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
package org.languagetool.dev.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.patterns.PatternRuleLoader;

/**
 * A class with a main() method that takes a rule id (of a simple rule) and the location of the
 * index that runs the query on that index and prints all matches
 * 
 * @author Tao Lin
 */
public class Searcher {

  private static final int DEFAULT_MAX_HITS = 1000;

  private int maxHits = DEFAULT_MAX_HITS;

  public Searcher() {
  }

  public int getMaxHits() {
    return maxHits;
  }

  public void setMaxHits(int maxHits) {
    this.maxHits = maxHits;
  }

  public List<RuleMatch> findRuleMatchesOnIndex(PatternRule rule, Language language, File indexDir) throws IOException {
    final IndexSearcher indexSearcher = new IndexSearcher(FSDirectory.open(indexDir));
    try {
      return findRuleMatchesOnIndex(rule, language, indexSearcher);
    } finally {
      indexSearcher.close();
    }
  }

  public List<RuleMatch> findRuleMatchesOnIndex(PatternRule rule, Language language, IndexSearcher indexSearcher) throws IOException {
    final List<RuleMatch> matches = new ArrayList<RuleMatch>();
    final Query query = createQuery(rule);
    final TopDocs topDocs = indexSearcher.search(query, maxHits);
    final JLanguageTool languageTool = getLanguageToolWithOneRule(language, rule);
    for (ScoreDoc match : topDocs.scoreDocs) {
      final Document doc = indexSearcher.doc(match.doc);
      final String sentence = doc.get(PatternRuleQueryBuilder.FIELD_NAME);
      final List<RuleMatch> ruleMatches = languageTool.check(sentence);
      matches.addAll(ruleMatches);
    }
    return matches;
  }

  private Query createQuery(PatternRule rule) {
    final PatternRuleQueryBuilder patternRuleQueryBuilder = new PatternRuleQueryBuilder();
    Query query;
    try {
      query = patternRuleQueryBuilder.buildQuery(rule);
    } catch (UnsupportedPatternRuleException e) {
      query = patternRuleQueryBuilder.buildPossiblyRelaxedQuery(rule);
    }
    return query;
  }

  public TopDocs run(PatternRule rule, IndexSearcher searcher, boolean throwExceptionOnUnsupportedRule)
       throws IOException, UnsupportedPatternRuleException {
    final PatternRuleQueryBuilder patternRuleQueryBuilder = new PatternRuleQueryBuilder();
    final Query query;
    if (throwExceptionOnUnsupportedRule) {
      query = patternRuleQueryBuilder.buildQuery(rule);
    } else {
      query = patternRuleQueryBuilder.buildPossiblyRelaxedQuery(rule);
    }
    return searcher.search(query, maxHits);
  }

  public TopDocs run(String ruleId, File xmlRuleFile, IndexSearcher searcher, boolean checkUnsupportedRule) throws IOException, UnsupportedPatternRuleException {
    final PatternRule rule = getRuleById(ruleId, xmlRuleFile);
    final TopDocs topDocs = run(rule, searcher, checkUnsupportedRule);
    return topDocs;
  }

  private PatternRule getRuleById(String ruleId, File xmlRuleFile) throws IOException {
    final List<PatternRule> rules = loadRules(xmlRuleFile);
    for (PatternRule rule : rules) {
      if (rule.getId().equals(ruleId)) {
        return rule;
      }
    }
    throw new PatternRuleNotFoundException(ruleId, xmlRuleFile);
  }

  private JLanguageTool getLanguageToolWithOneRule(Language lang, PatternRule patternRule) throws IOException {
    final JLanguageTool langTool = new JLanguageTool(lang);
    for (Rule rule : langTool.getAllActiveRules()) {
      langTool.disableRule(rule.getId());
    }
    langTool.addRule(patternRule);
    return langTool;
  }

  private List<PatternRule> loadRules(File xmlRuleFile) throws IOException {
    final InputStream xmlRulesStream = new FileInputStream(xmlRuleFile);
    try {
      final PatternRuleLoader ruleLoader = new PatternRuleLoader();
      return ruleLoader.getRules(xmlRulesStream, xmlRuleFile.getAbsolutePath());
    } finally {
      xmlRulesStream.close();
    }
  }

  private void run(String ruleId, File xmlRuleFile, File indexDir)
        throws IOException, UnsupportedPatternRuleException {
    if (!xmlRuleFile.exists() || !xmlRuleFile.canRead()) {
      System.out.println("Rule XML file '" + xmlRuleFile.getAbsolutePath()
          + "' does not exist or is not readable, please check the path");
      System.exit(1);
    }
    final IndexSearcher searcher = new IndexSearcher(FSDirectory.open(indexDir));
    
    try {
      final TopDocs topDocs = run(ruleId, xmlRuleFile, searcher, true);
      printResult(topDocs, searcher, null);
    } catch (UnsupportedPatternRuleException e) {
      System.out.println(e.getMessage() + " Will run checking on potential matches (this can be slow):");
      final TopDocs topDocs = run(ruleId, xmlRuleFile, searcher, false);
      final JLanguageTool languageTool = getLanguageToolWithOneRule(ruleId, xmlRuleFile);
      printResult(topDocs, searcher, languageTool);
    }
    searcher.close();
  }

  private JLanguageTool getLanguageToolWithOneRule(String ruleId, File xmlRuleFile) throws IOException {
    final JLanguageTool languageTool = new JLanguageTool(Language.DEMO);
    final List<PatternRule> patternRules = loadRules(xmlRuleFile);
    for (PatternRule patternRule : patternRules) {
      if (ruleId.equals(patternRule.getId())) {
        languageTool.addRule(patternRule);
      }
    }
    return languageTool;
  }

  private void printResult(TopDocs docs, IndexSearcher searcher, JLanguageTool languageTool)
      throws IOException {

    final ScoreDoc[] hits = docs.scoreDocs;
    if (languageTool != null) {
      System.out.println("Potential search results: " + docs.totalHits);
    } else {
      System.out.println("Search results: " + docs.totalHits);
    }

    for (int i = 0; i < Math.min(docs.totalHits, maxHits);) {
      final Document doc = searcher.doc(hits[i++].doc);
      final String sentence = doc.get(PatternRuleQueryBuilder.FIELD_NAME);
      if (languageTool != null) {
        final List<RuleMatch> ruleMatches = languageTool.check(sentence);
        if (ruleMatches.size() > 0) {
          System.out.println(i + ": " + sentence);
        }
      } else {
        System.out.println(i + ": " + sentence);
      }
    }
    if (docs.totalHits > maxHits) {
      System.out.println("NOTE: matches skipped due to maximum hit limit of " + maxHits);
    }
  }

  private static void ensureCorrectUsageOrExit(String[] args) {
    if (args.length != 3) {
      System.err.println("Usage: Searcher <ruleId> <ruleXML> <indexDir>");
      System.err.println("\truleId Id of the rule to search");
      System.err.println("\truleXML path to a rule file, e.g. en/grammar.xml");
      System.err.println("\tindexDir path to a directory containing the index");
      System.exit(1);
    }
  }

  public static void main(String[] args) throws Exception {
    ensureCorrectUsageOrExit(args);
    final Searcher searcher = new Searcher();
    searcher.run(args[0], new File(args[1]), new File(args[2]));
  }

}
