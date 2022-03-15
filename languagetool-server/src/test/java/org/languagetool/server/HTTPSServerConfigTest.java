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

import org.junit.jupiter.api.Test;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;

import static org.hamcrest.core.Is.is;

@SuppressWarnings("ResultOfObjectAllocationIgnored")
public class HTTPSServerConfigTest {

  @Test
  public void testArgumentParsing() {
    try {
      new HTTPSServerConfig(new String[]{});
      Assertions.fail();
    } catch (IllegalConfigurationException ignored) {}

    String propertyFile = HTTPSServerConfigTest.class.getResource("/org/languagetool/server/https-server.properties").getFile();

    HTTPSServerConfig config1 = new HTTPSServerConfig(("--public --config " + propertyFile).split(" "));
    MatcherAssert.assertThat(config1.getPort(), is(HTTPServerConfig.DEFAULT_PORT));
    MatcherAssert.assertThat(config1.isPublicAccess(), is(true));
    MatcherAssert.assertThat(config1.isVerbose(), is(false));
    MatcherAssert.assertThat(config1.getKeystore().toString().replace('\\', '/'), is("src/test/resources/org/languagetool/server/test-keystore.jks"));
    MatcherAssert.assertThat(config1.getKeyStorePassword(), is("mytest"));
    MatcherAssert.assertThat(config1.getMaxTextLengthAnonymous(), is(50000));

    HTTPSServerConfig config2 = new HTTPSServerConfig(("-p 9999 --config " + propertyFile).split(" "));
    MatcherAssert.assertThat(config2.getPort(), is(9999));
    MatcherAssert.assertThat(config2.isPublicAccess(), is(false));
    MatcherAssert.assertThat(config2.isVerbose(), is(false));
    MatcherAssert.assertThat(config2.getKeystore().toString().replace('\\', '/'), is("src/test/resources/org/languagetool/server/test-keystore.jks"));
    MatcherAssert.assertThat(config2.getKeyStorePassword(), is("mytest"));
    MatcherAssert.assertThat(config2.getMaxTextLengthAnonymous(), is(50000));
  }

  @Test
  public void testMinimalPropertyFile() {
    String propertyFile = HTTPSServerConfigTest.class.getResource("/org/languagetool/server/https-server-minimal.properties").getFile();
    HTTPSServerConfig config = new HTTPSServerConfig(("--config " + propertyFile).split(" "));
    MatcherAssert.assertThat(config.getPort(), is(8081));
    MatcherAssert.assertThat(config.isPublicAccess(), is(false));
    MatcherAssert.assertThat(config.isVerbose(), is(false));
    MatcherAssert.assertThat(config.getKeystore().toString().replace('\\', '/'), is("src/test/resources/org/languagetool/server/test-keystore.jks"));
    MatcherAssert.assertThat(config.getKeyStorePassword(), is("mytest"));
    MatcherAssert.assertThat(config.getMaxTextLengthAnonymous(), is(Integer.MAX_VALUE));
  }

  @Test
  public void testMissingPropertyFile() {
    String propertyFile = "/does-not-exist";
    try {
      new HTTPSServerConfig(("--config " + propertyFile).split(" "));
      Assertions.fail();
    } catch (Exception ignored) {}
  }

  @Test
  public void testIncompletePropertyFile() {
    String propertyFile = HTTPSServerConfigTest.class.getResource("/org/languagetool/server/https-server-incomplete.properties").getFile();
    try {
      new HTTPSServerConfig(("--config " + propertyFile).split(" "));
      Assertions.fail();
    } catch (IllegalConfigurationException ignored) {}
  }

}
