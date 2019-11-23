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
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.Language;
import org.languagetool.LanguageMaintainedState;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tools.ContextTools;

/**
 * Helper for the JTextPane where the result of text checking is displayed.
 */
class ResultAreaHelper implements LanguageToolListener, HyperlinkListener {

  private static final String KEY = "org.languagetool.gui.ResultAreaHelper";
  private static final String EMPTY_PARA = "<p class=\"small\"></p>";
  private static final String HEADER = "header";
  private static final String MAIN = "maincontent";
  private static final String TEMPLATE = "<html>\n"
          + "  <head>\n"
          + "     <style type=\"text/css\">\n"
          + "       #" + HEADER + " {  }\n"
          + "       #" + MAIN + " { }\n"
          + "       p { font-family: Arial,Helvetica; padding: 1px; margin: 1px }\n"
          + "       p.small { font-size: 1px; }\n"
          + "       p.grayed { font-family: Arial,Helvetica; color: #666666 }\n"
          + "     </style>\n"
          + "  </head>\n"
          + "  <body>\n"
          + "    <div id=\"" + HEADER + "\">\n"
          + "    </div>\n"
          + "    <div id=\"" + MAIN + "\">\n"
          + "    </div>\n"
          + "  </body>\n"
          + "</html>";
  private static final String DEACTIVATE_URL = "http://languagetool.org/deactivate/";
  private static final String REACTIVATE_URL = "http://languagetool.org/reactivate/";
  private static final String LT_ERROR_MARKER_START = "<b><font bgcolor=\"#d7d7ff\">";
  private static final String SPELL_ERROR_MARKER_START = "<b><font bgcolor=\"#ffd7d7\">";

  private final ResourceBundle messages;
  private final JTextPane statusPane;
  private final LanguageToolSupport ltSupport;

  private long runTime;
  private final Object lock = new Object();
  private boolean enabled = false;

  static void install(ResourceBundle messages, LanguageToolSupport ltSupport, JTextPane pane) {
    Object prev = pane.getClientProperty(KEY);
    if (prev != null && prev instanceof ResultAreaHelper) {
      enable(pane);
      return;
    }
    ResultAreaHelper helper = new ResultAreaHelper(messages, ltSupport, pane);
    pane.putClientProperty(KEY, helper);
  }

  static void enable(JTextPane pane) {
    Object helper = pane.getClientProperty(KEY);
    if (helper != null && helper instanceof ResultAreaHelper) {
      ((ResultAreaHelper) helper).enable();
    }
  }

  static void disable(JTextPane pane) {
    Object helper = pane.getClientProperty(KEY);
    if (helper != null && helper instanceof ResultAreaHelper) {
      ((ResultAreaHelper) helper).disable();
    }
  }

  static void uninstall(JTextPane pane) {
    Object helper = pane.getClientProperty(KEY);
    if (helper != null && helper instanceof ResultAreaHelper) {
      ((ResultAreaHelper) helper).disable();
      pane.putClientProperty(KEY, null);
    }
  }

  private ResultAreaHelper(ResourceBundle messages, LanguageToolSupport ltSupport, JTextPane statusPane) {
    this.messages = messages;
    this.ltSupport = ltSupport;
    this.statusPane = statusPane;
    statusPane.setContentType("text/html");
    statusPane.setEditable(false);
    statusPane.setTransferHandler(new RetainLineBreakTransferHandler());
    enable();
  }

  private void enable() {
    synchronized (lock) {
      if (enabled) {
        return;
      }
      enabled = true;
      statusPane.setText(TEMPLATE);
      setHeader(messages.getString("resultAreaText"));
      statusPane.addHyperlinkListener(this);
      ltSupport.addLanguageToolListener(this);
    }
  }

  private void disable() {
    synchronized (lock) {
      if (!enabled) {
        return;
      }
      enabled = false;
      statusPane.setText(TEMPLATE);
      statusPane.removeHyperlinkListener(this);
      ltSupport.removeLanguageToolListener(this);
    }
  }

  @Override
  public void languageToolEventOccurred(LanguageToolEvent event) {
    if (event.getType() == LanguageToolEvent.Type.CHECKING_STARTED) {
      Language lang = ltSupport.getLanguage();
      String langName;
      if (lang.isExternal()) {
        langName = lang.getTranslatedName(messages) + Main.EXTERNAL_LANGUAGE_SUFFIX;
      } else {
        langName = lang.getTranslatedName(messages);
      }
      String msg = org.languagetool.tools.Tools.i18n(
              messages, "startChecking", langName) + "...";
      setHeader(msg);
      setMain(EMPTY_PARA);
      if (event.getCaller() == this) {
        statusPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      }
    } else if (event.getType() == LanguageToolEvent.Type.CHECKING_FINISHED) {
      setRunTime(event.getElapsedTime());
      String inputText = event.getSource().getTextComponent().getText();
      displayResult(inputText, event.getSource().getMatches());
      if (event.getCaller() == this) {
        statusPane.setCursor(Cursor.getDefaultCursor());
      }
    } else if (event.getType() == LanguageToolEvent.Type.RULE_DISABLED
            || event.getType() == LanguageToolEvent.Type.RULE_ENABLED) {
      String inputText = event.getSource().getTextComponent().getText();
      displayResult(inputText, event.getSource().getMatches());
    }
  }

  private void setHeader(String txt) {
    HTMLDocument d = (HTMLDocument) statusPane.getDocument();
    Element e = d.getElement(HEADER);
    try {
      d.setInnerHTML(e, "<p class=\"grayed\">" + txt + "</p>");
    } catch (BadLocationException ex) {
      Tools.showError(ex);
    } catch (IOException ex) {
      Tools.showError(ex);
    }
  }

  private void setMain(String html) {
    HTMLDocument d = (HTMLDocument) statusPane.getDocument();
    Element e = d.getElement(MAIN);
    try {
      d.setInnerHTML(e, html);
    } catch (BadLocationException ex) {
      Tools.showError(ex);
    } catch (IOException ex) {
      Tools.showError(ex);
    }
  }

  private void appendMain(String html) {
    HTMLDocument d = (HTMLDocument) statusPane.getDocument();
    Element e = d.getElement(MAIN);
    try {
      d.insertBeforeEnd(e, html);
    } catch (BadLocationException ex) {
      Tools.showError(ex);
    } catch (IOException ex) {
      Tools.showError(ex);
    }
  }

  private void getRuleMatchHtml(List<RuleMatch> ruleMatches, String text) {
    ContextTools contextTools = new ContextTools();
    StringBuilder sb = new StringBuilder(200);
    if (ltSupport.getLanguage().getMaintainedState() != LanguageMaintainedState.ActivelyMaintained) {
      sb.append("<p><b>").append(messages.getString("unsupportedWarning"))
              .append("</b></p>\n");
    } else {
      sb.append(EMPTY_PARA);
    }
    setMain(sb.toString());
    sb.setLength(0);
    int i = 0;
    for (RuleMatch match : ruleMatches) {
      sb.append("<p>");
      String output = org.languagetool.tools.Tools.i18n(messages, "result1", i + 1, match.getLine() + 1, match.getColumn());
      sb.append(output);
      String msg = match.getMessage()
          .replaceAll("<suggestion>", "<b>").replaceAll("</suggestion>", "</b>")
          .replaceAll("<old>", "<b>").replaceAll("</old>", "</b>");
      sb.append("<b>").append(messages.getString("errorMessage")).append("</b> ");
      sb.append(msg);
      RuleLink ruleLink = RuleLink.buildDeactivationLink(match.getRule());
      sb.append(" <a href=\"").append(ruleLink).append("\">").append(messages.getString("deactivateRule")).append("</a><br>\n");
      if (match.getSuggestedReplacements().size() > 0) {
        String replacement = String.join("; ", match.getSuggestedReplacements());
        sb.append("<b>").append(messages.getString("correctionMessage")).append("</b> ").append(replacement).append("<br>\n");
      }
      if (ITSIssueType.Misspelling == match.getRule().getLocQualityIssueType()) {
        contextTools.setErrorMarkerStart(SPELL_ERROR_MARKER_START);
      } else {
        contextTools.setErrorMarkerStart(LT_ERROR_MARKER_START);
      }
      String context = contextTools.getContext(match.getFromPos(), match.getToPos(), text);
      sb.append("<b>").append(messages.getString("errorContext")).append("</b> ").append(context);
      if ((match.getRule().getUrl() != null || match.getUrl() != null) && Desktop.isDesktopSupported()) {
        sb.append("<br>\n");
        sb.append("<b>").append(messages.getString("moreInfo")).append("</b> <a href=\"");
        String url;
        if(match.getUrl() != null) {
          url = match.getUrl().toString();
        } else {
          url = match.getRule().getUrl().toString();
        }
        sb.append(url);
        String shortUrl = StringUtils.abbreviate(url, 60);
        sb.append("\">").append(shortUrl).append("</a>\n");
      }
      sb.append("</p>");
      i++;
      appendMain(sb.toString());
      sb.setLength(0);
    }
    sb.append("<p class=\"grayed\">");
    sb.append(getDisabledRulesHtml());
    String checkDone = org.languagetool.tools.Tools.i18n(messages, "checkDone",
            ruleMatches.size(), runTime);
    sb.append("<br>\n").append(checkDone);
    sb.append("<br>\n").append(messages.getString("makeLanguageToolBetter"));
    sb.append("<br>\n");
    sb.append("</p>");
    appendMain(sb.toString());
  }

  private String getDisabledRulesHtml() {
    StringBuilder sb = new StringBuilder(40);
    sb.append(messages.getString("deactivatedRulesText"));
    int i = 0;
    int deactivatedRuleCount = 0;
    for (String ruleId : ltSupport.getConfig().getDisabledRuleIds()) {
      if (ruleId.trim().isEmpty()) {
        continue;
      }
      Rule rule = ltSupport.getRuleForId(ruleId);
      if (rule == null || rule.isDefaultOff()) {
        continue;
      }
      if (i++ > 0) {
        sb.append(',');
      }
      RuleLink reactivationLink = RuleLink.buildReactivationLink(rule);
      sb.append(" <a href=\"").append(reactivationLink).append("\">")
              .append(rule.getDescription()).append("</a>");
      deactivatedRuleCount++;
    }
    sb.append("<br>");
    if (deactivatedRuleCount == 0) {
      return "";
    } else {
      return sb.toString();
    }
  }

  private void setRunTime(long runTime) {
    this.runTime = runTime;
  }

  private void displayResult(String inputText, List<RuleMatch> matches) {
    List<RuleMatch> filtered = filterRuleMatches(matches);
    getRuleMatchHtml(filtered, inputText);
    statusPane.setCaretPosition(0);
  }

  private List<RuleMatch> filterRuleMatches(List<RuleMatch> matches) {
    List<RuleMatch> filtered = new ArrayList<>();
    Set<String> disabledRuleIds = ltSupport.getConfig().getDisabledRuleIds();
    for (RuleMatch ruleMatch : matches) {
      if (!disabledRuleIds.contains(ruleMatch.getRule().getId())) {
        filtered.add(ruleMatch);
      }
    }
    return filtered;
  }

  @Override
  public void hyperlinkUpdate(HyperlinkEvent e) {
    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
      URL url = e.getURL();
      try {
        String uri = url.toURI().toString();
        if (uri.startsWith(DEACTIVATE_URL) || uri.startsWith(REACTIVATE_URL)) {
          handleRuleLinkClick(uri);
        } else {
          Tools.openURL(url);
        }
      } catch (Exception ex) {
        throw new RuntimeException("Could not handle URL click: " + url, ex);
      }
    }
  }

  private void handleRuleLinkClick(String uri) throws IOException {
    RuleLink ruleLink = RuleLink.getFromString(uri);
    String ruleId = ruleLink.getId();
    if (uri.startsWith(DEACTIVATE_URL)) {
      ltSupport.disableRule(ruleId);
    } else {
      ltSupport.enableRule(ruleId);
    }
    ltSupport.getConfig().saveConfiguration(ltSupport.getLanguage());
    ltSupport.checkImmediately(this);
  }

}
