package org.languagetool.rules.de;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;

public class CompoundCheckFilter  extends RuleFilter {
  
  private static final String FILE_ENCODING = "utf-8";
  private static final Map<String, List<String>> relevantWords = 
      loadWords("/de/addedCompound.txt");

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
      AnalyzedTokenReadings[] patternTokens) throws IOException {
    
    String part1 = arguments.get("part1").toLowerCase();
    String part2 = arguments.get("part2").toLowerCase();
    
    if (relevantWords.containsKey(part1)) {
      if (relevantWords.get(part1).contains(part2)) {
        return match;
      }
    }
    return null;
  }
  
  static Map<String, List<String>> loadWords(String path) {
    final Map<String, List<String>> map = new HashMap<>();
    final InputStream inputStream = JLanguageTool.getDataBroker().getFromRulesDirAsStream(path);
    try (Scanner scanner = new Scanner(inputStream, FILE_ENCODING)) {
      while (scanner.hasNextLine()) {
        final String line = scanner.nextLine().replaceAll("#.*$", "").trim(); // replace comments
        if (line.isEmpty()) {  
          continue;
        }
        final String[] parts = line.split(";");
        if (parts.length != 2) {
          throw new RuntimeException("Format error in file " + path + ", line: "
                  + line + ", " + "expected 2 semicolon-separated parts, got "
                  + parts.length);
        }
        String part0 = parts[0].trim().toLowerCase();
        String part1 = parts[1].trim().toLowerCase();
        if (!map.containsKey(part0)) {
          map.put(part0, Collections.singletonList(part1));
        } else {
          List<String> l = new ArrayList<>();
          l.addAll(map.get(part0));
          l.add(part1);
          map.replace(part0, l);
        }
      }
    }
    return map;
  }

}
