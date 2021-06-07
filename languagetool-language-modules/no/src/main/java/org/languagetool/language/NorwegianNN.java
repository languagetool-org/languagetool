package org.languagetool.language;

import org.jetbrains.annotations.NotNull;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.rules.XmlRuleDisambiguator;
import org.languagetool.tagging.no.NorwegianNNTagger;

public class NorwegianNN extends Norwegian {

  @Override
  public String getName() {
    return "Norwegian Nynorsk";
  }

  @Override
  public String getShortCode() {
    return "nn";
  }

  @NotNull
  @Override
  public Tagger createDefaultTagger() {
    return new NorwegianNNTagger();
  }

  @Override
  public Disambiguator createDefaultDisambiguator() {
    return new XmlRuleDisambiguator(new Norwegian());
  }
}
