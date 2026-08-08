/* LanguageTool, a natural language style checker
 * Copyright (C) 2026 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.ca;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.chunking.ChunkTag;
import org.languagetool.language.Catalan;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.synthesis.ca.VerbSynthesizer;
import org.languagetool.tagging.ca.VerbClassifier;

import static org.languagetool.rules.ca.PronomsFeblesHelper.PronounPosition.NORMALIZED;

/**
 * Filtre compartit per a les regles de "que/què" a l'inici de frase interrogativa.
 *
 * <p>Decideix si, a l'inici, cal escriure "que" (interrogativa de sí/no) o "què"
 * (equival a "quina cosa": subjecte o complement directe). El model analitza el verb
 * principal (via el chunk GV), els pronoms febles, la transitivitat del verb
 * ({@link VerbClassifier}) i si el rol de "què" (CD o subjecte) queda buit o ja està
 * ocupat per un complement/subjecte posposat.
 *
 * <p>Segons la direcció de la regla que l'invoca:
 * <ul>
 *   <li>AMB (que -&gt; què): confirma el match només si la predicció és "què".</li>
 *   <li>SENSE (què -&gt; que): confirma el match només si la predicció és "que".</li>
 * </ul>
 * Si no es pot decidir, deixa passar el match tal com el dona la regla.
 */
public class QueIniciFilter extends RuleFilter {

  private static final Pattern PRONOM = Pattern.compile("P0.{6}|PP3CN000|PP3NN000|PP3..A00|PP[123]CP000|PP3CSD00");
  private static final Pattern FINITE_VERB = Pattern.compile("V.[SI].*");
  private static final Pattern ANY_VERB = Pattern.compile("V.*");
  private static final Pattern DET_SINGULAR = Pattern.compile("D...S.");
  private static final Pattern DET_PLURAL = Pattern.compile("D...P.");
  private static final Pattern DET = Pattern.compile("D.*");
  private static final Pattern VERB_OR_PRONOM =
      Pattern.compile("V.[SI].*|P0.{6}|PP3CN000|PP3NN000|PP3..A00|PP[123]CP000|PP3CSD00");

  private static final ChunkTag PTIME_CHUNK = new ChunkTag("PTime");
  private static final ChunkTag CVERB_CHUNK = new ChunkTag("CVerb");

  private static final List<String> SINO_LAST_WORDS = Arrays.asList("potser", "oi");
  // Verbs copulatius: al classificador surten com a intransitius, però amb ells "què" fa
  // d'atribut/subjecte; els traiem del resultat intransitiu i deixem que la lògica general
  // de complements decideixi ("Que pot ser més versemblant?" -> "Què...").
  private static final Set<String> COPULAR_VERBS = new HashSet<>(Arrays.asList(
      "ser", "ésser", "estar", "semblar", "parèixer"));
  // Noms comuns animats que poden ser subjecte posposat ("Que vol aquesta gent?" -> "Què...").
  // Llista llavor; s'anirà ampliant amb la recopilació del corpus.
  private static final Set<String> ANIMATE_SUBJECT_NOUNS = new HashSet<>(Arrays.asList(
      "persona", "gent", "home", "dona", "noi", "noia", "nen", "nena", "nan", "senyor",
      "senyora", "senyoreta", "pare", "mare", "fill", "filla", "germà", "germana",
      "amic", "amiga", "professor", "professora", "mestre", "mestra", "metge", "metgessa",
      "doctor", "doctora", "alcalde", "rei", "reina", "policia", "nadó", "avi", "àvia",
      "tia", "tio", "oncle", "cosí", "cosina", "veí", "veïna", "client", "clienta",
      "jutge", "gos", "gossa", "gat", "gata", "nena", "criatura", "company", "companya",
      "merdós", "cabró", "imbècil", "idiota", "desgraciat", "malparit", "beneit", "tio",
      "individu", "paio", "tipus", "subjecte", "element", "marit", "muller", "xiquet"));
  private static final Set<String> NEVER_SUBJECT_NOUNS = new HashSet<>(Arrays.asList(
      "cop", "vegada", "volta", "mica", "miqueta",
      "dilluns", "dimarts", "dimecres", "dijous", "divendres", "dissabte", "diumenge",
      "dia", "nit", "matí", "tarda", "vespre", "hora", "temps", "setmana", "mes", "any"));
  private static final Set<String> TEMPORAL_ADJUNCT_NOUNS = new HashSet<>(Arrays.asList(
      "cop", "vegada", "volta",
      "dilluns", "dimarts", "dimecres", "dijous", "divendres", "dissabte", "diumenge",
      "dia", "nit", "nit_1", "matí", "tarda", "vespre", "hora", "temps", "setmana", "mes", "any"));
  private static final Set<String> WEEKDAY_NOUNS = new HashSet<>(Arrays.asList(
      "dilluns", "dimarts", "dimecres", "dijous", "divendres", "dissabte", "diumenge"));
  private static final Set<String> ACCUSATIVE_PRONOUNS = new HashSet<>(Arrays.asList("em", "et", "el", "la", "ens",
    "us", "les", "els"));
  private static final Set<String> DATIVE_PRONOUNS = new HashSet<>(Arrays.asList("em", "et", "li", "ens", "us", "els"));
  private static final Set<String> INTERROGATIVE_WORDS = new HashSet<>(Arrays.asList(
    "que", "què", "quin", "quina", "quins", "quines", "qui", "quant", "quants", "quanta", "quantes", "res"));

  // que/què com a paraula sencera (usat per helpers auxiliars del test)
  private static final Set<String> CONFIRMATION_TAGS = new HashSet<>(Arrays.asList(
      "veritat", "oi", "eh", "no", "cert", "potser"));
  private static final Set<String> EXPLETIVES = new HashSet<>(Arrays.asList(
      "collons", "coi", "cony", "dimonis", "dimoni", "carai", "diantre", "caram",
      "carall", "punyeta", "punyetes", "redimonis", "diables", "diable", "hòstia",
      "dimontri", "dimontris", "redéu", "redeu", "punyetera", "leche", "putes",
      "fotons", "llamps", "trons"));

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
                                   AnalyzedTokenReadings[] patternTokens, List<Integer> tokenPositions) {
    String id = match.getRule().getId();
    boolean isAmb = id.contains("AMBACCENT") || id.contains("AMB_ACCENT");
    boolean isSense = id.contains("SENSEACCENT") || id.contains("SENSE_ACCENT");
    if (!isAmb && !isSense) {
      return match;
    }
    Boolean predictsAccent = predictsAccent(match.getSentence(), getLanguageFromRuleMatch(match));
    if (predictsAccent == null) {
      return match;
    }
    if (isAmb) {
      // Text "que", suggerim "què": confirmem només si la predicció és "què".
      return predictsAccent ? match : null;
    }
    // SENSE: text "què", suggerim "que": confirmem només si la predicció és "que".
    return predictsAccent ? null : match;
  }

  /**
   * Prediu si l'inici interrogatiu ha de portar accent ("què", quina cosa) o no ("que", sí/no).
   * Retorna null si no es pot analitzar.
   */
  private Boolean predictsAccent(AnalyzedSentence sentence, Language language) {
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    try {
      int quePos = findQue(tokens);
      if (quePos < 0) {
        return null;
      }
      boolean startsWithAccent = tokens[quePos].getToken().equalsIgnoreCase("què");
      int questionPos = findQuestionMark(tokens, quePos);
      int coreEnd = questionPos >= 0 ? stripConfirmationTag(tokens, quePos, questionPos) : tokens.length;
      String lastWord = "";
      boolean isSiNoQuestion = false;
      if (tokens.length > 2) {
        lastWord = tokens[tokens.length - 2].getToken();
        isSiNoQuestion = SINO_LAST_WORDS.contains(lastWord.toLowerCase());
        isSiNoQuestion = isSiNoQuestion
            || (tokens[tokens.length - 2].getToken().equals("què") && tokens[tokens.length - 1].getToken().equals("o"));
        isSiNoQuestion = isSiNoQuestion
            || (tokens[tokens.length - 2].getToken().equals("no") && tokens[tokens.length - 1].getToken().equals(","));
      }
      boolean isAnotherSubject = lastWord.equals("això");

      // Subjecte dislocat al final (", el professor?"): si el subjecte ja hi és, el SN
      // postverbal és el CD -> no s'ha de comptar com a possible subjecte.
      boolean trailingDislocation = false;
      int j = tokens.length - 2;
      boolean sawNominal = false;
      while (j > quePos && tokens[j].matchesPosTagRegex("D.*|N.*|A.*|PP3.*|PX.*|PD.*")) {
        sawNominal = true;
        j--;
      }
      if (sawNominal && j > quePos && tokens[j].getToken().equals(",")) {
        trailingDislocation = true;
      }

      List<String> pronoms = new ArrayList<>();
      boolean verb3s = false;
      boolean verb3p = false;
      boolean verb2p = false;
      String mainVerbLemma = "";
      String firstPostagAfterVerb = "";
      int firstTokenAfterVerbPos = -1;
      int mainVerbPos = -1;
      VerbGroupInfo verbGroup = verbGroupAfterQue(tokens, quePos, coreEnd, language);
      if (verbGroup != null) {
        int firstVerbPos = verbGroup.firstVerbIndex;
        verb3s = tokens[firstVerbPos].matchesPosTagRegex("V.[SI].3S.*");
        verb3p = tokens[firstVerbPos].matchesPosTagRegex("V.[SI].3P.*");
        verb2p = tokens[firstVerbPos].matchesPosTagRegex("V.[SI].2P.*");
        mainVerbPos = verbGroup.lastVerbIndex;
        AnalyzedToken mainVerbReading = tokens[mainVerbPos].readingWithTagRegex(ANY_VERB);
        if (mainVerbReading != null) {
          mainVerbLemma = mainVerbReading.getLemma();
        }
        for (int i = firstVerbPos - verbGroup.numPronounsBefore; i < firstVerbPos; i++) {
          addPronounToken(tokens, i, pronoms);
        }
        for (int i = mainVerbPos + 1; i <= verbGroup.lastIndex; i++) {
          addPronounToken(tokens, i, pronoms);
        }
        firstTokenAfterVerbPos = verbGroup.lastIndex + 1;
        AnalyzedToken at = tokens[firstTokenAfterVerbPos].readingWithTagRegex(".*");
        firstPostagAfterVerb = at != null && at.getPOSTag() != null ? at.getPOSTag() : "UNKNOWN";
      }

      boolean hasHo = pronoms.contains("ho");
      boolean hasAccusativePronoun = false;
      boolean hasDativePronoun = false;
      boolean hasAccusativeNotDativePronoun = false;
      for (String p : pronoms) {
        if (ACCUSATIVE_PRONOUNS.contains(p)) {
          hasAccusativePronoun = true;
        }
        if (DATIVE_PRONOUNS.contains(p)) {
          hasDativePronoun = true;
        }
        if (ACCUSATIVE_PRONOUNS.contains(p) && !DATIVE_PRONOUNS.contains(p)) {
          hasAccusativeNotDativePronoun = true;
        }
      }

      String pronomStr = "";
      if (!pronoms.isEmpty()) {
        StringBuilder sb = new StringBuilder();
        for (String p : pronoms) {
          sb.append(p).append(" ");
        }
        pronomStr = PronomsFeblesHelper.transform(
            sb.toString().replaceAll(" -", "-").replaceAll(" '", "'").replaceAll("' ", "'").trim(),
            PronomsFeblesHelper.PronounPosition.DAVANT).replace("'", "").replaceAll(" ", "").trim();
      }

      // El complement posposat pot ser subjecte? Esbiaixem cap a "objecte": només si el nucli
      // del SN és un nom propi o un nom comú animat (i concorda en nombre amb el verb). Els SN
      // de nom comú inanimat es tracten com a CD (-> "que").
      boolean complementCanBeSubject = (verb3s || verb3p || verb2p) && firstTokenAfterVerbPos > 0
          && firstTokenAfterVerbPos < tokens.length
          && postverbalCanBeSubject(tokens, firstTokenAfterVerbPos, verb3s, verb3p, verb2p);

      // El complement posposat pot ser complement directe?
      boolean complementCanBeObject = hasAccusativeNotDativePronoun;
      if (firstPostagAfterVerb.startsWith("N")) {
        complementCanBeObject = true;
      } else if (firstTokenAfterVerbPos > 0 && firstTokenAfterVerbPos + 1 < tokens.length
          && (tokens[firstTokenAfterVerbPos].matchesPosTagRegex(DET)
          || INTERROGATIVE_WORDS.contains(tokens[firstTokenAfterVerbPos].getToken().toLowerCase()))) {
        complementCanBeObject = true;
      }

      // veure't
      if (mainVerbLemma.equals("veure") && hasAccusativePronoun) {
        complementCanBeObject = true;
      }

      // Excepcions: complements de temps o oracions de relatiu, "tot"...
      boolean isExceptionObject = false;
      boolean isExceptionSubject = false;
      if (firstTokenAfterVerbPos > 0 && firstTokenAfterVerbPos + 1 < tokens.length) {
        if (tokens[firstTokenAfterVerbPos + 1].getChunkTags().contains(PTIME_CHUNK)
            || tokens[firstTokenAfterVerbPos + 1].hasPosTag("_loc_unavegada")
            || tokens[firstTokenAfterVerbPos + 1].hasPosTag("_data_concreta")
            || (tokens[firstTokenAfterVerbPos].hasLemma("tot") && !hasAccusativeNotDativePronoun)) {
          isExceptionObject = true;
        }
        if (tokens[firstTokenAfterVerbPos + 1].getChunkTags().contains(PTIME_CHUNK)
            || tokens[firstTokenAfterVerbPos].getChunkTags().contains(CVERB_CHUNK)
            || tokens[firstTokenAfterVerbPos + 1].hasPosTag("_loc_unavegada")
            || tokens[firstTokenAfterVerbPos + 1].hasPosTag("_data_concreta")
            || tokens[firstTokenAfterVerbPos].hasLemma("tot")) {
          isExceptionSubject = true;
        }
      }
      complementCanBeObject = complementCanBeObject && !isExceptionObject;
      if (mainVerbLemma.equals("fer") && postverbalStartsTemporalAdjunct(tokens, firstTokenAfterVerbPos, coreEnd)) {
        complementCanBeObject = false;
      }
      complementCanBeSubject = complementCanBeSubject && !isExceptionSubject && !isAnotherSubject
          && !trailingDislocation;

      // Transitivitat i rol de "què"
      boolean isIntransitive = false;
      boolean isQueSubject = false;
      if (verb3s && mainVerbPos > 1) {
        AnalyzedToken atr = tokens[mainVerbPos - 1].readingWithTagRegex(ANY_VERB);
        if (atr != null) {
          int queFoundPos = findFirst(tokens, mainVerbPos + 1, "que");
          if (atr.getLemma().equals("fer")
              && (((mainVerbPos < queFoundPos) && (queFoundPos < mainVerbPos + 5)) || hasAccusativeNotDativePronoun)) {
            isQueSubject = true;
          }
        }
        atr = tokens[mainVerbPos].readingWithTagRegex(ANY_VERB);
        if (atr != null) {
          int queFoundPos = findFirst(tokens, mainVerbPos + 1, "que");
          if (atr.getLemma().equals("fer")
              && (((mainVerbPos < queFoundPos) && (queFoundPos < mainVerbPos + 5)) || hasAccusativeNotDativePronoun)) {
            isQueSubject = true;
          }
        }
      }
      boolean isQueObject = false;
      AnalyzedToken mainVerbFinite = tokens[mainVerbPos].readingWithTagRegex(FINITE_VERB);
      if (mainVerbFinite != null && mainVerbPos + 1 < tokens.length
          && tokens[mainVerbPos + 1].getToken().equals("que") && !mainVerbFinite.getLemma().equals("veure")) {
        isQueObject = true;
      }
      boolean ferPorMalPolar = !startsWithAccent
          && isFerPorMalPolar(tokens, mainVerbPos, firstTokenAfterVerbPos, coreEnd, hasDativePronoun);
      if (ferPorMalPolar) {
        isQueSubject = false;
        complementCanBeObject = true;
      } else if (mainVerbLemma.equals("fer") && isFerPorQue(tokens, firstTokenAfterVerbPos)) {
        isQueSubject = false;
        complementCanBeObject = true;
      }
      if (!ferPorMalPolar && mainVerbLemma.equals("fer") && isFerMal(tokens, firstTokenAfterVerbPos)) {
        isQueSubject = true;
        complementCanBeObject = false;
      }

      isIntransitive = (VerbClassifier.isIntransitive(mainVerbLemma) && !COPULAR_VERBS.contains(mainVerbLemma))
          || (mainVerbLemma.equals("ser") && pronomStr.equals("hi"));
      if (mainVerbLemma.equals("passar") || mainVerbLemma.equals("agradar")) {
        isIntransitive = true;
        isQueSubject = !complementCanBeObject;
      } else if (isIntransitive) {
        isQueSubject = false;
      }
      if (startsWithAccent && mainVerbLemma.equals("fer") && isFerMal(tokens, firstTokenAfterVerbPos)) {
        isIntransitive = false;
        isQueSubject = true;
        complementCanBeObject = false;
      }
      isQueSubject = isQueSubject && !isAnotherSubject;

      boolean predictsAccent;
      if (isIntransitive) {
        predictsAccent = isQueSubject;
      } else {
        predictsAccent = (!complementCanBeObject && !hasHo) || complementCanBeSubject;
      }
      predictsAccent = predictsAccent || isQueSubject || isQueObject;
      // Una cua "..., potser?"/"..., oi?" indica sí/no, tret que "què" sigui clarament
      // subjecte o CD (interrogativa retòrica: "Què passa, ..., potser?").
      if (isSiNoQuestion && !isQueSubject && !isQueObject) {
        predictsAccent = false;
      }
      return predictsAccent;
    } catch (IndexOutOfBoundsException | NullPointerException e) {
      return null;
    }
  }

  /**
   * El SN que comença a {@code start} pot ser el subjecte posposat? Cert si el nucli és un
   * nom propi (NP) o un nom comú animat que concorda en nombre amb el verb. Un nom comú
   * inanimat es considera CD (retorna false).
   */
  private static boolean postverbalCanBeSubject(AnalyzedTokenReadings[] tokens, int start,
                                                boolean verb3s, boolean verb3p, boolean verb2p) {
    boolean verbP = verb3p || verb2p;
    for (int i = start; i < tokens.length && i <= start + 4; i++) {
      String form = tokens[i].getToken();
      if (form.equals("?") || form.equals(",")) {
        break;
      }
      if (tokens[i].readingWithTagRegex("NP.*") != null) {
        // nom propi: normalment animat/subjecte (el nombre sovint no hi és marcat)
        return true;
      }
      AnalyzedToken commonNoun = tokens[i].readingWithTagRegex("NC.*");
      if (commonNoun != null) {
        if (NEVER_SUBJECT_NOUNS.contains(commonNoun.getLemma())) {
          return false;
        }
        if (!(ANIMATE_SUBJECT_NOUNS.contains(commonNoun.getLemma())
          || tokens[i].hasPosTag("NCCN000"))) { //numeral
          return false;
        }
        boolean nounSingular = tokens[i].matchesPosTagRegex("NC.S.*");
        boolean nounPlural = tokens[i].matchesPosTagRegex("NC.P.*|NCCN000");
        return (verb3s && nounSingular) || (verbP && nounPlural) || (!nounSingular && !nounPlural);
      }
    }
    return false;
  }

  private static boolean isFerMal(AnalyzedTokenReadings[] tokens, int start) {
    if (start <= 0 || start >= tokens.length) {
      return false;
    }
    String first = tokens[start].getToken().toLowerCase();
    if (first.equals("cap") || first.equals("algun") || first.equals("alguna") || first.equals("un")
        || first.equals("una")) {
      return false;
    }
    return tokens[start].hasLemma("mal") || (tokens[start].hasLemma("por") && !nextTokenIs(tokens, start, "que"));
  }

  private static boolean isFerPorMalPolar(AnalyzedTokenReadings[] tokens, int mainVerbPos, int start, int end,
                                          boolean hasDativePronoun) {
    if (mainVerbPos <= 0 || start <= 0 || start >= tokens.length || end <= start || !tokens[mainVerbPos].hasLemma("fer")) {
      return false;
    }
    int nounPos = firstLexicalToken(tokens, start, end);
    if (nounPos < 0 || !(tokens[nounPos].hasLemma("por") || tokens[nounPos].hasLemma("mal"))) {
      return false;
    }
    if (tokens[nounPos].hasLemma("por") && nextTokenIs(tokens, nounPos, "que")) {
      return false;
    }
    return hasDativePronoun
        || hasInfinitiveAfterNoun(tokens, nounPos, end)
        || hasSubjectAfterFerMal(tokens, nounPos, end)
        || hasAuxiliaryBeforeFer(tokens, mainVerbPos);
  }

  private static int firstLexicalToken(AnalyzedTokenReadings[] tokens, int start, int end) {
    for (int i = start; i < end && i < tokens.length; i++) {
      String token = tokens[i].getToken();
      if (token.equals(",") || token.equals("?")) {
        break;
      }
      if (token.equalsIgnoreCase("pas") || token.equalsIgnoreCase("gaire") || token.equalsIgnoreCase("més")) {
        continue;
      }
      return i;
    }
    return -1;
  }

  private static boolean hasSubjectAfterFerMal(AnalyzedTokenReadings[] tokens, int nounPos, int end) {
    for (int i = nounPos + 1; i < end && i < tokens.length && i <= nounPos + 4; i++) {
      if (tokens[i].getToken().equals(",") || tokens[i].getToken().equals("?")) {
        break;
      }
      if (tokens[i].matchesPosTagRegex("D.*|N.*|NP.*|PI.*")) {
        return true;
      }
    }
    return false;
  }

  private static boolean hasInfinitiveAfterNoun(AnalyzedTokenReadings[] tokens, int nounPos, int end) {
    for (int i = nounPos + 1; i < end && i < tokens.length && i <= nounPos + 3; i++) {
      if (tokens[i].getToken().equals(",") || tokens[i].getToken().equals("?")) {
        break;
      }
      if (tokens[i].matchesPosTagRegex("V.N.*")) {
        return true;
      }
    }
    return false;
  }

  private static boolean hasAuxiliaryBeforeFer(AnalyzedTokenReadings[] tokens, int verbPos) {
    for (int i = verbPos - 1; i > 0 && i >= verbPos - 4; i--) {
      if (tokens[i].getToken().equals(",") || tokens[i].getToken().equals("?")) {
        break;
      }
      AnalyzedToken verb = tokens[i].readingWithTagRegex(ANY_VERB);
      if (verb != null && verb.getLemma() != null
          && (verb.getLemma().equals("voler") || verb.getLemma().equals("poder")
          || verb.getLemma().equals("deure") || verb.getLemma().equals("haver"))) {
        return true;
      }
    }
    return false;
  }

  private static boolean isFerPorQue(AnalyzedTokenReadings[] tokens, int start) {
    return start > 0 && start < tokens.length && tokens[start].hasLemma("por") && nextTokenIs(tokens, start, "que");
  }

  private static boolean nextTokenIs(AnalyzedTokenReadings[] tokens, int start, String token) {
    return start + 1 < tokens.length && tokens[start + 1].getToken().equalsIgnoreCase(token);
  }

  private static boolean postverbalStartsTemporalAdjunct(AnalyzedTokenReadings[] tokens, int start, int end) {
    if (start <= 0 || start >= tokens.length || end <= start) {
      return false;
    }
    boolean hasDeterminer = false;
    for (int i = start; i < end && i <= start + 3; i++) {
      if (tokens[i].matchesPosTagRegex("D.*")) {
        hasDeterminer = true;
        continue;
      }
      AnalyzedToken noun = tokens[i].readingWithTagRegex("NC.*");
      return noun != null && TEMPORAL_ADJUNCT_NOUNS.contains(noun.getLemma())
          && (hasDeterminer || WEEKDAY_NOUNS.contains(noun.getLemma()));
    }
    return false;
  }

  static boolean isAnimateSubjectNoun(String lemma) {
    return ANIMATE_SUBJECT_NOUNS.contains(lemma);
  }

  static boolean isNeverSubjectNoun(String lemma) {
    return NEVER_SUBJECT_NOUNS.contains(lemma);
  }

  private static void addPronounToken(AnalyzedTokenReadings[] tokens, int index, List<String> pronoms) {
    if (index < 0 || index >= tokens.length) {
      return;
    }
    AnalyzedToken pronoun = tokens[index].readingWithTagRegex(PRONOM);
    if (pronoun != null) {
      pronoms.add(PronomsFeblesHelper.transform(pronoun.getToken(), NORMALIZED));
    }
  }

  private static int findFirst(AnalyzedTokenReadings[] tokens, int start, Pattern posTagPattern) {
    for (int i = start; i < tokens.length; i++) {
      if (tokens[i].matchesPosTagRegex(posTagPattern)) {
        return i;
      }
    }
    return -1;
  }

  private static int findFirst(AnalyzedTokenReadings[] tokens, int start, String form) {
    for (int i = start; i < tokens.length; i++) {
      if (tokens[i].getToken().equalsIgnoreCase(form)) {
        return i;
      }
    }
    return -1;
  }

  // ----- Helpers auxiliars usats també pel test de corpus -----

  /**
   * Indica si, a l'inici de la frase interrogativa, hi ha l'estructura
   * "que/què (no) (pronoms febles) verb". La cua de confirmació (", veritat?"...) s'ignora.
   */
  public static boolean hasVerbStructure(AnalyzedSentence sentence) {
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    int queIdx = findQue(tokens);
    if (queIdx < 0) {
      return false;
    }
    int questionIdx = findQuestionMark(tokens, queIdx);
    if (questionIdx < 0) {
      return false;
    }
    int coreEnd = stripConfirmationTag(tokens, queIdx, questionIdx);
    return verbIndexAfterQue(tokens, queIdx, coreEnd) >= 0;
  }

  static int verbIndexAfterQue(AnalyzedTokenReadings[] tokens, int queIdx, int coreEnd) {
    return verbIndexAfterQue(tokens, queIdx, coreEnd, Catalan.getInstance());
  }

  private static int verbIndexAfterQue(AnalyzedTokenReadings[] tokens, int queIdx, int coreEnd, Language language) {
    VerbGroupInfo verbGroup = verbGroupAfterQue(tokens, queIdx, coreEnd, language);
    return verbGroup == null ? -1 : verbGroup.lastVerbIndex;
  }

  private static VerbGroupInfo verbGroupAfterQue(AnalyzedTokenReadings[] tokens, int queIdx, int coreEnd, Language language) {
    VerbSynthesizer verbSynthesizer = new VerbSynthesizer(tokens, queIdx + 1, language);
    if (verbSynthesizer.isUndefined() || verbSynthesizer.getFirstVerbIndex() >= coreEnd) {
      return null;
    }
    int firstVerb = verbSynthesizer.getFirstVerbIndex();
    int nextQue = findFirst(tokens, queIdx + 1, "que");
    if (nextQue > queIdx && nextQue < firstVerb) {
      return null;
    }
    int boundary = nextQue > firstVerb && nextQue < coreEnd ? nextQue : coreEnd;
    int lastIndex = Math.min(verbSynthesizer.getLastIndex(), boundary - 1);
    int lastVerb = Math.min(verbSynthesizer.getLastVerbIndex(), lastIndex);
    while (lastVerb >= firstVerb && tokens[lastVerb].readingWithTagRegex(ANY_VERB) == null) {
      lastVerb--;
    }
    if (lastVerb < firstVerb) {
      return null;
    }
    return new VerbGroupInfo(firstVerb, lastVerb, lastIndex, verbSynthesizer.getNumPronounsBefore());
  }

  private static class VerbGroupInfo {
    final int firstVerbIndex;
    final int lastVerbIndex;
    final int lastIndex;
    final int numPronounsBefore;

    VerbGroupInfo(int firstVerbIndex, int lastVerbIndex, int lastIndex, int numPronounsBefore) {
      this.firstVerbIndex = firstVerbIndex;
      this.lastVerbIndex = lastVerbIndex;
      this.lastIndex = lastIndex;
      this.numPronounsBefore = numPronounsBefore;
    }
  }

  private static boolean isSkippableBeforeVerb(AnalyzedTokenReadings token) {
    String form = token.getToken().toLowerCase();
    if (form.equals("no") || EXPLETIVES.contains(form)) {
      return true;
    }
    if (isWeakPronoun(token)) {
      return true;
    }
    return token.matchesPosTagRegex("R.|I");
  }

  private static boolean isWeakPronoun(AnalyzedTokenReadings token) {
    return token.matchesPosTagRegex("P0.{6}|PP3CN000|PP3NN000|PP3..A00|PP[123]CP000|PP3CSD00");
  }

  private static boolean isVerb(AnalyzedTokenReadings token) {
    return token.matchesPosTagRegex("V.*");
  }

  static int findQue(AnalyzedTokenReadings[] tokens) {
    for (int i = 0; i < tokens.length; i++) {
      String form = tokens[i].getToken();
      if (form.equalsIgnoreCase("que") || form.equalsIgnoreCase("què")) {
        return i;
      }
    }
    return -1;
  }

  static int findQuestionMark(AnalyzedTokenReadings[] tokens, int fromIdx) {
    for (int i = fromIdx + 1; i < tokens.length; i++) {
      if (tokens[i].getToken().equals("?")) {
        return i;
      }
    }
    return -1;
  }

  static int stripConfirmationTag(AnalyzedTokenReadings[] tokens, int queIdx, int questionIdx) {
    int lastCore = questionIdx - 1;
    int commaIdx = lastCore - 1;
    if (commaIdx > queIdx
        && tokens[commaIdx].getToken().equals(",")
        && CONFIRMATION_TAGS.contains(tokens[lastCore].getToken().toLowerCase())) {
      return commaIdx;
    }
    return questionIdx;
  }

  static String verbLemma(AnalyzedTokenReadings token) {
    for (AnalyzedToken reading : token.getReadings()) {
      String posTag = reading.getPOSTag();
      if (posTag != null && posTag.startsWith("V")) {
        return reading.getLemma();
      }
    }
    return null;
  }

}
