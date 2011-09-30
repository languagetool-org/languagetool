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

import java.io.IOException;
import java.util.List;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.LuceneTestCase;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;

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
    searcher.close();
    directory.close();
    super.tearDown();
  }

  public void testIndexerSearcher() throws Exception {
    // Note that the second sentence ends with "lid" instead of "lids" (the inflated one)
    String content = "How to move back and fourth from linux to xmb? Calcium deposits on eye lid.";

    Indexer.run(content, directory, Language.ENGLISH, false);

    searcher = new IndexSearcher(directory);
    Searcher errorSearcher = new Searcher();
    List<TopDocs> topDocs = errorSearcher.run("BACK_AND_FOURTH", JLanguageTool.getDataBroker()
            .getFromRulesDirAsStream("/en/grammar.xml"), "/en/grammar.xml", searcher, true);
    assertEquals(1, topDocs.size());
    assertEquals(1, topDocs.get(0).totalHits);
    
    topDocs = errorSearcher.run("BACK_AND_FOURTH", JLanguageTool.getDataBroker()
        .getFromRulesDirAsStream("/en/grammar.xml"), "/en/grammar.xml", searcher, false);
    assertEquals(1, topDocs.size());
    assertEquals(1, topDocs.get(0).totalHits);

    topDocs = errorSearcher.run("ALL_OVER_THE_WORD", JLanguageTool.getDataBroker()
        .getFromRulesDirAsStream("/en/grammar.xml"), "/en/grammar.xml", searcher, true);
    assertEquals(1, topDocs.size());
    assertEquals(0, topDocs.get(0).totalHits);

    topDocs = errorSearcher.run("ALL_OVER_THE_WORD", JLanguageTool.getDataBroker()
        .getFromRulesDirAsStream("/en/grammar.xml"), "/en/grammar.xml", searcher, false);
    assertEquals(1, topDocs.size());
    assertEquals(0, topDocs.get(0).totalHits);

    try {
      errorSearcher.run("Invalid Rule Id",
          JLanguageTool.getDataBroker().getFromRulesDirAsStream("/en/grammar.xml"), "/en/grammar.xml", searcher, true);
      fail("Exception should be thrown for invalid rule id.");
    } catch (PatternRuleNotFoundException expected) {
      try {
        errorSearcher.run("Invalid Rule Id",
            JLanguageTool.getDataBroker().getFromRulesDirAsStream("/en/grammar.xml"), "/en/grammar.xml", searcher,
            false);
        fail("Exception should be thrown for invalid rule id.");
      } catch (PatternRuleNotFoundException expected2) {}
    }

    try {
      errorSearcher.run("EYE_BROW",
          JLanguageTool.getDataBroker().getFromRulesDirAsStream("/en/grammar.xml"), "/en/grammar.xml", searcher, true);
      fail("Exception should be thrown for unsupported PatternRule");
    } catch (IOException e) {
      assertTrue(e instanceof UnsupportedPatternRuleException);
      topDocs = errorSearcher
          .run("EYE_BROW",
              JLanguageTool.getDataBroker().getFromRulesDirAsStream("/en/grammar.xml"), "/en/grammar.xml", searcher,
              false);
      assertEquals(1, topDocs.size());
      assertEquals(1, topDocs.get(0).totalHits);
    }

    try {
      errorSearcher.run("ALL_FOR_NOT",
          JLanguageTool.getDataBroker().getFromRulesDirAsStream("/en/grammar.xml"), "/en/grammar.xml", searcher, true);
      fail("Exception should be thrown for unsupported PatternRule");
    } catch (UnsupportedPatternRuleException expected) {
      topDocs = errorSearcher
          .run("ALL_FOR_NOT",
              JLanguageTool.getDataBroker().getFromRulesDirAsStream("/en/grammar.xml"), "/en/grammar.xml", searcher,
              false);
      assertEquals(1, topDocs.size());
      assertEquals(0, topDocs.get(0).totalHits);
    }
  }
}
