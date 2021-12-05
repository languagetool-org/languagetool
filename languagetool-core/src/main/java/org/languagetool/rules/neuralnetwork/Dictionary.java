/* LanguageTool, a natural language style checker
 * Copyright (C) 2017 Markus Brenneis
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
package org.languagetool.rules.neuralnetwork;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Dictionary extends HashMap<String, Integer> {

  private HashMap<String, HashMap<Integer, String>> advancedDict;

  public Dictionary(InputStream filePath) {
    List<String> rows = ResourceReader.readAllLines(filePath);
    fromString(rows.get(0));
  }

  public Dictionary(String dict) {
    fromString(dict);
  }

  /**
   * Constructor aimed to add extra fields to the dictionary structure,
   * including both word frequency and flags
   * @param dict  List of words and their frequency in dictionary format
   * @param flags A list of comma separated flags for each word in the dictionary.
   *              The reason it is a comma-d list is that a word can have more than one flag.
   */
  public Dictionary(String dict, List<String> flags) throws Exception {
    // Issue here: https://github.com/languagetool-org/languagetool/issues/5609
    // word, (frequency, flags)
    advancedDict = new HashMap<>();
    HashMap<String, Integer> innerMap = advancedFromString(dict);
    try {
      int flagsIndex = 0;
      for (String key : innerMap.keySet()) {
        int finalFlagsIndex = flagsIndex;
        advancedDict.put(key, new HashMap<Integer, String>() {{
          put(innerMap.get(key), flags.get(finalFlagsIndex));
        }});
        flagsIndex++;
      }
    } catch (Exception e) {
      throw new Exception("The number of flags is smaller than the number of unique words." +
        " Please make sure they are equal.");
    }
  }

  /**
   * Standard getter method for advanced dictionary
   * @return  The contents of advancedDict
   */
  public HashMap<String, HashMap<Integer, String>> getAdvancedDict() {
    // Issue here: https://github.com/languagetool-org/languagetool/issues/5609
    return advancedDict;
  }

  /**
   * Getter method for retrieving specific values from advanced dictionary
   * @return  The values for specified String key
   */
  public HashMap<Integer, String> getAdvancedDictInfo(String key) {
    // Issue here: https://github.com/languagetool-org/languagetool/issues/5609
    return advancedDict.get(key);
  }

  /**
   * Same logic as fromString void statement, only this time for advanced dictionary
   * @param maps  List of words and their frequency in dictionary format
   * @return  A HashMap of Strings and their frequency converted from String parameter
   */
  private HashMap<String, Integer> advancedFromString(String maps) {
    HashMap<String, Integer> temp = new HashMap<String, Integer>();
    maps = maps.substring(1, maps.length() - 1);
    for (String entry : maps.split(", ")) {
      String[] kv = entry.split(": ");
      temp.put(kv[0].substring(1, kv[0].length() - 1), Integer.parseInt(kv[1]));
    }
    return temp;
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
      return get("UNK");
    }
  }

}
