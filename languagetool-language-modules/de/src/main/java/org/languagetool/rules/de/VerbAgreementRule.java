/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Markus Brenneis
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
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.language.German;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Example;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.TextLevelRule;
import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.rules.patterns.PatternTokenBuilder;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;

import static java.util.Arrays.*;
import static org.languagetool.JLanguageTool.SENTENCE_START_TAGNAME;
import static org.languagetool.rules.patterns.PatternRuleBuilderHelper.*;

/**
 * Simple agreement checker for German verbs and subject. Checks agreement in:
 * 
 * <ul>
 *  <li>VER:1:SIN w/o ich: e.g. "Max bin da." (incorrect) [same for VER:2:SIN w/o du, VER:1:PLU w/o wir]</li>
 *  <li>ich + VER:[123]:.* (not VER:1:SIN): e.g. "ich bist" (incorrect) [same for du, er, wir]</li> 
 * </ul>
 * 
 * TODO:
 * <ul>
 * <li>wenn nur ein mögliches finites Verb -&gt; das nehmen (Max machen das.)
 * <li>Sie (i&gt;1)
 * <li>bei ich/du/er/wir sofort prüfen, damit alle vorkommen geprüft werden (Ich geht jetzt nach Hause und dort gehe ich sofort unter die Dusche.) [aber: isNear]
 * <li>Alle Verbvorkommen merken (Er präsentieren wollte und Video hätte keine Pläne.)
 * </ul>
 * 
 * @author Markus Brenneis
 */
public class VerbAgreementRule extends TextLevelRule {

  private static final List<List<PatternToken>> ANTI_PATTERNS = asList(
    asList( // "Ken dachte, du wärst ich."
      token("du"),
      token("wärst"),
      token("ich")
    ),
    asList( // "*runterguck* das ist aber tief" (Inflektiv)
      token("*"),
      posRegex("VER:1:SIN:PRÄ:.*"),
      token("*")
    ),
    asList( // "Weder Peter noch ich wollen das."
      new PatternTokenBuilder().token("weder").setSkip(8).build(),
      token("noch"),
      token("ich")
    ),
    asList( // "Kümmerst dich ja gar nicht um sie." (okay for colloquial language)
      pos(SENTENCE_START_TAGNAME),
      posRegex("VER:2:SIN:PRÄ:.*"),
      posRegex("PRO:.*")
    ),
    asList( // "Stehst einfach nicht zu mir." (okay for colloquial language)
      pos(SENTENCE_START_TAGNAME),
      posRegex("VER:2:SIN:PRÄ:.*"),
      tokenRegex("einfach|denn|schon")
    ),
    asList( // "Stellst für deinen Dad etwas zu Essen bereit." (okay for colloquial language)
      pos(SENTENCE_START_TAGNAME),
      posRegex("VER:2:SIN:PRÄ:.*"),
      posRegex("PRP:.*")
    ),
    asList( // "Springst weit, oder?" (okay for colloquial language)
      pos(SENTENCE_START_TAGNAME),
      posRegex("VER:2:SIN:PRÄ:.*"),
      posRegex("ADJ:PRD:.*")
    ),
    asList( // "Siehst aus wie ein Wachhund." (okay for colloquial language)
      pos(SENTENCE_START_TAGNAME),
      posRegex("VER:2:SIN:PRÄ:.*"),
      pos("ZUS")
    ),
    asList( // "Könntest was erfinden, wie dein Papa." (okay for colloquial language)
      pos(SENTENCE_START_TAGNAME),
      pos("VER:MOD:2:SIN:KJ2"),
      posRegex("PRO:.*")
    ),
    asList( // "Darfst nicht so reden, Franz!" (okay for colloquial language)
      pos(SENTENCE_START_TAGNAME),
      pos("VER:MOD:2:SIN:PRÄ"),
      new PatternTokenBuilder().token("ich|er|sie|es|wir|ihr|sie").negate().build()
    ),
    asList(  // "sobald wir frische und neue Akkus haben."
      token("wir"),
      token("frische")
    ),
    asList(  // "wo der Stadtrat gleich mal zum Du übergeht."
      token("zum"),
      csToken("Du"),
      new PatternTokenBuilder().tokenRegex("wechseln|übergehen|schwenken").matchInflectedForms().build()
    ),
    asList(
      token("ich"),
      token("schlafen"),
      token("gehe")
    ),
    asList(
      token("du"),
      token("schlafen"),
      token("gehst")
    ),
    asList(
      token("per"),
      token("du"),
      tokenRegex("sind|waren|sein|wären|war|ist|gewesen")
    ),
    asList(
      token("schnellst"),
      token("möglich")
    ),
    asList(
      // "Da freut er sich, wenn er schlafen geht und was findet."
      token("er"),
      token("schlafen"),
      token("geht")
    ),
    asList(
      token("vermittelst")  // "Sie befestigen die Regalbretter vermittelst dreier Schrauben."
    ),
    asList(
      token("du"),
      token("denkst"),
      token("ich")
    ),
    asList(
      token("na"),
      token("komm")
    ),
    asList(
      tokenRegex("muß|mußten?|müßt?en?"), // alte rechtschreibung (andere fehler)
      tokenRegex("ich|wir|sie|er|es")
    ),
    asList(
      token("ich"),
      tokenRegex("würd|könnt|werd|wollt|sollt|müsst|fürcht"),
      tokenRegex("['’`´‘]")
    ),
    asList(
      tokenRegex("wir|sie|zu"),
      tokenRegex("seh|steh|geh"),
      tokenRegex("['’`´‘]"),
      token("n")
    ),
    asList(
      token("ick"), // different error (berlinerisch)
      tokenRegex("bin|war|wär|hab|hatte")
    ),
    asList(
      // hash tag
      token("#"),
      posRegex("VER.*")
    ),
    asList(
      // wie du war ich auch
      token("wie"),
      tokenRegex("du|ihr|er|es|sie"),
      tokenRegex("bin|war"),
      token("ich")
    ),
    asList(
      // Arabic names: Aryat Abraha bin Sabah Kaaba
      posRegex("UNKNOWN|EIG.*"),
      token("bin"),
      posRegex("UNKNOWN|EIG.*")
    ),
    asList(
      // Du scheiß Idiot
      tokenRegex("du|sie"),
      tokenRegex("schei(ß|ss)"),
      posRegex("SUB.*|UNKNOWN")
    ),
     asList(
       token("Du"),
       tokenRegex("bist|warst|wärst")
     ),
     asList(
       token("als"),
       token("auch"),
       tokenRegex("er|sie|wir|du|ich|ihr")
     ),
     asList(
       tokenRegex("so|wie|zu"),
       token("lange"),
       tokenRegex("er|sie|wir|du|ich|ihr")
     ),
     asList(
       // Ich will nicht so wie er enden.
       new PatternTokenBuilder().tokenRegex("so|genauso|ähnlich").matchInflectedForms().setSkip(2).build(),
       token("wie"),
       tokenRegex("er|sie|du|ihr|ich"),
       posRegex("VER.*")
     ),
    asList(
      // "Bekommst sogar eine Sicherheitszulage"
      pos(SENTENCE_START_TAGNAME),
      posRegex("VER:2:SIN:.*"),
      posRegex("ART.*|ADV.*|PRO:POS.*")
    ),
    asList(
      // "A, B und auch ich"
      token(","),
      posRegex("EIG:.*|UNKNOWN"),
      regex("und|oder"),
      token("auch"),
      token("ich")
    ),
    asList(
      // "Dallun sagte nur, dass er gleich kommen wird und legte wieder auf."
      // "Sie fragte, ob er bereit für die zweite Runde ist."
      posRegex("VER.*"),  // z.B. "Bist"
      tokenRegex("er|sie|ich|wir|du|es|ihr"),
      tokenRegex("gleich|bereit|lange|schnelle?|halt|bitte|dank")  // ist hier kein Verb
    ),
    asList(
      // "Dallun sagte nur, dass er gleich kommen wird und legte wieder auf."
      posRegex("ADV.*|KON.*"),
      tokenRegex("er|sie|ich|wir|du|es|ihr"),
      tokenRegex("gleich|bereit|lange|schnelle?|halt|bitte|dank")  // ist hier kein Verb
    ),
    asList(
      // "Woraufhin ich verlegen lächelte"
      posRegex("ADV.*|KON.*"),
      tokenRegex("er|sie|ich|wir|du|es|ihr"),
      tokenRegex("verlegen"),
      posRegex("VER.*")
    ),
    asList(
      // "Bringst nicht einmal so etwas Einfaches zustande!"
      pos(SENTENCE_START_TAGNAME),
      posRegex("VER:2:SIN:.*"),
      token("nicht")
    ),
    asList(
      // "Da machte er auch vor dem eigenen Volk nicht halt."
      new PatternTokenBuilder().token("machen").matchInflectedForms().setSkip(-1).build(),
      token("halt")
    ),
    asList(  // "Ich hoffe du auch."
      posRegex("VER:.*"),
      tokenRegex("du|ihr"),
      token("auch")
    ),
    asList(
      // "Für Sie mache ich eine Ausnahme."
      token("für"),
      token("Sie"),
      pos("VER:3:SIN:KJ1:SFT"),
      token("ich")
      ),
    asList(
      // "Einer wie du kennt ...", "Aber wenn jemand wie Du daherkommt"
      tokenRegex("(irgend)?einer?|(irgend)?jemand"),
      token("wie"),
      token("du"),
      posRegex("VER:3:.*")
    ),
    asList(
      // "Kannst mich gerne anrufen" (ugs.)
      pos("VER:MOD:2:SIN:PRÄ"),
      posRegex("PRO:PER:.*")
    ),
    asList(
      tokenRegex("die|welche"),
      tokenRegex(".*"),
      tokenRegex("mehr|weniger"),
      token("als"),
      tokenRegex("ich|du|e[rs]|sie")
    ),
    asList(
      token("wenn"),
      token("du"),
      token("anstelle")
    ),
    asList( // "Ok bin ab morgen bei euch." (umgangssprachlich, benötigt eigene Regel)
      tokenRegex("ok(ay)?|ja|nein|vielleicht|oh"),
      tokenRegex("bin|sind")
    ),
    asList(
      token("das"),
      csToken("Du"),
      new PatternTokenBuilder().token("anbieten").matchInflectedForms().build()
    ),
    asList(
      csToken("Du"),  // "jemanden mit Du anreden"
      new PatternTokenBuilder().tokenRegex("anreden|ansprechen").matchInflectedForms().build()
    ),
    asList(
      posRegex("SUB:.*PLU.*"),
      token("wie"),  // "Schreibtischtäter wie Du sind doch eher selten."
      token("Du"),
      posRegex("VER:3:PLU.*")
    ),
    asList(
      token("würd"),
      tokenRegex("[nm]ich|man|ichs|'")
    ),
    asList(
      token(","),
      posRegex("VER:MOD:2:.*")
    ),
    asList(
      csToken("Soll"),
      token("ich")
    ),
    asList(
      csToken("Solltest"),
      token("du")
    ),
    asList(
      csToken("Müsstest"), // Müsstest dir das mal genauer anschauen.
      token("dir")
    ),
    asList(
      csToken("Könntest"), // Könntest dir mal eine Scheibe davon abschneiden!
      token("dir")
    ),
    asList(
      csToken("Sollte"),
      tokenRegex("er|sie")
    ),
    asList(
      pos(SENTENCE_START_TAGNAME),  // "Bin gleich wieder da"
      tokenRegex("Bin|Kannst|Musst")
    ),
    asList(
      token(","),  // "..., hast aber keine Ahnung!"
      tokenRegex("bin|hast|kannst|musst")
    ),
    asList(
      token("er"),  // "egal, was er sagen wird, ..."
      posRegex("VER:.*"),
      token("wird")
    ),
    asList(
      tokenRegex("wie|als"),  // "Ein Mann wie ich braucht einen Hut"
      token("ich"),
      posRegex("VER:.*")
    ),
    asList(
      tokenRegex("ich"),  // "Ich weiß, was ich tun werde, falls etwas geschehen sollte."
      pos("VER:INF:NON"),
      token("werde")
    ),
    asList(
      posRegex("VER:IMP:SIN:.*"),  // "Kümmere du dich mal nicht darum!"
      token("du"),
      tokenRegex("dich|dein|deine[srnm]?|mal")
    ),
    asList(
      posRegex("VER:IMP:SIN:.*"),  // "Nee, geh du!"
      token("du"),
      token("!")
    ),
    asList(
      token("sei"),
      token("du"),
      token("selbst")
    ),
    asList(
      token("als"),  // "Du bist in dem Moment angekommen, als ich gegangen bin."
      token("ich"),
      posRegex("PA2:.*"),
      token("bin")
    ),
    asList(
     token("als"),
     tokenRegex("du|e[rs]|sie|ich"),
     new PatternTokenBuilder().token("sein").matchInflectedForms().build(),
     tokenRegex("[\\.,]")
    ),
    asList( // Musst du gehen?
     tokenRegex("D[au]rf.*|Muss.*"),
     posRegex("PRO:PER:NOM:.+"),
     posRegex("VER:INF:.+"),
     pos("PKT"),
     tokenRegex("(?!die).+")
    ),
    asList(
     csToken("("),
     posRegex("VER:2:SIN:.+"),
     csToken(")")
    ),
    asList(
     posRegex("VER:MOD:1:PLU:.+"),
     csToken("wir"),
     csToken("bitte")
    ),
    asList( // Ohne sie hätte ich das nie geschafft.
     token("ohne"),
     token("sie"),
     token("hätte"),
     token("ich")
    ),
    asList( // Geh du mal!
      pos(SENTENCE_START_TAGNAME),
      posRegex("VER:IMP:SIN.+"),
      token("du"),
      new PatternTokenBuilder().csToken("?").negate().build()
    ),
    asList( // -Du fühlst dich unsicher?
      tokenRegex("[^a-zäöüß]+du"),
      pos("VER:2:SIN:PRÄ:SFT")
    ),
    asList( // Mir ist bewusst, dass viele Menschen wie du empfinden.
      posRegex("PRO:IND.*"),
      posRegex("SUB:.+:PLU.*"),
      tokenRegex("wie|als"),
      posRegex("PRO:PER.+"),
      posRegex("VER:[1-3]:PLU.*")
    ),
    asList( //Weniger als du arbeitet keiner.
      pos("ADV:MOD"),
      tokenRegex("als"),
      posRegex("PRO:PER.+"),
      posRegex("VER:3:SIN.*"),
      posRegex("PRO:IND:NOM:SIN.*")
    ),
    asList( //So was wie er könnte ich nicht machen, dich einfach dann zu verlassen
      posRegex("PRO:IND.*"),
      tokenRegex("wie"),
      posRegex("PRO:PER.+"),
      posRegex("VER:.*:SIN.*"),
      posRegex("PRO:PER:NOM:SIN.*")
    ),
    asList( //Kein anderer als du kann mich glücklich machen
      tokenRegex("kein|keine"),
      tokenRegex("anderer|andere"),
      token("als"),
      tokenRegex("ich|du|er|sie|es"),
      posRegex("VER:MOD.*:PRÄ")
    ),
    asList( // Ich will nicht die gleiche Luft wie er einatmen
      posRegex("ART:DEF.*"),
      tokenRegex("gleich(e|en)|selb(e|en)"),
      posRegex("SUB:.+"),
      token("wie"),
      posRegex("PRO:PER:NOM:SIN.*"),
      posRegex("VER:INF.*")
    ),
    asList( // Was würdest du sagen, wenn du ich wärst?
      token("wenn"),
      posRegex("PRO:PER:NOM:SIN.+"),
      posRegex("PRO:PER:NOM.+"),
      posRegex("VER:AUX:[1-3]:SIN:KJ2")
    ),
    asList( // Was würdet ihr sagen, wenn ihr ich wärt?
      token("wenn"),
      posRegex("PRO:PER:NOM:PLU.+"),
      posRegex("PRO:PER:NOM.+"),
      posRegex("VER:AUX:[1-3]:PLU:KJ2")
    ),
    asList( //Wenn du gehen willst, dann geh!
      token("wenn"),
      token("du"),
      token("gehen"),
      token("willst"),
      token(","),
      token("dann"),
      token("geh")
    ),
    asList( //Ich habe mich noch nicht entschieden, ob ich studieren oder arbeiten gehen soll.
      token("ob"),
      token("ich"),
      posRegex("VER:1.+"),
      token("oder"),
      posRegex("VER:1.+"),
      new PatternTokenBuilder().token("gehen").matchInflectedForms().build(),
      posRegex("VER:MOD:1.*")
    ),
    asList( //Mal seh’n, wie’s Wetter wird.
      token("mal"),
      token("seh"),
      tokenRegex("’|'"),
      token("n")
    )
  );

  // Words that prevent a rule match when they occur directly before "bin":
  private static final Set<String> BIN_IGNORE = new HashSet<>(asList(
    "Suleiman",
    "Mohamed",
    "Muhammad",
    "Muhammed",
    "Mohammed",
    "Mohammad",
    "Mansour",
    "Qaboos",
    "Qabus",
    "Tamim",
    "Majid",
    "Salman",
    "Ghazi",
    "Mahathir",
    "Madschid",
    "Maktum",
    "al-Aziz",
    "Asis",
    "Numan",
    "Hussein",
    "Abdul",
    "Abdulla",
    "Abdullah",
    "Isa",
    "Osama",
    "Said",
    "Zayid",
    "Zayed",
    "Hamad",
    "Chalifa",
    "Raschid",
    "Turki",
    "/"
  ));
  
  private static final Set<String> CONJUNCTIONS = new HashSet<>(asList(
    "weil",
    "obwohl",
    "dass",
    "indem",
    "sodass"/*,
    "damit",
    "wenn"*/
  ));

  private static final Set<String> QUOTATION_MARKS = new HashSet<>(asList(
    "\"", "„"
  ));
  
  private final German language;
  private final Supplier<List<DisambiguationPatternRule>> antiPatterns;

  public VerbAgreementRule(ResourceBundle messages, German language) {
    this.language = language;
    super.setCategory(Categories.GRAMMAR.getCategory(messages));
    addExamplePair(Example.wrong("Ich <marker>bist</marker> über die Entwicklung sehr froh."),
                   Example.fixed("Ich <marker>bin</marker> über die Entwicklung sehr froh."));
    antiPatterns = cacheAntiPatterns(language, ANTI_PATTERNS);
  }
  
  @Override
  public String getId() {
    return "DE_VERBAGREEMENT";
  }
  
  @Override
  public String getDescription() {
    return "Kongruenz von Subjekt und Prädikat (nur 1. u. 2. Person oder m. Personalpronomen), z.B. 'Er bist (ist)'";
  }
  
  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    int pos = 0;
    for (AnalyzedSentence sentence : sentences) {
      int idx = 0;
      AnalyzedTokenReadings[] tokens = sentence.getTokens();
      AnalyzedSentence partialSentence;
      for (int i = 2; i < tokens.length; i++) {
        if (",".equals(tokens[i-2].getToken()) && CONJUNCTIONS.contains(tokens[i].getToken())) {
          partialSentence = new AnalyzedSentence(copyOfRange(tokens, idx, i));
          ruleMatches.addAll(match(partialSentence, pos, sentence));
          idx = i;
        }
      }
      partialSentence = new AnalyzedSentence(copyOfRange(tokens, idx, tokens.length));
      ruleMatches.addAll(match(partialSentence, pos, sentence));
      pos += sentence.getCorrectedTextLength();
    }
    return toRuleMatchArray(ruleMatches);
  }

  private List<RuleMatch> match(AnalyzedSentence sentence, int pos, AnalyzedSentence wholeSentence) {

    AnalyzedTokenReadings finiteVerb = null;
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = getSentenceWithImmunization(sentence).getTokensWithoutWhitespace();
    
    if (tokens.length < 4) { // ignore one-word sentences (3 tokens: SENT_START, one word, SENT_END)
      return ruleMatches;
    }
    
    // position of the pronouns:
    int posIch = -1;
    int posDu = -1;
    int posEr = -1;
    int posWir = -1;
    // positions of verbs which do match in person and number, and do not match any other person nor number:
    int posVer1Sin = -1;
    int posVer2Sin = -1;
    int posVer1Plu = -1;
    /*int posVer2Plu = -1;*/
    // positions of verbs which do match in person and number:
    int posPossibleVer1Sin = -1;
    int posPossibleVer2Sin = -1;
    int posPossibleVer3Sin = -1;
    int posPossibleVer1Plu = -1;
    /*int posPossibleVer2Plu = -1;*/
    
    for (int i = 1; i < tokens.length; ++i) { // ignore SENT_START

      String strToken = tokens[i].getToken().toLowerCase();
      strToken = strToken.replace("‚", "");

      switch (strToken) {
        case "ich":
          posIch = i;
          break;
        case "du":
          posDu = i;
          break;
        case "er":
          posEr = i;
          break;
        case "wir":
          posWir = i;
          break;
      }
      
      if (tokens[i].hasPartialPosTag("VER")
          && (Character.isLowerCase(tokens[i].getToken().charAt(0)) || i == 1 || isQuotationMark(tokens[i-1])) ) {
        if (hasUnambiguouslyPersonAndNumber(tokens[i], "1", "SIN")
            && !(strToken.equals("bin") && (BIN_IGNORE.contains(tokens[i-1].getToken())
                  || (tokens.length != i + 1 && tokens[i+1].getToken().startsWith("Laden")) ))) {
          posVer1Sin = i;
        } 
        else if (hasUnambiguouslyPersonAndNumber(tokens[i], "2", "SIN") && !"Probst".equals(tokens[i].getToken())) {
          posVer2Sin = i;
        } else if (hasUnambiguouslyPersonAndNumber(tokens[i], "1", "PLU")) {
          posVer1Plu = i;
//      } else if (hasUnambiguouslyPersonAndNumber(tokens[i], "2", "PLU")) {
//        posVer2Plu = i;
        }
        
        if (tokens[i].hasPartialPosTag(":1:SIN")) {
          posPossibleVer1Sin = i;
        }
        if (tokens[i].hasPartialPosTag(":2:SIN")) {
          posPossibleVer2Sin = i;
        }
        if (tokens[i].hasPartialPosTag(":3:SIN")) {
          posPossibleVer3Sin = i;
        }
        if (tokens[i].hasPartialPosTag(":1:PLU")) {
          posPossibleVer1Plu = i;
        }
//      if (tokens[i].hasPartialPosTag(":2:PLU"))
//        posPossibleVer2Plu = i;
        
      }
           
    } // for each token
      
    // "ich", "du", and "wir" must be subject (no other interpretation possible)
    // "ich", "du", "er", and "wir" must have a matching verb

    if (posVer1Sin != -1 && posIch == -1 && !isQuotationMark(tokens[posVer1Sin-1])) { // 1st pers sg verb but no "ich"
      if (!tokens[posVer1Sin].isImmunized()) {
        ruleMatches.add(ruleMatchWrongVerb(tokens[posVer1Sin], pos, wholeSentence));
      }
    } else if (posIch > 0 && !isNear(posPossibleVer1Sin, posIch) // check whether verb next to "ich" is 1st pers sg
               && (tokens[posIch].getToken().equals("ich") || tokens[posIch].getStartPos() <= 1 ||
                   (tokens[posIch].getToken().equals("Ich") && posIch >= 2 && tokens[posIch-2].getToken().equals(":")) ||
                   (tokens[posIch].getToken().equals("Ich") && posIch >= 1 && tokens[posIch-1].getToken().equals(":"))) // ignore "lyrisches Ich" etc.
               && (!isQuotationMark(tokens[posIch-1]) || posIch < 3 || (posIch > 1 && tokens[posIch-2].getToken().equals(":")))) {
      int plus1 = ((posIch + 1) == tokens.length) ? 0 : +1; // prevent posIch+1 segfault
      BooleanAndFiniteVerb check = verbDoesMatchPersonAndNumber(tokens[posIch - 1], tokens[posIch + plus1], "1", "SIN", finiteVerb);
      if (!check.verbDoesMatchPersonAndNumber && !nextButOneIsModal(tokens, posIch) && !"äußerst".equals(check.finiteVerb.getToken())) {
        if (!tokens[posIch].isImmunized()) {
          ruleMatches.add(ruleMatchWrongVerbSubject(tokens[posIch], check.finiteVerb, "1:SIN", pos, wholeSentence));
        }
      }
    }
    
    if (posVer2Sin != -1 && posDu == -1 && !isQuotationMark(tokens[posVer2Sin-1])) {
      if (!tokens[posVer2Sin].isImmunized()) {
        ruleMatches.add(ruleMatchWrongVerb(tokens[posVer2Sin], pos, wholeSentence));
      }
    } else if (posDu > 0 && !isNear(posPossibleVer2Sin, posDu)
               &&(!isQuotationMark(tokens[posDu-1]) || posDu < 3 || (posDu > 1 && tokens[posDu-2].getToken().equals(":")))) {
      int plus1 = ((posDu + 1) == tokens.length) ? 0 : +1;
      BooleanAndFiniteVerb check = verbDoesMatchPersonAndNumber(tokens[posDu - 1], tokens[posDu + plus1], "2", "SIN", finiteVerb);
      if (!check.verbDoesMatchPersonAndNumber &&
          !tokens[posDu+plus1].hasPosTagStartingWith("VER:1:SIN:KJ2") && // "Wenn ich du wäre"
          !(tokens[posDu+plus1].hasPosTagStartingWith("ADJ:") && !tokens[posDu+plus1].hasPosTag("ADJ:PRD:GRU"))&& // "dass du billige Klamotten..."
          !tokens[posDu-1].hasPosTagStartingWith("VER:1:SIN:KJ2") &&
          !nextButOneIsModal(tokens, posDu) &&
          !tokens[posDu].isImmunized()
      ) {
        ruleMatches.add(ruleMatchWrongVerbSubject(tokens[posDu], check.finiteVerb, "2:SIN", pos, wholeSentence));
      }
    }
    
    if (posEr > 0 && !isNear(posPossibleVer3Sin, posEr)
        && (!isQuotationMark(tokens[posEr-1])  || posEr < 3 || (posEr > 1 && tokens[posEr-2].getToken().equals(":")))) {
      int plus1 = ((posEr + 1) == tokens.length) ? 0 : +1;
      BooleanAndFiniteVerb check = verbDoesMatchPersonAndNumber(tokens[posEr - 1], tokens[posEr + plus1], "3", "SIN", finiteVerb);
      if (!check.verbDoesMatchPersonAndNumber 
              && !nextButOneIsModal(tokens, posEr)
              && !"äußerst".equals(check.finiteVerb.getToken())
              && !"regen".equals(check.finiteVerb.getToken())  // "wo er regen Anteil nahm"
              && !tokens[posEr].isImmunized()
          ) {
        ruleMatches.add(ruleMatchWrongVerbSubject(tokens[posEr], check.finiteVerb, "3:SIN", pos, wholeSentence));
      }
    }
    
    if (posVer1Plu != -1 && posWir == -1 && !isQuotationMark(tokens[posVer1Plu-1])) {
      if (!tokens[posVer1Plu].isImmunized()) {
        ruleMatches.add(ruleMatchWrongVerb(tokens[posVer1Plu], pos, wholeSentence));
      }
    } else if (posWir > 0 && !isNear(posPossibleVer1Plu, posWir) && !isQuotationMark(tokens[posWir-1])) {
      int plus1 = ((posWir + 1) == tokens.length) ? 0 : +1;
      BooleanAndFiniteVerb check = verbDoesMatchPersonAndNumber(tokens[posWir - 1], tokens[posWir + plus1], "1", "PLU", finiteVerb);
      if (!check.verbDoesMatchPersonAndNumber && !nextButOneIsModal(tokens, posWir) && !tokens[posWir].isImmunized() &&
          !check.finiteVerb.getToken().equals("äußerst")) {
        ruleMatches.add(ruleMatchWrongVerbSubject(tokens[posWir], check.finiteVerb, "1:PLU", pos, wholeSentence));
      }
    }
    
    return ruleMatches;
  }

  @Override
  public List<DisambiguationPatternRule> getAntiPatterns() {
    return antiPatterns.get();
  }

  // avoid false alarm on 'wenn ich sterben sollte ...':
  private boolean nextButOneIsModal(AnalyzedTokenReadings[] tokens, int pos) {
    return pos < tokens.length - 2 && tokens[pos+2].hasPartialPosTag(":MOD:");
  }

  /**
   * @return true if |a - b| < 5, and a != -1 
   */
  private boolean isNear(int a, int b) {
    return a != -1 && (Math.abs(a - b) < 5);
  }
  
  private boolean isQuotationMark(AnalyzedTokenReadings token) {
    return QUOTATION_MARKS.contains(token.getToken());
  }
  
  /**
   * @return true if the verb @param token (if it is a verb) matches @param person and @param number, and matches no other person/number
   */
  private boolean hasUnambiguouslyPersonAndNumber(AnalyzedTokenReadings tokenReadings, String person, String number) {
    if (tokenReadings.getToken().length() == 0
        || (Character.isUpperCase(tokenReadings.getToken().charAt(0)) && tokenReadings.getStartPos() != 0)
        || !tokenReadings.hasPosTagStartingWith("VER")) {
      return false;
    }
    for (AnalyzedToken analyzedToken : tokenReadings) {
      String postag = analyzedToken.getPOSTag();
      if (postag == null || postag.endsWith("_END")) { // ignore SENT_END and PARA_END
        continue;
      }
      if (!postag.contains(":" + person + ":" + number)) {
        return false;
      }
    }
    return true;
  }
  
  /**
   * @return true if @param token is a finite verb, and it is no participle, pronoun or number
   */
  private boolean isFiniteVerb(AnalyzedTokenReadings token) {
    if (token.getToken().length() == 0
        || (Character.isUpperCase(token.getToken().charAt(0)) && token.getStartPos() != 0)
        || !token.hasPosTagStartingWith("VER")
        || token.hasAnyPartialPosTag("PA2", "PRO:", "ZAL")
        || "einst".equals(token.getToken())) {
      return false;
    }
    return token.hasAnyPartialPosTag(":1:", ":2:", ":3:");
  }
  
  /**
   * @return false if neither the verb @param token1 (if any) nor @param token2 match @param person and @param number, and none of them is "und" or ","
   * if a finite verb is found, it is saved in finiteVerb
   */
  private BooleanAndFiniteVerb verbDoesMatchPersonAndNumber(AnalyzedTokenReadings token1, AnalyzedTokenReadings token2,
                                               String person, String number, AnalyzedTokenReadings finiteVerb) {
    if (StringUtils.equalsAny(token1.getToken(), ",", "und", "sowie", "&") ||
    		StringUtils.equalsAny(token2.getToken(), ",", "und", "sowie", "&")) {
      return new BooleanAndFiniteVerb(true, finiteVerb);
    }
   
    boolean foundFiniteVerb = false;
    
    if (isFiniteVerb(token1)) {
      foundFiniteVerb = true;
      finiteVerb = token1;
      if (token1.hasPartialPosTag(":" + person + ":" + number)) {
        return new BooleanAndFiniteVerb(true, finiteVerb);
      }
    }
    
    if (isFiniteVerb(token2)) {
      foundFiniteVerb = true;
      finiteVerb = token2;
      if (token2.hasPartialPosTag(":" + person + ":" + number)) {
        return new BooleanAndFiniteVerb(true, finiteVerb);
      }
    }
    
    return new BooleanAndFiniteVerb(!foundFiniteVerb, finiteVerb);
  }
  
  /**
   * @return a list of forms of @param verb which match @param expectedVerbPOS (person:number)
   * @param toUppercase true when the suggestions should be capitalized
   */
  private List<String> getVerbSuggestions(AnalyzedTokenReadings verb, String expectedVerbPOS, boolean toUppercase) {
    // find the first verb reading
    AnalyzedToken verbToken = new AnalyzedToken("", "", "");
    for (AnalyzedToken token : verb.getReadings()) {
      //noinspection ConstantConditions
      if (token.getPOSTag().startsWith("VER:")) {
        verbToken = token;
        break;
      }
    }
    
    try {
      String[] synthesized = language.getSynthesizer().synthesize(verbToken, "VER.*:"+expectedVerbPOS+".*", true);
      Set<String> suggestionSet = new HashSet<>(asList(synthesized));  // remove duplicates
      List<String> suggestions = new ArrayList<>(suggestionSet);
      if (toUppercase) {
        for (int i = 0; i < suggestions.size(); ++i) {
          suggestions.set(i, StringTools.uppercaseFirstChar(suggestions.get(i)));
        }
      }
      return suggestions;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  /**
   * @return a list of pronouns which match the person and number of @param verb
   * @param toUppercase true when the suggestions should be capitalized
   */
  private List<String> getPronounSuggestions(AnalyzedTokenReadings verb, boolean toUppercase) {
    List<String> result = new ArrayList<>();
    if (verb.hasPartialPosTag(":1:SIN")) {
      result.add("ich");
    }
    if (verb.hasPartialPosTag(":2:SIN")) {
      result.add("du");
    }
    if (verb.hasPartialPosTag(":3:SIN")) {
      result.add("er");
      result.add("sie");
      result.add("es");
    }
    if (verb.hasPartialPosTag(":1:PLU")) {
      result.add("wir");
    }
    if (verb.hasPartialPosTag(":2:PLU")) {
      result.add("ihr");
    }
    if (verb.hasPartialPosTag(":3:PLU") && !result.contains("sie")) { // do not add "sie" twice
      result.add("sie");
    }
    if (toUppercase) {
      for (int i = 0; i < result.size(); ++i) {
        result.set(i, StringTools.uppercaseFirstChar(result.get(i)));
      }
    }
    return result;
  }
  
  private RuleMatch ruleMatchWrongVerb(AnalyzedTokenReadings token, int pos, AnalyzedSentence sentence) {
    String msg = "Möglicherweise fehlende grammatische Übereinstimmung zwischen Subjekt und Prädikat (" +
      token.getToken() + ") bezüglich Person oder Numerus (Einzahl, Mehrzahl - Beispiel: " +
      "'Max bist' statt 'Max ist').";
    return new RuleMatch(this, sentence, pos+token.getStartPos(), pos+token.getEndPos(), msg);
  }
  
  private RuleMatch ruleMatchWrongVerbSubject(AnalyzedTokenReadings subject, AnalyzedTokenReadings verb, String expectedVerbPOS, int pos, AnalyzedSentence sentence) {
    String msg = "Möglicherweise fehlende grammatische Übereinstimmung zwischen Subjekt (" + subject.getToken() +
      ") und Prädikat (" + verb.getToken() + ") bezüglich Person oder Numerus (Einzahl, Mehrzahl - Beispiel: " +
      "'ich sind' statt 'ich bin').";
    
    List<String> suggestions = new ArrayList<>();
    List<String> verbSuggestions = new ArrayList<>();
    List<String> pronounSuggestions = new ArrayList<>();
    
    RuleMatch ruleMatch;
    if (subject.getStartPos() < verb.getStartPos()) {
      ruleMatch = new RuleMatch(this, sentence, pos+subject.getStartPos(), pos+verb.getStartPos()+verb.getToken().length(), msg);
      verbSuggestions.addAll(getVerbSuggestions(verb, expectedVerbPOS, false));
      for (String verbSuggestion : verbSuggestions) {
        suggestions.add(subject.getToken() + " " + verbSuggestion);
      }
      pronounSuggestions.addAll(getPronounSuggestions(verb, Character.isUpperCase(subject.getToken().charAt(0))));
      for (String pronounSuggestion : pronounSuggestions) {
        suggestions.add(pronounSuggestion + " " + verb.getToken());
      }
      String markedText = sentence.getText().substring(subject.getStartPos(), verb.getStartPos()+verb.getToken().length());
      sortBySimilarity(suggestions, markedText);
      ruleMatch.setSuggestedReplacements(suggestions);
    } else {
      ruleMatch = new RuleMatch(this, sentence, pos+verb.getStartPos(), pos+subject.getStartPos()+subject.getToken().length(), msg);
      verbSuggestions.addAll(getVerbSuggestions(verb, expectedVerbPOS, Character.isUpperCase(verb.getToken().charAt(0))));
      for (String verbSuggestion : verbSuggestions) {
        suggestions.add(verbSuggestion + " " + subject.getToken());
      }
      pronounSuggestions.addAll(getPronounSuggestions(verb, false));
      for (String pronounSuggestion : pronounSuggestions) {
        suggestions.add(verb.getToken() + " " + pronounSuggestion);
      }
      String markedText = sentence.getText().substring(verb.getStartPos(), subject.getStartPos()+subject.getToken().length());
      sortBySimilarity(suggestions, markedText);
      ruleMatch.setSuggestedReplacements(suggestions);
    }
    
    return ruleMatch;
  }

  private void sortBySimilarity(List<String> suggestions, String markedText) {
    suggestions.sort((o1, o2) -> {
      int diff1 = LevenshteinDistance.getDefaultInstance().apply(markedText, o1);
      int diff2 = LevenshteinDistance.getDefaultInstance().apply(markedText, o2);
      return diff1 - diff2;
    });
  }

  static class BooleanAndFiniteVerb {
    boolean verbDoesMatchPersonAndNumber;
    AnalyzedTokenReadings finiteVerb;
    private BooleanAndFiniteVerb(boolean verbDoesMatchPersonAndNumber, AnalyzedTokenReadings finiteVerb) {
      this.verbDoesMatchPersonAndNumber = verbDoesMatchPersonAndNumber;
      this.finiteVerb = finiteVerb;
    }
  }

  @Override
  public int minToCheckParagraph() {
    return 0;
  }
}
