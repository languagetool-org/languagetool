/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Example;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;

import java.util.*;
import java.util.function.Supplier;

import static java.util.Arrays.*;
import static org.languagetool.rules.patterns.PatternRuleBuilderHelper.*;

/**
 * Simple agreement checker for German noun phrases. Checks agreement in:
 *
 * <ul>
 *  <li>SENT_START ADJ NOUN: e.g. "Wirtschaftlicher Wachstum" (incorrect), "Wirtschaftliches Wachstum" (correct)</li>
 * </ul>
 *
 * @author Daniel Naber
 */
public class AgreementRule2 extends Rule {

  private static final List<List<PatternToken>> ANTI_PATTERNS = asList(
    asList(csRegex("Gelegentlich|Antizyklisch|Unbedingt|Zusätzlich|Natürlich|Äußerlich|Erfolgreich|Spät|Länger|Vorrangig|Rechtzeitig|Typisch|Wöchentlich|Inhaltlich|Täglich|Komplett|Genau|Gerade|Bewusst|Vereinzelt|Gänzlich|Ständig|Okay|Meist|Generell|Ausreichend|Genügend|Reichlich|Regelmäßig(e|es)?|Unregelmäßig|Hauptsächlich"), posRegex("SUB:.*")),  // "Regelmäßig Kiwis und Ananas zu essen...", "Reichlich Inspiration bietet..."
    asList(csRegex("Überraschend"), posRegex("SUB:.*"), posRegex("VER:.*")),  // "Überraschend Besuch bekommt er dann von ..."
    asList(regex("Nachhaltig"), posRegex("SUB:NOM:.*"), pos("VER:INF:SFT")),  // 'nachhaltig Yoga praktizieren'
    asList(regex("\\d0er"), regex("Jahren?")),
    asList(token("Schwäbisch"), token("Hall")),
    asList(regex("echt|absolut|voll|total"), regex("Wahnsinn|Klasse")),
    asList(pos("SENT_START"), pos("ADJ:PRD:GRU"), posRegex("SUB:NOM:SIN:NEU:INF")),  // "Ruhig Schlafen & Zentral Wohnen"
    asList(tokenRegex("voll|voller"), posRegex("SUB:NOM:SIN:.*")),  // "Voller Mitleid", "Voller Mitleid"
    asList(token("einzig"), posRegex("SUB:NOM:.*")),  // "Einzig Fernschüsse brachten Erfolgsaussichten"
    asList(token("Smart"), token("Updates")),
    asList(token("Intelligent"), token("Design")),
    asList(token("Alternativ"), token("Berufserfahrung")),  // "Alternativ Berufserfahrung im Bereich ..."
    asList(token("Maritim"), token("Hotel")),
    asList(csToken("Russisch"), csToken("Brot")),
    asList(token("ruhig"), csToken("Blut")),
    asList(token("Blind"), regex("Dates?")),
    asList(token("Fair"), token("Trade")),
    asList(token("Frei"), token("Haus")),
    asList(token("Global"), token("Player")),
    asList(token("psychisch"), regex("Kranken?")),
    asList(token("sportlich"), regex("Aktiven?")),
    asList(token("politisch"), regex("Interessierten?")),
    asList(token("voraussichtlich"), regex("Ende|Anfang")),
    asList(regex("gesetzlich|privat"), regex("Versicherten?")),
    asList(token("typisch"), posRegex("SUB:.*"), regex("[!?.]")),  // "Typisch November!"
    asList(token("lecker"), token("Essen")),  // "Lecker Essen an Weihnachten."
    asList(token("erneut"), posRegex("SUB:.*")),  // "Erneut Ausgangssperre beschlossen"
    asList(token("Gesetzlich"), regex("Krankenversicherten?")),
    asList(token("weitgehend"), token("Einigkeit")),      // feste Phrase
    asList(token("Ernst")),      // Vorname
    asList(token("Anders")),     // Vorname
    asList(token("wirklich")),   // "Wirklich Frieden herrscht aber noch nicht"
    asList(token("gemeinsam")),   // "Gemeinsam Sportler anfeuern"
    asList(token("wenig")),      // "Wenig Geld - ..."
    asList(token("weniger")),      // "Weniger Geld - ..."
    asList(token("richtig")),    // "Richtig Kaffee kochen ..."
    asList(token("weiß")),       // "Weiß Papa, dass ..."
    asList(token("speziell")),   // "Speziell Flugfähigkeit hat sich unabhängig voneinander ..."
    asList(token("halb")),       // "Halb Traum, halb Wirklichkeit"
    asList(token("hinter")),     // "Hinter Bäumen"
    asList(token("vermutlich")), // "Vermutlich Ende 1813 erkrankte..."
    asList(token("eventuell")), // "Eventuell Ende 1813 erkrankte..."
    asList(token("ausschließlich")),
    asList(token("ausschliesslich")),
    asList(token("bloß")),       // "Bloß Anhängerkupplung und solche Dinge..."
    asList(token("einfach")),    // "Einfach Bescheid sagen ..."
    asList(token("egal")),
    asList(token("endlich")),    // "Endlich Mittagspause!"
    asList(token("unbemerkt")),    // "Unbemerkt Süßigkeiten essen"
    asList(token("Typisch"), tokenRegex("Mann|Frau")),    // "Einfach Bescheid sagen ..."
    asList(token("Ausreichend"), tokenRegex("Bewegung")),    // "Ausreichend Bewegung ..."
    asList(token("Genau"), token("Null")),
    asList(token("wohl")),       // "Wohl Anfang 1725 begegnete Bach ..."
    asList(token("erst")),       // "Erst X, dann ..."
    asList(token("lieber")),     // "Lieber X als Y"
    asList(token("besser")),     // "Besser Brot ohne Butter..."
    asList(token("laut")),       // "Laut Fernsehen"
    asList(token("research")),   // engl.
    asList(token("researchs")),  // engl.
    asList(token("security")),   // engl.
    asList(token("business")),   // oft engl.
    asList(token("voll"), token("Sorge")),
    asList(token("Total"), tokenRegex("Tankstellen?")),
    asList(token("Ganz"), token("Gentleman")),
    asList(token("Golden"), token("Gate")),
    asList(token("Wirtschaftlich"), tokenRegex("Berechtigte[rn]?")),
    asList(token("Russisch"), token("Roulette")),
    asList(token("Clever"), tokenRegex("Shuttles?")), // name
    asList(token("Personal"), tokenRegex("(Computer|Coach|Trainer|Brand).*")),
    asList(tokenRegex("Digital|Regional|Global|Bilingual|International|National|Visual|Final|Rapid|Dual|Golden"), tokenRegex("(Initiative|Connection|Bootcamp|Leadership|Sales|Community|Service|Management|Board|Identity|City|Paper|Transfer|Transformation|Power|Shopping|Brand|Master|Gate|Drive).*")),
    asList(token("Smart"), tokenRegex("(Service|Home|Meter|City|Hall|Shopper|Shopping).*")),
    asList(token("International"), tokenRegex("Society|Olympic|Space")),
    asList(token("GmbH"))
  );
  private final Supplier<List<DisambiguationPatternRule>> antiPatterns;

  public AgreementRule2(ResourceBundle messages, Language language) {
    super.setCategory(Categories.GRAMMAR.getCategory(messages));
    addExamplePair(Example.wrong("<marker>Kleiner Haus</marker> am Waldrand"),
                   Example.fixed("<marker>Kleines Haus</marker> am Waldrand"));
    antiPatterns = cacheAntiPatterns(language, ANTI_PATTERNS);
  }

  @Override
  public String getId() {
    return "DE_AGREEMENT2";
  }

  @Override
  public String getDescription() {
    return "Kongruenz von Adjektiv und Nomen (unvollständig!), z.B. 'kleiner(kleines) Haus'";
  }

  @Override
  public int estimateContextForSureMatch() {
    return ANTI_PATTERNS.stream().mapToInt(List::size).max().orElse(0);
  }

  @Override
  public List<DisambiguationPatternRule> getAntiPatterns() {
    return antiPatterns.get();
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = getSentenceWithImmunization(sentence).getTokensWithoutWhitespace();
    for (int i = 0; i < tokens.length; i++) {
      String token = tokens[i].getToken();
      if (!tokens[i].isSentenceStart() && !StringUtils.equalsAny(token, "\"", "„", "»", "«")) {
        // skip quotes, as these are not relevant
        if (i+ 1 < tokens.length && tokens[i].hasPosTagStartingWith("ADJ") && tokens[i+1].hasPosTagStartingWith("SUB") && !tokens[i+1].hasPosTagStartingWith("EIG")) {
          if (tokens[i].isImmunized() || tokens[i+1].isImmunized() || tokens[i].getToken().equalsIgnoreCase("unter")) {
            continue;
          }
          AnalyzedTokenReadings nextToken = i + 2 < tokens.length ? tokens[i + 2] : null;
          if (nextToken != null && nextToken.hasPosTagStartingWith("SUB")) {
            // no alarm for e.g. "Deutscher Taschenbuch Verlag"
            break;
          }
          RuleMatch ruleMatch = checkAdjNounAgreement(tokens[i], tokens[i+1], sentence);
          if (ruleMatch != null) {
            ruleMatches.add(ruleMatch);
            break;
          }
        } else {
          // rule only works at sentence start (minus quotes)
          break;
        }
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

  private RuleMatch checkAdjNounAgreement(AnalyzedTokenReadings token1, AnalyzedTokenReadings token2, AnalyzedSentence sentence) {
    Set<String> set = retainCommonCategories(token1, token2);
    RuleMatch ruleMatch = null;
    if (set.isEmpty()) {
      String msg = "Möglicherweise fehlende grammatikalische Übereinstimmung zwischen Adjektiv und " +
            "Nomen bezüglich Kasus, Numerus oder Genus. Beispiel: 'kleiner Haus' statt 'kleines Haus'";
      String shortMsg = "Möglicherweise keine Übereinstimmung bezüglich Kasus, Numerus oder Genus";
      ruleMatch = new RuleMatch(this, sentence, token1.getStartPos(), token2.getEndPos(), msg, shortMsg);
    }
    return ruleMatch;
  }

  @NotNull
  private Set<String> retainCommonCategories(AnalyzedTokenReadings token1, AnalyzedTokenReadings token2) {
    Set<AgreementRule.GrammarCategory> categoryToRelaxSet = Collections.emptySet();
    Set<String> set1 = AgreementTools.getAgreementCategories(token1, categoryToRelaxSet, false);
    Set<String> set2 = AgreementTools.getAgreementCategories(token2, categoryToRelaxSet, false);
    set1.retainAll(set2);
    return set1;
  }

}
