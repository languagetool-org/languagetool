/*
 * LanguageTool, a natural language style checker
 * Copyright (C) 2021 Sohaib Afifi, Taha Zerrouki
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

public class ArabicTransVerbRule extends AbstractSimpleReplaceRule2 {

  public static final String AR_VERB_TRANS_INDIRECT_REPLACE = "AR_VERB_TRANSITIVE_IINDIRECT";

  private static final String FILE_NAME = "/ar/verb_trans_to_untrans2.txt";
  private static final Locale AR_LOCALE = new Locale("ar");

  private final ArabicTagger tagger;
  private final ArabicTagManager tagmanager;
  private final List<Map<String, SuggestionWithMessage>> wrongWords;

  public ArabicTransVerbRule(ResourceBundle messages) {
    super(messages, new Arabic());
    tagger = new ArabicTagger();
    tagger.enableNewStylePronounTag();
    tagmanager = new ArabicTagManager();

    super.setCategory(Categories.MISC.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Misspelling);
    addExamplePair(Example.wrong("The train arrived <marker>a hour</marker> ago."),
                   Example.fixed("The train arrived <marker>an hour</marker> ago."));

    // get wrong words from resource file
    wrongWords = getWrongWords(false);
  }

  @Override
  public String getId() {
    return AR_VERB_TRANS_INDIRECT_REPLACE;
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
    int prevTokenIndex = 0;
    for (int i = 1; i < tokens.length; i++) {  // ignoring token 0, i.e., SENT_START
      AnalyzedTokenReadings token = tokens[i];
      AnalyzedTokenReadings prevToken = prevTokenIndex > 0 ? tokens[prevTokenIndex] : null;
      String prevTokenStr = prevTokenIndex > 0 ? tokens[prevTokenIndex].getToken() : null;

      if (prevTokenStr != null) {
        // test if the first token is a verb
        boolean isAttachedVerbTransitive = isAttachedTransitiveVerb(prevToken);

        // test if the preposition token is suitable for verb token (previous)
        List<String> prepositions = getProperPrepositionForTransitiveVerb(prevToken);

        boolean isRightPreposition = isRightPreposition(token, prepositions);

        // the verb is attached and the next token is not the suitable preposition
        // we give the correct new form
        if (isAttachedVerbTransitive && !isRightPreposition) {
          String verb = getCorrectVerbForm(tokens[prevTokenIndex]);

          // generate suggestion according to suggested prepositions
          String newPreposition = prepositions.get(0);
          String preposition = getCorrectPrepositionForm(newPreposition, prevToken);

          String replacement = verb + " " + preposition;
          String msg = "قل <suggestion>" + replacement + "</suggestion> بدلا من '" + prevTokenStr + "' لأنّ الفعل " +
            " متعد بحرف  .";
          RuleMatch match = new RuleMatch(
            this, sentence, tokens[prevTokenIndex].getStartPos(), tokens[prevTokenIndex].getEndPos(),
            tokens[prevTokenIndex].getStartPos(), token.getEndPos(), msg, "خطأ في الفعل المتعدي بحرف");
          ruleMatches.add(match);
        }
      }

      if (isAttachedTransitiveVerb(token)) {
        prevTokenIndex = i;
      } else {
        prevTokenIndex = 0;
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

  private boolean isAttachedTransitiveVerb(AnalyzedTokenReadings mytoken) {
    List<AnalyzedToken> verbTokenList = mytoken.getReadings();

    for (AnalyzedToken verbTok : verbTokenList) {
      String verbLemma = verbTok.getLemma();
      String verbPostag = verbTok.getPOSTag();

      // if postag is attached
      // test if verb is in the verb list
      if (verbPostag != null)// && verbPostag.endsWith("H"))
      {
        // lookup in WrongWords
        SuggestionWithMessage verbLemmaMatch = wrongWords.get(wrongWords.size() - 1).get(verbLemma);
        // The lemma is found in the dictionary file
        if (verbLemmaMatch != null) {
          return true;
        }
      }

    }
    return false;
  }

  /* if the word is a transitive verb, we got proper preposition inorder to test it*/
  private List<String> getProperPrepositionForTransitiveVerb(AnalyzedTokenReadings mytoken) {
    List<AnalyzedToken> verbTokenList = mytoken.getReadings();

    // keep the suitable postags
    List<String> replacements = new ArrayList<>();

    for (AnalyzedToken verbTok : verbTokenList) {
      String verbLemma = verbTok.getLemma();
      String verbPostag = verbTok.getPOSTag();

      // if postag is attached
      // test if verb is in the verb list
      if (verbPostag != null) {
        // lookup in WrongWords
        SuggestionWithMessage verbLemmaMatch = wrongWords.get(wrongWords.size() - 1).get(verbLemma);
        // The lemma is found in the dictionary file
        if (verbLemmaMatch != null) {
          replacements = Arrays.asList(verbLemmaMatch.getSuggestion().split("\\|"));
          return replacements;
        }
      }
    }
    return replacements;
  }

  private static boolean isRightPreposition(AnalyzedTokenReadings nextToken, List<String> prepositionList) {
    String nextTokenStr = nextToken.getReadings().get(0).getLemma();
    return prepositionList.contains(nextTokenStr);
  }

  private String getCorrectVerbForm(AnalyzedTokenReadings token) {
    return generateUnattachedNewForm(token);
  }

  private String getCorrectPrepositionForm(String prepositionLemma, AnalyzedTokenReadings prevtoken) {
    return generateAttachedNewForm(prepositionLemma, prevtoken);
  }

  /* generate a new form according to a specific postag*/
  private String generateNewForm(String word, String posTag, char flag) {
    // generate new from word form
    String newposTag = tagmanager.setFlag(posTag, "PRONOUN", flag);
    if (flag != '-')
      newposTag = tagmanager.setFlag(newposTag, "OPTION", 'D');
    // generate the new preposition according to modified postag
    AnalyzedToken prepAToken = new AnalyzedToken(word, newposTag, word);
    String[] newwordList = ArabicSynthesizer.INSTANCE.synthesize(prepAToken, newposTag);
    String newWord = "";
    if (newwordList.length != 0) {
      newWord = newwordList[0];
    }
    return newWord;
  }

  /* generate a new form according to a specific postag, this form is Un-Attached*/
  private String generateUnattachedNewForm(AnalyzedTokenReadings token) {
    String lemma = token.getReadings().get(0).getLemma();
    String postag = token.getReadings().get(0).getPOSTag();
    return generateNewForm(lemma, postag, '-');
  }

  /* generate a new form according to a specific postag, this form is Attached*/
  private String generateAttachedNewForm(String prepositionLemma, AnalyzedTokenReadings prevtoken) {
    String postag = "PR-;---;---";
    String prevPosTag = prevtoken.getReadings().get(0).getPOSTag();
    char flag = tagmanager.getFlag(prevPosTag, "PRONOUN");
    return generateNewForm(prepositionLemma, postag, flag);
  }
}