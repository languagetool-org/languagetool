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
package org.languagetool.rules.de;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.language.German;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Example;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.rules.patterns.PatternTokenBuilder;
import org.languagetool.tagging.de.AnalyzedGermanToken;
import org.languagetool.tagging.de.GermanToken;
import org.languagetool.tagging.de.GermanToken.POSType;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;

/**
 * Simple agreement checker for German noun phrases. Checks agreement in:
 * 
 * <ul>
 *  <li>DET/PRO NOUN: e.g. "mein Auto", "der Mann", "die Frau" (correct), "die Haus" (incorrect)</li>
 *  <li>DET/PRO ADJ NOUN: e.g. "der riesige Tisch" (correct), "die riesigen Tisch" (incorrect)</li> 
 * </ul>
 * 
 * Note that this rule only checks agreement inside the noun phrase, not whether
 * e.g. the correct case is used. For example, "Es ist das Haus dem Mann" is not
 * detected as incorrect.
 *
 * <p>TODO: the implementation could use a re-write that first detects the relevant noun phrases and then checks agreement
 *  
 * @author Daniel Naber
 */
public class AgreementRule extends Rule {

  private final German language;

  private enum GrammarCategory {
    KASUS("Kasus (Fall: Wer/Was, Wessen, Wem, Wen/Was - Beispiel: 'das Fahrrads' statt 'des Fahrrads')"),
    GENUS("Genus (männlich, weiblich, sächlich - Beispiel: 'der Fahrrad' statt 'das Fahrrad')"),
    NUMERUS("Numerus (Einzahl, Mehrzahl - Beispiel: 'das Fahrräder' statt 'die Fahrräder')");
    
    private final String displayName;
    GrammarCategory(String displayName) {
      this.displayName = displayName;
    }
  }
  private static final AnalyzedToken[] INS_REPLACEMENT = {new AnalyzedToken("das", "ART:DEF:AKK:SIN:NEU", "das")};
  private static final AnalyzedToken[] ZUR_REPLACEMENT = {new AnalyzedToken("der", "ART:DEF:DAT:SIN:FEM", "der")};

  private static final List<List<PatternToken>> ANTI_PATTERNS = Arrays.asList(
    Arrays.asList(  // "Dies erlaubt Forschern, ..."
      new PatternTokenBuilder().posRegex("PRO:DEM:.+").build(),
      new PatternTokenBuilder().posRegex("PA2:.+").build(),
      new PatternTokenBuilder().posRegex("SUB:.*:PLU.*").build()
    ),
    Arrays.asList(  // "Wir bereinigen das nächsten Dienstag."
      new PatternTokenBuilder().posRegex("VER:.*").build(),
      new PatternTokenBuilder().token("das").build(),
      new PatternTokenBuilder().tokenRegex("(über)?nächste[ns]?|kommende[ns]?").build(),
      new PatternTokenBuilder().tokenRegex("Montag|D(ien|onner)stag|Mittwoch|Freitag|S(ams|onn)tag|Woche|Monat|Jahr").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().token("das").build(),
      new PatternTokenBuilder().tokenRegex("Zufall|Sinn|Spaß").build(),
      new PatternTokenBuilder().token("?").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().token("in").build(),
      new PatternTokenBuilder().tokenRegex("d(ies)?em").build(),
      new PatternTokenBuilder().token("Fall").build(),
      new PatternTokenBuilder().tokenRegex("(?i:hat(te)?)").build(),
      new PatternTokenBuilder().token("das").build()
    ),
    Arrays.asList( // "So hatte das Vorteile|Auswirkungen|Konsequenzen..."
      new PatternTokenBuilder().posRegex("ADV:.+").build(),
      new PatternTokenBuilder().tokenRegex("(?i:hat(te)?)").build(),
      new PatternTokenBuilder().token("das").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().tokenRegex("von|bei").build(),
      new PatternTokenBuilder().tokenRegex("(vielen|allen)").build(),
      new PatternTokenBuilder().posRegex("PA2:.*|ADJ:AKK:PLU:.*").build()  // "ein von vielen bewundertes Haus" / "Das weckte bei vielen ungute Erinnerungen."
    ),
    Arrays.asList(
      new PatternTokenBuilder().token("für").build(),
      new PatternTokenBuilder().tokenRegex("(viele|alle|[dm]ich|ihn|sie|uns)").build(),
      new PatternTokenBuilder().posRegex("ADJ:AKK:.*").build()  // "Ein für viele wichtiges Anliegen."
    ),
    Arrays.asList(
      new PatternTokenBuilder().tokenRegex("flößen|machen|jagen").matchInflectedForms().build(),
      new PatternTokenBuilder().token("einem").build(),
      new PatternTokenBuilder().token("Angst").build()  // "Dinge, die/ Etwas, das einem Angst macht"
    ),
    Arrays.asList(
      new PatternTokenBuilder().token("einem").build(),
      new PatternTokenBuilder().token("Angst").build(),  // "Was einem Angst macht"
      new PatternTokenBuilder().tokenRegex("machen|ein(flößen|jagen)").matchInflectedForms().build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().token("einem").build(),
      new PatternTokenBuilder().token("geschenkten").build(),
      new PatternTokenBuilder().token("Gaul").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().token("kein").build(),
      new PatternTokenBuilder().token("schöner").build(),
      new PatternTokenBuilder().token("Land").build()  // https://de.wikipedia.org/wiki/Kein_sch%C3%B6ner_Land
    ),
    Arrays.asList(
      new PatternTokenBuilder().tokenRegex("die|der|das").build(),
      new PatternTokenBuilder().tokenRegex("Anfang|Ende").build(),
      new PatternTokenBuilder().tokenRegex("Januar|Jänner|Februar|März|April|Mai|Ju[ln]i|August|September|Oktober|November|Dezember|[12][0-9]{3}").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().pos(JLanguageTool.SENTENCE_START_TAGNAME).build(),
      new PatternTokenBuilder().tokenRegex("Ist|Sind|Macht|Wird").build(),
      new PatternTokenBuilder().token("das").build(),
      new PatternTokenBuilder().posRegex("SUB:.*").build(),
      new PatternTokenBuilder().posRegex("PKT|KON:NEB|ZUS").build()// "Ist das Kunst?" / "Ist das Kunst oder Abfall?" / "Sind das Eier aus Bodenhaltung"
    ),
    Arrays.asList(
      new PatternTokenBuilder().pos(JLanguageTool.SENTENCE_START_TAGNAME).build(),
      new PatternTokenBuilder().tokenRegex("Meist(ens)?|Oft(mals)?|Häufig|Selten").build(),
      new PatternTokenBuilder().tokenRegex("sind|waren|ist").build(),
      new PatternTokenBuilder().token("das").build(),
      new PatternTokenBuilder().posRegex("SUB:.*").build() // Meistens sind das Frauen, die damit besser umgehen können.
    ),
    Arrays.asList(
      new PatternTokenBuilder().token("des").build(),
      new PatternTokenBuilder().token("Lied").build(),
      new PatternTokenBuilder().token("ich").build()// Wes Brot ich ess, des Lied ich sing
    ),
    Arrays.asList(
      new PatternTokenBuilder().pos(JLanguageTool.SENTENCE_START_TAGNAME).build(),
      new PatternTokenBuilder().tokenRegex("D(a|ie)s").build(),
      new PatternTokenBuilder().posRegex("VER:[123]:.*").build(),
      new PatternTokenBuilder().posRegex("SUB:NOM:.*").build()// "Das erfordert Können und..." / "Dies bestätigte Polizeimeister Huber"
    ),
    Arrays.asList(
      new PatternTokenBuilder().posRegex("ART:.+").build(), // "Das wenige Kilometer breite Tal"
      new PatternTokenBuilder().posRegex("ADJ:.+").build(),
      new PatternTokenBuilder().tokenRegex("(Kilo|Zenti|Milli)?meter|Jahre|Monate|Wochen|Tage|Stunden|Minuten|Sekunden").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().token("Van").build(), // https://de.wikipedia.org/wiki/Alexander_Van_der_Bellen
      new PatternTokenBuilder().token("der").build(),
      new PatternTokenBuilder().tokenRegex("Bellens?").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().token("mehrere").build(), // "mehrere Verwundete" http://forum.languagetool.org/t/de-false-positives-and-false-false/1516
      new PatternTokenBuilder().pos("SUB:NOM:SIN:FEM:ADJ").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().token("allen").build(),
      new PatternTokenBuilder().tokenRegex("Besitz|Mut").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().tokenRegex("d(ie|e[nr])|[md]eine[nr]?").build(),
      new PatternTokenBuilder().token("Top").build(),
      new PatternTokenBuilder().tokenRegex("\\d+").build()
    ),
    Arrays.asList( //"Unter diesen rief das großen Unmut hervor."
      new PatternTokenBuilder().posRegex("VER:3:SIN:.*").build(),
      new PatternTokenBuilder().token("das").build(),
      new PatternTokenBuilder().posRegex("ADJ:AKK:.*").build(),
      new PatternTokenBuilder().posRegex("SUB:AKK:.*").build(),
      new PatternTokenBuilder().pos("ZUS").build(),
      new PatternTokenBuilder().pos(JLanguageTool.SENTENCE_END_TAGNAME).build()
    ),
    Arrays.asList( // "Bei mir löste das Panik aus."
      new PatternTokenBuilder().posRegex("VER:3:SIN:.+").build(),
      new PatternTokenBuilder().token("das").build(),
      new PatternTokenBuilder().posRegex("SUB:AKK:.+").build(),
      new PatternTokenBuilder().pos("ZUS").build(),
      new PatternTokenBuilder().pos(JLanguageTool.SENTENCE_END_TAGNAME).build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().token("Außenring").build(),
      new PatternTokenBuilder().token("Autobahn").build()
    ),
    Arrays.asList( // "Ehre, wem Ehre gebührt"
      new PatternTokenBuilder().tokenRegex("[dw]em").build(),
      new PatternTokenBuilder().csToken("Ehre").build(),
      new PatternTokenBuilder().csToken("gebührt").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().token("Eurovision").build(),
      new PatternTokenBuilder().token("Song").build(),
      new PatternTokenBuilder().token("Contest").build()
    ),
    Arrays.asList( // "Das Holocaust Memorial Museum."
      new PatternTokenBuilder().posRegex("ART:.+").build(),
      new PatternTokenBuilder().posRegex("SUB:.+").build(),
      new PatternTokenBuilder().pos("UNKNOWN").build()
    ),
    Arrays.asList( // "Er fragte, ob das Spaß macht."
      new PatternTokenBuilder().csToken(",").build(),
      new PatternTokenBuilder().posRegex("KON:UNT|ADV:INR").build(),
      new PatternTokenBuilder().csToken("das").build(),
      new PatternTokenBuilder().posRegex("SUB:.+").build(),
      new PatternTokenBuilder().posRegex("VER:3:SIN.*").build()
    ),
    Arrays.asList( // "Es gibt viele solcher Bilder"
      new PatternTokenBuilder().tokenRegex("viele|wenige|einige|mehrere").build(),
      new PatternTokenBuilder().csToken("solcher").build(),
      new PatternTokenBuilder().posRegex("SUB:GEN:PLU:.*").build()
    ),
    Arrays.asList( // "der französischen First Lady"
      new PatternTokenBuilder().tokenRegex("[dD](ie|er)").build(),
      new PatternTokenBuilder().csToken("First").build(),
      new PatternTokenBuilder().csToken("Lady").build()
    ),
    Arrays.asList( // "der französischen First Lady"
      new PatternTokenBuilder().tokenRegex("[dD](ie|er)").build(),
      new PatternTokenBuilder().posRegex("ADJ:.*").build(),
      new PatternTokenBuilder().csToken("First").build(),
      new PatternTokenBuilder().csToken("Lady").build()
    ),
    Arrays.asList( // Texas und New Mexico, beides spanische Kolonien, sind
      new PatternTokenBuilder().csToken(",").build(),
      new PatternTokenBuilder().csToken("beides").build(),
      new PatternTokenBuilder().posRegex("ADJ:NOM:PLU.+").build(),
      new PatternTokenBuilder().posRegex("SUB:NOM:PLU.+").build(),
      new PatternTokenBuilder().csToken(",").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().tokenRegex("[dD]e[rn]").build(),
      new PatternTokenBuilder().csToken("Gold").build(),
      new PatternTokenBuilder().csToken("Cup").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().token("das").build(),
      new PatternTokenBuilder().tokenRegex("viele|wenige").build(),
      new PatternTokenBuilder().posRegex("SUB:.*").build()
    ),
    Arrays.asList( // "Er verspricht allen/niemandem/jedem hohe Gewinne."
      new PatternTokenBuilder().tokenRegex("allen|(nieman|je(man)?)dem").build(),
      new PatternTokenBuilder().posRegex("ADJ:AKK:PLU:.*").build(),
      new PatternTokenBuilder().posRegex("SUB:AKK:PLU:.*").build()
    ),
    Arrays.asList( // "Er verspricht allen/niemandem/jedem Gewinne von über 15 Prozent."
      new PatternTokenBuilder().tokenRegex("allen|(nieman|je(man)?)dem").build(),
      new PatternTokenBuilder().posRegex("SUB:AKK:PLU:.*").build()
    ),
    Arrays.asList( // "Für ihn ist das Alltag." / "Für die Religiösen ist das Blasphemie."
    	new PatternTokenBuilder().posRegex("PRP:.+|ADV:MOD").setSkip(2).build(),
      new PatternTokenBuilder().token("sein").matchInflectedForms().build(),
      new PatternTokenBuilder().csToken("das").build(),
      new PatternTokenBuilder().posRegex("SUB:NOM:.*").build(),
      new PatternTokenBuilder().posRegex("PKT|SENT_END").build()
    ),
    Arrays.asList( // "Sie sagte, dass das Rache bedeuten würden"
      new PatternTokenBuilder().pos("KON:UNT").build(),
      new PatternTokenBuilder().csToken("das").build(),
      new PatternTokenBuilder().posRegex("SUB:.+").build(),
      new PatternTokenBuilder().tokenRegex("bedeuten|sein").matchInflectedForms().build()
    ),
    Arrays.asList( // "Sie fragte, ob das wirklich Rache bedeuten würde"
      new PatternTokenBuilder().pos("KON:UNT").build(),
      new PatternTokenBuilder().csToken("das").build(),
      new PatternTokenBuilder().pos("ADV:MOD").build(),
      new PatternTokenBuilder().posRegex("SUB:.+").build(),
      new PatternTokenBuilder().tokenRegex("bedeuten|sein").matchInflectedForms().build()
    ),
    Arrays.asList( // "Karl sagte, dass sie niemandem Bescheid gegeben habe."
      new PatternTokenBuilder().token("niemand").matchInflectedForms().build(),
      new PatternTokenBuilder().posRegex("SUB:.+").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().token("alles").build(),
      new PatternTokenBuilder().csToken("Walzer").build()
    ),
    Arrays.asList( // "ei der Daus"
      new PatternTokenBuilder().csToken("der").build(),
      new PatternTokenBuilder().csToken("Daus").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().csToken("dem").build(),
      new PatternTokenBuilder().csToken("Achtung").setSkip(1).build(),
      new PatternTokenBuilder().csToken("schenken").matchInflectedForms().build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().csToken("schenken").matchInflectedForms().build(),
      new PatternTokenBuilder().csToken("dem").build(),
      new PatternTokenBuilder().csToken("Achtung").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().csToken("dem").build(),
      new PatternTokenBuilder().csToken("Rechnung").setSkip(1).build(),
      new PatternTokenBuilder().csToken("tragen").matchInflectedForms().build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().csToken("tragen").matchInflectedForms().build(),
      new PatternTokenBuilder().csToken("dem").build(),
      new PatternTokenBuilder().csToken("Rechnung").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().csToken("zum").build(),
      new PatternTokenBuilder().csToken("einen").build(),
      new PatternTokenBuilder().posRegex("ADJ:.+").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().token("auf").build(),
      new PatternTokenBuilder().csToken("die").build(),
      new PatternTokenBuilder().csToken("Lauer").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().token("dieser").build(),
      new PatternTokenBuilder().csToken("eine").build(),
      new PatternTokenBuilder().pos("SUB:NOM:SIN:MAS").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().token("das").build(),
      new PatternTokenBuilder().posRegex("SUB:DAT:.+").build(),
      new PatternTokenBuilder().token("vorbehalten").build()
    ),
    Arrays.asList( // Wenn hier einer Geld hat, dann ich.
      new PatternTokenBuilder().token("wenn").setSkip(1).build(),
      new PatternTokenBuilder().csToken("einer").build(),
      new PatternTokenBuilder().posRegex("SUB:AKK:.+").build(),
      new PatternTokenBuilder().posRegex("VER:(MOD:)?3:SIN:.+").build(),
      new PatternTokenBuilder().csToken(",").build()
    ),
    Arrays.asList( // Es ist nicht eines jeden Bestimmung
      new PatternTokenBuilder().tokenRegex("eine[rs]").build(),
      new PatternTokenBuilder().tokenRegex("jed(wed)?en").build()
    ),
    Arrays.asList( // Ich vertraue auf die Meinen.
      new PatternTokenBuilder().token("die").build(),
      new PatternTokenBuilder().tokenRegex("[MDS]einen").build()
    ),
    Arrays.asList( // Sie ist über die Maßen schön.
      new PatternTokenBuilder().csToken("über").build(),
      new PatternTokenBuilder().csToken("die").build(),
      new PatternTokenBuilder().csToken("Maßen").build()
    ),
    Arrays.asList( // Was nützt einem Gesundheit, wenn man sonst ein Idiot ist?
      new PatternTokenBuilder().token("was").build(),
      new PatternTokenBuilder().csToken("nützen").matchInflectedForms().build(),
      new PatternTokenBuilder().csToken("einem").build(),
      new PatternTokenBuilder().posRegex("SUB:NOM:.+").build()
    ),
    Arrays.asList( // Auch das hat sein Gutes.
      new PatternTokenBuilder().csToken("haben").matchInflectedForms().build(),
      new PatternTokenBuilder().csToken("sein").build(),
      new PatternTokenBuilder().csToken("Gutes").build()
    ),
    Arrays.asList( // Auch wenn es sein Gutes hatte.
      new PatternTokenBuilder().csToken("Gutes").build(),
      new PatternTokenBuilder().tokenRegex("haben|tun").matchInflectedForms().build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().csToken("dieser").build(),
      new PatternTokenBuilder().csToken("einen").build(),
      new PatternTokenBuilder().pos("SUB:DAT:SIN:FEM").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().posRegex("ABK:.+:SUB").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().tokenRegex("(all|je(d|glich))en").build(),
      new PatternTokenBuilder().csToken("Reiz").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().posRegex("VER:.*[1-3]:.+").setSkip(1).build(),
      new PatternTokenBuilder().csToken("vermehrt").build()
    )
  );

  private static final Set<String> MODIFIERS = new HashSet<>(Arrays.asList(
      "besonders",
      "fast",
      "ganz",
      "geradezu",
      "sehr",
      "überaus",
      "ziemlich"
    ));

  private static final Set<String> VIELE_WENIGE_LOWERCASE = new HashSet<>(Arrays.asList(
    "viele",
    "vieler",
    "wenige",
    "weniger",
    "einige",
    "einiger",
    "mehrerer",
    "mehrere"
  ));
  
  private static final String[] REL_PRONOUN_LEMMAS = {"der", "welch"};
  
  private static final Set<String> PRONOUNS_TO_BE_IGNORED = new HashSet<>(Arrays.asList(
    "ich",
    "dir",
    "du",
    "er", "sie", "es",
    "wir",
    "mir",
    "uns",
    "ihnen",
    "euch",
    "ihm",
    "ihr",
    "ihn",
    "dessen",
    "deren",
    "denen",
    "sich",
    "aller",
    "man",
    "beide",
    "beiden",
    "beider",
    "wessen",
    "a",
    "alle",
    "etwas",
    "irgendetwas",
    "was",
    "wer",
    "jenen",      // "...und mit jenen anderer Arbeitsgruppen verwoben"
    "diejenigen",
    "jemand", "jemandes",
    "niemand", "niemandes"
  ));
  
  private static final Set<String> NOUNS_TO_BE_IGNORED = new HashSet<>(Arrays.asList(
    "Prozent",   // Plural "Prozente", trotzdem ist "mehrere Prozent" korrekt
    "Gramm",
    "Kilogramm",
    "Uhr"   // "um ein Uhr"
  ));
    
  public AgreementRule(ResourceBundle messages, German language) {
    this.language = language;
    super.setCategory(Categories.GRAMMAR.getCategory(messages));
    addExamplePair(Example.wrong("<marker>Der Haus</marker> wurde letztes Jahr gebaut."),
                   Example.fixed("<marker>Das Haus</marker> wurde letztes Jahr gebaut."));
  }
  
  @Override
  public String getId() {
    return "DE_AGREEMENT";
  }

  @Override
  public int estimateContextForSureMatch() {
    return ANTI_PATTERNS.stream().mapToInt(List::size).max().orElse(0); 
  }

  @Override
  public String getDescription() {
    return "Kongruenz von Nominalphrasen (unvollständig!), z.B. 'mein kleiner(kleines) Haus'";
  }

  private void replacePrepositionsByArticle (AnalyzedTokenReadings[] tokens) {
  	for (int i = 0; i < tokens.length; i++) {
      if (StringUtils.equalsAny(tokens[i].getToken(), "ins", "ans", "aufs", "vors", "durchs", "hinters", "unters", "übers", "fürs", "ums")) {
  			tokens[i] = new AnalyzedTokenReadings(INS_REPLACEMENT, tokens[i].getStartPos());
  		} else if (StringUtils.equalsAny(tokens[i].getToken(), "zur")) {
  			tokens[i] = new AnalyzedTokenReadings(ZUR_REPLACEMENT, tokens[i].getStartPos());
  		}
  	}
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = getSentenceWithImmunization(sentence).getTokensWithoutWhitespace();
    replacePrepositionsByArticle(tokens);
    for (int i = 0; i < tokens.length; i++) {
      //defaulting to the first reading
      //TODO: check for all readings
      String posToken = tokens[i].getAnalyzedToken(0).getPOSTag();
      if (JLanguageTool.SENTENCE_START_TAGNAME.equals(posToken) || tokens[i].isImmunized()) {
        continue;
      }

      AnalyzedTokenReadings tokenReadings = tokens[i];
      boolean relevantPronoun = isRelevantPronoun(tokens, i);
     
      boolean ignore = couldBeRelativeOrDependentClause(tokens, i);
      if (i > 0) {
        String prevToken = tokens[i-1].getToken().toLowerCase();
        if (StringUtils.equalsAny(tokens[i].getToken(), "eine", "einen")
            && StringUtils.equalsAny(prevToken, "der", "die", "das", "des", "dieses")) {
          // TODO: "der eine Polizist" -> nicht ignorieren, sondern "der polizist" checken; "auf der einen Seite"
          ignore = true;
        }
      }
      
      // avoid false alarm on "nichts Gutes" and "alles Gute"
      if (StringUtils.equalsAny(tokenReadings.getToken(), "nichts", "alles", "dies")) {
        ignore = true;
      }

      // avoid false alarm on "Art. 1" and "bisherigen Art. 1" (Art. = Artikel):
      boolean detAbbrev = i < tokens.length-2 && tokens[i+1].getToken().equals("Art") && tokens[i+2].getToken().equals(".");
      boolean detAdjAbbrev = i < tokens.length-3 && tokens[i+2].getToken().equals("Art") && tokens[i+3].getToken().equals(".");
      // "einen Hochwasser führenden Fluss", "die Gott zugeschriebenen Eigenschaften":
      boolean followingParticiple = i < tokens.length-3 && (tokens[i+2].hasPartialPosTag("PA1") || tokens[i+2].getToken().matches("zugeschriebenen?|genannten?"));
      if (detAbbrev || detAdjAbbrev || followingParticiple) {
        ignore = true;
      }

      if ((GermanHelper.hasReadingOfType(tokenReadings, POSType.DETERMINER) || relevantPronoun) && !ignore) {
        int tokenPosAfterModifier = getPosAfterModifier(i+1, tokens);
        int tokenPos = tokenPosAfterModifier;
        if (tokenPos >= tokens.length) {
          break;
        }
        AnalyzedTokenReadings nextToken = tokens[tokenPos];
        if (isNonPredicativeAdjective(nextToken) || isParticiple(nextToken)) {
          tokenPos = tokenPosAfterModifier + 1;
          if (tokenPos >= tokens.length) {
            break;
          }
          if (GermanHelper.hasReadingOfType(tokens[tokenPos], POSType.NOMEN)) {
            // TODO: add a case (checkAdjNounAgreement) for special cases like "deren",
            // e.g. "deren komisches Geschenke" isn't yet detected as incorrect
            if (i >= 2 && GermanHelper.hasReadingOfType(tokens[i-2], POSType.ADJEKTIV)
                       && "als".equals(tokens[i-1].getToken())
                       && "das".equals(tokens[i].getToken())) {
              // avoid false alarm for e.g. "weniger farbenprächtig als das anderer Papageien"
              continue;
            }
            RuleMatch ruleMatch = checkDetAdjNounAgreement(tokens[i],
                nextToken, tokens[tokenPos], sentence);
            if (ruleMatch != null) {
              ruleMatches.add(ruleMatch);
            }
          }
        } else if (GermanHelper.hasReadingOfType(nextToken, POSType.NOMEN) && !"Herr".equals(nextToken.getToken())) {
          RuleMatch ruleMatch = checkDetNounAgreement(tokens[i], nextToken, sentence);
          if (ruleMatch != null) {
            ruleMatches.add(ruleMatch);
          }
        }
      }
    } // for each token
    return toRuleMatchArray(ruleMatches);
  }

  /**
   * Search for modifiers (such as "sehr", "1,4 Meter") which can expand a
   * determiner - adjective - noun group ("ein hohes Haus" -> "ein sehr hohes Haus",
   * "ein 500 Meter hohes Haus") and return the index of the first non-modifier token ("Haus")
   * @param startAt index of array where to start searching for modifier
   * @return index of first non-modifier token
   */
  private int getPosAfterModifier(int startAt, AnalyzedTokenReadings[] tokens) {
    if ((startAt + 1) < tokens.length && MODIFIERS.contains(tokens[startAt].getToken())) {
      startAt++;
    }
    if ((startAt + 1) < tokens.length && (StringUtils.isNumeric(tokens[startAt].getToken()) || tokens[startAt].hasPosTag("ZAL"))) {
      int posAfterModifier = startAt + 1;
      if ((startAt + 3) < tokens.length && ",".equals(tokens[startAt+1].getToken()) && StringUtils.isNumeric(tokens[startAt+2].getToken())) {
        posAfterModifier = startAt + 3;
      }
      if (StringUtils.endsWithAny(tokens[posAfterModifier].getToken(), "gramm", "Gramm", "Meter", "meter")) {
        return posAfterModifier + 1;
      }
    }
    return startAt;
  }

  @Override
  public List<DisambiguationPatternRule> getAntiPatterns() {
    return makeAntiPatterns(ANTI_PATTERNS, language);
  }

  private boolean isNonPredicativeAdjective(AnalyzedTokenReadings tokensReadings) {
    for (AnalyzedToken reading : tokensReadings.getReadings()) {
      String posTag = reading.getPOSTag();
      if (posTag != null && posTag.startsWith("ADJ") && !posTag.contains("PRD")) {
        return true;
      }
    }
    return false;
  }

  private boolean isParticiple(AnalyzedTokenReadings tokensReadings) {
    return tokensReadings.hasPartialPosTag("PA1") || tokensReadings.hasPartialPosTag("PA2");
  }

  private boolean isRelevantPronoun(AnalyzedTokenReadings[] tokens, int pos) {
    AnalyzedTokenReadings analyzedToken = tokens[pos];
    boolean relevantPronoun = GermanHelper.hasReadingOfType(analyzedToken, POSType.PRONOMEN);
    // avoid false alarms:
    String token = tokens[pos].getToken();
    if (PRONOUNS_TO_BE_IGNORED.contains(token.toLowerCase()) ||
        (pos > 0 && tokens[pos-1].getToken().equalsIgnoreCase("vor") && token.equalsIgnoreCase("allem"))) {
      relevantPronoun = false;
    }
    return relevantPronoun;
  }

  // TODO: improve this so it only returns true for real relative clauses
  private boolean couldBeRelativeOrDependentClause(AnalyzedTokenReadings[] tokens, int pos) {
    boolean comma;
    boolean relPronoun;
    if (pos >= 1) {
      // avoid false alarm: "Das Wahlrecht, das Frauen zugesprochen bekamen." etc:
      comma = tokens[pos-1].getToken().equals(",");
      relPronoun = comma && tokens[pos].hasAnyLemma(REL_PRONOUN_LEMMAS);
      if (relPronoun && pos+3 < tokens.length) {
        return true;
      }
    }
    if (pos >= 2) {
      // avoid false alarm: "Der Mann, in dem quadratische Fische schwammen."
      // or: "Die Polizei erwischte die Diebin, weil diese Ausweis und Visitenkarte hinterließ."
      comma = tokens[pos-2].getToken().equals(",");
      if(comma) {
        boolean prep = tokens[pos-1].hasPosTagStartingWith("PRP:");
        relPronoun = tokens[pos].hasAnyLemma(REL_PRONOUN_LEMMAS);
        return prep && relPronoun || (tokens[pos-1].hasPosTag("KON:UNT") && (tokens[pos].hasLemma("jen") || tokens[pos].hasLemma("dies")));
      }
    }
    return false;
  }

  @Nullable
  private RuleMatch checkDetNounAgreement(AnalyzedTokenReadings token1,
      AnalyzedTokenReadings token2, AnalyzedSentence sentence) {
    // TODO: remove "-".equals(token2.getToken()) after the bug fix 
    // see Daniel's comment from 20.12.2016 at https://github.com/languagetool-org/languagetool/issues/635
    if (token2.isImmunized() || NOUNS_TO_BE_IGNORED.contains(token2.getToken()) || "-".equals(token2.getToken())) {
      return null;
    }

    Set<String> set1 = null;
    if (token1.getReadings().size() == 1 &&
        token1.getReadings().get(0).getPOSTag() != null &&
        token1.getReadings().get(0).getPOSTag().endsWith(":STV")) {
      // catch the error in "Meiner Chef raucht."
      set1 = Collections.emptySet(); 
    } else {
      set1 = getAgreementCategories(token1);
    }
    if (set1 == null) {
      return null;  // word not known, assume it's correct
    }
    Set<String> set2 = getAgreementCategories(token2);
    if (set2 == null) {
      return null;
    }
    set1.retainAll(set2);
    RuleMatch ruleMatch = null;
    if (set1.isEmpty() && !isException(token1, token2)) {
      List<String> errorCategories = getCategoriesCausingError(token1, token2);
      String errorDetails = errorCategories.isEmpty() ?
            "Kasus, Genus oder Numerus" : String.join(" und ", errorCategories);
      String msg = "Möglicherweise fehlende grammatische Übereinstimmung zwischen Artikel und Nomen " +
            "bezüglich " + errorDetails + ".";
      String shortMsg = "Möglicherweise keine Übereinstimmung bezüglich " + errorDetails;
      ruleMatch = new RuleMatch(this, sentence, token1.getStartPos(),
              token2.getEndPos(), msg, shortMsg);
      /*try {
        // this will not give a match for compounds that are not in the dictionary...
        ruleMatch.setUrl(new URL("https://www.korrekturen.de/flexion/deklination/" + token2.getToken() + "/"));
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }*/
      AgreementSuggestor suggestor = new AgreementSuggestor(language.getSynthesizer(), token1, token2);
      List<String> suggestions = suggestor.getSuggestions();
      ruleMatch.setSuggestedReplacements(suggestions);
    }
    return ruleMatch;
  }

  private boolean isException(AnalyzedTokenReadings token1, AnalyzedTokenReadings token2) {
    return "allen".equals(token1.getToken()) && "Grund".equals(token2.getToken());
  }

  private List<String> getCategoriesCausingError(AnalyzedTokenReadings token1, AnalyzedTokenReadings token2) {
    List<String> categories = new ArrayList<>();
    List<GrammarCategory> categoriesToCheck = Arrays.asList(GrammarCategory.KASUS, GrammarCategory.GENUS, GrammarCategory.NUMERUS);
    for (GrammarCategory category : categoriesToCheck) {
      if (agreementWithCategoryRelaxation(token1, token2, category)) {
        categories.add(category.displayName);
      }
    }
    return categories;
  }

  private RuleMatch checkDetAdjNounAgreement(AnalyzedTokenReadings token1,
      AnalyzedTokenReadings token2, AnalyzedTokenReadings token3, AnalyzedSentence sentence) {
    // TODO: remove (token3 == null || token3.getToken().length() < 2) 
    // see Daniel's comment from 20.12.2016 at https://github.com/languagetool-org/languagetool/issues/635
    if(token3 == null || token3.getToken().length() < 2) {
      return null;
    }
    Set<String> set = retainCommonCategories(token1, token2, token3);
    RuleMatch ruleMatch = null;
    if (set.isEmpty()) {
      // TODO: more detailed error message:
      String msg = "Möglicherweise fehlende grammatische Übereinstimmung zwischen Artikel, Adjektiv und " +
            "Nomen bezüglich Kasus, Numerus oder Genus. Beispiel: 'mein kleiner Haus' " +
            "statt 'mein kleines Haus'";
      String shortMsg = "Möglicherweise keine Übereinstimmung bezüglich Kasus, Numerus oder Genus";
      ruleMatch = new RuleMatch(this, sentence, token1.getStartPos(), token3.getEndPos(), msg, shortMsg);
    }
    return ruleMatch;
  }

  private boolean agreementWithCategoryRelaxation(AnalyzedTokenReadings token1,
                                                  AnalyzedTokenReadings token2, GrammarCategory categoryToRelax) {
    Set<GrammarCategory> categoryToRelaxSet;
    if (categoryToRelax != null) {
      categoryToRelaxSet = Collections.singleton(categoryToRelax);
    } else {
      categoryToRelaxSet = Collections.emptySet();
    }
    Set<String> set1 = getAgreementCategories(token1, categoryToRelaxSet, true);
    if (set1 == null) {
      return true;  // word not known, assume it's correct
    }
    Set<String> set2 = getAgreementCategories(token2, categoryToRelaxSet, true);
    if (set2 == null) {
      return true;      
    }
    set1.retainAll(set2);
    return set1.size() > 0;
  }

  @NotNull
  private Set<String> retainCommonCategories(AnalyzedTokenReadings token1,
                                             AnalyzedTokenReadings token2, AnalyzedTokenReadings token3) {
    Set<GrammarCategory> categoryToRelaxSet = Collections.emptySet();
    Set<String> set1 = getAgreementCategories(token1, categoryToRelaxSet, true);
    if (set1 == null) {
      return Collections.emptySet();  // word not known, assume it's correct
    }
    boolean skipSol = !VIELE_WENIGE_LOWERCASE.contains(token1.getToken().toLowerCase());
    Set<String> set2 = getAgreementCategories(token2, categoryToRelaxSet, skipSol);
    if (set2 == null) {
      return Collections.emptySet();
    }
    Set<String> set3 = getAgreementCategories(token3, categoryToRelaxSet, true);
    if (set3 == null) {
      return Collections.emptySet();
    }
    set1.retainAll(set2);
    set1.retainAll(set3);
    return set1;
  }

  private Set<String> getAgreementCategories(AnalyzedTokenReadings aToken) {
    return getAgreementCategories(aToken, new HashSet<>(), false);
  }
  
  /** Return Kasus, Numerus, Genus of those forms with a determiner. */
  private Set<String> getAgreementCategories(AnalyzedTokenReadings aToken, Set<GrammarCategory> omit, boolean skipSol) {
    Set<String> set = new HashSet<>();
    List<AnalyzedToken> readings = aToken.getReadings();
    for (AnalyzedToken tmpReading : readings) {
      if (skipSol && tmpReading.getPOSTag() != null && tmpReading.getPOSTag().endsWith(":SOL")) {
        // SOL = alleinstehend - needs to be skipped so we find errors like "An der roter Ampel."
        continue;
      }
      AnalyzedGermanToken reading = new AnalyzedGermanToken(tmpReading);
      if (reading.getCasus() == null && reading.getNumerus() == null &&
          reading.getGenus() == null) {
        continue;
      }
      if (reading.getGenus() == GermanToken.Genus.ALLGEMEIN && 
          tmpReading.getPOSTag() != null && !tmpReading.getPOSTag().endsWith(":STV") &&  // STV: stellvertretend (!= begleitend)
          !possessiveSpecialCase(aToken, tmpReading)) {   
        // genus=ALG in the original data. Not sure if this is allowed, but expand this so
        // e.g. "Ich Arbeiter" doesn't get flagged as incorrect:
        if (reading.getDetermination() == null) {
          // Nouns don't have the determination property (definite/indefinite), and as we don't want to
          // introduce a special case for that, we just pretend they always fulfill both properties:
          set.add(makeString(reading.getCasus(), reading.getNumerus(), GermanToken.Genus.MASKULINUM, GermanToken.Determination.DEFINITE, omit));
          set.add(makeString(reading.getCasus(), reading.getNumerus(), GermanToken.Genus.MASKULINUM, GermanToken.Determination.INDEFINITE, omit));
          set.add(makeString(reading.getCasus(), reading.getNumerus(), GermanToken.Genus.FEMININUM, GermanToken.Determination.DEFINITE, omit));
          set.add(makeString(reading.getCasus(), reading.getNumerus(), GermanToken.Genus.FEMININUM, GermanToken.Determination.INDEFINITE, omit));
          set.add(makeString(reading.getCasus(), reading.getNumerus(), GermanToken.Genus.NEUTRUM, GermanToken.Determination.DEFINITE, omit));
          set.add(makeString(reading.getCasus(), reading.getNumerus(), GermanToken.Genus.NEUTRUM, GermanToken.Determination.INDEFINITE, omit));
        } else {
          set.add(makeString(reading.getCasus(), reading.getNumerus(), GermanToken.Genus.MASKULINUM, reading.getDetermination(), omit));
          set.add(makeString(reading.getCasus(), reading.getNumerus(), GermanToken.Genus.FEMININUM, reading.getDetermination(), omit));
          set.add(makeString(reading.getCasus(), reading.getNumerus(), GermanToken.Genus.NEUTRUM, reading.getDetermination(), omit));
        }
      } else {
        if (reading.getDetermination() == null || "jed".equals(tmpReading.getLemma()) || "manch".equals(tmpReading.getLemma())) {  // "jeder" etc. needs a special case to avoid false alarm
          set.add(makeString(reading.getCasus(), reading.getNumerus(), reading.getGenus(), GermanToken.Determination.DEFINITE, omit));
          set.add(makeString(reading.getCasus(), reading.getNumerus(), reading.getGenus(), GermanToken.Determination.INDEFINITE, omit));
        } else {
          set.add(makeString(reading.getCasus(), reading.getNumerus(), reading.getGenus(), reading.getDetermination(), omit));
        }
      }
    }
    return set;
  }

  private boolean possessiveSpecialCase(AnalyzedTokenReadings aToken, AnalyzedToken tmpReading) {
    // would cause error misses as it contains 'ALG', e.g. in "Der Zustand meiner Gehirns."
    return aToken.hasPosTagStartingWith("PRO:POS") && StringUtils.equalsAny(tmpReading.getLemma(), "ich", "sich");
  }

  private String makeString(GermanToken.Kasus casus, GermanToken.Numerus num, GermanToken.Genus gen,
      GermanToken.Determination determination, Set<GrammarCategory> omit) {
    List<String> l = new ArrayList<>();
    if (casus != null && !omit.contains(GrammarCategory.KASUS)) {
      l.add(casus.toString());
    }
    if (num != null && !omit.contains(GrammarCategory.NUMERUS)) {
      l.add(num.toString());
    }
    if (gen != null && !omit.contains(GrammarCategory.GENUS)) {
      l.add(gen.toString());
    }
    if (determination != null) {
      l.add(determination.toString());
    }
    return String.join("/", l);
  }

}
