/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Jaume Ortolà i Font
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
import java.util.stream.Collectors;

import static org.languagetool.JLanguageTool.getDataBroker;

public class BrazilianToponymMapLoader {
  private final String toponymFilepath = "pt/brazilian_municipalities";
  private final List<String> states = Arrays.asList(
    "AC", // Acre
    "AL", // Alagoas
    "AP", // Amapá
    "AM", // Amazonas
    "BA", // Bahia
    "CE", // Ceará
    "DF", // Distrito Federal
    "ES", // Espírito Santo
    "GO", // Goiás
    "MA", // Maranhão
    "MT", // Mato Grosso
    "MS", // Mato Grosso do Sul
    "MG", // Minas Gerais
    "PA", // Pará
    "PB", // Paraíba
    "PR", // Paraná
    "PE", // Pernambuco
    "PI", // Piauí
    "RJ", // Rio de Janeiro
    "RN", // Rio Grande do Norte
    "RS", // Rio Grande do Sul
    "RO", // Rondônia
    "RR", // Roraima
    "SC", // Santa Catarina
    "SP", // São Paulo
    "SE", // Sergipe
    "TO"  // Tocantins
  );

  BrazilianToponymMapLoader() {
  }

  private List<String> getToponymsFromState(String state) {
    List<String> toponyms = getDataBroker().getFromResourceDirAsLines(toponymFilepath + "/" + state + ".tsv");
    return toponyms.stream()
      .map(toponym -> toponym.replace('-', ' ').toLowerCase())
      .collect(Collectors.toList());
  }

  public Map<String, List<String>> buildMap() {
    Map<String, List<String>> map = new HashMap<>();
    for (String state : states) {
      map.put(state, getToponymsFromState(state));
    }
    return map;
  }
}
