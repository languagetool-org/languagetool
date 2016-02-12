/* LanguageTool, a natural language style checker 
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.en.ngrams;

import org.languagetool.Experimental;
import org.languagetool.JLanguageTool;
import org.languagetool.language.English;
import org.languagetool.rules.ngrams.NeuralNetTrainer;

@Experimental
public class EnglishNeuralNetTrainer extends NeuralNetTrainer {

  private JLanguageTool lt;
  
  private EnglishNeuralNetTrainer(String word1, String word2) {
    super(word1, word2);
  }

  @Override
  protected JLanguageTool getLanguageTool() {
    if (lt == null) {
      lt = new JLanguageTool(new English());
    }
    return lt;
  }

  public static void main(String[] args) throws Exception {
    NeuralNetTrainer errorClassifier = new EnglishNeuralNetTrainer("there", "their");
    errorClassifier.trainAndEval();
  }

}
