/*
 * LanguageTool, a natural language style checker
 * Copyright (C) 2022 Sohaib Afifi, Taha Zerrouki
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
package org.languagetool.rules.ar;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.language.Arabic;
import org.languagetool.rules.*;
import org.languagetool.synthesis.ar.ArabicSynthesizer;
import org.languagetool.tagging.ar.ArabicTagger;

import java.util.*;

/**
 * @since 6.2
 */
public class ArabicInflectedOneWordReplaceRule extends AbstractSimpleReplaceRule2 {

  public static final String AR_INFLECTED_ONE_WORD_REPLACE = "AR_INFLECTED_ONE_WORD";

  private static final String FILE_NAME = "/ar/inflected_one_word.txt";
  private static final Locale AR_LOCALE = new Locale("ar");

  private final ArabicTagger tagger;
  private final ArabicSynthesizer synthesizer;
  private final List<Map<String, SuggestionWithMessage>> wrongWords;

  public ArabicInflectedOneWordReplaceRule(ResourceBundle messages) {
    super(messages, new Arabic());
    tagger = new ArabicTagger();
    tagger.enableNewStylePronounTag();
    synthesizer = new ArabicSynthesizer(new Arabic());

    super.setCategory(Categories.MISC.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Inconsistency);
    addExamplePair(Example.wrong("أجريت <marker>أبحاثا</marker> في المخبر"),
                   Example.fixed("أجريت <marker>بحوثا</marker> في المخبر."));

    // get wrong words from resource file
    wrongWords = getWrongWords(false);
  }

  @Override
  public String getId() {
    return AR_INFLECTED_ONE_WORD_REPLACE;
  }


  @Override
  public final List<String> getFileNames() {
    return Collections.singletonList(FILE_NAME);
  }

  @Override
  public String getDescription() {
    return "قاعدة تطابق الكلمات التي يجب تجنبها وتقترح تصويبا لها";
  }

  @Override
  public String getShort() {
    return "خطأ، يفضل أن  يقال:";
  }

  @Override
  public String getMessage() {
    return " لا تقل '$match' بل قل: $suggestions";
  }

  @Override
  public String getSuggestionsSeparator() {
    return " أو ";
  }

  @Override
  public Locale getLocale() {
    return AR_LOCALE;
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    if (wrongWords.size() == 0) {
      return toRuleMatchArray(ruleMatches);
    }
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    for (AnalyzedTokenReadings token : tokens) {  // ignoring token 0, i.e., SENT_START
      // browse each word with
      for (AnalyzedToken wordTok : token.getReadings()) {
        // test if the first token is a to replace word
        boolean isCandidateWord = isCandidateWord(wordTok);
        if (isCandidateWord) {
          // get suggestions
          List<String> propositions = new ArrayList<>();
          String sugMsg = "";
          SuggestionWithMessage propositionsWithMessage = getSuggestedWords(wordTok);
          if (propositionsWithMessage != null) {
            propositions = Arrays.asList(propositionsWithMessage.getSuggestion().split("\\|"));
            sugMsg = propositionsWithMessage.getMessage();
            sugMsg = sugMsg != null ? sugMsg : "";
          }

          // generate suggestion according to suggested word
          StringBuilder replacement = new StringBuilder("");
          for (String proposition : propositions) {
            List<String> inflectedWordList = inflectSuggestedWords(proposition, wordTok);
            for (String w : inflectedWordList) {
              replacement.append("<suggestion>" + w + "</suggestion>&nbsp;");
            }
          }
          String msg = "' الكلمة خاطئة " + token.getToken() + " ' ،" + sugMsg + ". استعمل  " + replacement;
          RuleMatch match = new RuleMatch(
            this, sentence, token.getStartPos(), token.getEndPos(),
            token.getStartPos(), token.getEndPos(), msg, "خطأ في استعمال كلمة:" + sugMsg);
          ruleMatches.add(match);
        }
      } // end wordTok
    }
    return toRuleMatchArray(ruleMatches);
  }

  /* return True if the word is a candidate to be replaced in text rule file */
  private boolean isCandidateWord(AnalyzedToken mytoken) {
    if (getSuggestedWords(mytoken) != null) {
      return true;
    } else {
      return false;
    }
  }


  /* if the word is in text rules file, return the suggested word*/
  private SuggestionWithMessage getSuggestedWords(AnalyzedToken mytoken) {
    // keep the suitable postags
    AnalyzedToken wordTok = mytoken;
    String wordLemma = wordTok.getLemma();
    String wordPostag = wordTok.getPOSTag();

    // if postag is attached
    // test if word is in the word list
    if (wordPostag != null) {
      // lookup in WrongWords
      SuggestionWithMessage wordLemmaMatch = wrongWords.get(wrongWords.size() - 1).get(wordLemma);

      // The lemma is found in the dictionary file
      if (wordLemmaMatch != null) {
        return wordLemmaMatch;
      }
    }

    return null;
  }


  /* generate a new form according to a specific postag,*/
  private List<String> inflectSuggestedWords(String targetLemma, AnalyzedToken sourcetoken) {
    return synthesizer.inflectLemmaLike(targetLemma, sourcetoken);
  }
}
