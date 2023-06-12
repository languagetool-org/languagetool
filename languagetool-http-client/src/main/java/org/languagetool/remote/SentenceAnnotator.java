package org.languagetool.remote;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Scanner;

import org.languagetool.tools.Tools;

public class SentenceAnnotator {

  static HashMap<String, List<RemoteRuleMatch>> cachedMatches;

  public static void main(String[] args) throws IOException {
    long start = System.currentTimeMillis();
    // use configuration file
    if (args.length == 1) {
      String configurationFilename = args[0];
      Properties prop = new Properties();
      FileInputStream fis = new FileInputStream(configurationFilename);
      prop.load(new InputStreamReader(fis, Charset.forName("UTF-8")));
      AnnotatorConfig cfg = new AnnotatorConfig();
      cfg.remoteServer = prop.getProperty("remoteServer", "http://localhost:8081").trim();
      cfg.userName = prop.getProperty("userName", "").trim();
      cfg.apiKey = prop.getProperty("apiKey", "").trim();
      cfg.inputFilePath = prop.getProperty("inputFile", "").trim();
      cfg.outputFilePath = prop.getProperty("outputFile", "").trim();
      cfg.languageCode = prop.getProperty("languageCode").trim();
      String enabledOnlyRulesStr = prop.getProperty("enabledOnlyRules", "").trim();
      if (!enabledOnlyRulesStr.isEmpty()) {
        cfg.enabledOnlyRules = Arrays.asList(enabledOnlyRulesStr.split(","));
      }
      String disabledRulesStr = prop.getProperty("disabledRules", "").trim();
      if (!disabledRulesStr.isEmpty()) {
        cfg.disabledRules = Arrays.asList(disabledRulesStr.split(","));
      }
      // defaultColor="\u001B[0m"
      // highlightColor="\u001B[97m"
      cfg.ansiDefault = prop.getProperty("defaultColor", "").trim().replaceAll("\"", "");
      cfg.ansiHighlight = prop.getProperty("highlightColor", "").trim().replaceAll("\"", "");
      cfg.prepareConfiguration();
      runAnnotation(cfg);
    } else {
      writeHelp();
      System.exit(1);
    }
    System.out.println(printTimeFromStart(start, "Total time:"));
  }

  private static void runAnnotation(AnnotatorConfig cfg) throws IOException {

    List<String> lines = Files.readAllLines(Paths.get(cfg.inputFilePath));
    int numSentence = 0;
    Scanner sc = new Scanner(System.in);
    System.out.print("Start at line? ");
    String response = sc.nextLine();
    int startLine = 0;
    try {
      startLine = Integer.valueOf(response);
    } catch (NumberFormatException ex) {
      startLine = 0;
    }
    System.out.println("Starting at line " + String.valueOf(startLine) + " of file " + cfg.inputFilePath);
    boolean quit = false;
    for (String line : lines) {
      if (quit) {
        break;
      }
      String sentence = line;
      numSentence++;
      if (numSentence < startLine) {
        continue;
      }
      boolean done = false;
      List<String> fpMatches = new ArrayList<>();
      int annotationsPerSentence = 0;
      while (!done) {
        List<RemoteRuleMatch> matches = getMatches(cfg, sentence);
        RemoteRuleMatch match = null;
        int i = 0;
        boolean isValidMatch = false;
        while (!isValidMatch && i < matches.size()) {
          match = matches.get(i);
          i++;
          isValidMatch = !fpMatches.contains(getMatchIdentifier(sentence, match));
          if (!isValidMatch) {
            match = null;
          }
        }
        String formattedSentence = formatedSentence(sentence, match);
        String formattedCorrectedSentence = formattedSentence;
        String detectedErrorStr = "";
        System.out.println(cfg.ansiDefault + "=============================================");
        System.out.println("Sentence no. " + String.valueOf(numSentence));
        System.out.println("---------------------------------------------");
        System.out.println(cfg.ansiHighlight + formattedSentence + cfg.ansiDefault);
        System.out.println("---------------------------------------------");
        if (match != null) {
          System.out.println(match.getMessage());
          detectedErrorStr = sentence.substring(match.getErrorOffset(),
              match.getErrorOffset() + match.getErrorLength());
        }
        System.out.println(listSuggestions(match));
        System.out.println("---------------------------------------------");
        System.out.print("Action? ");
        response = sc.nextLine();
        if (!response.contains(">>")) {
          response = response.toLowerCase();
        }
        String errorType = "";
        int suggestionPos = -1;
        String suggestionApplied = "";
        int suggestionsTotal = 0;
        if (match != null) {
          suggestionsTotal = match.getReplacements().get().size();
        }
        switch (response) {
        case "r":
          sentence = line;
          cfg.outStrB = new StringBuilder();
          break;
        case "q":
          done = true;
          quit = true;
          writeToOutputFile(cfg);
          break;
        case "d":
          done = true;
          if (annotationsPerSentence == 0) {
            errorType = "OK";
          }
          writeToOutputFile(cfg);
          break;
        case "g":
          done = true;
          errorType = "IG";
          cfg.outStrB = new StringBuilder();
          match = null;
          break;
        case "i":
          fpMatches.add(getMatchIdentifier(sentence, match));
          errorType = "IM";
          break;
        case "b":
          fpMatches.add(getMatchIdentifier(sentence, match));
          errorType = "BO";
          break;
        case "f":
          fpMatches.add(getMatchIdentifier(sentence, match));
          errorType = "FP";
          break;
        case "1":
        case "2":
        case "3":
        case "4":
        case "5":
          errorType = "TP";
          if (suggestionsTotal > 1) {
            errorType = "TPmultiple";
          }
          int r = Integer.valueOf(response);
          if (match != null && r >= 1 && r <= 5) {
            formattedCorrectedSentence = formattedCorrectedSentence(sentence, match, r);
            sentence = replaceSuggestion(sentence, match, r);
            suggestionPos = r;
            suggestionApplied = match.getReplacements().get().get(suggestionPos - 1);
          }
          break;
        }
        if (quit) {
          break;
        }
        if (response.startsWith(">>") && match != null) { // alternative suggestion
          formattedCorrectedSentence = sentence.substring(0, match.getErrorOffset()) + "___" + response.substring(2)
              + "___" + sentence.substring(match.getErrorOffset() + match.getErrorLength());
          sentence = sentence.substring(0, match.getErrorOffset()) + response.substring(2)
              + sentence.substring(match.getErrorOffset() + match.getErrorLength());
          if (suggestionsTotal == 0) {
            errorType = "TPno";
          } else {
            errorType = "TPwrong";
          }
          suggestionApplied = response.substring(2);
        } else if (response.contains(">>")) {
          String[] parts = response.split(">>");
          String toReplace = parts[0];
          String replacement = parts[1];
          int ind = sentence.indexOf(toReplace);
          if (ind > -1) {
            if (sentence.substring(ind + toReplace.length()).indexOf(toReplace) > -1) {
              System.out.println("Cannot replace duplicate string in sentence.");
            } else {
              formattedSentence = sentence.substring(0, ind) + "___" + toReplace + "___"
                  + sentence.substring(ind + toReplace.length());
              formattedCorrectedSentence = sentence.substring(0, ind) + "___" + replacement + "___"
                  + sentence.substring(ind + toReplace.length());
              sentence = sentence.substring(0, ind) + replacement + sentence.substring(ind + toReplace.length());
              System.out.println("FN: replacement done.");
              errorType = "FN";
              suggestionApplied = replacement;
              detectedErrorStr = toReplace;
            }
          }
        }

        if (!errorType.isEmpty()) {
          printOutputLine(cfg, numSentence, formattedSentence, formattedCorrectedSentence, errorType, detectedErrorStr,
              suggestionApplied, suggestionPos, suggestionsTotal, getFullId(match), getRuleCategoryId(match),
              getRuleType(match));
          annotationsPerSentence++;
          if (errorType.equals("OK") || errorType.equals("IG")) {
            writeToOutputFile(cfg);
            cfg.outStrB = new StringBuilder();
          }
        }
      }
    }
    sc.close();
    cfg.out.close();
  }

  static private void printOutputLine(AnnotatorConfig cfg, int numSentence, String errorSentence,
      String correctedSentence, String errorType, String detectedErrorStr, String suggestion, int suggestionPos,
      int suggestionsTotal, String ruleId, String ruleCategory, String ruleType) {
    cfg.outStrB.append(String.valueOf(numSentence) + "\t" + errorSentence + "\t" + correctedSentence + "\t" + errorType
        + "\t" + detectedErrorStr + "\t" + suggestion + "\t" + ruleId + "\t" + String.valueOf(suggestionPos) + "\t"
        + String.valueOf(suggestionsTotal) + "\t" + ruleCategory + "\t" + ruleType + "\n");
  }

  static private void writeToOutputFile(AnnotatorConfig cfg) throws IOException {
    cfg.out.write(cfg.outStrB.toString());
    cfg.out.flush();
    cfg.outStrB = new StringBuilder();
  }

  static private String getMatchIdentifier(String sentence, RemoteRuleMatch match) {
    StringBuilder sb = new StringBuilder();
    sb.append(sentence.substring(match.getErrorOffset(), match.getErrorOffset() + match.getErrorLength()));
    sb.append(getFullId(match));
    sb.append(match.getReplacements().get().toString());
    return sb.toString();
  }

  static private String getFullId(RemoteRuleMatch match) {
    String ruleId = "";
    if (match != null) {
      String subId = null;
      try {
        subId = match.getRuleSubId().get();
      } catch (NoSuchElementException e) {
      }
      if (subId != null) {
        ruleId = match.getRuleId() + "[" + subId + "]";
      } else {
        ruleId = match.getRuleId();
      }
    }
    return ruleId;
  }

  static private String getRuleCategoryId(RemoteRuleMatch match) {
    String categoryId = "";
    if (match != null) {
      try {
        categoryId = match.getCategoryId().get();
      } catch (NoSuchElementException e) {
      }
    }
    return categoryId;
  }

  static private String getRuleType(RemoteRuleMatch match) {
    String ruleType = "";
    if (match != null) {
      try {
        ruleType = match.getLocQualityIssueType().get().toString();
      } catch (NoSuchElementException e) {
      }
    }
    return ruleType;
  }

  static private String listSuggestions(RemoteRuleMatch match) {
    StringBuilder sb = new StringBuilder();
    sb.append("(Q)uit (D)one (G)arbled (R)estartSentence ");
    if (match == null) {
      sb.append("NO MATCHES");
      return sb.toString();
    }
    sb.append("(I)gnoreMatch ");
    sb.append("(B)othOK ");
    sb.append("(F)P ");
    if (match.getReplacements().get().size() > 0) {
      sb.append("\nSUGGESTIONS: ");
    }
    int i = 1;
    for (String suggestion : match.getReplacements().get()) {
      sb.append(String.valueOf(i) + ") " + suggestion + " ");
      i++;
      if (i > 5) {
        break;
      }
    }
    return sb.toString();
  }

  static private String formatedSentence(String line, RemoteRuleMatch match) {
    if (match == null) {
      return line;
    }
    StringBuilder sb = new StringBuilder();
    sb.append(line.substring(0, match.getErrorOffset()));
    sb.append("___");
    sb.append(line.substring(match.getErrorOffset(), match.getErrorOffset() + match.getErrorLength()));
    sb.append("___");
    sb.append(line.substring(match.getErrorOffset() + match.getErrorLength()));
    return sb.toString();
  }

  static private String formattedCorrectedSentence(String line, RemoteRuleMatch match, int i) {
    if (match == null) {
      return line;
    }
    StringBuilder sb = new StringBuilder();
    sb.append(line.substring(0, match.getErrorOffset()));
    sb.append("___");
    sb.append(match.getReplacements().get().get(i - 1));
    sb.append("___");
    sb.append(line.substring(match.getErrorOffset() + match.getErrorLength()));
    return sb.toString();
  }

  static private String replaceSuggestion(String line, RemoteRuleMatch match, int i) {
    StringBuilder sb = new StringBuilder();
    sb.append(line.substring(0, match.getErrorOffset()));
    sb.append(match.getReplacements().get().get(i - 1));
    sb.append(line.substring(match.getErrorOffset() + match.getErrorLength()));
    return sb.toString();
  }

  private static List<RemoteRuleMatch> getMatches(AnnotatorConfig cfg, String sentence) {
    List<RemoteRuleMatch> matches;
    if (cachedMatches.containsKey(sentence)) {
      matches = cachedMatches.get(sentence);
    } else {
      try {
        matches = cfg.lt.check(sentence, cfg.ltConfig).getMatches();
      } catch (RuntimeException e) {
        e.printStackTrace();
        wait(1000);
        matches = cfg.lt.check(sentence, cfg.ltConfig).getMatches();
      }
      cachedMatches.put(sentence, matches);
    }
    return matches;
  }

  static private class AnnotatorConfig {
    String remoteServer;
    String userName;
    String apiKey;
    String inputFilePath;
    String outputFilePath;
    String languageCode;
    File inputFile;
    File outputFile;
    CheckConfiguration ltConfig;
    RemoteLanguageTool lt;
    FileWriter out;
    StringBuilder outStrB;
    String ansiDefault = "";
    String ansiHighlight = "";
    List<String> enabledOnlyRules = new ArrayList<String>();
    List<String> disabledRules = new ArrayList<String>();

    void prepareConfiguration() throws IOException {
      CheckConfigurationBuilder cfgBuilder = new CheckConfigurationBuilder(languageCode);
      //cfgBuilder.textSessionID("-2");
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
      ltConfig = cfgBuilder.build();
      inputFile = new File(inputFilePath);
      if (!inputFile.exists() || inputFile.isDirectory()) {
        throw new IOException("File not found: " + inputFile);
      }
      String fileName = inputFile.getName();
      // System.out.println("Analyzing file: " + fileName);
      fileName = fileName.substring(0, fileName.lastIndexOf('.'));
      if (outputFilePath.isEmpty()) {
        outputFile = new File(inputFile.getParentFile() + "/" + fileName + "-annotations.tsv");
      } else {
        outputFile = new File(outputFilePath);
      }
      outStrB = new StringBuilder();
      out = new FileWriter(outputFile, true);
      cachedMatches = new HashMap<>();
      lt = new RemoteLanguageTool(Tools.getUrl(remoteServer));
    }

  }

  private static String printTimeFromStart(long start, String tag) {
    if (tag.isEmpty()) {
      tag = "Time:";
    }
    long totalSecs = (long) ((System.currentTimeMillis() - start) / 1000.0);
    long hours = totalSecs / 3600;
    int minutes = (int) ((totalSecs % 3600) / 60);
    int seconds = (int) (totalSecs % 60);
    return String.format(tag + " %02d:%02d:%02d\n", hours, minutes, seconds);
  }

  private static void writeHelp() {
    System.out.println("Usage: " + SentenceAnnotator.class.getSimpleName() + " <configuration file>");
  }

  public static void wait(int ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }
  }

}
