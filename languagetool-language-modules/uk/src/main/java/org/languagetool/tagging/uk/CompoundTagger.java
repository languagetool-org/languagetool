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
import java.util.HashSet;
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
  private static final Pattern EXTRA_TAGS_DROP = Pattern.compile(":(comp.|np|ns|slang|xp[1-9]|&predic|&insert)");
  private static final Pattern NOUN_SING_V_ROD_REGEX = Pattern.compile("noun.*?:[mfn]:v_rod.*");
//  private static final Pattern NOUN_V_NAZ_REGEX = Pattern.compile("noun.*?:.:v_naz.*");
  private static final Pattern SING_REGEX_F = Pattern.compile(":[mfn]:");
  private static final Pattern O_ADJ_PATTERN = Pattern.compile(".+?(о|[чшщ]е)");
  private static final Pattern NUMR_ADJ_PATTERN = Pattern.compile(".+?(одно|дво|ох|и)");
  private static final Pattern DASH_PREFIX_LAT_PATTERN = Pattern.compile("[a-zA-Z]{3,}|[α-ωΑ-Ω]");
  private static final Pattern YEAR_NUMBER = Pattern.compile("[12][0-9]{3}");
  private static final Pattern NOUN_PREFIX_NUMBER = Pattern.compile("[0-9]+");
  private static final Pattern NOUN_SUFFIX_NUMBER_LETTER = Pattern.compile("[0-9][0-9А-ЯІЇЄҐ-]*");
  private static final Pattern ADJ_PREFIX_NUMBER = Pattern.compile("[0-9]+(,[0-9]+)?([-–—][0-9]+(,[0-9]+)?)?%?|(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})|І{2,3}");
  private static final Pattern REQ_NUM_DVA_PATTERN = Pattern.compile("(місн|томник|поверхів).{0,4}");
  private static final Pattern REQ_NUM_DESYAT_PATTERN = Pattern.compile("(класни[кц]|бальни[кц]|раундов|томн|томов|хвилин|десятиріч|кілометрів|річ).{0,4}");
  private static final Pattern REQ_NUM_STO_PATTERN = Pattern.compile("(річч|літт|метрів|грамов|тисячник).{0,3}");
  private static final Pattern INTJ_PATTERN = Pattern.compile("intj.*");
  private static final Pattern ONOMAT_PATTERN = Pattern.compile("onomat.*");
  private static final Pattern UKR_LETTERS_PATTERN = Pattern.compile("[А-ЯІЇЄҐа-яіїєґ'-]+");
  private static final Pattern GEO_V_NAZ = Pattern.compile("noun:inanim:.:v_naz.*:geo.*");
  private static final Pattern FNAME = Pattern.compile("noun:anim:[mf].*fname.*");
  private static final Pattern LNAME_V_NAZ = Pattern.compile("noun:anim:[fm]:v_naz.*lname.*");
  private static final Pattern LNAME_V_ROD = Pattern.compile("noun:anim:[fm]:v_rod.*lname.*");
  private static final Pattern NAME = Pattern.compile("noun:anim:.*name.*");
  private static final Pattern PROP_V_NAZ = Pattern.compile("noun:inanim:.:v_naz.*prop.*");

  private static final Pattern MNP_NAZ_REGEX = Pattern.compile(".*?:[mnp]:v_naz.*");
  private static final Pattern MNP_ZNA_REGEX = Pattern.compile(".*?:[mnp]:v_zna.*");
  private static final Pattern MNP_ROD_REGEX = Pattern.compile(".*?:[mnp]:v_rod.*");

  private static final Pattern stdNounTagRegex = Pattern.compile("noun:(?:in)?anim:(.):(v_...).*");
  private static final Map<String, String> dashPrefixes;
  private static final Set<String> leftMasterSet;
  private static final Map<String, List<String>> numberedEntities;
  private static final Map<String, Pattern> rightPartsWithLeftTagMap = new HashMap<>();
  private static final Set<String> followerSet;
  private static final Set<String> dashPrefixesInvalid;
  private static final Set<String> noDashPrefixes2019;
  private static final Set<String> noDashPrefixes;
  private static final String ADJ_TAG_FOR_PO_ADV_MIS = "adj:m:v_mis";
  private static final String ADJ_TAG_FOR_PO_ADV_NAZ = "adj:m:v_naz";
  private static final Pattern PREFIX_NO_DASH_POSTAG_PATTERN = Pattern.compile("(noun|adj|adv)(?!.*&pron).*");

  // додаткові вкорочені прикметникові ліві частини, що не мають відповідного прикметника
  private static final List<String> LEFT_O_ADJ = Arrays.asList(
    "австро", "адиго", "американо", "англо", "афро", "еко", "індо", "іспано", "італо", "історико", 
    "києво", "марокано", "угро", "японо", "румуно"
  );

  static final List<String> LEFT_O_ADJ_INVALID = Arrays.asList(
    "багато", "мало", "високо", "низько", "старо", "важко", "зовнішньо", "внутрішньо", "ново", "середньо",
    "південно", "північно", "західно", "східно", "центрально", "ранньо", "пізньо"
  );

  static final Pattern LEFT_O_ADJ_INVALID_PATTERN = Pattern.compile("^(" + StringUtils.join(LEFT_O_ADJ_INVALID, "|") + ")(.+)");

  // TODO: чемпіонат світу-2014, людина року-2018, Червона рута-2011, Нова хвиля-2012, Фабрика зірок-2
  private static final List<String> WORDS_WITH_YEAR = Arrays.asList(
      "бюджет", "вибори", "гра", "держбюджет", "кошторис", "кампанія",
      "єврокубок", "єврокваліфікація", "євровідбір", "єврофорум",
      "конкурс", "кінофестиваль", "кубок", "мундіаль", "м'яч", "олімпіада", "оцінювання", "оскар",
      "пектораль", "перегони", "першість", "політреформа", "премія", "рейтинг", "реформа", "сезон", 
      "турнір", "універсіада", "фестиваль", "форум", "чемпіонат", "чемпіон", "чемпіонка", "ярмарок", "ЧУ", "ЧЄ");
  private static final List<String> WORDS_WITH_NUM = Arrays.asList(
      "Формула", "Карпати", "Динамо", "Шахтар", "Фукусіма", "Квартал", "Золоте", "Мінськ", "Нюренберг",
      "омега", "плутоній", "полоній", "стронцій", "уран", "потік"); //TODO: потік-2 - prop
  private static final List<String> NAME_SUFFIX = Arrays.asList("ага", "ефенді", "бек", "заде", "огли", "сан", "кизи", "сенсей");
  private static final List<String> BAD_SUFFIX = Arrays.asList("б", "би", "ж", "же");
  private static final Pattern SKY_PATTERN = Pattern.compile(".*[сзц]ьки");
  private static final Pattern SKYI_PATTERN = Pattern.compile(".*[сзц]ький");

  // http://www.pravopys.net/sections/33/
  static {
    rightPartsWithLeftTagMap.put("бо", Pattern.compile("(verb|.*?pron|noun|adv|intj|part).*"));
    rightPartsWithLeftTagMap.put("но", Pattern.compile("((verb(?!.*bad).*?:(impr|futr|&insert))|intj|adv|part|conj).*")); 
    rightPartsWithLeftTagMap.put("от", Pattern.compile("(.*?pron|adv|part|verb).*"));
    rightPartsWithLeftTagMap.put("то", Pattern.compile("(.*?pron|verb|noun|adj|adv|conj).*")); // part|conj
    // noun gives false on зразу-таки
    rightPartsWithLeftTagMap.put("таки", Pattern.compile("(verb|adv|adj|.*?pron|part|noninfl:&predic).*")); 

    dashPrefixes = ExtraDictionaryLoader.loadMap("/uk/dash_prefixes.txt");
    dashPrefixesInvalid = ExtraDictionaryLoader.loadSet("/uk/dash_prefixes_invalid.txt");
    noDashPrefixes2019 = dashPrefixes.entrySet().stream()
         .filter(e -> e.getValue().contains("ua_1992"))
         .map(e -> e.getKey())
         .collect(Collectors.toSet());

    noDashPrefixes = new HashSet<>(dashPrefixesInvalid);
    noDashPrefixes.addAll(noDashPrefixes2019);
    // too many false positives
    noDashPrefixes.remove("мілі");
    noDashPrefixes.remove("поп");
    noDashPrefixes.remove("прес");
    
    leftMasterSet = ExtraDictionaryLoader.loadSet("/uk/dash_left_master.txt");
    // TODO: "бабуся", "лялька", "рятівник" - not quite followers, could be masters too
    followerSet = ExtraDictionaryLoader.loadSet("/uk/dash_follower.txt");
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
    List<AnalyzedToken> guessedTokens = doGuessCompoundTag(word);
    compoundDebugLogger.logTaggedCompound(guessedTokens);
    return guessedTokens;
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
    String leftWordLowerCase = leftWord.toLowerCase(conversionLocale);

    // з-зателефоную

    if( leftWord.length() == 1 && rightWord.length() > 3 && rightWord.startsWith(leftWordLowerCase) ) {
      List<TaggedWord> rightWdList = wordTagger.tag(rightWord);
      rightWdList = PosTagHelper.adjust(rightWdList, null, null, ":alt");
      return ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(word, rightWdList);
    }
    
    
    boolean dashPrefixMatch = dashPrefixes.containsKey( leftWord ) 
        || dashPrefixes.containsKey( leftWordLowerCase ) 
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
    if( dashPrefixesInvalid.contains(leftWordLowerCase) ) {
      List<TaggedWord> rightWdList = tagEitherCase(rightWord);
      
      rightWdList = PosTagHelper.filter2(rightWdList, Pattern.compile("(noun|adj)(?!.*pron).*"));
      
      if( rightWdList.isEmpty() )
        return null;

//      String lemma = leftWord + "-" + rightWdList.get(0).getLemma();
      String extraTag = StringTools.isCapitalizedWord(rightWord) ? "" : ":bad";
      rightWdList = PosTagHelper.adjust(rightWdList, leftWord + "-", null, extraTag);
      return ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(word, rightWdList);
    }


    // wrong: пів-качана
    if( leftWordLowerCase.equals("пів")
        && Character.isLowerCase(rightWord.charAt(0)) ) {

      List<TaggedWord> rightWdList = tagEitherCase(rightWord);
      List<AnalyzedToken> rightAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(rightWord, rightWdList);

      List<AnalyzedToken> newAnalyzedTokens = addPluralNvTokens(word, rightAnalyzedTokens, ":bad");
      return newAnalyzedTokens;
    }

    List<TaggedWord> leftWdList = tagAsIsAndWithLowerCase(leftWord);


    // стривай-бо, чекай-но, прийшов-таки, такий-от, такий-то

    String rightWordLowerCase = rightWord.toLowerCase();
    if( rightPartsWithLeftTagMap.containsKey(rightWordLowerCase) 
        && ! PosTagHelper.hasPosTagPart2(leftWdList, "abbr") ) {

      if( leftWdList.isEmpty() )
        return null;

      Pattern leftTagRegex = rightPartsWithLeftTagMap.get(rightWordLowerCase);

      List<AnalyzedToken> leftAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(leftWord, leftWdList);
      List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>(leftAnalyzedTokens.size());

      // ignore хто-то
      if( rightWordLowerCase.equals("то")
          && LemmaHelper.hasLemma(leftAnalyzedTokens, Arrays.asList("хто", "що", "чи")) )
        return null;

      for (AnalyzedToken analyzedToken : leftAnalyzedTokens) {
        String posTag = analyzedToken.getPOSTag();
        if (leftWord.equalsIgnoreCase("як") && posTag != null && posTag.contains("noun") )
          continue;
          
        if( posTag != null
            && (leftWordLowerCase.equals("дуже") && posTag.contains("adv")) 
             || (leftTagRegex.matcher(posTag).matches()) ) {
          
          newAnalyzedTokens.add(new AnalyzedToken(word, posTag, analyzedToken.getLemma()));
        }
      }

      return newAnalyzedTokens.isEmpty() ? null : newAnalyzedTokens;
    }


    // по-болгарськи, по-болгарському

    if( leftWord.equalsIgnoreCase("по") && SKY_PATTERN.matcher(rightWord).matches() ) {
      rightWord += "й";
    }
    
    // Пенсильванія-авеню

    if( Character.isUpperCase(leftWord.charAt(0)) && LemmaHelper.CITY_AVENU.contains(rightWordLowerCase) ) {
      String addPos = rightWord.equals("штрассе") ? ":alt" : "";
      return PosTagHelper.generateTokensForNv(word, "f", ":prop" + addPos);
    }

    // Fe-вмісний
    if( rightWordLowerCase.startsWith("вмісн") ) {
      String adjustedWord = "боро" + rightWord;
      List<TaggedWord> rightWdList = tagEitherCase(adjustedWord);
      rightWdList = rightWdList.stream().map(wd -> new TaggedWord("вмісний", wd.getPosTag())).collect(Collectors.toList());
      List<AnalyzedToken> rightAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(rightWord, rightWdList);
      return generateTokensWithRighInflected(word, leftWord, rightAnalyzedTokens, IPOSTag.adj.getText(), null, Pattern.compile(":comp."));
    }

    List<TaggedWord> rightWdList = tagEitherCase(rightWord);
      
     
    if( word.toLowerCase().startsWith("напів") ) {
      // напівпольської-напіванглійської
      Matcher napivMatcher = Pattern.compile("напів(.+?)-напів(.+)").matcher(word);
      if( napivMatcher.matches() ) {
        List<TaggedWord> napivLeftWdList = PosTagHelper.adjust(tagAsIsAndWithLowerCase(napivMatcher.group(1)), "напів", null);
        List<TaggedWord> napivRightWdList = rightWdList.size() > 0 ? rightWdList : PosTagHelper.adjust(tagAsIsAndWithLowerCase(napivMatcher.group(2)), "напів", null);

        if( napivLeftWdList.isEmpty() || napivRightWdList.isEmpty() )
          return null;
        
        List<AnalyzedToken> napivLeftAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(napivMatcher.group(1), napivLeftWdList);
        List<AnalyzedToken> napivRightAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(napivMatcher.group(2), napivRightWdList);

        List<AnalyzedToken> tagMatch = tagMatch(word, napivLeftAnalyzedTokens, napivRightAnalyzedTokens);
        if( tagMatch != null ) {
          return tagMatch;
        }
      }
    }

    Pattern TAGS_TO_REMOVE = Pattern.compile(":comp.|:&predic|:&insert");
    List<AnalyzedToken> leftAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(leftWord, leftWdList);
    
    // гірко-прегірко
    if( rightWord.startsWith("пре") && leftWordLowerCase.equals(rightWord.substring(3).toLowerCase()) ) {
      if (PosTagHelper.hasPosTagStart2(leftWdList, "adv")) {

        return leftAnalyzedTokens.stream()
            .filter(a -> a.getPOSTag() != null && a.getPOSTag().startsWith("adv") )
            .map(a -> new AnalyzedToken(word, TAGS_TO_REMOVE.matcher(a.getPOSTag()).replaceAll(""), word))
            .collect(Collectors.toList());
      }
      // гіркий-прегіркий
      else if( PosTagHelper.hasPosTagStart2(leftWdList, "adj") ) {

        return leftAnalyzedTokens.stream()
            .filter(a -> a.getPOSTag() != null && a.getPOSTag().startsWith("adj") )
            .map(a -> new AnalyzedToken(word, TAGS_TO_REMOVE.matcher(a.getPOSTag()).replaceAll(""), a.getLemma()+"-пре"+a.getLemma()))
            .collect(Collectors.toList());
      }
    }

    // Мустафа-ага
    if( NAME_SUFFIX.contains(rightWord)
        && PosTagHelper.hasPosTagPart(leftAnalyzedTokens, "name") ) {
      List<TaggedWord> wordList = PosTagHelper.adjust(leftWdList, null, "-" + rightWord);
      return ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(word, wordList);
    }

    if( leftWord.equals("аль") ) {
      String wd = "Аль-" + rightWord;
      List<TaggedWord> wdList = wordTagger.tag(wd);
      if( wdList.size() > 0 ) {
        wdList = PosTagHelper.adjust(wdList, null, null, ":bad");
        return ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(wd, wdList);
      }
    }

    if( rightWdList.isEmpty() ) {
      return null;
    }

    List<AnalyzedToken> rightAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(rightWord, rightWdList);

    // півгодини-годину
    if( word.startsWith("пів") 
        && PosTagHelper.hasPosTag(leftAnalyzedTokens, Pattern.compile("noun:inanim:p:v_...:nv.*")) ) {
      
      return rightAnalyzedTokens.stream()
          .filter(a -> a.getPOSTag() != null && a.getPOSTag().startsWith("noun:inanim:") )
          .map(a -> new AnalyzedToken(word, a.getPOSTag().replaceFirst(":[mfn]:", ":p:"), word))
          .collect(Collectors.toList());
      
    }

    if( leftWord.equalsIgnoreCase("по") ) {
      if( rightWord.endsWith("ому") ) {
        return poAdvMatch(word, rightAnalyzedTokens, ADJ_TAG_FOR_PO_ADV_MIS);
      }
      else if( SKYI_PATTERN.matcher(rightWord).matches() ) {
        return poAdvMatch(word, rightAnalyzedTokens, ADJ_TAG_FOR_PO_ADV_NAZ);
      }
      return null;
    }

    if( Character.isUpperCase(leftWord.charAt(0)) && Character.isUpperCase(rightWord.charAt(0)) ) {  
        // Київ-Прага
        if( PosTagHelper.hasPosTag(leftAnalyzedTokens, GEO_V_NAZ)
            && PosTagHelper.hasPosTag(rightAnalyzedTokens, GEO_V_NAZ) ) {
          return Arrays.asList(new AnalyzedToken(word, "noninfl:prop:geo", word));
        }
        // Хуана-Карлоса
        if( PosTagHelper.hasPosTag(leftAnalyzedTokens, FNAME)
            && PosTagHelper.hasPosTag(rightAnalyzedTokens, FNAME) ) {
          leftAnalyzedTokens = PosTagHelper.filter(leftAnalyzedTokens, Pattern.compile(".*fname.*"));
          rightAnalyzedTokens = PosTagHelper.filter(rightAnalyzedTokens, Pattern.compile(".*fname.*"));
          return tagMatch(word, leftAnalyzedTokens, rightAnalyzedTokens);
        }
        // подружжя Карпа-Хансен
        if( PosTagHelper.hasPosTag(leftAnalyzedTokens, LNAME_V_NAZ)
            && PosTagHelper.hasPosTag(rightAnalyzedTokens, LNAME_V_NAZ) ) {
          return Arrays.asList(new AnalyzedToken(word, "noninfl:prop:lname", word));
        }
        // Джеймса-Веніка
        if( PosTagHelper.hasPosTag(leftAnalyzedTokens, LNAME_V_ROD)
            && PosTagHelper.hasPosTag(rightAnalyzedTokens, LNAME_V_ROD) ) {
          return Arrays.asList(new AnalyzedToken(word, "noninfl:prop:lname", word));
        }
        // bad: Квітки-Основ'яненко
        if( PosTagHelper.hasPosTag(leftAnalyzedTokens, NAME)
            && PosTagHelper.hasPosTag(rightAnalyzedTokens, NAME) ) {
          return null;
        }
        // Україна-ЄС
        if( PosTagHelper.hasPosTag(leftAnalyzedTokens, PROP_V_NAZ)
            && PosTagHelper.hasPosTag(rightAnalyzedTokens, PROP_V_NAZ) ) {
          return Arrays.asList(new AnalyzedToken(word, "noninfl:prop", word));
        }
    }

    // exclude: Малишко-це, відносини-коли

//    List<AnalyzedToken> leftAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(leftWord, leftWdList);

    // був-би, but not м-б
    if( leftWord.length() > 1 && BAD_SUFFIX.contains(rightWord) ) {
      List<TaggedWord> wordList = PosTagHelper.adjust(leftWdList, null, "-" + rightWord);
      wordList = PosTagHelper.addIfNotContains(leftWdList, ":bad", null);
      List<AnalyzedToken> tagged = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(word, wordList);
      return tagged;
    }

    if( leftWord.equalsIgnoreCase(rightWord)
        && leftAnalyzedTokens.size() > 0
        && LemmaHelper.hasLemma(leftAnalyzedTokens, Pattern.compile("[ув]?весь|[ву]с[еі]")) ) {
      List<AnalyzedToken> tagMatch = tagMatch(word, leftAnalyzedTokens, rightAnalyzedTokens);
      if( tagMatch != null ) {
        return tagMatch.stream()
          .filter(m -> equalParts(m.getLemma()) )
          .collect(Collectors.toList());
      }
    }

    
    if( PosTagHelper.hasPosTagPart(leftAnalyzedTokens, "&pron")
        && ! PosTagHelper.hasPosTagPart(leftAnalyzedTokens, "numr") )
      return null;

    if( ! leftWord.equalsIgnoreCase(rightWord) && PosTagHelper.hasPosTag(rightAnalyzedTokens, Pattern.compile("(part|conj).*|.*?:&pron.*")) 
        && ! (PosTagHelper.hasPosTagStart(leftAnalyzedTokens, "numr") && PosTagHelper.hasPosTagStart(rightAnalyzedTokens, "numr")) )
      return null;

    List<AnalyzedToken> adjCompounds = new ArrayList<>();
    if( leftWord.matches("[А-ЯІЇЄҐa-zA-Zα-ωΑ-Ω]|[a-zA-Z-]+") ) {
        if( PosTagHelper.hasPosTag(rightAnalyzedTokens, Pattern.compile("adj(?!.*(pron|bad|slang|arch)).*")) ) {
          adjCompounds = generateTokensWithRighInflected(word, leftWord, rightAnalyzedTokens, IPOSTag.adj.getText(), null, Pattern.compile(":comp."));
        }
    }

    // майстер-класу
    
    if( dashPrefixMatch 
        && ! ( leftWord.equalsIgnoreCase("міді") && LemmaHelper.hasLemma(rightAnalyzedTokens, Arrays.asList("бронза"))) ) {

      List<AnalyzedToken> newTokens = new ArrayList<>();
//      if( leftWord.length() == 1 && leftWord.matches("[a-zA-Zα-ωΑ-Ω]") ) {
//        List<AnalyzedToken> newTokensAdj = getNvPrefixLatWithAdjMatch(word, rightAnalyzedTokens, leftWord);
//        if( newTokensAdj != null ) {
//          newTokens.addAll(newTokensAdj);
//        }
//      }
      
      String extraTag = "";
      boolean lowerCased = false;
      if( dashPrefixes.containsKey( leftWord ) ) {
        extraTag = dashPrefixes.get(leftWord);
      }
      else { 
        if( dashPrefixes.containsKey( leftWordLowerCase ) ) {
          extraTag = dashPrefixes.get(leftWordLowerCase);
          if( leftWordLowerCase.matches("[а-яіїєґ']+") ) { // Інтернет-пошуковик
            lowerCased = true;
          }
        }
      }
      
      List<AnalyzedToken> newTokensNoun = getNvPrefixNounMatch(word, rightAnalyzedTokens, lowerCased ? leftWordLowerCase : leftWord, extraTag);
      if( newTokensNoun != null ) {
        newTokens.addAll(newTokensNoun);
      }
      
      // топ-десять
      if( leftWord.equalsIgnoreCase("топ") && PosTagHelper.hasPosTagPart(rightAnalyzedTokens, "numr:") ) {
        return generateTokensWithRighInflected(word, leftWord, rightAnalyzedTokens, "numr:", ":bad", null);
      }

      if( newTokens.isEmpty() ) {
        newTokens.addAll(adjCompounds);
      }
      
      return newTokens;
    }

    if( adjCompounds.size() > 0 )
      return adjCompounds;
    
    // пів-України

    if( Character.isUpperCase(rightWord.charAt(0)) ) {
      if (word.startsWith("пів-")) {
        List<AnalyzedToken> newAnalyzedTokens = addPluralNvTokens(word, rightAnalyzedTokens, ":ua_1992");
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

        // Жінка-Актриса
        if( PosTagHelper.hasPosTag(leftAnalyzedTokens, Pattern.compile("noun(?!.prop).*")) 
              && PosTagHelper.hasPosTag(rightAnalyzedTokens, Pattern.compile("noun(?!.prop).*")) ) {
            // flow-through
        }
        else {
          return null;
        }
      }
    }

    // don't allow: Донець-кий, зовнішньо-економічний, мас-штаби

    // allow га-га!

    List<AnalyzedToken> noDashAnalyzedTokens = new ArrayList<>();
    
    boolean hasIntj = PosTagHelper.hasPosTagStart(leftAnalyzedTokens, "intj");
    if( ! hasIntj ) {
      String noDashWord = word.replace("-", "");
      List<TaggedWord> noDashWordList = tagAsIsAndWithLowerCase(noDashWord);
      noDashAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(noDashWord, noDashWordList);
    }


    // вгору-вниз, лікар-гомеопат, жило-було

    if( noDashAnalyzedTokens.isEmpty() ) {
      if( ! leftWdList.isEmpty() && (leftWord.length() > 2 || hasIntj) ) {
        List<AnalyzedToken> tagMatch = tagMatch(word, leftAnalyzedTokens, rightAnalyzedTokens);
        if( tagMatch != null ) {
          return tagMatch;
        }
      }
    }

    List<AnalyzedToken> match = tryOWithAdj(word, leftWord, rightAnalyzedTokens);
    if( match != null )
      return match;

    compoundDebugLogger.logUnknownCompound(word);
    
    return null;
  }


  private List<AnalyzedToken> addPluralNvTokens(String word, List<AnalyzedToken> rightAnalyzedTokens, String addTag) {
    List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>(rightAnalyzedTokens.size());
    
    for (AnalyzedToken rightAnalyzedToken : rightAnalyzedTokens) {
      String rightPosTag = rightAnalyzedToken.getPOSTag();
      if( rightPosTag != null && NOUN_SING_V_ROD_REGEX.matcher(rightPosTag).matches() ) {
        addPluralNvTokens(word, newAnalyzedTokens, rightPosTag, addTag);
      }
    }
    return newAnalyzedTokens;
  }


  private void addPluralNvTokens(String word, List<AnalyzedToken> newAnalyzedTokens, String rightPosTag, String addTag) {
    for(String vid: PosTagHelper.VIDMINKY_MAP.keySet()) {
      if( vid.equals("v_kly") )
        continue;

      String posTag = rightPosTag.replace("v_rod", vid).replaceFirst(":[mfn]:v_", ":p:v_") + ":nv" + addTag;
      AnalyzedToken token = new AnalyzedToken(word, posTag, word);
      if( ! newAnalyzedTokens.contains(token) ) {
        newAnalyzedTokens.add(token);
      }
    }
  }

  private static boolean equalParts(String lemma) {
    if( ! lemma.contains("-") )
      return false;
    String[] parts = lemma.split("-", 2);
    return parts[0].equals(parts[1]);
  }


  private List<TaggedWord> tagEitherCase(String word) {
    if( word.isEmpty() )
      return new ArrayList<>();
    
    List<TaggedWord> rightWdList = wordTagger.tag(word);
    if( rightWdList.isEmpty() ) {
      if( Character.isUpperCase(word.charAt(0)) ) {
        rightWdList = wordTagger.tag(word.toLowerCase());
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
          && PosTagHelper.hasPosTag2(rightWdList, INTJ_PATTERN)
          || PosTagHelper.hasPosTag2(leftWdList, ONOMAT_PATTERN)
          && PosTagHelper.hasPosTag2(rightWdList, ONOMAT_PATTERN)) {
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

    if( parts.length == 3 ) {
        Set<AnalyzedToken> tokens = generateEntities(word);
        if( tokens.size() > 0 )
          return new ArrayList<>(tokens);
    }

    
    // filter out г-г-г
    if( parts.length >= 3 && set.size() > 1
        && ! dashPrefixes.containsKey(parts[0])
        && ! dashPrefixesInvalid.contains(parts[0]) ) {


      // ва-ре-ни-ки
      String merged = word.replace("-", "");
      List<TaggedWord> tagged = tagBothCases(merged,  null);
      tagged = PosTagHelper.filter2Negative(tagged, ABBR_PATTERN);
      if( ! tagged.isEmpty() ) {
        return ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(word, PosTagHelper.addIfNotContains(tagged, ":alt"));
      }

      // ду-у-у-же
      merged = collapseStretch(word);
      tagged = tagBothCases(merged, null);
      tagged = PosTagHelper.filter2Negative(tagged, ABBR_PATTERN);
      if( ! tagged.isEmpty() ) {
        return ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(word, PosTagHelper.addIfNotContains(tagged, ":alt"));
      }
    }

    return null;
  }

  private final static Pattern ABBR_PATTERN = Pattern.compile(".*abbr.*");
  private final static Pattern STRETCH_PATTERN = Pattern.compile("([а-іяїєґА-ЯІЇЄҐ])\\1*-\\1+");
  
  private static String collapseStretch(String word) {
    boolean capitalized = StringTools.isCapitalizedWord(word);
    String merged = STRETCH_PATTERN.matcher(word.toLowerCase()).replaceAll("$1");
    merged = STRETCH_PATTERN.matcher(merged).replaceAll("$1");
    merged = merged.replace("-", "");
    if( capitalized ) {
      merged = StringUtils.capitalize(merged);
    }
    return merged;
  }


  private List<AnalyzedToken> doGuessTwoHyphens(String word, int firstDashIdx, int dashIdx) {
    String[] parts = word.split("-");

    List<TaggedWord> rightWdList = tagEitherCase(parts[2]);

    if( rightWdList.isEmpty() )
      return null;

    List<AnalyzedToken> rightAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(parts[2], rightWdList);

    String firstAndSecond = parts[0] + "-" + parts[1];

    boolean twoDash = false;
    String extraTag = "";
    if( dashPrefixes.containsKey(firstAndSecond) ) {
      extraTag = dashPrefixes.get(firstAndSecond);
      twoDash = true;
    }
    else if( dashPrefixes.containsKey( firstAndSecond.toLowerCase() ) ) {
      extraTag = dashPrefixes.get(firstAndSecond.toLowerCase());
      twoDash = true;
    }

    if( twoDash ) {
      return getNvPrefixNounMatch(word, rightAnalyzedTokens, firstAndSecond, extraTag);
    }


    List<TaggedWord> secondWdList = tagEitherCase(parts[1]);
    
    // try full match - only adj for now - nouns are complicated
    if( PosTagHelper.hasPosTagStart2(secondWdList, "adj") ) {
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


  private static List<AnalyzedToken> generateTokensWithRighInflected(String word, String leftWord, List<AnalyzedToken> rightAnalyzedTokens, String posTagStart, String addTag, Pattern dropTag) {
    List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>(rightAnalyzedTokens.size());
    for (AnalyzedToken analyzedToken : rightAnalyzedTokens) {
      String posTag = analyzedToken.getPOSTag();
      if( posTag.startsWith( posTagStart )
            && ! posTag.contains("v_kly") ) {
        if( dropTag != null ) {
          posTag = dropTag.matcher(posTag).replaceAll("");
        }
        posTag = PosTagHelper.addIfNotContains(posTag, addTag);
        newAnalyzedTokens.add(new AnalyzedToken(word, posTag, leftWord + "-" + analyzedToken.getLemma()));
      }
    }
    return newAnalyzedTokens;
  }


  private List<AnalyzedToken> matchNumberedProperNoun(String word, String leftWord, String rightWord) {

    // Ан-140
    if( NOUN_SUFFIX_NUMBER_LETTER.matcher(rightWord).matches() ) {
      Set<AnalyzedToken> newAnalyzedTokens = generateEntities(word);

      if (newAnalyzedTokens.size() > 0)
        return new ArrayList<>(newAnalyzedTokens);
    }

    // Вибори-2014
    if (YEAR_NUMBER.matcher(rightWord).matches()) {
      List<TaggedWord> leftWdList = tagAsIsAndWithLowerCase(leftWord);

      if (!leftWdList.isEmpty() /*&& Character.isUpperCase(leftWord.charAt(0))*/) {
        List<AnalyzedToken> leftAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(leftWord, leftWdList);

        List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>();

        boolean isUppercase = Character.isUpperCase(leftWord.charAt(0));
        for (AnalyzedToken analyzedToken : leftAnalyzedTokens) {
          if (!PosTagHelper.hasPosTagPart(analyzedToken, ":prop")
              && !WORDS_WITH_YEAR.contains(analyzedToken.getLemma()))
            continue;

          String posTag = analyzedToken.getPOSTag();

          // only noun - відкидаємо: вибори - вибороти
          // Афіни-2014 - потрібне лише місто, не ім'я
          // TODO: чемпіон-2012
          if (posTag == null || ! posTag.startsWith("noun:inanim"))
            continue;

          if (posTag.contains("v_kly"))
            continue;

          if (posTag.contains(":p:") 
              && !Arrays.asList("гра", "бюджет").contains(analyzedToken.getLemma())
              && !posTag.contains(":ns"))
            continue;

          String lemma = analyzedToken.getLemma();
          posTag = posTag.replace(":geo", "");

          if (!posTag.contains(":prop")) {
            if( isUppercase ) {
              posTag += ":prop";
              lemma = StringUtils.capitalize(lemma);
            }
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


  Set<AnalyzedToken> generateEntities(String word) {
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
    return newAnalyzedTokens;
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

          // вбивство 15-ти селян
          if( "ти".equals(rightWord) 
              && Pattern.compile(".*([0569]|1[0-9])").matcher(leftWord).matches() ) {
            newAnalyzedTokens.add(new AnalyzedToken(word, "numr:p:v_rod:bad", leftWord));
            newAnalyzedTokens.add(new AnalyzedToken(word, "numr:p:v_dav:bad", leftWord));
            newAnalyzedTokens.add(new AnalyzedToken(word, "numr:p:v_mis:bad", leftWord));
            return newAnalyzedTokens;
          }
          // на 20-ці
          else if( "ці".equals(rightWord) 
              && Pattern.compile(".*([0569]|1[0-9])").matcher(leftWord).matches() ) {
            newAnalyzedTokens.add(new AnalyzedToken(word, "numr:f:v_dav:bad", leftWord));
            newAnalyzedTokens.add(new AnalyzedToken(word, "numr:f:v_mis:bad", leftWord));
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

      if( leftPosTag.startsWith("noun:inanim") ) {
        // we don't want to have v_kly for рибо-полювання
        // but we do for пане-товаришу
        if( leftPosTag.contains("v_kly") )
          continue;
      }

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

        if( rightPosTag.startsWith("noun:inanim") ) {
          if (rightPosTag.contains("v_kly"))
            continue;
          // skip Гірник geo for Гірник-спорт
          if( leftPosTag.contains(":geo") 
              && ! rightPosTag.contains(":geo")
              && ! rightAnalyzedToken.getLemma().matches("(?iu)ріка|гора|місто|град|поле|море|парк") )
            continue;
        }

        // країни-агресори - не треба v_zna:rare
        if( rightPosTag.startsWith("noun:anim:p:v_zna:rare")
            && leftPosTag.startsWith("noun:inanim") )
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
            || (IPOSTag.startsWith(leftPosTag, IPOSTag.intj, IPOSTag.onomat) 
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
        // numr-numr: один-три
        else if ( leftPosTag.startsWith(IPOSTag.numr.getText()) && rightPosTag.startsWith(IPOSTag.numr.getText()) ) {
            String agreedPosTag = getNumAgreedPosTag(leftPosTag, rightPosTag, leftNv);
            if( agreedPosTag != null ) {
              
              if( rightPosTag.contains(":p:") && ! agreedPosTag.contains(":p:") ) {
                agreedPosTag = agreedPosTag.replaceFirst(":[mfn]:", ":p:");
              }
              
              newAnalyzedTokens.add(new AnalyzedToken(word, agreedPosTag + extraNvTag + leftPosTagExtra, leftAnalyzedToken.getLemma() + "-" + rightAnalyzedToken.getLemma()));
            }
        }
        // noun-numr match
        else if ( IPOSTag.startsWith(leftPosTag, IPOSTag.noun) && IPOSTag.startsWith(rightPosTag, IPOSTag.numr) ) {
          if( ! leftAnalyzedToken.getLemma().equals("п'ята") ) {
            // gender tags match
            String leftGenderConj = PosTagHelper.getGenderConj(leftPosTag);
            if( leftGenderConj != null && leftGenderConj.equals(PosTagHelper.getGenderConj(rightPosTag)) ) {
              newAnalyzedTokens.add(new AnalyzedToken(word, leftPosTag + extraNvTag + leftPosTagExtra, leftAnalyzedToken.getLemma() + "-" + rightAnalyzedToken.getLemma()));
              // година-півтори може бути як одниною так і множиною: минула година-півтори, минули година-півтори
              if( ! leftPosTag.contains(":p:") ) {
                newAnalyzedTokens.add(new AnalyzedToken(word, leftPosTag.replaceAll(":[mfn]:", ":p:") + extraNvTag + leftPosTagExtra, leftAnalyzedToken.getLemma() + "-" + rightAnalyzedToken.getLemma()));
              }
            }
            else {
              // (with different gender tags): сотні (:p:) - дві (:f:)
              String agreedPosTag = getNumAgreedPosTag(leftPosTag, rightPosTag, leftNv);
              if( agreedPosTag != null ) {
                newAnalyzedTokens.add(new AnalyzedToken(word, agreedPosTag + extraNvTag + leftPosTagExtra, leftAnalyzedToken.getLemma() + "-" + rightAnalyzedToken.getLemma()));
                // рік-два може бути як одниною так і множиною: минулий рік-два, минули рік-два
                if( ! agreedPosTag.contains(":p:") ) {
                  newAnalyzedTokens.add(new AnalyzedToken(word, agreedPosTag.replaceAll(":[mfn]:", ":p:") + extraNvTag + leftPosTagExtra, leftAnalyzedToken.getLemma() + "-" + rightAnalyzedToken.getLemma()));
                }
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
        // чарка-друга
        else if( leftPosTag.startsWith(IPOSTag.noun.getText()) 
                && rightAnalyzedToken.getLemma().equals("другий")
                ) {
          String leftGenderConj = PosTagHelper.getGenderConj(leftPosTag);
          if( leftGenderConj != null && leftGenderConj.equals(PosTagHelper.getGenderConj(rightPosTag)) ) {
            String rightLemma = leftGenderConj.startsWith("m") ? "другий" :
              leftGenderConj.startsWith("f") ? "друга" : "друге";
            newAnalyzedTokens.add(new AnalyzedToken(word, leftPosTag + extraNvTag + leftPosTagExtra, leftAnalyzedToken.getLemma() + "-" + rightLemma));
          }
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
            // yes for вчителька-педагог
            // no for піт-стопа
            if( word.length() < 10 )
              return null;
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
    else if ( followerSet.contains(rightLemma) ) {
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
    else if ( followerSet.contains(leftLemma) ) {
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
      if( taggedWords.isEmpty() && leftWord.length() > 4 ) {
        taggedWords = tagBothCases(leftBase, Pattern.compile("^noun.*"));         // паталог для паталого-анатомічний
      }
      if( taggedWords.isEmpty() ) {
        // два для дво-триметровий, етико-філологічний
        taggedWords = tagBothCases(leftBase + "а", Pattern.compile("(noun:inanim:f:v_naz|numr).*"));   
      }
      if( taggedWords.isEmpty() )
        return null;

      // важконапрацьований - разом
      if(! extraTag.equals(":bad")) {
        List<AnalyzedToken> allCapTokens = ukrainianTagger.analyzeAllCapitamizedAdj(word);

        if (taggedWords.get(0).getPosTag().startsWith(IPOSTag.adv.getText()) && PosTagHelper.hasPosTagPart(analyzedTokens, "adjp")) {
          extraTag = ":bad";
        }
        else if ( PosTagHelper.hasPosTagPart2(taggedWords, ":bad")) {
          extraTag = ":bad";
        }
        // багато..., мало.... пишуться разом
        else if( LEFT_O_ADJ_INVALID.contains(leftWord.toLowerCase()) ) {
          // do not mark Центрально-Східної as :bad
          if( allCapTokens.isEmpty() ) {
            extraTag = ":bad";
          }
        }
        else {
          // do not mark Івано-Франківської as :bad
          if( allCapTokens.size() == 0 ) {
            // марк високо-продуктивний as :bad
            String noDashWord = word.replace("-", "");
            List<TaggedWord> noDashWordList = tagAsIsAndWithLowerCase(noDashWord);
            List<AnalyzedToken> noDashAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(noDashWord, noDashWordList);

            if( ! noDashAnalyzedTokens.isEmpty() ) {
              extraTag = ":bad";
            }
          }
        }
      }
    }

    for (AnalyzedToken analyzedToken : analyzedTokens) {
      String posTag = analyzedToken.getPOSTag();
      if( posTag.startsWith( IPOSTag.adj.getText() ) ) {
        if( posTag.contains(":comp") ) {
          posTag = PosTagHelper.ADJ_COMP_REGEX.matcher(posTag).replaceFirst("");
        }
        if( extraTag.contains(":bad") ) {
          posTag = posTag.replace(":arch", "");
        }
        posTag = PosTagHelper.addIfNotContains(posTag, extraTag);

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
      if( ! PosTagHelper.hasPosTagStart2(taggedWords, "numr") )
        return null;

      String leftWordLowerCase = leftWord.toLowerCase();

      // двох-трьохметровий - bad
      if( leftWordLowerCase.matches(".*?(двох|трьох|чотирьох)") ) {
        //        taggedWords = wordTagger.tag("два");
        extraTag = ":bad";
      }
      // три-метровий - bad
      else if( analyzedTokens.size() > 0 
          && ! analyzedTokens.get(0).getToken().matches("(?iu)(дво|три|чотири|п'яти|шести|семи|вісьми|двох|трьох|чотирьох).+") ) {
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
          String newLemma = leftWordLowerCase + "-" + analyzedToken.getLemma();
          newAnalyzedTokens.add(new AnalyzedToken(word, posTag, newLemma));
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
  private static List<AnalyzedToken> getNvPrefixNounMatch(String word, List<AnalyzedToken> analyzedTokens, String leftWord, String extraTag) {
    List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>(analyzedTokens.size());

    for (AnalyzedToken analyzedToken : analyzedTokens) {
      String posTag = analyzedToken.getPOSTag();
      if( posTag.startsWith(IPOSTag.noun.getText() )
          && ! posTag.contains("v_kly") ) {

//        if( Arrays.asList("В2В", "АІ", "комьюніті", "пресс").contains(leftWord) ) {
//            posTag = PosTagHelper.addIfNotContains(posTag, ":bad");
//        }

        // міні-БПЛА - ok for ua_2019 too
        if( ! extraTag.equals(":ua_1992") || ! Character.isUpperCase(analyzedToken.getLemma().charAt(0)) ) {
          if( StringUtils.isNotEmpty(extraTag) ) {
            posTag = PosTagHelper.addIfNotContains(posTag, extraTag);
          }
        }

        String newLemma = leftWord + "-" + analyzedToken.getLemma();
        newAnalyzedTokens.add(new AnalyzedToken(word, posTag, newLemma));
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
    return word.substring(0, 1).toUpperCase(conversionLocale) + word.substring(1);
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

  
  @Nullable
  List<AnalyzedToken> guessOtherTags(String word) {
    List<AnalyzedToken> guessedTokens = guessOtherTagsInternal(word);
    compoundDebugLogger.logTaggedCompound(guessedTokens);
    return guessedTokens;
  }
  
  @Nullable
  private List<AnalyzedToken> guessOtherTagsInternal(String word) {
    if( word.length() <= 7 
        || ! UKR_LETTERS_PATTERN.matcher(word).matches() )
      return null;

    if( StringTools.isCapitalizedWord(word) ) {

      if (word.endsWith("штрассе")
          || word.endsWith("штрасе")) {
        String addPos = word.endsWith("штрассе") ? ":alt" : "";
        return PosTagHelper.generateTokensForNv(word, "f", ":prop" + addPos);
      }

      if (word.endsWith("дзе")
          || word.endsWith("швілі")
          || word.endsWith("іані") ) {
        return PosTagHelper.generateTokensForNv(word, "mf", ":prop:lname");
      }
      
    }
    
    String lowerCase = word.toLowerCase();
    for(String prefix: noDashPrefixes) {

      if( ! lowerCase.startsWith(prefix) )
        continue;

      String right = word.substring(prefix.length(), word.length());

      String apo = "";
      List<String> addTag = new ArrayList<>();

      if( right.startsWith("'") ) { 
        right = right.substring(1);
        apo = "'";
      }
      
      if( right.length() < 2 )
        continue;

      boolean apoNeeded = false;
      if( "єїюя".indexOf(right.charAt(0)) != -1
          && "аеєиіїоуюя".indexOf(prefix.charAt(prefix.length()-1)) == -1) {
        apoNeeded = true;
      }
      // екс'прес
      if( ! apoNeeded && ! apo.isEmpty() ){
        break;
      }

      if( apoNeeded == apo.isEmpty() ){
        addTag.add(":bad");
      }
      if( noDashPrefixes2019.contains(prefix) ) {
        addTag.add(":ua_2019");
      }

      if( right.length() >= 4 && ! StringTools.isCapitalizedWord(right) ) {
        List<TaggedWord> rightWdList = wordTagger.tag(right);
        rightWdList = PosTagHelper.filter2(rightWdList, PREFIX_NO_DASH_POSTAG_PATTERN);
        rightWdList.removeIf(w -> w.getPosTag().startsWith("noun:inanim") && w.getPosTag().contains("v_kly"));

        if( rightWdList.size() > 0 ) {
          rightWdList = PosTagHelper.adjust(rightWdList, prefix+apo, null, addTag.toArray(new String[0]));

          List<AnalyzedToken> compoundTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(word, rightWdList);
          return compoundTokens;
        }
      }
    }
  
    return null;
  }

}