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
package org.languagetool.tagging.de;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * List of German verb prefixes. Not guaranteed to be complete.
 * @since 5.0
 */
public class VerbPrefixes {

  // https://deutschegrammatik20.de/spezielle-verben/verben-mit-praefix-trennbare-und-nicht-trennbare-verben/uebersicht-trennbare-praefixe/
  private static final List<String> prefixes = Arrays.asList("ab", "an", "auf", "aus", "auseinander", "bei", "ein", "empor", "entgegen", "entlang", "entzwei",
    "fehl", "fern", "fest", "fort", "gegenüber", "heim", "hinterher", "hoch", "los", "mit", "nach", "neben", "nieder", "vor",
    "weg", "weiter", "zu", "zurecht", "zurück", "zusammen", "da", "hin", "her",
    "herab", "heran", "herauf", "heraus", "herbei", "herein", "hernieder", "herüber", "herum", "herunter", "hervor", "herzu",
    "hinab", "hinan", "hinauf", "hinaus", "hinein", "hinüber", "hinunter", "hinweg", "hinzu", "vorab", "voran", "vorauf", "voraus",
    "vorbei", "vorweg", "vorher", "vorüber",
    "dabei", "dafür", "dagegen", "daher", "dahin", "dahinter", "daneben", "daran", "darauf", "darein", "darüber", "darunter",
    "hinter", "dran", "drauf", "drein", "drüber", "drunter",
    "davon", "davor", "dazu", "dazwischen",
    "durch", "über", "unter", "um", "wider", "wieder", "rüber", "aneinander",
    // not listed in source above (deutschegrammatik20.de):
    "umher", "ent", "frei", "ver", "zer", "gegen"
  );
  static {
    // sort alphabetically and then longest first:
    Collections.sort(prefixes);
    prefixes.sort((k, v) -> Integer.compare(v.length(), k.length()));
  }

  private VerbPrefixes() {
  }

  public static List<String> get() {
    return Collections.unmodifiableList(prefixes);
  }
}
