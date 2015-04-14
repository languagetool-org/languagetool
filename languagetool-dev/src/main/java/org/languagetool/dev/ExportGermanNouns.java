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

import morfologik.fsa.FSA;
import org.languagetool.JLanguageTool;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Export German nouns as a serialized Java HashSet, to be used
 * by jWordSplitter.  
 * 
 * @author Daniel Naber
 */
public class ExportGermanNouns {

  private static final String DICT_FILENAME = "/de/german.dict";
  
  private ExportGermanNouns() {
  }
  
  private List<String> getSortedWords() throws IOException {
    Set<String> words = getWords();
    List<String> sortedWords = new ArrayList<>(words);
    Collections.sort(sortedWords);
    return sortedWords;
  }
  
  private Set<String> getWords() throws IOException {
    final FSA fsa = FSA.read(JLanguageTool.getDataBroker().getFromResourceDirAsStream(DICT_FILENAME));
    final Set<String> set = new HashSet<>();
    for (ByteBuffer bb : fsa) {
      final byte [] sequence = new byte [bb.remaining()];
      bb.get(sequence);
      final String output = new String(sequence, "iso-8859-1");
      boolean isNoun = output.contains("+SUB:") || (output.contains("+EIG:") && output.contains("COU")); // COU = Country
      if (isNoun && !output.contains(":ADJ") && !StringTools.isAllUppercase(output)) {
        final String[] parts = output.split("\\+");
        final String term = parts[0].toLowerCase();
        set.add(term);
      }
    }
    return set;
  }
  
  public static void main(String[] args) throws IOException {
    ExportGermanNouns prg = new ExportGermanNouns();
    List<String> words = prg.getSortedWords();
    System.out.println("# DO NOT MODIFY - automatically exported");
    System.out.println("# Exporting class: " + ExportGermanNouns.class.getName());
    System.out.println("# Export date: " + new Date());
    System.out.println("# LanguageTool: " + JLanguageTool.VERSION + " (" + JLanguageTool.BUILD_DATE + ")");
    System.out.println("# Potential German compound parts.");
    System.out.println("# Data from Morphy (http://www.wolfganglezius.de/doku.php?id=cl:morphy)");
    System.out.println("# with extensions by LanguageTool (https://languagetool.org)");
    System.out.println("# License: Creative Commons Attribution-Share Alike 4.0, http://creativecommons.org/licenses/by-sa/4.0/");
    for (String word : words) {
      System.out.println(word);
    }
    //System.err.println("Done. Printed " + words.size() + " words.");
  }
    
}
