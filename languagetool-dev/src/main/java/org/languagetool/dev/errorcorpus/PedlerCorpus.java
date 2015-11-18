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
package org.languagetool.dev.errorcorpus;

import org.apache.commons.io.IOUtils;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Access to the Jenny Pedler's Real-word Error Corpus corpus.
 * Get it from http://www.dcs.bbk.ac.uk/~jenny/resources.html.
 * @since 2.7
 */
public class PedlerCorpus implements ErrorCorpus {

  private static final String NORMALIZE_REGEX = "\\s*<ERR targ\\s*=\\s*([^>]*?)\\s*>\\s*(.*?)\\s*</ERR>\\s*";
  
  private final List<String> lines = new ArrayList<>();
  
  private int pos;
  
  public PedlerCorpus(File dir) throws IOException {
    File[] files = dir.listFiles();
    if (files == null) {
      throw new RuntimeException("Directory not found or is not a directory: " + dir);
    }
    for (File file : files) {
      if (!file.getName().endsWith(".txt")) {
        System.out.println("Ignoring " + file + ", does not match *.txt");
        continue;
      }
      try (FileInputStream fis = new FileInputStream(file)) {
        lines.addAll(IOUtils.readLines(fis));
      }
    }
  }

  @Override
  public Iterator<ErrorSentence> iterator() {
    return new Iterator<ErrorSentence>() {
      @Override
      public boolean hasNext() {
        return pos < lines.size();
      }

      @Override
      public ErrorSentence next() {
        String line = lines.get(pos++);
        ErrorSentence sentence = getIncorrectSentence(line);
        return sentence;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  private ErrorSentence getIncorrectSentence(String line) {
    String normalized = line.replaceAll(NORMALIZE_REGEX, " <ERR targ=$1>$2</ERR> ").replaceAll("\\s+", " ").trim();
    List<Error> errors = new ArrayList<>();
    int startPos = 0;
    while (normalized.indexOf("<ERR targ=", startPos) != -1) {
      int startTagStart = normalized.indexOf("<ERR targ=", startPos);
      int startTagEnd = normalized.indexOf(">", startTagStart);
      int endTagStart = normalized.indexOf("</ERR>", startTagStart);
      int correctionEnd = normalized.indexOf(">", startTagStart);
      String correction = normalized.substring(startTagStart + "<ERR targ=".length(), correctionEnd);
      errors.add(new Error(startTagEnd + 1, endTagStart, correction));
      startPos = startTagStart + 1;
    }
    return new ErrorSentence(normalized, makeAnnotatedText(normalized), errors);
  }

  private AnnotatedText makeAnnotatedText(String pseudoXml) {
    AnnotatedTextBuilder builder = new AnnotatedTextBuilder();
    StringTokenizer tokenizer = new StringTokenizer(pseudoXml, "<>", true);
    boolean inMarkup = false;
    while (tokenizer.hasMoreTokens()) {
      String part = tokenizer.nextToken();
      if (part.startsWith("<")) {
        builder.addMarkup(part);
        inMarkup = true;
      } else if (part.startsWith(">")) {
        inMarkup = false;
        builder.addMarkup(part);
      } else {
        if (inMarkup) {
          builder.addMarkup(part);
        } else {
          builder.addText(part);
        }
      }
    }
    return builder.build();
  }

}
