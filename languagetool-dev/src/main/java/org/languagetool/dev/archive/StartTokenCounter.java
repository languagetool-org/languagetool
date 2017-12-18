/* LanguageTool, a natural language style checker 
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.archive;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.languagetool.languagemodel.LanguageModel;

import java.io.File;
import java.io.IOException;

/**
 * Add up the counts of all items that start with _START_. This way we
 * get a count for that, as the Google index doesn't see to contain it
 * but our formula needs it.
 * @since 3.2
 */
final class StartTokenCounter {

  private StartTokenCounter() {
  }

  public static void main(String[] args) throws IOException {
    long totalCount = 0;
    File dir = new File("/data/google-ngram-index/en/2grams");
    try (FSDirectory directory = FSDirectory.open(dir.toPath());
         IndexReader reader = DirectoryReader.open(directory)) {
      IndexSearcher searcher = new IndexSearcher(reader);
      Fields fields = MultiFields.getFields(reader);
      Terms ngrams = fields.terms("ngram");
      TermsEnum iterator = ngrams.iterator();
      BytesRef next;
      int i = 0;
      while ((next = iterator.next()) != null) {
        String term = next.utf8ToString();
        if (term.startsWith(LanguageModel.GOOGLE_SENTENCE_START)) {
          if (term.matches(".*_(ADJ|ADV|NUM|VERB|ADP|NOUN|PRON|CONJ|DET|PRT)$")) {
            //System.out.println("ignore: " + term);
            continue;
          }
          TopDocs topDocs = searcher.search(new TermQuery(new Term("ngram", term)), 3);
          if (topDocs.totalHits == 0) {
            throw new RuntimeException("No hits for " + term + ": " + topDocs.totalHits);
          } else if (topDocs.totalHits == 1) {
            int docId = topDocs.scoreDocs[0].doc;
            Document document = reader.document(docId);
            Long count = Long.parseLong(document.get("count"));
            //System.out.println(term + " -> " + count);
            totalCount += count;
            if (++i % 10_000 == 0) {
              System.out.println(i + " ... " + totalCount);
            }
          } else {
            throw new RuntimeException("More hits than expected for " + term + ": " + topDocs.totalHits);
          }
        }
      }
    }
    System.out.println("==> " + totalCount);
  }
  
}
