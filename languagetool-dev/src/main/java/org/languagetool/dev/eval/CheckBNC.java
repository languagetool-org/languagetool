/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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

package org.languagetool.dev.eval;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.languagetool.JLanguageTool;
import org.languagetool.commandline.CommandLineTools;
import org.languagetool.language.English;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tools.StringTools;

/**
 * Uses JLanguageTol recursively on the files of the BNC (British National Corpus).
 * 
 * @author Daniel Naber
 */
public final class CheckBNC {

  private final JLanguageTool lt;
  private final BNCTextFilter textFilter = new BNCTextFilter();

  private static final boolean CHECK_BY_SENTENCE = true;

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.out.println("Usage: CheckBNC <directory>");
      System.exit(1);
    }
    final CheckBNC prg = new CheckBNC();
    prg.run(new File(args[0]));
  }
  
  private CheckBNC() {
    lt = new JLanguageTool(new English());
    final String[] disRules = {"UPPERCASE_SENTENCE_START", "COMMA_PARENTHESIS_WHITESPACE",
        "WORD_REPEAT_RULE", "DOUBLE_PUNCTUATION"};
    System.err.println("Note: disabling the following rules:");
    for (String disRule : disRules) {
      lt.disableRule(disRule);
      System.err.println(" " + disRule);
    }
  }

  private void run(final File file) throws IOException {
    if (file.isDirectory()) {
      final File[] files = file.listFiles();
      for (File file1 : files) {
        run(new File(file, file1.getName()));
      }
    } else {
      System.out.println("Checking " + file.getAbsolutePath());
      String text = StringTools.readStream(new FileInputStream(file.getAbsolutePath()), "utf-8");
      text = textFilter.filter(text);
      if (CHECK_BY_SENTENCE) {
        final Tokenizer sentenceTokenizer = lt.getLanguage().getSentenceTokenizer();
        final List<String> sentences = sentenceTokenizer.tokenize(text);
        for (String sentence : sentences) {
          CommandLineTools.checkText(sentence, lt, false, false, 1000);
        }
      } else {
        CommandLineTools.checkText(text, lt);
      }
    }
  }

  static class BNCTextFilter {

    public String filter(String text) {
      String fText = text.replaceAll("(?s)<header.*?>.*?</header>", "");
      fText = fText.replaceAll("<w.*?>", "");
      fText = fText.replaceAll("<c.*?>", "");
      fText = fText.replaceAll("<.*?>", "");
      fText = fText.replaceAll(" +", " ");
      fText = fText.replaceAll("&bquo|&equo", "\"");
      fText = fText.replaceAll("&mdash;?", "--");
      fText = fText.replaceAll("&amp;?", "&");
      return fText;
    }

  }

}
