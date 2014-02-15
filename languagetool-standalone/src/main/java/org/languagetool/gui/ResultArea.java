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

import java.awt.Cursor;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.apache.commons.lang.StringUtils;
import org.languagetool.Language;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tools.ContextTools;
import org.languagetool.tools.StringTools;

/**
 * Area where the result of text checking is displayed.
 */
class ResultArea {

  private static final String DEACTIVATE_URL = "http://languagetool.org/deactivate/";
  private static final String REACTIVATE_URL = "http://languagetool.org/reactivate/";
  private static final String LT_ERROR_MARKER_START = "<b><font bgcolor=\"#d7d7ff\">";
  private static final String SPELL_ERROR_MARKER_START = "<b><font bgcolor=\"#ffd7d7\">";

  private final ResourceBundle messages;
  private final JTextPane statusPane;
  private final LanguageToolSupport ltSupport;
  private final Object marker = new Object();

  private String inputText;
  private String startText;
  private List<RuleMatch> allRuleMatches;
  private List<RuleMatch> ruleMatches;    // will be filtered to not show disabled rules
  private long runTime;

  ResultArea(final ResourceBundle messages, final LanguageToolSupport ltSupport, final JTextPane statusPane) {
    this.messages = messages;
    this.ltSupport = ltSupport;
    this.statusPane = statusPane;
    statusPane.setContentType("text/html");
    statusPane.setText(Main.HTML_GREY_FONT_START + messages.getString("resultAreaText") + Main.HTML_FONT_END);
    statusPane.setEditable(false);
    statusPane.addHyperlinkListener(new MyHyperlinkListener());
    statusPane.setTransferHandler(new RetainLineBreakTransferHandler());
    ltSupport.addLanguageToolListener(new LanguageToolListener() {
      @Override
      public void languageToolEventOccurred(LanguageToolEvent event) {
        if (event.getType() == LanguageToolEvent.Type.CHECKING_STARTED) {
          final Language lang = ltSupport.getLanguageTool().getLanguage();
          final String langName;
          if (lang.isExternal()) {
            langName = lang.getTranslatedName(messages) + Main.EXTERNAL_LANGUAGE_SUFFIX;
          } else {
            langName = lang.getTranslatedName(messages);
          }
          final String startCheckText = Main.HTML_GREY_FONT_START
              + Tools.makeTexti18n(messages, "startChecking", langName) + "..." + Main.HTML_FONT_END;
          statusPane.setText(startCheckText);
          setStartText(startCheckText);
          if (event.getCaller() == marker) {
            statusPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          }
        } else if (event.getType() == LanguageToolEvent.Type.CHECKING_FINISHED) {
          inputText = event.getSource().getTextComponent().getText();
          setRuleMatches(event.getSource().getMatches());
          if (event.getCaller() == marker || event.getCaller() == null) {
            displayResult();
            if (event.getCaller() == marker) {
              statusPane.setCursor(Cursor.getDefaultCursor());
            }
          }
        } else if (event.getType() == LanguageToolEvent.Type.RULE_DISABLED || event.getType() == LanguageToolEvent.Type.RULE_ENABLED) {
          inputText = event.getSource().getTextComponent().getText();
          setRuleMatches(event.getSource().getMatches());
          displayResult();
        }
      }
    });
  }

  private String getRuleMatchHtml(List<RuleMatch> ruleMatches, String text, String startCheckText) {
    final ContextTools contextTools = new ContextTools();
    final StringBuilder sb = new StringBuilder(200);
    sb.append(startCheckText);
    sb.append("<br>\n");
    int i = 0;
    for (final RuleMatch match : ruleMatches) {
      final String output = Tools.makeTexti18n(messages, "result1", i + 1, match.getLine() + 1, match.getColumn());
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
      if ("misspelling".equals(match.getRule().getLocQualityIssueType())) {
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
    final String checkDone = Tools.makeTexti18n(messages, "checkDone", ruleMatches.size(), runTime);
    sb.append("<br>\n").append(checkDone);
    sb.append("<br>\n").append(messages.getString("makeLanguageToolBetter"));
    sb.append(Main.HTML_FONT_END).append("<br>\n");
    return sb.toString();
  }

  private String getDisabledRulesHtml() {
    final StringBuilder sb = new StringBuilder(40);
    sb.append(messages.getString("deactivatedRulesText"));
    int i = 0;
    int deactivatedRuleCount = 0;
    for (String ruleId : ltSupport.getConfig().getDisabledRuleIds()) {
      if (ruleId.trim().isEmpty()) {
        continue;
      }
      final Rule rule = ltSupport.getRuleForId(ruleId);
      if (rule == null || rule.isDefaultOff()) {
        continue;
      }
      if (i++ > 0) {
        sb.append(',');
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

  private void setStartText(String startText) {
    this.startText = startText;
  }

  void setRunTime(long runTime) {
    this.runTime = runTime;
  }

  void setRuleMatches(List<RuleMatch> ruleMatches) {
    this.allRuleMatches = new ArrayList<>(ruleMatches);
    this.ruleMatches = new ArrayList<>(ruleMatches);
  }

  void displayResult() {
    ruleMatches = filterRuleMatches();
    final String ruleMatchHtml = getRuleMatchHtml(ruleMatches, inputText, startText);
    displayText(ruleMatchHtml);
  }

  private void displayText(String text) {
    // TODO: use a JTable for faster rendering
    statusPane.setText(Main.HTML_FONT_START + text + Main.HTML_FONT_END);
    statusPane.setCaretPosition(0);
  }

  private List<RuleMatch> filterRuleMatches() {
    final List<RuleMatch> filtered = new ArrayList<>();
    final Set<String> disabledRuleIds = ltSupport.getConfig().getDisabledRuleIds();
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
      final RuleLink ruleLink = RuleLink.getFromString(uri);
      final String ruleId = ruleLink.getId();
      if (uri.startsWith(DEACTIVATE_URL)) {
        ltSupport.disableRule(ruleId);
      } else {
        ltSupport.enableRule(ruleId);
      }
      ltSupport.getConfig().saveConfiguration(ltSupport.getLanguageTool().getLanguage());
      ltSupport.checkImmediately(marker);
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
