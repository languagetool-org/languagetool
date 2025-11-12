package org.languagetool.rules.ca;

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.synthesis.ca.VerbSynthesizer;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.languagetool.rules.ca.PronomsFeblesHelper.*;
import static org.languagetool.synthesis.ca.VerbSynthesizer.pVerb;
import static org.languagetool.rules.ca.VerbsHelper.isVerbDicendiBefore;

public class DonarseliBeFilter extends RuleFilter {

  private final Pattern pDespresDarrerAdverbi = Pattern.compile("V.N.*|D.*|PD.*");
  private final List<String> adverbiFinal = Arrays.asList("bé", "malament", "mal", "millor", "pitjor", "fatal");
  private final List<String> pronomsPersonals = Arrays.asList("mi", "tu", "ell", "ella", "nosaltres", "vosaltres",
    "ells", "elles");
  private final List<String> exceptionsQue = Arrays.asList("ja", "ara", "per", "de", "a", "en");

  @Nullable
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
                                   AnalyzedTokenReadings[] patternTokens, List<Integer> tokenPositions) throws IOException {
    int posWord = 0;
    int posDonar;
    int posPrimerVerb;
    int primerAdverbi;
    int darrerAdverbi;
    int posInitUnderline;
    AnalyzedToken primerVerb = null;
    AnalyzedToken pronomFebleRelevant = null;
    boolean isPronomFebleDavant = false;
    AnalyzedToken despresDarrerAdverbi = null;
    Language lang = getLanguageFromRuleMatch(match);
    AnalyzedTokenReadings[] tokens = match.getSentence().getTokensWithoutWhitespace();
    while (posWord < tokens.length
      && (tokens[posWord].getStartPos() < match.getFromPos() || tokens[posWord].isSentenceStart())) {
      posWord++;
    }
    VerbSynthesizer verbSynth = new VerbSynthesizer(tokens, posWord, lang);
    if (verbSynth.isUndefined() || tokens[verbSynth.getLastVerbIndex()].getEndPos() > match.getToPos()) {
      return null;
    }
    posDonar = verbSynth.getLastVerbIndex();
    posPrimerVerb = verbSynth.getFirstVerbIndex();
    posInitUnderline = posPrimerVerb - verbSynth.getNumPronounsBefore();
    isPronomFebleDavant = verbSynth.getNumPronounsBefore() > 0;
    int posPronomFebleRelevant = -1;
    if (verbSynth.getNumPronounsAfter() == 2) {
      posPronomFebleRelevant = posDonar + 2;
    } else if (verbSynth.getNumPronounsBefore() >= 2) {
      // Si n'hi ha tres, suposem que és un "hi" que ignorem
      posPronomFebleRelevant = posPrimerVerb - (verbSynth.getNumPronounsBefore() - 1);
    }
    if (posPronomFebleRelevant < 1) {
      return null;
    }
    pronomFebleRelevant = tokens[posPronomFebleRelevant].readingWithTagRegex(pPronomFeble);
    if (pronomFebleRelevant == null) {
      return null;
    }
    // mira darrere: molt bé
    posWord = verbSynth.getLastVerbIndex() + verbSynth.getNumPronounsAfter() + 1;
    primerAdverbi = posWord;
    while (posWord < tokens.length && !adverbiFinal.contains(tokens[posWord].getToken().toLowerCase())) {
      posWord++;
    }
    if (posWord == tokens.length || !adverbiFinal.contains(tokens[posWord].getToken().toLowerCase())) {
      return null;
    }
    darrerAdverbi = posWord;
    String darrerAdverbiStr = tokens[darrerAdverbi].getToken();
    if (darrerAdverbiStr.equalsIgnoreCase("mal") || darrerAdverbiStr.equalsIgnoreCase("fatal")) {
      darrerAdverbiStr = "malament";
    }
    if (primerAdverbi == -1 || darrerAdverbi == -1) {
      return null;
    }
    if (darrerAdverbi + 1 < tokens.length) {
      despresDarrerAdverbi = tokens[darrerAdverbi + 1].readingWithTagRegex(pDespresDarrerAdverbi);
    }

    int addTokensToRight = 0;
    String addStringToRight = "";
    int addTokensToLeft = 0;
    String addStringToLeft = "";

    // analitza paraules prèvies: que a mi mai no se'm dona malament
    boolean isNo = posInitUnderline - 1 > 0 && (tokens[posInitUnderline - 1].getToken().equalsIgnoreCase("no")
      || tokens[posInitUnderline - 1].getToken().equalsIgnoreCase("mai"));
    boolean isMaiNo = isNo && posInitUnderline - 2 > 0 && tokens[posInitUnderline - 2].getToken().equalsIgnoreCase(
      "mai");
    boolean isMalament = darrerAdverbiStr.equalsIgnoreCase("malament") || darrerAdverbiStr.equalsIgnoreCase("pitjor");
    // No ... malament
    boolean isNoMalament = isNo && isMalament;
    // ... malement
    isMalament = isMalament && !isNoMalament;
    if (isMaiNo) {
      addTokensToLeft++;
    }
    if (isNo) {
      addTokensToLeft++;
    }
    String aMiString = "";
    if (posInitUnderline - addTokensToLeft - 2 > 0 && tokens[posInitUnderline - addTokensToLeft - 2].getToken().equalsIgnoreCase("a")
      & pronomsPersonals.contains(tokens[posInitUnderline - addTokensToLeft - 1].getToken().toLowerCase())) {
      aMiString =
        tokens[posInitUnderline - addTokensToLeft - 2].getToken() + " " + tokens[posInitUnderline - addTokensToLeft - 1].getToken() + " ";
      addTokensToLeft += 2;
    }
    boolean isQue =
      posInitUnderline - addTokensToLeft - 1 > 0 && tokens[posInitUnderline - addTokensToLeft - 1].getToken().equalsIgnoreCase("que")
        && !isVerbDicendiBefore(tokens, posInitUnderline - addTokensToLeft - 2);
    boolean isQueAccent =
      posInitUnderline - addTokensToLeft - 1 > 0 && tokens[posInitUnderline - addTokensToLeft - 1].getToken().equalsIgnoreCase("què");
    if (posInitUnderline - addTokensToLeft - 2 > 0 && exceptionsQue.contains(tokens[posInitUnderline - addTokensToLeft - 2].getToken().toLowerCase())) {
      isQueAccent = false;
      isQue = false;
    }
    boolean isElQue = false;
    if (isQue && posInitUnderline - addTokensToLeft - 2 > 0 && tokens[posInitUnderline - addTokensToLeft - 2].hasPosTagStartingWith("DA")) {
      isElQue = true;
      isQue = false; // no subratllem "que"
    }
    if (isQue) {
      addTokensToLeft++;
    }
    if (isQueAccent) {
      addTokensToLeft++;
    }
    for (int j = posInitUnderline - addTokensToLeft; j < posInitUnderline; j++) {
      addStringToLeft = addStringToLeft + tokens[j].getToken() + " ";
    }

    //TODO: quines coses se li donen bé; les que no se't donen tan bé;
    // al teu fill no se li dona gaire bé dibuixar; La geografia se't donava prou bé
    // Altres suggermients: tenir-hi la mà trencada, ser el meu fort

    // Crea suggeriments
    List<String> replacements = new ArrayList<>();

    String persona = pronomFebleRelevant.getPOSTag().substring(2, 3);
    String nombre = pronomFebleRelevant.getPOSTag().substring(4, 5);
    primerVerb = tokens[posPrimerVerb].readingWithTagRegex(pVerb);
    String verbPostag = primerVerb.getPOSTag();
    String newVerbPostag = verbPostag.substring(0, 4) + persona + nombre + verbPostag.substring(6, 8);

    StringBuilder suggestion = new StringBuilder();
    // tinc traça (per a)
    String addStringToLeftTincTraca = addStringToLeft.replaceFirst("(?i)què ", "en què ")
      .replaceFirst("(?i)que ", "en què ");
    addStringToLeftTincTraca = addStringToLeftTincTraca.replaceFirst(aMiString, "");
    if (isNoMalament) {
      addStringToLeftTincTraca = addStringToLeftTincTraca.replaceFirst("(?i)no ", "");
      addStringToLeftTincTraca = addStringToLeftTincTraca.replaceFirst("(?i)mai ", "");
    }
    suggestion.append(addStringToLeftTincTraca);
    if (isMalament) {
      suggestion.append("no ");
    }
    if (!addStringToLeft.toLowerCase().startsWith("qu") && despresDarrerAdverbi == null) {
      suggestion.append("hi ");
    }
    verbSynth.setLemmaAndPostag("tenir", newVerbPostag);
    suggestion.append(verbSynth.synthesize());
    if (!isNoMalament && !isMalament) {
      suggestion.append(getAdverbsFor(tokens, primerAdverbi, darrerAdverbi, "traça"));
    }
    suggestion.append(" traça");
    if (despresDarrerAdverbi != null) {
      if (despresDarrerAdverbi.getToken().toLowerCase().equals("el")) {
        suggestion.append(" per al");
        addTokensToRight = 1;
        addStringToRight = " el";
      } else if (despresDarrerAdverbi.getToken().toLowerCase().equals("els")) {
        suggestion.append(" per als");
        addTokensToRight = 1;
        addStringToRight = " els";
      } else {
        suggestion.append(" per a");
      }
    }
    if (!isElQue) {
      replacements.add(StringTools.preserveCase(suggestion.toString(),
        tokens[posInitUnderline - addTokensToLeft].getToken()));
    }


    // faig bé
    suggestion.setLength(0);
    suggestion.append(addStringToLeft.replaceFirst(aMiString, ""));
    if (!addStringToLeft.toLowerCase().startsWith("qu") && despresDarrerAdverbi == null && !isElQue) {
      suggestion.append("ho ");
    }
    verbSynth.setLemmaAndPostag("fer", newVerbPostag);
    suggestion.append(verbSynth.synthesize());
    suggestion.append(getAdverbsFor(tokens, primerAdverbi, darrerAdverbi, "bé"));
    suggestion.append(" " + darrerAdverbiStr);
    suggestion.append(addStringToRight);
    replacements.add(StringTools.preserveCase(suggestion.toString(),
      tokens[posInitUnderline - addTokensToLeft].getToken()));

    // me'n surto (en)
    suggestion.setLength(0);
    suggestion.append(addStringToLeftTincTraca);
    if (isMalament) {
      suggestion.append("no ");
    }
    if (isPronomFebleDavant) {
      String pronom = pronomFebleRelevant.getToken();
      if (pronom.equalsIgnoreCase("'ls") | pronom.equalsIgnoreCase("li")) {
        pronom = "es";
      }
      String pronomsNormalitzats = transform(pronom, PronounPosition.NORMALIZED) + " en";
      suggestion.append(transformDavant(pronomsNormalitzats, primerVerb.getToken()));
    }
    verbSynth.setLemmaAndPostag("sortir", newVerbPostag);
    suggestion.append(verbSynth.synthesize());
    if (!isPronomFebleDavant) {
      String pronom = pronomFebleRelevant.getToken();
      if (pronom.equalsIgnoreCase("'ls") | pronom.equalsIgnoreCase("-li")) {
        pronom = "es";
      }
      String pronomsNormalitzats = transform(pronom, PronounPosition.NORMALIZED) + " en";
      suggestion.append(transformDarrere(pronomsNormalitzats, primerVerb.getToken()));
    }

    /*if (!isNoMalament && !isMalament) {
      suggestion.append(getAdverbsFor(tokens, primerAdverbi, darrerAdverbi, "traça"));
    }*/
    if (despresDarrerAdverbi != null) {
      if (despresDarrerAdverbi.getPOSTag().startsWith("V")) {
        suggestion.append(" a");
      } else {
        suggestion.append(" en");
      }
    }
    suggestion.append(addStringToRight);
    if (!isElQue) {
      replacements.add(StringTools.preserveCase(suggestion.toString(),
        tokens[posInitUnderline - addTokensToLeft].getToken()));
    }

    // em van bé
    suggestion.setLength(0);
    suggestion.append(addStringToLeft);
    verbSynth.setLemmaAndPostag("anar", verbPostag);
    String verb = verbSynth.synthesize();
    if (isPronomFebleDavant) {
      suggestion.append(transformDavant(pronomFebleRelevant.getToken(), verb));
    }
    suggestion.append(verb);
    if (!isPronomFebleDavant) {
      suggestion.append(transformDarrere(pronomFebleRelevant.getToken(), verb));
    }
    suggestion.append(getAdverbsFor(tokens, primerAdverbi, darrerAdverbi, "bé"));
    suggestion.append(" " + darrerAdverbiStr);
    suggestion.append(addStringToRight);
    replacements.add(StringTools.preserveCase(suggestion.toString(),
      tokens[posInitUnderline - addTokensToLeft].getToken()));

    // m'ixen bé, em surten bé
    suggestion.setLength(0);
    suggestion.append(addStringToLeft);
    String newLemmaSortir = (lang.getShortCodeWithCountryAndVariant().equals("ca-ES-valencia") ? "eixir" : "sortir");
    verbSynth.setLemmaAndPostag(newLemmaSortir, verbPostag);
    verb = verbSynth.synthesize();
    if (isPronomFebleDavant) {
      suggestion.append(transformDavant(pronomFebleRelevant.getToken(), verb));
    }
    suggestion.append(verb);
    if (!isPronomFebleDavant) {
      suggestion.append(transformDarrere(pronomFebleRelevant.getToken(), verb));
    }
    suggestion.append(getAdverbsFor(tokens, primerAdverbi, darrerAdverbi, "bé"));
    suggestion.append(" " + darrerAdverbiStr);
    suggestion.append(addStringToRight);
    replacements.add(StringTools.preserveCase(suggestion.toString(),
      tokens[posInitUnderline - addTokensToLeft].getToken()));

    if (replacements.isEmpty()) {
      return null;
    }
    RuleMatch ruleMatch = new RuleMatch(match.getRule(), match.getSentence(),
      tokens[posInitUnderline - addTokensToLeft].getStartPos(),
      tokens[darrerAdverbi + addTokensToRight].getEndPos(), match.getMessage(), match.getShortMessage());
    ruleMatch.setType(match.getType());
    ruleMatch.setSuggestedReplacements(replacements);
    return ruleMatch;
  }

  private String getAdverbsFor(AnalyzedTokenReadings[] tokens, int primerAdverbi, int darrerAdverbi, String target) {
    StringBuilder result = new StringBuilder();
    for (int i = primerAdverbi; i < darrerAdverbi; i++) {
      if (tokens[i].isWhitespaceBefore()) {
        result.append(" ");
      }
      result.append(tokens[i].getToken());
    }
    String resultStr = result.toString();
    if (target.equals("traça")) {
      if (resultStr.equalsIgnoreCase(" molt")) {
        resultStr = " molta";
      } else if (resultStr.equalsIgnoreCase(" gens")) {
        resultStr = " gens de";
      } else if (resultStr.equalsIgnoreCase(" tan")) {
        resultStr = " tanta";
      }
    }
    return resultStr;
  }
}
