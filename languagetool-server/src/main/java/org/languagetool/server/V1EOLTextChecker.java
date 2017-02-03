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
package org.languagetool.server;

import com.sun.net.httpserver.HttpExchange;
import org.languagetool.Language;
import org.languagetool.rules.RuleMatch;

import java.util.List;

import static org.languagetool.server.ServerTools.setCommonHeaders;

/**
 * Pseudo checker for v1 of the API, which returns a static XML with a message
 * that this version of the API isn't supported anymore (unless in AtD mode).
 * @since 3.5
 * @deprecated use {@link V2TextChecker}
 */
class V1EOLTextChecker extends V1TextChecker {

  private static final String XML_CONTENT_TYPE = "text/xml; charset=UTF-8";
  
  V1EOLTextChecker(HTTPServerConfig config, boolean internalServer) {
    super(config, internalServer);
  }

  @Override
  protected void setHeaders(HttpExchange httpExchange) {
    setCommonHeaders(httpExchange, XML_CONTENT_TYPE, config.allowOriginUrl);
  }

  @Override
  protected String getResponse(String text, Language lang, Language motherTongue, List<RuleMatch> matches) {
    if (config.getMode() == HTTPServerConfig.Mode.AfterTheDeadline) {
      AtDXmlSerializer serializer = new AtDXmlSerializer();
      return serializer.ruleMatchesToXml(matches, text);
    } else {
      throw new RuntimeException("This API version is not supported anymore.");
    }
  }
  
}
