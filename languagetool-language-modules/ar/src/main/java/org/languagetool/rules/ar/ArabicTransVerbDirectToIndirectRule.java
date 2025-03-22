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
import org.languagetool.tagging.ar.ArabicTagManager;
import org.languagetool.tagging.ar.ArabicTagger;

import java.util.*;

import static java.lang.Math.min;

/**
 * @since 6.2
 */
public class ArabicTransVerbDirectToIndirectRule extends AbstractSimpleReplaceRule2 {

  public static final String AR_VERB_TRANS_DIRECT_TO_INDIRECT_REPLACE = "AR_VERB_TRANSITIVE_DIRECT_TO_INDIRECT";

  private static final String FILE_NAME = "/ar/verb_trans_direct_to_indirect.txt";
  private static final Locale AR_LOCALE = new Locale("ar");
  private final int MAX_CHUNK = 4;

  private final ArabicTagger tagger;
  private final ArabicTagManager tagmanager;
  private final ArabicSynthesizer synthesizer;
  private final List<Map<String, SuggestionWithMessage>> wrongWords;

  public ArabicTransVerbDirectToIndirectRule(ResourceBundle messages) {
    super(messages, new Arabic());
    tagger = new ArabicTagger();
    tagger.enableNewStylePronounTag();
    tagmanager = new ArabicTagManager();
    synthesizer = new ArabicSynthesizer(new Arabic());

    super.setCategory(Categories.MISC.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Inconsistency);
    addExamplePair(Example.wrong("قال <marker>كشفت</marker> الأمر الخفي."),
                   Example.fixed("قال <marker>كشفت عن</marker> الأمر الخفي."));

    // get wrong words from resource file
    wrongWords = getWrongWords(false);
  }

  @Override
  public String getId() {
    return AR_VERB_TRANS_DIRECT_TO_INDIRECT_REPLACE;
  }

  @Override
  public String getDescription() {
    return "َTransitive verbs corrected to indirect transitive";
  }

  @Override
  public final List<String> getFileNames() {
    return Collections.singletonList(FILE_NAME);
  }

  @Override
  public String getShort() {
    return "أفعال متعدية بحرف، يخطئ في تعديتها";
  }

  @Override
  public String getMessage() {
    return "'$match' الفعل خاطئ في التعدية بحرف: $suggestions";
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
    for (int i = 1; i < tokens.length; i++) {  // ignoring token 0, i.e., SENT_START
      AnalyzedTokenReadings token = tokens[i];
      int prevTokenIndex = i - 1;
      AnalyzedTokenReadings prevToken = prevTokenIndex > 0 ? tokens[prevTokenIndex] : null;
      String prevTokenStr = prevTokenIndex > 0 ? tokens[prevTokenIndex].getToken() : null;

      if (prevTokenStr != null) {
        // browse each verb with each preposition
        for (AnalyzedToken verbTok : prevToken.getReadings()) {
          // test if the first token is a verb
          boolean isCandidateVerb = isCandidateVerb(verbTok);

          // test if the preposition token is suitable for verb token (previous)
          List<String> prepositions = new ArrayList<>();
          String sugMsg = "";
          SuggestionWithMessage prepositionsWithMessage = getSuggestedPreposition(verbTok);
          if (prepositionsWithMessage != null) {
            prepositions = Arrays.asList(prepositionsWithMessage.getSuggestion().split("\\|"));
            sugMsg = prepositionsWithMessage.getMessage();
            sugMsg = sugMsg != null ? sugMsg : "";
          }
          // the current token can be a preposition or any words else
          // test if the token is in the suitable prepositions
          // browse all next  tokens to assure that proper preposition doesn't exist
          boolean isRightPreposition = false;
          AnalyzedTokenReadings currentTokenReading = token;

          int[] nextIndexes = getNextMatch(tokens, i, prepositions);
          if (nextIndexes[0] != -1) {
            int tokReadingPos = nextIndexes[0];
            isRightPreposition = true;
            currentTokenReading = tokens[tokReadingPos];
          }

          // the verb is attached and the next token is not the suitable preposition
          // we give the correct new form
          if (isCandidateVerb && !isRightPreposition) {
            String verb = inflectVerb(verbTok);
            // generate suggestion according to suggested prepositions
            // FIXED: test all suggestions
            StringBuilder replacement = new StringBuilder("");
            for (String proposition : prepositions) {
              String newPreposition = inflectSuggestedPreposition(proposition, verbTok);

              replacement.append("<suggestion>" + verb + " " + newPreposition + "</suggestion>&nbsp;");
            }
            String msg = "' الفعل " + prevTokenStr + " ' متعدٍ بحرف،" + sugMsg + ". فهل تقصد؟" + replacement;
            RuleMatch match = new RuleMatch(
              this, sentence, prevToken.getStartPos(), prevToken.getEndPos(),
              prevToken.getStartPos(), currentTokenReading.getEndPos(), msg, "خطأ في الفعل المتعدي بحرف");
            ruleMatches.add(match);
          }
        }
      } // end verbTok
    }
    return toRuleMatchArray(ruleMatches);
  }

  private boolean isCandidateVerb(AnalyzedToken mytoken) {
    if (getSuggestedPreposition(mytoken) != null) {
      return true;
    } else {
      return false;
    }
  }

  /* if the word is a transitive verb, we get proper preposition in order to test it*/
  private SuggestionWithMessage getSuggestedPreposition(AnalyzedToken mytoken) {
    // keep the suitable postags
    AnalyzedToken verbTok = mytoken;
    String verbLemma = verbTok.getLemma();
    String verbPostag = verbTok.getPOSTag();

    // if postag is attached
    // test if verb is in the verb list
    if (verbPostag != null) {
      // lookup in WrongWords
      SuggestionWithMessage verbLemmaMatch = wrongWords.get(wrongWords.size() - 1).get(verbLemma);

      // The lemma is found in the dictionary file
      if (verbLemmaMatch != null) {
        return verbLemmaMatch;
      }
    }

    return null;
  }

  private static boolean isRightPreposition(AnalyzedToken nextToken, List<String> prepositionList) {
    // We might have to test here if the next token  is the suitable preposition for the previous token as verb-token
    String nextTokenStr = nextToken.getLemma();
    return prepositionList.contains(nextTokenStr);
  }

  /* Lookup for next token matched */
  public int[] getNextMatch(AnalyzedTokenReadings[] tokens, int currentIndex, List<String> prepositions) {
    int tokReadIndex = currentIndex;
    int maxLength = min(currentIndex + MAX_CHUNK, tokens.length);
    int tokIndex = 0;
    int[] indexes = {-1, -1};
    // browse all next  tokens to assure that proper preposition doesn't exist
    boolean isRightPreposition = false;
    // used to save skipped tokens
    // initial as first token
    AnalyzedTokenReadings currentTokenReading = tokens[currentIndex];

    while (tokReadIndex < maxLength && !isRightPreposition) {
      currentTokenReading = tokens[tokReadIndex];
      tokIndex = 0;
      while (tokIndex < currentTokenReading.getReadings().size() && !isRightPreposition) {
        AnalyzedToken curTok = currentTokenReading.getReadings().get(tokIndex);
        isRightPreposition = isRightPreposition(curTok, prepositions);
        if (isRightPreposition) {
          if (isRightPreposition) {
            indexes[0] = tokReadIndex;
            indexes[1] = tokIndex;
          }
          return indexes;
        }
        tokIndex++;
      } // end while 2
      // increment
      tokReadIndex++;
    } // end while 1

    return indexes;
  }


  /* generate a new form according to a specific postag, this form is Un-Attached*/
  private String inflectVerb(AnalyzedToken token) {
    String word2 = synthesizer.setEnclitic(token, "");
    return word2;
  }

  /* generate a new form according to a specific postag, this form is Attached*/
  private String inflectSuggestedPreposition(String prepositionLemma, AnalyzedToken prevtoken) {

    String postag2 = "PRD;---;---";
    String suffix = tagger.getEnclitic(prevtoken);

    if (suffix.isEmpty()) {
      postag2 = "PR-;---;---";
    }
    AnalyzedToken token = new AnalyzedToken(prepositionLemma, postag2, prepositionLemma);
    String word2 = synthesizer.setEnclitic(token, suffix);
    return word2;

  }
}
