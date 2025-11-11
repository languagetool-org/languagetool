/* LanguageTool, a natural language style checker
 * Copyright (C) 2025 Jaume Ortolà
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
package org.languagetool.synthesis.ca;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.chunking.ChunkTag;
import org.languagetool.synthesis.Synthesizer;

import java.io.IOException;
import java.util.regex.Pattern;

import static org.languagetool.rules.ca.PronomsFeblesHelper.pPronomFeble;

/*
 * Synthesize a new verb with newLemma (used in the last token) and newPostag (used in the first token).
 * The verb can be single-word or multi-word
 */
public class VerbSynthesizer {

  final public static Pattern pVerb = Pattern.compile("V.*");
  final public static Pattern pInflectedVerb = Pattern.compile("V.[SIM].*");
  final public static Pattern pVerbIS = Pattern.compile("V.[IS].*");
  final private static Pattern pNonParticiple = Pattern.compile("V.[^P].*");
  final private static Pattern pParticiple = Pattern.compile("V.P.*");

  AnalyzedTokenReadings[] tokens;
  int posFirstVerb = -1;
  int posLastVerb = -1;
  String newLemma;
  String newPostag;
  int numPronounsBefore = -1;
  int numPronounsAfter = -1;
  Language language;

  public VerbSynthesizer(AnalyzedTokenReadings[] tokens, int startPos, Language lang) {
    this.tokens = tokens;
    setIndexes(startPos);
    this.language = lang;
  }

  public void setLemmaAndPostag(String lemma, String postag) {
    this.newLemma = lemma;
    this.newPostag = postag;
  }

  private void setIndexes(int startPos) {
    int j = startPos;
    //If it is not a verb, find the first one
    while (j < tokens.length && !isVerb(j)) {
      j++;
    }
    if (isVerb(j)) {
      posFirstVerb = posLastVerb = j;
      //enrere
      int i = j - 1;
      while (isVerb(i)) {
        posFirstVerb = i;
        i--;
      }
      //avant
      i = j + 1;
      while (isVerb(i) && !(isFirstVerbIS() && isVerbIS(i))) {
        posLastVerb = i;
        i++;
      }
    }

    int i = 1;
    int pronounsAfter = 0;
    while (posLastVerb + i < tokens.length && !tokens[posLastVerb + i].isWhitespaceBefore() && tokens[posLastVerb + i].hasPosTagStartingWith("P")) {
      pronounsAfter++;
      i++;
    }
    numPronounsAfter = pronounsAfter;

    i = -1;
    int pronounsBeforeNoSpaceBefore = 0;
    int pronounsBefore = 0;
    while (posFirstVerb + i > 0 && tokens[posFirstVerb + i].readingWithTagRegex(pPronomFeble) != null) {
      if (tokens[posFirstVerb + i].isWhitespaceBefore() || posFirstVerb + i == 1) {
        pronounsBefore = pronounsBefore + pronounsBeforeNoSpaceBefore + 1;
        pronounsBeforeNoSpaceBefore = 0;
      } else {
        pronounsBeforeNoSpaceBefore++;
      }
      i--;
    }
    numPronounsBefore = pronounsBefore;
  }

  private boolean isVerb(int i) {
    if (i < 0 || i > tokens.length - 1) {
      return false; // out of bounds
    }
    return tokens[i].getChunkTags().contains(new ChunkTag("GV")) || tokens[i].readingWithTagRegex(pNonParticiple) != null
      || (tokens[i].readingWithTagRegex(pParticiple) != null && tokens[i].hasPosTag("_GV_"));
  }

  public String synthesize() throws IOException {
    Synthesizer synth = language.getSynthesizer();
    StringBuilder result = new StringBuilder();
    AnalyzedToken firstVerb = tokens[posFirstVerb].readingWithTagRegex(pVerb);
    if (posFirstVerb == posLastVerb) {
      String[] synthesized = synth.synthesize(new AnalyzedToken("", newPostag, newLemma),
        adjustPostagTolemma(newLemma, newPostag));
      if (synthesized != null && synthesized.length > 0) {
        result.append(synthesized[0]);
      }
    } else {
      for (int i = posFirstVerb; i <= posLastVerb; i++) {
        if (i == posFirstVerb) {
          String[] synthesized = synth.synthesize(firstVerb, newPostag);
          if (synthesized != null && synthesized.length > 0) {
            result.append(synthesized[0]);
          }
        } else if (i == posLastVerb) {
          if (tokens[i].isWhitespaceBefore()) {
            result.append(" ");
          }
          String postag = tokens[posLastVerb].readingWithTagRegex(pVerb).getPOSTag();
          AnalyzedToken toSynthesize = new AnalyzedToken("", postag, newLemma);
          String[] synthesized = synth.synthesize(toSynthesize, adjustPostagTolemma(newLemma, postag));
          if (synthesized != null && synthesized.length > 0) {
            result.append(synthesized[0]);
          }
        } else {
          if (tokens[i].isWhitespaceBefore()) {
            result.append(" ");
          }
          result.append(tokens[i].getToken());
        }
      }
    }
    return language.adaptSuggestion(result.toString(), "");
  }

  private String adjustPostagTolemma(String lemma, String postag) {
    if (lemma.equals("haver")) {
      postag = "VA" + postag.substring(2);
    }
    if (lemma.equals("ser")) {
      postag = "VS" + postag.substring(2);
    }
    return postag;
  }

  public String getStringFromTo(int start, int end) {
    StringBuilder sb = new StringBuilder();
    for (int i = start; i <= end; i++) {
      if (i > start && tokens[i].isWhitespaceBefore()) {
        sb.append(" ");
      }
      sb.append(tokens[i].getToken());
    }
    return sb.toString();
  }

  public String getPronounsStrBefore() {
    return getStringFromTo(posFirstVerb - numPronounsBefore, posFirstVerb - 1);
  }

  public String getPronounsStrAfter() {
    return getStringFromTo(posLastVerb + 1, posLastVerb + numPronounsAfter);
  }

  public int getFirstVerbPos() {
    return posFirstVerb;
  }

  public int getLastVerbPos() {
    return posLastVerb;
  }

  public int getNumPronounsAfter() {
    return numPronounsAfter;
  }

  public int getNumPronounsBefore() {
    return numPronounsBefore;
  }

  public String getFirstVerbPersonaNumber() {
    AnalyzedToken reading = tokens[posFirstVerb].readingWithTagRegex(pInflectedVerb);
    if (reading != null) {
      return reading.getPOSTag().substring(4, 6);
    }
    return "";
  }

  public boolean isFirstVerbIS() {
    AnalyzedToken reading = tokens[posFirstVerb].readingWithTagRegex(pVerbIS);
    return reading != null;
  }

  private boolean isVerbIS(int i) {
    if (i < 0 || i >= tokens.length) {
      return false;
    }
    AnalyzedToken reading = tokens[i].readingWithTagRegex(pVerbIS);
    return reading != null;
  }


  public String getCasingModel() {
    return getStringFromTo(posFirstVerb - numPronounsBefore, posFirstVerb);
  }

  public boolean isUndefined() {
    return (posFirstVerb == -1 || posLastVerb == -1 || numPronounsAfter == -1 || numPronounsBefore == -1);
  }

}
