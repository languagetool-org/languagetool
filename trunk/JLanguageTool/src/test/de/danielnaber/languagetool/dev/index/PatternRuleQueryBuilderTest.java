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

  private final String FIELD_NAME = "field";

  @Override
  public void setUp() throws Exception {
    super.setUp();
    directory = newDirectory();
    RandomIndexWriter writer = new RandomIndexWriter(random, directory, new LanguageToolAnalyzer(
        Version.LUCENE_31, new JLanguageTool(Language.ENGLISH)));
    Document doc = new Document();
    doc.add(newField(FIELD_NAME, "How do you thin about this wonderful idea?", Field.Store.NO,
        Field.Index.ANALYZED));
    writer.addDocument(doc);
    doc = new Document();
    doc.add(newField(FIELD_NAME, "other texts", Field.Store.NO, Field.Index.ANALYZED));
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
    StringBuffer sb = new StringBuffer();

    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> <rules lang=\"en\"> <category name=\"Test\"> <rule id=\"TEST_RULE\" name=\"test\"> <pattern>");

    sb.append("<token skip=\"-1\">How</token>"); // match "How"
    sb.append("<token postag=\"PRP\"></token>");// match "you/[PRP]"
    sb.append("<token skip=\"1\">thin</token>"); // match "thin"
    sb.append("<token postag_regexp=\"yes\" postag=\"DT|NN\">this</token>"); // match "this/[DT]"
    sb.append("<token regexp=\"yes\" negate=\"yes\">bad|good</token>"); // match "wonderful"
    sb.append("<token regexp=\"yes\">idea|proposal</token>"); // match "idea"

    sb.append("</pattern> </rule> </category> </rules>");

    InputStream input = new ByteArrayInputStream(sb.toString().getBytes());
    final PatternRuleLoader ruleLoader = new PatternRuleLoader();

    List<PatternRule> rules = ruleLoader.getRules(input, "test.xml");

    Query query = PatternRuleQueryBuilder.buildQuery(rules.get(0));
    System.out.println(query);
    assertEquals(1, searcher.search(query, null, 1000).totalHits);

  }

  public void testUnsupportedPatternRule() throws Exception {
    StringBuffer sb = new StringBuffer();

    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> <rules lang=\"en\"> <category name=\"Test\"> <rule id=\"TEST_RULE\" name=\"test\"> <pattern>");

    sb.append("<token skip=\"-1\">both<exception scope=\"next\">and</exception></token>"); // exception
                                                                                           // is not
                                                                                           // supported

    sb.append("</pattern> </rule> </category> </rules>");

    InputStream input = new ByteArrayInputStream(sb.toString().getBytes());
    final PatternRuleLoader ruleLoader = new PatternRuleLoader();

    List<PatternRule> rules = ruleLoader.getRules(input, "test.xml");
    try {
      PatternRuleQueryBuilder.buildQuery(rules.get(0));
      fail("Exception should be thrown for unsupported PatternRule");
    } catch (UnsupportedPatternRuleException expected) {}

    sb = new StringBuffer();

    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> <rules lang=\"en\"> <category name=\"Test\"> <rule id=\"TEST_RULE\" name=\"test\"> <pattern>");

    sb.append("<token inflected=\"yes\">suppose</token>"); // inflated token is not supported

    sb.append("</pattern> </rule> </category> </rules>");

    input = new ByteArrayInputStream(sb.toString().getBytes());

    rules = ruleLoader.getRules(input, "test.xml");
    try {
      PatternRuleQueryBuilder.buildQuery(rules.get(0));
      fail("Exception should be thrown for unsupported PatternRule");
    } catch (UnsupportedPatternRuleException expected) {}
      
  }
}
