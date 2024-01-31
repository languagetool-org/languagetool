/* LanguageTool, a natural language style checker
 * Copyright (C) 2010 Daniel Naber (http://www.languagetool.org)
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

package org.languagetool.rules.en;

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.Example;
import org.languagetool.rules.GenericUnpairedQuotesRule;
import org.languagetool.tools.Tools;

public class EnglishUnpairedQuotesRule extends GenericUnpairedQuotesRule {

  private static final List<String> EN_START_SYMBOLS = Arrays.asList("“", "\"", "'", "‘");
  private static final List<String> EN_END_SYMBOLS   = Arrays.asList("”", "\"", "'", "’");

  public EnglishUnpairedQuotesRule(ResourceBundle messages, Language language) {
    super(messages, EN_START_SYMBOLS, EN_END_SYMBOLS);
    setUrl(Tools.getUrl("https://languagetool.org/insights/post/punctuation-guide/#what-are-parentheses"));
      addExamplePair(Example.wrong("\"I'm over here,<marker></marker> she said."),
                     Example.fixed("\"I'm over here,<marker>\"</marker> she said."));
  }

  @Override
  public String getId() {
    return "EN_UNPAIRED_QUOTES";
  }

  @Override
  protected boolean isNotBeginningApostrophe(AnalyzedTokenReadings[] tokens, int i) {
    if (tokens[i].hasPosTag("_apostrophe_contraction_") || tokens[i].hasPosTag("POS")) {
      return false;
    }
    return true;
  }

  @Override
  protected boolean isNotEndingApostrophe(AnalyzedTokenReadings[] tokens, int i) {
    if (tokens[i].hasPosTag("_apostrophe_contraction_") || tokens[i].hasPosTag("POS")) {
      return false;
    }
    return true;
  }
 

}
