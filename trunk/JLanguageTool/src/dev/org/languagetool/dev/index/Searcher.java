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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TimeLimitingCollector;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Counter;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.patterns.PatternRuleLoader;

/**
 * A class with a main() method that takes a rule id  and the location of the
 * index that runs the query on that index and prints all matches.
 * Will transparently handle rules that are not supported, i.e. run on the candidate matches
 * up to a limit.
 * 
 * @author Tao Lin
 * @author Daniel Naber
 */
public class Searcher {

  private int maxHits = 1000;
  private int maxSearchTimeMillis = 5000;

  public Searcher() {
  }

  public SearcherResult findRuleMatchesOnIndex(PatternRule rule, Language language, File indexDir) throws IOException {
    final IndexSearcher indexSearcher = new IndexSearcher(FSDirectory.open(indexDir));
    try {
      return findRuleMatchesOnIndex(rule, language, indexSearcher);
    } finally {
      indexSearcher.close();
    }
  }

  public int getMaxHits() {
    return maxHits;
  }

  public void setMaxHits(int maxHits) {
    this.maxHits = maxHits;
  }

  public int getMaxSearchTimeMillis() {
    return maxSearchTimeMillis;
  }

  public void setMaxSearchTimeMillis(int maxSearchTimeMillis) {
    this.maxSearchTimeMillis = maxSearchTimeMillis;
  }

  public SearcherResult findRuleMatchesOnIndex(PatternRule rule, Language language, IndexSearcher indexSearcher) throws IOException {
    final PossiblyRelaxedQuery query = createQuery(rule);
    final Sort sort = new Sort(new SortField("docCount", SortField.INT));  // do not sort by relevance as this will move the shortest documents to the top
    if (query.query == null) {
      throw new NullPointerException("Cannot search on null query for rule: " + rule);
    }
    final PossiblyLimitedTopDocs limitedTopDocs = getTopDocs(indexSearcher, query, sort);
    final JLanguageTool languageTool = getLanguageToolWithOneRule(language, rule);
    final List<MatchingSentence> matchingSentences = findMatchingSentences(indexSearcher, limitedTopDocs.topDocs, languageTool);
    final int sentencesChecked = getSentenceCheckCount(query, limitedTopDocs.topDocs, indexSearcher);
    final SearcherResult searcherResult = new SearcherResult(matchingSentences, sentencesChecked, query);
    searcherResult.setResultIsTimeLimited(limitedTopDocs.resultIsTimeLimited);
    return searcherResult;
  }

  private PossiblyLimitedTopDocs getTopDocs(IndexSearcher indexSearcher, PossiblyRelaxedQuery query, Sort sort) throws IOException {
    final TopFieldCollector topCollector = TopFieldCollector.create(sort, maxHits, true, false, false, false);
    final TimeLimitingCollector collector = new TimeLimitingCollector(topCollector, Counter.newCounter(), maxSearchTimeMillis);
    boolean timeLimitActivated = false;
    try {
      indexSearcher.search(query.query, collector);
    } catch (TimeLimitingCollector.TimeExceededException e) {
      timeLimitActivated = true;
    }
    return new PossiblyLimitedTopDocs(topCollector.topDocs(), timeLimitActivated);
  }

  PatternRule getRuleById(String ruleId, File xmlRuleFile) throws IOException {
    final PatternRuleLoader ruleLoader = new PatternRuleLoader();
    final List<PatternRule> rules = ruleLoader.getRules(xmlRuleFile);
    for (PatternRule rule : rules) {
      if (rule.getId().equals(ruleId)) {
        return rule;
      }
    }
    throw new PatternRuleNotFoundException(ruleId, xmlRuleFile);
  }

  private int getSentenceCheckCount(PossiblyRelaxedQuery query, TopDocs topDocs, IndexSearcher indexSearcher) {
    final int sentencesChecked;
    final int indexSize = indexSearcher.getIndexReader().numDocs();
    if (query.isRelaxed) {
      // unsupported rules: we actually check up to maxHits sentences:
      sentencesChecked = Math.min(maxHits, indexSize);
    } else {
      // supported rules: no need to run LT (other than getting the exact match position), so we can claim
      // that we really have checked all the sentences in the index:
      sentencesChecked = indexSize;
    }
    return sentencesChecked;
  }

  private List<MatchingSentence> findMatchingSentences(IndexSearcher indexSearcher, TopDocs topDocs, JLanguageTool languageTool) throws IOException {
    final List<MatchingSentence> matchingSentences = new ArrayList<MatchingSentence>();
    //long t = System.currentTimeMillis();
    for (ScoreDoc match : topDocs.scoreDocs) {
      final Document doc = indexSearcher.doc(match.doc);
      final String sentence = doc.get(PatternRuleQueryBuilder.FIELD_NAME);
      final List<RuleMatch> ruleMatches = languageTool.check(sentence);
      if (ruleMatches.size() > 0) {
        final MatchingSentence matchingSentence = new MatchingSentence(sentence, ruleMatches);
        matchingSentences.add(matchingSentence);
      }
    }
    //System.out.println(">>>" + (System.currentTimeMillis() - t) + "ms for " + topDocs.scoreDocs.length + " docs");
    return matchingSentences;
  }

  private PossiblyRelaxedQuery createQuery(PatternRule rule) {
    final PatternRuleQueryBuilder patternRuleQueryBuilder = new PatternRuleQueryBuilder();
    Query query;
    boolean relaxed;
    try {
      query = patternRuleQueryBuilder.buildQuery(rule);
      relaxed = false;
    } catch (UnsupportedPatternRuleException e) {
      query = patternRuleQueryBuilder.buildPossiblyRelaxedQuery(rule);
      relaxed = true;
    }
    return new PossiblyRelaxedQuery(query, relaxed);
  }

  private JLanguageTool getLanguageToolWithOneRule(Language lang, PatternRule patternRule) throws IOException {
    final JLanguageTool langTool = new JLanguageTool(lang);
    for (Rule rule : langTool.getAllActiveRules()) {
      langTool.disableRule(rule.getId());
    }
    langTool.addRule(patternRule);
    return langTool;
  }

  class PossiblyLimitedTopDocs {
    TopDocs topDocs;
    boolean resultIsTimeLimited;

    PossiblyLimitedTopDocs(TopDocs topDocs, boolean resultIsTimeLimited) {
      this.topDocs = topDocs;
      this.resultIsTimeLimited = resultIsTimeLimited;
    }
  }

  class PossiblyRelaxedQuery {

    Query query;
    boolean isRelaxed;

    PossiblyRelaxedQuery(Query query, boolean relaxed) {
      this.query = query;
      isRelaxed = relaxed;
    }

    @Override
    public String toString() {
      return query.toString() + "[relaxed=" + isRelaxed + "]";
    }
  }

  private static void ensureCorrectUsageOrExit(String[] args) {
    if (args.length != 4) {
      System.err.println("Usage: Searcher <ruleId> <ruleXML> <languageCode> <indexDir>");
      System.err.println("\truleId       Id of the rule to search for");
      System.err.println("\truleXML      path to a rule file, e.g. en/grammar.xml");
      System.err.println("\tlanguageCode short language code, e.g. en for English");
      System.err.println("\tindexDir     path to a directory containing the index");
      System.exit(1);
    }
  }

  public static void main(String[] args) throws Exception {
    ensureCorrectUsageOrExit(args);
    final Searcher searcher = new Searcher();
    final String ruleId = args[0];
    final File ruleFile = new File(args[1]);
    final String languageCode = args[2];
    final Language language = Language.getLanguageForShortName(languageCode);
    if (language == null) {
      throw new RuntimeException("Unknown language code '" + languageCode + "'");
    }
    final File indexDir = new File(args[3]);
    final PatternRule rule = searcher.getRuleById(ruleId, ruleFile);
    final SearcherResult searcherResult = searcher.findRuleMatchesOnIndex(rule, language, indexDir);
    int i = 1;
    for (MatchingSentence ruleMatch : searcherResult.getMatchingSentences()) {
      System.out.println(i + ": " + ruleMatch.getSentence());
      i++;
    }
  }

}
