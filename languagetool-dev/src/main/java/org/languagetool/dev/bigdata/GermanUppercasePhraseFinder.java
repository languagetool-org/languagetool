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
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.tools.StringTools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Prototype to find potential upper-only phrases like "Persischer Golf".
 * Uses Google 2-gram index.
 */
final class GermanUppercasePhraseFinder {

  private static final long MIN_TERM_LEN = 4;
  private static final long LIMIT = 500;

  private GermanUppercasePhraseFinder() {
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.out.println("Usage: " + GermanUppercasePhraseFinder.class.getSimpleName() + " <ngramIndexDir>");
      System.exit(1);
    }
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("de"));
    FSDirectory fsDir = FSDirectory.open(new File(args[0]).toPath());
    IndexReader reader = DirectoryReader.open(fsDir);
    IndexSearcher searcher = new IndexSearcher(reader);
    Fields fields = MultiFields.getFields(reader);
    Terms terms = fields.terms("ngram");
    TermsEnum termsEnum = terms.iterator();
    int count = 0;
    BytesRef next;
    while ((next = termsEnum.next()) != null) {
      String term = next.utf8ToString();
      count++;
      //term = "persischer Golf";  // for testing
      String[] parts = term.split(" ");
      boolean useful = true;
      int lcCount = 0;
      List<String> ucParts = new ArrayList<>();
      for (String part : parts) {
        if (part.length() < MIN_TERM_LEN) {
          useful = false;
          break;
        }
        String uc = StringTools.uppercaseFirstChar(part);
        if (!part.equals(uc)) {
          lcCount++;
        }
        ucParts.add(uc);
      }
      if (!useful || lcCount == 0 || lcCount == 2) {
        continue;
      }
      String uppercase = String.join(" ", ucParts);
      if (term.equals(uppercase)){
        continue;
      }
      long thisCount = getOccurrenceCount(reader, searcher, term);
      long thisUpperCount = getOccurrenceCount(reader, searcher, uppercase);
      if (count % 10_000 == 0) {
        System.err.println(count + " @ " + term);
      }
      if (thisCount > LIMIT || thisUpperCount > LIMIT) {
        if (thisUpperCount > thisCount) {
          if (isRelevant(lt, term)) {
            float factor = (float)thisUpperCount / thisCount;
            System.out.printf("%.2f " + thisUpperCount + " " + uppercase + " " + thisCount + " " + term + "\n", factor);
          }
        }
      }
    }
  }

  private static boolean isRelevant(JLanguageTool lt, String term) throws IOException {
    AnalyzedSentence analyzedSentence = lt.analyzeText(term).get(0);
    AnalyzedTokenReadings[] tokens = analyzedSentence.getTokensWithoutWhitespace();
    if (tokens.length == 1+2) {  // 1 is for sentence start
      if (tokens[1].hasPartialPosTag("ADJ:") && tokens[2].hasPartialPosTag("SUB:")) {
        return true;
      }
    }
    return false;
  }

  private static long getOccurrenceCount(IndexReader reader, IndexSearcher searcher, String term) throws IOException {
    TopDocs topDocs = searcher.search(new TermQuery(new Term("ngram", term)), 5);
    if (topDocs.totalHits == 0) {
      return 0;
    }
    int docId = topDocs.scoreDocs[0].doc;
    Document document = reader.document(docId);
    return Long.parseLong(document.get("count"));
  }
  
}
