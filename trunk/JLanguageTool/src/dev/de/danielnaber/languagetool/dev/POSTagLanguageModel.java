/* LanguageTool, a natural language style checker 
 * Copyright (C) 2009 Daniel Naber (http://www.danielnaber.de)
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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;

/**
 * Tag text and display only POS tags to create an n-gram language model.
 * 
 * @author Marcin Milkowski
 */
public class POSTagLanguageModel {

  /**
   * @param args
   * @throws IOException
   */
  public static void main(final String[] args) throws IOException {
    if (args.length == 1) {
      final Language language = getLanguageOrExit(args[0]);
      final JLanguageTool lt = new JLanguageTool(language, null);
      runOnStdIn(lt);
    } else {
      exitWithUsageMessage();
    }
  }

  private static Language getLanguageOrExit(final String lang) {
    Language language = null;
    boolean foundLanguage = false;
    final List<String> supportedLanguages = new ArrayList<String>();
    for (final Language tmpLang : Language.LANGUAGES) {
      supportedLanguages.add(tmpLang.getShortName());
      if (lang.equals(tmpLang.getShortName())) {
        language = tmpLang;
        foundLanguage = true;
        break;
      }
    }
    if (!foundLanguage) {
      System.out.println("Unknown language '" + lang
          + "'. Supported languages are: " + supportedLanguages);
      exitWithUsageMessage();
    }
    return language;
  }

  private static void exitWithUsageMessage() {
    System.out
        .println("Usage: java de.danielnaber.languagetool.dev.POSTagLanguageModel language");
  }

  private static void runOnStdIn(final JLanguageTool lt) throws IOException {
    final int MAX_FILE_SIZE = 64000;
    InputStreamReader isr = null;
    BufferedReader br = null;
    StringBuilder sb = new StringBuilder();
    try {
      isr = new InputStreamReader(new BufferedInputStream(System.in));
      br = new BufferedReader(isr);
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line);
        sb.append('\n');
        if (lt.getLanguage().getSentenceTokenizer().singleLineBreaksMarksPara()) {
          tagText(sb.toString(), lt);
          sb = new StringBuilder();
        } else {
          if ("".equals(line) || sb.length() >= MAX_FILE_SIZE) {
            tagText(sb.toString(), lt);
            sb = new StringBuilder();
          }
        }
      }
    } finally {
      if (sb.length() > 0) {
        tagText(sb.toString(), lt);
      }
    }

    br.close();
    isr.close();
  }

  private static void tagText(final String contents, final JLanguageTool lt)
      throws IOException {
    AnalyzedSentence analyzedText;
    final List<String> sentences = lt.sentenceTokenize(contents);
    for (final String sentence : sentences) {
      analyzedText = lt.getAnalyzedSentence(sentence);
      System.out.println(getSentence(analyzedText));
    }
  }

  private static String getSentence(final AnalyzedSentence sent) {
    final StringBuilder sb = new StringBuilder();
    sb.append("<S>");
    for (final AnalyzedTokenReadings atr : sent.getTokensWithoutWhitespace()) {
      sb.append(getPOS(atr));
      sb.append(' ');
    }
    sb.append("</S>");
    return sb.toString();
  }

  private static String getPOS(final AnalyzedTokenReadings atr) {
    final StringBuilder sb = new StringBuilder();
    final int readNum = atr.getReadingsLength();
    for (int i = 0; i < readNum; i++) {
      if (!atr.isWhitespace()) {
        sb.append(atr.getAnalyzedToken(i).getPOSTag());
        if (i != readNum - 1) {
          sb.append('+');
        }
      }
    }
    return sb.toString();
  }
  
}
