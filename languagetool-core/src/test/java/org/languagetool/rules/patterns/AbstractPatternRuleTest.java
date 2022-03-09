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
package org.languagetool.rules.patterns;

import org.junit.Test;
import org.languagetool.*;
import org.languagetool.broker.ResourceDataBroker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
  
  protected List<AbstractPatternRule> getAllPatternRules(Language language, JLanguageTool languageTool) throws IOException {
    List<AbstractPatternRule> rules = new ArrayList<>();
    for (String patternRuleFileName : language.getRuleFileNames()) {
      rules.addAll(languageTool.loadPatternRules(patternRuleFileName));
    }
    return rules;
  }

  protected boolean skipCountryVariant(Language lang) {
    if (Languages.get().isEmpty() || Languages.get().get(0).equals(lang)) { // test always the first one
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
          && !shortNameWithVariant.endsWith("-ANY") && Languages.get().size() > 1
          && !shortNameWithVariant.equals("de-DE")
          && !shortNameWithVariant.equals("ca-ES")) { // TODO: change Catalan language definitions?
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
