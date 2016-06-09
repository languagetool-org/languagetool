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

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class CheckConfigurationBuilderTest {
  
  @Test
  public void test() {
    CheckConfiguration config1 = new CheckConfigurationBuilder("xx").build();
    assertThat(config1.getLangCode().get(), is("xx"));
    assertNull(config1.getMotherTongueLangCode());
    assertThat(config1.getEnabledRuleIds().size(), is(0));
    assertThat(config1.enabledOnly(), is(false));
    assertThat(config1.guessLanguage(), is(false));

    CheckConfiguration config2 = new CheckConfigurationBuilder()
            .setMotherTongueLangCode("mm")
            .enabledOnly()
            .enabledRuleIds(Arrays.asList("RULE1", "RULE2"))
            .disabledRuleIds(Arrays.asList("RULE3", "RULE4"))
            .build();
    assertFalse(config2.getLangCode().isPresent());
    assertThat(config2.getMotherTongueLangCode(), is("mm"));
    assertThat(config2.getEnabledRuleIds().toString(), is("[RULE1, RULE2]"));
    assertThat(config2.getDisabledRuleIds().toString(), is("[RULE3, RULE4]"));
    assertThat(config2.enabledOnly(), is(true));
    assertThat(config2.guessLanguage(), is(true));
  }

  @Test(expected = IllegalStateException.class)
  public void testInvalidConfig() {
    new CheckConfigurationBuilder("xx").enabledOnly().build();
  }

}