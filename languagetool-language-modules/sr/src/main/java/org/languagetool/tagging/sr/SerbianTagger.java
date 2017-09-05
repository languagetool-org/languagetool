package org.languagetool.tagging.sr;

import org.languagetool.tagging.BaseTagger;

import java.util.Locale;

public class SerbianTagger extends BaseTagger {

  public SerbianTagger() {
    super("/sr/serbian.dict", new Locale("sr"));
  }

  @Override
  public String getManualAdditionsFileName() {
    return "/sr/added.txt";
  }

  @Override
  public String getManualRemovalsFileName() {
    return "/sr/removed.txt";
  }
}
