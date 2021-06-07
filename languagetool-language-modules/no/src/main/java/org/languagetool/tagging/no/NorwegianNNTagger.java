package org.languagetool.tagging.no;

import org.languagetool.tagging.BaseTagger;

import java.util.Locale;

public class NorwegianNNTagger extends BaseTagger {
  public NorwegianNNTagger() {
    super("/no/nn_NO.dict", new Locale("nn", "NO"));
  }
}
