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
import java.util.regex.Pattern;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.language.Ukrainian;
import org.languagetool.rules.uk.LemmaHelper;
import org.languagetool.tagging.disambiguation.AbstractDisambiguator;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.rules.XmlRuleDisambiguator;
import org.languagetool.tagging.uk.PosTagHelper;

/**
 * Hybrid chunker-disambiguator for Ukrainian.
 */

public class UkrainianHybridDisambiguator extends AbstractDisambiguator {
  private static final String LAST_NAME_TAG = ":lname";
  private static final Pattern INITIAL_REGEX = Pattern.compile("[А-ЯІЇЄҐ]\\.");
  private static final Pattern INANIM_VKLY = Pattern.compile("noun:inanim:.:v_kly.*");
  private static final Pattern PLURAL_NAME = Pattern.compile("noun:anim:p:.*:fname.*");
//  private static final Pattern PLURAL_LNAME_OR_PATR = Pattern.compile("noun:anim:p:.*:lname.*");
  private static final String PLURAL_LNAME = "noun:anim:p:.*:[lp]name.*";
  private static final String ST_ABBR = "ст.";
  private static final Pattern LATIN_DIGITS_PATTERN = Pattern.compile("[XIVХІ]+([–—-][XIVХІ]+)?");
  private static final Pattern DIGITS_PATTERN = Pattern.compile("[0-9]+([–—-][0-9]+)?");
  private static final Pattern STATION_NAME_PATTERN = Pattern.compile("метро|[А-Я][а-яіїєґ'-]+");
  
  private final Disambiguator chunker = new UkrainianMultiwordChunker("/uk/multiwords.txt", true);

  private final Disambiguator disambiguator = new XmlRuleDisambiguator(new Ukrainian());
  private final SimpleDisambiguator simpleDisambiguator = new SimpleDisambiguator();

  
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
    retagInitials(input);
    removeInanimVKly(input);
    removePluralForNames(input);
    simpleDisambiguator.removeRareForms(input);
    disambiguateSt(input);

    return input;
  }


  private void removeInanimVKly(AnalyzedSentence input) {
    AnalyzedTokenReadings[] tokens = input.getTokensWithoutWhitespace();
    for (int i = 1; i < tokens.length; i++) {
      List<AnalyzedToken> analyzedTokens = tokens[i].getReadings();
      
      if( i < tokens.length -1
          && Arrays.asList(",", "!", "»", "\u201C", "\u201D", "...").contains(tokens[i+1].getToken()) 
          && PosTagHelper.hasPosTag(tokens[i-1], "adj.*v_kly.*") )
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
//        System.err.println("====================1 " + tokens[i]);
        for(AnalyzedToken analyzedToken: inanimVklyReadings) {
          tokens[i].removeReading(analyzedToken);
//          System.err.println("===== Removing: " + analyzedToken);
//          System.err.println("====================2 " + tokens[i]);
        }
      }
    }
  }

  private void removePluralForNames(AnalyzedSentence input) {
    AnalyzedTokenReadings[] tokens = input.getTokensWithoutWhitespace();
    for (int i = 1; i < tokens.length; i++) {
      List<AnalyzedToken> analyzedTokens = tokens[i].getReadings();
      
      if( i > 1
          && (PosTagHelper.hasPosTag(tokens[i-1], "adj:p:.*")
              //TODO: unify adj and noun
              || PosTagHelper.hasPosTag(tokens[i-1], ".*num.*")
              || LemmaHelper.hasLemma(tokens[i-1], Arrays.asList("багато", "мало", "півсотня", "сотня"))) )
        continue;

      // Юріїв Луценків
      if( i<tokens.length-1 
          && PosTagHelper.hasPosTag(tokens[i+1], PLURAL_LNAME) )
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
          
//        System.err.println("-- " + analyzedTokens.get(j));
        if( PLURAL_NAME.matcher(posTag).matches() ) {
          pluralNameReadings.add(analyzedTokens.get(j));
        }
        else {
          otherFound = true;
        }
      }
      if( pluralNameReadings.size() > 0 && otherFound ) {
//        System.err.println("====================1 " + tokens[i]);
        for(AnalyzedToken analyzedToken: pluralNameReadings) {
          tokens[i].removeReading(analyzedToken);
//          System.err.println("===== Removing: " + analyzedToken);
//          System.err.println("====================2 " + tokens[i]);
        }
      }
    }
  }

  private void retagInitials(AnalyzedSentence input) {
    AnalyzedTokenReadings[] tokens = input.getTokens();
    for (int i = 1; i < tokens.length - 1; i++) {
      if( isInitial(tokens, i) ) {
        boolean spaced = isSpace(tokens[i+1].getToken());
        int spacedOffset = spaced ? 1 : 0;

        int nextPos = i + 1 + spacedOffset;
        
        // checking for :pname
        if( nextPos + 1 + spacedOffset < tokens.length
            && isInitial(tokens, nextPos)
            && (! spaced || isSpace(tokens[nextPos+1].getToken()) )
            && tokens[nextPos + 1 + spacedOffset].hasPartialPosTag(LAST_NAME_TAG) ) {
          
          int currPos = nextPos;
          nextPos += 1 + spacedOffset;
          
          AnalyzedTokenReadings newReadings = getInitialReadings(tokens[currPos], tokens[nextPos], "pname");
          tokens[currPos] = newReadings;
        }
        
        if( nextPos < tokens.length && tokens[nextPos].hasPartialPosTag(LAST_NAME_TAG) ) {
          AnalyzedTokenReadings newReadings = getInitialReadings(tokens[i], tokens[nextPos], "fname");
          tokens[i] = newReadings;
          i = nextPos;
        }
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
          readings.removeReading(analyzedToken);
        }
      }

  }

  private static AnalyzedTokenReadings getInitialReadings(AnalyzedTokenReadings initialsReadings, AnalyzedTokenReadings lnameTokens, String initialType) {
    List<AnalyzedToken> newTokens = new ArrayList<>();
    for(AnalyzedToken lnameToken: lnameTokens.getReadings()) {
      String lnamePosTag = lnameToken.getPOSTag();
      if( lnamePosTag == null || ! lnamePosTag.contains(LAST_NAME_TAG) )
        continue;
      
      String initialsToken = initialsReadings.getAnalyzedToken(0).getToken();
      AnalyzedToken newToken = new AnalyzedToken(initialsToken, lnamePosTag.replace(LAST_NAME_TAG, ":"+initialType+":abbr"), initialsToken);
      newTokens.add(newToken);
    }
    return new AnalyzedTokenReadings(newTokens, initialsReadings.getStartPos());
  }

  private static boolean isInitial(AnalyzedTokenReadings[] tokens, int pos) {
    return pos < tokens.length - 1
        && INITIAL_REGEX.matcher(tokens[pos].getToken()).matches();
  }
  
  private static boolean isSpace(String str) {
    return str != null && (str.equals(" ") || str.equals("\u00A0")|| str.equals("\u202F"));
  }
}
