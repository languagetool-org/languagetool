package org.languagetool.rules.ca;

import org.languagetool.JLanguageTool;
import org.languagetool.language.Catalan;
import org.languagetool.rules.AbstractSuppressIfAnyRuleMatchesFilter;

public class SuppressIfAnyRuleMatchesFilter extends AbstractSuppressIfAnyRuleMatchesFilter {

  @Override
  protected JLanguageTool getJLanguageTool() {
    return new Catalan().createDefaultJLanguageTool();
  }
}
