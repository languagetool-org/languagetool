/* LanguageTool, a natural language style checker 
 * Copyright (C) 2013 Andriy Rysin
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
package org.languagetool.rules.uk;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.uk.LemmaHelper.Dir;
import org.languagetool.tagging.uk.PosTagHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A rule that checks if noun and verb agree
 * 
 * @author Andriy Rysin
 * @since 3.6
 */
public class TokenAgreementNounVerbRule extends Rule {
  
  private static final Logger logger = LoggerFactory.getLogger(TokenAgreementNounVerbRule.class);

  private static final Pattern NOUN_V_NAZ_PATTERN = Pattern.compile("noun.*:v_naz.*");


  public TokenAgreementNounVerbRule(ResourceBundle messages) throws IOException {
    super.setCategory(Categories.MISC.getCategory(messages));
//    setDefaultOff();
  }

  @Override
  public final String getId() {
    return "UK_NOUN_VERB_INFLECTION_AGREEMENT";
  }

  @Override
  public String getDescription() {
    return "Узгодження іменника та дієслова за родом, числом та особою";
  }

  public String getShort() {
    return "Узгодження іменника з дієсловом";
  }

  /**
   * Indicates if the rule is case-sensitive. 
   * @return true if the rule is case-sensitive, false otherwise.
   */
  public boolean isCaseSensitive() {
    return false;
  }
  
  
  private static class State {
    int nounPos;
    List<AnalyzedToken> nounTokenReadings = new ArrayList<>(); 
    AnalyzedTokenReadings nounAnalyzedTokenReadings = null;
    List<AnalyzedToken> adjTokenReadings = new ArrayList<>(); 
  }
  

  @Override
  public final RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();    

    State state = null;

    for (int i = 1; i < tokens.length; i++) {
      AnalyzedTokenReadings tokenReadings = tokens[i];
      String cleanToken = tokenReadings.getCleanToken();

      String posTag0 = tokenReadings.getAnalyzedToken(0).getPOSTag();

      if( posTag0 == null ) {
        state = null;
        continue;
      }

      if( state == null ) {
        // no need to start checking on last token or if no noun
        if( i == tokens.length - 1 )
          continue;
      }

//      if( LemmaHelper.hasLemma(tokenReadings, Arrays.asList("як")) ) {
//        state = null;
//        continue;
//      }
    

      if( PosTagHelper.hasPosTag(tokenReadings, NOUN_V_NAZ_PATTERN)
          || Arrays.asList("яка").contains(cleanToken) ) {
        state = new State();

        for (AnalyzedToken token: tokenReadings) {
          String nounPosTag = token.getPOSTag();

          if( nounPosTag == null ) { // can happen for words with \u0301 or \u00AD
            continue;
          }

//          if( nounPosTag.startsWith("<") ) {
//            state = null;
//            break;
//          }
          if( "який".equals(token.getLemma()) && token.getPOSTag().contains(":f:v_naz") ) {
            state.nounPos = i;
            state.nounTokenReadings.add(token);
            state.nounAnalyzedTokenReadings = tokenReadings;
          }
          else if( i >= 3 && "хто".equalsIgnoreCase(cleanToken) 
              && ",".equals(tokens[i-1].getToken()) 
              && Arrays.asList("те").contains(StringUtils.defaultIfEmpty(tokens[i-2].getCleanToken(), "").toLowerCase())
              && LemmaHelper.tokenSearch(tokens, i+1, Pattern.compile("verb.*:f\\b.*"), null, Pattern.compile("part"), Dir.FORWARD) > 0 ) {
            // ignore: про те, хто була ця клята Пандора
            state = null;
            break;
          }
          else if( i >= 3 && "хто".equalsIgnoreCase(cleanToken) 
              && ",".equals(tokens[i-1].getToken()) 
              && Arrays.asList("ті", "всі").contains(StringUtils.defaultIfEmpty(tokens[i-2].getCleanToken(), "").toLowerCase())
              && LemmaHelper.tokenSearch(tokens, i+1, Pattern.compile("verb.*:p\\b.*"), null, Pattern.compile("part"), Dir.FORWARD) > 0 ) {
            state.nounPos = i-2;
            state.nounTokenReadings.addAll(PosTagHelper.filter(tokens[i-2].getReadings(), Pattern.compile("adj.*")));
            state.nounAnalyzedTokenReadings = tokens[i-2];
          }
          else if( nounPosTag.startsWith("noun") && nounPosTag.contains("v_naz") ) {
            state.nounPos = i;
            state.nounTokenReadings.add(token);
            state.nounAnalyzedTokenReadings = tokenReadings;
          }
          else if( nounPosTag.startsWith("noun") && nounPosTag.contains("v_kly") ) {
            // ignore
          }
          else if( PosTagHelper.isPredictOrInsert(token) ) {
            // ignore
          }
          else if( token.getPOSTag().matches("adj:.:(v_naz|v_kly).*")
              || (token.getPOSTag().startsWith("adj:m:v_zna:rinanim") 
                  && ! PosTagHelper.hasPosTagStart(tokens[i-1], "prep"))
              && ! Arrays.asList("кожен", "інший", "старий", "черговий").contains(token.getToken().toLowerCase()) ) {
            state.adjTokenReadings.add(token);
          }
          else {
            state = null;
            break;
          }
        }

        continue;
      }
      
      if( state == null )
        continue;

      if( Arrays.asList("не", "б", "би", "бодай").contains(tokenReadings.getToken()) )
        continue;

      if( PosTagHelper.hasPosTagPartAll(tokenReadings, "adv") )
        continue;

      // see if we get a following verb
//       System.err.println("Check for verb: " + tokenReadings);

      List<AnalyzedToken> verbTokenReadings = new ArrayList<>(); 
      for (AnalyzedToken token: tokenReadings) {
        String verbPosTag = token.getPOSTag();

        if( verbPosTag == null // can happen for words with \u0301 or \u00AD
            || verbPosTag.equals(JLanguageTool.SENTENCE_END_TAGNAME)
            || verbPosTag.equals(JLanguageTool.PARAGRAPH_END_TAGNAME)) {
          continue;
        }

        if( verbPosTag.startsWith("<") ) {
          verbTokenReadings.clear();
          break;
        }

        if( verbPosTag.startsWith("verb") ) {

          verbTokenReadings.add(token);
        }
        else if( PosTagHelper.isPredictOrInsert(token) ) {
          // ignore
        }
        else {
          verbTokenReadings.clear();
          break;
        }
      }

      // no slave token - restart

      if( verbTokenReadings.isEmpty() ) {
        state = null;
        continue;
      }

      logger.debug("=== Checking\n\t{}\n\t{}", state.nounTokenReadings, verbTokenReadings);

      // perform the check

      List<VerbInflectionHelper.Inflection> masterInflections = VerbInflectionHelper.getNounInflections(state.nounTokenReadings);

      List<VerbInflectionHelper.Inflection> slaveInflections = VerbInflectionHelper.getVerbInflections(verbTokenReadings);

      logger.debug("\t\t{}\n\t{}", masterInflections, slaveInflections);

      if( Collections.disjoint(masterInflections, slaveInflections) ) {
        if( TokenAgreementNounVerbExceptionHelper.isException(tokens, state.nounPos, i, masterInflections, slaveInflections, state.nounTokenReadings, verbTokenReadings)) {
          state.nounTokenReadings.clear();
          break;
        }

        if( logger.isDebugEnabled() ) {
          logger.debug(MessageFormat.format("=== Found noun/verb mismatch\n\t{0}\n\t{1}",
              state.nounAnalyzedTokenReadings.getToken() + ": " + masterInflections + " // " + state.nounAnalyzedTokenReadings,
            verbTokenReadings.get(0).getToken() + ": " + slaveInflections+ " // " + verbTokenReadings));
        }
        
        String msg = String.format("Не узгоджено %s з дієсловом: \"%s\" (%s) і \"%s\" (%s)",
            LemmaHelper.hasLemma(state.nounTokenReadings, Arrays.asList("який")) ? "займенник" : "іменник",
                state.nounTokenReadings.get(0).getToken(), formatInflections(masterInflections, true), 
            verbTokenReadings.get(0).getToken(), formatInflections(slaveInflections, false));
        RuleMatch potentialRuleMatch = new RuleMatch(this, sentence, state.nounAnalyzedTokenReadings.getStartPos(), tokenReadings.getEndPos(), msg, getShort());
        ruleMatches.add(potentialRuleMatch);
      }

      state = null;
    }

    return toRuleMatchArray(ruleMatches);
  }


  private static String formatInflections(List<VerbInflectionHelper.Inflection> inflections, boolean noun) {

    Collections.sort(inflections);

    List<String> list = new ArrayList<>();

    for (VerbInflectionHelper.Inflection inflection : inflections) {
      String str = "";
      if (inflection.gender != null) {
        str = PosTagHelper.GENDER_MAP.get(inflection.gender);
      }
      else {
        if( inflection.person != null ) {
          str = PosTagHelper.PERSON_MAP.get(inflection.person);
        }
        if( inflection.plural != null ) {
          if( str.length() > 0 ) {
            str += " ";
          }
          str += PosTagHelper.GENDER_MAP.get(inflection.plural);
        }
      }
      list.add(str);
    }

    LinkedHashSet<String> uniqeList = new LinkedHashSet<>(list);

    return StringUtils.join(uniqeList, ", ");
  }



}
