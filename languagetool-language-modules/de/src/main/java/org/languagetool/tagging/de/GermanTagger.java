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
package org.languagetool.tagging.de;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.tagging.BaseTagger;
import org.languagetool.tagging.ManualTagger;
import org.languagetool.tagging.TaggedWord;
import org.languagetool.tokenizers.de.GermanCompoundTokenizer;
import org.languagetool.tools.StringTools;

/**
 * German part-of-speech tagger, requires data file in <code>de/german.dict</code> in the classpath.
 * The POS tagset is described in
 * <a href="https://github.com/languagetool-org/languagetool/blob/master/languagetool-language-modules/de/src/main/resources/org/languagetool/resource/de/tagset.txt">tagset.txt</a>
 *
 * @author Marcin Milkowski, Daniel Naber
 */
public class GermanTagger extends BaseTagger {

  private final ManualTagger removalTagger;
  private final Pattern IMPERATIVE_PATTERN = Pattern.compile("[iI](ch|hr)|[eE][rs]|[Ss]ie");

  private GermanCompoundTokenizer compoundTokenizer;

  public GermanTagger() {
    super("/de/german.dict");
    try (InputStream stream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(getManualRemovalsFileName())) {
      removalTagger = new ManualTagger(stream);
    }  catch (IOException e) {
      throw new RuntimeException("Could not load manual tagger data from " + getManualAdditionsFileName(), e);
    }
  }

  @Override
  public String getManualAdditionsFileName() {
    return "/de/added.txt";
  }

  @Override
  public String getManualRemovalsFileName() {
    return "/de/removed.txt";
  }
  
  /**
   * Return only the first reading of the given word or {@code null}.
   */
  @Nullable
  public AnalyzedTokenReadings lookup(String word) throws IOException {
    List<AnalyzedTokenReadings> result = tag(Collections.singletonList(word), false);
    AnalyzedTokenReadings atr = result.get(0);
    if (atr.getAnalyzedToken(0).getPOSTag() == null) {
      return null;
    }
    return atr;
  }

  @Override
  public List<AnalyzedTokenReadings> tag(List<String> sentenceTokens) throws IOException {
    return tag(sentenceTokens, true);
  }

  public List<AnalyzedTokenReadings> tag(List<String> sentenceTokens, boolean ignoreCase) throws IOException {
    initializeIfRequired();

    boolean firstWord = true;
    List<AnalyzedTokenReadings> tokenReadings = new ArrayList<>();
    int pos = 0;

    for (String word : sentenceTokens) {
      List<AnalyzedToken> l = new ArrayList<>();
      List<TaggedWord> taggerTokens = getWordTagger().tag(word);
      if (firstWord && taggerTokens.isEmpty() && ignoreCase) { // e.g. "Das" -> "das" at start of sentence
        taggerTokens = getWordTagger().tag(word.toLowerCase());
        firstWord = word.matches("^\\W?$");
      } else if (pos == 0 && ignoreCase) {   // "Haben", "Sollen", "Können", "Gerade" etc. at start of sentence
        taggerTokens.addAll(getWordTagger().tag(word.toLowerCase()));
      }
      if (taggerTokens.size() > 0) {
        l.addAll(getAnalyzedTokens(taggerTokens, word));
      } else {
        // word not known, try to decompose it and use the last part for POS tagging:
        if (!StringTools.isEmpty(word.trim())) {
          List<String> compoundParts = compoundTokenizer.tokenize(word);
          if (compoundParts.size() <= 1) {
              // recognize alternative imperative forms (e.g., "Geh bitte!" in addition to "Gehe bitte!")
              List<AnalyzedToken> imperativeFormList = getImperativeForm(word, sentenceTokens, pos);
              if (imperativeFormList != null && imperativeFormList.size() > 0) {
                l.addAll(imperativeFormList);
              } else {
                l.add(getNoInfoToken(word));
              }
          } else {
            // last part governs a word's POS:
            String lastPart = compoundParts.get(compoundParts.size()-1);
            if (StringTools.startsWithUppercase(word)) {
              lastPart = StringTools.uppercaseFirstChar(lastPart);
            }
            List<TaggedWord> partTaggerTokens = getWordTagger().tag(lastPart);
            if (partTaggerTokens.size() > 0) {
              l.addAll(getAnalyzedTokens(partTaggerTokens, word, compoundParts));
            } else {
              l.add(getNoInfoToken(word));
            }
          }
        } else {
          l.add(getNoInfoToken(word));
        }
      }

      tokenReadings.add(new AnalyzedTokenReadings(l.toArray(new AnalyzedToken[l.size()]), pos));
      pos += word.length();
    }
    return tokenReadings;
  }

 /*
  * Tag alternative imperative forms (e.g., "Geh bitte!" in addition to "Gehe bitte!")
  * To avoid false positives and conflicts with DE_CASE the tagging is restricted to
  * [a] words at the start of a sentence ("Geh bitte!") if the sentence counts more than one word
  * [b] words preceded by ich/ihr/er/es/sie to catch some real errors ("Er geh jetzt.") by the new rule in rulegroup SUBJECT_VERB_AGREEMENT
  * @param word to be checked
  */
  private List<AnalyzedToken> getImperativeForm (String word, List<String> sentenceTokens, int pos) {
      int idx = sentenceTokens.indexOf(word);
      String previousWord = "";
      while (--idx > -1) {
        previousWord = sentenceTokens.get(idx);
        if (previousWord.matches("\\s+")) {
          continue;
        }
        break;
      }
      if (!(pos == 0 && sentenceTokens.size() > 1) && !IMPERATIVE_PATTERN.matcher(previousWord).matches()) {
        return null;
      }
      String w = pos == 0 ? word.toLowerCase() : word;
      List<TaggedWord> taggedWithE = getWordTagger().tag(w+"e");
      for (TaggedWord tagged : taggedWithE) {
        if (tagged.getPosTag().startsWith("VER:IMP:SIN:")) {
          // do not overwrite manually removed tags
          if (removalTagger == null || !removalTagger.tag(w).contains(tagged)) {
            return getAnalyzedTokens(Arrays.asList(tagged), word);
          }
          break;
        }
      }
      return null;
  }

  private synchronized void initializeIfRequired() throws IOException {
    if (compoundTokenizer == null) {
      compoundTokenizer = new GermanCompoundTokenizer();
    }
  }

  private AnalyzedToken getNoInfoToken(String word) {
    return new AnalyzedToken(word, null, null);
  }

  private List<AnalyzedToken> getAnalyzedTokens(List<TaggedWord> taggedWords, String word) {
    List<AnalyzedToken> result = new ArrayList<>();
    for (TaggedWord taggedWord : taggedWords) {
      result.add(new AnalyzedToken(word, taggedWord.getPosTag(), taggedWord.getLemma()));
    }
    return result;
  }

  private List<AnalyzedToken> getAnalyzedTokens(List<TaggedWord> taggedWords, String word, List<String> compoundParts) {
    List<AnalyzedToken> result = new ArrayList<>();
    for (TaggedWord taggedWord : taggedWords) {
      List<String> allButLastPart = compoundParts.subList(0, compoundParts.size() - 1);
      String lemma = String.join("", allButLastPart)
                   + StringTools.lowercaseFirstChar(taggedWord.getLemma());
      result.add(new AnalyzedToken(word, taggedWord.getPosTag(), lemma));
    }
    return result;
  }

}
