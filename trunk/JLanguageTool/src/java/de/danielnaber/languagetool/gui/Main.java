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
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;
import javax.swing.UIManager;
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

  private ResourceBundle messages;
  
  private static final String HTML_FONT_START = "<font face='Arial,Helvetica'>";
  private static final String HTML_FONT_END = "</font>";
  
  private final Icon SYSTEM_TRAY_ICON = new ImageIcon(this.getClass().getResource("/resource/TrayIcon.png"));
  private static final String SYSTEM_TRAY_TOOLTIP = "LanguageTool";
  private static final String CONFIG_FILE = ".languagetool.cfg";

  private Configuration config = null;
  
  private JFrame frame = null;
  private JTextArea textArea = null;
  private JTextPane resultArea = null;
  private JComboBox langBox = null;
  
  private HTTPServer httpServer = null;
  
  private Map<Language, ConfigurationDialog> configDialogs = new HashMap<Language, ConfigurationDialog>();

  // whether clicking on the window close button hides to system tray:
  private boolean trayMode = false;

  private boolean isInTray = false;

  private Main() throws IOException {
    config = new Configuration(new File(System.getProperty("user.home")), CONFIG_FILE);
    messages = JLanguageTool.getMessageBundle();
    maybeStartServer();
  }

  private void createGUI() {
    frame = new JFrame("LanguageTool " + JLanguageTool.VERSION);
    frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    frame.addWindowListener(new CloseListener());
    frame.setIconImage(new ImageIcon(this.getClass().getResource("/resource/TrayIcon.png")).getImage());
    frame.setJMenuBar(new MainMenuBar(this, messages));

    textArea = new JTextArea(messages.getString("guiDemoText"));
    // TODO: wrong line number is displayed for lines that are wrapped automatically:
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    resultArea = new JTextPane();
    resultArea.setContentType("text/html");
    resultArea.setText(HTML_FONT_START + messages.getString("resultAreaText") + HTML_FONT_END);
    resultArea.setEditable(false);
    JLabel label = new JLabel(messages.getString("enterText"));
    JButton button = new JButton(messages.getString("checkText"));
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
    panel.add(new JLabel(" " + messages.getString("textLanguage") + " "), buttonCons);
    buttonCons.gridx = 2;
    buttonCons.gridy = 0;
    langBox = new JComboBox();
    populateLanguageBox();
    // use the system default language to preselect the language from the combo box:
    try {
      Locale defaultLocale = Locale.getDefault();
      langBox.setSelectedItem(messages.getString((defaultLocale.getLanguage())));
    } catch (MissingResourceException e) {
      // language not supported, so don't select a default
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
    cons.insets = new Insets(3, 3, 3, 3);
    //cons.fill = GridBagConstraints.NONE;
    contentPane.add(label, cons);
    cons.gridy = 3;
    contentPane.add(panel, cons);
    
    frame.pack();
    frame.setSize(600, 550);
  }

  private void populateLanguageBox() {
    langBox.removeAllItems();
    for (Language lang : Language.LANGUAGES) {
      if (lang != Language.DEMO) {
        try {
          langBox.addItem(messages.getString(lang.getShortName()));
        } catch (MissingResourceException e) {
          // can happen with external rules:
          langBox.addItem(lang.getName());
        }
      }
    }
  }

  private void showGUI() {
    frame.setVisible(true);
  }
  
  public void actionPerformed(final ActionEvent e) {
    try {
      if (e.getActionCommand().equals(messages.getString("checkText"))) {
        JLanguageTool langTool = getCurrentLanguageTool();
        checkTextAndDisplayResults(langTool, getCurrentLanguage());
      } else {
        throw new IllegalArgumentException("Unknown action " + e);
      }
    } catch (Exception exc) {
      Tools.showError(exc);
    }
  }

  void loadFile() {
    File file = Tools.openFileDialog(frame, new PlainTextFilter());
    if (file == null)   // user cancelled
      return;
    try {
      String fileContents = StringTools.readFile(new FileInputStream(file.getAbsolutePath()));
      textArea.setText(fileContents);
      JLanguageTool langTool = getCurrentLanguageTool();
      checkTextAndDisplayResults(langTool, getCurrentLanguage());
    } catch (IOException e) {
      Tools.showError(e);
    }
  }
  
  void hideToTray() {
    String version = System.getProperty("java.version");
    if (!isInTray && version.startsWith("1.5")) {    // we don't run under <= 1.4, so we don't check for that
      TrayIcon trayIcon = null;
      try {
        trayIcon = new TrayIcon(SYSTEM_TRAY_ICON);
      } catch (NoClassDefFoundError e) {
        throw new MissingJdicException(e);
      }
      SystemTray tray = SystemTray.getDefaultSystemTray();
      trayIcon.addActionListener(new TrayActionListener());
      trayIcon.setToolTip(SYSTEM_TRAY_TOOLTIP);
      tray.addTrayIcon(trayIcon);
    } else if (!isInTray) {
      // Java 1.6 or later
      java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();
      Image img = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/resource/TrayIcon.png"));
      PopupMenu popup = makePopupMenu();
      try {
        java.awt.TrayIcon trayIcon = new java.awt.TrayIcon(img, "tooltip", popup);
        trayIcon.addMouseListener(new TrayActionListener());
        trayIcon.setToolTip(SYSTEM_TRAY_TOOLTIP);
        tray.add(trayIcon);
      } catch (AWTException e1) {
        // thrown if there's no system tray
        Tools.showError(e1);
      }
    }
    isInTray = true;
    frame.setVisible(false);
  }

  private PopupMenu makePopupMenu() {
    PopupMenu popup = new PopupMenu();
    ActionListener rmbListener = new TrayActionRMBListener(); 
    // Check clipboard text:
    MenuItem checkClipboardItem = new MenuItem(messages.getString("guiMenuCheckClipboard"));
    checkClipboardItem.addActionListener(rmbListener);
    popup.add(checkClipboardItem);
    // Open main window:
    MenuItem restoreItem = new MenuItem(messages.getString("guiMenuShowMainWindow"));
    restoreItem.addActionListener(rmbListener);
    popup.add(restoreItem);
    // Exit:
    MenuItem exitItem = new MenuItem(messages.getString("guiMenuQuit"));
    exitItem.addActionListener(rmbListener);
    popup.add(exitItem);
    return popup;
  }

  void addLanguage() {
    LanguageManagerDialog lmd = new LanguageManagerDialog(frame, Language.getExternalLanguages());
    lmd.show();
    try {
      Language.reInit(lmd.getLanguages());
    } catch (RuleFilenameException e) {
      Tools.showErrorMessage(e);
    }
    populateLanguageBox();
  }
  
  void showOptions() {
    JLanguageTool langTool = getCurrentLanguageTool();
    List<Rule> rules = langTool.getAllRules();
    ConfigurationDialog configDialog = getCurrentConfigDialog();
    configDialog.show(rules);   // this blocks until OK/Cancel is clicked in the dialog
    config.setDisabledRuleIds(configDialog.getDisabledRuleIds());
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
    String s = getClipboardText();
    restoreFromTray();
    textArea.setText(s);
    JLanguageTool langTool = getCurrentLanguageTool();
    checkTextAndDisplayResults(langTool, getCurrentLanguage());
  }

  void checkClipboardText() {
    String s = getClipboardText();
    textArea.setText(s);
    JLanguageTool langTool = getCurrentLanguageTool();
    checkTextAndDisplayResults(langTool, getCurrentLanguage());
  }
  
  private String getClipboardText() {
    // get text from clipboard or selection:
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemSelection();
    if (clipboard == null) {    // on Windows
      clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    }
    String s = null;
    final Transferable data = clipboard.getContents(this);
    try {
      if (data != null
          && data.isDataFlavorSupported(
              DataFlavor.getTextPlainUnicodeFlavor())) {
        final DataFlavor df = DataFlavor.getTextPlainUnicodeFlavor();
        final Reader sr = df.getReaderForText(data);
        s = StringTools.readerToString(sr);
      } else {
        s = "";
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      s = data.toString();
    }
    return s;
  }

  void quitOrHide() {
    if (trayMode)
      hideToTray();
    else
      quit();
  }
  
  void quit() {
    stopServer();
    try {
      config.saveConfiguration();
    } catch (IOException e) {
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
      } catch (PortBindingException e) {
        JOptionPane.showMessageDialog(null, e.getMessage(), "Error",
            JOptionPane.ERROR_MESSAGE);
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
    String langName = langBox.getSelectedItem().toString();
    String lang = langName;
    for (Enumeration<String> e = messages.getKeys(); e.hasMoreElements();) {
      String elem = e.nextElement();
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
    Language language = getCurrentLanguage();
    ConfigurationDialog configDialog = null;
    if (configDialogs.containsKey(language)) {
      configDialog = (ConfigurationDialog)configDialogs.get(language);
    } else {
      configDialog = new ConfigurationDialog(frame, false);
      configDialog.setMotherTongue(config.getMotherTongue());
      configDialog.setDisabledRules(config.getDisabledRuleIds());
      configDialog.setRunServer(config.getRunServer());
      configDialog.setServerPort(config.getServerPort());
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

  private void checkTextAndDisplayResults(final JLanguageTool langTool, final Language lang) {
    if (textArea.getText().trim().equals("")) {
      textArea.setText(messages.getString("enterText2"));
    } else {
      StringBuilder sb = new StringBuilder();
      String startChecktext = Tools.makeTexti18n(messages, "startChecking", 
          new Object[] { lang.getTranslatedName(messages) });
      resultArea.setText(HTML_FONT_START + startChecktext +"<br>\n" + HTML_FONT_END);
      resultArea.repaint(); // FIXME: why doesn't this work?
      //TODO: resultArea.setCursor(new Cursor(Cursor.WAIT_CURSOR)); 
      sb.append(startChecktext+"...<br>\n");
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
      String checkDone = Tools.makeTexti18n(messages, "checkDone", new Object[] { Integer.valueOf(matches) });
      sb.append(checkDone + "<br>\n");
      resultArea.setText(HTML_FONT_START + sb.toString() + HTML_FONT_END);
      resultArea.setCaretPosition(0);
    }
  }

  private int checkText(final JLanguageTool langTool, final String text, final StringBuilder sb) throws IOException {
    long startTime = System.currentTimeMillis();
    List<RuleMatch> ruleMatches = langTool.check(text);
    long startTimeMatching = System.currentTimeMillis();
    int i = 0;
    for (RuleMatch match : ruleMatches) {
      String output = Tools.makeTexti18n(messages, "result1", new Object[] {
          Integer.valueOf(i+1), Integer.valueOf(match.getLine()+1), Integer.valueOf(match.getColumn())
      });
      sb.append(output);
      String msg = match.getMessage();
      msg = msg.replaceAll("<suggestion>", "<b>");
      msg = msg.replaceAll("</suggestion>", "</b>");
      msg = msg.replaceAll("<old>", "<b>");
      msg = msg.replaceAll("</old>", "</b>");
      sb.append("<b>" +messages.getString("errorMessage")+ "</b> " + msg + "<br>\n");
      if (match.getSuggestedReplacements().size() > 0) {
        String repl = StringTools.listToString(match.getSuggestedReplacements(), "; ");
        sb.append("<b>" +messages.getString("correctionMessage")+ "</b> " + repl + "<br>\n");
      }
      String context = Tools.getContext(match.getFromPos(), match.getToPos(), text);
      sb.append("<b>" +messages.getString("errorContext")+ "</b> " + context);
      sb.append("<br>\n");
      i++;
    }
    long endTime = System.currentTimeMillis();
    sb.append(Tools.makeTexti18n(messages, "resultTime", new Object[] {
       Long.valueOf(endTime - startTime), 
       Long.valueOf(endTime - startTimeMatching)
    }));
    return ruleMatches.size();
  }

  private void setTrayMode(boolean trayMode) {
    this.trayMode = trayMode;
  }

  public static void main(final String[] args) {
    try {
      final Main prg = new Main();
      if (args.length == 1 && (args[0].equals("-t") || args[0].equals("--tray"))) {
        // dock to systray on startup
        javax.swing.SwingUtilities.invokeLater(new Runnable() {          
          public void run() {
            try {
              prg.createGUI();
              prg.setTrayMode(true);
              prg.hideToTray();
            } catch (MissingJdicException e) {
              JOptionPane.showMessageDialog(null, e.getMessage(), "Error",
                  JOptionPane.ERROR_MESSAGE);
              System.exit(1);
            } catch (Exception e) {
              Tools.showError(e);
              System.exit(1);
            }
          }
        });
      } else if (args.length >= 1) {
        System.out.println("Usage: java de.danielnaber.languagetool.gui.Main [-t|--tray]");
        System.out.println("  -t, --tray: dock LanguageTool to system tray on startup");
      } else {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {          
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

  //
  // The System Tray stuff
  //

  class TrayActionRMBListener implements ActionListener {

      public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equalsIgnoreCase(messages.getString("guiMenuCheckClipboard"))) {
          restoreFromTrayAndCheck();
        } else if (e.getActionCommand().equalsIgnoreCase(messages.getString("guiMenuShowMainWindow"))) {
          restoreFromTray();
        } else if (e.getActionCommand().equalsIgnoreCase(messages.getString("guiMenuQuit"))) {
          quit();
        } else {
          JOptionPane.showMessageDialog(null, "Unknown action: " + e.getActionCommand(), "Error",
              JOptionPane.ERROR_MESSAGE);
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

    public void mouseEntered(@SuppressWarnings("unused")MouseEvent e) {}
    public void mouseExited(@SuppressWarnings("unused")MouseEvent e) {}
    public void mousePressed(@SuppressWarnings("unused")MouseEvent e) {}
    public void mouseReleased(@SuppressWarnings("unused")MouseEvent e) {}
    
  }

  class CloseListener implements WindowListener {

    public void windowClosing(@SuppressWarnings("unused") WindowEvent e) {
      quitOrHide();
    }

    public void windowActivated(@SuppressWarnings("unused")WindowEvent e) {}
    public void windowClosed(@SuppressWarnings("unused")WindowEvent e) {}
    public void windowDeactivated(@SuppressWarnings("unused")WindowEvent e) {}
    public void windowDeiconified(@SuppressWarnings("unused")WindowEvent e) {}
    public void windowIconified(@SuppressWarnings("unused")WindowEvent e) {}
    public void windowOpened(@SuppressWarnings("unused")WindowEvent e) {}
    
  }
  
  static class PlainTextFilter extends FileFilter {

    public boolean accept(final File f) {
      if (f.getName().toLowerCase().endsWith(".txt"))
        return true;
      return false;
    }

    public String getDescription() {
      return "*.txt";
    }
    
  }
  
}
