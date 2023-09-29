package org.languagetool.dev;

import org.languagetool.JLanguageTool;
import org.languagetool.language.en.AmericanEnglish;
import org.languagetool.language.de.GermanyGerman;
import org.languagetool.rules.de.GermanSpellerRule;
import org.languagetool.rules.en.MorfologikAmericanSpellerRule;
import org.languagetool.tagging.de.GermanTagger;
import org.languagetool.tools.StringTools;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * List words from given wordlist that are unknown to the German speller or German tagger.
 *
 * Also, remove English words from wordlist.
 *
 * Wordlist should be in CSV format
 * word,freq
 */
public class MissingGermanWords {

  private final String filename;
  private final boolean outputCombinedListing;
  private final GermanSpellerRule germanSpeller;
  private final GermanTagger germanTagger;
  private final MorfologikAmericanSpellerRule englishSpeller;

  public MissingGermanWords(String filename) throws IOException {
    this.filename = filename;
    this.outputCombinedListing = true;
    germanSpeller = new GermanSpellerRule(JLanguageTool.getMessageBundle(), new GermanyGerman());
    germanTagger = new GermanTagger();
    englishSpeller = new MorfologikAmericanSpellerRule(JLanguageTool.getMessageBundle(), new AmericanEnglish());
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.out.println("Usage: " + MissingGermanWords.class.getSimpleName() + " <filename>");
      System.exit(1);
    }
    String filename = args[0];
    new MissingGermanWords(filename).run();
  }

  private void run() throws IOException {
    if (outputCombinedListing) {
      listMissingWords(filename);
    } else {
      listMissingWordsSpeller(filename);
      listMissingWordsTagger(filename);
    }
  }

  private void listMissingWordsSpeller(String filename) throws java.io.IOException {
    System.out.println("# missing words speller");
    BufferedReader reader = getReaderForFilename(filename);
    String line;
    while ((line = reader.readLine()) != null) {
      String word = wordFromLine(line);
      if (!isKnownByGermanSpeller(word) && !isKnownByEnglishSpeller(word)) {
        System.out.println(line);
      }
    }
    reader.close();
  }

  private void listMissingWordsTagger(String filename) throws java.io.IOException {
    System.out.println("# missing words tagger");
    BufferedReader reader = getReaderForFilename(filename);
    String line;
    while ((line = reader.readLine()) != null) {
      String word = wordFromLine(line);
      if (!isKnownByGermanTagger(word) && !isKnownByEnglishSpeller(word)) {
        System.out.println(line);
      }
    }
    reader.close();
  }

  private void listMissingWords(String filename) throws java.io.IOException {
    BufferedReader reader = getReaderForFilename(filename);
    String line;
    while ((line = reader.readLine()) != null) {
      String word = wordFromLine(line);
      boolean knownBySpeller = isKnownByGermanSpeller(word);
      boolean knownByTagger = isKnownByGermanTagger(word);
      if ((!knownBySpeller || !knownByTagger) && !isKnownByEnglishSpeller(word)) {
        System.out.print(line);
        System.out.print(",");
        if (!knownBySpeller && !knownByTagger) {
          System.out.println("speller+tagger");
        } else if (!knownBySpeller) {
          System.out.println("speller");
        } else {
          System.out.println("tagger");
        }
      }
    }
    reader.close();
  }

  private boolean isKnownByGermanSpeller(String word) {
    return !germanSpeller.isMisspelled(StringTools.uppercaseFirstChar(word)) ||
      !germanSpeller.isMisspelled(StringTools.lowercaseFirstChar(word));
  }

  private boolean isKnownByGermanTagger(String word) throws IOException {
    return germanTagger.lookup(StringTools.uppercaseFirstChar(word)) != null ||
      germanTagger.lookup(StringTools.lowercaseFirstChar(word)) != null;
  }

  private boolean isKnownByEnglishSpeller(String word) throws IOException {
    return !englishSpeller.isMisspelled(StringTools.uppercaseFirstChar(word)) ||
      !englishSpeller.isMisspelled(StringTools.lowercaseFirstChar(word));
  }

  private BufferedReader getReaderForFilename(String filename) throws FileNotFoundException {
    FileInputStream fis = new FileInputStream(filename);
    InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
    return new BufferedReader(isr);
  }

  private String wordFromLine(String line) {
    return line.split(",")[0];
  }
}
