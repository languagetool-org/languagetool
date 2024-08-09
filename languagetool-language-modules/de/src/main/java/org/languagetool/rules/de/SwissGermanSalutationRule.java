package org.languagetool.rules.de;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.Rule;
import org.languagetool.rules.Category;
import org.languagetool.rules.CategoryId;

public class SwissGermanSalutationRule extends Rule {
  // Update the salutations list as needed, consider that these elements can include regex patterns.
  private static final String[] SALUTATIONS = {
    "Hallo\\s+([A-Za-z]+)?",
    "Hey\\s+([A-Za-z]+)?",
    "Hi\\s+([A-Za-z]+)?",
    "Greetings",
    "Guten\\s+(Morgen|Mittag|Abend)",
    "Sehr geehrter?\\s+(Frau|Herr|Professor|Doktor)\\s+[A-Za-z]+"
  };

  private static final String SALUTATIONS_REGEX = String.join("|", SALUTATIONS);
  private static final Pattern SALUTATION_PATTERN = Pattern.compile(
    "(?i)\\b(?:"
      + SALUTATIONS_REGEX
      + ")\\b,\\n"
  );

  private static final Category PUNCTUATION_CATEGORY = new Category(CategoryId.PUNCTUATION, "Punctuation");

  public SwissGermanSalutationRule(ResourceBundle messages) {
    super(messages);
    setCategory(PUNCTUATION_CATEGORY); // Set the PUNCTUATION category
  }

  @Override
  public String getId() {
    return "SWISS_GERMAN_SALUTATION_RULE";
  }

  @Override
  public String getDescription() {
    return "Detects salutations followed by a comma and a newline, and suggests to remove the comma";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    String sentenceText = sentence.getText();
    Matcher matcher = SALUTATION_PATTERN.matcher(sentenceText);

    while (matcher.find()) {
      int start = matcher.start(0);
      int end = matcher.end(0);
      String matchedText = matcher.group();

      // Remove the comma before the newline
      String correctedText = matchedText.replaceFirst(",\\n", "\n");
      RuleMatch ruleMatch = new RuleMatch(this, sentence, start, end,
        "The salutation should not be followed by a comma before a newline.",
        "Remove the comma");
      ruleMatch.setSuggestedReplacements(Arrays.asList(correctedText));
      ruleMatches.add(ruleMatch);
    }
    return ruleMatches.toArray(new RuleMatch[0]);
  }
}