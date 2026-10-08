package org.languagetool;

import java.util.Collections;

public class CatalanDisambiguationProfile {
  public static void main(String[] args) throws Exception {
    // Fetch the language dynamically to avoid the double-instantiation crash
    Language catalan = Languages.getLanguageForShortCode("ca");
    JLanguageTool langTool = new JLanguageTool(catalan);

    String text = String.join(" ", Collections.nCopies(50,
      "L'objectiu principal és la creació d'una eina lliure i de codi obert per a la " +
        "correcció gramatical i d'estil. Això és una prova de text en català molt llarg " +
        "per mesurar el rendiment del desambiguador."
    ));

    System.out.println("Warming up JVM (JIT compilation)...");
    for (int i = 0; i < 50; i++) {
      langTool.analyzeText(text);
    }

    System.out.println("Starting profiling phase...");
    long start = System.currentTimeMillis();

    for (int i = 0; i < 500; i++) {
      langTool.analyzeText(text);
    }

    long end = System.currentTimeMillis();
    System.out.println("Finished in " + (end - start) + "ms");
  }
}