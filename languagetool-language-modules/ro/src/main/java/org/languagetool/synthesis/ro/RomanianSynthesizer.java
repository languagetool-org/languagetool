/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.synthesis.ro;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.languagetool.JLanguageTool;
import org.languagetool.synthesis.BaseSynthesizer;
import org.languagetool.synthesis.ManualSynthesizer;

/**
 * Romanian word form synthesizer.
 *
 * @author Ionuț Păduraru
 */
public class RomanianSynthesizer extends BaseSynthesizer {

  private static final String RESOURCE_FILENAME = "/ro/romanian_synth.dict";
  private static final String TAGS_FILE_NAME = "/ro/romanian_tags.txt";
  private static final String USER_DICT_FILENAME = "/ro/added.txt";

  private static ManualSynthesizer manualSynthesizer;

  public RomanianSynthesizer() {
    super(RESOURCE_FILENAME, TAGS_FILE_NAME);
  }

  @Override
  protected void lookup(String lemma, String posTag, List<String> results) {
    super.lookup(lemma, posTag, results);
    initSynth();
    // add words that are missing from the romanian_synth.dict file
    final List<String> manualForms = manualSynthesizer.lookup(lemma, posTag);
    if (manualForms != null) {
      results.addAll(manualForms);
    }
  }

  @Override
  protected void initPossibleTags() throws IOException {
    super.initPossibleTags();
    initSynth();
    // add any possible tag from manual synthesiser
    for (String tag : manualSynthesizer.getPossibleTags()) {
      if (!possibleTags.contains(tag)) {
        possibleTags.add(tag);
      }
    }
  }

  private synchronized void initSynth() {
    if (manualSynthesizer == null) {
      try {
        try (InputStream stream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(USER_DICT_FILENAME)) {
          manualSynthesizer = new ManualSynthesizer(stream);
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
