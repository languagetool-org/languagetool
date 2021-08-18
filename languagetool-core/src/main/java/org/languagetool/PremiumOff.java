/* LanguageTool, a natural language style checker
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
 * All rights reserved - not part of the Open Source edition
 */
package org.languagetool;

import org.languagetool.rules.Rule;

import java.util.HashSet;
import java.util.Set;

/**
 * Information about premium-only rules.
 */
public class PremiumOff extends Premium {
  
  @Override
  public boolean isPremiumRule(Rule rule) {
    return false;
  }

  @Override
  public Set<String> getPremiumRuleIds() {
    return new HashSet<>();
  }

}
