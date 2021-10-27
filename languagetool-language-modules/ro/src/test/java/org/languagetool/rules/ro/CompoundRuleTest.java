/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.ro;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Romanian;
import org.languagetool.rules.AbstractCompoundRuleTest;

/**
 * @author Ionuț Păduraru
 */
public class CompoundRuleTest extends AbstractCompoundRuleTest {

  @Before
  public void setUp() throws Exception {
    lt = new JLanguageTool(new Romanian());
    rule = new CompoundRule(TestTools.getMessages("ro"), new Romanian(), null);
  }

  @Test
  public void testRule() throws IOException {
    // correct sentences:
    check(0, "Au plecat câteșitrei.");
    // incorrect sentences:
    check(1, "câte și trei", "câteșitrei");
    check(1, "Câte și trei", "Câteșitrei");
    // check(1, "Câte și trei", new String[] { "Câteșitrei" });
    check(1, "câte-și-trei", "câteșitrei");

    check(1, "tus trei", "tustrei");
    check(1, "tus-trei", "tustrei");
  }

}
