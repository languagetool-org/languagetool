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
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.uk.InflectionHelper.Inflection;
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
public class TokenAgreementVerbNounRule extends Rule {
  
  private static final Logger logger = LoggerFactory.getLogger(TokenAgreementVerbNounRule.class);

  public TokenAgreementVerbNounRule(ResourceBundle messages) throws IOException {
    super.setCategory(Categories.MISC.getCategory(messages));
//    setDefaultOff();
  }

  @Override
  public final String getId() {
    return "UK_VERB_NOUN_INFLECTION_AGREEMENT";
  }

  @Override
  public String getDescription() {
    return "Узгодження дієслова та іменника за відмінком";
  }

  public String getShort() {
    return "Узгодження дієслова з іменником";
  }

  /**
   * Indicates if the rule is case-sensitive. 
   * @return true if the rule is case-sensitive, false otherwise.
   */
  public boolean isCaseSensitive() {
    return false;
  }
  
  
  static class State {
    int verbPos;
    int nounPos;
    List<AnalyzedToken> verbTokenReadings = new ArrayList<>(); 
    AnalyzedTokenReadings verbAnalyzedTokenReadings = null;
//    List<AnalyzedToken> nounAdjTokenReadings = new ArrayList<>(); 
    List<org.languagetool.rules.uk.VerbInflectionHelper.Inflection> nounAdjInflections;
    Set<String> cases = new HashSet<>();
  }
  

  @Override
  public final RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();    

    State state = null;

    for (int i = 1; i < tokens.length; i++) {
      AnalyzedTokenReadings tokenReadings = tokens[i];
//      String cleanToken = tokenReadings.getCleanToken();

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

      if( PosTagHelper.hasPosTagStart(tokenReadings, "verb") ) {
        
        if( LemmaHelper.hasLemma(tokenReadings, Arrays.asList("бути", "могти", "змогти", "мати", "хотіти", "мусити", "намагатися", "вдатися", "доводитися"), "verb") ) {
          state = null;
          break;
        }

        if( PosTagHelper.hasPosTag(tokenReadings, Pattern.compile(".*(arch|bad|slang|alt).*")) ) {
          state = null;
          continue;
        }

        state = new State();

        for (AnalyzedToken token: tokenReadings) {
          String verbPosTag = token.getPOSTag();

          if( verbPosTag == null ) { // can happen for words with \u0301 or \u00AD
            continue;
          }

          if( ! verbPosTag.startsWith("verb")
              || verbPosTag.contains("abbr")
              || "значить".equals(token.getToken())
              || "діяти".equals(token.getToken()) ) {
            state = null;
            break;
          }
          
//        else if( PosTagHelper.isPredictOrInsert(token) ) {
          // ignore
//        }

          state.verbPos = i;
          state.verbTokenReadings.add(token);
          state.verbAnalyzedTokenReadings = tokenReadings;
        }

        continue;
      }
      
      if( state == null )
        continue;

      if( tokenReadings.getCleanToken().matches("[0-9]{4}-.+|нікому|нічого|нічим|решту") ) {
        state = null;
        continue;
      }

      if( isSkip(tokens, i) ) {
        i++;
        continue;
      }

      if( LemmaHelper.hasLemma(tokenReadings, Arrays.asList("готовий", "повинний")) ) {
        state = null;
        continue;
      }


      List<AnalyzedToken> nounAdjTokenReadingsVnaz = new ArrayList<>(); 
      List<AnalyzedToken> nounAdjTokenReadingsIndir = new ArrayList<>(); 

      for (AnalyzedToken token: tokenReadings) {
        String nounAdjPosTag = token.getPOSTag();

        if( nounAdjPosTag == null // can happen for words with \u0301 or \u00AD
            || nounAdjPosTag.equals(JLanguageTool.SENTENCE_END_TAGNAME)
            || nounAdjPosTag.equals(JLanguageTool.PARAGRAPH_END_TAGNAME)) {
          continue;
        }

        if( nounAdjPosTag.startsWith("<") ) {
          nounAdjTokenReadingsVnaz.clear();
          nounAdjTokenReadingsIndir.clear();
          break;
        }

        if( nounAdjPosTag.startsWith("noun") || nounAdjPosTag.startsWith("adj") || nounAdjPosTag.startsWith("numr")  ) {

          if( nounAdjPosTag.contains("v_naz") ) {
            nounAdjTokenReadingsVnaz.add(token);
          }
          else {
            nounAdjTokenReadingsIndir.add(token);
          }
          state.nounPos = i;
        }
        else {
          nounAdjTokenReadingsVnaz.clear();
          nounAdjTokenReadingsIndir.clear();
          break;
        }
      }

      // no following token - restart

      if( nounAdjTokenReadingsVnaz.isEmpty() && nounAdjTokenReadingsIndir.isEmpty() ) {
        state = null;
        continue;
      }

      logger.debug("=== Checking\n\t{}\n\tnd: {}\n\tni: {}", state.verbTokenReadings, nounAdjTokenReadingsVnaz, nounAdjTokenReadingsIndir);

      // perform the check

      // боротиметься кілька однопартійців
      if( PosTagHelper.hasPosTag(state.verbAnalyzedTokenReadings, Pattern.compile(".*:[sn](:.*|$)"))
          && PosTagHelper.hasPosTag(tokens[i], Pattern.compile("numr.*v_naz.*")) ) {
        state = null;
        continue;
      }

      
      List<VerbInflectionHelper.Inflection> verbInflections = null;
      boolean pass = false;

      if( nounAdjTokenReadingsVnaz.size() > 0 ) {
        state.nounAdjInflections = VerbInflectionHelper.getNounInflections(nounAdjTokenReadingsVnaz);
        state.nounAdjInflections.addAll(VerbInflectionHelper.getAdjInflections(nounAdjTokenReadingsVnaz));
        verbInflections = VerbInflectionHelper.getVerbInflections(state.verbTokenReadings);

        logger.debug("\t\t{}\n\t{}", verbInflections, state.nounAdjInflections);

        pass = ! Collections.disjoint(verbInflections, state.nounAdjInflections);
      }

      if( ! pass && nounAdjTokenReadingsIndir.size() > 0 ) {

        Set<String> cases = CaseGovernmentHelper.getCaseGovernments(state.verbAnalyzedTokenReadings, "verb");

        // віддати-відрізати Донбас
        if( cases.isEmpty() 
            && state.verbAnalyzedTokenReadings.getCleanToken().contains("-")
            && LemmaHelper.hasLemma(state.verbAnalyzedTokenReadings, Pattern.compile(".+ти(ся)?-.+ти(ся)?")) ) {
          
          List<AnalyzedToken> nodashReadings = state.verbAnalyzedTokenReadings.getReadings().stream()
              .filter(r -> PosTagHelper.hasPosTagStart(r, "verb"))
              .map(r -> new AnalyzedToken(r.getToken(), r.getPOSTag(), r.getLemma().replaceFirst("(ти(ся)?)-.*", "$1")))
              .collect(Collectors.toList());
          AnalyzedTokenReadings newReadings = new AnalyzedTokenReadings(state.verbAnalyzedTokenReadings, nodashReadings, "nodash");
          cases = CaseGovernmentHelper.getCaseGovernments(newReadings, "verb");
        }
        
        state.cases = cases;

        String tokenLowerCase = tokens[i].getCleanToken().toLowerCase();

        if( cases.contains("v_zna") && tokenLowerCase.matches("грошей|дров|товарів") ) {
//          cases.add("v_rod");
          state = null;
          continue;
        }

        if( cases.isEmpty() || ! TokenAgreementPrepNounRule.hasVidmPosTag(cases, nounAdjTokenReadingsIndir) ) {
          
        }
        else {
          pass = true;
        }
      }
      

      if( ! pass ) {

        if( i < tokens.length - 1
            && LemmaHelper.hasLemma(tokens[i], Arrays.asList("він", "вона", "вони"), Pattern.compile("noun:.*v_rod.*")) 
            && PosTagHelper.hasPosTag(tokens[i+1], Pattern.compile("(noun|adj).*"))) {
          // skip pron and try next token
          continue;
        }


        if( TokenAgreementVerbNounExceptionHelper.isException(tokens, state.verbPos, i, state, verbInflections, state.nounAdjInflections, state.verbTokenReadings, nounAdjTokenReadingsVnaz)) {
          state.verbTokenReadings.clear();
          break;
        }

        if( nounAdjTokenReadingsVnaz.size() > 0 || nounAdjTokenReadingsIndir.size() > 0 ) {

//        if( nounAdjTokenReadingsVnaz.size() > 0 ) {
//          
//          if( logger.isDebugEnabled() ) {
//            logger.debug(MessageFormat.format("=== Found verb/noun mismatch\n\t{0}\n\t{1}",
//                state.verbAnalyzedTokenReadings.getToken() + ": " + verbInflections + " // " + state.verbAnalyzedTokenReadings,
//                nounAdjTokenReadingsVnaz.get(0).getToken() + ": " + nounAdjInflections+ " // " + nounAdjTokenReadingsVnaz));
//          }
//
//          String msg = String.format("Не узгоджено %s з іменником: \"%s\" (%s) і \"%s\" (%s)",
//              LemmaHelper.hasLemma(state.verbTokenReadings, Arrays.asList("який")) ? "займенник" : "іменник",
//                  state.verbTokenReadings.get(0).getToken(), formatInflections(verbInflections, true), 
//                  nounAdjTokenReadingsVnaz.get(0).getToken(), formatInflections(nounAdjInflections, false));
//
//          RuleMatch potentialRuleMatch = new RuleMatch(this, sentence, state.verbAnalyzedTokenReadings.getStartPos(), tokenReadings.getEndPos(), msg, getShort());
//          ruleMatches.add(potentialRuleMatch);
//        }
//
//        if( nounAdjTokenReadingsIndir.size() > 0 ) {
          Set<String> cases = CaseGovernmentHelper.getCaseGovernments(state.verbAnalyzedTokenReadings, "verb");
          if( ! TokenAgreementPrepNounRule.hasVidmPosTag(cases, nounAdjTokenReadingsIndir) ) {

            if( logger.isDebugEnabled() ) {
              logger.debug(MessageFormat.format("=== Found verb/noun mismatch\n\t{0}\n\t{1}",
                  state.verbAnalyzedTokenReadings.getToken() + " // " + state.verbAnalyzedTokenReadings,
                  tokens[state.nounPos].getToken() + " // " + nounAdjTokenReadingsIndir));
            }

            List<Inflection> nounAdjInflections2 = InflectionHelper.getNounInflections(nounAdjTokenReadingsIndir);
            nounAdjInflections2.addAll(InflectionHelper.getAdjInflections(nounAdjTokenReadingsIndir));
            nounAdjInflections2.addAll(InflectionHelper.getNumrInflections(nounAdjTokenReadingsIndir));

            String msg = String.format("Не узгоджено дієслово з іменником: \"%s\" (вимагає: %s) і \"%s\" (%s)",
                //              LemmaHelper.hasLemma(state.verbTokenReadings, Arrays.asList("який")) ? "займенник" : "іменник",
                state.verbTokenReadings.get(0).getToken(), cases, // formatInflections(verbInflections, true), 
                nounAdjTokenReadingsIndir.get(0).getToken(), TokenAgreementAdjNounRule.formatInflections(nounAdjInflections2, false));

            RuleMatch potentialRuleMatch = new RuleMatch(this, sentence, state.verbAnalyzedTokenReadings.getStartPos(), tokenReadings.getEndPos(), msg, getShort());
            ruleMatches.add(potentialRuleMatch);
          }
        }
      }

      state = null;
    }

    return toRuleMatchArray(ruleMatches);
  }

  private boolean isSkip(AnalyzedTokenReadings[] tokens, int i) {
    
    // висміювати такого роду забобони
    if( i < tokens.length - 2
        && tokens[i].getCleanToken().matches("свого|такого|різного|одного|певного")
        && tokens[i+1].getCleanToken().matches("роду|разу|типу|штибу")
        && PosTagHelper.hasPosTag(tokens[i+2], Pattern.compile("(noun|adj|adv).*"))) {
      return true;
    }
    if( i < tokens.length - 1
        && tokens[i].getCleanToken().matches("таким|якимо?сь|відповідним|жодним")
        && tokens[i+1].getCleanToken().matches("чином|способом|робом") ) {
        if( i >= tokens.length - 2 || PosTagHelper.hasPosTag(tokens[i+2], Pattern.compile("[a-z].*"))) {
          return true;
        }
    }
    if( i < tokens.length - 2
        && tokens[i].getCleanToken().matches("більшою|меншою|(не)?значною|якоюсь")
        && tokens[i+1].getCleanToken().matches("мірою")
        && PosTagHelper.hasPosTag(tokens[i+2], Pattern.compile("(noun|adj|adv).*"))) {
      return true;
    }
    
    return false;
  }

}
