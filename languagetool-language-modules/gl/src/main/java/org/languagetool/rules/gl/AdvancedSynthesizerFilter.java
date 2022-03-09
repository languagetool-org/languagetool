/* LanguageTool, a natural language style checker 
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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

package org.languagetool.rules.gl;


import org.languagetool.language.Galician;
import org.languagetool.rules.AbstractAdvancedSynthesizerFilter;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.gl.GalicianSynthesizer;

/*
 * Synthesize suggestions using the lemma from one token (lemma_from) 
 * and the POS tag from another one (postag_from).
 * 
 * The lemma_select and postag_select attributes are required 
 * to choose one among several possible readings.
 */
public class AdvancedSynthesizerFilter extends AbstractAdvancedSynthesizerFilter {

  private final GalicianSynthesizer synth = new GalicianSynthesizer(new Galician());
  
  @Override
  protected Synthesizer getSynthesizer() {
    return synth;
  }

}