/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev;

import com.google.common.base.Charsets;
import morfologik.fsa.FSA;
import org.languagetool.JLanguageTool;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.*;

/**
 * Export German nouns, to be used by jWordSplitter.  
 * 
 * @author Daniel Naber
 */
public class ExportGermanNouns {

  private static final String DICT_FILENAME = "/de/german.dict";
  private static final String ADDED_DICT_FILENAME = "languagetool-language-modules/de/src/main/resources/org/languagetool/resource/de/added.txt";
  
  private ExportGermanNouns() {
  }
  
  private List<String> getSortedWords() throws IOException {
    Set<String> words1 = getBinaryDictWords();
    Set<String> words2 = getAddedDictWords();
    List<String> sortedWords = new ArrayList<>();
    sortedWords.addAll(words1);
    sortedWords.addAll(words2);
    Collections.sort(sortedWords);
    return sortedWords;
  }

  private Set<String> getBinaryDictWords() throws IOException {
    FSA fsa = FSA.read(JLanguageTool.getDataBroker().getFromResourceDirAsStream(DICT_FILENAME));
    Set<String> set = new HashSet<>();
    for (ByteBuffer buffer : fsa) {
      byte[] sequence = new byte[buffer.remaining()];
      buffer.get(sequence);
      String output = new String(sequence, StandardCharsets.UTF_8);
      if (isRelevantNoun(output)) {
        String[] parts = output.split("_");
        String term = parts[0].toLowerCase();
        set.add(term);
      }
    }
    return set;
  }

  private Set<String> getAddedDictWords() throws IOException {
    Set<String> set = new HashSet<>();
    List<String> lines = Files.readAllLines(FileSystems.getDefault().getPath(ADDED_DICT_FILENAME), Charsets.UTF_8);
    for (String line : lines) {
      if (isRelevantNoun(line)) {
        final String[] parts = line.split("\t");
        final String term = parts[0].toLowerCase();
        set.add(term);
      }
    }
    return set;
  }

  private boolean isRelevantNoun(String output) {
    boolean isNoun = output.contains("SUB:") || (output.contains("EIG:") && output.contains("COU"));
    return isNoun && !output.contains(":ADJ") && !StringTools.isAllUppercase(output);
  }
  
  public static void main(String[] args) throws IOException {
    ExportGermanNouns prg = new ExportGermanNouns();
    List<String> words = prg.getSortedWords();
    System.out.println("# DO NOT MODIFY - automatically exported");
    System.out.println("# Exporting class: " + ExportGermanNouns.class.getName());
    System.out.println("# Export date: " + new Date());
    System.out.println("# LanguageTool: " + JLanguageTool.VERSION + " (" + JLanguageTool.BUILD_DATE + ")");
    System.out.println("# Potential German compound parts.");
    System.out.println("# Data from Morphy (https://danielnaber.de/download/wklassen.pdf)");
    System.out.println("# with extensions by LanguageTool (https://languagetool.org)");
    System.out.println("# License: Creative Commons Attribution-Share Alike 4.0, http://creativecommons.org/licenses/by-sa/4.0/");
    for (String word : words) {
      System.out.println(word);
    }
    //System.err.println("Done. Printed " + words.size() + " words.");
  }
    
}
