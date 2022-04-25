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

import org.junit.Test;
import org.languagetool.LanguageSpecificTest;
import org.languagetool.language.French;

import java.io.IOException;
import java.util.Arrays;

public class FrenchTest extends LanguageSpecificTest {
  
  @Test
  public void testLanguage() throws IOException {
    // NOTE: this text needs to be kept in sync with config.ts -> DEMO_TEXTS:
    String s = "Écrivez ou collez votre texte ici pour le faire vérifier en continue. Les erreurs seront soulignés de différentes couleurs : les erreurs d'orthografe en rouge et les erreurs grammaticaux en jaune. Les problèmes de style, comme par exemple ceci, seront marqués en bleu dans vos textes. Le saviez vous ? LanguageTool vous propose des synonymes lorsque vous double-cliquez sur un mot .  Découvrez la multitude de ses fonctions, parfoi inattendues, tel que ça vérification des date. Par exemple, le mercredi 28 août 2020 était en fait un vendredi !";
    French lang = new French();
    testDemoText(lang, s,
      Arrays.asList("EN_GENERALE", "ETRE_VPPA_OU_ADJ", "FR_SPELLING_RULE", "AGREEMENT_POSTPONED_ADJ", "COMME_PAR_EXEMPLE", "TRAIT_UNION_INVERSION", "COMMA_PARENTHESIS_WHITESPACE", "WHITESPACE_RULE", "FR_SPELLING_RULE", "CA_SA", "D_N", "DATE_JOUR")
    );
    runTests(lang);
  }
}
