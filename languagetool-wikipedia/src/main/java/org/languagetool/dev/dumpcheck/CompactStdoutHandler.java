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
 * Print compact rule matches to STDOUT.
 * @since 3.3
 */
class CompactStdoutHandler extends ResultHandler {

  private final ContextTools contextTools = new ContextTools();

  CompactStdoutHandler(int maxSentences, int maxErrors) {
    super(maxSentences, maxErrors);
    contextTools.setContextSize(70);
    contextTools.setErrorMarkerStart("**");
    contextTools.setErrorMarkerEnd("**");
    contextTools.setEscapeHtml(false);
  }

  @Override
  protected void handleResult(Sentence sentence, List<RuleMatch> ruleMatches, Language language) {
    if (ruleMatches.size() > 0) {
      for (RuleMatch match : ruleMatches) {
        String ruleId = match.getRule().getId();
        if (match.getRule() instanceof AbstractPatternRule) {
          AbstractPatternRule pRule = (AbstractPatternRule) match.getRule();
          ruleId = pRule.getFullId();
        }
        System.out.println(ruleId + ": " + contextTools.getContext(match.getFromPos(), match.getToPos(), sentence.getText()));
        checkMaxErrors(++errorCount);
      }
    }
    checkMaxSentences(++sentenceCount);
  }

  @Override
  public void close() throws Exception {
  }
}
