package org.languagetool.rules.ca;

import org.jspecify.annotations.Nullable;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.ca.VerbSynthesizer;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class EnNoInfinitiuSuggestionFilter extends RuleFilter {

  final List<String> tempsVerbalsPresent = Arrays.asList("IP", "IF");

  @Override
  public @Nullable RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
                                             AnalyzedTokenReadings[] patternTokens, List<Integer> tokenPositions) throws IOException {
    Synthesizer synth = getSynthesizerFromRuleMatch(match);
    int posWord = 0;
    AnalyzedTokenReadings[] tokens = match.getSentence().getTokensWithoutWhitespace();
    while (posWord < tokens.length
      && (tokens[posWord].getStartPos() < match.getFromPos() || tokens[posWord].isSentenceStart())) {
      posWord++;
    }
    VerbSynthesizer verbSynthInfinitiu = new VerbSynthesizer(tokens, posWord, getLanguageFromRuleMatch(match));
    posWord = verbSynthInfinitiu.getLastIndex() + 1;
    VerbSynthesizer verbAfter = new VerbSynthesizer(tokens, posWord, getLanguageFromRuleMatch(match));
    VerbSynthesizer verbBefore = new VerbSynthesizer(tokens, verbSynthInfinitiu.getFirstVerbIndex() - 1,
      getLanguageFromRuleMatch(match), true);
    // verb found out of bounds
    if (verbSynthInfinitiu.isUndefined() || tokens[verbSynthInfinitiu.getLastVerbIndex()].getEndPos() > match.getToPos()) {
      return null;
    }
    String postagTempsVerbal = "";
    boolean isPassatPerifrastic = false;
    //boolean isPerfet = false;
    String verbAfterPostag = verbAfter.getFirstVerbISPostag();
    String verbBeforePostag = verbBefore.getFirstVerbISPostag();
    if (!verbAfter.isUndefined() && verbAfterPostag != null) {
      postagTempsVerbal = verbAfterPostag;
      isPassatPerifrastic = verbAfter.isPassatPerifrastic();
      //isPerfet = verbAfter.isPerfet();
    } else if (!verbBefore.isUndefined() && verbBeforePostag != null) {
      postagTempsVerbal = verbBeforePostag;
      isPassatPerifrastic = verbBefore.isPassatPerifrastic();
      //isPerfet = verbBefore.isPerfet();
    } else {
      return null;
    }
    if (postagTempsVerbal == null) {
      return null;
    }
    // caldria incloure sempre l'oció 3S si no hi és.
    String postagPrefix = "";
    if (tempsVerbalsPresent.contains(postagTempsVerbal.substring(2, 4)) && !isPassatPerifrastic) {
      postagPrefix = "VMIP";
    } else {
      postagPrefix = "VMII";
    }
    List<String> synthVerbs = new ArrayList<>();
    if (!postagTempsVerbal.substring(4, 6).equals("3S")) {
      verbSynthInfinitiu.setPostag(postagPrefix + "3S" + postagTempsVerbal.substring(6));
      synthVerbs.add(verbSynthInfinitiu.synthesize());
    }
    verbSynthInfinitiu.setPostag(postagPrefix + postagTempsVerbal.substring(4));
    synthVerbs.add(verbSynthInfinitiu.synthesize());
    int startPos = verbSynthInfinitiu.getFirstVerbIndex() - 2;
    if (tokens[startPos].getToken().equalsIgnoreCase("l")) {
      startPos--;
    }
    int endPos = verbSynthInfinitiu.getLastIndex();
    String originalStr = match.getSentence().getText().substring(tokens[startPos].getStartPos(),
      tokens[endPos].getEndPos());
    List<String> suggestions = new ArrayList<>();
    for (String synthVerb : synthVerbs) {
      StringBuilder suggestion = new StringBuilder();
      if (!verbBefore.isUndefined() && verbBeforePostag != null) {
        suggestion.append("perquè no ");
      } else {
        suggestion.append("com que no ");
      }
      String pronounsAfter = verbSynthInfinitiu.getPronounsStrAfter();
      if (!pronounsAfter.isEmpty()) {
        suggestion.append(PronomsFeblesHelper.transformDavant(pronounsAfter, synthVerb));
      }
      suggestion.append(synthVerb);
      String suggestionStr = StringTools.preserveCase(suggestion.toString(), originalStr);
      if (!suggestions.contains(suggestionStr)) {
        suggestions.add(suggestionStr);
      }
    }
    if (suggestions.isEmpty()) {
      return null;
    }
    RuleMatch ruleMatch = new RuleMatch(match.getRule(), match.getSentence(), tokens[startPos].getStartPos(),
      tokens[endPos].getEndPos(), match.getMessage(), match.getShortMessage());
    ruleMatch.setType(match.getType());
    ruleMatch.setSuggestedReplacements(suggestions);
    return ruleMatch;
  }
}
