/* LanguageTool, a natural language style checker
 * Copyright (C) 2011 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.openoffice;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.languagetool.JLanguageTool;

import com.sun.star.lang.Locale;
import com.sun.star.linguistic2.DictionaryType;
import com.sun.star.linguistic2.XDictionary;
import com.sun.star.linguistic2.XDictionaryEntry;
import com.sun.star.linguistic2.XSearchableDictionaryList;
import com.sun.star.uno.XComponentContext;

/**
 * Class to add manual LT dictionaries temporarily to LibreOffice/OpenOffice
 * @since 5.0
 * @author Fred Kruse
 */
public class LtDictionary {
  
  private final static String INTERNAL_DICT_PREFIX = "__LT_";
  private final static String[] ADD_ALL = { "e", "er", "es", "en", "em" };
  private final static int MAX_DICTIONARY_SIZE = 30000;
  private final static int NUM_PATHS = 7;
  
  private static boolean debugMode; //  should be false except for testing

  private Set<String> dictionaryList = new HashSet<>();
  private XDictionary listIgnoredWords = null;
  
  public LtDictionary() {
    debugMode = OfficeTools.DEBUG_MODE_LD;
  }

  /**
   * Add a non permanent dictionary to LO/OO that contains additional words defined in LT
   */
  public boolean setLtDictionary(XComponentContext xContext, Locale locale, LinguisticServices linguServices) {
    XSearchableDictionaryList searchableDictionaryList = OfficeTools.getSearchableDictionaryList(xContext);
    if (searchableDictionaryList == null) {
      MessageHandler.printToLogFile("LtDictionary: setLtDictionary: searchableDictionaryList == null");
      return false;
    }
    if (listIgnoredWords == null) {
      XDictionary[] dictionaryList = searchableDictionaryList.getDictionaries();
      listIgnoredWords = dictionaryList[dictionaryList.length - 1];
      if (debugMode) {
        MessageHandler.printToLogFile("dictionary for ignored words found: " + listIgnoredWords.getName());
      }
    }
    String shortCode = OfficeTools.localeToString(locale);
    String dictionaryNamePrefix = INTERNAL_DICT_PREFIX + shortCode + "_internal";
    String dictionaryName = dictionaryNamePrefix + "1.dic";
//    String dictionaryName = "LT_Spelling_" + shortCode + ".dic";
//    MessageHandler.printToLogFile("dictionary name: " + dictionaryName);
    if (!dictionaryList.contains(dictionaryName) && searchableDictionaryList.getDictionaryByName(dictionaryName) == null) {
      dictionaryList.add(dictionaryName);
      String ltDictionaryPath = getLTDictionaryFile(locale, linguServices);
      if (ltDictionaryPath != null) {
        if (debugMode) {
          MessageHandler.printToLogFile("setLtDictionary: Create LT spelling dictionary " + dictionaryName + ": File path: " + ltDictionaryPath);
        } else {
          MessageHandler.printToLogFile("setLtDictionary: Create LT spelling dictionary " + dictionaryName);
        }
        long startTime = System.currentTimeMillis();
        List<String> words = getSpellingWordsAsLines(ltDictionaryPath);
        int count = 0;
        int dictNum = 1;
        int dictCount = 0;
        XDictionary manualDictionary = searchableDictionaryList.createDictionary(dictionaryName, locale, DictionaryType.POSITIVE, "");
        if (debugMode) {
          MessageHandler.printToLogFile("Add " + words.size() + " words to " + dictionaryName);
        }
        for (String word : words) {
          manualDictionary.add(word, false, "");
          count++;
          if (count >= MAX_DICTIONARY_SIZE) {
            manualDictionary.setActive(true);
            searchableDictionaryList.addDictionary(manualDictionary);
            dictCount += manualDictionary.getCount();
            dictNum++;
            dictionaryName = dictionaryNamePrefix + dictNum + ".dic";
            manualDictionary = searchableDictionaryList.createDictionary(dictionaryName, locale, DictionaryType.POSITIVE, "");
            count = 0;
          }
        }
        manualDictionary.setActive(true);
        searchableDictionaryList.addDictionary(manualDictionary);
        dictCount += manualDictionary.getCount();
        long endTime = System.currentTimeMillis();
        MessageHandler.printToLogFile("Internal LT dicitionary for language " + shortCode + " added: Number of words = " + dictCount);
        MessageHandler.printToLogFile("Time to generate dictionary: " + (endTime - startTime));
        if (debugMode) {
          for (XDictionaryEntry entry : manualDictionary.getEntries()) {
            MessageHandler.printToLogFile(entry.getDictionaryWord());
          }
        }
        return true;
      }
    }
//    MessageHandler.printToLogFile("dictionary name " + dictionaryName + " is in list");
    return false;
  }
  
  private String getSpellingFilePath(Locale locale, int i) {
    if (i == 0) {
      return "/" + locale.Language + "/spelling.txt";
    } else if (i == 1) {
      return "/" + locale.Language + "/spelling/spelling.txt";
    } else if (i == 2) {
      return "/" + locale.Language + "/hunspell/spelling.txt";
    } else if (i == 3) {
      return "/" + locale.Language + "/hunspell/spelling-" + locale.Language + "-" + locale.Country + ".txt";
    } else if (i == 4) {
      return "/" + locale.Language + "/hunspell/spelling-" + locale.Language + "_" + locale.Country + ".txt";
    } else if (i == 5) {
      return "/" + locale.Language + "/hunspell/spelling_merged.txt";
    } else {
      return "/" + locale.Language + "/hunspell/spelling_custom.txt";
    }
  }
  
  /**
   * get the list of words out of spelling.txt files defined by LT
   */
  private Set<String> getManualWordList(Locale locale, LinguisticServices linguServices) {
    Set<String> words = new HashSet<>();
    String path;
    for (int i = 0; i < NUM_PATHS; i++) {
      path = getSpellingFilePath(locale, i);
      if (JLanguageTool.getDataBroker().resourceExists(path)) {
        List<String> lines = JLanguageTool.getDataBroker().getFromResourceDirAsLines(path);
        if (lines != null) {
          for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith("#")) {
              String[] lineWords = line.trim().split("#");
              lineWords = lineWords[0].trim().split("/");
              lineWords[0] = lineWords[0].replaceAll("_","");
              if (!lineWords[0].isEmpty()) {
                words.add(lineWords[0]);
                if (lineWords.length > 1) {
                  lineWords[1] = lineWords[1].trim();
                  for (int n = 0; n < lineWords[1].length(); n++) {
                    if (lineWords[1].charAt(n) == 'A') {
                      for (String add : ADD_ALL) {
                        String word = lineWords[0] + add;
                        words.add(word);
                      }
                    } else {
                      String word = lineWords[0];
                      if (lineWords[1].charAt(n) == 'E') {
                        word += "e";
                      } else if (lineWords[1].charAt(n) == 'S') {
                        word += "s";
                      } else if (lineWords[1].charAt(n) == 'N') {
                        word += "n";
                      } else if (lineWords[1].charAt(n) == 'F') {
                        word += "in";
                      }
                      words.add(word);
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    return words;
  }
  
  /**
   * get the list of words out of LT full spelling list
   */
  public List<String> getSpellingWordsAsLines(String path) {
    List<String> lines = new ArrayList<>();
    try (InputStream stream = new FileInputStream(path);
         InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
         BufferedReader br = new BufferedReader(reader)
        ) {
      String line;
      while ((line = br.readLine()) != null) {
        lines.add(line);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return lines;
  }
  
  /**
   * get the LT dictionary file from LT installation directory
   * create the dictionary file, if not exist
   */
  private String getLTDictionaryFile(Locale locale, LinguisticServices linguServices) {
    if (debugMode) {
      MessageHandler.printToLogFile("getLTDictionaryFile: start generate full LT dictionary file");
    }
    long startTime = System.currentTimeMillis();
    URL url = null;
    for (int i = 0; i < NUM_PATHS && url == null; i++) {
      url = JLanguageTool.getDataBroker().getAsURL(JLanguageTool.getDataBroker().getResourceDir() + getSpellingFilePath(locale, i));
    }
    if (url == null) {
      MessageHandler.printToLogFile("No LT spelling file found for " + OfficeTools.localeToString(locale));
      return null;
    }
    try {
      URI uri = url.toURI();
      File file = new File(uri.getPath());
      File dictionary = new File(file.getParent(), "LT_Spelling_"  + OfficeTools.localeToString(locale) + ".dic");
      if (dictionary.exists() && !dictionary.isDirectory()) {
        return dictionary.getAbsolutePath();
      }
      String path = dictionary.getPath();
      if (debugMode) {
        MessageHandler.printToLogFile("getLTDictionaryFile: LT dictionary file " + path + " doesn't exist: start to create");
      } else {
        MessageHandler.printToLogFile("getLTDictionaryFile: LT dictionary file doesn't exist: start to create");
      }
      Set<String> words = getManualWordList(locale, linguServices);
      try (OutputStream stream = new FileOutputStream(path);
          OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
          BufferedWriter br = new BufferedWriter(writer)
          ) {
        for (String word : words) {
          writer.write(word + "\n");
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      long endTime = System.currentTimeMillis();
      if (debugMode) {
        MessageHandler.printToLogFile(words.size() + "written to file " + path);
      }
      MessageHandler.printToLogFile("Time to generate LT spelling file: " + (endTime - startTime));
      return dictionary.getAbsolutePath();
    } catch (Throwable t) {
      MessageHandler.printException(t);
    }
    return null;
  }
  
  /**
   * Remove the non permanent LT dictionaries
   */
  public boolean removeLtDictionaries(XComponentContext xContext) {
    if (!dictionaryList.isEmpty()) {
      XSearchableDictionaryList searchableDictionaryList = OfficeTools.getSearchableDictionaryList(xContext);
      if (searchableDictionaryList == null) {
        MessageHandler.printToLogFile("LtDictionary: removeLtDictionaries: searchableDictionaryList == null");
        return false;
      }
      for (String dictionaryName : dictionaryList) {
        XDictionary manualDictionary = searchableDictionaryList.getDictionaryByName(dictionaryName);
        if (manualDictionary != null) {
          searchableDictionaryList.removeDictionary(manualDictionary);
        }
      }
      dictionaryList.clear();
      return true;
    }
    return false;
  }
  
  /**
   * Add a word to the List of ignored words
   * Used for ignore all in spelling check
   */
  public void addIgnoredWord(String word) {
    listIgnoredWords.add(word, false, "");
  }
  
  /**
   * Remove a word from the List of ignored words
   * Used for ignore all in spelling check
   */
  public void removeIgnoredWord(String word) {
    listIgnoredWords.remove(word);
  }
  
  /**
   * Add a word to a user dictionary
   */
  public void addWordToDictionary(String dictionaryName, String word, XComponentContext xContext) {
    XSearchableDictionaryList searchableDictionaryList = OfficeTools.getSearchableDictionaryList(xContext);
    if (searchableDictionaryList == null) {
      MessageHandler.printToLogFile("LtDictionary: addWordToDictionary: searchableDictionaryList == null");
      return;
    }
    XDictionary dictionary = searchableDictionaryList.getDictionaryByName(dictionaryName);
    dictionary.add(word, false, "");
  }
  
  /**
   * Add a word to a user dictionary
   */
  public void removeWordFromDictionary(String dictionaryName, String word, XComponentContext xContext) {
    XSearchableDictionaryList searchableDictionaryList = OfficeTools.getSearchableDictionaryList(xContext);
    if (searchableDictionaryList == null) {
      MessageHandler.printToLogFile("LtDictionary: removeWordFromDictionary: searchableDictionaryList == null");
      return;
    }
    XDictionary dictionary = searchableDictionaryList.getDictionaryByName(dictionaryName);
    dictionary.remove(word);
  }
  
  /**
   * Get all user dictionaries
   */
  public String[] getUserDictionaries(XComponentContext xContext) {
    XSearchableDictionaryList searchableDictionaryList = OfficeTools.getSearchableDictionaryList(xContext);
    if (searchableDictionaryList == null) {
      MessageHandler.printToLogFile("LtDictionary: getUserDictionaries: searchableDictionaryList == null");
      return null;
    }
    XDictionary[] dictionaryList = searchableDictionaryList.getDictionaries();
    if (listIgnoredWords == null) {
      listIgnoredWords = dictionaryList[dictionaryList.length - 1];
      if (debugMode) {
        MessageHandler.printToLogFile("dictionary for ignored words found: " + listIgnoredWords.getName());
      }
    }
    List<String> userDictionaries = new ArrayList<String>();
    for (XDictionary dictionary : dictionaryList) {
      if (dictionary.isActive()) {
        String name = dictionary.getName();
        if (!name.startsWith(INTERNAL_DICT_PREFIX) && !name.equals(listIgnoredWords.getName())) {
          userDictionaries.add(new String(name));
        }
      }
    }
    return userDictionaries.toArray(new String[userDictionaries.size()]);
  }
}
