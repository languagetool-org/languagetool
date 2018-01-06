/* LanguageTool, a natural language style checker 
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.archive;

import morfologik.fsa.FSA;
import org.apache.commons.io.FileUtils;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.tagging.de.GermanTagger;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * One-time script: find missing "-es" forms in the German tagger dictionary,
 * e.g. Morphy knows "Antrag" and "Antrags", but not "Antrages".
 * Uses Google n-gram data as a filter, but may nonetheless create
 * forms that aren't common anymore (e.g. Verb -&gt; Verbes).
 * 
 * @author Daniel Naber
 */
public class MissingGenitiveFinder {

  private static final String DICT_FILENAME = "/de/german.dict";

  private final Map<String,Integer> occurrences;

  private MissingGenitiveFinder() throws IOException {
    occurrences = loadOccurrences("/media/Data/google-ngram/de/1gram-aggregated/all_without_underscore");
  }

  @SuppressWarnings("unchecked")
  private Map<String, Integer> loadOccurrences(String filename) throws IOException {
    System.err.println("Loading " + filename);
    Map<String, Integer> map = new HashMap<>();
    List<String> lines = (List<String>)FileUtils.readLines(new File(filename));
    for (String line : lines) {
      String[] parts = line.split(" ");
      map.put(parts[0], Integer.valueOf(parts[1]));
    }
    System.err.println("Loaded " + map.size() + " occurrence items");
    return map;
  }

  @SuppressWarnings("UnnecessaryParentheses")
  private void run() throws IOException {
    GermanTagger tagger = new GermanTagger();
    final FSA fsa = FSA.read(JLanguageTool.getDataBroker().getFromResourceDirAsStream(DICT_FILENAME));
    int i = 0;
    for (ByteBuffer buffer : fsa) {
      final byte [] sequence = new byte [buffer.remaining()];
      buffer.get(sequence);
      final String output = new String(sequence, "iso-8859-1");
      boolean isNoun = output.contains("+SUB:") || (output.contains("+EIG:") && output.contains("COU")); // COU = Country
      if (isNoun && output.contains(":GEN:")) {
        final String[] parts = output.split("\\+");
        String word = parts[0];
        String esWord = parts[0].replaceFirst("s$", "es");
        if (isRelevantWord(word)) {
          boolean hasEsGenitive = hasEsGenitive(tagger, word);
          boolean ignore1 = word.endsWith("els") && !word.endsWith("iels");
          Integer occurrence = occurrences.get(esWord);
          if (!hasEsGenitive && !ignore1 &&  occurrence != null) {
            //System.out.println(i + ". " + word + " " + occurrence);
            System.out.println(esWord + "\t" + word.replaceFirst("s$", "") + "\t" + parts[2]);
            i++;
          }
        }
      }
    }
  }

  private boolean isRelevantWord(String word) {
    return word.endsWith("s")
            && !word.endsWith("es") 
            && !word.endsWith("ens")
            && !word.endsWith("ems")
            && !word.endsWith("els")
            && !word.endsWith("ers")
            && !word.endsWith("lings")
            && !word.endsWith("leins")
            && !word.endsWith("chens")
            && !word.endsWith("erns")
            && !word.endsWith("elns")
            && !word.endsWith("os")
            && !word.endsWith("us")
            && !word.endsWith("is")
            && !word.endsWith("as")
            && !word.endsWith("ols");
  }

  private boolean hasEsGenitive(GermanTagger tagger, String word) throws IOException {
    String esForm = word.replaceFirst("s$", "es");
    List<AnalyzedTokenReadings> readings = tagger.tag(Collections.singletonList(esForm));
    for (AnalyzedTokenReadings reading : readings) {
      if (reading.isTagged()) {
        return true;
      }
    }
    return false;
  }

  public static void main(String[] args) throws IOException {
    MissingGenitiveFinder prg = new MissingGenitiveFinder();
    prg.run();
  }
    
}
