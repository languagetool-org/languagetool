/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.JLanguageTool;
import org.languagetool.rules.RuleMatch;

import java.util.List;

import static org.languagetool.tools.StringTools.escapeForXmlContent;

/**
 * Return LanguageTool matches in the same XML format as After The Deadline.
 * See the <a href="http://www.afterthedeadline.com/api.slp">AtD API documentation</a>.
 * @since 2.7
 */
@Deprecated
public class AtDXmlSerializer {
  
  public String ruleMatchesToXml(List<RuleMatch> matches, String text) {
    StringBuilder sb = new StringBuilder();
    sb.append("<results>\n");
    sb.append("<!-- THIS MODE HAS BEEN DEPRECATED - PLEASE USE THE STANDARD JSON MODE -->\n");
    sb.append("<!-- Server: LanguageTool " + JLanguageTool.VERSION + " (").append(JLanguageTool.BUILD_DATE).append(") -->\n");
    for (RuleMatch match : matches) {
      addRuleMatch(sb, match, text);
    }
    sb.append("</results>\n");
    return sb.toString();
  }

  private void addRuleMatch(StringBuilder sb, RuleMatch match, String text) {
    String errorText = text.substring(match.getFromPos(), match.getToPos());
    if (errorText.contains("(") || errorText.contains(")")) {
      // these matches need to be removed, they seem to confuse the client
      // so that no matches at all get shown...
      return;
    }
    sb.append("  <error>\n");
    sb.append("    <string>").append(escapeForXmlContent(errorText)).append("</string>\n");
    boolean hasShortMessage = match.getShortMessage() != null && match.getShortMessage().length() > 0;
    String cleanMessage = hasShortMessage ?
            match.getShortMessage() : match.getMessage().replace("<suggestion>", "'").replace("</suggestion>", "'");
    sb.append("    <description>").append(escapeForXmlContent(cleanMessage)).append("</description>\n");
    String preContext = getPreContext(text, match.getFromPos());
    if (preContext.isEmpty()) {
      sb.append("    <precontext/>\n");
    } else {
      sb.append("    <precontext>").append(escapeForXmlContent(preContext)).append("</precontext>\n");
    }
    if (match.getSuggestedReplacements().size() > 0) {
      sb.append("    <suggestions>\n");
      for (String suggestion : match.getSuggestedReplacements()) {
        sb.append("      <option>").append(escapeForXmlContent(suggestion)).append("</option>\n");
      }
      sb.append("    </suggestions>\n");
    }
    String type = match.getRule().isDictionaryBasedSpellingRule() ? "spelling" : "grammar";
    sb.append("    <type>").append(escapeForXmlContent(type)).append("</type>\n");
    // TODO: we return the URL of external pages here, but WordPress/Jetpack shows the page
    // in a window that's too small, so we have to disable this very nice feature for now...
    //if (rule.getUrl() != null) {
    //  sb.append("    <url>").append(escapeForXmlContent(rule.getUrl().toString())).append("</url>\n");
    //}
    sb.append("  </error>\n");
  }

  /** Get the context to the left of the given position. */
  String getPreContext(String text, int fromPos) {
    String preText = text.substring(0, fromPos);
    String[] parts = preText.trim().split("\\s");
    if (parts.length == 0) {
      return "";
    } else {
      String lastPart = parts[parts.length-1];
      // This seems a bit strange, but we (roughly) emulate AtD's behavior here. See AtD's error.slp.
      if (lastPart.matches(".*[()\\[\\],;:/-].*")) {
        return "";
      } else {
        return lastPart;
      }
    }
  }
  
}
