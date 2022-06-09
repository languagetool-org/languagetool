/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.eval;

import org.languagetool.Language;
import org.languagetool.language.identifier.LanguageIdentifier;
import org.languagetool.language.identifier.LanguageIdentifierService;

/**
 * Manually test the language detector.
 */
class LanguageDetectionEval2 {

  //private final static String text = "Als sie oben auf dem Felsen ankamen, waren die jungen";  // detected as DE
  //private final static String text = "Als sie oben auf dem Felsen ankamen, waren die junge";  // not detected anymore with default settings
  // Not properly detected with shortTextAlgorithm(500):
  private final static String text = "Den Vogel kennt man am Gesang, den Topf an dem Klang, den Esel an den Ohren und am Gesang den Toren.";

  public static void main(String[] args) {
    LanguageIdentifier defaultLanguageIdentifier = LanguageIdentifierService.INSTANCE.getDefaultLanguageIdentifier(0, null, null, null);
    Language detectedLangObj = defaultLanguageIdentifier.detectLanguage(text);
    System.out.println("'" + text + "'");
    System.out.println("=> " + detectedLangObj);
  }

}
