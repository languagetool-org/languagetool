/* LanguageTool, a natural language style checker 
 * Copyright (C) 2022 Daniel Naber (http://www.danielnaber.de)
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

import java.util.List;

/**
 * Print rule matches to STDOUT in CSV format (tab-separated).
 */
class CSVHandler extends ResultHandler {

  CSVHandler(int maxSentences, int maxErrors) {
    super(maxSentences, maxErrors);
  }

  @Override
  protected void handleResult(Sentence sentence, List<RuleMatch> ruleMatches, Language language) {
    String sentenceStr = sentence.getText();
    if (ruleMatches.size() > 0) {
      for (RuleMatch match : ruleMatches) {
        sentenceStr = noTabs(sentenceStr.substring(0, match.getFromPos())) +
                      "__" +
                      noTabs(sentenceStr.substring(match.getFromPos(), match.getToPos())) +
                      "__" +
                      noTabs(sentenceStr.substring(match.getToPos()));
        System.out.println("MATCH\t" + match.getRule().getFullId() + "\t" + noTabs(sentenceStr));
        checkMaxErrors(++errorCount);
      }
    } else {
      System.out.println("NOMATCH\t\t" + noTabs(sentenceStr));
    }
    checkMaxSentences(++sentenceCount);
  }

  private String noTabs(String s) {
    return s.replace("\t", "\\t");
  }

  @Override
  public void close() throws Exception {
  }
}
