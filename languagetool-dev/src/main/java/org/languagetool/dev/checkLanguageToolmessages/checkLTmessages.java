package org.languagetool.dev.checkLanguageToolmessages;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.commandline.CommandLineTools;
import org.languagetool.dev.eval.SpellCheckEvaluation;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.PatternRule;

public class checkLTmessages {

  public static void main(String[] args)
      throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    if (args.length != 1) {
      System.out.println("Usage: " + SpellCheckEvaluation.class.getSimpleName() + " <langCode> | ALL");
      System.exit(1);
    }
    checkLTmessages check = new checkLTmessages();
    if (args[0].equalsIgnoreCase("all")) {
      for (Language lang : Languages.get()) {
        check.run(lang);
      }
    } else {
      check.run(Languages.getLanguageForShortCode(args[0]));
    }

  }

  private void run(Language lang)
      throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

    JLanguageTool lt = new JLanguageTool(lang);
    for (Rule r : lt.getAllRules()) {
      String message = "";
      try {
        // parameter type is null
        Method m = r.getClass().getMethod("getMessage", null);
        message = (String) m.invoke(r);
      } catch (NoSuchMethodException e) {
        // do nothing
      }
      if (!message.isEmpty()) {
        message = message.replaceAll("<suggestion>", lang.getOpeningQuote()).replaceAll("</suggestion>",
            lang.getClosingQuote());
        message = message.replaceAll("<[^>]+>", "");
        List<RuleMatch> matches = lt.check(message);
        for (RuleMatch match : matches) {
          System.out.println("**** Rule ID: " + r.getFullId());
          CommandLineTools.printMatches(matches, 0, message, 15, lang);
        }
      }
    }

  }
}
