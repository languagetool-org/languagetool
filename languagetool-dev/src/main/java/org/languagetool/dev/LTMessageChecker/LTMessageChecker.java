/* LanguageTool, a natural language style checker 
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.LTMessageChecker;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tools.ContextTools;
import org.languagetool.tools.StringTools;

/**
 * Checks LanguageTool messages, short messages and rule descriptions, using LanguageTool itself.
 */
public class LTMessageChecker {

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.out.println("Usage: " + LTMessageChecker.class.getSimpleName() + " <langCode> | ALL");
      System.exit(1);
    }
    LTMessageChecker check = new LTMessageChecker();
    long start = System.currentTimeMillis();
    if (args[0].equalsIgnoreCase("all")) {
      for (Language lang : Languages.get()) {
        check.run(lang);
      }
    } else {
      check.run(Languages.getLanguageForShortCode(args[0]));
    }
    float time = (float) ((System.currentTimeMillis() - start) / 1000.0);
    System.out.println("Total checking time: " + String.format("%.2f", time) + " seconds");
  }

  private void run(Language lang)
      throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

    long start = System.currentTimeMillis();
    JLanguageTool lt = new JLanguageTool(lang);
    ContextTools contextTools = new ContextTools();
    contextTools.setErrorMarker("**", "**");
    contextTools.setEscapeHtml(false);
    System.out.println("Checking language: " + lang.getName() + " (" + lang.getShortCodeWithCountryAndVariant() + ")");
    System.out.println("Version: " + JLanguageTool.VERSION + " (" + JLanguageTool.BUILD_DATE + ")");
    for (Rule r : lt.getAllRules()) {
      String message = "";
      try {
        Method m = r.getClass().getMethod("getMessage", null);
        message = (String) m.invoke(r);
      } catch (NoSuchMethodException e) {
        // do nothing
      }
      String shortMessage = "";
      try {
        Method m = r.getClass().getMethod("getShortMessage", null);
        shortMessage = (String) m.invoke(r);
      } catch (NoSuchMethodException e) {
        // do nothing
      }
      if (!message.isEmpty()) {
        message = message.replaceAll("<suggestion>", lang.getOpeningDoubleQuote()).replaceAll("</suggestion>",
            lang.getClosingDoubleQuote());
        message = message.replaceAll("<[^>]+>", "");
        message = lang.toAdvancedTypography(message);
      }
      // don't require upper case sentence start in description (?)
      String ruleDescription = StringTools.uppercaseFirstChar(r.getDescription());
      String textToCheck = message + "\n\n" + shortMessage + "\n\n" + ruleDescription;
      if (!textToCheck.isEmpty()) {
        List<RuleMatch> matches = lt.check(textToCheck);
        if (matches.size() > 0) {
          List<RuleMatch> matchesToShow = new ArrayList<>();
          for (RuleMatch match : matches) {
            // if (!match.getRule().getFullId().equals(r.getFullId())) {
            if (!match.getRule().getId().equals(r.getId())) {
              matchesToShow.add(match);
            }
          }
          if (matchesToShow.size() > 0) {
            System.out.println("Source: " + r.getFullId());
            for (RuleMatch match : matchesToShow) {
              System.out.println(match.getMessage().replace("<suggestion>", "'").replace("</suggestion>", "'"));
              System.out.println(contextTools.getContext(match.getFromPos(), match.getToPos(), textToCheck));
              System.out.println();
            }
          }
        }
      }
    }
    float time = (float) ((System.currentTimeMillis() - start) / 1000.0);
    System.out.println("Checked " + lang.getName() + " (" + lang.getShortCodeWithCountryAndVariant() + ") in "
        + String.format("%.2f", time) + " seconds");
  }
  
}
