package org.languagetool.synthesis.sr;

import org.languagetool.synthesis.BaseSynthesizer;

/**
 * Ukrainian word form synthesizer
 *
 * @author Zoltan Csala
 */
public class SerbianSynthesizer extends BaseSynthesizer {

  private static final String RESOURCE_FILENAME = "/sr/serbian_synth.dict";
  private static final String TAGS_FILE_NAME = "/sr/serbian_synth_tags.txt";

  public SerbianSynthesizer() {
    super(RESOURCE_FILENAME, TAGS_FILE_NAME);
  }
}
