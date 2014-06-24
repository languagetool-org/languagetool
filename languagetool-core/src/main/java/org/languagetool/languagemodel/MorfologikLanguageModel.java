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

import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.IStemmer;
import morfologik.stemming.WordData;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Information about ngram occurrences, taken from a Morfologik file.
 */
public class MorfologikLanguageModel implements LanguageModel {

  private final File file;
  
  private volatile Dictionary dictionary;
  
  public MorfologikLanguageModel(File file) {
    this.file = file;
  }

  protected Dictionary getDictionary() {
    Dictionary dict = dictionary;
    if (dict == null) {
      synchronized (this) {
        dict = dictionary;
        if (dict == null) {
          try {
            dictionary = dict = Dictionary.read(file);
          } catch (IOException e) {
            throw new RuntimeException("Could not load frequency dict file: " + file, e);
          }
        }
      }
    }
    return dict;
  }

  @Override
  public long getCount(String token1, String token2) {
    final IStemmer dictLookup = new DictionaryLookup(getDictionary());
    List<WordData> data = dictLookup.lookup(token1 + " " + token2);
    long count = 0;
    for (WordData wordData : data) {
      count += Long.parseLong(wordData.getStem().toString());   //we use the 'stem' field for the frequency value...
    }
    return count;
  }
}
