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
package org.languagetool.rules.de;

import java.util.ResourceBundle;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.AbstractStatisticStyleRule;
import org.languagetool.rules.Example;

/**
 * A rule that gives Hints about the use of non-significant verbs.
 * The Hints are only given when the percentage of non-significant verbs per chapter/text exceeds the given limit.
 * A limit of 0 shows all used filler words. Direct speech or citation is excluded otherwise. 
 * This rule detects no grammar error but gives stylistic hints (default off).
 * @author Fred Kruse
 * @since 5.3
 */
public class NonSignificantVerbsRule extends AbstractStatisticStyleRule {
  
  private static final int DEFAULT_MIN_PER_MILL = 8;

  private static final String[] nonSignificant = {"haben", "sein", "machen", "tun"};
  
  public NonSignificantVerbsRule(ResourceBundle messages, Language lang, UserConfig userConfig) {
    super(messages, lang, userConfig, DEFAULT_MIN_PER_MILL);
    addExamplePair(Example.wrong("Er <marker>machte</marker> einen Kuchen."),
        Example.fixed("Das macht mir Angst."));
  }

  /*
   * Is a unknown word (has only letters and no PosTag) 
   */
  private static boolean isUnknownWord(AnalyzedTokenReadings token) {
    return token.isPosTagUnknown() && token.getToken().length() > 2 && token.getToken().matches("^[A-Za-zÄÖÜäöüß]+$");
  }
  
  private static boolean isException(AnalyzedTokenReadings[] tokens, int num) {
    if (tokens[num].getToken().startsWith("sein") || tokens[num].getToken().startsWith("Sein")) {
      return true;
    } else if (tokens[num].hasLemma("machen")) {
      for (int i = 1; i < tokens.length; i++) {
        if ("Angst".equals(tokens[i].getToken()) || "frisch".equals(tokens[i].getToken()) || "bemerkbar".equals(tokens[i].getToken()) ||
            "aufmerksam".equals(tokens[i].getToken())) {
          return true;
        }
      }
    } else {
      boolean isHaben = tokens[num].hasLemma("haben");
      if (isHaben) {
        for (int i = 1; i < tokens.length; i++) {
          String sToken = tokens[i].getToken();
          if (sToken.equals("Glück") || sToken.equals("Angst") || sToken.equals("Recht") || sToken.equals("recht")) {
            return true;
          }
        }
      }  
      if (isHaben || tokens[num].hasLemma("sein")) {
        for (int i = 1; i < tokens.length; i++) {
          if (tokens[i].hasPosTagStartingWith("PA2") || tokens[i].hasPosTagStartingWith("VER:PA2") || isUnknownWord(tokens[i])) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Minimal value is given in per mil
   */
  @Override
  public double denominator() {
    return 1000.0;
  }
  
  @Override
  protected int conditionFulfilled(AnalyzedTokenReadings[] tokens, int nAnalysedToken) {
    if (tokens[nAnalysedToken].hasAnyLemma(nonSignificant) && !isException(tokens, nAnalysedToken)) {
      return nAnalysedToken;
    }
    return -1;
  }
  
  @Override
  protected boolean sentenceConditionFulfilled(AnalyzedTokenReadings[] tokens, int nToken) {
    return false;
  }

  @Override
  protected boolean excludeDirectSpeech() {
    return true;
  }

  @Override
  protected String getLimitMessage(int limit, double percent) {
    if (limit == 0) {
      return "Dieses Verb hat wenig Aussagekraft. Verwenden Sie wenn möglich ein anderes oder formulieren Sie den Satz um.";
    }
    return "Mehr als " + limit + "‰ wenig aussagekräftige Verben {" + ((int) (percent +0.5d)) + 
        "‰} gefunden. Verwenden Sie wenn möglich ein anderes Verb oder formulieren Sie den Satz um.";
  }

  @Override
  protected String getSentenceMessage() {
    return null;
  }

  @Override
  public String getId() {
    return "NON_SIGNIFICANT_VERB_DE";
  }

  @Override
  public String getDescription() {
    return "Statistische Stilanalyse: Verben mit wenig Aussagekraft";
  }

  @Override
  public String getConfigureText() {
    return "Anzeigen wenn mehr als ...‰ eines Kapitels wenig aussagekräftige Verben sind:";
  }

}
