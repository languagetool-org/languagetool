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

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * Walks through a file and looks up the occurrence count for the word
 * in each line.
 */
class OccurrenceFinder {

  private void run(File input, File indexDir) throws IOException {
    FSDirectory directory = FSDirectory.open(indexDir);
    DirectoryReader reader = DirectoryReader.open(directory);
    IndexSearcher searcher = new IndexSearcher(reader);
    try (Scanner s = new Scanner(input)) {
      while (s.hasNextLine()) {
        String line = s.nextLine();
        long occurrences = getCount(new Term("ngram", line), searcher);
        System.out.println(occurrences + "\t" + line);
      }
    }
  }

  private long getCount(Term term, IndexSearcher searcher) throws IOException {
    TopDocs docs = searcher.search(new TermQuery(term), 1);
    if (docs.totalHits > 0) {
      int docId = docs.scoreDocs[0].doc;
      return Long.parseLong(searcher.getIndexReader().document(docId).get("count"));
    } else {
      return 0;
    }
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.out.println("Usage: " + OccurrenceFinder.class.getName() + " <textfile> <luceneNgramIndex>");
      System.exit(1);
    }
    OccurrenceFinder finder = new OccurrenceFinder();
    finder.run(new File(args[0]), new File(args[1]));
  }
}
