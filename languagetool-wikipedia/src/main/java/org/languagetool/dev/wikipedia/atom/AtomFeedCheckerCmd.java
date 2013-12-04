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

import org.languagetool.Language;

import java.io.IOException;

/**
 * Command line tool to check the changes from a Wikipedia Atom feed with LanguageTool.
 * @since 2.4
 */
final class AtomFeedCheckerCmd {

  public static void main(String[] args) throws IOException, InterruptedException {
    boolean loopMode = true;
    if (args.length != 1 && args.length != 2) {
      System.out.println("Usage: " + AtomFeedCheckerCmd.class.getSimpleName() + " <atomFeedUrl> [database.properties]");
      System.out.println("  <atomFeedUrl> is a Wikipedia URL to the latest changes, for example:");
      System.out.println("  https://de.wikipedia.org/w/index.php?title=Spezial:Letzte_%C3%84nderungen&feed=atom&namespace=0");
      System.exit(1);
    }
    // TODO: load lastDiffId from properties file
    // TODO: print currentDiffId - lastDiffId (if it's large, content might have been missed)
    String url = args[0];
    String langCode = url.substring(url.indexOf("//") + 2, url.indexOf("."));
    System.out.println("Using URL: " + url);
    System.out.println("Language code: " + langCode);
    DatabaseConfig databaseConfig = null;
    if (args.length == 2) {
      String propFile = args[1];
      databaseConfig = new DatabaseConfig(propFile);
      System.out.println("Writing results to database at: " + databaseConfig.getUrl());
    }
    Language language = Language.getLanguageForShortName(langCode);
    AtomFeedChecker atomFeedChecker = new AtomFeedChecker(language, databaseConfig);
    long latestDiffId = 0;
    if (loopMode) {
      System.out.println("Running in loop mode until stopped...");
      while (true) {
        System.out.println("\nRunning with latestDiffId " + latestDiffId);
        CheckResult checkResult = atomFeedChecker.runCheck(url, latestDiffId);
        latestDiffId = checkResult.getLatestDiffId();
        Thread.sleep(60*1000);
      }
    } else {
      CheckResult checkResult = atomFeedChecker.runCheck(url, 0);
    }
    // TODO: store lastDiffId to properties file
  }

}
