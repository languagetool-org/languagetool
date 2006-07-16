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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.dawidweiss.stemmers.Lametyzator;

import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.tagging.Tagger;

/**
 * German tagger, requires data file in <code>resource/de/german.dict</code>.
 * 
 * @author Marcin Milkowski, Daniel Naber
 */
public class GermanTagger implements Tagger {

  private static final String RESOURCE_FILENAME = "resource" + File.separator + "de"
      + File.separator + "german.dict";

  private Lametyzator morfologik = null;

  public GermanTagger() {
  }

  public AnalyzedGermanTokenReadings lookup(String word) throws IOException {
    List<String> l = new ArrayList<String>();
    l.add(word);
    List<AnalyzedTokenReadings> result = tag(l, false);
    AnalyzedGermanTokenReadings atr = (AnalyzedGermanTokenReadings) result.get(0);
    if (atr.getAnalyzedToken(0).getPOSTag() == null)
      return null;
    return atr;
  }

  public List<AnalyzedTokenReadings> tag(List sentenceTokens) throws IOException {
    return tag(sentenceTokens, true);
  }
  
  public List<AnalyzedTokenReadings> tag(List sentenceTokens, boolean ignoreCase) throws IOException {
    String[] taggerTokens;
    boolean firstWord = true;
    List<AnalyzedTokenReadings> tokenReadings = new ArrayList<AnalyzedTokenReadings>();
    int pos = 0;
    // caching Lametyzator instance - lazy init
    if (morfologik == null) {
      File resourceFile = JLanguageTool.getAbsoluteFile(RESOURCE_FILENAME);
      System.setProperty(Lametyzator.PROPERTY_NAME_LAMETYZATOR_DICT, resourceFile.getAbsolutePath());
      morfologik = new Lametyzator();
    }

    for (Iterator iter = sentenceTokens.iterator(); iter.hasNext();) {
      String word = (String) iter.next();
      
      List<AnalyzedGermanToken> l = new ArrayList<AnalyzedGermanToken>();
      taggerTokens = morfologik.stemAndForm(word);
      if (firstWord && taggerTokens == null && ignoreCase) { // e.g. "Das" -> "das" at start of sentence
        taggerTokens = morfologik.stemAndForm(word.toLowerCase());
        firstWord = false;
      }
      if (taggerTokens != null) {
        int i = 0;
        while (i < taggerTokens.length) {
          // Lametyzator returns data as String[]
          // first lemma, then annotations
          l.add(new AnalyzedGermanToken(word, taggerTokens[i + 1], taggerTokens[i]));
          i = i + 2;
        }
      } else {
        l.add(new AnalyzedGermanToken(word, null, pos));
      }
      pos += word.length();
      //tokenReadings.add(new AnalyzedGermanToken(new AnalyzedTokenReadings((AnalyzedToken[]) l.toArray(new AnalyzedToken[0]))));
      tokenReadings.add(new AnalyzedGermanTokenReadings((AnalyzedGermanToken[]) l.toArray(new AnalyzedGermanToken[0])));
    }
    return tokenReadings;
  }

  public Object createNullToken(String token, int startPos) {
    return new AnalyzedGermanTokenReadings(new AnalyzedGermanToken(token, null, startPos));
  }

  /**
   * Test only
   */
  public static void main(String[] args) throws IOException {
    GermanTagger gt = new GermanTagger();
    //PolishTagger gt =  new PolishTagger();
    List<String> l = new ArrayList<String>();
    l.add("Einfacher");
    //l.add("każdym");
    //System.err.println(gt.lookup("Treffen", 0));
    
    List res = gt.tag(l);
    System.err.println(res);
  }
  
}
