package org.languagetool.tagging.no;

import org.languagetool.tagging.BaseTagger;

import java.util.Locale;

public class NorwegianTagger extends BaseTagger {
  public NorwegianTagger() {
    super("/no/norwegian.dict", new Locale("no"));
  }
}
