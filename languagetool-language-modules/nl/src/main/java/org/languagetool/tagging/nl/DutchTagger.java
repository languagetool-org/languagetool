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

import com.google.common.collect.ImmutableSet;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.nl.CompoundAcceptor;
import org.languagetool.tagging.BaseTagger;
import org.languagetool.tools.StringTools;
import org.languagetool.language.Dutch;

import java.util.*;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

/**
 * Dutch tagger.
 * 
 * @author Marcin Milkowski
 */
public class DutchTagger extends BaseTagger {

  public static final DutchTagger INSTANCE = new DutchTagger();
  private static final Pattern PATTERN1_A = compile("([^aeiouáéíóú])(á)([^aeiouáéíóú])");
  private static final Pattern PATTERN1_E = compile("([^aeiouáéíóú])(é)([^aeiouáéíóú])");
  private static final Pattern PATTERN1_I = compile("([^aeiouáéíóú])(í)([^aeiouáéíóú])");
  private static final Pattern PATTERN1_O = compile("([^aeiouáéíóú])(ó)([^aeiouáéíóú])");
  private static final Pattern PATTERN1_U = compile("([^aeiouáéíóú])(ú)([^aeiouáéíóú])");
  private static final Pattern CHAR_PATTERN_AA = compile("áá");
  private static final Pattern CHAR_PATTERN_AE = compile("áé");
  private static final Pattern CHAR_PATTERN_AI = compile("áí");
  private static final Pattern CHAR_PATTERN_AU = compile("áú");
  private static final Pattern CHAR_PATTERN_EE = compile("éé");
  private static final Pattern CHAR_PATTERN_EI = compile("éí");
  private static final Pattern CHAR_PATTERN_EU = compile("éú");
  private static final Pattern CHAR_PATTERN_IE = compile("íé");
  private static final Pattern CHAR_PATTERN_OE = compile("óé");
  private static final Pattern CHAR_PATTERN_OI = compile("óí");
  private static final Pattern CHAR_PATTERN_OO = compile("óó");
  private static final Pattern CHAR_PATTERN_OU = compile("óú");
  private static final Pattern CHAR_PATTERN_UI = compile("úí");
  private static final Pattern CHAR_PATTERN_UU = compile("úú");
  private static final Pattern CHAR_PATTERN_IJ = compile("íj");
  private static final Pattern PATTERN2_A = compile("(^|[^aeiou])á([^aeiou]|$)");
  private static final Pattern PATTERN2_E = compile("(^|[^aeiou])é([^aeiou]|$)");
  private static final Pattern PATTERN2_I = compile("(^|[^aeiou])í([^aeiou]|$)");
  private static final Pattern PATTERN2_O = compile("(^|[^aeiou])ó([^aeiou]|$)");
  private static final Pattern PATTERN2_U = compile("(^|[^aeiou])ú([^aeiou]|$)");
  private static final Pattern HYPHEN1_PATTERN = compile("(^.*)-(.*$)");
  private static final Pattern HYPHEN2_PATTERN = compile("([a-z])-([a-z])");

  public DutchTagger() {
    super("/nl/dutch.dict", new Locale("nl"));
  }
  private static final Set<String> alwaysNeedsHet = ImmutableSet.of(
    "patroon",
    "punt",
    "gemaal",
    "weer",
    "kussen",
    "deel"
  );
  private static final Set<String> alwaysNeedsDe = ImmutableSet.of(
    "keten",
    "boor",
    "dans"
  );
  private static final Set<String> alwaysNeedsMrv = ImmutableSet.of(
    "pies",
    "koeken",
    "heden"
  );
  // custom code to deal with words carrying optional accents
  @Override
  public List<AnalyzedTokenReadings> tag(List<String> sentenceTokens) {
    List<AnalyzedTokenReadings> tokenReadings = new ArrayList<>();
    int pos = 0;
    CompoundAcceptor compoundAcceptor = Dutch.getCompoundAcceptor();

    for (String word : sentenceTokens) {
      boolean ignoreSpelling = false;

      // make treatment of weird apostrophes same as in tokenizer (R. Baars, 2020-11-06)
      String originalWord = word;
      word = word.replace('`', '\'').replace('’', '\'').replace('‘', '\'').replace('´', '\'');
      
      List<AnalyzedToken> l = new ArrayList<>();
      String lowerWord = word.toLowerCase(locale);
      boolean isLowercase = word.equals(lowerWord);
      boolean isMixedCase = StringTools.isMixedCase(word);
      boolean isAllUpper = StringTools.isAllUppercase(word);

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
        String firstUpper = StringTools.uppercaseFirstChar(lowerWord);
        List<AnalyzedToken> firstupperTaggerTokens = asAnalyzedTokenListForTaggedWords(originalWord, getWordTagger().tag(firstUpper));
        addTokens(firstupperTaggerTokens, l);
      }

      if (l.isEmpty()) {
        // there is still no postag found
        //String word2 = lowerWord;
        String word2 = word; // why the lowerword?
        // remove single accented characters
        word2 = PATTERN1_A.matcher(word2).replaceAll("$1a$3");
        word2 = PATTERN1_E.matcher(word2).replaceAll("$1e$3");
        word2 = PATTERN1_I.matcher(word2).replaceAll("$1i$3");
        word2 = PATTERN1_O.matcher(word2).replaceAll("$1o$3");
        word2 = PATTERN1_U.matcher(word2).replaceAll("$1u$3");

        // remove allowed accented characters
        word2 = CHAR_PATTERN_AA.matcher(word2).replaceAll("aa");
        word2 = CHAR_PATTERN_AE.matcher(word2).replaceAll("ae");
        word2 = CHAR_PATTERN_AI.matcher(word2).replaceAll("ai");
        word2 = CHAR_PATTERN_AU.matcher(word2).replaceAll("au");
        word2 = CHAR_PATTERN_EE.matcher(word2).replaceAll("ee");
        word2 = CHAR_PATTERN_EI.matcher(word2).replaceAll("ei");
        word2 = CHAR_PATTERN_EU.matcher(word2).replaceAll("eu");
        word2 = CHAR_PATTERN_IE.matcher(word2).replaceAll("ie");
        word2 = CHAR_PATTERN_OE.matcher(word2).replaceAll("oe");
        word2 = CHAR_PATTERN_OI.matcher(word2).replaceAll("oi");
        word2 = CHAR_PATTERN_OO.matcher(word2).replaceAll("oo");
        word2 = CHAR_PATTERN_OU.matcher(word2).replaceAll("ou");
        word2 = CHAR_PATTERN_UI.matcher(word2).replaceAll("ui");
        word2 = CHAR_PATTERN_UU.matcher(word2).replaceAll("uu");
        word2 = CHAR_PATTERN_IJ.matcher(word2).replaceAll("ij");

        word2 = PATTERN2_A.matcher(word2).replaceAll("$1a$2");
        word2 = PATTERN2_E.matcher(word2).replaceAll("$1e$2");
        word2 = PATTERN2_I.matcher(word2).replaceAll("$1i$2");
        word2 = PATTERN2_O.matcher(word2).replaceAll("$1o$2");
        word2 = PATTERN2_U.matcher(word2).replaceAll("$1u$2");

        // best would be to check the parts as well (uncompound)
        if (word2.contains("-")) {
          //String part1 = word2.replaceAll("(^.*)-(.*$)", "$1");
          //List<AnalyzedToken> p1 = asAnalyzedTokenListForTaggedWords(originalWord, getWordTagger().tag(part1));
          String part2 = HYPHEN1_PATTERN.matcher(word2).replaceAll("$2");
          List<AnalyzedToken> p2 = asAnalyzedTokenListForTaggedWords(originalWord, getWordTagger().tag(part2));
          //if (!(p1.isEmpty()||p2.isEmpty())) {
          if (!p2.isEmpty()) {
            // word is split on a likely location
            word2 = HYPHEN2_PATTERN.matcher(word2).replaceAll("$1$2");
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
            String part2 = parts.get(1);
            List<AnalyzedTokenReadings> part2ReadingsList = tag(Collections.singletonList(part2));
            AnalyzedTokenReadings part2Readings = part2ReadingsList.get(0);
            String part1lc = part1.toLowerCase();
            for (AnalyzedToken part2Reading : part2Readings) {
              if (part2Reading.getPOSTag() != null) {
                // if part1 ends with a hyphen, check if we are dealing with geographical compound word
                if (part1.endsWith("-")) {
                  if (part2Reading.getPOSTag().startsWith("ENM:LOC")) {
                    l.add(new AnalyzedToken(word, part2Reading.getPOSTag(), part2));
                    break;
                  }
                }
                if (part2Reading.getPOSTag().startsWith("ZNW")) {
                  String tag;
                  if (alwaysNeedsHet.contains(part2)) {
                    tag = "ZNW:EKV:HET";
                  } else if (alwaysNeedsDe.contains(part2)) {
                    tag = "ZNW:EKV:DE_";
                  } else if (alwaysNeedsMrv.contains(part2)) {
                    tag = "ZNW:MRV:DE_";
                  } else {
                    tag = part2Reading.getPOSTag();
                  }
                  l.add(new AnalyzedToken(word, tag, part1lc + part2Reading.getLemma()));
                  // if any of these lists contain part2 of the compound, exit the loop after adding a single tag
                  if (alwaysNeedsHet.contains(part2) || alwaysNeedsDe.contains(part2) || alwaysNeedsMrv.contains(part2)) {
                    break;
                  }
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

  // get tags and prevent tagger from passing value back to CompoundAcceptor, going into tagging loop
  public List<AnalyzedToken> getPostags(String word) {
    return asAnalyzedTokenListForTaggedWords(word, getWordTagger().tag(word));
  }

  private void addTokens(List<AnalyzedToken> taggedTokens, List<AnalyzedToken> l) {
    if (taggedTokens != null) {
      l.addAll(taggedTokens);
    }
  }

}
