package org.languagetool.rules.uk;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;

import org.languagetool.JLanguageTool;

/**
 * Loads extra helper dictionaries in plain text format
 * @since 2.9
 */
public class ExtraDictionaryLoader {

  public static Set<String> loadSet(String path) {
    Set<String> result = new HashSet<>();
    try (InputStream is = JLanguageTool.getDataBroker().getFromResourceDirAsStream(path);
         Scanner scanner = new Scanner(is, "UTF-8")) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        if( ! line.startsWith("#") ) {
          result.add(line);
        }
      }
      return result;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Map<String, List<String>> loadSpacedLists(String path) {
    Map<String, List<String>> result = new HashMap<>();
    try (InputStream is = JLanguageTool.getDataBroker().getFromResourceDirAsStream(path);
         Scanner scanner = new Scanner(is, "UTF-8")) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        if( ! line.startsWith("#") && ! line.trim().isEmpty() ) {
          line = line.replaceFirst("#.*", "").trim();
          String[] split = line.split(" |\\|");
          List<String> list = Arrays.asList(split).subList(1, split.length);
          result.put(split[0], list);
        }
      }
      return result;
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public static Map<String, List<String>> loadLists(String path) {
    Map<String, List<String>> result = new HashMap<>();
    try (InputStream is = JLanguageTool.getDataBroker().getFromRulesDirAsStream(path);
         Scanner scanner = new Scanner(is, "UTF-8")) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
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
