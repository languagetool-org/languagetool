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
package de.danielnaber.languagetool.dev;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.tools.StringTools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Command line tool to list activity for grammar files. Requires a typical developer setup,
 * i.e. a local "svn" command and the sources checked out from SVN. 
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
    
    final List<String> sortedLanguages = new ArrayList<String>();
    for (Language element : Language.LANGUAGES) {
      if (element == Language.DEMO) {
        continue;
      }
      sortedLanguages.add(element.getName());
    }
    Collections.sort(sortedLanguages);

    final Calendar today = GregorianCalendar.getInstance();
    final Calendar past = GregorianCalendar.getInstance();
    past.add(Calendar.DAY_OF_MONTH, -PAST_DAYS);
    
    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    final String todayString = dateFormat.format(today.getTime());
    final String pastString = dateFormat.format(past.getTime());
    
    final Runtime runtime = Runtime.getRuntime();
    for (final String langName : sortedLanguages) {
      final Language lang = Language.getLanguageForName(langName);
      final File xmlFile = new File(".", JLanguageTool.getDataBroker().getRulesDir() + File.separator + lang.getShortName() + File.separator + "grammar.xml");
      final String command = "svn log -q -r {" + pastString + "}:{" + todayString + "} src/" + xmlFile;
      final Process process = runtime.exec(command);
      final InputStream inputStream = process.getInputStream();
      final String output = StringTools.readFile(inputStream);
      process.waitFor();
      final int commits = getCommits(output);
      System.out.println(commits + "\t" + langName);
    }
  }

  private int getCommits(String svnOutput) {
    int count = 0;
    final Scanner scanner = new Scanner(svnOutput);
    try {
      while (scanner.hasNextLine()) {
        final String line = scanner.nextLine();
        if (line.matches("^r\\d+.*")) {
          count++;
        }
      }
    } finally {
      scanner.close();
    }
    return count;
  }

}
