/* LanguageTool, a natural language style checker 
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.bigdata;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.File;
import java.io.IOException;

/**
 * Find the ngram with the largest occurrence in a Lucene index,
 * i.e. the one with the largest value in the 'count' field.
 * @since 3.3
 */
final class LargestNGramFinder {

  private LargestNGramFinder() {
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.out.println("Usage: " + LargestNGramFinder.class.getSimpleName() + " <ngramIndexDir>");
      System.exit(1);
    }
    FSDirectory fsDir = FSDirectory.open(new File(args[0]).toPath());
    IndexReader reader = DirectoryReader.open(fsDir);
    IndexSearcher searcher = new IndexSearcher(reader);
    Fields fields = MultiFields.getFields(reader);
    long max = 0;
    String maxTerm = "";
    Terms terms = fields.terms("ngram");
    TermsEnum termsEnum = terms.iterator();
    int count = 0;
    BytesRef next;
    while ((next = termsEnum.next()) != null) {
      String term = next.utf8ToString();
      TopDocs topDocs = searcher.search(new TermQuery(new Term("ngram", term)), 5);
      int docId = topDocs.scoreDocs[0].doc;
      Document document = reader.document(docId);
      long thisCount = Long.parseLong(document.get("count"));
      if (max < thisCount) {
        max = thisCount;
        maxTerm = term;
      }
      if (count % 10_000 == 0) {
        System.out.println(count + " -> " + topDocs.totalHits + " for " + term + " -> " + thisCount + ", max so far: " + max + " for '" + maxTerm + "'");
      }
      count++;
    }
    System.out.println("Max: " + max + " for " + maxTerm);
  }
  
}
