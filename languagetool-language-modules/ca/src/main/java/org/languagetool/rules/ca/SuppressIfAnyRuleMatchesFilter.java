package org.languagetool.rules.ca;

import org.languagetool.JLanguageTool;
import org.languagetool.language.Catalan;
import org.languagetool.rules.AbstractSuppressIfAnyRuleMatchesFilter;

public class SuppressIfAnyRuleMatchesFilter extends AbstractSuppressIfAnyRuleMatchesFilter {

  private static final JLanguageTool lt = new JLanguageTool(new Catalan());

  @Override
  protected JLanguageTool getLT() {
    return lt;
  }
}
