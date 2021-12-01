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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.LuceneTestCase;
import org.junit.Ignore;
import org.languagetool.Language;
import org.languagetool.language.English;
import org.languagetool.rules.patterns.AbstractPatternRule;
import org.languagetool.rules.patterns.PatternRuleLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.languagetool.dev.index.Lucene.FIELD_NAME;
import static org.languagetool.dev.index.Lucene.FIELD_NAME_LOWERCASE;

@Ignore
public class PatternRuleQueryBuilderTest extends LuceneTestCase {

  private IndexSearcher searcher;
  private DirectoryReader reader;
  private Directory directory;
  private Language language;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    language = new English();
    directory = new RAMDirectory();
    /*File indexPath = new File("/tmp/lucene");
    if (indexPath.exists()) {
      FileUtils.deleteDirectory(indexPath);
    }
    directory = FSDirectory.open(indexPath);*/

    Analyzer analyzer = Indexer.getAnalyzer(language);
    IndexWriterConfig config = Indexer.getIndexWriterConfig(analyzer);
    try (IndexWriter writer = new IndexWriter(directory, config)) {
      addDocument(writer, "How do you thin about this wonderful idea?");
      addDocument(writer, "The are several grammar checkers for English, E.G. LanguageTool 123.");
    }
    reader = DirectoryReader.open(directory);
    searcher = newSearcher(reader);
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    if (reader != null) {
      reader.close();
    }
    if (directory != null) {
      directory.close();
    }
  }

  private void addDocument(IndexWriter writer, String content) throws IOException {
    Document doc = new Document();
    FieldType type = new FieldType();
    type.setStored(true);
    type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
    type.setTokenized(true);
    doc.add(new Field(FIELD_NAME, content, type));
    doc.add(new Field(FIELD_NAME_LOWERCASE, content, type));
    writer.addDocument(doc);
  }

  public void testQueryBuilder() throws Exception {
    String ruleXml =
        "<token skip='-1'>How</token>" // match "How"
      + "<token postag='PRP'></token>"// match"you/[PRP]"
      + "<token skip='1'>thin</token>" // match "thin"
      + "<token postag_regexp='yes' postag='JJ|DT'>this</token>" // match "this/[DT]"
      + "<token regexp='yes' negate='yes'>bad|good</token>" // match "wonderful"
      + "<token regexp='yes'>idea|proposal</token>"; // match "idea"

    AbstractPatternRule patternRule = makeRule(ruleXml);
    PatternRuleQueryBuilder patternRuleQueryBuilder = new PatternRuleQueryBuilder(language, searcher);
    Query query = patternRuleQueryBuilder.buildRelaxedQuery(patternRule);
    assertEquals("+fieldLowercase:how +fieldLowercase:_pos_prp +fieldLowercase:thin " +
            "+spanNear([fieldLowercase:this, SpanMultiTermQueryWrapper(fieldLowercase:/_pos_(jj|dt)/)], 0, false) " +
            "+fieldLowercase:/idea|proposal/", query.toString());
  }

  public void testCaseSensitive() throws Exception {
    InputStream input = new ByteArrayInputStream(("<?xml version='1.0' encoding='UTF-8'?> <rules lang='en'> <category id='TEST' name='Test'>" +
            "<rule id='TEST_RULE_1' name='test_1'> <pattern case_sensitive='yes'><token>How</token></pattern> </rule>" +
            "<rule id='TEST_RULE_2' name='test_2'> <pattern case_sensitive='yes'><token>how</token></pattern> </rule>" +
            "<rule id='TEST_RULE_3' name='test_3'> <pattern><token>How</token></pattern> </rule>" +
            "<rule id='TEST_RULE_4' name='test_4'> <pattern><token>how</token></pattern> </rule>" +
            "</category> </rules>").getBytes());
    PatternRuleLoader ruleLoader = new PatternRuleLoader();

    List<AbstractPatternRule> rules = ruleLoader.getRules(input, "test.xml");

    PatternRuleQueryBuilder patternRuleQueryBuilder = new PatternRuleQueryBuilder(language, searcher);
    Query query1 = patternRuleQueryBuilder.buildRelaxedQuery(rules.get(0));
    assertEquals(1, searcher.search(query1, 1000).totalHits);

    Query query2 = patternRuleQueryBuilder.buildRelaxedQuery(rules.get(1));
    assertEquals(1, searcher.search(query2, 1000).totalHits);  // also a match, as candidates are always case-insensitive

    Query query3 = patternRuleQueryBuilder.buildRelaxedQuery(rules.get(2));
    assertEquals(1, searcher.search(query3, 1000).totalHits);

    Query query4 = patternRuleQueryBuilder.buildRelaxedQuery(rules.get(3));
    assertEquals(1, searcher.search(query4, 1000).totalHits);
  }

  public void testUnsupportedPatternRule() throws Exception {
    PatternRuleQueryBuilder patternRuleQueryBuilder = new PatternRuleQueryBuilder(language, searcher);
    try {
      patternRuleQueryBuilder.buildRelaxedQuery(makeRule("<token skip='-1'><exception>and</exception></token>"));
      fail("Exception should be thrown for unsupported PatternRule");
    } catch (UnsupportedPatternRuleException ignored) {}
  }

  public void testUnsupportedBackReferencePatternRule() throws Exception {
    PatternRuleQueryBuilder patternRuleQueryBuilder = new PatternRuleQueryBuilder(language, searcher);
    try {
      patternRuleQueryBuilder.buildRelaxedQuery(makeRule("<token>\\1</token>"));
      fail("Exception should be thrown for unsupported PatternRule");
    } catch (UnsupportedPatternRuleException ignored) {}
  }

  public void testSpecialRegexSyntax() throws Exception {
    AbstractPatternRule patternRule = makeRule("<token regexp='yes'>\\p{Punct}</token>");
    PatternRuleQueryBuilder queryBuilder = new PatternRuleQueryBuilder(language, searcher);
    try {
      queryBuilder.buildRelaxedQuery(patternRule);
      fail();
    } catch (UnsupportedPatternRuleException ignore) {}
  }

  public void testSpecialRegexSyntax2() throws Exception {
    AbstractPatternRule patternRule = makeRule("<token regexp='yes' inflected='yes'>\\p{Lu}\\p{Ll}+</token>");
    PatternRuleQueryBuilder queryBuilder = new PatternRuleQueryBuilder(language, searcher);
    try {
      queryBuilder.buildRelaxedQuery(patternRule);
      fail();
    } catch (UnsupportedPatternRuleException ignore) {}
  }

  public void testNumberRegex() throws Exception {
    assertMatches(makeRule("<token regexp='yes'>13\\d</token>"), 0);
    assertMatches(makeRule("<token regexp='yes'>12\\d</token>"), 1);
  }

  public void testIgnoreOptionalTokens() throws Exception {
    assertMatches(makeRule("<token min='0'>optional</token><token>idea</token>"), 1);
  }

  public void testOnlyInflected() throws Exception {
    assertMatches(makeRule("<token inflected='yes'>think</token>"), 0);
    assertMatches(makeRule("<token inflected='yes'>LanguageTool</token>"), 1);
    assertMatches(makeRule("<token inflected='yes'>checker</token>"), 1);
  }

  public void testInflectedAndRegex() throws Exception {
    assertMatches(makeRule("<token inflected='yes' regexp='yes'>foo|bar</token>"), 0);
    assertMatches(makeRule("<token inflected='yes' regexp='yes'>walk|be</token>"), 1);
    assertMatches(makeRule("<token inflected='yes' regexp='yes'>somefoo|wonderful</token>"), 1);
    assertMatches(makeRule("<token inflected='yes' regexp='yes'>somefoo|wonderf.l</token>"), 1);
    assertMatches(makeRule("<token inflected='yes' regexp='yes'>somefoo|wonderX.l</token>"), 0);
  }

  public void testSeveralElements() throws Exception {

    // See setup() for the texts we can match
    
    assertMatches(makeRule("<token>How</token>"), 1);
    assertMatches(makeRule("<token>how</token>"), 1);
    assertMatches(makeRule("<token>LanguageTool</token>"), 1);
    assertMatches(makeRule("<token>UnknownWord</token>"), 0);

    assertMatches(makeRule("<token>How</token>"), 1);

    assertMatches(makeRule("<token regexp='yes'>Foo|How</token>"), 1);
    assertMatches(makeRule("<token regexp='yes'>Foo|how</token>"), 1);
    assertMatches(makeRule("<token regexp='yes'>Foo|Bar</token>"), 0);

    assertMatches(makeRule("<token regexp='yes'>Foo|How</token>"), 1);

    assertMatches(makeRule("<token postag='WRB'></token>"), 1);
    assertMatches(makeRule("<token postag='FOO'></token>"), 0);

    assertMatches(makeRule("<token postag='[XW]RB' postag_regexp='yes'></token>"), 1);
    assertMatches(makeRule("<token postag='FOO|WRB' postag_regexp='yes'></token>"), 1);
    assertMatches(makeRule("<token postag='WRB|FOO' postag_regexp='yes'></token>"), 1);
    assertMatches(makeRule("<token postag='[XY]OO' postag_regexp='yes'></token>"), 0);

    // inflected
    assertMatches(makeRule("<token>grammar</token><token>checker</token>"), 0);
    assertMatches(makeRule("<token>grammar</token><token>checkers</token>"), 1);
    assertMatches(makeRule("<token>grammar</token><token inflected='yes'>checker</token>"), 1);
    
    // combine term and POS tag:
    assertMatches(makeRule("<token postag='WRB'>How</token>"), 1);
    assertMatches(makeRule("<token postag='[XW]RB' postag_regexp='yes'>How</token>"), 1);
    assertMatches(makeRule("<token postag='WRB'>Foo</token>"), 0);
    assertMatches(makeRule("<token postag='FOO'>How</token>"), 0);

    // rules with more than one token:
    assertMatches(makeRule("<token>How</token> <token>do</token>"), 1);
    //assertMatches(makeRule("<token>do</token> <token>How</token>"), 0);
    assertMatches(makeRule("<token>How</token> <token>foo</token>"), 0);
    assertMatches(makeRule("<token>How</token> <token>do</token> <token>you</token>"), 1);
    assertMatches(makeRule("<token>How</token> <token>do</token> <token>foo</token>"), 0);

    assertMatches(makeRule("<token regexp='yes'>Foo|How</token> <token>do</token>"), 1);

    assertMatches(makeRule("<token skip='-1'>How</token> <token>wonderful</token>"), 1);
    //assertMatches(makeRule("<token skip='-1'>wonderful</token> <token>How</token>"), 0);
    assertMatches(makeRule("<token skip='6'>How</token> <token>wonderful</token>"), 1);
    assertMatches(makeRule("<token skip='5'>How</token> <token>wonderful</token>"), 1);
    //assertMatches(makeRule("<token skip='4'>How</token> <token>wonderful</token>"), 0);

    assertMatches(makeRule("<token>How</token> <token skip='-1'>do</token> <token>wonderful</token>"), 1);
    assertMatches(makeRule("<token>How</token> <token skip='4'>do</token> <token>wonderful</token>"), 1);
    //assertMatches(makeRule("<token>How</token> <token skip='3'>do</token> <token>wonderful</token>"), 0);

    assertMatches(makeRule("<token skip='-1'>How</token> <token skip='-1'>thin</token> <token>wonderful</token>"), 1);
    assertMatches(makeRule("<token skip='3'>How</token> <token skip='3'>thin</token> <token>wonderful</token>"), 1);
    assertMatches(makeRule("<token skip='3'>How</token> <token skip='3'>thin</token> <token>foo</token>"), 0);

    assertMatches(makeRule("<token>E</token> <token>.</token> <token>G</token> <token>.</token>"), 1);
    assertMatches(makeRule("<token>X</token> <token>.</token> <token>G</token> <token>.</token>"), 0);
    //assertMatches(makeRule("<token>E</token> <token>,</token> <token>G</token> <token>.</token>"), 0);

    assertMatches(makeRule("<token>E</token> <token>.</token> <token>G</token> <token>.</token> <token>LanguageTool</token>"), 1);
    assertMatches(makeRule("<token>E</token> <token>.</token> <token>G</token> <token>.</token> <token>foo</token>"), 0);

    // negation:
    assertMatches(makeRule("<token>How</token> <token negate='yes'>foo</token>"), 1);
    assertMatches(makeRule("<token>How</token> <token negate='yes'>do</token>"), 1);  // known overmatching
    assertMatches(makeRule("<token>How</token> <token>do</token> <token negate='yes'>foo</token>"), 1);
    assertMatches(makeRule("<token>How</token> <token negate='yes'>foo</token> <token>you</token>"), 1);
    assertMatches(makeRule("<token>How</token> <token>do</token> <token negate='yes'>you</token>"), 1); // known overmatching
    assertMatches(makeRule("<token>How</token> <token negate='yes'>do</token> <token>you</token>"), 1); // known overmatching
    assertMatches(makeRule("<token>How</token> <token negate='yes'>do</token> <token negate='yes'>you</token>"), 1); // known overmatching
  }

  private void assertMatches(AbstractPatternRule patternRule, int expectedMatches) throws Exception {
    PatternRuleQueryBuilder queryBuilder = new PatternRuleQueryBuilder(language, searcher);
    Query query = queryBuilder.buildRelaxedQuery(patternRule);
    //System.out.println("QUERY: " + query);
    int matches = searcher.search(query, 1000).totalHits;
    assertEquals("Query failed: " + query, expectedMatches, matches);
  }

  private AbstractPatternRule makeRule(String ruleXml) throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append("<?xml version='1.0' encoding='UTF-8'?>");
    sb.append("<rules lang='en'> <category id='TEST' name='Test'> <rule id='TEST_RULE' name='test'>");
    sb.append("<pattern>");
    sb.append(ruleXml);
    sb.append("</pattern> </rule> </category> </rules>");
    InputStream input = new ByteArrayInputStream(sb.toString().getBytes());
    PatternRuleLoader ruleLoader = new PatternRuleLoader();
    List<AbstractPatternRule> rules = ruleLoader.getRules(input, "test.xml");
    assertEquals(1, rules.size());
    return rules.get(0);
  }

}
