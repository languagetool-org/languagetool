/* LanguageTool, a natural language style checker 
 * Copyright (C) 2010 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.wikipedia;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.dev.dumpcheck.ErrorLimitReachedException;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.tools.ContextTools;
import org.languagetool.tools.StringTools;

import java.util.Date;
import java.util.List;

/**
 * Writes result of LanguageTool check to stdout.
 *  
 * @author Daniel Naber
 * @deprecated use {@link org.languagetool.dev.dumpcheck.StdoutHandler} instead (deprecated since 2.4)
 */
@Deprecated
class OutputDumpHandler extends BaseWikipediaDumpHandler {

    private static final ContextTools contextTools = new ContextTools();

    OutputDumpHandler(JLanguageTool lt, Date dumpDate, String langCode,
            Language lang) {
      super(lt, dumpDate, langCode, lang);
      contextTools.setContextSize(CONTEXT_SIZE);
    }
    
    @Override
    protected void close() {
    }

    @Override
    protected void handleResult(String title, List<RuleMatch> ruleMatches,
            String text, Language language) {
      if (ruleMatches.size() > 0) {
        int i = 1;
        System.out.println("\nTitle: " + title);
        for (RuleMatch match : ruleMatches) {
          String output = i + ".) Line " + (match.getLine() + 1) + ", column "
            + match.getColumn() + ", Rule ID: " + match.getRule().getId();
          if (match.getRule() instanceof PatternRule) {
            final PatternRule pRule = (PatternRule) match.getRule();
            output += "[" + pRule.getSubId() + "]";
          }
          System.out.println(output);
          String msg = match.getMessage();
          msg = msg.replaceAll("<suggestion>", "'");
          msg = msg.replaceAll("</suggestion>", "'");
          System.out.println("Message: " + msg);
          final List<String> replacements = match.getSuggestedReplacements();
          if (!replacements.isEmpty()) {
            System.out.println("Suggestion: " + StringTools.listToString(replacements, "; "));
          }
          System.out.println(contextTools.getPlainTextContext(match.getFromPos(), match.getToPos(), text));
          i++;
          errorCount++;
          if (maxErrors > 0 && errorCount >= maxErrors) {
            throw new ErrorLimitReachedException(maxErrors);
          }
        }
      }
    }

}
