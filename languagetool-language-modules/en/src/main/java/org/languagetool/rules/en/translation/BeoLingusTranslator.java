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

import org.jetbrains.annotations.NotNull;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.GlobalConfig;
import org.languagetool.Languages;
import org.languagetool.rules.translation.DataSource;
import org.languagetool.rules.translation.TranslationEntry;
import org.languagetool.rules.translation.Translator;
import org.languagetool.tagging.Tagger;
import org.languagetool.tools.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * German / English translator.
 * @since 4.9
 */
public class BeoLingusTranslator implements Translator {

  private static final Logger logger = LoggerFactory.getLogger(BeoLingusTranslator.class);
  private static final Pattern enUsPattern = Pattern.compile(".*?\\w+ \\[(Br|Am)\\.\\] ?/ ?\\w+ \\[(Br|Am)\\.\\].*");

  private static BeoLingusTranslator instance;

  // Source: https://www.cafe-lingua.de/englische-grammatik/verben-mit-to-infinitiv.php
  private static final Set<String> verbsWithTo = new HashSet<>(Arrays.asList("afford", "agree", "aim", "appear", "arrange",
    "attempt", "beg", "care", "choose", "claim", "condescend", "consent", "dare", "decide", "demand", "deserve",
    "determine", "endeavour", "expect", "fail", "forget", "guarantee", "happen", "have", "help", "hesitate", "hope",
    "learn", "long", "manage", "mean", "need", "neglect", "offer", "plan", "prepare", "pretend", "proceed", "promise",
    "refuse", "resolve", "seem", "stop", "swear", "tend", "threaten", "trouble", "undertake", "volunteer", "vow", "want", "wish"));

  private final Tagger tagger;
  private final Map<String,List<TranslationEntry>> de2en = new HashMap<>();
  private final Map<String,List<TranslationEntry>> en2de = new HashMap<>();
  private final Inflector inflector = new Inflector();

  public static synchronized BeoLingusTranslator getInstance(File beolingusFile) throws IOException {
    GlobalConfig config = new GlobalConfig();
    config.setBeolingusFile(beolingusFile);
    return getInstance(config);
  }

  public static synchronized BeoLingusTranslator getInstance(GlobalConfig globalConfig) throws IOException {
    if (instance == null && globalConfig != null && globalConfig.getBeolingusFile() != null) {
      long t1 = System.currentTimeMillis();
      logger.info("Init dict from " + globalConfig.getBeolingusFile() + "...");
      instance = new BeoLingusTranslator(globalConfig.getBeolingusFile());
      long t2 = System.currentTimeMillis();
      logger.info("Init dict done (" + (t2-t1) + "ms) - loaded " + instance.getDeEnSize() + " de -> en items.");
    }
    return instance;
  }

  public BeoLingusTranslator(File file) throws IOException {
    tagger = Languages.getLanguageForShortCode("de").getTagger();
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
        //handleItem(en2de, englishParts, germanParts, i, englishParts[i]);  -- direction not supported yet
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
  public List<String> split(String s) {
    List<String> parts = splitAtSemicolon(s);
    List<String> newParts = new ArrayList<>();
    for (String part : parts) {
      if (enUsPattern.matcher(part).matches()) {
        String variant1 = part.replaceFirst("^(.*?)(\\w+) (\\[(?:Br|Am)\\.\\]) ?/ ?\\w+ \\[(?:Br|Am)\\.\\](.*)", "$1$2$4 $3");
        String variant2 = part.replaceFirst("^(.*?)(\\w+) (\\[(?:Br|Am)\\.\\]) ?/ ?(\\w+) (\\[(?:Br|Am)\\.\\])(.*)", "$1$4$6 $5");
        newParts.add(variant1);
        newParts.add(variant2);
      } else {
        newParts.add(part);
      }
    }
    return newParts;
  }

  int getDeEnSize() {
    return de2en.size();
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
    Set<TranslationEntry> entriesSet = new HashSet<>();
    if (entries != null) {
      entriesSet.addAll(entries);
    }
    List<TranslationEntry> translationsForBaseforms = getTranslationsForBaseforms(term, map);
    for (TranslationEntry trans : translationsForBaseforms) {
      if (entries != null) {
        Optional<TranslationEntry> first = entries.stream().filter(k -> k.getL1().equals(trans.getL1())).findFirst();
        if (first.isPresent() && first.get().getL1().equals(trans.getL1())) {
          // skip duplicates
        } else {
          entriesSet.add(trans);
        }
      } else {
        entriesSet.add(trans);
      }
    }
    List<TranslationEntry> sortedList = new ArrayList<>(entriesSet);
    Collections.sort(sortedList, (t1, t2) -> Integer.compare(t2.getItemCount(), t1.getItemCount()));
    return sortedList;
  }

  @NotNull
  private List<TranslationEntry> getTranslationsForBaseforms(String term, Map<String, List<TranslationEntry>> map) {
    List<TranslationEntry> result = new ArrayList<>();
    try {
      List<AnalyzedTokenReadings> readings = tagger.tag(Collections.singletonList(term));
      readings.addAll(tagger.tag(Collections.singletonList(StringTools.uppercaseFirstChar(term))));  // user can spell German noun lowercase here
      for (AnalyzedTokenReadings reading : readings) {
        List<AnalyzedToken> aTokens = reading.getReadings();
        addResultsForTokens(map, aTokens, result);
      }
      return result;
    } catch (IOException e) {
      throw new RuntimeException("Could not tag '" + term + "'", e);
    }
  }

  private void addResultsForTokens(Map<String, List<TranslationEntry>> map, List<AnalyzedToken> aTokens, List<TranslationEntry> result) {
    for (AnalyzedToken aToken : aTokens) {
      String lemma = aToken.getLemma();
      if (lemma != null) {
        List<TranslationEntry> tmp = map.get(lemma.toLowerCase());
        if (tmp != null) {
          for (TranslationEntry tmpEntry : tmp) {
            if (!result.contains(tmpEntry)) {
              TranslationEntry entry = cleanTranslationEntry(tmpEntry, aToken);
              if (entry != null) {
                result.add(entry);
              }
            }
          }
        }
      }
    }
  }

  private TranslationEntry cleanTranslationEntry(TranslationEntry tmpEntry, AnalyzedToken aToken) {
    List<String> l = new ArrayList<>();
    for (String s : tmpEntry.getL2()) {
      List<String> inflected = inflector.inflect(cleanTranslationForReplace(s, null), aToken.getPOSTag());
      for (String inflectedForm : inflected) {
        String cleanEntry = inflectedForm + " " + getTranslationSuffix(s);
        l.add(cleanEntry.trim());
      }
    }
    return l.size() > 0 ? new TranslationEntry(tmpEntry.getL1(), l, tmpEntry.getItemCount()) : null;
  }

  private String cleanForLookup(String s) {
    return s.replaceAll("\\{.*?\\}", "")
      .replaceAll("\\[.*?\\]", "")
      .replaceAll("\\(.*?\\)", "")
      .replaceAll("/.*?/\\b", "")   // abbreviations, e.g. "oder {conj} /o.; od./"
      .replace("jdn./etw. ", "")
      .replace("jdm./etw. ", "")
      .replace("etw./jdn. ", "")
      .replace("etw./jdm. ", "")
      .replace("etw. ", "")
      .replace("jdn. ", "")
      .replace("jdm. ", "")
      .trim()
      .toLowerCase();
  }

  @Override
  public String cleanTranslationForReplace(String s, String prevWord) {
    String clean = s
      .replaceAll("\\[.*?\\]", "")   // e.g. "[coll.]", "[Br.]"
      .replaceAll("\\{.*?\\}", "")   // e.g. "to go {went; gone}"
      .replaceAll("\\(.*?\\)", "")   // e.g. "icebox (old-fashioned)"
      .replace("sth./sb.", "")
      .replace("sb./sth.", "")
      .replace("sth.", "")
      .replace("sb.", "")
      .replaceAll("/[A-Z]+/", "")    // e.g. "heavy goods vehicle /HGV/"
      .trim();
    if ("to".equals(prevWord) && clean.startsWith("to ")) {
      return clean.substring(3);
    }
    if (!"to".equals(prevWord) && clean.startsWith("to ") && !verbsWithTo.contains(prevWord)) {
      return clean.substring(3);
    }
    return clean;
  }

  @Override
  public String getMessage() {
    return "Translate to English?";
  }

  @Override
  public String getTranslationSuffix(String s) {
    StringBuilder sb = new StringBuilder();
    List<String> lookingFor = new ArrayList<>();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '[') {
        lookingFor.add("]");
      } else if (c == ']' && lookingFor.contains("]")) {
        sb.append(c);
        sb.append(' ');
        lookingFor.remove("]");
      } else if (c == '(') {
        lookingFor.add(")");
      } else if (c == ')') {
        sb.append(c);
        sb.append(' ');
        lookingFor.remove(")");
      } else if (c == '{') {
        lookingFor.add("}");
      } else if (c == '}') {
        sb.append(c);
        sb.append(' ');
        lookingFor.remove("}");
      }
      if (lookingFor.size() > 0) {
        sb.append(c);
      }
    }
    return sb.toString().trim();
  }

  @Override
  public DataSource getDataSource() {
    return new DataSource("https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html", "BEOLINGUS", "http://dict.tu-chemnitz.de");
  }

}
