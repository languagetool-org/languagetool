/*
 * LanguageTool, a natural language style checker
 * Copyright (c) 2022.  Stefan Viol (https://stevio.de)
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  USA
 */

package org.languagetool.remote.multiLang;

import org.languagetool.remote.*;
import org.languagetool.tools.Tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class MultiLanguageTextCheckEval {
  private static Random randomGen = new Random();
  private static int minMainLanguageSentences = 100;
  private static int maxOtherLanguageSentences = 15;
  private static int maxOtherLanguageSentencesAtOnce = 4;
  private static String mainLanguages = "de";
  private static String otherLanguage = "en";
  private static boolean useLangDetectionService = false;
  private static int rounds = 100;
  private static List<DetectionResults> roundResults = new ArrayList<>();
  private static boolean spamToMe = false;
  private static Set<String> allWrongRanges = new HashSet<>();
  private static Set<String> allNotDetected = new HashSet<>();

  private static boolean vsMode = false;

  public static void main(String[] args) {
    String inputFolder = "/home/stefan/Dokumente/Test_texte/multiLangChecker/creatorMode/";
    Map<String, List<String>> corporaFiles = new HashMap<>();
    try (Stream<Path> path = Files.walk(Paths.get(inputFolder))) {
      path.filter(Files::isRegularFile)
              .forEach(pathToFile -> {
                try {
                  List<String> lines = Files.readAllLines(pathToFile);
                  String language = pathToFile.getFileName().toString().split("\\.")[0];
                  corporaFiles.put(language, lines);
                } catch (IOException e) {
                  throw new RuntimeException(e);
                }
              });
    } catch (IOException ex) {
      System.exit(1);
    }
    if (vsMode) {
      List<MultiLangCorpora> savedCorpora = new ArrayList<>();
      for (int i = 0; i < rounds; i++) {
        MultiLangCorpora corporaFromFiles = createCorporaFromFiles(mainLanguages, corporaFiles.get(mainLanguages), otherLanguage, corporaFiles.get(otherLanguage));
        savedCorpora.add(corporaFromFiles);
        DetectionResults detectionResults = runTest(corporaFromFiles, "http://localhost:8081");
        if (detectionResults != null) {
          roundResults.add(detectionResults);
          System.out.print(".");
        } else {
          System.out.print("x");
        }
        if (rounds > 1 && (i + 1) % (rounds / 10) == 0) {
          System.out.println(" " + (i + 1) + " finished");
        }
      }
      printSummary(" with LanguageDetection ");
      allNotDetected.clear();
      allWrongRanges.clear();
      roundResults.clear();
      for (int i = 0; i < rounds; i++) {
        DetectionResults detectionResults = runTest(savedCorpora.get(i), "http://localhost:8082");
        if (detectionResults != null) {
          roundResults.add(detectionResults);
          System.out.print(".");
        } else {
          System.out.print("x");
        }
        if (rounds > 1 && (i + 1) % (rounds / 10) == 0) {
          System.out.println(" " + (i + 1) + " finished");
        }
      }
      printSummary(" without LanguageDetection");
    } else {
      for (int i = 0; i < rounds; i++) {
        MultiLangCorpora corporaFromFiles = createCorporaFromFiles(mainLanguages, corporaFiles.get(mainLanguages), otherLanguage, corporaFiles.get(otherLanguage));
        DetectionResults detectionResults = runTest(corporaFromFiles);

        if (detectionResults != null) {
          roundResults.add(detectionResults);
          System.out.print(".");
        } else {
          System.out.print("x");
        }
        if (rounds > 1 && (i + 1) % (rounds / 10) == 0) {
          System.out.println(" " + (i + 1) + " finished");
        }
      }
      printSummary("normal");
    }
    //checker.createCorporaFromFiles(inputFolder);
  }

  private static void printSummary(String mode) {
    System.out.println(rounds + " of " + mode + " finished");
    System.out.println("Avg. time to check: " + String.format("%.2f", roundResults.stream().mapToDouble(detectionResults -> detectionResults.time).average().getAsDouble()) + " seconds");
    System.out.println("Avg. time to check (no MultiLanguage): " + String.format("%.2f", roundResults.stream().mapToDouble(detectionResults -> detectionResults.timeWithout).average().getAsDouble()) + " seconds");
    System.out.println("Avg. timediff: " + String.format("%.2f", roundResults.stream().mapToDouble(DetectionResults::getTimeDiff).average().getAsDouble()) + " seconds");
    System.out.println("Avg. chars in corpora: " + String.format("%.2f", roundResults.stream().mapToDouble(detectionResults -> detectionResults.charsInText).average().getAsDouble()));
    System.out.println("Avg. detection rate: " + String.format("%.2f", roundResults.stream().mapToDouble(detectionResults -> detectionResults.detectionRate).average().getAsDouble()) + " %");
    System.out.println("Avg. sentences in corpora: " + String.format("%.2f", roundResults.stream().mapToDouble(detectionResults -> detectionResults.sentencesInText).average().getAsDouble()));
    System.out.println("Avg. injected sentences in corpora: " + String.format("%.2f", roundResults.stream().mapToDouble(detectionResults -> detectionResults.injectedSentecesInText).average().getAsDouble()));
    System.out.println("Avg. wrong detected: " + String.format("%.2f", roundResults.stream().mapToDouble(detectionResults -> detectionResults.wrongDetected).average().getAsDouble()) + " sentences per corpora");
    if (rounds <= 10) {
      System.out.println("### Not detected sentences:");
      allNotDetected.forEach(System.out::println);
      System.out.println("### Wrong detected ranges:");
      allWrongRanges.forEach(System.out::println);
    }
  }

  private static MultiLangCorpora createCorporaFromFiles(String mainLanguage, List<String> mainLanguageLines, String otherLanguage, List<String> otherLanguageLines) {
    if (spamToMe) {
      System.out.printf("Created mixed %s corpora with %s.%n", mainLanguage, otherLanguage);
    }
    int sentencesInMainLanguageCorpus = mainLanguageLines.size();
    if (sentencesInMainLanguageCorpus < minMainLanguageSentences) {
      System.err.printf("The corpora for %s has not enough lines.%n", mainLanguage);
      System.exit(1);
    }
    MultiLangCorpora mlc = new MultiLangCorpora(mainLanguage);
    int sentencesInNewCorpusCount = 0;
    int otherLanguageSentencesInjected = 0;
    String lastInjected = null;
    while (sentencesInNewCorpusCount < minMainLanguageSentences + maxOtherLanguageSentences) {
      int whichShouldITake = randomGen.nextInt(minMainLanguageSentences + maxOtherLanguageSentences);
      boolean addOther = whichShouldITake < maxOtherLanguageSentences && otherLanguageSentencesInjected + maxOtherLanguageSentencesAtOnce <= maxOtherLanguageSentences;
      if (lastInjected == null || lastInjected.equals(otherLanguage) || !addOther) {
        int startLine = randomGen.nextInt(sentencesInMainLanguageCorpus);
        mlc.addSentence(mainLanguageLines.get(startLine).trim());
        sentencesInNewCorpusCount++;
        lastInjected = mainLanguages;
      } else {
        int startLine = randomGen.nextInt(otherLanguageLines.size() - maxOtherLanguageSentencesAtOnce);
        mlc.injectOtherSentence(otherLanguage, otherLanguageLines.get(startLine).trim());
        sentencesInNewCorpusCount++;
        otherLanguageSentencesInjected++;
        lastInjected = otherLanguage;
      }

    }
    return mlc;
  }

  static class DetectionResults {
    private float time;
    private float timeWithout;
    private float detectionRate;
    private int wrongDetected;
    private long charsInText;
    private int sentencesInText;
    private int injectedSentecesInText;

    DetectionResults(float time, float timeWithout, float detectionRate, int wrongDetected, long charsInText, int sentencesInText, int injectedSentecesInText) {
      this.time = time;
      this.timeWithout = timeWithout;
      this.detectionRate = detectionRate;
      this.wrongDetected = wrongDetected;

      this.charsInText = charsInText;
      this.sentencesInText = sentencesInText;
      this.injectedSentecesInText = injectedSentecesInText;
    }

    public float getTimeDiff() {
      return time - timeWithout;
    }

  }

  private static DetectionResults runTest(MultiLangCorpora mlc) {
    return runTest(mlc, "http://localhost:8081");
  }

  private static DetectionResults runTest(MultiLangCorpora mlc, String languageToolServer) {
    RemoteLanguageTool remoteLanguageTool = new RemoteLanguageTool(Tools.getUrl(languageToolServer));
    String language = useLangDetectionService ? "auto" : getSupportedLangCode(mlc.getLanguage());
    long startTime = System.currentTimeMillis();
    RemoteResult results = null;
    try {
      Map<String, String> params = new HashMap<>();
      params.put("enableMultiLanguageChecks", "true");
      params.put("preferredLanguages", "de,en");
      results = remoteLanguageTool.check(mlc.getText(), language, params);
    } catch (RuntimeException ex) {
      if (spamToMe) {
        System.out.println("too many errors");
      }
      return null;
    }
    long endTime = System.currentTimeMillis();
    float timeToCheck = (endTime - startTime) / 1000f;
    //2nd check without multilanguage
    long startTimeRound2 = System.currentTimeMillis();
    try {
      Map<String, String> params = new HashMap<>();
      params.put("enableMultiLanguageChecks", "false");
      params.put("preferredLanguages", "de");
      remoteLanguageTool.check(mlc.getText(), language, params);
    } catch (RuntimeException ex) {
      if (spamToMe) {
        System.out.println("too many errors");
      }
      return null;
    }
    long endTimeRound2 = System.currentTimeMillis();
    float timeToCheckRound2 = (endTimeRound2 - startTimeRound2) / 1000f;

    //detected sentences by lt
    List<String> detectedSentences = new ArrayList<>();
    for (RemoteIgnoreRange range : results.getIgnoreRanges()) {
      detectedSentences.add(mlc.getText().substring(range.getFrom(), range.getTo()).trim());
    }
    //injected lines by checker
    List<String> injectedLines = mlc.getInjectedSentences().stream().map(InjectedSentence::getText).collect(Collectors.toList());

    //later cleaned by not detectedLines
    List<String> detectedLines = new ArrayList<>(injectedLines);

    //assume everything is not detected
    List<String> notDetectedLines = new ArrayList<>(injectedLines);

    //assume everything is wrong detected
    List<String> wrongDetectedSentences = new ArrayList<>(detectedSentences);

    //filter all correct detected sentences from wrong detected sentences.
    List<String> notWrongDetected = new ArrayList<>();
    List<String> tmpRemoveFromWrongDetectedSentences = new ArrayList<>();
    for (String sentence : wrongDetectedSentences) {
      for (String injectedLine : injectedLines) {
        if (injectedLine.contains(sentence)) { //If a line in the corpora has more than one sentence, but the range is per sentence
          notWrongDetected.add(injectedLine);
          tmpRemoveFromWrongDetectedSentences.add(sentence);
        }
      }
    }
    wrongDetectedSentences.removeAll(tmpRemoveFromWrongDetectedSentences);
    notDetectedLines.removeAll(notWrongDetected);
    detectedLines.removeAll(notDetectedLines);

    allWrongRanges.addAll(wrongDetectedSentences);
    allNotDetected.addAll(notDetectedLines);

    if (rounds == 1) {
      System.out.println(mlc.getText());
    }
    return new DetectionResults(timeToCheck, timeToCheckRound2, ((float) detectedLines.size() / (float) injectedLines.size()) * 100, wrongDetectedSentences.size(), mlc.getText().length(), mlc.getSentencesInText(), mlc.getInjectedSentences().size());
  }


  private static String getSupportedLangCode(String shortCode) {
    switch (shortCode) {
      case "de":
        return "de-DE";
      case "en":
        return "en-US";
      case "pt":
        return "pt-BR";
      default:
        return shortCode;
    }
  }
}
