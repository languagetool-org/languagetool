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
package org.languagetool.rules.fr;

import org.junit.jupiter.api.Test;
import org.languagetool.LanguageSpecificTest;
import org.languagetool.language.French;

import java.io.IOException;
import java.util.Arrays;

public class FrenchTest extends LanguageSpecificTest {
  
  @Test
  public void testLanguage() throws IOException {
    // NOTE: this text needs to be kept in sync with WelcomeController.php's getDefaultDemoTexts():
    String s = "LanguageTool offre une vérification orthographique, grammaticale et de style. Il vous suffit de coller votre texte ici et de cliquer sur le bouton \\\"Vérifier le texte\\\". En voici quelques exemples : Cliqez sur les phrases en couleurs pour plus de détails sur les erreurs potentiels, ou utilisez ce texte pour voir quelques-uns des problèmes que LanguageTool Plus peut détecter. Que pensez vous des des correcteurs grammaticaux ? Non pas qu'ils soient parfaits bien sur. Également pour des erreurs plus communes : Il est 17h de l'après-midi. Il faisait beau le jeudi 27 juin 2017.";
    French lang = new French();
    testDemoText(lang, s,
      Arrays.asList("FR_SPELLING_RULE", "ACCORD_GENRE", "TRAIT_UNION_INVERSION", "FRENCH_WORD_REPEAT_RULE", "SUR_ACCENT", "HEURES", "DATE_JOUR")
    );
    runTests(lang);
  }
}
