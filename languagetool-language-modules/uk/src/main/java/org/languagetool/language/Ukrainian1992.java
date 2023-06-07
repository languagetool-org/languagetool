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

package org.languagetool.language;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.rules.uk.SimpleReplaceSpelling2019Rule;


// Only for testing for now
public class Ukrainian1992 extends Ukrainian {
  @Override
  public String getVariant() {
    return "";
    //return "1992";
  }

  @Override
  public String getName() {
    return "Ukrainian (1992)";
  }

  protected SimpleReplaceSpelling2019Rule getSpellingReplacementRule(ResourceBundle messages) throws IOException {
    return new SimpleReplaceSpelling2019Rule(messages);
  }

  @Override
  public List<String> getDefaultDisabledRulesForVariant() {
    return Arrays.asList("piv_okremo_2019", "consistency_numeric_fractional_2019");
  }
}
