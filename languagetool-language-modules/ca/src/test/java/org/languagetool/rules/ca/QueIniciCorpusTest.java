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

import org.junit.Assume;
import org.junit.Test;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.language.Catalan;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;
import org.languagetool.synthesis.ca.VerbSynthesizer;

/**
 * Corpus test per a les regles QUE_INICI_SENSE_ACCENT i QUE_INICI_AMB_ACCENT.
 *
 * <p>Compta:
 * <ul>
 *   <li>FP (falsos positius): matches sobre les frases originals del corpus, que
 *       en principi són correctes i, per tant, no haurien de generar cap avís.</li>
 *   <li>TP (positius verdaders) i FN (falsos negatius): invertint el que/què inicial
 *       de cada frase del corpus la fem incorrecta; llavors la regla hauria de
 *       saltar (TP) i, si no ho fa, és un FN.</li>
 * </ul>
 *
 * <p>El fitxer d'exemples ambigus (les dues solucions són vàlides) només s'usa per
 * comptar FP: ni la frase original ni la invertida haurien de generar avisos.
 *
 * <p>Els fitxers del corpus no es distribueixen amb el codi; si no hi són, el test
 * se salta.
 */
public class QueIniciCorpusTest {

  // Etiquetes de direcció (claus dels comptadors i del report)
  private static final String SENSE_ACCENT = "SENSE (què->que)";
  private static final String AMB_ACCENT = "AMB (que->què)";

  // Ids de regla mesurats per a cada direcció. Configurables amb -DqueInici.sense=... i
  // -DqueInici.amb=... (llistes separades per comes) per a poder comparar regles (p. ex.
  // les QUE_INICIAL_* existents) amb les noves QUE_INICI_*.
  private static final List<String> SENSE_IDS = idsFromProperty("queInici.sense",
      "QUE_INICIAL_SENSEACCENT_NOVERB,QUE_INICIAL_SENSEACCENT_HO,QUE_INICIAL_SENSEACCENT_VERB");
  private static final List<String> AMB_IDS = idsFromProperty("queInici.amb",
      "QUE_INICIAL_AMBACCENT_NOVERB,QUE_INICIAL_AMBACCENT_VERB,QUE_INICIAL_AMBACCENT_HO");

  // que/què com a paraula sencera (sense lletres a banda i banda)
  private static final Pattern QUE_WORD = Pattern.compile("(?<!\\p{L})[Qq][Uu][EeÈè](?!\\p{L})");
  private static final String AMBACCENT_VERB_ID = "QUE_INICIAL_AMBACCENT_VERB";
  private static final Set<String> EXPLETIVES = new HashSet<>(Arrays.asList(
      "collons", "coi", "cony", "dimonis", "dimoni", "carai", "diantre", "caram",
      "carall", "punyeta", "punyetes", "redimonis", "diables", "diable", "hòstia",
      "dimontri", "dimontris", "redéu", "redeu", "punyetera", "leche", "putes",
      "fotons", "llamps", "trons"));

  private static List<String> idsFromProperty(String property, String defaultValue) {
    String value = System.getProperty(property, defaultValue);
    List<String> ids = new ArrayList<>();
    for (String id : value.split(",")) {
      String trimmed = id.trim();
      if (!trimmed.isEmpty()) {
        ids.add(trimmed);
      }
    }
    return ids;
  }

  private static int intFromProperty(String property, int defaultValue) {
    String value = System.getProperty(property);
    if (value == null) {
      return defaultValue;
    }
    return Integer.parseInt(value);
  }

  private JLanguageTool getTool() {
    Language lang = Catalan.getInstance();
    JLanguageTool tool = new JLanguageTool(lang);
    // Desactivem tot el que no siguin les regles objectiu, però NO forcem l'activació de les
    // objectiu: així les subregles default="off" segueixen inactives (fidel a producció).
    for (Rule rule : tool.getAllRules()) {
      String id = rule.getId();
      if (!matchesAny(id, SENSE_IDS) && !matchesAny(id, AMB_IDS)) {
        tool.disableRule(id);
      }
    }
    return tool;
  }

  private File findCorpusFile(String name) {
    String[] candidates = {"var/" + name, "../../var/" + name, "../../../var/" + name};
    for (String c : candidates) {
      File f = new File(c);
      if (f.exists()) {
        return f;
      }
    }
    return new File("var/" + name);
  }

  /** Inverteix l'accent del primer que/què que apareix a la frase, conservant les majúscules. */
  static String invertQue(String sentence) {
    Matcher m = QUE_WORD.matcher(sentence);
    if (!m.find()) {
      return null;
    }
    String word = m.group();
    char last = word.charAt(2);
    char inverted;
    switch (last) {
      case 'e': inverted = 'è'; break;
      case 'è': inverted = 'e'; break;
      case 'E': inverted = 'È'; break;
      case 'È': inverted = 'E'; break;
      default: return null;
    }
    String newWord = word.substring(0, 2) + inverted;
    return sentence.substring(0, m.start()) + newWord + sentence.substring(m.end());
  }

  /**
   * Retorna la direcció de la regla que ha saltat sobre la frase ({@link #SENSE_ACCENT} o
   * {@link #AMB_ACCENT}), o {@code null} si cap. Com que en una frase concreta el que/què
   * inicial només pot activar una direcció (segons si té accent o no), no cal desambiguar.
   */
  private String matchedRuleId(JLanguageTool tool, String sentence) throws IOException {
    for (RuleMatch match : tool.check(sentence)) {
      String id = match.getRule().getId();
      if (matchesAny(id, SENSE_IDS) || matchesAny(id, AMB_IDS)) {
        return match.getRule().getFullId();
      }
    }
    return null;
  }

  private List<String> matchedRuleIds(JLanguageTool tool, String sentence) throws IOException {
    List<String> ids = new ArrayList<>();
    for (RuleMatch match : tool.check(sentence)) {
      String id = match.getRule().getId();
      if (matchesAny(id, SENSE_IDS) || matchesAny(id, AMB_IDS)) {
        ids.add(match.getRule().getFullId());
      }
    }
    return ids;
  }

  private boolean matchesAmbaccentVerb(JLanguageTool tool, String sentence) throws IOException {
    for (String id : matchedRuleIds(tool, sentence)) {
      if (id.startsWith(AMBACCENT_VERB_ID)) {
        return true;
      }
    }
    return false;
  }

  /** Direcció (SENSE/AMB) d'un id de regla, o null. */
  private String directionOf(String ruleId) {
    if (ruleId == null) {
      return null;
    }
    if (matchesAny(ruleId, SENSE_IDS)) {
      return SENSE_ACCENT;
    }
    if (matchesAny(ruleId, AMB_IDS)) {
      return AMB_ACCENT;
    }
    return null;
  }

  private static boolean matchesAny(String ruleId, List<String> ids) {
    for (String id : ids) {
      if (ruleId.startsWith(id)) {
        return true;
      }
    }
    return false;
  }

  /** true si el primer que/què de la frase porta accent (què), false si no (que), null si no n'hi ha. */
  static Boolean firstQueHasAccent(String sentence) {
    Matcher m = QUE_WORD.matcher(sentence);
    if (!m.find()) {
      return null;
    }
    char last = m.group().charAt(2);
    return last == 'è' || last == 'È';
  }

  @Test
  public void testCorpus() throws IOException {
    // Eina estadística, no un test de pass/fail: se salta per defecte (p. ex. a `./build.sh ca test`).
    // Per executar-la: mvn ... -Dtest=QueIniciCorpusTest -DqueInici.run=true
    boolean errorsOnly = Boolean.getBoolean("queInici.errorsOnly");
    Assume.assumeTrue("Eina estadística desactivada; executeu-la amb -DqueInici.run=true o -DqueInici.errorsOnly=true",
        Boolean.getBoolean("queInici.run") || errorsOnly);
    File mainCorpus = findCorpusFile("que_inici.txt");
    File ambigCorpus = findCorpusFile("que_inici_ambigu.txt");
    File reportDir = new File(mainCorpus.getParentFile(), "que_inici_report");
    if (errorsOnly) {
      Assume.assumeTrue("Report dir not found: " + reportDir.getAbsolutePath(), reportDir.exists());
      writeVerbNounContextReportsFromExistingErrors(reportDir);
      return;
    }
    Assume.assumeTrue("Corpus file not found: " + mainCorpus.getAbsolutePath(), mainCorpus.exists());

    JLanguageTool tool = getTool();

    // FP: matches sobre frases originals (correctes), separats per regla
    List<String> fpSense = new ArrayList<>();
    List<String> fpAmb = new ArrayList<>();
    List<ErrorCase> fpSenseCases = new ArrayList<>();
    List<ErrorCase> fpAmbCases = new ArrayList<>();
    // FN: frases invertides (incorrectes) que la regla no detecta, separats per regla
    List<String> fnSense = new ArrayList<>();
    List<String> fnAmb = new ArrayList<>();
    List<ErrorCase> fnSenseCases = new ArrayList<>();
    List<ErrorCase> fnAmbCases = new ArrayList<>();
    int tpSense = 0;
    int tpAmb = 0;
    // Acumulació de verbs del patró net "que/què + verb + ?", per a classificar-los (tr/intr)
    Map<String, Integer> verbsAfterQueAccent = new HashMap<>();
    Map<String, Integer> verbsAfterQue = new HashMap<>();
    // Recopilació de noms comuns darrere el verb (per lema), per accent: els que surten amb
    // "Què" són subjecte-candidats (animats); els que surten amb "Que", objecte-candidats.
    Map<String, Integer> nounsAfterAccent = new HashMap<>();
    Map<String, Integer> nounsAfterNoAccent = new HashMap<>();
    Map<String, Integer> verbNounAfterAccent = new HashMap<>();
    Map<String, Integer> verbNounAfterNoAccent = new HashMap<>();
    Map<String, List<String>> verbNounExamplesAccent = new HashMap<>();
    Map<String, List<String>> verbNounExamplesNoAccent = new HashMap<>();
    // Comprovació de la hipòtesi "sense estructura verbal -> que": distribució accent x té-verb
    int accentVerb = 0;
    int accentNoVerb = 0;
    int senseAccentVerb = 0;
    int senseAccentNoVerb = 0;
    List<String> accentNoVerbExamples = new ArrayList<>();

    List<String> lines = Files.readAllLines(mainCorpus.toPath(), StandardCharsets.UTF_8);
    int totalSentences = 0;
    for (String line : lines) {
      if (!line.trim().isEmpty()) {
        totalSentences++;
      }
    }
    int progressEvery = intFromProperty("queInici.progressEvery", 5000);
    int processedSentences = 0;
    for (String line : lines) {
      String sentence = line.trim();
      if (sentence.isEmpty()) {
        continue;
      }
      processedSentences++;
      if (progressEvery > 0 && (processedSentences % progressEvery == 0 || processedSentences == totalSentences)) {
        System.err.println("QUE_INICI: analitzades " + processedSentences + "/" + totalSentences + " frases");
      }
      // Comprovació de la hipòtesi + acumulació de verbs (per lema) darrere el que/què
      Boolean origAccent = firstQueHasAccent(sentence);
      if (origAccent != null) {
        AnalyzedTokenReadings[] toks = tool.getAnalyzedSentence(sentence).getTokensWithoutWhitespace();
        int qi = QueIniciFilter.findQue(toks);
        int verbIdx = -1;
        int coreEnd = -1;
        if (qi >= 0) {
          int qq = QueIniciFilter.findQuestionMark(toks, qi);
          if (qq >= 0) {
            coreEnd = QueIniciFilter.stripConfirmationTag(toks, qi, qq);
            verbIdx = QueIniciFilter.verbIndexAfterQue(toks, qi, coreEnd);
          }
        }
        boolean hasVerb = verbIdx >= 0;
        if (hasVerb) {
          String lemma = QueIniciFilter.verbLemma(toks[verbIdx]);
          if (lemma != null) {
            (origAccent ? verbsAfterQueAccent : verbsAfterQue).merge(lemma, 1, Integer::sum);
          }
        }
        if (origAccent) {
          if (hasVerb) { accentVerb++; } else { accentNoVerb++; accentNoVerbExamples.add(sentence); }
        } else {
          if (hasVerb) { senseAccentVerb++; } else { senseAccentNoVerb++; }
        }
      }
      // FP: la frase original és (en principi) correcta -> no hauria de saltar cap regla
      String firedIdOriginal = matchedRuleId(tool, sentence);
      String firedOnOriginal = directionOf(firedIdOriginal);
      if (SENSE_ACCENT.equals(firedOnOriginal)) {
        fpSense.add(firedIdOriginal + "\t" + sentence);
        fpSenseCases.add(new ErrorCase(firedIdOriginal, sentence, sentence, false));
      } else if (AMB_ACCENT.equals(firedOnOriginal)) {
        fpAmb.add(firedIdOriginal + "\t" + sentence);
        fpAmbCases.add(new ErrorCase(firedIdOriginal, sentence, sentence, false));
      }
      // TP/FN: invertim el que/què inicial -> la frase esdevé incorrecta -> hauria de saltar.
      // La forma invertida determina quina regla ha d'actuar:
      //   invertida amb accent (què) -> QUE_INICI_SENSE_ACCENT
      //   invertida sense accent (que) -> QUE_INICI_AMB_ACCENT
      String inverted = invertQue(sentence);
      if (inverted != null && !inverted.equals(sentence)) {
        Boolean invAccent = firstQueHasAccent(inverted);
        String expected = Boolean.TRUE.equals(invAccent) ? SENSE_ACCENT : AMB_ACCENT;
        String firedIdInverted = matchedRuleId(tool, inverted);
        String fired = directionOf(firedIdInverted);
        boolean detected = expected.equals(fired);
        if (SENSE_ACCENT.equals(expected)) {
          if (detected) {
            tpSense++;
          } else {
            ErrorCase errorCase = new ErrorCase(fnId(expected, firedIdInverted), sentence, inverted, true);
            fnSense.add(errorCase.format());
            fnSenseCases.add(errorCase);
          }
        } else {
          if (detected) {
            tpAmb++;
          } else {
            ErrorCase errorCase = new ErrorCase(fnId(expected, firedIdInverted), sentence, inverted, true);
            fnAmb.add(errorCase.format());
            fnAmbCases.add(errorCase);
          }
        }
      }
    }

    Set<String> seenVerbNounContextSentences = new HashSet<>();
    collectVerbNounContextsFromErrors(tool, fpSenseCases, seenVerbNounContextSentences,
        nounsAfterAccent, nounsAfterNoAccent,
        verbNounAfterAccent, verbNounAfterNoAccent, verbNounExamplesAccent, verbNounExamplesNoAccent);
    collectVerbNounContextsFromErrors(tool, fpAmbCases, seenVerbNounContextSentences,
        nounsAfterAccent, nounsAfterNoAccent,
        verbNounAfterAccent, verbNounAfterNoAccent, verbNounExamplesAccent, verbNounExamplesNoAccent);
    collectVerbNounContextsFromErrors(tool, fnSenseCases, seenVerbNounContextSentences,
        nounsAfterAccent, nounsAfterNoAccent,
        verbNounAfterAccent, verbNounAfterNoAccent, verbNounExamplesAccent, verbNounExamplesNoAccent);
    collectVerbNounContextsFromErrors(tool, fnAmbCases, seenVerbNounContextSentences,
        nounsAfterAccent, nounsAfterNoAccent,
        verbNounAfterAccent, verbNounAfterNoAccent, verbNounExamplesAccent, verbNounExamplesNoAccent);

    // Fitxer ambigu: cap forma (original ni invertida) hauria de generar avisos
    List<String> fpAmbig = new ArrayList<>();
    if (ambigCorpus.exists()) {
      for (String line : Files.readAllLines(ambigCorpus.toPath(), StandardCharsets.UTF_8)) {
        String sentence = line.trim();
        if (sentence.isEmpty()) {
          continue;
        }
        String firedOrig = matchedRuleId(tool, sentence);
        if (firedOrig != null) {
          fpAmbig.add("[orig][" + firedOrig + "] " + sentence);
        }
        String inverted = invertQue(sentence);
        if (inverted != null && !inverted.equals(sentence)) {
          String firedInv = matchedRuleId(tool, inverted);
          if (firedInv != null) {
            fpAmbig.add("[inv] [" + firedInv + "] " + inverted);
          }
        }
      }
    }

    reportDir.mkdirs();
    writeLines(new File(reportDir, "false_positives_sense_accent.txt"), fpSense);
    writeLines(new File(reportDir, "false_positives_amb_accent.txt"), fpAmb);
    writeLines(new File(reportDir, "false_negatives_sense_accent.txt"), fnSense);
    writeLines(new File(reportDir, "false_negatives_amb_accent.txt"), fnAmb);
    writeLines(new File(reportDir, "false_positives_ambig.txt"), fpAmbig);
    writeLines(new File(reportDir, "verbs_after_que_accent.txt"), sortByFreq(verbsAfterQueAccent));
    writeLines(new File(reportDir, "verbs_after_que.txt"), sortByFreq(verbsAfterQue));
    writeVerbNounContextReports(reportDir, nounsAfterAccent, nounsAfterNoAccent,
        verbNounAfterAccent, verbNounAfterNoAccent, verbNounExamplesAccent, verbNounExamplesNoAccent);
    writeLines(new File(reportDir, "hipotesi_que_accent_sense_verb.txt"), accentNoVerbExamples);

    int applicableSense = tpSense + fnSense.size();
    int applicableAmb = tpAmb + fnAmb.size();
    int fpTotal = fpSense.size() + fpAmb.size();
    int tpTotal = tpSense + tpAmb;
    int applicableTotal = applicableSense + applicableAmb;

    StringBuilder report = new StringBuilder();
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    report.append("========================================================\n");
    report.append("=== QUE_INICI corpus report -- ").append(timestamp).append(" ===\n");
    report.append("Corpus principal (").append(mainCorpus.getName()).append("): ")
        .append(lines.size()).append(" línies\n");
    report.append("Regles SENSE (què->que): ").append(SENSE_IDS).append("\n");
    report.append("Regles AMB (que->què):   ").append(AMB_IDS).append("\n");
    appendRuleStats(report, SENSE_ACCENT, fpSense.size(), tpSense, fnSense.size(), applicableSense);
    appendRuleStats(report, AMB_ACCENT, fpAmb.size(), tpAmb, fnAmb.size(), applicableAmb);
    report.append("--- Global ---\n");
    appendMetrics(report, fpTotal, tpTotal, (fnSense.size() + fnAmb.size()), applicableTotal);
    report.append("FP (corpus ambigu, orig+inv):  ").append(fpAmbig.size()).append("\n");
    report.append("--- Hipòtesi \"sense estructura verbal -> que\" ---\n");
    int accentTotal = accentVerb + accentNoVerb;
    int senseAccentTotal = senseAccentVerb + senseAccentNoVerb;
    report.append(String.format("  Què  (amb accent): amb verb %d / sense verb %d  (sense verb: %.1f%%)%n",
        accentVerb, accentNoVerb, accentTotal == 0 ? 0.0 : 100.0 * accentNoVerb / accentTotal));
    report.append(String.format("  Que  (sense accent): amb verb %d / sense verb %d  (sense verb: %.1f%%)%n",
        senseAccentVerb, senseAccentNoVerb, senseAccentTotal == 0 ? 0.0 : 100.0 * senseAccentNoVerb / senseAccentTotal));
    report.append("  FP potencials de SENSE_ACCENT si apliquem \"sense verb -> que\": ")
        .append(accentNoVerb).append("\n");

    System.out.print(report);
    System.out.println("Detall a: " + reportDir.getAbsolutePath());
    appendToLog(new File(reportDir, "results.log"), report.toString());

    assertTrue("No s'ha processat cap frase invertida", applicableTotal > 0);
  }

  private void writeVerbNounContextReportsFromExistingErrors(File reportDir) throws IOException {
    JLanguageTool tool = getTool();
    Map<String, Integer> nounsAfterAccent = new HashMap<>();
    Map<String, Integer> nounsAfterNoAccent = new HashMap<>();
    Map<String, Integer> verbNounAfterAccent = new HashMap<>();
    Map<String, Integer> verbNounAfterNoAccent = new HashMap<>();
    Map<String, List<String>> verbNounExamplesAccent = new HashMap<>();
    Map<String, List<String>> verbNounExamplesNoAccent = new HashMap<>();
    Set<String> seenVerbNounContextSentences = new HashSet<>();

    int errors = 0;
    errors += collectVerbNounContextsFromErrorFile(tool, new File(reportDir, "false_positives_sense_accent.txt"), false,
        seenVerbNounContextSentences,
        nounsAfterAccent, nounsAfterNoAccent, verbNounAfterAccent, verbNounAfterNoAccent,
        verbNounExamplesAccent, verbNounExamplesNoAccent);
    errors += collectVerbNounContextsFromErrorFile(tool, new File(reportDir, "false_positives_amb_accent.txt"), false,
        seenVerbNounContextSentences,
        nounsAfterAccent, nounsAfterNoAccent, verbNounAfterAccent, verbNounAfterNoAccent,
        verbNounExamplesAccent, verbNounExamplesNoAccent);
    errors += collectVerbNounContextsFromErrorFile(tool, new File(reportDir, "false_negatives_sense_accent.txt"), true,
        seenVerbNounContextSentences,
        nounsAfterAccent, nounsAfterNoAccent, verbNounAfterAccent, verbNounAfterNoAccent,
        verbNounExamplesAccent, verbNounExamplesNoAccent);
    errors += collectVerbNounContextsFromErrorFile(tool, new File(reportDir, "false_negatives_amb_accent.txt"), true,
        seenVerbNounContextSentences,
        nounsAfterAccent, nounsAfterNoAccent, verbNounAfterAccent, verbNounAfterNoAccent,
        verbNounExamplesAccent, verbNounExamplesNoAccent);

    writeVerbNounContextReports(reportDir, nounsAfterAccent, nounsAfterNoAccent,
        verbNounAfterAccent, verbNounAfterNoAccent, verbNounExamplesAccent, verbNounExamplesNoAccent);
    System.out.println("Regenerats els TSV de context verb+nom a partir de " + errors
        + " casos FP/FN de: " + reportDir.getAbsolutePath());
    assertTrue("No s'ha trobat cap cas FP/FN a " + reportDir.getAbsolutePath(), errors > 0);
  }

  private int collectVerbNounContextsFromErrorFile(JLanguageTool tool, File file, boolean inverted,
                                                   Set<String> seenVerbNounContextSentences,
                                                   Map<String, Integer> nounsAfterAccent,
                                                   Map<String, Integer> nounsAfterNoAccent,
                                                   Map<String, Integer> verbNounAfterAccent,
                                                   Map<String, Integer> verbNounAfterNoAccent,
                                                   Map<String, List<String>> verbNounExamplesAccent,
                                                   Map<String, List<String>> verbNounExamplesNoAccent)
      throws IOException {
    if (!file.exists()) {
      return 0;
    }
    List<ErrorCase> errors = new ArrayList<>();
    for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
      ErrorCase error = parseErrorCase(line, inverted);
      if (error != null) {
        errors.add(error);
      }
    }
    collectVerbNounContextsFromErrors(tool, errors, seenVerbNounContextSentences,
        nounsAfterAccent, nounsAfterNoAccent,
        verbNounAfterAccent, verbNounAfterNoAccent, verbNounExamplesAccent, verbNounExamplesNoAccent);
    return errors.size();
  }

  private ErrorCase parseErrorCase(String line, boolean inverted) {
    String trimmed = line.trim();
    if (trimmed.isEmpty()) {
      return null;
    }
    String[] parts = trimmed.split("\t", 2);
    if (parts.length != 2) {
      return null;
    }
    if (inverted) {
      String[] sentences = parts[1].split("  ==>  ", 2);
      if (sentences.length != 2) {
        return null;
      }
      return new ErrorCase(parts[0], sentences[0], sentences[1], true);
    }
    return new ErrorCase(parts[0], parts[1], parts[1], false);
  }

  private void writeVerbNounContextReports(File reportDir,
                                           Map<String, Integer> nounsAfterAccent,
                                           Map<String, Integer> nounsAfterNoAccent,
                                           Map<String, Integer> verbNounAfterAccent,
                                           Map<String, Integer> verbNounAfterNoAccent,
                                           Map<String, List<String>> verbNounExamplesAccent,
                                           Map<String, List<String>> verbNounExamplesNoAccent)
      throws IOException {
    writeLines(new File(reportDir, "nouns_after_verb_accent.txt"), sortByFreq(nounsAfterAccent));
    writeLines(new File(reportDir, "nouns_after_verb_noaccent.txt"), sortByFreq(nounsAfterNoAccent));
    writeLines(new File(reportDir, "verb_noun_after_verb_accent.tsv"),
        sortVerbNounContexts(verbNounAfterAccent, verbNounExamplesAccent));
    writeLines(new File(reportDir, "verb_noun_after_verb_noaccent.tsv"),
        sortVerbNounContexts(verbNounAfterNoAccent, verbNounExamplesNoAccent));
    writeLines(new File(reportDir, "verb_noun_after_verb_contrast.tsv"),
        sortVerbNounContrast(verbNounAfterAccent, verbNounAfterNoAccent,
            verbNounExamplesAccent, verbNounExamplesNoAccent));
    List<VerbNounStats> verbNounStats = sortedVerbNounStats(verbNounAfterAccent, verbNounAfterNoAccent);
    writeLines(new File(reportDir, "verb_noun_subject_candidates.tsv"),
        filterVerbNounCandidates(verbNounStats, verbNounExamplesAccent, verbNounExamplesNoAccent, CandidateKind.SUBJECT));
    writeLines(new File(reportDir, "verb_noun_object_candidates.tsv"),
        filterVerbNounCandidates(verbNounStats, verbNounExamplesAccent, verbNounExamplesNoAccent, CandidateKind.OBJECT));
    writeLines(new File(reportDir, "verb_noun_manual_review.tsv"),
        filterVerbNounCandidates(verbNounStats, verbNounExamplesAccent, verbNounExamplesNoAccent, CandidateKind.REVIEW));
  }

  private void appendRuleStats(StringBuilder sb, String ruleId, int fp, int tp, int fn, int applicable) {
    sb.append("--- ").append(ruleId).append(" ---\n");
    appendMetrics(sb, fp, tp, fn, applicable);
  }

  private void appendMetrics(StringBuilder sb, int fp, int tp, int fn, int applicable) {
    double precision = (tp + fp) == 0 ? 0 : (double) tp / (tp + fp);
    double recall = applicable == 0 ? 0 : (double) tp / applicable;
    sb.append("  FP: ").append(fp).append("   TP: ").append(tp).append("   FN: ").append(fn)
        .append("   (aplicables: ").append(applicable).append(")\n");
    sb.append(String.format("  Precisió: %.4f   Recall: %.4f%n", precision, recall));
  }

  /** Afegeix (append) el report al fitxer de log, per anar acumulant els resultats de cada execució. */
  private void appendToLog(File logFile, String report) throws IOException {
    try (BufferedWriter writer = Files.newBufferedWriter(logFile.toPath(), StandardCharsets.UTF_8,
        StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
      writer.write(report);
      writer.newLine();
    }
  }

  private String fnId(String expectedDirection, String firedId) {
    String expected = SENSE_ACCENT.equals(expectedDirection) ? "SENSE" : "AMB";
    if (firedId != null) {
      return "EXPECTED_" + expected + "_GOT_" + firedId;
    }
    return "EXPECTED_" + expected + "_NO_MATCH";
  }

  private void collectVerbNounContextsFromErrors(JLanguageTool tool, List<ErrorCase> errors,
                                                 Set<String> seenVerbNounContextSentences,
                                                 Map<String, Integer> nounsAfterAccent,
                                                 Map<String, Integer> nounsAfterNoAccent,
                                                 Map<String, Integer> verbNounAfterAccent,
                                                 Map<String, Integer> verbNounAfterNoAccent,
                                                 Map<String, List<String>> verbNounExamplesAccent,
                                                 Map<String, List<String>> verbNounExamplesNoAccent)
      throws IOException {
    for (ErrorCase error : errors) {
      Boolean correctAccent = firstQueHasAccent(error.correctSentence);
      if (correctAccent == null) {
        continue;
      }
      if (!seenVerbNounContextSentences.add(error.correctSentence)) {
        continue;
      }
      AnalyzedTokenReadings[] candidateToks = tool.getAnalyzedSentence(error.correctSentence).getTokensWithoutWhitespace();
      int candidateQi = QueIniciFilter.findQue(candidateToks);
      int candidateCoreEnd = -1;
      if (candidateQi >= 0) {
        int candidateQuestion = QueIniciFilter.findQuestionMark(candidateToks, candidateQi);
        if (candidateQuestion >= 0) {
          candidateCoreEnd = QueIniciFilter.stripConfirmationTag(candidateToks, candidateQi, candidateQuestion);
        }
      }
      VerbNounContext context = exactVerbNounContextAfterQue(candidateToks, candidateQi, candidateCoreEnd);
      if (context != null) {
        boolean subjectContext = correctAccent && context.subjectCompatible;
        (subjectContext ? nounsAfterAccent : nounsAfterNoAccent).merge(context.nounLemma, 1, Integer::sum);
        addVerbNounContext(subjectContext ? verbNounAfterAccent : verbNounAfterNoAccent,
            subjectContext ? verbNounExamplesAccent : verbNounExamplesNoAccent,
            context.verbLemma, context.nounLemma, "[" + error.id + "] " + error.correctSentence);
      }
    }
  }

  private VerbNounContext exactVerbNounContextAfterQue(AnalyzedTokenReadings[] tokens, int queIdx, int coreEnd) {
    if (queIdx < 0 || coreEnd < 0) {
      return null;
    }
    VerbSynthesizer verbSynthesizer = new VerbSynthesizer(tokens, queIdx + 1, Catalan.getInstance());
    if (verbSynthesizer.isUndefined() || verbSynthesizer.getFirstVerbIndex() >= coreEnd
        || verbSynthesizer.getLastIndex() >= coreEnd) {
      return null;
    }
    for (int i = queIdx + 1; i < verbSynthesizer.getFirstVerbIndex(); i++) {
      if (EXPLETIVES.contains(tokens[i].getToken().toLowerCase())) {
        return null;
      }
    }
    AnalyzedToken mainVerbReading = tokens[verbSynthesizer.getLastVerbIndex()].readingWithTagRegex("V.*");
    if (mainVerbReading == null) {
      return null;
    }
    int nounIdx = verbSynthesizer.getLastIndex() + 1;
    while (nounIdx < coreEnd && tokens[nounIdx].matchesPosTagRegex("D.*")) {
      nounIdx++;
    }
    if (nounIdx >= coreEnd) {
      return null;
    }
    AnalyzedToken nounReading = tokens[nounIdx].readingWithTagRegex("NC.*");
    if (nounReading == null) {
      return null;
    }
    boolean verb3s = tokens[verbSynthesizer.getFirstVerbIndex()].matchesPosTagRegex("V.[SI].3S.*");
    boolean verb3p = tokens[verbSynthesizer.getFirstVerbIndex()].matchesPosTagRegex("V.[SI].3P.*");
    boolean nounSingular = tokens[nounIdx].matchesPosTagRegex("NC.S.*");
    boolean nounPlural = tokens[nounIdx].matchesPosTagRegex("NC.P.*");
    boolean subjectCompatible = (verb3s && nounSingular) || (verb3p && nounPlural);
    return new VerbNounContext(mainVerbReading.getLemma(), nounReading.getLemma(), subjectCompatible);
  }

  private static class VerbNounContext {
    final String verbLemma;
    final String nounLemma;
    final boolean subjectCompatible;

    VerbNounContext(String verbLemma, String nounLemma, boolean subjectCompatible) {
      this.verbLemma = verbLemma;
      this.nounLemma = nounLemma;
      this.subjectCompatible = subjectCompatible;
    }
  }

  private static class ErrorCase {
    final String id;
    final String correctSentence;
    final String checkedSentence;
    final boolean inverted;

    ErrorCase(String id, String correctSentence, String checkedSentence, boolean inverted) {
      this.id = id;
      this.correctSentence = correctSentence;
      this.checkedSentence = checkedSentence;
      this.inverted = inverted;
    }

    String format() {
      if (inverted) {
        return id + "\t" + correctSentence + "  ==>  " + checkedSentence;
      }
      return id + "\t" + checkedSentence;
    }
  }

  private void addVerbNounContext(Map<String, Integer> counts, Map<String, List<String>> examples,
                                  String verbLemma, String nounLemma, String sentence) {
    String key = verbLemma + "\t" + nounLemma;
    counts.merge(key, 1, Integer::sum);
    List<String> keyExamples = examples.computeIfAbsent(key, k -> new ArrayList<>());
    if (keyExamples.size() < 3) {
      keyExamples.add(sentence);
    }
  }

  private List<String> sortByFreq(Map<String, Integer> counts) {
    List<Map.Entry<String, Integer>> entries = new ArrayList<>(counts.entrySet());
    entries.sort((a, b) -> b.getValue().compareTo(a.getValue()));
    List<String> result = new ArrayList<>();
    for (Map.Entry<String, Integer> e : entries) {
      result.add(e.getValue() + "\t" + e.getKey());
    }
    return result;
  }

  private List<String> sortVerbNounContexts(Map<String, Integer> counts, Map<String, List<String>> examples) {
    List<Map.Entry<String, Integer>> entries = new ArrayList<>(counts.entrySet());
    entries.sort((a, b) -> b.getValue().compareTo(a.getValue()));
    List<String> result = new ArrayList<>();
    result.add("count\tverb\tnoun\texamples");
    for (Map.Entry<String, Integer> e : entries) {
      String[] parts = e.getKey().split("\t", 2);
      result.add(e.getValue() + "\t" + parts[0] + "\t" + parts[1] + "\t"
          + String.join(" || ", examples.getOrDefault(e.getKey(), new ArrayList<>())));
    }
    return result;
  }

  private List<String> sortVerbNounContrast(Map<String, Integer> accentCounts, Map<String, Integer> noAccentCounts,
                                            Map<String, List<String>> accentExamples,
                                            Map<String, List<String>> noAccentExamples) {
    List<VerbNounStats> entries = sortedVerbNounStats(accentCounts, noAccentCounts);
    List<String> result = new ArrayList<>();
    result.add("total\taccent_count\tnoaccent_count\taccent_ratio\tverb\tnoun\taccent_examples\tnoaccent_examples");
    for (VerbNounStats e : entries) {
      result.add(formatVerbNounStats(e, accentExamples, noAccentExamples));
    }
    return result;
  }

  private List<VerbNounStats> sortedVerbNounStats(Map<String, Integer> accentCounts, Map<String, Integer> noAccentCounts) {
    Map<String, Integer> all = new HashMap<>();
    for (Map.Entry<String, Integer> e : accentCounts.entrySet()) {
      all.merge(e.getKey(), e.getValue(), Integer::sum);
    }
    for (Map.Entry<String, Integer> e : noAccentCounts.entrySet()) {
      all.merge(e.getKey(), e.getValue(), Integer::sum);
    }
    List<Map.Entry<String, Integer>> entries = new ArrayList<>(all.entrySet());
    entries.sort((a, b) -> b.getValue().compareTo(a.getValue()));
    List<VerbNounStats> result = new ArrayList<>();
    for (Map.Entry<String, Integer> e : entries) {
      int accent = accentCounts.getOrDefault(e.getKey(), 0);
      int noAccent = noAccentCounts.getOrDefault(e.getKey(), 0);
      String[] parts = e.getKey().split("\t", 2);
      result.add(new VerbNounStats(parts[0], parts[1], accent, noAccent));
    }
    return result;
  }

  private List<String> filterVerbNounCandidates(List<VerbNounStats> stats,
                                                Map<String, List<String>> accentExamples,
                                                Map<String, List<String>> noAccentExamples,
                                                CandidateKind kind) {
    List<String> result = new ArrayList<>();
    result.add("total\taccent_count\tnoaccent_count\taccent_ratio\tverb\tnoun\taccent_examples\tnoaccent_examples");
    for (VerbNounStats stat : stats) {
      if (kind.accepts(stat)) {
        result.add(formatVerbNounStats(stat, accentExamples, noAccentExamples));
      }
    }
    return result;
  }

  private String formatVerbNounStats(VerbNounStats stats,
                                     Map<String, List<String>> accentExamples,
                                     Map<String, List<String>> noAccentExamples) {
    String key = stats.verb + "\t" + stats.noun;
    return stats.total + "\t" + stats.accent + "\t" + stats.noAccent + "\t"
        + String.format("%.4f", stats.accentRatio()) + "\t"
        + stats.verb + "\t" + stats.noun + "\t"
        + String.join(" || ", accentExamples.getOrDefault(key, new ArrayList<>())) + "\t"
        + String.join(" || ", noAccentExamples.getOrDefault(key, new ArrayList<>()));
  }

  private enum CandidateKind {
    SUBJECT {
      @Override
      boolean accepts(VerbNounStats stats) {
        return stats.total >= 5 && stats.accent >= 5 && stats.accentRatio() >= 0.90
            && !QueIniciFilter.isAnimateSubjectNoun(stats.noun)
            && !QueIniciFilter.isNeverSubjectNoun(stats.noun);
      }
    },
    OBJECT {
      @Override
      boolean accepts(VerbNounStats stats) {
        return stats.total >= 5 && stats.noAccent >= 5 && stats.accentRatio() <= 0.05;
      }
    },
    REVIEW {
      @Override
      boolean accepts(VerbNounStats stats) {
        return stats.total >= 10 && !SUBJECT.accepts(stats) && !OBJECT.accepts(stats);
      }
    };

    abstract boolean accepts(VerbNounStats stats);
  }

  private static class VerbNounStats {
    final String verb;
    final String noun;
    final int accent;
    final int noAccent;
    final int total;

    VerbNounStats(String verb, String noun, int accent, int noAccent) {
      this.verb = verb;
      this.noun = noun;
      this.accent = accent;
      this.noAccent = noAccent;
      this.total = accent + noAccent;
    }

    double accentRatio() {
      return total == 0 ? 0.0 : (double) accent / total;
    }
  }

  private void writeLines(File file, List<String> lines) throws IOException {
    try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
      for (String line : lines) {
        writer.write(line);
        writer.newLine();
      }
    }
  }

}
