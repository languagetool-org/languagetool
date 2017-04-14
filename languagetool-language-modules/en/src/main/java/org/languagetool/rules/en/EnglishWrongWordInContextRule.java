package org.languagetool.rules.en;

import java.util.ResourceBundle;

import org.languagetool.rules.Example;
import org.languagetool.rules.WrongWordInContextRule;

public class EnglishWrongWordInContextRule extends WrongWordInContextRule {

  public EnglishWrongWordInContextRule(ResourceBundle messages) {
    super(messages);
    addExamplePair(Example.wrong("I have <marker>proscribed</marker> you a course of antibiotics."),
                   Example.fixed("I have <marker>prescribed</marker> you a course of antibiotics."));
  }
  
  @Override
  protected String getCategoryString() {
    return "Easily confused words";
  }
  
  @Override
  public String getId() {
    return "ENGLISH_WRONG_WORD_IN_CONTEXT";
  }
  
  @Override
  public String getDescription() {
    return "confused words (proscribe/prescribe, dual/duel etc.)";
  }
  
  @Override
  protected String getFilename() {
    return "/en/wrongWordInContext.txt";
  }
  
  @Override
  protected String getMessageString() {
    return "Possibly confused word: Did you mean <suggestion>$SUGGESTION</suggestion> instead of '$WRONGWORD'?";
  }
  
  @Override
  protected String getShortMessageString() {
    return "Possibly confused word";
  }
  
  @Override
  protected String getLongMessageString() {
    return "Possibly confused word: Did you mean <suggestion>$SUGGESTION</suggestion> (= $EXPLANATION_SUGGESTION) instead of '$WRONGWORD' (= $EXPLANATION_WRONGWORD)?";
  }
}
