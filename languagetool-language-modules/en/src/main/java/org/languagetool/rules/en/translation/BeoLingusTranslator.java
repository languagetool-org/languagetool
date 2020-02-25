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
package org.languagetool.rules.en.translation;

import org.languagetool.GlobalConfig;
import org.languagetool.rules.translation.DataSource;
import org.languagetool.rules.translation.TranslationEntry;
import org.languagetool.rules.translation.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @since 4.9
 */
public class BeoLingusTranslator implements Translator {

  private static final Logger logger = LoggerFactory.getLogger(BeoLingusTranslator.class);
  private static final Pattern enUsPattern = Pattern.compile(".*?\\w+ \\[(Br|Am)\\.\\]/\\w+ \\[(Br|Am)\\.\\].*");

  private static BeoLingusTranslator instance;

  private final Map<String,List<TranslationEntry>> de2en = new HashMap<>();
  private final Map<String,List<TranslationEntry>> en2de = new HashMap<>();

  static synchronized public BeoLingusTranslator getInstance(GlobalConfig globalConfig) throws IOException {
    if (instance == null && globalConfig != null && globalConfig.getBeolingusFile() != null) {
      long t1 = System.currentTimeMillis();
      logger.info("Init dict from " + globalConfig.getBeolingusFile() + "...");
      instance = new BeoLingusTranslator(globalConfig.getBeolingusFile());
      long t2 = System.currentTimeMillis();
      logger.info("Init dict done (" + (t2-t1) + "ms).");
    }
    return instance;
  }
  
  private BeoLingusTranslator(File file) throws IOException {
    List<String> lines = Files.readAllLines(file.toPath());
    for (String line : lines) {
      if (line.trim().isEmpty() || line.startsWith("#")) {
        continue;
      }
      String[] parts = line.split(" :: ");
      String german = parts[0];
      String english = parts[1];
      String[] germanParts = german.split("\\|");
      String[] englishParts = english.split("\\|");
      if (germanParts.length != englishParts.length) {
        throw new IOException("Invalid line format: " + line);
      }
      int i = 0;
      for (String germanPart : germanParts) {
        handleItem(de2en, germanParts, englishParts, i, germanPart);
        handleItem(en2de, englishParts, germanParts, i, englishParts[i]);
        i++;
      }
    }
  }

  private void handleItem(Map<String, List<TranslationEntry>> map, String[] germanParts, String[] englishParts, int i, String germanPart) {
    germanPart = germanPart.replaceAll("/.*?/", "");    // e.g. "oder {conj} /o.; od./"
    List<String> germanSubParts = split(germanPart);
    for (String germanSubPart : germanSubParts) {
      String key = cleanForLookup(germanSubPart);
      List<TranslationEntry> oldEntries = map.get(key);
      if (oldEntries != null) {
        oldEntries.add(new TranslationEntry(split(germanPart), split(englishParts[i].trim()), germanParts.length));
        map.put(key, oldEntries);
      } else {
        List<TranslationEntry> l = new ArrayList<>();
        l.add(new TranslationEntry(split(germanPart), split(englishParts[i]), germanParts.length));
        map.put(key, l);
      }
      //System.out.println(cleanForLookup(germanSubPart) + " ==> " + new TranslationEntry(split(germanPart), split(englishParts[i]), germanParts.length));
    }
  }

  // "tyre [Br.]/tire [Am.] pump" -> "tyre pump [Br.]" + "tire pump [Am.]"
  List<String> split(String s) {
    List<String> parts = splitAtSemicolon(s);
    List<String> newParts = new ArrayList<>();
    for (String part : parts) {
      if (enUsPattern.matcher(part).matches()) {
        String variant1 = part.replaceFirst("^(.*?)(\\w+) (\\[(?:Br|Am)\\.\\])/\\w+ \\[(?:Br|Am)\\.\\](.*)", "$1$2$4 $3");
        String variant2 = part.replaceFirst("^(.*?)(\\w+) (\\[(?:Br|Am)\\.\\])/(\\w+) (\\[(?:Br|Am)\\.\\])(.*)", "$1$4$6 $5");
        newParts.add(variant1);
        newParts.add(variant2);
      } else {
        newParts.add(part);
      }
    }
    return newParts;
  }

  // split input like "family doctors; family physicians" at ";", unless it's in "{...}":
  List<String> splitAtSemicolon(String s) {
    List<String> list = Arrays.stream(s.split(";\\s+")).map(k -> k.trim()).collect(Collectors.toList());
    List<String> mergedList = new ArrayList<>();
    int mergeListPos = 0;
    boolean merging = false;
    for (String item : list) {
      int openPos = item.indexOf("{");
      int closePos = item.indexOf("}");
      if (merging) {
        mergedList.set(mergeListPos-1, mergedList.get(mergeListPos-1) + "; " + item);
        mergeListPos--;
        if (closePos >= 0) {
          merging = false;
        }
      } else if (openPos > closePos) {
        // ";" inside "{...}" - merge those again
        mergedList.add(item);
        merging = true;
      } else {
        mergedList.add(item);
      }
      mergeListPos++;
    }
    return mergedList;
  }

  private String cleanForLookup(String s) {
    return s.replaceAll("\\{.*?\\}", "")
            .replaceAll("\\[.*?\\]", "")
            .replaceAll("\\(.*?\\)", "")
            .replaceAll("/.*?/\\b", "")   // abbreviations, e.g. "oder {conj} /o.; od./"
            .trim()
            .toLowerCase();
  }

  @Override
  public List<TranslationEntry> translate(String term, String fromLang, String toLang) {
    Map<String, List<TranslationEntry>> map;
    if (fromLang.equals("de") && toLang.equals("en")) {
      map = this.de2en;
    } else if (fromLang.equals("en") && toLang.equals("de")) {
      map = this.en2de;
    } else {
      throw new RuntimeException("Not supported: " + fromLang + " -> " + toLang);
    }
    List<TranslationEntry> entries = map.get(term.toLowerCase());
    if (entries == null) {
      return new ArrayList<>();
    }
    List<TranslationEntry> sortedList = new ArrayList<>(entries);
    Collections.sort(sortedList);
    return sortedList;
  }

  @Override
  public DataSource getDataSource() {
    return new DataSource("https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html", "BEOLINGUS", "http://dict.tu-chemnitz.de");
  }

}
