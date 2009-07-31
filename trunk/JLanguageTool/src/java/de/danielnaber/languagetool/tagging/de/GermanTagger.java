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
import java.util.ArrayList;
import java.util.List;

import morfologik.stemmers.Lametyzator;
import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.tagging.ManualTagger;
import de.danielnaber.languagetool.tagging.Tagger;
import de.danielnaber.languagetool.tokenizers.de.GermanCompoundTokenizer;
import de.danielnaber.languagetool.tools.StringTools;
import de.danielnaber.languagetool.tools.Tools;

/**
 * German tagger, requires data file in <code>resource/de/german.dict</code>.
 * 
 * @author Marcin Milkowski, Daniel Naber
 */
public class GermanTagger implements Tagger {

  private static final String DICT_FILENAME = "/resource/de/german.dict";
  private static final String USER_DICT_FILENAME = "/resource/de/added.txt";

  private static Lametyzator morfologik;
  private static ManualTagger manualTagger;
  private static GermanCompoundTokenizer compoundTokenizer;
  
  public GermanTagger() {
  }

  public AnalyzedGermanTokenReadings lookup(final String word) throws IOException {
    List<String> l = new ArrayList<String>();
    l.add(word);
    List<AnalyzedTokenReadings> result = tag(l, false);
    AnalyzedGermanTokenReadings atr = (AnalyzedGermanTokenReadings) result.get(0);
    if (atr.getAnalyzedToken(0).getPOSTag() == null)
      return null;
    return atr;
  }

  public void setFileName() {
    System.setProperty(Lametyzator.PROPERTY_NAME_LAMETYZATOR_DICTIONARY, 
        DICT_FILENAME);    
  }
    
  public List<AnalyzedTokenReadings> tag(final List<String> sentenceTokens) throws IOException {
    return tag(sentenceTokens, true);
  }
  
  public List<AnalyzedTokenReadings> tag(final List<String> sentenceTokens, final boolean ignoreCase) throws IOException {
    String[] taggerTokens;
    boolean firstWord = true;
    List<AnalyzedTokenReadings> tokenReadings = new ArrayList<AnalyzedTokenReadings>();
    int pos = 0;
    // caching Lametyzator instance - lazy init
    if (morfologik == null) {
      setFileName();
      morfologik = new Lametyzator();
    }
    if (manualTagger == null) {
      manualTagger = new ManualTagger(Tools.getStream(USER_DICT_FILENAME));
    }
    if (compoundTokenizer == null) {
      compoundTokenizer = new GermanCompoundTokenizer();
    }

    for (String word: sentenceTokens) {
      List<AnalyzedGermanToken> l = new ArrayList<AnalyzedGermanToken>();
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
          List<String> compoundParts = compoundTokenizer.tokenize(word);
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
              tagWord(taggerTokens, word, l);
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
    int i = 0;
    while (i < taggerTokens.length) {
      // Lametyzator returns data as String[]
      // first lemma, then annotations
      l.add(new AnalyzedGermanToken(word, taggerTokens[i + 1], taggerTokens[i]));
      i = i + 2;
    }
  }
  
  private String[] lexiconLookup(final String word) {
    String[] posTagsFromUserDict = manualTagger.lookup(word);
    String[] posTagsFromDict = morfologik.stemAndForm(word);
    if (posTagsFromUserDict != null && posTagsFromDict != null) {
      String[] allPosTags = new String[posTagsFromUserDict.length + posTagsFromDict.length];
      System.arraycopy(posTagsFromDict, 0, allPosTags, 0, posTagsFromDict.length);
      System.arraycopy(posTagsFromUserDict, 0, allPosTags, posTagsFromDict.length, posTagsFromUserDict.length);
      return allPosTags;
    } else if (posTagsFromUserDict == null && posTagsFromDict != null) {
      return posTagsFromDict;
    } else {
      return posTagsFromUserDict;
    }
  }
    
  public final AnalyzedGermanTokenReadings createNullToken(final String token, final int startPos) {
    return new AnalyzedGermanTokenReadings(new AnalyzedGermanToken(token, null, null), startPos);
  }

  public AnalyzedToken createToken(String token, String posTag) {
    return new AnalyzedGermanToken(token, posTag);
  }
  
  /**
   * Test only
   */
  public static void main(final String[] args) throws IOException {
    GermanTagger gt = new GermanTagger();
    List<String> l = new ArrayList<String>();
    l.add("Einfacher");
    //System.err.println(gt.lookup("Treffen", 0));
    
    List<AnalyzedTokenReadings> res = gt.tag(l);
    System.err.println(res);
  }
  
}
