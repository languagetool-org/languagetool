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
package org.languagetool.dev.dumpcheck;

import org.languagetool.Language;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.AbstractPatternRule;
import org.languagetool.tools.ContextTools;

import java.util.List;

/**
 * Print rule matches to STDOUT.
 * @since 2.4
 */
class StdoutHandler extends ResultHandler {

  private final ContextTools contextTools = new ContextTools();

  StdoutHandler(int maxSentences, int maxErrors) {
    this(maxSentences, maxErrors, CONTEXT_SIZE);
  }

  StdoutHandler(int maxSentences, int maxErrors, int contextSize) {
    super(maxSentences, maxErrors);
    contextTools.setContextSize(contextSize);
    //contextTools.setContextSize(100);
    //contextTools.setErrorMarker("**", "**");
    //contextTools.setEscapeHtml(false);
  }

  @Override
  protected void handleResult(Sentence sentence, List<RuleMatch> ruleMatches, Language language) {
    if (ruleMatches.size() > 0) {
      int i = 1;
      System.out.println("\nTitle: " + sentence.getTitle());
      for (RuleMatch match : ruleMatches) {
        String output = i + ".) Line " + (match.getLine() + 1) + ", column "
                + match.getColumn() + ", Rule ID: " + match.getSpecificRuleId(); //match.getRule().getId();
        if (match.getRule() instanceof AbstractPatternRule) {
          AbstractPatternRule pRule = (AbstractPatternRule) match.getRule();
          output += "[" + pRule.getSubId() + "]";
        }
        if (match.getRule().isDefaultTempOff()) {
          output += " [temp_off]";
        }
        System.out.println(output);
        String msg = match.getMessage();
        msg = msg.replaceAll("<suggestion>", "'");
        msg = msg.replaceAll("</suggestion>", "'");
        System.out.println("Message: " + msg);
        List<String> replacements = match.getSuggestedReplacements();
        if (!replacements.isEmpty()) {
          System.out.println("Suggestion: " + String.join("; ", replacements.subList(0, Math.min(replacements.size(), 5))));
        }
        if (match.getRule() instanceof AbstractPatternRule) {
          AbstractPatternRule pRule = (AbstractPatternRule) match.getRule();
          if (pRule.getSourceFile() != null) {
            System.out.println("Rule source: " + pRule.getSourceFile());
          }
        }
        System.out.println(contextTools.getPlainTextContext(match.getFromPos(), match.getToPos(), sentence.getText()));
        //System.out.println(contextTools.getContext(match.getFromPos(), match.getToPos(), sentence.getText()));
        i++;
        checkMaxErrors(++errorCount);
      }
    }
    checkMaxSentences(++sentenceCount);
  }

  @Override
  public void close() throws Exception {
  }
}
