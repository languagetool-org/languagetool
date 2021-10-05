package org.languagetool.rules.de;

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.language.German;
import org.languagetool.language.GermanyGerman;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class NumberInWordFilter extends RuleFilter {

  private final German language = new GermanyGerman();
  private JLanguageTool lt;

  @Nullable
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos, AnalyzedTokenReadings[] patternTokens) throws IOException {
    initLt();
    String word = arguments.get("word");
    Pattern typoPattern = Pattern.compile("[0-9]");
    String wordWithoutNumberCharacter = typoPattern.matcher(word).replaceAll("");

    List<String> replacements = new ArrayList<>();
    List<RuleMatch> matches = lt.check(wordWithoutNumberCharacter);
    if (matches.isEmpty()) {
      replacements.add(wordWithoutNumberCharacter);
    } else {
      replacements.addAll(matches.get(0).getSuggestedReplacements());
    }
    if (!replacements.isEmpty()) {
      RuleMatch ruleMatch = new RuleMatch(match.getRule(), match.getSentence(), match.getFromPos(), match.getToPos(),
        match.getMessage(), match.getShortMessage());
      ruleMatch.setType(match.getType());
      ruleMatch.setSuggestedReplacements(replacements);
      return ruleMatch;
    }
    return null;
  }

  private void initLt() {
    if (lt == null) {
      lt = new JLanguageTool(language);
      for (Rule rule : lt.getAllActiveRules()) {
        if (!rule.getId().equals("GERMAN_SPELLER_RULE")) {
          lt.disableRule(rule.getId());
        }
      }
    }
  }
}
