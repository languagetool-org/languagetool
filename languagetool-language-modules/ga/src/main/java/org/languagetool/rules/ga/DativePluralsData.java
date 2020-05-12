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

import org.languagetool.JLanguageTool;
import org.languagetool.tagging.ga.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

final class DativePluralsData {

  private static final Set<DativePluralsEntry> datives = loadWords("/ga/dative-plurals.txt");
  private static final Map<String, String> modernisations = getModernisations(datives);
  private static final Map<String, String> simpleReplacements = buildSimpleReplacements(datives);

  public static Map<String, String> getModernisations() {
    return modernisations;
  }
  public static Map<String, String> getSimpleReplacements() {
    return simpleReplacements;
  }

  /**
   * Load words.
   */
  private static Set<DativePluralsEntry> loadWords(String path) {
    Set<DativePluralsEntry> set = new HashSet<>();
    InputStream stream = JLanguageTool.getDataBroker().getFromRulesDirAsStream(path);
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty() || line.charAt(0) == '#') {
          continue;
        }
        String[] parts = line.split(";");
        if (parts.length != 4) {
          throw new RuntimeException("Incorrect number of fields: " + line);
        }
        String form = parts[0];
        String formModern = null;
        if (form.contains(":")) {
          String[] forms = form.split(":");
          if (forms.length != 2) {
            throw new RuntimeException("Form has more than 1 modern form:" + line);
          }
          form = forms[0];
          formModern = forms[1];
        }
        String repl = parts[3];
        String replModern = null;
        if (repl.contains(":")) {
          String[] repls = repl.split(":");
          if (repls.length != 2) {
            throw new RuntimeException("Replacement has more than 1 modern form:" + line);
          }
          repl = repls[0];
          replModern = repls[1];
        }
        String lemma = parts[1];
        String lemmaModern = null;
        if (lemma.contains(":")) {
          String[] lemmata = lemma.split(":");
          if (lemmata.length != 2) {
            throw new RuntimeException("Lemma has more than 1 modern form:" + line);
          }
          lemma = lemmata[0];
          lemmaModern = lemmata[1];
        }
        DativePluralsEntry entry = new DativePluralsEntry(form, lemma, parts[2], repl);
        if (formModern != null) {
          entry.setModernised(formModern);
        }
        if (replModern != null) {
          entry.setEquivalent(replModern);
        }
        if (lemmaModern != null) {
          entry.setModernLemma(lemmaModern);
        }
        set.add(entry);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return Collections.unmodifiableSet(set);
  }

  private static Map<String, String> buildSimpleReplacements(Set<DativePluralsEntry> datives) {
    Map<String, String> out = new HashMap<>();
    for (DativePluralsEntry entry : datives) {
      out.put(entry.getForm(), entry.getStandard());
      String lenitedForm = Utils.lenite(entry.getForm());
      String lenitedRepl = Utils.lenite(entry.getStandard());
      if (!lenitedForm.equals(entry.getForm())) {
        out.put(lenitedForm, lenitedRepl);
      }
      String eclipsedForm = Utils.eclipse(entry.getForm());
      String eclipsedRepl = Utils.eclipse(entry.getStandard());
      if (!eclipsedForm.equals(entry.getForm())) {
        out.put(eclipsedForm, eclipsedRepl);
      }
      // h-prothesis
      if (Utils.isVowel(entry.getForm().charAt(0))) {
        out.put("h" + entry.getForm(), "h" + entry.getStandard());
        out.put("h-" + entry.getForm(), "h" + entry.getStandard());
      }
      if (entry.hasModernised()) {
        out.put(entry.getModern(), entry.getStandard());
        lenitedForm = Utils.lenite(entry.getModern());
        lenitedRepl = Utils.lenite(entry.getStandard());
        if (!lenitedForm.equals(entry.getModern())) {
          out.put(lenitedForm, lenitedRepl);
        }
        eclipsedForm = Utils.eclipse(entry.getModern());
        eclipsedRepl = Utils.eclipse(entry.getStandard());
        if (!eclipsedForm.equals(entry.getModern())) {
          out.put(eclipsedForm, eclipsedRepl);
        }
        // h-prothesis
        if (Utils.isVowel(entry.getModern().charAt(0))) {
          out.put("h" + entry.getModern(), "h" + entry.getStandard());
          out.put("h-" + entry.getModern(), "h" + entry.getStandard());
        }
      }
    }
    return out;
  }

  /**
   * Makes a map of modernisations (i.e., if there is a more modern dative
   * plural). This is only relevant to Munster Irish.
   * Additionally generates mutated forms
   * @param datives data, as loaded from dative-plurals.txt
   * @return a map of replacements
   */
  private static Map<String, String> getModernisations(Set<DativePluralsEntry> datives) {
    Map<String, String> out = new HashMap<>();
    for (DativePluralsEntry entry : datives) {
      if (entry.hasModernised()) {
        out.put(entry.getForm(), entry.getModern());
        String lenitedForm = Utils.lenite(entry.getForm());
        String lenitedRepl = Utils.lenite(entry.getReplacement());
        if (!lenitedForm.equals(entry.getForm())) {
          out.put(lenitedForm, lenitedRepl);
        }
        String eclipsedForm = Utils.eclipse(entry.getForm());
        String eclipsedRepl = Utils.eclipse(entry.getReplacement());
        if (!eclipsedForm.equals(entry.getForm())) {
          out.put(eclipsedForm, eclipsedRepl);
        }
        // h-prothesis
        if (Utils.isVowel(entry.getForm().charAt(0))) {
          out.put("h" + entry.getForm(), "h" + entry.getModern());
          out.put("h-" + entry.getForm(), "h" + entry.getModern());
        }
      }
    }
    return out;
  }
}
