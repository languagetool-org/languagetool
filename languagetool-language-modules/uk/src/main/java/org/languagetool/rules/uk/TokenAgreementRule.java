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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.language.Ukrainian;
import org.languagetool.rules.Category;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tagging.uk.IPOSTag;
import org.languagetool.tagging.uk.UkrainianTagger;

/**
 * A rule that checks if tokens in the sentence agree on inflection etc
 * 
 * @author Andriy Rysin
 */
public class TokenAgreementRule extends Rule {
  private static final String NO_VIDMINOK_SUBSTR = ":nv";
  private static final String REQUIRE_VIDMINOK_SUBSTR = ":rv_";
  private static final String VIDMINOK_SUBSTR = ":v_";
  private static final Pattern REQUIRE_VIDMINOK_REGEX = Pattern.compile(":r(v_[a-z]+)");
  private static final Pattern VIDMINOK_REGEX = Pattern.compile(":(v_[a-z]+)");

  private final Ukrainian ukrainian = new Ukrainian();

  private static final Set<String> STREETS = new HashSet<>(Arrays.asList(
      "Штрассе", "Авеню", "Стріт"
      ));

  public TokenAgreementRule(final ResourceBundle messages) throws IOException {
    if (messages != null) {
      super.setCategory(new Category(messages.getString("category_misc")));
    }
  }

  @Override
  public final String getId() {
    return "UK_TOKEN_AGREEMENT";
  }

  @Override
  public String getDescription() {
    return "Узгодження слів у реченні";
  }

  public String getShort() {
    return "Узгодження слів у реченні";
  }
  /**
   * Indicates if the rule is case-sensitive. 
   * @return true if the rule is case-sensitive, false otherwise.
   */
  public boolean isCaseSensitive() {
    return false;
  }
  
  private static boolean hasMultiword(AnalyzedTokenReadings analyzedTokenReadings) {
      for(AnalyzedToken analyzedToken: analyzedTokenReadings) {
        String posTag = analyzedToken.getPOSTag();
        if( posTag != null && posTag.startsWith("<") )
          return true;
      }
      return false;
  }

  @Override
  public final RuleMatch[] match(final AnalyzedSentence text) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = text.getTokensWithoutWhitespace();    

    AnalyzedTokenReadings reqTokenReadings = null;
    int i = -1;
    for (AnalyzedTokenReadings tokenReadings: tokens) {
      i++;

      String posTag = tokenReadings.getAnalyzedToken(0).getPOSTag();

      //TODO: skip conj напр. «бодай»

      if (posTag == null
          || posTag.contains(IPOSTag.unknown.getText())
          || posTag.equals(JLanguageTool.SENTENCE_START_TAGNAME) ){
        reqTokenReadings = null;
        continue;
      }


      String token = tokenReadings.getAnalyzedToken(0).getToken();
      if( posTag.contains(REQUIRE_VIDMINOK_SUBSTR) && tokenReadings.getReadingsLength() == 1 ) {
        String prep = token;

        if( hasMultiword(tokenReadings) ) {
          reqTokenReadings = null;
          continue;
        }

        if( prep.equals("за") && reverseSearch(tokens, i, "що") )
          continue;

        if( prep.equalsIgnoreCase("понад") )
          continue;

        if( (prep.equalsIgnoreCase("окрім") || prep.equalsIgnoreCase("крім"))
            && tokens.length > i+1 && tokens[i+1].getAnalyzedToken(0).getToken().equalsIgnoreCase("як") ) {
          reqTokenReadings = null;
          continue;
        }

        reqTokenReadings = tokenReadings;
        continue;
      }

      if( reqTokenReadings == null )
        continue;

      ArrayList<String> posTagsToFind = new ArrayList<>();

      //      if( tokens.length > i+1 && Character.isUpperCase(tokenReadings.getAnalyzedToken(0).getToken().charAt(0))
      //        && hasRequiredPosTag(Arrays.asList("v_naz"), tokenReadings)
      //        && Character.isUpperCase(tokens[i+1].getAnalyzedToken(0).getToken().charAt(0)) )
      //          continue; // "у Конан Дойла"

      String prep = reqTokenReadings.getAnalyzedToken(0).getToken();

      //TODO: for numerics only v_naz
      if( prep.equalsIgnoreCase("понад") ) { //&& tokenReadings.getAnalyzedToken(0).getPOSTag().equals(IPOSTag.numr) ) { 
        posTagsToFind.add("v_naz");
      }
      if( prep.equalsIgnoreCase("замість") ) {
        posTagsToFind.add("v_naz");
      }

      String reqPosTag = reqTokenReadings.getAnalyzedToken(0).getPOSTag();

      Matcher matcher = REQUIRE_VIDMINOK_REGEX.matcher(reqPosTag);
      while( matcher.find() ) {
        posTagsToFind.add(matcher.group(1));
      }

      for(AnalyzedToken readingToken: tokenReadings) {
        if( IPOSTag.numr.match(readingToken.getPOSTag()) ) {
          posTagsToFind.add("v_naz");  // TODO: only if noun is following?
          break;
        }
      }

      //      System.out.println("For " + tokenReadings + " to match " + posTagsToFind + " of " + reqTokenReadings.getToken());
      if( ! hasRequiredPosTag(posTagsToFind, tokenReadings) ) {
        if( isTokenToSkip(tokenReadings) )
          continue;

//        if( isTokenToIgnore(tokenReadings) ) {
//          reqTokenReadings = null;
//          continue;
//        }

        if( prep.equalsIgnoreCase("до") ) {
          if( tokenReadings.getAnalyzedToken(0).getToken().compareToIgnoreCase("Я") == 0 ) {  // від А до Я
            reqTokenReadings = null;
            continue;
          }
        }

        if( prep.equalsIgnoreCase("в") || prep.equalsIgnoreCase("у") || prep.equals("межи") || prep.equals("між") ) {
          if( hasRequiredPosTag(Arrays.asList("p:v_naz"), tokenReadings) ) {  //TODO: only for subset: президенти/депутати/мери/гості... or by verb піти/йти/балотуватися/записатися...
            reqTokenReadings = null;
            continue;
          }
        }

        // exceptions
        if( tokens.length > i+1 ) {
          if( isCapitalized( token ) 
              && STREETS.contains( tokens[i+1].getAnalyzedToken(0).getToken()) ) {
            reqTokenReadings = null;
            continue;
          }

          if( IPOSTag.isNum(tokens[i+1].getAnalyzedToken(0).getPOSTag())
              && (token.equals("мінус") || token.equals("плюс")
                  || token.equals("мінімум") || token.equals("максимум") ) ) {
            reqTokenReadings = null;
            continue;
          }

          if( (prep.equalsIgnoreCase("через") || prep.equalsIgnoreCase("на"))  // років 10, відсотки 3-4
              && (posTag.startsWith("noun:p:v_naz") || posTag.startsWith("noun:p:v_rod")) // token.equals("років") 
              && IPOSTag.isNum(tokens[i+1].getAnalyzedToken(0).getPOSTag()) ) {
            reqTokenReadings = null;
            continue;
          }

          if( (token.equals("собі") || token.equals("йому"))
              && tokens[i+1].getAnalyzedToken(0).getToken().startsWith("подібн") ) {
            //          reqTokenReadings = null;
            continue;
          }

          if( (token.equals("усім") || token.equals("всім"))
              && tokens[i+1].getAnalyzedToken(0).getToken().startsWith("відом") ) {
            //          reqTokenReadings = null;
            continue;
          }

          if( prep.equalsIgnoreCase("до") && token.equals("схід") 
                && tokens[i+1].getAnalyzedToken(0).getToken().equals("сонця") ) {
            reqTokenReadings = null;
            continue;
          }

          if( tokens[i+1].getAnalyzedToken(0).getToken().equals("«") 
              && tokens[i].getAnalyzedToken(0).getPOSTag().contains(":abbr") ) {
            reqTokenReadings = null;
            continue;
          }

          
          if( tokens.length > i+2 ) {
            if ((token.equals("нікому") || token.equals("ніким") || token.equals("нічим") || token.equals("нічому")) 
                && tokens[i+1].getAnalyzedToken(0).getToken().equals("не")) {
              //          reqTokenReadings = null;
              continue;
            }


          }
        }

        RuleMatch potentialRuleMatch = createRuleMatch(tokenReadings, reqTokenReadings, posTagsToFind);
        ruleMatches.add(potentialRuleMatch);
      }

      reqTokenReadings = null;
    }

    return toRuleMatchArray(ruleMatches);
  }

  private static boolean isCapitalized(String token) {
    return token.length() > 1 && Character.isUpperCase(token.charAt(0)) && Character.isLowerCase(token.charAt(1));
  }

  private boolean reverseSearch(AnalyzedTokenReadings[] tokens, int pos, String string) {
    for(int i=pos-1; i >= 0 && i > pos-4; i--) {
      if( tokens[i].getAnalyzedToken(0).getToken().equalsIgnoreCase("що") )
        return true;
    }
    return false;
  }

  private boolean isTokenToSkip(AnalyzedTokenReadings tokenReadings) {
    for(AnalyzedToken token: tokenReadings) {
//      System.out.println("    tag: " + token.getPOSTag() + " for " + token.getToken());
      if( IPOSTag.adv.match(token.getPOSTag())
          || IPOSTag.contains(token.getPOSTag(), "adv>")
          ||  IPOSTag.insert.match(token.getPOSTag()) )
        return true;
    }
    return false;
  }

//  private boolean isTokenToIgnore(AnalyzedTokenReadings tokenReadings) {
//    for(AnalyzedToken token: tokenReadings) {
//      if( token.getPOSTag().contains("abbr") )
//        return true;
//    }
//    return false;
//  }

  private boolean hasRequiredPosTag(Collection<String> posTagsToFind, AnalyzedTokenReadings tokenReadings) {
    boolean vidminokFound = false;  // because POS dictionary is not complete

    for(AnalyzedToken token: tokenReadings) {
      String posTag = token.getPOSTag();

      if( posTag == null ) {
        if( tokenReadings.getReadingsLength() == 1) 
          return true;
        
        continue;
      }
      
      if( posTag.contains(NO_VIDMINOK_SUBSTR) )
        return true;

      if( posTag.contains(VIDMINOK_SUBSTR) ) {
        vidminokFound = true;

        for(String posTagToFind: posTagsToFind) {
          //          System.out.println("  verifying: " + token + " -> " + posTag + " ~ " + posTagToFind);

          if ( posTag.contains(posTagToFind) )
            return true;
        }
      }
    }

    return ! vidminokFound; //false;
  }

  private RuleMatch createRuleMatch(AnalyzedTokenReadings tokenReadings, AnalyzedTokenReadings reqTokenReadings, List<String> posTagsToFind) {
    String tokenString = tokenReadings.getToken();

    Synthesizer ukrainianSynthesizer = ukrainian.getSynthesizer();

    ArrayList<String> suggestions = new ArrayList<>();
    String oldPosTag = tokenReadings.getAnalyzedToken(0).getPOSTag();
    String requiredPostTagsRegEx = ":(" + StringUtils.join(posTagsToFind,"|") + ")";
    String posTag = oldPosTag.replaceFirst(":v_[a-z]+", requiredPostTagsRegEx);

    //    System.out.println("  creating suggestion for " + tokenReadings + " / " + tokenReadings.getAnalyzedToken(0) +" and tag " + posTag);

    try {
      String[] synthesized = ukrainianSynthesizer.synthesize(tokenReadings.getAnalyzedToken(0), posTag, true);

      //      System.out.println("Synthesized: " + Arrays.asList(synthesized));
      suggestions.addAll( Arrays.asList(synthesized) );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    ArrayList<String> reqVidminkyNames = new ArrayList<>();
    for (String vidm: posTagsToFind) {
      reqVidminkyNames.add(UkrainianTagger.VIDMINKY_MAP.get(vidm));
    }

    ArrayList<String> foundVidminkyNames = new ArrayList<>();
    for(AnalyzedToken token: tokenReadings) {
      String posTag2 = token.getPOSTag();
      if( posTag2 != null && posTag2.contains(VIDMINOK_SUBSTR) ) {
        String vidmName = UkrainianTagger.VIDMINKY_MAP.get(posTag2.replaceFirst("^.*"+VIDMINOK_REGEX+".*$", "$1"));
        if( foundVidminkyNames.contains(vidmName) ) {
          if (posTag2.contains(":p:")) {
            vidmName = vidmName + " (мн.)";
            foundVidminkyNames.add(vidmName);
          }
          // else skip dup
        }
        else {
          foundVidminkyNames.add(vidmName);
        }
      }
    }

    String msg = MessageFormat.format("Прийменник «{0}» вимагає іншого відмінка: {1}, а знайдено: {2}", 
        reqTokenReadings.getToken(), StringUtils.join(reqVidminkyNames, ", "), StringUtils.join(foundVidminkyNames, ", "));
        
    if( tokenString.equals("їх") ) {
      msg += ". Можливо тут потрібно присвійний займенник «їхній»?";
      try {
        String newYihPostag = "adj:p" + requiredPostTagsRegEx + ".*";
        String[] synthesized = ukrainianSynthesizer.synthesize(new AnalyzedToken("їхній", "adj:m:v_naz:&pron", "їхній"), newYihPostag, true);
        suggestions.addAll( Arrays.asList(synthesized) );
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    else if( reqTokenReadings.getToken().equalsIgnoreCase("о") ) {
      for(AnalyzedToken token: tokenReadings.getReadings()) {
        String posTag2 = token.getPOSTag();
        if( posTag2.matches(".*:v_naz.*:anim.*") ) {
          msg += ". Можливо тут «о» — це вигук і потрібно кличний відмінок?";
          try {
            String newPostag = posTag2.replace("v_naz", "v_kly");
            String[] synthesized = ukrainianSynthesizer.synthesize(token, newPostag, false);
            for (String string : synthesized) {
              if( ! string.equals(token.getToken()) && ! suggestions.contains(string) ) {
                suggestions.add( string );
              }
            }
            break;
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      }
      
    }
        
    int pos = tokenReadings.getStartPos();

    RuleMatch potentialRuleMatch = new RuleMatch(this, pos, pos + tokenString.length(), msg, getShort());

    potentialRuleMatch.setSuggestedReplacements(suggestions);

    return potentialRuleMatch;
  }

  @Override
  public void reset() {
  }

}
