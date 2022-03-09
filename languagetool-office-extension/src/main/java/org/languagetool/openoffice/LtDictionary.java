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

import java.util.ArrayList;
import java.util.List;

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
  
  private static boolean debugMode; //  should be false except for testing

  private List<String> dictionaryList = new ArrayList<>();
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
    }
    String shortCode = locale.Language;
    String dictionaryName = "__LT_" + shortCode + "_internal.dic";
    if (!dictionaryList.contains(dictionaryName)) {
      dictionaryList.add(dictionaryName);
      XDictionary manualDictionary = searchableDictionaryList.createDictionary(dictionaryName, locale, DictionaryType.POSITIVE, "");
      for (String word : getManualWordList(locale, linguServices)) {
        manualDictionary.add(word, false, "");
      }
      manualDictionary.setActive(true);
      searchableDictionaryList.addDictionary(manualDictionary);
      MessageHandler.printToLogFile("Internal LT dicitionary for language " + shortCode + " added: Number of words = " + manualDictionary.getCount());
      if (debugMode) {
        for (XDictionaryEntry entry : manualDictionary.getEntries()) {
          MessageHandler.printToLogFile(entry.getDictionaryWord());
        }
      }
      return true;
    }
    return false;
  }
  
  /**
   * get the list of words out of spelling.txt files defined by LT
   */
  private List<String> getManualWordList(Locale locale, LinguisticServices linguServices) {
    List<String> words = new ArrayList<>();
    String shortLangCode = locale.Language;
    String path;
    for (int i = 0; i < 4; i++) {
      if (i == 0) {
        path = "/" + shortLangCode + "/spelling.txt";
      } else if (i == 1) {
        path = "/" + shortLangCode + "/hunspell/spelling.txt";
      } else if (i == 2) {
        path = "/" + shortLangCode + "/hunspell/spelling-" + shortLangCode + "-" + locale.Country + ".txt";
      } else {
        path = "/" + shortLangCode + "/hunspell/spelling-" + shortLangCode + "_" + locale.Country + ".txt";
      }
      if (JLanguageTool.getDataBroker().resourceExists(path)) {
        List<String> lines = JLanguageTool.getDataBroker().getFromResourceDirAsLines(path);
        if (lines != null) {
          for (String line : lines) {
            if (!line.isEmpty() && !line.startsWith("#")) {
              String[] lineWords = line.trim().split("\\h");
              lineWords = lineWords[0].trim().split("/");
              lineWords[0] = lineWords[0].replaceAll("_","");
              if (!lineWords[0].isEmpty() && !words.contains(lineWords[0]) && !linguServices.isCorrectSpell(lineWords[0], locale)) {
                words.add(lineWords[0]);
              }
            }
          }
        }
      }
    }
    return words;
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
    }
    List<String> userDictionaries = new ArrayList<String>();
    for (XDictionary dictionary : dictionaryList) {
      if (dictionary.isActive()) {
        String name = dictionary.getName();
        if (!name.startsWith("__LT_") && !name.equals(listIgnoredWords.getName())) {
          userDictionaries.add(new String(name));
        }
      }
    }
    return userDictionaries.toArray(new String[userDictionaries.size()]);
  }
}
