/* LanguageTool, a natural language style checker 
 * Copyright (C) 2011 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.gui;

import org.junit.Test;
import org.languagetool.Language;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.language.French;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConfigurationTest {

  @Test
  public void testSaveAndLoadConfiguration() throws Exception {
    File tempFile = File.createTempFile(ConfigurationTest.class.getSimpleName(), ".cfg");
    createConfiguration(tempFile, null);
    try {
      Configuration conf = new Configuration(tempFile.getParentFile(), tempFile.getName(), null);
      Set<String> disabledRuleIds = conf.getDisabledRuleIds();
      assertTrue(disabledRuleIds.contains("FOO1"));
      assertTrue(disabledRuleIds.contains("Foo2"));
      assertEquals(2, disabledRuleIds.size());
      Set<String> enabledRuleIds = conf.getEnabledRuleIds();
      assertTrue(enabledRuleIds.contains("enabledRule"));
      assertEquals(1, enabledRuleIds.size());
    } finally {
      Files.delete(tempFile.toPath());
    }
  }

  private void createConfiguration(File configFile, Language lang) throws Exception {
    Configuration conf = new Configuration(configFile.getParentFile(), configFile.getName(), lang);
    conf.setDisabledRuleIds(new HashSet<>(Arrays.asList("FOO1", "Foo2")));
    conf.setEnabledRuleIds(new HashSet<>(Arrays.asList("enabledRule")));
    conf.saveConfiguration(lang);
  }

  @Test
  public void testSaveAndLoadConfigurationForManyLanguages() throws Exception {
    File tempFile = File.createTempFile(ConfigurationTest.class.getSimpleName(), ".cfg");
    createConfiguration(tempFile, new AmericanEnglish());
    try {
      Configuration conf = new Configuration(tempFile.getParentFile(), tempFile.getName(),
              new AmericanEnglish());
      Set<String> disabledRuleIds = conf.getDisabledRuleIds();
      assertTrue(disabledRuleIds.contains("FOO1"));
      assertTrue(disabledRuleIds.contains("Foo2"));
      assertEquals(2, disabledRuleIds.size());
      Set<String> enabledRuleIds = conf.getEnabledRuleIds();
      assertTrue(enabledRuleIds.contains("enabledRule"));
      assertEquals(1, enabledRuleIds.size());

      //now change language
      conf = new Configuration(tempFile.getParentFile(), tempFile.getName(),
              new French());
      disabledRuleIds = conf.getDisabledRuleIds();
      assertTrue(disabledRuleIds.isEmpty());
      enabledRuleIds = conf.getEnabledRuleIds();
      assertTrue(enabledRuleIds.isEmpty());

      conf.setEnabledRuleIds(new HashSet<>(Arrays.asList("enabledFRRule")));
      conf.saveConfiguration(new French());

      //and back...
      conf = new Configuration(tempFile.getParentFile(), tempFile.getName(),
              new AmericanEnglish());
      disabledRuleIds = conf.getDisabledRuleIds();
      assertTrue(disabledRuleIds.contains("FOO1"));
      assertTrue(disabledRuleIds.contains("Foo2"));
      assertEquals(2, disabledRuleIds.size());
      enabledRuleIds = conf.getEnabledRuleIds();
      assertTrue(enabledRuleIds.contains("enabledRule"));
      assertEquals(1, enabledRuleIds.size());


    } finally {
      Files.delete(tempFile.toPath());
    }
  }

}
