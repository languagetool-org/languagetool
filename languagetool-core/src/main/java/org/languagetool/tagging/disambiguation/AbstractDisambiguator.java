package org.languagetool.tagging.disambiguation;

import org.languagetool.AnalyzedSentence;

/**
 * Abstract Disambiguator class to provide default (empty) implementation
 * for {@link Disambiguator#preDisambiguate(AnalyzedSentence)}.
 */
public abstract class AbstractDisambiguator implements Disambiguator {

  @Override
  public AnalyzedSentence preDisambiguate(AnalyzedSentence input) {
    return input;
  }

}
