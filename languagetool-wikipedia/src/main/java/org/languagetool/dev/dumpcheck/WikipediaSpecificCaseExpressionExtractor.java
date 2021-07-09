/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.dumpcheck;

import java.io.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Pattern;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.languagetool.Language;
import org.languagetool.Languages;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Command line tool to extract <strong>Greek</strong> expressions, that 
 * their words are starting with capital letter, 
 * from a (optionally bz2-compressed) Wikipedia XML dump.
 */
class WikipediaSpecificCaseExpressionExtractor {

  /**
   * The HashMap that contains the specific case expressions and how many times
   * they are found
   */
  private static HashMap<String, Integer> specificCaseExpressionsCounter =
    new HashMap<>();

  /**
   * The number of expressions to be extracted
   */
  private final int NUMBER_OF_EXPRESSIONS = 100;

  /**
   * This method extracts the specific case expressions and writes them in the
   * given file. Based on the extract method of the WikipediaSentenceExtractor.
   * 
   * @param language    the language of the xml dump
   * @param xmlDumpPath the path of the xml dump
   * @param outputFile  the path of the file to write the extracted expressions
   */
  private void extractSpecificCaseExpressions(Language language, 
               String xmlDumpPath, String outputFile) 
               throws IOException, CompressorException {
    try (FileInputStream fis = new FileInputStream(xmlDumpPath);
    BufferedInputStream bis = new BufferedInputStream(fis);
    FileWriter fw = new FileWriter(outputFile)) {
      InputStream input;
      if (xmlDumpPath.endsWith(".bz2")) {
        input = new CompressorStreamFactory().createCompressorInputStream(bis);
      } else if (xmlDumpPath.endsWith(".xml")) {
        input = bis;
      } else {
        throw new IllegalArgumentException("Unknown file name, expected '.xml' or '.bz2': " 
                                              + xmlDumpPath);
      }
      WikipediaSentenceSource source = new WikipediaSentenceSource(input, language);
      while (source.hasNext()) {
        String sentence = source.next().getText();
        if (skipSentence(sentence)) {
          continue;
        }
        detectSpecificCaseExpressions(sentence);
      }
  
      specificCaseExpressionsCounter = sortByValue(specificCaseExpressionsCounter);
      int number_of_expressions_added = 0;
      for (String foundExpression : specificCaseExpressionsCounter.keySet()) {
        // System.out.println(foundExpression + " : " +
         // specificCaseExpressionsCounter.get(foundExpression));
        fw.write(foundExpression);
        fw.write('\n');
        number_of_expressions_added++;
        if (number_of_expressions_added == NUMBER_OF_EXPRESSIONS) {
          break;
        }
      }
    }
  }

  /**
   * This method adds the specific case expressions of the given sentence to the
   * specificCaseExpressionCounter.
   * @param sentence the sentence to detect specific case exceptions
   */
  private void detectSpecificCaseExpressions(String sentence) {
    Queue<String> specificCaseQueue = new LinkedList<>(); // contains back to back words 
                                                          // that start with uppercase
    String[] words = sentence.split(" ");
    boolean isExpressionFound = false, // true if an expression is present in the queue
              isExpressionFinished = false; // true if the expression does not continue
  
    for (int i = 1; i < words.length; i++) { // starting from 1 as the first word of 
                                           // the sentence will always start with capital
      String word = words[i];
      if (!containsLetter(word)) { // if the word does not contain any letters
        continue;
      }
  
      // to check if the last character is symbol, which means that any expression
      // created till now is finished
      if (isExpressionFound) {
        // to check if the first character is symbol, which means that any expression
          // created till now is finished and a new one can start
          // with the current word
          if (!Character.isLetter(word.charAt(0))) {
            incrementSpecificCaseExpressionsCounter(specificCaseQueue);
            isExpressionFound = false;
            isExpressionFinished = false;
            // to check if the last character is symbol, which means that any expression
            // created till now is finished
          } else if (!Character.isLetter(word.charAt(word.length() - 1))) {
        isExpressionFinished = true;
          }
      }
        // remove any special characters like brackets and commas except " ' "
      word = word.replaceAll("^[^α-ωΑ-Ωίϊΐόάέύϋΰήώ'\\s]+|[^α-ωΑ-Ωίϊΐόάέύϋΰήώ'\\s]+$", "");
      
      if (Character.isUpperCase(word.charAt(0))) {
        specificCaseQueue.add(word);
        if (specificCaseQueue.size() > 1) {
        isExpressionFound = true;
          }
      } else {
        if (isExpressionFound) {
        isExpressionFinished = true;
        } else if (specificCaseQueue.size() > 0) {
        specificCaseQueue.remove();
        }
      }
      if (isExpressionFound && isExpressionFinished) {
        incrementSpecificCaseExpressionsCounter(specificCaseQueue);
        isExpressionFound = false;
        isExpressionFinished = false;
      }
    }
  }

  /**
   * This method sorts the HashMap according to the values in descending order.
   * @param hm the HashMap to sort
   * @return the given HashMap sorted
   */
  public static HashMap<String, Integer> sortByValue(HashMap<String, Integer> hm) {
    // Create a list from elements of HashMap
    List<Map.Entry<String, Integer>> list = new LinkedList<>(hm.entrySet());

    Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
      public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
        return (o2.getValue()).compareTo(o1.getValue());
      }
    });

    // put data from sorted list to hashmap
    HashMap<String, Integer> temp = new LinkedHashMap<>();
    for (Map.Entry<String, Integer> aa : list) {
      temp.put(aa.getKey(), aa.getValue());
    }
    return temp;
  }

  /**
   * This method adds the given expression to the specificCaseExpressionsCounter
   * @param queueContainingExpression a queue that contains the expression to add
   */
  private void incrementSpecificCaseExpressionsCounter(Queue<String> queueContainingExpression) {
    String specificCaseExpression = "";
    while (!queueContainingExpression.isEmpty()) {
      String wordInExpression = queueContainingExpression.remove();
      specificCaseExpression += wordInExpression + " ";
    }
    int count = specificCaseExpressionsCounter.getOrDefault(specificCaseExpression, 0);
      specificCaseExpressionsCounter.put(specificCaseExpression, count + 1);
  }

  /**
   * This method checks if the given string contains at least one letter.
   * 
   * @param word the word to check
   * @return <code>true</code> if the word contains at least one letter, else
   *         <code>false</code>
   */
  private static boolean containsLetter(String word) { // returns true if at least one character is letter
    return Pattern.matches(".*[α-ωΑ-Ωίϊΐόάέύϋΰήώ\\s].*", word);
  }

  /**
   * This method checks if the given expression should be skipped.
   * 
   * @param sentence the sentence to check
   * @return <code>true</code> if the sentence should be skipped, else
   *         <code>false</code>
   */
  private boolean skipSentence(String sentence) {
    return sentence.trim().length() == 0 || Character.isLowerCase(sentence.trim().charAt(0))
        || !sentence.matches("^[α-ωΑ-Ωίϊΐόάέύϋΰήώ\\s].*$");
  }

  /**
   * The main method.
   * @param args args[0] the path of the xml dump, 
   *             args[1] the path of the output file
   */
  public static void main(String[] args) throws IOException, CompressorException {
    if (args.length != 2) {
      System.out.println("Usage: " + WikipediaSentenceExtractor.class.getSimpleName()
                          + " <langCode> <wikipediaXmlDump> <output>");
      System.exit(1);
    }
    WikipediaSpecificCaseExpressionExtractor extractor = new WikipediaSpecificCaseExpressionExtractor();
    extractor.extractSpecificCaseExpressions(Languages.getLanguageForShortCode("el"), args[0], args[1]);
  }
}
