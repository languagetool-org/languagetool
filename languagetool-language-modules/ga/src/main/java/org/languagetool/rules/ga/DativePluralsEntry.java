/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Jim O'Regan
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
package org.languagetool.rules.ga;

class DativePluralsEntry {
  
  private String form;
  private String formModern;
  private String lemma;
  private String lemmaModern;
  private String equivalent;
  private String replacement;

  String getForm() {
    return form;
  }

  String getModern() {
    return formModern;
  }

  String getLemma() {
    return lemma;
  }

  String getLemmaModern() {
    return lemmaModern;
  }

  String getEquivalent() {
    return equivalent;
  }

  String getReplacement() {
    return replacement;
  }

  String getGender() {
    return gender;
  }

  private String gender;

  DativePluralsEntry(String form, String lemma, String gender, String replacement) {
    this.form = form;
    this.lemma = lemma;
    this.gender = gender;
    this.replacement = replacement;
  }

  void setEquivalent(String equiv) {
    this.equivalent = equiv;
  }
  void setModernised(String modernised) {
    this.formModern = modernised;
  }
  void setModernLemma(String modernised) {
    this.lemmaModern = modernised;
  }
  boolean hasEquivalent() {
    return !(equivalent == null || equivalent.equals(""));
  }
  boolean hasModernised() {
    return !(formModern == null || formModern.equals(""));
  }
  boolean hasModernLemma() {
    return !(lemmaModern == null || lemmaModern.equals(""));
  }
  String getBaseTag() {
    return (this.gender.equals("f")) ? "Noun:Fem:Dat:Pl" : "Noun:Masc:Dat:Pl";
  }
  String getStandard() {
    if(hasEquivalent()) {
      return equivalent;
    } else {
      return replacement;
    }
  }
}
