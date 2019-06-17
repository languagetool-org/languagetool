/* LanguageTool, a natural language style checker 
 * Copyright (C) 2011 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Scanner;

import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.tools.StringTools;

/**
 * Command line tool to list activity for grammar files. Requires a typical developer setup,
 * i.e. a local "git" command and the sources cloned from git. 
 * 
 * @author Daniel Naber
 */
final class RuleActivityOverview {

  private static final int PAST_DAYS = 365/2;

  RuleActivityOverview() {
  }

  private void run() {
    System.out.println("Commits per language in the last " + PAST_DAYS + " days");
    System.out.println("Date: " + new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
    
    List<String> sortedLanguages = new ArrayList<>();
    for (Language element : Languages.get()) {
      sortedLanguages.add(element.getName());
    }
    Collections.sort(sortedLanguages);
    for (String langName : sortedLanguages) {
      Language lang = Languages.getLanguageForName(langName);
      int commits = getActivityFor(lang, PAST_DAYS);
      System.out.println(commits + "\t" + lang.getName() + (lang.isVariant() ? " (including the parent language)" : ""));
    }
  }

  int getActivityFor(Language lang, int pastDays) {
    try {
      Calendar past = GregorianCalendar.getInstance();
      past.add(Calendar.DAY_OF_MONTH, -pastDays);
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      String pastString = dateFormat.format(past.getTime());
      String langCode = lang.getShortCode();
      List<File> xmlFiles = getAllXmlFiles(lang, langCode);
      int commits = 0;
      for (File file : xmlFiles) {
        if (!file.getName().contains("-test-") && !file.exists()) {
          throw new RuntimeException("Not found: " + file);
        }
        String command = "git log --after=" + pastString + " " + file;
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(command);
        InputStream inputStream = process.getInputStream();
        String output = StringTools.readStream(inputStream, "utf-8");
        process.waitFor();
        commits += getCommits(output);
      }
      return commits;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private List<File> getAllXmlFiles(Language lang, String langCode) {
    List<File> files = new ArrayList<>();
    List<String> ruleFileNames = lang.getRuleFileNames();
    for (String ruleFileName : ruleFileNames) {
      files.add(new File("../languagetool-language-modules/" + langCode + "/src/main/resources/" + ruleFileName));
    }
    File disambiguationFile = new File("../languagetool-language-modules/" + langCode +
            "/src/main/resources/org/languagetool/resource/" + langCode + "/disambiguation.xml");
    if (disambiguationFile.exists()) {
      files.add(disambiguationFile);
    }
    return files;
  }

  private int getCommits(String svnOutput) {
    int count = 0;
    try (Scanner scanner = new Scanner(svnOutput)) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        if (line.startsWith("commit ")) {
          count++;
        }
      }
    }
    return count;
  }

  public static void main(String[] args) throws Exception {
    RuleActivityOverview prg = new RuleActivityOverview();
    prg.run();
  }

}
