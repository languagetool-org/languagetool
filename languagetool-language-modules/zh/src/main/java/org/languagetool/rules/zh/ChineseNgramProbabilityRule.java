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
