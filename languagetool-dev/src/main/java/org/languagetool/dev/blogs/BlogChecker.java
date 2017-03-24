/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.blogs;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.commandline.CommandLineTools;

import java.io.File;
import java.io.IOException;

/**
 * Runs LT over the blog content fetched with {@link BlogFetcher}.
 * @since 2.7
 */
class BlogChecker {

  private void check(File dir, Language lang) throws IOException {
    JLanguageTool lt = new JLanguageTool(lang);
    lt.disableRule("WHITESPACE_RULE");
    lt.disableRule("UNPAIRED_BRACKETS");
    File[] files = dir.listFiles();
    for (File file : files) {
      System.out.println("\n=== " + file.getName() +  " ================================");
      String content = cleanup(FileUtils.readFileToString(file, "utf-8"));
      CommandLineTools.checkText(content, lt);
    }
  }

  private String cleanup(String content) {
    String result = content
      .replaceAll("\\s+", " ")
      .replaceAll("<div.*?>", "")
      .replaceAll("</div>", "\n\n")
      .replaceAll("</h[1-6]>", "\n\n")
      .replaceAll("<li>", "\n")
      .replaceAll("<p.*?>", "")
      .replaceAll("</p>", "\n\n")
      .replaceAll("<a.*?>", "")
      .replaceAll("</a>", "")
      .replaceAll("<br\\s*/>", "")
      .replaceAll("<br>", "")
      .replaceAll("<.*?>", "");
    return StringEscapeUtils.unescapeHtml4(result).replace(" ", " ");  // nbsp
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.err.println("Usage: " + BlogChecker.class.getSimpleName() + " <langCode> <contentDir>");
      System.exit(1);
    }
    BlogChecker checker = new BlogChecker();
    Language lang = Languages.getLanguageForShortCode(args[0]);
    checker.check(new File(args[1]), lang);
  }
}
