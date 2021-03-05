/* LanguageTool, a natural language style checker 
 * Copyright (C) 2009 Marcin Miłkowski (http://www.languagetool.org)
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
package org.languagetool.openoffice;

import com.sun.star.beans.PropertyState;
import com.sun.star.beans.PropertyValue;
import com.sun.star.lang.Locale;
import com.sun.star.linguistic2.ProofreadingResult;
import org.junit.Test;
import org.languagetool.rules.Rule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainTest {

  @Test
//  @Ignore("see https://github.com/languagetool-org/languagetool/issues/4064")
  public void testDoProofreading() {
    Main prog = new Main(null);
    prog.setTestMode(true);
    String testString = "To jest trudne zdanie. A to następne.  A to przedostatnie jest.\u0002 Test ostatniego.";
    Locale plLoc = new Locale("pl", "PL", "");
    PropertyValue[] prop = new PropertyValue[0];
    for (int i = 0; i <= testString.length(); i++) {
      ProofreadingResult paRes = prog.doProofreading("1", testString, plLoc, i, testString.length(), prop);
      assertEquals("1", paRes.aDocumentIdentifier);
      assertTrue(paRes.nStartOfNextSentencePosition >= i);
      if (i < "To jest trudne zdanie. ".length()) {
        assertEquals("To jest trudne zdanie. ".length(), paRes.nStartOfNextSentencePosition);
        assertEquals(0, paRes.nStartOfSentencePosition);
      }
    }
    ProofreadingResult paRes1 = prog.doProofreading("1", testString, plLoc, 0, testString.length(), prop);
    assertEquals("1", paRes1.aDocumentIdentifier);
    assertEquals(23, paRes1.nStartOfNextSentencePosition);
    assertEquals(0, paRes1.nStartOfSentencePosition);
    //that was causing NPE but not anymore:
    String testString2 = "To jest „nowy problem”. A to inny jeszcze( „problem. Co jest „?"; 
    ProofreadingResult paRes2 = prog.doProofreading("1", testString2, plLoc, 0, testString2.length(), prop);
    assertEquals("1", paRes2.aDocumentIdentifier);
    assertEquals(24, paRes2.nStartOfNextSentencePosition);
    assertEquals(0, paRes2.nStartOfSentencePosition);
  }

  @Test
  public void testGetCheckResults() {
    // create a list of paragraphs for a virtual document
    List<String> paragraphs = new ArrayList<String>();
    paragraphs.add("»Dies ist eine Beispieltext.");
    paragraphs.add("Dies ist ein  zweiter Satz.");
    paragraphs.add("Dies ist ein (dritter.");
    paragraphs.add("Dies ist ein vierter Satz.«");
    // all paragraphs are text paragraphs (not footnotes, tables, etc.
    List<String> textParagraphs = new ArrayList<String>(paragraphs);
    List<int[]> footnotes = new ArrayList<int[]>();
    // no footnotes in text
    for (int i = 0; i < paragraphs.size(); i++) {
      footnotes.add(new int[0]);
    }
    // English text
    Locale locale = new Locale("de", "DE", "");
    // property value without any footnote
    // NOTE: an empty property value forces the extension to do an single paragraph check only
    PropertyValue[] propertyValues = { new PropertyValue("FootnotePositions", -1, new int[0], PropertyState.DIRECT_VALUE) };

    Main prog = new Main(null);
    prog.setTestMode(true);
    // one proof has to be done to initialize LT and the SingleDocument class
    ProofreadingResult paRes = prog.doProofreading("1", paragraphs.get(0), locale, 0, paragraphs.get(0).length(), propertyValues);
    assertEquals("1", paRes.aDocumentIdentifier);
    assertEquals(2, paRes.aErrors.length);  // This may be critical if rules changed
    assertTrue(paRes.aErrors[0].aRuleIdentifier.equals("UNPAIRED_BRACKETS"));
    assertTrue(paRes.aErrors[1].aRuleIdentifier.equals("DE_AGREEMENT"));
    MultiDocumentsHandler documents = prog.getMultiDocumentsHandler();
    SingleDocument document = documents.getDocuments().get(0);
    SwJLanguageTool langtool = documents.getLanguageTool();
    // disable all rules not needed for test
    // test rules are:
    // DE_AGREEMENT - test of rule on sentence level
    // WHITESPACE_RULE - test of rule on level of single paragraph
    // ENGLISH_WORD_REPEAT_BEGINNING_RULE - test of rule on level of three paragraphs
    // EN_UNPAIRED_BRACKETS - test of rule on level of chapter / full text
    // EN_QUOTES - negative test of rule on level of chapter / full text
    Set<String> enabledRules = new HashSet<String>();
    for (Rule rule : langtool.getAllActiveOfficeRules()) {
      if (!rule.getId().equals("DE_AGREEMENT") && !rule.getId().equals("WHITESPACE_RULE")
          && !rule.getId().equals("UNPAIRED_BRACKETS") && !rule.getId().equals("GERMAN_WORD_REPEAT_BEGINNING_RULE")) {
        langtool.disableRule(rule.getId());
      } else {
        enabledRules.add(rule.getId());
      }
    }
    assertEquals(4, enabledRules.size()); // test if all needed 4 rules are enabled 
    enabledRules.clear();
    for (Rule rule : langtool.getAllActiveOfficeRules()) {
      enabledRules.add(rule.getId());
    }
    assertEquals(4, enabledRules.size()); // test if not more than needed rules are enabled
    // set document cache of virtual document
    // NOTE: this step has to be done, when all other preparations are done
    document.setDocumentCacheForTests(paragraphs, textParagraphs, footnotes, locale);
    for (int i = 0; i < paragraphs.size(); i++) {
      paRes.nStartOfSentencePosition = 0;
      paRes.nBehindEndOfSentencePosition = paragraphs.get(i).length();
      paRes.nStartOfNextSentencePosition = paRes.nBehindEndOfSentencePosition;
      paRes = document.getCheckResults(paragraphs.get(i), locale, paRes, propertyValues, false, langtool, i);
      if (i == 0) {
        assertEquals(1, paRes.aErrors.length);
        assertTrue(paRes.aErrors[0].aRuleIdentifier.equals("DE_AGREEMENT"));
      } else if (i == 1) {
        assertEquals(1, paRes.aErrors.length);
        assertTrue(paRes.aErrors[0].aRuleIdentifier.equals("WHITESPACE_RULE"));
      } else if (i == 2) {
        assertEquals(1, paRes.aErrors.length);
        assertTrue(paRes.aErrors[0].aRuleIdentifier.equals("UNPAIRED_BRACKETS"));
      } else if (i == 3) {
        assertEquals(1, paRes.aErrors.length);
        assertTrue(paRes.aErrors[0].aRuleIdentifier.equals("GERMAN_WORD_REPEAT_BEGINNING_RULE"));
      }
    }
  }

//  @Ignore("see https://github.com/languagetool-org/languagetool/issues/4064")
  @Test
  public void testVariants() {
    Main prog = new Main(null);
    prog.setTestMode(true);
    String testString = "Sigui quina siga la teva intenció. Això és una prova.";
    // LibreOffice config for languages with variants
    Locale cavaLoc = new Locale("qlt", "ES", "ca-ES-valencia"); 
    PropertyValue[] prop = new PropertyValue[0];
    for (int i = 0; i <= testString.length(); i++) {
      ProofreadingResult paRes = prog.doProofreading("1", testString, cavaLoc, i, testString.length(), prop);
      assertEquals("1", paRes.aDocumentIdentifier);
      assertTrue(paRes.nStartOfNextSentencePosition >= i);
      if (i < "Sigui quina siga la teva intenció. ".length()) {
        assertEquals("Sigui quina siga la teva intenció. ".length(), paRes.nStartOfNextSentencePosition);
        assertEquals(0, paRes.nStartOfSentencePosition);
        //The test result depends on the CONFIG_FILE
        //assertEquals(2, paRes.aErrors.length);
      }
    }
    Locale caLoc = new Locale("ca", "ES", "");
    ProofreadingResult paRes = prog.doProofreading("1", testString, caLoc, 0, testString.length(), prop);
    assertEquals("1", paRes.aDocumentIdentifier);
    //assertEquals(1, paRes.aErrors.length);
  }

  @Test
  public void testCleanFootnotes() {
    Main main = new Main(null);
    main.setTestMode(true);
    assertEquals("A house.¹ Here comes more text.", SingleCheck.cleanFootnotes("A house.1 Here comes more text."));
    assertEquals("A road that's 3.4 miles long.", SingleCheck.cleanFootnotes("A road that's 3.4 miles long."));
    assertEquals("A house.1234 Here comes more text.", SingleCheck.cleanFootnotes("A house.1234 Here comes more text."));  // too many digits for a footnote
    String input    = "Das Haus.1 Hier kommt mehr Text2. Und nochmal!3 Und schon wieder ein Satz?4 Jetzt ist aber Schluss.";
    String expected = "Das Haus.¹ Hier kommt mehr Text2. Und nochmal!¹ Und schon wieder ein Satz?¹ Jetzt ist aber Schluss.";
    assertEquals(expected, SingleCheck.cleanFootnotes(input));
  }

}
