/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Ionuț Păduraru
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
package org.languagetool.synthesis;

import morfologik.stemming.IStemmer;
import org.languagetool.Language;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *  Adapter from {@link ManualSynthesizer} to {@link Synthesizer}. 
 *  Note: It resides in "test" package because for now it is only used on unit testing.
 */
public class ManualSynthesizerAdapter extends BaseSynthesizer {

  private final ManualSynthesizer manualSynthesizer;

  public ManualSynthesizerAdapter(ManualSynthesizer manualSynthesizer, Language lang) {
    super(null, null, lang.getShortCode()); // no file
    this.manualSynthesizer = manualSynthesizer;
  }

  @Override
  protected IStemmer createStemmer() {
    return word -> Collections.emptyList();
  }
  
  @Override
  protected void initPossibleTags() throws IOException {
    if (possibleTags == null) {
      possibleTags = new ArrayList<>(manualSynthesizer.getPossibleTags());
    }
  }

  @Override
  protected List<String> lookup(String lemma, String posTag) {
    List<String> results = super.lookup(lemma, posTag);
    List<String> manualForms = manualSynthesizer.lookup(lemma.toLowerCase(), posTag);
    if (manualForms != null) {
      results.addAll(manualForms);
    }
    return results;
  }

}
