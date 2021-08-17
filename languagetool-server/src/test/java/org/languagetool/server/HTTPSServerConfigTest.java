/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.server;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@SuppressWarnings("ResultOfObjectAllocationIgnored")
public class HTTPSServerConfigTest {

  @Test
  public void testArgumentParsing() {
    try {
      new HTTPSServerConfig(new String[]{});
      fail();
    } catch (IllegalConfigurationException ignored) {}

    String propertyFile = HTTPSServerConfigTest.class.getResource("/org/languagetool/server/https-server.properties").getFile();

    HTTPSServerConfig config1 = new HTTPSServerConfig(("--public --config " + propertyFile).split(" "));
    assertThat(config1.getPort(), is(HTTPServerConfig.DEFAULT_PORT));
    assertThat(config1.isPublicAccess(), is(true));
    assertThat(config1.isVerbose(), is(false));
    assertThat(config1.getKeystore().toString().replace('\\', '/'), is("src/test/resources/org/languagetool/server/test-keystore.jks"));
    assertThat(config1.getKeyStorePassword(), is("mytest"));
    assertThat(config1.getMaxTextLengthAnonymous(), is(50000));

    HTTPSServerConfig config2 = new HTTPSServerConfig(("-p 9999 --config " + propertyFile).split(" "));
    assertThat(config2.getPort(), is(9999));
    assertThat(config2.isPublicAccess(), is(false));
    assertThat(config2.isVerbose(), is(false));
    assertThat(config2.getKeystore().toString().replace('\\', '/'), is("src/test/resources/org/languagetool/server/test-keystore.jks"));
    assertThat(config2.getKeyStorePassword(), is("mytest"));
    assertThat(config2.getMaxTextLengthAnonymous(), is(50000));
  }

  @Test
  public void testMinimalPropertyFile() {
    String propertyFile = HTTPSServerConfigTest.class.getResource("/org/languagetool/server/https-server-minimal.properties").getFile();
    HTTPSServerConfig config = new HTTPSServerConfig(("--config " + propertyFile).split(" "));
    assertThat(config.getPort(), is(8081));
    assertThat(config.isPublicAccess(), is(false));
    assertThat(config.isVerbose(), is(false));
    assertThat(config.getKeystore().toString().replace('\\', '/'), is("src/test/resources/org/languagetool/server/test-keystore.jks"));
    assertThat(config.getKeyStorePassword(), is("mytest"));
    assertThat(config.getMaxTextLengthAnonymous(), is(Integer.MAX_VALUE));
  }

  @Test
  public void testMissingPropertyFile() {
    String propertyFile = "/does-not-exist";
    try {
      new HTTPSServerConfig(("--config " + propertyFile).split(" "));
      fail();
    } catch (Exception ignored) {}
  }

  @Test
  public void testIncompletePropertyFile() {
    String propertyFile = HTTPSServerConfigTest.class.getResource("/org/languagetool/server/https-server-incomplete.properties").getFile();
    try {
      new HTTPSServerConfig(("--config " + propertyFile).split(" "));
      fail();
    } catch (IllegalConfigurationException ignored) {}
  }

}
