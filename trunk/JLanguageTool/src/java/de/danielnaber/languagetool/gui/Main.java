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

import java.awt.AWTException;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.ParserConfigurationException;

import org.jdesktop.jdic.tray.SystemTray;
import org.jdesktop.jdic.tray.TrayIcon;
import org.xml.sax.SAXException;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.language.RuleFilenameException;
import de.danielnaber.languagetool.rules.Rule;
import de.danielnaber.languagetool.rules.RuleMatch;
import de.danielnaber.languagetool.server.HTTPServer;
import de.danielnaber.languagetool.server.PortBindingException;
import de.danielnaber.languagetool.tools.StringTools;

/**
 * A simple GUI to check texts with.
 * 
 * @author Daniel Naber
 */
public final class Main implements ActionListener {

  private static final String HTML_FONT_START = "<font face='Arial,Helvetica'>";

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
  private JComboBox languageBox;

  private HTTPServer httpServer;

  private final Map<Language, ConfigurationDialog> configDialogs = new HashMap<Language, ConfigurationDialog>();

  private boolean closeHidesToTray;
  private boolean isInTray;

  private Main() throws IOException {
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
    resultArea = new JTextPane();
    resultArea.setContentType("text/html");
    resultArea.setText(HTML_FONT_START + messages.getString("resultAreaText")
        + HTML_FONT_END);
    resultArea.setEditable(false);
    final JLabel label = new JLabel(messages.getString("enterText"));
    final JButton button = new JButton(StringTools.getLabel(messages
        .getString("checkText")));
    button
        .setMnemonic(StringTools.getMnemonic(messages.getString("checkText")));
    button.addActionListener(this);

    final JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());
    final GridBagConstraints buttonCons = new GridBagConstraints();
    buttonCons.gridx = 0;
    buttonCons.gridy = 0;
    panel.add(button, buttonCons);
    buttonCons.gridx = 1;
    buttonCons.gridy = 0;
    panel.add(new JLabel(" " + messages.getString("textLanguage") + " "), buttonCons);
    buttonCons.gridx = 2;
    buttonCons.gridy = 0;
    languageBox = new JComboBox();
    populateLanguageBox(languageBox);
    preselectLanguage(languageBox);
    panel.add(languageBox, buttonCons);

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
    cons.insets = new Insets(3, 3, 3, 3);
    // cons.fill = GridBagConstraints.NONE;
    contentPane.add(label, cons);
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
    } catch (Exception ex) {
      // Well, what can we do...
    }
  }

  private void populateLanguageBox(final JComboBox languageBox) {
    final List<String> languages = new ArrayList<String>();
    languageBox.removeAllItems();
    for (final Language language : Language.LANGUAGES) {
      if (language != Language.DEMO) {
        try {
          languages.add(messages.getString(language.getShortName()));
        } catch (final MissingResourceException e) {
          // can happen with external rules:
          languages.add(language.getName());
        }
      }
    }
    Collections.sort(languages);
    for (final String languageName : languages) {
      languageBox.addItem(languageName);
    }
  }

  private void preselectLanguage(final JComboBox languageBox) {
    // use the system default language to preselect the language from the combo
    // box:
    try {
      final Locale defaultLocale = Locale.getDefault();
      languageBox.setSelectedItem(messages.getString(defaultLocale.getLanguage()));
    } catch (final MissingResourceException e) {
      // language not supported, so don't select a default
    }
  }

  private void showGUI() {
    frame.setVisible(true);
  }

  public void actionPerformed(final ActionEvent e) {
    try {
      if (e.getActionCommand().equals(
          StringTools.getLabel(messages.getString("checkText")))) {
        final JLanguageTool langTool = getCurrentLanguageTool();
        checkTextAndDisplayResults(langTool, getCurrentLanguage());
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
      final JLanguageTool langTool = getCurrentLanguageTool();
      checkTextAndDisplayResults(langTool, getCurrentLanguage());
    } catch (final IOException e) {
      Tools.showError(e);
    }
  }

  void hideToTray() {
    final String version = System.getProperty("java.version");
    if (!isInTray && version.startsWith("1.5")) { // we don't run under <= 1.4,
      // so we don't check for that
      TrayIcon trayIcon = null;
      try {
    	final Icon sysTrayIcon = new ImageIcon(JLanguageTool.getDataBroker().getFromResourceDirAsUrl(Main.SYSTEM_TRAY_ICON_NAME));
        trayIcon = new TrayIcon(sysTrayIcon);
      } catch (final NoClassDefFoundError e) {
        throw new MissingJdicException(e);
      }
      final SystemTray tray = SystemTray.getDefaultSystemTray();
      trayIcon.addActionListener(new TrayActionListener());
      trayIcon.setToolTip(SYSTEM_TRAY_TOOLTIP);
      tray.addTrayIcon(trayIcon);
    } else if (!isInTray) {
      // Java 1.6 or later
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
    populateLanguageBox(languageBox);
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
    config.setServerPort(configDialog.getServerPort());
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
    final JLanguageTool langTool = getCurrentLanguageTool();
    checkTextAndDisplayResults(langTool, getCurrentLanguage());
  }

  void checkClipboardText() {
    final String s = getClipboardText();
    textArea.setText(s);
    final JLanguageTool langTool = getCurrentLanguageTool();
    checkTextAndDisplayResults(langTool, getCurrentLanguage());
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
      ex.printStackTrace();
      if (data != null) {
        s = data.toString();
      } else {
        s = "";
      }
    }
    return s;
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
    System.exit(0);
  }

  private void maybeStartServer() {
    if (config.getRunServer()) {
      httpServer = new HTTPServer(config.getServerPort());
      try {
        httpServer.run();
      } catch (final PortBindingException e) {
        JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void stopServer() {
    if (httpServer != null) {
      httpServer.stop();
      httpServer = null;
    }
  }

  private Language getCurrentLanguage() {
    final String langName = languageBox.getSelectedItem().toString();
    String lang = langName;
    for (final Enumeration<String> e = messages.getKeys(); e.hasMoreElements();) {
      final String elem = e.nextElement();
      if (messages.getString(elem).equals(langName)) {
        lang = elem;
        break;
      }
    }
    // external rules:
    if (lang.length() > 2) {
      return Language.getLanguageForName(lang);
    }
    return Language.getLanguageForShortName(lang);
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
    } catch (final IOException ioe) {
      throw new RuntimeException(ioe);
    } catch (final ParserConfigurationException ex) {
      throw new RuntimeException(ex);
    } catch (final SAXException ex) {
      throw new RuntimeException(ex);
    }
    return langTool;
  }

  private void checkTextAndDisplayResults(final JLanguageTool langTool,
      final Language lang) {
    if (StringTools.isEmpty(textArea.getText().trim())) {
      textArea.setText(messages.getString("enterText2"));
    } else {
      final StringBuilder sb = new StringBuilder();
      final String startCheckText = Tools.makeTexti18n(messages,
          "startChecking", new Object[] { lang.getTranslatedName(messages) });
      resultArea.setText(HTML_FONT_START + startCheckText + "<br>\n"
          + HTML_FONT_END);
      resultArea.repaint(); // FIXME: why doesn't this work?
      // TODO: resultArea.setCursor(new Cursor(Cursor.WAIT_CURSOR));
      sb.append(startCheckText);
      sb.append("...<br>\n");
      int matches = 0;
      try {
        matches = checkText(langTool, textArea.getText(), sb);
      } catch (final Exception ex) {
        sb.append("<br><br><b><font color=\"red\">" + ex.toString() + "<br>");
        final StackTraceElement[] elements = ex.getStackTrace();
        for (final StackTraceElement element : elements) {
          sb.append(element);
          sb.append("<br>");
        }
        sb.append("</font></b><br>");
        ex.printStackTrace();
      }
      final String checkDone = Tools.makeTexti18n(messages, "checkDone",
          new Object[] {matches});
      sb.append(checkDone);
      sb.append("<br>\n");
      resultArea.setText(HTML_FONT_START + sb.toString() + HTML_FONT_END);
      resultArea.setCaretPosition(0);
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
      i++;
    }
    final long endTime = System.currentTimeMillis();
    sb.append(Tools.makeTexti18n(messages, "resultTime", new Object[] {
            endTime - startTime,
            endTime - startTimeMatching}));
    return ruleMatches.size();
  }

  private void setTrayMode(boolean trayMode) {
    this.closeHidesToTray = trayMode;
  }

  public static void main(final String[] args) {
    try {
      final Main prg = new Main();
      if (args.length == 1
          && (args[0].equals("-t") || args[0].equals("--tray"))) {
        // dock to systray on startup
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            try {
              prg.createGUI();
              prg.setTrayMode(true);
              prg.hideToTray();
            } catch (final MissingJdicException e) {
              JOptionPane.showMessageDialog(null, e.getMessage(), "Error",
                  JOptionPane.ERROR_MESSAGE);
              System.exit(1);
            } catch (final Exception e) {
              Tools.showError(e);
              System.exit(1);
            }
          }
        });
      } else if (args.length >= 1) {
        System.out
            .println("Usage: java de.danielnaber.languagetool.gui.Main [-t|--tray]");
        System.out
            .println("  -t, --tray: dock LanguageTool to system tray on startup");
      } else {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
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

  //
  // The System Tray stuff
  //

  class TrayActionRMBListener implements ActionListener {

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

  class TrayActionListener implements ActionListener, MouseListener {

    // for Java 1.5 / Jdic:
    public void actionPerformed(@SuppressWarnings("unused")ActionEvent e) {
      handleClick();
    }

    // Java 1.6:
    public void mouseClicked(@SuppressWarnings("unused")MouseEvent e) {
      handleClick();
    }

    private void handleClick() {
      if (frame.isVisible() && frame.isActive()) {
        frame.setVisible(false);
      } else if (frame.isVisible() && !frame.isActive()) {
        frame.toFront();
        restoreFromTrayAndCheck();
      } else {
        restoreFromTrayAndCheck();
      }
    }

    public void mouseEntered(@SuppressWarnings("unused") MouseEvent e) {
    }

    public void mouseExited(@SuppressWarnings("unused")MouseEvent e) {
    }

    public void mousePressed(@SuppressWarnings("unused")MouseEvent e) {
    }

    public void mouseReleased(@SuppressWarnings("unused")MouseEvent e) {
    }

  }

  class CloseListener implements WindowListener {

    public void windowClosing(@SuppressWarnings("unused")WindowEvent e) {
      quitOrHide();
    }

    public void windowActivated(@SuppressWarnings("unused")WindowEvent e) {
    }

    public void windowClosed(@SuppressWarnings("unused")WindowEvent e) {
    }

    public void windowDeactivated(@SuppressWarnings("unused")WindowEvent e) {
    }

    public void windowDeiconified(@SuppressWarnings("unused")WindowEvent e) {
    }

    public void windowIconified(@SuppressWarnings("unused")WindowEvent e) {
    }

    public void windowOpened(@SuppressWarnings("unused")WindowEvent e) {
    }

  }

  static class PlainTextFileFilter extends FileFilter {

    @Override
    public boolean accept(final File f) {
      return f.getName().toLowerCase().endsWith(".txt");
    }

    @Override
    public String getDescription() {
      return "*.txt";
    }

  }

}
