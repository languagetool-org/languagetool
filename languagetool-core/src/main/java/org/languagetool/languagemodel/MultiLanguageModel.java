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

import org.languagetool.rules.ngrams.Probability;

import java.util.List;

/**
 * Combines the results of several {@link LanguageModel}s.
 * @since 3.2
 */
public class MultiLanguageModel implements LanguageModel {

  private final List<LanguageModel> lms;

  public MultiLanguageModel(List<LanguageModel> lms) {
    if (lms.isEmpty()) {
      throw new IllegalArgumentException("List of language models is empty");
    }
    this.lms = lms;
  }

  @Override
  public Probability getPseudoProbability(List<String> context) {
    double prob = 0;
    float coverage = 0;
    long occurrences = 0;
    for (LanguageModel lm : lms) {
      Probability pProb = lm.getPseudoProbability(context);
      //System.out.println(i + ". " + pProb.getProb() + " (" + pProb.getCoverage() + ")");
      // TODO: decide what's the proper way to combine the probabilities
      prob += pProb.getProb();
      coverage += pProb.getCoverage();
      occurrences += pProb.getOccurrences();
    }
    return new Probability(prob, coverage/lms.size(), occurrences);
  }

  @Override
  public void close() {
    lms.stream().forEach(LanguageModel::close);
  }

  @Override
  public String toString() {
    return lms.toString();
  }
}
