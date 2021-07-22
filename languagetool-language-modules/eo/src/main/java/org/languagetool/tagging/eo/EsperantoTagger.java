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
package org.languagetool.tagging.eo;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.tagging.ManualTagger;
import org.languagetool.tagging.TaggedWord;
import org.languagetool.tagging.Tagger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

  // Transform a string such as "jxauxdo" into "ĵaŭdo".
  //
  // We only care about lower case, as this is always
  // invoked on the lemma which has already been transformed
  // into lower case.
  private static String xSystemToUnicode(String s) {
    String result = "";
    int length = s.length();

    for (int i = 0; i < length; i++){
      char c1 = s.charAt(i);
      char c2 = (i + 1 < length) ? s.charAt(i + 1) : ' ';

      if (c2 == 'x') {

        switch (c1) {
          case 'c': result += 'ĉ'; ++i; break;
          case 'g': result += 'ĝ'; ++i; break;
          case 'h': result += 'ĥ'; ++i; break;
          case 'j': result += 'ĵ'; ++i; break;
          case 's': result += 'ŝ'; ++i; break;
          case 'u': result += 'ŭ'; ++i; break;

          default: result += c1; break;
        }
      } else {
        result += c1;
      }
    }
    return result;
  }

  /**
   * Load list of words from UTF-8 file (one word per line).
   */
  private Set<String> loadWords(InputStream stream) throws IOException {
    Set<String> words = new HashSet<>();
    try (
      InputStreamReader isr = new InputStreamReader(stream, StandardCharsets.UTF_8);
      BufferedReader br = new BufferedReader(isr)
    ) {
      String line;
      while ((line = br.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty() || line.charAt(0) == '#') {  // ignore comments
          continue;
        }
        words.add(line);
      }
    }
    return words;
  }

  private synchronized void lazyInit() throws IOException {
    if (manualTagger != null) {
      return;
    }

    // A manual tagger is used for closed words.  Closed words are the small
    // limited set of words in Esperanto which have no standard ending, as
    // opposed to open words which are unlimited in numbers and which follow
    // strict rules for their suffixes.
    try (InputStream stream = JLanguageTool.getDataBroker().getFromResourceDirAsStream("/eo/manual-tagger.txt")) {
      manualTagger = new ManualTagger(stream);
    }

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
      boolean isTransitive   = setTransitiveVerbs.contains(verb);
      boolean isIntransitive = setIntransitiveVerbs.contains(verb);

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
      Matcher matcherPrefix = patternPrefix.matcher(verb);
      if (matcherPrefix.find()) {
        // Remove a prefix and try again.
        verb = matcherPrefix.group(1);
        continue;
      }
      Matcher matcherSuffix = patternSuffix.matcher(verb);
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
  public List<AnalyzedTokenReadings> tag(List<String> sentenceTokens) throws IOException {

    lazyInit();
    Matcher matcher;

    List<AnalyzedTokenReadings> tokenReadings =
      new ArrayList<>();
    for (String word : sentenceTokens) {
      List<AnalyzedToken> l = new ArrayList<>();

      if (word.length() > 50) {
        // avoid excessively long computation times for long (probably artificial) tokens:
        l.add(new AnalyzedToken(word, null, null));
      } else if (word.length() > 1) {
        // No Esperanto word is made of one letter only. This check avoids
        // spurious tagging as single letter words "A", "O", "E", etc.
        // Lemma contains words in lower case, and with Unicode transcription (as opposed
        // to x-system).
        String lWord = xSystemToUnicode(word.toLowerCase());
        List<TaggedWord> manualTags = manualTagger.tag(lWord);

        if (manualTags.size() > 0) {
          // This is a closed word for which we know its lemmas and tags.
          for (TaggedWord manualTag : manualTags) {
            l.add(new AnalyzedToken(word, manualTag.getPosTag(), manualTag.getLemma()));
          }
        } else {
          // This is an open word, we need to look at the word ending
          // to determine its lemma and POS tag.  For verb, we also
          // need to look up the dictionary of known transitive and
          // intransitive verbs.

          // Tiu, kiu (tabelvortoj).
          if ((matcher = patternTabelvorto.matcher(lWord)).find()) {
            String type1Group = matcher.group(1).substring(0, 1).toLowerCase();
            String type2Group = matcher.group(2);
            String plGroup    = matcher.group(3);
            String accGroup   = matcher.group(4);
            String type3Group = matcher.group(5);
            String type;
            String plural;
            String accusative;

            if (accGroup == null) {
              accusative = "xxx";
            } else {
              accusative = accGroup.equalsIgnoreCase("n") ? "akz" : "nak";
            }
            if (plGroup == null) {
              plural = " pn ";
            } else {
              plural = plGroup.equalsIgnoreCase("j") ? " pl " : " np ";
            }
            type = ((type2Group == null) ? type3Group : type2Group).toLowerCase();

            l.add(new AnalyzedToken(word, "T " +
              accusative + plural + type1Group + " " + type, null));

            if (patternTabelvortoAdverb.matcher(lWord).find()) {
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

          // Words ending in .*aj?n? are adjectives.
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
            String verb = matcher.group(1) + "i";
            String tense = matcher.group(2);
            String transitive = findTransitivity(verb);

            l.add(new AnalyzedToken(word, "V " + transitive + " " + tense, verb));

          // Irregular word (no tag).
          } else {
            l.add(new AnalyzedToken(word, null, null));
          }

          // Participle (can be combined with other tags).
          if ((matcher = patternParticiple.matcher(lWord)).find()) {
            if (!setNonParticiple.contains(matcher.group(1))) {
              String verb = matcher.group(2) + "i";
              String aio = matcher.group(3);
              String antAt = matcher.group(4).equals("n") ? "n" : "-";
              String aoe = matcher.group(5);
              String plural = matcher.group(6).equals("j") ? "pl" : "np";
              String accusative = matcher.group(7).equals("n") ? "akz" : "nak";
              String transitive = findTransitivity(verb);

              l.add(new AnalyzedToken(word, "C " + accusative + " " + plural + " " +
                                      transitive + " " + aio + " " + antAt + " " + aoe,
                                      verb));
            }
          }
        }
      } else {
        // Single letter word (no tag).
        l.add(new AnalyzedToken(word, null, null));
      }
      tokenReadings.add(new AnalyzedTokenReadings(l, 0));
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
