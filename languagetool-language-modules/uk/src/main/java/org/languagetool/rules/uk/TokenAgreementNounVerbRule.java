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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
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
  
  private static Logger logger = LoggerFactory.getLogger(TokenAgreementNounVerbRule.class);

  private static final Pattern VERB_INFLECTION_PATTERN = Pattern.compile(":([mfnps])(:([123])?|$)");
  private static final Pattern NOUN_INFLECTION_PATTERN = Pattern.compile("(?::((?:[iu]n)?anim))?:([mfnps]):(v_naz)");
  private static final Pattern NOUN_PERSON_PATTERN = Pattern.compile(":([123])");


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

  @Override
  public final RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();    

    List<AnalyzedToken> nounTokenReadings = new ArrayList<>(); 
    AnalyzedTokenReadings nounAnalyzedTokenReadings = null;

    for (int i = 1; i < tokens.length; i++) {
      AnalyzedTokenReadings tokenReadings = tokens[i];

      String posTag0 = tokenReadings.getAnalyzedToken(0).getPOSTag();

      //TODO: skip conj напр. «бодай»

      if( posTag0 == null ) {
        nounTokenReadings.clear();
        continue;
      }

      if( nounTokenReadings.isEmpty() ) {
        // no need to start checking on last token or if no noun
        if( i == tokens.length - 1 )
          continue;

        if( ! PosTagHelper.hasPosTag(tokenReadings, "noun.*:v_naz.*") )
          continue;


        for (AnalyzedToken token: tokenReadings) {
          String nounPosTag = token.getPOSTag();

          if( nounPosTag == null ) { // can happen for words with \u0301 or \u00AD
            continue;
          }

//          if( nounPosTag.startsWith("<") ) {
//            nounTokenReadings.clear();
//            break;
//          }

          if( nounPosTag.startsWith("noun") && nounPosTag.contains("v_naz") ) {
            nounTokenReadings.add(token);
            nounAnalyzedTokenReadings = tokenReadings;
          }
//          else if ( nounPosTag.equals(JLanguageTool.SENTENCE_END_TAGNAME) ) {
//            continue;
//          }
          else {
            nounTokenReadings.clear();
            break;
          }
        }

        continue;
      }


      // see if we get a following verb
//       System.err.println("Check for verb: " + tokenReadings);

      List<AnalyzedToken> verbTokenReadings = new ArrayList<>(); 
      for (AnalyzedToken token: tokenReadings) {
        String verbPosTag = token.getPOSTag();

        if( verbPosTag == null ) { // can happen for words with \u0301 or \u00AD
          continue;
        }

        if( verbPosTag.startsWith("<") ) {
          verbTokenReadings.clear();
          break;
        }

        if( verbPosTag.startsWith("verb") ) {

          verbTokenReadings.add(token);
        }
        else if ( verbPosTag.equals(JLanguageTool.SENTENCE_END_TAGNAME) ) {
          continue;
        }
        else {
          verbTokenReadings.clear();
          break;
        }
      }

      // no slave token - restart

      if( verbTokenReadings.isEmpty() ) {
        nounTokenReadings.clear();
        continue;
      }

      logger.debug("=== Checking\n\t{}\n\t{}", nounTokenReadings, verbTokenReadings);

      // perform the check

      List<Inflection> masterInflections = getNounInflections(nounTokenReadings);

      List<Inflection> slaveInflections = getVerbInflections(verbTokenReadings);

      logger.debug("\t\t{}\n\t{}", masterInflections, slaveInflections);

      if( Collections.disjoint(masterInflections, slaveInflections) ) {
        if( TokenAgreementNounVerbExceptionHelper.isException(tokens, i, masterInflections, slaveInflections, nounTokenReadings, verbTokenReadings)) {
          nounTokenReadings.clear();
          break;
        }

        if( logger.isDebugEnabled() ) {
          logger.debug(MessageFormat.format("=== Found noun/verb mismatch\n\t{0}\n\t{1}",
            nounAnalyzedTokenReadings.getToken() + ": " + masterInflections + " // " + nounAnalyzedTokenReadings,
            verbTokenReadings.get(0).getToken() + ": " + slaveInflections+ " // " + verbTokenReadings));
        }
        
        String msg = String.format("Не узгоджено іменник з дієсловом: \"%s\" (%s) і \"%s\" (%s)", 
            nounTokenReadings.get(0).getToken(), formatInflections(masterInflections, true), 
            verbTokenReadings.get(0).getToken(), formatInflections(slaveInflections, false));
        RuleMatch potentialRuleMatch = new RuleMatch(this, sentence, nounAnalyzedTokenReadings.getStartPos(), tokenReadings.getEndPos(), msg, getShort());
        ruleMatches.add(potentialRuleMatch);
      }

      nounTokenReadings.clear();
    }

    return toRuleMatchArray(ruleMatches);
  }


  private static String formatInflections(List<Inflection> inflections, boolean noun) {

    Collections.sort(inflections);

    List<String> list = new ArrayList<>();

    for (Inflection inflection : inflections) {
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



  static List<Inflection> getVerbInflections(List<AnalyzedToken> nounTokenReadings) {
    List<Inflection> verbGenders = new ArrayList<>();
    for (AnalyzedToken token: nounTokenReadings) {
      String posTag = token.getPOSTag();

      if( posTag == null || ! posTag.startsWith("verb") )
        continue;

      if( posTag.contains(":inf") ) {
        verbGenders.add(new Inflection("i", null));
        continue;
      }

      if( posTag.contains(":impers") ) {
        verbGenders.add(new Inflection("o", null));
        continue;
      }

      Matcher matcher = VERB_INFLECTION_PATTERN.matcher(posTag);
      matcher.find();

      String gen = matcher.group(1);
      String person = matcher.group(3);

      verbGenders.add(new Inflection(gen, person));
    }
//    System.err.println("verbInfl: " + verbGenders);
    return verbGenders;
  }


  static List<Inflection> getNounInflections(List<AnalyzedToken> nounTokenReadings) {
    List<Inflection> slaveInflections = new ArrayList<>();
    for (AnalyzedToken token: nounTokenReadings) {
      String posTag2 = token.getPOSTag();
      if( posTag2 == null )
        continue;

      Matcher matcher = NOUN_INFLECTION_PATTERN.matcher(posTag2);
      if( ! matcher.find() ) {
        //  			System.err.println("Failed to find slave inflection tag in " + posTag2 + " for " + nounTokenReadings);
        continue;
      }
      String gen = matcher.group(2);
      
      Matcher matcherPerson = NOUN_PERSON_PATTERN.matcher(posTag2);
      String person = matcherPerson.find() ? matcherPerson.group(1) : null;
      
      slaveInflections.add(new Inflection(gen, person));
    }
//    System.err.println("nounInfl: " + slaveInflections);
    return slaveInflections;
  }

  static boolean inflectionsOverlap(List<AnalyzedToken> verbTokenReadings, List<AnalyzedToken> nounTokenReadings) {
    return ! Collections.disjoint(
      getVerbInflections(verbTokenReadings), getNounInflections(nounTokenReadings)
    );
  }

  static class Inflection implements Comparable<Inflection> {
    final String gender;
    final String plural;
    final String person;

    Inflection(String gender, String person) {
      if( gender.equals("s") || gender.equals("p") ) {
        this.gender = null;
        this.plural = gender;
      }
      else if( gender.equals("i") ) {
        this.gender = gender;
        this.plural = gender;
      }
      else {
        this.gender = gender;
        this.plural = "s";
      }
      this.person = person;
    }
    
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;

      Inflection other = (Inflection) obj;

      if( person != null && other.person != null ) {
        if( ! person.equals(other.person) )
          return false;
      }
      
      if( gender != null && other.gender != null ) {

        // infinitive matches all for now, otherwise too many false positives
        // e.g. чи могла вона програти
        if( gender.equals("i") || other.gender.equals("i") )
          return true;

        if( ! gender.equals(other.gender) )
          return false;
      }

      return plural.equals(other.plural);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((gender == null) ? 0 : gender.hashCode());
        result = prime * result + ((plural == null) ? 0 : plural.hashCode());
        result = prime * result + ((person == null) ? 0 : person.hashCode());
        return result;
    }


    @Override
    public String toString() {
        return "Gender: " + gender + "/" + plural + "/" + person;
    }

    @Override
    public int compareTo(Inflection o) {
      Integer thisOrder = gender != null ? InflectionHelper.GEN_ORDER.get(gender) : 0;
      Integer otherOrder = o.gender != null ? InflectionHelper.GEN_ORDER.get(o.gender) : 0;
      
      int compared = thisOrder.compareTo(otherOrder);
//      if( compared != 0 )
        return compared;
      
//      compared = VIDM_ORDER.get(_case).compareTo(VIDM_ORDER.get(o._case));
//      return compared;
    }
  

  }


}
