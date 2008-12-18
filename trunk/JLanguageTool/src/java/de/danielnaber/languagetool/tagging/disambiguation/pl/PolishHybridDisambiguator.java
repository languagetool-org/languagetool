package de.danielnaber.languagetool.tagging.disambiguation.pl;

import java.io.IOException;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.tagging.disambiguation.Disambiguator;
import de.danielnaber.languagetool.tagging.disambiguation.rules.pl.PolishRuleDisambiguator;

/**
 * Hybrid chunker-disambiguator for Polish. 
 * 
 * @author Marcin Miłkowski
 */

public class PolishHybridDisambiguator implements Disambiguator {


  private Disambiguator chunker = new PolishChunker();
  private Disambiguator disambiguator = new PolishRuleDisambiguator();
  
  /** 
   * Calls two disambiguator classes: (1) a chunker;
   * (2) a rule-based disambiguator.
   */
  @Override
  public AnalyzedSentence disambiguate(AnalyzedSentence input)
  throws IOException {
    return disambiguator.disambiguate
    ((chunker.disambiguate(input)));
  }

}
