package org.languagetool.rules.ca;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.Map;

import static org.languagetool.rules.ca.PronomsFeblesHelper.*;

public class PossessiusRedundantsFilter extends RuleFilter {

  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
                                   AnalyzedTokenReadings[] patternTokens) throws IOException {
    AnalyzedTokenReadings[] tokens = match.getSentence().getTokensWithoutWhitespace();
    int posPossessive = patternTokenPos;
    while (posPossessive < tokens.length && !tokens[posPossessive].hasPartialPosTag("PX")) {
      posPossessive++;
    }
    String possessivePostag = tokens[posPossessive].readingWithTagRegex("PX.*").getPOSTag();
    String number = possessivePostag.substring(6,7);
    String persona = possessivePostag.substring(2,3);
    int posVerb = patternTokenPos - 1;
    while (posVerb > 0 && tokens[posVerb].getChunkTags().stream().anyMatch(x -> x.getChunkTag().equals("GV"))) {
      posVerb--;
    }
    posVerb++;
    boolean pronounFound = false;
    boolean hasSomePronoun = false;
    // pronom enrere
    int posPronoun = posVerb - 1;
    while (!pronounFound && posPronoun > 0 && tokens[posPronoun].hasPosTagStartingWith("P")) {
      hasSomePronoun = true;
      String pronounPostag = tokens[posPronoun].readingWithTagRegex("P.*").getPOSTag();
      pronounFound = pronounPostag.substring(2,3).equals(persona) && (number.equals("C") || pronounPostag.substring(4,5).equals(number));
      posPronoun--;
    }
    //pronom avant
    posPronoun = patternTokenPos + 1;
    while (!pronounFound && posPronoun < tokens.length && tokens[posPronoun].hasPosTagStartingWith("P")) {
      hasSomePronoun = true;
      String pronounPostag = tokens[posPronoun].readingWithTagRegex("P.*").getPOSTag();
      pronounFound = pronounPostag.substring(2,3).equals(persona) && (number.equals("C") || pronounPostag.substring(4,5).equals(number));
      posPronoun++;
    }
    // Cal apostrofar
    boolean apostropheNeeded = tokens[posPossessive-1].hasAnyPartialPosTag("DA0MS0", "DA0FS0")
      && pApostropheNeeded.matcher(tokens[posPossessive + 1].getToken()).matches();
    if (pronounFound) {
      if (apostropheNeeded) {
        match.setOffsetPosition(tokens[posPossessive-1].getStartPos(), tokens[posPossessive+1].getEndPos());
        match.setSuggestedReplacement("l'" + tokens[posPossessive+1].getToken());
      } else {
        match.setOffsetPosition(tokens[posPossessive].getStartPos(), tokens[posPossessive].getEndPos());
        match.setSuggestedReplacement("");
      }
      return match;
    }
    if (!hasSomePronoun) {
      StringBuilder suggestion = new StringBuilder();
      suggestion.append(StringTools.preserveCase(transformDavant(dativePronoun.get(persona + number), tokens[posVerb].getToken()),
        tokens[posVerb].getToken()));
      suggestion.append(tokens[posVerb].getToken().toLowerCase());
      for (int i = posVerb + 1; i <= posPossessive - 2; i++) {
        if (tokens[i].isWhitespaceBefore()) {
          suggestion.append(" ");
        }
        suggestion.append(tokens[i].getToken().toLowerCase());
      }
      if (apostropheNeeded) {
        suggestion.append(" ");
        suggestion.append("l'" + tokens[posPossessive+1].getToken());
      } else {
        for (int i = posPossessive - 1; i <= posPossessive + 1; i++) {
          if (i == posPossessive) {
            continue;
          }
          if (tokens[i].isWhitespaceBefore()) {
            suggestion.append(" ");
          }
          suggestion.append(tokens[i].getToken());
        }
      }
      match.setOffsetPosition(tokens[posVerb].getStartPos(), tokens[posPossessive + 1].getEndPos());
      match.setSuggestedReplacement(suggestion.toString());
      return match;
    }
    return null;
  }
}
