/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.languagemodel;

import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Information about ngram occurrences, taken from a Lucene index.
 * @since 2.7
 */
public class LuceneLanguageModel implements LanguageModel {

  private final List<File> indexes = new ArrayList<>();
  private final Map<Integer,LuceneSearcher> luceneSearcherMap = new HashMap<>();
  private final File topIndexDir;

  /**
   * @param topIndexDir a directory which contains at least another sub directory called {@code 3grams},
   *                    which is a Lucene index with ngram occurrences as created by
   *                    {@code org.languagetool.dev.FrequencyIndexCreator}.
   */
  public LuceneLanguageModel(File topIndexDir) throws IOException {
    if (!topIndexDir.exists() || !topIndexDir.isDirectory()) {
      throw new RuntimeException("Not found or is not a directory: " + topIndexDir);
    }
    this.topIndexDir = topIndexDir;
    addIndex(topIndexDir, 2);
    addIndex(topIndexDir, 3);
    if (luceneSearcherMap.size() == 0) {
      throw new RuntimeException("No directories '2grams' and/or '3grams' found in " + topIndexDir);
    }
  }

  private void addIndex(File topIndexDir, int ngramSize) throws IOException {
    File indexDir = new File(topIndexDir, ngramSize + "grams");
    if (indexDir.exists() && indexDir.isDirectory()) {
      luceneSearcherMap.put(ngramSize, new LuceneSearcher(indexDir));
      indexes.add(indexDir);
    }
  }

  @Override
  public long getCount(String token1, String token2) {
    Objects.requireNonNull(token1);
    Objects.requireNonNull(token2);
    Term term = new Term("ngram", token1 + " " + token2);
    LuceneSearcher luceneSearcher = getLuceneSearcher(2);
    return getCount(term, luceneSearcher);
  }

  @Override
  public long getCount(String token1, String token2, String token3) {
    Objects.requireNonNull(token1);
    Objects.requireNonNull(token2);
    Objects.requireNonNull(token3);
    Term term = new Term("ngram", token1 + " " + token2 + " " + token3);
    LuceneSearcher luceneSearcher = getLuceneSearcher(3);
    long count = getCount(term, luceneSearcher);
    //System.out.println("Lookup: " + token1 + " " + token2 + " " + token3 + " => " + count);
    return count;
  }

  protected LuceneSearcher getLuceneSearcher(int ngramSize) {
    LuceneSearcher luceneSearcher = luceneSearcherMap.get(ngramSize);
    if (luceneSearcher == null) {
      throw new RuntimeException("No " + ngramSize + "grams directory found in " + topIndexDir);
    }
    return luceneSearcher;
  }

  private long getCount(Term term, LuceneSearcher luceneSearcher) {
    try {
      TopDocs docs = luceneSearcher.searcher.search(new TermQuery(term), 1);
      if (docs.totalHits > 0) {
        int docId = docs.scoreDocs[0].doc;
        return Long.parseLong(luceneSearcher.reader.document(docId).get("count"));
      } else {
        return 0;
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close() {
    for (LuceneSearcher searcher : luceneSearcherMap.values()) {
      try {
        searcher.reader.close();
        searcher.directory.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public String toString() {
    return indexes.toString();
  }

  protected static class LuceneSearcher {
    final FSDirectory directory;
    final IndexReader reader;
    final IndexSearcher searcher;
    private LuceneSearcher(File indexDir) throws IOException {
      this.directory = FSDirectory.open(indexDir);
      this.reader = DirectoryReader.open(directory);
      this.searcher = new IndexSearcher(reader);
    }
    public IndexReader getReader() {
      return reader;
    }
  }
}
