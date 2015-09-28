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
package org.languagetool.tools;

import java.net.URL;
import java.util.List;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.Category;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.PatternRule;

import static org.languagetool.tools.StringTools.*;

/**
 * Generate XML to represent matching rules.
 *
 * @since 2.5
 */
public class RuleAsXmlSerializer {

  private static final int CAPACITY = 200;

  /**
   * Get the string to begin the XML. After this, use {@link #ruleMatchesToXmlSnippet} and then {@link #getXmlEnd()}
   * or better, simply use {@link #ruleMatchesToXml}.
   */
  public String getXmlStart(Language lang, Language motherTongue) {
    StringBuilder xml = new StringBuilder(CAPACITY);
    xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            .append("<matches software=\"LanguageTool\" version=\"" + JLanguageTool.VERSION + "\"" + " buildDate=\"")
            .append(JLanguageTool.BUILD_DATE).append("\">\n");
    if (lang != null || motherTongue != null) {
      String languageXml = "<language ";
      if (lang != null) {
        languageXml += "shortname=\"" + lang.getShortNameWithCountryAndVariant() + "\" name=\"" + lang.getName() + "\"";
      }
      if(motherTongue != null && (lang == null || !motherTongue.getShortName().equals(lang.getShortNameWithCountryAndVariant()))) {
        languageXml += " mothertongueshortname=\"" + motherTongue.getShortName() + "\" mothertonguename=\"" + motherTongue.getName() + "\"";
      }
      languageXml += "/>\n";
      xml.append(languageXml);
    }
    return xml.toString();
  }

  /**
   * Get the string to end the XML. Use after {@link #ruleMatchesToXmlSnippet} and {@link #getXmlStart}.
   */
  public String getXmlEnd() {
    return "</matches>\n";
  }

  /**
   * Get the XML snippet (i.e. not a complete XML document) for the given rules.
   * @see #getXmlStart
   * @see #getXmlEnd()
   */
  public String ruleMatchesToXmlSnippet(List<RuleMatch> ruleMatches, String text, int contextSize) {
    StringBuilder xml = new StringBuilder(CAPACITY);
    //
    // IMPORTANT: people rely on this format, don't change it!
    //
    ContextTools contextTools = new ContextTools();
    contextTools.setEscapeHtml(false);
    contextTools.setContextSize(contextSize);
    String startMarker = "__languagetool_start_marker";
    contextTools.setErrorMarkerStart(startMarker);
    contextTools.setErrorMarkerEnd("");

    for (RuleMatch match : ruleMatches) {
      String subId = "";
      if (match.getRule() instanceof PatternRule) {
        PatternRule pRule = (PatternRule) match.getRule();
        if (pRule.getSubId() != null) {
          subId = " subId=\"" + escapeXMLForAPIOutput(pRule.getSubId()) + "\" ";
        }
      }
      xml.append("<error fromy=\"").append(match.getLine()).append('"')
              .append(" fromx=\"").append(match.getColumn() - 1).append('"')
              .append(" toy=\"").append(match.getEndLine()).append('"')
              .append(" tox=\"").append(match.getEndColumn() - 1).append('"')
              .append(" ruleId=\"").append(match.getRule().getId()).append('"');
      String msg = match.getMessage().replaceAll("</?suggestion>", "'");
      xml.append(subId);
      xml.append(" msg=\"").append(escapeXMLForAPIOutput(msg)).append('"');
      String context = contextTools.getContext(match.getFromPos(), match.getToPos(), text);
      xml.append(" replacements=\"").append(escapeXMLForAPIOutput(listToString(
              match.getSuggestedReplacements(), "#"))).append('"');
      // get position of error in context and remove artificial marker again:
      int contextOffset = context.indexOf(startMarker);
      context = context.replaceFirst(startMarker, "");
      context = context.replaceAll("[\n\r]", " ");
      xml.append(" context=\"").append(escapeForXmlAttribute(context)).append('"')
              .append(" contextoffset=\"").append(contextOffset).append('"')
              .append(" offset=\"").append(match.getFromPos()).append('"')
              .append(" errorlength=\"").append(match.getToPos() - match.getFromPos()).append('"');
      Category category = match.getRule().getCategory();
      if (category != null) {
        xml.append(" category=\"").append(escapeXMLForAPIOutput(category.getName())).append('"');
      }
      ITSIssueType type = match.getRule().getLocQualityIssueType();
      if (type != null) {
        xml.append(" locqualityissuetype=\"").append(escapeXMLForAPIOutput(type.toString())).append('"');
      }
      xml.append(">\n");
      for (URL url : match.getRule().getUrls()) {
        xml.append("<url>").append(escapeXMLForAPIOutput(url.toString())).append("</url>\n");
      }
      xml.append("</error>\n");
    }
    return xml.toString();
  }

  /**
   * Get an XML representation of the given rule matches.
   * @param text the original text that was checked, used to get the context of the matches
   * @param contextSize the desired context size in characters
   */
  public String ruleMatchesToXml(List<RuleMatch> ruleMatches, String text, int contextSize,
                                 Language lang, Language motherTongue) {
    return getXmlStart(lang, motherTongue) + ruleMatchesToXmlSnippet(ruleMatches, text, contextSize) + getXmlEnd();
  }

  /**
   * Get an XML representation of the given rule matches.
   * @param text the original text that was checked, used to get the context of the matches
   * @param contextSize the desired context size in characters
   */
  public String ruleMatchesToXml(List<RuleMatch> ruleMatches, String text, int contextSize, Language lang) {
    return getXmlStart(lang, null) + ruleMatchesToXmlSnippet(ruleMatches, text, contextSize) + getXmlEnd();
  }

  /**
   * Get an XML representation of the given rule matches.
   *
   * @param text the original text that was checked, used to get the context of the matches
   * @param contextSize the desired context size in characters
   * @param unknownWords unknown words to be printed in a separated list
   * @since 3.0
   */
  public String ruleMatchesToXml(List<RuleMatch> ruleMatches, String text, int contextSize, XmlPrintMode xmlMode, Language lang, List<String> unknownWords) {
    String xmlSnippet = ruleMatchesToXmlSnippet(ruleMatches, text, contextSize);
    switch (xmlMode) {
      case START_XML:
        return getXmlStart(lang, null) + xmlSnippet;
      case CONTINUE_XML:
        return xmlSnippet;
      case END_XML:
        return xmlSnippet + getXmlUnknownWords(unknownWords) + getXmlEnd();
      case NORMAL_XML:
        return getXmlStart(lang, null) + xmlSnippet + getXmlUnknownWords(unknownWords) + getXmlEnd();
    }
    throw new IllegalArgumentException("Unknown XML mode: " + xmlMode);
  }

  private String getXmlUnknownWords(List<String> unknownWords) {
    StringBuilder xml = new StringBuilder(CAPACITY);
    if (!unknownWords.isEmpty()) {
      xml.append("<unknown_words>\n");
      for (String word : unknownWords) {
        xml.append("    <word>");
        xml.append(escapeForXmlAttribute(word));
        xml.append("</word>\n");
      }
      xml.append("</unknown_words>\n");
    }
    return xml.toString();
  }

  private static String escapeXMLForAPIOutput(String s) {
    // this is simplified XML, i.e. put the "<error>" in one line:
    return escapeForXmlAttribute(s).replaceAll("[\n\r]", " ");
  }

}
