/* LanguageTool, a natural language style checker
 * Copyright (C) 2017 Sohaib Afifi, Taha Zerrouki
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
package org.languagetool.rules.ar;

import java.io.IOException;
import java.util.ResourceBundle;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;

public final class MorfologikArabicSpellerRule extends MorfologikSpellerRule {

  public static final String RULE_ID = "MORFOLOGIK_RULE_AR";
  private static final String RESOURCE_FILENAME = "ar/hunspell/arabic.dict";

  public MorfologikArabicSpellerRule(ResourceBundle messages, Language language) throws IOException {
    super(messages, language);
  }

  @Override
  public String getFileName() {
    if (!JLanguageTool.getDataBroker().resourceExists(RESOURCE_FILENAME)) {
      throw new RuntimeException("Could not set up morfologik spell checker for Arabic");
    }
    return RESOURCE_FILENAME;
  }

  @Override
  public String getId() {
    return RULE_ID;
  }
  @Override
  protected String getIgnoreFileName() {
    return "ar/hunspell/ignore.txt";
  }

  @Override
  public String getSpellingFileName() {
    return "ar/hunspell/spelling.txt";
  }

  @Override
  protected String getProhibitFileName() {
    return "ar/hunspell/prohibit.txt";
  }
}