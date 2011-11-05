// LanguageTool, a natural language style checker 

package org.languagetool.rules.km;

import org.languagetool.Language;
import org.languagetool.rules.GenericUnpairedBracketsRule;

import java.util.ResourceBundle;

public class KhmerUnpairedBracketsRule extends GenericUnpairedBracketsRule {
  
  private static final String[] KM_START_SYMBOLS = { "[", "(", "{", "“", "\"", "'", "«" };
  private static final String[] KM_END_SYMBOLS   = { "]", ")", "}", "”", "\"", "'", "»" };
  
  public KhmerUnpairedBracketsRule(final ResourceBundle messages, final Language language) {
    super(messages, language);
    startSymbols = KM_START_SYMBOLS;
    endSymbols = KM_END_SYMBOLS;
    uniqueMapInit();
  }

  @Override
  public String getId() {
    return "KM_UNPAIRED_BRACKETS";
  }
}
