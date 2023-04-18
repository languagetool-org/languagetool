package org.languagetool.rules.uk;

import java.util.ResourceBundle;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.CorrectExample;
import org.languagetool.rules.IncorrectExample;
import org.languagetool.rules.UppercaseSentenceStartRule;

public class UkrainianUppercaseSentenceStartRule extends UppercaseSentenceStartRule {

  public UkrainianUppercaseSentenceStartRule(ResourceBundle messages, Language language, IncorrectExample incorrectExample, CorrectExample correctExample) {
    super(messages, language, incorrectExample, correctExample);
  }

  @Override
  protected boolean isException(AnalyzedTokenReadings[] tokens, int tokenIdx) {
    // list, e.g. а) б) в)
    if( tokenIdx == 1 && tokenIdx < tokens.length-1
        && tokens[tokenIdx].getCleanToken().matches("[а-яіїєґ]") 
        && tokens[tokenIdx+1].getToken().equals(")") )
      return true;
    
    return false;
  }

}
