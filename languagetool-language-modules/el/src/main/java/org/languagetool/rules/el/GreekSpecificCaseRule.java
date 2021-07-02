/* LanguageTool, a natural language style checker 
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.el;

import org.languagetool.rules.*;

import java.util.*;

/**
 * A rule that matches words which need a specific upper/lowercase spelling.
 * @author Nikos-Antonopoulos, giorgossideris
 */
public class GreekSpecificCaseRule extends AbstractSpecificCaseRule {

  @Override
  public String getPhrasesPath() {
    return "/el/specific_case.txt";
  }
  
  @Override
  public String getInitialCapitalMessage() {
    return "Οι λέξεις της συγκεκριμένης έκφρασης χρείαζεται να ξεκινούν με κεφαλαία γράμματα.";
  }

  @Override
  public String getOtherCapitalizationMessage() {
    return "Η συγκεκριμένη έκφραση γράφεται σύμφωνα με την προτεινόμενη κεφαλαιοποίηση.";
  }
  
  @Override
  public String getShortMessage() {
    return "Ειδική κεφαλαιοποίηση";
  }

  public GreekSpecificCaseRule(ResourceBundle messages) {
    super(messages);
    addExamplePair(Example.wrong("Κατοικώ στις <marker>Ηνωμένες πολιτείες</marker>."),
                   Example.fixed("Κατοικώ στις <marker>Ηνωμένες Πολιτείες</marker>."));
  }

  @Override
  public final String getId() {
    return "EL_SPECIFIC_CASE";
  }

  @Override
  public String getDescription() {
    return "Ελέγχει αν κάποιες λέξεις χρειάζονται κεφαλαίο το πρώτο τους γράμμα";
  }
  
}
