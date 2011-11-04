/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package de.danielnaber.languagetool.rules;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.tools.StringTools;

/**
 * A rule that matches words or phrases which should not be used and suggests
 * correct ones instead. Loads the relevant words  from 
 * <code>rules/XX/replace.txt</code>, where XX is a code of the language.
 * 
 * @author Andriy Rysin
 */
public abstract class AbstractSimpleReplaceRule extends Rule {
  
  private static final String FILE_ENCODING = "utf-8";

  private Map<String, String> wrongWords; // e.g. "вреѿті реѿт" -> "зреѿтою"

  public abstract String getFileName();
  
  public String getEncoding() {
    return FILE_ENCODING;
  }
  
  /**
   * Indicates if the rule is case-sensitive. Default value is <code>true</code>.
   * @return true if the rule is case-sensitive, false otherwise.
   */
  public boolean isCaseSensitive() {
    return true;  
  }
  
  /**
   * @return the locale used for case conversion when {@link #isCaseSensitive()} is set to <code>false</code>.
   */
  public Locale getLocale() {
    return Locale.getDefault();
  }  
  
  public AbstractSimpleReplaceRule(final ResourceBundle messages) throws IOException {
    if (messages != null) {
      super.setCategory(new Category(messages.getString("category_misc")));
    }
    wrongWords = loadWords(JLanguageTool.getDataBroker().getFromRulesDirAsStream(getFileName()));
  }

  @Override
  public String getId() {
    return "SIMPLE_REPLACE";
  }

  @Override
  public String getDescription() {
    return "Checks for wrong words/phrases";
  }
  
  public String getSuggestion() {
    return " is not valid, use ";
  }
  
  public String getShort() {
    return "Wrong word";
  }

  @Override
  public final RuleMatch[] match(final AnalyzedSentence text) {
    final List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
    final AnalyzedTokenReadings[] tokens = text.getTokensWithoutWhitespace();

    for (int i = 1; i < tokens.length; i++) {
      final String token = tokens[i].getToken();

      final String origToken = token;
      final String replacement = isCaseSensitive()?wrongWords.get(token):wrongWords.get(token.toLowerCase(getLocale()));
      if (replacement != null) {
    	final String msg = token + getSuggestion() + replacement;
        final int pos = tokens[i].getStartPos();
        final RuleMatch potentialRuleMatch = new RuleMatch(this, pos, pos
            + origToken.length(), msg, getShort());
        if (!isCaseSensitive() && StringTools.startsWithUppercase(token)) {
          potentialRuleMatch.setSuggestedReplacement(StringTools.uppercaseFirstChar(replacement));
        } else {
          potentialRuleMatch.setSuggestedReplacement(replacement);
        }
        ruleMatches.add(potentialRuleMatch);
      }
    }
    return toRuleMatchArray(ruleMatches);
  }


  private Map<String, String> loadWords(final InputStream file) throws IOException {
    final Map<String, String> map = new HashMap<String, String>();
    InputStreamReader isr = null;
    BufferedReader br = null;
    try {
      isr = new InputStreamReader(file, getEncoding());
      br = new BufferedReader(isr);
      String line;

      while ((line = br.readLine()) != null) {
        line = line.trim();
        if (line.length() < 1) {
          continue;
        }
        if (line.charAt(0) == '#') { // ignore comments
          continue;
        }
        final String[] parts = line.split("=");
        if (parts.length != 2) {
          throw new IOException("Format error in file "
              + JLanguageTool.getDataBroker().getFromRulesDirAsUrl(getFileName()) + ", line: " + line);
        }
        map.put(parts[0], parts[1]);
      }

    } finally {
      if (br != null) {
        br.close();
      }
      if (isr != null) {
        isr.close();
      }
    }
    return map;
  }

  @Override
  public void reset() {
  }  
  
}
