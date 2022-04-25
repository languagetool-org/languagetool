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
package org.languagetool.clientexample;

import org.languagetool.Premium;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.List;

/**
 * A simple interactive test to see if the languages are available and don't crash.
 */
public class Example {

  public static void main(String[] args) throws IOException {
    List<Language> realLanguages = Languages.get();
    System.out.println("Premium version: " + Premium.isPremiumVersion());
    System.out.println("This example will test a short string with all languages known to LanguageTool.");
    System.out.println("It's just a test to make sure there's at least no crash.");
    System.out.println("Using LanguageTool " + JLanguageTool.VERSION + " (" + JLanguageTool.BUILD_DATE + ")");
    System.out.println("Supported languages: " + realLanguages.size());
    for (Language language : realLanguages) {
      JLanguageTool lt = new JLanguageTool(language);
      String input = "And the the";
      List<RuleMatch> result = lt.check(input);
      System.out.println("Checking '" + input + "' with " + language + ":");
      for (RuleMatch ruleMatch : result) {
        System.out.println("    " + ruleMatch);
      }
    }
  }

}
