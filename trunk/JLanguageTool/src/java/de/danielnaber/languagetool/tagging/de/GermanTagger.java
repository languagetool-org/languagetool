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
package de.danielnaber.languagetool.tagging.de;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.IStemmer;
import morfologik.stemming.WordData;
import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.tagging.ManualTagger;
import de.danielnaber.languagetool.tagging.Tagger;
import de.danielnaber.languagetool.tokenizers.de.GermanCompoundTokenizer;
import de.danielnaber.languagetool.tools.StringTools;

/**
 * German tagger, requires data file in <code>resource/de/german.dict</code>.
 * 
 * @author Marcin Milkowski, Daniel Naber
 */
public class GermanTagger implements Tagger {

  private static final String DICT_FILENAME = "/de/german.dict";
  private static final String USER_DICT_FILENAME = "/de/added.txt";

  private IStemmer morfologik;
  private ManualTagger manualTagger;
  private GermanCompoundTokenizer compoundTokenizer;
  
  public GermanTagger() {
  }

  public AnalyzedGermanTokenReadings lookup(final String word) throws IOException {
    final List<String> words = new ArrayList<String>();
    words.add(word);
    final List<AnalyzedTokenReadings> result = tag(words, false);
    final AnalyzedGermanTokenReadings atr = (AnalyzedGermanTokenReadings) result.get(0);
    if (atr.getAnalyzedToken(0).getPOSTag() == null) {
      return null;
    }
    return atr;
  }
    
  @Override
  public List<AnalyzedTokenReadings> tag(final List<String> sentenceTokens) throws IOException {
    return tag(sentenceTokens, true);
  }
  
  public List<AnalyzedTokenReadings> tag(final List<String> sentenceTokens, final boolean ignoreCase) throws IOException {
    String[] taggerTokens;
    boolean firstWord = true;
    final List<AnalyzedTokenReadings> tokenReadings = new ArrayList<AnalyzedTokenReadings>();
    int pos = 0;
    // caching Lametyzator instance - lazy init
    if (morfologik == null) {      
      final URL url = JLanguageTool.getDataBroker().getFromResourceDirAsUrl(DICT_FILENAME);
      morfologik = new DictionaryLookup(Dictionary.read(url));      
    }
    if (manualTagger == null) {
      manualTagger = new ManualTagger(JLanguageTool.getDataBroker().getFromResourceDirAsStream(USER_DICT_FILENAME));
    }
    if (compoundTokenizer == null) {
      compoundTokenizer = new GermanCompoundTokenizer();
    }

    for (String word: sentenceTokens) {
      final List<AnalyzedGermanToken> l = new ArrayList<AnalyzedGermanToken>();
      taggerTokens = lexiconLookup(word);
      if (firstWord && taggerTokens == null && ignoreCase) { // e.g. "Das" -> "das" at start of sentence
        taggerTokens = lexiconLookup(word.toLowerCase());
        firstWord = false;
      }
      if (taggerTokens != null) {
        tagWord(taggerTokens, word, l);
      } else {
        // word not known, try to decompose it and use the last part for POS tagging:
        if (!StringTools.isEmpty(word.trim())) {
          final List<String> compoundParts = compoundTokenizer.tokenize(word);
          if (compoundParts.size() <= 1) {
            l.add(new AnalyzedGermanToken(word, null, null));
          } else {
            // last part governs a word's POS:
            String lastPart = compoundParts.get(compoundParts.size()-1);
            if (StringTools.startsWithUppercase(word)) {
              lastPart = StringTools.uppercaseFirstChar(lastPart);
            }
            taggerTokens = lexiconLookup(lastPart);
            if (taggerTokens != null) {
              tagWord(taggerTokens, word, l, compoundParts);
            } else {
              l.add(new AnalyzedGermanToken(word, null, null));
            }
          }
        } else {
          l.add(new AnalyzedGermanToken(word, null, null));
        }
      }
      
      //tokenReadings.add(new AnalyzedGermanToken(new AnalyzedTokenReadings((AnalyzedToken[]) l.toArray(new AnalyzedToken[0]))));
      tokenReadings.add(new AnalyzedGermanTokenReadings(l.toArray(new AnalyzedGermanToken[l.size()]), pos));
      pos += word.length();
    }
    return tokenReadings;
  }

  private void tagWord(String[] taggerTokens, String word, List<AnalyzedGermanToken> l) {
      tagWord(taggerTokens, word, l, null);
  }

  /**
   * @param compoundParts all compound parts of the complete word or <code>null</code>,
   *   if the original input is not a compound
   */
  private void tagWord(String[] taggerTokens, String word, List<AnalyzedGermanToken> l,
          List<String> compoundParts) {
    int i = 0;
    while (i < taggerTokens.length) {
      // Lametyzator returns data as String[]
      // first lemma, then annotations
      if (compoundParts != null) {
          // was originally a compound word
          final List<String> allButLastPart = compoundParts.subList(0, compoundParts.size() - 1);
          final String lemma = StringTools.listToString(allButLastPart, "")
              + StringTools.lowercaseFirstChar(taggerTokens[i]);
          l.add(new AnalyzedGermanToken(word, taggerTokens[i + 1], lemma));
      } else {
          l.add(new AnalyzedGermanToken(word, taggerTokens[i + 1], taggerTokens[i]));
      }
      i = i + 2;
    }
  }
  
  private String[] lexiconLookup(final String word) {
    final String[] posTagsFromUserDict = manualTagger.lookup(word);
    final List<WordData> posTagsFromDict = morfologik.lookup(word);
    if (posTagsFromUserDict != null && !posTagsFromDict.isEmpty()) {
      final String[] allPosTags = new String[posTagsFromUserDict.length + posTagsFromDict.size() * 2];
      //System.arraycopy(posTagsFromDict, 0, allPosTags, 0, posTagsFromDict.size());
      int i = 0;
      for (WordData wd : posTagsFromDict) {
        allPosTags[i] = wd.getStem().toString();
        allPosTags[i + 1] = wd.getTag().toString();
        i = i + 2;
      }
      System.arraycopy(posTagsFromUserDict, 0, allPosTags, posTagsFromDict.size() * 2, posTagsFromUserDict.length);
      return allPosTags;
    } else if (posTagsFromUserDict == null && !posTagsFromDict.isEmpty()) {
      final String[] allPosTags = new String[posTagsFromDict.size() * 2];
      int i = 0;
      for (WordData wd : posTagsFromDict) {
        allPosTags[i] = wd.getStem().toString();
        allPosTags[i + 1] = wd.getTag().toString();
        i = i + 2;
      }
      return allPosTags;
    } else {
      return posTagsFromUserDict;
    }
  }
    
  @Override
  public final AnalyzedGermanTokenReadings createNullToken(final String token, final int startPos) {
    return new AnalyzedGermanTokenReadings(new AnalyzedGermanToken(token, null, null), startPos);
  }

  @Override
  public AnalyzedToken createToken(String token, String posTag) {
    return new AnalyzedGermanToken(token, posTag);
  }
  
  /**
   * Test only
   */
  public static void main(final String[] args) throws IOException {
    final GermanTagger gt = new GermanTagger();
    final List<String> l = new ArrayList<String>();
    l.add("Einfacher");
    //System.err.println(gt.lookup("Treffen", 0));
    final List<AnalyzedTokenReadings> res = gt.tag(l);
    System.err.println(res);
  }
  
}
