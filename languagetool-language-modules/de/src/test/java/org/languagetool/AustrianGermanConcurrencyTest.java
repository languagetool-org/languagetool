/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Stefan Lotties
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
package org.languagetool;

import org.languagetool.language.AbstractLanguageConcurrencyTest;
import org.languagetool.language.AustrianGerman;

public class AustrianGermanConcurrencyTest extends AbstractLanguageConcurrencyTest {
  @Override
  protected Language createLanguage() {
    return new AustrianGerman();
  }

  @Override
  protected String createSampleText() {
    return " \"Auch wenn Deine xleinen Füße die Erde nie berührten, sind deine Spuren trotzdem da überall.\"\n \n";
  }
}
