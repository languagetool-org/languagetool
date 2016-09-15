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
import org.languagetool.AnalyzedSentence;
import org.languagetool.Language;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tools.RuleMatchAsXmlSerializer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static org.languagetool.server.ServerTools.setCommonHeaders;

/**
 * Pseudo checker for v1 of the API, which returns a static XML with a message
 * that this version of the API isn't supported anymore (unless in AtD mode).
 * @since 3.5
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
      RuleMatch ruleMatch = new RuleMatch(new FakeRule(), 0, 1,
              "Internal error: The software you're using is making use of an old LanguageTool API. " +
              "Please ask the software developer to use the recent JSON API. Follow the link below for more information.");
      List<RuleMatch> ruleMatches;
      if (text.length() > 0) {
        ruleMatches = Collections.singletonList(ruleMatch);
      } else {
        ruleMatches = Collections.emptyList();
      }
      RuleMatchAsXmlSerializer serializer = new RuleMatchAsXmlSerializer();
      String xml = serializer.ruleMatchesToXml(ruleMatches, text, CONTEXT_SIZE, lang, motherTongue);
      return xml.replaceFirst("\\?>", "?>\n");
    }
  }
  
  static class FakeRule extends Rule {

    @Override
    public String getId() {
      return "API_EOL_PSEUDO_ID";
    }

    @Override
    public URL getUrl() {
      try {
        return new URL("https://languagetool.org/http-api/migration.php");
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public String getDescription() {
      return "A pseudo rule indicating the API is end-of-life";
    }

    @Override
    public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
      return new RuleMatch[0];
    }

    @Override
    public void reset() {}
  }
  
}
