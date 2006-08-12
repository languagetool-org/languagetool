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
package de.danielnaber.languagetool.gui;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.xml.parsers.ParserConfigurationException;

import org.jdesktop.jdic.tray.SystemTray;
import org.jdesktop.jdic.tray.TrayIcon;
import org.xml.sax.SAXException;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.Rule;
import de.danielnaber.languagetool.rules.RuleMatch;
import de.danielnaber.languagetool.tools.StringTools;

/**
 * A simple GUI to check texts with.
 * 
 * @author Daniel Naber
 */
public class Main implements ActionListener {

  private static final String HTML_FONT_START = "<font face='Arial,Helvetica'>";
  private static final String HTML_FONT_END = "</font>";
  
  private static final Icon SYSTEM_TRAY_ICON = new ImageIcon("resource/TrayIcon.png");
  private static final String WINDOW_ICON_URL = "resource/TrayIcon.png";
  private static final String CHECK_TEXT_BUTTON = "Check text";

  private TrayIcon trayIcon = null;
  private JFrame frame = null;
  private JTextArea textArea = null;
  private JTextPane resultArea = null;
  private JComboBox langBox = null;
  
  private Map<Language, ConfigurationDialog> configDialogs = new HashMap<Language, ConfigurationDialog>();

  private Main() {
  }

  private void createGUI() {
    frame = new JFrame("LanguageTool " +JLanguageTool.VERSION+ " Demo");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setIconImage(new ImageIcon(WINDOW_ICON_URL).getImage());
    frame.setJMenuBar(new MainMenuBar(this));

    textArea = new JTextArea("This is a example input to to show you how JLanguageTool works. " +
        "Note, however, that it does not include a spell checka.");
    // TODO: wrong line number is displayed for lines that are wrapped automatically:
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    resultArea = new JTextPane();
    resultArea.setContentType("text/html");
    resultArea.setText(HTML_FONT_START + "Results will appear here" + HTML_FONT_END);
    JLabel label = new JLabel("Please type or paste text to check in the top area");
    JButton button = new JButton(CHECK_TEXT_BUTTON);
    button.setMnemonic('c'); 
    button.addActionListener(this);

    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());
    GridBagConstraints buttonCons = new GridBagConstraints();
    buttonCons.gridx = 0;
    buttonCons.gridy = 0;
    panel.add(button, buttonCons);
    buttonCons.gridx = 1;
    buttonCons.gridy = 0;
    panel.add(new JLabel(" in: "), buttonCons);
    buttonCons.gridx = 2;
    buttonCons.gridy = 0;
    langBox = new JComboBox();
    for (Language lang : Language.LANGUAGES) {
      if (lang != Language.DEMO) {
        langBox.addItem(lang);
      }
    }
    panel.add(langBox, buttonCons);

    Container contentPane = frame.getContentPane();
    GridBagLayout gridLayout = new GridBagLayout();
    contentPane.setLayout(gridLayout);
    GridBagConstraints cons = new GridBagConstraints();
    cons.insets = new Insets(5, 5, 5, 5);
    cons.fill = GridBagConstraints.BOTH;
    cons.weightx = 10.0f;
    cons.weighty = 10.0f;
    cons.gridx = 0;
    cons.gridy = 1;
    cons.weighty = 5.0f;
    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(textArea),
        new JScrollPane(resultArea));
    splitPane.setDividerLocation(200);
    contentPane.add(splitPane, cons);

    cons.fill = GridBagConstraints.NONE;
    cons.gridx = 0;
    cons.gridy = 2;
    cons.weighty = 0.0f;
    cons.insets = new Insets(3,3,3,3);
    //cons.fill = GridBagConstraints.NONE;
    contentPane.add(label, cons);
    cons.gridy = 3;
    contentPane.add(panel, cons);
    
    frame.pack();
    frame.setSize(600, 600);
  }

  private void showGUI() {
    frame.setVisible(true);
  }
  
  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equals(CHECK_TEXT_BUTTON)) {
      JLanguageTool langTool = getCurrentLanguageTool();
      checkTextAndDisplayResults(langTool, getCurrentLanguage().getName());
    }
  }

  void hideToTray() {
    if (trayIcon == null) {
      trayIcon = new TrayIcon(SYSTEM_TRAY_ICON);
      SystemTray tray = SystemTray.getDefaultSystemTray();
      trayIcon.addActionListener(new TrayActionListener());
      tray.addTrayIcon(trayIcon);
    }
    frame.setVisible(false);
  }
  
  void showOptions() {
    JLanguageTool langTool = getCurrentLanguageTool();
    List<Rule> rules = langTool.getAllRules();
    ConfigurationDialog configDialog = getCurrentConfigDialog();
    configDialog.show(rules);
  }

  private void restoreFromTray() {
    // get text from clipboard or selection:
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemSelection();
    if (clipboard == null) {    // on Windows
      clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    }
    String s = null;
    Transferable data = clipboard.getContents(this);
    try {
      DataFlavor df = DataFlavor.getTextPlainUnicodeFlavor();
      Reader sr = df.getReaderForText(data);
      s = StringTools.readerToString(sr);
    } catch (Exception ex) {
      ex.printStackTrace();
      s = data.toString();
    }
    // show GUI and check the text from clipboard/selection:
    frame.setVisible(true);
    textArea.setText(s);
    JLanguageTool langTool = getCurrentLanguageTool();
    checkTextAndDisplayResults(langTool, getCurrentLanguage().getName());
  }
  
  void quit() {
    if (trayIcon != null) {
      SystemTray tray = SystemTray.getDefaultSystemTray();
      tray.removeTrayIcon(trayIcon);
    }
    frame.setVisible(false);
  }

  private Language getCurrentLanguage() {
    String langName = langBox.getSelectedItem().toString();
    return Language.getLanguageforName(langName);
  }
  
  private ConfigurationDialog getCurrentConfigDialog() {
    Language language = getCurrentLanguage();
    ConfigurationDialog configDialog = null;
    if (configDialogs.containsKey(language)) {
      configDialog = (ConfigurationDialog)configDialogs.get(language);
    } else {
      configDialog = new ConfigurationDialog(false);
      configDialogs.put(language, configDialog);
    }
    return configDialog;
  }
  
  private JLanguageTool getCurrentLanguageTool() {
    JLanguageTool langTool;
    try {
      ConfigurationDialog configDialog = getCurrentConfigDialog();
      langTool = new JLanguageTool(getCurrentLanguage(), configDialog.getMotherTongue());
      langTool.activateDefaultPatternRules();
      langTool.activateDefaultFalseFriendRules();
      Set<String> disabledRules = configDialog.getDisabledRuleIds();
      if (disabledRules != null) {
        for (String ruleId : disabledRules) {
          langTool.disableRule(ruleId);
        }
      }
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    } catch (ParserConfigurationException ex) {
      throw new RuntimeException(ex);
    } catch (SAXException ex) {
      throw new RuntimeException(ex);
    }
    return langTool;
  }

  private void checkTextAndDisplayResults(JLanguageTool langTool, String langName) {
    if (textArea.getText().trim().equals("")) {
      textArea.setText("Please insert text to check here");
    } else {
      StringBuilder sb = new StringBuilder();
      resultArea.setText(HTML_FONT_START + "Starting check in "+langName+"...<br>\n" + HTML_FONT_END);
      resultArea.repaint(); // FIXME: why doesn't this work?
      //TODO: resultArea.setCursor(new Cursor(Cursor.WAIT_CURSOR)); 
      sb.append("Starting check in " +langName+ "...<br>\n");
      int matches = 0;
      try {
        matches = checkText(langTool, textArea.getText(), sb);
      } catch (Exception ex) {
        sb.append("<br><br><b><font color=\"red\">" + ex.toString() + "<br>");
        StackTraceElement[] elements = ex.getStackTrace();
        for (StackTraceElement element : elements) {
          sb.append(element + "<br>");
        }
        sb.append("</font></b><br>");
        ex.printStackTrace();
      }
      sb.append("Check done. " +matches+ " potential problems found<br>\n");
      resultArea.setText(HTML_FONT_START + sb.toString() + HTML_FONT_END);
      resultArea.setCaretPosition(0);
    }
  }

  private int checkText(JLanguageTool langTool, String text, StringBuilder sb) throws IOException {
    long startTime = System.currentTimeMillis();
    List<RuleMatch> ruleMatches = langTool.check(text);
    long startTimeMatching = System.currentTimeMillis();
    int i = 0;
    for (RuleMatch match : ruleMatches) {
      sb.append("<br>\n<b>" +(i+1)+ ". Line " + (match.getLine() + 1) + ", column " + match.getColumn()
          + "</b><br>\n");
      String msg = match.getMessage();
      msg = msg.replaceAll("<suggestion>", "<b>");
      msg = msg.replaceAll("</suggestion>", "</b>");
      msg = msg.replaceAll("<old>", "<b>");
      msg = msg.replaceAll("</old>", "</b>");
      sb.append("<b>Message:</b> " + msg + "<br>\n");
      String context = Tools.getContext(match.getFromPos(), match.getToPos(), StringTools.escapeHTML(text));
      sb.append("<b>Context:</b> " + context);
      sb.append("<br>\n");
      i++;
    }
    long endTime = System.currentTimeMillis();
    sb.append("<br>\nTime: " + (endTime - startTime) + "ms (including "
        + (endTime - startTimeMatching) + "ms for rule matching)<br>\n");
    return ruleMatches.size();
  }

  public static void main(String[] args) {
    final Main prg = new Main();
    if (args.length == 1 && (args[0].equals("-t") || args[0].equals("--tray"))) {
      // dock to systray on startup
      javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          prg.createGUI();
          prg.hideToTray();
        }
      });
    } else if (args.length >= 1) {
      System.out.println("Usage: java de.danielnaber.languagetool.gui.Main [-t|--tray]");
      System.out.println("  -t|--tray: dock LanguageTool to tray on startup");
    } else {
      javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          prg.createGUI();
          prg.showGUI();
        }
      });
    }
  }

  //
  // The System Tray stuff
  //
  
  class TrayActionListener implements ActionListener {

    public void actionPerformed(@SuppressWarnings("unused") ActionEvent e) {
      if (frame.isVisible() && frame.isActive()) {
        frame.setVisible(false);
      } else if (frame.isVisible() && !frame.isActive()) {
        frame.toFront();
        restoreFromTray();
      } else {
        restoreFromTray();
      }
    }
    
  }

}
