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

import org.languagetool.AnalyzedToken;
import org.languagetool.tagging.BaseTagger;
import org.languagetool.tagging.TaggedWord;
import org.languagetool.tagging.WordTagger;

/** 
 * Ukrainian part-of-speech tagger.
 * See README for details, the POS tagset is described in tagset.txt
 * 
 * @author Andriy Rysin
 */
public class UkrainianTagger extends BaseTagger {
  
  // full latin number regex: M{0,4}(CM|CD|D?C{0,3})(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})
  static final Pattern NUMBER = Pattern.compile("[+-±]?[€₴\\$]?[0-9]+(,[0-9]+)?([-–—][0-9]+(,[0-9]+)?)?(%|°С?)?|(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})");
  private static final Pattern DATE = Pattern.compile("[\\d]{2}\\.[\\d]{2}\\.[\\d]{4}");
  private static final Pattern TIME = Pattern.compile("([01]?[0-9]|2[0-3])[.:][0-5][0-9]");
  
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
    
    if ( word.contains("-") ) {
      List<AnalyzedToken> guessedCompoundTags = compoundTagger.guessCompoundTag(word);
      return guessedCompoundTags;
    }
    
    return null;
  }

  protected List<AnalyzedToken> getAnalyzedTokens(String word) {
    List<AnalyzedToken> tkns = super.getAnalyzedTokens(word);

    if( tkns.get(0).getPOSTag() == null ) {
      if( (word.indexOf('\u2013') != -1) 
           && word.matches(".*[а-яіїєґ][\u2013][а-яіїєґ].*")) {
        String newWord = word.replace('\u2013', '-');
        
        List<AnalyzedToken> newTokens = super.getAnalyzedTokens(newWord);
        
        for (int i = 0; i < newTokens.size(); i++) {
          AnalyzedToken analyzedToken = newTokens.get(i);
          if( newWord.equals(analyzedToken.getToken()) ) {
            String lemma = analyzedToken.getLemma();
            if( lemma != null ) {
              lemma = lemma.replace('-', '\u2013');
            }
            AnalyzedToken newToken = new AnalyzedToken(word, analyzedToken.getPOSTag(), lemma);
            newTokens.set(i, newToken);
          }
        }
        
        tkns = newTokens;
      }
    }
    
//    if( taggedDebugWriter != null && ! tkns.isEmpty() ) {
//      debug_tagged_write(tkns, taggedDebugWriter);
//    }
    
    return tkns;
  }

  List<AnalyzedToken> asAnalyzedTokenListForTaggedWordsInternal(String word, List<TaggedWord> taggedWords) {
    return super.asAnalyzedTokenListForTaggedWords(word, taggedWords);
  }
  
}
