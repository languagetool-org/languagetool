package org.languagetool.language;

import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tokenizers.zh.TChinsesWordTokenizer;

public class TraditionalChinese extends Chinese{

  private Tokenizer wordTokenizer;

  @Override
  public String getName() {
    return "Chinese (Traditional)";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"TW"};
  }

//  @Override
//  public Tokenizer getWordTokenizer() {
//    if (wordTokenizer == null) {
//      wordTokenizer = new TChinsesWordTokenizer();
//    }
//    return wordTokenizer;
//  }

}
