/*
 * LanguageTool, a natural language style checker
 * Copyright (c) 2023.  Stefan Viol (https://stevio.de)
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  USA
 */

package org.languagetool;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public final class ExtendedSentenceRange extends SentenceRange {
  
  private final Map<String, Float> languageConfidenceRates = new TreeMap<>(); //languageCode;0-1 confidenceRate from LanguageDetectionService
  private final Map<String, Float> sentenceScoring = new TreeMap<>(); //Style; 0-1 score from scoring model
  
    
  ExtendedSentenceRange(int fromPos, int toPos) {
    super(fromPos, toPos);
  }
  
  public void addLanguageConfidenceRate(String languageCode, Float confidenceRate) {
    this.languageConfidenceRates.put(languageCode, confidenceRate);
  }
  
  public void addSentenceScoring(String style, Float score) {
    this.sentenceScoring.put(style, score);
  }

  public Map<String, Float> getLanguageConfidenceRates() {
    return Collections.unmodifiableMap(languageConfidenceRates);
  }

  public Map<String, Float> getSentenceScoring() {
    return Collections.unmodifiableMap(sentenceScoring);
  }
}
