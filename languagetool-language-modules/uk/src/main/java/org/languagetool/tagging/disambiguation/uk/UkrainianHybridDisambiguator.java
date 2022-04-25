/* LanguageTool, a natural language style checker 
 * Copyright (C) 2007 Daniel Naber (http://www.danielnaber.de)
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

package org.languagetool.tagging.disambiguation.uk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.language.Ukrainian;
import org.languagetool.rules.uk.CaseGovernmentHelper;
import org.languagetool.rules.uk.LemmaHelper;
import org.languagetool.tagging.disambiguation.AbstractDisambiguator;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.rules.XmlRuleDisambiguator;
import org.languagetool.tagging.uk.PosTagHelper;

/**
 * Hybrid chunker-disambiguator for Ukrainian.
 */

public class UkrainianHybridDisambiguator extends AbstractDisambiguator {
  private static final String LAST_NAME_TAG = ":prop:lname";
  private static final Pattern INITIAL_REGEX = Pattern.compile("[А-ЯІЇЄҐ]\\.");
  private static final Pattern INANIM_VKLY = Pattern.compile("noun:inanim:.:v_kly.*");
  private static final Pattern PLURAL_NAME = Pattern.compile("noun:anim:p:.*:fname.*");
//  private static final Pattern PLURAL_LNAME_OR_PATR = Pattern.compile("noun:anim:p:.*:lname.*");
  private static final Pattern PLURAL_LNAME_PATTERN = Pattern.compile("noun:anim:p:.*:[lp]name.*");
  private static final String ST_ABBR = "ст.";
  private static final Pattern LATIN_DIGITS_PATTERN = Pattern.compile("[XIVХІ]+([–—-][XIVХІ]+)?");
  private static final Pattern DIGITS_PATTERN = Pattern.compile("[0-9]+([–—-][0-9]+)?");
  private static final Pattern STATION_NAME_PATTERN = Pattern.compile("метро|[А-Я][а-яіїєґ'-]+");

  private final Disambiguator chunker = new UkrainianMultiwordChunker("/uk/multiwords.txt", true);

  private final Disambiguator disambiguator = new XmlRuleDisambiguator(new Ukrainian());
  private final SimpleDisambiguator simpleDisambiguator = new SimpleDisambiguator();

  static final Set<String> V_MIS_PREPS = CaseGovernmentHelper.CASE_GOVERNMENT_MAP.entrySet()
      .stream().filter(e -> e.getValue().contains("v_mis")).map(Map.Entry::getKey).collect(Collectors.toSet());
  static final Set<String> V_NON_MIS_PREPS = CaseGovernmentHelper.CASE_GOVERNMENT_MAP.entrySet()
      .stream().filter(e -> ! e.getValue().contains("v_mis")).map(Map.Entry::getKey).collect(Collectors.toSet());
  
  static {
    // add Latin y/B - often used instead of real prep
    // we'll catch it in MixedAlphabetsRule
    V_MIS_PREPS.add("y");
    V_MIS_PREPS.add("B");
  }
  

  /**
   * Calls two disambiguator classes: (1) a chunker; (2) a rule-based disambiguator.
   */
  @Override
  public final AnalyzedSentence disambiguate(AnalyzedSentence input) throws IOException {
    preDisambiguate(input);
    
    return disambiguator.disambiguate(chunker.disambiguate(input));
  }

  @Override
  public AnalyzedSentence preDisambiguate(AnalyzedSentence input) {
    simpleDisambiguator.removeRareForms(input);
    removeVmis(input);
    retagFemNames(input);
    retagInitials(input);
    retagUnknownInitials(input);
    removeInanimVKly(input);
    removePluralForNames(input);
    removeLowerCaseHomonymsForAbbreviations(input);
    removeLowerCaseBadForUpperCaseGood(input);
    disambiguateSt(input);

    return input;
  }

  private void retagFemNames(AnalyzedSentence input) {
    AnalyzedTokenReadings[] tokens = input.getTokensWithoutWhitespace();
    String ruleApplied = "proper_name_gender_override";
    
    for (int i = 1; i < tokens.length-2; i++) {

      for(String gen: new String[] {"f", "m"}) {
        
        List<String> prefix = gen.equals("f") 
            ? Arrays.asList("пані", "місіс", "місис", "міс", "леді", "княгиня", "німкеня")
                : Arrays.asList("пан", "містер", "м-р", "сер", "князь", "німець", "поляк");

        String animPropTagPrefix = "noun:anim:"+gen+":v_naz:prop";
        

        if( (LemmaHelper.hasLemma(tokens[i], prefix, Pattern.compile("noun:anim:"+gen+":v_naz.*"))
            || PosTagHelper.hasPosTagStart(tokens[i], animPropTagPrefix + ":fname"))
            && PosTagHelper.hasPosTag(tokens[i+2], Pattern.compile("verb.*:past:"+gen)) ) {

          AnalyzedTokenReadings nameToken = tokens[i+1];
          if( PosTagHelper.hasPosTagStart(nameToken, animPropTagPrefix) ) {
            for (AnalyzedToken analyzedToken : nameToken) {
              if( ! PosTagHelper.hasPosTagStart(analyzedToken, animPropTagPrefix) ) {
                nameToken.removeReading(analyzedToken, ruleApplied);
              }
            }
          }
          // леді Черчилль
          else if ( gen.equals("f") && PosTagHelper.hasPosTagStart(nameToken, "noun:anim:m:v_naz:prop") ) {
            for (AnalyzedToken analyzedToken : nameToken) {
              nameToken.removeReading(analyzedToken, ruleApplied);
            }
            nameToken.addReading(new AnalyzedToken(nameToken.getToken(), "noun:anim:f:v_naz:prop:lname", nameToken.getToken()), ruleApplied);
          }
          // Олег П'ятниця
          else if( LemmaHelper.isCapitalized(nameToken.getCleanToken())
              && ! PosTagHelper.hasPosTagPart(nameToken, ":prop")
              && PosTagHelper.hasPosTagStart(tokens[i], animPropTagPrefix + ":fname") ) {
            for (AnalyzedToken analyzedToken : nameToken) {
              nameToken.removeReading(analyzedToken, ruleApplied);
            }
            nameToken.addReading(new AnalyzedToken(nameToken.getToken(), animPropTagPrefix + ":lname", nameToken.getToken()), ruleApplied);
          }

          i+=1;
        }
      }
    }
  }

  private void removeVmis(AnalyzedSentence input) {
    AnalyzedTokenReadings[] tokens = input.getTokensWithoutWhitespace();
    
    boolean startCheck = false;
    for (int i = 1; i < tokens.length; i++) {
      List<AnalyzedToken> analyzedTokens = tokens[i].getReadings();

      if (tokens[i].getToken() == null )
        continue;
      
      String lowerCaseToken = tokens[i].getToken().toLowerCase();

      boolean hasPrep = PosTagHelper.hasPosTagPart(tokens[i], "prep");

      if( ! startCheck ) {
        if( hasPrep ) {
          startCheck = true;
        }
        else {
          if( lowerCaseToken.matches("[а-яіїєґa-z0-9].*") ) {
            // sometimes sentences incorrectly split, e.g. "захопленню зброї;" - leave it
            if( StringUtils.isAllLowerCase(tokens[i].getToken()) ) {
              continue;
            }
            startCheck = true;
          }
        }
      }
      
      if( hasPrep && V_MIS_PREPS.contains(lowerCaseToken) )
        return;

      if ( ! canRemoveVmis(analyzedTokens) )
        continue;

      for (AnalyzedToken analyzedToken : analyzedTokens) {
        if( PosTagHelper.hasPosTagPart(analyzedToken, "v_mis") ) {
          tokens[i].removeReading(analyzedToken, "dis_v_mis");
        }
      }
    }
  }

  private boolean canRemoveVmis(List<AnalyzedToken> analyzedTokens) {
    boolean foundVmis = false, foundOther = false;
    for(AnalyzedToken token: analyzedTokens) {
      if( PosTagHelper.hasPosTagPart(token, "v_mis") ) {
        foundVmis = true;
      }
      else if( token.getPOSTag() != null && ! token.getPOSTag().endsWith("_END") ) {
        foundOther = true;
      }
      if( foundVmis && foundOther )
        break;
    }
    return foundVmis && foundOther;
  }

  // correct: Єврокомісія, but often written: єврокомісія
  // we will tag 2nd as :bad but need to remove :bad from Єврокомісія (tagger brings lowercase lemma too)
  private void removeLowerCaseBadForUpperCaseGood(AnalyzedSentence input) {
    AnalyzedTokenReadings[] tokens = input.getTokensWithoutWhitespace();
    for (int i = 1; i < tokens.length; i++) {
      if( tokens[i].getReadings().size() > 1
          && LemmaHelper.isCapitalized(tokens[i].getCleanToken())
          && LemmaHelper.hasLemma(tokens[i], Pattern.compile("[А-ЯІЇЄҐ][а-яіїєґ'-].*"), Pattern.compile(".*?:prop")) ) {

        String lowerLemmaToCheck = tokens[i].getAnalyzedToken(0).getLemma().toLowerCase();
        
        List<AnalyzedToken> analyzedTokens = tokens[i].getReadings();
        for(int j=analyzedTokens.size()-1; j>=0; j--) {
          AnalyzedToken analyzedToken = analyzedTokens.get(j);
          
          if( PosTagHelper.hasPosTagPart(analyzedToken, ":bad") 
              && lowerLemmaToCheck.equals(analyzedToken.getLemma()) ) {
            tokens[i].removeReading(analyzedToken, "lowercase_bad_vs_uppercase_good");
          }
        }
      }
    }
  }

  // all uppercase mostly are abbreviations, e.g. "АТО" is not part/intj
  private void removeLowerCaseHomonymsForAbbreviations(AnalyzedSentence input) {
    AnalyzedTokenReadings[] tokens = input.getTokensWithoutWhitespace();
    for (int i = 1; i < tokens.length; i++) {
      if( StringUtils.isAllUpperCase(tokens[i].getToken())
          && PosTagHelper.hasPosTagPart(tokens[i], ":abbr") ) {
        
        List<AnalyzedToken> analyzedTokens = tokens[i].getReadings();
        for(int j=analyzedTokens.size()-1; j>=0; j--) {
          AnalyzedToken analyzedToken = analyzedTokens.get(j);
          
          if( ! PosTagHelper.hasPosTagPart(analyzedToken, ":abbr") 
              && ! JLanguageTool.SENTENCE_END_TAGNAME.equals(analyzedToken.getPOSTag())
              && ! JLanguageTool.PARAGRAPH_END_TAGNAME.equals(analyzedToken.getPOSTag())) {
            tokens[i].removeReading(analyzedToken, "lowercase_vs_abbr");
          }
        }
      }
    }    
  }

  private static final Pattern PUNCT_AFTER_KLY_PATTERN = Pattern.compile("[!?,»\"\u201C\u201D…]|[\\.!?]{2,3}");

  private void removeInanimVKly(AnalyzedSentence input) {
    AnalyzedTokenReadings[] tokens = input.getTokensWithoutWhitespace();
    for (int i = 1; i < tokens.length; i++) {
      List<AnalyzedToken> analyzedTokens = tokens[i].getReadings();

      if( ! PosTagHelper.hasPosTag(analyzedTokens, Pattern.compile("noun:inanim:.:v_kly(?!.*:geo).*") )
          || likelyVklyContext(tokens, i) )
        continue;

      ArrayList<AnalyzedToken> inanimVklyReadings = new ArrayList<>();
      boolean otherFound = false;
      for(int j=0; j<analyzedTokens.size(); j++) {
        String posTag = analyzedTokens.get(j).getPOSTag();

        if( posTag == null )
          break;
        if( posTag.equals(JLanguageTool.SENTENCE_END_TAGNAME) )
          continue;

        if( INANIM_VKLY.matcher(posTag).matches() ) {
          inanimVklyReadings.add(analyzedTokens.get(j));
        }
        else {
          otherFound = true;
        }
      }

      if( inanimVklyReadings.size() > 0 && otherFound ) {
        for(AnalyzedToken analyzedToken: inanimVklyReadings) {
          if( Arrays.asList("зоря").contains(analyzedToken.getLemma()) )
            continue;

          tokens[i].removeReading(analyzedToken, "inanim_v_kly");
        }
      }
    }
  }

  private static final List<String> LIKELY_V_KLY = Arrays.asList("суде", "роде", "заходе");
  private boolean likelyVklyContext(AnalyzedTokenReadings[] tokens, int i) {
    if( LIKELY_V_KLY.contains(tokens[i].getToken().toLowerCase()) )
      return true;

    return i < tokens.length - 1
        && ("о".equalsIgnoreCase(tokens[i-1].getToken()) || ! PosTagHelper.hasPosTagStart(tokens[i-1], "prep"))
        && PUNCT_AFTER_KLY_PATTERN.matcher(tokens[i+1].getToken()).matches()
        && (PosTagHelper.hasPosTag(tokens[i-1], PosTagHelper.ADJ_V_KLY_PATTERN)
          || "о".equalsIgnoreCase(tokens[i-1].getToken()));
  }

  private void removePluralForNames(AnalyzedSentence input) {
    AnalyzedTokenReadings[] tokens = input.getTokensWithoutWhitespace();
    for (int i = 1; i < tokens.length; i++) {
      List<AnalyzedToken> analyzedTokens = tokens[i].getReadings();
      
      if( i > 1
          && (PosTagHelper.hasPosTagStart(tokens[i-1], "adj:p")
              //TODO: unify adj and noun
              || PosTagHelper.hasPosTagPart(tokens[i-1], "num")
              || LemmaHelper.hasLemma(tokens[i-1], Arrays.asList("багато", "мало", "півсотня", "сотня"))) )
        continue;

      // Юріїв Луценків
      if( i<tokens.length-1 
          && PosTagHelper.hasPosTag(tokens[i+1], PLURAL_LNAME_PATTERN) )
        continue;
      
      // Андріїв Фартушняка й Варанкова
      if( i<tokens.length-3
          && PosTagHelper.hasPosTagPart(tokens[i+1], ":lname")
          && PosTagHelper.hasPosTagPart(tokens[i+3], ":lname") )
        continue;

      
      ArrayList<AnalyzedToken> pluralNameReadings = new ArrayList<>();
      boolean otherFound = false;
      for(int j=0; j<analyzedTokens.size(); j++) {
        String posTag = analyzedTokens.get(j).getPOSTag();
        if( posTag == null )
          break;
        if( posTag.equals(JLanguageTool.SENTENCE_END_TAGNAME) )
          continue;
          
        if( PLURAL_NAME.matcher(posTag).matches() ) {
          pluralNameReadings.add(analyzedTokens.get(j));
        }
        else {
          otherFound = true;
        }
      }
      if( pluralNameReadings.size() > 0 && otherFound ) {
        for(AnalyzedToken analyzedToken: pluralNameReadings) {
          tokens[i].removeReading(analyzedToken, "plural_for_names");
        }
      }
    }
  }

  private void retagInitials(AnalyzedSentence input) {
    AnalyzedTokenReadings[] tokens = input.getTokens();

    List<Integer> initialsIdxs = new ArrayList<Integer>();
    AnalyzedTokenReadings lastName = null;

    for (int i = 1; i < tokens.length; i++) {

      if( tokens[i].isWhitespace() ) {
        continue;
      }

      if( tokens[i].hasPartialPosTag(LAST_NAME_TAG) ) {
        lastName = tokens[i];

        // split before next initial starts: "для Л.Кучма Л.Кравчук"
        if( initialsIdxs.size() > 0 ) {
          checkForInitialRetag(lastName, initialsIdxs, tokens);
          lastName = null;
          initialsIdxs.clear();
        }
        continue;
      }


      if( isInitial(tokens, i) ) {
        initialsIdxs.add(i);
        continue;
      }

      checkForInitialRetag(lastName, initialsIdxs, tokens);

      lastName = null;
      initialsIdxs.clear();
    }

    checkForInitialRetag(lastName, initialsIdxs, tokens);
  }

  private void retagUnknownInitials(AnalyzedSentence input) {
    AnalyzedTokenReadings[] tokens = input.getTokens();

    for (int i = 1; i < tokens.length; i++) {
      if( tokens[i].getToken().endsWith(".") 
          && INITIAL_REGEX.matcher(tokens[i].getToken()).matches() ) {
        
        if( PosTagHelper.hasPosTagPart(tokens[i], "name") )
          continue;

        for(AnalyzedToken tokenReading: tokens[i].getReadings()) {
//          if( ! "noninfl:abbr".equals(tokenReading.getPOSTag()) ) {
            tokens[i].removeReading(tokenReading, "dis_unknown_initials");
//          }
        }

        AnalyzedToken newToken = new AnalyzedToken(tokens[i].getToken(), "noninf:abbr", null);
        tokens[i].addReading(newToken, "dis_unknown_initials");
      }
    }
  }
  
  private static void checkForInitialRetag(AnalyzedTokenReadings lastName, List<Integer> initialsIdxs, AnalyzedTokenReadings[] tokens) {
    if( lastName != null
        && (initialsIdxs.size() == 1 || initialsIdxs.size() == 2) ) {

      int fnamePos = initialsIdxs.get(0);
      AnalyzedTokenReadings newReadings = getInitialReadings(tokens[fnamePos], lastName, "fname");
      tokens[fnamePos] = newReadings;

      if( initialsIdxs.size() == 2 ) {
        int pnamePos = initialsIdxs.get(1);
        AnalyzedTokenReadings newReadings2 = getInitialReadings(tokens[pnamePos], lastName, "pname");
        tokens[pnamePos] = newReadings2;
      }
    }
  }

  private void disambiguateSt(AnalyzedSentence input) {
    AnalyzedTokenReadings[] tokens = input.getTokensWithoutWhitespace();

    for (int i = 1; i < tokens.length; i++) {

      if (!ST_ABBR.equals(tokens[i].getToken()))
        continue;

      // стаття/сторінка
      if (i < tokens.length - 1) {
        if (tokens[i + 1].getToken().matches("[0-9]+([.,–—-][0-9]+)?")) {
          Pattern pattern = Pattern.compile("noun:inanim:f:.*");

          if (i > 2 && ST_ABBR.equals(tokens[i - 1].getToken())) {
            pattern = Pattern.compile("noun:inanim:p:.*");
            remove(tokens[i - 1], pattern);
          }

          remove(tokens[i], pattern);
          continue;
        }

      }

      if (i < tokens.length - 1) {
        // столова
        if (LemmaHelper.hasLemma(tokens[i + 1], "ложка") 
            || tokens[i + 1].getToken().equals("л.")) {
          Pattern pattern = Pattern.compile("adj:[fp]:.*");
          remove(tokens[i], pattern);
          i++;
          continue;
        }

        // старший
        if (LemmaHelper.hasLemma(tokens[i + 1],
            Arrays.asList("лейтенант", "сержант", "солдат", "науковий", "медсестра"))) {
          Pattern pattern = Pattern.compile("adj:m:.*");
          remove(tokens[i], pattern);
          i++;
          continue;
        }

        // станція
        if (STATION_NAME_PATTERN.matcher(tokens[i + 1].getToken()).matches()) {
          Pattern pattern = Pattern.compile("noun:inanim:f:.*");
          remove(tokens[i], pattern);
          i++;
          continue;
        }
      }
      
      // століття
      if (i > 1) {
        if( LATIN_DIGITS_PATTERN.matcher(tokens[i - 1].getToken()).matches() ) {
          Pattern pattern = Pattern.compile("noun:inanim:n:.*");

          if (i < tokens.length - 1 && ST_ABBR.equals(tokens[i + 1].getToken())) {
            pattern = Pattern.compile("noun:inanim:p:.*");
            remove(tokens[i + 1], pattern);
          }

          remove(tokens[i], pattern);
          i++;
          continue;
        }
        else if( DIGITS_PATTERN.matcher(tokens[i - 1].getToken()).matches() ) {
          Pattern pattern = Pattern.compile("noun:inanim:[nf]:.*"); // 18 ст. - стаття або століття

          if (i < tokens.length - 1 && ST_ABBR.equals(tokens[i + 1].getToken())) {
            pattern = Pattern.compile("noun:inanim:p:.*");
            remove(tokens[i + 1], pattern);
          }

          remove(tokens[i], pattern);
          i++;
          continue;
        }

      }

    }
  }
  
/*
TODO:
рт.ст.
ст.ст. - старий стиль
18 ст. - 18-та стаття
18 ст. - 18-те століття
*/


  private static void remove(AnalyzedTokenReadings readings, Pattern pattern) {
      List<AnalyzedToken> analyzedTokens = readings.getReadings();
      for (int j = analyzedTokens.size()-1; j>=0; j--) {
        AnalyzedToken analyzedToken = analyzedTokens.get(j);

        if( ! JLanguageTool.SENTENCE_END_TAGNAME.equals(analyzedToken.getPOSTag())
            && ! PosTagHelper.hasPosTag(analyzedToken, pattern) ) {
          readings.removeReading(analyzedToken, "UkranianHybridDisambiguator");
        }
      }

  }

  private static AnalyzedTokenReadings getInitialReadings(AnalyzedTokenReadings initialsReadings, AnalyzedTokenReadings lnameTokens, String initialType) {
    List<AnalyzedToken> newTokens = new ArrayList<>();

    for(AnalyzedToken lnameToken: lnameTokens.getReadings()) {
      String lnamePosTag = lnameToken.getPOSTag();
      if( lnamePosTag == null || ! lnamePosTag.contains(LAST_NAME_TAG) )
        continue;
      
      lnamePosTag = lnamePosTag.replaceAll(":(alt|ua_\\d{4}|xp\\d)", "");

      String initialsToken = initialsReadings.getAnalyzedToken(0).getToken();
      AnalyzedToken newToken = new AnalyzedToken(initialsToken, lnamePosTag.replace(LAST_NAME_TAG, ":nv:abbr:prop:"+initialType), initialsToken);
      newToken.setWhitespaceBefore(initialsReadings.isWhitespaceBefore());
      newTokens.add(newToken);
    }
    return new AnalyzedTokenReadings(newTokens, initialsReadings.getStartPos());
  }

  private static boolean isInitial(AnalyzedTokenReadings[] tokens, int pos) {
    return //pos < tokens.length - 1
        tokens[pos].getToken().endsWith(".")
        && INITIAL_REGEX.matcher(tokens[pos].getToken()).matches();
  }
  
//  private static boolean isSpace(String str) {
//    return str != null && (str.equals(" ") || str.equals("\u00A0")|| str.equals("\u202F"));
//  }
}
