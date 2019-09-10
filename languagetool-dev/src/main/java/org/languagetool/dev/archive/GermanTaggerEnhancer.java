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

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.language.GermanyGerman;
import org.languagetool.tagging.Tagger;
import org.languagetool.tools.StringTools;

import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.WordData;

/**
 * One-time script (used 2015-02-25) to add forms:
 * City names are incoherently tagged in the Morphy data. To avoid
 * false alarms on phrases like "das Stuttgarter Auto" we have to
 * explicitly add these adjective readings to "Stuttgarter" and to all
 * other potential city names.
 */
public class GermanTaggerEnhancer {

  private static final String[] ADJ_READINGS = {
          // singular:
          "ADJ:NOM:SIN:MAS:GRU", "ADJ:NOM:SIN:NEU:GRU", "ADJ:NOM:SIN:FEM:GRU",    // das Berliner Auto
          "ADJ:GEN:SIN:MAS:GRU", "ADJ:GEN:SIN:NEU:GRU", "ADJ:GEN:SIN:FEM:GRU",    // des Berliner Autos
          "ADJ:DAT:SIN:MAS:GRU", "ADJ:DAT:SIN:NEU:GRU", "ADJ:DAT:SIN:FEM:GRU",    // dem Berliner Auto
          "ADJ:AKK:SIN:MAS:GRU", "ADJ:AKK:SIN:NEU:GRU", "ADJ:AKK:SIN:FEM:GRU",    // den Berliner Bewohner
          // plural:
          "ADJ:NOM:PLU:MAS:GRU", "ADJ:NOM:PLU:NEU:GRU", "ADJ:NOM:PLU:FEM:GRU",    // die Berliner Autos
          "ADJ:GEN:PLU:MAS:GRU", "ADJ:GEN:PLU:NEU:GRU", "ADJ:GEN:PLU:FEM:GRU",    // der Berliner Autos
          "ADJ:DAT:PLU:MAS:GRU", "ADJ:DAT:PLU:NEU:GRU", "ADJ:DAT:PLU:FEM:GRU",    // den Berliner Autos
          "ADJ:AKK:PLU:MAS:GRU", "ADJ:AKK:PLU:NEU:GRU", "ADJ:AKK:PLU:FEM:GRU",    // den Berliner Bewohnern
  };

  private void run() throws IOException {
    final Dictionary dictionary = Dictionary.read(
            JLanguageTool.getDataBroker().getFromResourceDirAsUrl("/de/german.dict"));
    final DictionaryLookup dl = new DictionaryLookup(dictionary);
    Tagger tagger = new GermanyGerman().getTagger();
    String prev = null;
    for (WordData wd : dl) {
      String word = wd.getWord().toString();
      if (word.endsWith("er")
          && StringTools.startsWithUppercase(word)
          && !hasAnyPosTagStartingWith(tagger, word, "ADJ:NOM")
          && hasAnyPosTagStartingWith(tagger, word.substring(0, word.length()-2), "EIG")
          && !word.equals(prev)) {
        for (String newTags : ADJ_READINGS) {
          System.out.println(word + "\t" + word + "\t" + newTags + ":DEF\n"+
                             word + "\t" + word + "\t" + newTags + ":IND\n"+
                             word + "\t" + word + "\t" + newTags + ":SOL");
        }
        prev = word;
      }
    }
  }

  private boolean hasAnyPosTagStartingWith(Tagger tagger, String word, String initialPosTag) throws IOException {
    List<AnalyzedTokenReadings> readings = tagger.tag(Collections.singletonList(word));
    return readings.stream().anyMatch(atr -> atr.hasPosTagStartingWith(initialPosTag));
  }

  public static void main(String[] args) throws IOException {
    GermanTaggerEnhancer enhancer = new GermanTaggerEnhancer();
    enhancer.run();
  }

}
