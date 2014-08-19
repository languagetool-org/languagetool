/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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

import java.awt.*;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.lang.StringUtils;
import org.languagetool.rules.IncorrectExample;
import org.languagetool.rules.Rule;
import org.languagetool.rules.patterns.FalseFriendPatternRule;
import org.languagetool.tools.StringTools;

/**
 * GUI-related tools.
 * 
 * @author Daniel Naber
 */
public class Tools {

  private Tools() {
    // no public constructor
  }

  public static String makeTexti18n(final ResourceBundle messages, final String key,
                                    final Object... messageArguments) {
    final MessageFormat formatter = new MessageFormat("");
    formatter.applyPattern(messages.getString(key).replaceAll("'", "''"));
    return formatter.format(messageArguments);
  }

  /**
   * Show a file chooser dialog and return the file selected by the user or
   * <code>null</code>.
   */
  static File openFileDialog(final Frame frame, final FileFilter fileFilter) {
    final JFileChooser jfc = new JFileChooser();
    jfc.setFileFilter(fileFilter);
    jfc.showOpenDialog(frame);
    return jfc.getSelectedFile();
  }

  /**
   * Show a file chooser dialog in a specified directory
   * @param frame Owner frame.
   * @param fileFilter The pattern of files to choose from.
   * @param initialDir The initial directory
   * @return the selected file.
   * @since 2.6
   */
  static File openFileDialog(final Frame frame, final FileFilter fileFilter, final File initialDir) {
    final JFileChooser jfc = new JFileChooser();
    jfc.setCurrentDirectory(initialDir);
    jfc.setFileFilter(fileFilter);
    jfc.showOpenDialog(frame);
    return jfc.getSelectedFile();
  }

  /**
   * Show the exception (with stacktrace) in a dialog and print it to STDERR.
   */
  static void showError(final Exception e) {
    String stackTrace = org.languagetool.tools.Tools.getFullStackTrace(e);
    final String msg = "<html><p style='width: 600px;'>" + StringTools.escapeHTML(stackTrace);
    JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
    e.printStackTrace();
  }

  /**
   * Show the exception (message without stacktrace) in a dialog and print the
   * stacktrace to STDERR.
   */
  static void showErrorMessage(final Exception e, final Component parent) {
    final String msg = e.getMessage();
    JOptionPane.showMessageDialog(parent, msg, "Error", JOptionPane.ERROR_MESSAGE);
    e.printStackTrace();
  }

  /**
   * Show the exception (message without stacktrace) in a dialog and print the
   * stacktrace to STDERR.
   */
  static void showErrorMessage(final Exception e) {
    showErrorMessage(e, null);
  }

  /**
   * LibO shortens menu items with more than ~100 characters by dropping text in the middle.
   * That isn't really sensible, so we shorten the text here in order to preserve the important parts.
   */
  public static String shortenComment(String comment) {
    final int maxCommentLength = 100;
    if (comment.length() > maxCommentLength) {
      // if there is text in brackets, drop it (beginning at the end)
      while (comment.lastIndexOf(" [") > 0
              && comment.lastIndexOf(']') > comment.lastIndexOf(" [")
              && comment.length() > maxCommentLength) {
        comment = comment.substring(0,comment.lastIndexOf(" [")) + comment.substring(comment.lastIndexOf(']')+1);
      }
      while (comment.lastIndexOf(" (") > 0
              && comment.lastIndexOf(')') > comment.lastIndexOf(" (")
              && comment.length() > maxCommentLength) {
        comment = comment.substring(0,comment.lastIndexOf(" (")) + comment.substring(comment.lastIndexOf(')')+1);
      }
      // in case it's still not short enough, shorten at the end
      if (comment.length() > maxCommentLength) {
        comment = comment.substring(0,maxCommentLength-1) + "…";
      }
    }
    return comment;
  }

  /**
   * Returns translation of the UI element without the control character {@code &}. To
   * have {@code &} in the UI, use {@code &&}.
   *
   * @param label Label to convert.
   * @return String UI element string without mnemonics.
   */
  public static String getLabel(final String label) {
    return label.replaceAll("&([^&])", "$1").replaceAll("&&", "&");
  }

  /**
   * Returns mnemonic of a UI element.
   *
   * @param label String Label of the UI element
   * @return Mnemonic of the UI element, or {@code \u0000} in case of no mnemonic set.
   */
  public static char getMnemonic(final String label) {
    int mnemonicPos = label.indexOf('&');
    while (mnemonicPos != -1 && mnemonicPos == label.indexOf("&&")
            && mnemonicPos < label.length()) {
      mnemonicPos = label.indexOf('&', mnemonicPos + 2);
    }
    if (mnemonicPos == -1 || mnemonicPos == label.length()) {
      return '\u0000';
    }
    return label.charAt(mnemonicPos + 1);
  }

  /**
   * Set dialog location to the center of the screen
   *
   * @param dialog the dialog which will be centered
   * @since 2.6
   */
  public static void centerDialog(JDialog dialog) {
    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    final Dimension frameSize = dialog.getSize();
    dialog.setLocation(screenSize.width / 2 - frameSize.width / 2,
            screenSize.height / 2 - frameSize.height / 2);
    dialog.setLocationByPlatform(true);
  }

  static void showRuleInfoDialog(Component parent, String title, String message, Rule rule, ResourceBundle messages, String lang) {
    int dialogWidth = 320;
    JTextPane textPane = new JTextPane();
    textPane.setEditable(false);
    textPane.setContentType("text/html");
    textPane.setBorder(BorderFactory.createEmptyBorder());
    textPane.setOpaque(false);
    textPane.setBackground(new Color(0, 0, 0, 0));
    textPane.addHyperlinkListener(new HyperlinkListener() {
      @Override
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          if (Desktop.isDesktopSupported()) {
            try {
              Desktop.getDesktop().browse(e.getURL().toURI());
            } catch (Exception ex) {
              Tools.showError(ex);
            }
          }
        }
      }
    });
    textPane.setSize(dialogWidth, Short.MAX_VALUE);
    String messageWithBold = message.replaceAll("<suggestion>", "<b>").replaceAll("</suggestion>", "</b>");
    String exampleSentences = getExampleSentences(rule, messages);
    String url = "http://community.languagetool.org/rule/show/" + encodeUrl(rule)
            + "?lang=" + lang + "&amp;ref=standalone-gui";
    String ruleDetailLink = rule instanceof FalseFriendPatternRule ? "" : "<a href='" + url + "'>" + messages.getString("ruleDetailsLink") +"</a>";
    textPane.setText("<html>"
            + messageWithBold + exampleSentences + formatURL(rule.getUrl())
            + "<br><br>"
            + ruleDetailLink
            + "</html>");
    JScrollPane scrollPane = new JScrollPane(textPane);
    scrollPane.setPreferredSize(
            new Dimension(dialogWidth, textPane.getPreferredSize().height));
    scrollPane.setBorder(BorderFactory.createEmptyBorder());

    String cleanTitle = title.replace("<suggestion>", "'").replace("</suggestion>", "'");
    JOptionPane.showMessageDialog(parent, scrollPane, cleanTitle,
            JOptionPane.INFORMATION_MESSAGE);
  }

  private static String encodeUrl(Rule rule) {
    try {
      return URLEncoder.encode(rule.getId(), "utf-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  private static String getExampleSentences(Rule rule,  ResourceBundle messages) {
    StringBuilder examples = new StringBuilder(200);
    java.util.List<IncorrectExample> incorrectExamples = rule.getIncorrectExamples();
    if (incorrectExamples.size() > 0) {
      String incorrectExample = incorrectExamples.iterator().next().getExample();
      String sentence = incorrectExample.replace("<marker>", "<span style='background-color:#ff8080'>").replace("</marker>", "</span>");
      examples.append("<br/>").append(sentence).append("&nbsp;<span style='color:red;font-style:italic;font-weight:bold'>x</span>");
    }
    java.util.List<String> correctExamples = rule.getCorrectExamples();
    if (correctExamples.size() > 0) {
      String correctExample = correctExamples.iterator().next();
      String sentence = correctExample.replace("<marker>", "<span style='background-color:#80ff80'>").replace("</marker>", "</span>");
      examples.append("<br/>").append(sentence).append("&nbsp;<span style='color:green'>✓</span>");
    }
    if (examples.length() > 0) {
      examples.insert(0, "<br/><br/>" + messages.getString("guiExamples"));
    }
    return examples.toString();
  }

  private static String formatURL(URL url) {
    if (url == null) {
      return "";
    }
    return String.format("<br/><br/><a href=\"%s\">%s</a>",
            url.toExternalForm(), StringUtils.abbreviate(url.toString(), 50));
  }
}
