package org.languagetool.rules.spelling.hunspell;

import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.Language;

/**
 * Like {@link HunspellRule}, but does not offer suggestions for incorrect words
 * as that is very slow with Hunspell.
 */
public class HunspellNoSuggestionRule extends HunspellRule {

  public static final String RULE_ID = "HUNSPELL_NO_SUGGEST_RULE";

  public HunspellNoSuggestionRule(final ResourceBundle messages, final Language language) {
    super(messages, language);
  }

  @Override
  public String getId() {
    return RULE_ID;
  }

  @Override
  public String getDescription() {
    return messages.getString("desc_spelling_no_suggestions");
  }

  @Override
  public List<String> getSuggestions(String word) throws IOException {
    return null;
  }
  
}
