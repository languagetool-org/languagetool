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
import org.languagetool.rules.Categories;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.uk.InflectionHelper.Inflection;
import org.languagetool.tagging.uk.PosTagHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A rule that checks if noun and verb agree
 * 
 * @author Andriy Rysin
 * @since 5.9
 */
public class TokenAgreementVerbNounRule extends Rule {
  
  private static final Logger logger = LoggerFactory.getLogger(TokenAgreementVerbNounRule.class);

  public TokenAgreementVerbNounRule(ResourceBundle messages) throws IOException {
    super.setCategory(Categories.MISC.getCategory(messages));
  }

  @Override
  public final String getId() {
    return "UK_VERB_NOUN_INFLECTION_AGREEMENT";
  }

  @Override
  public String getDescription() {
    return "Узгодження дієслова з іменником";
  }

  public String getShort() {
    return "Узгодження дієслова з іменником";
  }

  public boolean isCaseSensitive() {
    return false;
  }
  
  
  static class State {
    int verbPos;
    int nounPos;
    List<AnalyzedToken> verbTokenReadings = new ArrayList<>(); 
    AnalyzedTokenReadings verbAnalyzedTokenReadings = null;
    List<org.languagetool.rules.uk.VerbInflectionHelper.Inflection> nounAdjNazInflections;
    Set<String> cases = new HashSet<>();
    List<AnalyzedToken> nounAdjIndirTokenReadings = new ArrayList<>(); 
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
              || "читай".equals(token.getToken())
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

      if( tokenReadings.getCleanToken().toLowerCase().matches("[0-9]{4}-.+|нікому|нічому|нічого|нікого|нічим|решту") ) {
        state = null;
        continue;
      }

      if( LemmaHelper.hasLemma(tokenReadings, Arrays.asList("сам", "самий", "себе", "один")) ) {
        state = null;
        continue;
      }

      if( isSkip(tokens, i) ) {
//        i++;
        state = null;
        continue;
      }


      List<AnalyzedToken> nounAdjTokenReadingsVnaz = new ArrayList<>(); 

      for (AnalyzedToken token: tokenReadings) {
        String nounAdjPosTag = token.getPOSTag();

        if( nounAdjPosTag == null // can happen for words with \u0301 or \u00AD
            || nounAdjPosTag.endsWith("_END")) {
          continue;
        }

        if( nounAdjPosTag.startsWith("<") ) {
          state = null;
          break;
        }

        if( nounAdjPosTag.startsWith("noun") || nounAdjPosTag.startsWith("adj") || nounAdjPosTag.startsWith("numr")  ) {

          if( nounAdjPosTag.contains("v_naz") ) {
            nounAdjTokenReadingsVnaz.add(token);
          }
          else {
            state.nounAdjIndirTokenReadings.add(token);
          }
          state.nounPos = i;
        }
        else {
          state = null;
          break;
        }
      }

      // no following token - restart

      if( state == null || nounAdjTokenReadingsVnaz.isEmpty() && state.nounAdjIndirTokenReadings.isEmpty() ) {
        state = null;
        continue;
      }

      logger.debug("=== Checking\n\t{}\n\tnDir: {}\n\tnIndir: {}", 
          state.verbTokenReadings, nounAdjTokenReadingsVnaz, state.nounAdjIndirTokenReadings);

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
        state.nounAdjNazInflections = VerbInflectionHelper.getNounInflections(nounAdjTokenReadingsVnaz);
        state.nounAdjNazInflections.addAll(VerbInflectionHelper.getAdjInflections(nounAdjTokenReadingsVnaz));
        verbInflections = VerbInflectionHelper.getVerbInflections(state.verbTokenReadings);

        logger.debug("\t\t{}\n\t{}", verbInflections, state.nounAdjNazInflections);

        pass = ! Collections.disjoint(verbInflections, state.nounAdjNazInflections);
      }

      if( ! pass && state.nounAdjIndirTokenReadings.size() > 0 ) {

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

        if( cases.contains("v_zna") && tokenLowerCase.matches("грошей|грошенят|дров|товарів|пісень") ) {
//          cases.add("v_rod");
          state = null;
          continue;
        }

        if( cases.isEmpty() || ! TokenAgreementPrepNounRule.hasVidmPosTag(cases, state.nounAdjIndirTokenReadings) ) {
          
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


        if( TokenAgreementVerbNounExceptionHelper.isException(tokens, state.verbPos, i, state, verbInflections, state.nounAdjNazInflections, state.verbTokenReadings, nounAdjTokenReadingsVnaz)) {
          state.verbTokenReadings.clear();
          break;
        }

        if( nounAdjTokenReadingsVnaz.size() > 0 || state.nounAdjIndirTokenReadings.size() > 0 ) {

          Set<String> cases = CaseGovernmentHelper.getCaseGovernments(state.verbAnalyzedTokenReadings, "verb");
          if( ! TokenAgreementPrepNounRule.hasVidmPosTag(cases, state.nounAdjIndirTokenReadings) ) {

            logger.debug("=== Found verb/noun mismatch\n\t{} // {}\n\t{} // {}",
                state.verbAnalyzedTokenReadings.getToken(), state.verbAnalyzedTokenReadings,
                tokens[state.nounPos].getToken(), state.nounAdjIndirTokenReadings);

            List<Inflection> nounAdjInflections2 = InflectionHelper.getNounInflections(state.nounAdjIndirTokenReadings);
            nounAdjInflections2.addAll(InflectionHelper.getAdjInflections(state.nounAdjIndirTokenReadings));
            nounAdjInflections2.addAll(InflectionHelper.getNumrInflections(state.nounAdjIndirTokenReadings));

            if( nounAdjTokenReadingsVnaz.size() > 0 ) {
//              cases.add("v_naz");
              List<Inflection> nounAdjInflections0 = InflectionHelper.getNounInflections(nounAdjTokenReadingsVnaz);
              nounAdjInflections0.addAll(InflectionHelper.getAdjInflections(nounAdjTokenReadingsVnaz));
              nounAdjInflections0.addAll(InflectionHelper.getNumrInflections(nounAdjTokenReadingsVnaz));
              nounAdjInflections2.addAll(nounAdjInflections0);
            }
            
//            nounAdjInflections2.addAll(nounAdjTokenReadingsVnaz.stream().)
            
            String msg = String.format("Не узгоджено дієслово з іменником: \"%s\" (%s) і \"%s\" (%s)",
                state.verbTokenReadings.get(0).getToken(), formatInflections(cases), 
                state.nounAdjIndirTokenReadings.get(0).getToken(), TokenAgreementAdjNounRule.formatInflections(nounAdjInflections2, false));

            RuleMatch potentialRuleMatch = new RuleMatch(this, sentence, state.verbAnalyzedTokenReadings.getStartPos(), tokenReadings.getEndPos(), msg, getShort());
            ruleMatches.add(potentialRuleMatch);
          }
        }
      }

      state = null;
    }

    return toRuleMatchArray(ruleMatches);
  }

  private String formatInflections(Set<String> cases) {
    if( cases.isEmpty() )
      return "неперех.";
    
    return "вимагає: " + cases.stream()
        .map(c -> PosTagHelper.VIDMINKY_I_MAP.get(c))
        .collect(Collectors.joining(", "));
  }

  private boolean isSkip(AnalyzedTokenReadings[] tokens, int i) {
    
    // висміювати такого роду забобони
    if( i < tokens.length - 1
        && tokens[i].getCleanToken().matches("свого|такого|різного|одного|певного|подібного")
        && tokens[i+1].getCleanToken().matches("роду|разу|типу|штибу")
        ) {
      return true;
    }
    if( i < tokens.length - 1
        && tokens[i].getCleanToken().matches("таким|якимо?сь|відповідним|певним|жодним|дивним")
        && tokens[i+1].getCleanToken().matches("чином|способом|робом|ходом") ) {
          return true;
    }
    if( i < tokens.length - 1
        && tokens[i].getCleanToken().matches("більшою|меншою|(не)?значною|якоюсь|неабиякою|достатньою|великою")
        && tokens[i+1].getCleanToken().matches("мірою")
        ) {
      return true;
    }
    if( i < tokens.length - 1
        && tokens[i+1].getCleanToken().matches("темпами")
        ) {
      return true;
    }
    
    return false;
  }

}
