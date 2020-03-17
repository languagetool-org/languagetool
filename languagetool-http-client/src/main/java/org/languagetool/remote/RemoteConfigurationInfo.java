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

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * Check a text using a <a href="http://wiki.languagetool.org/http-server">remote LanguageTool server</a> via HTTP or HTTPS.
 * Our public HTTPS API and its restrictions are documented
 * <a href="http://wiki.languagetool.org/public-http-api">in our wiki</a>.
 * @since 4.8
 */
public class RemoteConfigurationInfo {

  private final int maxTextLength;
  private final Map<String,Object> softwareInfo;
  private final List<Map<String,String>> rules;
  
  @SuppressWarnings("unchecked")
  RemoteConfigurationInfo(ObjectMapper mapper, InputStream inputStream) throws IOException {
    Map<String, Object> map = mapper.readValue(inputStream, Map.class);
    softwareInfo = (Map<String,Object>) map.get("software");
    Map<String, Object> parameter = (Map<String, Object>) map.get("parameter");
    maxTextLength = (int)parameter.get("maxTextLength");
    rules = (List<Map<String, String>>) map.get("rules");
  }

  public Map<String,Object> getSoftwareInfo() {
    return softwareInfo;
  }
  
  public int getMaxTextLength () {
    return maxTextLength;
  }
  
  public List<Map<String,String>> getRemoteRules() {
    return rules;
  }

}
