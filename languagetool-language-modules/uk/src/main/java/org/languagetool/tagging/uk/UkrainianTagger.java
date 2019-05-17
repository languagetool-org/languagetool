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
import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.AnalyzedToken;
import org.languagetool.tagging.BaseTagger;
import org.languagetool.tagging.TaggedWord;
import org.languagetool.tagging.WordTagger;
import org.languagetool.tools.StringTools;

/** 
 * Ukrainian part-of-speech tagger.
 * See README for details, the POS tagset is described in tagset.txt
 * 
 * @author Andriy Rysin
 */
public class UkrainianTagger extends BaseTagger {

  private static final Pattern NUMBER = Pattern.compile("[+-±]?[€₴\\$]?[0-9]+(,[0-9]+)?([-–—][0-9]+(,[0-9]+)?)?(%|°С?)?|\\d{1,3}([\\s\u00A0\u202F]\\d{3})+");
  // full latin number regex: M{0,4}(CM|CD|D?C{0,3})(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})
  private static final Pattern LATIN_NUMBER = Pattern.compile("(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})");
  private static final Pattern LATIN_NUMBER_CYR = Pattern.compile("[IXІХ]|[IІ]V|V?[IІ]{0,3}");

  private static final Pattern DATE = Pattern.compile("[\\d]{2}\\.[\\d]{2}\\.[\\d]{4}");
  private static final Pattern TIME = Pattern.compile("([01]?[0-9]|2[0-3])[.:][0-5][0-9]");
  private static final Pattern ALT_DASHES_IN_WORD = Pattern.compile("[а-яіїєґ0-9a-z]\u2013[а-яіїєґ]|[а-яіїєґ]\u2013[0-9]", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern NAPIV_ALLOWED_TAGS_REGEX = Pattern.compile("(noun|ad(j|v(?!p))(?!.*?:comp[cs])).*");
  private static final Pattern NAPIV_REMOVE_TAGS_REGEX = Pattern.compile(":comp.|:&adjp(:(actv|pasv|perf|imperf))*");

  private final CompoundTagger compoundTagger = new CompoundTagger(this, wordTagger, conversionLocale);
//  private BufferedWriter taggedDebugWriter;

  @Override
  public String getManualAdditionsFileName() {
    return "/uk/added.txt";
  }

  public UkrainianTagger() {
    super("/uk/ukrainian.dict", new Locale("uk", "UA"), false);
  }

  @Override
  public List<AnalyzedToken> additionalTags(String word, WordTagger wordTagger) {
    if ( NUMBER.matcher(word).matches() ) {
      List<AnalyzedToken> additionalTaggedTokens = new ArrayList<>();
      additionalTaggedTokens.add(new AnalyzedToken(word, IPOSTag.number.getText(), word));
      return additionalTaggedTokens;
    }

    if ( LATIN_NUMBER.matcher(word).matches() ) {
      List<AnalyzedToken> additionalTaggedTokens = new ArrayList<>();
      additionalTaggedTokens.add(new AnalyzedToken(word, IPOSTag.number.getText()+":latin", word));
      return additionalTaggedTokens;
    }

    if ( LATIN_NUMBER_CYR.matcher(word).matches() ) {
      List<AnalyzedToken> additionalTaggedTokens = new ArrayList<>();
      additionalTaggedTokens.add(new AnalyzedToken(word, IPOSTag.number.getText()+":latin:bad", word));
      return additionalTaggedTokens;
    }

    if ( TIME.matcher(word).matches() ) {
      List<AnalyzedToken> additionalTaggedTokens = new ArrayList<>();
      additionalTaggedTokens.add(new AnalyzedToken(word, IPOSTag.time.getText(), word));
      return additionalTaggedTokens;
    }

    if ( DATE.matcher(word).matches() ) {
      List<AnalyzedToken> additionalTaggedTokens = new ArrayList<>();
      additionalTaggedTokens.add(new AnalyzedToken(word, IPOSTag.date.getText(), word));
      return additionalTaggedTokens;
    }

    if ( word.indexOf('-') > 0 ) {
      List<AnalyzedToken> guessedCompoundTags = compoundTagger.guessCompoundTag(word);
      return guessedCompoundTags;
    }

    return guessOtherTags(word);
  }

  private List<AnalyzedToken> guessOtherTags(String word) {
    if( word.length() > 7
        && StringTools.isCapitalizedWord(word)
        && (word.endsWith("штрассе")
        || word.endsWith("штрасе")) ) {
      return PosTagHelper.generateTokensForNv(word, "f", ":prop");
    }

    return null;
  }


  @Override
  protected List<AnalyzedToken> getAnalyzedTokens(String word) {
    List<AnalyzedToken> tokens = super.getAnalyzedTokens(word);

    if( tokens.get(0).hasNoTag() ) {
      String origWord = word;

      if( word.endsWith("м²") ||  word.endsWith("м³") ) {
        word = origWord.substring(0, word.length()-1);
        List<AnalyzedToken> newTokens = getAdjustedAnalyzedTokens(origWord, word, Pattern.compile("noun:inanim.*"), null, null);
        return newTokens.size() > 0 ? newTokens : tokens; 
      }

      
      if( word.indexOf('\u2013') > 0
           && ALT_DASHES_IN_WORD.matcher(word).find() ) {

        word = origWord.replace('\u2013', '-');

        List<AnalyzedToken> newTokens = getAdjustedAnalyzedTokens(origWord, word, null, null, null);

        if( newTokens.size() > 0 ) {
          tokens = newTokens;
        }
      }
      
      if( word.length() > 7 && word.startsWith("напів") ) {
        String addPosTag = "";

        Matcher matcher = Pattern.compile("(напів['-]?)(.*)").matcher(word);
        matcher.matches();

        String prefix = matcher.group(1);
        String adjustedWord = matcher.group(2);

        List<AnalyzedToken> newTokens = getAdjustedAnalyzedTokens(origWord, adjustedWord, NAPIV_ALLOWED_TAGS_REGEX, null, null);

        if( newTokens.size() > 0 ) {
          if( ! addPosTag.contains(":bad:") ) {
            if( word.charAt(5) == '-'
                && ! adjustedWord.matches("[А-ЯІЇЄҐ].*") ) {
              addPosTag += ":bad";
            }
            else if( word.charAt(5) != '\''
                && adjustedWord.matches("[єїюя].*") ) {
              addPosTag += ":bad";
            }
          }

          for (int i = 0; i < newTokens.size(); i++) {
            AnalyzedToken analyzedToken = newTokens.get(i);

            String lemma = analyzedToken.getLemma();
            String posTag = analyzedToken.getPOSTag();

            posTag = NAPIV_REMOVE_TAGS_REGEX.matcher(posTag).replaceAll("");

            posTag = PosTagHelper.addIfNotContains(posTag, addPosTag);

            AnalyzedToken newToken = new AnalyzedToken(origWord, posTag, prefix+lemma);
            newTokens.set(i, newToken);
          }
          tokens = newTokens;
        }
      }
      // try г instead of ґ
      else if( word.contains("ґ") ) {
        tokens = convertTokens(tokens, word, "ґ", "г", ":alt");
      }
      else if( word.contains("ія") ) {
        tokens = convertTokens(tokens, word, "ія", "іа", ":alt");
      }
      else if( word.endsWith("тер") ) {
        tokens = convertTokens(tokens, word, "тер", "тр", ":alt");
      }
    }

    // try УКРАЇНА as Україна and СИРІЮ as Сирію
    if( word.length() > 2 && StringUtils.isAllUpperCase(word) ) {

      String newWord = StringUtils.capitalize(StringUtils.lowerCase(word));

      List<AnalyzedToken> newTokens = getAdjustedAnalyzedTokens(word, newWord, Pattern.compile("noun.*?:prop.*"), null, null);
      if( newTokens.size() > 0 ) {
          if( tokens.get(0).hasNoTag() ) {
            //TODO: add special tags if necessary
            tokens = newTokens;
          }
          else {
            tokens.addAll(newTokens);
          }
        }
    }

    // Івано-Франківська as adj from івано-франківський
    if( word.indexOf('-') > 1 && ! word.endsWith("-") ) {
      String[] parts = word.split("-");
      if( isAllCapitalized(parts) ) {
        String lowerCasedWord = Stream.of(parts).map(String::toLowerCase).collect(Collectors.joining("-"));
        List<TaggedWord> wdList = wordTagger.tag(lowerCasedWord);
        if( PosTagHelper.hasPosTagPart2(wdList, "adj") ) {
          List<AnalyzedToken> analyzedTokens = asAnalyzedTokenListForTaggedWordsInternal(word, wdList);
          analyzedTokens = PosTagHelper.filter(analyzedTokens, Pattern.compile("adj.*"));
          if( tokens.get(0).hasNoTag() ) {
            tokens = analyzedTokens;
          }
          else {
            // compound tagging has already been performed and may have added tokens
            for(AnalyzedToken token: analyzedTokens) {
              if( ! tokens.contains(token) ) {
                tokens.add(token);
              }
            }
          }
        }
      }
    }

    return tokens;
  }


  private static boolean isAllCapitalized(String[] parts) {
    for (String string : parts) {
      if( ! StringTools.isCapitalizedWord(string) )
        return false;
    }
    return true;
  }

  private List<AnalyzedToken> convertTokens(List<AnalyzedToken> origTokens, String word, String str, String dictStr, String additionalTag) {
    String adjustedWord = word.replace(str, dictStr);

    List<AnalyzedToken> newTokens = getAdjustedAnalyzedTokens(word, adjustedWord, null, additionalTag,
        (lemma) -> lemma.replace(dictStr, str));
    
    if( newTokens.isEmpty() )
        return origTokens;

    return newTokens;
  }

  private List<AnalyzedToken> getAdjustedAnalyzedTokens(String word, String adjustedWord, Pattern posTagRegex, 
      String additionalTag, UnaryOperator<String> lemmaFunction) {

    List<AnalyzedToken> newTokens = super.getAnalyzedTokens(adjustedWord);

    if( newTokens.get(0).hasNoTag() )
      return new ArrayList<>();

    List<AnalyzedToken> derivedTokens = new ArrayList<>();

    for (int i = 0; i < newTokens.size(); i++) {
      AnalyzedToken analyzedToken = newTokens.get(i);
      String posTag = analyzedToken.getPOSTag();

      if( adjustedWord.equals(analyzedToken.getToken()) // filter out tokens with accents etc with null pos tag
          && (posTagRegex == null || posTagRegex.matcher(posTag).matches()) ) {
        
        String lemma = analyzedToken.getLemma();
        if( lemmaFunction != null ) {
          lemma = lemmaFunction.apply(lemma);
        }

        if( additionalTag != null ) {
          posTag = PosTagHelper.addIfNotContains(posTag, additionalTag);
        }

        AnalyzedToken newToken = new AnalyzedToken(word, posTag, lemma);
        derivedTokens.add(newToken);
      }
    }

    return derivedTokens;
  }


  List<AnalyzedToken> asAnalyzedTokenListForTaggedWordsInternal(String word, List<TaggedWord> taggedWords) {
    return super.asAnalyzedTokenListForTaggedWords(word, taggedWords);
  }

  // we need to expose this as some rules want to know if the word is in the dictionary
  public WordTagger getWordTagger() {
    return super.getWordTagger();
  }

}
