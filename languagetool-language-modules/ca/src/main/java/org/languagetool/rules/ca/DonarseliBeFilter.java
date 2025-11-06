package org.languagetool.rules.ca;

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.chunking.ChunkTag;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.languagetool.rules.ca.PronomsFeblesHelper.*;

public class DonarseliBeFilter extends RuleFilter {

  private Pattern verbConjugat = Pattern.compile("V.[SI].*");
  private Pattern pDespresDarrerAdverbi = Pattern.compile("V.N.*|D.*|PD.*");
  private List<String> adverbiFinal = Arrays.asList("bé", "malament", "mal", "millor", "pitjor", "fatal");
  private List<String> pronomsPersonals = Arrays.asList("mi", "tu", "ell", "ella", "nosaltres", "vosaltres", "ells",
    "elles");
  Synthesizer synth;

  @Nullable
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
                                   AnalyzedTokenReadings[] patternTokens, List<Integer> tokenPositions) throws IOException {
    int posWord = 0;
    int posDonar = -1;
    int posPrimerVerb = -1;
    int primerAdverbi = -1;
    int darrerAdverbi = -1;
    int posInitUnderline = -1;
    AnalyzedToken primerVerb = null;
    AnalyzedToken pronomFebleRelevant = null;
    boolean isPronomFebleDavant = false;
    AnalyzedToken despresDarrerAdverbi = null;
    synth = getSynthesizerFromRuleMatch(match);
    Language lang = getLanguageFromRuleMatch(match);
    AnalyzedTokenReadings[] tokens = match.getSentence().getTokensWithoutWhitespace();
    while (posWord < tokens.length
      && (tokens[posWord].getStartPos() < match.getFromPos() || tokens[posWord].isSentenceStart())) {
      posWord++;
    }
    while (posWord < tokens.length && !tokens[posWord].hasLemma("donar")) {
      posWord++;
    }
    if (tokens[posWord].hasLemma("donar")) {
      posDonar = posWord;
    }
    if (posDonar == -1) {
      throw new RuntimeException("No hi ha cap verb 'donar' en la regla DONARSELIBE");
    }
    primerVerb = tokens[posWord].readingWithTagRegex(verbConjugat);
    if (primerVerb != null) {
      posPrimerVerb = posWord;
    } else {
      while (posWord > 0 && (primerVerb = tokens[posWord].readingWithTagRegex(verbConjugat)) == null
        && tokens[posWord].getChunkTags().contains(new ChunkTag("GV"))) {
        posWord--;
      }
      posPrimerVerb = posWord;
    }
    posInitUnderline = posPrimerVerb;
    /*if (posPrimerVerb == -1) {
      // pot ser una forma impersonal: donar-se-li
      return null;
    }*/
    int posPronomDavant = 1;
    if (posPrimerVerb - 1 > 0) {
      // ignorem "hi"
      if (tokens[posPrimerVerb - 1].getToken().equalsIgnoreCase("hi")) {
        posPronomDavant = 2;
      }
    }
    if (posPrimerVerb - posPronomDavant > 0) {
      pronomFebleRelevant = tokens[posPrimerVerb - posPronomDavant].readingWithTagRegex(pronomFeble);
      if (pronomFebleRelevant != null) {
        posInitUnderline = posPrimerVerb - posPronomDavant - 1;
        isPronomFebleDavant = true;
      }
    }
    if (pronomFebleRelevant == null && posDonar + 2 < tokens.length) {
      pronomFebleRelevant = tokens[posDonar + 2].readingWithTagRegex(pronomFeble);
    }
    if (pronomFebleRelevant == null) {
      return null;
    }
    // mira darrere: molt bé
    posWord = posDonar + 1;
    if (!isPronomFebleDavant) {
      posWord = posWord + 2;
    }
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
      posInitUnderline - addTokensToLeft - 1 > 0 && tokens[posInitUnderline - addTokensToLeft - 1].getToken().equalsIgnoreCase("que");
    if (isQue) {
      addTokensToLeft++;
    }
    boolean isQueAccent =
      posInitUnderline - addTokensToLeft - 1 > 0 && tokens[posInitUnderline - addTokensToLeft - 1].getToken().equalsIgnoreCase("què");
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
    if (!addStringToLeft.toLowerCase().startsWith("qu") &&  despresDarrerAdverbi == null) {
      suggestion.append("hi ");
    }
    suggestion.append(createVerb(tokens, posPrimerVerb, primerVerb, posDonar, "tenir", newVerbPostag, lang));
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
    replacements.add(StringTools.preserveCase(suggestion.toString(),
      tokens[posInitUnderline - addTokensToLeft].getToken()));

    // faig bé
    suggestion.setLength(0);
    suggestion.append(addStringToLeft.replaceFirst(aMiString, ""));
    if (!addStringToLeft.toLowerCase().startsWith("qu") && despresDarrerAdverbi == null) {
      suggestion.append("ho ");
    }
    suggestion.append(createVerb(tokens, posPrimerVerb, primerVerb, posDonar, "fer", newVerbPostag, lang));
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
    suggestion.append(createVerb(tokens, posPrimerVerb, primerVerb, posDonar, "sortir", newVerbPostag, lang));
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
    replacements.add(StringTools.preserveCase(suggestion.toString(),
      tokens[posInitUnderline - addTokensToLeft].getToken()));

    // em van bé
    suggestion.setLength(0);
    suggestion.append(addStringToLeft);
    String verb = createVerb(tokens, posPrimerVerb, primerVerb, posDonar, "anar", verbPostag, lang);
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
    verb = createVerb(tokens, posPrimerVerb, primerVerb, posDonar, newLemmaSortir, verbPostag, lang);
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

  private String createVerb(AnalyzedTokenReadings[] tokens, int posPrimerVerb, AnalyzedToken primerVerb, int posDonar,
                            String newLemma, String newPostag, Language lang) throws IOException {
    StringBuilder result = new StringBuilder();
    if (posPrimerVerb == posDonar) {
      String[] synthesized = synth.synthesize(new AnalyzedToken("", newPostag, newLemma), newPostag);
      if (synthesized.length > 0) {
        result.append(synthesized[0]);
      }
    } else {
      for (int i = posPrimerVerb; i <= posDonar; i++) {
        if (i == posPrimerVerb) {
          String[] synthesized = synth.synthesize(primerVerb, newPostag);
          if (synthesized.length > 0) {
            result.append(synthesized[0]);
          }
        } else if (i == posDonar) {
          if (tokens[i].isWhitespaceBefore()) {
            result.append(" ");
          }
          String postagDonar = tokens[posDonar].readingWithLemma("donar").getPOSTag();
          AnalyzedToken toSynthesize = new AnalyzedToken("", postagDonar, newLemma);
          String[] synthesized = synth.synthesize(toSynthesize, postagDonar);
          if (synthesized.length > 0) {
            result.append(synthesized[0]);
          }
        } else {
          if (tokens[i].isWhitespaceBefore()) {
            result.append(" ");
          }
          result.append(tokens[i].getToken());
        }
      }
    }
    return lang.adaptSuggestion(result.toString(), "");
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
