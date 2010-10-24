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

/**
 * A part-of-speech tagger for Esperanto.
 *
 * @author Dominique Pellé
 */
public class EsperantoTagger implements Tagger {

  // These words don't need to be tagged.
  private final static String wordsNotTagged[] = {
    "ajn", "ĉi", "des", "ja", "ju", "malpli", "ne", "nek", "ol", "pli"
  };

  private final static Set setWordsNotTagged = new HashSet<String>(Arrays.asList(wordsNotTagged));

  // Following preposition are never followed by accusative.
  private final static String prepositionsNoAccusative[] = {
      "al", "anstataŭ", "apud", "cis", "da", "de", "dum", "el", "far",
      "ĝis", "je", "kontraŭ", "krom", "kun", "laŭ", "malgraŭ", "na",
      "per", "po", "post", "por", "pri", "pro", "sen", "super", "tra"
  };

  private final static Set setPrepositionsNoAccusative =
    new HashSet<String>(Arrays.asList(prepositionsNoAccusative));

  // Following preposition may be followed by accusative.
  private final static String prepositionsAccusative[] = {
    "en", "sur", "sub", "trans", "preter", "ĉirkaŭ", "antaŭ", "ekster",
    "inter", "ĉe"
  };

  private final Set setPrepositionsAccusative =
    new HashSet<String>(Arrays.asList(prepositionsAccusative));

  // Conjunctions.
  private final static String conjunctions[] = {
    "ĉar", "kaj", "aŭ", "sed", "plus", "minus", "tamen"
  };

  private final static Set setConjunctions = new HashSet<String>(Arrays.asList(conjunctions));

  // Numbers.
  private final static String numbers[] = {
    "nul", "unu", "du", "tri", "kvar", "kvin", "ses",
    "sep", "ok", "naŭ", "dek", "cent", "mil"
  };

  private final static Set setNumbers = new HashSet<String>(Arrays.asList(numbers));

  // Adverbs which do not end in -e
  private final static String adverbs[] = {
    "ankoraŭ", "almenaŭ", "apenaŭ", "baldaŭ", "preskaŭ", "eĉ",
    "jam", "jen", "ĵus", "morgaŭ", "hodiaŭ", "hieraŭ", "nun",
    "nur", "plu", "tre", "tro", "tuj", "for"
  };

  private final static Set setAdverbs = new HashSet<String>(Arrays.asList(adverbs));

  // Set of transitive verbs and non-transitive verbs.
  private Set setTransitiveVerbs = null;
  private Set setNonTransitiveVerbs = null;

  // Verbs always end with this pattern.
  private final static Pattern patternVerb1 = Pattern.compile("(.*)(as|os|is|us|u|i)$");
  private final static Pattern patternVerb2 = Pattern.compile(".*(ig|iĝ)(.s|.)$");

  // Particips -ant-, -int, ont-, -it-, -it-, -ot-
  // TODO: this is not used yet.
  final Pattern patternParticip =
    Pattern.compile("(.*)([aio])(n?)t([aoe])(j?)(n?)");
  // Groups           11  22222  33   44444  55  66

  // Pattern 'tabelvortoj'.
  final Pattern patternTabelvorto = 
    Pattern.compile("^(i|ti|ki|ĉi|neni)((([uoae])(j?)(n?))|(am|al|es|el|om))$");
  // Groups            111111111111111  22222222222222222222222222222222
  //                                     3333333333333333   77777777777
  //                                      444444  55  66                  

  /**
   * Load list of words from UTF-8 file (one word per line).
   */
  private Set loadWords(final InputStream file) throws IOException {
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
    if (setTransitiveVerbs != null) {
      return;
    }

    // Load set of transitive and non-transitive verbs.  Files don't contain
    // verbs with suffix -iĝ or -ig since transitivity is obvious for those verbs.
    // They also don't contain verbs with prefixes mal-, ek-, re-, mis- fi- and
    // suffixes -ad, -aĉ, -et, -eg since these affixes never alter transitivity.
    setTransitiveVerbs    = loadWords(JLanguageTool.getDataBroker().getFromRulesDirAsStream("/eo/verb-tr.txt"));
    setNonTransitiveVerbs = loadWords(JLanguageTool.getDataBroker().getFromRulesDirAsStream("/eo/verb-ntr.txt"));
  }

  public List<AnalyzedTokenReadings> tag(final List<String> sentenceTokens) throws IOException {

    lazyInit();

    Matcher matcher;

    final List<AnalyzedTokenReadings> tokenReadings = 
      new ArrayList<AnalyzedTokenReadings>();
    int pos = 0;
    for (String word : sentenceTokens) {
      final List<AnalyzedToken> l = new ArrayList<AnalyzedToken>();
      final String lWord = word.toLowerCase();

      if (lWord.equals(".")) {
        l.add(new AnalyzedToken(word, "M fino", lWord));

      } else if (lWord.equals("?")) {
        l.add(new AnalyzedToken(word, "M fino dem", lWord));

      } else if (lWord.equals("!")) {
        l.add(new AnalyzedToken(word, "M fino kri", lWord));

      } else if (lWord.equals("la")) {
        l.add(new AnalyzedToken(word, "D", lWord));

      } else if (setAdverbs.contains(lWord)) {
        l.add(new AnalyzedToken(word, "E nak", lWord));

      } else if (setWordsNotTagged.contains(lWord)) {
        l.add(new AnalyzedToken(word, null, lWord));

      // Pronouns.
      } else if (lWord.equals("mi") || lWord.equals("ci")
              || lWord.equals("li") || lWord.equals("ŝi")
              || lWord.equals("ĝi") || lWord.equals("si")
              || lWord.equals("oni")) {
        l.add(new AnalyzedToken(word, "R nak np", lWord));
      } else if (lWord.equals("min") || lWord.equals("cin")
              || lWord.equals("lin") || lWord.equals("ŝin") 
              || lWord.equals("ĝin") || lWord.equals("sin")) {
        l.add(new AnalyzedToken(word, "R akz np", lWord.substring(0, lWord.length() - 1)));
      } else if (lWord.equals("ni") || lWord.equals("ili")) {
        l.add(new AnalyzedToken(word, "R nak pl", lWord));
      } else if (lWord.equals("nin") || lWord.equals("ilin")) {
        l.add(new AnalyzedToken(word, "R akz pl", lWord.substring(0, lWord.length() - 1)));
      } else if (lWord.equals("vi")) {
        l.add(new AnalyzedToken(word, "R nak pn", lWord));
      } else if (lWord.equals("vin")) {
        l.add(new AnalyzedToken(word, "R akz pn", lWord.substring(0, lWord.length() - 1)));

      // Conjunctions (kaj, sed, ...)
      } else if (setConjunctions.contains(lWord)) {
        l.add(new AnalyzedToken(word, "K", lWord));
  
      // Prepositions.
      } else if (setPrepositionsNoAccusative.contains(lWord)) {
        l.add(new AnalyzedToken(word, "P sak", lWord));
      } else if (setPrepositionsAccusative.contains(lWord)) {
        l.add(new AnalyzedToken(word, "P kak", lWord));

      } else if (setNumbers.contains(lWord)) {
        l.add(new AnalyzedToken(word, "N", lWord));

      // Tiu, kiu (tabelvortoj).
      } else if ((matcher = patternTabelvorto.matcher(lWord)).find()) {
        final String type1Group = matcher.group(1).substring(0, 1).toLowerCase();
        final String type2Group = matcher.group(4);
        final String plGroup    = matcher.group(5);
        final String accGroup   = matcher.group(6);
        final String type3Group = matcher.group(7);
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

      // Words ending in .*oj?n? are nouns.
      } else if (lWord.endsWith("o")) {
        l.add(new AnalyzedToken(word, "O nak np", lWord));
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
      } else if ((matcher = patternVerb1.matcher(lWord)).find()) {
        final String verb = matcher.group(1) + "i";
        final String tense = matcher.group(2);
        final String transitive;

        final Matcher matcher2 = patternVerb2.matcher(lWord);
        if (matcher2.find()) {
          transitive = matcher2.group(1).equals("ig") ? "tr" : "nt";
        } else {
          final boolean isTransitive   = setTransitiveVerbs.contains(verb);
          final boolean isIntransitive = setNonTransitiveVerbs.contains(verb);

          if (isTransitive) {
            transitive = isIntransitive ? "tn" : "tr";
          } else {
            transitive = isIntransitive ? "nt" : "tn";
          }
        }
        l.add(new AnalyzedToken(word, "V " + transitive + " " + tense, verb));

      // Irregular word (no tag).
      } else {
        l.add(new AnalyzedToken(word, null, null));
      }
      pos += word.length();
      tokenReadings.add(new AnalyzedTokenReadings(
        l.toArray(new AnalyzedToken[0]), 0));
    }
    return tokenReadings;
  }

  public AnalyzedTokenReadings createNullToken(String token, int startPos) {
    return new AnalyzedTokenReadings(
      new AnalyzedToken(token, null, null), startPos);
  }

  public AnalyzedToken createToken(String token, String posTag) {
    return new AnalyzedToken(token, posTag, null);
  }
}
