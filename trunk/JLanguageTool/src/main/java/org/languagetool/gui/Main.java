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

import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.language.RuleFilenameException;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.server.HTTPServer;
import org.languagetool.server.HTTPServerConfig;
import org.languagetool.server.PortBindingException;
import org.languagetool.tools.LanguageIdentifierTools;
import org.languagetool.tools.StringTools;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * A simple GUI to check texts with.
 *
 * @author Daniel Naber
 */
public final class Main implements ActionListener {

  static final String EXTERNAL_LANGUAGE_SUFFIX = " (ext.)";
  static final String HTML_FONT_START = "<font face='Arial,Helvetica'>";
  static final String HTML_FONT_END = "</font>";
  static final String HTML_GREY_FONT_START = "<font face='Arial,Helvetica' color='#666666'>";

  private static final String TRAY_ICON = "/TrayIcon.png";
  private static final String TRAY_SERVER_ICON = "/TrayIconWithServer.png";
  private static final String TRAY_SMALL_ICON = "/TrayIconSmall.png";
  private static final String TRAY_SMALL_SERVER_ICON = "/TrayIconSmallWithServer.png";
  private static final String TRAY_TOOLTIP = "LanguageTool";

  private static final String CONFIG_FILE = ".languagetool.cfg";
  private static final int WINDOW_WIDTH = 600;
  private static final int WINDOW_HEIGHT = 550;

  private final ResourceBundle messages;

  private List<RuleMatch> ruleMatches;
  private Configuration config;
  private JLanguageTool langTool;
  private JFrame frame;
  private JTextArea textArea;
  private ResultArea resultArea;
  private JButton checkTextButton;
  private LanguageComboBox languageBox;
  private LanguageDetectionCheckbox autoDetectBox;
  private Cursor prevCursor;
  private CheckboxMenuItem enableHttpServerItem;

  private HTTPServer httpServer;

  private final Map<Language, ConfigurationDialog> configDialogs = new HashMap<Language, ConfigurationDialog>();

  private TrayIcon trayIcon;
  private boolean closeHidesToTray;
  private boolean isInTray;
  private boolean isAlreadyChecking;

  private Main() throws IOException {
    LanguageIdentifierTools.addLtProfiles();
    config = new Configuration(new File(System.getProperty("user.home")), CONFIG_FILE, null);
    messages = JLanguageTool.getMessageBundle();
    maybeStartServer();
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    try {
      if (e.getActionCommand().equals(StringTools.getLabel(messages.getString("checkText")))) {
        checkTextAndDisplayResults();
      } else {
        throw new IllegalArgumentException("Unknown action " + e);
      }
    } catch (Exception exc) {
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
      final FileInputStream inputStream = new FileInputStream(file);
      try {
        final String fileContents = StringTools.readFile(inputStream);
        textArea.setText(fileContents);
      } finally {
        inputStream.close();
      }
      checkTextAndDisplayResults();
    } catch (IOException e) {
      Tools.showError(e);
    }
  }

  void addLanguage() {
    final LanguageManagerDialog lmd = new LanguageManagerDialog(frame, Language.getExternalLanguages());
    lmd.show();
    try {
      Language.reInit(lmd.getLanguages());
    } catch (RuleFilenameException e) {
      Tools.showErrorMessage(e);
    }
    languageBox.populateLanguageBox();
  }

  void showOptions() {
    final Language currentLanguage = getCurrentLanguage();
    final JLanguageTool langTool = getCurrentLanguageTool(currentLanguage);
    final List<Rule> rules = langTool.getAllRules();
    final ConfigurationDialog configDialog = getCurrentConfigDialog(currentLanguage);
    configDialog.show(rules); // this blocks until OK/Cancel is clicked in the dialog
    config.setDisabledRuleIds(configDialog.getDisabledRuleIds());
    config.setEnabledRuleIds(configDialog.getEnabledRuleIds());
    config.setDisabledCategoryNames(configDialog.getDisabledCategoryNames());
    config.setMotherTongue(configDialog.getMotherTongue());
    config.setRunServer(configDialog.getRunServer());
    config.setUseGUIConfig(configDialog.getUseGUIConfig());
    config.setServerPort(configDialog.getServerPort());
    try { //save config - needed for the server
      config.saveConfiguration(langTool.getLanguage());
    } catch (IOException e) {
      Tools.showError(e);
    }
    // Stop server, start new server if requested:
    stopServer();
    maybeStartServer();
  }

  private void createGUI() {
    frame = new JFrame("LanguageTool " + JLanguageTool.VERSION);

    setLookAndFeel();

    frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    frame.addWindowListener(new CloseListener());
    final URL iconUrl = JLanguageTool.getDataBroker().getFromResourceDirAsUrl(TRAY_ICON);
    frame.setIconImage(new ImageIcon(iconUrl).getImage());
    frame.setJMenuBar(new MainMenuBar(this, messages));

    textArea = new JTextArea(messages.getString("guiDemoText"));
    // TODO: wrong line number is displayed for lines that are wrapped automatically:
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    textArea.addKeyListener(new ControlReturnTextCheckingListener());
    resultArea = new ResultArea(messages, textArea, config);
    checkTextButton = new JButton(StringTools.getLabel(messages.getString("checkText")));
    checkTextButton.setMnemonic(StringTools.getMnemonic(messages.getString("checkText")));
    checkTextButton.addActionListener(this);

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
    languageBox.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        langTool = null;  // we cannot re-use the existing LT object anymore
      }
    });
    buttonCons.gridx = 1;
    buttonCons.gridy = 0;
    insidePanel.add(languageBox, buttonCons);
    buttonCons.gridx = 0;
    buttonCons.gridy = 0;
    panel.add(insidePanel);
    buttonCons.gridx = 2;
    buttonCons.gridy = 0;
    insidePanel.add(checkTextButton, buttonCons);

    autoDetectBox = new LanguageDetectionCheckbox(messages, languageBox, config);
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
    
    warmUpChecker();

    frame.pack();
    frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
  }

  private void warmUpChecker() {
    // Warm-up: we have a lot of lazy init in LT, which causes the first check to
    // be very slow (several seconds) for languages with a lot of data and a lot of 
    // rules. We just assume that the default language is the language that the user
    // often uses and init the LT object for that now, not just when it's first used.
    // This makes the first check feel much faster:
    getCurrentLanguageTool(languageBox.getDefaultLanguage());
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

  private PopupMenu makePopupMenu() {
    final PopupMenu popup = new PopupMenu();
    final ActionListener rmbListener = new TrayActionRMBListener();
    // Enable or disable embedded HTTP server:
    enableHttpServerItem = new CheckboxMenuItem(StringTools.getLabel(messages.getString("tray_menu_enable_server")));
    enableHttpServerItem.setState(httpServer != null && httpServer.isRunning());
    enableHttpServerItem.addItemListener(new TrayActionItemListener());
    popup.add(enableHttpServerItem);
    // Check clipboard text:
    final MenuItem checkClipboardItem =
            new MenuItem(StringTools.getLabel(messages.getString("guiMenuCheckClipboard")));
    checkClipboardItem.addActionListener(rmbListener);
    popup.add(checkClipboardItem);
    // Open main window:
    final MenuItem restoreItem = new MenuItem(StringTools.getLabel(messages.getString("guiMenuShowMainWindow")));
    restoreItem.addActionListener(rmbListener);
    popup.add(restoreItem);
    // Exit:
    final MenuItem exitItem = new MenuItem(StringTools.getLabel(messages.getString("guiMenuQuit")));
    exitItem.addActionListener(rmbListener);
    popup.add(exitItem);
    return popup;
  }

  void checkClipboardText() {
    final String s = getClipboardText();
    textArea.setText(s);
    checkTextAndDisplayResults();
  }

  void hideToTray() {
    if (!isInTray) {
      final SystemTray tray = SystemTray.getSystemTray();
      final String iconPath = tray.getTrayIconSize().height > 16 ? TRAY_ICON : TRAY_SMALL_ICON;
      final URL iconUrl = JLanguageTool.getDataBroker().getFromResourceDirAsUrl(iconPath);
      final Image img = Toolkit.getDefaultToolkit().getImage(iconUrl);
      final PopupMenu popup = makePopupMenu();
      try {
        trayIcon = new TrayIcon(img, TRAY_TOOLTIP, popup);
        trayIcon.addMouseListener(new TrayActionListener());
        setTrayIcon();
        tray.add(trayIcon);
      } catch (AWTException e1) {
        // thrown if there's no system tray
        Tools.showError(e1);
      }
    }
    isInTray = true;
    frame.setVisible(false);
  }

  void tagText() {
    new Thread() {
      @Override
      public void run() {
        setWaitCursor();
        try {
          final JLanguageTool langTool = getCurrentLanguageTool(getCurrentLanguage());
          tagTextAndDisplayResults(langTool);
        } finally {
          unsetWaitCursor();
        }
      }
    }.start();
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
      config.saveConfiguration(getCurrentLanguage());
    } catch (IOException e) {
      Tools.showError(e);
    }
    frame.setVisible(false);
    JLanguageTool.removeTemporaryFiles();
    System.exit(0);
  }

  private void setTrayIcon() {
    if (trayIcon != null) {
      final SystemTray tray = SystemTray.getSystemTray();
      final boolean httpServerRunning = httpServer != null && httpServer.isRunning();
      final boolean smallTray = tray.getTrayIconSize().height <= 16;
      final String iconPath;
      if (httpServerRunning) {
        trayIcon.setToolTip(messages.getString("tray_tooltip_server_running"));
        iconPath = smallTray ? TRAY_SMALL_SERVER_ICON : TRAY_SERVER_ICON;
      } else {
        trayIcon.setToolTip(TRAY_TOOLTIP);
        iconPath = smallTray ? TRAY_SMALL_ICON : TRAY_ICON;
      }
      final URL iconUrl = JLanguageTool.getDataBroker().getFromResourceDirAsUrl(iconPath);
      final Image img = Toolkit.getDefaultToolkit().getImage(iconUrl);
      trayIcon.setImage(img);
    }
  }

  private void showGUI() {
    frame.setVisible(true);
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
    } catch (Exception ex) {
      if (data != null) {
        s = data.toString();
      } else {
        s = "";
      }
    }
    return s;
  }

  private boolean maybeStartServer() {
    if (config.getRunServer()) {
      try {
        final HTTPServerConfig serverConfig = new HTTPServerConfig(config.getServerPort(), false);
        httpServer = new HTTPServer(serverConfig, true);
    	  httpServer.run();
        if (enableHttpServerItem != null) {
          enableHttpServerItem.setState(httpServer.isRunning());
          setTrayIcon();
        }
      } catch (PortBindingException e) {
        JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
    return httpServer != null && httpServer.isRunning();
  }

  private void stopServer() {
    if (httpServer != null) {
      httpServer.stop();
      if (enableHttpServerItem != null) {
        enableHttpServerItem.setState(httpServer.isRunning());
        setTrayIcon();
      }
      httpServer = null;
    }
  }

  private Language getCurrentLanguage() {
    if (autoDetectBox.isSelected()) {
      return autoDetectBox.autoDetectLanguage(textArea.getText());
    } else {
      return ((I18nLanguage) languageBox.getSelectedItem()).getLanguage();
    }
  }

  private ConfigurationDialog getCurrentConfigDialog(Language language) {
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

  private JLanguageTool getCurrentLanguageTool(Language currentLanguage) {
    if (langTool == null) {
      try {
        config = new Configuration(new File(System.getProperty("user.home")), CONFIG_FILE, currentLanguage);
        resultArea.setConfiguration(config);
        final ConfigurationDialog configDialog = getCurrentConfigDialog(currentLanguage);
        langTool = new JLanguageTool(currentLanguage, configDialog.getMotherTongue());
        langTool.activateDefaultPatternRules();
        langTool.activateDefaultFalseFriendRules();
        resultArea.setLanguageTool(langTool);
        final Set<String> disabledRules = configDialog.getDisabledRuleIds();
        if (disabledRules != null) {
          for (final String ruleId : disabledRules) {
            langTool.disableRule(ruleId);
          }
        }
        final Set<String> disabledCategories = configDialog.getDisabledCategoryNames();
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
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return langTool;
  }

  private void checkTextAndDisplayResults() {
      final Language lang = getCurrentLanguage();
      if (StringTools.isEmpty(textArea.getText().trim())) {
          textArea.setText(messages.getString("enterText2"));
      } else {
          final String langName;
          if (lang.isExternal()) {
              langName = lang.getTranslatedName(messages) + EXTERNAL_LANGUAGE_SUFFIX;
          } else {
              langName = lang.getTranslatedName(messages);
          }
          new Thread() {
              @Override
              public void run() {
                  if (!isAlreadyChecking) {
                      isAlreadyChecking = true;
                      setWaitCursor();
                      checkTextButton.setEnabled(false);
                      try {
                          final long startTime = System.currentTimeMillis();
                          final String startCheckText = HTML_GREY_FONT_START +
                                  Tools.makeTexti18n(messages, "startChecking", langName) + "..." + HTML_FONT_END;
                          resultArea.setText(startCheckText);
                          resultArea.repaint();
                          try {
                            final JLanguageTool langTool = getCurrentLanguageTool(lang);
                            ruleMatches = langTool.check(textArea.getText());
                            resultArea.setStartText(startCheckText);
                            resultArea.setInputText(textArea.getText());
                            resultArea.setRuleMatches(ruleMatches);
                            resultArea.setRunTime(System.currentTimeMillis() - startTime);
                            resultArea.setLanguageTool(langTool);
                            resultArea.displayResult();
                          } catch (Exception e) {
                            final String error = getStackTraceAsHtml(e);
                            resultArea.displayText(error);
                          }
                      } finally {
                          checkTextButton.setEnabled(true);
                          unsetWaitCursor();
                          isAlreadyChecking = false;
                      }
                  }
              }
          }.start();
      }
  }

  private String getStackTraceAsHtml(Exception e) {
    return "<br><br><b><font color=\"red\">"
         + org.languagetool.tools.Tools.getFullStackTrace(e).replace("\n", "<br/>")
         + "</font></b><br>";
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
          sb.append(analyzedTextString).append("\n");
        }
      } catch (Exception e) {
        sb.append(getStackTraceAsHtml(e));
      }
      resultArea.setText(HTML_FONT_START + sb.toString() + HTML_FONT_END);
    }
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
            } catch (Exception e) {
              Tools.showError(e);
              System.exit(1);
            }
          }
        });
      } else if (args.length >= 1) {
        System.out.println("Usage: java org.languagetool.gui.Main [-t|--tray]");
        System.out.println("  -t, --tray: dock LanguageTool to system tray on startup");
        prg.stopServer();
      } else {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            try {
              prg.createGUI();
              prg.showGUI();
            } catch (Exception e) {
              Tools.showError(e);
            }
          }
        });
      }
    } catch (Exception e) {
      Tools.showError(e);
    }
  }

  private class ControlReturnTextCheckingListener implements KeyListener {

    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
      if (e.getKeyCode() == KeyEvent.VK_ENTER) {
        if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK) {
          checkTextAndDisplayResults();
        }
      }
    }

  }
  
  //
  // The System Tray stuff
  //

  class TrayActionItemListener implements ItemListener {
    @Override
    public void itemStateChanged(ItemEvent e) {
      try {
        final Language language = getCurrentLanguage();
        final ConfigurationDialog configDialog = configDialogs.get(language);
        if (e.getStateChange() == ItemEvent.SELECTED) {
          config.setRunServer(true);
          final boolean serverStarted = maybeStartServer();
          enableHttpServerItem.setState(serverStarted);
          config.setRunServer(serverStarted);
          config.saveConfiguration(language);
          if (configDialog != null) {
            configDialog.setRunServer(true);
          }
        } else if (e.getStateChange() == ItemEvent.DESELECTED) {
          config.setRunServer(false);
          config.saveConfiguration(language);
          if (configDialog != null) {
            configDialog.setRunServer(false);
          }
          stopServer();
        }
      } catch (IOException ex) {
        Tools.showError(ex);
      }
    }
  }

  class TrayActionRMBListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      if (isCommand(e, "guiMenuCheckClipboard")) {
        restoreFromTrayAndCheck();
      } else if (isCommand(e, "guiMenuShowMainWindow")) {
        restoreFromTray();
      } else if (isCommand(e, "guiMenuQuit")) {
        quit();
      } else {
        JOptionPane.showMessageDialog(null, "Unknown action: "
            + e.getActionCommand(), "Error", JOptionPane.ERROR_MESSAGE);
      }
    }

    private boolean isCommand(ActionEvent e, String label) {
      return e.getActionCommand().equalsIgnoreCase(StringTools.getLabel(messages.getString(label)));
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
