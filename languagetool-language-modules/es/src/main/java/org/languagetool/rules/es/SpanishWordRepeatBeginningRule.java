/* LanguageTool, a natural language style checker 
 * Copyright (C) 2021 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.es;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.Example;
import org.languagetool.rules.WordRepeatBeginningRule;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Adds Spanish suggestions to {@link WordRepeatBeginningRule}.
 * 
 * @author Jaume Ortolà
 */
public class SpanishWordRepeatBeginningRule extends WordRepeatBeginningRule {
  
  public SpanishWordRepeatBeginningRule(ResourceBundle messages, Language language) {
    super(messages, language);
    //super.setDefaultTempOff();
    addExamplePair(Example.wrong("Asimismo, la calle es casi toda residencial. <marker>Asimismo</marker>, lleva el nombre de un poeta."),
                   Example.fixed("Además, la calle es casi toda residencal. También lleva el nombre de un poeta."));
  }
   
  @Override
  public String getId() {
    return "SPANISH_WORD_REPEAT_BEGINNING_RULE";
  }
  
    //==================== ADVERBS ======================
  
  // adverbs used to add to what the previous sentence mentioned
  private static final Set<String> ADD_ADVERBS = new HashSet<>();

  // adverbs used to express contrast to what the previous sentence mentioned
  private static final Set<String> CONTRAST_CONJ = new HashSet<>();

  // adverbs used to express emphasis to what the previous sentence mentioned
  private static final Set<String> EMPHASIS_ADVERBS = new HashSet<>();

  // adverbs used to explain what the previous sentence mentioned
  private static final Set<String> EXPLAIN_ADVERBS = new HashSet<>();
  
  // personal pronouns 
    private static final Set<String> PERSONAL_PRONOUNS = new HashSet<>();
    
  
  //==================== EXPRESSIONS ======================
  // the expressions will be used only as additional suggestions
  
  // linking expressions that can be used instead of the ADD_ADVERBS
  private static final List<String> ADD_EXPRESSIONS = Arrays.asList("Así mismo");
  
  // linking expressions that can be used instead of the CONTRAST_ADVERBS
  //TODO check if the comma is already present in the sentence
  private static final List<String> CONTRAST_EXPRESSIONS = Arrays.asList("Aun así", "Por otra parte", "Sin embargo");
  
  private static final List<String> EXCEPCIONS_START = Arrays.asList("el", "la", "los", "las", "punto", "artículo",
      "módulo", "parte", "sesión", "unidad", "tema", "n");
  
  static {
    // based on https://www.pinterest.com/pin/229542912245527548/
    ADD_ADVERBS.add("Asimismo");
    ADD_ADVERBS.add("Igualmente");
    ADD_ADVERBS.add("Además");
    ADD_ADVERBS.add("También");
    ADD_ADVERBS.add("Adicionalmente");
    CONTRAST_CONJ.add("Pero");
    CONTRAST_CONJ.add("Empero");
    CONTRAST_CONJ.add("Mas");
    EMPHASIS_ADVERBS.add("Obviamente");
    EMPHASIS_ADVERBS.add("Claramente");
    EMPHASIS_ADVERBS.add("Absolutamente");
    EMPHASIS_ADVERBS.add("Definitivamente");
    EXPLAIN_ADVERBS.add("Específicamente");
    EXPLAIN_ADVERBS.add("Concretamente");
    EXPLAIN_ADVERBS.add("Particularmente");
    EXPLAIN_ADVERBS.add("Precisamente");  
    PERSONAL_PRONOUNS.add("yo");
    PERSONAL_PRONOUNS.add("tú");
    PERSONAL_PRONOUNS.add("él");
    PERSONAL_PRONOUNS.add("ella");
    PERSONAL_PRONOUNS.add("nosostros");
    PERSONAL_PRONOUNS.add("nosotras");
    PERSONAL_PRONOUNS.add("vosotros");
    PERSONAL_PRONOUNS.add("vosotras");
    PERSONAL_PRONOUNS.add("ellos");
    PERSONAL_PRONOUNS.add("ellas");
    PERSONAL_PRONOUNS.add("usted");
    PERSONAL_PRONOUNS.add("ustedes");

  }

  @Override
  public boolean isException(String token) {
    return super.isException(token) || Character.isDigit(token.charAt(0))
        || EXCEPCIONS_START.contains(token.toLowerCase());
  }

  @Override
  protected boolean isAdverb(AnalyzedTokenReadings token) {
    if (token.hasPosTag("RG") || token.hasPosTag("LOC_ADV")) {
      return true;
    }
    String tok = token.getToken();
    return ADD_ADVERBS.contains(tok) || CONTRAST_CONJ.contains(tok) || EMPHASIS_ADVERBS.contains(tok)
        || EXPLAIN_ADVERBS.contains(tok);
  }

  @Override
  protected List<String> getSuggestions(AnalyzedTokenReadings token) {
    String tok = token.getToken();
    String lowerTok = tok.toLowerCase();
    // the repeated word is a personal pronoun
    if (PERSONAL_PRONOUNS.contains(lowerTok)) {
      return Arrays.asList("Además, " + lowerTok, "Igualmente, " + lowerTok,
          "No solo eso, sino que " + lowerTok);
    } else if (ADD_ADVERBS.contains(tok)) {
      List<String> addSuggestions = getDifferentAdverbsOfSameCategory(tok, ADD_ADVERBS);
      addSuggestions.addAll(ADD_EXPRESSIONS);
      return addSuggestions;
    } else if (CONTRAST_CONJ.contains(tok)) {
      List<String> contrastSuggestions = CONTRAST_EXPRESSIONS; //getDifferentAdverbsOfSameCategory(tok, CONTRAST_CONJ);
      return contrastSuggestions;
    } else if (EMPHASIS_ADVERBS.contains(tok)) {
      return getDifferentAdverbsOfSameCategory(tok, EMPHASIS_ADVERBS);
    } else if (EXPLAIN_ADVERBS.contains(tok)) {
      return getDifferentAdverbsOfSameCategory(tok, EXPLAIN_ADVERBS);
    }
    return Collections.emptyList();
  }

  /**
   * Gives suggestions to replace the given adverb.
   * 
   * @param adverb            to get suggestions for
   * @param adverbsOfCategory the adverbs of the same category as adverb (adverb
   *                          is <b>required</b> to be contained in the Set)
   * @return a List of suggested adverbs to replace the given adverb
   */
  private List<String> getDifferentAdverbsOfSameCategory(String adverb, Set<String> adverbsOfCategory) {
    return adverbsOfCategory.stream().filter(adv -> !adv.equals(adverb)).collect(Collectors.toList());
  }
}
