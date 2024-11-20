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

import static org.languagetool.dev.index.Lucene.FIELD_NAME;
import static org.languagetool.dev.index.Lucene.FIELD_NAME_LOWERCASE;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.LuceneTestCase;
import org.junit.Ignore;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.language.English;
import org.languagetool.language.German;
import org.languagetool.language.GermanyGerman;
import org.languagetool.rules.IncorrectExample;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.patterns.PatternToken;

@Ignore
public class IndexerSearcherTest extends LuceneTestCase {

  private Searcher errorSearcher;
  private Directory directory;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    directory = new RAMDirectory();
    //directory = FSDirectory.open(new File("/tmp/lucenetest"));   // for debugging
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    if (directory != null) {
      directory.close();
    }
  }

  @Ignore("ignored as long as it doesn't work 100%")
  public void testAllRules() throws Exception {
    long startTime = System.currentTimeMillis();
    // comment in to test with external index:
    //directory = new SimpleFSDirectory(new File("/media/external-disk/corpus/languagetool/fast-rule-evaluation-de/"));
    //errorSearcher = new Searcher(directory);

    // TODO: make this work for all languages
    Language language = new English();
    //Language language = new French();
    //Language language = new Spanish();
    //Language language = new Polish();
    //Language language = new German();
    JLanguageTool lt = new JLanguageTool(language);

    System.out.println("Creating index for " + language + "...");
    int ruleCount = createIndex(lt);
    System.out.println("Index created with " + ruleCount + " rules");

    int ruleCounter = 0;
    int ruleProblems = 0;
    int exceptionCount = 0;

    List<Rule> rules = lt.getAllActiveRules();
    for (Rule rule : rules) {
      if (rule instanceof PatternRule && !rule.isDefaultOff()) {
        PatternRule patternRule = (PatternRule) rule;
        try {
          ruleCounter++;
          SearcherResult searcherResult = errorSearcher.findRuleMatchesOnIndex(patternRule, language);
          List<MatchingSentence> matchingSentences = searcherResult.getMatchingSentences();
          boolean foundExpectedMatch = false;
          for (MatchingSentence matchingSentence : matchingSentences) {
            List<RuleMatch> ruleMatches = matchingSentence.getRuleMatches();
            List<String> ruleMatchIds = getRuleMatchIds(ruleMatches);
            if (ruleMatchIds.contains(patternRule.getFullId())) {
              // TODO: there can be more than one expected match, can't it?
              foundExpectedMatch = true;
              break;
            }
          }
          if (!foundExpectedMatch) {
            System.out.println("Error: No match found for " + patternRule);
            System.out.println("Query      : " + searcherResult.getRelaxedQuery().toString(FIELD_NAME_LOWERCASE)); 
            System.out.println("Default field: " + FIELD_NAME_LOWERCASE);
            System.out.println("Lucene Hits: " + searcherResult.getLuceneMatchCount());
            System.out.println("Matches    : " + matchingSentences);
            System.out.println("Examples   : " + rule.getIncorrectExamples());
            System.out.println();
            ruleProblems++;
          } else {
            //long time = System.currentTimeMillis() - startTime;
            //System.out.println("Tested " + matchingSentences.size() + " sentences in " + time + "ms for rule " + patternRule);
          }
        } catch (UnsupportedPatternRuleException e) {
          System.out.println("UnsupportedPatternRuleException searching for rule " + patternRule.getFullId() + ": " + e.getMessage());
          ruleProblems++;
        } catch (Exception e) {
          System.out.println("Exception searching for rule " + patternRule.getFullId() + ": " + e.getMessage());
          e.printStackTrace(System.out);
          exceptionCount++;
        }
      }
    }
    System.out.println(language + ": problems: " + ruleProblems + ", total rules: " + ruleCounter);
    System.out.println(language + ": exceptions: " + exceptionCount + " (including timeouts)");
    System.out.println("Total time: " + (System.currentTimeMillis() - startTime) + "ms");
  }

  private List<String> getRuleMatchIds(List<RuleMatch> ruleMatches) {
    List<String> ids = new ArrayList<>();
    for (RuleMatch ruleMatch : ruleMatches) {
      if (ruleMatch.getRule() instanceof PatternRule) {
        PatternRule patternRule = (PatternRule) ruleMatch.getRule();
        ids.add(patternRule.getFullId());
      }
    }
    return ids;
  }

  private int createIndex(JLanguageTool lt) throws IOException {
    int ruleCount = 0;
    try (Indexer indexer = new Indexer(directory, lt.getLanguage())) {
      List<Rule> rules = lt.getAllActiveRules();
      for (Rule rule : rules) {
        if (rule instanceof PatternRule && !rule.isDefaultOff()) {
          PatternRule patternRule = (PatternRule) rule;
          List<IncorrectExample> incorrectExamples = rule.getIncorrectExamples();
          Document doc = new Document();
          FieldType idType = new FieldType();
          idType.setStored(true);
          idType.setTokenized(false);
          doc.add(new Field("ruleId", patternRule.getFullId(), idType));
          for (IncorrectExample incorrectExample : incorrectExamples) {
            String example = incorrectExample.getExample().replaceAll("</?marker>", "");
            FieldType fieldType = new FieldType();
            fieldType.setStored(true);
            fieldType.setTokenized(true);
            fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
            doc.add(new Field(FIELD_NAME, example, fieldType));
            // no lowercase here, it would lowercase the input to the LT analysis, leading to wrong POS tags:
            doc.add(new Field(FIELD_NAME_LOWERCASE, example, fieldType));
          }
          indexer.add(doc);
          ruleCount++;
        }
      }
    }
    errorSearcher = new Searcher(directory);
    return ruleCount;
  }

  @Ignore("manual debugging only")
  public void testForDebugging() throws Exception {
    // Note that the second sentence ends with "lid" instead of "lids" (the inflated one)
    //createIndex("I thin so");
    useRealIndex();
    German language = new GermanyGerman();
    PatternRule rule = getFirstRule("I_THIN", language);
    SearcherResult searcherResult = errorSearcher.findRuleMatchesOnIndex(rule, language);
    System.out.println("Matches: " + searcherResult.getMatchingSentences());
  }

  public void testIndexerSearcherWithEnglish() throws Exception {
    // Note that the second sentence ends with "lid" instead of "lids" (the inflated one)
    createIndex("How to move back and fourth from linux to xmb? Calcium deposits on eye lid.");
    English language = new English();
    SearcherResult searcherResult =
        errorSearcher.findRuleMatchesOnIndex(getFirstRule("BACK_AND_FOURTH", language), language);
    assertEquals(1, searcherResult.getCheckedSentences());
    assertEquals(false, searcherResult.isResultIsTimeLimited());
    assertEquals(1, searcherResult.getMatchingSentences().size());

    searcherResult = errorSearcher.findRuleMatchesOnIndex(getFirstRule("EYE_COMPOUNDS", language), language);
    assertEquals(1, searcherResult.getCheckedSentences());
    assertEquals(false, searcherResult.isResultIsTimeLimited());
    assertEquals(1, searcherResult.getMatchingSentences().size());

    searcherResult = errorSearcher.findRuleMatchesOnIndex(getFirstRule("ALL_OVER_THE_WORD", language), language);
    assertEquals(0, searcherResult.getCheckedSentences());
    assertEquals(false, searcherResult.isResultIsTimeLimited());
    assertEquals(0, searcherResult.getMatchingSentences().size());

    try {
      errorSearcher.findRuleMatchesOnIndex(getFirstRule("Invalid Rule Id", language), language);
      fail("Exception should be thrown for invalid rule id.");
    } catch (PatternRuleNotFoundException ignored) {}
  }

  private PatternRule getFirstRule(String ruleId, Language language) throws IOException {
    return errorSearcher.getRuleById(ruleId, language).get(0);
  }

  public void testWithNewRule() throws Exception {
    createIndex("How to move back and fourth from linux to xmb?");
    List<PatternToken> patternTokens = Arrays.asList(
        new PatternToken("move", false, false, false),
        new PatternToken("back", false, false, false)
        );
    PatternRule rule1 = new PatternRule("RULE1", new English(), patternTokens, "desc", "msg", "shortMsg");
    Searcher errorSearcher = new Searcher(directory);
    SearcherResult searcherResult = errorSearcher.findRuleMatchesOnIndex(rule1, new English());
    assertEquals(1, searcherResult.getCheckedSentences());
    assertEquals(1, searcherResult.getMatchingSentences().size());
    List<RuleMatch> ruleMatches = searcherResult.getMatchingSentences().get(0).getRuleMatches();
    assertEquals(1, ruleMatches.size());
    Rule rule = ruleMatches.get(0).getRule();
    assertEquals("RULE1", rule.getId());
  }

  public void testWithRegexRule() throws Exception {
    createIndex("How to move back and fourth from linux to xmb?");
    List<PatternToken> patternTokens = Arrays.asList(
        new PatternToken("move", false, false, false),
        new PatternToken("forth|back", false, true, false)
        );
    PatternRule rule1 = new PatternRule("RULE1", new English(), patternTokens, "desc", "msg", "shortMsg");
    Searcher errorSearcher = new Searcher(directory);
    SearcherResult searcherResult = errorSearcher.findRuleMatchesOnIndex(rule1, new English());
    assertEquals(1, searcherResult.getCheckedSentences());
    assertEquals(1, searcherResult.getMatchingSentences().size());
    List<RuleMatch> ruleMatches = searcherResult.getMatchingSentences().get(0).getRuleMatches();
    assertEquals(1, ruleMatches.size());
    Rule rule = ruleMatches.get(0).getRule();
    assertEquals("RULE1", rule.getId());
  }

  public void testApostropheElement() throws Exception {
    createIndex("Daily Bleed's Anarchist Encyclopedia");
    List<PatternToken> elements1 = Arrays.asList(
        new PatternToken("Bleed", false, false, false),
        new PatternToken("'s", false, false, false)
        //new PatternToken("s", false, false, false)
        );
    PatternRule rule1 = new PatternRule("RULE1", new English(), elements1, "desc", "msg", "shortMsg");

    List<PatternToken> elements2 = Arrays.asList(
        new PatternToken("Bleed", false, false, false),
        new PatternToken("'x", false, false, false)
        //new PatternToken("x", false, false, false)
        );
    PatternRule rule2 = new PatternRule("RULE", new English(), elements2, "desc", "msg", "shortMsg");

    SearcherResult searcherResult1 = errorSearcher.findRuleMatchesOnIndex(rule1, new English());
    assertEquals(1, searcherResult1.getMatchingSentences().size());
    List<RuleMatch> ruleMatches = searcherResult1.getMatchingSentences().get(0).getRuleMatches();
    assertEquals(1, ruleMatches.size());
    Rule rule = ruleMatches.get(0).getRule();
    assertEquals("RULE1", rule.getId());

    SearcherResult searcherResult2 = errorSearcher.findRuleMatchesOnIndex(rule2, new English());
    assertEquals(0, searcherResult2.getMatchingSentences().size());
  }

  public void testWithException() throws Exception {
    createIndex("How to move back and fourth from linux to xmb?");
    PatternToken exceptionElem = new PatternToken("forth|back", false, true, false);
    exceptionElem.setStringPosException("exception", false, false, false, false, false, "POS", false, false, null);
    List<PatternToken> patternTokens = Arrays.asList(
        new PatternToken("move", false, false, false),
        exceptionElem
        );
    PatternRule rule1 = new PatternRule("RULE1", new English(), patternTokens, "desc", "msg", "shortMsg");
    Searcher errorSearcher = new Searcher(directory);
    SearcherResult searcherResult = errorSearcher.findRuleMatchesOnIndex(rule1, new English());
    assertEquals(1, searcherResult.getCheckedSentences());
    assertEquals(1, searcherResult.getMatchingSentences().size());
    List<RuleMatch> ruleMatches = searcherResult.getMatchingSentences().get(0).getRuleMatches();
    assertEquals(1, ruleMatches.size());
    Rule rule = ruleMatches.get(0).getRule();
    assertEquals("RULE1", rule.getId());
  }

  public void testNegatedMatchAtSentenceStart() throws Exception {
    createIndex("How to move?");
    PatternToken negatedPatternToken = new PatternToken("Negated", false, false, false);
    negatedPatternToken.setNegation(true);
    List<PatternToken> patternTokens = Arrays.asList(
        negatedPatternToken,
        new PatternToken("How", false, false, false)
        );
    Searcher errorSearcher = new Searcher(directory);
    PatternRule rule1 = new PatternRule("RULE1", new English(), patternTokens, "desc", "msg", "shortMsg");
    SearcherResult searcherResult = errorSearcher.findRuleMatchesOnIndex(rule1, new English());
    assertEquals(1, searcherResult.getCheckedSentences());
    assertEquals(1, searcherResult.getMatchingSentences().size());
    List<RuleMatch> ruleMatches = searcherResult.getMatchingSentences().get(0).getRuleMatches();
    assertEquals(1, ruleMatches.size());
    Rule rule = ruleMatches.get(0).getRule();
    assertEquals("RULE1", rule.getId());
  }

  public void testWithOneElementWithException() throws Exception {
    createIndex("How to move back and fourth from linux to xmb?");
    PatternToken exceptionElem = new PatternToken("", false, true, false);
    exceptionElem.setStringPosException("exception", false, false, false, false, false, "POS", false, false, null);
    List<PatternToken> patternTokens = Arrays.asList(exceptionElem);
    PatternRule rule1 = new PatternRule("RULE1", new English(), patternTokens, "desc", "msg", "shortMsg");
    Searcher errorSearcher = new Searcher(directory);
    try {
      errorSearcher.findRuleMatchesOnIndex(rule1, new English());
      fail();
    } catch (UnsupportedPatternRuleException ignored) {
    }
  }

  private void createIndex(String content) throws IOException {
    directory = new RAMDirectory();
    //directory = FSDirectory.open(new File("/tmp/lucenetest"));  // for debugging
    Indexer.run(content, directory, new English());
    errorSearcher = new Searcher(directory);
  }

  private void useRealIndex() throws IOException {
    directory = FSDirectory.open(new File("/home/languagetool/corpus/en/").toPath());  // for debugging with more data
    errorSearcher = new Searcher(directory);
  }

  /*public void testForManualDebug() throws Exception {
    createIndex("How to move back and fourth from linux to xmb?");
    Searcher errorSearcher = new Searcher();
    SearcherResult searcherResult = errorSearcher.findRuleMatchesOnIndex(getRule("IS_EVEN_WORST"), Language.ENGLISH);
    System.out.println(searcherResult.getCheckedSentences());
    System.out.println(searcherResult.isRelaxedQuery());
  }*/

}
