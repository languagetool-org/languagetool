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
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.LanguageMaintainedState;
import org.languagetool.Languages;
import org.languagetool.language.Contributor;

/**
 * A dialog with version and copyright information.
 * 
 * @author Daniel Naber
 */
public class AboutDialog {

  private final ResourceBundle messages;
  private final Component parent;

  public AboutDialog(ResourceBundle messages, Component parent) {
    this.messages = messages;
    this.parent = parent;
  }

  public void show() {
    String aboutText = Tools.getLabel(messages.getString("guiMenuAbout"));

    JTextPane aboutPane = new JTextPane();
    aboutPane.setBackground(new Color(0, 0, 0, 0));
    aboutPane.setBorder(BorderFactory.createEmptyBorder());
    aboutPane.setContentType("text/html");
    aboutPane.setEditable(false);
    aboutPane.setOpaque(false);

    aboutPane.setText(String.format("<html>"
            + "<p>LanguageTool %s (%s, %s)<br>"
            + "Copyright (C) 2005-2022 the LanguageTool community and Daniel Naber<br>"
            + "This software is licensed under the GNU Lesser General Public License.<br>"
            + "<a href=\"https://www.languagetool.org\">https://www.languagetool.org</a><br>"
            + "Java max/total/free memory: %sMB, %sMB, %sMB</p>"
            + "<p>Maintainers or former maintainers of the language modules -<br>"
            + "(*) means language is unmaintained in LanguageTool:</p><br>"
            + "</html>", JLanguageTool.VERSION,
             JLanguageTool.BUILD_DATE,
             JLanguageTool.GIT_SHORT_ID,
             Runtime.getRuntime().maxMemory()/1024/1024,
             Runtime.getRuntime().totalMemory()/1024/1024,
             Runtime.getRuntime().freeMemory()/1024/1024));

    Tools.addHyperlinkListener(aboutPane);

    JTextPane maintainersPane = new JTextPane();
    maintainersPane.setBackground(new Color(0, 0, 0, 0));
    maintainersPane.setBorder(BorderFactory.createEmptyBorder());
    maintainersPane.setContentType("text/html");
    maintainersPane.setEditable(false);
    maintainersPane.setOpaque(false);

    maintainersPane.setText(getMaintainers());

    int prefWidth = Math.max(520, maintainersPane.getPreferredSize().width);
    int maxHeight = Toolkit.getDefaultToolkit().getScreenSize().height / 2;
    maxHeight = Math.min(maintainersPane.getPreferredSize().height, maxHeight);
    maintainersPane.setPreferredSize(new Dimension(prefWidth, maxHeight));

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
    TreeMap<String, Language> list = new TreeMap<>();
    for (Language lang : Languages.get()) {
      if (!lang.isVariant()) {
        if (lang.getMaintainers() != null) {
          list.put(messages.getString(lang.getShortCode()), lang);
        }
      }
    }
    StringBuilder str = new StringBuilder();
    str.append("<table border=0 cellspacing=0 cellpadding=0>");
    for (Map.Entry<String, Language> entry : list.entrySet()) {
      str.append("<tr valign=\"top\"><td>");
      str.append(entry.getKey());
      if (entry.getValue().getMaintainedState() == LanguageMaintainedState.LookingForNewMaintainer) {
        str.append("(*)");
      }
      str.append(":</td>");
      str.append("<td>&nbsp;</td>");
      str.append("<td>");
      int i = 0;
      Contributor[] maintainers = list.get(entry.getKey()).getMaintainers();
      if (maintainers != null) {
        for (Contributor contributor : maintainers) {
          if (i > 0) {
            str.append(", ");
            if (i % 3 == 0) {
              str.append("<br>");
            }
          }
          str.append(contributor.getName());
          i++;
        }
      }
      str.append("</td></tr>");
    }
    str.append("</table>");
    return str.toString();
  }

}
