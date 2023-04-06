package org.languagetool.remote;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.tools.ContextTools;
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
    int checkedSentences = 0;
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
    for (String line : lines) {
      String sentence = line;
      checkedSentences++;
      if (checkedSentences < startLine) {
        continue;
      }
      boolean done = false;
      int fpNumber = 0;
      while (!done) {
        List<RemoteRuleMatch> matches = getMatches(cfg, sentence);
        RemoteRuleMatch match = null;
        if (matches.size() > fpNumber) {
          match = matches.get(fpNumber);
        }
        System.out.println("=============================================");
        System.out.println("Sentence no. " + String.valueOf(checkedSentences));
        System.out.println("---------------------------------------------");

        System.out.println(formatedSentence(sentence, match));
        System.out.println("---------------------------------------------");
        System.out.println(listSuggestions(match));

        System.out.println("---------------------------------------------");
        System.out.print("Action? ");
        response = sc.nextLine();
        if (!response.contains(">>")) {
          response = response.toLowerCase();
        }
        switch (response) {
        case "d":
          done = true;
          break;
        case "g":
          done = true;
          break;
        case "f":
          fpNumber++;
          break;
        case "1":
        case "2":
        case "3":
        case "4":
        case "5":
          int r = Integer.valueOf(response);
          if (match != null && r >= 1 && r <= 5) {
            sentence = replaceSuggestion(sentence, match, r);
          }
          break;
        }
        if (response.startsWith(">>")) { // alternative suggestion
          sentence = sentence.substring(0, match.getErrorOffset()) + response.substring(2)
              + sentence.substring(match.getErrorOffset() + match.getErrorLength());
        } else if (response.contains(">>")) {
          String[] parts = response.split(">>");
          String toReplace = parts[0];
          String replacement = parts[1];
          int ind = sentence.indexOf(toReplace);
          if (ind > -1) {
            if (sentence.substring(ind + toReplace.length()).indexOf(toReplace) > -1) {
              System.out.println("Cannot replace duplicate string in sentence.");
            } else {
              sentence = sentence.substring(0, ind) + replacement + sentence.substring(ind + toReplace.length());
              System.out.println("FN: replacement done.");
            }
          }
        }
      }
    }
    sc.close();
  }

  static private String listSuggestions(RemoteRuleMatch match) {
    StringBuilder sb = new StringBuilder();
    sb.append("(D)one (G)arbled ");
    if (match == null) {
      sb.append("NO MATCHES");
      return sb.toString();
    }
    sb.append("(F)P ");
    if (match.getReplacements().get().size() > 0) {
      sb.append("SUGGESTIONS: ");
    } else {
      sb.append("NO SUGGESTIONS ");
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

    void prepareConfiguration() throws IOException {
      if (!userName.isEmpty() && !apiKey.isEmpty()) {
        ltConfig = new CheckConfigurationBuilder(languageCode).disabledRuleIds("WHITESPACE_RULE").textSessionID("-2")
            .username(userName).apiKey(apiKey).build();
      } else {
        ltConfig = new CheckConfigurationBuilder(languageCode).disabledRuleIds("WHITESPACE_RULE").textSessionID("-2")
            .build();
      }
      inputFile = new File(inputFilePath);
      if (!inputFile.exists() || inputFile.isDirectory()) {
        throw new IOException("File not found: " + inputFile);
      }
      String fileName = inputFile.getName();
      // System.out.println("Analyzing file: " + fileName);
      fileName = fileName.substring(0, fileName.lastIndexOf('.'));
      if (outputFilePath.isEmpty()) {
        outputFile = new File(inputFile.getParentFile() + "/" + fileName + "-annotations.txt");
      } else {
        outputFile = new File(outputFilePath);
      }
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
