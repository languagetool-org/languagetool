package org.languagetool.language;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BelgianFrench extends French {
  @Override
  public String getName() {
    return "French (Belgium)";
  }

  @Override
  public String[] getCountries() {
    return new String[] { "BE"
    };
  }

  @Override
  public List<String> getDefaultDisabledRulesForVariant() {
    List<String> rules = Arrays.asList("DOUBLER_UNE_CLASSE");
    return Collections.unmodifiableList(rules);
  }
}
