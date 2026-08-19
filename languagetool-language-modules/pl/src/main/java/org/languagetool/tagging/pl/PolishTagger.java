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
package org.languagetool.tagging.pl;

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.spelling.CachingWordListLoader;
import org.languagetool.tagging.BaseTagger;
import org.languagetool.tagging.WordTagger;
import org.languagetool.tools.StringTools;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Polish POS tagger based on FSA morphological dictionaries.
 *
 * @author Marcin Milkowski
 */
public class PolishTagger extends BaseTagger {

  private static final String NEGATION_PREFIX = "nie";

  // Comparative and superlative adverbs and adjectives, i.e. exactly the forms
  // that "nie" may be joined to since the 2026 spelling reform. Matched against
  // the whole tag, so that unrelated tags ending in similar strings (num:comp)
  // are not caught.
  private static final Pattern COMPAR_SUPERL = Pattern.compile("(adv|adj:.+):(com|sup)");

  // Shortest joined form worth trying: "nie" + a four-letter comparative.
  private static final int MIN_NEGATED_LENGTH = NEGATION_PREFIX.length() + 4;

  private static final String NEGATION_EXCEPTIONS_FILE = "/pl/nie_compar_exceptions.txt";

  // Loaded here rather than in a static initializer, so that a data broker set
  // by the embedding application is picked up. CachingWordListLoader caches
  // statically, so further instances do not re-read the file.
  private final Set<String> negationExceptions;

  public PolishTagger() {
    super("/pl/polish.dict", new Locale("pl"));
    negationExceptions = new HashSet<>(new CachingWordListLoader().loadWords(NEGATION_EXCEPTIONS_FILE));
  }

  @Override
  public final List<AnalyzedTokenReadings> tag(List<String> sentenceTokens) {
    List<AnalyzedToken> taggerTokens;
    List<AnalyzedToken> lowerTaggerTokens;
    List<AnalyzedToken> upperTaggerTokens;    
    List<AnalyzedTokenReadings> tokenReadings = new ArrayList<>();
    int pos = 0;

    for (String word : sentenceTokens) {
      List<AnalyzedToken> l = new ArrayList<>();
      String lowerWord = word.toLowerCase(locale);
      taggerTokens = asAnalyzedTokenListForTaggedWords(word, getWordTagger().tag(word));
      lowerTaggerTokens = asAnalyzedTokenListForTaggedWords(word, getWordTagger().tag(lowerWord));
      boolean isLowercase = word.equals(lowerWord);

      //normal case
      addTokens(taggerTokens, l);

      if (!isLowercase) {
        //lowercase
        addTokens(lowerTaggerTokens, l);
      }

      //uppercase
      if (l.isEmpty() && isLowercase) {
        upperTaggerTokens = asAnalyzedTokenListForTaggedWords(word,
            getWordTagger().tag(StringTools.uppercaseFirstChar(word)));
        addTokens(upperTaggerTokens, l);
      }

      //additional language-dependent tagging, e.g. joined "nie"
      if (l.isEmpty()) {
        addTokens(additionalTags(word, getWordTagger()), l);
      }

      if (l.isEmpty()) {
        l.add(new AnalyzedToken(word, null, null));
      }
      tokenReadings.add(new AnalyzedTokenReadings(l, pos));
      pos += word.length();
    }

    return tokenReadings;
  }

  /**
   * Analyzes forms in which "nie" is joined to a comparative or superlative
   * adverb or adjective, such as <em>nielepiej</em> or <em>nienajlepszy</em>.
   * The 2026 spelling reform allows these to be written as one word, but they
   * are not in the binary dictionary yet, so the prefix is stripped and the
   * remainder looked up instead, in the same way other languages handle
   * productive prefixes.
   * <p>
   * The lemma is the negated positive form, following the dictionary's own
   * convention for negated lexemes (<em>niepewien</em> has the lemma
   * <em>niepewny</em>), so that all degrees of one negated lexeme share a
   * lemma and rules keyed on the affirmative lemma do not fire on the
   * negated word.
   *
   * @since 6.9
   */
  @Nullable
  @Override
  protected List<AnalyzedToken> additionalTags(String word, WordTagger wordTagger) {
    String lowerWord = word.toLowerCase(locale);
    if (lowerWord.length() < MIN_NEGATED_LENGTH
        || !lowerWord.startsWith(NEGATION_PREFIX)
        || StringTools.isMixedCase(word)
        || negationExceptions.contains(lowerWord)) {
      return null;
    }
    // Only one lookup, so "nienielepiej" stays untagged. "nienajlepiej" needs
    // no extra handling, as "najlepiej" is in the dictionary already.
    String base = lowerWord.substring(NEGATION_PREFIX.length());
    List<AnalyzedToken> result = new ArrayList<>();
    for (AnalyzedToken baseToken : asAnalyzedTokenListForTaggedWords(word, wordTagger.tag(base))) {
      String posTag = baseToken.getPOSTag();
      if (posTag != null && baseToken.getLemma() != null && COMPAR_SUPERL.matcher(posTag).matches()) {
        result.add(new AnalyzedToken(word, posTag, NEGATION_PREFIX + baseToken.getLemma()));
      }
    }
    return result.isEmpty() ? null : result;
  }

  private void addTokens(List<AnalyzedToken> taggedTokens, List<AnalyzedToken> l) {
    if (taggedTokens != null) {
      for (AnalyzedToken at : taggedTokens) {
        String[] tagsArr = StringTools.asString(at.getPOSTag()).split("\\+");
        for (String currTag : tagsArr) {
          l.add(new AnalyzedToken(at.getToken(), currTag, at.getLemma()));
        }
      }
    }
  }

}
