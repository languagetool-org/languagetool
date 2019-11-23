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

import org.apache.lucene.index.Fields;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;
import org.languagetool.JLanguageTool;
import org.languagetool.languagemodel.LuceneSingleIndexLanguageModel;
import org.languagetool.rules.ConfusionPair;
import org.languagetool.rules.ConfusionSetLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Dump the occurrences of homophone 3grams to STDOUT. Useful to have a more
 * compact file with homophone occurrences, as searching the homophones and
 * their contexts in the Lucene index requires iterating all terms and is
 * thus slow.
 * @since 2.8
 */
class HomophoneOccurrenceDumper extends LuceneSingleIndexLanguageModel {

  private static final int MIN_COUNT = 1000;

  HomophoneOccurrenceDumper(File topIndexDir) throws IOException {
    super(topIndexDir);
  }

  /**
   * Get the context (left and right words) for the given word(s). This is slow,
   * as it needs to scan the whole index.
   */
  Map<String,Long> getContext(String... tokens) throws IOException {
    Objects.requireNonNull(tokens);
    TermsEnum iterator = getIterator();
    Map<String,Long> result = new HashMap<>();
    BytesRef byteRef;
    int i = 0;
    while ((byteRef = iterator.next()) != null) {
      String term = new String(byteRef.bytes, byteRef.offset, byteRef.length);
      for (String token : tokens) {
        if (term.contains(" " + token + " ")) {
          String[] split = term.split(" ");
          if (split.length == 3) {
            long count = getCount(Arrays.asList(split[0], split[1], split[2]));
            result.put(term, count);
          }
        }
      }
      /*if (i++ > 1_000_000) { // comment in for faster testing with subsets of the data
        break;
      }*/
    }
    return result;
  }

  private void run(String confusionSetPath) throws IOException {
    System.err.println("Loading confusion sets from " + confusionSetPath + ", minimum occurrence: " + MIN_COUNT);
    ConfusionSetLoader confusionSetLoader = new ConfusionSetLoader();
    InputStream inputStream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(confusionSetPath);
    Map<String,List<ConfusionPair>> map = confusionSetLoader.loadConfusionPairs(inputStream);
    Set<String> confusionTerms = map.keySet();
    dumpOccurrences(confusionTerms);
  }

  private void dumpOccurrences(Set<String> tokens) throws IOException {
    Objects.requireNonNull(tokens);
    TermsEnum iterator = getIterator();
    BytesRef byteRef;
    int i = 0;
    while ((byteRef = iterator.next()) != null) {
      String term = new String(byteRef.bytes, byteRef.offset, byteRef.length);
      String[] split = term.split(" ");
      if (split.length == 3) {
        String token = split[1];
        if (tokens.contains(token)) {
          long count = getCount(Arrays.asList(split[0], split[1], split[2]));
          if (count >= MIN_COUNT) {
            System.out.println(token + "\t" + count + "\t" + split[0] + " " + split[1] + " " + split[2]);
          }
        }
      }
      if (i % 10_000 == 0) {
        System.err.println(i + "...");
      }
      i++;
    }
  }

  private TermsEnum getIterator() throws IOException {
    LuceneSearcher luceneSearcher = getLuceneSearcher(3);
    Fields fields = MultiFields.getFields(luceneSearcher.getReader());
    Terms terms = fields.terms("ngram");
    return terms.iterator();
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.out.println("Usage: " + HomophoneOccurrenceDumper.class.getSimpleName() + " <indexDir>");
      System.exit(1);
    }
    try (HomophoneOccurrenceDumper dumper = new HomophoneOccurrenceDumper(new File(args[0]))) {
      dumper.run("/en/confusion_sets.txt");
    }
  }

  @Override
  public long getTotalTokenCount() {
    throw new RuntimeException("not implemented");
  }
}
