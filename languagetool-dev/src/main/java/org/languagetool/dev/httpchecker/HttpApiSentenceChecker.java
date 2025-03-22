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
package org.languagetool.dev.httpchecker;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;

/**
 * Checks sentences by posting them to the HTTP API from several threads,
 * merging the result. Can be used for nightly regression tests.
 */
class HttpApiSentenceChecker {

  private final String baseUrl;
  private final String langCode;
  private final int threadCount;
  private final String token;
  @Nullable
  private final String user;
  @Nullable
  private final String password;

  @Nullable
  private final String parameters;


  public HttpApiSentenceChecker(CommandLine cmd) {
    baseUrl = cmd.hasOption("url") ? cmd.getOptionValue("url") : "https://api.languagetool.org";
    if (baseUrl.endsWith("/")) {
      throw new IllegalArgumentException("Don't let baseUrl end with a '/': " + baseUrl + ". Correct example: 'https://api.languagetool.org'");
    }
    langCode = cmd.getOptionValue("lang");
    threadCount = Integer.parseInt(cmd.getOptionValue("threads"));
    token = cmd.hasOption("token") ? cmd.getOptionValue("token") : null;
    user = cmd.hasOption("user") ? cmd.getOptionValue("user") : null;
    password = cmd.hasOption("password") ? cmd.getOptionValue("password") : null;
    parameters = cmd.hasOption("parameters") ? cmd.getOptionValue("parameters") : null;
  }

  private void run(File input, File output) throws IOException, InterruptedException, ExecutionException {
    long t1 = System.currentTimeMillis();
    int lines = countLines(input);
    System.out.println(input + " has " + lines + " lines");
    List<String> inputTexts = splitInput(input, threadCount);
    List<File> threadFiles = runOnTexts(inputTexts);
    joinResults(threadFiles, output);
    long t2 = System.currentTimeMillis();
    Duration duration = Duration.of(t2 - t1, ChronoUnit.MILLIS);
    System.out.println("Runtime: " + formatDuration(duration) + " (h:mm:ss)");
  }

  private String formatDuration(Duration duration) {
    long seconds = duration.getSeconds();
    long absSeconds = Math.abs(seconds);
    return String.format("%d:%02d:%02d", absSeconds / 3600, (absSeconds % 3600) / 60, absSeconds % 60);
  }

  private int countLines(File input) throws FileNotFoundException {
    int count = 0;
    try (Scanner sc = new Scanner(input)) {
      while (sc.hasNextLine()) {
        sc.nextLine();
        count++;
      }
    }
    return count;
  }

  private List<String> splitInput(File input, int threadCount) throws IOException {
    List<String> texts = new ArrayList<>();
    final int batchSize = 10;  // do not modify - this would change the results
    System.out.println("Working with " + threadCount + " threads, single batch size: " + batchSize + " lines");
    int lineCount = 0;
    StringBuilder sb = new StringBuilder();
    try (Scanner sc = new Scanner(input)) {
      while (sc.hasNextLine()) {
        String line = sc.nextLine();
        sb.append(line);
        sb.append("\n");
        lineCount++;
        if (lineCount > 0 && lineCount % batchSize == 0) {
          texts.add(sb.toString());
          sb = new StringBuilder();
        }
      }
    }
    System.out.println(lineCount + " lines from " + input + " split into " + texts.size() + " text of " + batchSize + " lines each");
    return texts;
  }

  private List<File> runOnTexts(List<String> texts) throws InterruptedException, ExecutionException {
    List<File> resultFiles = new ArrayList<>();
    ExecutorService execService = new ForkJoinPool(threadCount, ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, false);
    List<Callable<File>> callables = new ArrayList<>();
    int count = 0;
    int textsPerThread = texts.size() / threadCount;
    System.out.println("textsPerThread: " + textsPerThread);
    // We shouldn't shuffle sentences (would lead to different results), but we can shuffle the batches
    // in order to get a more or less uniform distribution (i.e. not one thread getting short texts,
    // another one getting long texts):
    Collections.shuffle(texts, new Random(123));
    for (int i = 0; i < threadCount; i++) {
      List<String> tmpTexts = new ArrayList<>();
      for (int j = 0; j < textsPerThread; j++) {
        // TODO: check?
        if (texts.size() == 0) {
          System.out.println("No more texts to be collected");
          break;
        }
        tmpTexts.add(texts.remove(0));
      }
      callables.add(new CheckCallable(count, baseUrl, token, tmpTexts, langCode, user, password, parameters));
      System.out.println("Created thread " + count + " with " + tmpTexts.size() + " texts");
      count++;
    }
    if (texts.size() > 0) {
      System.out.println(texts.size() + " texts remaining, creating another thread for them");
      callables.add(new CheckCallable(count, baseUrl, token, texts, langCode, user, password, parameters));
    } else {
      System.out.println("No texts remaining");
    }
    System.out.println("Created " + callables.size() + " threads");
    List<Future<File>> futures = execService.invokeAll(callables);
    for (Future<File> future : futures) {
      resultFiles.add(future.get());
    }
    execService.shutdownNow();
    return resultFiles;
  }

  private void joinResults(List<File> threadFiles, File output) throws IOException {
    // for the diff in RuleMatchDiffFinder, order doesn't matter...
    System.out.println("Joining " + threadFiles.size() + " result files...");
    ObjectMapper mapper = new ObjectMapper(new JsonFactory());
    threadFiles.sort(Comparator.naturalOrder());
    Set<String> buildDates = new HashSet<>();
    try (FileWriter fw = new FileWriter(output)) {
      for (File threadFile : threadFiles) {
        List<String> lines = Files.readAllLines(threadFile.toPath());
        for (String line : lines) {
          JsonNode node;
          try {
            node = mapper.readTree(line);
          } catch (Exception e) {
            System.err.println("ERROR: Could not parse line from " + threadFile + ": " + line);
            throw e;
          }
          JsonNode date = node.get("software").get("buildDate");
          if (date.isNull() && !line.contains(CheckCallable.FAIL_MESSAGE)) {
            System.err.println("WARNING: 'null' buildDate in " + threadFile + " with " + lines.size() + " lines, line: " + StringUtils.abbreviate(line, 500));
          }
          buildDates.add(date.asText());
          fw.write(line);
          fw.write('\n');
        }
        FileUtils.deleteQuietly(threadFile);
      }
    }
    if (buildDates.size() > 1) {
      System.err.println("-----------------------------------------------------");
      System.err.println("WARNING: inconsistent build dates across API servers ('null' can be ignored): Found " + buildDates);
      System.err.println("-----------------------------------------------------");
    } else {
      System.out.println("All requests answered by API servers with build date " + buildDates);
    }
    System.out.println("Joined result stored at " + output);
  }

  public static void main(String[] args) throws Exception {
    Options options = new Options();
    options.addRequiredOption(null, "input", true, "Plain text input file");
    options.addRequiredOption(null, "lang", true, "Language code, e.g. en-US");
    options.addRequiredOption(null, "threads", true, "Number of threads (NOTE: changing this will change output due to the way input is split)");
    options.addRequiredOption(null, "output", true, "Output file");
    options.addOption(null, "token", true, "Secret token to skip server's limits");
    options.addOption(null, "url", true, "Base URL, defaults to https://api.languagetool.org");
    options.addOption(null, "user", true, "User name for authentication (Basic Auth)");
    // TODO: read from file instead of command line
    options.addOption(null, "password", true, "Password for authentication (Basic Auth)");
    options.addOption(null, "parameters", true, "Additional parameters to send with HTTP requests as form-encoded POST data");
    CommandLine cmd = new DefaultParser().parse(options, args);
    HttpApiSentenceChecker checker = new HttpApiSentenceChecker(cmd);
    checker.run(new File(cmd.getOptionValue("input")), new File(cmd.getOptionValue("output")));
  }

}
