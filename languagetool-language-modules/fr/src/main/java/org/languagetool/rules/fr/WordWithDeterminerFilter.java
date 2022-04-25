/* LanguageTool, a natural language style checker 
 * Copyright (C) 2020 Jaume Ortolà (http://www.danielnaber.de)
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
package org.languagetool.rules.fr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.language.French;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.synthesis.FrenchSynthesizer;
import org.languagetool.tools.StringTools;

/*
 * Create suggestions for: determiner + noun/adjective
 */

public class WordWithDeterminerFilter extends RuleFilter {

  private static final FrenchSynthesizer synth = new FrenchSynthesizer(new French());
  private static JLanguageTool lt = new JLanguageTool(new French());

  private static final String determinerRegexp = "(P.)?D .*|J .*|V.* ppa .*";
  private static final Pattern DETERMINER = Pattern.compile(determinerRegexp);
  private static final String wordRegexp = "[ZNJ] .*|V.* ppa .*";
  private static final Pattern WORD = Pattern.compile(wordRegexp);

  // 0=MS, 1=FS, 2=MP, 3=FP
  private static final String[] GenderNumber = { "(m|e) (s|sp)", "(f|e) (s|sp)", "(m|e) (p|sp)", "(f|e) (p|sp)" };
  private static final String determiner = "((P.)?D |J |V.* ppa )";

  private static final List<String> exceptionsDeterminer = Arrays
      .asList(new String[] { "bels", "fols", "mols", "nouvels" });
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
      AnalyzedTokenReadings[] patternTokens) throws IOException {

    String wordFrom = getRequired("wordFrom", arguments);
    String determinerFrom = getRequired("determinerFrom", arguments);
    int posWord = 0;
    int posDeterminer = 0;
    if (wordFrom != null && determinerFrom != null) {
      posWord = Integer.parseInt(wordFrom);
      if (posWord < 1 || posWord > patternTokens.length) {
        throw new IllegalArgumentException("WordWithDeterminerFilter: Index out of bounds in "
            + match.getRule().getFullId() + ", wordFrom: " + posWord);
      }
      posDeterminer = Integer.parseInt(determinerFrom);
      if (posDeterminer < 1 || posDeterminer > patternTokens.length) {
        throw new IllegalArgumentException("WordWithDeterminerFilter: Index out of bounds in "
            + match.getRule().getFullId() + ", posDeterminer: " + posWord);
      }
    } else {
      throw new IllegalArgumentException("WordWithDeterminerFilter: undefined parameters wordFrom or determinerFrom in "
          + match.getRule().getFullId());
    }
    AnalyzedTokenReadings atrDeterminer = patternTokens[posDeterminer - 1];
    AnalyzedTokenReadings atrWord = patternTokens[posWord - 1];
    boolean isDeterminerCapitalized = StringTools.isCapitalizedWord(atrDeterminer.getToken());
    boolean isWordCapitalized = StringTools.isCapitalizedWord(atrWord.getToken());
    boolean isDeterminerAllupper = StringTools.isAllUppercase(atrDeterminer.getToken())
        && !atrDeterminer.getToken().equalsIgnoreCase("L'");
    boolean isWordAllupper = StringTools.isAllUppercase(atrWord.getToken());
    AnalyzedToken atDeterminer = getAnalyzedToken(atrDeterminer, DETERMINER);
    AnalyzedToken atWord = getAnalyzedToken(atrWord, WORD);
    if (atWord == null || atDeterminer == null) {
      throw new RuntimeException(
          "Error analyzing sentence: '" + match.getSentence().getText() + "' with rule " + match.getRule().getFullId());
    }
    boolean isNoun = atWord.getPOSTag().startsWith("N") || atWord.getPOSTag().startsWith("Z");
    boolean isAdjective = atWord.getPOSTag().startsWith("J");
    // boolean isParticiple = atWord.getPOSTag().startsWith("V");

    String prefix = "[ZNJ] ";
    if (isNoun && !isAdjective) {
      prefix = "[NZ] ";
    } else if (!isNoun && isAdjective) {
      prefix = "J ";
    }

    // synthesize all forms
    String[][] determinerForms = new String[4][];
    String[][] wordForms = new String[4][];
    for (int i = 0; i < 4; i++) {
      determinerForms[i] = synth.synthesize(atDeterminer, determiner + GenderNumber[i], true);
      wordForms[i] = synth.synthesize(atWord, prefix + GenderNumber[i], true);
      // if it cannot be synthesyzed, keep the original determiner
      if (determinerForms[i].length == 0 && atDeterminer.getPOSTag().matches(".+" + GenderNumber[i])) {
        determinerForms[i] = new String[] { atDeterminer.getToken() };
      }
      // if it cannot be synthesyzed, keep the original word
      if (wordForms[i].length == 0 && atWord.getPOSTag().matches(".+" + GenderNumber[i])) {
        wordForms[i] = new String[] { atWord.getToken() };
      }
    }

    for (Rule r : lt.getAllRules()) {
      if (r.getCategory().getId().toString().equals("CAT_ELISION") || r.getId().equals("CET_CE")
          || r.getId().equals("CE_CET") || r.getId().equals("MA_VOYELLE") || r.getId().equals("MON_NFS")
          || r.getId().equals("VIEUX")) {
        lt.enableRule(r.getId());
      } else {
        lt.disableRule(r.getId());
      }
    }

    // generate suggestions
    List<String> replacements = new ArrayList<>();
    for (int i = 0; i < 4; i++) {
      for (int cw = 0; cw < wordForms[i].length; cw++) {
        for (int cd = 0; cd < determinerForms[i].length; cd++) {
          if (exceptionsDeterminer.contains(determinerForms[i][cd])) {
            continue;
          }
          if (determinerForms[i][cd] != null && wordForms[i][cw] != null) {
            String determiner = determinerForms[i][cd];
            String word = wordForms[i][cw];
            if (isDeterminerCapitalized) {
              determiner = StringTools.uppercaseFirstChar(determiner);
            }
            if (isWordCapitalized) {
              word = StringTools.uppercaseFirstChar(word);
            }
            if (isDeterminerAllupper) {
              determiner = determiner.toUpperCase();
            }
            if (isWordAllupper) {
              word = word.toUpperCase();
            }
            String r = determiner + " " + word;
            r = r.replace("' ", "'");
            // remove suggestions with errors
            List<RuleMatch> matches = lt.check(r);
            if (matches.size() == 0 && !replacements.contains(r)) {
              if (r.endsWith(atWord.getToken())) {
                replacements.add(0, r);
              } else {
                replacements.add(r);
              }
            }
          }
        }
      }
    }

    String message = match.getMessage();
    RuleMatch ruleMatch = new RuleMatch(match.getRule(), match.getSentence(), match.getFromPos(), match.getToPos(),
        message, match.getShortMessage());
    ruleMatch.setType(match.getType());
    //add existing suggestion in the XML rule
    replacements.addAll(0, match.getSuggestedReplacements());
    if (!replacements.isEmpty()) {
      ruleMatch.setSuggestedReplacements(replacements);
    }
    return ruleMatch;
  }

  private AnalyzedToken getAnalyzedToken(AnalyzedTokenReadings aToken, Pattern pattern) {
    for (AnalyzedToken analyzedToken : aToken) {
      String posTag = analyzedToken.getPOSTag();
      if (posTag == null) {
        posTag = "UNKNOWN";
      }
      final Matcher m = pattern.matcher(posTag);
      if (m.matches()) {
        return analyzedToken;
      }
    }
    return null;
  }

}
