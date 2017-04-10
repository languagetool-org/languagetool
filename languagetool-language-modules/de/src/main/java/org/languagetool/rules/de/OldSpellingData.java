/* LanguageTool, a natural language style checker 
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.de;

import org.jetbrains.annotations.NotNull;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.rules.patterns.PatternTokenBuilder;
import org.languagetool.tagging.de.GermanTagger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Old to new spelling data loaded form CSV.
 * @since 3.8
 */
class OldSpellingData {

  private final List<OldSpellingRuleWithSuggestion> spellingRules = new ArrayList<>();
  
  OldSpellingData(String ruleDesc) {
    String filePath = "/de/alt_neu.csv";
    try (InputStream inputStream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(filePath);
         Scanner scanner = new Scanner(inputStream, "utf-8")) {
      Language german = Languages.getLanguageForShortCode("de");
      String message = "Diese Schreibweise war nur in der alten Rechtschreibung korrekt.";
      String shortMessage = "alte Rechtschreibung";
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        if (line.startsWith("#")) {
          continue;
        }
        String[] parts = line.split(";");
        if (parts.length != 2) {
          throw new RuntimeException("Unexpected format in file " + filePath + ": " + line);
        }
        String oldSpelling = parts[0];
        String newSpelling = parts[1];
        List<PatternToken> patternTokens = getTokens(oldSpelling, german);
        PatternRule rule = new PatternRule("OLD_SPELLING_INTERNAL", german, patternTokens, ruleDesc, message, shortMessage);
        spellingRules.add(new OldSpellingRuleWithSuggestion(rule, oldSpelling, newSpelling));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @NotNull
  private List<PatternToken> getTokens(String oldSpelling, Language lang) {
    PatternTokenBuilder builder = new PatternTokenBuilder();
    String[] newSpellingTokens = oldSpelling.split(" ");
    List<PatternToken> patternTokens = new ArrayList<>();
    for (String part : newSpellingTokens) {
      PatternToken token;
      if (isBaseform(oldSpelling, lang)) {
        token = builder.csToken(part).matchInflectedForms().build();
      } else {
        token = builder.csToken(part).build();
      }
      patternTokens.add(token);
    }
    return patternTokens;
  }

  private boolean isBaseform(String term, Language lang) {
    try {
      AnalyzedTokenReadings lookup = ((GermanTagger) lang.getTagger()).lookup(term);
      if (lookup != null) {
        return lookup.hasLemma(term);
      }
      return false;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public List<OldSpellingRuleWithSuggestion> get() {
    return spellingRules;
  }

}
