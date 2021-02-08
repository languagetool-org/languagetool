package org.languagetool.tagging.no;

import org.languagetool.tagging.BaseTagger;

import java.util.Locale;

public class NorwegianNBTagger extends BaseTagger {
  public NorwegianNBTagger() {
    super("/no/nb_NO.dict", new Locale("nb", "NO"));
  }
}
