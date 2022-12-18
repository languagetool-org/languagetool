/* LanguageTool, a natural language style checker
 * Copyright (C) 2022 Daniel Naber (http://www.danielnaber.de)
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

import java.util.List;

import static java.util.Arrays.*;
import static java.util.Collections.unmodifiableList;

class AdjectiveTags {

  // adjective forms for base form, like "chemisch":
  static List<String> tagsForAdj = unmodifiableList(asList(
    "ADJ:PRD:GRU"
  ));

  // adjective forms ending in -e, like "chemische":
  static List<String> tagsForAdjE = unmodifiableList(asList(
    "ADJ:AKK:PLU:FEM:GRU:SOL",
    "ADJ:AKK:PLU:MAS:GRU:SOL",
    "ADJ:AKK:PLU:NEU:GRU:SOL",
    "ADJ:AKK:SIN:FEM:GRU:DEF",
    "ADJ:AKK:SIN:FEM:GRU:IND",
    "ADJ:AKK:SIN:FEM:GRU:SOL",
    "ADJ:AKK:SIN:NEU:GRU:DEF",

    "ADJ:NOM:PLU:FEM:GRU:SOL",
    "ADJ:NOM:PLU:MAS:GRU:SOL",
    "ADJ:NOM:PLU:NEU:GRU:SOL",
    "ADJ:NOM:SIN:FEM:GRU:DEF",
    "ADJ:NOM:SIN:FEM:GRU:IND",
    "ADJ:NOM:SIN:FEM:GRU:SOL",
    "ADJ:NOM:SIN:MAS:GRU:DEF",
    "ADJ:NOM:SIN:NEU:GRU:DEF"
  ));

  static List<String> tagsForAdjEn = unmodifiableList(asList(
    "ADJ:AKK:PLU:FEM:GRU:DEF",
    "ADJ:AKK:PLU:FEM:GRU:IND",
    "ADJ:AKK:PLU:MAS:GRU:DEF",
    "ADJ:AKK:PLU:MAS:GRU:IND",
    "ADJ:AKK:PLU:NEU:GRU:DEF",
    "ADJ:AKK:PLU:NEU:GRU:IND",
    "ADJ:AKK:SIN:MAS:GRU:DEF",
    "ADJ:AKK:SIN:MAS:GRU:IND",
    "ADJ:AKK:SIN:MAS:GRU:SOL",
    "ADJ:DAT:PLU:FEM:GRU:DEF",
    "ADJ:DAT:PLU:FEM:GRU:IND",
    "ADJ:DAT:PLU:FEM:GRU:SOL",
    "ADJ:DAT:PLU:MAS:GRU:DEF",
    "ADJ:DAT:PLU:MAS:GRU:IND",
    "ADJ:DAT:PLU:MAS:GRU:SOL",
    "ADJ:DAT:PLU:NEU:GRU:DEF",
    "ADJ:DAT:PLU:NEU:GRU:IND",
    "ADJ:DAT:PLU:NEU:GRU:SOL",
    "ADJ:DAT:SIN:FEM:GRU:DEF",
    "ADJ:DAT:SIN:FEM:GRU:IND",
    "ADJ:DAT:SIN:MAS:GRU:DEF",
    "ADJ:DAT:SIN:MAS:GRU:IND",
    "ADJ:DAT:SIN:NEU:GRU:DEF",
    "ADJ:DAT:SIN:NEU:GRU:IND",
    "ADJ:GEN:PLU:FEM:GRU:DEF",
    "ADJ:GEN:PLU:FEM:GRU:IND",
    "ADJ:GEN:PLU:MAS:GRU:DEF",
    "ADJ:GEN:PLU:MAS:GRU:IND",
    "ADJ:GEN:PLU:NEU:GRU:DEF",
    "ADJ:GEN:PLU:NEU:GRU:IND",
    "ADJ:GEN:SIN:FEM:GRU:DEF",
    "ADJ:GEN:SIN:FEM:GRU:IND",
    "ADJ:GEN:SIN:MAS:GRU:DEF",
    "ADJ:GEN:SIN:MAS:GRU:IND",
    "ADJ:GEN:SIN:MAS:GRU:SOL",
    "ADJ:GEN:SIN:NEU:GRU:DEF",
    "ADJ:GEN:SIN:NEU:GRU:IND",
    "ADJ:GEN:SIN:NEU:GRU:SOL",
    "ADJ:NOM:PLU:FEM:GRU:DEF",
    "ADJ:NOM:PLU:FEM:GRU:IND",
    "ADJ:NOM:PLU:MAS:GRU:DEF",
    "ADJ:NOM:PLU:MAS:GRU:IND",
    "ADJ:NOM:PLU:NEU:GRU:DEF",
    "ADJ:NOM:PLU:NEU:GRU:IND"
  ));

  static List<String> tagsForAdjEr = unmodifiableList(asList(
    "ADJ:DAT:SIN:FEM:GRU:SOL",
    "ADJ:GEN:PLU:FEM:GRU:SOL",
    "ADJ:GEN:PLU:MAS:GRU:SOL",
    "ADJ:GEN:PLU:NEU:GRU:SOL",
    "ADJ:GEN:SIN:FEM:GRU:SOL",
    "ADJ:NOM:SIN:MAS:GRU:IND",
    "ADJ:NOM:SIN:MAS:GRU:SOL"
  ));

  static List<String> tagsForAdjEm = unmodifiableList(asList(
    "ADJ:DAT:SIN:MAS:GRU:SOL",
    "ADJ:DAT:SIN:NEU:GRU:SOL"
  ));

  static List<String> tagsForAdjEs = unmodifiableList(asList(
    "ADJ:AKK:SIN:NEU:GRU:IND",
    "ADJ:AKK:SIN:NEU:GRU:SOL",
    "ADJ:NOM:SIN:NEU:GRU:IND",
    "ADJ:NOM:SIN:NEU:GRU:SOL"
  ));

}
