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
package org.languagetool.dev.messagechecker;

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

  private static final boolean SPELLCHECK_ONLY = false;

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
    print("Total checking time: " + String.format("%.2f", time) + " seconds");
  }

  private static void print(String s) {
    System.out.println("LTM: " + s);
  }

  private void run(Language lang)
      throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

    long start = System.currentTimeMillis();
    JLanguageTool lt = new JLanguageTool(lang);
    ContextTools contextTools = new ContextTools();
    contextTools.setErrorMarker("**", "**");
    contextTools.setEscapeHtml(false);
    print("Checking language: " + lang.getName() + " (" + lang.getShortCodeWithCountryAndVariant() + ")");
    print("Version: " + JLanguageTool.VERSION + " (" + JLanguageTool.BUILD_DATE + ", " + JLanguageTool.GIT_SHORT_ID + ")");
    if (SPELLCHECK_ONLY) {
      int enabledRules = 0;
      print("NOTE: Running spell check only");
      for (Rule r : lt.getAllRules()) {
        if (!r.isDictionaryBasedSpellingRule()) {
          lt.disableRule(r.getId());
        } else {
          enabledRules++;
        }
      }
      if (enabledRules == 0) {
        System.out.println("Error: No rule found to enable. Make sure to use a language code like 'en-US' (not just 'en') that supports spell checking.");
        System.exit(1);
      }
    }
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
        message = lang.toAdvancedTypography(message);
        message = message.replaceAll("<suggestion>", lang.getOpeningDoubleQuote()).replaceAll("</suggestion>",
            lang.getClosingDoubleQuote());
        message = message.replaceAll("<[^>]+>", "");
      }
      // don't require upper case sentence start in description (?)
      // Advanced typography in rule description is not used in production. Here is used to avoid too many positives.
      String ruleDescription = lang.toAdvancedTypography(StringTools.uppercaseFirstChar(r.getDescription()));
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
            print("Source: " + r.getFullId());
            for (RuleMatch match : matchesToShow) {
              print(lang.toAdvancedTypography(match.getMessage()));
              print(contextTools.getContext(match.getFromPos(), match.getToPos(), textToCheck));
              print("");
            }
          }
        }
      }
    }
    float time = (float) ((System.currentTimeMillis() - start) / 1000.0);
    print("Checked " + lang.getName() + " (" + lang.getShortCodeWithCountryAndVariant() + ") in "
        + String.format("%.2f", time) + " seconds");
  }

}
