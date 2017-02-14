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
import org.jetbrains.annotations.NotNull;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.RuleMatch;

import java.util.*;

import static org.languagetool.server.ServerTools.setCommonHeaders;

/**
 * Checker for v1 of the API, which returns XML.
 * @since 3.4
 * @deprecated use {@link V2TextChecker}
 */
class V1TextChecker extends TextChecker {

  private static final String XML_CONTENT_TYPE = "text/xml; charset=UTF-8";

  V1TextChecker(HTTPServerConfig config, boolean internalServer) {
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

  @NotNull
  @Override
  protected List<String> getEnabledRuleIds(Map<String, String> parameters) {
    String enabledParam = parameters.get("enabled");
    List<String> enabledRules = new ArrayList<>();
    if (enabledParam != null) {
      enabledRules.addAll(Arrays.asList(enabledParam.split(",")));
    }
    return enabledRules;
  }

  @NotNull
  @Override
  protected List<String> getDisabledRuleIds(Map<String, String> parameters) {
    return getCommaSeparatedStrings("disabled", parameters);
  }

  @Override
  protected boolean getLanguageAutoDetect(Map<String, String> parameters) {
    if (config.getMode() == HTTPServerConfig.Mode.AfterTheDeadline) {
      return "true".equals(parameters.get("guess"));
    } else {
      boolean autoDetect = "1".equals(parameters.get("autodetect")) || "yes".equals(parameters.get("autodetect"));
      if (parameters.get("language") == null && !autoDetect) {
        throw new IllegalArgumentException("Missing 'language' parameter. Specify language or use 'autodetect=yes' for auto-detecting the language of the input text.");
      }
      return autoDetect;
    }
  }
  
  @Override
  @NotNull
  protected Language getLanguage(String text, Map<String, String> parameters, List<String> preferredVariants) {
    Language lang;
    if (getLanguageAutoDetect(parameters)) {
      lang = detectLanguageOfString(text, parameters.get("language"), preferredVariants);
    } else {
      if (config.getMode() == HTTPServerConfig.Mode.AfterTheDeadline) {
        lang = config.getAfterTheDeadlineLanguage();
        if (lang == null) {
          throw new RuntimeException("In AfterTheDeadline mode but AfterTheDeadline language not set");
        }
      } else {
        lang = Languages.getLanguageForShortCode(parameters.get("language"));
      }
    }
    return lang;
  }

  @Override
  @NotNull
  protected List<String> getPreferredVariants(Map<String, String> parameters) {
    List<String> preferredVariants;
    if (parameters.get("preferredvariants") != null) {
      preferredVariants = Arrays.asList(parameters.get("preferredvariants").split(",\\s*"));
      if (!getLanguageAutoDetect(parameters)) {
        throw new IllegalArgumentException("You specified 'preferredvariants' but you didn't specify 'autodetect=yes'");
      }
    } else {
      preferredVariants = Collections.emptyList();
    }
    return preferredVariants;
  }

}
