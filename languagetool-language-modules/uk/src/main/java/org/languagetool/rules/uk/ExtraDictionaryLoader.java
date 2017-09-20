package org.languagetool.rules.uk;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

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

}
