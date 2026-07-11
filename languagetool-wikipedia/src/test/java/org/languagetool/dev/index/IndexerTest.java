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

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.LuceneTestCase;
import org.languagetool.Language;
import org.languagetool.language.English;

import java.util.ArrayList;
import java.util.List;

import static org.languagetool.dev.index.Lucene.FIELD_NAME;

/**
 * Tests that {@link Indexer} segments input into one sentence per Lucene document (the invariant
 * {@link PatternRuleQueryBuilder} relies on), streaming line by line rather than accumulating whole
 * paragraphs in memory.
 */
public class IndexerTest extends LuceneTestCase {

  private final Language language = new English();

  /** A line with several sentences becomes several one-sentence documents; no blank lines needed. */
  public void testMultipleSentencesPerLineAreSplit() throws Exception {
    List<String> stored = indexAndReadBack("This is one. This is two.\nA third sentence here.");
    assertEquals(3, stored.size());
    assertTrue(stored.contains("This is one."));
    assertTrue(stored.contains("This is two."));
    assertTrue(stored.contains("A third sentence here."));
  }

  /** Blank and whitespace-only lines contribute no documents (guarded by the trim in add()). */
  public void testBlankAndWhitespaceLinesProduceNoDocs() throws Exception {
    List<String> stored = indexAndReadBack("First sentence.\n\n   \nSecond sentence.");
    assertEquals(2, stored.size());
    assertTrue(stored.contains("First sentence."));
    assertTrue(stored.contains("Second sentence."));
  }

  private List<String> indexAndReadBack(String content) throws Exception {
    List<String> stored = new ArrayList<>();
    try (Directory directory = new RAMDirectory()) {
      Indexer.run(content, directory, language);
      try (DirectoryReader reader = DirectoryReader.open(directory)) {
        for (int i = 0; i < reader.maxDoc(); i++) {
          stored.add(reader.document(i).get(FIELD_NAME));
        }
      }
    }
    return stored;
  }
}
