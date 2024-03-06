/* LanguageTool, a natural language style checker
 * Copyright (C) 2024 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.server;

import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.tools.ConfidenceKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

class ConfidenceMapLoader {

  private static final Logger logger = LoggerFactory.getLogger(ConfidenceMapLoader.class);

  Map<ConfidenceKey,Float> load(File filePattern) throws IOException {
    if (!filePattern.toString().contains("{lang}")) {
      throw new RuntimeException("The 'ruleIdToConfidenceFile' parameter must contain '{lang}' as a placeholder for the language code");
    }
    Map<ConfidenceKey,Float> confMap = new HashMap<>();
    for (Language lang : Languages.get()) {
      int loadCount = 0;
      String fileName = filePattern.getAbsolutePath().replace("{lang}", lang.getShortCode());
      if (!Paths.get(fileName).toFile().exists()) {
        continue;
      }
      List<String> lines = Files.readAllLines(Paths.get(fileName), UTF_8);
      for (String line : lines) {
        if (line.startsWith("#")) {
          continue;
        }
        String[] parts = line.split(",");
        if (parts.length >= 2) {   // there might be more columns for better debugging, but we don't use them here
          try {
            float confidence = Float.parseFloat(parts[1]);
            confMap.put(new ConfidenceKey(lang, parts[0]), confidence);
            loadCount++;
          } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid confidence float value in " + fileName + ", expected 'RULE_ID,float_value[,...]': " + line);
          }
        } else {
          throw new RuntimeException("Invalid line in " + fileName + ", expected 'RULE_ID,float_value[,...]': " + line);
        }
      }
      logger.info("Loaded " + loadCount + " mappings for " + lang + " from confidence map for rules from " + fileName);
    }
    if (confMap.size() == 0) {
      throw new RuntimeException("No confidence values could be loaded for " + filePattern +
        " -- please check there are actually files that match this pattern");
    }
    return confMap;
  }

}
