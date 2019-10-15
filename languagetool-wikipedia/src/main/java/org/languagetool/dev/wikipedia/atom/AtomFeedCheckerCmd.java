/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.wikipedia.atom;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;

import java.io.File;
import java.io.IOException;

/**
 * Command line tool to check the changes from a Wikipedia Atom feed with LanguageTool.
 * @since 2.4
 */
final class AtomFeedCheckerCmd {

  private AtomFeedCheckerCmd() {
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    if (args.length < 2 || args.length > 4) {
      System.out.println("Usage: " + AtomFeedCheckerCmd.class.getSimpleName() + " <atomFeedUrl> <sleepTime> [database.properties] [languageModelDir]");
      System.out.println("  <atomFeedUrl> is a Wikipedia URL to the latest changes, for example:");
      System.out.println("    https://de.wikipedia.org/w/index.php?title=Spezial:Letzte_%C3%84nderungen&feed=atom&namespace=0");
      System.out.println("  <sleepTime> -1: don't loop at all (run once), 0: run in loop, other number: run in loop and");
      System.out.println("    wait this many milliseconds between runs");
      System.out.println("  [database.properties] (optional) is a file that defines dbUrl, dbUser, and dbPassword,");
      System.out.println("    used to write the results to a database via JDBC");
      System.out.println("  [languageModelDir] (optional, use only together with database.properties) a directory with ngram");
      System.out.println("    sub directories, activates the confusion rule if supported");
      System.out.println("");
      System.out.println("  When the database.properties file is specified, this command will store all feed changes that");
      System.out.println("  cause LanguageTool rule matches to the database. If an error is then fixed later, this will");
      System.out.println("  usually also be detected and the rule match in the database will be marked as fixed. One case");
      System.out.println("  where this does not work is if the context of the error gets modified before the error is fixed.");
      System.out.println("");
      System.out.println("  Run this command regularly so that you don't miss any changes from the feed.");
      System.out.println("  As the feed may contain only the latest 50 changes, running it more often than");
      System.out.println("  once per minute may be needed for active Wikipedias.");
      System.exit(1);
    }
    String url = args[0];
    String langCode = url.substring(url.indexOf("//") + 2, url.indexOf('.'));
    System.out.println("Using URL: " + url);
    System.out.println("Language code: " + langCode);
    int sleepTimeMillis = Integer.parseInt(args[1]);
    System.out.println("Sleep time: " + sleepTimeMillis + "ms (-1 = don't loop)");
    System.out.println("LanguageTool version: " + JLanguageTool.VERSION + " (" + JLanguageTool.BUILD_DATE + ")");
    DatabaseConfig databaseConfig = null;
    if (args.length >= 3) {
      String propFile = args[2];
      databaseConfig = new DatabaseConfig(propFile);
      System.out.println("Writing results to database at: " + databaseConfig.getUrl());
    }
    AtomFeedChecker atomFeedChecker;
    Language language = Languages.getLanguageForShortCode(langCode);
    if (args.length == 4) {
      String languageModelDir = args[3];
      atomFeedChecker = new AtomFeedChecker(language, databaseConfig, new File(languageModelDir));
    } else {
      atomFeedChecker = new AtomFeedChecker(language, databaseConfig);
    }
    while (true) {
      long startTime = System.currentTimeMillis();
      try {
        atomFeedChecker.runCheck(url);
        System.out.println("Run time: " + (System.currentTimeMillis() - startTime) + "ms");
        if (sleepTimeMillis == -1) {
          // don't loop at all
          break;
        } else {
          System.out.println("Sleeping " + sleepTimeMillis + "ms...");
          Thread.sleep(sleepTimeMillis);
        }
      } catch (Exception e) {
        // e.g. 50x HTTP errors, network problems
        e.printStackTrace();
        System.out.println("Sleeping " + sleepTimeMillis + "ms...");
        Thread.sleep(sleepTimeMillis);
      }
    }
  }

}
