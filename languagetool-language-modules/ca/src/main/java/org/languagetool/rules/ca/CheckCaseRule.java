/* LanguageTool, a natural language style checker 
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.ca;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.languagetool.Language;
import org.languagetool.rules.AbstractCheckCaseRule;

public class CheckCaseRule  extends AbstractCheckCaseRule {

  
  private static final String FILE_NAME = "/ca/check_case.txt";
  private static final Locale CA_LOCALE = new Locale("ca");
  
  public CheckCaseRule(ResourceBundle messages, Language language) {
    super(messages, language);
  }

  @Override
  public List<String> getFileNames() {
    return Arrays.asList(FILE_NAME);
  }

  @Override
  public String getId() {
    return "CA_CHECKCASE";
  }

  @Override
  public String getDescription() {
    return "Comprova majúscules i minúscules";
  }

  @Override
  public String getShort() {
    return "Majúscules i minúscules";
  }

  @Override
  public String getMessage() {
    return "Majúscules i minúscules recomanades.";
  }
  
  @Override
  public Locale getLocale() {
    return CA_LOCALE;
  }

}
