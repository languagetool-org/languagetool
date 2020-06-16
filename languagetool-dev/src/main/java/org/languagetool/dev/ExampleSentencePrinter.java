/* LanguageTool, a natural language style checker
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.IncorrectExample;
import org.languagetool.rules.Rule;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Prints the first of the incorrect examples per rule.
 */
final class ExampleSentencePrinter {

  private static final int MAX_BLOCK_SIZE = 5000;
  
  private void run(Language lang) {
    File basePath = new File("/home/dnaber/lt/git/languagetool/languagetool-language-modules");
    if (!basePath.exists()) {
      throw new RuntimeException("basePath does not exist: " + basePath);
    }
    JLanguageTool tool = new JLanguageTool(lang);
    System.out.println("<html>");
    System.out.println("<head>");
    System.out.println("  <title>LanguageTool examples sentences</title>");
    System.out.println("  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />");
    System.out.println("</head>");
    System.out.println("<body>");
    int i = 1;
    int blockSize = 0;
    for (Rule rule : tool.getAllActiveRules()) {
      List<IncorrectExample> incorrectExamples = rule.getIncorrectExamples();
      if (incorrectExamples.size() > 0) {
        String example = incorrectExamples.get(0).getExample()
                .replace("<marker>", "<b>")
                .replace("</marker>", "</b>");
        int exampleLength = example.replace("<b>", "").replace("</b>", "").length();
        if (blockSize + exampleLength > MAX_BLOCK_SIZE) {
          System.out.println("<br><br>");
          blockSize = 0;
        }
        //System.out.println(i + ". " + example + " [" + rule.getId() + "]<br>");
        System.out.println(example + "<br>");
        blockSize += exampleLength;
        i++;
      }
    }
    System.out.println("</body>");
    System.out.println("</html>");
  }

  public static void main(String[] args) {
    ExampleSentencePrinter prg = new ExampleSentencePrinter();
    prg.run(Languages.getLanguageForShortCode("de"));
  }

}
