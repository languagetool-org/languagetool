/* LanguageTool, a natural language style checker 
 * Copyright (C) 2007 Daniel Naber (http://www.danielnaber.de)
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

/*
 * Created on 01.10.2010
 */
package de.danielnaber.languagetool.tagging.eo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.tagging.Tagger;
import de.danielnaber.languagetool.tagging.ManualTagger;

/**
 * A part-of-speech tagger for Esperanto.
 *
 * @author Dominique Pellé
 */
public class EsperantoTagger implements Tagger {
  // manual tagger is used to tag the list of closed Esperanto words
  // (small limited number of words which do not have regular ending).
  private ManualTagger manualTagger = null;

  // Set of transitive verbs and intransitive verbs.
  private Set<String> setTransitiveVerbs = null;
  private Set<String> setIntransitiveVerbs = null;

  // Verbs always end with this pattern.
  private static final Pattern patternVerb = Pattern.compile("(..+)(as|os|is|us|u|i)$");
  private static final Pattern patternPrefix = Pattern.compile("^(?:mal|mis|ek|re|fi|ne)(.*)");
  private static final Pattern patternSuffix = Pattern.compile("(.*)(?:ad|aĉ|eg|et)i$");

  // Participles -ant-, -int, ont-, -it-, -it-, -ot-
  private static final Pattern patternParticiple =
    Pattern.compile("((..+)([aio])(n?)t)([aoe])(j?)(n?)$");
  // Groups           11111111111111111  55555  66  77
  //                   222  33333  44                 
 
  private Set<String> setNonParticiple;

  // Pattern 'tabelvortoj'.
  private static final Pattern patternTabelvorto =
    Pattern.compile("^(i|ti|ki|ĉi|neni)(?:(?:([uoae])(j?)(n?))|(am|al|es|el|om))$");
  // Groups            111111111111111        222222  33  44    55555555555555
  //                                          

  // Pattern of 'tabelvortoj' which are also tagged adverbs.
  private static final Pattern patternTabelvortoAdverb = 
    Pattern.compile("^(?:ti|i|ĉi|neni)(?:am|om|el|e)$");

  /**
   * Load list of words from UTF-8 file (one word per line).
   */
  private Set<String> loadWords(final InputStream file) throws IOException {
    InputStreamReader isr = null;
    BufferedReader br = null;
    final Set<String> words = new HashSet<String>();
    try {
      isr = new InputStreamReader(file, "UTF-8");
      br = new BufferedReader(isr);
      String line;

      while ((line = br.readLine()) != null) {
        line = line.trim();
        if (line.length() < 1) {
          continue;
        }
        if (line.charAt(0) == '#') { // ignore comments
          continue;
        }
        words.add(line);
      }
    } finally {
      if (br != null) {
        br.close();
      }
      if (isr != null) {
        isr.close();
      }
    }
    return words;
  }

  private void lazyInit() throws IOException {
    if (manualTagger != null) {
      return;
    }

    // A manual tagger is used for closed words.  Closed words are the small
    // limited set of words in Esperanto which have no standard ending, as
    // opposed to open words which are unlimited in numbers and which follow
    // strict rules for their suffixes.
    manualTagger = new ManualTagger(JLanguageTool.getDataBroker().getFromResourceDirAsStream("/eo/manual-tagger.txt"));

    // Load set of transitive and intransitive verbs.  Files don't contain
    // verbs with suffix -iĝ or -ig since transitivity is obvious for those verbs.
    // They also don't contain verbs with prefixes mal-, ek-, re-, mis- fi- and
    // suffixes -ad, -aĉ, -et, -eg since these affixes never alter transitivity.
    setTransitiveVerbs   = loadWords(JLanguageTool.getDataBroker().getFromRulesDirAsStream("/eo/verb-tr.txt"));
    setIntransitiveVerbs = loadWords(JLanguageTool.getDataBroker().getFromRulesDirAsStream("/eo/verb-ntr.txt"));
    setNonParticiple     = loadWords(JLanguageTool.getDataBroker().getFromRulesDirAsStream("/eo/root-ant-at.txt"));
  }

  // For a given verb (.*i) find whether it is transitive and/or non transitive.
  // Returns:
  // "tr" for a verb which is transitive
  // "nt" for a verb which is non-transitive
  // "tn" for a verb which is transitive and non-transitive
  // "xx" for an unknown verb.
  private String findTransitivity(String verb) {
    if (verb.endsWith("iĝi")) {
      return "nt";
    } else if (verb.endsWith("igi")) {
      // The verb "memmortigi is strange: even though it ends in -igi, it
      // is intransitive.
      return verb.equals("memmortigi") ? "nt" : "tr";
    }

    // This loop executes only once for most verbs (or very few times).
    for (;;) {
      final boolean isTransitive   = setTransitiveVerbs.contains(verb);
      final boolean isIntransitive = setIntransitiveVerbs.contains(verb);

      if (isTransitive) {
        return isIntransitive ? "tn" : "tr";
      } else if (isIntransitive) {
        return "nt";
      }

      // Verb is not explicitly listed as transitive or intransitive.
      // Try to remove a prefix mal-, ek-, re-, mis- fi- or
      // suffix -ad, -aĉ, -et, -eg since those never alter
      // transitivity.  Then look up verb again in case we find 
      // a verb with a known transitivity.  For example, given a verb
      // "malŝategi", we will probe "malŝategi", "ŝategi" "ŝati"
      // and then finally find out that "ŝati" is transitive.
      final Matcher matcherPrefix = patternPrefix.matcher(verb);
      if (matcherPrefix.find()) {
        // Remove a prefix and try again.
        verb = matcherPrefix.group(1);
        continue;
      }
      final Matcher matcherSuffix = patternSuffix.matcher(verb);
      if (matcherSuffix.find()) {
        // Remove a suffix and try again.
        verb = matcherSuffix.group(1) + "i";
        continue;
      }
      break;
    }
    return "xx"; // Unknown transitivity.
  }

  @Override
  public List<AnalyzedTokenReadings> tag(final List<String> sentenceTokens) throws IOException {

    lazyInit();
    Matcher matcher;

    final List<AnalyzedTokenReadings> tokenReadings = 
      new ArrayList<AnalyzedTokenReadings>();
    int pos = 0;
    for (String word : sentenceTokens) {
      final List<AnalyzedToken> l = new ArrayList<AnalyzedToken>();
      final String lWord = word.toLowerCase();
      final String[] manualTags = manualTagger.lookup(lWord);

      if (manualTags != null) {
        // This is a closed word for which we know its lemmas and tags.
        for (int i = 0; i < manualTags.length; i += 2) {
          final String lemma  = manualTags[2*i];
          final String postag = manualTags[2*i + 1];
          l.add(new AnalyzedToken(word, postag, lemma));
        }
      } else {
        // This is an open word, we need to look at the word ending 
        // to determine its lemma and POS tag.  For verb, we also
        // need to look up the dictionary of known transitive and
        // intransitive verbs.

        // Tiu, kiu (tabelvortoj).
        if ((matcher = patternTabelvorto.matcher(lWord)).find()) {
          final String type1Group = matcher.group(1).substring(0, 1).toLowerCase();
          final String type2Group = matcher.group(2);
          final String plGroup    = matcher.group(3);
          final String accGroup   = matcher.group(4);
          final String type3Group = matcher.group(5);
          final String type;
          final String plural;
          final String accusative;

          if (accGroup == null) {
            accusative = "xxx";
          } else {
            accusative = accGroup.toLowerCase().equals("n") ? "akz" : "nak";
          }
          if (plGroup == null) {
            plural = " pn ";
          } else {
            plural = plGroup.toLowerCase().equals("j") ? " pl " : " np ";
          }
          type = ((type2Group == null) ? type3Group : type2Group).toLowerCase();

          l.add(new AnalyzedToken(word, "T " + 
            accusative + plural + type1Group + " " + type, null));

          if ((matcher = patternTabelvortoAdverb.matcher(lWord)).find()) {
            l.add(new AnalyzedToken(word, "E nak", lWord));
          }

        // Words ending in .*oj?n? are nouns.
        } else if (lWord.endsWith("o")) {
          l.add(new AnalyzedToken(word, "O nak np", lWord));
        } else if (lWord.length() >= 2 && lWord.endsWith("'")) {
          l.add(new AnalyzedToken(word, "O nak np", lWord.substring(0, lWord.length() - 1) + "o"));
        } else if (lWord.endsWith("oj")) {
          l.add(new AnalyzedToken(word, "O nak pl", lWord.substring(0, lWord.length() - 1)));
        } else if (lWord.endsWith("on")) {
          l.add(new AnalyzedToken(word, "O akz np", lWord.substring(0, lWord.length() - 1)));
        } else if (lWord.endsWith("ojn")) {
          l.add(new AnalyzedToken(word, "O akz pl", lWord.substring(0, lWord.length() - 2)));

        // Words ending in .*aj?n? are nouns.
        } else if (lWord.endsWith("a")) {
          l.add(new AnalyzedToken(word, "A nak np", lWord));
        } else if (lWord.endsWith("aj")) {
          l.add(new AnalyzedToken(word, "A nak pl", lWord.substring(0, lWord.length() - 1)));
        } else if (lWord.endsWith("an")) {
          l.add(new AnalyzedToken(word, "A akz np", lWord.substring(0, lWord.length() - 1)));
        } else if (lWord.endsWith("ajn")) {
          l.add(new AnalyzedToken(word, "A akz pl", lWord.substring(0, lWord.length() - 2)));

        // Words ending in .*en? are adverbs.
        } else if (lWord.endsWith("e")) {
          l.add(new AnalyzedToken(word, "E nak", lWord));
        } else if (lWord.endsWith("en")) {
          l.add(new AnalyzedToken(word, "E akz", lWord.substring(0, lWord.length() - 1)));

        // Verbs.
        } else if ((matcher = patternVerb.matcher(lWord)).find()) {
          final String verb = matcher.group(1) + "i";
          final String tense = matcher.group(2);
          final String transitive = findTransitivity(verb);

          l.add(new AnalyzedToken(word, "V " + transitive + " " + tense, verb));

        // Irregular word (no tag).
        } else {
          l.add(new AnalyzedToken(word, null, null));
        }

        // Participle (can be combined with other tags).
        if ((matcher = patternParticiple.matcher(lWord)).find()) {
          if (!setNonParticiple.contains(matcher.group(1))) {
            final String verb = matcher.group(2) + "i";
            final String aio = matcher.group(3);
            final String antAt = matcher.group(4).equals("n") ? "n" : "-";
            final String aoe = matcher.group(5);
            final String plural = matcher.group(6).equals("j") ? "pl" : "np";
            final String accusative = matcher.group(7).equals("n") ? "akz" : "nak";
            final String transitive = findTransitivity(verb);

            l.add(new AnalyzedToken(word, "C " + accusative + " " + plural + " " +
                                    transitive + " " + aio + " " + antAt + " " + aoe,
                                    verb));
          }
        }
      }

      pos += word.length();
      tokenReadings.add(new AnalyzedTokenReadings(
        l.toArray(new AnalyzedToken[0]), 0));
    }
    return tokenReadings;
  }

  @Override
  public AnalyzedTokenReadings createNullToken(String token, int startPos) {
    return new AnalyzedTokenReadings(
      new AnalyzedToken(token, null, null), startPos);
  }

  @Override
  public AnalyzedToken createToken(String token, String posTag) {
    return new AnalyzedToken(token, posTag, null);
  }
}
