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
package org.languagetool.tagging;

import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.IStemmer;
import morfologik.stemming.WordData;
import org.languagetool.JLanguageTool;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Tags a word using a Morfologik binary dictionary.
 * @since 2.8
 */
public class MorfologikTagger implements WordTagger {

  private final URL dictUrl;

  private Dictionary dictionary;

  public MorfologikTagger(String dictPath) {
    dictUrl = JLanguageTool.getDataBroker().getFromResourceDirAsUrl(Objects.requireNonNull(dictPath));
  }

  MorfologikTagger(URL dictUrl) {
    this.dictUrl = Objects.requireNonNull(dictUrl);
  }

  private synchronized Dictionary getDictionary() throws IOException {
    if (dictionary == null) {
      dictionary = Dictionary.read(dictUrl);
    }
    return dictionary;
  }

  @Override
  public List<TaggedWord> tag(String word) {
    List<TaggedWord> result = new ArrayList<>();
    try {
      IStemmer dictLookup = new DictionaryLookup(getDictionary());
      List<WordData> lookup = dictLookup.lookup(word);
      for (WordData wordData : lookup) {
        TaggedWord taggedWord = new TaggedWord(wordData.getStem() == null ? null : wordData.getStem().toString(),
                                               wordData.getTag() == null ? null : wordData.getTag().toString());
        result.add(taggedWord);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return result;
  }

}
