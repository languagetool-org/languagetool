/*
 * Created on 04.04.2010
 */
package de.danielnaber.languagetool.dev.wikipedia;

import java.util.Date;
import java.util.List;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.RuleMatch;
import de.danielnaber.languagetool.rules.patterns.PatternRule;
import de.danielnaber.languagetool.tools.StringTools;

/**
 * Writes result of LanguageTool check to stdout.
 *  
 * @author Daniel Naber
 */
class OutputDumpHandler extends BaseWikipediaDumpHandler {

    OutputDumpHandler(JLanguageTool lt, int maxArticles, Date dumpDate, String langCode,
            Language lang) {
      super(lt, maxArticles, dumpDate, langCode, lang);
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
          System.out.println(StringTools.getContext(match.getFromPos(), match
              .getToPos(), text, CONTEXT_SIZE));
          i++;
        }
      }
    }

}
