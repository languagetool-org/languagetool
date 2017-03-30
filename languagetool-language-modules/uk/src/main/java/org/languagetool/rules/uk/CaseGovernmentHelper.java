package org.languagetool.rules.uk;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;

public class CaseGovernmentHelper {

  static final Map<String, Set<String>> CASE_GOVERNMENT_MAP = loadMap("/uk/case_government.txt");

  private static Map<String, Set<String>> loadMap(String path) {
    Map<String, Set<String>> result = new HashMap<>();
    try (InputStream is = JLanguageTool.getDataBroker().getFromResourceDirAsStream(path);
        Scanner scanner = new Scanner(is, "UTF-8")) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        String[] parts = line.split(" ");
        String[] vidm = parts[1].split(":");
        result.put(parts[0], new HashSet<>(Arrays.asList(vidm)));
      }
      //        System.err.println("Found case governments: " + result.size());
      return result;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static boolean hasCaseGovernment(AnalyzedTokenReadings analyzedTokenReadings, String rvCase) {
    for(AnalyzedToken token: analyzedTokenReadings.getReadings()) {
      if( CASE_GOVERNMENT_MAP.containsKey(token.getLemma())
          && CASE_GOVERNMENT_MAP.get(token.getLemma()).contains(rvCase) )
        return true;
    }
    return false;
  }

}
