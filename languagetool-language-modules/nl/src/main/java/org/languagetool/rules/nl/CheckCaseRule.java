/* LanguageTool, a natural language style checker 
 * Copyright (C) 2021 Jaume Ortolà (http://www.danielnaber.de)
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
package org.languagetool.rules.nl;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.languagetool.Language;
import org.languagetool.rules.AbstractCheckCaseRule;

public class CheckCaseRule  extends AbstractCheckCaseRule {

  
  private static final String FILE_NAME = "/nl/check_case.txt";
  private static final Locale NL_LOCALE = new Locale("nl");
  
  public CheckCaseRule(ResourceBundle messages, Language language) {
    super(messages, language);
    //this.setDefaultTempOff();
  }

  @Override
  public List<String> getFileNames() {
    return Arrays.asList(FILE_NAME);
  }

  @Override
  public String getId() {
    return "NL_CHECKCASE";
  }

  @Override
  public String getDescription() {
    return "Controle op hoofd- en kleine letters";
  }

  @Override
  public String getShort() {
    return "Schrijfwijze";
  }

  @Override
  public String getMessage() {
    return "Aanbevolen schrijfwijze";
  }
  
  @Override
  public Locale getLocale() {
    return NL_LOCALE;
  }

}
