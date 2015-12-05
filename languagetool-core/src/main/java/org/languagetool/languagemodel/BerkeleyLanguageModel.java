/* LanguageTool, a natural language style checker 
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.languagemodel;

import edu.berkeley.nlp.lm.StupidBackoffLm;
import edu.berkeley.nlp.lm.io.LmReaders;
import org.languagetool.Experimental;
import org.languagetool.rules.ngrams.Probability;

import java.io.File;
import java.util.List;

/**
 * The BerkeleyLM language model.
 * Can be used with data from e.g. http://tomato.banatao.berkeley.edu:8080/berkeleylm_binaries/
 * @since 3.2
 */
@Experimental
public class BerkeleyLanguageModel implements LanguageModel {

  private final StupidBackoffLm<String> lm;
          
  public BerkeleyLanguageModel(File berkeleyLm) {
    if (!berkeleyLm.isFile()) {
      throw new RuntimeException("You need to specify a BerkeleyLM file (*.blm.gz): " + berkeleyLm);
    }
    File vocabFile = new File(berkeleyLm.getParent(), "vocab_cs.gz");
    if (!vocabFile.exists()) {
      throw new RuntimeException("No vocabulary file 'vocab_cs.gz' found in the BerkeleyLM directory: " + vocabFile);
    }
    lm = LmReaders.readGoogleLmBinary(berkeleyLm.getAbsolutePath(), vocabFile.getAbsolutePath());
  }

  @Override
  public Probability getPseudoProbability(List<String> context) {
    float logProb = lm.getLogProb(context);
    if (Float.isNaN(logProb)) {
      return new Probability(0, 1.0f);
    } else {
      //System.out.println(context + " -> " + logProb + " => " + Math.pow(10, logProb));
      return new Probability(Math.pow(10, logProb), 1.0f);
    }
  }

  @Override
  public void close() {}

}
