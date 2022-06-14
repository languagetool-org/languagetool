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

public class ArabicTransVerbIndirectToIndirectRule extends AbstractSimpleReplaceRule2 {

  public static final String AR_VERB_TRANS_INDIRECT_TO_INDIRECT_REPLACE = "AR_VERB_TRANSITIVE_INDIRECT_TO_INDIRECT";

  private static final String FILE_NAME = "/ar/verb_trans_indirect_to_indirect.txt";
  private static final Locale AR_LOCALE = new Locale("ar");

  private final ArabicTagger tagger;
  private final ArabicTagManager tagmanager;
  private final ArabicSynthesizer synthesizer;
  private final List<Map<String, SuggestionWithMessage>> wrongWords;
  private final int MAX_CHUNK = 4;


  public ArabicTransVerbIndirectToIndirectRule(ResourceBundle messages) {
    super(messages, new Arabic());
    tagger = new ArabicTagger();
    tagger.enableNewStylePronounTag();
    tagmanager = new ArabicTagManager();
    synthesizer = new ArabicSynthesizer(new Arabic());

    super.setCategory(Categories.MISC.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Inconsistency);
    //FIXME: choose another example
    addExamplePair(Example.wrong("الولد <marker>يتردد على</marker> المعهد."),
      Example.fixed("الولد <marker>يتردد إلى</marker> المعهد."));

    // get wrong words from resource file
    wrongWords = getWrongWords(false);
  }

  @Override
  public String getId() {
    return AR_VERB_TRANS_INDIRECT_TO_INDIRECT_REPLACE;
  }

  @Override
  public String getDescription() {
    return "َIntransitive verbs corrected to indirect transitive";
  }

  @Override
  public final List<String> getFileNames() {
    return Collections.singletonList(FILE_NAME);
  }

  @Override
  public String getShort() {
    return "أفعال متعدية بحرف، الصواب تعديتها بحرف آخر";
  }

  @Override
  public String getMessage() {
    return "'$match' الفعل يتعدى بحرف آخرف: $suggestions";
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
      AnalyzedTokenReadings nextToken = i + 1 < tokens.length ? tokens[i + 1] : null;
      String prevTokenStr = prevTokenIndex > 0 ? tokens[prevTokenIndex].getToken() : null;

      if (prevTokenStr != null) {
        // test if the first token is a verb
//        boolean is_candidate_verb = isCandidateVerb(prevToken, token);

        // if the string composed of two tokens is candidate,
        // get suggestion preposition
//        if(is_candidate_verb) {
        // test if the preposition token is suitable for verb token (previous)
        List<String> prepositions = new ArrayList<>();
        String sug_msg = "";
        StringBuilder replacement = new StringBuilder("");
        AnalyzedTokenReadings current_token_reading = token;
        // browse each verb with each preposition
        for (AnalyzedToken verbTok : prevToken.getReadings()) {
          if (tagmanager.isVerb(verbTok.getPOSTag())) {

            // browse all instance of tokens
            String skippedString = "";
            boolean is_candidate_case = false;
            // initial as first token

            AnalyzedToken current_token = token.getReadings().get(0);
            int[] next_indexes = getNextMatch(tokens, i, verbTok);

            if (next_indexes[0] != -1) {
              int tokReadingPos = next_indexes[0];
              int tokPos = next_indexes[1];
              is_candidate_case = true;
              current_token_reading = tokens[tokReadingPos];
              current_token = current_token_reading.getReadings().get(tokPos);
              skippedString = getSkippedString(tokens, i, tokReadingPos);
//              System.out.println(" Skipped2:"+skippedString + " skipped:"+ skippedString.toString());
//              System.out.println(" current token:"+current_token_reading.getToken() + " lemma:"+ current_token.getLemma());
            }
//            for(AnalyzedToken current_token: token.getReadings()) {
            SuggestionWithMessage prepositionsWithMessage = getSuggestedPreposition(verbTok, current_token);
//              System.out.println("ArabicIntransIndirectVerbRule.java: verb"+verbTok.getLemma()+" replaced: "+current_token.getToken()+" math:"+Boolean.toString(prepositionsWithMessage != null));

            if (prepositionsWithMessage != null) {

//              if (is_candidate_case) {
              prepositions = Arrays.asList(prepositionsWithMessage.getSuggestion().split("\\|"));
              sug_msg = prepositionsWithMessage.getMessage();
              sug_msg = sug_msg != null ? sug_msg : "";
              for (String prep : prepositions) {
                String inflectPrep = inflectSuggestedPreposition(current_token, prep);
//                  System.out.println("ArabicIntransIndirectVerbRule.java: verb"+verbTok.getLemma()+" replaced: "+current_token.getToken()+" prep:"+prep +" suggestion:"+inflectPrep);
                replacement.append("<suggestion>" + prevTokenStr + " " + skippedString + " " + inflectPrep + "</suggestion>");
              }
//              } // if preposition match
//            }// if verb

//          } // for verbtok

              String msg = "الفعل ' " + prevTokenStr + " ' متعدِ بحرف آخر ،" + sug_msg + ". فهل تقصد؟" + replacement.toString();
              RuleMatch match = new RuleMatch(
                this, sentence, prevToken.getStartPos(), current_token_reading.getEndPos(),
                prevToken.getStartPos(), current_token_reading.getEndPos(), msg, "خطأ في الفعل المتعدي بحرف ");
              ruleMatches.add(match);
            }
          } // if preposition match

        }// if verb
      } // for verbtok

    }
    return toRuleMatchArray(ruleMatches);
  }

  /* lookup for candidat verbs with preposition */
  private boolean isCandidateVerb(AnalyzedTokenReadings mytoken, AnalyzedTokenReadings nexttoken) {
    return (getSuggestedPreposition(mytoken, nexttoken) != null);
  }

  private String getSkippedString(AnalyzedTokenReadings[] tokens, int start, int end) {
    StringBuilder skipped = new StringBuilder("");
    for (int i = start; i < end; i++) {
      skipped.append(tokens[i].getToken());
      skipped.append(" ");
    }

    return skipped.toString();

  }

  /* lookup for candidat verbs with preposition */
  private SuggestionWithMessage getSuggestedPreposition(AnalyzedTokenReadings mytoken, AnalyzedTokenReadings nexttoken) {

    List<AnalyzedToken> verbTokenList = mytoken.getReadings();
    List<AnalyzedToken> prepTokenList = nexttoken.getReadings();
    for (AnalyzedToken verbTok : verbTokenList) {
      String verbLemma = verbTok.getLemma();
      String verbPostag = verbTok.getPOSTag();

      // if postag is attached
      // test if verb is in the verb list
      if (verbPostag != null && tagmanager.isVerb(verbPostag)) {
        for (AnalyzedToken prepTok : prepTokenList) {
          String prepLemma = prepTok.getLemma();
          String prepPostag = prepTok.getPOSTag();
          // FIXME: add isBreak to tagmannager

          if (prepPostag != null && tagmanager.isStopWord(prepPostag) && !tagmanager.isBreak(prepPostag)) {
            // the candidate string is composed of verb + preposition
            String candidateString = verbLemma + " " + prepLemma;
            // lookup in WrongWords
            SuggestionWithMessage verbLemmaMatch = wrongWords.get(wrongWords.size() - 1).get(candidateString);
            // The lemma is found in the dictionary file
            if (verbLemmaMatch != null)
              return verbLemmaMatch;
          }
          // case of Lam Jar and Beh Jar as indirect transitive preposition
          // a noun with a jar but without conjugation
          // لعبت بالكرة:right
          // لعبت وبالكرةWrong
          else if (prepPostag != null && tagmanager.isNoun(prepPostag) &&
            (tagmanager.hasJar(prepPostag) && !tagmanager.hasConjunction(prepPostag))) {
            // the candidate string is composed of verb + preposition
            prepLemma = tagger.getProclitic(prepTok);
            String candidateString = verbLemma + " " + prepLemma;

            // lookup in WrongWords
            SuggestionWithMessage verbLemmaMatch = wrongWords.get(wrongWords.size() - 1).get(candidateString);
            // The lemma is found in the dictionary file
            if (verbLemmaMatch != null)
              return verbLemmaMatch;
          }
        }

      }
    }
    return null;
  }

  /* lookup for candidat verbs with preposition */
  private SuggestionWithMessage getSuggestedPreposition(AnalyzedToken verbTok, AnalyzedToken prepTok) {

    String verbLemma = verbTok.getLemma();
    String verbPostag = verbTok.getPOSTag();

    // if postag is attached
    // test if verb is in the verb list
    if (verbPostag != null && tagmanager.isVerb(verbPostag)) {
      String prepLemma = prepTok.getLemma();
      String prepPostag = prepTok.getPOSTag();

      if (prepPostag != null && tagmanager.isStopWord(prepPostag) && !tagmanager.isBreak(prepPostag)) {
        // the candidate string is composed of verb + preposition
        String candidateString = verbLemma + " " + prepLemma;
        // lookup in WrongWords
        SuggestionWithMessage verbLemmaMatch = wrongWords.get(wrongWords.size() - 1).get(candidateString);
        // The lemma is found in the dictionary file
        if (verbLemmaMatch != null)
          return verbLemmaMatch;
      }
      // case of Lam Jar and Beh Jar as indirect transitive preposition
      // a noun with a jar but without conjugation
      // لعبت بالكرة:right
      // لعبت وبالكرةWrong
      else if (prepPostag != null && tagmanager.isNoun(prepPostag) &&
        (tagmanager.hasJar(prepPostag) && !tagmanager.hasConjunction(prepPostag))) {
        // the candidate string is composed of verb + preposition
        prepLemma = tagger.getProclitic(prepTok);
        String candidateString = verbLemma + " " + prepLemma;

        // lookup in WrongWords
        SuggestionWithMessage verbLemmaMatch = wrongWords.get(wrongWords.size() - 1).get(candidateString);
        // The lemma is found in the dictionary file
        if (verbLemmaMatch != null)
          return verbLemmaMatch;
      }

    }
    return null;
  }


  /* generate a new form according to a specific postag, this form is Attached*/
  private String inflectSuggestedPreposition(AnalyzedToken currentPrepTok, String suggPrepLemma) {
    // extract verb postag
    // extract preposition postag
    // get pronoun flag
    // regenerate verb form with original postag and new flag to add Pronoun if exists
    // أربع حالات
    //1- الحالي حرف منفصل والتصحيح حرف منفصل
    //2- الحالي حرف منفصل والتصحيح حرف متصل
    //3- الحالي اسما مجرورا والتصحيح حرف منفصل
    //4- الحالي اسما مجرورا والتصحيح حرف متصل

    // الجالة الأولى
    String postag = currentPrepTok.getPOSTag();
    String currentWord = currentPrepTok.getToken();
    boolean isAttachedJar = (suggPrepLemma.equals("ب") || suggPrepLemma.equals("ل"));
    String suffix = tagger.getEnclitic(currentPrepTok);
    String newWord = "";
    //1- الحالي حرف منفصل والتصحيح حرف منفصل
    if (tagmanager.isStopWord(postag)) {
      if (!isAttachedJar) {
        String newpostag = "PRD;---;---";
        AnalyzedToken suggPrepToken = new AnalyzedToken(suggPrepLemma, newpostag, suggPrepLemma);
        newWord = synthesizer.setEnclitic(suggPrepToken, suffix);
      } else {     //2- الحالي حرف منفصل والتصحيح حرف متصل
        newWord = suggPrepLemma + suffix;
        //FIXME : add the Attached jar preposition to next word
      }

    }
    //3- الحالي اسما مجرورا والتصحيح حرف منفصل
    else if (tagmanager.isNoun(postag)) {
      if (!isAttachedJar) {
        // remove jar procletic if exists
        // add unattached jar and a space
        currentWord = synthesizer.setJarProcletic(currentPrepTok, "");
        newWord = suggPrepLemma + " " + currentWord;
      } else {
        //4- الحالي اسما مجرورا والتصحيح حرف متصل
        // Add the attached Jar to the current Noun
        newWord = synthesizer.setJarProcletic(currentPrepTok, suggPrepLemma);
      }
    }

    return newWord;
    // FIXME: to remove

  }

  /* Lookup for next token matched */
  public int[] getNextMatch(AnalyzedTokenReadings[] tokens, int current_index, AnalyzedToken verbToken) {
    int tokRead_index = current_index;
    int max_length = min(current_index + MAX_CHUNK, tokens.length);


    int tokIndex = 0;
    int[] indexes = {-1, -1};
    // browse all next  tokens to assure that proper preposition doesn't exist
    boolean is_wrong_preposition = false;
    // used to save skipped tokens
    // initial as first token
    AnalyzedTokenReadings current_token_reading = tokens[current_index];

    while (tokRead_index < max_length && !is_wrong_preposition) {
      current_token_reading = tokens[tokRead_index];
      tokIndex = 0;
      while (tokIndex < current_token_reading.getReadings().size() && !is_wrong_preposition) {
        AnalyzedToken curTok = current_token_reading.getReadings().get(tokIndex);
        SuggestionWithMessage prepositionsWithMessage = getSuggestedPreposition(verbToken, curTok);

        is_wrong_preposition = (prepositionsWithMessage != null);
        if (is_wrong_preposition) {
          indexes[0] = tokRead_index;
          indexes[1] = tokIndex;
          return indexes;
        }
        tokIndex++;
      } // end while 2
      // increment
      tokRead_index++;
    } // end while 1

    return indexes;
  }
}
