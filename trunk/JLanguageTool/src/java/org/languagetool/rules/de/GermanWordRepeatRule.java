/*
 * Created on 03.10.2009
 */
package org.languagetool.rules.de;

import java.util.ResourceBundle;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.WordRepeatRule;

/**
 * Check if a word is repeated twice, taking into account an exception 
 * for German where e.g. "..., die die ..." is often okay.
 *   
 * @author Daniel Naber
 */
public class GermanWordRepeatRule extends WordRepeatRule {

  public GermanWordRepeatRule(final ResourceBundle messages, final Language language) {
    super(messages, language);
  }

  @Override
  public String getId() {
    return "GERMAN_WORD_REPEAT_RULE";
  }

  @Override
  public boolean ignore(final AnalyzedTokenReadings[] tokens, final int position) {
    // Don't mark error for cases like:
    // "wie Honda und Samsung, die die Bezahlung ihrer Firmenchefs..."
    if (position >= 2 && ",".equals(tokens[position - 2].getToken())) {
      return true;
    }
    return false;
  }

}
