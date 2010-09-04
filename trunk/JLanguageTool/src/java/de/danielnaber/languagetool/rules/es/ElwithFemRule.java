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
package de.danielnaber.languagetool.rules.es;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TreeSet;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.rules.Category;
import de.danielnaber.languagetool.rules.RuleMatch;
import de.danielnaber.languagetool.tools.StringTools;

/**
 * Check if the determiner (if any) preceding a feminine noun is "el". This
 * rule loads a list of words (feminine nouns starting with stressed ha- or a-)
 * from an external file. These words enforce the use of 'el' as determiner
 * instead of 'la' (also with 'un', 'algun' and 'ningun').
 *
 * Sample
 *
 *   *la alma    -> el alma
 *   *la hambre  -> el hambre
 *
 * http://blog.lengua-e.com/2007/el-arma-determinante-masculino-ante-nombre-femenino/
 * http://tinyurl.com/m9uzte
 *
 *   
 * @author Susana Sotelo Docio
 *
 * based on English AvsAnRule rule
 */
public class ElwithFemRule extends SpanishRule {

  private static final String FILENAME_EL = "/es/el.txt";
  private final TreeSet<String> requiresEl;
  
  public ElwithFemRule(final ResourceBundle messages) throws IOException {
    if (messages != null) {
      super.setCategory(new Category(messages.getString("category_misc")));
    }
    requiresEl = loadWords(JLanguageTool.getDataBroker().getFromRulesDirAsStream(FILENAME_EL));
  }
  
  @Override
  public String getId() {
    return "EL_WITH_FEM";
  }

  @Override
  public String getDescription() {
    return "Uso de 'el' con sustantivos femeninos que comienzan por a- o ha- t\u00f3nicas";
  }

  @Override
  public RuleMatch[] match(final AnalyzedSentence text) {
    final List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
    final AnalyzedTokenReadings[] tokens = text.getTokensWithoutWhitespace();
    String prevToken = "";
    int prevPos = 0;
    //ignoring token 0, i.e., SENT_START
    for (int i = 1; i < tokens.length; i++) {
      String token = tokens[i].getToken();
        boolean doesRequireEl = false;

        token = token.replaceAll("[^a-záéíóúñüA-ZÁÉÍÓÚÑÜ0-9\\.']", "");     // el 'alma'
        if (StringTools.isEmpty(token)) {          
          continue;
        }
        if (requiresEl.contains(token.toLowerCase()) || requiresEl.contains(token)) {
          doesRequireEl = true;
        }

	// FIXME: temporal solution for "La Haya" (change)
	if (prevToken.equals("La") && token.equals("Haya")) {
	  doesRequireEl = false;
	}

        String msg = null;        
        String replacement = null;
        if (prevToken.equalsIgnoreCase("la") && doesRequireEl)
        {
          replacement = "el";
          if (prevToken.equals("La")) { replacement = "El"; }
        }
        else if (prevToken.equalsIgnoreCase("una") && doesRequireEl)
        {
          replacement = "un";
          if (prevToken.equals("Una")) { replacement = "Un"; }
        }
        else if (prevToken.equalsIgnoreCase("alguna") && doesRequireEl)
        {
          replacement = "alg\u00fan";
          if (prevToken.equals("Alguna")) { replacement = "Alg\u00fan"; }
        }
        else if (prevToken.equalsIgnoreCase("ninguna") && doesRequireEl)
        {
          replacement = "ning\u00fan";
          if (prevToken.equals("Ninguna")) { replacement = "Ning\u00fan"; }
        }

        msg = "Use <suggestion>" +replacement+ "</suggestion> en lugar de '" +prevToken+ "' si la siguiente "+
          "palabra comienza por 'a' o 'ha' t\u00f3nicas, por ejemplo 'el hampa', "
          + "'un agua'";


        if (replacement != null) {
          final RuleMatch ruleMatch = new RuleMatch(this, prevPos, prevPos+prevToken.length(), msg, "Art\u00edculo incorrecto");
          ruleMatches.add(ruleMatch);
        }
        if (tokens[i].hasPosTag("DA0FS0") || tokens[i].hasPosTag("DI0FS0")  ) {          
          prevToken = token;
          prevPos = tokens[i].getStartPos();
        } else {
          prevToken = "";
        }
    }
    return toRuleMatchArray(ruleMatches);
  }

  /**
   * Load words, normalized to lowercase.
   */
  private TreeSet<String> loadWords(final InputStream file) throws IOException {
    BufferedReader br = null;
    final TreeSet<String> set = new TreeSet<String>();
    try {
      br = new BufferedReader(new InputStreamReader(file, "utf-8"));
      String line;
      while ((line = br.readLine()) != null) {
        line = line.trim();
        if (line.length() < 1) {
          continue;
        }
        if (line.charAt(0) == '#') {
          continue;
        }
        if (line.charAt(0) == '*') {
          set.add(line.substring(1));
        } else {
          set.add(line.toLowerCase());
        }
      }
    } finally {
      if (br != null) {
        br.close();
      }
    }
    return set;
  }

  @Override
  public void reset() {
    // nothing
  }
}
