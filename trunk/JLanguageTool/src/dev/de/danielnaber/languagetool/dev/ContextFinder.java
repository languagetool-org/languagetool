/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package de.danielnaber.languagetool.dev;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;

/**
 * Compare the one-word right or left context of two words. This is useful
 * to find potential rules for similar words, i.e. contexts that are typical
 * for only one of the words.  
 * 
 * @author Daniel Naber
 */
public class ContextFinder {

  private ContextFinder() {}

  public static void main(String[] args) throws IOException {
    if (args.length != 4 || !args[3].startsWith("--context")) {
      printUsageAndExit();
    }
    final ContextFinder prg = new ContextFinder();
    if (args[3].endsWith("=right"))
      prg.run(args[0], args[1], args[2], true);
    else if (args[3].endsWith("=left"))
      prg.run(args[0], args[1], args[2], false);
    else
      printUsageAndExit();
  }
  
  private static void printUsageAndExit() {
    System.err.println("Usage: ContextFinder <indexDir> <term1> <term2> --context=right|left"); 
    System.exit(1);
  }
  
  private void run(String indexDir, String term1, String term2, boolean rightContext) throws IOException {
    final IndexReader reader = IndexReader.open(indexDir);
    final IndexSearcher searcher = new IndexSearcher(reader);
    final TermEnum termEnum = reader.terms();
    int termCount = 0;
    System.out.println(term1 + ": " + reader.docFreq(new Term(Indexer.BODY_FIELD, term1)) + "x");
    System.out.println(term2 + ": " + reader.docFreq(new Term(Indexer.BODY_FIELD, term2)) + "x");
    while (termEnum.next()) {
      final Term t = termEnum.term();
      if (isPOSTag(t))
        continue;
      // first term:
      final PhraseQuery pq1 = makeQuery(t, term1, rightContext);
      final int hits1 = search(pq1, searcher);
      // second term:
      final PhraseQuery pq2 = makeQuery(t, term2, rightContext);
      final int hits2 = search(pq2, searcher);
      final float rel = (float)(hits1+1) / (float)(hits2+1);
      if (rel > 1.0f)
        System.out.println("#1: " + rel + ": " + myToString(pq1) + ": " + hits1 + " <-> " + myToString(pq2) + ": " + hits2);
      else if (rel < 1.0f)
        System.out.println("#2: " + rel + ": " + myToString(pq1) + ": " + hits1 + " <-> " + myToString(pq2) + ": " + hits2);
      termCount++;
    }
    System.out.println("termCount = " + termCount);
    searcher.close();
    reader.close();
  }

  private String myToString(PhraseQuery pq) {
    return pq.toString().replaceAll("body:", "");
  }

  private PhraseQuery makeQuery(Term t, String term1, boolean rightContext) {
    final PhraseQuery pq = new PhraseQuery();
    if (rightContext) {
      pq.add(new Term(Indexer.BODY_FIELD, term1));
      pq.add(new Term(Indexer.BODY_FIELD, t.text()));
    } else {
      pq.add(new Term(Indexer.BODY_FIELD, t.text()));
      pq.add(new Term(Indexer.BODY_FIELD, term1));
    }
    return pq;
  }

  private int search(PhraseQuery pq, IndexSearcher searcher) throws IOException {
    //long time = System.currentTimeMillis();
    final Hits h = searcher.search(pq);
    //long searchTime = System.currentTimeMillis()-time;
    if (h.length() > 0) {
      //System.err.println(h.length() + " " + pq);
      //System.err.println("  " + searchTime + "ms");
    }
    return h.length();
  }

  private boolean isPOSTag(Term t) {
    if (t.text().equals(t.text().toUpperCase())) {    // e.g. "VER:1:PLU:KJ2:NON:NEB"
      return true;
    }
    return false;
  }

}
