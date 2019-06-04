package org.languagetool.language;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.rules.uk.SimpleReplaceSpelling2019Rule;

public class Ukrainian2019 extends Ukrainian {
  @Override
  public String getVariant() {
    return "2019";
  }
  
  protected SimpleReplaceSpelling2019Rule getSpellingReplacementRule(ResourceBundle messages) throws IOException {
    return new SimpleReplaceSpelling2019Rule(messages);
  }

  @Override
  public List<String> getDefaultDisabledRulesForVariant() {
    return Arrays.asList("piv_before_iotized_1992");
  }
}
