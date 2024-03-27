package org.languagetool.rules.pt;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.synthesis.pt.PortugueseSynthesizer;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.*;

public class PortugueseEnclisisFilter extends RuleFilter {
  protected PortugueseSynthesizer getSynthesizer() {
    return PortugueseSynthesizer.INSTANCE;
  }

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
                                   AnalyzedTokenReadings[] patternTokens, List<Integer> tokenPositions) throws IOException {
    AnalyzedTokenReadings verbStemTokenReadings = patternTokens[0];
    AnalyzedTokenReadings pronounTokenReadings = patternTokens[2];
    String pronounTag = null;
    for (AnalyzedToken at : pronounTokenReadings.getReadings()) {
      String posTag = at.getPOSTag();
      if (posTag != null && posTag.startsWith("PP")) {
        pronounTag = posTag;
        break;
      }
    }
    if (pronounTag == null) {
      return null;
    }
    List<String> suggestions = new ArrayList<>(Collections.emptyList());
    boolean isTitleCase = StringTools.startsWithUppercase(verbStemTokenReadings.getToken());
    boolean isAllCaps = StringTools.isAllUppercase(verbStemTokenReadings.getToken());
    for (AnalyzedToken at : verbStemTokenReadings.getReadings()) {
      String posTag = at.getPOSTag();
      if (posTag != null && posTag.startsWith("V")) {
        String enclisisTag = posTag + ":" + pronounTag;
        String[] forms = getSynthesizer().synthesize(at, enclisisTag);
        for (String form : forms) {
          if (isTitleCase) {
            form = StringTools.uppercaseFirstChar(form);
          } else if (isAllCaps) {
            form = form.toUpperCase();
          }
          suggestions.add(form);
        }
        break;
      }
    }
    match.setSuggestedReplacements(suggestions);
    return match;
  }
}
