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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.*;
import org.languagetool.synthesis.ca.CatalanSynthesizer;
import org.languagetool.tagging.ca.CatalanTagger;

/**
 * A rule that matches incorrect verbs (including all inflected forms) and
 * suggests correct ones instead.
 * 
 * Loads the relevant words from <code>rules/ca/replace_verbs.txt</code>.
 * 
 * @author Jaume Ortolà
 */
public class SimpleReplaceVerbsRule extends AbstractSimpleReplaceRule {

  private static final Map<String, List<String>> wrongWords = loadFromPath("/ca/replace_verbs.txt");
  private static final Locale CA_LOCALE = new Locale("CA");

  @Override
  protected Map<String, List<String>> getWrongWords() {
    return wrongWords;
  }

  // totes les terminacions possibles dels verbs: cantar, servir, conèixer, desmerèixer
  private static final String endings = "a|ada|ades|am|ant|ar|ara|aran|arem|aren|ares|areu|aria|arien|aries|arà|aràs|aré|aríem|aríeu|assen|asses|assin|assis|at|ats|au|ava|aven|aves|e|ec|ega|eguda|egudes|eguem|eguen|eguera|egueren|egueres|egues|eguessen|eguesses|eguessin|eguessis|egueu|egui|eguin|eguis|egut|eguts|egué|eguérem|eguéreu|egués|eguéssem|eguésseu|eguéssim|eguéssiu|eguí|eix|eixem|eixen|eixent|eixeran|eixerem|eixeren|eixeres|eixereu|eixeria|eixerien|eixeries|eixerà|eixeràs|eixeré|eixeríem|eixeríeu|eixes|eixessen|eixesses|eixessin|eixessis|eixeu|eixi|eixia|eixien|eixies|eixin|eixis|eixo|eixé|eixérem|eixéreu|eixés|eixéssem|eixésseu|eixéssim|eixéssiu|eixí|eixíem|eixíeu|em|en|es|esc|esca|escuda|escudes|escut|escuts|esquem|esquen|esquera|esqueren|esqueres|esques|esquessen|esquesses|esquessin|esquessis|esqueu|esqui|esquin|esquis|esqué|esquérem|esquéreu|esqués|esquéssem|esquésseu|esquéssim|esquéssiu|esquí|essen|esses|essin|essis|eu|i|ia|ida|ides|ien|ies|iguem|igueu|im|in|int|ir|ira|iran|irem|iren|ires|ireu|iria|irien|iries|irà|iràs|iré|iríem|iríeu|is|isc|isca|isquen|isques|issen|isses|issin|issis|it|its|iu|ix|ixen|ixes|o|à|àrem|àreu|às|àssem|àsseu|àssim|àssiu|àvem|àveu|éixer|és|éssem|ésseu|éssim|éssiu|í|íem|íeu|írem|íreu|ís|íssem|ísseu|íssim|íssiu|ïs";
  private static final Pattern desinencies_1conj_0 = Pattern.compile("(.+?)(" + endings + ")");
  private static final Pattern desinencies_1conj_1 = Pattern.compile("(.+)(" + endings + ")");
  private CatalanTagger tagger;
  private CatalanSynthesizer synth;

  public SimpleReplaceVerbsRule(final ResourceBundle messages, Language language) {
    super(messages);
    super.setCategory(Categories.TYPOS.getCategory(messages));
    super.setLocQualityIssueType(ITSIssueType.Misspelling);
    super.setIgnoreTaggedWords();
    tagger = (CatalanTagger) language.getTagger();
    synth = (CatalanSynthesizer) language.getSynthesizer();
  }

  @Override
  public final String getId() {
    return "CA_SIMPLE_REPLACE_VERBS";
  }

  @Override
  public String getDescription() {
    return "Detecta verbs incorrectes i proposa suggeriments de canvi";
  }

  @Override
  public String getShort() {
    return "Verb incorrecte";
  }

  @Override
  public String getMessage(String tokenStr, List<String> replacements) {
    return "Verb incorrecte.";
  }

  @Override
  public Locale getLocale() {
    return CA_LOCALE;
  }

  @Override
  public final RuleMatch[] match(final AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();

    for (AnalyzedTokenReadings tokenReadings : tokens) {
      String originalTokenStr = tokenReadings.getToken();
      if (ignoreTaggedWords && tokenReadings.isTagged()) {
        continue;
      }
      String tokenString = originalTokenStr.toLowerCase(getLocale());
      AnalyzedTokenReadings analyzedTokenReadings = null;
      String infinitive = null;
      int i = 0;
      while (i < 2 && analyzedTokenReadings == null) {
        Matcher m;
        if (i == 0) {
          m = desinencies_1conj_0.matcher(tokenString);
        } else {
          m = desinencies_1conj_1.matcher(tokenString);
        }
        if (m.matches()) {
          String lexeme = m.group(1);
          String desinence = m.group(2);
          if (desinence.startsWith("e") || desinence.startsWith("é") || desinence.startsWith("i")
              || desinence.startsWith("ï")) {
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
            desinence = "i" + desinence.substring(1);
          }
          infinitive = lexeme.concat("ar");
          if (wrongWords.containsKey(infinitive)) {
            List<String> wordAsArray = Arrays.asList("cant".concat(desinence)); // cantar
            List<AnalyzedTokenReadings> analyzedTokenReadingsList = tagger.tag(wordAsArray);
            if (analyzedTokenReadingsList.get(0).getAnalyzedToken(0).getPOSTag() != null) {
              analyzedTokenReadings = analyzedTokenReadingsList.get(0);
            }
          }
          if (analyzedTokenReadings == null) {
            infinitive = lexeme.concat("ir");
            if (wrongWords.containsKey(infinitive)) {
              List<String> wordAsArray = Arrays.asList("serv".concat(desinence)); // servir
              List<AnalyzedTokenReadings> analyzedTokenReadingsList = tagger.tag(wordAsArray);
              if (analyzedTokenReadingsList.get(0).getAnalyzedToken(0).getPOSTag() != null) {
                analyzedTokenReadings = analyzedTokenReadingsList.get(0);
              }
            }
          }
          if (analyzedTokenReadings == null && lexeme.endsWith("g")) {
            infinitive = lexeme.concat("uir");
            if (wrongWords.containsKey(infinitive)) {
              List<String> wordAsArray = Arrays.asList("serv".concat(desinence)); // servir
              List<AnalyzedTokenReadings> analyzedTokenReadingsList = tagger.tag(wordAsArray);
              if (analyzedTokenReadingsList.get(0).getAnalyzedToken(0).getPOSTag() != null) {
                analyzedTokenReadings = analyzedTokenReadingsList.get(0);
              }
            }
          }
          if (analyzedTokenReadings == null) {
            infinitive = lexeme.concat("èixer");
            if (wrongWords.containsKey(infinitive)) {
              List<String> wordAsArray = Arrays.asList("con".concat(desinence)); // conèixer
              List<AnalyzedTokenReadings> analyzedTokenReadingsList = tagger.tag(wordAsArray);
              if (analyzedTokenReadingsList.get(0).getAnalyzedToken(0).getPOSTag() != null) {
                analyzedTokenReadings = analyzedTokenReadingsList.get(0);
              } else {
                List<String> wordAsArray2 = Arrays.asList("desmer".concat(desinence)); // desmerèixer
                List<AnalyzedTokenReadings> analyzedTokenReadingsList2 = tagger.tag(wordAsArray2);
                if (analyzedTokenReadingsList2.get(0).getAnalyzedToken(0).getPOSTag() != null) {
                  analyzedTokenReadings = analyzedTokenReadingsList2.get(0);
                }
              }
            }
          }
        }
        i++;
      }

      // synthesize replacements
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
            AnalyzedToken infinitiveAsAnTkn = new AnalyzedToken(parts[0], "V.*", parts[0]);
            for (AnalyzedToken analyzedToken : analyzedTokenReadings) {
              try {
                String POSTag = analyzedToken.getPOSTag();
                if (infinitiveAsAnTkn.getLemma().equals("haver")) {
                  POSTag = "VA" + POSTag.substring(2);
                }
                synthesized = synth.synthesize(infinitiveAsAnTkn, POSTag);
              } catch (IOException e) {
                throw new RuntimeException(
                    "Could not synthesize: " + infinitiveAsAnTkn + " with tag " + analyzedToken.getPOSTag(), e);
              }
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
          RuleMatch potentialRuleMatch = createRuleMatch(tokenReadings, possibleReplacements, sentence, infinitive);
          ruleMatches.add(potentialRuleMatch);
        }
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

}
