/* LanguageTool, a natural language style checker
 * Copyright (C) 2023 Pedro Goulart
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
package org.languagetool.rules.pt;

import java.util.*;

public class BrazilianStateInfo {
  Set<String> ambiguousStates = new HashSet<>(Arrays.asList("RJ", "SP"));
  static final BrazilianStateInfoMap map = new BrazilianStateInfoMap();
  String name;
  String abbreviation;
  String[] articles;
  String capital;

  BrazilianStateInfo(String name, String abbreviation, String[] articles, String capital) {
    this.name = name;
    this.abbreviation = abbreviation;
    this.articles = articles.clone();
    this.capital = capital;
  }

  public boolean isAmbiguous() {
    return ambiguousStates.contains(abbreviation);
  }

  @Override
  public String toString() {
    return String.format("<%s|%s|%s|%s>", name, abbreviation, Arrays.toString(articles), capital);
  }
}
