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
package org.languagetool.rules.ca;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.Example;
import org.languagetool.rules.WordRepeatBeginningRule;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Adds Catalan suggestions to {@link WordRepeatBeginningRule}.
 * 
 * @author Jaume Ortolà
 */
public class CatalanWordRepeatBeginningRule extends WordRepeatBeginningRule {

  public CatalanWordRepeatBeginningRule(ResourceBundle messages, Language language) {
    super(messages, language);
    //super.setDefaultTempOff();
    addExamplePair(Example.wrong("Però el carrer és tot modernista. <marker>Però</marker> té nom de poeta."),
        Example.fixed("Però el carrer és tot modernista. Així i tot, té nom de poeta."));
  }

  @Override
  public String getId() {
    return "CATALAN_WORD_REPEAT_BEGINNING_RULE";
  }

  // ==================== ADVERBS ======================

  // adverbs used to add to what the previous sentence mentioned
  private static final Set<String> ADD_ADVERBS = new HashSet<>();

  // adverbs used to express contrast to what the previous sentence mentioned
  private static final Set<String> CONTRAST_CONJ = new HashSet<>();
  private static final Set<String> CAUSE_CONJ = new HashSet<>();

  // adverbs used to express emphasis to what the previous sentence mentioned
  private static final Set<String> EMPHASIS_ADVERBS = new HashSet<>();

  // adverbs used to explain what the previous sentence mentioned
  private static final Set<String> EXPLAIN_ADVERBS = new HashSet<>();

  // personal pronouns
  private static final Set<String> PERSONAL_PRONOUNS = new HashSet<>();

  // ==================== EXPRESSIONS ======================
  // the expressions will be used only as additional suggestions

  // linking expressions that can be used instead of the ADD_ADVERBS
  private static final List<String> ADD_EXPRESSIONS = Arrays.asList("Així mateix", "A més a més");

  // linking expressions that can be used instead of the CONTRAST_ADVERBS
  private static final List<String> CONTRAST_EXPRESSIONS = Arrays.asList("Així i tot", "D'altra banda",
      "Per altra part");
  private static final List<String> CAUSE_EXPRESSIONS = Arrays.asList("Ja que", "Per tal com", "Pel fet que",
      "Puix que");

  private static final List<String> EXCEPCIONS_START = Arrays.asList("el", "la", "els", "les", "punt", "article",
      "mòdul", "part", "sessió", "unitat", "tema", "a");

  static {
    // based on https://www.pinterest.com/pin/229542912245527548/

    ADD_ADVERBS.add("Igualment");
    ADD_ADVERBS.add("També");
    ADD_ADVERBS.add("Addicionalment");
    CONTRAST_CONJ.add("Però");
    CONTRAST_CONJ.add("Emperò");
    CONTRAST_CONJ.add("Mes");
    CAUSE_CONJ.add("Perquè");
    CAUSE_CONJ.add("Car");
    EMPHASIS_ADVERBS.add("Òbviament");
    EMPHASIS_ADVERBS.add("Clarament");
    EMPHASIS_ADVERBS.add("Absolutament");
    EMPHASIS_ADVERBS.add("Definitivament");
    EXPLAIN_ADVERBS.add("Específicament");
    EXPLAIN_ADVERBS.add("Concretament");
    EXPLAIN_ADVERBS.add("Particularment");
    EXPLAIN_ADVERBS.add("Precisament");
    PERSONAL_PRONOUNS.add("jo");
    PERSONAL_PRONOUNS.add("tu");
    PERSONAL_PRONOUNS.add("ell");
    PERSONAL_PRONOUNS.add("ella");
    PERSONAL_PRONOUNS.add("nosaltres");
    PERSONAL_PRONOUNS.add("vosaltres");
    PERSONAL_PRONOUNS.add("ells");
    PERSONAL_PRONOUNS.add("elles");
    PERSONAL_PRONOUNS.add("vostè");
    PERSONAL_PRONOUNS.add("vostès");
    PERSONAL_PRONOUNS.add("vosté");
    PERSONAL_PRONOUNS.add("vostés");
    PERSONAL_PRONOUNS.add("vós");
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
        || EXPLAIN_ADVERBS.contains(tok) || CAUSE_CONJ.contains(tok);
  }

  @Override
  protected List<String> getSuggestions(AnalyzedTokenReadings token) {
    String tok = token.getToken();
    String lowerTok = tok.toLowerCase();
    // the repeated word is a personal pronoun
    if (PERSONAL_PRONOUNS.contains(lowerTok)) {
      return Arrays.asList("A més a més, " + lowerTok, "Igualment, " + lowerTok, "No sols aixó, sinó que " + lowerTok);
    } else if (ADD_ADVERBS.contains(tok)) {
      List<String> addSuggestions = getDifferentAdverbsOfSameCategory(tok, ADD_ADVERBS);
      addSuggestions.addAll(ADD_EXPRESSIONS);
      return addSuggestions;
    } else if (CONTRAST_CONJ.contains(tok)) {
      List<String> contrastSuggestions = new ArrayList<>(); // getDifferentAdverbsOfSameCategory(tok, CONTRAST_CONJ);
      contrastSuggestions.addAll(CONTRAST_EXPRESSIONS);
      return contrastSuggestions;
    } else if (EMPHASIS_ADVERBS.contains(tok)) {
      return getDifferentAdverbsOfSameCategory(tok, EMPHASIS_ADVERBS);
    } else if (EXPLAIN_ADVERBS.contains(tok)) {
      return getDifferentAdverbsOfSameCategory(tok, EXPLAIN_ADVERBS);
    } else if (CAUSE_CONJ.contains(tok)) {
      List<String> causeSuggestions = getDifferentAdverbsOfSameCategory(tok, CAUSE_CONJ);
      causeSuggestions.addAll(CAUSE_EXPRESSIONS);
      return causeSuggestions;
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
