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

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Example;
import org.languagetool.rules.WordRepeatRule;
import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.rules.patterns.PatternTokenBuilder;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Supplier;

import static org.languagetool.rules.patterns.PatternRuleBuilderHelper.*;

/**
 * Check if a word is repeated twice, taking into account an exception
 * for German where e.g. "..., die die ..." is often okay.
 *
 * @author Daniel Naber
 */
public class GermanWordRepeatRule extends WordRepeatRule {

  private static final List<List<PatternToken>> ANTI_PATTERNS = Arrays.asList(
    Arrays.asList(
      csToken("Bora"),
      csToken("Bora")
    ),
    Arrays.asList(
      csToken("Miu"),
      csToken("Miu")
    ),
    Arrays.asList(
      token("Moin"),
      token("Moin")
    ),
    Arrays.asList(
      token("Na"),
      token("na")
    ),
    Arrays.asList( // La La Land / la la la ...
      token("la"),
      token("la")
    ),
    Arrays.asList( // "Bei Fragen fragen"
      csToken("Fragen"),
      csToken("fragen")
    ),
    Arrays.asList(
      token("ha"),
      token("ha")
    ),
    Arrays.asList(
      token("teils"),
      token("teils")
    ),
    Arrays.asList(
      token("Marsch"),
      token("Marsch")
    ),
    Arrays.asList(
      token("hip"),
      token("hip"),
      token("hurra")
    ),
    Arrays.asList(
      token("möp"),
      token("möp")
    ),
    Arrays.asList(
      token("piep"),
      token("piep")
    ),
    Arrays.asList(
      token("bla"),
      token("bla")
    ),
    Arrays.asList(
      token("blah"),
      token("blah")
    ),
    Arrays.asList(
      token("oh"),
      token("oh")
    ),
    Arrays.asList(
      token("klopf"),
      token("klopf")
    ),
    Arrays.asList(
      token("ne"),
      token("ne")
    ),
    Arrays.asList(
      token("Fakten"),
      token("Fakten"),
      token("Fakten")
    ),
    Arrays.asList(
      token("Top"),
      token("Top"),
      token("Top")
    ),
    Arrays.asList(
      token("Toi"),
      token("Toi"),
      token("Toi")
    ),
    Arrays.asList(
      token("und"),
      token("und"),
      token("und")
    ),
    Arrays.asList(
      token("man"),
      token("man"),
      token("man")
    ),
    Arrays.asList(
      token("Arbeit"),
      token("Arbeit"),
      token("Arbeit")
    ),
    Arrays.asList( // Art Direktor*in in der ...
      tokenRegex("\\*|:|\\/"),
      token("in"),
      token("in")
    ),
    Arrays.asList(
      token("Üben"),
      token("Üben"),
      token("Üben")
    ),
    Arrays.asList(
      token("cha"),
      token("cha")
    ),
    Arrays.asList(
      token("zack"),
      token("zack")
    ),
    Arrays.asList(
      token("sapiens"),
      token("sapiens")
    ),
    Arrays.asList(
      token("peng"),
      token("peng")
    ),
    Arrays.asList(
      token("bye"),
      token("bye")
    ),
    Arrays.asList( // Man kann nicht nicht kommunizieren
      token("nicht"),
      token("nicht"),
      token("kommunizieren")
    ),
    Arrays.asList( // Phi Phi Islands
      token("Phi"),
      token("Phi")
    ),
    Arrays.asList( // Ich weiß, wer wer ist!
      tokenRegex(",|wei(ß|ss)|nicht"),
      token("wer"),
      token("wer"),
      tokenRegex("war|ist|sein")
    ),
    Arrays.asList( // Wahrscheinlich ist das das Problem.
      tokenRegex("ist|war|wäre?|für|dass"),
      token("das"),
      token("das"),
      posRegex(".*SUB:.*NEU.*")
    ),
    Arrays.asList( // Wahrscheinlich ist das das Problem.
      tokenRegex("ist|war|wäre?|für|dass"),
      token("das"),
      token("das"),
      posRegex("ADJ:.*"),
      posRegex(".*SUB:.*NEU.*")
    ),
    Arrays.asList( // Wahrscheinlich ist das das Problem.
      tokenRegex("ist|war|wäre?|für|dass"),
      token("das"),
      token("das"),
      posRegex("ADJ:.*NEU.*"),
      posRegex("UNKNOWN")
    ),
    Arrays.asList( // Als wir das das erste Mal
      tokenRegex("als|wenn"),
      posRegex("(PRO|EIG):.*"),
      token("das"),
      token("das"),
      posRegex("ADJ:.*NEU.*"),
      posRegex(".*SUB:.*NEU.*")
    ),
    Arrays.asList( // Werden sie sie töten?
      tokenRegex("werden|würden|sollt?en|müsst?en|könnt?en"),
      token("sie"),
      token("sie"),
      posRegex("VER:1:PLU:.*")
    ),
    Arrays.asList(// "wie Honda und Samsung, die die Bezahlung ihrer Firmenchefs..."
      csToken(","),
      new PatternTokenBuilder().csToken("der").matchInflectedForms().build(),
      new PatternTokenBuilder().csToken("der").matchInflectedForms().build()
    ),
    Arrays.asList(// "Alle die die"
      tokenRegex("alle|nur|obwohl|lediglich|für|zwar|aber"),
      new PatternTokenBuilder().csToken("die").build(),
      new PatternTokenBuilder().csToken("die").build()
    ),
    Arrays.asList(// "Haben die die Elemente ..."
      posRegex("PKT|SENT_START|KON:NEB"),
      tokenRegex("haben|hatten"),
      new PatternTokenBuilder().csToken("die").build(),
      new PatternTokenBuilder().csToken("die").build(),
      posRegex(".*SUB.*PLU.*|UNKNOWN")
    ),
    Arrays.asList(// "und ob die die Währungen ..."
      posRegex("PKT|SENT_START|KON:NEB"),
      tokenRegex("ob|falls"),
      new PatternTokenBuilder().csToken("die").build(),
      new PatternTokenBuilder().csToken("die").build(),
      posRegex(".*SUB.*PLU.*|UNKNOWN")
    ),
    Arrays.asList(// "Das Haus, in das das Kind läuft."
      csToken(","),
      posRegex("PRP:.+"),
      new PatternTokenBuilder().csToken("der").matchInflectedForms().build(),
      new PatternTokenBuilder().csToken("der").matchInflectedForms().build()
    ),
    Arrays.asList(// "Er will sein Leben leben"
      csToken("Leben"),
      csToken("leben")
    ),
    Arrays.asList(// "Die markierten Stellen stellen die Aufnahmepunkte dar."
      csToken("Stellen"),
      csToken("stellen")
    ),
    Arrays.asList(// "Wir reisen in die ferne Ferne."
      token("die"),
      csToken("ferne"),
      csToken("Ferne")
    ),
    Arrays.asList(// "Er muss sein Essen essen"
      csToken("Essen"),
      csToken("essen")
    ),
    Arrays.asList(
      tokenRegex("^[_]+$"),
      tokenRegex("^[_]+$")
    )
  );
  private final Supplier<List<DisambiguationPatternRule>> antiPatterns;

  public GermanWordRepeatRule(ResourceBundle messages, Language language) {
    super(messages, language);
    super.setCategory(Categories.REDUNDANCY.getCategory(messages));
    addExamplePair(Example.wrong("In diesem Satz <marker>ist ist</marker> ein Wort doppelt."),
                   Example.fixed("In diesem Satz <marker>ist</marker> ein Wort doppelt."));
    antiPatterns = cacheAntiPatterns(language, ANTI_PATTERNS);
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
    // "Ihre verbotenen Waren waren bisher nicht aufgeflogen"
    if (position != 2 && tokens[position - 1].getToken().equals("Waren") && tokens[position].getToken().equals("waren") ||
        tokens[position - 1].getToken().equals("waren") && tokens[position].getToken().equals("Waren")) {
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

    if (tokens[position].getToken().matches("(?i)^[a-z]$") && position > 1 && tokens[position - 2].getToken().matches("(?i)^[a-z]$") && (position + 1 < tokens.length) && tokens[position + 1].getToken().matches("(?i)^[a-z]$")) {
      // spelling with spaces in between: "A B B A"
      return true;
    }

    return super.ignore(tokens, position);
  }

  @Override
  public List<DisambiguationPatternRule> getAntiPatterns() {
    return antiPatterns.get();
  }
}
