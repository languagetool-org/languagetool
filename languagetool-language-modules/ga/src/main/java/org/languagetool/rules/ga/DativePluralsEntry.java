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

public class DativePluralsEntry {
  private String form;
  private String formModern;
  private String lemma;
  private String lemmaModern;
  private String equivalent;
  private String replacement;

  public String getForm() {
    return form;
  }

  public String getModern() {
    return formModern;
  }

  public String getLemma() {
    return lemma;
  }

  public String getLemmaModern() {
    return lemmaModern;
  }

  public String getEquivalent() {
    return equivalent;
  }

  public String getReplacement() {
    return replacement;
  }

  public String getGender() {
    return gender;
  }

  private String gender;

  public DativePluralsEntry(String form, String lemma, String gender, String replacement) {
    this.form = form;
    this.lemma = lemma;
    this.gender = gender;
    this.replacement = replacement;
  }

  public void setEquivalent(String equiv) {
    this.equivalent = equiv;
  }
  public void setModernised(String modernised) {
    this.formModern = modernised;
  }
  public void setModernLemma(String modernised) {
    this.lemmaModern = modernised;
  }
  public boolean hasEquivalent() {
    return !(equivalent == null || equivalent.equals(""));
  }
  public boolean hasModernised() {
    return !(formModern == null || formModern.equals(""));
  }
  public boolean hasModernLemma() {
    return !(lemmaModern == null || lemmaModern.equals(""));
  }
  public String getBaseTag() {
    return (this.gender.equals("f")) ? "Noun:Fem:Dat:Pl" : "Noun:Masc:Dat:Pl";
  }
  public String getStandard() {
    if(hasEquivalent()) {
      return equivalent;
    } else {
      return replacement;
    }
  }
}
