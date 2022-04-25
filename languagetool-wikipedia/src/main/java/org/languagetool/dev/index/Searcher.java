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

import static org.languagetool.dev.dumpcheck.SentenceSourceIndexer.MAX_DOC_COUNT_FIELD;
import static org.languagetool.dev.dumpcheck.SentenceSourceIndexer.MAX_DOC_COUNT_FIELD_VAL;
import static org.languagetool.dev.dumpcheck.SentenceSourceIndexer.MAX_DOC_COUNT_VALUE;
import static org.languagetool.dev.index.Lucene.FIELD_NAME_LOWERCASE;
import static org.languagetool.dev.index.Lucene.SOURCE_FIELD_NAME;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
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
import org.languagetool.Languages;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.tools.ContextTools;

/**
 * A class with a main() method that takes a rule id  and the location of the
 * index that runs the query on that index and prints all matches.
 * Will transparently handle rules that are not supported, i.e. run on the candidate matches
 * up to a limit.
 * See {@link Indexer} for how to create the index.
 * 
 * @author Tao Lin
 * @author Daniel Naber
 */
public class Searcher {

  private static final boolean WIKITEXT_OUTPUT = false;
  
  private final Directory directory;
  private final String fieldName;

  private int skipHits = 0;
  private int maxHits = 1000;
  private int maxSearchTimeMillis = 5000;
  private IndexSearcher indexSearcher;
  private DirectoryReader reader;
  private boolean limitSearch = true;

  public Searcher(Directory directory) {
    this(directory, FIELD_NAME_LOWERCASE);
  }

  public Searcher(Directory directory, String fieldName) {
    this.directory = directory;
    this.fieldName = fieldName;
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
      IndexSearcher indexSearcher = new IndexSearcher(reader);
      return getDocCount(indexSearcher);
    }
  }

  private int getDocCount(IndexSearcher indexSearcher) throws IOException {
    Term searchTerm = new Term(MAX_DOC_COUNT_FIELD, MAX_DOC_COUNT_FIELD_VAL);
    TopDocs search = indexSearcher.search(new TermQuery(searchTerm), 1);
    if (search.totalHits != 1) {
      return -1;
    }
    ScoreDoc scoreDoc = search.scoreDocs[0];
    Document doc = indexSearcher.doc(scoreDoc.doc);
    return Integer.parseInt(doc.get(MAX_DOC_COUNT_VALUE));
  }

  public int getMaxHits() {
    return maxHits;
  }

  public void setMaxHits(int maxHits) {
    this.maxHits = maxHits;
  }

  public int getSkipHits() {
    return skipHits;
  }

  public void setSkipHits(int skipHits) {
    this.skipHits = skipHits;
  }

  public int getMaxSearchTimeMillis() {
    return maxSearchTimeMillis;
  }

  public void setMaxSearchTimeMillis(int maxSearchTimeMillis) {
    this.maxSearchTimeMillis = maxSearchTimeMillis;
  }

  public SearcherResult findRuleMatchesOnIndex(PatternRule rule, Language language) throws IOException, UnsupportedPatternRuleException {
    return findRuleMatchesOnIndex(rule, language, FIELD_NAME_LOWERCASE);
  }

  /**
   * @since 4.8
   */
  public SearcherResult findRuleMatchesOnIndex(PatternRule rule, Language language, String fieldName) throws IOException, UnsupportedPatternRuleException {
    // it seems wasteful to re-open the index every time, but I had strange problems (OOM, Array out of bounds, ...)
    // when not doing so...
    open();
    try {
      PatternRuleQueryBuilder patternRuleQueryBuilder = new PatternRuleQueryBuilder(language, indexSearcher, fieldName);
      Query query = patternRuleQueryBuilder.buildRelaxedQuery(rule);
      if (query == null) {
        throw new NullPointerException("Cannot search on null query for rule: " + rule.getId());
      }

      System.out.println("Running query: " + query);
      SearchRunnable runnable = new SearchRunnable(indexSearcher, query, language, rule);
      Thread searchThread = new Thread(runnable);
      searchThread.start();
      try {
        // using a TimeLimitingCollector is not enough, as it doesn't cover all time required to
        // search for a complicated regex, so interrupt the whole thread instead:
        if (limitSearch) { // I don't know a simpler way to achieve this...
          searchThread.join(maxSearchTimeMillis);
        } else {
          searchThread.join(Integer.MAX_VALUE);
        }
        //searchThread.interrupt();
      } catch (InterruptedException e) {
        throw new RuntimeException("Search thread got interrupted for query " + query, e);
      }
      if (searchThread.isInterrupted()) {
        throw new SearchTimeoutException("Search timeout of " + maxSearchTimeMillis + "ms reached for query " + query);
      }
      Exception exception = runnable.getException();
      if (exception != null) {
        if (exception instanceof SearchTimeoutException) {
          throw (SearchTimeoutException)exception;
        }
        throw new RuntimeException("Exception during search for query " + query + " on rule " + rule.getId(), exception);
      }

      List<MatchingSentence> matchingSentences = runnable.getMatchingSentences();
      SearcherResult searcherResult = new SearcherResult(matchingSentences, runnable.docsChecked, query);
      searcherResult.setMaxDocChecked(runnable.getMaxDocChecked());
      searcherResult.setHasTooManyLuceneMatches(runnable.hasTooManyLuceneMatches());
      searcherResult.setLuceneMatchCount(runnable.getLuceneMatchCount());
      searcherResult.setSkipHits(skipHits);
      searcherResult.setNumDocs(runnable.numDocs);
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

  private PossiblyLimitedTopDocs getTopDocs(Query query) throws IOException {
    TopScoreDocCollector topCollector = TopScoreDocCollector.create(maxHits);
    Counter clock = Counter.newCounter(true);
    int waitMillis = 1000;
    // TODO: if we interrupt the whole thread anyway, do we still need the TimeLimitingCollector?
    TimeLimitingCollector collector = new TimeLimitingCollector(topCollector, clock, maxSearchTimeMillis / waitMillis);
    collector.setBaseline(0);
    Thread counterThread = new Thread() {
      @Override
      public void run() {
        long startTime = System.currentTimeMillis();
        while (true) {
          long runTimeMillis = System.currentTimeMillis() - startTime;
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

  List<PatternRule> getRuleById(String ruleId, Language language) throws IOException {
    List<PatternRule> rules = new ArrayList<>();
    JLanguageTool lt = new JLanguageTool(language);
    for (Rule rule : lt.getAllRules()) {
      if (rule.getId().equals(ruleId) && rule instanceof PatternRule) {
        rules.add((PatternRule) rule);
      }
    }
    if (rules.size() > 0) {
      return rules;
    } else {
      throw new PatternRuleNotFoundException(ruleId, language);
    }
  }

  private MatchingSentencesResult findMatchingSentences(IndexSearcher indexSearcher, TopDocs topDocs, JLanguageTool languageTool) throws IOException {
    List<MatchingSentence> matchingSentences = new ArrayList<>();
    int i = 0;
    int docsChecked = 0;
    for (ScoreDoc match : topDocs.scoreDocs) {
      i++;
      if (i < skipHits) {
        // needed for paging
        continue;
      }
      Document doc = indexSearcher.doc(match.doc);
      String sentence = doc.get(fieldName);
      if (sentence == null) {
        throw new RuntimeException("No field '" + fieldName + "' found in doc " + match.doc);
      }
      List<RuleMatch> ruleMatches = languageTool.check(sentence);
      docsChecked++;
      if (ruleMatches.size() > 0) {
        String source = doc.get(SOURCE_FIELD_NAME);
        String title = doc.get(Indexer.TITLE_FIELD_NAME);
        AnalyzedSentence analyzedSentence = languageTool.getAnalyzedSentence(sentence);
        MatchingSentence matchingSentence = new MatchingSentence(sentence, source, title, analyzedSentence, ruleMatches);
        matchingSentences.add(matchingSentence);
      }
    }
    return new MatchingSentencesResult(matchingSentences, i, docsChecked);
  }
  
  class MatchingSentencesResult {
    List<MatchingSentence> matchingSentences;
    int maxDocChecked;
    int docsChecked;
    MatchingSentencesResult(List<MatchingSentence> matchingSentences, int maxDocChecked, int docsChecked) {
      this.matchingSentences = matchingSentences;
      this.maxDocChecked = maxDocChecked;
      this.docsChecked = docsChecked;
    }
  }

  private JLanguageTool getLanguageToolWithOneRule(Language lang, PatternRule patternRule) {
    JLanguageTool lt = new JLanguageTool(lang);
    for (Rule rule : lt.getAllActiveRules()) {
      if (!rule.getId().equals(patternRule.getId())) {
        lt.disableRule(rule.getId());
      }
    }
    lt.addRule(patternRule);
    lt.enableRule(patternRule.getId()); // rule might be off by default
    return lt;
  }

  static class PossiblyLimitedTopDocs {
    TopDocs topDocs;
    boolean resultIsTimeLimited;

    PossiblyLimitedTopDocs(TopDocs topDocs, boolean resultIsTimeLimited) {
      this.topDocs = topDocs;
      this.resultIsTimeLimited = resultIsTimeLimited;
    }
  }

  private static void ensureCorrectUsageOrExit(String[] args) {
    if (args.length < 3 || (args.length == 4 && !"--no_limit".equals(args[3]))) {
      System.err.println("Usage: Searcher <ruleId> <languageCode> <indexDir> [--no_limit]");
      System.err.println("\truleId       Id of the rule to search for (or comma-separated list of ids)");
      System.err.println("\tlanguageCode short language code, e.g. 'en' for English");
      System.err.println("\tindexDir     path to a directory containing the index");
      System.err.println("\t--no_limit   do not limit search time");
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
    private int luceneMatchCount;
    private int maxDocChecked;
    private int docsChecked;
    private int numDocs;

    SearchRunnable(IndexSearcher indexSearcher, Query query, Language language, PatternRule rule) {
      this.indexSearcher = indexSearcher;
      this.query = query;
      this.language = language;
      this.rule = rule;
    }

    @Override
    public void run() {
      try {
        long t1 = System.currentTimeMillis();
        JLanguageTool languageTool = getLanguageToolWithOneRule(language, rule);
        long langToolCreationTime = System.currentTimeMillis() - t1;
        long t2 = System.currentTimeMillis();
        PossiblyLimitedTopDocs limitedTopDocs = getTopDocs(query);
        long luceneTime = System.currentTimeMillis() - t2;
        long t3 = System.currentTimeMillis();
        luceneMatchCount = limitedTopDocs.topDocs.totalHits;
        tooManyLuceneMatches = limitedTopDocs.topDocs.scoreDocs.length >= maxHits;
        MatchingSentencesResult res = findMatchingSentences(indexSearcher, limitedTopDocs.topDocs, languageTool);
        matchingSentences = res.matchingSentences;
        maxDocChecked = res.maxDocChecked;
        docsChecked = res.docsChecked;
        numDocs = indexSearcher.getIndexReader().numDocs();
        System.out.println("Check done in " + langToolCreationTime + "/" + luceneTime + "/" + (System.currentTimeMillis() - t3)
            + "ms (LT creation/Lucene/matching) for " + limitedTopDocs.topDocs.scoreDocs.length + " docs");
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

    int getLuceneMatchCount() {
      return luceneMatchCount;
    }

    List<MatchingSentence> getMatchingSentences() {
      return matchingSentences;
    }

    int getMaxDocChecked() {
      return maxDocChecked;
    }
  }

  private static ContextTools getContextTools(int contextSize) {
    ContextTools contextTools = new ContextTools();
    contextTools.setEscapeHtml(false);
    contextTools.setContextSize(contextSize);
    contextTools.setErrorMarker("**", "**");
    return contextTools;
  }

  public static void main(String[] args) throws Exception {
    ensureCorrectUsageOrExit(args);
    long startTime = System.currentTimeMillis();
    String[] ruleIds = args[0].split(",");
    String languageCode = args[1];
    Language language = Languages.getLanguageForShortCode(languageCode);
    File indexDir = new File(args[2]);
    boolean limitSearch = !(args.length > 3 && "--no_limit".equals(args[3]));
    Searcher searcher = new Searcher(new SimpleFSDirectory(indexDir.toPath()));
    if (!limitSearch) {
      searcher.setMaxHits(100_000);
    }
    searcher.limitSearch = limitSearch;
    ContextTools contextTools = getContextTools(140);
    int totalMatches = 0;
    for (String ruleId : ruleIds) {
      long ruleStartTime = System.currentTimeMillis();
      for (PatternRule rule : searcher.getRuleById(ruleId, language)) {
        System.out.println("===== " + rule.getFullId() + " =========================================================");
        SearcherResult searcherResult = searcher.findRuleMatchesOnIndex(rule, language);
        int i = 1;
        if (searcherResult.getMatchingSentences().isEmpty()) {
          System.out.println("[no matches]");
        }
        for (MatchingSentence ruleMatch : searcherResult.getMatchingSentences()) {
          for (RuleMatch match : ruleMatch.getRuleMatches()) {
            String context = contextTools.getContext(match.getFromPos(), match.getToPos(), ruleMatch.getSentence());
            if (WIKITEXT_OUTPUT) {
              ContextTools contextTools2 = getContextTools(0);
              String coveredText = contextTools2.getContext(match.getFromPos(), match.getToPos(), ruleMatch.getSentence());
              coveredText = coveredText.replaceFirst("^\\.\\.\\.", "").replaceFirst("\\.\\.\\.$", "");
              coveredText = coveredText.replaceFirst("^\\*\\*", "").replaceFirst("\\*\\*$", "");
              String encodedTextWithQuotes = URLEncoder.encode("\"" + coveredText + "\"", "UTF-8");
              String searchLink = "https://de.wikipedia.org/w/index.php?search=" + encodedTextWithQuotes + "&title=Spezial%3ASuche&go=Artikel";
              context = context.replaceAll("\\*\\*.*?\\*\\*", "[" + searchLink + " " + coveredText + "]");
              String encTitle = URLEncoder.encode(ruleMatch.getTitle(), "UTF-8");
              String encodedText = URLEncoder.encode(coveredText, "UTF-8");
              System.out.println("# [[" + ruleMatch.getTitle() + "]]: " + context +
                " ([http://wikipedia.ramselehof.de/wikiblame.php?user_lang=de&lang=de&project=wikipedia&article=" + encTitle +
                      "&needle=" + encodedText + "&skipversions=0&ignorefirst=0&limit=500&searchmethod=int&order=desc&start=Start WikiBlame])");
            } else {
              System.out.println(i + ": " + context + " [" + ruleMatch.getSource() + "]");
            }
          }
          totalMatches += ruleMatch.getRuleMatches().size();
          i++;
        }
        System.out.println("Time: " + (System.currentTimeMillis() - ruleStartTime) + "ms");
      }
    }
    System.out.println("Total time: " + (System.currentTimeMillis() - startTime) + "ms, " + totalMatches + " matches");
  }

}
