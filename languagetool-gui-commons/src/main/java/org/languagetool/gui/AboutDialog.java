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
package org.languagetool.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.util.ResourceBundle;
import java.util.TreeMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.language.Contributor;

/**
 * A dialog with version and copyright information.
 * 
 * @author Daniel Naber
 */
public class AboutDialog {

  protected final ResourceBundle messages;

  private final Component parent;

  public AboutDialog(final ResourceBundle messages, Component parent) {
    this.messages = messages;
    this.parent = parent;
  }

  public void show() {
    final String aboutText = Tools.getLabel(messages.getString("guiMenuAbout"));

    JTextPane aboutPane = new JTextPane();
    aboutPane.setBackground(new Color(0, 0, 0, 0));
    aboutPane.setBorder(BorderFactory.createEmptyBorder());
    aboutPane.setContentType("text/html");
    aboutPane.setEditable(false);
    aboutPane.setOpaque(false);

    aboutPane.setText(String.format("<html>"
            + "<p>LanguageTool %s (%s)<br>"
            + "Copyright (C) 2005-2014 the LanguageTool community and Daniel Naber<br>"
            + "This software is licensed under the GNU Lesser General Public License.<br>"
            + "<a href=\"http://www.languagetool.org\">http://www.languagetool.org</a></p>"
            + "<p>Maintainers of the language modules:</p><br>"
            + "</html>", JLanguageTool.VERSION, JLanguageTool.BUILD_DATE));

    aboutPane.addHyperlinkListener(new HyperlinkListener() {
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

    JTextPane maintainersPane = new JTextPane();
    maintainersPane.setBackground(new Color(0, 0, 0, 0));
    maintainersPane.setBorder(BorderFactory.createEmptyBorder());
    maintainersPane.setContentType("text/html");
    maintainersPane.setEditable(false);
    maintainersPane.setOpaque(false);

    maintainersPane.setText(getMaintainers());

    int maxHeight = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height / 2;
    if(maintainersPane.getPreferredSize().height > maxHeight) {
      maintainersPane.setPreferredSize(
                new Dimension(maintainersPane.getPreferredSize().width, maxHeight));
    }

    JScrollPane scrollPane = new JScrollPane(maintainersPane);
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    panel.add(aboutPane);
    panel.add(scrollPane);

    JOptionPane.showMessageDialog(parent, panel,
        aboutText, JOptionPane.INFORMATION_MESSAGE);
  }

  private String getMaintainers() {
    final TreeMap<String, Language> list = new TreeMap<>();
    for (final Language lang : Languages.get()) {
      if (!lang.isVariant()) {
        if (lang.getMaintainers() != null) {
          list.put(messages.getString(lang.getShortName()), lang);
        }
      }
    }
    final StringBuilder maintainersInfo = new StringBuilder();
    maintainersInfo.append("<table border=0 cellspacing=0 cellpadding=0>");
    for(String lang : list.keySet()) {
      maintainersInfo.append("<tr valign=\"top\"><td>");
      maintainersInfo.append(lang);
      maintainersInfo.append(":</td>");
      maintainersInfo.append("<td>&nbsp;</td>");
      maintainersInfo.append("<td>");
      int i = 0;
      for (Contributor contributor : list.get(lang).getMaintainers()) {
        if (i > 0) {
          maintainersInfo.append(", ");
          if (i % 3 == 0) {
            maintainersInfo.append("<br>");
          }
        }
        maintainersInfo.append(contributor.getName());
        i++;
      }
      maintainersInfo.append("</td></tr>");
    }
    maintainersInfo.append("</table>");
    return maintainersInfo.toString();
  }

}
