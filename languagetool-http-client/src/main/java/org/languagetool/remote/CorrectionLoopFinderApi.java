/* LanguageTool, a natural language style checker
 * Copyright (C) 2023 Jaume Ortlà
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;

import org.languagetool.tools.Tools;

/**
 * Find correction loops, i.e. cases where accepting a correction causes
 * another rule to match which then again suggests the original text.
 * Use on any plain text file with one sentence per line (like Tatoeba).
 */
public class CorrectionLoopFinderApi {

  private final static int linesToSkip = 0;

  private static void run(Configuration cfg) throws IOException {
    Scanner sc = new Scanner(cfg.inputFile);
    int lineCount = 0;
    while (sc.hasNextLine()) {
      String line = sc.nextLine();
      lineCount++;
      if (lineCount < linesToSkip) {
        if (lineCount % 1000 == 0) {
          System.out.println(lineCount + " skipped ...");
        }
        continue;
      }
      List<RemoteRuleMatch> matches = null;
      try {
        matches = cfg.lt.check(line, cfg.ltConfig, cfg.customParams).getMatches();
      } catch (RuntimeException e) {
        System.err.println("An exception occurred: " + e.getMessage());
      }
      if (matches != null) {
        for (RemoteRuleMatch match : matches) {
          int suggCount = 0;
          for (String repl : match.getReplacements().get()) {
            if (++suggCount > 5) {
              break;
            }
            String corr = new StringBuilder(line).replace(match.getErrorOffset(), match.getErrorOffset()+match.getErrorLength(), repl).toString();
            //System.out.println(line + " => " + corr);
            List<RemoteRuleMatch> corrMatches = null;
            try {
              corrMatches = cfg.lt.check(corr, cfg.ltConfig, cfg.customParams).getMatches();
            } catch (RuntimeException e) {
              System.err.println("An exception occurred: " + e.getMessage());
            }
            if (corrMatches == null) {
              continue;
            }
            for (RemoteRuleMatch corrMatch : corrMatches) {
              for (String repl2 : corrMatch.getReplacements().get()) {
                String corr2 = new StringBuilder(corr).replace(corrMatch.getErrorOffset(), corrMatch.getErrorOffset()+corrMatch.getErrorLength(), repl2).toString();
                if (corr2.equals(line)) {
                  cfg.out.write("LOOP by " + getFullId(match) + "/" + getFullId(corrMatch) + ": " +
                    line.substring(match.getErrorOffset(), match.getErrorOffset()+match.getErrorLength()) + " => " + repl + "\n");
                  cfg.out.write("  " + line + "\n");
                  cfg.out.write("  " + corr + "\n");
                }
              }
            }
          }
        }
        cfg.out.flush();
      }
      if (lineCount % 1000 == 0) {
        System.out.println(lineCount + "...");
      }
    }
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.out.println("Usage: " + CorrectionLoopFinderApi.class.getSimpleName() + " <configFile>");
      System.exit(1);
    }
    String configurationFilename = args[0];
    Properties prop = new Properties();
    FileInputStream fis = new FileInputStream(configurationFilename);
    prop.load(new InputStreamReader(fis, Charset.forName("UTF-8")));
    Configuration cfg = new Configuration();
    cfg.remoteServer = prop.getProperty("remoteServer", "http://localhost:8081").trim();
    cfg.userName = prop.getProperty("userName", "").trim();
    cfg.annotatorName = prop.getProperty("annotatorName", "").trim();
    cfg.apiKey = prop.getProperty("apiKey", "").trim();
    cfg.inputFilePath = prop.getProperty("inputFile", "").trim();
    cfg.outputFilePath = prop.getProperty("outputFile", "").trim();
    cfg.languageCode = prop.getProperty("languageCode").trim();
    String customParamsStr = prop.getProperty("customParams", "").trim();
    if (!customParamsStr.isEmpty()) {
      for (String customParam : customParamsStr.split(";")) {
        String[] parts = customParam.split(",");
        cfg.customParams.put(parts[0], parts[1]);
      }
    }
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
    run(cfg);
  }

  static private class Configuration {
    String remoteServer;
    String userName;
    String annotatorName;
    String apiKey;
    String inputFilePath;
    String outputFilePath;
    String languageCode;
    File inputFile;
    File outputFile;
    boolean automaticAnnotation;
    CheckConfiguration ltConfig;
    RemoteLanguageTool lt;
    Map<String, String> customParams = new HashMap<>();
    FileWriter out;
    StringBuilder outStrB;
    String ansiDefault = "";
    String ansiHighlight = "";
    List<String> enabledOnlyRules = new ArrayList<String>();
    List<String> disabledRules = new ArrayList<String>();

    void prepareConfiguration() throws IOException {
      CheckConfigurationBuilder cfgBuilder = new CheckConfigurationBuilder(languageCode);
      // cfgBuilder.textSessionID("-2");
      if (enabledOnlyRules.isEmpty()) {
        cfgBuilder.disabledRuleIds("WHITESPACE_RULE");
        cfgBuilder.disabledRuleIds("UNPAIRED_BRACKETS");
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
        outputFile = new File(inputFile.getParentFile() + "/" + fileName + "-loops.txt");
      } else {
        outputFile = new File(outputFilePath);
      }
      outStrB = new StringBuilder();
      out = new FileWriter(outputFile, true);
      lt = new RemoteLanguageTool(Tools.getUrl(remoteServer));
    }
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
}
