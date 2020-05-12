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

import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.rules.Rule;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Check runtime depending on number of active rules. Not a unit test, for interactive use only.
 */
final class RuleNumberScalabilityTest {

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.out.println("Usage: " + RuleNumberScalabilityTest.class.getSimpleName() + " <languageCode> <text_file>");
      System.exit(1);
    }
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode(args[0]));
    try (InputStream is = Files.newInputStream(Paths.get(args[1]))) {
      String text = StringTools.readStream(is, "utf-8");
      System.out.println("Warmup...");
      lt.check(text);
      lt.check(text);

      long baselineTime = getBaselineTime(lt, text);
      System.out.println("Baseline: " + baselineTime + "ms (time with no pattern rules active)");

      int ruleNumber = lt.getAllActiveRules().size();
      System.out.println("Total rules: " + ruleNumber);
      int steps = 5;
      int prevActiveRules = -1;
      long prevCleanRunTime = -1;
      for (int i = steps; i > 0; i--) {
        int targetActiveRules = ruleNumber / i;
        deactivateAllRules(lt);
        for (Rule rule : lt.getAllRules()) {
          lt.enableRule(rule.getId());
          if (lt.getAllActiveRules().size() > targetActiveRules) {
            break;
          }
        }
        int activeRules = lt.getAllActiveRules().size();
        long startTime = System.currentTimeMillis();
        lt.check(text);
        long runTime = System.currentTimeMillis() - startTime;
        long cleanRunTime = runTime - baselineTime;
        if (prevActiveRules != -1 && prevCleanRunTime != -1) {
          float ruleFactor = (float) activeRules / prevActiveRules;
          float cleanRuntimeFactor = (float) cleanRunTime / prevCleanRunTime;
          System.out.println("Active rules: " + activeRules + ", runtime: " + runTime + "ms, cleanRunTime: " + cleanRunTime
            + ", ruleFactor: " + ruleFactor + ", cleanRuntimeFactor: " + cleanRuntimeFactor);
        } else {
          System.out.println("Active rules: " + activeRules + ", runtime: " + runTime + "ms, cleanRunTime: " + cleanRunTime);
        }
        prevActiveRules = activeRules;
        prevCleanRunTime = cleanRunTime;
      }
    }
    System.out.println("ruleFactor = the number of rules compared to the previous run");
    System.out.println("cleanRuntimeFactor = the runtime (without baseline) compared to the previous run");
    System.out.println(" => cleanRuntimeFactor should not grow much more than ruleFactor, otherwise we scale");
    System.out.println(" => badly with respect to the number of rules");
  }

  private static long getBaselineTime(JLanguageTool langTool, String text) throws IOException {
    deactivateAllRules(langTool);
    long baselineStartTime = System.currentTimeMillis();
    langTool.check(text);
    long baselineTime = System.currentTimeMillis() - baselineStartTime;
    if (langTool.getAllActiveRules().size() > 0) {
      throw new RuntimeException("Did not expect to get any pattern rules: " + langTool.getAllActiveRules().size());
    }
    for (Rule rule : langTool.getAllRules()) {
      langTool.enableRule(rule.getId());
    }
    return baselineTime;
  }

  private static void deactivateAllRules(JLanguageTool langTool) {
    for (Rule rule : langTool.getAllActiveRules()) {
      langTool.disableRule(rule.getId());
    }
  }

}
