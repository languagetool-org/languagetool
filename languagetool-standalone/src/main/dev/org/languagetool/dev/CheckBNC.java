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

package org.languagetool.dev;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.languagetool.JLanguageTool;
import org.languagetool.TextFilter;
import org.languagetool.language.English;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tools.StringTools;
import org.languagetool.tools.Tools;

/**
 * Uses JLanguageTol recursively on the files of the BNC (British National Corpus).
 * 
 * @author Daniel Naber
 */
public final class CheckBNC {

  private JLanguageTool langTool = null;
  private final TextFilter textFilter = new BNCTextFilter();

  static final boolean CHECK_BY_SENTENCE = true;

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.out.println("Usage: CheckBNC <directory>");
      System.exit(1);
    }
    final CheckBNC prg = new CheckBNC();
    prg.run(new File(args[0]));
  }
  
  private CheckBNC() throws IOException {
    langTool = new JLanguageTool(new English());
    langTool.activateDefaultPatternRules();
    final String[] disRules = new String[] {"UPPERCASE_SENTENCE_START", "COMMA_PARENTHESIS_WHITESPACE",
        "WORD_REPEAT_RULE", "DOUBLE_PUNCTUATION"};
    System.err.println("Note: disabling the following rules:");
    for (String disRule : disRules) {
      langTool.disableRule(disRule);
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
      String text = StringTools.readStream(new FileInputStream(file.getAbsolutePath()));
      text = textFilter.filter(text);
      if (CHECK_BY_SENTENCE) {
        final Tokenizer sentenceTokenizer = langTool.getLanguage().getSentenceTokenizer();
        final List<String> sentences = sentenceTokenizer.tokenize(text);
        for (String sentence : sentences) {
          CommandLineTools.checkText(sentence, langTool, false, 1000);
        }
      } else {
        Tools.checkText(text, langTool);
      }
    }
  }

}

class BNCTextFilter implements TextFilter {

  public String filter(String text) {
    text = text.replaceAll("(?s)<header.*?>.*?</header>", "");
    text = text.replaceAll("<w.*?>", "");
    text = text.replaceAll("<c.*?>", "");
    text = text.replaceAll("<.*?>", "");
    text = text.replaceAll(" +", " ");
    text = text.replaceAll("&bquo|&equo", "\"");
    text = text.replaceAll("&mdash;?", "--");
    text = text.replaceAll("&amp;?", "&");
    return text;
  }
  
}
