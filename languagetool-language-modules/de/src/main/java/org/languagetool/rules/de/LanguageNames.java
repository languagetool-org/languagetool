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
package org.languagetool.rules.de;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LanguageNames {

  private static final Set<String> languages = Stream.of(
    "Angelsächsisch",
    "Afrikanisch",
    "Albanisch",
    "Altarabisch",
    "Altchinesisch",
    "Altgriechisch",
    "Althochdeutsch",
    "Altpersisch",
    "Amerikanisch",
    "Arabisch",
    "Armenisch",
    "Bairisch",
    "Baskisch",
    "Bengalisch",
    "Bulgarisch",
    "Chinesisch",
    "Dänisch",
    "Deutsch",
    "Englisch",
    "Estnisch",
    "Finnisch",
    "Französisch",
    "Frühneuhochdeutsch",
    "Germanisch",
    "Georgisch",
    "Griechisch",
    "Hebräisch",
    "Hocharabisch",
    "Hochchinesisch",
    "Hochdeutsch",
    "Holländisch",
    "Indonesisch",
    "Irisch",
    "Isländisch",
    "Italienisch",
    "Japanisch",
    "Jiddisch",
    "Jugoslawisch",
    "Kantonesisch",
    "Katalanisch",
    "Klingonisch",
    "Koreanisch",
    "Kroatisch",
    "Kurdisch",
    "Lateinisch",
    "Lettisch",
    "Litauisch",
    "Luxemburgisch",
    "Mittelhochdeutsch",
    "Mongolisch",
    "Neuhochdeutsch",
    "Niederländisch",
    "Norwegisch",
    "Persisch",
    "Plattdeutsch",
    "Polnisch",
    "Portugiesisch",
    "Rätoromanisch",
    "Rumänisch",
    "Russisch",
    "Sächsisch",
    "Schwäbisch",
    "Schwedisch",
    "Schweizerisch",
    "Serbisch",
    "Serbokroatisch",
    "Slawisch",
    "Slowakisch",
    "Slowenisch",
    "Spanisch",
    "Syrisch",
    "Tamilisch",
    "Tibetisch",
    "Tschechisch",
    "Tschetschenisch",
    "Türkisch",
    "Turkmenisch",
    "Uigurisch",
    "Ukrainisch",
    "Ungarisch",
    "Usbekisch",
    "Vietnamesisch",
    "Walisisch",
    "Weißrussisch"
  ).collect(Collectors.toCollection(HashSet::new));

  public static Set<String> get() {
    return languages;
  }
}
