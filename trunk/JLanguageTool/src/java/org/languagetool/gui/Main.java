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

import org.apache.commons.lang.StringUtils;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.language.RuleFilenameException;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.server.HTTPServer;
import org.languagetool.server.PortBindingException;
import org.languagetool.tools.StringTools;
import org.languagetool.tools.LanguageIdentifierTools;
import org.languagetool.AnalyzedSentence;

import org.apache.tika.language.*;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.List;

/**
 * A simple GUI to check texts with.
 *
 * @author Daniel Naber
 */
public final class Main implements ActionListener {

  static final String EXTERNAL_LANGUAGE_SUFFIX = " (ext.)";
  
  private static final String HTML_FONT_START = "<font face='Arial,Helvetica'>";
  private static final String HTML_GREY_FONT_START = "<font face='Arial,Helvetica' color='#666666'>";

  private static final String HTML_FONT_END = "</font>";
  private static final String SYSTEM_TRAY_ICON_NAME = "/TrayIcon.png";

  private static final String SYSTEM_TRAY_TOOLTIP = "LanguageTool";
  private static final String CONFIG_FILE = ".languagetool.cfg";
  private static final int WINDOW_WIDTH = 600;
  private static final int WINDOW_HEIGHT = 550;

  private final ResourceBundle messages;

  private final Configuration config;

  private JFrame frame;
  private JTextArea textArea;
  private JTextPane resultArea;
  private LanguageComboBox languageBox;
  private JCheckBox autoDetectBox;
  private Cursor prevCursor;

  private HTTPServer httpServer;

  private final Map<Language, ConfigurationDialog> configDialogs = new HashMap<Language, ConfigurationDialog>();

  private boolean closeHidesToTray;
  private boolean isInTray;

  private Main() throws IOException {
    LanguageIdentifierTools.addLtProfiles();  
    config = new Configuration(new File(System.getProperty("user.home")), CONFIG_FILE);
    messages = JLanguageTool.getMessageBundle();
    maybeStartServer();
  }

  private void createGUI() {
    frame = new JFrame("LanguageTool " + JLanguageTool.VERSION);

    setLookAndFeel();

    frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    frame.addWindowListener(new CloseListener());
    frame.setIconImage(new ImageIcon(JLanguageTool.getDataBroker().getFromResourceDirAsUrl(
        Main.SYSTEM_TRAY_ICON_NAME)).getImage());
    frame.setJMenuBar(new MainMenuBar(this, messages));

    textArea = new JTextArea(messages.getString("guiDemoText"));
    // TODO: wrong line number is displayed for lines that are wrapped automatically:
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    textArea.addKeyListener(new ControlReturnTextCheckingListener());
    resultArea = new JTextPane();
    resultArea.setContentType("text/html");
    resultArea.setText(HTML_GREY_FONT_START + messages.getString("resultAreaText") + HTML_FONT_END);
    resultArea.setEditable(false);
    resultArea.addHyperlinkListener(new MyHyperlinkListener());
    final JButton button = new JButton(StringTools.getLabel(messages.getString("checkText")));
    button.setMnemonic(StringTools.getMnemonic(messages.getString("checkText")));
    button.addActionListener(this);

    final JPanel panel = new JPanel();
    panel.setOpaque(false);    // to get rid of the gray background
    panel.setLayout(new GridBagLayout());
    final GridBagConstraints buttonCons = new GridBagConstraints();
    final JPanel insidePanel = new JPanel();
    insidePanel.setOpaque(false);
    insidePanel.setLayout(new GridBagLayout());
    buttonCons.gridx = 0;
    buttonCons.gridy = 0;
    buttonCons.anchor = GridBagConstraints.WEST;
    insidePanel.add(new JLabel(" " + messages.getString("textLanguage") + " "), buttonCons);
    languageBox = new LanguageComboBox(messages);
    buttonCons.gridx = 1;
    buttonCons.gridy = 0;
    insidePanel.add(languageBox, buttonCons);
    buttonCons.gridx = 0;
    buttonCons.gridy = 0;
    panel.add(insidePanel);
    buttonCons.gridx = 2;
    buttonCons.gridy = 0;
    insidePanel.add(button, buttonCons);
      
    autoDetectBox = new JCheckBox(messages.getString("atd"));
    autoDetectBox.addActionListener( new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            languageBox.setEnabled(!autoDetectBox.isSelected());
            config.setAutoDetect(autoDetectBox.isSelected());
        }
    });
    autoDetectBox.setSelected(config.getAutoDetect());
    languageBox.setEnabled(!autoDetectBox.isSelected());
    
    buttonCons.gridx = 1;
    buttonCons.gridy = 1;
    buttonCons.gridwidth = 2;
    buttonCons.anchor = GridBagConstraints.WEST;
    insidePanel.add(autoDetectBox, buttonCons);
    
    final Container contentPane = frame.getContentPane();
    final GridBagLayout gridLayout = new GridBagLayout();
    contentPane.setLayout(gridLayout);
    final GridBagConstraints cons = new GridBagConstraints();
    cons.insets = new Insets(5, 5, 5, 5);
    cons.fill = GridBagConstraints.BOTH;
    cons.weightx = 10.0f;
    cons.weighty = 10.0f;
    cons.gridx = 0;
    cons.gridy = 1;
    cons.weighty = 5.0f;
    final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
        new JScrollPane(textArea), new JScrollPane(resultArea));
    splitPane.setDividerLocation(200);
    contentPane.add(splitPane, cons);

    cons.fill = GridBagConstraints.NONE;
    cons.gridx = 0;
    cons.gridy = 2;
    cons.weighty = 0.0f;
    cons.insets = new Insets(1, 10, 10, 1);
    cons.gridy = 3;
    contentPane.add(panel, cons);

    frame.pack();
    frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
  }

  private void setLookAndFeel() {
    try {
      for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
        if ("Nimbus".equals(info.getName())) {
          UIManager.setLookAndFeel(info.getClassName());
          break;
        }
      }
    } catch (Exception ignored) {
      // Well, what can we do...
    }
  }

  private void showGUI() {
    frame.setVisible(true);
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    try {
      if (e.getActionCommand().equals(
          StringTools.getLabel(messages.getString("checkText")))) {
        checkTextAndDisplayResults();
      } else {
        throw new IllegalArgumentException("Unknown action " + e);
      }
    } catch (final Exception exc) {
      Tools.showError(exc);
    }
  }

  void loadFile() {
    final File file = Tools.openFileDialog(frame, new PlainTextFileFilter());
    if (file == null) {
      // user clicked cancel
      return;
    }
    try {
      final String fileContents = StringTools.readFile(new FileInputStream(file
          .getAbsolutePath()));
      textArea.setText(fileContents);
      checkTextAndDisplayResults();
    } catch (final IOException e) {
      Tools.showError(e);
    }
  }

  void hideToTray() {
    if (!isInTray) {
      final java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();
      final Image img = Toolkit.getDefaultToolkit().getImage(
              JLanguageTool.getDataBroker().getFromResourceDirAsUrl(Main.SYSTEM_TRAY_ICON_NAME));
      final PopupMenu popup = makePopupMenu();
      try {
        final java.awt.TrayIcon trayIcon = new java.awt.TrayIcon(img,
            "tooltip", popup);
        trayIcon.addMouseListener(new TrayActionListener());
        trayIcon.setToolTip(SYSTEM_TRAY_TOOLTIP);
        tray.add(trayIcon);
      } catch (final AWTException e1) {
        // thrown if there's no system tray
        Tools.showError(e1);
      }
    }
    isInTray = true;
    frame.setVisible(false);
  }

  private PopupMenu makePopupMenu() {
    final PopupMenu popup = new PopupMenu();
    final ActionListener rmbListener = new TrayActionRMBListener();
    // Check clipboard text:
    final MenuItem checkClipboardItem = new MenuItem(StringTools
        .getLabel(messages.getString("guiMenuCheckClipboard")));
    checkClipboardItem.addActionListener(rmbListener);
    popup.add(checkClipboardItem);
    // Open main window:
    final MenuItem restoreItem = new MenuItem(StringTools.getLabel(messages
        .getString("guiMenuShowMainWindow")));
    restoreItem.addActionListener(rmbListener);
    popup.add(restoreItem);
    // Exit:
    final MenuItem exitItem = new MenuItem(StringTools.getLabel(messages
        .getString("guiMenuQuit")));
    exitItem.addActionListener(rmbListener);
    popup.add(exitItem);
    return popup;
  }

  void addLanguage() {
    final LanguageManagerDialog lmd = new LanguageManagerDialog(frame, Language
        .getExternalLanguages());
    lmd.show();
    try {
      Language.reInit(lmd.getLanguages());
    } catch (final RuleFilenameException e) {
      Tools.showErrorMessage(e);
    }
    languageBox.populateLanguageBox();
  }

  void showOptions() {
    final JLanguageTool langTool = getCurrentLanguageTool();
    final List<Rule> rules = langTool.getAllRules();
    final ConfigurationDialog configDialog = getCurrentConfigDialog();
    configDialog.show(rules); // this blocks until OK/Cancel is clicked in the dialog
    config.setDisabledRuleIds(configDialog.getDisabledRuleIds());
    config.setEnabledRuleIds(configDialog.getEnabledRuleIds());
    config.setDisabledCategoryNames(configDialog.getDisabledCategoryNames());
    config.setMotherTongue(configDialog.getMotherTongue());
    config.setRunServer(configDialog.getRunServer());
    config.setUseGUIConfig(configDialog.getUseGUIConfig());
    config.setServerPort(configDialog.getServerPort());
    try { //save config - needed for the server
        config.saveConfiguration();
      } catch (final IOException e) {
        Tools.showError(e);
      }
    // Stop server, start new server if requested:
    stopServer();        
    maybeStartServer();
  }

  private void restoreFromTray() {
    frame.setVisible(true);
  }

  // show GUI and check the text from clipboard/selection:
  private void restoreFromTrayAndCheck() {
    final String s = getClipboardText();
    restoreFromTray();
    textArea.setText(s);
    checkTextAndDisplayResults();
  }

  void checkClipboardText() {
    final String s = getClipboardText();
    textArea.setText(s);
    checkTextAndDisplayResults();
  }

  private String getClipboardText() {
    // get text from clipboard or selection:
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemSelection();
    if (clipboard == null) { // on Windows
      clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    }
    String s;
    final Transferable data = clipboard.getContents(this);
    try {
      if (data != null
          && data.isDataFlavorSupported(DataFlavor.getTextPlainUnicodeFlavor())) {
        final DataFlavor df = DataFlavor.getTextPlainUnicodeFlavor();
        final Reader sr = df.getReaderForText(data);
        s = StringTools.readerToString(sr);
      } else {
        s = "";
      }
    } catch (final Exception ex) {
      if (data != null) {
        s = data.toString();
      } else {
        s = "";
      }
    }
    return s;
  }
  
  void tagText() {
    final JLanguageTool langTool = getCurrentLanguageTool();
    tagTextAndDisplayResults(langTool);
  }

  void quitOrHide() {
    if (closeHidesToTray) {
      hideToTray();
    } else {
      quit();
    }
  }

  void quit() {
    stopServer();
    try {
      config.saveConfiguration();
    } catch (final IOException e) {
      Tools.showError(e);
    }
    frame.setVisible(false);
    JLanguageTool.removeTemporaryFiles();
    System.exit(0);
  }

  private void maybeStartServer() {
    if (config.getRunServer()) {
      httpServer = new HTTPServer(config.getServerPort(), false, true);
      try {
    	httpServer.run();        
      } catch (final PortBindingException e) {
        final String message = e.getMessage() + "\n\n" + org.languagetool.tools.Tools.getFullStackTrace(e);
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void stopServer() {
    if (httpServer != null) {
      httpServer.stop();
      httpServer = null;
    }
  }

  // method modified to add automatic language detection
  private Language getCurrentLanguage() {
    if (autoDetectBox.isSelected()) {
      final LanguageIdentifier langIdentifier = new LanguageIdentifier(textArea.getText());
      Language lang = Language.getLanguageForShortName(langIdentifier.getLanguage());
      if (lang == null) {
        lang = Language.ENGLISH;
      }
      for (int i = 0; i < languageBox.getItemCount(); i++) {
        final I18nLanguage boxLanguage = (I18nLanguage) languageBox.getItemAt(i);
        if (boxLanguage.toString().equals(lang.getTranslatedName(messages))) {
          languageBox.setSelectedIndex(i);
        }
      }
      return lang;
    } else {
      return ((I18nLanguage) languageBox.getSelectedItem()).getLanguage();
    }
  }

  private ConfigurationDialog getCurrentConfigDialog() {
    final Language language = getCurrentLanguage();
    final ConfigurationDialog configDialog;
    if (configDialogs.containsKey(language)) {
      configDialog = configDialogs.get(language);
    } else {
      configDialog = new ConfigurationDialog(frame, false);
      configDialog.setMotherTongue(config.getMotherTongue());
      configDialog.setDisabledRules(config.getDisabledRuleIds());
      configDialog.setEnabledRules(config.getEnabledRuleIds());
      configDialog.setDisabledCategories(config.getDisabledCategoryNames());
      configDialog.setRunServer(config.getRunServer());
      configDialog.setServerPort(config.getServerPort());
      configDialog.setUseGUIConfig(config.getUseGUIConfig());
      configDialogs.put(language, configDialog);
    }
    return configDialog;
  }

  private JLanguageTool getCurrentLanguageTool() {
    final JLanguageTool langTool;
    try {
      final ConfigurationDialog configDialog = getCurrentConfigDialog();
      langTool = new JLanguageTool(getCurrentLanguage(), configDialog
          .getMotherTongue());
      langTool.activateDefaultPatternRules();
      langTool.activateDefaultFalseFriendRules();
      final Set<String> disabledRules = configDialog.getDisabledRuleIds();
      if (disabledRules != null) {
        for (final String ruleId : disabledRules) {
          langTool.disableRule(ruleId);
        }
      }
      final Set<String> disabledCategories = configDialog
          .getDisabledCategoryNames();
      if (disabledCategories != null) {
        for (final String categoryName : disabledCategories) {
          langTool.disableCategory(categoryName);
        }
      }
      final Set<String> enabledRules = configDialog.getEnabledRuleIds();
      if (enabledRules != null) {
        for (String ruleName : enabledRules) {
          langTool.enableDefaultOffRule(ruleName);
          langTool.enableRule(ruleName);
        }
      }
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
    return langTool;
  }

  private void checkTextAndDisplayResults() {
    setWaitCursor();
    try {
      final JLanguageTool langTool = getCurrentLanguageTool();
      final Language lang = getCurrentLanguage();
      if (StringTools.isEmpty(textArea.getText().trim())) {
        textArea.setText(messages.getString("enterText2"));
      } else {
        final StringBuilder sb = new StringBuilder();
        final String langName;
        if (lang.isExternal()) {
          langName = lang.getTranslatedName(messages) + EXTERNAL_LANGUAGE_SUFFIX;
        } else {
          langName = lang.getTranslatedName(messages);
        }
        final String startCheckText = HTML_GREY_FONT_START + Tools.makeTexti18n(messages,
            "startChecking", new Object[] { langName }) + HTML_FONT_END;
        resultArea.setText(startCheckText);
        resultArea.repaint(); // FIXME: why doesn't this work?
        sb.append(startCheckText);
        sb.append("...<br>\n");
        int matches = 0;
        try {
          matches = checkText(langTool, textArea.getText(), sb);
        } catch (final Exception e) {
          sb.append("<br><br><b><font color=\"red\">");
          sb.append(org.languagetool.tools.Tools.getFullStackTrace(e).replace("\n", "<br/>"));
          sb.append("</font></b><br>");
        }
        final String checkDone = Tools.makeTexti18n(messages, "checkDone",
            new Object[] {matches});
        sb.append(HTML_GREY_FONT_START);
        sb.append(checkDone);
        sb.append(HTML_FONT_END);
        sb.append("<br>\n");
        resultArea.setText(HTML_FONT_START + sb.toString() + HTML_FONT_END);
        resultArea.setCaretPosition(0);
      }
    } finally {
      unsetWaitCursor();
    }
  }

  private void setWaitCursor() {
    prevCursor = resultArea.getCursor();
    frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
    // For some reason we also have to set the cursor here so it also shows
    // when user starts checking text with Ctrl+Return:
    textArea.setCursor(new Cursor(Cursor.WAIT_CURSOR));
    resultArea.setCursor(new Cursor(Cursor.WAIT_CURSOR));
  }

  private void unsetWaitCursor() {
    frame.setCursor(prevCursor);
    textArea.setCursor(prevCursor);
    resultArea.setCursor(prevCursor);
  }

  private void tagTextAndDisplayResults(final JLanguageTool langTool) {
    if (StringTools.isEmpty(textArea.getText().trim())) {
      textArea.setText(messages.getString("enterText2"));
    } else {
      // tag text
      final List<String> sentences = langTool.sentenceTokenize(textArea.getText());
      final StringBuilder sb = new StringBuilder();
      try {
        for (String sent : sentences) {
          final AnalyzedSentence analyzedText = langTool.getAnalyzedSentence(sent);
          final String analyzedTextString = StringTools.escapeHTML(analyzedText.toString(", ")).
                  replace("[", "<font color='#888888'>[").replace("]", "]</font>");
          sb.append(analyzedTextString);
          sb.append("\n");
        }
      } catch (IOException e) {
        sb.append("An error occurred while tagging the text: " + e.getMessage());
      }
      resultArea.setText(HTML_FONT_START + sb.toString() + HTML_FONT_END);
    }
  }

  private int checkText(final JLanguageTool langTool, final String text,
      final StringBuilder sb) throws IOException {
    final long startTime = System.currentTimeMillis();
    final List<RuleMatch> ruleMatches = langTool.check(text);
    final long startTimeMatching = System.currentTimeMillis();
    int i = 0;
    for (final RuleMatch match : ruleMatches) {
      final String output = Tools.makeTexti18n(messages, "result1",
          new Object[] {i + 1,
                  match.getLine() + 1,
                  match.getColumn()});
      sb.append(output);
      String msg = match.getMessage();
      msg = msg.replaceAll("<suggestion>", "<b>");
      msg = msg.replaceAll("</suggestion>", "</b>");
      msg = msg.replaceAll("<old>", "<b>");
      msg = msg.replaceAll("</old>", "</b>");
      sb.append("<b>" + messages.getString("errorMessage") + "</b> " + msg + "<br>\n");
      if (match.getSuggestedReplacements().size() > 0) {
        final String repl = StringTools.listToString(match
            .getSuggestedReplacements(), "; ");
        sb.append("<b>" + messages.getString("correctionMessage") + "</b> "
            + repl + "<br>\n");
      }
      final String context = Tools.getContext(match.getFromPos(), match
          .getToPos(), text);
      sb.append("<b>" + messages.getString("errorContext") + "</b> " + context);
      sb.append("<br>\n");
      if (match.getRule().getUrl() != null && Desktop.isDesktopSupported()) {
    	  sb.append("<b>More info:</b> <a href=\"");
        final String url = match.getRule().getUrl().toString();
        sb.append(url);
        final String shortUrl = StringUtils.abbreviate(url, 60);
    	  sb.append("\">" + shortUrl +"</a><br>\n");
      }
      i++;
    }
    final long endTime = System.currentTimeMillis();
    sb.append(HTML_GREY_FONT_START);
    sb.append(Tools.makeTexti18n(messages, "resultTime", new Object[] {
            endTime - startTime,
            endTime - startTimeMatching}));
    sb.append(HTML_FONT_END);
    return ruleMatches.size();
  }

  private void setTrayMode(boolean trayMode) {
    this.closeHidesToTray = trayMode;
  }

  public static void main(final String[] args) {
    try {
      final Main prg = new Main();
      if (args.length == 1 && (args[0].equals("-t") || args[0].equals("--tray"))) {
        // dock to systray on startup
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            try {
              prg.createGUI();
              prg.setTrayMode(true);
              prg.hideToTray();
            } catch (final Exception e) {
              Tools.showError(e);
              System.exit(1);
            }
          }
        });
      } else if (args.length >= 1) {
        System.out
            .println("Usage: java org.languagetool.gui.Main [-t|--tray]");
        System.out
            .println("  -t, --tray: dock LanguageTool to system tray on startup");
        prg.stopServer();
      } else {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            try {
              prg.createGUI();
              prg.showGUI();
            } catch (final Exception e) {
              Tools.showError(e);
            }
          }
        });
      }
    } catch (final Exception e) {
      Tools.showError(e);
    }
  }

  private class MyHyperlinkListener implements HyperlinkListener {
    @Override
    public void hyperlinkUpdate(HyperlinkEvent e) {
      if (Desktop.isDesktopSupported() && e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        final Desktop desktop = java.awt.Desktop.getDesktop();
        try {
          desktop.browse(e.getURL().toURI());
        } catch (Exception ex) {
          throw new RuntimeException("Could not open URL: " + e.getURL(), ex);
        }
      }
    }
  }

  private class ControlReturnTextCheckingListener implements KeyListener {

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
      if (e.getKeyCode() == KeyEvent.VK_ENTER) {
        if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK) {
          checkTextAndDisplayResults();
        }
      }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
    
  }
  
  //
  // The System Tray stuff
  //

  class TrayActionRMBListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      if (e.getActionCommand().equalsIgnoreCase(
          StringTools.getLabel(messages.getString("guiMenuCheckClipboard")))) {
        restoreFromTrayAndCheck();
      } else if (e.getActionCommand().equalsIgnoreCase(
          StringTools.getLabel(messages.getString("guiMenuShowMainWindow")))) {
        restoreFromTray();
      } else if (e.getActionCommand().equalsIgnoreCase(
          StringTools.getLabel(messages.getString("guiMenuQuit")))) {
        quit();
      } else {
        JOptionPane.showMessageDialog(null, "Unknown action: "
            + e.getActionCommand(), "Error", JOptionPane.ERROR_MESSAGE);
      }
    }

  }

  class TrayActionListener implements MouseListener {

    @Override
    public void mouseClicked(@SuppressWarnings("unused")MouseEvent e) {
      if (frame.isVisible() && frame.isActive()) {
        frame.setVisible(false);
      } else if (frame.isVisible() && !frame.isActive()) {
        frame.toFront();
        restoreFromTrayAndCheck();
      } else {
        restoreFromTrayAndCheck();
      }
    }

    @Override
    public void mouseEntered(@SuppressWarnings("unused") MouseEvent e) {}
    @Override
    public void mouseExited(@SuppressWarnings("unused")MouseEvent e) {}
    @Override
    public void mousePressed(@SuppressWarnings("unused")MouseEvent e) {}
    @Override
    public void mouseReleased(@SuppressWarnings("unused")MouseEvent e) {}

  }

  class CloseListener implements WindowListener {

    @Override
    public void windowClosing(@SuppressWarnings("unused")WindowEvent e) {
      quitOrHide();
    }

    @Override
    public void windowActivated(@SuppressWarnings("unused")WindowEvent e) {}
    @Override
    public void windowClosed(@SuppressWarnings("unused")WindowEvent e) {}
    @Override
    public void windowDeactivated(@SuppressWarnings("unused")WindowEvent e) {}
    @Override
    public void windowDeiconified(@SuppressWarnings("unused")WindowEvent e) {}
    @Override
    public void windowIconified(@SuppressWarnings("unused")WindowEvent e) {}
    @Override
    public void windowOpened(@SuppressWarnings("unused")WindowEvent e) {}

  }

  static class PlainTextFileFilter extends FileFilter {

    @Override
    public boolean accept(final File f) {
        final boolean isTextFile = f.getName().toLowerCase().endsWith(".txt");
        return isTextFile || f.isDirectory();
    }

    @Override
    public String getDescription() {
      return "*.txt";
    }

  }

}
