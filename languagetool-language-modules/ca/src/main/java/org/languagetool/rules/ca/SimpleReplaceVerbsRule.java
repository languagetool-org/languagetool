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
package org.languagetool.rules.ca;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.rules.*;
import org.languagetool.synthesis.ca.CatalanSynthesizer;
import org.languagetool.tagging.ca.CatalanTagger;
import org.languagetool.tools.StringTools;

/**
 * A rule that matches incorrect verbs (including all inflected forms) and suggests
 * correct ones instead. 
 * 
 * Loads the relevant words from <code>rules/ca/replace_verbs.txt</code>.
 * 
 * @author Jaume Ortolà
 */
public class SimpleReplaceVerbsRule extends Rule {

  private static final String FILE_NAME = "/ca/replace_verbs.txt";
  // locale used on case-conversion
  private static final Locale CA_LOCALE = new Locale("CA");
  
  private static final String FILE_ENCODING = "utf-8";
  protected final Map<String, List<String>> wrongWords;
  
  protected boolean ignoreTaggedWords = true;
  
  private static final Pattern[] desinencies_1conj= new Pattern[2];
  private final CatalanTagger tagger;
  private final CatalanSynthesizer synth;

  public final String getFileName() {
    return FILE_NAME;
  }
  
  public String getEncoding() {
    return FILE_ENCODING;
  }
  
  public SimpleReplaceVerbsRule(final ResourceBundle messages) throws IOException {
    super.setLocQualityIssueType(ITSIssueType.Misspelling);
    super.setCategory(Categories.TYPOS.getCategory(messages));
    wrongWords = loadWords(JLanguageTool.getDataBroker()
        .getFromRulesDirAsStream(getFileName()));
    tagger = new CatalanTagger();
    synth = new CatalanSynthesizer();
    String s = "a|à|ada|ades|am|ant|ar|ara|arà|aran|aràs|aré|arem|àrem|aren|ares|areu|àreu|aria|aríem|arien|aries|aríeu|" +
               "às|àssem|assen|asses|àsseu|àssim|assin|assis|àssiu|at|ats|au|ava|àvem|aven|aves|àveu|e|em|en|es|és|éssem|essen|" +
               "esses|ésseu|éssim|essin|essis|éssiu|eu|i|í|in|is|o|ïs";
    desinencies_1conj[0] = Pattern.compile("(.+?)(" + s + ")");
    desinencies_1conj[1] = Pattern.compile("(.+)(" + s + ")");
  }  

  @Override
  public final String getId() {
    return "CA_SIMPLE_REPLACE_VERBS";
  }

 @Override
  public String getDescription() {
    return "Detecta verbs incorrectes i proposa suggeriments de canvi";
  }

  public String getShort() {
    return "Verb incorrecte";
  }
  
  public String getMessage(String tokenStr,List<String> replacements) {
    return "Verb incorrecte.";
  }

  public Locale getLocale() {
    return CA_LOCALE;
  }
  
  @Override
  public final RuleMatch[] match(final AnalyzedSentence sentence) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();

    for (AnalyzedTokenReadings tokenReadings : tokens) {
      String originalTokenStr = tokenReadings.getToken();
      if (ignoreTaggedWords && tokenReadings.isTagged()) {
        continue;
      }
      String tokenString = originalTokenStr.toLowerCase(getLocale());
      AnalyzedTokenReadings analyzedTokenReadings=null;
      String infinitive=null;
      int i=0;
      while (i<2 && analyzedTokenReadings == null) {
        Matcher m = desinencies_1conj[i].matcher(tokenString);
        if (m.matches()) {
          String lexeme = m.group(1);
          String desinence = m.group(2);
          if (desinence.startsWith("e") || desinence.startsWith("é")
              || desinence.startsWith("i") || desinence.startsWith("ï")) {
            if (lexeme.endsWith("c")) {
              lexeme = lexeme.substring(0, lexeme.length() - 1).concat("ç");
            } else if (lexeme.endsWith("qu")) {
              lexeme = lexeme.substring(0, lexeme.length() - 2).concat("c");
            } else if (lexeme.endsWith("g")) {
              lexeme = lexeme.substring(0, lexeme.length() - 1).concat("j");
            } else if (lexeme.endsWith("gü")) {
              lexeme = lexeme.substring(0, lexeme.length() - 2).concat("gu");
            } else if (lexeme.endsWith("gu")) {
              lexeme = lexeme.substring(0, lexeme.length() - 2).concat("g");
            }
          }
          if (desinence.startsWith("ï")) {
            desinence = "i" + desinence.substring(1, desinence.length());
          }
          infinitive = lexeme.concat("ar");
          if (wrongWords.containsKey(infinitive)) {
            List<String> wordAsArray = Arrays.asList("cant".concat(desinence));
            List<AnalyzedTokenReadings> analyzedTokenReadingsList = tagger
                .tag(wordAsArray);
            if (analyzedTokenReadingsList != null) {
              analyzedTokenReadings = analyzedTokenReadingsList.get(0);
            }
          }
        }
        i++;
      }

      //synthesize replacements
      if (analyzedTokenReadings != null) {
        List<String> possibleReplacements = new ArrayList<>();
        String[] synthesized = null;
        List<String> replacementInfinitives = wrongWords.get(infinitive);
        for (String replacementInfinitive : replacementInfinitives) {
          if (replacementInfinitive.startsWith("(")) {
            possibleReplacements.add(replacementInfinitive);
          } else {
            String[] parts = replacementInfinitive.split(" "); // the first part
                                                               // is the verb
            AnalyzedToken infinitiveAsAnTkn = new AnalyzedToken(parts[0],
                "V.*", parts[0]);
            for (AnalyzedToken analyzedToken : analyzedTokenReadings) {

              synthesized = synth.synthesize(infinitiveAsAnTkn,
                  analyzedToken.getPOSTag());
              for (String s : synthesized) {
                for (int j = 1; j < parts.length; j++) {
                  s = s.concat(" ").concat(parts[j]);
                }
                if (!possibleReplacements.contains(s)) {
                  possibleReplacements.add(s);
                }
              }
            }
          }
        }
        if (possibleReplacements.size() > 0) {
            RuleMatch potentialRuleMatch = createRuleMatch(tokenReadings,possibleReplacements);
            ruleMatches.add(potentialRuleMatch);
        }
      }
    }
    return toRuleMatchArray(ruleMatches);
  }


  private RuleMatch createRuleMatch(AnalyzedTokenReadings tokenReadings,
      List<String> replacements) {
    String tokenString = tokenReadings.getToken();
    int pos = tokenReadings.getStartPos();

    RuleMatch potentialRuleMatch = new RuleMatch(this, pos, pos
        + tokenString.length(), getMessage(tokenString, replacements), getShort());

    if (StringTools.startsWithUppercase(tokenString)) {
      for (int i = 0; i < replacements.size(); i++) {
        replacements
            .set(i, StringTools.uppercaseFirstChar(replacements.get(i)));
      }
    }

    potentialRuleMatch.setSuggestedReplacements(replacements);

    return potentialRuleMatch;
  }


  private Map<String, List<String>> loadWords(final InputStream stream)
      throws IOException {
    Map<String, List<String>> map = new HashMap<>();

    try (Scanner scanner = new Scanner(stream, getEncoding())) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        if (line.isEmpty() || line.charAt(0) == '#') { // # = comment
          continue;
        }
        String[] parts = line.split("=");
        if (parts.length != 2) {
          throw new IOException("Format error in file "
                  + JLanguageTool.getDataBroker().getFromRulesDirAsUrl(
                  getFileName()) + ", line: " + line);
        }

        String[] replacements = parts[1].split("\\|");

        // multiple incorrect forms
        final String[] wrongForms = parts[0].split("\\|");
        for (String wrongForm : wrongForms) {
          map.put(wrongForm, Arrays.asList(replacements));
        }
      }
    }
    return map;
  }

  @Override
  public void reset() {
  }

}
