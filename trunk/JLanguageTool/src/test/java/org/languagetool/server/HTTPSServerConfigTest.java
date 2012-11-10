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

public class HTTPSServerConfigTest {

  @Test
  public void testArgumentParsing() {
    try {
      new HTTPSServerConfig(new String[]{});
      fail();
    } catch (IllegalArgumentException expected) {}

    final HTTPSServerConfig config1 = new HTTPSServerConfig("--public --keystore foo --password xxx".split(" "));
    assertThat(config1.getPort(), is(HTTPServerConfig.DEFAULT_PORT));
    assertThat(config1.isPublicAccess(), is(true));
    assertThat(config1.isVerbose(), is(false));
    assertThat(config1.getKeystore().getName(), is("foo"));
    assertThat(config1.getKeyStorePassword(), is("xxx"));

    final HTTPSServerConfig config2 = new HTTPSServerConfig("-p 9999 --keystore /tmp/foo.kjs --password pwd".split(" "));
    assertThat(config2.getPort(), is(9999));
    assertThat(config2.isPublicAccess(), is(false));
    assertThat(config2.isVerbose(), is(false));
    assertThat(config2.getKeystore().getAbsolutePath(), is("/tmp/foo.kjs"));
    assertThat(config2.getKeyStorePassword(), is("pwd"));
  }

}
