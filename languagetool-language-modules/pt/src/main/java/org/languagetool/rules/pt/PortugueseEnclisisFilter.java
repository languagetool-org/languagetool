package org.languagetool.rules.pt;

import org.jetbrains.annotations.NotNull;
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

  // Extract pronoun tags from pronoun tokens
  // We cannot just get all tags uncritically because of the 'nos' issue.
  private List<String> getPronounTags(AnalyzedTokenReadings pronounReadings, String verbText,
                                      boolean convertToAccusative) {
    List<String> pronounTags = new ArrayList<>(Collections.emptyList());
    for (AnalyzedToken pronounToken : pronounReadings) {
      String pronounText = pronounToken.getToken();
      if (pronounText.equals("nos")) {
        pronounTags.add("PP1CPO00");
        if (verbText.endsWith("m") || verbText.endsWith("ão") || verbText.endsWith("õe")) {
          pronounTags.add("PP3MPA00");
        }
        break;
      }
      String posTag = pronounToken.getPOSTag();
      if (posTag != null && posTag.startsWith("PP")) {
        if (convertToAccusative) {
          posTag = convertPronounToAccusative(posTag);
        }
        pronounTags.add(posTag);
      }
    }
    return pronounTags;
  }

  @NotNull
  private ArrayList<String> getVerbForms(AnalyzedTokenReadings verbStemTokenReadings, List<String> pronounTags) throws IOException {
    HashSet<String> suggestions = new HashSet<>(Collections.emptyList());
    boolean isTitleCase = StringTools.isCapitalizedWord(verbStemTokenReadings.getToken());
    boolean isAllCaps = StringTools.isAllUppercase(verbStemTokenReadings.getToken());
    for (AnalyzedToken at : verbStemTokenReadings.getReadings()) {
      String posTag = at.getPOSTag();
      if (posTag != null && posTag.startsWith("V")) {
        for (String pronounTag : pronounTags) {
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
        }
        break;
      }
    }
    return new ArrayList<>(suggestions);
  }

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
                                   AnalyzedTokenReadings[] patternTokens, List<Integer> tokenPositions) throws IOException {
    int verbPos = Integer.parseInt(arguments.get("verbPos"));
    int pronounPos = Integer.parseInt(arguments.get("pronounPos"));
    boolean convertToAccusative = Boolean.parseBoolean(arguments.get("convertToAccusative"));
    AnalyzedTokenReadings verbStemTokenReadings = patternTokens[verbPos];
    AnalyzedTokenReadings pronounTokenReadings = patternTokens[pronounPos];
    List<String> pronounTags = getPronounTags(pronounTokenReadings, verbStemTokenReadings.getToken(),
      convertToAccusative);
    if (pronounTags.isEmpty()) {
      return null;
    }
    match.setSuggestedReplacements(getVerbForms(verbStemTokenReadings, pronounTags));
    return match;
  }
}
