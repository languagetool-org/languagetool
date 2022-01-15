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
import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.languagetool.AnalyzedToken;
import org.languagetool.rules.uk.LemmaHelper;
import org.languagetool.tagging.BaseTagger;
import org.languagetool.tagging.TaggedWord;
import org.languagetool.tagging.WordTagger;
import org.languagetool.tokenizers.uk.UkrainianWordTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** 
 * Ukrainian part-of-speech tagger.
 * See README for details, the POS tagset is described in tagset.txt
 * 
 * @author Andriy Rysin
 */
public class UkrainianTagger extends BaseTagger {
  private static final Logger logger = LoggerFactory.getLogger(UkrainianTagger.class);

  private static final Pattern NUMBER = Pattern.compile("[-+±]?[0-9]+(,[0-9]+)?([-–—][0-9]+(,[0-9]+)?)?|\\d{1,3}([\\s\u00A0\u202F]\\d{3})+");
  // full latin number regex: M{0,4}(CM|CD|D?C{0,3})(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})
  private static final Pattern LATIN_NUMBER = Pattern.compile("(?=[MDCLXVI])M*(C[MD]|D?C*)(X[CL]|L?X*)(I[XV]|V?I*)");
  private static final Pattern LATIN_NUMBER_CYR = Pattern.compile("[IXІХV]{2,4}(-[а-яі]{1,4})?|[IXІХV](-[а-яі]{1,4})");
  private static final Pattern HASHTAG = Pattern.compile("#[а-яіїєґa-z_][а-яіїєґa-z0-9_]*", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);

  private static final Pattern DATE = Pattern.compile("[\\d]{2}\\.[\\d]{2}\\.[\\d]{4}");
  private static final Pattern TIME = Pattern.compile("([01]?[0-9]|2[0-3])[.:][0-5][0-9]");
  private static final Pattern ALT_DASHES_IN_WORD = Pattern.compile("[а-яіїєґ0-9a-z]\u2013[а-яіїєґ]|[а-яіїєґ]\u2013[0-9]", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern COMPOUND_WITH_QUOTES_REGEX = Pattern.compile("-[«\"„]");


  private final CompoundTagger compoundTagger = new CompoundTagger(this, wordTagger, locale);
//  private BufferedWriter taggedDebugWriter;

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

    if ( LATIN_NUMBER.matcher(word).matches() && ! word.matches("[MD]+") ) {
      List<AnalyzedToken> additionalTaggedTokens = new ArrayList<>();
      additionalTaggedTokens.add(new AnalyzedToken(word, "number:latin", word));
      return additionalTaggedTokens;
    }

    if ( LATIN_NUMBER_CYR.matcher(word).matches() ) {
      List<AnalyzedToken> additionalTaggedTokens = new ArrayList<>();
      additionalTaggedTokens.add(new AnalyzedToken(word, "number:latin:bad", word));
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

    if ( word.startsWith("#") && HASHTAG.matcher(word).matches() ) {
      List<AnalyzedToken> additionalTaggedTokens = new ArrayList<>();
      additionalTaggedTokens.add(new AnalyzedToken(word, IPOSTag.hashtag.getText(), word));
      return additionalTaggedTokens;
    }

    if ( word.length() >= 3 && word.indexOf('-') > 0 ) {

      // екс-«депутат»
      if( word.length() >= 6 && COMPOUND_WITH_QUOTES_REGEX.matcher(word).find() ) {
        String adjustedWord = word.replaceAll("[«»\"„“]", "");
        return getAdjustedAnalyzedTokens(word, adjustedWord, null, null, null);
      }

      try {
        List<AnalyzedToken> guessedCompoundTags = compoundTagger.guessCompoundTag(word);
        return guessedCompoundTags;
      }
      catch(Exception e) {
        logger.error("Failed to tag \"" + word + "\"", e);
        return new ArrayList<>();
      }
    }

    return compoundTagger.guessOtherTags(word);
  }

  @Override
  protected List<AnalyzedToken> getAnalyzedTokens(String word) {
    
    if( word.indexOf('`') > 0 ) {
      word = word.replace('`', '\'');
    }
    
    List<AnalyzedToken> tokens = super.getAnalyzedTokens(word);

    if( word.length() < 2 )
      return tokens;
    
    if( tokens.get(0).hasNoTag() ) {
      String origWord = word;

      if( word.lastIndexOf('м') == word.length()-2 
          && word.matches("([ксмнд]|мк)?м[23²³]") ) {
//        word = origWord.substring(0, word.length()-1);
//        List<AnalyzedToken> newTokens = getAdjustedAnalyzedTokens(origWord, word, Pattern.compile("noun:inanim.*"), null, null);
//        return newTokens.size() > 0 ? newTokens : tokens;
        return Arrays.asList(new AnalyzedToken(origWord, "noninfl", origWord));
      }

//      if( word.matches("[0-9]+[а-яїієґa-z]") ) {
//        return Arrays.asList(new AnalyzedToken(origWord, "noninfl", origWord));
//      }

      if( word.length() > 2 ) {
        if( word.indexOf('\u2013') > 0
            && ALT_DASHES_IN_WORD.matcher(word).find() ) {

          word = origWord.replace('\u2013', '-');

          List<AnalyzedToken> newTokens = super.getAnalyzedTokens(word);
//          List<AnalyzedToken> newTokens = getAdjustedAnalyzedTokens(origWord, word, null, null, null);

          if( newTokens.size() > 0 && ! newTokens.get(0).hasNoTag() ) {
            newTokens.add(new AnalyzedToken(origWord, null, null));
            tokens = newTokens;
          }
        }

//        String lowerWord = word.toLowerCase();
        
        // try г instead of ґ
        else if( word.contains("ґ") || word.contains("Ґ") ) {
          tokens = convertTokens(tokens, word, "ґ", "г", ":alt");
        }
        else if( word.contains("ія") ) {
          tokens = convertTokens(tokens, word, "ія", "іа", ":alt");
        }
        else if( word.endsWith("тер") ) {
          tokens = convertTokens(tokens, word, "тер", "тр", ":alt");
        }
        else if( word.contains("льо") ) {
          tokens = convertTokens(tokens, word, "льо", "ло", ":alt");
        }
        else if( word.startsWith("сьвя") ) {
          tokens = convertTokens(tokens, word, "сьвя", "свя", ":arch");
        }
        else if( word.startsWith("сьві") ) {
          tokens = convertTokens(tokens, word, "сьві", "сві", ":arch");
        }
        else if( word.contains("ьск") && ! word.endsWith("ская") && ! word.equals("Комсомольском")) {
          tokens = convertTokens(tokens, word, "ьск", "ьськ", ":bad");
        }

        if( tokens.get(0).hasNoTag() ) {
          if ( word.length() >= 3 ) {
            if ( word.length() >= 9 ) {
              Matcher matcher2 = CompoundTagger.LEFT_O_ADJ_INVALID_PATTERN.matcher(word);
              if (matcher2.matches()) {
                String prefix = matcher2.group(1);
                String adjustedWord = matcher2.group(2);
                List<AnalyzedToken> newTokens = getAdjustedAnalyzedTokens(word, adjustedWord, Pattern.compile("^adj.*"), null,
                    (lemma) -> prefix + lemma);
                if( ! newTokens.isEmpty() ) {
                  tokens = newTokens;
                }
              }
            }
            if( tokens.get(0).hasNoTag()
                && ! word.equalsIgnoreCase("ііі") ) {// often stands for Latin number
              Matcher matcher = Pattern.compile("([аеєиіїоуюя])\\1{2,}", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE).matcher(word);
              if( matcher.find() ) {
                String adjustedWord = matcher.replaceAll("$1");
                List<AnalyzedToken> newTokens = getAdjustedAnalyzedTokens(word, adjustedWord, Pattern.compile("(?!noun.*:prop).*"), ":alt",
                    (lemma) -> lemma);
                if( ! newTokens.isEmpty() ) {
                  tokens = newTokens;
                }
              }
            }
            if( tokens.get(0).hasNoTag() 
                && word.indexOf("[") != -1 && word.indexOf("]") != -1 
                && UkrainianWordTokenizer.WORDS_WITH_BRACKETS_PATTERN.matcher(word).find() ) {
              String adjustedWord = word.replace("[", "").replace("]", "");
              List<AnalyzedToken> newTokens = getAdjustedAnalyzedTokens(word, adjustedWord, null, ":alt",
                  (lemma) -> lemma);
              if( ! newTokens.isEmpty() ) {
                tokens = newTokens;
              }
            }
          }
        }
      }
    }

    // try УКРАЇНА as Україна and СИРІЮ as Сирію
    if( word.length() > 2 && LemmaHelper.isAllUppercaseUk(word) ) {

      String newWord = LemmaHelper.capitalizeProperName(word);

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
    List<AnalyzedToken> analyzedTokens = analyzeAllCapitamizedAdj(word);
    if( analyzedTokens.size() > 0 ) {
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

    return tokens;
  }


  protected List<AnalyzedToken> analyzeAllCapitamizedAdj(String word) {
    if( word.indexOf('-') > 1 && ! word.endsWith("-") ) {
      String[] parts = word.split("-");
      if( Stream.of(parts).allMatch(LemmaHelper::isCapitalized) ) {
        String lowerCasedWord = word.toLowerCase(); //Stream.of(parts).map(String::toLowerCase).collect(Collectors.joining("-"));
        List<TaggedWord> wdList = wordTagger.tag(lowerCasedWord);
        if( PosTagHelper.hasPosTagPart2(wdList, "adj") ) {
          List<AnalyzedToken> analyzedTokens = asAnalyzedTokenListForTaggedWordsInternal(word, wdList);
          analyzedTokens = PosTagHelper.filter(analyzedTokens, Pattern.compile("adj.*"));
          return analyzedTokens;
        }
      }
    }
    return new ArrayList<>();
  }


  private List<AnalyzedToken> convertTokens(List<AnalyzedToken> origTokens, String word, String str, String dictStr, String additionalTag) {
    String adjustedWord = word.replace(str, dictStr);
    if( str.length() == 1 ) {
        adjustedWord = adjustedWord.replace(str.toUpperCase(), dictStr.toUpperCase());
    }

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
