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
import java.util.regex.Pattern;

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
  
  // full latin number regex: M{0,4}(CM|CD|D?C{0,3})(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})
  static final Pattern NUMBER = Pattern.compile("[+-±]?[€₴\\$]?[0-9]+(,[0-9]+)?([-–—][0-9]+(,[0-9]+)?)?(%|°С?)?|\\d{1,3}([\\s\u00A0\u202F]\\d{3})+|(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})");
  
  private static final Pattern DATE = Pattern.compile("[\\d]{2}\\.[\\d]{2}\\.[\\d]{4}");
  private static final Pattern TIME = Pattern.compile("([01]?[0-9]|2[0-3])[.:][0-5][0-9]");
  private static final Pattern ALT_DASHES_IN_WORD = Pattern.compile("[а-яіїєґ0-9a-z]\u2013[а-яіїєґ]|[а-яіїєґ]\u2013[0-9]", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  
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
    
    if ( word.indexOf('-') != -1 ) {
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

    if( tokens.get(0).getPOSTag() == null ) {
      char otherHyphen = getOtherHyphen(word);
      if( otherHyphen != '\u0000'
           && ALT_DASHES_IN_WORD.matcher(word).find() ) {

        String newWord = word.replace(otherHyphen, '-');

        List<AnalyzedToken> newTokens = super.getAnalyzedTokens(newWord);

        for (int i = 0; i < newTokens.size(); i++) {
          AnalyzedToken analyzedToken = newTokens.get(i);
          if( newWord.equals(analyzedToken.getToken()) ) {
            String lemma = analyzedToken.getLemma();
    // we probably want the original lemma
//            if( lemma != null ) {
//              lemma = lemma.replace('-', otherHyphen);
//            }
            AnalyzedToken newToken = new AnalyzedToken(word, analyzedToken.getPOSTag(), lemma);
            newTokens.set(i, newToken);
          }
        }
        
        tokens = newTokens;
      }
      // try УКРАЇНА as Україна
      else if( StringUtils.isAllUpperCase(word) ) {
        String newWord = StringUtils.capitalize(StringUtils.lowerCase(word));
        List<AnalyzedToken> newTokens = super.getAnalyzedTokens(newWord);

        for (int i = 0; i < newTokens.size(); i++) {
          AnalyzedToken analyzedToken = newTokens.get(i);
          String lemma = analyzedToken.getLemma();
          AnalyzedToken newToken = new AnalyzedToken(word, analyzedToken.getPOSTag(), lemma);
          newTokens.set(i, newToken);
        }

        tokens = newTokens;
      }
    }

//    if( taggedDebugWriter != null && ! tkns.isEmpty() ) {
//      debug_tagged_write(tkns, taggedDebugWriter);
//    }
    
    return tokens;
  }

  private static char getOtherHyphen(String word) {
    if( word.indexOf('\u2013') != -1 )
      return '\u2013';
// we normalize \u2011 to \u002D in tokenizer
//    if( word.indexOf('\u2011') != -1 )
//      return '\u2011';
    
    return '\u0000';
  }

  List<AnalyzedToken> asAnalyzedTokenListForTaggedWordsInternal(String word, List<TaggedWord> taggedWords) {
    return super.asAnalyzedTokenListForTaggedWords(word, taggedWords);
  }
  
  // we need to expose this as some rules want to know if the word is in the dictionary
  public WordTagger getWordTagger() {
    return super.getWordTagger();
  }
  
}
