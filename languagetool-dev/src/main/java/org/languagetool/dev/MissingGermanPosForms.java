/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.tagging.TaggedWord;
import org.languagetool.tagging.de.GermanTagger;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Fast hack to find words which have an uppercase reading in the speller dict
 * but only a lowercase reading in the POS dict =&gt; Reading might be missing
 * in POS dict.
 */
public class MissingGermanPosForms {

  public static void main(String[] args) throws IOException {
    List<String> lines = Files.readAllLines(Paths.get("/home/dnaber/lt/git/languagetool/languagetool-language-modules/de/src/main/resources/org/languagetool/resource/de/hunspell/de_DE.dic"));
    //List<String> lines = Arrays.asList("Beige", "Zoom", "Perl", "Haus", "Drücke", "Wisch");
    GermanTagger tagger = new GermanTagger();
    for (String line : lines) {
      String word = line.replaceFirst("/.*", "");
      if (StringTools.startsWithUppercase(word)) {
        List<TaggedWord> ucMatches = tagger.tag(word);
        List<TaggedWord> lcMatches = tagger.tag(StringTools.lowercaseFirstChar(word));
        //System.out.println(word + " " + ucMatches + " " + lcMatches);
        if (ucMatches.size() == 0 && lcMatches.size() > 0) {
          System.out.println(word + " " + lcMatches);
        }
      }
    }
  }
}
