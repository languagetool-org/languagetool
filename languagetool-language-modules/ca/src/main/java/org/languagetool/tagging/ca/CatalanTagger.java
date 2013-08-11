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
package org.languagetool.tagging.ca;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.IStemmer;
import morfologik.stemming.WordData;

import org.languagetool.AnalyzedToken;
import org.languagetool.tagging.BaseTagger;

/**
 * Catalan Tagger
 *
 * Based on FreeLing tagger dictionary
 *
 * @author Jaume Ortolà 
 */
public class CatalanTagger extends BaseTagger {

  private static final String DICT_FILENAME = "/ca/catalan.dict";
  private static final Pattern ADJ_PART_FS = Pattern.compile("VMP00SF.|A[QO].[FC][SN].");
  private static final Pattern VERB = Pattern.compile("V.+");
  private static final Pattern NOUN = Pattern.compile("NC.+");
  
  private static final Pattern PREFIXES_FOR_VERBS = Pattern.compile("(auto|re)(.+)",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);

  @Override
  public final String getFileName() {
    return DICT_FILENAME;
  }

  public CatalanTagger() {
    super();
    setLocale(new Locale("ca"));
    this.dontTagLowercaseWithUppercase();
  }

  public boolean existsWord(String word) throws IOException {
	  IStemmer dictLookup = new DictionaryLookup(getDictionary());
    final String lowerWord = word.toLowerCase(conversionLocale);
    List<WordData> posTagsFromDict = dictLookup.lookup(lowerWord);
    if (posTagsFromDict.isEmpty()) {
      posTagsFromDict = dictLookup.lookup(word);
      if (posTagsFromDict.isEmpty())
        return false;
    }
    return true;
  }

  @Override
  public List<AnalyzedToken> additionalTags(String word) {
	  IStemmer dictLookup;
	  try {
		  dictLookup = new DictionaryLookup(getDictionary());
	  } catch (IOException e) {
		  throw new IllegalStateException("Could not load dictionary: " + e.getMessage(), e);
	  }
	  
    List<AnalyzedToken> additionalTaggedTokens = new ArrayList<>();
    //Any well-formed adverb with suffix -ment is tagged as an adverb (RG)
    //Adjectiu femení singular o participi femení singular + -ment
    if (word.endsWith("ment")){
      final String lowerWord = word.toLowerCase(conversionLocale);
      final String possibleAdj = lowerWord.replaceAll("^(.+)ment$", "$1");
      List<AnalyzedToken> taggerTokens;
      taggerTokens = asAnalyzedTokenList(possibleAdj, dictLookup.lookup(possibleAdj));
      for (AnalyzedToken taggerToken : taggerTokens ) {
        final String posTag = taggerToken.getPOSTag();
        if (posTag != null) {
          final Matcher m = ADJ_PART_FS.matcher(posTag);
          if (m.matches()) {
            additionalTaggedTokens.add(new AnalyzedToken(word, "RG", lowerWord));
            return additionalTaggedTokens;
          }
        }
      }
    }
    //Any well-formed verb with prefixes is tagged as a verb copying the original tags   
    Matcher matcher=PREFIXES_FOR_VERBS.matcher(word);
    if (matcher.matches()) {
      final String possibleVerb = matcher.group(2).toLowerCase();
      List<AnalyzedToken> taggerTokens;
      taggerTokens = asAnalyzedTokenList(possibleVerb, dictLookup.lookup(possibleVerb));
      for (AnalyzedToken taggerToken : taggerTokens ) {
        final String posTag = taggerToken.getPOSTag();
        if (posTag != null) {
          final Matcher m = VERB.matcher(posTag);
          if (m.matches()) {
            String lemma=matcher.group(1).toLowerCase().concat(taggerToken.getLemma());
            additionalTaggedTokens.add(new AnalyzedToken(word, posTag, lemma));
          }
        }
      }
      return additionalTaggedTokens;
    }
    // Any well-formed noun with prefix ex- is tagged as a noun copying the original tags
    if (word.startsWith("ex")) {
      final String lowerWord = word.toLowerCase(conversionLocale);
      final String possibleNoun = lowerWord.replaceAll("^ex(.+)$", "$1");
      List<AnalyzedToken> taggerTokens;
      taggerTokens = asAnalyzedTokenList(possibleNoun,dictLookup.lookup(possibleNoun));
      for (AnalyzedToken taggerToken : taggerTokens) {
        final String posTag = taggerToken.getPOSTag();
        if (posTag != null) {
          final Matcher m = NOUN.matcher(posTag);
          if (m.matches()) {
            String lemma = "ex".concat(taggerToken.getLemma());
            additionalTaggedTokens.add(new AnalyzedToken(word, posTag, lemma));
          }
        }
      }
      return additionalTaggedTokens;
    }
    return null;
  }

}
