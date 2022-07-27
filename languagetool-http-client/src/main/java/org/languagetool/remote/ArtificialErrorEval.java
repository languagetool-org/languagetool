/* LanguageTool, a natural language style checker
 * Copyright (C) 2022 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.remote;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tools.StringTools;
import org.languagetool.tools.Tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Takes correct sentences, introduces errors (e.g. confusion pairs), and
 * evaluates the LT rules.
 */
public class ArtificialErrorEval {

  static String[] words = new String[2];
  static String[] lemmas = new String[2];
  static String[] fakeRuleIDs = new String[2];
  static List<String> classifyTypes = Arrays.asList("TP", "FP", "TN", "FN", "TPs");
  static int[][] results = new int[2][5]; // word0/word1 ; TP/FP/TN/FN/TP with expected suggestion
  static int[] accumulateResults = new int[5]; // totalErrors/TP/FP/TN/FN
  static RemoteLanguageTool lt;
  static JLanguageTool localLt;
  static Synthesizer synth;
  static int maxInputSentences = 1000000; // decrease this number for testing
  static boolean verboseOutput = false;
  static boolean unidirectional = false;
  static boolean wholeword = true;
  static boolean isDoubleLetters = false;
  static boolean inflected = false;
  static Pattern pWordboundaries = Pattern.compile("\\b.+\\b");
  static int countLine = 0;
  static int checkedSentences = 0;
  static int maxCheckedSentences = 1000000; // decrease this number for testing
  static List<String> onlyRules = new ArrayList<String>();
  static String summaryOutputFilename = "";
  static String verboseOutputFilename = "";
  static String errorCategory = "";
  static String langCode = "";
  static String corpusFilePath = "";
  static String outputPathRoot = "";
  static HashMap<String, List<RemoteRuleMatch>> cachedMatches; 

  public static void main(String[] args) throws IOException {
    //use configuration file
    if (args.length==1) {
      String configurationFilename = args[0];
      Properties prop = new Properties();
      FileInputStream fis = new FileInputStream(configurationFilename);
      prop.load(fis);
      String inputFolder = prop.getProperty("inputFolder");
      String outpuFolder = prop.getProperty("outputFolder");
      String remoteServer = prop.getProperty("remoteServer");
      String maxInputSentencesStr = prop.getProperty("maxInputSentences");
      String maxCheckedSentencesStr = prop.getProperty("maxCheckedSentences");
      if (maxInputSentencesStr != null) {
        maxInputSentences = Integer.parseInt(maxInputSentencesStr);
      }
      if (maxCheckedSentencesStr != null) {
        maxCheckedSentences = Integer.parseInt(maxCheckedSentencesStr) + 1;
      }
      boolean printSummaryDetails = Boolean.parseBoolean(prop.getProperty("printSummaryDetails", "true"));
      boolean printHeader = Boolean.parseBoolean(prop.getProperty("printHeader", "true"));
      runEvaluationOnFolders(inputFolder, outpuFolder, remoteServer, printSummaryDetails, printHeader);
      System.exit(0);
    }
    if (args.length < 4 || args.length > 12) {
      writeHelp();
      System.exit(1);
    }
    
    //Parse options from args
    for (int k = 4; k < args.length; k++) {
      if (args[k].contentEquals("-v")) {
        verboseOutput = true;
      }
      if (args[k].contentEquals("-u")) {
        unidirectional = true;
      }
      if (args[k].contentEquals("-r")) {
        onlyRules = Arrays.asList(args[k + 1].split(","));
      }
      if (args[k].contentEquals("-s")) {
        summaryOutputFilename = args[k + 1];
      }
      if (args[k].contentEquals("-c")) {
        errorCategory = args[k + 1];
      }
      if (args[k].contentEquals("--inflected")) {
        inflected = true;
      }
    }
    words[0] = args[2];
    words[1] = args[3];
    lemmas[0] = words[0];
    lemmas[1] = words[1];
    langCode = args[0];
    corpusFilePath = args[1];
    Language language = Languages.getLanguageForShortCode(langCode);
    localLt = new JLanguageTool(language);
    synth = language.getSynthesizer();
    lt = new RemoteLanguageTool(Tools.getUrl("http://localhost:8081"));
    run(true);
    // end of parsing from args  
  }
  
  private static void runEvaluationOnFolders(String inputFolder, String outputFolder, String remoteServer, boolean printSummaryDetails, boolean printHeader) throws IOException {
    
    verboseOutput = true;
    SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd");
    Date date = new Date(System.currentTimeMillis());
    outputPathRoot = outputFolder+"/"+formatter.format(date);
    Files.createDirectories(Paths.get(outputPathRoot));
    //TODO: remove existing folder 
    
    lt = new RemoteLanguageTool(Tools.getUrl(remoteServer));
    File[] languageDirectories = new File(inputFolder).listFiles(File::isDirectory);
    for (File languageDirectory : languageDirectories) {
      langCode = languageDirectory.getName();
      Files.createDirectories(Paths.get(outputPathRoot+"/"+langCode));
      summaryOutputFilename = outputPathRoot+"/"+langCode+"/"+langCode+".tsv";
      if (printHeader) {
        appendToFile(summaryOutputFilename, "Category\tRules\tErrors\tPrecision\tRecall\tTP\tFP\tTN\tFN");
      }
      File[] categoryDirectories = languageDirectory.listFiles(File::isDirectory);
      for (File categoryDirectory: categoryDirectories) {
        Arrays.fill(accumulateResults, 0);
        errorCategory = categoryDirectory.getName();
        Files.createDirectories(Paths.get(outputPathRoot+"/"+langCode+"/"+errorCategory));
        File[] corpusFiles = categoryDirectory.listFiles(File::isFile);
        for (File myCorpusFile: corpusFiles) {
          corpusFilePath = myCorpusFile.getAbsolutePath(); 
          String fileName = myCorpusFile.getName();
          System.out.println("Analyzing file: " + fileName);
          fileName = fileName.substring(0, fileName.lastIndexOf('.'));
          if (fileName.equals("double_letters")) {
            isDoubleLetters = true;
            unidirectional = true;
          }
          else {
            isDoubleLetters = false;
            String[] parts = fileName.split("~");
            words[0] = parts[0].replaceAll("_", " ");
            words[1] = parts[1].replaceAll("_", " ");
            unidirectional = false;
            if (parts.length > 2) {
              unidirectional = parts[2].equals("u");
              if (parts[2].equals("u_notwholeword")) {
                unidirectional = true;
                wholeword = false;
              }
              if (parts[2].equals("notwholeword")) {
                unidirectional = false;
                wholeword = false;
              }
            }  
          }
          verboseOutputFilename = outputPathRoot+"/"+langCode+"/"+errorCategory+"/"+myCorpusFile.getName();
          run(printSummaryDetails);
        }
        // total by category
        float precision = accumulateResults[1] / (float) (accumulateResults[1] + accumulateResults[2]);
        float recall = accumulateResults[1] / (float) (accumulateResults[1] + accumulateResults[4]);
        appendToFile (summaryOutputFilename, errorCategory + "\t" + "TOTAL" + "\t" 
            + accumulateResults[0] + "\t" 
            + String.format(Locale.ROOT, "%.4f", precision) + "\t" 
            + String.format(Locale.ROOT, "%.4f", recall) + "\t"
            + accumulateResults[1] + "\t"
            + accumulateResults[2] + "\t"
            + accumulateResults[3] + "\t"
            + accumulateResults[4] + "\t"
            );
      }
    }
    System.out.println("FINISHED!"); 
  }
  
  private static void run(boolean printSummaryDetails) throws IOException {
    Arrays.fill(results[0], 0);
    Arrays.fill(results[1], 0);
    fakeRuleIDs[0] = "rules_" + words[0] + "->" + words[1]; // rules in one direction
    fakeRuleIDs[1] = "rules_" + words[1] + "->" + words[0]; // rules in the other direction
    CheckConfiguration config = new CheckConfigurationBuilder(langCode)
      .disabledRuleIds("WHITESPACE_RULE")
      .textSessionID("-2")
      .build();
    long start = System.currentTimeMillis();
    List<String> lines = Files.readAllLines(Paths.get(corpusFilePath));
    if (!inflected && !isDoubleLetters) {
      final Pattern p0;
      Matcher mWordBoundaries = pWordboundaries.matcher(words[0]);
      if (mWordBoundaries.matches() && wholeword) {
        p0 = Pattern.compile("\\b" + words[0] + "\\b", Pattern.CASE_INSENSITIVE);
      } else {
        p0 = Pattern.compile(words[0], Pattern.CASE_INSENSITIVE);
      }
      final Pattern p1;
      mWordBoundaries = pWordboundaries.matcher(words[1]);
      if (mWordBoundaries.matches() && wholeword) {
        p1 = Pattern.compile("\\b" + words[1] + "\\b", Pattern.CASE_INSENSITIVE);
      } else {
        p1 = Pattern.compile(words[1], Pattern.CASE_INSENSITIVE);
      }
      countLine = 0;
      checkedSentences = 0;
      for (String line : lines) {
        cachedMatches = new HashMap<>();
        countLine++;
        if (countLine > maxInputSentences || checkedSentences > maxCheckedSentences) {
          break;
        }
        boolean foundSomething = false;
        if (words[0].length() > 0) {
          Matcher m = p0.matcher(line);
          while (m.find()) {
            foundSomething = true;
            analyzeSentence(line, 0, m.start(), config);
          }
        }
        if (words[1].length() > 0) {
          Matcher m = p1.matcher(line);
          while (m.find()) {
            foundSomething = true;
            analyzeSentence(line, 1, m.start(), config);
          }
        }
        if (!foundSomething) {
          // printSentenceOutput("Ignored, no error", line, "");
        }
      } 
    } 
    if (isDoubleLetters) {
      // introduce error: nn -> n
      fakeRuleIDs[0] = "rules_double_letters";
      countLine = 0;
      checkedSentences = 0;
      final Pattern p1 = Pattern.compile("([a-zA-Z])\\1+");
      for (String line : lines) {
        cachedMatches = new HashMap<>();
        countLine++;
        if (countLine > maxInputSentences || checkedSentences > maxCheckedSentences) {
          break;
        }
        Matcher m = p1.matcher(line);
        while (m.find()) {
          words[1] = m.group(0);
          words[0] = words[1].substring(0, 1); 
          analyzeSentence(line, 1, m.start(), config);
        }
      }
    }
    if (inflected) {
      // search lemma
      countLine = 0;
      checkedSentences = 0;
      for (String line : lines) {
        cachedMatches = new HashMap<>();
        countLine++;
        if (countLine > maxInputSentences || checkedSentences > maxCheckedSentences) {
          break;
        }
        List<AnalyzedSentence> analyzedSentences = localLt.analyzeText(line);
        boolean foundSomething = false;
        for (AnalyzedSentence analyzedSentence: analyzedSentences) {
          for (AnalyzedTokenReadings token : analyzedSentence.getTokensWithoutWhitespace()) {
            if (lemmas[0].length() > 0) {
              if (token.hasLemma(lemmas[0])) {
                words[0] = token.getToken();
                AnalyzedToken atr1 = token.readingWithLemma(lemmas[0]);
                AnalyzedToken atr2 = new AnalyzedToken(atr1.getToken(), atr1.getPOSTag(), lemmas[1]);
                String[] syntheziedWords = synth.synthesize(atr2, atr2.getPOSTag());
                words[1] = syntheziedWords[0];
                foundSomething = true;
                analyzeSentence(line, 0, token.getStartPos(), config);
              }
            }
            if (lemmas[1].length() > 0) {
              if (token.hasLemma(lemmas[1])) {
                words[1] = token.getToken();
                AnalyzedToken atr1 = token.readingWithLemma(lemmas[1]);
                AnalyzedToken atr2 = new AnalyzedToken(atr1.getToken(), atr1.getPOSTag(), lemmas[0]);
                String[] syntheziedWords = synth.synthesize(atr2, atr2.getPOSTag());
                words[0] = syntheziedWords[0];
                foundSomething = true;
                analyzeSentence(line, 1, token.getStartPos(), config);
              }
            }
          }
        }
      }
    }
    
    // print results
    int oneOrTwo = (unidirectional ? 1 : 2);
    for (int i = 0; i < oneOrTwo; i++) {
      float precision = results[i][classifyTypes.indexOf("TP")]
          / (float) (results[i][classifyTypes.indexOf("TP")] + results[i][classifyTypes.indexOf("FP")]);
      float recall = results[i][classifyTypes.indexOf("TP")]
          / (float) (results[i][classifyTypes.indexOf("TP")] + results[i][classifyTypes.indexOf("FN")]);
      //float expectedSuggestionPercentage = (float) results[i][classifyTypes.indexOf("TPs")]
      //    / results[i][classifyTypes.indexOf("TP")];
      int errorsTotal = results[i][classifyTypes.indexOf("TP")] + results[i][classifyTypes.indexOf("FP")]
          + results[i][classifyTypes.indexOf("TN")] + results[i][classifyTypes.indexOf("FN")];
      StringWriter resultsString = new StringWriter();

      resultsString.append("-------------------------------------\n");
      resultsString.append("Results for " + fakeRuleIDs[i] + "\n");
      //resultsString.append("TP (with expected suggestion): " + results[i][4] + "\n");
      for (int j = 0; j < 4; j++) {
        resultsString.append(classifyTypes.get(j) + ": " + results[i][j] + "\n");
      }

      resultsString.append("Precision: " + String.format(Locale.ROOT, "%.4f", precision) + "\n");
      resultsString.append("Recall: " + String.format(Locale.ROOT, "%.4f", recall) + "\n");
      // out.write("TP with expected suggestion: " + String.format("%.4f",
      // expectedSuggestionPercentage)+"\n");
      resultsString.append("Errors: " + String.valueOf(errorsTotal) + "\n");
      appendToFile(verboseOutputFilename, resultsString.toString());
      
      if (printSummaryDetails) {
          appendToFile(summaryOutputFilename, errorCategory + "\t" + fakeRuleIDs[i]
                  + "\t" + errorsTotal + "\t" + String.format(Locale.ROOT, "%.4f", precision) + "\t" + String.format(Locale.ROOT, "%.4f", recall) + "\t"
                  + results[i][classifyTypes.indexOf("TP")] + "\t"
                  + results[i][classifyTypes.indexOf("FP")] + "\t"
                  + results[i][classifyTypes.indexOf("TN")] + "\t"
                  + results[i][classifyTypes.indexOf("FN")] + "\t");
      }
      
      accumulateResults[0] += errorsTotal;
      accumulateResults[1] += results[i][classifyTypes.indexOf("TP")];
      accumulateResults[2] += results[i][classifyTypes.indexOf("FP")];
      accumulateResults[3] += results[i][classifyTypes.indexOf("TN")];
      accumulateResults[4] += results[i][classifyTypes.indexOf("FN")];
      
    }
    float time = (float) ((System.currentTimeMillis() - start) / 1000.0);
    System.out.println("Total time: " + String.format(Locale.ROOT, "%.2f", time) + " seconds");
    System.out.println("-------------------------------------");
  }
  
  private static void appendToFile(String FilePath, String text) throws IOException {
    if (!FilePath.isEmpty()) { 
      try (BufferedWriter out = new BufferedWriter(new FileWriter(FilePath, true))) {
        out.write(text + "\n");
      }
    } else {
      System.out.println(text);
    }
  }

  private static void analyzeSentence(String correctSentence, int j, int fromPos, CheckConfiguration config)
      throws IOException {
    // Correct sentence
    if (!unidirectional || j == 0) {
      List<RemoteRuleMatch> matchesCorrect;
      if (cachedMatches.containsKey(correctSentence)) {
        matchesCorrect = cachedMatches.get(correctSentence);
      } else {
        matchesCorrect = lt.check(correctSentence, config).getMatches();
        checkedSentences++;
        cachedMatches.put(correctSentence, matchesCorrect);
      }
      String replaceWith = words[1 - j];
      String originalString = correctSentence.substring(fromPos, fromPos + words[j].length());
      if (StringTools.isCapitalizedWord(originalString)) {
        replaceWith = StringTools.uppercaseFirstChar(replaceWith);
      }
      List<String> ruleIDs = ruleIDsAtPos(matchesCorrect, fromPos, replaceWith);
      if (ruleIDs.size() > 0) {
        results[j][classifyTypes.indexOf("FP")]++;
        printSentenceOutput("FP", correctSentence, fakeRuleIDs[j] + ":" + String.join(",", ruleIDs));
      } else {
        results[j][classifyTypes.indexOf("TN")]++;
        // Too verbose...
        // printSentenceOutput("TN", correctSentence, fakeRuleIDs[j]);
      }
    }
    // Wrong sentence
    if (!unidirectional || j == 1) {
      String replaceWith = words[1 - j];
      String originalString = correctSentence.substring(fromPos, fromPos + words[j].length());
      if (StringTools.isCapitalizedWord(originalString)) {
        replaceWith = StringTools.uppercaseFirstChar(replaceWith);
      }
      if (StringTools.isAllUppercase(originalString)) {
        replaceWith = replaceWith.toUpperCase();
      }
      String wrongSentence = correctSentence.substring(0, fromPos) + replaceWith
          + correctSentence.substring(fromPos + words[j].length(), correctSentence.length());
      if (wrongSentence.equals(correctSentence)) {
        // Should not happen
        printSentenceOutput("Error: word cannot be replaced", correctSentence, "");
        return;
      }    
      List<RemoteRuleMatch> matchesWrong;
      if (cachedMatches.containsKey(wrongSentence)) {
        matchesWrong = cachedMatches.get(wrongSentence);
      } else {
        matchesWrong = lt.check(wrongSentence, config).getMatches();
        checkedSentences++;
        cachedMatches.put(wrongSentence, matchesWrong);
      }
      
      List<String> ruleIDs = ruleIDsAtPos(matchesWrong, fromPos, originalString);
      if (ruleIDs.size() > 0) {
        //results[1 - j][classifyTypes.indexOf("TP")]++;
        if (isExpectedSuggestionAtPos(matchesWrong, fromPos, originalString, wrongSentence, correctSentence)) {
          //results[1 - j][classifyTypes.indexOf("TPs")]++;
          results[1 - j][classifyTypes.indexOf("TP")]++;
          printSentenceOutput("TP", wrongSentence, fakeRuleIDs[1 - j] + ":" + String.join(",", ruleIDs));
        } else {
          //printSentenceOutput("TP no expected suggestion", wrongSentence,
          //    fakeRuleIDs[1 - j] + ":" + String.join(",", ruleIDs));
          results[1 - j][classifyTypes.indexOf("FN")]++;
          printSentenceOutput("FN", wrongSentence, fakeRuleIDs[1 - j]);
        }
      } else {
        results[1 - j][classifyTypes.indexOf("FN")]++;
        printSentenceOutput("FN", wrongSentence, fakeRuleIDs[1 - j]);
      }
    }
  }

  private static void printSentenceOutput(String classification, String sentence, String ruleIds) throws IOException {
    if (verboseOutput) {
      if (verboseOutputFilename.isEmpty()) {
        System.out.println(countLine + ". " + classification + ": " + sentence + " –– " + ruleIds);
      } else {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(verboseOutputFilename, true))) {
          out.write(countLine + "\t" + classification + "\t" + sentence + "\t" + ruleIds+"\n");
        }  
      }
      
    }
  }

  private static List<String> ruleIDsAtPos(List<RemoteRuleMatch> matchesCorrect, int pos, String expectedSuggestion) {
    List<String> ruleIDs = new ArrayList<>();
    for (RemoteRuleMatch match : matchesCorrect) {
      if (match.getErrorOffset() <= pos && match.getErrorOffset() + match.getErrorLength() >= pos) {
        if (!onlyRules.isEmpty() && !onlyRules.contains(match.getRuleId())) {
          continue;
        }
        String subId = null;
        List<String> replacements = null;
        try {
          subId = match.getRuleSubId().get();
        } catch (NoSuchElementException e) {
          // System.out.println("Exception, skipping '" + countLine + "': ");
          // e.printStackTrace();
        }
        try {
          replacements = match.getReplacements().get();
        } catch (NoSuchElementException e) {
        }
        boolean containsDesiredSuggestion = false;
        if (replacements != null) {
          for (String replacement : replacements) {
            if (replacement.contains(expectedSuggestion.trim())) {
              containsDesiredSuggestion = true;
            }
          } 
        }
        if (!containsDesiredSuggestion) {
          continue;
        }
        if (subId != null) {
          ruleIDs.add(match.getRuleId() + "[" + match.getRuleSubId().get() + "]");
        } else {
          ruleIDs.add(match.getRuleId());
        }
      }
    }
    return ruleIDs;
  }

  private static boolean isExpectedSuggestionAtPos(List<RemoteRuleMatch> matchesCorrect, int pos,
      String expectedSuggestion, String wrongSentence, String correctSentence) {
    for (RemoteRuleMatch match : matchesCorrect) {
      if (match.getErrorOffset() <= pos && match.getErrorOffset() + match.getErrorLength() >= pos) {
        for (String s : match.getReplacements().get()) {
          // check that the replacement rebuilds the original correct sentence
          String correctedSentence = wrongSentence.substring(0, match.getErrorOffset()) + s
              + wrongSentence.substring(match.getErrorOffset() + match.getErrorLength(), wrongSentence.length());
          if (correctedSentence.equals(correctSentence)) {
            return true;
          }
        }
      }
    }
    return false;
  }
  
  private static void writeHelp() {
    System.out.println("Usage: " + ArtificialErrorEval.class.getSimpleName()
        + " <language code> <file> <string1> <string2> <options>");
    System.out.println("  <language code>, e.g. en, en-US, de, fr...");
    System.out.println("  <file> is a file with correct sentences, once sentence per line; errors will be "
        + "introduced and each line will be checked");
    System.out.println("  <string1> is the string to be replaced by <string2>, word boundaries will be "
        + "assumed at the start and the end of the strings");
    System.out.println("  <options>");
    System.out.println("    -v           verbose, print all false positive or false negative sentences");
    System.out.println("    -u           unidirectional, analyze only rules for string1 (wrong) -> string2 (correct)");
    System.out.println("    -r           list of comma-separated rules to be considered");
    System.out.println("    -s           summary output file");
    System.out.println("    -c           error category");
    System.out.println("    --inflected  search lemmas insted of forms");
  }
}
