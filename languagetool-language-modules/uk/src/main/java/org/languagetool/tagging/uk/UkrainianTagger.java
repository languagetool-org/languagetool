/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tagging.uk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedToken;
import org.languagetool.tagging.BaseTagger;
import org.languagetool.tagging.TaggedWord;
import org.languagetool.tagging.WordTagger;

/** 
 * Ukrainian part-of-speech tagger.
 * See README for details, the POS tagset is
 * described in tagset.txt
 * 
 * @author Andriy Rysin
 */
public class UkrainianTagger extends BaseTagger {
  private static final String VERB_TAG_FOR_REV_IMPR = IPOSTag.verb.getText()+":rev:impr";
  private static final String VERB_TAG_FOR_IMPR = IPOSTag.verb.getText()+":impr";
  private static final String ADJ_TAG_FOR_PO_ADV_MIS = IPOSTag.adj.getText() + ":m:v_mis";
  private static final String ADJ_TAG_FOR_PO_ADV_NAZ = IPOSTag.adj.getText() + ":m:v_naz";
  private static final Pattern NUMBER = Pattern.compile("[+-]?[0-9]+(,[0-9]+)?([-–—][0-9]+(,[0-9]+)?)?(%|°С?)?");
  private static final String stdNounTag = IPOSTag.noun.getText() + ":.:v_";
  private static final int stdNounTagLen = stdNounTag.length();
  private static final Pattern stdNounTagRegex = Pattern.compile(stdNounTag + ".*");
  private static final Pattern stdNounNvTagRegex = Pattern.compile(IPOSTag.noun.getText() + ":.:nv");
  private static final HashSet<String> dashPrefixes = new HashSet<String>(
        Arrays.asList("віце", "екс", "лейб", "максі", "міді", "міні", "обер", "горе", "медіа"));
  private static final HashSet<String> cityAvenue = new HashSet<String>(Arrays.asList("сіті", "авеню", "стріт", "штрассе"));

  public static final Map<String, String> VIDMINKY_MAP;

  static {
    HashMap<String, String> map = new HashMap<String, String>();
    map.put("v_naz", "називний");
    map.put("v_rod", "родовий");
    map.put("v_dav", "давальний");
    map.put("v_zna", "знахідний");
    map.put("v_oru", "орудний");
    map.put("v_mis", "місцевий");
    map.put("v_kly", "кличний");
    VIDMINKY_MAP = Collections.unmodifiableMap(map);
  }

  @Override
  public final String getFileName() {
    return "/uk/ukrainian.dict";
  }

  public UkrainianTagger() {
    super();
    setLocale(new Locale("uk", "UA"));
    dontTagLowercaseWithUppercase();
  }

  @Override
  public List<AnalyzedToken> additionalTags(String word, WordTagger wordTagger) {
    if ( NUMBER.matcher(word).matches() ){
      List<AnalyzedToken> additionalTaggedTokens  = new ArrayList<>();
      additionalTaggedTokens.add(new AnalyzedToken(word, IPOSTag.number.getText(), word));
      return additionalTaggedTokens;
    }
    
    if ( word.contains("-") ) {
      int dashIdx = word.lastIndexOf('-');
      if( dashIdx == 0 || dashIdx == word.length() - 1 )
        return null;

      int firstDashIdx = word.indexOf('-');
      if( dashIdx != firstDashIdx )
        return null;

      String leftWord = word.substring(0, dashIdx);
      String rightWord = word.substring(dashIdx + 1);
      
      if( rightWord.equals("но") || rightWord.equals("бо") ) {
        List<TaggedWord> leftWdList = wordTagger.tag(leftWord);
        if( leftWdList.isEmpty() )
          return null;

        List<AnalyzedToken> leftAnalyzedTokens = asAnalyzedTokenListForTaggedWords(leftWord, leftWdList);
        return verbImperNoBo(word, leftAnalyzedTokens);
      }

      if( leftWord.equalsIgnoreCase("по") && rightWord.endsWith("ськи") ) {
        rightWord += "й";
      }
      
      List<TaggedWord> wdList = wordTagger.tag(rightWord);
      if( wdList.isEmpty() )
        return null;

      List<AnalyzedToken> rightAnalyzedTokens = asAnalyzedTokenListForTaggedWords(rightWord, wdList);

      if( leftWord.equalsIgnoreCase("по") ) {
        if( rightWord.endsWith("ому") ) {
          return poAdvMatch(word, rightAnalyzedTokens, ADJ_TAG_FOR_PO_ADV_MIS);
        }
        else if( rightWord.endsWith("ський") ) {
          return poAdvMatch(word, rightAnalyzedTokens, ADJ_TAG_FOR_PO_ADV_NAZ);
        }
        return null;
      }

      if( dashPrefixes.contains( leftWord.toLowerCase() ) ) {
        return eksNounMatch(word, rightAnalyzedTokens, leftWord);
      }

      if( Character.isUpperCase(leftWord.charAt(0)) && cityAvenue.contains(rightWord) ) {
        List<TaggedWord> leftWdList = wordTagger.tag(leftWord);
        if( leftWdList.isEmpty() )
          return null;
        
        List<AnalyzedToken> leftAnalyzedTokens = asAnalyzedTokenListForTaggedWords(leftWord, leftWdList);
        return cityAvenueMatch(word, leftAnalyzedTokens);
      }

      List<TaggedWord> leftWdList = wordTagger.tag(leftWord);
      if( ! leftWdList.isEmpty() ) {
        List<AnalyzedToken> leftAnalyzedTokens = asAnalyzedTokenListForTaggedWords(leftWord, leftWdList);

        List<AnalyzedToken> tagMatch = tagMatch(word, leftAnalyzedTokens, rightAnalyzedTokens);
        if( tagMatch != null ) {
          return tagMatch;
        }
      }

      if( leftWord.endsWith("о") ) {
        return oAdjMatch(word, rightAnalyzedTokens, leftWord);
      }

    }
    
    return null;
  }

  private List<AnalyzedToken> cityAvenueMatch(String word, List<AnalyzedToken> leftAnalyzedTokens) {
    List<AnalyzedToken> newAnalyzedTokens = new ArrayList<AnalyzedToken>(leftAnalyzedTokens.size());
    
    for (AnalyzedToken analyzedToken : leftAnalyzedTokens) {
      String posTag = analyzedToken.getPOSTag();
      if( posTag.matches(IPOSTag.noun.getText() + ":.:v_naz.*") ) {
        newAnalyzedTokens.add(new AnalyzedToken(word, posTag.replaceFirst("v_naz", "nv"), word));
      }
    }
    
    return newAnalyzedTokens.isEmpty() ? null : newAnalyzedTokens;
  }

  private List<AnalyzedToken> verbImperNoBo(String word, List<AnalyzedToken> leftAnalyzedTokens) {
    List<AnalyzedToken> newAnalyzedTokens = new ArrayList<AnalyzedToken>(leftAnalyzedTokens.size());
    
    for (AnalyzedToken analyzedToken : leftAnalyzedTokens) {
      String posTag = analyzedToken.getPOSTag();
      if( posTag.startsWith( VERB_TAG_FOR_IMPR) 
          || posTag.startsWith( VERB_TAG_FOR_REV_IMPR) ) {
        newAnalyzedTokens.add(new AnalyzedToken(word, posTag, analyzedToken.getLemma()));
      }
    }
    
    return newAnalyzedTokens.isEmpty() ? null : newAnalyzedTokens;
  }

  private List<AnalyzedToken> tagMatch(String word, List<AnalyzedToken> leftAnalyzedTokens, List<AnalyzedToken> rightAnalyzedTokens) {
    List<AnalyzedToken> newAnalyzedTokens = new ArrayList<AnalyzedToken>();
    
    for (AnalyzedToken leftAnalyzedToken : leftAnalyzedTokens) {
      String leftPosTag = leftAnalyzedToken.getPOSTag();

      for (AnalyzedToken rightAnalyzedToken : rightAnalyzedTokens) {
        String rightPosTag = rightAnalyzedToken.getPOSTag();
        
        if( leftPosTag != null && rightPosTag != null ) {
          if (leftPosTag.equals(rightPosTag) 
              && (leftPosTag.startsWith(IPOSTag.numr.getText()) || leftPosTag.startsWith(IPOSTag.adv.getText()) || leftPosTag.startsWith(IPOSTag.adj.getText())) ) {
            newAnalyzedTokens.add(new AnalyzedToken(word, leftPosTag, leftAnalyzedToken.getLemma() + "-" + rightAnalyzedToken.getLemma()));
          }
          else if ( leftPosTag.startsWith(IPOSTag.noun.getText()) && rightPosTag.startsWith(IPOSTag.noun.getText()) ) {
            String agreedPosTag = getArgreedPosTag(leftPosTag, rightPosTag);
            if( agreedPosTag != null ) {
              newAnalyzedTokens.add(new AnalyzedToken(word, agreedPosTag, leftAnalyzedToken.getLemma() + "-" + rightAnalyzedToken.getLemma()));
            }
          }
        }
        
        //TODO: noun-adj match?
//        newAnalyzedTokens.add(new AnalyzedToken(word, leftPosTag, leftWord + "-" + analyzedToken.getLemma()));
      }
    }

    return newAnalyzedTokens.isEmpty() ? null : newAnalyzedTokens;
  }

  private String getArgreedPosTag(String leftPosTag, String rightPosTag) {
    if( isPlural(leftPosTag) && ! isPlural(rightPosTag) )
      return null;
    
    if( stdNounTagRegex.matcher(leftPosTag).matches() ) {
      // TODO: finish this
//      if (stdNounTagRegex.matcher(rightPosTag).matches()) {
//        String substring1 = leftPosTag.substring(stdNounTagLen, stdNounTagLen + 3);
//        String substring2 = rightPosTag.substring(stdNounTagLen, stdNounTagLen + 3);
//        if( substring1.equals(substring2) ) {
//          return istotaNeistota(leftPosTag, rightPosTag) ? leftPosTag : rightPosTag;
//        }
//        else if( istotaNeistotaNazZna(leftPosTag, rightPosTag) ) {
//          return rightPosTag;
//        }
//        else if( istotaNeistotaNazZna(rightPosTag, leftPosTag) ) {
//          return leftPosTag;
//        }
//      }
//      else 
      if( stdNounNvTagRegex.matcher(rightPosTag).matches()) {
        return leftPosTag;
      }
    }
    else if( stdNounNvTagRegex.matcher(leftPosTag).matches() ) {
      if( stdNounTagRegex.matcher(rightPosTag).matches() ) {
        return rightPosTag;
      }
    }

    return null;
  }

  private static boolean istotaNeistota(String leftPosTag, String rightPosTag) {
    return leftPosTag.contains(":ist") && ! rightPosTag.contains(":ist");
  }

  private static boolean istotaNeistotaNazZna(String leftPosTag, String rightPosTag) {
    return istotaNeistota(leftPosTag, rightPosTag)
        && (isPlural(leftPosTag) && isPlural(rightPosTag) || rightPosTag.contains(":m:") || rightPosTag.contains(":n:")) 
        && leftPosTag.contains(":v_naz") && rightPosTag.contains(":v_zna");
  }

  private static boolean isPlural(String posTag) {
    return posTag.startsWith(IPOSTag.noun.getText() + ":p:");
  }

  private List<AnalyzedToken> oAdjMatch(String word, List<AnalyzedToken> analyzedTokens, String leftWord) {
    List<AnalyzedToken> newAnalyzedTokens = new ArrayList<AnalyzedToken>(analyzedTokens.size());
    
    for (AnalyzedToken analyzedToken : analyzedTokens) {
      String posTag = analyzedToken.getPOSTag();
      if( posTag.startsWith( IPOSTag.adj.getText() ) ) {
        newAnalyzedTokens.add(new AnalyzedToken(word, posTag, leftWord + "-" + analyzedToken.getLemma()));
      }
    }
    
    return newAnalyzedTokens.isEmpty() ? null : newAnalyzedTokens;
  }

  private List<AnalyzedToken> eksNounMatch(String word, List<AnalyzedToken> analyzedTokens, String leftWord) {
    List<AnalyzedToken> newAnalyzedTokens = new ArrayList<AnalyzedToken>(analyzedTokens.size());
    
    for (AnalyzedToken analyzedToken : analyzedTokens) {
      String posTag = analyzedToken.getPOSTag();
      if( posTag.startsWith( IPOSTag.noun.getText() ) ) {
        newAnalyzedTokens.add(new AnalyzedToken(word, posTag, leftWord + "-" + analyzedToken.getLemma()));
      }
    }
    
    return newAnalyzedTokens.isEmpty() ? null : newAnalyzedTokens;
  }

  private List<AnalyzedToken> poAdvMatch(String word, List<AnalyzedToken> analyzedTokens, String adjTag) {
    
    for (AnalyzedToken analyzedToken : analyzedTokens) {
      String posTag = analyzedToken.getPOSTag();
      if( posTag.startsWith( adjTag ) ) {
        return Arrays.asList(new AnalyzedToken(word, IPOSTag.adv.getText(), word));
      }
    }
    
    return null;
  }

}
