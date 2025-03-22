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
import org.languagetool.rules.AbstractWordCoherencyRule;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Example;
import org.languagetool.rules.WordCoherencyDataLoader;
import org.languagetool.synthesis.Synthesizer;

import java.io.IOException;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Catalan version of {@link AbstractWordCoherencyRule}.
 */
public class WordCoherencyValencianRule extends AbstractWordCoherencyRule {

  private final static Map<String, Set<String>> wordMap = new WordCoherencyDataLoader().loadWords("/ca/coherency-valencia.txt");
  private final Synthesizer synth = Languages.getLanguageForShortCode("ca").getSynthesizer();
  private final Pattern allowedPostags = Pattern.compile("[VAND].*");

  public WordCoherencyValencianRule(ResourceBundle messages) throws IOException {
    super(messages);
    this.setCategory(Categories.STYLE.getCategory(messages));
    addExamplePair(Example.wrong("<marker>Este</marker> home d'ací parla amb <marker>aquest</marker> altre ací."),
      Example.fixed("<marker>Este</marker> home d'ací parla amb <marker>este</marker> altre ací."));
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
    return "CA_WORD_COHERENCY_VALENCIA";
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
