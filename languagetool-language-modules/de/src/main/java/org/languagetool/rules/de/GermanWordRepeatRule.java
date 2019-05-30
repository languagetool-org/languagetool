/* LanguageTool, a natural language style checker
 * Copyright (C) 2009 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.de;

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Example;
import org.languagetool.rules.WordRepeatRule;
import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.rules.patterns.PatternTokenBuilder;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;

/**
 * Check if a word is repeated twice, taking into account an exception 
 * for German where e.g. "..., die die ..." is often okay.
 *   
 * @author Daniel Naber
 */
public class GermanWordRepeatRule extends WordRepeatRule {

	private final Language GERMAN;
  private static final List<List<PatternToken>> ANTI_PATTERNS = Arrays.asList(
    Arrays.asList(
      new PatternTokenBuilder().csToken("Bora").build(),
      new PatternTokenBuilder().csToken("Bora").build()
    ),
    Arrays.asList(// "wie Honda und Samsung, die die Bezahlung ihrer Firmenchefs..."
      new PatternTokenBuilder().csToken(",").build(),
      new PatternTokenBuilder().csToken("der").matchInflectedForms().build(),
      new PatternTokenBuilder().csToken("der").matchInflectedForms().build()
    ),
    Arrays.asList(// "Das Haus, in das das Kind läuft."
      new PatternTokenBuilder().csToken(",").build(),
      new PatternTokenBuilder().posRegex("PRP:.+").build(),
      new PatternTokenBuilder().csToken("der").matchInflectedForms().build(),
      new PatternTokenBuilder().csToken("der").matchInflectedForms().build()
    ),
    Arrays.asList(// "Er will sein Leben leben"
      new PatternTokenBuilder().csToken("Leben").build(),
      new PatternTokenBuilder().csToken("leben").build()
    )
  );

  public GermanWordRepeatRule(ResourceBundle messages, Language language) {
    super(messages, language);
    super.setCategory(Categories.REDUNDANCY.getCategory(messages));
    addExamplePair(Example.wrong("In diesem Satz <marker>ist ist</marker> ein Wort doppelt."),
                   Example.fixed("In diesem Satz <marker>ist</marker> ein Wort doppelt."));
    this.GERMAN = language;
  }

  @Override
  public String getId() {
    return "GERMAN_WORD_REPEAT_RULE";
  }

  @Override
  public boolean ignore(AnalyzedTokenReadings[] tokens, int position) {
    // "Warum fragen Sie sie nicht selbst?"
    if (position != 2 && tokens[position - 1].getToken().equals("Sie") && tokens[position].getToken().equals("sie") ||
        tokens[position - 1].getToken().equals("sie") && tokens[position].getToken().equals("Sie")) {
      return true;
    }
    if (position > 2 && tokens[position - 1].getToken().equals("sie") && tokens[position].getToken().equals("sie")) {
      if (tokens[position - 2].hasPosTag("KON:UNT")) {
        // "Sie tut das, damit sie sie nicht fortschickt"
        return true;
      }
      if (tokens.length-1 > position
        && ((tokens[position - 2].hasPosTagStartingWith("VER:3:") && tokens[position + 1].hasPosTag("ZUS")) // "Dann warfen sie sie weg."
            || (tokens[position - 2].hasPosTagStartingWith("VER:MOD:3") && tokens[position + 1].hasPosTag("VER:INF:NON")))) {// "Dann konnte sie sie sehen."
          return true;
      }
    }
    return false;
  }

  @Override
  public List<DisambiguationPatternRule> getAntiPatterns() {
    return makeAntiPatterns(ANTI_PATTERNS, GERMAN);
  }
}
