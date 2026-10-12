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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReduplicationRule extends Rule {

  private final Language language;
  private static final Pattern REDUP = Pattern.compile("^(.+?)\\1$");

  public ReduplicationRule(ResourceBundle messages, Language language) {
    super(messages);
    this.language = language;
    setLocQualityIssueType(ITSIssueType.Misspelling);
  }

  @Override
  public String getId() {
    return "TL_REDUPLICATION";
  }

  @Override
  public String getDescription() {
    return "Suggest hyphenation for reduplicated words";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    List<RuleMatch> matches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();

    for (int i = 1; i < tokens.length; i++) {
      AnalyzedTokenReadings atr = tokens[i];
      if (atr.isNonWord()) {
        continue;
      }
      String token = atr.getToken();
      if (token.length() < 4) {
        continue;
      }
      Matcher m = REDUP.matcher(token);
      if (!m.matches()) {
        continue;
      }
      String part = m.group(1);
      String hyphenated = part + "-" + part;

      if (isValidWord(token) || !isValidWord(part)) {
        continue;
      }

      RuleMatch match = new RuleMatch(this, sentence, atr.getStartPos(), atr.getEndPos(),
          messages.getString("reduplication"));
      match.addSuggestedReplacement(hyphenated);
      matches.add(match);
    }

    return matches.toArray(new RuleMatch[0]);
  }

  private boolean isValidWord(String word) throws IOException {
    SpellingCheckRule spellingRule = language.getDefaultSpellingRule();
    return spellingRule != null && !spellingRule.isMisspelled(word);
  }

}
