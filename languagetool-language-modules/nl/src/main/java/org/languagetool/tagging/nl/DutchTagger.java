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
package org.languagetool.tagging.nl;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.nl.CompoundAcceptor;
import org.languagetool.tagging.BaseTagger;
import org.languagetool.tools.StringTools;

import java.util.*;

/**
 * Dutch tagger.
 * 
 * @author Marcin Milkowski
 */
public class DutchTagger extends BaseTagger {

  public DutchTagger() {
    super("/nl/dutch.dict", new Locale("nl"));
  }
    // custom code to deal with words carrying optional accents
  @Override
  public List<AnalyzedTokenReadings> tag(final List<String> sentenceTokens) {
    final List<AnalyzedTokenReadings> tokenReadings = new ArrayList<>();
    int pos = 0;
    CompoundAcceptor compoundAcceptor = new CompoundAcceptor(this);

    for (String word : sentenceTokens) {
      boolean ignoreSpelling = false;

      // make treatment of weird apostrophes same as in tokenizer (R. Baars, 2020-11-06)
      String originalWord = word;
      word = word.replace('`', '\'').replace('’', '\'').replace('‘', '\'').replace('´', '\'');
      
      final List<AnalyzedToken> l = new ArrayList<>();
      final String lowerWord = word.toLowerCase(locale);
      final boolean isLowercase = word.equals(lowerWord);
      final boolean isMixedCase = StringTools.isMixedCase(word);
      final boolean isAllUpper = StringTools.isAllUppercase(word);

      // assign tokens for flattened word to original word
      List<AnalyzedToken> taggerTokens = asAnalyzedTokenListForTaggedWords(originalWord, getWordTagger().tag(word));
      //List<AnalyzedToken> taggerTokens = asAnalyzedTokenListForTaggedWords(word, getWordTagger().tag(word));
      // normal case:
      addTokens(taggerTokens, l);
      // tag non-lowercase (alluppercase or startuppercase), but not mixedcase
      // word with lowercase word tags:
      if (!isLowercase && !isMixedCase) {
        List<AnalyzedToken> lowerTaggerTokens = asAnalyzedTokenListForTaggedWords(originalWord, getWordTagger().tag(lowerWord));
        addTokens(lowerTaggerTokens, l);
      }

      // tag all-uppercase proper nouns
      if (l.isEmpty() && isAllUpper) {
        final String firstUpper = StringTools.uppercaseFirstChar(lowerWord);
        List<AnalyzedToken> firstupperTaggerTokens = asAnalyzedTokenListForTaggedWords(originalWord, getWordTagger().tag(firstUpper));
        addTokens(firstupperTaggerTokens, l);
      }

      if (l.isEmpty()) {
        // there is still no postag found
        //String word2 = lowerWord;
        String word2 = word; // why the lowerword?
        // remove single accented characters
        word2 = word2.replaceAll("([^aeiouáéíóú])(á)([^aeiouáéíóú])", "$1a$3");
        word2 = word2.replaceAll("([^aeiouáéíóú])(é)([^aeiouáéíóú])", "$1e$3");
        word2 = word2.replaceAll("([^aeiouáéíóú])(í)([^aeiouáéíóú])", "$1i$3");
        word2 = word2.replaceAll("([^aeiouáéíóú])(ó)([^aeiouáéíóú])", "$1o$3");
        word2 = word2.replaceAll("([^aeiouáéíóú])(ú)([^aeiouáéíóú])", "$1u$3");

        // remove allowed accented characters
        word2 = word2.replace("áá", "aa");
        word2 = word2.replace("áé", "ae");
        word2 = word2.replace("áí", "ai");
        word2 = word2.replace("áú", "au");
        word2 = word2.replace("éé", "ee");
        word2 = word2.replace("éí", "ei");
        word2 = word2.replace("éú", "eu");
        word2 = word2.replace("íé", "ie");
        word2 = word2.replace("óé", "oe");
        word2 = word2.replace("óí", "oi");
        word2 = word2.replace("óó", "oo");
        word2 = word2.replace("óú", "ou");
        word2 = word2.replace("úí", "ui");
        word2 = word2.replace("úú", "uu");
        word2 = word2.replace("íj", "ij");

        word2 = word2.replaceAll("(^|[^aeiou])á([^aeiou]|$)", "$1a$2");
        word2 = word2.replaceAll("(^|[^aeiou])é([^aeiou]|$)", "$1e$2");
        word2 = word2.replaceAll("(^|[^aeiou])í([^aeiou]|$)", "$1i$2");
        word2 = word2.replaceAll("(^|[^aeiou])ó([^aeiou]|$)", "$1o$2");
        word2 = word2.replaceAll("(^|[^aeiou])ú([^aeiou]|$)", "$1u$2");

        // best would be to check the parts as well (uncompound)
        if (word2.contains("-")) {
          //String part1 = word2.replaceAll("(^.*)-(.*$)", "$1");
          //List<AnalyzedToken> p1 = asAnalyzedTokenListForTaggedWords(originalWord, getWordTagger().tag(part1));
          String part2 = word2.replaceAll("(^.*)-(.*$)", "$2");
          List<AnalyzedToken> p2 = asAnalyzedTokenListForTaggedWords(originalWord, getWordTagger().tag(part2));
          //if (!(p1.isEmpty()||p2.isEmpty())) {
          if (!p2.isEmpty()) {
            // word is split on a likely location
            word2 = word2.replaceAll("([a-z])-([a-z])", "$1$2");
          }
        }

        if (!word2.equals(word)) {
          List<AnalyzedToken> l2 = asAnalyzedTokenListForTaggedWords(originalWord, getWordTagger().tag(word2));
          if (!l2.isEmpty()) {
            // woord bestaat
            addTokens(l2, l);
            ignoreSpelling = true;
          }
        }

        // Tag unknown compound words:
        if (l.isEmpty() && word.length() > 5) {
          List<String> parts = compoundAcceptor.getParts(word);
          if (parts.size() == 2) {
            String part1 = parts.get(0);
            List<AnalyzedTokenReadings> part2ReadingsList = tag(Collections.singletonList(parts.get(1)));
            if (part2ReadingsList.size() > 0) {
              AnalyzedTokenReadings part2Readings = part2ReadingsList.get(0);
              String part1lc = part1.toLowerCase();
              for (AnalyzedToken part2Reading : part2Readings) {
                if (part2Reading.getPOSTag() != null && part2Reading.getPOSTag().contains("ZNW")) {
                  //System.out.println("Adding " + word + " with postag " + part2Reading.getPOSTag() + ", part2 has lemma " + part2Reading.getLemma());
                  l.add(new AnalyzedToken(word, part2Reading.getPOSTag(), part1lc + part2Reading.getLemma()));
                }
              }
            }
          }
        }
      }

      // set word to original
      word = originalWord;

      if (l.isEmpty()) {
        l.add(new AnalyzedToken(originalWord, null, null));
      }

      AnalyzedTokenReadings atr = new AnalyzedTokenReadings(l, pos);
      if (ignoreSpelling) {
        // it might be a word that needs captials! Check this in dictionary
        if (isLowercase) {
          List<AnalyzedToken> fu = asAnalyzedTokenListForTaggedWords(StringTools.uppercaseFirstChar(originalWord), getWordTagger().tag(StringTools.uppercaseFirstChar(originalWord)));
          if (fu.isEmpty()) {
            // does not exist in dictionary having firstupper
            atr.ignoreSpelling();
          } else {
            // there is an uppercased form in the dictionary; so this one is probably wrong
            //System.out.println("=>"+l.toString());
            // TODO clearing the l list does not work here; the 'LIKELY_SPELLING' tag should be removed! But somehow, this does not work when done here.
            l.clear();
            l.add(new AnalyzedToken(originalWord, null, null));
            //System.out.println("=>"+l.toString());
          }
        } else {
            atr.ignoreSpelling();
        }
      }

      tokenReadings.add(atr);
      
      pos += word.length();
    }
    
    return tokenReadings;
  }

  private void addTokens(final List<AnalyzedToken> taggedTokens, final List<AnalyzedToken> l) {
    if (taggedTokens != null) {
      l.addAll(taggedTokens);
    }
  }

}
