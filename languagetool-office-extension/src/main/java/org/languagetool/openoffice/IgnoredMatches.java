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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.star.lang.Locale;

/**
 * class for store and handle ignored matches
 * @since 6.4
 * @author Fred Kruse
 */
public class IgnoredMatches {
  
  private final Map<Integer, Map<String, Set<Integer>>> ignoredMatches;
  private final Map<Integer, List<LocaleEntry>> spellLocales;
  
  public IgnoredMatches () {
    ignoredMatches = new HashMap<>();
    spellLocales = new HashMap<>();
  }

  public IgnoredMatches (Map<Integer, Map<String, Set<Integer>>> ignoredMatches, Map<Integer, List<LocaleEntry>> spellLocales) {
    this.ignoredMatches = new HashMap<>(ignoredMatches);
    this.spellLocales = new HashMap<>(spellLocales);
    MessageHandler.printToLogFile("IgnoredMatches: IgnoredMatches(1): spellLocales.size: " + spellLocales.size());
  }

  public IgnoredMatches (IgnoredMatches ignoredMatches) {
    this.ignoredMatches = new HashMap<>(ignoredMatches.ignoredMatches);
    this.spellLocales = new HashMap<>(ignoredMatches.spellLocales);
    MessageHandler.printToLogFile("IgnoredMatches: IgnoredMatches(2): spellLocales.size: " + spellLocales.size());
  }
  
  /**
   * Set an ignored match
   */
  public void setIgnoredMatch(int x, int y, String ruleId) {
    setIgnoredMatch(x, y, 0, ruleId, null, null);
  }
  
  public void setIgnoredMatch(int x, int y, int len, String ruleId, Locale locale, FlatParagraphTools flatPara) {
    Map<String, Set<Integer>> ruleAtX;
    Set<Integer> charNums;
    if (ignoredMatches.containsKey(y)) {
      ruleAtX = ignoredMatches.get(y);
      if (ruleAtX.containsKey(ruleId)) {
        charNums = ruleAtX.get(ruleId);
      } else {
        charNums = new HashSet<>();
      }
    } else {
      ruleAtX = new HashMap<String, Set<Integer>>();
      charNums = new HashSet<>();
    }
    charNums.add(x);
    ruleAtX.put(ruleId, charNums);
    ignoredMatches.put(y, ruleAtX);
    if (locale != null) {
      List<LocaleEntry> locales;
      if (!spellLocales.containsKey(y)) {
        locales = new ArrayList<>();
      } else {
        locales = spellLocales.get(y);
      }
      for (int i = locales.size() - 1; i >= 0; i--) {
        if (locales.get(i).start == x) {
          locales.remove(i);
        }
      }
      locales.add(new LocaleEntry(x, len, locale, ruleId));
      spellLocales.put(y, locales);
      removeSpellingMark(y, x, len, flatPara);
    }
  }
 
  /**
   * Remove an ignored matches in a paragraph
   */
  public void removeIgnoredMatches(int y, FlatParagraphTools flatPara) {
    if (ignoredMatches.containsKey(y)) {
      ignoredMatches.remove(y);
      if (spellLocales.containsKey(y)) {
        List<LocaleEntry> locales = spellLocales.get(y);
        for (LocaleEntry entry : locales) {
          resetLocale(y, entry.start, entry.length, entry.locale, flatPara);
        }
        spellLocales.remove(y);
      }
    }
  }
    
  /**
   * Remove an ignored matches of a special ruleID in a paragraph
   */
  public void removeIgnoredMatches(int y, String ruleId, FlatParagraphTools flatPara) {
    if (ignoredMatches.containsKey(y)) {
      Map<String, Set<Integer>> ruleAtX = ignoredMatches.get(y);
      if (ruleAtX.containsKey(ruleId)) {
        ruleAtX.remove(ruleId);
      }
      if (ruleAtX.isEmpty()) {
        ignoredMatches.remove(y);
      } else {
        ignoredMatches.put(y, ruleAtX);
      }
      if (spellLocales.containsKey(y)) {
        List<LocaleEntry> locales = spellLocales.get(y);
        for (int i = locales.size() - 1; i >= 0; i--) {
          LocaleEntry entry = locales.get(i);
          if (entry.ruleId.equals(ruleId)) {
            resetLocale(y, entry.start, entry.length, entry.locale, flatPara);
            locales.remove(i);
          }
        }
        spellLocales.put(y, locales);
      }
    }
  }
    
  /**
   * Remove one ignored match
   */
  public void removeIgnoredMatch(int x, int y, String ruleId, FlatParagraphTools flatPara) {
    if (ignoredMatches.containsKey(y)) {
      Map<String, Set<Integer>> ruleAtX = ignoredMatches.get(y);
      if (ruleAtX.containsKey(ruleId)) {
        Set<Integer> charNums = ruleAtX.get(ruleId);
        if (charNums.contains(x)) {
          charNums.remove(x);
          if (charNums.isEmpty()) {
            ruleAtX.remove(ruleId);
          } else {
            ruleAtX.put(ruleId, charNums);
          }
          if (ruleAtX.isEmpty()) {
            ignoredMatches.remove(y);
          } else {
            ignoredMatches.put(y, ruleAtX);
          }
        }
      }
      if (spellLocales.containsKey(y)) {
        List<LocaleEntry> locales = spellLocales.get(y);
        for (int i = locales.size() - 1; i >= 0; i--) {
          LocaleEntry entry = locales.get(i);
          if (entry.start == x && entry.ruleId.equals(ruleId)) {
            resetLocale(y, entry.start, entry.length, entry.locale, flatPara);
            locales.remove(i);
          }
        }
        spellLocales.put(y, locales);
      }
    }
  }

  /**
   * Is the match of a ruleID at a position ignored
   */
  public boolean isIgnored(int xFrom, int xTo, int y, String ruleId) {
    if (ignoredMatches.containsKey(y) && ignoredMatches.get(y).containsKey(ruleId)) {
      for (int x : ignoredMatches.get(y).get(ruleId)) {
        if (x >= xFrom && x < xTo) {
          return true;
        }
      }
    }
    return false;
  }
  
  /**
   * Contains a paragraph ignored matches
   */
  public boolean containsParagraph(int y) {
    return ignoredMatches.containsKey(y);
  }

  /**
   * Is the list of ignored matches empty - no ignored matches
   */
  public boolean isEmpty() {
    return ignoredMatches.isEmpty();
  }

  /**
   * size: number of paragraphs containing ignored matches
   */
  public int size() {
    return ignoredMatches.size();
  }

  /**
   * Get all ignored matches of a paragraph
   */
  public Map<String, Set<Integer>>  get(int y) {
    return ignoredMatches.get(y);
  }

  /**
   * Get a copy of ignored matches map
   */
  public Map<Integer, Map<String, Set<Integer>>> getFullIMMap() {
    return ignoredMatches;
  }

  /**
   * Get all spelling locales of a paragraph
   */
  public List<LocaleEntry> getLocaleEntries(int y) {
    return spellLocales.get(y);
  }

  /**
   * Get a copy of spelling locals map
   */
  public Map<Integer, List<LocaleEntry>> getFullSLMap() {
    return this.spellLocales;
  }

  /**
   * add or replace a map of ignored matches to a paragraph
   */
  public void put(int y, Map<String, Set<Integer>> ruleAtX) {
    ignoredMatches.put(y, ruleAtX);
  }

  /**
   * get all paragraphs containing ignored matches
   */
  public List<Integer> getAllParagraphs() {
    return new ArrayList<Integer>(ignoredMatches.keySet());
  }
  
  void removeSpellingMark(int y, int start, int length, FlatParagraphTools flatPara) {
    if (flatPara != null) {
      flatPara.setLanguageOfParagraph(y, start, length, new Locale (OfficeTools.IGNORE_LANGUAGE, "", ""));
    }
  }

  private void resetLocale(int y, int start, int length, Locale locale, FlatParagraphTools flatPara) {
    if (flatPara != null) {
      MessageHandler.printToLogFile("IgnoredMatches: resetLocale: y: " + y + ", start: " + start 
          + ", length: " + length + ", locale: " + OfficeTools.localeToString(locale));
      flatPara.setLanguageOfParagraph(y, start, length, locale);
    }
  }
  
  public void resetAllIgnoredSpellingMarks(FlatParagraphTools flatPara) {
    if (flatPara != null) {
      for (int y : spellLocales.keySet()) {
        for (LocaleEntry entry : spellLocales.get(y)) {
          removeSpellingMark(y, entry.start, entry.length, flatPara);
        }
      }
    }
  }

  public void resetAllLocale(FlatParagraphTools flatPara) {
    if (flatPara != null) {
      MessageHandler.printToLogFile("IgnoredMatches: resetAllLocale: spellLocales.size: " + spellLocales.size());
      for (int y : spellLocales.keySet()) {
        for (LocaleEntry entry : spellLocales.get(y)) {
          resetLocale(y, entry.start, entry.length, entry.locale, flatPara);
        }
      }
    } else {
      MessageHandler.printToLogFile("IgnoredMatches: resetAllLocale: flatPara == null");
    }
  }

  public static class LocaleEntry {
    int start;
    int length;
    Locale locale;
    String ruleId;
    
    LocaleEntry(int start, int length, Locale locale, String ruleId) {
      this.start = start;
      this.length = length;
      this.locale = locale;
      this.ruleId = ruleId;
    }
  }
  
}


