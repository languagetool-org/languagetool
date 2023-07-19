package org.languagetool.language;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CanadianFrench extends French {
  @Override
  public String getName() {
    return "French (Canada)";
  }

  @Override
  public String[] getCountries() {
    return new String[] { "CA"
    };
  }

  @Override
  public List<String> getDefaultDisabledRulesForVariant() {
    List<String> rules = Arrays.asList("DOUBLER_UNE_CLASSE");
    return Collections.unmodifiableList(rules);
  }
}
