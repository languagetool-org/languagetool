/* LanguageTool, a natural language style checker
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.patterns.bitext;

import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.bitext.StringPair;
import org.languagetool.rules.IncorrectExample;
import org.languagetool.rules.bitext.IncorrectBitextExample;
import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.rules.patterns.Match;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.patterns.PatternRuleHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class BitextPatternRuleHandler extends PatternRuleHandler {

  private static final String SOURCE = "source";
  private static final String TARGET = "target";
  private static final String SRC_EXAMPLE = "srcExample";
  private static final String TRG_EXAMPLE = "trgExample";

  private final List<BitextPatternRule> rules = new ArrayList<>();

  private PatternRule srcRule;
  private PatternRule trgRule;

  private IncorrectExample trgExample;
  private IncorrectExample srcExample;

  private Language srcLang;

  private List<StringPair> correctExamples = new ArrayList<>();
  private List<IncorrectBitextExample> incorrectExamples = new ArrayList<>();

  List<BitextPatternRule> getBitextRules() {
    return rules;
  }

  // ===========================================================
  // SAX DocumentHandler methods
  // ===========================================================

  @Override
  public void startElement(String namespaceURI, String lName,
      String qName, Attributes attrs) throws SAXException {
    switch (qName) {
      case RULES:
        String languageStr = attrs.getValue("targetLang");
        language = Languages.getLanguageForShortCode(languageStr);
        break;
      case RULE:
        super.startElement(namespaceURI, lName, qName, attrs);
        correctExamples = new ArrayList<>();
        incorrectExamples = new ArrayList<>();
        break;
      case TARGET:
        startPattern(attrs);
        break;
      case SOURCE:
        srcLang = Languages.getLanguageForShortCode(attrs.getValue("lang"));
        break;
      default:
        super.startElement(namespaceURI, lName, qName, attrs);
        break;
    }
  }

  @Override
  public void endElement(String namespaceURI, String sName,
      String qName) throws SAXException {
    switch (qName) {
      case RULE:
        trgRule.setMessage(message.toString());
        for (Match m : suggestionMatches) {
          trgRule.addSuggestionMatch(m);
        }
        if (phrasePatternTokens.size() <= 1) {
          suggestionMatches.clear();
        }
        BitextPatternRule bRule = new BitextPatternRule(srcRule, trgRule);
        bRule.setCorrectBitextExamples(correctExamples);
        bRule.setIncorrectBitextExamples(incorrectExamples);
        bRule.setSourceLanguage(srcLang);
        rules.add(bRule);
        break;
      case SRC_EXAMPLE:
        srcExample = setExample();
        break;
      case TRG_EXAMPLE:
        trgExample = setExample();
        break;
      case SOURCE:
        srcRule = finalizeRule();
        break;
      case TARGET:
        trgRule = finalizeRule();
        break;
      case EXAMPLE:
        if (inCorrectExample) {
          correctExamples.add(new StringPair(srcExample.getExample(), trgExample.getExample()));
        } else if (inIncorrectExample) {
          StringPair examplePair = new StringPair(srcExample.getExample(), trgExample.getExample());
          if (trgExample.getCorrections().isEmpty()) {
            incorrectExamples.add(new IncorrectBitextExample(examplePair));
          } else {
            List<String> corrections = trgExample.getCorrections();
            incorrectExamples.add(new IncorrectBitextExample(examplePair, corrections));
          }
        }
        inCorrectExample = false;
        inIncorrectExample = false;
        inErrorTriggerExample = false;
        break;
      default:
        super.endElement(namespaceURI, sName, qName);
        break;
    }

  }

  private IncorrectExample setExample() {
    IncorrectExample example = null;
    if (inCorrectExample) {
      example = new IncorrectExample(correctExample.toString());
    } else if (inIncorrectExample) {
      if (exampleCorrection != null) {
        List<String> corrections = new ArrayList<>(Arrays.asList(exampleCorrection.toString().split("\\|")));
        if (exampleCorrection.toString().endsWith("|")) {  // suggestions plus an empty suggestion (split() will ignore trailing empty items)
          corrections.add("");
        }
        example = new IncorrectExample(incorrectExample.toString(), corrections);
      } else {
        example = new IncorrectExample(incorrectExample.toString());
      }
    } else if (inErrorTriggerExample) {
      throw new RuntimeException("'triggers_error' is not supported for bitext XML");
    }
    correctExample = new StringBuilder();
    incorrectExample = new StringBuilder();
    exampleCorrection = null;
    return example;
  }

  private PatternRule finalizeRule() {
    PatternRule rule = null;
    if (phrasePatternTokens.isEmpty()) {
      rule = new PatternRule(id, language, patternTokens,
          name, "", shortMessage.toString());
      prepareRule(rule);
    } else {
      if (!patternTokens.isEmpty()) {
        for (List<PatternToken> ph : phrasePatternTokens) {
          ph.addAll(new ArrayList<>(patternTokens));
        }
      }
      for (List<PatternToken> phrasePatternToken : phrasePatternTokens) {
        processElement(phrasePatternToken);
        rule = new PatternRule(id, language, phrasePatternToken,
            name, message.toString(), shortMessage.toString(), "",
            phrasePatternTokens.size() > 1);
        prepareRule(rule);
      }
    }
    patternTokens.clear();
    if (phrasePatternTokens != null) {
      phrasePatternTokens.clear();
    }
    startPositionCorrection = 0;
    endPositionCorrection = 0;
    return rule;
  }

}
