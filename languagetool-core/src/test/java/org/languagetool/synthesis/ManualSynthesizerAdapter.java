package org.languagetool.synthesis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import morfologik.stemming.IStemmer;
import morfologik.stemming.WordData;

/**
 *  Adapter from {@link ManualSynthesizer} to {@link Synthesizer}. <br/> 
 *  Note: It resides in "test" package because for now it is only used on unit testing.
 */
public class ManualSynthesizerAdapter extends BaseSynthesizer implements Synthesizer  {

  private final ManualSynthesizer manualSynthesizer;

  public ManualSynthesizerAdapter(ManualSynthesizer manualSynthesizer) {
    super(null, null); // no file
    this.manualSynthesizer = manualSynthesizer;
  }

  @Override
  protected void initSynthesizer() throws IOException {
    synthesizer = new IStemmer() { // null synthesiser 
      @Override
      public List<WordData> lookup(CharSequence word) {
        return new ArrayList<WordData>();
      }
    };
  }

  @Override
  protected void initPossibleTags() throws IOException {
    if (possibleTags == null) {
      possibleTags = new ArrayList<String>(manualSynthesizer.getPossibleTags());
    }
  }

  @Override
  protected void lookup(String lemma, String posTag, List<String> results) {
    super.lookup(lemma, posTag, results);
    final List<String> manualForms = manualSynthesizer.lookup(lemma.toLowerCase(), posTag);
    if (manualForms != null) {
      results.addAll(manualForms);
    }
  }

}
