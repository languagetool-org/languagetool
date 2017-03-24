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
package org.languagetool.dev.wikipedia;

import org.languagetool.JLanguageTool;
import org.languagetool.dev.dumpcheck.SentenceSourceChecker;
import org.languagetool.dev.dumpcheck.SentenceSourceIndexer;
import org.languagetool.dev.index.Indexer;
import org.languagetool.dev.index.Searcher;
import org.languagetool.tools.JnaTools;

import java.util.Arrays;

/**
 * A class to be called from command line - dispatches to the actual implementations
 * by calling their main method.
 */
public class Main {

  public static void main(String[] args) throws Exception {
    JnaTools.setBugWorkaroundProperty();
    if (args.length == 0) {
      printUsageAndExit();
    } else {
      String[] remainingArgs = Arrays.copyOfRange(args, 1, args.length);
      String command = args[0];
      switch (command) {
        case "check-data":
          SentenceSourceChecker.main(remainingArgs);
          break;
        case "index-data":
          SentenceSourceIndexer.main(remainingArgs);
          break;
        case "wiki-check":
          WikipediaQuickCheck.main(remainingArgs);
          break;
        case "index":
          Indexer.main(remainingArgs);
          break;
        case "search":
          Searcher.main(remainingArgs);
          break;
        case "version":
          System.out.println(JLanguageTool.VERSION + " (" + JLanguageTool.BUILD_DATE + ")");
          break;
        default:
          System.out.println("Error: unknown command '" + command + "'");
          printUsageAndExit();
          break;
      }
    }
  }

  private static void printUsageAndExit() {
    System.out.println("Usage: " + Main.class.getName() + " <command> <command-specific-arguments>");
    System.out.println("Where <command> is one of:");
    System.out.println("   check-data - check a Wikipedia XML dump like those available from");
    System.out.println("                http://dumps.wikimedia.org/backup-index.html");
    System.out.println("                and/or a Tatoeba file (http://tatoeba.org)");
    System.out.println("   index-data - fulltext-index a Wikipedia XML dump and/or a Tatoeba file");
    System.out.println("   wiki-check - check a single Wikipedia page, fetched via the Mediawiki API");
    System.out.println("   index      - index a plain text file, putting the analysis in a Lucene index for faster rule match search");
    System.out.println("   search     - search for rule matches in an index created with 'index' or 'wiki-index'");
    System.out.println("   version    - print LanguageTool version number and build date");
    System.out.println("");
    System.out.println("All commands have different usages. Call them without arguments to get help.");
    System.out.println("");
    System.out.println("Example for a call with valid arguments:");
    System.out.println("   java -jar languagetool-wikipedia.jar wiki-check http://de.wikipedia.org/wiki/Bielefeld");
    System.exit(1);
  }

}
