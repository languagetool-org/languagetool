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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedToken;
import org.languagetool.JLanguageTool;
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
  private static final String DEBUG_COMPOUNDS_PROPERTY = "org.languagetool.tagging.uk.UkrainianTagger.debugCompounds";
  private static final String TAG_ANIM = ":anim";
  private static final String VERB_TAG_FOR_REV_IMPR = IPOSTag.verb.getText()+":rev:impr";
  private static final String VERB_TAG_FOR_IMPR = IPOSTag.verb.getText()+":impr";
  private static final String ADJ_TAG_FOR_PO_ADV_MIS = IPOSTag.adj.getText() + ":m:v_mis";
  private static final String ADJ_TAG_FOR_PO_ADV_NAZ = IPOSTag.adj.getText() + ":m:v_naz";
  // full latin number regex: M{0,4}(CM|CD|D?C{0,3})(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})
  private static final Pattern NUMBER = Pattern.compile("[+-]?[€₴\\$]?[0-9]+(,[0-9]+)?([-–—][0-9]+(,[0-9]+)?)?(%|°С?)?|(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})");
  private static final Pattern DATE = Pattern.compile("[\\d]{2}\\.[\\d]{2}\\.[\\d]{4}");
  private static final String stdNounTag = IPOSTag.noun.getText() + ":.:v_";
  private static final int stdNounTagLen = stdNounTag.length();
  private static final Pattern stdNounTagRegex = Pattern.compile(stdNounTag + ".*");
  private static final Pattern stdNounNvTagRegex = Pattern.compile(IPOSTag.noun.getText() + ".*:nv.*");
  private static final Set<String> dashPrefixes;
  private static final Set<String> cityAvenue = new HashSet<>(Arrays.asList("сіті", "авеню", "стріт", "штрассе"));

  public static final Map<String, String> VIDMINKY_MAP;
  private static final Map<String, List<String>> NUMR_ENDING_MAP;
  private BufferedWriter compoundDebugWriter; 

  static {
    Map<String, String> map = new LinkedHashMap<>();
    map.put("v_naz", "називний");
    map.put("v_rod", "родовий");
    map.put("v_dav", "давальний");
    map.put("v_zna", "знахідний");
    map.put("v_oru", "орудний");
    map.put("v_mis", "місцевий");
    map.put("v_kly", "кличний");
    VIDMINKY_MAP = Collections.unmodifiableMap(map);

    Map<String, List<String>> map2 = new HashMap<>();
    map2.put("й", Arrays.asList(":m:v_naz", ":m:v_zna"));
    map2.put("го", Arrays.asList("m:v_rod", ":m:v_zna", ":n:v_rod"));
    map2.put("му", Arrays.asList(":m:v_dav", ":m:v_mis", ":n:v_dav", ":n:v_mis", ":f:v_zna"));  // TODO: depends on the last digit
//    map2.put("им", Arrays.asList(":m:v_oru", ":n:v_oru"));
//    map2.put("ім", Arrays.asList(":m:v_mis", ":n:v_mis"));
//    map2.put("та", Arrays.asList(":f:v_naz"));
//    map2.put("тої", Arrays.asList(":f:v_rod"));
//    map2.put("тій", Arrays.asList(":f:v_dav", ":f:v_mis"));
//    map2.put("ту", Arrays.asList(":f:v_zna"));
//    map2.put("тою", Arrays.asList(":f:v_oru"));
    map2.put("те", Arrays.asList(":n:v_naz", ":n:v_zna"));
    map2.put("ті", Arrays.asList(":p:v_naz", ":p:v_zna"));
//    map2.put("тих", Arrays.asList(":p:v_rod", ":p:v_zna"));
    NUMR_ENDING_MAP = Collections.unmodifiableMap(map2);
    
    try {
      InputStream is = JLanguageTool.getDataBroker().getFromResourceDirAsStream("/uk/dash_prefixes.txt");
      Scanner scanner = new Scanner(is,"UTF-8");
      String text = scanner.useDelimiter("\\A").next();
      scanner.close();
      dashPrefixes = new HashSet<>( java.util.Arrays.asList(text.split("\n")) );
    }
    catch(Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public final String getFileName() {
    return "/uk/ukrainian.dict";
  }

  @Override
  public String getManualAdditionsFileName() {
    return "/uk/added.txt";
  }

  public UkrainianTagger() {
    super();
    setLocale(new Locale("uk", "UA"));
    dontTagLowercaseWithUppercase();
    
    if( Boolean.valueOf( System.getProperty(DEBUG_COMPOUNDS_PROPERTY) ) ) {
      debugCompounds();
    }
  }

  private void debugCompounds() {
    Path newFile = Paths.get("compounds-unknown.txt");
    try {
       Files.deleteIfExists(newFile);
       newFile = Files.createFile(newFile);
       compoundDebugWriter = Files.newBufferedWriter(newFile, Charset.defaultCharset());
     } catch (IOException ex) {
       throw new RuntimeException(ex);
     }
  }

  @Override
  public List<AnalyzedToken> additionalTags(String word, WordTagger wordTagger) {
    if ( NUMBER.matcher(word).matches() ) {
      List<AnalyzedToken> additionalTaggedTokens  = new ArrayList<>();
      additionalTaggedTokens.add(new AnalyzedToken(word, IPOSTag.number.getText(), word));
      return additionalTaggedTokens;
    }

    if ( DATE.matcher(word).matches() ) {
      List<AnalyzedToken> additionalTaggedTokens  = new ArrayList<>();
      additionalTaggedTokens.add(new AnalyzedToken(word, IPOSTag.date.getText(), word));
      return additionalTaggedTokens;
    }

    if ( word.contains("-") ) {
      return guessCompoundTag(word);
    }
    
    return null;
  }

  private List<AnalyzedToken> guessCompoundTag(String word) {
    int dashIdx = word.lastIndexOf('-');
    if( dashIdx == 0 || dashIdx == word.length() - 1 )
      return null;

    int firstDashIdx = word.indexOf('-');
    if( dashIdx != firstDashIdx )
      return null;

    String leftWord = word.substring(0, dashIdx);
    String rightWord = word.substring(dashIdx + 1);

    List<TaggedWord> leftWdList = wordTagger.tag(leftWord);
    String leftLowerCase = leftWord.toLowerCase(conversionLocale);
    if( ! leftWord.equals(leftLowerCase)) {
      leftWdList.addAll(wordTagger.tag(leftLowerCase));
    }

    if( rightWord.equals("но") || rightWord.equals("бо") ) {
      if( leftWdList.isEmpty() )
        return null;

      List<AnalyzedToken> leftAnalyzedTokens = asAnalyzedTokenListForTaggedWords(leftWord, leftWdList);
      return verbImperNoBo(word, leftAnalyzedTokens);
    }

    if( rightWord.equals("таки") ) {
      if( leftWdList.isEmpty() )
        return null;

      List<AnalyzedToken> leftAnalyzedTokens = asAnalyzedTokenListForTaggedWords(leftWord, leftWdList);
      List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>(leftAnalyzedTokens.size());
      for (AnalyzedToken analyzedToken : leftAnalyzedTokens) {
        String posTag = analyzedToken.getPOSTag();
        newAnalyzedTokens.add(new AnalyzedToken(word, posTag, analyzedToken.getLemma()));
      }
      
      return newAnalyzedTokens.isEmpty() ? null : newAnalyzedTokens;
    }

    if( leftWord.equalsIgnoreCase("по") && rightWord.endsWith("ськи") ) {
      rightWord += "й";
    }
    
    List<TaggedWord> rightWdList = wordTagger.tag(rightWord);
    if( rightWdList.isEmpty() )
      return null;

    List<AnalyzedToken> rightAnalyzedTokens = asAnalyzedTokenListForTaggedWords(rightWord, rightWdList);

    if( leftWord.equalsIgnoreCase("по") ) {
      if( rightWord.endsWith("ому") ) {
        return poAdvMatch(word, rightAnalyzedTokens, ADJ_TAG_FOR_PO_ADV_MIS);
      }
      else if( rightWord.endsWith("ський") ) {
        return poAdvMatch(word, rightAnalyzedTokens, ADJ_TAG_FOR_PO_ADV_NAZ);
      }
      return null;
    }

    if( NUMBER.matcher(leftWord).matches() ) {
      List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>(rightAnalyzedTokens.size());
      // e.g. 101-го
      if( NUMR_ENDING_MAP.containsKey(rightWord) ) {
        List<String> tags = NUMR_ENDING_MAP.get(rightWord);
        for (String tag: tags) {
          // TODO: shall it be numr or adj?
          newAnalyzedTokens.add(new AnalyzedToken(word, IPOSTag.adj.getText()+tag, leftWord + "-" + "й"));
        }
      }
      else {
        // e.g. 100-річному
        for (AnalyzedToken analyzedToken : rightAnalyzedTokens) {
          if( analyzedToken.getPOSTag().startsWith(IPOSTag.adj.getText()) ) {
            newAnalyzedTokens.add(new AnalyzedToken(word, analyzedToken.getPOSTag(), leftWord + "-" + analyzedToken.getLemma()));
          }
        }
      }
      return newAnalyzedTokens.isEmpty() ? null : newAnalyzedTokens;
    }

    if( dashPrefixes.contains( leftWord ) || dashPrefixes.contains( leftWord.toLowerCase() ) ) {
      return eksNounMatch(word, rightAnalyzedTokens, leftWord);
    }

    if( word.startsWith("пів-") && Character.isUpperCase(word.charAt(4)) ) {
      List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>(rightAnalyzedTokens.size());
      
      for (AnalyzedToken rightAnalyzedToken : rightAnalyzedTokens) {
        String rightPosTag = rightAnalyzedToken.getPOSTag();

        if( rightPosTag == null )
          continue;

        if( rightPosTag.matches("^noun:[mfn]:v_rod.*") ) {
          for(String vid: VIDMINKY_MAP.keySet()) {
            if( vid.equals("v_kly") )
              continue;
            String posTag = rightPosTag.replaceFirst("v_...", vid);
            newAnalyzedTokens.add(new AnalyzedToken(word, posTag, word));
          }
        }
      }

      return newAnalyzedTokens;
    }

    if( Character.isUpperCase(leftWord.charAt(0)) && cityAvenue.contains(rightWord) ) {
      if( leftWdList.isEmpty() )
        return null;
      
      List<AnalyzedToken> leftAnalyzedTokens = asAnalyzedTokenListForTaggedWords(leftWord, leftWdList);
      return cityAvenueMatch(word, leftAnalyzedTokens);
    }

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

    debug_compound_write(word);
    
    return null;
  }

  private void debug_compound_write(String word) {
    if( compoundDebugWriter == null )
      return;
    
    try {
      compoundDebugWriter.append(word);
      compoundDebugWriter.newLine();
      compoundDebugWriter.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private List<AnalyzedToken> cityAvenueMatch(String word, List<AnalyzedToken> leftAnalyzedTokens) {
    List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>(leftAnalyzedTokens.size());
    
    for (AnalyzedToken analyzedToken : leftAnalyzedTokens) {
      String posTag = analyzedToken.getPOSTag();
      if( posTag.matches(IPOSTag.noun.getText() + ":.:v_naz.*") ) {
        newAnalyzedTokens.add(new AnalyzedToken(word, posTag.replaceFirst("v_naz", "nv"), word));
      }
    }
    
    return newAnalyzedTokens.isEmpty() ? null : newAnalyzedTokens;
  }

  private List<AnalyzedToken> verbImperNoBo(String word, List<AnalyzedToken> leftAnalyzedTokens) {
    List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>(leftAnalyzedTokens.size());
    
    for (AnalyzedToken analyzedToken : leftAnalyzedTokens) {
      String posTag = analyzedToken.getPOSTag();
      if( posTag.startsWith( VERB_TAG_FOR_IMPR) 
          || posTag.startsWith( VERB_TAG_FOR_REV_IMPR) ) {
        newAnalyzedTokens.add(new AnalyzedToken(word, posTag, analyzedToken.getLemma()));
      }
    }
    
    return newAnalyzedTokens.isEmpty() ? null : newAnalyzedTokens;
  }

  private static String getGenderConj(String posTag) {
    if( posTag.matches("(noun|adjp|numr):.:v_....*") )
      return posTag.substring(5, 11);

    if( posTag.matches("adj:.:v_....*") )
      return posTag.substring(4, 10);
    
    return null;
  }
  
  private List<AnalyzedToken> tagMatch(String word, List<AnalyzedToken> leftAnalyzedTokens, List<AnalyzedToken> rightAnalyzedTokens) {
    List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>();
    
 boolean anim_inanim = false;
    
    for (AnalyzedToken leftAnalyzedToken : leftAnalyzedTokens) {
      String leftPosTag = leftAnalyzedToken.getPOSTag();

      for (AnalyzedToken rightAnalyzedToken : rightAnalyzedTokens) {
        String rightPosTag = rightAnalyzedToken.getPOSTag();
        
        if( leftPosTag == null || rightPosTag == null )
          continue;
        
        if (leftPosTag.equals(rightPosTag) 
            && (leftPosTag.startsWith(IPOSTag.numr.getText()) 
                || leftPosTag.startsWith(IPOSTag.adv.getText()) 
                || leftPosTag.startsWith(IPOSTag.adj.getText())) ) {
          newAnalyzedTokens.add(new AnalyzedToken(word, leftPosTag, leftAnalyzedToken.getLemma() + "-" + rightAnalyzedToken.getLemma()));
        }
        // noun-noun
        else if ( leftPosTag.startsWith(IPOSTag.noun.getText()) && rightPosTag.startsWith(IPOSTag.noun.getText()) ) {
          String agreedPosTag = getArgreedPosTag(leftPosTag, rightPosTag);

  if( agreedPosTag == null && ! istotaNeistotaMatch(leftPosTag, rightPosTag) ) {
    anim_inanim = true;
  }
          
          if( agreedPosTag == null && rightPosTag.startsWith(IPOSTag.noun.getText()+":m:v_naz")
              && (rightAnalyzedToken.getToken().equals("максимум")
                  || rightAnalyzedToken.getToken().equals("мінімум")) ) {
            agreedPosTag = leftPosTag;
          }
          
          if( agreedPosTag != null ) {
            newAnalyzedTokens.add(new AnalyzedToken(word, agreedPosTag, leftAnalyzedToken.getLemma() + "-" + rightAnalyzedToken.getLemma()));
          }
        }
        // noun-adj match: Буш-молодший, братів-православних, рік-два
        else if( leftPosTag.startsWith(IPOSTag.noun.getText()) 
            && IPOSTag.startsWith(rightPosTag, IPOSTag.adj, IPOSTag.numr) ) {
          String leftGenderConj = getGenderConj(leftPosTag);
          if( leftGenderConj != null && leftGenderConj.equals(getGenderConj(rightPosTag)) ) {
            newAnalyzedTokens.add(new AnalyzedToken(word, leftPosTag, leftAnalyzedToken.getLemma() + "-" + rightAnalyzedToken.getLemma()));
          }
        }
      }
    }

 if( anim_inanim && newAnalyzedTokens.isEmpty() ) {
   debug_compound_write(word + " anim-inanim");
 }
    
    return newAnalyzedTokens.isEmpty() ? null : newAnalyzedTokens;
  }

  private String getArgreedPosTag(String leftPosTag, String rightPosTag) {
    if( isPlural(leftPosTag) && ! isPlural(rightPosTag)
        || ! isPlural(leftPosTag) && isPlural(rightPosTag) )
      return null;
    
    if( ! istotaNeistotaMatch(leftPosTag, rightPosTag) )
      return null;
    
//    if( stdNounNvTagRegex.matcher(leftPosTag).matches() ) {
//      if( stdNounTagRegex.matcher(rightPosTag).matches() ) {
//        return rightPosTag;
//      }
//    }
//    else
      if( stdNounTagRegex.matcher(leftPosTag).matches() ) {
      // TODO: finish this
//      if( stdNounNvTagRegex.matcher(rightPosTag).matches()) {
//        return leftPosTag;
//      }
//      else 
      if (stdNounTagRegex.matcher(rightPosTag).matches()) {
        String substring1 = leftPosTag.substring(stdNounTagLen, stdNounTagLen + 3);
        String substring2 = rightPosTag.substring(stdNounTagLen, stdNounTagLen + 3);
        if( substring1.equals(substring2) ) {
          if( leftPosTag.contains(":nv") )
            return rightPosTag;
          
          return leftPosTag;
//          return istotaNeistota(leftPosTag, rightPosTag) ? leftPosTag : rightPosTag;
        }
//        else if( istotaNeistotaNazZna(leftPosTag, rightPosTag) ) {
//          return rightPosTag;
//        }
//        else if( istotaNeistotaNazZna(rightPosTag, leftPosTag) ) {
//          return leftPosTag;
//        }
      }
    }

    return null;
  }

  private static boolean istotaNeistota(String leftPosTag, String rightPosTag) {
    return leftPosTag.contains(TAG_ANIM) && ! rightPosTag.contains(TAG_ANIM);
  }

  private static boolean istotaNeistotaMatch(String leftPosTag, String rightPosTag) {
    return leftPosTag.contains(TAG_ANIM) && rightPosTag.contains(TAG_ANIM)
        || ! leftPosTag.contains(TAG_ANIM) && ! rightPosTag.contains(TAG_ANIM);
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
    List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>(analyzedTokens.size());
    
    for (AnalyzedToken analyzedToken : analyzedTokens) {
      String posTag = analyzedToken.getPOSTag();
      if( posTag.startsWith( IPOSTag.adj.getText() ) ) {
        newAnalyzedTokens.add(new AnalyzedToken(word, posTag, leftWord + "-" + analyzedToken.getLemma()));
      }
    }
    
    return newAnalyzedTokens.isEmpty() ? null : newAnalyzedTokens;
  }

  private List<AnalyzedToken> eksNounMatch(String word, List<AnalyzedToken> analyzedTokens, String leftWord) {
    List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>(analyzedTokens.size());
    
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
