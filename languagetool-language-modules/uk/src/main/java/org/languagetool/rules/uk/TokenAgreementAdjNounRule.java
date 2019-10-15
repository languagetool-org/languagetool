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
import org.languagetool.tagging.uk.PosTagHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A rule that checks if adjective and following noun agree on gender and inflection
 * 
 * @author Andriy Rysin
 */
public class TokenAgreementAdjNounRule extends Rule {
  private static Logger logger = LoggerFactory.getLogger(TokenAgreementAdjNounRule.class);

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

  @Override
  public final RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();

    List<AnalyzedToken> adjTokenReadings = new ArrayList<>(); 
    AnalyzedTokenReadings adjAnalyzedTokenReadings = null;

    for (int i = 1; i < tokens.length; i++) {
      AnalyzedTokenReadings tokenReadings = tokens[i];

      String posTag0 = tokenReadings.getAnalyzedToken(0).getPOSTag();

      if( posTag0 == null ) {
//          || posTag0.equals(JLanguageTool.SENTENCE_START_TAGNAME) ){
        adjTokenReadings.clear();
        continue;
      }

      // grab initial adjective inflections

      if( adjTokenReadings.isEmpty() ) {

        // no need to start checking on last token or if no noun
        if( i == tokens.length - 1 )
          continue;

        //TODO: nv still can be wrong if :np/:ns is present to it's not much gain for lots of work
        if( PosTagHelper.hasPosTagPart(tokens[i], PosTagHelper.NO_VIDMINOK_SUBSTR)
            //TODO: turn back on when we can handle pron
            || PosTagHelper.hasPosTagPart(tokens[i], "&pron")
            || PosTagHelper.hasPosTagPart(tokens[i], "<") )
          continue;

        if( ! PosTagHelper.hasPosTagPart(tokens[i+1], "noun:")
            || PosTagHelper.hasPosTagPart(tokens[i+1], PosTagHelper.NO_VIDMINOK_SUBSTR)
            || PosTagHelper.hasPosTagPart(tokens[i+1], "&pron")
            || PosTagHelper.hasPosTagPart(tokens[i+1], "<") )
          continue;

        //TODO: TEMP?

        if( LemmaHelper.hasLemma(tokens[i], Arrays.asList("червоний", "правий", "місцевий", "найсильніший", "найкращі"), ":p:")
            || LemmaHelper.hasLemma(tokens[i], Arrays.asList("новенький", "головний", "вибраний", "більший", "побачений", "подібний"), ":n:")
            || LemmaHelper.hasLemma(tokens[i], Arrays.asList("державний"), ":f:") ) {
          adjTokenReadings.clear();
          break;
        }



        for (AnalyzedToken token: tokenReadings) {
          String adjPosTag = token.getPOSTag();

          if( adjPosTag == null ) { // can happen for words with \u0301 or \u00AD
            continue;
          }

          if( adjPosTag.startsWith("adj") ) {

            adjTokenReadings.add(token);
            adjAnalyzedTokenReadings = tokenReadings;
          }
          else {
            adjTokenReadings.clear();
            break;
          }
        }

        continue;
      }


      List<AnalyzedToken> slaveTokenReadings = new ArrayList<>();

      for (AnalyzedToken token: tokenReadings) {
        String nounPosTag = token.getPOSTag();

        if( nounPosTag == null ) { // can happen for words with \u0301 or \u00AD
          continue;
        }

        if( nounPosTag.startsWith("noun") 
            && ! nounPosTag.contains(PosTagHelper.NO_VIDMINOK_SUBSTR) ) {

          slaveTokenReadings.add(token);
        }
        else if ( nounPosTag.equals(JLanguageTool.SENTENCE_END_TAGNAME)
            || nounPosTag.equals(JLanguageTool.PARAGRAPH_END_TAGNAME) ) {
          continue;
        }
        else {
          slaveTokenReadings.clear();
          break;
        }
      }


      // no slave token - restart

      if( slaveTokenReadings.isEmpty() ) {
        adjTokenReadings.clear();
        continue;
      }

      logger.debug("=== Checking:\n\t{}\n\t{}", adjTokenReadings, slaveTokenReadings);

      // perform the check

      List<InflectionHelper.Inflection> masterInflections = InflectionHelper.getAdjInflections(adjTokenReadings);

      List<InflectionHelper.Inflection> slaveInflections = InflectionHelper.getNounInflections(slaveTokenReadings, "v_zna:var");

      if( Collections.disjoint(masterInflections, slaveInflections) ) {

        if( TokenAgreementAdjNounExceptionHelper.isException(tokens, i, masterInflections, slaveInflections, adjTokenReadings, slaveTokenReadings) ) {
          adjTokenReadings.clear();
          continue;
        }

        if( logger.isDebugEnabled()) {
          logger.debug(MessageFormat.format("=== Found:\n\t{0}\n\t",
            adjAnalyzedTokenReadings.getToken() + ": " + masterInflections + " // " + adjAnalyzedTokenReadings,
            slaveTokenReadings.get(0).getToken() + ": " + slaveInflections+ " // " + slaveTokenReadings));
        }

        String msg = String.format("Потенційна помилка: прикметник не узгоджений з іменником: \"%s\": [%s] і \"%s\": [%s]", 
            adjTokenReadings.get(0).getToken(), formatInflections(masterInflections, true),
            slaveTokenReadings.get(0).getToken(), formatInflections(slaveInflections, false));

        if( PosTagHelper.hasPosTagPart(adjTokenReadings, ":m:v_rod")
            && tokens[i].getToken().matches(".*[ую]")
            && PosTagHelper.hasPosTag(slaveTokenReadings, "noun.*?:m:v_dav.*") ) {
          msg += ". Можливо, вжито невнормований родовий відмінок ч.р. з закінченням -у/-ю замість -а/-я (така тенденція є в сучасній мові)?";
        }
        else if( adjAnalyzedTokenReadings.getToken().contains("-")
            && Pattern.compile(".*([23]-є|[02-9]-а|[0-9]-ма)").matcher(adjAnalyzedTokenReadings.getToken()).matches() ) {
          msg += ". Можливо, вжито зайве літерне нарощення після кількісного числівника?";
        }
        else if( adjAnalyzedTokenReadings.getToken().startsWith("не")
        // TODO: && tag(adjAnalyzedTokenReadings.getToken().substring(2)) has adjp
            && PosTagHelper.hasPosTag(slaveTokenReadings, "noun.*?:v_oru.*") ) {
          msg += ". Можливо, тут «не» потрібно писати окремо?";
        }

        RuleMatch potentialRuleMatch = new RuleMatch(this, sentence, adjAnalyzedTokenReadings.getStartPos(), tokenReadings.getEndPos(), msg, getShort());

        Synthesizer ukrainianSynthesizer = ukrainian.getSynthesizer();
        List<String> suggestions = new ArrayList<>();


        try {

        for (Inflection adjInflection : masterInflections) {
          String genderTag = ":"+adjInflection.gender+":";
          String vidmTag = adjInflection._case;


            if( ! adjInflection._case.equals("v_kly")
                && (adjInflection.gender.equals("p")
                || PosTagHelper.hasPosTagPart(slaveTokenReadings, genderTag)) ) {
              for(AnalyzedToken nounToken: slaveTokenReadings) {

                if( adjInflection.animMatters() ) {
                  if( ! nounToken.getPOSTag().contains(":" + adjInflection.animTag) )
                    continue;
                }

                String newNounPosTag = nounToken.getPOSTag().replaceFirst(":.:v_...", genderTag + vidmTag);

                String[] synthesized = ukrainianSynthesizer.synthesize(nounToken, newNounPosTag, false);

                for (String s : synthesized) {
                  String suggestion = adjAnalyzedTokenReadings.getToken() + " " + s;
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

          for(AnalyzedToken adjToken: adjTokenReadings) {
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

      adjTokenReadings.clear();
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
