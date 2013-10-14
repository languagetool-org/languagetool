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
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Counter;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.patterns.PatternRuleLoader;

import static org.languagetool.dev.index.PatternRuleQueryBuilder.*;
import static org.languagetool.dev.wikipedia.WikipediaIndexHandler.MAX_DOC_COUNT_FIELD;
import static org.languagetool.dev.wikipedia.WikipediaIndexHandler.MAX_DOC_COUNT_FIELD_VAL;
import static org.languagetool.dev.wikipedia.WikipediaIndexHandler.MAX_DOC_COUNT_VALUE;

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
  
  private Directory directory;
  private IndexSearcher indexSearcher;
  private DirectoryReader reader;

  public Searcher(Directory directory) throws IOException {
    //openIndex(directory);
    this.directory = directory;
  }

  private void open() throws IOException {
    reader = DirectoryReader.open(directory);
    indexSearcher = new IndexSearcher(reader);
    //System.out.println("Opened index " + directory + " with " + indexSearcher.getIndexReader().numDocs() + " docs");
  }

  private void close() throws IOException {
    if (reader != null) {
      reader.close();
    }
  }
  
  public int getDocCount() throws IOException {
    try (DirectoryReader reader = DirectoryReader.open(directory)) {
      final IndexSearcher indexSearcher = new IndexSearcher(reader);
      return getDocCount(indexSearcher);
    }
  }

  private int getDocCount(IndexSearcher indexSearcher) throws IOException {
    final Term searchTerm = new Term(MAX_DOC_COUNT_FIELD, MAX_DOC_COUNT_FIELD_VAL);
    final TopDocs search = indexSearcher.search(new TermQuery(searchTerm), 1);
    if (search.totalHits != 1) {
      return -1;
    }
    final ScoreDoc scoreDoc = search.scoreDocs[0];
    final Document doc = indexSearcher.doc(scoreDoc.doc);
    return Integer.parseInt(doc.get(MAX_DOC_COUNT_VALUE));
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

  public SearcherResult findRuleMatchesOnIndex(PatternRule rule, Language language) throws IOException, UnsupportedPatternRuleException {
    // it seems wasteful to re-open the index every time, but I had strange problems (OOM, Array out of bounds, ...)
    // when not doing so...
    open();
    try {
      final PatternRuleQueryBuilder patternRuleQueryBuilder = new PatternRuleQueryBuilder(language);
      final Query query = patternRuleQueryBuilder.buildRelaxedQuery(rule);
      if (query == null) {
        throw new NullPointerException("Cannot search on null query for rule: " + rule.getId());
      }
  
      final SearchRunnable runnable = new SearchRunnable(indexSearcher, query, language, rule);
      final Thread searchThread = new Thread(runnable);
      searchThread.start();
      try {
        // using a TimeLimitingCollector is not enough, as it doesn't cover all time required to
        // search for a complicated regex, so interrupt the whole thread instead:
        searchThread.join(maxSearchTimeMillis);
        searchThread.interrupt();
      } catch (InterruptedException e) {
        throw new RuntimeException("Search thread got interrupted for query " + query, e);
      }
      if (searchThread.isInterrupted()) {
        throw new SearchTimeoutException("Search timeout of " + maxSearchTimeMillis + "ms reached");
      }
      final Exception exception = runnable.getException();
      if (exception != null) {
        if (exception instanceof SearchTimeoutException) {
          throw (SearchTimeoutException)exception;
        }
        throw new RuntimeException("Exception during search for query " + query + " on rule " + rule.getId(), exception);
      }
  
      final List<MatchingSentence> matchingSentences = runnable.getMatchingSentences();
      final int sentencesChecked = getSentenceCheckCount(query, indexSearcher);
      final SearcherResult searcherResult = new SearcherResult(matchingSentences, sentencesChecked, query);
      searcherResult.setHasTooManyLuceneMatches(runnable.hasTooManyLuceneMatches());
      if (runnable.hasTooManyLuceneMatches()) {
        // more potential matches than we can check in an acceptable time :-(
        searcherResult.setDocCount(maxHits);
      } else {
        searcherResult.setDocCount(getDocCount(indexSearcher));
      }
      //TODO: the search itself could also timeout, don't just ignore that:
      //searcherResult.setResultIsTimeLimited(limitedTopDocs.resultIsTimeLimited);
      return searcherResult;
    } finally {
      close();
    }
  }

  private PossiblyLimitedTopDocs getTopDocs(Query query, Sort sort) throws IOException {
    final TopFieldCollector topCollector = TopFieldCollector.create(sort, maxHits, true, false, false, false);
    final Counter clock = Counter.newCounter(true);
    final int waitMillis = 1000;
    // TODO: if we interrupt the whole thread anyway, do we still need the TimeLimitingCollector?
    final TimeLimitingCollector collector = new TimeLimitingCollector(topCollector, clock, maxSearchTimeMillis / waitMillis);
    collector.setBaseline(0);
    final Thread counterThread = new Thread() {
      @Override
      public void run() {
        final long startTime = System.currentTimeMillis();
        while (true) {
          final long runTimeMillis = System.currentTimeMillis() - startTime;
          if (runTimeMillis > maxSearchTimeMillis) {
            // make sure there's no lingering thread for too long
            return;
          }
          clock.addAndGet(1);
          try {
            Thread.sleep(waitMillis);
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        }
      }
    };
    counterThread.setName("LuceneSearchTimeoutThread");
    counterThread.start();
    
    boolean timeLimitActivated = false;
    try {
      indexSearcher.search(query, collector);
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

  private int getSentenceCheckCount(Query query, IndexSearcher indexSearcher) {
    final int indexSize = indexSearcher.getIndexReader().numDocs();
    // we actually check up to maxHits sentences:
    // TODO: ??
    final int sentencesChecked = Math.min(maxHits, indexSize);
    return sentencesChecked;
  }

  private List<MatchingSentence> findMatchingSentences(IndexSearcher indexSearcher, TopDocs topDocs, JLanguageTool languageTool) throws IOException {
    final List<MatchingSentence> matchingSentences = new ArrayList<>();
    for (ScoreDoc match : topDocs.scoreDocs) {
      final Document doc = indexSearcher.doc(match.doc);
      final String sentence = doc.get(FIELD_NAME);
      final List<RuleMatch> ruleMatches = languageTool.check(sentence);
      if (ruleMatches.size() > 0) {
        final AnalyzedSentence analyzedSentence = languageTool.getAnalyzedSentence(sentence);
        final MatchingSentence matchingSentence = new MatchingSentence(sentence, analyzedSentence, ruleMatches);
        matchingSentences.add(matchingSentence);
      }
    }
    return matchingSentences;
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

  class SearchRunnable implements Runnable {

    private final IndexSearcher indexSearcher;
    private final Query query;
    private final Language language;
    private final PatternRule rule;

    private List<MatchingSentence> matchingSentences;
    private Exception exception;
    private boolean tooManyLuceneMatches;

    SearchRunnable(IndexSearcher indexSearcher, Query query, Language language, PatternRule rule) {
      this.indexSearcher = indexSearcher;
      this.query = query;
      this.language = language;
      this.rule = rule;
    }

    @Override
    public void run() {
      try {
        final Sort sort = new Sort(new SortField("docCount", SortField.Type.INT));  // do not sort by relevance as this will move the shortest documents to the top
        final long t1 = System.currentTimeMillis();
        final JLanguageTool languageTool = getLanguageToolWithOneRule(language, rule);
        final long langToolCreationTime = System.currentTimeMillis() - t1;
        final long t2 = System.currentTimeMillis();
        final PossiblyLimitedTopDocs limitedTopDocs = getTopDocs(query, sort);
        final long luceneTime = System.currentTimeMillis() - t2;
        final long t3 = System.currentTimeMillis();
        if (limitedTopDocs.topDocs.scoreDocs.length >= maxHits) {
          tooManyLuceneMatches = true;
        } else {
          tooManyLuceneMatches = false;
        }
        matchingSentences = findMatchingSentences(indexSearcher, limitedTopDocs.topDocs, languageTool);
        System.out.println("Check done in " + langToolCreationTime + "/" + luceneTime + "/" + (System.currentTimeMillis() - t3) 
                + "ms (LT creation/Lucene/matching) for " + limitedTopDocs.topDocs.scoreDocs.length + " docs, query " + query.toString(FIELD_NAME_LOWERCASE));
      } catch (Exception e) {
        exception = e;
      }
    }

    Exception getException() {
      return exception;
    }

    /**
     * There were more Lucene matches than we can actually check with LanguageTool in
     * an acceptable time, so real matches might be lost.
     */
    boolean hasTooManyLuceneMatches() {
      return tooManyLuceneMatches;
    }

    List<MatchingSentence> getMatchingSentences() {
      return matchingSentences;
    }
  }

  public static void main(String[] args) throws Exception {
    ensureCorrectUsageOrExit(args);
    final long startTime = System.currentTimeMillis();
    final String[] ruleIds = args[0].split(",");
    final File ruleFile = new File(args[1]);
    final String languageCode = args[2];
    final Language language = Language.getLanguageForShortName(languageCode);
    final File indexDir = new File(args[3]);
    final Searcher searcher = new Searcher(new SimpleFSDirectory(indexDir));
    for (String ruleId : ruleIds) {
      final long ruleStartTime = System.currentTimeMillis();
      final PatternRule rule = searcher.getRuleById(ruleId, ruleFile);
      final SearcherResult searcherResult = searcher.findRuleMatchesOnIndex(rule, language);
      int i = 1;
      if (searcherResult.getMatchingSentences().size() == 0) {
        System.out.println("[no matches]");
      }
      for (MatchingSentence ruleMatch : searcherResult.getMatchingSentences()) {
        System.out.println(i + ": " + ruleMatch.getSentence());
        i++;
      }
      System.out.println("Time: " + (System.currentTimeMillis() - ruleStartTime) + "ms");
      System.out.println("==============================================================");
    }
    System.out.println("Total time: " + (System.currentTimeMillis() - startTime) + "ms");
  }

}
