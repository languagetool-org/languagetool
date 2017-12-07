package org.languagetool.rules.neuralnetwork;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

public class Dictionary extends HashMap<String, Integer> {

  public Dictionary(InputStream filePath) {
    List<String> rows = ResourceReader.readAllLines(filePath);
    fromString(rows.get(0));
  }

  public Dictionary(String dict) {
    fromString(dict);
  }

  private void fromString(String maps) {
    maps = maps.substring(1, maps.length() - 1);
    for (String entry : maps.split(", ")) {
      String[] kv = entry.split(": ");
      put(kv[0].substring(1, kv[0].length() - 1), Integer.parseInt(kv[1]));
    }
  }

  Integer safeGet(String key) {
    if (containsKey(key)) {
      return get(key);
    } else {
      System.out.println(key + " unknown");
      return get("UNK");
    }
  }

}
