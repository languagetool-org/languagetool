package org.languagetool.rules.de;

import org.languagetool.rules.DoublePunctuationRule;

import java.util.ResourceBundle;

/**
 * Double punctuation rule with German-specific error message.
 */
public class GermanDoublePunctuationRule extends DoublePunctuationRule {
  
  public GermanDoublePunctuationRule(final ResourceBundle messages) {
    super(messages);
  }
  
  @Override
  public final String getId() {
    return "DE_DOUBLE_PUNCTUATION";
  }

  @Override
  protected String getDotMessage() {
    return "Zwei aufeinander folgende Punkte. Auch wenn ein Satz mit einer Abkürzung endet, " +
            "endet er nur mit einem Punkt (§103 Regelwerk).";
  }
    
}
