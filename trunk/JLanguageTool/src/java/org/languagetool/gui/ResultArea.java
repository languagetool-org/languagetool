package org.languagetool.gui;

import org.apache.commons.lang.StringUtils;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.tools.StringTools;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Area where the result of text checking is displayed.
 */
class ResultArea extends JTextPane {

  private static final String LT_ERROR_MARKER_START = "<b><font bgcolor=\"#d7d7ff\">";
  private static final String SPELL_ERROR_MARKER_START = "<b><font bgcolor=\"#ffd7d7\">";

  private final ResourceBundle messages;

  private String inputText;
  private String startText;
  private List<RuleMatch> ruleMatches;
  private long runTime;

  ResultArea(ResourceBundle messages) {
    this.messages = messages;
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
      String msg = match.getMessage();
      msg = msg.replaceAll("<suggestion>", "<b>");
      msg = msg.replaceAll("</suggestion>", "</b>");
      msg = msg.replaceAll("<old>", "<b>");
      msg = msg.replaceAll("</old>", "</b>");
      sb.append("<b>" + messages.getString("errorMessage") + "</b> ");
      sb.append(msg);
      // TODO: enable this:
      //sb.append(" <a href=\"" + Main.DEACTIVATE_URL + match.getRule().getId() + "\">(deactivate)</a>");
      sb.append("<br>\n");
      if (match.getSuggestedReplacements().size() > 0) {
        final String repl = StringTools.listToString(match.getSuggestedReplacements(), "; ");
        sb.append("<b>" + messages.getString("correctionMessage") + "</b> " + repl + "<br>\n");
      }
      if (match.getRule() instanceof SpellingCheckRule) {
        contextTools.setErrorMarkerStart(SPELL_ERROR_MARKER_START);
      } else {
        contextTools.setErrorMarkerStart(LT_ERROR_MARKER_START);
      }
      final String context = contextTools.getContext(match.getFromPos(), match.getToPos(), text);
      sb.append("<b>" + messages.getString("errorContext") + "</b> " + context);
      sb.append("<br>\n");
      if (match.getRule().getUrl() != null && Desktop.isDesktopSupported()) {
    	  sb.append("<b>" + messages.getString("moreInfo") + "</b> <a href=\"");
        final String url = match.getRule().getUrl().toString();
        sb.append(url);
        final String shortUrl = StringUtils.abbreviate(url, 60);
    	  sb.append("\">" + shortUrl +"</a><br>\n");
      }
      i++;
    }
    sb.append(Main.HTML_GREY_FONT_START);
    final String checkDone = Tools.makeTexti18n(messages, "checkDone", new Object[] {ruleMatches.size(), runTime});
    sb.append("<br>\n");
    sb.append(checkDone);
    sb.append(Main.HTML_FONT_END);
    sb.append("<br>\n");
    return sb.toString();
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
    this.ruleMatches = ruleMatches;
  }

  void displayResult() {
    final String ruleMatchHtml = getRuleMatchHtml(ruleMatches, inputText, startText);
    displayText(ruleMatchHtml);
  }

  void displayText(String text) {
    setText(Main.HTML_FONT_START + text + Main.HTML_FONT_END);
    setCaretPosition(0);
  }

}
