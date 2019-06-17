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

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
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

  private GermanCompoundTokenizer compoundTokenizer;

  public GermanTagger() {
    super("/de/german.dict");
    try (InputStream stream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(getManualRemovalsFileName())) {
      removalTagger = new ManualTagger(stream);
    } catch (IOException e) {
      throw new RuntimeException("Could not load manual tagger data from " + getManualAdditionsFileName(), e);
    }
  }

  private List<TaggedWord> addStem(List<TaggedWord> analyzedWordResults, String stem) {
    List<TaggedWord> result = new ArrayList<>();
    for (TaggedWord tw : analyzedWordResults) {
      String lemma = tw.getLemma();
      if (stem.length() > 0 && stem.charAt(stem.length() - 1) != '-' && tw.getPosTag().startsWith("SUB:")) {
        lemma = lemma.toLowerCase();
      }
      result.add(new TaggedWord(stem + lemma, tw.getPosTag()));
    }
    return result;
  }
  
  //Removes the irrelevant part of dash-linked words (SSL-Zertifikat -> Zertifikat)
  private String sanitizeWord(String word) {
    String result = word;

    //Find the last part of the word that is not nothing
    //Skip words ending in a dash as they'll be misrecognized
    if (!word.endsWith("-")) {
      String[] splitWord = word.split("-");
      String lastPart = splitWord.length > 1 && !splitWord[splitWord.length - 1].trim().equals("") ? splitWord[splitWord.length - 1] : word;

      //Find only the actual important part of the word
      List<String> compoundedWord = compoundTokenizer.tokenize(lastPart);
      if (compoundedWord.size() > 1) {
        lastPart = StringTools.uppercaseFirstChar(compoundedWord.get(compoundedWord.size() - 1));
      } else {
        lastPart = compoundedWord.get(compoundedWord.size() - 1);
      }

      //Only give result if the last part is either a noun or an adjective (or adjective written in Uppercase)
      List<TaggedWord> tagged = tag(lastPart);
      if (tagged.size() > 0 && (StringUtils.startsWithAny(tagged.get(0).getPosTag(), "SUB", "ADJ") || matchesUppercaseAdjective(lastPart))) {
        result = lastPart;
      }
    }
    return result;
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

  public List<TaggedWord> tag(String word) {
    return getWordTagger().tag(word);
  }

  private boolean matchesUppercaseAdjective(String unknownUppercaseToken) {
    List<TaggedWord> temp = getWordTagger().tag(StringTools.lowercaseFirstChar(unknownUppercaseToken));
    return temp.size() > 0 && temp.get(0).getPosTag().startsWith("ADJ");
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
      List<AnalyzedToken> readings = new ArrayList<>();
      List<TaggedWord> taggerTokens = getWordTagger().tag(word);

      //Only first iteration
      if (firstWord && taggerTokens.isEmpty() && ignoreCase) { // e.g. "Das" -> "das" at start of sentence
        taggerTokens = getWordTagger().tag(word.toLowerCase());
        firstWord = word.matches("^\\W?$");
      } else if (pos == 0 && ignoreCase) {   // "Haben", "Sollen", "Können", "Gerade" etc. at start of sentence
        taggerTokens.addAll(getWordTagger().tag(word.toLowerCase()));
      } else if (pos > 1 && taggerTokens.isEmpty() && ignoreCase) {
          int idx = sentenceTokens.indexOf(word);
          // add lowercase token readings to words at start of direct speech
          if (idx > 2 && sentenceTokens.get(idx-1).contentEquals("„") && sentenceTokens.get(idx-3).contentEquals(":")) {
            taggerTokens.addAll(getWordTagger().tag(word.toLowerCase()));
          }
      }

      if (taggerTokens.size() > 0) { //Word known, just add analyzed token to readings
        readings.addAll(getAnalyzedTokens(taggerTokens, word));
      } else { // Word not known, try to decompose it and use the last part for POS tagging:
        if (!StringTools.isEmpty(word.trim())) {
          List<String> compoundParts = compoundTokenizer.tokenize(word);
          if (compoundParts.size() <= 1) {//Could not find simple compound parts
            // Recognize alternative imperative forms (e.g., "Geh bitte!" in addition to "Gehe bitte!")
            List<AnalyzedToken> imperativeFormList = getImperativeForm(word, sentenceTokens, pos);
            List<AnalyzedToken> substantivatedFormsList = getSubstantivatedForms(word, sentenceTokens, pos);
            if (imperativeFormList.size() > 0) {
              readings.addAll(imperativeFormList);
            } else if (substantivatedFormsList.size() > 0) {
              readings.addAll(substantivatedFormsList);
            } else {
              if (StringUtils.startsWithAny(word, "bitter", "dunkel", "erz", "extra", "früh",
                "gemein", "hyper", "lau", "mega", "minder", "stock", "super", "tod", "ultra", "ur")) {
                String lastPart = RegExUtils.removePattern(word, "^(bitter|dunkel|erz|extra|früh|gemein|grund|hyper|lau|mega|minder|stock|super|tod|ultra|ur|voll)");
                if (lastPart.length() > 1) {
                  String firstPart = StringUtils.removeEnd(word, lastPart);
                  List<TaggedWord> taggedWords = getWordTagger().tag(lastPart);
                  for (TaggedWord taggedWord : taggedWords) {
                    readings.add(new AnalyzedToken(word, taggedWord.getPosTag(), firstPart+taggedWord.getLemma()));
                  }
                }
              }
              //Separate dash-linked words
              //Only check single word tokens and skip words containing numbers because it's unpredictable
              if (word.split(" ").length == 1 && !Character.isDigit(word.charAt(0))) {
                String wordOrig = word;
                word = sanitizeWord(word);
                String wordStem = wordOrig.substring(0, wordOrig.length() - word.length());

                //Tokenize, start word uppercase if it's a result of splitting
                List<String> compoundedWord = compoundTokenizer.tokenize(word);
                if (compoundedWord.size() > 1) {
                  word = StringTools.uppercaseFirstChar(compoundedWord.get(compoundedWord.size() - 1));
                } else {
                  word = compoundedWord.get(compoundedWord.size() - 1);
                }
                
                List<TaggedWord> linkedTaggerTokens = addStem(getWordTagger().tag(word), wordStem); //Try to analyze the last part found

                //Some words that are linked with a dash ('-') will be written in uppercase, even adjectives
                if (wordOrig.contains("-") && linkedTaggerTokens.isEmpty() && matchesUppercaseAdjective(word)) {
                  word = StringTools.lowercaseFirstChar(word);
                  linkedTaggerTokens = getWordTagger().tag(word);
                }

                word = wordOrig;
                
                boolean wordStartsUppercase = StringTools.startsWithUppercase(word);
                if (linkedTaggerTokens.isEmpty()) {
                  readings.add(getNoInfoToken(word));
                } else {
                  if (wordStartsUppercase) { //Choose between uppercase/lowercase Lemma
                    readings.addAll(getAnalyzedTokens(linkedTaggerTokens, word));
                  } else {
                    readings.addAll(getAnalyzedTokens(linkedTaggerTokens, word, compoundedWord));
                  }
                }
              } else {
                readings.add(getNoInfoToken(word));
              }
            }
          } else {
            // last part governs a word's POS:
            String lastPart = compoundParts.get(compoundParts.size() - 1);
            if (StringTools.startsWithUppercase(word)) {
              lastPart = StringTools.uppercaseFirstChar(lastPart);
            }
            List<TaggedWord> partTaggerTokens = getWordTagger().tag(lastPart);
            if (partTaggerTokens.isEmpty()) {
              readings.add(getNoInfoToken(word));
            } else {
              readings.addAll(getAnalyzedTokens(partTaggerTokens, word, compoundParts));
            }
          }
        } else {
          readings.add(getNoInfoToken(word));
        }
      }
      tokenReadings.add(new AnalyzedTokenReadings(readings.toArray(new AnalyzedToken[readings.size()]), pos));
      pos += word.length();
    }
    return tokenReadings;
  }

  /*
   * Tag alternative imperative forms (e.g., "Geh bitte!" in addition to "Gehe bitte!")
   * To avoid false positives and conflicts with DE_CASE the tagging is restricted to
   * [a] words at the start of a sentence ("Geh bitte!") if the sentence counts more than one word
   * [b1] words preceded by ich/ihr/er/es/sie to catch some real errors ("Er geh jetzt.") by the new rule in rulegroup SUBJECT_VERB_AGREEMENT
   * [b2] words preceded by aber/nun/jetzt (e.g., "Bitte geh!", "Jetzt sag schon!" etc.)
   * @param word to be checked
   */
  private List<AnalyzedToken> getImperativeForm(String word, List<String> sentenceTokens, int pos) {
    int idx = sentenceTokens.indexOf(word);
    String previousWord = "";
    while (--idx > -1) {
      previousWord = sentenceTokens.get(idx);
      if (!StringUtils.isWhitespace(previousWord)) {
        break;
      }
    }
    if (!(pos == 0 && sentenceTokens.size() > 1)
        && !StringUtils.equalsAnyIgnoreCase(previousWord, "ich", "er", "es", "sie", "bitte", "aber", "nun", "jetzt", "„")) {
      return Collections.emptyList();
    }
    String w = pos == 0 || "„".equals(previousWord) ? word.toLowerCase() : word;
    List<TaggedWord> taggedWithE = getWordTagger().tag(w.concat("e"));
    for (TaggedWord tagged : taggedWithE) {
      if (tagged.getPosTag().startsWith("VER:IMP:SIN:")) {
        // do not overwrite manually removed tags
        if (removalTagger == null || !removalTagger.tag(w).contains(tagged)) {
          return getAnalyzedTokens(Arrays.asList(tagged), word);
        }
        break;
      }
    }
    return Collections.emptyList();
  }

  /*
   * Tag substantivated adjectives and participles, which are currently tagged not tagged correctly
   * (e.g., "Verletzter" in "Ein Verletzter kam ins Krankenhaus" needs to be tagged as "SUB:NOM:SIN:MAS")
   * @param word to be checked
   */
  private List<AnalyzedToken> getSubstantivatedForms(String word, List<String> sentenceTokens, int pos) {
    if (word.endsWith("er")) {
      List<TaggedWord> lowerCaseTags = getWordTagger().tag(word.toLowerCase());
      // do not add tag words whose lower case variant is an adverb (e.g, "Früher") to avoid false negatives for DE_CASE
      if (lowerCaseTags.stream().anyMatch(t -> t.getPosTag().startsWith("ADV"))) {
        return Collections.emptyList();
      }
      int idx = sentenceTokens.indexOf(word);
      // is followed by an uppercase word? If 'yes', the word is probably not substantivated
      while (++idx < sentenceTokens.size()) {
        String nextWord = sentenceTokens.get(idx);
        if (StringUtils.isWhitespace(nextWord)) {
          continue;
        }
        if (nextWord.length() > 0 && (Character.isUpperCase(nextWord.charAt(0)) || "als".equals(nextWord))) {
          return Collections.emptyList();
        }
        break;
      }
      String femaleForm = word.substring(0, word.length()-1);
      List<TaggedWord> taggedFemaleForm = getWordTagger().tag(femaleForm);
      boolean isSubstantivatedForm = taggedFemaleForm.stream().anyMatch(t -> t.getPosTag().equals("SUB:NOM:SIN:FEM:ADJ"));
      if (isSubstantivatedForm) {
        List<AnalyzedToken> list = new ArrayList<>();
        list.add(new AnalyzedToken(word, "SUB:NOM:SIN:MAS:ADJ", word));
        list.add(new AnalyzedToken(word, "SUB:GEN:PLU:MAS:ADJ", word));
        return list;
      }
    }
    return Collections.emptyList();
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
      StringBuilder lemma = new StringBuilder();
      int i = 0;
      for (String s : allButLastPart) {
        lemma.append(i == 0 ? s : StringTools.lowercaseFirstChar(s));
        i++;
      }
      lemma.append(StringTools.lowercaseFirstChar(taggedWord.getLemma()));
      result.add(new AnalyzedToken(word, taggedWord.getPosTag(), lemma.toString()));
    }
    return result;
  }

}
