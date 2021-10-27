package org.languagetool.rules;

import java.io.IOException;
import java.util.Objects;
import java.util.ResourceBundle;

import org.languagetool.AnalyzedSentence;

public class SpecificIdRule extends Rule {

  private final String id;
  private final String desc;
  
  public SpecificIdRule(String id, String desc, ResourceBundle messages, boolean isPremium) {
    super(messages);
    this.id = Objects.requireNonNull(id);
    this.desc = desc;
    this.setPremium(isPremium);
  }
  
  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getDescription() {
    return desc;
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    throw new RuntimeException("not implemented");
  }

}
