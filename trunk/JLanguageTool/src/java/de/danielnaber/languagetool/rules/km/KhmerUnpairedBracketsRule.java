// LanguageTool, a natural language style checker 

package de.danielnaber.languagetool.rules.km;

import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.GenericUnpairedBracketsRule;

import java.util.ResourceBundle;

public class KhmerUnpairedBracketsRule extends GenericUnpairedBracketsRule {
  
  private static final String[] KM_START_SYMBOLS = { "[", "(", "{", "â€œ", "\"", "'", "«" };
  private static final String[] KM_END_SYMBOLS   = { "]", ")", "}", "â€�", "\"", "'", "»" };
  
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
