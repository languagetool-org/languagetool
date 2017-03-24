/* LanguageTool, a natural language style checker 
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedToken;
import org.languagetool.JLanguageTool;
import org.languagetool.rules.uk.LemmaHelper;
import org.languagetool.tagging.TaggedWord;
import org.languagetool.tagging.WordTagger;

/**
 * @since 3.0
 */
class CompoundTagger {
  private static final String DEBUG_COMPOUNDS_PROPERTY = "org.languagetool.tagging.uk.UkrainianTagger.debugCompounds";

  private static final String TAG_ANIM = ":anim";
  private static final String TAG_INANIM = ":inanim";
  private static final String NV_TAG = ":nv";
//  private static final String V_U_TAG = ":v-u";
  private static final Pattern EXTRA_TAGS = Pattern.compile(":bad");
  private static final Pattern EXTRA_TAGS_DROP = Pattern.compile(":(compb|np|ns|slang|rare|xp[1-9])");
  private static final Pattern NOUN_SING_V_ROD_REGEX = Pattern.compile("noun.*?:[mfn]:v_rod.*");
  private static final Pattern NOUN_V_NAZ_REGEX = Pattern.compile("noun.*?:.:v_naz.*");
  private static final Pattern SING_REGEX_F = Pattern.compile(":[mfn]:");
  private static final Pattern O_ADJ_PATTERN = Pattern.compile(".*(о|[чшщ]е)");
  private static final Pattern DASH_PREFIX_LAT_PATTERN = Pattern.compile("[a-zA-Z]{3,}");

  private static final Pattern MNP_NAZ_REGEX = Pattern.compile(".*:[mnp]:v_naz.*");
  private static final Pattern MNP_ZNA_REGEX = Pattern.compile(".*:[mnp]:v_zna.*");
  private static final Pattern MNP_ROD_REGEX = Pattern.compile(".*:[mnp]:v_rod.*");

  private static final Pattern stdNounTagRegex = Pattern.compile("noun:(?:in)?anim:(.):(v_...).*");
  private static final Set<String> dashPrefixes;
  private static final Set<String> leftMasterSet;
  private static final Set<String> cityAvenue = new HashSet<>(Arrays.asList("сіті", "авеню", "стріт", "штрассе"));
  private static final Map<String, Pattern> rightPartsWithLeftTagMap = new HashMap<>();
  private static final Set<String> slaveSet;
  private static final Map<String, List<String>> NUMR_ENDING_MAP;


  private static final String ADJ_TAG_FOR_PO_ADV_MIS = "adj:m:v_mis";
  private static final String ADJ_TAG_FOR_PO_ADV_NAZ = "adj:m:v_naz";

  private static final List<String> LEFT_O_ADJ = Arrays.asList(
    "австро", "адиго", "американо", "англо", "афро", "еко", "етно", "іспано", "києво", 
    "марокано", "угро"
  );

  private static final List<String> LEFT_INVALID = Arrays.asList(
    "авіа", "авто", "агро", "анти", "аудіо", "біо", "вело", "відео", "водо", "газо", "геліо", "гео", "гідро", "давньо", "древньо", "екзо",
    "екстра", "електро", "зоо", "ізо", "квазі", "кіно", "космо", "контр", "лже", "максимально", "мінімально", "макро", "мета",
    "метео", "мікро", "мілі", "моно", "мото", "мульти", "напів", "нео", "палео", "пост", "псевдо", "радіо",
    "рентгено", "соціо", "стерео", "супер", "теле", "термо", "турбо", "ультра", "фоно", "фото"
  );

  private static final List<String> LEFT_O_ADJ_INVALID = Arrays.asList(
    "багато", "мало", "високо", "низько"
  );

  static {
    Map<String, List<String>> map2 = new HashMap<>();
    map2.put("й", Arrays.asList(":m:v_naz", ":m:v_zna:rinanim", ":f:v_dav", ":f:v_mis")); // 1-й
    map2.put("ій", Arrays.asList(":m:v_naz", ":m:v_zna:rinanim", ":f:v_dav", ":f:v_mis")); // 3-тій
    map2.put("го", Arrays.asList(":m:v_rod", ":m:v_zna:ranim", ":n:v_rod"));
    map2.put("му", Arrays.asList(":m:v_dav", ":m:v_mis", ":n:v_dav", ":n:v_mis", ":f:v_zna"));  // TODO: depends on the last digit
    map2.put("м", Arrays.asList(":m:v_oru", ":n:v_oru", ":p:v_dav"));
    map2.put("им", Arrays.asList(":m:v_oru", ":n:v_oru", ":p:v_dav"));
    map2.put("ім", Arrays.asList(":m:v_oru", ":m:v_mis", ":n:v_oru", ":n:v_mis"));
    map2.put("ша", Arrays.asList(":f:v_naz")); // 1-ша
    map2.put("га", Arrays.asList(":f:v_naz")); // 2-га
    map2.put("тя", Arrays.asList(":f:v_naz")); // 3-тя
    map2.put("та", Arrays.asList(":f:v_naz")); // 4-та
    map2.put("ої", Arrays.asList(":f:v_rod")); // 4-тої
    map2.put("тої", Arrays.asList(":f:v_rod")); // 4-тої
    map2.put("тій", Arrays.asList(":f:v_dav", ":f:v_mis")); // 3-тій
    map2.put("ту", Arrays.asList(":f:v_zna"));
    map2.put("тю", Arrays.asList(":f:v_zna"));
    map2.put("ою", Arrays.asList(":f:v_oru"));
    map2.put("ге", Arrays.asList(":n:v_naz", ":n:v_zna")); // 2-ге
    map2.put("тє", Arrays.asList(":n:v_naz", ":n:v_zna")); // 3-тє
    map2.put("те", Arrays.asList(":n:v_naz", ":n:v_zna")); // 4-те
    map2.put("ті", Arrays.asList(":p:v_naz", ":p:v_zna"));
    map2.put("х", Arrays.asList(":p:v_rod", ":p:v_zna", ":p:v_mis")); // 5-х
    NUMR_ENDING_MAP = Collections.unmodifiableMap(map2);
    
    rightPartsWithLeftTagMap.put("бо", Pattern.compile("(verb.*:impr|.*pron|noun|adv|intj|part|predic).*"));
    rightPartsWithLeftTagMap.put("но", Pattern.compile("(verb.*:(impr|futr)|intj).*")); 
    rightPartsWithLeftTagMap.put("от", Pattern.compile("(.*pron|adv|part).*"));
    rightPartsWithLeftTagMap.put("то", Pattern.compile("(adv.*pron|adj.*pron|conj).*")); // adv|part|conj
    // noun gives false on зразу-таки
    rightPartsWithLeftTagMap.put("таки", Pattern.compile("(verb.*:(futr|past|pres)|adv|.*pron|part|predic|insert).*")); 
    
    dashPrefixes = loadSet("/uk/dash_prefixes.txt");
    leftMasterSet = loadSet("/uk/dash_left_master.txt");
    slaveSet = loadSet("/uk/dash_slaves.txt");
    // TODO: "бабуся", "лялька", "рятівник" - not quite slaves, could be masters too
  }

  private final WordTagger wordTagger;
  private final Locale conversionLocale;
  private final UkrainianTagger ukrainianTagger;
  
  private BufferedWriter compoundUnknownDebugWriter;
  private BufferedWriter compoundTaggedDebugWriter;
  private BufferedWriter compoundGenderMixDebugWriter;

  
  CompoundTagger(UkrainianTagger ukrainianTagger, WordTagger wordTagger, Locale conversionLocale) {
    this.ukrainianTagger = ukrainianTagger;
    this.wordTagger = wordTagger;
    this.conversionLocale = conversionLocale;
    
    if( Boolean.valueOf( System.getProperty(DEBUG_COMPOUNDS_PROPERTY) ) ) {
      debugCompounds();
    }
  }
  

  @Nullable
  public List<AnalyzedToken> guessCompoundTag(String word) {
    List<AnalyzedToken> guessedCompoundTags = doGuessCompoundTag(word);
    debug_compound_tagged_write(guessedCompoundTags);
    return guessedCompoundTags;
  }

  @Nullable
  private List<AnalyzedToken> doGuessCompoundTag(String word) {
    int dashIdx = word.lastIndexOf('-');
    if( dashIdx == 0 || dashIdx == word.length() - 1 )
      return null;

    int firstDashIdx = word.indexOf('-');
    if( dashIdx != firstDashIdx )
      return null;

    String leftWord = word.substring(0, dashIdx);
    String rightWord = word.substring(dashIdx + 1);


    // авіа..., авто... пишуться разом
    if( LEFT_INVALID.contains(leftWord.toLowerCase()) )
      return null;


    // wrong: пів-качана
    if( leftWord.equalsIgnoreCase("пів")
        && Character.isLowerCase(rightWord.charAt(0)) )
      return null;

    List<TaggedWord> leftWdList = tagAsIsAndWithLowerCase(leftWord);


    // стривай-бо, чекай-но, прийшов-таки, такий-от, такий-то

    if( rightPartsWithLeftTagMap.containsKey(rightWord) ) {
      if( leftWdList.isEmpty() )
        return null;

      Pattern leftTagRegex = rightPartsWithLeftTagMap.get(rightWord);
      
      List<AnalyzedToken> leftAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(leftWord, leftWdList);
      List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>(leftAnalyzedTokens.size());
      
      // ignore хто-то
      if( rightWord.equals("то")
          && LemmaHelper.hasLemma(leftAnalyzedTokens, Arrays.asList("хто", "що", "чи")) )
        return null;

      for (AnalyzedToken analyzedToken : leftAnalyzedTokens) {
        String posTag = analyzedToken.getPOSTag();
        if( posTag != null &&
            (leftWord.equals("дуже") && posTag.contains("adv")) 
             || (leftTagRegex.matcher(posTag).matches()) ) {
          newAnalyzedTokens.add(new AnalyzedToken(word, posTag, analyzedToken.getLemma()));
        }
      }
      
      return newAnalyzedTokens.isEmpty() ? null : newAnalyzedTokens;
    }


    // 101-й, 100-річному

    if( UkrainianTagger.NUMBER.matcher(leftWord).matches() ) {
      List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>();

      // e.g. 101-го
      if( NUMR_ENDING_MAP.containsKey(rightWord) ) {
        List<String> tags = NUMR_ENDING_MAP.get(rightWord);
        for (String tag: tags) {
          // TODO: shall it be numr or adj?
          newAnalyzedTokens.add(new AnalyzedToken(word, IPOSTag.adj.getText() + tag + ":&numr", leftWord + "-" + "й"));
        }
      }
      else {
        List<TaggedWord> rightWdList = wordTagger.tag(rightWord);
        if( rightWdList.isEmpty() )
          return null;

        List<AnalyzedToken> rightAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(rightWord, rightWdList);

        // e.g. 100-річному
        for (AnalyzedToken analyzedToken : rightAnalyzedTokens) {
          if( analyzedToken.getPOSTag().startsWith(IPOSTag.adj.getText()) ) {
            newAnalyzedTokens.add(new AnalyzedToken(word, analyzedToken.getPOSTag(), leftWord + "-" + analyzedToken.getLemma()));
          }
        }
      }
      return newAnalyzedTokens.isEmpty() ? null : newAnalyzedTokens;
    }


    // по-болгарськи, по-болгарському

    if( leftWord.equalsIgnoreCase("по") && rightWord.endsWith("ськи") ) {
      rightWord += "й";
    }

    List<TaggedWord> rightWdList = wordTagger.tag(rightWord);
    if( rightWdList.isEmpty() )
      return null;

    List<AnalyzedToken> rightAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(rightWord, rightWdList);

    if( leftWord.equalsIgnoreCase("по") ) {
      if( rightWord.endsWith("ому") ) {
        return poAdvMatch(word, rightAnalyzedTokens, ADJ_TAG_FOR_PO_ADV_MIS);
      }
      else if( rightWord.endsWith("ський") ) {
        return poAdvMatch(word, rightAnalyzedTokens, ADJ_TAG_FOR_PO_ADV_NAZ);
      }
      return null;
    }


    // exclude: Малишко-це, відносини-коли

    List<AnalyzedToken> leftAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(leftWord, leftWdList);

    if( ! leftWord.equalsIgnoreCase(rightWord) && PosTagHelper.hasPosTag(rightAnalyzedTokens, "(part|conj).*|.*:&pron.*") 
        && ! (PosTagHelper.hasPosTag(leftAnalyzedTokens, "numr.*") && PosTagHelper.hasPosTag(rightAnalyzedTokens, "numr.*")) )
      return null;


    // пів-України

    if( Character.isUpperCase(rightWord.charAt(0)) ) {
      if (word.startsWith("пів-")) {
        List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>(rightAnalyzedTokens.size());
        
        for (AnalyzedToken rightAnalyzedToken : rightAnalyzedTokens) {
          String rightPosTag = rightAnalyzedToken.getPOSTag();

          if( rightPosTag == null )
            continue;

          if( NOUN_SING_V_ROD_REGEX.matcher(rightPosTag).matches() ) {
            for(String vid: PosTagHelper.VIDMINKY_MAP.keySet()) {
              if( vid.equals("v_kly") )
                continue;
              String posTag = rightPosTag.replace("v_rod", vid);
              newAnalyzedTokens.add(new AnalyzedToken(word, posTag, word));
            }
          }
        }

        return newAnalyzedTokens;
      }
      else {
        // we don't want Нью-Париж
        return null;
      }
    }


    // майстер-класу
    
    if( dashPrefixes.contains( leftWord ) || dashPrefixes.contains( leftWord.toLowerCase() ) || DASH_PREFIX_LAT_PATTERN.matcher(leftWord).matches() ) {
      return getNvPrefixNounMatch(word, rightAnalyzedTokens, leftWord);
    }

    // Пенсильванія-авеню

    if( Character.isUpperCase(leftWord.charAt(0)) && cityAvenue.contains(rightWord) ) {
      if( leftWdList.isEmpty() )
        return null;
      
      return cityAvenueMatch(word, leftAnalyzedTokens);
    }



    // don't allow: Донець-кий, зовнішньо-економічний, мас-штаби

    // allow га-га!

    if( ! PosTagHelper.hasPosTag(leftAnalyzedTokens, "intj.*") ) {
      String noDashWord = word.replace("-", "");
      List<TaggedWord> noDashWordList = tagAsIsAndWithLowerCase(noDashWord);
      List<AnalyzedToken> noDashAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(noDashWord, noDashWordList);

      if( ! noDashAnalyzedTokens.isEmpty() )
        return null;
    }


    // вгору-вниз, лікар-гомеопат, жило-було

    if( ! leftWdList.isEmpty() ) {

      List<AnalyzedToken> tagMatch = tagMatch(word, leftAnalyzedTokens, rightAnalyzedTokens);
      if( tagMatch != null ) {
        return tagMatch;
      }
    }


    // австро..., англо... пишуться разом

    if( LEFT_O_ADJ_INVALID.contains(leftWord.toLowerCase()) )
      return null;


    // яскраво-барвистий...

    if( O_ADJ_PATTERN.matcher(leftWord).matches() ) {
      return oAdjMatch(word, rightAnalyzedTokens, leftWord);
    }

    debug_compound_unknown_write(word);
    
    return null;
  }
  

  @Nullable
  private List<AnalyzedToken> cityAvenueMatch(String word, List<AnalyzedToken> leftAnalyzedTokens) {
    List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>(leftAnalyzedTokens.size());
    
    for (AnalyzedToken analyzedToken : leftAnalyzedTokens) {
      String posTag = analyzedToken.getPOSTag();
      if( NOUN_V_NAZ_REGEX.matcher(posTag).matches() ) {
        newAnalyzedTokens.add(new AnalyzedToken(word, posTag.replaceFirst("v_naz", "nv"), word));
      }
    }
    
    return newAnalyzedTokens.isEmpty() ? null : newAnalyzedTokens;
  }

  @Nullable
  private List<AnalyzedToken> tagMatch(String word, List<AnalyzedToken> leftAnalyzedTokens, List<AnalyzedToken> rightAnalyzedTokens) {
    List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>();
    List<AnalyzedToken> newAnalyzedTokensAnimInanim = new ArrayList<>();
    
    String animInanimNotTagged = null;
    
    for (AnalyzedToken leftAnalyzedToken : leftAnalyzedTokens) {
      String leftPosTag = leftAnalyzedToken.getPOSTag();
      
      if( leftPosTag == null || IPOSTag.contains(leftPosTag, IPOSTag.abbr.getText()) )
        continue;

      // we don't want to mess with v_kly, e.g. no v_kly у рибо-полювання
      if( leftPosTag.startsWith("noun") && leftPosTag.contains("v_kly") )
        continue;

      String leftPosTagExtra = "";
      boolean leftNv = false;

      if( leftPosTag.contains(NV_TAG) ) {
        leftNv = true;
        leftPosTag = leftPosTag.replace(NV_TAG, "");
      }

      Matcher matcher = EXTRA_TAGS_DROP.matcher(leftPosTag);
      if( matcher.find() ) {
        leftPosTag = matcher.replaceAll("");
      }

      matcher = EXTRA_TAGS.matcher(leftPosTag);
      if( matcher.find() ) {
        leftPosTagExtra += matcher.group();
        leftPosTag = matcher.replaceAll("");
      }

      for (AnalyzedToken rightAnalyzedToken : rightAnalyzedTokens) {
        String rightPosTag = rightAnalyzedToken.getPOSTag();
        
        if( rightPosTag == null || IPOSTag.contains(rightPosTag, IPOSTag.abbr.getText()) )
          continue;

        String extraNvTag = "";
        boolean rightNv = false;
        if( rightPosTag.contains(NV_TAG) ) {
          rightNv = true;
          
          if( leftNv ) {
            extraNvTag += NV_TAG;
          }
        }

        Matcher matcherR = EXTRA_TAGS_DROP.matcher(rightPosTag);
        if( matcherR.find() ) {
          rightPosTag = matcherR.replaceAll("");
        }

        matcherR = EXTRA_TAGS.matcher(rightPosTag);
        if( matcherR.find() ) {
          rightPosTag = matcherR.replaceAll("");
        }
        
        if (leftPosTag.equals(rightPosTag) 
            && (IPOSTag.startsWith(leftPosTag, IPOSTag.numr, IPOSTag.adv, IPOSTag.adj, IPOSTag.verb)
            || (IPOSTag.startsWith(leftPosTag, IPOSTag.intj) && leftAnalyzedToken.getLemma().equalsIgnoreCase(rightAnalyzedToken.getLemma())) ) ) {
          newAnalyzedTokens.add(new AnalyzedToken(word, leftPosTag + extraNvTag + leftPosTagExtra, leftAnalyzedToken.getLemma() + "-" + rightAnalyzedToken.getLemma()));
        }
        // noun-noun
        else if ( leftPosTag.startsWith(IPOSTag.noun.getText()) && rightPosTag.startsWith(IPOSTag.noun.getText()) ) {

        	// discard чорний-чорний as noun:anim
        	if( leftAnalyzedToken.getToken().equalsIgnoreCase(rightAnalyzedToken.getToken())
        			&& leftPosTag.contains(TAG_ANIM) && rightPosTag.contains(TAG_ANIM) )
        		continue;
        	
          String agreedPosTag = getAgreedPosTag(leftPosTag, rightPosTag, leftNv, word);

          if( agreedPosTag == null 
              && rightPosTag.startsWith("noun:inanim:m:v_naz")
              && isMinMax(rightAnalyzedToken.getToken()) ) {
            agreedPosTag = leftPosTag;
          }

          if( agreedPosTag == null && ! isSameAnimStatus(leftPosTag, rightPosTag) ) {

            agreedPosTag = tryAnimInanim(leftPosTag, rightPosTag, leftAnalyzedToken.getLemma(), rightAnalyzedToken.getLemma(), leftNv, rightNv, word);
            
            if( agreedPosTag == null ) {
              animInanimNotTagged = leftPosTag.contains(":anim") ? "anim-inanim" : "inanim-anim";
            }
            else {
              newAnalyzedTokensAnimInanim.add(new AnalyzedToken(word, agreedPosTag + extraNvTag + leftPosTagExtra, leftAnalyzedToken.getLemma() + "-" + rightAnalyzedToken.getLemma()));
              continue;
            }
          }
          
          if( agreedPosTag != null ) {
            newAnalyzedTokens.add(new AnalyzedToken(word, agreedPosTag + extraNvTag + leftPosTagExtra, leftAnalyzedToken.getLemma() + "-" + rightAnalyzedToken.getLemma()));
          }
        }
        // numr-numr: один-два
        else if ( leftPosTag.startsWith(IPOSTag.numr.getText()) && rightPosTag.startsWith(IPOSTag.numr.getText()) ) {
            String agreedPosTag = getNumAgreedPosTag(leftPosTag, rightPosTag, leftNv);
            if( agreedPosTag != null ) {
              newAnalyzedTokens.add(new AnalyzedToken(word, agreedPosTag + extraNvTag + leftPosTagExtra, leftAnalyzedToken.getLemma() + "-" + rightAnalyzedToken.getLemma()));
            }
        }
        // noun-numr match
        else if ( IPOSTag.startsWith(leftPosTag, IPOSTag.noun) && IPOSTag.startsWith(rightPosTag, IPOSTag.numr) ) {
          // gender tags match
          String leftGenderConj = PosTagHelper.getGenderConj(leftPosTag);
          if( leftGenderConj != null && leftGenderConj.equals(PosTagHelper.getGenderConj(rightPosTag)) ) {
            newAnalyzedTokens.add(new AnalyzedToken(word, leftPosTag + extraNvTag + leftPosTagExtra, leftAnalyzedToken.getLemma() + "-" + rightAnalyzedToken.getLemma()));
            // година-півтори може бути як одниною так і множиною: минула година-півтори, минулі година-півтори
            if( ! leftPosTag.contains(":p:") ) {
              newAnalyzedTokens.add(new AnalyzedToken(word, leftPosTag.replaceAll(":[mfn]:", ":p:") + extraNvTag + leftPosTagExtra, leftAnalyzedToken.getLemma() + "-" + rightAnalyzedToken.getLemma()));
            }
          }
          else {
            // (with different gender tags): сотні (:p:) - дві (:f:)
            String agreedPosTag = getNumAgreedPosTag(leftPosTag, rightPosTag, leftNv);
            if( agreedPosTag != null ) {
              newAnalyzedTokens.add(new AnalyzedToken(word, agreedPosTag + extraNvTag + leftPosTagExtra, leftAnalyzedToken.getLemma() + "-" + rightAnalyzedToken.getLemma()));
              // рік-два може бути як одниною так і множиною: минулий рік-два, минулі рік-два
              if( ! agreedPosTag.contains(":p:") ) {
                newAnalyzedTokens.add(new AnalyzedToken(word, agreedPosTag.replaceAll(":[mfn]:", ":p:") + extraNvTag + leftPosTagExtra, leftAnalyzedToken.getLemma() + "-" + rightAnalyzedToken.getLemma()));
              }
            }
          }
        }
        // noun-adj match: Буш-молодший, рік-два
        // не робимо братів-православних — загальний noun-adj дає забагато фальшивих спрацьовувань
        else if( leftPosTag.startsWith(IPOSTag.noun.getText()) 
            && IPOSTag.startsWith(rightPosTag, IPOSTag.numr) 
                || (IPOSTag.startsWith(rightPosTag, IPOSTag.adj) && isJuniorSenior(leftAnalyzedToken, rightAnalyzedToken)) ) {
          
//          if( ! leftPosTag.contains(":prop")
//              || isJuniorSenior(leftAnalyzedToken, rightAnalyzedToken) ) { 
          	
          	// discard чорний-чорний as noun:anim
//          	if( leftAnalyzedToken.getToken().equalsIgnoreCase(rightAnalyzedToken.getToken()) )
//          		continue;

            String leftGenderConj = PosTagHelper.getGenderConj(leftPosTag);
            if( leftGenderConj != null && leftGenderConj.equals(PosTagHelper.getGenderConj(rightPosTag)) ) {
              newAnalyzedTokens.add(new AnalyzedToken(word, leftPosTag + extraNvTag + leftPosTagExtra, leftAnalyzedToken.getLemma() + "-" + rightAnalyzedToken.getLemma()));
            }
  //        }
        }
      }
    }

    // remove duplicates
    newAnalyzedTokens = new ArrayList<>(new LinkedHashSet<>(newAnalyzedTokens));
    
    if( newAnalyzedTokens.isEmpty() ) {
      newAnalyzedTokens = newAnalyzedTokensAnimInanim;
    }

    if( animInanimNotTagged != null && newAnalyzedTokens.isEmpty() ) {
      debug_compound_unknown_write(word + " " + animInanimNotTagged);
    }
    
    return newAnalyzedTokens.isEmpty() ? null : newAnalyzedTokens;
  }


  private boolean isJuniorSenior(AnalyzedToken leftAnalyzedToken, AnalyzedToken rightAnalyzedToken) {
    return leftAnalyzedToken.getPOSTag().matches(".*:([lf]name|patr).*") && rightAnalyzedToken.getLemma().matches(".*(молодший|старший)");
  }

  // right part is numr
  @Nullable
  private String getNumAgreedPosTag(String leftPosTag, String rightPosTag, boolean leftNv) {
    String agreedPosTag = null;
    
    if( leftPosTag.contains(":p:") && SING_REGEX_F.matcher(rightPosTag).find()
        || SING_REGEX_F.matcher(leftPosTag).find() && rightPosTag.contains(":p:")) {
      String leftConj = PosTagHelper.getConj(leftPosTag);
      if( leftConj != null && leftConj.equals(PosTagHelper.getConj(rightPosTag)) ) {
        agreedPosTag = leftPosTag;
      }
    }
    return agreedPosTag;
  }

  @Nullable
  private String getAgreedPosTag(String leftPosTag, String rightPosTag, boolean leftNv, String word) {
    boolean leftPlural = isPlural(leftPosTag);
    boolean rightPlural = isPlural(rightPosTag);
      if (leftPlural != rightPlural)
        return null;
    
    if( ! isSameAnimStatus(leftPosTag, rightPosTag) )
      return null;
    
    Matcher stdNounMatcherLeft = stdNounTagRegex.matcher(leftPosTag);
    if( stdNounMatcherLeft.matches() ) {
      Matcher stdNounMatcherRight = stdNounTagRegex.matcher(rightPosTag);
      if (stdNounMatcherRight.matches()) {
        String substring1 = stdNounMatcherLeft.group(2); //leftPosTag.substring(stdNounTagLen, stdNounTagLen + 3);
        String substring2 = stdNounMatcherRight.group(2); //rightPosTag.substring(stdNounTagLen, stdNounTagLen + 3);
        if( substring1.equals(substring2) ) {
          if( ! stdNounMatcherLeft.group(1).equals(stdNounMatcherRight.group(1)) ) {
            if( compoundGenderMixDebugWriter != null ) {
              try {
                compoundGenderMixDebugWriter.append(word + " " + (leftNv ? rightPosTag : leftPosTag));
                compoundGenderMixDebugWriter.newLine();
                compoundGenderMixDebugWriter.flush();
              } catch (IOException e) {
                System.err.println("Failed to write into gender mix file");
              }
            }
          }
          
          if( leftNv )
            return rightPosTag;

          return leftPosTag;
        }
      }
    }

    return null;
  }

  private static boolean isMinMax(String rightToken) {
    return rightToken.equals("максимум")
        || rightToken.equals("мінімум");
  }

  @Nullable
  private String tryAnimInanim(String leftPosTag, String rightPosTag, String leftLemma, String rightLemma, boolean leftNv, boolean rightNv, String word) {
    String agreedPosTag = null;
    
    // підприємство-банкрут
    if( leftMasterSet.contains(leftLemma) ) {
      if( leftPosTag.contains(TAG_ANIM) ) {
        rightPosTag = rightPosTag.replace(TAG_INANIM, TAG_ANIM);
      }
      else {
        rightPosTag = rightPosTag.replace(TAG_ANIM, TAG_INANIM);
      }
      
      agreedPosTag = getAgreedPosTag(leftPosTag, rightPosTag, leftNv, word);
      
      if( agreedPosTag == null ) {
        if (! leftPosTag.contains(TAG_ANIM)) {
          if (MNP_ZNA_REGEX.matcher(leftPosTag).matches() && MNP_NAZ_REGEX.matcher(rightPosTag).matches()
              && ! leftNv && ! rightNv ) {
            agreedPosTag = leftPosTag;
          }
        }
        else {
          if (MNP_ZNA_REGEX.matcher(leftPosTag).matches() && MNP_ROD_REGEX.matcher(rightPosTag).matches()
              && ! leftNv && ! rightNv ) {
            agreedPosTag = leftPosTag;
          }
        }
      }
      
    }
    // сонях-красень
    else if ( slaveSet.contains(rightLemma) ) {
      rightPosTag = rightPosTag.replace(":anim", ":inanim");
      agreedPosTag = getAgreedPosTag(leftPosTag, rightPosTag, false, word);
      if( agreedPosTag == null ) {
        if (leftPosTag.contains(TAG_INANIM)) {
          if (MNP_ZNA_REGEX.matcher(leftPosTag).matches() && MNP_NAZ_REGEX.matcher(rightPosTag).matches()
              && PosTagHelper.getNum(leftPosTag).equals(PosTagHelper.getNum(rightPosTag))
              && ! leftNv && ! rightNv ) {
            agreedPosTag = leftPosTag;
          }
        }
      }
    }
    // красень-сонях
    else if ( slaveSet.contains(leftLemma) ) {
      leftPosTag = leftPosTag.replace(":anim", ":inanim");
      agreedPosTag = getAgreedPosTag(rightPosTag, leftPosTag, false, word);
      if( agreedPosTag == null ) {
        if ( rightPosTag.contains(TAG_INANIM) ) {
          if (MNP_ZNA_REGEX.matcher(rightPosTag).matches() && MNP_NAZ_REGEX.matcher(leftPosTag).matches()
              && PosTagHelper.getNum(leftPosTag).equals(PosTagHelper.getNum(rightPosTag))
              && ! leftNv && ! rightNv ) {
            agreedPosTag = rightPosTag;
          }
        }
      }
    }
    // else
    // рослин-людожерів, слалому-гіганту, місяця-князя, депутатів-привидів
    
    return agreedPosTag;
  }

  private static boolean isSameAnimStatus(String leftPosTag, String rightPosTag) {
    boolean leftAnim = leftPosTag.contains(TAG_ANIM);
    boolean rightAnim = rightPosTag.contains(TAG_ANIM);
    return leftAnim == rightAnim;
  }

  private static boolean isPlural(String posTag) {
    return posTag.startsWith("noun:") && posTag.contains(":p:");
  }

  @Nullable
  private List<AnalyzedToken> oAdjMatch(String word, List<AnalyzedToken> analyzedTokens, String leftWord) {
    List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>(analyzedTokens.size());

    String leftBase = leftWord.substring(0, leftWord.length()-1);
    
    String extraTag = "";
    if( ! LEFT_O_ADJ.contains(leftWord.toLowerCase(conversionLocale)) ) {

      List<TaggedWord> taggedWords = tagBothCases(leftWord);            // яскраво для яскраво-барвистий
      if( taggedWords.isEmpty() ) {
        taggedWords = tagBothCases(oToYj(leftWord));  // кричущий для кричуще-яскравий
      }
      if( taggedWords.isEmpty() ) {
        taggedWords = tagBothCases(leftBase);         // паталог для паталого-анатомічний
      }
      if( taggedWords.isEmpty() ) {
        taggedWords = tagBothCases(leftBase + "а");   // два для дво-триметровий
      }
      if( taggedWords.isEmpty() )
        return null;

      for(TaggedWord taggedWord: taggedWords) {
        if( taggedWord.getPosTag().contains(":bad") ) {
          extraTag = ":bad";
          break;
        }
      }
    }
    
    for (AnalyzedToken analyzedToken : analyzedTokens) {
      String posTag = analyzedToken.getPOSTag();
      if( posTag.startsWith( IPOSTag.adj.getText() ) ) {
        newAnalyzedTokens.add(new AnalyzedToken(word, posTag + extraTag, leftWord.toLowerCase() + "-" + analyzedToken.getLemma()));
      }
    }
    
    return newAnalyzedTokens.isEmpty() ? null : newAnalyzedTokens;
  }

  private static String oToYj(String leftWord) {
    return leftWord.endsWith("ьо") 
        ? leftWord.substring(0, leftWord.length()-2) + "ій" 
        : leftWord.substring(0,  leftWord.length()-1) + "ий";
  }

  @Nullable
  private List<AnalyzedToken> getNvPrefixNounMatch(String word, List<AnalyzedToken> analyzedTokens, String leftWord) {
    List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>(analyzedTokens.size());
    
    for (AnalyzedToken analyzedToken : analyzedTokens) {
      String posTag = analyzedToken.getPOSTag();
      if( posTag.startsWith( IPOSTag.noun.getText() )
            && ! posTag.contains("v_kly") ) {
        newAnalyzedTokens.add(new AnalyzedToken(word, posTag, leftWord + "-" + analyzedToken.getLemma()));
      }
    }
    
    return newAnalyzedTokens.isEmpty() ? null : newAnalyzedTokens;
  }

  @Nullable
  private List<AnalyzedToken> poAdvMatch(String word, List<AnalyzedToken> analyzedTokens, String adjTag) {
    
    for (AnalyzedToken analyzedToken : analyzedTokens) {
      String posTag = analyzedToken.getPOSTag();
      if( posTag.startsWith( adjTag ) ) {
        return Arrays.asList(new AnalyzedToken(word, IPOSTag.adv.getText(), word));
      }
    }
    
    return null;
  }


  private String capitalize(String word) {
    return word.substring(0, 1).toUpperCase(conversionLocale) + word.substring(1, word.length());
  }

  private List<TaggedWord> tagBothCases(String leftWord) {
    List<TaggedWord> leftWdList = wordTagger.tag(leftWord);
    
    String leftLowerCase = leftWord.toLowerCase(conversionLocale);
    if( ! leftWord.equals(leftLowerCase)) {
      leftWdList.addAll(wordTagger.tag(leftLowerCase));
    }
    else {
      String leftUpperCase = capitalize(leftWord);
      if( ! leftWord.equals(leftUpperCase)) {
        leftWdList.addAll(wordTagger.tag(leftUpperCase));
      }
    }

    return leftWdList;
  }

  private List<TaggedWord> tagAsIsAndWithLowerCase(String leftWord) {
    List<TaggedWord> leftWdList = wordTagger.tag(leftWord);
    
    String leftLowerCase = leftWord.toLowerCase(conversionLocale);
    if( ! leftWord.equals(leftLowerCase)) {
      leftWdList.addAll(wordTagger.tag(leftLowerCase));
    }

    return leftWdList;
  }

  private static Set<String> loadSet(String path) {
    Set<String> result = new HashSet<>();
    try (InputStream is = JLanguageTool.getDataBroker().getFromResourceDirAsStream(path);
         Scanner scanner = new Scanner(is, "UTF-8")) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        result.add(line);
      }
      return result;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  
  // methods for debugging compounds

  private void debugCompounds() {
    try {
      Path unknownFile = Paths.get("compounds-unknown.txt");
      Files.deleteIfExists(unknownFile);
      unknownFile = Files.createFile(unknownFile);
      compoundUnknownDebugWriter = Files.newBufferedWriter(unknownFile, Charset.defaultCharset());

      Path taggedFile = Paths.get("compounds-tagged.txt");
      Files.deleteIfExists(taggedFile);
      taggedFile = Files.createFile(taggedFile);
      compoundTaggedDebugWriter = Files.newBufferedWriter(taggedFile, Charset.defaultCharset());

      Path genderMixFile = Paths.get("gender-mix.txt");
      Files.deleteIfExists(genderMixFile);
      genderMixFile = Files.createFile(genderMixFile);
      compoundGenderMixDebugWriter = Files.newBufferedWriter(genderMixFile, Charset.defaultCharset());

//      Path tagged2File = Paths.get("tagged.txt");
//      Files.deleteIfExists(tagged2File);
//      taggedFile = Files.createFile(tagged2File);
//      taggedDebugWriter = Files.newBufferedWriter(tagged2File, Charset.defaultCharset());
    } catch (IOException ex) {
//      throw new RuntimeException(ex);
      System.err.println("Failed to open debug compounds file");
    }
  }

  private void debug_compound_tagged_write(List<AnalyzedToken> guessedCompoundTags) {
    if( compoundTaggedDebugWriter == null || guessedCompoundTags == null )
      return;

    debug_tagged_write(guessedCompoundTags, compoundTaggedDebugWriter);
  }

  private void debug_compound_unknown_write(String word) {
    if( compoundUnknownDebugWriter == null )
      return;
    
    try {
      compoundUnknownDebugWriter.append(word);
      compoundUnknownDebugWriter.newLine();
      compoundUnknownDebugWriter.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  private void debug_tagged_write(List<AnalyzedToken> analyzedTokens, BufferedWriter writer) {
    if( analyzedTokens.get(0).getLemma() == null || analyzedTokens.get(0).getToken().trim().isEmpty() )
      return;

    try {
      String prevToken = "";
      String prevLemma = "";
      for (AnalyzedToken analyzedToken : analyzedTokens) {
        String token = analyzedToken.getToken();
        
        boolean firstTag = false;
        if (! prevToken.equals(token)) {
          if( prevToken.length() > 0 ) {
            writer.append(";  ");
            prevLemma = "";
          }
          writer.append(token).append(" ");
          prevToken = token;
          firstTag = true;
        }
        
        String lemma = analyzedToken.getLemma();

        if (! prevLemma.equals(lemma)) {
          if( prevLemma.length() > 0 ) {
            writer.append(", ");
          }
          writer.append(lemma); //.append(" ");
          prevLemma = lemma;
          firstTag = true;
        }

        writer.append(firstTag ? " " : "|").append(analyzedToken.getPOSTag());
        firstTag = false;
      }
      writer.newLine();
      writer.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}