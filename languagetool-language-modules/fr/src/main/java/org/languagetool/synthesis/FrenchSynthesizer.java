/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.synthesis;

import org.languagetool.Language;

import java.util.Arrays;
import java.util.List;

/**
 * French word form synthesizer.
 */
public class FrenchSynthesizer extends BaseSynthesizer {

  public static final FrenchSynthesizer INSTANCE = new FrenchSynthesizer();

  private static final List<String> exceptionsEgrave = Arrays.asList(new String[]{"burkinabè", "koinè", "épistémè"});

  /** @deprecated use {@link #INSTANCE} */
  public FrenchSynthesizer(Language lang) {
    this();
  }

  private FrenchSynthesizer() {
    super("fr/fr.sor", "/fr/french_synth.dict", "/fr/french_tags.txt", "fr");
  }

  @Override
  protected boolean isException(String w) {
    // remove: qq, qqe...
    if (w.startsWith("qq")) {
      return true;
    }
    // informè V ind pres 1 s
    if (w.endsWith("è") && !exceptionsEgrave.contains(w.toLowerCase())) {
      return true;
    }
    return false;
  }
}
