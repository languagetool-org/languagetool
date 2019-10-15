/* LanguageTool, a natural language style checker 
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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

import org.apache.lucene.index.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.ConfusionSetLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Counts how many ngrams in the Lucene index are actually needed, i.e.
 * that contain a word from confusion_set.txt. 
 * @since 3.9
 */
final class NeededNGramCounter {

  private static final String LANG = "en";

  private NeededNGramCounter() {
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.out.println("Usage: " + NeededNGramCounter.class.getSimpleName() + " <ngramIndexDir>");
      System.exit(1);
    }
    Language lang = Languages.getLanguageForShortCode(LANG);
    String path = "/" + lang.getShortCode() + "/confusion_sets.txt";
    Set<String> ngrams;
    try (InputStream confSetStream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(path)) {
      ngrams = new ConfusionSetLoader().loadConfusionPairs(confSetStream).keySet();
    }
    String ngramIndexDir = args[0];
    FSDirectory fsDir = FSDirectory.open(new File(ngramIndexDir).toPath());
    IndexReader reader = DirectoryReader.open(fsDir);
    Fields fields = MultiFields.getFields(reader);
    Terms terms = fields.terms("ngram");
    TermsEnum termsEnum = terms.iterator();
    int i = 0;
    int needed = 0;
    int notNeeded = 0;
    BytesRef next;
    while ((next = termsEnum.next()) != null) {
      String term = next.utf8ToString();
      String[] tmpTerms = term.split(" ");
      boolean ngramNeeded = false;
      for (String tmpTerm : tmpTerms) {
        if (ngrams.contains(tmpTerm)) {
          ngramNeeded = true;
          break;
        }
      }
      if (ngramNeeded) {
        //System.out.println("needed: " + term);
        needed++;
      } else {
        //System.out.println("not needed: " + term);
        notNeeded++;
      }
      if (i % 500_000 == 0) {
        System.out.println(i + "/" + terms.getDocCount());
      }
      i++;
    }
    System.out.println("language         : " + LANG);
    System.out.println("ngram index      : " + ngramIndexDir);
    System.out.println("needed ngrams    : " + needed);
    System.out.println("not needed ngrams: " + notNeeded);
  }
  
}
