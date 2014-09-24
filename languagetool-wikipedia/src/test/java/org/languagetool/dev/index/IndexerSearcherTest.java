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

import static org.languagetool.dev.index.PatternRuleQueryBuilder.FIELD_NAME;
import static org.languagetool.dev.index.PatternRuleQueryBuilder.FIELD_NAME_LOWERCASE;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.LuceneTestCase;
import org.junit.Ignore;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.language.English;
import org.languagetool.language.German;
import org.languagetool.rules.IncorrectExample;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.Element;
import org.languagetool.rules.patterns.PatternRule;

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
    final long startTime = System.currentTimeMillis();
    // comment in to test with external index:
    //directory = new SimpleFSDirectory(new File("/media/external-disk/corpus/languagetool/fast-rule-evaluation-de/"));
    //errorSearcher = new Searcher(directory);

    // TODO: make this work for all languages
    final Language language = new English();
    //final Language language = new French();
    //final Language language = new Spanish();
    //final Language language = new Polish();
    //final Language language = new German();
    final JLanguageTool lt = new JLanguageTool(language);
    lt.activateDefaultPatternRules();

    System.out.println("Creating index for " + language + "...");
    final int ruleCount = createIndex(lt);
    System.out.println("Index created with " + ruleCount + " rules");

    int ruleCounter = 0;
    int ruleProblems = 0;
    int exceptionCount = 0;

    final List<Rule> rules = lt.getAllActiveRules();
    for (Rule rule : rules) {
      if (rule instanceof PatternRule && !rule.isDefaultOff()) {
        final PatternRule patternRule = (PatternRule) rule;
        try {
          ruleCounter++;
          final SearcherResult searcherResult = errorSearcher.findRuleMatchesOnIndex(patternRule, language);
          final List<MatchingSentence> matchingSentences = searcherResult.getMatchingSentences();
          boolean foundExpectedMatch = false;
          for (MatchingSentence matchingSentence : matchingSentences) {
            final List<RuleMatch> ruleMatches = matchingSentence.getRuleMatches();
            final List<String> ruleMatchIds = getRuleMatchIds(ruleMatches);
            if (ruleMatchIds.contains(getFullId(patternRule))) {
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
            //final long time = System.currentTimeMillis() - startTime;
            //System.out.println("Tested " + matchingSentences.size() + " sentences in " + time + "ms for rule " + patternRule);
          }
        } catch (UnsupportedPatternRuleException e) {
          System.out.println("UnsupportedPatternRuleException searching for rule " + getFullId(patternRule) + ": " + e.getMessage());
          ruleProblems++;
        } catch (Exception e) {
          System.out.println("Exception searching for rule " + getFullId(patternRule) + ": " + e.getMessage());
          e.printStackTrace(System.out);
          exceptionCount++;
        }
      }
    }
    System.out.println(language + ": problems: " + ruleProblems + ", total rules: " + ruleCounter);
    System.out.println(language + ": exceptions: " + exceptionCount + " (including timeouts)");
    System.out.println("Total time: " + (System.currentTimeMillis() - startTime) + "ms");
  }

  private String getFullId(PatternRule patternRule) {
    return patternRule.getId() + "[" + patternRule.getSubId() + "]";
  }

  private List<String> getRuleMatchIds(List<RuleMatch> ruleMatches) {
    final List<String> ids = new ArrayList<>();
    for (RuleMatch ruleMatch : ruleMatches) {
      if (ruleMatch.getRule() instanceof PatternRule) {
        final PatternRule patternRule = (PatternRule) ruleMatch.getRule();
        ids.add(getFullId(patternRule));
      }
    }
    return ids;
  }

  private int createIndex(JLanguageTool lt) throws IOException {
    int ruleCount = 0;
    try (Indexer indexer = new Indexer(directory, lt.getLanguage())) {
      final List<Rule> rules = lt.getAllActiveRules();
      for (Rule rule : rules) {
        if (rule instanceof PatternRule && !rule.isDefaultOff()) {
          final PatternRule patternRule = (PatternRule) rule;
          final List<IncorrectExample> incorrectExamples = rule.getIncorrectExamples();
          final Document doc = new Document();
          final FieldType idType = new FieldType();
          idType.setStored(true);
          idType.setTokenized(false);
          doc.add(new Field("ruleId", getFullId(patternRule), idType));
          for (IncorrectExample incorrectExample : incorrectExamples) {
            final String example = incorrectExample.getExample().replaceAll("</?marker>", "");
            final FieldType fieldType = new FieldType();
            fieldType.setStored(true);
            fieldType.setTokenized(true);
            fieldType.setIndexed(true);
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
    German language = new German();
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
    assertEquals(2, searcherResult.getCheckedSentences());
    assertEquals(false, searcherResult.isResultIsTimeLimited());
    assertEquals(1, searcherResult.getMatchingSentences().size());

    searcherResult = errorSearcher.findRuleMatchesOnIndex(getFirstRule("EYE_BROW", language), language);
    assertEquals(2, searcherResult.getCheckedSentences());
    assertEquals(false, searcherResult.isResultIsTimeLimited());
    assertEquals(1, searcherResult.getMatchingSentences().size());

    searcherResult = errorSearcher.findRuleMatchesOnIndex(getFirstRule("ALL_OVER_THE_WORD", language), language);
    assertEquals(2, searcherResult.getCheckedSentences());
    assertEquals(false, searcherResult.isResultIsTimeLimited());
    assertEquals(0, searcherResult.getMatchingSentences().size());

    try {
      errorSearcher.findRuleMatchesOnIndex(getFirstRule("Invalid Rule Id", language), language);
      fail("Exception should be thrown for invalid rule id.");
    } catch (PatternRuleNotFoundException expected) {}
  }

  private PatternRule getFirstRule(String ruleId, Language language) throws IOException {
    return errorSearcher.getRuleById(ruleId, language).get(0);
  }

  public void testWithNewRule() throws Exception {
    createIndex("How to move back and fourth from linux to xmb?");
    final List<Element> elements = Arrays.asList(
        new Element("move", false, false, false),
        new Element("back", false, false, false)
        );
    final PatternRule rule1 = new PatternRule("RULE1", new English(), elements, "desc", "msg", "shortMsg");
    final Searcher errorSearcher = new Searcher(directory);
    final SearcherResult searcherResult = errorSearcher.findRuleMatchesOnIndex(rule1, new English());
    assertEquals(1, searcherResult.getCheckedSentences());
    assertEquals(1, searcherResult.getMatchingSentences().size());
    final List<RuleMatch> ruleMatches = searcherResult.getMatchingSentences().get(0).getRuleMatches();
    assertEquals(1, ruleMatches.size());
    final Rule rule = ruleMatches.get(0).getRule();
    assertEquals("RULE1", rule.getId());
  }

  public void testWithRegexRule() throws Exception {
    createIndex("How to move back and fourth from linux to xmb?");
    final List<Element> elements = Arrays.asList(
        new Element("move", false, false, false),
        new Element("forth|back", false, true, false)
        );
    final PatternRule rule1 = new PatternRule("RULE1", new English(), elements, "desc", "msg", "shortMsg");
    final Searcher errorSearcher = new Searcher(directory);
    final SearcherResult searcherResult = errorSearcher.findRuleMatchesOnIndex(rule1, new English());
    assertEquals(1, searcherResult.getCheckedSentences());
    assertEquals(1, searcherResult.getMatchingSentences().size());
    final List<RuleMatch> ruleMatches = searcherResult.getMatchingSentences().get(0).getRuleMatches();
    assertEquals(1, ruleMatches.size());
    final Rule rule = ruleMatches.get(0).getRule();
    assertEquals("RULE1", rule.getId());
  }

  public void testApostropheElement() throws Exception {
    createIndex("Daily Bleed's Anarchist Encyclopedia");
    final List<Element> elements1 = Arrays.asList(
        new Element("Bleed", false, false, false),
        new Element("'", false, false, false),
        new Element("s", false, false, false)
        );
    final PatternRule rule1 = new PatternRule("RULE1", new English(), elements1, "desc", "msg", "shortMsg");

    final List<Element> elements2 = Arrays.asList(
        new Element("Bleed", false, false, false),
        new Element("'", false, false, false),
        new Element("x", false, false, false)
        );
    final PatternRule rule2 = new PatternRule("RULE", new English(), elements2, "desc", "msg", "shortMsg");

    final SearcherResult searcherResult1 = errorSearcher.findRuleMatchesOnIndex(rule1, new English());
    assertEquals(1, searcherResult1.getMatchingSentences().size());
    final List<RuleMatch> ruleMatches = searcherResult1.getMatchingSentences().get(0).getRuleMatches();
    assertEquals(1, ruleMatches.size());
    final Rule rule = ruleMatches.get(0).getRule();
    assertEquals("RULE1", rule.getId());

    final SearcherResult searcherResult2 = errorSearcher.findRuleMatchesOnIndex(rule2, new English());
    assertEquals(0, searcherResult2.getMatchingSentences().size());
  }

  public void testWithException() throws Exception {
    createIndex("How to move back and fourth from linux to xmb?");
    final Element exceptionElem = new Element("forth|back", false, true, false);
    exceptionElem.setStringPosException("exception", false, false, false, false, false, "POS", false, false);
    final List<Element> elements = Arrays.asList(
        new Element("move", false, false, false),
        exceptionElem
        );
    final PatternRule rule1 = new PatternRule("RULE1", new English(), elements, "desc", "msg", "shortMsg");
    final Searcher errorSearcher = new Searcher(directory);
    final SearcherResult searcherResult = errorSearcher.findRuleMatchesOnIndex(rule1, new English());
    assertEquals(1, searcherResult.getCheckedSentences());
    assertEquals(1, searcherResult.getMatchingSentences().size());
    final List<RuleMatch> ruleMatches = searcherResult.getMatchingSentences().get(0).getRuleMatches();
    assertEquals(1, ruleMatches.size());
    final Rule rule = ruleMatches.get(0).getRule();
    assertEquals("RULE1", rule.getId());
  }

  public void testNegatedMatchAtSentenceStart() throws Exception {
    createIndex("How to move?");
    final Element negatedElement = new Element("Negated", false, false, false);
    negatedElement.setNegation(true);
    final List<Element> elements = Arrays.asList(
        negatedElement,
        new Element("How", false, false, false)
        );
    final Searcher errorSearcher = new Searcher(directory);
    final PatternRule rule1 = new PatternRule("RULE1", new English(), elements, "desc", "msg", "shortMsg");
    final SearcherResult searcherResult = errorSearcher.findRuleMatchesOnIndex(rule1, new English());
    assertEquals(1, searcherResult.getCheckedSentences());
    assertEquals(1, searcherResult.getMatchingSentences().size());
    final List<RuleMatch> ruleMatches = searcherResult.getMatchingSentences().get(0).getRuleMatches();
    assertEquals(1, ruleMatches.size());
    final Rule rule = ruleMatches.get(0).getRule();
    assertEquals("RULE1", rule.getId());
  }

  public void testWithOneElementWithException() throws Exception {
    createIndex("How to move back and fourth from linux to xmb?");
    final Element exceptionElem = new Element("", false, true, false);
    exceptionElem.setStringPosException("exception", false, false, false, false, false, "POS", false, false);
    final List<Element> elements = Arrays.asList(
        exceptionElem
        );
    final PatternRule rule1 = new PatternRule("RULE1", new English(), elements, "desc", "msg", "shortMsg");
    final Searcher errorSearcher = new Searcher(directory);
    try {
      errorSearcher.findRuleMatchesOnIndex(rule1, new English());
      fail();
    } catch (UnsupportedPatternRuleException expected) {
    }
  }

  private void createIndex(String content) throws IOException {
    directory = new RAMDirectory();
    //directory = FSDirectory.open(new File("/tmp/lucenetest"));  // for debugging
    Indexer.run(content, directory, new English(), false);
    errorSearcher = new Searcher(directory);
  }

  private void useRealIndex() throws IOException {
    directory = FSDirectory.open(new File("/home/languagetool/corpus/en/"));  // for debugging with more data
    errorSearcher = new Searcher(directory);
  }

  /*public void testForManualDebug() throws Exception {
    createIndex("How to move back and fourth from linux to xmb?");
    final Searcher errorSearcher = new Searcher();
    SearcherResult searcherResult = errorSearcher.findRuleMatchesOnIndex(getRule("IS_EVEN_WORST"), Language.ENGLISH);
    System.out.println(searcherResult.getCheckedSentences());
    System.out.println(searcherResult.isRelaxedQuery());
  }*/

}
