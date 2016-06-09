/* LanguageTool, a natural language style checker
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.remote;

import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class CheckConfigurationTest {

  @Test
  public void test() {
    CheckConfiguration config = new CheckConfiguration("en", null, false, 
            Collections.emptyList(), false, Collections.emptyList());
    assertThat(config.getLangCode().get(), is("en"));
    assertNull(config.getMotherTongueLangCode());
    assertThat(config.getEnabledRuleIds().size(), is(0));
    assertThat(config.enabledOnly(), is(false));
    assertThat(config.guessLanguage(), is(false));
    assertThat(config.getDisabledRuleIds().size(), is(0));
  }

  @SuppressWarnings("ResultOfObjectAllocationIgnored")
  @Test(expected = IllegalArgumentException.class)
  public void testNull() {
    new CheckConfiguration(null, null, false, null, false, null);
  }

}
