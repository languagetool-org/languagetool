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

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.tagging.Tagger;

/**
 * A part-of-speech tagger for Esperanto.
 *
 * @author Dominique Pellé
 */
public class EsperantoTagger implements Tagger {

  // These words don't need to be tagged.
  private final static String wordsNotTagged[] = {
    "ne", "nek", "pli", "malpli", "ol", "ju", "des"
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

  // Stem of transitive and intransitive verbs.  A few verbs can be both
  // transitive and intransitive. Such verbs may appear in both ntrVerb[]
  // and trVerbs[].

  // Intransitive verbs (TODO: this should be stored in a dictionary).
  // No need to list verbs with suffix iĝ suffix since those are
  // always intransitive.
  private final static String ntrVerbs[] = {
    "abort",
    "abstin",
    "ag",
    "agoni",
    "aĝi",
    "akord",
    "altern",
    "aparten",
    "apelaci",
    "aper",
    "aspekt",
    "at",
    "atut",
    "aŭdac",
    "aviad",
    "balot",
    "bankrot",
    "barakt",
    "batal",
    "bicikl",
    "blasfem",
    "blek",
    "blov",
    "boj",
    "boks",
    "bol",
    "bril",
    "brokant",
    "bru",
    "brul",
    "cirkul",
    "daŭr",
    "degel",
    "dron",
    "eksplod",
    "est",
    "evolu",
    "fal",
    "grimp",
    "halt",
    "ir",
    "koler",
    "kresk",
    "krev",
    "labor",
    "mir",
    "odor",
    "okaz",
    "parol",
    "pend",
    "porol",
    "rid",
    "sid",
    "star",
    "velk",
    "ven",
    "vetur",
    "zorg",
    "ĉes",
    "ĝoj",
    "ŝpruc",
    "ŝrump",
    "ŝvel",
  };

  private final static Set setNtrVerbs = new HashSet<String>(Arrays.asList(ntrVerbs));

  // Verbs always end with this pattern.
  private final static Pattern patternVerb1 = Pattern.compile("(.*)(as|os|is|us|u|i)$");
  private final static Pattern patternVerb2 = Pattern.compile(".*(ig|iĝ)(.s|.)$");

  // Transitive verbs (TODO: this should be stored in a dictionary).
  // No need to list verbs with suffix ig suffix since those are
  // always transitive.
  private final static String trVerbs[] = {
    "balanc",
    "ban",
    "bonven",
    "bukl",
    "dezir",
    "difin",
    "dolor",
    "duŝ",
    "enhav",
    "etend",
    "far",
    "fend",
    "ferm",
    "fin",
    "fleks",
    "forges",
    "hav",
    "interes",
    "kaŝ",
    "kirl",
    "klin",
    "kolekt",
    "komenc",
    "komplik",
    "komunik",
    "konduk",
    "korekt",
    "lav",
    "lig",
    "lud",
    "manĝ",
    "memor",
    "mov",
    "mezur",
    "miks",
    "mov",
    "nask",
    "naŭz",
    "pag",
    "parol",
    "paŝt",
    "pes",
    "perd",
    "profit",
    "renkont",
    "renvers",
    "romp",
    "rul",
    "sci",
    "send",
    "sent",
    "sku",
    "spekt",
    "streĉ",
    "sufok",
    "sving",
    "ted",
    "tim",
    "tir",
    "tord",
    "tranĉ",
    "translok",
    "tren",
    "turn",
    "uz",
    "vek",
    "vend",
    "venĝ",
    "verŝ",
    "vest",
    "vid",
    "vind",
    "vol",
    "volv",
    "ŝancel",
    "ŝanĝ",
    "ŝlos",
    "ŝir",
    "ŝut",
  };

  private final static Set setTrVerbs = new HashSet<String>(Arrays.asList(trVerbs));

  public List<AnalyzedTokenReadings> tag(final List<String> sentenceTokens) {

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
    Matcher matcher;

    final List<AnalyzedTokenReadings> tokenReadings = 
      new ArrayList<AnalyzedTokenReadings>();
    int pos = 0;
    for (String word : sentenceTokens) {
      final List<AnalyzedToken> l = new ArrayList<AnalyzedToken>();
      final String lWord = word.toLowerCase();

      if (lWord.equals(".")) {
        l.add(new AnalyzedToken(word, "M fino", null));

      } else if (lWord.equals("?")) {
        l.add(new AnalyzedToken(word, "M fino dem", null));

      } else if (lWord.equals("!")) {
        l.add(new AnalyzedToken(word, "M fino kri", null));

      } else if (lWord.equals("la")) {
        l.add(new AnalyzedToken(word, "D", null));


      } else if (setAdverbs.contains(lWord)) {
        l.add(new AnalyzedToken(word, "E nak", null));

      } else if (setWordsNotTagged.contains(lWord)) {
        l.add(new AnalyzedToken(word, null, null));

      // Pronouns.
      } else if (lWord.equals("mi") || lWord.equals("ci")
              || lWord.equals("li")
              || lWord.equals("ŝi") || lWord.equals("oni")) {
        l.add(new AnalyzedToken(word, "R nak np", null));
      } else if (lWord.equals("min") || lWord.equals("cin")
             ||  lWord.equals("lin") || lWord.equals("ŝin")) {
        l.add(new AnalyzedToken(word, "R akz np", null));
      } else if (lWord.equals("ni") || lWord.equals("ili")) {
        l.add(new AnalyzedToken(word, "R nak pl", null));
      } else if (lWord.equals("nin") || lWord.equals("ilin")) {
        l.add(new AnalyzedToken(word, "R akz pl", null));
      } else if (lWord.equals("vi")) {
        l.add(new AnalyzedToken(word, "R nak pn", null));
      } else if (lWord.equals("vin")) {
        l.add(new AnalyzedToken(word, "R akz pn", null));

      // Conjunctions (kaj, sed, ...)
      } else if (setConjunctions.contains(lWord)) {
        l.add(new AnalyzedToken(word, "K", null));
  
      // Prepositions.
      } else if (setPrepositionsNoAccusative.contains(lWord)) {
        l.add(new AnalyzedToken(word, "P sak", null));
      } else if (setPrepositionsAccusative.contains(lWord)) {
        l.add(new AnalyzedToken(word, "P kak", null));

      } else if (setNumbers.contains(lWord)) {
        l.add(new AnalyzedToken(word, "B", null));

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
        l.add(new AnalyzedToken(word, "O nak np", null));
      } else if (lWord.endsWith("oj")) {
        l.add(new AnalyzedToken(word, "O nak pl", null));
      } else if (lWord.endsWith("on")) {
        l.add(new AnalyzedToken(word, "O akz np", null));
      } else if (lWord.endsWith("ojn")) {
        l.add(new AnalyzedToken(word, "O akz pl", null));

      // Words ending in .*aj?n? are nouns.
      } else if (lWord.endsWith("a")) {
        l.add(new AnalyzedToken(word, "A nak np", null));
      } else if (lWord.endsWith("aj")) {
        l.add(new AnalyzedToken(word, "A nak pl", null));
      } else if (lWord.endsWith("an")) {
        l.add(new AnalyzedToken(word, "A akz np", null));
      } else if (lWord.endsWith("ajn")) {
        l.add(new AnalyzedToken(word, "A akz pl", null));

      // Words ending in .*en? are adverbs.
      } else if (lWord.endsWith("e")) {
        l.add(new AnalyzedToken(word, "E nak", null));
      } else if (lWord.endsWith("en")) {
        l.add(new AnalyzedToken(word, "E akz", null));

      // Verbs.
      } else if ((matcher = patternVerb1.matcher(lWord)).find()) {
        final String tense = matcher.group(2);
        final String transitive;

        final Matcher matcher2 = patternVerb2.matcher(lWord);
        if (matcher2.find()) {
          transitive = matcher2.group(1).equals("ig") ? "tr" : "nt";
        } else {
          final String verb = matcher.group(1);
          final boolean isTransitive   = setTrVerbs .contains(verb);
          final boolean isIntransitive = setNtrVerbs.contains(verb);

          if (isTransitive) {
            transitive = isIntransitive ? "tn" : "tr";
          } else {
            transitive = isIntransitive ? "nt" : "tn";
          }
        }
        l.add(new AnalyzedToken(word, "V " + transitive + " " + tense, null));

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
