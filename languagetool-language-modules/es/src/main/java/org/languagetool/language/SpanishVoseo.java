package org.languagetool.language;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SpanishVoseo extends Spanish {

  @Override
  public String getName() {
    return "Spanish (voseo)";
  }

  @Override
  public String[] getCountries() {
    return new String[] { "AR" //, "PA" , "UY", "CR"
    };
  }

  @Override
  public List<String> getDefaultDisabledRulesForVariant() {
    List<String> rules = Arrays.asList("VOSEO");
    return Collections.unmodifiableList(rules);
  }

}
