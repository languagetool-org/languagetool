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
package org.languagetool.gui;

import org.apache.commons.lang.StringUtils;
import org.languagetool.JLanguageTool;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.tools.StringTools;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * Area where the result of text checking is displayed.
 */
class ResultArea extends JTextPane {

  private static final String DEACTIVATE_URL = "http://languagetool.org/deactivate/";
  private static final String REACTIVATE_URL = "http://languagetool.org/reactivate/";
  private static final String LT_ERROR_MARKER_START = "<b><font bgcolor=\"#d7d7ff\">";
  private static final String SPELL_ERROR_MARKER_START = "<b><font bgcolor=\"#ffd7d7\">";

  private final ResourceBundle messages;
  private final JTextArea textArea;

  private Configuration config;
  private String inputText;
  private String startText;
  private List<RuleMatch> allRuleMatches;
  private List<RuleMatch> ruleMatches;    // will be filtered to not show disabled rules
  private long runTime;
  private JLanguageTool languageTool;

  ResultArea(ResourceBundle messages, JTextArea textArea, Configuration config) {
    this.messages = messages;
    this.textArea = textArea;
    this.config = config;
    setContentType("text/html");
    setText(Main.HTML_GREY_FONT_START + messages.getString("resultAreaText") + Main.HTML_FONT_END);
    setEditable(false);
    addHyperlinkListener(new MyHyperlinkListener());
    setTransferHandler(new RetainLineBreakTransferHandler());
  }

  String getRuleMatchHtml(List<RuleMatch> ruleMatches, String text, String startCheckText) {
    final ContextTools contextTools = new ContextTools();
    final StringBuilder sb = new StringBuilder();
    sb.append(startCheckText);
    sb.append("<br>\n");
    int i = 0;
    for (final RuleMatch match : ruleMatches) {
      final String output = Tools.makeTexti18n(messages, "result1",
          new Object[] {i + 1, match.getLine() + 1, match.getColumn()});
      sb.append(output);
      final String msg = match.getMessage()
          .replaceAll("<suggestion>", "<b>").replaceAll("</suggestion>", "</b>")
          .replaceAll("<old>", "<b>").replaceAll("</old>", "</b>");
      sb.append("<b>").append(messages.getString("errorMessage")).append("</b> ");
      sb.append(msg);
      final RuleLink ruleLink = RuleLink.buildDeactivationLink(match.getRule());
      sb.append(" <a href=\"").append(ruleLink).append("\">").append(messages.getString("deactivateRule")).append("</a><br>\n");
      if (match.getSuggestedReplacements().size() > 0) {
        final String replacement = StringTools.listToString(match.getSuggestedReplacements(), "; ");
        sb.append("<b>").append(messages.getString("correctionMessage")).append("</b> ").append(replacement).append("<br>\n");
      }
      if (match.getRule() instanceof SpellingCheckRule) {
        contextTools.setErrorMarkerStart(SPELL_ERROR_MARKER_START);
      } else {
        contextTools.setErrorMarkerStart(LT_ERROR_MARKER_START);
      }
      final String context = contextTools.getContext(match.getFromPos(), match.getToPos(), text);
      sb.append("<b>").append(messages.getString("errorContext")).append("</b> ").append(context);
      sb.append("<br>\n");
      if (match.getRule().getUrl() != null && Desktop.isDesktopSupported()) {
    	  sb.append("<b>").append(messages.getString("moreInfo")).append("</b> <a href=\"");
        final String url = match.getRule().getUrl().toString();
        sb.append(url);
        final String shortUrl = StringUtils.abbreviate(url, 60);
    	  sb.append("\">").append(shortUrl).append("</a><br>\n");
      }
      i++;
    }
    sb.append(Main.HTML_GREY_FONT_START);
    sb.append(getDisabledRulesHtml());
    final String checkDone = Tools.makeTexti18n(messages, "checkDone", new Object[] {ruleMatches.size(), runTime});
    sb.append("<br>\n").append(checkDone);
    sb.append(Main.HTML_FONT_END).append("<br>\n");
    return sb.toString();
  }

  private String getDisabledRulesHtml() {
    final StringBuilder sb = new StringBuilder();
    sb.append(messages.getString("deactivatedRulesText"));
    int i = 0;
    int deactivatedRuleCount = 0;
    for (String ruleId : config.getDisabledRuleIds()) {
      if (ruleId.trim().isEmpty()) {
        continue;
      }
      final Rule rule = getRuleForId(ruleId);
      if (rule == null || rule.isDefaultOff()) {
        continue;
      }
      if (i++ > 0) {
        sb.append(",");
      }
      final RuleLink reactivationLink = RuleLink.buildReactivationLink(rule);
      sb.append(" <a href=\"").append(reactivationLink).append("\">").append(rule.getDescription()).append("</a>");
      deactivatedRuleCount++;
    }
    sb.append("<br>");
    if (deactivatedRuleCount == 0) {
      return "";
    } else {
      return sb.toString();
    }
  }

  private Rule getRuleForId(String ruleId) {
    final List<Rule> allRules = languageTool.getAllRules();
    for (Rule rule : allRules) {
      if (rule.getId().equals(ruleId)) {
        return rule;
      }
    }
    return null;
  }

  void setInputText(String inputText) {
    this.inputText = inputText;
  }

  void setStartText(String startText) {
    this.startText = startText;
  }

  void setRunTime(long runTime) {
    this.runTime = runTime;
  }

  void setRuleMatches(List<RuleMatch> ruleMatches) {
    this.allRuleMatches = new ArrayList<RuleMatch>(ruleMatches);
    this.ruleMatches = new ArrayList<RuleMatch>(ruleMatches);
  }

  void displayResult() {
    ruleMatches = filterRuleMatches();
    final String ruleMatchHtml = getRuleMatchHtml(ruleMatches, inputText, startText);
    displayText(ruleMatchHtml);
  }

  void displayText(String text) {
    setText(Main.HTML_FONT_START + text + Main.HTML_FONT_END);
    setCaretPosition(0);
  }

  void setConfiguration(Configuration config) {
    this.config = config;
  }

  void setLanguageTool(JLanguageTool languageTool) {
    this.languageTool = languageTool;
  }

  private List<RuleMatch> filterRuleMatches() {
    final List<RuleMatch> filtered = new ArrayList<RuleMatch>();
    final Set<String> disabledRuleIds = config.getDisabledRuleIds();
    for (RuleMatch ruleMatch : allRuleMatches) {
      if (!disabledRuleIds.contains(ruleMatch.getRule().getId())) {
        filtered.add(ruleMatch);
      }
    }
    return filtered;
  }

  private class MyHyperlinkListener implements HyperlinkListener {

    private MyHyperlinkListener() {
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent e) {
      if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        final URL url = e.getURL();
        try {
          final String uri = url.toURI().toString();
          if (uri.startsWith(DEACTIVATE_URL) || uri.startsWith(REACTIVATE_URL)) {
            handleRuleLinkClick(uri);
          } else {
            handleHttpClick(url);
          }
        } catch (Exception ex) {
          throw new RuntimeException("Could not handle URL click: " + url, ex);
        }
      }
    }

    private void handleRuleLinkClick(String uri) throws IOException {
      final Cursor prevCursor = getCursor();
      setCursor(new Cursor(Cursor.WAIT_CURSOR));
      try {
        final RuleLink ruleLink = RuleLink.getFromString(uri);
        final String ruleId = ruleLink.getId();
        final Set<String> disabledRuleIds = config.getDisabledRuleIds();
        if (uri.startsWith(DEACTIVATE_URL)) {
          disabledRuleIds.add(ruleId);
          languageTool.disableRule(ruleId);
        } else {
          disabledRuleIds.remove(ruleId);
          languageTool.enableRule(ruleId);
        }
        config.setDisabledRuleIds(disabledRuleIds);
        config.saveConfiguration(languageTool.getLanguage());
        allRuleMatches = languageTool.check(textArea.getText());
        reDisplayRuleMatches();
      } finally {
        setCursor(prevCursor);
      }
    }

    private void reDisplayRuleMatches() {
      ruleMatches = filterRuleMatches();
      setInputText(textArea.getText());
      displayResult();
    }

    private void handleHttpClick(URL url) {
      if (Desktop.isDesktopSupported()) {
        try {
          final Desktop desktop = Desktop.getDesktop();
          desktop.browse(url.toURI());
        } catch (Exception ex) {
          throw new RuntimeException("Could not open URL: " + url, ex);
        }
      }
    }

  }

}
