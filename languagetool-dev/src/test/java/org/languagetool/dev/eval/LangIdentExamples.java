/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.eval;

import org.languagetool.language.identifier.detector.FastTextDetector;
import org.languagetool.language.identifier.detector.NGramDetector;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Test FastText or ngram-based approach with some examples.
 */
class LangIdentExamples {

  private static final String MODE = "fasttext";   // 'ngram' or 'fasttext'
  private static final File MODEL_PATH = new File("/prg/fastText-0.1.0/data/lid.176.bin");
  private static final File BINARY_PATH = new File("/prg/fastText-0.1.0/fasttext");
  private static final File NGRAM_ZIP = new File("/home/languagetool/ngram-lang-id/model_ml50_new.zip");

  private static final String DE = "de";
  private static final String EN = "en";
  private static final String NL = "nl";
  private static final String ES = "es";
  private static final String FR = "fr";
  private static final String IT = "it";
  private static final String UK = "uk";

  private final FastTextDetector ft;
  private final NGramDetector ngram;
  private final Map<String,Integer> langTotalCount = new HashMap<>();
  private final Map<String,Integer> langToCorrectCount = new HashMap<>();

  private LangIdentExamples() throws IOException {
    if (MODE.equals("ngram")) {
      ngram = new NGramDetector(NGRAM_ZIP, 50);
      ft = null;
    } else if (MODE.equals("fasttext")) {
      ngram = null;
      ft = new FastTextDetector(MODEL_PATH, BINARY_PATH);
    } else {
      throw new RuntimeException("MODE not known: " + MODE);
    }
  }

  private void run() throws IOException {
    eval("Osteuropa", DE);
    eval("USA, Osteuropa", DE);
    eval("Sehr leckere Sourcreme", DE);
    eval("Am 31. September", DE);
    eval("Anbei.", DE);
    eval("was meinst", DE);
    eval("Hi Traumfrau", DE);
    eval("Weihnachtsmail an AG gesendet", DE);
    eval("Moin Jan", DE);
    eval("Moin Jan,", DE);
    eval("Erstattung Bewirtungskosten Steak House Arizona", DE);
    eval("\u200B\u200BAnbindung an digitale Liste", DE);
    eval("Anbindung an digitale Liste", DE);
    eval("IT-Zertifikate", DE);
    eval("Klebeband", DE);
    eval("", DE);
    eval("", DE);
    eval("DE/Bad\n" +
         "Ernährung\n" +
         "Mappe /Drucker\n" +
         "Elektro sortieren\n" +
         "WZ /Steckdose\n", DE);
    eval("Bequem", DE);
    eval("danke schon", DE);
    eval("der Schwund", DE);
    eval("E-Bikes Vergleichen", DE);
    eval("Enterprise-Lösung", DE);
    eval("Expressversand", DE);
    eval("Hanteln", DE);
    eval("Impressum", DE);
    eval("Kulinarik", DE);
    eval("Leder", DE);
    eval("Metall", DE);
    eval("Mission erfüllt! 💪", DE);
    eval("Monitor-Halterungen", DE);
    eval("Montageservice", DE);
    eval("Nummer ist besetzt", DE);
    eval("Paare", DE);
    eval("Professionell", DE);
    eval("Schwarz", DE);
    eval("Wahle den Ort", DE);
    eval("Web & Technologie", DE);
    eval("weil die die siend", DE);
    eval("W-Lan", DE);

    eval("If the", EN);
    eval("if the man", EN);
    eval("if the man is", EN);
    eval("Die in peace", EN);
    eval("Paste text", EN);
    eval("do it", EN);
    eval("I added variant verb inflections to coherency_exact_premium.txt (https://github.com/languagetooler-gmbh/languagetool-premium/commit/1cbbcd4648aeb349c3ae9126dbe9141abd77b975).", EN);
    eval("I added variant verb inflections to coherency_exact_premium.txt.", EN);
    eval("Customizable Plans", EN);
    eval("Rebuilt Celect EUI Injector for\n" +
         "Cummins N14 3411765RX", EN);
    eval("und:\n" +
         ">", EN);
    eval("\n" +
         "\n" +
         "    BK 2.26.2\n" +
         "    BK-1101 - D -\n" +
         "    BK-1104 - D -\n" +
         "\n" +
         "    Content for test\n" +
         "\n" +
         "    H\n" +
         "    BK version 2.26.2 - Executed BK-1101, 1104 test script\n", EN);
    eval("QUESTIONS ONLY!!!", EN);
    eval("My town", EN);
    eval("scandalised", EN);
    eval("a)", EN);
    eval("decaying", EN);
    eval("Ok profesor", EN);

    eval("Bewerk lettertypen", NL);
    eval("Creeer", NL);

    eval("Colores", ES);
    eval("listo", ES);
    eval("quieto", ES);

    eval("Intervention Souris", FR);
    eval("Contacts Disponible", FR);
    eval("La box Harry Potter accessoires", FR);
    eval("Guides D'Achat", FR);
    eval("Lapeyre", FR);
    eval("mangeable", FR);
    eval("Merci beacoupeval", FR);
    eval("Lapeyre", FR);
    eval("Merci beacoup", FR);

    eval("Зараз десь когось нема", UK);
    eval("по тихому", UK);

    eval("Brand in arrivo!", IT);

    printSummary();

    // cases that are actually difficult:
    //Lettertypes is dutch but english is detected
    //Fermer is french but english is detected
    //Cerrar is spanish but swedish is detected
    //UST.-ID is German but is detected as Polish
    //pt-br: marso (detected as it)
  }

  private void eval(String input, String expectedLang) throws IOException {
    Map<String, Double> map;
    if (MODE.equals("ngram")) {
      map = ngram.detectLanguages(input, Collections.emptyList());
    } else {
      map = ft.runFasttext(input, Collections.emptyList());
    }
    double max = 0;
    String bestLang = null;
    for (Map.Entry<String, Double> entry : map.entrySet()) {
      if (entry.getValue() > max) {
        max = entry.getValue();
        bestLang = entry.getKey();
      }
    }
    langTotalCount.compute(expectedLang, (k, v) -> v == null ? 1 : v + 1);
    if (expectedLang.equals(bestLang)) {
      langToCorrectCount.compute(expectedLang, (k, v) -> v == null ? 1 : v + 1);
      System.out.print("GOOD ");
    } else {
      System.out.print("BAD  ");
    }
    System.out.printf(Locale.ENGLISH, "%s (expected: %s) with confidence %.2f for: %s\n", bestLang, expectedLang, max,
            input.replaceAll("\n", "\\\\n"));
  }

  private void printSummary() {
    List<String> langs = Arrays.asList(DE, EN, FR, ES, IT, UK);
    System.out.println("\nCorrectly detected (" + MODE + "):");
    for (String lang : langs) {
      System.out.println(lang + ": " + langToCorrectCount.get(lang) + " out of " + langTotalCount.get(lang));
    }
  }

  public static void main(String[] args) throws IOException {
    LangIdentExamples eval = new LangIdentExamples();
    eval.run();
  }

}
