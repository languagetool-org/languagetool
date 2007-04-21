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
package de.danielnaber.languagetool.synthesis.en;

import java.io.IOException;

import com.dawidweiss.stemmers.Lametyzator;

import de.danielnaber.languagetool.synthesis.Synthesizer;

/** English word form synthesizer.
 * Based on part-of-speech lists in Public Domain.
 * see readme.txt for details, the POS tagset is
 * described in tagset.txt
 * 
 * @author Marcin Milkowski
 */


public class EnglishSynthesizer implements Synthesizer {

  private static final String RESOURCE_FILENAME = "/resource/en/english_synth.dict";

  private Lametyzator synthesizer = null;

  
  public String[] synthesize(String lemma, String posTag) throws IOException {
    if (synthesizer == null) {
      synthesizer = 
        new Lametyzator(this.getClass().getResourceAsStream(RESOURCE_FILENAME),
          "iso8859-1", '+');
    }
    String[] wordForms = null;
    wordForms = synthesizer.stem(lemma + "|" + posTag);
    return wordForms;
  }

  public String[] synthesize(String lemma, String posTag, boolean posTagRegExp)
      throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

}
