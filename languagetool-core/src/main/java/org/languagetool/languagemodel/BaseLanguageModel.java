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

import org.jetbrains.annotations.Nullable;
import org.languagetool.rules.ngrams.Probability;

import java.util.List;
import java.util.Locale;

/**
 * The algorithm of a language model, independent of the way data
 * is stored (see sub classes for that).
 * @since 3.2
 */
public abstract class BaseLanguageModel implements LanguageModel {

  private static final boolean DEBUG = false;
  
  private Long totalTokenCount;

  public BaseLanguageModel()  {
  }


  private long tryGetCount(List<String> context) {
    try {
      return getCount(context);
    } catch(RuntimeException ignored) { // TODO: custom exception
      return 0;
    }
  }

  //@Override
  public Probability getPseudoProbabilityStupidBackoff(List<String> context) {
    // stupid backoff, see Brants et al. (2007)
    List<String> backoffContext = context;
    int maxCoverage = context.size();
    int coverage = maxCoverage;
    double lambda = 1.0;
    final double lambdaFactor = 0.4;
    while (backoffContext.size() != 0) {
      long count = tryGetCount(backoffContext);
      if (count != 0) {
        long baseCount = tryGetCount(backoffContext.subList(0, backoffContext.size() - 1));
        double prob = (double) count / baseCount;
        float coverageRate = (float) coverage / maxCoverage;
        return new Probability(lambda * prob, coverageRate);
      } else {
        coverage--;
        backoffContext = backoffContext.subList(0, backoffContext.size() - 1);
        lambda *= lambdaFactor;
      }
    }
    return new Probability(0.0, 0.0f);
  }


  @Override
  public Probability getPseudoProbability(List<String> context) {
    if (this.totalTokenCount == null) {
      this.totalTokenCount = getTotalTokenCount();
    }
    int maxCoverage = 0;
    int coverage = 0;
    // TODO: lm.getCount("_START_") returns 0 for Google data -- see getCount(String) in LuceneLanguageModel
    long firstWordCount = getCount(context.get(0));
    maxCoverage++;
    if (firstWordCount > 0) {
      coverage++;
    }
    // chain rule of probability (https://www.coursera.org/course/nlp, "Introduction to N-grams" and "Estimating N-gram Probabilities"),
    // https://www.ibm.com/developerworks/community/blogs/nlp/entry/the_chain_rule_of_probability?lang=en
    double p = (double) (firstWordCount + 1) / (totalTokenCount + 1);
    debug("    P for %s: %.20f (%d)\n", context.get(0), p, firstWordCount);
    long totalCount = 0;
    for (int i = 2; i <= context.size(); i++) {
      List<String> subList = context.subList(0, i);
      long phraseCount = getCount(subList);
      //System.out.println(subList + " -> " +phraseCount);
      if (subList.size() == 3) {
        totalCount = phraseCount;
      }
      double thisP = (double) (phraseCount + 1) / (firstWordCount + 1);
      /* boosting 4grams seems to improve f-measure a tiny bit:
      if (subList.size() == 4 && phraseCount > 0) {
        thisP = 100;
      }*/
      maxCoverage++;
      debug("    P for " + subList + ": %.20f (%d)\n", thisP, phraseCount);
      if (phraseCount > 0) {
        coverage++;
      }
      p *= thisP;
    }
    debug("  " + String.join(" ", context) + " => %.20f\n", p);
    return new Probability(p, (float)coverage/maxCoverage, totalCount);
  }

  /**
   * Get the occurrence count for {@code token}.
   */
  public abstract long getCount(String token1);

  /**
   * Get the occurrence count for the given token sequence.
   */
  public abstract long getCount(List<String> tokens);

  public abstract long getTotalTokenCount();

  private void debug(String message, Object... vars) {
    if (DEBUG) {
      System.out.printf(Locale.ENGLISH, message, vars);
    }
  }

}
