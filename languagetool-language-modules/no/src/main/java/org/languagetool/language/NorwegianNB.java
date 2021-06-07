package org.languagetool.language;

import org.jetbrains.annotations.NotNull;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.rules.XmlRuleDisambiguator;
import org.languagetool.tagging.no.NorwegianNBTagger;

public class NorwegianNB extends Norwegian {

  @Override
  public String getName() {
    return "Norwegian Bokmål";
  }

  @Override
  public String getShortCode() {
    return "nb";
  }

  @NotNull
  @Override
  public Tagger createDefaultTagger() {
    return new NorwegianNBTagger();
  }

  @Override
  public Disambiguator createDefaultDisambiguator() {
    return new XmlRuleDisambiguator(new Norwegian());
  }
}
