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
package org.languagetool.dev;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.FSDirectory;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class FrequencyIndexCreatorTest {

  private static final File INDEX_DIR = new File("/media/Data/google-ngram/3gram/lucene-index");

  @Test
  @Ignore("Interactive use only")
  public void testReadPerformance() throws IOException {
    try (FSDirectory directory = FSDirectory.open(INDEX_DIR.toPath())) {
      DirectoryReader reader = DirectoryReader.open(directory);
      IndexSearcher searcher = new IndexSearcher(reader);
      try (Scanner scanner = new Scanner(new File("/lt/performance-test/en.txt"))) {
        while (scanner.hasNextLine()) {
          String line = scanner.nextLine();
          String[] parts = line.split(" ");
          accessNgrams(parts, searcher);
        }
      }
    }
  }

  private void accessNgrams(String[] parts, IndexSearcher searcher) throws IOException {
    String prevPart = null;
    String prevPrevPart = null;
    for (String part : parts) {
      if (prevPart != null && prevPrevPart != null) {
        String ngram = prevPrevPart + " " + prevPart + " " + part;
        long startTime = System.currentTimeMillis(); 
        Query query = new TermQuery(new Term("ngram", ngram));
        ScoreDoc[] hits = searcher.search(query, 10).scoreDocs;
        //if (hits.length == 0) {
        //  System.out.println("No hit for: " + ngram);
        //}
        for (ScoreDoc hit : hits) {
          Document hitDoc = searcher.doc(hit.doc);
          long runTime = System.currentTimeMillis() - startTime;
          System.out.println(ngram + ": " + hitDoc.getField("count").stringValue() + " (" + runTime + "ms)");
        }
      }
      prevPrevPart = prevPart;
      prevPart = part;
    }
  }
}
