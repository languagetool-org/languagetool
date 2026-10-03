package org.languagetool.rules.tl;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.SpellingCheckRule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class PangVowelWordRule extends Rule {

  private final Language language;

  public PangVowelWordRule(ResourceBundle messages, Language language) {
    super(messages);
    this.language = language;
    setLocQualityIssueType(ITSIssueType.Misspelling);
  }

  @Override
  public String getId() {
    return "TL_PANG_VOWEL_WORD";
  }

  @Override
  public String getDescription() {
    return "Possible forms for 'pang' before a vowel-starting word";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    List<RuleMatch> matches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();

    for (int i = 1; i < tokens.length - 1; i++) {
      AnalyzedTokenReadings token1 = tokens[i];
      AnalyzedTokenReadings token2 = tokens[i + 1];

      if (!"pang".equalsIgnoreCase(token1.getToken()) || token2.isNonWord()) {
        continue;
      }
      String second = token2.getToken();
      if (second.isEmpty() || !startsWithVowel(second)) {
        continue;
      }

      String hyphenated = token1.getToken() + "-" + second;
      String concatenated = token1.getToken() + second;

      RuleMatch match = new RuleMatch(
          this, sentence, token1.getStartPos(), token2.getEndPos(),
          messages.getString("pang_vowel_word")
      );
      match.addSuggestedReplacement(hyphenated);

      if (isValidWord(concatenated)) {
        match.addSuggestedReplacement(concatenated);
      }

      matches.add(match);
      i++;
    }

    return matches.toArray(new RuleMatch[0]);
  }

  private boolean startsWithVowel(String token) {
    char firstChar = Character.toLowerCase(token.charAt(0));
    return firstChar == 'a' || firstChar == 'e' || firstChar == 'i' || firstChar == 'o' || firstChar == 'u';
  }

  private boolean isValidWord(String word) throws IOException {
    SpellingCheckRule spellingRule = language.getDefaultSpellingRule();
    return spellingRule != null && !spellingRule.isMisspelled(word);
  }

}
