package org.languagetool.languagemodel;

import org.languagetool.rules.ngrams.Probability;

import java.util.List;

/**
 * Produces zero probability for any passed text.
 */
public class MockLanguageModel implements LanguageModel {
  @Override
  public Probability getPseudoProbability(List<String> context) {
    return new Probability(0.,0);
  }

  @Override
  public void close() {}
}
