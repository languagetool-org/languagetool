/* LanguageTool, a natural language style checker 
 * Copyright (C) 2007 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.openoffice;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.net.URL;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import org.languagetool.gui.Tools;
import org.languagetool.rules.Category;
import org.languagetool.rules.Rule;
import org.languagetool.rules.patterns.FalseFriendPatternRule;


/**
 * A dialog with information about a special rule.
 * 
 * @author Fred Kruse
 * @since 6.4
 */
public class MoreInfoDialog {

  private final JDialog dialog = new JDialog();
  private final String title;
  private final String message;
  private final Rule rule;
  private final URL matchUrl;
  private final ResourceBundle messages;
  private final String lang;

  public MoreInfoDialog(String title, String message, Rule rule, URL matchUrl, ResourceBundle messages, String lang) {
    this.title = title;
    this.message = message;
    this.rule = rule;
    this.matchUrl = matchUrl;
    this.messages = messages;
    this.lang = lang;
  }

  public void show() {
    try {
      int dialogWidth = 320;
      JTextPane textPane = new JTextPane();
      textPane.setEditable(false);
      textPane.setContentType("text/html");
      textPane.setBorder(BorderFactory.createEmptyBorder());
      textPane.setOpaque(false);
      textPane.setBackground(new Color(0, 0, 0, 0));
      Tools.addHyperlinkListener(textPane);
      textPane.setSize(dialogWidth, Short.MAX_VALUE);
      String messageWithBold = message.replaceAll("<suggestion>", "<b>").replaceAll("</suggestion>", "</b>");
      String exampleSentences = Tools.getExampleSentences(rule, messages);
      String url = "http://community.languagetool.org/rule/show/" + Tools.encodeUrl(rule)
              + "?lang=" + lang + "&amp;ref=standalone-gui";
      boolean isExternal = rule.getCategory().getLocation() == Category.Location.EXTERNAL;
      String ruleDetailLink = rule instanceof FalseFriendPatternRule || isExternal ?
              "" : "<a href='" + url + "'>" + messages.getString("ruleDetailsLink") +"</a>";
      textPane.setText("<html>"
              + messageWithBold + exampleSentences + Tools.formatURL(matchUrl)
              + "<br><br>"
              + ruleDetailLink
              + "</html>");
      JScrollPane scrollPane = new JScrollPane(textPane);
      scrollPane.setPreferredSize(
              new Dimension(dialogWidth, textPane.getPreferredSize().height));
      scrollPane.setBorder(BorderFactory.createEmptyBorder());

      String cleanTitle = title.replace("<suggestion>", "'").replace("</suggestion>", "'");

      dialog.setName(cleanTitle);
      dialog.setTitle(cleanTitle);
      dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      Image ltImage = OfficeTools.getLtImage();
      ((Frame) dialog.getOwner()).setIconImage(ltImage);
      
      dialog.addWindowFocusListener(new WindowFocusListener() {
        @Override
        public void windowGainedFocus(WindowEvent e) {
        }

        @Override
        public void windowLostFocus(WindowEvent e) {
          close();
        }
      });
      
      JButton close = new JButton(messages.getString("guiOOoCloseButton"));
      close.addActionListener(e -> {
        close();
      });

      JPanel infoPanel = new JPanel();
      infoPanel.setLayout(new GridBagLayout());
      GridBagConstraints cons = new GridBagConstraints();
      cons.insets = new Insets(6, 0, 0, 15);
      cons.gridx = 0;
      cons.gridy = 0;
      cons.anchor = GridBagConstraints.WEST;
      cons.fill = GridBagConstraints.NONE;
      cons.weightx = 1.0f;
      cons.weighty = 1.0f;
      infoPanel.add(textPane, cons);
      
      JPanel closeButtonPanel = new JPanel();
      closeButtonPanel.setLayout(new GridBagLayout());
      cons = new GridBagConstraints();
      cons.insets = new Insets(6, 12, 0, 15);
      cons.gridx = 0;
      cons.gridy = 0;
      cons.anchor = GridBagConstraints.EAST;
      cons.fill = GridBagConstraints.NONE;
      cons.weightx = 1.0f;
      cons.weighty = 1.0f;
      closeButtonPanel.add(close, cons);
      
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
      panel.add(infoPanel);
      panel.add(closeButtonPanel);
      Container contentPane = dialog.getContentPane();
      contentPane.setLayout(new GridBagLayout());
      cons = new GridBagConstraints();
      cons.insets = new Insets(8, 8, 8, 8);
      cons.gridx = 0;
      cons.gridy = 0;
      cons.weightx = 10.0f;
      cons.weighty = 10.0f;
      cons.fill = GridBagConstraints.BOTH;
      cons.anchor = GridBagConstraints.NORTHWEST;
      contentPane.add(panel, cons);
      dialog.pack();
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension frameSize = dialog.getSize();
      dialog.setLocation(screenSize.width / 2 - frameSize.width / 2,
          screenSize.height / 2 - frameSize.height / 2);
      dialog.setLocationByPlatform(true);
      dialog.setAutoRequestFocus(true);
      dialog.setVisible(true);
      dialog.setAlwaysOnTop(true);
      dialog.toFront();
    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
  }

  public void close() {
    dialog.setVisible(false);
  }

}
