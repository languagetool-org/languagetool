/* LanguageTool, a natural language style checker
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.ca;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Languages;
import org.languagetool.rules.*;
import org.languagetool.synthesis.Synthesizer;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Catalan version of {@link AbstractWordCoherencyRule}.
 */
public class WordCoherencyRule extends AbstractWordCoherencyRule {

  private final static Map<String, Set<String>> wordMap = new WordCoherencyDataLoader().loadWords("/ca/coherency.txt");
  private final Synthesizer synth = Languages.getLanguageForShortCode("ca").getSynthesizer();
  private final Pattern allowedPostags = java.util.regex.Pattern.compile("[VAND].*");

  public WordCoherencyRule(ResourceBundle messages) throws IOException {
    super(messages);
    this.setCategory(Categories.STYLE.getCategory(messages));
    addExamplePair(Example.wrong("Un <marker>pesebre</marker> ací i un altre <marker>pessebre</marker> allà."),
      Example.fixed("Un <marker>pesebre</marker> ací i un altre <marker>pesebre</marker> allà."));
  }

  @Override
  protected Map<String, Set<String>> getWordMap() {
    return wordMap;
  }

  @Override
  protected String getMessage(String word1, String word2) {
    return "No és coherent usar '" + word1 + "' i '" + word2 + "' dins d'un mateix text.";
  }

  @Override
  public String getId() {
    return "CA_WORD_COHERENCY";
  }

  @Override
  public String getShortMessage() {
    return "Coherència";
  }

  @Override
  public String getDescription() {
    return "Detecta l'ús incoherent de diferents formes dins d'un text.";
  }

  protected String createReplacement(String marked, String token, String otherSpelling, AnalyzedTokenReadings atrs) {
    String[] synthesizedForms;
    AnalyzedToken atr = atrs.readingWithTagRegex(allowedPostags);
    if (atr != null) {
      try {
        synthesizedForms = synth.synthesize(new AnalyzedToken("", "", otherSpelling), atr.getPOSTag());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      if (synthesizedForms != null && synthesizedForms.length > 0) {
        return synthesizedForms[0];
      }
    }
    return super.createReplacement(marked, token, otherSpelling, atrs);
  }

}
