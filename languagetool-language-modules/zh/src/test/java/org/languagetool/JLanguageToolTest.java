package org.languagetool;

import org.junit.Test;

import org.languagetool.language.SimplifiedChinese;
import org.languagetool.language.TraditionalChinese;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.List;

public class JLanguageToolTest {

  private JLanguageTool languageTool1 = new JLanguageTool(new SimplifiedChinese());
  private JLanguageTool languageTool2 = new JLanguageTool(new TraditionalChinese());
  /**
   * Demo of using java-api.
   * @link http://wiki.languagetool.org/java-api
   */
  @Test
  public void demoCodeForHomepage() throws IOException {
    String[] text = {"戎边的战士们真的很辛苦。.", "他们定婚了。","常年累月"};
    for (String t : text) {
//      System.out.println(languageTool1.analyzeText(t));
      List<RuleMatch> matches = languageTool1.check(t);
      for (RuleMatch match: matches) {
        System.out.println("Potential error at " +
                match.getFromPos() + "-" + match.getToPos() + ": " +
                match.getMessage());
        System.out.println("Suggested correction: " +
                match.getSuggestedReplacements());
      }
    }
  }

  @Test
  public void demoCodeForHomepage2() throws IOException {
    String[] text = {"他的公司解散後，生計並無著落，簡直是走頭無路。"};
    for (String t : text) {
//      System.out.println(languageTool2.analyzeText(t));
      List<RuleMatch> matches = languageTool2.check(t);
      for (RuleMatch match: matches) {
        System.out.println("Potential error at " +
                match.getFromPos() + "-" + match.getToPos() + ": " +
                match.getMessage());
        System.out.println("Suggested correction: " +
                match.getSuggestedReplacements());
      }
    }
  }

}
