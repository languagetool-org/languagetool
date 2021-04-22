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
package org.languagetool.dev.errorcorpus;

import org.apache.commons.io.IOUtils;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Access to a simple error corpus with this format:
 * <pre>
 * 1. This is _a_ error. =&gt; an
 * 2. Here _come_ another example. =&gt; comes
 * </pre>
 * @since 3.2
 */
public class SimpleCorpus implements ErrorCorpus {

  private final List<String> lines = new ArrayList<>();
  
  private int pos;
  
  public SimpleCorpus(File simpleTextFile) throws IOException {
    try (FileInputStream fis = new FileInputStream(simpleTextFile)) {
      lines.addAll(IOUtils.readLines(fis).stream().filter(line -> line.matches("\\d+\\..*")).collect(Collectors.toList()));
    }
    System.out.println("Loaded " + lines.size() + " example sentences");
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
        return getIncorrectSentence(line);
      }
      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  private ErrorSentence getIncorrectSentence(String line) {
    String normalized = line.replaceFirst("\\d+\\.\\s*", "");
    String normalizedNoCorrection = normalized.replaceFirst("=>.*", "").trim();
    int startError = normalized.indexOf('_');
    int endError = normalized.indexOf('_', startError+1);
    if (startError == -1 || endError == -1) {
      throw new RuntimeException("No '_..._' marker found: " + line);
    }
    int startCorrectionMarker = normalized.indexOf("=>");
    if (startCorrectionMarker == -1) {
      throw new RuntimeException("No '=>' marker found: " + line);
    }
    String correction = normalized.substring(startCorrectionMarker + "=>".length());
    List<Error> errors = Arrays.asList(new Error(startError + 1, endError - 1, correction));
    return new ErrorSentence(normalizedNoCorrection, makeAnnotatedText(normalizedNoCorrection), errors);
  }

  private AnnotatedText makeAnnotatedText(String text) {
    AnnotatedTextBuilder builder = new AnnotatedTextBuilder();
    builder.addText(text.replace("_", " ").replaceAll("\\s+", " "));
    return builder.build();
  }

}
