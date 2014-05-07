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
import java.io.IOException;
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
import org.languagetool.tools.StringTools;

/**
 * Command line tool to list activity for grammar files. Requires a typical developer setup,
 * i.e. a local "git" command and the sources cloned from git. 
 * 
 * @author Daniel Naber
 */
public final class RuleActivityOverview {

  private static final int PAST_DAYS = 365/2;

  public static void main(final String[] args) throws Exception {
    final RuleActivityOverview prg = new RuleActivityOverview();
    prg.run();
  }
  
  private RuleActivityOverview() {
    // no constructor
  }
  
  private void run() throws IOException, InterruptedException {

    System.out.println("Commits per language in the last " + PAST_DAYS + " days");
    System.out.println("Date: " + new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
    
    final List<String> sortedLanguages = new ArrayList<>();
    for (Language element : Language.REAL_LANGUAGES) {
      sortedLanguages.add(element.getName());
    }
    Collections.sort(sortedLanguages);

    final Calendar past = GregorianCalendar.getInstance();
    past.add(Calendar.DAY_OF_MONTH, -PAST_DAYS);
    
    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    final String pastString = dateFormat.format(past.getTime());
    
    final Runtime runtime = Runtime.getRuntime();
    for (final String langName : sortedLanguages) {
      final Language lang = Language.getLanguageForName(langName);
      String langCode = lang.getShortName();
      List<String> ruleFileNames = lang.getRuleFileNames();
      int commits = 0;
      for (String ruleFileName : ruleFileNames) {
        final File xmlFile = new File("languagetool-language-modules/" + langCode
                + "/src/main/resources/" + ruleFileName);
        final String command = "git log --after=" + pastString + " " + xmlFile;
        final Process process = runtime.exec(command);
        final InputStream inputStream = process.getInputStream();
        final String output = StringTools.readStream(inputStream, "utf-8");
        process.waitFor();
        commits += getCommits(output);
      }
      System.out.println(commits + "\t" + langName + (lang.isVariant() ? " (including the parent language)" : ""));
    }
  }

  private int getCommits(String svnOutput) {
    int count = 0;
    try (Scanner scanner = new Scanner(svnOutput)) {
      while (scanner.hasNextLine()) {
        final String line = scanner.nextLine();
        if (line.startsWith("commit ")) {
          count++;
        }
      }
    }
    return count;
  }

}
