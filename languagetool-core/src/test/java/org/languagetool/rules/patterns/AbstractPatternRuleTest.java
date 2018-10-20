package org.languagetool.rules.patterns;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.databroker.ResourceDataBroker;

public class AbstractPatternRuleTest {
  @Test
  public void shortMessageIsLongerThanErrorMessage() throws IOException {
    for (Language lang : Languages.get()) {
      if (skipCountryVariant(lang)) {
        // Skipping because there are no specific rules for this variant
        return;
      }
      JLanguageTool languageTool = new JLanguageTool(lang);
      for (AbstractPatternRule rule : getAllPatternRules(lang, languageTool)) {
        warnIfShortMessageLongerThanErrorMessage(rule);
      }
    }
  }

  private void warnIfShortMessageLongerThanErrorMessage(AbstractPatternRule rule) {
    if (rule instanceof PatternRule ||rule instanceof RegexPatternRule) {
      String shortMessage = rule.getShortMessage();
      int sizeOfShortMessage = shortMessage.length();
      int sizeOfErrorMessage = rule.getMessage().length();
      if (sizeOfShortMessage >= sizeOfErrorMessage) {
        if (shortMessage.equals(rule.getMessage())) {
          System.err.println("Warning: The content of <short> and <message> are identical. No need for <short> tag in that case. "
                  + "<message>. Language: " + rule.language.getName() + ". Rule: " + rule.getFullId() + ":\n"
                  + "  <short>:   " + shortMessage + "\n"
                  + "  <message>: " + rule.getMessage());
        } else {
          System.err.println("Warning: The content of <short> should be shorter than the content of "
                  + "<message>. Language: " + rule.language.getName() + ". Rule: " + rule.getFullId() + ":\n"
                  + "  <short>:   " + shortMessage + "\n"
                  + "  <message>: " + rule.getMessage());
        }
      }
    }
  }
  
  protected List<AbstractPatternRule> getAllPatternRules(Language language, JLanguageTool languageTool) throws IOException {
    List<AbstractPatternRule> rules = new ArrayList<>();
    for (String patternRuleFileName : language.getRuleFileNames()) {
      rules.addAll(languageTool.loadPatternRules(patternRuleFileName));
    }
    return rules;
  }

  protected boolean skipCountryVariant(Language lang) {
    if (Languages.get().get(0).equals(lang)) { // test always the first one
      return false;
    }
    ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
    boolean hasGrammarFiles = false;
    for (String grammarFile : getGrammarFileNames(lang)) {
      if (dataBroker.ruleFileExists(grammarFile)) {
        hasGrammarFiles = true;
      }
    }
    return !hasGrammarFiles && Languages.get().size() > 1;
  }

  protected List<String> getGrammarFileNames(Language lang) {
    String shortNameWithVariant = lang.getShortCodeWithCountryAndVariant();
    List<String> fileNames = new ArrayList<>();
    for (String ruleFile : lang.getRuleFileNames()) {
      String nameOnly = new File(ruleFile).getName();
      String fileName;
      if (shortNameWithVariant.contains("-x-")) {
        fileName = lang.getShortCode() + "/" + nameOnly;
      } else if (shortNameWithVariant.contains("-") && !shortNameWithVariant.equals("xx-XX")
              && !shortNameWithVariant.endsWith("-ANY") && Languages.get().size() > 1) {
        fileName = lang.getShortCode() + "/" + shortNameWithVariant + "/" + nameOnly;
      } else {
        fileName = lang.getShortCode() + "/" + nameOnly;
      }
      if (!fileNames.contains(fileName)) {
        fileNames.add(fileName);
      }
    }
    return fileNames;
  }
}
