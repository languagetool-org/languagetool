/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class ServerToolsTest {

  @Test
  public void testCleanUserTextFromMessage() {
    Map<String, String> loggingOn = new HashMap<>();
    Map<String, String> loggingOff = new HashMap<>();
    loggingOff.put("inputLogging", "no");
    assertThat(ServerTools.cleanUserTextFromMessage("my test", loggingOn), is("my test"));
    assertThat(ServerTools.cleanUserTextFromMessage("my test", loggingOff), is("my test"));
    assertThat(ServerTools.cleanUserTextFromMessage("<sentcontent>my test</sentcontent>", loggingOn), is("<sentcontent>my test</sentcontent>"));
    assertThat(ServerTools.cleanUserTextFromMessage("<sentcontent>my test</sentcontent>", loggingOff), is("<< content removed >>"));
    assertThat(ServerTools.cleanUserTextFromMessage("<sentcontent>my\ntest</sentcontent>", loggingOff), is("<< content removed >>"));
  }

}
