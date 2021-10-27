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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.language.Ukrainian;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.uk.InflectionHelper.Inflection;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tagging.uk.IPOSTag;
import org.languagetool.tagging.uk.PosTagHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A rule that checks if adjective and following noun agree on gender and inflection
 * 
 * @author Andriy Rysin
 */
public class TokenAgreementAdjNounRule extends Rule {
  static final List<String> FAKE_FEM_LIST = Arrays.asList("ступінь", "степінь", "продаж", "собака", "дріб", "ярмарок", "нежить", "рукопис", "накип", "насип", "путь");

  private static final Logger logger = LoggerFactory.getLogger(TokenAgreementAdjNounRule.class);

  static final Pattern ADJ_INFLECTION_PATTERN = Pattern.compile(":([mfnp]):(v_...)(:r(in)?anim)?");
  static final Pattern NOUN_INFLECTION_PATTERN = Pattern.compile("((?:[iu]n)?anim):([mfnps]):(v_...)");

  private final Ukrainian ukrainian = new Ukrainian();

  public TokenAgreementAdjNounRule(ResourceBundle messages) throws IOException {
    super.setCategory(Categories.MISC.getCategory(messages));
//    setDefaultOff();
  }

  @Override
  public final String getId() {
    return "UK_ADJ_NOUN_INFLECTION_AGREEMENT";
  }

  @Override
  public String getDescription() {
    return "Узгодження відмінків, роду і числа прикметника та іменника";
  }

  public String getShort() {
    return "Узгодження прикметника та іменника";
  }

  private static class State {
    int adjPos;
    List<AnalyzedToken> adjTokenReadings = new ArrayList<>(); 
    AnalyzedTokenReadings adjAnalyzedTokenReadings = null;
    
    public boolean isEmpty() {
      return adjTokenReadings.isEmpty();
    }
    public void reset() {
      adjTokenReadings.clear();
      adjAnalyzedTokenReadings = null;
    }
  }
  
  @Override
  public final RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();

    State state = new State();

    for (int i = 1; i < tokens.length; i++) {
      AnalyzedTokenReadings tokenReadings = tokens[i];

      String posTag0 = tokenReadings.getAnalyzedToken(0).getPOSTag();

      if( posTag0 == null ) {
        state.reset();
        continue;
      }

      if( state.isEmpty() ) {
        // no need to start checking on last token or if no noun
        if( i == tokens.length - 1 )
          continue;
      }
      else {

        if( (PosTagHelper.hasPosTagPartAll(tokenReadings, "adv")
             || Arrays.asList("дуже", "небагато", "багато").contains(tokenReadings.getCleanToken()) )
            //TODO: temp for adv/prep
            && ! ( i < tokens.length - 1
//                && Arrays.asList("стосовно", "відносно", "усередині", "всередині", "неподалік").contains(tokenReadings.getCleanToken())
//                 && PosTagHelper.hasPosTag(tokens[i+1], Pattern.compile(".*v_rod.*")) )
                && PosTagHelper.hasPosTagStart(tokens[i], "prep")
                && TokenAgreementPrepNounRule.hasVidmPosTag(
                     CaseGovernmentHelper.getCaseGovernments(tokens[i], IPOSTag.prep.name()), tokens[i+1]))
            && PosTagHelper.hasPosTagPart(state.adjTokenReadings, "adjp") ) {
          continue;
        }

      }

      // grab initial adjective inflections

      if( PosTagHelper.hasPosTagStart(tokens[i], "adj") ) {
        state.reset();

        //TODO: nv still can be wrong if :np/:ns is present to it's not much gain for lots of work
        if( PosTagHelper.hasPosTagPart(tokens[i], PosTagHelper.NO_VIDMINOK_SUBSTR)
            //TODO: turn back on when we can handle pron
            || PosTagHelper.hasPosTagPart(tokens[i], "&pron")
            || PosTagHelper.hasPosTagPart(tokens[i], "<") )
          continue;

        //        if( LemmaHelper.hasLemma(tokens[i], Arrays.asList("червоний", "правий", "місцевий", "найсильніший", "найкращі"), ":p:")
        //            || LemmaHelper.hasLemma(tokens[i], Arrays.asList("новенький", "головний", "вибраний", "більший", "побачений", "подібний"), ":n:")
        //            || LemmaHelper.hasLemma(tokens[i], Arrays.asList("державний"), ":f:") ) {
        //          adjTokenReadings.clear();
        //          break;
        //        }

        if ( LemmaHelper.hasLemma(tokens[i], Arrays.asList("подібний"), ":n:") ) {
          state.reset();
          break;
        }

        for (AnalyzedToken token: tokenReadings) {
          String adjPosTag = token.getPOSTag();

          if( adjPosTag == null ) { // can happen for words with \u0301 or \u00AD
            continue;
          }

          if( adjPosTag.startsWith("adj") ) {
            state.adjPos = i;
            state.adjTokenReadings.add(token);
            state.adjAnalyzedTokenReadings = tokenReadings;
          }
          else if( ! LemmaHelper.hasLemma(tokenReadings, Arrays.asList("другий"), "adj:f:")
              || (i + 1 < tokens.length && ! LemmaHelper.hasLemma(tokens[i+1], FAKE_FEM_LIST, "noun:inanim:m:"))
              && ! PosTagHelper.isPredictOrInsert(token) ) {
            state.reset();
            break;
          }
        }

        continue;
      }

      if( state.isEmpty() )
        continue;
      
      if( // ! PosTagHelper.hasPosTagPart(tokens[i+1], "noun:")
          PosTagHelper.hasPosTagPart(tokens[i], PosTagHelper.NO_VIDMINOK_SUBSTR)
         || PosTagHelper.hasPosTagPart(tokens[i], "&pron")
         || PosTagHelper.hasPosTagPart(tokens[i], "<") ) {
        
        state.reset();
        continue;
      }


      List<AnalyzedToken> nounTokenReadings = new ArrayList<>();

      for (AnalyzedToken token: tokenReadings) {
        String nounPosTag = token.getPOSTag();

        if( nounPosTag == null ) { // can happen for words with \u0301 or \u00AD
          continue;
        }

        if( nounPosTag.startsWith("noun") 
            && ! nounPosTag.contains(PosTagHelper.NO_VIDMINOK_SUBSTR) ) {

          nounTokenReadings.add(token);
        }
        else if ( nounPosTag.equals(JLanguageTool.SENTENCE_END_TAGNAME)
            || nounPosTag.equals(JLanguageTool.PARAGRAPH_END_TAGNAME) ) {
          continue;
        }
        else if( ! PosTagHelper.isPredictOrInsert(token) ) {
          nounTokenReadings.clear();
          break;
        }
      }


      // no slave token - restart

      if( nounTokenReadings.isEmpty() ) {
        state.reset();
        continue;
      }

      logger.debug("=== Checking:\n\t{}\n\t{}", state.adjTokenReadings, nounTokenReadings);

      // perform the check

      List<InflectionHelper.Inflection> masterInflections = InflectionHelper.getAdjInflections(state.adjTokenReadings);

      List<InflectionHelper.Inflection> slaveInflections = InflectionHelper.getNounInflections(nounTokenReadings, "v_zna:var");

      if( Collections.disjoint(masterInflections, slaveInflections) ) {

        if( TokenAgreementAdjNounExceptionHelper.isException(tokens, state.adjPos, i, masterInflections, slaveInflections, state.adjTokenReadings, nounTokenReadings) ) {
          state.reset();
          continue;
        }

        if( logger.isDebugEnabled()) {
          logger.debug(MessageFormat.format("=== Found:\n\t{0}\n\t",
              state.adjAnalyzedTokenReadings.getToken() + ": " + masterInflections + " // " + state.adjAnalyzedTokenReadings,
            nounTokenReadings.get(0).getToken() + ": " + slaveInflections+ " // " + nounTokenReadings));
        }

        String msg = String.format("Потенційна помилка: прикметник не узгоджений з іменником: \"%s\": [%s] і \"%s\": [%s]", 
            state.adjTokenReadings.get(0).getToken(), formatInflections(masterInflections, true),
            nounTokenReadings.get(0).getToken(), formatInflections(slaveInflections, false));

        if( PosTagHelper.hasPosTagPart(state.adjTokenReadings, ":m:v_rod")
            && tokens[i].getToken().matches(".*[ую]")
            && PosTagHelper.hasPosTag(nounTokenReadings, "noun.*?:m:v_dav.*") ) {
          msg += ". Можливо, вжито невнормований родовий відмінок ч.р. з закінченням -у/-ю замість -а/-я (така тенденція є в сучасній мові)?";
        }
        else if( state.adjAnalyzedTokenReadings.getToken().contains("-")
            && Pattern.compile(".*([23]-є|[02-9]-а|[0-9]-ма)").matcher(state.adjAnalyzedTokenReadings.getToken()).matches() ) {
          msg += ". Можливо, вжито зайве літерне нарощення після кількісного числівника?";
        }
        else if( state.adjAnalyzedTokenReadings.getToken().startsWith("не")
        // TODO: && tag(adjAnalyzedTokenReadings.getToken().substring(2)) has adjp
            && PosTagHelper.hasPosTag(nounTokenReadings, "noun.*?:v_oru.*") ) {
          msg += ". Можливо, тут «не» потрібно писати окремо?";
        }
        else if( ! PosTagHelper.hasPosTag(state.adjTokenReadings, "adj.*?v_mis.*")
            && PosTagHelper.hasPosTag(nounTokenReadings, "noun.*?v_mis.*") ) {
          msg += ". Можливо, пропущено прийменник на/в/у...?";
        }

        RuleMatch potentialRuleMatch = new RuleMatch(this, sentence, state.adjAnalyzedTokenReadings.getStartPos(), tokenReadings.getEndPos(), msg, getShort());

        Synthesizer ukrainianSynthesizer = ukrainian.getSynthesizer();
        List<String> suggestions = new ArrayList<>();


        try {

        for (Inflection adjInflection : masterInflections) {
          String genderTag = ":"+adjInflection.gender+":";
          String vidmTag = adjInflection._case;


            if( ! adjInflection._case.equals("v_kly")
                && (adjInflection.gender.equals("p")
                || PosTagHelper.hasPosTagPart(nounTokenReadings, genderTag)) ) {
              for(AnalyzedToken nounToken: nounTokenReadings) {

                if( adjInflection.animMatters() ) {
                  if( ! nounToken.getPOSTag().contains(":" + adjInflection.animTag) )
                    continue;
                }

                String newNounPosTag = nounToken.getPOSTag().replaceFirst(":.:v_...", genderTag + vidmTag);

                String[] synthesized = ukrainianSynthesizer.synthesize(nounToken, newNounPosTag, false);

                for (String s : synthesized) {
                  String suggestion = state.adjAnalyzedTokenReadings.getToken() + " " + s;
                  if( ! suggestions.contains(suggestion) ) {
                    suggestions.add(suggestion);
                  }
                }
              }
            }
        }

        for (Inflection nounInflection : slaveInflections) {
          String genderTag = ":"+nounInflection.gender+":";
          String vidmTag = nounInflection._case;

          if( nounInflection.animMatters() ) {
            vidmTag += ":r" + nounInflection.animTag;
          }

          for(AnalyzedToken adjToken: state.adjTokenReadings) {
            String newAdjTag = adjToken.getPOSTag().replaceFirst(":.:v_...(:r(in)?anim)?", genderTag + vidmTag);

            String[] synthesized = ukrainianSynthesizer.synthesize(adjToken, newAdjTag, false);

            for (String s : synthesized) {
              String suggestion = s + " " + tokenReadings.getToken();
              if( ! suggestions.contains(suggestion) ) {
                suggestions.add(suggestion);
              }
            }
          }

        }

        } catch (IOException e) {
          throw new RuntimeException(e);
        }

//        System.err.println("### " + suggestions);

        if( suggestions.size() > 0 ) {
            potentialRuleMatch.setSuggestedReplacements(suggestions);
        }

        ruleMatches.add(potentialRuleMatch);
      }

      state.reset();
    }

    return toRuleMatchArray(ruleMatches);
  }

  private static String formatInflections(List<Inflection> inflections, boolean adj) {

    Collections.sort(inflections);
    
    Map<String, List<String>> map = new LinkedHashMap<>();
    
    for (Inflection inflection : inflections) {
      if( ! map.containsKey(inflection.gender) ) {
        map.put(inflection.gender, new ArrayList<>());
      }
      String caseStr = PosTagHelper.VIDMINKY_MAP.get(inflection._case);
      if( adj && inflection.animTag != null ) {
        caseStr += " (" + (inflection.animTag.equals("anim") ? "іст." : "неіст.") + ")";
      }
      map.get(inflection.gender).add(caseStr);
    }

    
    List<String> list = new ArrayList<>();
    for(Entry<String, List<String>> entry : map.entrySet()) {
      String genderStr = PosTagHelper.GENDER_MAP.get(entry.getKey());
      
      List<String> caseValues = entry.getValue();

      list.add(genderStr + ": " + StringUtils.join(caseValues, ", "));
    }
    
    return StringUtils.join(list, ", ");
  }

}
