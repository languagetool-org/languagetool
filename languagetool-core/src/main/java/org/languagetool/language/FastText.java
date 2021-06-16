/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.language;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.languagetool.language.LanguageIdentifier.canLanguageBeDetected;

/**
 * @since 5.0
 */
public class FastText {

  private static final Logger logger = LoggerFactory.getLogger(FastText.class);
  private static final int K_HIGHEST_SCORES = 5;

  private final Process fasttextProcess;
  private final BufferedReader fasttextIn;
  private final BufferedWriter fasttextOut;

  public FastText(File modelPath, File binaryPath) throws IOException {
    fasttextProcess = new ProcessBuilder(binaryPath.getPath(), "predict-prob", modelPath.getPath(), "-", "" + K_HIGHEST_SCORES).start();
    fasttextIn = new BufferedReader(new InputStreamReader(fasttextProcess.getInputStream(), StandardCharsets.UTF_8));
    fasttextOut = new BufferedWriter(new OutputStreamWriter(fasttextProcess.getOutputStream(), StandardCharsets.UTF_8));
  }

  // for tests only
  FastText() {
    fasttextProcess = null;
    fasttextIn = null;
    fasttextOut = null;
  }

  public Map<String, Double> runFasttext(String text, List<String> additionalLanguageCodes) throws IOException {
    String joined = text.replace("\n", " ");
    String buffer;
    synchronized (this) {
      fasttextOut.write(joined);
      fasttextOut.newLine();
      fasttextOut.flush();
      buffer = fasttextIn.readLine();
      if (buffer == null) {
        // hack to see if this helps us debug the rare case of readLine() returning null:
        try {
          logger.warn("fasttextIn.readLine() returned null, trying again after short delay");
          Thread.sleep(10);
          buffer = fasttextIn.readLine();
          if (buffer == null) {
            logger.warn("fasttextIn.readLine() returned null again");
          }
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    }
    return parseBuffer(buffer, additionalLanguageCodes);
  }

  @NotNull
  Map<String, Double> parseBuffer(String buffer, List<String> additionalLanguageCodes) {
    String[] values = buffer.split(" ");
    if (values.length % 2 != 0) {
      logger.error("Error while parsing fasttext output '{}'", buffer);
      throw new RuntimeException("Error while parsing fasttext output: " + buffer);
    }
    if (!buffer.startsWith("__label__")) {
      logger.error("FastText output is expected to start with '__label__', will continue anyway: '{}'", buffer);
    }
    Map<String, Double> probabilities = new HashMap<>();
    for (int i = 0; i < values.length; i += 2) {
      String lang = values[i];
      String langCode = lang.substring(lang.lastIndexOf("__") + 2);
      String prob = values[i + 1];
      Double probValue = Double.parseDouble(prob);
      if (canLanguageBeDetected(langCode, additionalLanguageCodes)) {
        probabilities.put(langCode, probValue);
      }
    }
    return probabilities;
  }

  void destroy() {
    fasttextProcess.destroy();
  }

}
