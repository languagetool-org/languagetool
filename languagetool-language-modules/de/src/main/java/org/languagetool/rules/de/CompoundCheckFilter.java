package org.languagetool.rules.de;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.tools.MostlySingularMultiMap;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;

public class CompoundCheckFilter  extends RuleFilter {
  
  private static final String FILE_ENCODING = "utf-8";
  private static final MostlySingularMultiMap<String, String> relevantWords = loadWords("/de/addedCompound.txt");

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
      AnalyzedTokenReadings[] patternTokens) throws IOException {
    
    String part1 = arguments.get("part1").toLowerCase();
    String part2 = arguments.get("part2").toLowerCase();

    List<String> list = relevantWords.getList(part1);
    return list != null && list.contains(part2) ? match : null;
  }
  
  private static MostlySingularMultiMap<String, String> loadWords(String path) {
    Map<String, String> interned = new HashMap<>();
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
        String part0 = interned.computeIfAbsent(parts[0].trim().toLowerCase(), Function.identity());
        String part1 = interned.computeIfAbsent(parts[1].trim().toLowerCase(), Function.identity());
        map.computeIfAbsent(part0, __ -> new ArrayList<>()).add(part1);
      }
    }
    return new MostlySingularMultiMap<>(map);
  }

}
