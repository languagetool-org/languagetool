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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedToken;
import org.languagetool.rules.uk.ExtraDictionaryLoader;
import org.languagetool.rules.uk.LemmaHelper;
import org.languagetool.tagging.TaggedWord;
import org.languagetool.tagging.WordTagger;
import org.languagetool.tools.StringTools;

/**
 * Allows to tag compound words with hyphen dynamically by analyzing each part
 * 
 * @since 3.0
 */
class CompoundTagger {
  private static final String TAG_ANIM = ":anim";
  private static final String TAG_INANIM = ":inanim";
  private static final Pattern EXTRA_TAGS = Pattern.compile(":bad");
  private static final Pattern EXTRA_TAGS_DROP = Pattern.compile(":(comp.|np|ns|slang|rare|xp[1-9]|&predic|&insert)");
  private static final Pattern NOUN_SING_V_ROD_REGEX = Pattern.compile("noun.*?:[mfn]:v_rod.*");
//  private static final Pattern NOUN_V_NAZ_REGEX = Pattern.compile("noun.*?:.:v_naz.*");
  private static final Pattern SING_REGEX_F = Pattern.compile(":[mfn]:");
  private static final Pattern O_ADJ_PATTERN = Pattern.compile(".+?(о|[чшщ]е)");
  private static final Pattern NUMR_ADJ_PATTERN = Pattern.compile(".+?(одно|дво|ох|и)");
  private static final Pattern DASH_PREFIX_LAT_PATTERN = Pattern.compile("[a-zA-Z]{3,}|[α-ωΑ-Ω]");
  private static final Pattern YEAR_NUMBER = Pattern.compile("[12][0-9]{3}");
  private static final Pattern NOUN_PREFIX_NUMBER = Pattern.compile("[0-9]+");
  private static final Pattern NOUN_SUFFIX_NUMBER_LETTER = Pattern.compile("[0-9][0-9А-ЯІЇЄҐ-]*");
  private static final Pattern ADJ_PREFIX_NUMBER = Pattern.compile("[0-9]+(,[0-9]+)?([-–—][0-9]+(,[0-9]+)?)?%?|(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})");
  private static final Pattern REQ_NUM_DVA_PATTERN = Pattern.compile("(місн|томник|поверхів).{0,4}");
  private static final Pattern REQ_NUM_DESYAT_PATTERN = Pattern.compile("(класни[кц]|раундов|томн|томов|хвилин|десятиріч|кілометрів|річ).{0,4}");
  private static final Pattern REQ_NUM_STO_PATTERN = Pattern.compile("(річч|літт|метрів|грамов|тисячник).{0,3}");
  private static final Pattern INTJ_PATTERN = Pattern.compile("intj.*");

  private static final Pattern MNP_NAZ_REGEX = Pattern.compile(".*?:[mnp]:v_naz.*");
  private static final Pattern MNP_ZNA_REGEX = Pattern.compile(".*?:[mnp]:v_zna.*");
  private static final Pattern MNP_ROD_REGEX = Pattern.compile(".*?:[mnp]:v_rod.*");

  private static final Pattern stdNounTagRegex = Pattern.compile("noun:(?:in)?anim:(.):(v_...).*");
  private static final Set<String> dashPrefixes;
  private static final Set<String> dashPrefixes2;
  private static final Set<String> leftMasterSet;
  private static final Map<String, List<String>> numberedEntities;
  private static final Map<String, Pattern> rightPartsWithLeftTagMap = new HashMap<>();
  private static final Set<String> slaveSet;
  private static final String ADJ_TAG_FOR_PO_ADV_MIS = "adj:m:v_mis";
  private static final String ADJ_TAG_FOR_PO_ADV_NAZ = "adj:m:v_naz";

  private static final List<String> LEFT_O_ADJ = Arrays.asList(
    "австро", "адиго", "американо", "англо", "афро", "еко", "етно", "іспано", "італо", "києво", "марокано", "угро"
  );

  private static final List<String> LEFT_INVALID = Arrays.asList(
    "авіа", "авто", "агро", "анти", "аудіо", "біо", "вело", "відео", "водо", "газо", "геліо", "гео", "гідро", "давньо", "древньо", "екзо",
    "екстра", "електро", "зоо", "ізо", "квазі", "кіно", "космо", "контр", "лже", "максимально", "мінімально", "макро", "мета",
    "метео", "мікро", "мілі", "моно", "мото", "мульти", "напів", "нео", "палео", "пост", "псевдо", "радіо",
    "рентгено", "соціо", "стерео", "супер", "теле", "термо", "турбо", "ультра", "фоно", "фото"
  );

  private static final List<String> LEFT_O_ADJ_INVALID = Arrays.asList(
    "багато", "мало", "високо", "низько", "старо", "ново"
  );

  private static final List<String> WORDS_WITH_YEAR = Arrays.asList("гра", "бюджет", "вибори", "олімпіада", "універсіада");
  private static final List<String> WORDS_WITH_NUM = Arrays.asList("Формула", "Карпати", "Динамо", "Шахтар", "Фукусіма", "омега");

  // http://www.pravopys.net/sections/33/
  static {
    rightPartsWithLeftTagMap.put("бо", Pattern.compile("(verb|.*?pron|noun|adv|intj|part).*"));
    rightPartsWithLeftTagMap.put("но", Pattern.compile("(verb.*?:(impr|futr|&insert)|intj).*")); 
    rightPartsWithLeftTagMap.put("от", Pattern.compile("(.*?pron|adv|part|verb).*"));
    rightPartsWithLeftTagMap.put("то", Pattern.compile("(.*?pron|verb|noun|adj|conj).*")); // adv|part|conj
    // noun gives false on зразу-таки
    rightPartsWithLeftTagMap.put("таки", Pattern.compile("(verb|adv|adj|.*?pron|part|noninfl:&predic).*")); 

    dashPrefixes = ExtraDictionaryLoader.loadSet("/uk/dash_prefixes.txt");
    dashPrefixes2 = ExtraDictionaryLoader.loadSet("/uk/dash_prefixes2.txt");
    leftMasterSet = ExtraDictionaryLoader.loadSet("/uk/dash_left_master.txt");
    // TODO: "бабуся", "лялька", "рятівник" - not quite slaves, could be masters too
    slaveSet = ExtraDictionaryLoader.loadSet("/uk/dash_slaves.txt");
    numberedEntities = ExtraDictionaryLoader.loadSpacedLists("/uk/entities.txt");
  }

  private final WordTagger wordTagger;
  private final Locale conversionLocale;
  private final UkrainianTagger ukrainianTagger;
  private final CompoundDebugLogger compoundDebugLogger = new CompoundDebugLogger();


  CompoundTagger(UkrainianTagger ukrainianTagger, WordTagger wordTagger, Locale conversionLocale) {
    this.ukrainianTagger = ukrainianTagger;
    this.wordTagger = wordTagger;
    this.conversionLocale = conversionLocale;
  }


  @Nullable
  public List<AnalyzedToken> guessCompoundTag(String word) {
    List<AnalyzedToken> guessedCompoundTags = doGuessCompoundTag(word);
    compoundDebugLogger.logTaggedCompound(guessedCompoundTags);
    return guessedCompoundTags;
  }

  @Nullable
  private List<AnalyzedToken> doGuessCompoundTag(String word) {
    int dashIdx = word.lastIndexOf('-');
    if( dashIdx == word.length() - 1 )
      return null;

    int firstDashIdx = word.indexOf('-');
    if( firstDashIdx == 0 )
      return null;
    
    boolean startsWithDigit = Character.isDigit(word.charAt(0));

    if( ! startsWithDigit && dashIdx != firstDashIdx ) {
      int dashCount = StringUtils.countMatches(word, "-");

      if( dashCount >= 2
          && dashIdx > firstDashIdx + 1 ) {
        List<AnalyzedToken> tokens = doGuessMultiHyphens(word, firstDashIdx, dashIdx);
        if( tokens != null )
          return tokens;
      }
      
      if( dashCount == 2
          && dashIdx > firstDashIdx + 1 ) {
        return doGuessTwoHyphens(word, firstDashIdx, dashIdx);
      }
      
      return null;
    }

    String leftWord = word.substring(0, dashIdx);
    String rightWord = word.substring(dashIdx + 1);

    
    boolean dashPrefixMatch = dashPrefixes.contains( leftWord ) 
        || dashPrefixes.contains( leftWord.toLowerCase() ) 
        || DASH_PREFIX_LAT_PATTERN.matcher(leftWord).matches();

    if( ! dashPrefixMatch 
        && (startsWithDigit || word.matches("[XLIV]+-.*")) ) {
      return matchDigitCompound(word, leftWord, rightWord);
    }

    if( Character.isDigit(rightWord.charAt(0)) ) {
      return matchNumberedProperNoun(word, leftWord, rightWord);
    }


    // авіа..., авто... пишуться разом
    //TODO: але може бути: авто-пенсіонер
    if( LEFT_INVALID.contains(leftWord.toLowerCase()) ) {
      List<TaggedWord> rightWdList = tagEitherCase(rightWord);
      
      rightWdList = PosTagHelper.filter2(rightWdList, Pattern.compile("(noun|adj)(?!.*pron).*"));
      
      if( rightWdList.isEmpty() )
        return null;

      String lemma = leftWord + "-" + rightWdList.get(0).getLemma();
      String extraTag = StringTools.isCapitalizedWord(rightWord) ? "" : ":bad";
      rightWdList = PosTagHelper.addIfNotContains(rightWdList, extraTag, lemma);
      return ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(word, rightWdList);
    }


    // wrong: пів-качана
    if( leftWord.equalsIgnoreCase("пів")
        && Character.isLowerCase(rightWord.charAt(0)) )
      return null;

    List<TaggedWord> leftWdList = tagAsIsAndWithLowerCase(leftWord);


    // стривай-бо, чекай-но, прийшов-таки, такий-от, такий-то

    if( rightPartsWithLeftTagMap.containsKey(rightWord) 
        && ! PosTagHelper.hasPosTagPart2(leftWdList, "abbr") ) {

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


    // по-болгарськи, по-болгарському

    if( leftWord.equalsIgnoreCase("по") && rightWord.endsWith("ськи") ) {
      rightWord += "й";
    }
    
    // Пенсильванія-авеню

    if( Character.isUpperCase(leftWord.charAt(0)) && LemmaHelper.CITY_AVENU.contains(rightWord) ) {
      return PosTagHelper.generateTokensForNv(word, "f", ":prop");
    }

    List<TaggedWord> rightWdList = tagEitherCase(rightWord);
      
    if( rightWdList.isEmpty() ) {
     
      if( word.startsWith("напів") ) {
        // напівпольської-напіванглійської
        Matcher napivMatcher = Pattern.compile("напів(.+?)-напів(.+)").matcher(word);
        if( napivMatcher.matches() ) {
          List<TaggedWord> napivLeftWdList = tagAsIsAndWithLowerCase(napivMatcher.group(1));
          List<TaggedWord> napivRightWdList = tagAsIsAndWithLowerCase(napivMatcher.group(2));

          List<AnalyzedToken> napivLeftAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(napivMatcher.group(1), napivLeftWdList);
          List<AnalyzedToken> napivRightAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(napivMatcher.group(2), napivRightWdList);

          List<AnalyzedToken> tagMatch = tagMatch(word, napivLeftAnalyzedTokens, napivRightAnalyzedTokens);
          if( tagMatch != null ) {
            return tagMatch;
          }
        }
      }
      
      return null;
    }

    List<AnalyzedToken> rightAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(rightWord, rightWdList);

    // Ш-подібний
    if( leftWord.length() == 1
        && Character.isUpperCase(leftWord.charAt(0))
        && LemmaHelper.hasLemma(rightAnalyzedTokens, Arrays.asList("подібний")) ) {

      return generateTokensWithRighInflected(word, leftWord, rightAnalyzedTokens, IPOSTag.adj.getText());
    }

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
    
    if( PosTagHelper.hasPosTagPart(leftAnalyzedTokens, "&pron")
        && ! PosTagHelper.hasPosTagPart(leftAnalyzedTokens, "numr") )
      return null;

    if( ! leftWord.equalsIgnoreCase(rightWord) && PosTagHelper.hasPosTag(rightAnalyzedTokens, "(part|conj).*|.*?:&pron.*") 
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
              String posTag = rightPosTag.replace("v_rod", vid) + ":ua_1992";
              newAnalyzedTokens.add(new AnalyzedToken(word, posTag, word));
            }
          }
        }

        return newAnalyzedTokens;
      }
      else {
        // we don't want Нью-Париж but want Австрійсько-Карпатський
        if( StringTools.isCapitalizedWord(rightWord)
            || leftWord.endsWith("о")
            || PosTagHelper.hasPosTag(rightAnalyzedTokens, Pattern.compile("adj.*")) ) {

          // tag Чорноморське/noun і чорноморське adj
          List<TaggedWord> rightWdList2 = tagAsIsAndWithLowerCase(rightWord);
          List<AnalyzedToken> rightAnalyzedTokens2 = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(rightWord, rightWdList2);

          List<AnalyzedToken> match = tryOWithAdj(word, leftWord, rightAnalyzedTokens2);
          if( match != null )
            return match;
        }

        return null;
      }
    }

    // TODO: ua_2019
    // майстер-класу
    
    if( dashPrefixMatch ) {
      List<AnalyzedToken> newTokens = new ArrayList<>();
      if( leftWord.length() == 1 && leftWord.matches("[a-zA-Zα-ωΑ-Ω]") ) {
        List<AnalyzedToken> newTokensAdj = getNvPrefixLatWithAdjMatch(word, rightAnalyzedTokens, leftWord);
        if( newTokensAdj != null ) {
          newTokens.addAll(newTokensAdj);
        }
      }
      List<AnalyzedToken> newTokensNoun = getNvPrefixNounMatch(word, rightAnalyzedTokens, leftWord);
      if( newTokensNoun != null ) {
        newTokens.addAll(newTokensNoun);
      }
      return newTokens;
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

    if( ! leftWdList.isEmpty() && leftWord.length() > 2 ) {
      List<AnalyzedToken> tagMatch = tagMatch(word, leftAnalyzedTokens, rightAnalyzedTokens);
      if( tagMatch != null ) {
        return tagMatch;
      }
    }

    List<AnalyzedToken> match = tryOWithAdj(word, leftWord, rightAnalyzedTokens);
    if( match != null )
      return match;

    compoundDebugLogger.logUnknownCompound(word);
    
    return null;
  }

  private List<TaggedWord> tagEitherCase(String rightWord) {
    List<TaggedWord> rightWdList = wordTagger.tag(rightWord);
    if( rightWdList.isEmpty() ) {
      if( Character.isUpperCase(rightWord.charAt(0)) ) {
        rightWdList = wordTagger.tag(rightWord.toLowerCase());
      }
    }
    return rightWdList;
  }

//  private List<TaggedWord> tagBothCases(String rightWord) {
//    List<TaggedWord> rightWdList = wordTagger.tag(rightWord);
//    if( Character.isUpperCase(rightWord.charAt(0)) ) {
//      rightWdList = wordTagger.tag(rightWord.toLowerCase());
//    }
//    return rightWdList;
//  }


  private List<AnalyzedToken> tryOWithAdj(String word, String leftWord, List<AnalyzedToken> rightAnalyzedTokens) {
    if( leftWord.length() < 3 )
      return null;
    
    // багато..., мало.... пишуться разом
    if( LEFT_O_ADJ_INVALID.contains(leftWord.toLowerCase()) )
      return null;

    // дво-триметровий...
    if( NUMR_ADJ_PATTERN.matcher(leftWord).matches() ) {
      return numrAdjMatch(word, rightAnalyzedTokens, leftWord);
    }

    // яскраво-барвистий...
    if( O_ADJ_PATTERN.matcher(leftWord).matches() ) {
      return oAdjMatch(word, rightAnalyzedTokens, leftWord);
    }
    
    return null;
  }

  private List<AnalyzedToken> doGuessMultiHyphens(String word, int firstDashIdx, int dashIdx) {
    String lowerWord = word.toLowerCase();

    String[] parts = lowerWord.split("-");
    LinkedHashSet<String> set = new LinkedHashSet<>(Arrays.asList(parts));

    // try intj
    String leftWd = parts[0];
    if( set.size() == 2 ) {
      List<TaggedWord> leftWdList = tagEitherCase(leftWd);
      List<TaggedWord> rightWdList = tagEitherCase(new ArrayList<>(set).get(1));

      if( PosTagHelper.hasPosTag2(leftWdList, INTJ_PATTERN)
          && PosTagHelper.hasPosTag2(rightWdList, INTJ_PATTERN) ) {
        return Arrays.asList(new AnalyzedToken(word, rightWdList.get(0).getPosTag(), lowerWord));
      }
    }
    else if( set.size() == 1 ) {
      if( lowerWord.equals("ла") ) {
        return Arrays.asList(new AnalyzedToken(word, "intj", lowerWord));
      }

      List<TaggedWord> rightWdList = tagEitherCase(leftWd);
      if( PosTagHelper.hasPosTag2(rightWdList, INTJ_PATTERN) ) {
        return Arrays.asList(new AnalyzedToken(word, rightWdList.get(0).getPosTag(), lowerWord));
      }
    } 
//      Pattern stretch = Pattern.compile("[а-яіїєґ]+([а-яіїєґ])(-$1)+-[а-яіїєґ]+");
//      Matcher matcher = stretch.matcher(word);
    // ду-у-у-же
    if( parts.length >= 3 
        && (set.size() == 3 || set.size() == 2) 
        && parts[1].length() == 1 ) {

      String rightWd = parts[parts.length-1];
      List<TaggedWord> wdList = tagEitherCase(leftWd+rightWd);

      if( wdList.isEmpty() 
          && leftWd.charAt(leftWd.length()-1) == rightWd.charAt(0)
          && rightWd.charAt(0) == parts[1].charAt(0) ) {
        // ду-у-у-уже
        wdList = tagEitherCase(leftWd+rightWd.substring(1));
      }
      if( ! wdList.isEmpty() ) {
        return ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(word, PosTagHelper.addIfNotContains(wdList, ":coll"));
      }
    }

    return null;
  }

  private List<AnalyzedToken> doGuessTwoHyphens(String word, int firstDashIdx, int dashIdx) {
    String[] parts = word.split("-");

    List<TaggedWord> rightWdList = tagEitherCase(parts[2]);

    if( rightWdList.isEmpty() )
      return null;

    List<AnalyzedToken> rightAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(parts[2], rightWdList);

    String firstAndSecond = parts[0] + "-" + parts[1];

    if( dashPrefixes2.contains( firstAndSecond )
        || dashPrefixes2.contains( firstAndSecond.toLowerCase() ) ) {

      return getNvPrefixNounMatch(word, rightAnalyzedTokens, firstAndSecond);
    }


    List<TaggedWord> secondWdList = tagEitherCase(parts[1]);
    
    // try full match - only adj for now - nouns are complicated
    if( PosTagHelper.startsWithPosTag(secondWdList, "adj") ) {
      List<AnalyzedToken> secondAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(parts[1], secondWdList);

      List<AnalyzedToken> tagMatchSecondAndThird = tagMatch(word, secondAnalyzedTokens, rightAnalyzedTokens);
      if( tagMatchSecondAndThird != null ) {
        
        List<TaggedWord> leftWdList = tagEitherCase(parts[0]);
        
        if( ! leftWdList.isEmpty() ) {
          List<AnalyzedToken> leftAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(parts[0], leftWdList);
          tagMatch(word, leftAnalyzedTokens, tagMatchSecondAndThird);
        }
        
        return tagMatchSecondAndThird;
      }
    }

    // try ірансько-нігерійсько-зімбабвійський
    List<AnalyzedToken> secondAndThird = tryOWithAdj(word, parts[1], rightAnalyzedTokens);
    
    if( secondAndThird != null ) {
      return tryOWithAdj(word, parts[0], secondAndThird);
    }
    
    return null;
  }


  private static List<AnalyzedToken> generateTokensWithRighInflected(String word, String leftWord, List<AnalyzedToken> rightAnalyzedTokens, String posTagStart) {
    List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>(rightAnalyzedTokens.size());
    for (AnalyzedToken analyzedToken : rightAnalyzedTokens) {
      String posTag = analyzedToken.getPOSTag();
      if( posTag.startsWith( posTagStart )
            && ! posTag.contains("v_kly") ) {
        newAnalyzedTokens.add(new AnalyzedToken(word, posTag, leftWord + "-" + analyzedToken.getLemma()));
      }
    }
    return newAnalyzedTokens;
  }


  private List<AnalyzedToken> matchNumberedProperNoun(String word, String leftWord, String rightWord) {

    // Ан-140
    if( NOUN_SUFFIX_NUMBER_LETTER.matcher(rightWord).matches() ) {
      Set<AnalyzedToken> newAnalyzedTokens = new LinkedHashSet<>();

      for(Map.Entry<String, List<String>> entry: numberedEntities.entrySet()) {
        if( word.matches(entry.getKey()) ) {
            for(String tag: entry.getValue()) {
                if( tag.contains(":nv") ) {
                  String[] tagParts = tag.split(":");
                  String extraTags = tag.replaceFirst(".*?:nv", "").replace(":np", "");
                  List<AnalyzedToken> newTokens = PosTagHelper.generateTokensForNv(word, tagParts[1], extraTags);
                  newAnalyzedTokens.addAll(newTokens);

                  if( ! tag.contains(":np") && ! tag.contains(":p") ) {
                    newTokens = PosTagHelper.generateTokensForNv(word, "p", extraTags);
                    newAnalyzedTokens.addAll(newTokens);
                  }
                }
                else {
                    newAnalyzedTokens.add(new AnalyzedToken(word, tag, word));
                }
            }
        }
      }

      if (newAnalyzedTokens.size() > 0)
        return new ArrayList<>(newAnalyzedTokens);
    }

    // Вибори-2014
    if (YEAR_NUMBER.matcher(rightWord).matches()) {
      List<TaggedWord> leftWdList = tagAsIsAndWithLowerCase(leftWord);

      if (!leftWdList.isEmpty() && Character.isUpperCase(leftWord.charAt(0))) {
        List<AnalyzedToken> leftAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(leftWord, leftWdList);

        List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>();

        for (AnalyzedToken analyzedToken : leftAnalyzedTokens) {
          if (!PosTagHelper.hasPosTagPart(analyzedToken, ":prop")
              && !WORDS_WITH_YEAR.contains(analyzedToken.getLemma()))
            continue;

          String posTag = analyzedToken.getPOSTag();

          // only noun - відкидаємо: вибори - вибороти
          // Афіни-2014 - потрібне лише місто, не ім'я
          if (posTag == null || ! posTag.startsWith("noun:inanim"))
            continue;

          if (posTag.contains("v_kly"))
            continue;

          if (posTag.contains(":p:") && !Arrays.asList("гра", "вибори", "бюджет").contains(analyzedToken.getLemma())
              && !posTag.contains(":ns"))
            continue;

          String lemma = analyzedToken.getLemma();
          posTag = posTag.replace(":geo", "");

          if (!posTag.contains(":prop")) {
            posTag += ":prop";
            lemma = StringUtils.capitalize(lemma);
          }
          newAnalyzedTokens.add(new AnalyzedToken(word, posTag, lemma + "-" + rightWord));

        }

        if (newAnalyzedTokens.size() > 0)
          return newAnalyzedTokens;
      }
    }


    // Формула-1, Карпати-2, омега-3
    if (NOUN_PREFIX_NUMBER.matcher(rightWord).matches()) {
      List<TaggedWord> leftWdList = tagAsIsAndWithLowerCase(leftWord);

      if (!leftWdList.isEmpty() /*&& Character.isUpperCase(leftWord.charAt(0)) && leftWord.matches("[А-ЯІЇЄҐ][а-яіїєґ'].*")*/ ) {

        List<AnalyzedToken> leftAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(leftWord, leftWdList);
        List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>();

        for (AnalyzedToken analyzedToken : leftAnalyzedTokens) {

          String posTag = analyzedToken.getPOSTag();
          String lemma = analyzedToken.getLemma();

          if( posTag == null || ! posTag.startsWith("noun:inanim") )
            continue;

          if (posTag.contains("v_kly"))
            continue;

          if ( ! posTag.contains(":prop") ) {
            if( ! WORDS_WITH_NUM.contains(lemma) ) {
              posTag += ":prop";
              lemma = StringUtils.capitalize(lemma);
            }
          }

          if( ! WORDS_WITH_NUM.contains(lemma) )
            continue;


          newAnalyzedTokens.add(new AnalyzedToken(word, posTag, lemma + "-" + rightWord));
        }

        if (newAnalyzedTokens.size() > 0)
          return newAnalyzedTokens;
      }
    }

    return null;
  }


  private List<AnalyzedToken> matchDigitCompound(String word, String leftWord, String rightWord) {
    // 101-й, 100-річному

    if( ADJ_PREFIX_NUMBER.matcher(leftWord).matches() ) {
      List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>();

      // e.g. 101-го
      String[] tags = LetterEndingForNumericHelper.findTags(leftWord, rightWord); 
      if( tags != null ) {
        for (String tag: tags) {
          String lemma = leftWord + "-" + "й";  // lemma is approximate here, we mostly care about the tag
          newAnalyzedTokens.add(new AnalyzedToken(word, IPOSTag.adj.getText() + tag + ":&numr", lemma));
        }

        // з 3-ма вікнами - не дуже правильно, але вживають часто
        if( "ма".equals(rightWord) ) {
          newAnalyzedTokens.add(new AnalyzedToken(word, IPOSTag.noun.getText() + ":p:v_oru:&numr:bad", leftWord));
        }
        // вбивство 148-ми селян
        else if( "ми".equals(rightWord) 
            && Pattern.compile("(.*[^1]|^)[78]").matcher(leftWord).matches() ) {
          newAnalyzedTokens.add(new AnalyzedToken(word, "numr:p:v_rod:bad", leftWord));
          newAnalyzedTokens.add(new AnalyzedToken(word, "numr:p:v_dav:bad", leftWord));
          newAnalyzedTokens.add(new AnalyzedToken(word, "numr:p:v_mis:bad", leftWord));
        }
      }
      else {
        if( NOUN_PREFIX_NUMBER.matcher(leftWord).matches() ) {
          // 100-річчя
          
          String tryPrefix = getTryPrefix(rightWord);
          
          if( tryPrefix != null ) {
            List<TaggedWord> rightWdList = wordTagger.tag(tryPrefix + rightWord);
            
            if( rightWdList == null )
              return null;

            List<AnalyzedToken> rightAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(rightWord, rightWdList);

            for (AnalyzedToken analyzedToken : rightAnalyzedTokens) {
              String lemma = analyzedToken.getLemma().substring(tryPrefix.length());
              newAnalyzedTokens.add(new AnalyzedToken(word, analyzedToken.getPOSTag(), leftWord + "-" + lemma));
            }

            return newAnalyzedTokens;
          }
          // 100-мм гаубиці
          else if( "мм".equals(rightWord) ) {
            for(String gender: PosTagHelper.BASE_GENDERS ) {
              for(String vidm: PosTagHelper.VIDMINKY_MAP.keySet()) {
                if( vidm.equals("v_kly") )
                  continue;

                String posTag = IPOSTag.adj.getText() + ":" + gender + ":" + vidm;
                newAnalyzedTokens.add(new AnalyzedToken(word, posTag, word));
              }
            }
            return newAnalyzedTokens;
          }
          // вбивство 15-ти селян
          else if( "ти".equals(rightWord) 
              && Pattern.compile(".*([0569]|1[0-9])").matcher(leftWord).matches() ) {
            newAnalyzedTokens.add(new AnalyzedToken(word, "numr:p:v_rod:bad", leftWord));
            newAnalyzedTokens.add(new AnalyzedToken(word, "numr:p:v_dav:bad", leftWord));
            newAnalyzedTokens.add(new AnalyzedToken(word, "numr:p:v_mis:bad", leftWord));
          }
        }

        // e.g. 100-річному, 100-відсотково
        List<TaggedWord> rightWdList = wordTagger.tag(rightWord);
        if( rightWdList.isEmpty() )
          return null;

        List<AnalyzedToken> rightAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(rightWord, rightWdList);

        for (AnalyzedToken analyzedToken : rightAnalyzedTokens) {
          if( analyzedToken.getPOSTag().startsWith(IPOSTag.adj.getText())
              || "відсотково".equals(analyzedToken.getLemma()) ) {
            newAnalyzedTokens.add(new AnalyzedToken(word, analyzedToken.getPOSTag(), leftWord + "-" + analyzedToken.getLemma()));
          }
        }
      }
      return newAnalyzedTokens.isEmpty() ? null : newAnalyzedTokens;
    }
    
    return null;
  }


  private String getTryPrefix(String rightWord) {
    if( REQ_NUM_STO_PATTERN.matcher(rightWord).matches() )
      return "сто";
    if( REQ_NUM_DESYAT_PATTERN.matcher(rightWord).matches() ) 
      return "десяти";
    if( REQ_NUM_DVA_PATTERN.matcher(rightWord).matches() ) 
      return "дво";

    return null;
  }


  @Nullable
  private List<AnalyzedToken> tagMatch(String word, List<AnalyzedToken> leftAnalyzedTokens, List<AnalyzedToken> rightAnalyzedTokens) {
    List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>();
    List<AnalyzedToken> newAnalyzedTokensAnimInanim = new ArrayList<>();

    String animInanimNotTagged = null;

    for (AnalyzedToken leftAnalyzedToken : leftAnalyzedTokens) {
      String leftPosTag = leftAnalyzedToken.getPOSTag();

      if( leftPosTag == null 
          || IPOSTag.contains(leftPosTag, IPOSTag.abbr.getText()) )
        continue;

      // we don't want to have v_kly for рибо-полювання
      // but we do for пане-товаришу
      if( leftPosTag.startsWith("noun:inanim")
          && leftPosTag.contains("v_kly") )
        continue;

      String leftPosTagExtra = "";
      boolean leftNv = false;

      if( leftPosTag.contains(PosTagHelper.NO_VIDMINOK_SUBSTR) ) {
        leftNv = true;
        leftPosTag = leftPosTag.replace(PosTagHelper.NO_VIDMINOK_SUBSTR, "");
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

        if( rightPosTag == null
//            || rightPosTag.contains("v_kly")
            || rightPosTag.contains(IPOSTag.abbr.getText()) )
          continue;

        if( rightPosTag.startsWith("noun:inanim")
            && rightPosTag.contains("v_kly") )
          continue;

        String extraNvTag = "";
        boolean rightNv = false;
        if( rightPosTag.contains(PosTagHelper.NO_VIDMINOK_SUBSTR) ) {
          rightNv = true;
          
          if( leftNv ) {
            extraNvTag += PosTagHelper.NO_VIDMINOK_SUBSTR;
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
        
        if (stripPerfImperf(leftPosTag).equals(stripPerfImperf(rightPosTag)) 
            && (IPOSTag.startsWith(leftPosTag, IPOSTag.numr, IPOSTag.adv, IPOSTag.adj, IPOSTag.verb)
            || (IPOSTag.startsWith(leftPosTag, IPOSTag.intj) 
                && leftAnalyzedToken.getLemma().equalsIgnoreCase(rightAnalyzedToken.getLemma())) ) ) {
          String newPosTag = leftPosTag + extraNvTag + leftPosTagExtra;

          if( (leftPosTag.contains("adjp") && ! rightPosTag.contains("adjp"))
              || (! leftPosTag.contains("adjp") && rightPosTag.contains("adjp")) ) {
            newPosTag = newPosTag.replaceFirst(":&adjp:(actv|pasv):(im)?perf", "");
          }
          
          String newLemma = leftAnalyzedToken.getLemma() + "-" + rightAnalyzedToken.getLemma();
          newAnalyzedTokens.add(new AnalyzedToken(word, newPosTag, newLemma));
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

    if( ! newAnalyzedTokens.isEmpty() 
        && ! PosTagHelper.hasPosTagPart(newAnalyzedTokens, ":p:") ) {
      if( (LemmaHelper.hasLemma(leftAnalyzedTokens, LemmaHelper.DAYS_OF_WEEK) && LemmaHelper.hasLemma(rightAnalyzedTokens, LemmaHelper.DAYS_OF_WEEK))
          || (LemmaHelper.hasLemma(leftAnalyzedTokens, LemmaHelper.MONTH_LEMMAS) && LemmaHelper.hasLemma(rightAnalyzedTokens, LemmaHelper.MONTH_LEMMAS)) ) {
        newAnalyzedTokens.add(new AnalyzedToken(word, newAnalyzedTokens.get(0).getPOSTag().replaceAll(":[mfn]:", ":p:"), newAnalyzedTokens.get(0).getLemma()));
      }
    }
    
    // remove duplicates
    newAnalyzedTokens = new ArrayList<>(new LinkedHashSet<>(newAnalyzedTokens));
    
    if( newAnalyzedTokens.isEmpty() ) {
      newAnalyzedTokens = newAnalyzedTokensAnimInanim;
    }

    if( animInanimNotTagged != null && newAnalyzedTokens.isEmpty() ) {
      compoundDebugLogger.logUnknownCompound(word + " " + animInanimNotTagged);
    }
    
    return newAnalyzedTokens.isEmpty() ? null : newAnalyzedTokens;
  }


  private static String stripPerfImperf(String leftPosTag) {
    return leftPosTag.replaceAll(":(im)?perf|:&adjp:(actv|pasv)", "");
  }


  private boolean isJuniorSenior(AnalyzedToken leftAnalyzedToken, AnalyzedToken rightAnalyzedToken) {
    return leftAnalyzedToken.getPOSTag().matches(".*?:[flp]name.*") && rightAnalyzedToken.getLemma().matches(".*(молодший|старший)");
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
            compoundDebugLogger.logGenderMix(word, leftNv, leftPosTag, rightPosTag);
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

      List<TaggedWord> taggedWords = new ArrayList<>();

      // яскраво для яскраво-барвистий, три-чотириметровий
      taggedWords = tagBothCases(leftWord, Pattern.compile("^adv.*|.*?numr.*"));
      if( taggedWords.isEmpty() ) {
        taggedWords = tagBothCases(oToYj(leftWord), Pattern.compile("^adj.*"));  // кричущий для кричуще-яскравий
      }
      if( taggedWords.isEmpty() ) {
        taggedWords = tagBothCases(leftBase, Pattern.compile("^noun.*"));         // паталог для паталого-анатомічний
      }
      if( taggedWords.isEmpty() ) {
        // два для дво-триметровий, етико-філологічний
        taggedWords = tagBothCases(leftBase + "а", Pattern.compile("(noun:inanim:f:v_naz|numr).*"));   
      }
      if( taggedWords.isEmpty() )
        return null;

      // важконапрацьований - разом
      if(! extraTag.equals(":bad") && taggedWords.get(0).getPosTag().startsWith(IPOSTag.adv.getText())
          && PosTagHelper.hasPosTagPart(analyzedTokens, "adjp")) {
        extraTag = ":bad";
      }

      if (! extraTag.equals(":bad") && PosTagHelper.hasPosTagPart2(taggedWords, ":bad")) {
          extraTag = ":bad";
      }
    }

    for (AnalyzedToken analyzedToken : analyzedTokens) {
      String posTag = analyzedToken.getPOSTag();
      if( posTag.startsWith( IPOSTag.adj.getText() ) ) {
        if( posTag.contains(":comp") ) {
          posTag = PosTagHelper.ADJ_COMP_REGEX.matcher(posTag).replaceFirst("");
        }
        if( ! posTag.contains(":bad") ) {
          posTag += extraTag;
        }
        newAnalyzedTokens.add(new AnalyzedToken(word, posTag, leftWord.toLowerCase() + "-" + analyzedToken.getLemma()));
      }
    }
    
    return newAnalyzedTokens.isEmpty() ? null : newAnalyzedTokens;
  }

    @Nullable
    private List<AnalyzedToken> numrAdjMatch(String word, List<AnalyzedToken> analyzedTokens, String leftWord) {
      List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>(analyzedTokens.size());

      String extraTag = "";

      List<TaggedWord> taggedWords = wordTagger.tag(leftWord);
      if( ! PosTagHelper.startsWithPosTag(taggedWords, "numr") )
        return null;

      // двох-трьохметровий - bad
      if( leftWord.matches(".*?(двох|трьох|чотирьох)") ) {
        //        taggedWords = wordTagger.tag("два");
        extraTag = ":bad";
      }

      for (AnalyzedToken analyzedToken : analyzedTokens) {
        String posTag = analyzedToken.getPOSTag();
        if( posTag.startsWith( IPOSTag.adj.getText() ) ) {
          if( posTag.contains(":comp") ) {
            posTag = PosTagHelper.ADJ_COMP_REGEX.matcher(posTag).replaceFirst("");
          }
          if( ! posTag.contains(":bad") ) {
            posTag += extraTag;
          }
          newAnalyzedTokens.add(new AnalyzedToken(word, posTag, leftWord.toLowerCase() + "-" + analyzedToken.getLemma()));
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
  private static List<AnalyzedToken> getNvPrefixNounMatch(String word, List<AnalyzedToken> analyzedTokens, String leftWord) {
    List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>(analyzedTokens.size());

    for (AnalyzedToken analyzedToken : analyzedTokens) {
      String posTag = analyzedToken.getPOSTag();
      if( posTag.startsWith(IPOSTag.noun.getText() )
          && ! posTag.contains("v_kly") ) {

        if( Arrays.asList("В2В", "АІ").contains(leftWord) ) {
            posTag = PosTagHelper.addIfNotContains(posTag, ":bad");
        }

        newAnalyzedTokens.add(new AnalyzedToken(word, posTag, leftWord + "-" + analyzedToken.getLemma()));
      }
    }

    return newAnalyzedTokens.isEmpty() ? null : newAnalyzedTokens;
  }

  @Nullable
  private static List<AnalyzedToken> getNvPrefixLatWithAdjMatch(String word, List<AnalyzedToken> analyzedTokens, String leftWord) {
    List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>(analyzedTokens.size());
    
    for (AnalyzedToken analyzedToken : analyzedTokens) {
      String posTag = analyzedToken.getPOSTag();
         if( posTag.startsWith(IPOSTag.adj.getText())    // n-векторний 
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

  private List<TaggedWord> tagBothCases(String leftWord, Pattern posTagMatcher) {
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
    
    if( posTagMatcher != null ) {
      leftWdList = leftWdList.stream()
          .filter(word -> posTagMatcher.matcher(word.getPosTag()).matches())
          .collect(Collectors.toList());
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

}