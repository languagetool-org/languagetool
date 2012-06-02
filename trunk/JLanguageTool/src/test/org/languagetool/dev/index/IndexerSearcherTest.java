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

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.LuceneTestCase;
import org.languagetool.Language;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.Element;
import org.languagetool.rules.patterns.PatternRule;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class IndexerSearcherTest extends LuceneTestCase {

  private IndexSearcher searcher;
  private Directory directory;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    directory = newDirectory();
  }

  @Override
  public void tearDown() throws Exception {
    if (searcher != null) {
      searcher.close();
    }
    directory.close();
    super.tearDown();
  }

  public void testIndexerSearcher() throws Exception {
    // Note that the second sentence ends with "lid" instead of "lids" (the inflated one)
    final String content = "How to move back and fourth from linux to xmb? Calcium deposits on eye lid.";

    Indexer.run(content, directory, Language.ENGLISH, false);

    searcher = new IndexSearcher(directory);
    final Searcher errorSearcher = new Searcher();
    final File ruleFile = new File("src/rules/en/grammar.xml");
    TopDocs topDocs = errorSearcher.run("BACK_AND_FOURTH", ruleFile, searcher, true);
    assertEquals(1, topDocs.totalHits);

    topDocs = errorSearcher.run("BACK_AND_FOURTH", ruleFile, searcher, false);
    assertEquals(1, topDocs.totalHits);

    topDocs = errorSearcher.run("ALL_OVER_THE_WORD", ruleFile, searcher, true);
    assertEquals(0, topDocs.totalHits);

    topDocs = errorSearcher.run("ALL_OVER_THE_WORD", ruleFile, searcher, false);
    assertEquals(0, topDocs.totalHits);

    try {
      errorSearcher.run("Invalid Rule Id", ruleFile, searcher, true);
      fail("Exception should be thrown for invalid rule id.");
    } catch (PatternRuleNotFoundException expected) {
      try {
        errorSearcher.run("Invalid Rule Id", ruleFile, searcher, false);
        fail("Exception should be thrown for invalid rule id.");
      } catch (PatternRuleNotFoundException expected2) {}
    }

    try {
      errorSearcher.run("EYE_BROW", ruleFile, searcher, true);
      fail("Exception should be thrown for unsupported PatternRule");
    } catch (UnsupportedPatternRuleException expected) {}

    topDocs = errorSearcher.run("EYE_BROW", ruleFile, searcher, false);
    assertEquals(1, topDocs.totalHits);


    try {
      errorSearcher.run("ALL_FOR_NOT", ruleFile, searcher, true);
      fail("Exception should be thrown for unsupported PatternRule");
    } catch (UnsupportedPatternRuleException expected) {
      topDocs = errorSearcher
          .run("ALL_FOR_NOT", ruleFile, searcher, false);
      assertEquals(0, topDocs.totalHits);
    }
  }

  public void testIndexerSearcher2() throws Exception {
    final String content = "How to move back and fourth from linux to xmb?";

    Indexer.run(content, directory, Language.ENGLISH, false);

    final Searcher errorSearcher = new Searcher();
    final List<Element> elements = Arrays.asList(
            new Element("move", false, false, false),
            new Element("back", false, false, false)
    );
    final PatternRule rule1 = new PatternRule("RULE1", Language.ENGLISH, elements, "desc", "msg", "shortMsg");
    final IndexSearcher indexSearcher = new IndexSearcher(directory);
    try {
      final List<RuleMatch> matches = errorSearcher.findRuleMatchesOnIndex(rule1, Language.ENGLISH, indexSearcher);
      assertEquals(1, matches.size());
      assertEquals("RULE1", matches.get(0).getRule().getId());
    } finally {
      indexSearcher.close();
    }
  }

}
