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

import java.io.InputStream;
import java.util.*;

final class DativePluralsData {

  private static final Set<DativePluralsEntry> datives = loadWords("/ga/dative-plurals.txt");

  private static final Map<String, String> modernisations = getModernisations(datives);

  private static final Map<String, String> simple_replacements = buildSimpleReplacements(datives);

  public static Map<String, String> getModernisations() {
    return modernisations;
  }

  public static Map<String, String> getSimpleReplacements() {
    return simple_replacements;
  }

  /**
   * Load words.
   */
  private static Set<DativePluralsEntry> loadWords(String path) {
    Set<DativePluralsEntry> set = new HashSet<>();
    InputStream stream = JLanguageTool.getDataBroker().getFromRulesDirAsStream(path);
    try (Scanner scanner = new Scanner(stream, "utf-8")) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine().trim();
        if (line.isEmpty() || line.charAt(0) == '#') {
          continue;
        }
        String parts[] = line.split(";");
        if(parts.length != 4) {
          System.err.println("Skipping entry (Incorrect number of fields): " + line);
          continue;
        }
        String form = parts[0];
        String form_modern = null;
        if(form.contains(":")) {
          String forms[] = form.split(":");
          if(forms.length != 2) {
            System.err.println("Skipping entry (form has more than 1 modern form): " + line);
            continue;
          }
          form = forms[0];
          form_modern = forms[1];
        }
        String repl = parts[3];
        String repl_modern = null;
        if(repl.contains(":")) {
          String repls[] = repl.split(":");
          if(repls.length != 2) {
            System.err.println("Skipping entry (replacement has more than 1 modern form): " + line);
            continue;
          }
          repl = repls[0];
          repl_modern = repls[1];
        }
        String lemma = parts[1];
        String lemma_modern = null;
        if(lemma.contains(":")) {
          String lemmata[] = lemma.split(":");
          if(lemmata.length != 2) {
            System.err.println("Skipping entry (lemma has more than 1 modern form): " + line);
            continue;
          }
          lemma = lemmata[0];
          lemma_modern = lemmata[1];
        }
        DativePluralsEntry entry = new DativePluralsEntry(form, lemma, parts[2], repl);
        if(form_modern != null) {
          entry.setModernised(form_modern);
        }
        if(repl_modern != null) {
          entry.setEquivalent(repl_modern);
        }
        if(lemma_modern != null) {
          entry.setModernLemma(lemma_modern);
        }
        set.add(entry);
      }
    }
    return Collections.unmodifiableSet(set);
  }

  private static Map<String, String> buildSimpleReplacements(Set<DativePluralsEntry> datives) {
    Map<String, String> out = new HashMap<>();
    for(DativePluralsEntry entry : datives) {
      out.put(entry.getForm(), entry.getStandard());
      String lenited_form = Utils.lenite(entry.getForm());
      String lenited_repl = Utils.lenite(entry.getStandard());
      if(!lenited_form.equals(entry.getForm())) {
        out.put(lenited_form, lenited_repl);
      }
      String eclipsed_form = Utils.eclipse(entry.getForm());
      String eclipsed_repl = Utils.eclipse(entry.getStandard());
      if(!eclipsed_form.equals(entry.getForm())) {
        out.put(eclipsed_form, eclipsed_repl);
      }
      // h-prothesis
      if(Utils.isVowel(entry.getForm().charAt(0))) {
        out.put("h" + entry.getForm(), "h" + entry.getStandard());
        out.put("h-" + entry.getForm(), "h" + entry.getStandard());
      }
      if(entry.hasModernised()) {
        out.put(entry.getModern(), entry.getStandard());
        lenited_form = Utils.lenite(entry.getModern());
        lenited_repl = Utils.lenite(entry.getStandard());
        if(!lenited_form.equals(entry.getModern())) {
          out.put(lenited_form, lenited_repl);
        }
        eclipsed_form = Utils.eclipse(entry.getModern());
        eclipsed_repl = Utils.eclipse(entry.getStandard());
        if(!eclipsed_form.equals(entry.getModern())) {
          out.put(eclipsed_form, eclipsed_repl);
        }
        // h-prothesis
        if(Utils.isVowel(entry.getModern().charAt(0))) {
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
    for(DativePluralsEntry entry : datives) {
      if(entry.hasModernised()) {
        out.put(entry.getForm(), entry.getModern());
        String lenited_form = Utils.lenite(entry.getForm());
        String lenited_repl = Utils.lenite(entry.getReplacement());
        if(!lenited_form.equals(entry.getForm())) {
          out.put(lenited_form, lenited_repl);
        }
        String eclipsed_form = Utils.eclipse(entry.getForm());
        String eclipsed_repl = Utils.eclipse(entry.getReplacement());
        if(!eclipsed_form.equals(entry.getForm())) {
          out.put(eclipsed_form, eclipsed_repl);
        }
        // h-prothesis
        if(Utils.isVowel(entry.getForm().charAt(0))) {
          out.put("h" + entry.getForm(), "h" + entry.getModern());
          out.put("h-" + entry.getForm(), "h" + entry.getModern());
        }
      }
    }
    return out;
  }
}
