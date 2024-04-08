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

  // Used when we we have a pronoun like "eles", that is frequently used as a direct object, but we need it to be
  // "os", which is tagged as the accusative form of "eles".
  private String convertPronounToAccusative(String pronounTag) {
    if (pronounTag.endsWith("N00")) {
      return pronounTag.substring(0, pronounTag.length() - 3) + "A00";
    }
    return pronounTag;
  }

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
                                   AnalyzedTokenReadings[] patternTokens, List<Integer> tokenPositions) throws IOException {
    int verbPos = Integer.parseInt(arguments.get("verbPos"));
    int pronounPos = Integer.parseInt(arguments.get("pronounPos"));
    boolean convertToAccusative = Boolean.parseBoolean(arguments.get("convertToAccusative"));
    AnalyzedTokenReadings verbStemTokenReadings = patternTokens[verbPos];
    AnalyzedTokenReadings pronounTokenReadings = patternTokens[pronounPos];
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
    if (convertToAccusative) {
      pronounTag = convertPronounToAccusative(pronounTag);
    }
    List<String> suggestions = new ArrayList<>(Collections.emptyList());
    boolean isTitleCase = StringTools.isCapitalizedWord(verbStemTokenReadings.getToken());
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
