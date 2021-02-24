package org.languagetool.rules.uk;

import java.util.ResourceBundle;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.CommaWhitespaceRule;
import org.languagetool.rules.CorrectExample;
import org.languagetool.rules.IncorrectExample;

public class UkrainianCommaWhitespaceRule extends CommaWhitespaceRule {

  public UkrainianCommaWhitespaceRule(ResourceBundle messages, IncorrectExample incorrectExample, CorrectExample correctExample) {
    super(messages, incorrectExample, correctExample);
  }

  @Override
  protected boolean isException(AnalyzedTokenReadings[] tokens, int tokenIdx) {
    String token = tokens[tokenIdx].getToken();
    if( "\u2014".equals(token) || "\u2013".equals(token) )
      return true;

    return super.isException(tokens, tokenIdx);
  }
  
}
