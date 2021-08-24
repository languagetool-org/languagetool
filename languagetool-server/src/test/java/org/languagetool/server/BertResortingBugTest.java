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

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.tools.Tools;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

public class BertResortingBugTest {

  @Test
  @Ignore("used to show bug #2969 - for interactive use, requires server")
  public void testHttpApi() throws IOException {
    String s = "A teext.\nAn errör-free text.\nSo much teext.";  // must exactly match 'data'
    String data = "{\"annotation\":[{\"text\":\"A teext.\"},{\"markup\":\"\\n\",\"interpretAs\":\"\\n\\n\"},{\"text\":\"An errör-free text.\"},{\"markup\":\"\\n\",\"interpretAs\":\"\\n\\n\"},{\"text\":\"So much teext.\"}]}";
    String server = "http://localhost:8081";
    String url = server + "/v2/check?data=" + URLEncoder.encode(data, "utf-8") + "&language=en-US";
    String json = HTTPTestTools.checkAtUrl(Tools.getUrl(url));
    ObjectMapper mapper = new ObjectMapper();
    Map map = mapper.readValue(json, Map.class);
    List matches = (List) map.get("matches");
    for (Object match : matches) {
      Map m = (Map) match;
      int offset = Integer.parseInt(m.get("offset").toString());
      int length = Integer.parseInt(m.get("length").toString());
      // crashes with "String index out of range" if server uses BERT re-sorting:
      System.out.println(s.substring(offset, offset + length));
    }
  }

}
