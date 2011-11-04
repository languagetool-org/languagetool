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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.RandomIndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.util.Version;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.patterns.PatternRule;
import de.danielnaber.languagetool.rules.patterns.PatternRuleLoader;

public class PatternRuleQueryBuilderTest extends LuceneTestCase {
  
  private IndexSearcher searcher;
  private IndexReader reader;
  private Directory directory;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    directory = newDirectory();
    final RandomIndexWriter writer = new RandomIndexWriter(random, directory, new LanguageToolAnalyzer(
        Version.LUCENE_31, new JLanguageTool(Language.ENGLISH)));
    final Document doc = new Document();
    doc.add(newField(PatternRuleQueryBuilder.FIELD_NAME,
        "How do you thin about this wonderful idea?", Field.Store.NO, Field.Index.ANALYZED));
    doc.add(newField(PatternRuleQueryBuilder.FIELD_NAME,
        "The are several grammar checkers for English, eg. LanguageTool.", Field.Store.NO,
        Field.Index.ANALYZED));
    writer.addDocument(doc);
    reader = writer.getReader();
    writer.close();
    searcher = newSearcher(reader);
  }

  @Override
  public void tearDown() throws Exception {
    searcher.close();
    reader.close();
    directory.close();
    super.tearDown();
  }

  public void testQueryBuilder() throws Exception {
    final StringBuilder sb = new StringBuilder();

    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> <rules lang=\"en\"> <category name=\"Test\"> <rule id=\"TEST_RULE\" name=\"test\"> <pattern>");

    sb.append("<token skip=\"-1\">How</token>"); // match "How"
    sb.append("<token postag=\"PRP\"></token>");// match"you/[PRP]"
    sb.append("<token skip=\"1\">thin</token>"); // match "thin"
    sb.append("<token postag_regexp=\"yes\" postag=\"JJ|DT\">this</token>"); // match "this/[DT]"
    sb.append("<token regexp=\"yes\" negate=\"yes\">bad|good</token>"); // match "wonderful"
    sb.append("<token regexp=\"yes\">idea|proposal</token>"); // match "idea"

    sb.append("</pattern> </rule> </category> </rules>");

    final InputStream input = new ByteArrayInputStream(sb.toString().getBytes());
    final PatternRuleLoader ruleLoader = new PatternRuleLoader();

    final List<PatternRule> rules = ruleLoader.getRules(input, "test.xml");

    final PatternRuleQueryBuilder patternRuleQueryBuilder = new PatternRuleQueryBuilder();
    final Query query1 = patternRuleQueryBuilder.buildQuery(rules.get(0), true);
    final Query query2 = patternRuleQueryBuilder.buildQuery(rules.get(0), false);
    assertEquals(query1, query2);
    assertEquals(1, searcher.search(query1, null, 1000).totalHits);
    assertEquals(1, searcher.search(query2, null, 1000).totalHits);
  }

  public void testCaseSensitive() throws Exception {
    final StringBuilder sb = new StringBuilder();

    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> <rules lang=\"en\"> <category name=\"Test\">");

    sb.append("<rule id=\"TEST_RULE_1\" name=\"test_1\"> <pattern case_sensitive=\"yes\">");
    sb.append("<token>How</token>");
    sb.append("</pattern> </rule>");

    sb.append("<rule id=\"TEST_RULE_2\" name=\"test_2\"> <pattern case_sensitive=\"yes\">");
    sb.append("<token>how</token>");
    sb.append("</pattern> </rule>");

    sb.append("<rule id=\"TEST_RULE_3\" name=\"test_3\"> <pattern>");
    sb.append("<token>How</token>");
    sb.append("</pattern> </rule>");

    sb.append("<rule id=\"TEST_RULE_4\" name=\"test_4\"> <pattern>");
    sb.append("<token>how</token>");
    sb.append("</pattern> </rule>");

    sb.append("</category> </rules>");

    final InputStream input = new ByteArrayInputStream(sb.toString().getBytes());
    final PatternRuleLoader ruleLoader = new PatternRuleLoader();

    final List<PatternRule> rules = ruleLoader.getRules(input, "test.xml");

    final PatternRuleQueryBuilder patternRuleQueryBuilder = new PatternRuleQueryBuilder();
    Query query = patternRuleQueryBuilder.buildQuery(rules.get(0), true);
    assertEquals(1, searcher.search(query, null, 1000).totalHits);

    query = patternRuleQueryBuilder.buildQuery(rules.get(1), true);
    assertEquals(0, searcher.search(query, null, 1000).totalHits);

    query = patternRuleQueryBuilder.buildQuery(rules.get(2), true);
    assertEquals(1, searcher.search(query, null, 1000).totalHits);

    query = patternRuleQueryBuilder.buildQuery(rules.get(3), true);
    assertEquals(1, searcher.search(query, null, 1000).totalHits);
  }

  public void testUnsupportedPatternRule() throws Exception {
    StringBuilder sb = new StringBuilder();

    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> <rules lang=\"en\"> <category name=\"Test\"> <rule id=\"TEST_RULE\" name=\"test\"> <pattern>");

    sb.append("<token skip=\"-1\">both<exception scope=\"next\">and</exception></token>"); // exception is not supported

    sb.append("</pattern> </rule> </category> </rules>");

    InputStream input = new ByteArrayInputStream(sb.toString().getBytes());
    final PatternRuleLoader ruleLoader = new PatternRuleLoader();

    final PatternRuleQueryBuilder patternRuleQueryBuilder = new PatternRuleQueryBuilder();
    List<PatternRule> rules = ruleLoader.getRules(input, "test.xml");
    try {
      patternRuleQueryBuilder.buildQuery(rules.get(0), true);
      fail("Exception should be thrown for unsupported PatternRule");
    } catch (UnsupportedPatternRuleException expected) {}
    try {
      patternRuleQueryBuilder.buildQuery(rules.get(0), false);
    } catch (UnsupportedPatternRuleException e) {
      fail("Exception should not be thrown, if not checkUnsupportedRule");
    }

    sb = new StringBuilder();

    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> <rules lang=\"en\"> <category name=\"Test\"> <rule id=\"TEST_RULE\" name=\"test\"> <pattern>");
    sb.append("<token inflected=\"yes\">suppose</token>"); // inflated token is not supported
    sb.append("</pattern> </rule> </category> </rules>");

    input = new ByteArrayInputStream(sb.toString().getBytes());

    rules = ruleLoader.getRules(input, "test.xml");
    try {
      patternRuleQueryBuilder.buildQuery(rules.get(0), true);
      fail("Exception should be thrown for unsupported PatternRule");
    } catch (UnsupportedPatternRuleException expected) {}

    try {
      patternRuleQueryBuilder.buildQuery(rules.get(0), false);
    } catch (UnsupportedPatternRuleException e) {
      fail("Exception should not be thrown, if not checkUnsupportedRule");
    }

  }

  /*
   * public void testAllPatternRules() throws IOException {
   * System.out.println("\nStatistics information for supported rule ratio of each language:"); int
   * successAll = 0; int failAll = 0; HashMap<String, Integer> messagesAll = new HashMap<String,
   * Integer>();
   * 
   * for (Language language : Language.LANGUAGES) { final PatternRuleLoader ruleLoader = new
   * PatternRuleLoader(); final String name = "/" + language.getShortName() + "/grammar.xml"; final
   * List<PatternRule> rules = ruleLoader.getRules(JLanguageTool.getDataBroker()
   * .getFromRulesDirAsStream(name), name); int success = 0; int fail = 0; HashMap<String, Integer>
   * messages = new HashMap<String, Integer>();
   * 
   * for (PatternRule rule : rules) {
   * 
   * try { PatternRuleQueryBuilder.buildQuery((PatternRule) rule, true); success++; } catch
   * (UnsupportedPatternRuleException e) { if (messages.get(e.getMessage()) == null) {
   * messages.put(e.getMessage(), 0); } messages.put(e.getMessage(), messages.get(e.getMessage()) +
   * 1);
   * 
   * fail++; }
   * 
   * } System.out.println("\t" + language.getName() + " ratio: " + (float) success / (success +
   * fail) + " (failure:" + fail + ")"); for (Entry<String, Integer> entry : messages.entrySet()) {
   * System.out.println("\t\t" + entry.getKey() + ": " + entry.getValue()); }
   * 
   * successAll += success; failAll += fail; } System.out.println("All languages ratio: " + (float)
   * successAll / (successAll + failAll) + " (failure:" + failAll + ")");
   * 
   * for (Entry<String, Integer> entry : messagesAll.entrySet()) { System.out.println("\t" +
   * entry.getKey() + ": " + entry.getValue()); }
   * 
   * }
   */
}
