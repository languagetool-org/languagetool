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
package org.languagetool.dev.bigdata;

import org.languagetool.JLanguageTool;
import org.languagetool.rules.ConfusionPair;
import org.languagetool.rules.ConfusionSetLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Generate URLs for the confusion set to download the parts of Google's ngram corpus (v2)
 * that we need to cover the confusion set.
 * @since 2.7
 */
@SuppressWarnings("DynamicRegexReplaceableByCompiledPattern")
final class NGramUrlGenerator {

  private NGramUrlGenerator() {}

  public static void main(String[] args) throws IOException {
    String url = "http://storage.googleapis.com/books/ngrams/books/googlebooks-eng-all-4gram-20120701-<XX>.gz";
    String chars = "abcdefghijklmnopqrstuvwxyz";
    String chars2 = "abcdefghijklmnopqrstuvwxyz_";
    for (int i = 0; i <= 9; i++) {
      System.out.println(url.replace("<XX>", String.valueOf(i)));
    }
    for (int i = 0; i < chars.length(); i++) {
      for (int j = 0; j < chars2.length(); j++) {
        String name = String.valueOf(chars.charAt(i)) + String.valueOf(chars2.charAt(j));
        System.out.println(url.replace("<XX>", name));
      }
    }
    System.out.println(url.replace("<XX>", "punctuation"));
  }

  public static void mainDownloadSome(String[] args) throws IOException {
    ConfusionSetLoader confusionSetLoader =  new ConfusionSetLoader();
    InputStream inputStream = JLanguageTool.getDataBroker().getFromResourceDirAsStream("/en/homophones.txt");
    Map<String,List<ConfusionPair>> map = confusionSetLoader.loadConfusionPairs(inputStream);
    String url = "http://storage.googleapis.com/books/ngrams/books/googlebooks-eng-all-2gram-20120701-<XX>.gz";
    Set<String> nameSet = new HashSet<>();
    for (String s : map.keySet()) {
      if (s.length() < 2) {
        nameSet.add(s.substring(0, 1).toLowerCase() + "_");
      } else {
        nameSet.add(s.substring(0, 2).toLowerCase());
      }
    }
    List<String> nameList = new ArrayList<>(nameSet);
    Collections.sort(nameList);
    for (String name : nameList) {
      System.out.println(url.replace("<XX>", name));
    }
    System.err.println("Number of files: " + nameList.size());
  }
}
