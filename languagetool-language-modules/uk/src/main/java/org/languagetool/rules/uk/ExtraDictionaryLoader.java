package org.languagetool.rules.uk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;

import org.languagetool.JLanguageTool;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Loads extra helper dictionaries in plain text format
 * @since 2.9
 */
public class ExtraDictionaryLoader {

  public static Set<String> loadSet(String path) {
    Set<String> result = new HashSet<>();
    List<String> lines = JLanguageTool.getDataBroker().getFromResourceDirAsLines(path);
    for (String line : lines) {
      if (!line.startsWith("#")) {
        result.add(line);
      }
    }
    return result;
  }

  public static Map<String, String> loadMap(String path) {
    Set<String> set = loadSet(path);
    return set.stream()
        .map(str -> str.trim().split(" "))
        .collect(Collectors.toMap(x -> x[0], x -> x.length > 1 ? x[1] : ""));
  }
  
  public static Map<String, List<String>> loadSpacedLists(String path) {
    Map<String, List<String>> result = new HashMap<>();
    List<String> lines = JLanguageTool.getDataBroker().getFromResourceDirAsLines(path);
    for (String line : lines) {
      if(!line.startsWith("#") && !line.trim().isEmpty()) {
        line = line.replaceFirst("#.*", "").trim();
        String[] split = line.split(" |\\|");
        List<String> list = Arrays.asList(split).subList(1, split.length);
        result.put(split[0], list);
      }
    }
    return result;
  }

  public static Map<String, List<String>> loadLists(String path) {
    Map<String, List<String>> result = new HashMap<>();
    try (InputStream is = JLanguageTool.getDataBroker().getFromRulesDirAsStream(path);
         BufferedReader reader = new BufferedReader(new InputStreamReader(is, UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if( ! line.startsWith("#") && ! line.trim().isEmpty() ) {
          String[] split = line.split(" *= *|\\|");
          List<String> list = Arrays.asList(split).subList(1, split.length);
          result.put(split[0], list);
        }
      }
      return result;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
