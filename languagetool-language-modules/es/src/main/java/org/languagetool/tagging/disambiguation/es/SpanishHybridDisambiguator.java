/* LanguageTool, a natural language style checker 
 * Copyright (C) 2007 Daniel Naber (http://www.danielnaber.de)
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

package org.languagetool.tagging.disambiguation.es;

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.language.Spanish;
import org.languagetool.tagging.disambiguation.AbstractDisambiguator;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.MultiWordChunker;
import org.languagetool.tagging.disambiguation.rules.XmlRuleDisambiguator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Hybrid chunker-disambiguator for Spanish
 * 
 * @author Jaume Ortolà
 */
public class SpanishHybridDisambiguator extends AbstractDisambiguator {

  private final Disambiguator chunker = new MultiWordChunker("/es/multiwords.txt", true, true);
  private final Disambiguator disambiguator = new XmlRuleDisambiguator(new Spanish(), true);

  @Override
  public AnalyzedSentence disambiguate(AnalyzedSentence input) throws IOException {
    return disambiguate(input, null);
  }

  /**
   * Calls two disambiguator classes: (1) a chunker; (2) a rule-based
   * disambiguator.
   */
  @Override
  public AnalyzedSentence disambiguate(AnalyzedSentence input, @Nullable JLanguageTool.CheckCancelledCallback checkCanceled) throws IOException {
    AnalyzedSentence analyzedSentence = chunker.disambiguate(input, checkCanceled);
    
    /* Put the results of the MultiWordChunker in a more appropriate and useful way
      <NP..></NP..> becomes NP.. NP..
      <NCMS000></NCMS000> becomes NCMS000 AQ0MS0
      The individual original tags are removed */
        
    AnalyzedTokenReadings[] aTokens = analyzedSentence.getTokens();
    int i=0;
    String POSTag = "";
    String lemma = "";
    String nextPOSTag = "";
    AnalyzedToken analyzedToken = null;
    while (i < aTokens.length) {
      if (!aTokens[i].isWhitespace()) {
        if (checkCanceled != null && checkCanceled.checkCancelled()) {
          break;
        }

        if (!nextPOSTag.isEmpty()) {
          AnalyzedToken newAnalyzedToken = new AnalyzedToken(aTokens[i].getToken(), nextPOSTag, lemma);
          if (aTokens[i].hasPosTagAndLemma("</" + POSTag + ">", lemma)) {
            nextPOSTag = "";
            lemma = "";
          }
          aTokens[i] = new AnalyzedTokenReadings(aTokens[i], Arrays.asList(newAnalyzedToken),
              "SpanishHybridDisambiguator");
        } else if ((analyzedToken = getMultiWordAnalyzedToken(aTokens, i)) != null) {
          POSTag = analyzedToken.getPOSTag().substring(1, analyzedToken.getPOSTag().length() - 1);
          lemma = analyzedToken.getLemma();
          AnalyzedToken newAnalyzedToken = new AnalyzedToken(analyzedToken.getToken(), POSTag, lemma);
          aTokens[i] = new AnalyzedTokenReadings(aTokens[i], Arrays.asList(newAnalyzedToken), "ESHybridDisamb");
          if (POSTag.startsWith("NC")) {
            nextPOSTag = "AQ0" + POSTag.substring(2, 4) + "0";
          } else {
            nextPOSTag = POSTag;
          }
        }
      }
      i++;
    }

    return disambiguator.disambiguate(new AnalyzedSentence(aTokens), checkCanceled);
  }
  
  private AnalyzedToken getMultiWordAnalyzedToken(AnalyzedTokenReadings[] aTokens, Integer i) {
    List<AnalyzedToken> l = new ArrayList<AnalyzedToken>();
    for (AnalyzedToken reading : aTokens[i]) {
      String POSTag = reading.getPOSTag();
      if (POSTag != null) {
        if (POSTag.startsWith("<") && POSTag.endsWith(">") && !POSTag.startsWith("</")) {
          l.add(reading);
        }
      }
    }
    // choose the longest one
    if (l.size() > 0) { 
      AnalyzedToken selectedAT = null;
      int maxDistance = 0;
      for (AnalyzedToken at : l) {
        String tag = "</" + at.getPOSTag().substring(1);
        String lemma = at.getLemma();
        int distance = 1;
        while (i + distance < aTokens.length) {
          if (aTokens[i + distance].hasPosTagAndLemma(tag, lemma)) {
            if (distance > maxDistance) {
              distance = maxDistance;
              selectedAT = at;
            }
            break;
          }
          distance++;
        }
      }
      return selectedAT;
    }
    return null;
    
  }

}
