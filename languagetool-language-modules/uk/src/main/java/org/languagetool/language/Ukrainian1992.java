package org.languagetool.language;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.rules.uk.SimpleReplaceSpelling2019Rule;


// Only for testing for now
public class Ukrainian1992 extends Ukrainian {
  @Override
  public String getVariant() {
    return "1992";
  }

  @Override
  public String getName() {
    return "Ukrainian (1992)";
  }

  protected SimpleReplaceSpelling2019Rule getSpellingReplacementRule(ResourceBundle messages) throws IOException {
    return new SimpleReplaceSpelling2019Rule(messages);
  }

  @Override
  public List<String> getDefaultDisabledRulesForVariant() {
    return Arrays.asList("piv_okremo_2019");
  }
}
