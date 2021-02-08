package org.languagetool.commandline;

import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.hunspell.HunspellRule;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class MyMain {
  public static void main(String[] args) throws IOException {
    String sentence = "Hallo A320. Toll bar.";
    ResourceBundle messages = JLanguageTool.getDataBroker().getResourceBundle(
      JLanguageTool.MESSAGE_BUNDLE,
      new Locale("de-DE"));
    JLanguageTool langTool = new JLanguageTool(Languages.getLanguageForShortCode("de-DE"));
    List<AnalyzedSentence> analyzedSentences = langTool.analyzeText(sentence);
    System.out.println(analyzedSentences.size());
    System.out.println(analyzedSentences.get(0));
    System.out.println(analyzedSentences.get(1));
    HunspellRule rule = new HunspellRule(messages,
      Languages.getLanguageForShortCode("de-DE"), null);
    RuleMatch[] matches = rule.match(analyzedSentences.get(0));
    System.out.println(matches.length);
    RuleMatch match = matches[0];
    System.out.println(match);
  }
}
