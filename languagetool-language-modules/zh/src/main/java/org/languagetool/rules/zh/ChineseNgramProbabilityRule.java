/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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

package org.languagetool.rules.zh;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import java.util.ArrayList;
import java.util.List;

public class ChineseNgramProbabilityRule extends Rule {

  @Override
  public String getDescription() {
    return "A rule that makes use of ngram language model to analyze text.";
  }

  @Override
  public String getId() {
    return "ZH_NGRAM_RULE";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {

    Detector detector = new Detector();
    Corrector corrector = new Corrector();

    List<PosInfo> segPosList = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokens();
    for (AnalyzedTokenReadings token : tokens) {
      PosInfo pos = new PosInfo(token.getStartPos(), token.getEndPos());
      segPosList.add(pos);
    }

    String ss = sentence.getText();

    List<PosInfo> errorPosList = detector.detect(ss);
    if (errorPosList.size()>0) {
      List<RuleMatch> ruleMatches = corrector.correct(errorPosList, segPosList);
      return toRuleMatchArray(ruleMatches);
    } else {
      return null;
    }
  }

  private class Detector {
    //TODO: implement this class. (Code Period 2)

    private List<PosInfo> detect(String sentence) {
      return null;
    }
  }

  private class Corrector {
    // TODO: implement this class. (Code Period 3)

    private List<RuleMatch> correct(List<PosInfo> errorPosList, List<PosInfo> segPosList) {
      return null;
    }
  }

  /**
   * A helper class that stores position information
   * for each word.
   */
  private class PosInfo {
    private int startPos;
    private int endPos;

    private PosInfo(int s, int e) {
      startPos = s;
      endPos = e;
    }
  }

}
