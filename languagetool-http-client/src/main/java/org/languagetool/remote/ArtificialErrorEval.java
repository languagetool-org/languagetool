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
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
  //TP: true positive with the expected suggestion
  //TPns: true positive with no suggestion
  static List<String> classifyTypes = Arrays.asList("TP", "FP", "TN", "FN", "TPns", "TPws");
  static int[][] results = new int[2][6]; // word0/word1 ; TP/FP/TN/FN/TP with no suggestion/TP wrong suggestion
  static int[] accumulateResults = new int[6]; // totalErrors/TP/FP/TN/FN
  static RemoteLanguageTool lt;
  static JLanguageTool localLt;
  static Synthesizer synth;
  static int maxInputSentences = 1000000; // decrease this number for testing
  static boolean verboseOutput = false;
  static boolean unidirectional = false;
  static boolean wholeword = true;
  static boolean isDoubleLetters = false;
  static boolean isDiacritics = false;
  static boolean inflected = false;
  static boolean isParallelCorpus = false;
  static int columnCorrect = 1;
  static int columnIncorrect = 2;
  static Pattern pWordboundaries = Pattern.compile("\\b.+\\b");
  static int countLine = 0;
  static int checkedSentences = 0;
  static int maxCheckedSentences = 1000000; // decrease this number for testing
  static List<String> onlyRules = new ArrayList<String>();
  static List<String> disabledRules = new ArrayList<String>();
  static List<String> enabledOnlyRules = new ArrayList<String>();
  static String summaryOutputFilename = "";
  static String verboseOutputFilename = "";
  static String errorCategory = "";
  static String langCode = "";
  static Language language;
  static String corpusFilePath = "";
  static String outputPathRoot = "";
  static HashMap<String, List<RemoteRuleMatch>> cachedMatches; 
  static String remoteServer = "http://localhost:8081";
  static String userName = "";
  static String apiKey = "";

  public static void main(String[] args) throws IOException {
    long start = System.currentTimeMillis();
    //use configuration file
    if (args.length==1) {
      String configurationFilename = args[0];
      Properties prop = new Properties();
      FileInputStream fis = new FileInputStream(configurationFilename);
      prop.load(new InputStreamReader(fis, Charset.forName("UTF-8")));
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
      remoteServer = prop.getProperty("remoteServer", "http://localhost:8081");
      String enabledOnlyRulesStr = prop.getProperty("enabledOnlyRules", "").trim();
      if (!enabledOnlyRulesStr.isEmpty()) {
        enabledOnlyRules = Arrays.asList(enabledOnlyRulesStr.split(","));
      }
      String disabledRulesStr = prop.getProperty("disabledRules", "");
      if (!disabledRulesStr.isEmpty()) {
        disabledRules = Arrays.asList(disabledRulesStr.split(","));  
      }
      String onlyRulesStr = prop.getProperty("onlyRules", ""); 
      if (!onlyRulesStr.isEmpty()) {
        onlyRules = Arrays.asList(onlyRulesStr.split(","));  
      }
      userName = prop.getProperty("userName", "");
      apiKey = prop.getProperty("apiKey", "");
      String inputFolder = prop.getProperty("inputFolder", "").trim();
      String outputFolder = prop.getProperty("outputFolder", inputFolder).trim();
      // Only one file
      //String analyzeOneFile = prop.getProperty("analyzeOneFile");
      String inputFilename = prop.getProperty("inputFile", "").trim();
      if (!inputFilename.isEmpty()) {
        runEvaluationOnFile(prop.getProperty("languageCode"), inputFilename, outputFolder);
      }
      if (!inputFolder.isEmpty()) {
        summaryOutputFilename = outputFolder + "/" + prop.getProperty("languageCode") + "-summary.tsv";
        appendToFile(summaryOutputFilename, "Category\tRules\tSentences\tPrecision\tRecall\tTP\tFP\tTN\tFN\tTPns\tTPws");
        File[] inputFiles = new File(inputFolder).listFiles(File::isFile);
        for (File inputFile : inputFiles) {
          runEvaluationOnFile(prop.getProperty("languageCode"), inputFile.getAbsolutePath(), outputFolder);
        }
      }
      /* Obsolete...
      else {
        String inputFolder = prop.getProperty("inputFolder");
        String outpuFolder = prop.getProperty("outputFolder");
        runEvaluationOnFolders(inputFolder, outpuFolder, printSummaryDetails, printHeader);
      }*/
    }
    // language code + input file
    else if (args.length == 2) { 
      runEvaluationOnFile(args[0], args[1], "");
    } else {
      writeHelp();
      System.exit(1);  
    }
    System.out.println(printTimeFromStart(start, "Total time:"));
  }
  
  private static void runEvaluationOnFile(String languageCode, String inputFile, String outputFolder) throws IOException {
    langCode = languageCode;
    corpusFilePath = inputFile;
    verboseOutput = true;
    language = Languages.getLanguageForShortCode(langCode);
    localLt = new JLanguageTool(language);
    synth = language.getSynthesizer();
    lt = new RemoteLanguageTool(Tools.getUrl(remoteServer));
    File corpusFile = new File(corpusFilePath);
    if (!corpusFile.exists() || corpusFile.isDirectory()) {
      throw new IOException("File not found: " + corpusFilePath);
    }
    String fileName = corpusFile.getName();
    System.out.println("Analyzing file: " + fileName);
    fileName = fileName.substring(0, fileName.lastIndexOf('.'));
    if (outputFolder.isEmpty()) {
      verboseOutputFilename = corpusFile.getParentFile()+ "/"+ fileName + "-results.txt";
    } else {
      verboseOutputFilename = outputFolder + fileName + "-results.txt";
    }
    
    // reset all global variables to default
    unidirectional = false;
    wholeword = true;
    isDoubleLetters = false;
    isDiacritics = false;
    inflected = false;
    isParallelCorpus = false;
    columnCorrect = 1;
    columnIncorrect = 2;
    if (fileName.startsWith("parallelcorpus") || fileName.startsWith("pc-")) {
      isParallelCorpus = true;
      unidirectional = true;
      wholeword = false;
      String parts[] = fileName.split("-");
      if (parts.length > 2) {
        columnCorrect = Integer.parseInt(parts[1]);
        columnIncorrect = Integer.parseInt(parts[2]);
      }
    }
    else if (fileName.equals("diacritics")) {
      isDiacritics = true;
      unidirectional = true;
    }
    else if (fileName.equals("double_letters")) {
      isDoubleLetters = true;
      unidirectional = true;
    }
    else {
      String[] parts = fileName.split("~");
      words[0] = parts[0].replaceAll("_", " ");
      words[1] = parts[1].replaceAll("_", " ");
      if (parts.length > 2) {
        unidirectional = parts[2].equals("u");
        if (parts[2].equals("u_notwholeword")) {
          unidirectional = true;
          wholeword = false;
        }
        if (parts[2].equals("notwholeword")) {
          wholeword = false;
        }
      }  
    }
     
    run(true);
  }
  
  private static void runEvaluationOnFolders(String inputFolder, String outputFolder, 
      boolean printSummaryDetails, boolean printHeader) throws IOException {
 
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
      language = Languages.getLanguageForShortCode(langCode);
      Files.createDirectories(Paths.get(outputPathRoot+"/"+langCode));
      summaryOutputFilename = outputPathRoot+"/"+langCode+"/"+langCode+".tsv";
      if (printHeader) {
        appendToFile(summaryOutputFilename, "Category\tRules\tSentences\tPrecision\tRecall\tTP\tFP\tTN\tFN\tTPns\tTPws");
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
          //reset all global Variables to default
          unidirectional = false;
          wholeword = true;
          isDoubleLetters = false;
          isDiacritics = false;
          inflected = false;
          isParallelCorpus = false;
          columnCorrect = 1;
          columnIncorrect = 2;
          if (fileName.startsWith("parallelcorpus") || fileName.startsWith("pc-")) {
            isParallelCorpus = true;
            unidirectional = true;
            String parts[] = fileName.split("-");
            if (parts.length > 2) {
              columnCorrect = Integer.parseInt(parts[1]);
              columnIncorrect = Integer.parseInt(parts[2]);
            }
          }
          else if (fileName.equals("diacritics")) {
            isDiacritics = true;
            unidirectional = true;
          }
          else if (fileName.equals("double_letters")) {
            isDoubleLetters = true;
            unidirectional = true;
          }
          else {
            String[] parts = fileName.split("~");
            words[0] = parts[0].replaceAll("_", " ");
            words[1] = parts[1].replaceAll("_", " ");
            if (parts.length > 2) {
              unidirectional = parts[2].equals("u");
              if (parts[2].equals("u_notwholeword")) {
                unidirectional = true;
                wholeword = false;
              }
              if (parts[2].equals("notwholeword")) {
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
    int ignoredLines = 0;
    Arrays.fill(results[0], 0);
    Arrays.fill(results[1], 0);
    fakeRuleIDs[0] = "rules_" + words[0] + "->" + words[1]; // rules in one direction
    fakeRuleIDs[1] = "rules_" + words[1] + "->" + words[0]; // rules in the other direction
    CheckConfiguration config;
    CheckConfigurationBuilder cfgBuilder = new CheckConfigurationBuilder(langCode);
    cfgBuilder.textSessionID("-2");
    if (enabledOnlyRules.isEmpty()) {
      cfgBuilder.disabledRuleIds("WHITESPACE_RULE");
      if (!disabledRules.isEmpty()) {
        cfgBuilder.disabledRuleIds(disabledRules);
      }
    } else {
      cfgBuilder.enabledRuleIds(enabledOnlyRules).enabledOnly();
    }
    if (!userName.isEmpty() && !apiKey.isEmpty()) {
      cfgBuilder.username(userName).apiKey(apiKey).build();
    }
    config = cfgBuilder.build();
    long start = System.currentTimeMillis();
    List<String> lines = Files.readAllLines(Paths.get(corpusFilePath));
    if (!inflected && !isDoubleLetters && !isDiacritics && !isParallelCorpus) {
      final Pattern p0;
      Matcher mWordBoundaries = pWordboundaries.matcher(words[0]);
      if (mWordBoundaries.matches() && wholeword) {
        p0 = Pattern.compile("\\b" + words[0] + "\\b", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
      } else {
        p0 = Pattern.compile(words[0], Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
      }
      final Pattern p1;
      mWordBoundaries = pWordboundaries.matcher(words[1]);
      if (mWordBoundaries.matches() && wholeword) {
        p1 = Pattern.compile("\\b" + words[1] + "\\b", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
      } else {
        p1 = Pattern.compile(words[1], Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
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
    if (isParallelCorpus) {
      final Pattern p = Pattern.compile("(.*)__(.*)__(.*)");
      countLine = 0;
      checkedSentences = 0;
      for (String line : lines) {
        cachedMatches = new HashMap<>();
        countLine++;
        if (countLine > maxInputSentences || checkedSentences > maxCheckedSentences) {
          break;
        }
        String[] parts = line.split("\t");
        // adjust the numbers 3 and 4 according to the source file
        if (parts.length < columnCorrect && parts.length < columnIncorrect) {
          continue;
        }
        String correctSource = parts[columnCorrect - 1];
        String incorrectSource = parts[columnIncorrect - 1];
        words[0] = null;
        words[1] = null;
        /*String correctSentence = "";
        String incorrectSentence = "";
        /*Matcher mIncorrect = p.matcher(incorrectSource);
        if (mIncorrect.matches()) {
          words[0] = mIncorrect.group(2);
        }
        int posError = -1;
        Matcher mCorrect = p.matcher(correctSource);
        if (mCorrect.matches()) {
          words[1] = mCorrect.group(2);
          correctSentence = mCorrect.group(1) + mCorrect.group(2) + mCorrect.group(3);
          posError = mCorrect.group(1).length();
        }*/
        String correctSentence = correctSource.replaceAll("__", "");
        String incorrectSentence = incorrectSource.replaceAll("__", "");
        if (correctSentence.equals(incorrectSentence)) {
          printSentenceOutput("IGNORED LINE: sentences are identical!", correctSource, 0, "");
          ignoredLines++;
          continue;
        }
        List<String> diffs = differences(correctSentence, incorrectSentence);
        int posError = diffs.get(0).length();
        words[1] = diffs.get(1);
        words[0] = diffs.get(2);
        if (words[1] != null) {
          // words[0] may be null!
          // check FN
          analyzeSentence(correctSentence, 1, posError, config);
          // check FP in the correct sentence
          words[0] = words[1];
          words[1] = null;
          analyzeSentence(correctSentence, 0, posError, config);
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
    if (isDiacritics) {
      // check missing diacritics 
      countLine = 0;
      checkedSentences = 0;
      for (String line : lines) {
        cachedMatches = new HashMap<>();
        countLine++;
        if (countLine > maxInputSentences || checkedSentences > maxCheckedSentences) {
          break;
        }
        List<String> tokens = language.getWordTokenizer().tokenize(line);
        int pos = 0;
        for (String token: tokens) {
          if (StringTools.hasDiacritics(token)) {
            words[1] = token;
            words[0] = StringTools.removeDiacritics(token);
            analyzeSentence(line, 1, pos, config);
          }
          pos += token.length();
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
          / (float) (results[i][classifyTypes.indexOf("TP")] + results[i][classifyTypes.indexOf("FN")] 
              + results[i][classifyTypes.indexOf("TPns")] + results[i][classifyTypes.indexOf("TPws")]);
      // recall including empty suggestions
      float recall2 = (results[i][classifyTypes.indexOf("TP")] + results[i][classifyTypes.indexOf("TPns")])
          / (float) (results[i][classifyTypes.indexOf("TP")] + results[i][classifyTypes.indexOf("FN")]
              + results[i][classifyTypes.indexOf("TPns")] + results[i][classifyTypes.indexOf("TPws")]);
      //float expectedSuggestionPercentage = (float) results[i][classifyTypes.indexOf("TPs")]
      //    / results[i][classifyTypes.indexOf("TP")];
      int errorsTotal = results[i][classifyTypes.indexOf("TP")] + results[i][classifyTypes.indexOf("FP")]
          + results[i][classifyTypes.indexOf("TN")] + results[i][classifyTypes.indexOf("FN")] + results[i][classifyTypes.indexOf("TPns")]
          + results[i][classifyTypes.indexOf("TPws")];
      StringWriter resultsString = new StringWriter();

      resultsString.append("-------------------------------------\n");
      resultsString.append("Results for " + fakeRuleIDs[i] + "\n");
      
      int nCorrectSentences =  results[i][1] + results[i][2] ; // FP + TN
      int nIncorrectSentences =  results[i][0] + results[i][4] + results[i][5] + results[i][3]; // TP + TPns + TPws + FN  
      
      resultsString.append("Total sentences: " + String.valueOf(errorsTotal) + "\n");
      resultsString.append(formattedAbsoluteAndPercentage("\nCorrect sentences", nCorrectSentences, nCorrectSentences + nIncorrectSentences));
      resultsString.append(formattedAbsoluteAndPercentage("FP", results[i][1], nCorrectSentences));
      resultsString.append(formattedAbsoluteAndPercentage("TN", results[i][2], nCorrectSentences));
      
      resultsString.append(formattedAbsoluteAndPercentage("\nIncorrect sentences", nIncorrectSentences, nCorrectSentences + nIncorrectSentences));
      resultsString.append(formattedAbsoluteAndPercentage("TP (total)", results[i][4] + results[i][5] + results[i][0], nIncorrectSentences));
      resultsString.append(formattedAbsoluteAndPercentage(" TP (expected suggestion)", results[i][0], nIncorrectSentences));
      resultsString.append(formattedAbsoluteAndPercentage(" TPns (no suggestion)", results[i][4], nIncorrectSentences));
      resultsString.append(formattedAbsoluteAndPercentage(" TPws (wrong suggestion)", results[i][5], nIncorrectSentences));
      resultsString.append(formattedAbsoluteAndPercentage("FN", results[i][3], nIncorrectSentences));

      resultsString.append("\nPrecision: " + String.format(Locale.ROOT, "%.4f", precision) + "\n");
      resultsString.append("Recall: " + String.format(Locale.ROOT, "%.4f", recall) + "\n");
      resultsString.append("Recall (including empty suggestions): " + String.format(Locale.ROOT, "%.4f", recall2) + "\n");
      
      if (ignoredLines > 0) {
        resultsString.append("\nIgnored lines from source: " + ignoredLines + "\n");
      }
      
      resultsString.append(printTimeFromStart(start, ""));
      resultsString.append("\n" + printCurrentDateTime() + "\n");
      appendToFile(verboseOutputFilename, resultsString.toString());
      
      if (printSummaryDetails) {
          appendToFile(summaryOutputFilename, errorCategory + "\t" + fakeRuleIDs[i]
                  + "\t" + errorsTotal + "\t" + String.format(Locale.ROOT, "%.4f", precision) + "\t" + String.format(Locale.ROOT, "%.4f", recall) + "\t"
                  + results[i][classifyTypes.indexOf("TP")] + "\t"
                  + results[i][classifyTypes.indexOf("FP")] + "\t"
                  + results[i][classifyTypes.indexOf("TN")] + "\t"
                  + results[i][classifyTypes.indexOf("FN")] + "\t"
                  + results[i][classifyTypes.indexOf("TPns")] + "\t"
                  + results[i][classifyTypes.indexOf("TPws")] + "\t");
      }
      
      accumulateResults[0] += errorsTotal;
      accumulateResults[1] += results[i][classifyTypes.indexOf("TP")];
      accumulateResults[2] += results[i][classifyTypes.indexOf("FP")];
      accumulateResults[3] += results[i][classifyTypes.indexOf("TN")];
      accumulateResults[4] += results[i][classifyTypes.indexOf("FN")];
      
    }
    System.out.println(printTimeFromStart(start, ""));
    System.out.println("-------------------------------------");
  }
  
  private static String formattedAbsoluteAndPercentage (String tag, int i, int j) {
    float percentage = (float) i*100/j;
    StringWriter r = new StringWriter();
    r.append(tag+": ");
    r.append(Integer.toString(i));
    r.append(" (");
    r.append(String.format(Locale.ROOT, "%.2f", percentage));
    r.append("%)\n");
    return r.toString();
  }
  
  private static String printTimeFromStart(long start, String tag) {
    if (tag.isEmpty()) {
      tag = "Time:";
    }
    long totalSecs = (long) ((System.currentTimeMillis() - start) / 1000.0);
    long hours = totalSecs / 3600;
    int minutes = (int) ((totalSecs % 3600) / 60);
    int seconds = (int) (totalSecs % 60);
    return String.format(tag+" %02d:%02d:%02d\n", hours, minutes, seconds);
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
        try {
          matchesCorrect = lt.check(correctSentence, config).getMatches();
        } catch (RuntimeException e) {
          e.printStackTrace();
          wait(1000);
          matchesCorrect = lt.check(correctSentence, config).getMatches();
        }
        checkedSentences++;
        cachedMatches.put(correctSentence, matchesCorrect);
      }
      String replaceWith = words[1 - j];
      String originalString = correctSentence.substring(fromPos, fromPos + words[j].length());
      //capitalization change only makes sense with full words
      if (wholeword && StringTools.isCapitalizedWord(originalString) && replaceWith != null) {
        replaceWith = StringTools.uppercaseFirstChar(replaceWith);
      }
      List<String> ruleIDs = ruleIDsAtPos(matchesCorrect, fromPos, replaceWith);
      if (ruleIDs.size() > 0) {
        results[j][classifyTypes.indexOf("FP")]++;
        printSentenceOutput("FP", correctSentence, j, String.join(",", ruleIDs));
      } else {
        results[j][classifyTypes.indexOf("TN")]++;
        // Too verbose...
        // printSentenceOutput("TN", correctSentence, fakeRuleIDs[j]);
      }
    }
    // Wrong sentence
    if ( (!unidirectional || j == 1) && words[1 - j] != null) {
      String replaceWith = words[1 - j];
      String originalString = correctSentence.substring(fromPos, fromPos + words[j].length());
      // capitalization change only makes sense with full words
      if (wholeword) {
        replaceWith = StringTools.preserveCase(replaceWith, originalString);  
      }
      String wrongSentence = correctSentence.substring(0, fromPos) + replaceWith
          + correctSentence.substring(fromPos + words[j].length(), correctSentence.length());
      if (wrongSentence.equals(correctSentence)) {
        // Should not happen
        printSentenceOutput("Error: word cannot be replaced", correctSentence, j, "");
        return;
      }    
      List<RemoteRuleMatch> matchesWrong;
      if (cachedMatches.containsKey(wrongSentence)) {
        matchesWrong = cachedMatches.get(wrongSentence);
      } else {
        try {
          matchesWrong = lt.check(wrongSentence, config).getMatches();
        } catch (RuntimeException e) {
          e.printStackTrace();
          wait(1000);
          matchesWrong = lt.check(wrongSentence, config).getMatches();
        }
        checkedSentences++;
        cachedMatches.put(wrongSentence, matchesWrong);
      }
      
      List<String> ruleIDs = ruleIDsAtPos(matchesWrong, fromPos, originalString);
      if (ruleIDs.size() > 0) {
        if (isExpectedSuggestionAtPos(matchesWrong, fromPos, originalString, wrongSentence, correctSentence)) {
          results[1 - j][classifyTypes.indexOf("TP")]++;
          printSentenceOutput("TP", wrongSentence, 1 - j, String.join(",", ruleIDs));
        } else if (isEmptySuggestionAtPos(matchesWrong, fromPos, originalString, wrongSentence, correctSentence)) {
          results[1 - j][classifyTypes.indexOf("TPns")]++;
          printSentenceOutput("TPns", wrongSentence, 1 - j, String.join(",", ruleIDs));
        } else {
          results[1 - j][classifyTypes.indexOf("TPws")]++;
          printSentenceOutput("TPws", wrongSentence, 1 - j, String.join(",", ruleIDs));
        }
      } else {
        results[1 - j][classifyTypes.indexOf("FN")]++;
        printSentenceOutput("FN", wrongSentence, 1 - j, "");
      }
    }
  }

  private static void printSentenceOutput(String classification, String sentence, int i, String ruleIds) throws IOException { 
    if (verboseOutput) {
      String fakeRuleID = "";
      if (fakeRuleIDs[i].contains("null")) {
        fakeRuleID = "rules_" + words[i] + "->" + words[1 - i]; 
      } else {
        fakeRuleID = fakeRuleIDs[i];
      }
      if (verboseOutputFilename.isEmpty()) {
        System.out.println(countLine + ". " + classification + ": " + sentence + " –– " + fakeRuleID + ":" + ruleIds);
      } else {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(verboseOutputFilename, true))) {
          out.write(countLine + "\t" + classification + "\t" + sentence + "\t" + fakeRuleID + ":" + ruleIds+"\n");
        }  
      }
    }
  }

  private static List<String> ruleIDsAtPos(List<RemoteRuleMatch> matchesCorrect, int pos, String expectedSuggestion) {
    List<String> ruleIDs = new ArrayList<>();
    for (RemoteRuleMatch match : matchesCorrect) {
      if (match.getErrorOffset() <= pos && match.getErrorOffset() + match.getErrorLength() >= pos) {
        if (disabledRules.contains(match.getRuleId())) {
          continue;
        }
        if (!onlyRules.isEmpty() && !onlyRules.contains(match.getRuleId())) {
          continue;
        }
        String subId = null;
        try {
          subId = match.getRuleSubId().get();
        } catch (NoSuchElementException e) {
          //System.out.println("Exception, skipping '" + countLine + "': ");
          //e.printStackTrace();
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
  
  private static boolean isEmptySuggestionAtPos(List<RemoteRuleMatch> matchesCorrect, int pos,
      String expectedSuggestion, String wrongSentence, String correctSentence) {
    for (RemoteRuleMatch match : matchesCorrect) {
      if (match.getReplacements().get().size() == 0) {
        if (match.getErrorOffset() <= pos && match.getErrorOffset() + match.getErrorLength() >= pos) {
          return true;
        }
      }
    }
    return false;
  }
  
  private static void writeHelp() {
    System.out.println("Usage 1: " + ArtificialErrorEval.class.getSimpleName()
        + " <language code> <input file>");
    System.out.println("Usage 2: " + ArtificialErrorEval.class.getSimpleName()
        + " <configuration file>");
  }
  
  private static List<String> differences(String s1, String s2) {
    List<String> results = new ArrayList<>();
    if (s1.equals(s2)) {
      results.add(s1);
      results.add("");
      results.add("");
      results.add("");
      return results;
    }
    int l1 = s1.length();
    int l2 = s2.length();
    int fromStart = 0;
    while (fromStart < l1 && fromStart < l2 && s1.charAt(fromStart) == s2.charAt(fromStart)) {
      fromStart++;
    }
    int fromEnd = 0;
    while (fromEnd < l1 && fromEnd < l2 && s1.charAt(l1 - 1 - fromEnd) == s2.charAt(l2 - 1 - fromEnd)) {
      fromEnd++;
    }
    // corrections (e.g. stress vs stresses)
    while (fromStart > l1 - fromEnd) {
      fromEnd--;
    }
    while (fromStart > l2 - fromEnd) {
      fromEnd--;
    }
    // common string at start
    results.add(s1.substring(0, fromStart));
    // diff in sentence 1
    results.add(s1.substring(fromStart, l1 - fromEnd));
    // diff in sentence 2
    results.add(s2.substring(fromStart, l2 - fromEnd));
    // common string at end
    results.add(s1.substring(l1 - fromEnd, l1));
    return results;
    
  }
  
  public static void wait(int ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }
  }

  private static String printCurrentDateTime() {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    LocalDateTime now = LocalDateTime.now();
    return dtf.format(now);
  }
}
