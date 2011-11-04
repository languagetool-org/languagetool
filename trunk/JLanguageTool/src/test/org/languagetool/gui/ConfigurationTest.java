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
package de.danielnaber.languagetool.gui;

import junit.framework.TestCase;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ConfigurationTest extends TestCase {

  public void testSaveAndLoadConfiguration() throws Exception {
    final File tempFile = File.createTempFile(ConfigurationTest.class.getSimpleName(), ".cfg");
    createConfiguration(tempFile);
    try {
      final Configuration conf = new Configuration(tempFile.getParentFile(), tempFile.getName());
      final Set<String> disabledRuleIds = conf.getDisabledRuleIds();
      assertTrue(disabledRuleIds.contains("FOO1"));
      assertTrue(disabledRuleIds.contains("Foo2"));
      assertEquals(2, disabledRuleIds.size());
      final Set<String> enabledRuleIds = conf.getEnabledRuleIds();
      assertTrue(enabledRuleIds.contains("enabledRule"));
      assertEquals(1, enabledRuleIds.size());
    } finally {
       tempFile.delete();
    }
  }
  
  private void createConfiguration(File configFile) throws Exception {
    final Configuration conf = new Configuration(configFile.getParentFile(), configFile.getName());
    conf.setDisabledRuleIds(new HashSet<String>(Arrays.asList("FOO1", "Foo2")));
    conf.setEnabledRuleIds(new HashSet<String>(Arrays.asList("enabledRule")));
    conf.saveConfiguration();
  }

}
