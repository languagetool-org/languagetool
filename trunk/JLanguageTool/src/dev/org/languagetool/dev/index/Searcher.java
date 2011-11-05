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
package de.danielnaber.languagetool.dev.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.RuleMatch;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import de.danielnaber.languagetool.rules.patterns.PatternRule;
import de.danielnaber.languagetool.rules.patterns.PatternRuleLoader;

/**
 * A class with a main() method that takes a rule id (of a simple rule) and the location of the
 * index that runs the query on that index and prints all matches
 * 
 * @author Tao Lin
 */
public class Searcher {

  private static final int MAX_HITS = 1000;

  public TopDocs run(PatternRule rule, IndexSearcher searcher, boolean checkUnsupportedRule)
      throws IOException {
    final PatternRuleQueryBuilder patternRuleQueryBuilder = new PatternRuleQueryBuilder();
    final Query query = patternRuleQueryBuilder.buildQuery(rule, checkUnsupportedRule);
    //System.out.println("QUERY: " + query);
    return searcher.search(query, MAX_HITS);
  }

  public List<TopDocs> run(String ruleId, File xmlRuleFile, IndexSearcher searcher,
                            boolean checkUnsupportedRule) throws IOException {
    final List<TopDocs> topDocsList = new ArrayList<TopDocs>();
    final List<PatternRule> rules = loadRules(xmlRuleFile);
    boolean foundRule = false;
    for (PatternRule rule : rules) {
      if (rule.getId().equals(ruleId)) {
        final TopDocs topDocs = run(rule, searcher, checkUnsupportedRule);
        topDocsList.add(topDocs);
        foundRule = true;
      }
    }
    if (!foundRule) {
      throw new PatternRuleNotFoundException(ruleId);
    }
    return topDocsList;
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
      throws IOException {
    if (!xmlRuleFile.exists() || !xmlRuleFile.canRead()) {
      System.out.println("Rule XML file '" + xmlRuleFile.getAbsolutePath()
          + "' does not exist or is not readable, please check the path");
      System.exit(1);
    }
    final IndexSearcher searcher = new IndexSearcher(FSDirectory.open(indexDir));
    
    try {
      final List<TopDocs> matchesList = run(ruleId, xmlRuleFile, searcher, true);
      for (TopDocs matches : matchesList) {
        printResult(matches, searcher, null);
      }
    } catch (UnsupportedPatternRuleException e) {
      System.out.println(e.getMessage() + " Will run checking on potential matches (this can be slow):");
      final List<TopDocs> potentialMatchesList = run(ruleId, xmlRuleFile, searcher, false);
      final JLanguageTool languageTool = getLanguageToolWithOneRule(ruleId, xmlRuleFile);
      for (TopDocs potentialMatches : potentialMatchesList) {
        printResult(potentialMatches, searcher, languageTool);
      }
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

    for (int i = 0; i < Math.min(docs.totalHits, MAX_HITS);) {
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
    if (docs.totalHits > MAX_HITS) {
      System.out.println("NOTE: matches skipped due to maximum hit limit of " + MAX_HITS);
    }
  }

  private static void ensureCorrectUsageOrExit(String[] args) {
    if (args.length != 3) {
      System.err.println("Usage: Searcher <ruleId> <ruleXML> <indexDir>");
      System.err.println("\truleId Id of the rule to search");
      System.err.println("\truleXML path to a rule file, e.g. en/grammar.xml");
      System.err.println("\tindexDir path to a directory storing the index");
      System.exit(1);
    }
  }

  public static void main(String[] args) throws Exception {
    ensureCorrectUsageOrExit(args);
    final Searcher searcher = new Searcher();
    searcher.run(args[0], new File(args[1]), new File(args[2]));
  }

}
