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
package org.languagetool.language.identifier.detector;

import org.jetbrains.annotations.NotNull;
import org.languagetool.language.identifier.LanguageIdentifierService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @since 5.0
 */
public class FastTextDetector {

  private static final Logger logger = LoggerFactory.getLogger(FastTextDetector.class);
  private static final int K_HIGHEST_SCORES = 5;
  private static final int BUFFER_SIZE = 4096;

  private Process fasttextProcess;
  private Reader fasttextIn;
  private Writer fasttextOut;
  
  private File modelPath;
  private File binaryPath;

  public static class FastTextException extends RuntimeException {
    private final boolean disabled;

    public FastTextException(String message, boolean disabled) {
      super(message);
      this.disabled = disabled;
    }

    /**
     * Should {@link FastTextDetector} be disable after this exception
    */
    public boolean isDisabled() {
      return disabled;
    }
  }

  public FastTextDetector(File modelPath, File binaryPath) throws IOException {
    this.modelPath = modelPath;
    this.binaryPath = binaryPath;
    init();
  }
  
  private void init() throws IOException{
    fasttextProcess = new ProcessBuilder(binaryPath.getPath(), "predict-prob", modelPath.getPath(), "-", "" + K_HIGHEST_SCORES).start();
    // avoid buffering, we want to flush/read all data immediately
    // might cause mixup
    fasttextIn = new InputStreamReader(fasttextProcess.getInputStream(), StandardCharsets.UTF_8);
    fasttextOut = new OutputStreamWriter(fasttextProcess.getOutputStream(), StandardCharsets.UTF_8);
  }

  // for tests only
  FastTextDetector() {
    fasttextProcess = null;
    fasttextIn = null;
    fasttextOut = null;
  }

  public Map<String, Double> runFasttext(String text, List<String> additionalLanguageCodes) throws IOException {
    String joined = text.replace("\n", " ").toLowerCase(Locale.ROOT);
    char[] cbuf = new char[BUFFER_SIZE];
    synchronized (this) {
      fasttextOut.write(joined + System.lineSeparator());
      fasttextOut.flush();
      long read = fasttextIn.read(cbuf);
      if (read <= 0) {
        // hack to see if this helps us debug the rare case of readLine() returning null:
        try {
          logger.warn("fasttextIn.read() returned no data, trying again after short delay");
          Thread.sleep(10);
          read = fasttextIn.read(cbuf);
          if (read == -1) {
            logger.warn("fasttextIn.read() returned no data again");
          }
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
      if (fasttextIn.ready()) {
        logger.warn("More input to read from Fasttext, this should not happen; language detection results might be mixed up");
      }
    }
    return parseBuffer(new String(cbuf), additionalLanguageCodes);
  }

  @NotNull
  Map<String, Double> parseBuffer(String buffer, List<String> additionalLanguageCodes) {
    String[] values = buffer.trim().split("\\s+");
    if (!buffer.startsWith("__label__")) {
      throw new FastTextException("FastText output is expected to start with '__label__': ''" + buffer + "'", true);
    }
    if (values.length % 2 != 0) {
      throw new FastTextException("Error while parsing fasttext output, expected pairs of '__label_xx' and float: '" + buffer + "'", true);
    }
    if (buffer.trim().contains("\n")) {
      logger.warn("Got multiple lines to read from Fasttext, this should not happen: '" + buffer + "'" );
    }
    Map<String, Double> probabilities = new HashMap<>();
    for (int i = 0; i < values.length; i += 2) {
      String lang = values[i];
      String langCode = lang.substring(lang.lastIndexOf("__") + 2);
      String prob = values[i + 1];
      Double probValue = Double.parseDouble(prob);
      if (LanguageIdentifierService.INSTANCE.canLanguageBeDetected(langCode, additionalLanguageCodes)) {
        probabilities.put(langCode, probValue);
      }
    }
    return probabilities;
  }

  public synchronized boolean restartProcess() throws IOException {
    try {
      runFasttext("This is a test text that should work.", Collections.emptyList());
    } catch (IOException | FastTextException e) {
      if (fasttextProcess != null && fasttextIn != null && fasttextOut != null) {
        this.fasttextProcess.destroy();
        this.fasttextIn.close();
        this.fasttextOut.close();
      }
      init();
      return true;
    }
    return false;
  }

  void destroy() {
    fasttextProcess.destroy();
  }

}
