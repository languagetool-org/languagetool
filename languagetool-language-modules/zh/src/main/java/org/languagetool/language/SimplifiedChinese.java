package org.languagetool.language;

import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tokenizers.zh.SChineseWordTokenizer;


public class SimplifiedChinese extends Chinese {

  private Tokenizer wordTokenizer;

  @Override
  public String getName() {
      return "Chinese (Simplified)";
  }

  @Override
  public String[] getCountries() {
    return  new String[]{"CN"};
  }

}
