package org.languagetool.rules.pt;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.synthesis.pt.PortugueseSynthesizer;

import java.io.IOException;
import java.util.*;

public class PortugueseProclisisFilter extends RuleFilter {
  protected PortugueseSynthesizer getSynthesizer() {
    return PortugueseSynthesizer.INSTANCE;
  }

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
                                   AnalyzedTokenReadings[] patternTokens, List<Integer> tokenPositions) throws IOException {
    AnalyzedTokenReadings encliticVerbTokenReadings = patternTokens[patternTokens.length - 1];
    HashSet<String> suggestions = new HashSet<>(Collections.emptyList());
    for (AnalyzedToken at : encliticVerbTokenReadings.getReadings()) {
      String posTag = at.getPOSTag();
      if (posTag == null || (!posTag.startsWith("V") && !posTag.contains(":"))) {
        continue;
      }
      String oldToken = at.getToken();
      String[] tagParts = posTag.split(":");
      String verbTag = tagParts[0];
      String newVerb = getSynthesizer().synthesize(at, verbTag)[0];
      String[] oldTokenParts = oldToken.split("-");
      // Includes only the relevant part of the verb; i.e. for mesoclitics, we ignore the ending
      String oldVerb = oldTokenParts[0];
      String oldPronoun = oldTokenParts[1];
      List<String> newPronounForms = new ArrayList<>(Collections.emptyList());
      switch (oldPronoun) {
        case "lo":
        case "no":
          newPronounForms.add("o");
          break;
        case "la":
        case "na":
          newPronounForms.add("a");
          break;
        case "los":
          newPronounForms.add("os");
          break;
        case "las":
        case "nas":
          newPronounForms.add("as");
          break;
        case "nos":
          newPronounForms.add("nos");
          if (oldVerb.endsWith("m") || oldVerb.endsWith("ão") || oldVerb.endsWith("õe")) {
            newPronounForms.add("os");
          }
          break;
        default:
          newPronounForms.add(oldPronoun);
      }
      for (String newPronoun : newPronounForms) {
        String suggestion = newPronoun + " " + newVerb;
        suggestions.add(suggestion);
      }
    }
    match.setSuggestedReplacements(new ArrayList<>(suggestions));
    return match;
  }
}
