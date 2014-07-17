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

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;

/**
 * Information about ngram occurrences, taken from a Lucene index.
 */
public class LuceneLanguageModel implements LanguageModel {

  private final FSDirectory directory;
  private final IndexReader reader;
  private final IndexSearcher searcher;
  
  public LuceneLanguageModel(File indexDir) throws IOException {
    directory = FSDirectory.open(indexDir);
    reader = DirectoryReader.open(directory);
    searcher = new IndexSearcher(reader);
  }
  
  @Override
  public long getCount(String token1, String token2) {
    Term term = new Term("ngram", token1 + " " + token2);
    return getCount(term);
  }

  @Override
  public long getCount(String token1, String token2, String token3) {
    Term term = new Term("ngram", token1 + " " + token2 + " " + token3);
    return getCount(term);
  }

  private long getCount(Term term) {
    try {
      TopDocs docs = searcher.search(new TermQuery(term), 1);
      if (docs.totalHits > 0) {
        int docId = docs.scoreDocs[0].doc;
        return Long.parseLong(reader.document(docId).get("count"));
      } else {
        return 0;
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close() {
    try {
      if (reader != null) reader.close();
      if (directory != null) directory.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
