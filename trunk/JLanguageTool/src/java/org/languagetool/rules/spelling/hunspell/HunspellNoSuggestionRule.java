package org.languagetool.rules.spelling.hunspell;

import java.io.IOException;
import java.util.ResourceBundle;

import org.languagetool.Language;

/**
 * Like {@link HunspellRule}, but does not offer suggestions for incorrect words
 * as that is very slow with Hunspell.
 */
public class HunspellNoSuggestionRule extends HunspellRule {

  public static final String RULE_ID = "HUNSPELL_NO_SUGGEST_RULE";

  public HunspellNoSuggestionRule(final ResourceBundle messages, final Language language) throws UnsatisfiedLinkError, UnsupportedOperationException, IOException {
    super(messages, language);
  }

  @Override
  public String getId() {
    return RULE_ID;
  }

  @Override
  public String getDescription() {
    // TODO: make i18n after LT 1.8
    return messages.getString("desc_spelling") + " (no suggestions)";
  }

  @Override
  protected boolean offerSuggestions() {
    return false;
  }

}
