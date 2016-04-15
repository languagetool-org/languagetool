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
import org.languagetool.rules.Rule;
import org.languagetool.server.HTTPServer;
import org.languagetool.server.HTTPServerConfig;
import org.languagetool.server.PortBindingException;
import org.languagetool.tools.JnaTools;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import org.apache.commons.lang.StringUtils;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;

/**
 * A simple GUI to check texts with.
 *
 * @author Daniel Naber
 */
public final class Main {

  static final String EXTERNAL_LANGUAGE_SUFFIX = " (ext.)";
  static final String HTML_FONT_START = "<font face='Arial,Helvetica'>";
  static final String HTML_FONT_END = "</font>";
  static final String HTML_GREY_FONT_START = "<font face='Arial,Helvetica' color='#666666'>";

  private static final String TRAY_ICON = "/TrayIcon.png";
  private static final String TRAY_SERVER_ICON = "/TrayIconWithServer.png";
  private static final String TRAY_SMALL_ICON = "/TrayIconSmall.png";
  private static final String TRAY_SMALL_SERVER_ICON = "/TrayIconSmallWithServer.png";
  private static final String TRAY_TOOLTIP = "LanguageTool";
  private static final String TAG_COLOR = "#888888";

  private static final int WINDOW_WIDTH = 600;
  private static final int WINDOW_HEIGHT = 550;

  private final ResourceBundle messages;
  private final List<Language> externalLanguages = new ArrayList<>();

  private JFrame frame;
  private JDialog taggerDialog;
  private JTextPane taggerArea;
  private JTextArea textArea;
  private JTextPane resultArea;
  private ResultArea resultAreaHelper;
  private LanguageComboBox languageBox;
  private CheckboxMenuItem enableHttpServerItem;
  private HTTPServer httpServer;

  private TrayIcon trayIcon;
  private boolean closeHidesToTray;
  private boolean isInTray;
  private boolean taggerShowsDisambigLog = false;

  private LanguageToolSupport ltSupport;
  private OpenAction openAction;
  private SaveAction saveAction;
  private SaveAsAction saveAsAction;
  private AutoCheckAction autoCheckAction;

  private CheckAction checkAction;
  private File currentFile;
  private UndoRedoSupport undoRedo;
  private final JLabel statusLabel = new JLabel(" ", null, SwingConstants.RIGHT);
  private FontChooser fontChooserDialog;

  private Main() {
    messages = JLanguageTool.getMessageBundle();
  }

  private void loadFile() {
    File file = Tools.openFileDialog(frame, new PlainTextFileFilter());
    if (file == null) {  // user clicked cancel
      return;
    }
    loadFile(file);
  }

  private void loadFile(File file) {
    try (FileInputStream inputStream = new FileInputStream(file)) {
      String fileContents = StringTools.readStream(inputStream, null);
      textArea.setText(fileContents);
      currentFile = file;
      updateTitle();
    } catch (IOException e) {
      Tools.showError(e);
    }
  }

  private void saveFile(boolean newFile) {
    if (currentFile == null || newFile) {
      JFileChooser jfc = new JFileChooser();
      jfc.setFileFilter(new PlainTextFileFilter());
      jfc.showSaveDialog(frame);

      File file = jfc.getSelectedFile();
      if (file == null) {  // user clicked cancel
        return;
      }
      currentFile = file;
      updateTitle();
    }
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile))) {
      writer.write(textArea.getText());
    } catch (IOException ex) {
      Tools.showError(ex);
    }
  }

  private void addLanguage() throws InstantiationException, IllegalAccessException {
    LanguageManagerDialog dialog = new LanguageManagerDialog(frame, externalLanguages);
    dialog.show();
    List<Language> newExtLanguages = dialog.getLanguages();
    externalLanguages.clear();
    externalLanguages.addAll(newExtLanguages);
    languageBox.populateLanguageBox(externalLanguages);
    languageBox.selectLanguage(ltSupport.getLanguage());
  }

  private void showOptions() {
    JLanguageTool langTool = ltSupport.getLanguageTool();
    List<Rule> rules = langTool.getAllRules();
    ConfigurationDialog configDialog = ltSupport.getCurrentConfigDialog();
    configDialog.show(rules); // this blocks until OK/Cancel is clicked in the dialog
    Configuration config = ltSupport.getConfig();
    try { //save config - needed for the server
      config.saveConfiguration(langTool.getLanguage());
    } catch (IOException e) {
      Tools.showError(e);
    }
    ltSupport.reloadConfig();
    // Stop server, start new server if requested:
    stopServer();
    maybeStartServer();
  }

  private void showSelectFontDialog() {
    Configuration config = ltSupport.getConfig();
    if (fontChooserDialog == null) {
      fontChooserDialog = new FontChooser(frame, true);
      Tools.centerDialog(fontChooserDialog);
    }
    fontChooserDialog.setSelectedFont(this.textArea.getFont());
    fontChooserDialog.setVisible(true);
    if (fontChooserDialog.getSelectedFont() != null) {
      this.textArea.setFont(fontChooserDialog.getSelectedFont());
      config.setFontName(fontChooserDialog.getSelectedFont().getFamily());
      config.setFontStyle(fontChooserDialog.getSelectedFont().getStyle());
      config.setFontSize(fontChooserDialog.getSelectedFont().getSize());
      try {
        config.saveConfiguration(ltSupport.getLanguage());
      } catch (IOException e) {
        Tools.showError(e);
      }
    }
  }

  private Component getFrame() {
    return frame;
  }

  private void updateTitle() {
    if (currentFile == null) {
      frame.setTitle("LanguageTool " + JLanguageTool.VERSION);
    } else {
      frame.setTitle(currentFile.getName() + " - LanguageTool " + JLanguageTool.VERSION);
    }
  }

  private void createGUI() {
    frame = new JFrame("LanguageTool " + JLanguageTool.VERSION);

    setLookAndFeel();
    openAction = new OpenAction();
    saveAction = new SaveAction();
    saveAsAction = new SaveAsAction();
    checkAction = new CheckAction();
    autoCheckAction = new AutoCheckAction(true);

    frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    frame.addWindowListener(new CloseListener());
    URL iconUrl = JLanguageTool.getDataBroker().getFromResourceDirAsUrl(TRAY_ICON);
    frame.setIconImage(new ImageIcon(iconUrl).getImage());

    textArea = new JTextArea();
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    textArea.addKeyListener(new ControlReturnTextCheckingListener());
    resultArea = new JTextPane();
    undoRedo = new UndoRedoSupport(this.textArea, messages);
    frame.setJMenuBar(createMenuBar());

    GridBagConstraints buttonCons = new GridBagConstraints();

    JPanel insidePanel = new JPanel();
    insidePanel.setOpaque(false);
    insidePanel.setLayout(new GridBagLayout());

    buttonCons.gridx = 0;
    buttonCons.gridy = 0;
    buttonCons.anchor = GridBagConstraints.LINE_START;
    insidePanel.add(new JLabel(messages.getString("textLanguage") + " "), buttonCons);

    languageBox = new LanguageComboBox(messages, EXTERNAL_LANGUAGE_SUFFIX);
    languageBox.setRenderer(new LanguageComboBoxRenderer(messages, EXTERNAL_LANGUAGE_SUFFIX));
    buttonCons.gridx = 1;
    buttonCons.gridy = 0;
    buttonCons.anchor = GridBagConstraints.LINE_START;
    insidePanel.add(languageBox, buttonCons);

    JCheckBox autoDetectBox = new JCheckBox(messages.getString("atd"));
    buttonCons.gridx = 2;
    buttonCons.gridy = 0;
    buttonCons.gridwidth = GridBagConstraints.REMAINDER;
    buttonCons.anchor = GridBagConstraints.LINE_START;
    insidePanel.add(autoDetectBox, buttonCons);

    buttonCons.gridx = 0;
    buttonCons.gridy = 1;
    buttonCons.gridwidth = GridBagConstraints.REMAINDER;
    buttonCons.fill = GridBagConstraints.HORIZONTAL;
    buttonCons.anchor = GridBagConstraints.LINE_END;
    buttonCons.weightx = 1.0;
    insidePanel.add(statusLabel, buttonCons);

    Container contentPane = frame.getContentPane();
    GridBagLayout gridLayout = new GridBagLayout();
    contentPane.setLayout(gridLayout);
    GridBagConstraints cons = new GridBagConstraints();

    cons.gridx = 0;
    cons.gridy = 1;
    cons.fill = GridBagConstraints.HORIZONTAL;
    cons.anchor = GridBagConstraints.FIRST_LINE_START;
    JToolBar toolbar = new JToolBar("Toolbar", JToolBar.HORIZONTAL);
    toolbar.setFloatable(false);
    contentPane.add(toolbar,cons);

    JButton openButton = new JButton(openAction);
    openButton.setHideActionText(true);
    openButton.setFocusable(false);
    toolbar.add(openButton);

    JButton saveButton = new JButton(saveAction);
    saveButton.setHideActionText(true);
    saveButton.setFocusable(false);
    toolbar.add(saveButton);

    JButton saveAsButton = new JButton(saveAsAction);
    saveAsButton.setHideActionText(true);
    saveAsButton.setFocusable(false);
    toolbar.add(saveAsButton);

    JButton spellButton = new JButton(this.checkAction);
    spellButton.setHideActionText(true);
    spellButton.setFocusable(false);
    toolbar.add(spellButton);

    JToggleButton autoSpellButton = new JToggleButton(autoCheckAction);
    autoSpellButton.setHideActionText(true);
    autoSpellButton.setFocusable(false);
    toolbar.add(autoSpellButton);

    JButton clearTextButton = new JButton(new ClearTextAction());
    clearTextButton.setHideActionText(true);
    clearTextButton.setFocusable(false);
    toolbar.add(clearTextButton);

    cons.insets = new Insets(5, 5, 5, 5);
    cons.fill = GridBagConstraints.BOTH;
    cons.weightx = 10.0f;
    cons.weighty = 10.0f;
    cons.gridx = 0;
    cons.gridy = 2;
    cons.weighty = 5.0f;
    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            new JScrollPane(textArea), new JScrollPane(resultArea));
    splitPane.setDividerLocation(200);
    contentPane.add(splitPane, cons);

    cons.fill = GridBagConstraints.HORIZONTAL;
    cons.gridx = 0;
    cons.gridy = 3;
    cons.weightx = 1.0f;
    cons.weighty = 0.0f;
    cons.insets = new Insets(4, 12, 4, 12);
    contentPane.add(insidePanel, cons);

    ltSupport = new LanguageToolSupport(this.frame, this.textArea, this.undoRedo);
    resultAreaHelper = new ResultArea(messages, ltSupport, resultArea);
    languageBox.selectLanguage(ltSupport.getLanguage());
    languageBox.setEnabled(!ltSupport.getConfig().getAutoDetect());
    autoDetectBox.setSelected(ltSupport.getConfig().getAutoDetect());
    taggerShowsDisambigLog = ltSupport.getConfig().getTaggerShowsDisambigLog();

    languageBox.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          // we cannot re-use the existing LT object anymore
          frame.applyComponentOrientation(
            ComponentOrientation.getOrientation(Locale.getDefault()));
          Language lang = (Language) languageBox.getSelectedItem();
          ComponentOrientation componentOrientation =
            ComponentOrientation.getOrientation(lang.getLocale());
          textArea.applyComponentOrientation(componentOrientation);
          resultArea.applyComponentOrientation(componentOrientation);
          ltSupport.setLanguage(lang);
        }
      }
    });
    autoDetectBox.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        boolean selected = e.getStateChange() == ItemEvent.SELECTED;
        languageBox.setEnabled(!selected);
        ltSupport.getConfig().setAutoDetect(selected);
        if (selected) {
          Language detected = ltSupport.autoDetectLanguage(textArea.getText());
          languageBox.selectLanguage(detected);
        }
      }
    });
    ltSupport.addLanguageToolListener(new LanguageToolListener() {
      @Override
      public void languageToolEventOccurred(LanguageToolEvent event) {
        if (event.getType() == LanguageToolEvent.Type.CHECKING_STARTED) {
          String msg = org.languagetool.tools.Tools.i18n(messages, "checkStart");
          statusLabel.setText(msg);
          if (event.getCaller() == getFrame()) {
            setWaitCursor();
            checkAction.setEnabled(false);
          }
        } else if (event.getType() == LanguageToolEvent.Type.CHECKING_FINISHED) {
          if (event.getCaller() == getFrame()) {
            checkAction.setEnabled(true);
            unsetWaitCursor();
          }
          String msg = org.languagetool.tools.Tools.i18n(messages, "checkDone", event.getSource().getMatches().size(), event.getElapsedTime());
          statusLabel.setText(msg);          
        } else if (event.getType() == LanguageToolEvent.Type.LANGUAGE_CHANGED) {
          languageBox.selectLanguage(ltSupport.getLanguage());
        } else if (event.getType() == LanguageToolEvent.Type.RULE_ENABLED) {
            //this will trigger a check and the result will be updated by
            //the CHECKING_FINISHED event
        } else if (event.getType() == LanguageToolEvent.Type.RULE_DISABLED) {
            String msg = org.languagetool.tools.Tools.i18n(messages, "checkDoneNoTime", event.getSource().getMatches().size());
            statusLabel.setText(msg);
        }
      }
    });
    frame.applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
    Language lang = ltSupport.getLanguage();
    ComponentOrientation componentOrientation =
      ComponentOrientation.getOrientation(lang.getLocale());
    textArea.applyComponentOrientation(componentOrientation);
    resultArea.applyComponentOrientation(componentOrientation);

    ResourceBundle textLanguageMessageBundle = JLanguageTool.getMessageBundle(ltSupport.getLanguage());
    textArea.setText(textLanguageMessageBundle.getString("guiDemoText"));

    Configuration config = ltSupport.getConfig();
    if (config.getFontName() != null
            || config.getFontStyle() != Configuration.FONT_STYLE_INVALID
            || config.getFontSize() != Configuration.FONT_SIZE_INVALID) {
      String fontName = config.getFontName();
      if (fontName == null) {
        fontName = textArea.getFont().getFamily();
      }
      int fontSize = config.getFontSize();
      if (fontSize == Configuration.FONT_SIZE_INVALID) {
        fontSize = textArea.getFont().getSize();
      }
      Font font = new Font(fontName, config.getFontStyle(), fontSize);
      textArea.setFont(font);
    }

    frame.pack();
    frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
    frame.setLocationByPlatform(true);
    maybeStartServer();
  }

  private String getLabel(String key) {
    return Tools.getLabel(messages.getString(key));
  }

  private int getMnemonic(String key) {
    return Tools.getMnemonic(messages.getString(key));
  }

  private KeyStroke getMenuKeyStroke(int keyEvent) {
    return KeyStroke.getKeyStroke(keyEvent, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
  }

  private JMenuBar createMenuBar() {
    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu(getLabel("guiMenuFile"));
    fileMenu.setMnemonic(getMnemonic("guiMenuFile"));
    JMenu editMenu = new JMenu(getLabel("guiMenuEdit"));
    editMenu.setMnemonic(getMnemonic("guiMenuEdit"));
    JMenu grammarMenu = new JMenu(getLabel("guiMenuGrammar"));
    grammarMenu.setMnemonic(getMnemonic("guiMenuGrammar"));
    JMenu helpMenu = new JMenu(getLabel("guiMenuHelp"));
    helpMenu.setMnemonic(getMnemonic("guiMenuHelp"));

    fileMenu.add(openAction);
    fileMenu.add(saveAction);
    fileMenu.add(saveAsAction);
    fileMenu.addSeparator();
    fileMenu.add(new HideAction());
    fileMenu.addSeparator();
    fileMenu.add(new QuitAction());

    grammarMenu.add(checkAction);
    JCheckBoxMenuItem item = new JCheckBoxMenuItem(autoCheckAction);
    grammarMenu.add(item);
    grammarMenu.add(new CheckClipboardAction());
    grammarMenu.add(new TagTextAction());
    grammarMenu.add(new AddRulesAction());
    grammarMenu.add(new OptionsAction());
    grammarMenu.add(new SelectFontAction());
    JMenu lafMenu = new JMenu(messages.getString("guiLookAndFeelMenu"));
    UIManager.LookAndFeelInfo[] lafInfo = UIManager.getInstalledLookAndFeels();
    ButtonGroup buttonGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo laf : lafInfo) {
      if (!"Nimbus".equals(laf.getName())) {
        continue;
      }
      addLookAndFeelMenuItem(lafMenu, laf, buttonGroup);
    }
    for (UIManager.LookAndFeelInfo laf : lafInfo) {
      if ("Nimbus".equals(laf.getName())) {
        continue;
      }
      addLookAndFeelMenuItem(lafMenu, laf, buttonGroup);
    }
    grammarMenu.add(lafMenu);

    helpMenu.add(new AboutAction());

    undoRedo.undoAction.putValue(Action.NAME, getLabel("guiMenuUndo"));
    undoRedo.undoAction.putValue(Action.MNEMONIC_KEY, getMnemonic("guiMenuUndo"));
    undoRedo.redoAction.putValue(Action.NAME, getLabel("guiMenuRedo"));
    undoRedo.redoAction.putValue(Action.MNEMONIC_KEY, getMnemonic("guiMenuRedo"));

    editMenu.add(undoRedo.undoAction);
    editMenu.add(undoRedo.redoAction);
    editMenu.addSeparator();

    Action cutAction = new DefaultEditorKit.CutAction();
    cutAction.putValue(Action.SMALL_ICON, getImageIcon("sc_cut.png"));
    cutAction.putValue(Action.LARGE_ICON_KEY, getImageIcon("lc_cut.png"));
    cutAction.putValue(Action.NAME, getLabel("guiMenuCut"));
    cutAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_T);
    editMenu.add(cutAction);

    Action copyAction = new DefaultEditorKit.CopyAction();
    copyAction.putValue(Action.SMALL_ICON, getImageIcon("sc_copy.png"));
    copyAction.putValue(Action.LARGE_ICON_KEY, getImageIcon("lc_copy.png"));
    copyAction.putValue(Action.NAME, getLabel("guiMenuCopy"));
    copyAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
    editMenu.add(copyAction);

    Action pasteAction = new DefaultEditorKit.PasteAction();
    pasteAction.putValue(Action.SMALL_ICON, getImageIcon("sc_paste.png"));
    pasteAction.putValue(Action.LARGE_ICON_KEY, getImageIcon("lc_paste.png"));
    pasteAction.putValue(Action.NAME, getLabel("guiMenuPaste"));
    pasteAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_P);
    editMenu.add(pasteAction);

    editMenu.addSeparator();

    editMenu.add(new SelectAllAction());

    menuBar.add(fileMenu);
    menuBar.add(editMenu);
    menuBar.add(grammarMenu);
    menuBar.add(helpMenu);
    return menuBar;
  }

  private void addLookAndFeelMenuItem(JMenu lafMenu,
        UIManager.LookAndFeelInfo laf, ButtonGroup buttonGroup)
  {
    JRadioButtonMenuItem lfItem = new JRadioButtonMenuItem(new SelectLFAction(laf));
    lafMenu.add(lfItem);
    buttonGroup.add(lfItem);
    if (laf.getName().equals(UIManager.getLookAndFeel().getName())) {
      buttonGroup.setSelected(lfItem.getModel(), true);
    }
  }

  private void setLookAndFeel() {
    String lookAndFeelName = null;
    String className = null;
    try {
      Configuration config = new Configuration(new File(System.getProperty("user.home")), LanguageToolSupport.CONFIG_FILE, null);
      if (config.getLookAndFeelName() != null) {
        lookAndFeelName = config.getLookAndFeelName();
      }
    } catch (IOException ex) {
      // ignore
    }
    if (lookAndFeelName == null) {
      lookAndFeelName = "Nimbus";
      // to use the system's look and feel:
      //lookAndFeelName = UIManager.getSystemLookAndFeelClassName();
    }
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      if (lookAndFeelName.equals(info.getName())) {
        className = info.getClassName();
        break;
      }
    }
    if (className != null) {
      try {
        UIManager.setLookAndFeel(className);
      } catch (Exception ignored) {
        // Well, what can we do...
      }
    }
  }

  private PopupMenu makePopupMenu() {
    PopupMenu popup = new PopupMenu();
    ActionListener rmbListener = new TrayActionRMBListener();
    // Enable or disable embedded HTTP server:
    enableHttpServerItem = new CheckboxMenuItem(Tools.getLabel(messages.getString("tray_menu_enable_server")));
    enableHttpServerItem.setState(httpServer != null && httpServer.isRunning());
    enableHttpServerItem.addItemListener(new TrayActionItemListener());
    popup.add(enableHttpServerItem);
    // Check clipboard text:
    MenuItem checkClipboardItem =
            new MenuItem(Tools.getLabel(messages.getString("guiMenuCheckClipboard")));
    checkClipboardItem.addActionListener(rmbListener);
    popup.add(checkClipboardItem);
    // Open main window:
    MenuItem restoreItem = new MenuItem(Tools.getLabel(messages.getString("guiMenuShowMainWindow")));
    restoreItem.addActionListener(rmbListener);
    popup.add(restoreItem);
    // Exit:
    MenuItem exitItem = new MenuItem(Tools.getLabel(messages.getString("guiMenuQuit")));
    exitItem.addActionListener(rmbListener);
    popup.add(exitItem);
    return popup;
  }

  private void checkClipboardText() {
    String s = getClipboardText();
    textArea.setText(s);
  }

  private void hideToTray() {
    if (!isInTray) {
      SystemTray tray = SystemTray.getSystemTray();
      String iconPath = tray.getTrayIconSize().height > 16 ? TRAY_ICON : TRAY_SMALL_ICON;
      URL iconUrl = JLanguageTool.getDataBroker().getFromResourceDirAsUrl(iconPath);
      Image img = Toolkit.getDefaultToolkit().getImage(iconUrl);
      PopupMenu popup = makePopupMenu();
      try {
        trayIcon = new TrayIcon(img, TRAY_TOOLTIP, popup);
        trayIcon.addMouseListener(new TrayActionListener());
        setTrayIcon();
        tray.add(trayIcon);
      } catch (AWTException e1) {
        Tools.showError(e1);
      }
    }
    isInTray = true;
    frame.setVisible(false);
  }

  private void tagText() {
    if (StringTools.isEmpty(textArea.getText().trim())) {
      textArea.setText(messages.getString("enterText2"));
      return;
    }
    setWaitCursor();
    new Thread() {
      @Override
      public void run() {
        try {
          tagTextAndDisplayResults();
        } finally {
          SwingUtilities.invokeLater(() -> unsetWaitCursor());
        }
      }
    }.start();
  }

  private void quitOrHide() {
    if (closeHidesToTray) {
      hideToTray();
    } else {
      quit();
    }
  }

  private void quit() {
    stopServer();
    try {
      Configuration config = ltSupport.getConfig();
      config.setLanguage(ltSupport.getLanguage());
      config.saveConfiguration(ltSupport.getLanguage());
    } catch (IOException e) {
      Tools.showError(e);
    }
    frame.setVisible(false);
    JLanguageTool.removeTemporaryFiles();
    System.exit(0);
  }

  private void setTrayIcon() {
    if (trayIcon != null) {
      SystemTray tray = SystemTray.getSystemTray();
      boolean httpServerRunning = httpServer != null && httpServer.isRunning();
      boolean smallTray = tray.getTrayIconSize().height <= 16;
      String iconPath;
      if (httpServerRunning) {
        trayIcon.setToolTip(messages.getString("tray_tooltip_server_running"));
        iconPath = smallTray ? TRAY_SMALL_SERVER_ICON : TRAY_SERVER_ICON;
      } else {
        trayIcon.setToolTip(TRAY_TOOLTIP);
        iconPath = smallTray ? TRAY_SMALL_ICON : TRAY_ICON;
      }
      URL iconUrl = JLanguageTool.getDataBroker().getFromResourceDirAsUrl(iconPath);
      Image img = Toolkit.getDefaultToolkit().getImage(iconUrl);
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
    String s = getClipboardText();
    restoreFromTray();
    textArea.setText(s);
  }

  private String getClipboardText() {
    // get text from clipboard or selection:
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemSelection();
    if (clipboard == null) { // on Windows
      clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    }
    String s;
    Transferable data = clipboard.getContents(this);
    try {
      if (data != null
          && data.isDataFlavorSupported(DataFlavor.getTextPlainUnicodeFlavor())) {
        DataFlavor df = DataFlavor.getTextPlainUnicodeFlavor();
        try (Reader sr = df.getReaderForText(data)) {
          s = StringTools.readerToString(sr);
        }
      } else {
        s = "";
      }
    } catch (Exception ex) {
      s = data.toString();
    }
    return s;
  }

  private boolean maybeStartServer() {
    Configuration config = ltSupport.getConfig();
    if (config.getRunServer()) {
      try {
        HTTPServerConfig serverConfig = new HTTPServerConfig(config.getServerPort(), false);
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

  private void checkTextAndDisplayResults() {
    if (StringTools.isEmpty(textArea.getText().trim())) {
      textArea.setText(messages.getString("enterText2"));
      return;
    }
    ltSupport.checkImmediately(getFrame());
  }

  private String getStackTraceAsHtml(Exception e) {
    return "<br><br><b><font color=\"red\">"
         + org.languagetool.tools.Tools.getFullStackTrace(e).replace("\n", "<br/>")
         + "</font></b><br>";
  }

  private void setWaitCursor() {
    frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    // For some reason we also have to set the cursor here so it also shows
    // when user starts checking text with Ctrl+Return:
    textArea.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    resultArea.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
  }

  private void unsetWaitCursor() {
    frame.setCursor(Cursor.getDefaultCursor());
    textArea.setCursor(Cursor.getDefaultCursor());
    resultArea.setCursor(Cursor.getDefaultCursor());
  }

  private boolean appendTagsWithDisambigLog(StringBuilder sb,
          AnalyzedSentence sentence, boolean odd) {

    for (AnalyzedTokenReadings t : sentence.getTokens()) {
      if (t.isWhitespace() && !t.isSentenceStart()) {
        continue;
      }
      odd = !odd;
      sb.append("<tr>");
      sb.append("<td bgcolor=\"");
      if(odd) {
          sb.append("#ffffff");
      }
      else {
          sb.append("#f1f1f1");
      }
      sb.append("\">");
      if(!t.isWhitespace()) {
        sb.append(t.getToken());
        sb.append("<font color='");
        sb.append(TAG_COLOR);
        sb.append("'>[");
      }
      Iterator<AnalyzedToken> iterator = t.iterator();
      while (iterator.hasNext()) {
        AnalyzedToken token = iterator.next();
        String posTag = token.getPOSTag();
        if (t.isSentenceStart()) {
          sb.append(StringTools.escapeHTML("<S>"));
        } else if (JLanguageTool.SENTENCE_END_TAGNAME.equals(posTag)) {
          sb.append(StringTools.escapeHTML("</S>"));
        } else if (JLanguageTool.PARAGRAPH_END_TAGNAME.equals(posTag)) {
          sb.append(StringTools.escapeHTML("<P/>"));
        } else {
          if (!t.isWhitespace()) {
            sb.append(token);
            if (iterator.hasNext()) {
              sb.append(", ");
            }
          }
        }
      }
      if (!t.isWhitespace()) {
        if (t.getChunkTags().size() > 0) {
          sb.append(',');
          sb.append(StringUtils.join(t.getChunkTags(), "|"));
        }
        if (t.isImmunized()) {
          sb.append("{!}");
        }
        sb.append("]</font>");
      } else {
        sb.append(' ');
      }
      sb.append("</td>");
      sb.append("<td bgcolor=\"");
      if(odd) {
          sb.append("#ffffff");
      }
      else {
          sb.append("#f1f1f1");
      }
      sb.append("\">");
      if (!"".equals(t.getHistoricalAnnotations())) {
        sb.append(StringTools.escapeHTML(t.getHistoricalAnnotations())
                .trim().replace("\n", "<br>"));
      }
      sb.append("</td>");
      sb.append("</tr>");
    }
    return odd;
  }

  private void tagTextAndDisplayResults() {
    JLanguageTool langTool = ltSupport.getLanguageTool();
    // tag text
    List<String> sentences = langTool.sentenceTokenize(textArea.getText());
    StringBuilder sb = new StringBuilder();
    if(taggerShowsDisambigLog) {
      sb.append("<table>");
      sb.append("<tr>");
      sb.append("<td><b>");
      sb.append(messages.getString("token"));
      sb.append("</b></td>");
      sb.append("<td><b>");
      sb.append(messages.getString("disambiguatorLog"));
      sb.append("</b></td>");
      sb.append("</tr>");
      boolean odd = true;
      try {
        for (String sent : sentences) {
          AnalyzedSentence analyzed = langTool.getAnalyzedSentence(sent);
          odd = appendTagsWithDisambigLog(sb, analyzed, odd);
        }
      } catch (Exception e) {
        sb.append(getStackTraceAsHtml(e));
      }
      sb.append("</table>");
    } else {
      try {
        for (String sent : sentences) {
          AnalyzedSentence analyzed = langTool.getAnalyzedSentence(sent);
          String analyzedString = StringTools.escapeHTML(analyzed.toString(",")).
                replace("&lt;S&gt;", "&lt;S&gt;<br>").
                replace("[", "<font color='" + TAG_COLOR + "'>[").
                replace("]", "]</font><br>");
          sb.append(analyzedString).append('\n');
        }
      } catch (Exception e) {
        sb.append(getStackTraceAsHtml(e));
      }
    }
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        if (taggerDialog == null) {
          taggerDialog = new JDialog(frame);
          taggerDialog.setTitle(messages.getString("taggerWindowTitle"));
          taggerDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
          taggerDialog.setResizable(true);
          taggerDialog.setSize(640, 480);
          taggerDialog.setLocationRelativeTo(frame);
          KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
          ActionListener actionListener = actionEvent -> taggerDialog.setVisible(false);
          taggerDialog.getRootPane().registerKeyboardAction(actionListener,
                  stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
          JPanel panel = new JPanel(new GridBagLayout());
          taggerDialog.add(panel);
          taggerArea = new JTextPane();
          taggerArea.setContentType("text/html");
          taggerArea.setEditable(false);
          GridBagConstraints c = new GridBagConstraints();
          c.gridx = 0;
          c.gridwidth = 2;
          c.gridy = 0;
          c.weightx = 1.0;
          c.weighty = 1.0;
          c.insets = new Insets(8,8,4,8);
          c.fill = GridBagConstraints.BOTH;
          panel.add(new JScrollPane(taggerArea),c);
          c.gridwidth = 1;
          c.gridx = 0;
          c.gridy = 1;
          c.weightx = 0.0;
          c.weighty = 0.0;
          c.insets = new Insets(4,8,8,8);
          c.fill = GridBagConstraints.NONE;
          c.anchor = GridBagConstraints.EAST;
          JCheckBox showDisAmbig =
                  new JCheckBox(messages.getString("ShowDisambiguatorLog"));
          showDisAmbig.setSelected(taggerShowsDisambigLog);
          showDisAmbig.addItemListener((ItemEvent e) -> {
              taggerShowsDisambigLog = e.getStateChange() == ItemEvent.SELECTED;
              ltSupport.getConfig().setTaggerShowsDisambigLog(taggerShowsDisambigLog);
          });
          panel.add(showDisAmbig,c);
          c.gridx = 1;
          JButton closeButton = new JButton(
                  messages.getString("guiCloseButton"));
          closeButton.addActionListener(actionListener);
          panel.add(closeButton,c);
        }
        // orientation each time should be set as language may is changed
        taggerDialog.applyComponentOrientation(ComponentOrientation.getOrientation(
          ((Language) languageBox.getSelectedItem()).getLocale()));

        taggerDialog.setVisible(true);
        taggerArea.setText(HTML_FONT_START + sb + HTML_FONT_END);
      }
    });
  }

  private void setTrayMode(boolean trayMode) {
    this.closeHidesToTray = trayMode;
  }

  public static void main(String[] args) {
    if (System.getSecurityManager() == null) {
      JnaTools.setBugWorkaroundProperty();
    }
    Main prg = new Main();
    if (args.length == 1 && (args[0].equals("-t") || args[0].equals("--tray"))) {
      // dock to systray on startup
      SwingUtilities.invokeLater(new Runnable() {
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
    } else if (args.length == 1 && (args[0].equals("-h") || args[0].equals("--help"))) {
      printUsage();
    } else if (args.length == 0 || args.length == 1) {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          try {
            prg.createGUI();
            prg.showGUI();
            if (args.length == 1) {
              prg.loadFile(new File(args[0]));
            }
          } catch (Exception e) {
            Tools.showError(e);
          }
        }
      });
    } else {
      printUsage();
    }
  }

  private static void printUsage() {
    System.out.println("Usage: java org.languagetool.gui.Main [-t|--tray]");
    System.out.println("    or java org.languagetool.gui.Main [file]");
    System.out.println("Parameters:");
    System.out.println("    -t, --tray: dock LanguageTool to system tray on startup");
    System.out.println("    file:       a plain text file to load on startup");
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
        Configuration config = ltSupport.getConfig();
        if (e.getStateChange() == ItemEvent.SELECTED) {
          config.setRunServer(true);
          boolean serverStarted = maybeStartServer();
          enableHttpServerItem.setState(serverStarted);
          config.setRunServer(serverStarted);
          config.saveConfiguration(ltSupport.getLanguage());
        } else if (e.getStateChange() == ItemEvent.DESELECTED) {
          config.setRunServer(false);
          config.saveConfiguration(ltSupport.getLanguage());
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
      return e.getActionCommand().equalsIgnoreCase(Tools.getLabel(messages.getString(label)));
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
    public boolean accept(File f) {
      boolean isTextFile = f.getName().toLowerCase().endsWith(".txt");
      return isTextFile || f.isDirectory();
    }

    @Override
    public String getDescription() {
      return "*.txt";
    }

  }

  class OpenAction extends AbstractAction {

    OpenAction() {
      super(getLabel("guiMenuOpen"));
      putValue(Action.SHORT_DESCRIPTION, messages.getString("guiMenuOpenShortDesc"));
      putValue(Action.LONG_DESCRIPTION, messages.getString("guiMenuOpenLongDesc"));
      putValue(Action.MNEMONIC_KEY, getMnemonic("guiMenuOpen"));
      putValue(Action.ACCELERATOR_KEY, getMenuKeyStroke(KeyEvent.VK_O));
      putValue(Action.SMALL_ICON, getImageIcon("sc_open.png"));
      putValue(Action.LARGE_ICON_KEY, getImageIcon("lc_open.png"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      loadFile();
    }
  }

  class SaveAction extends AbstractAction {

    SaveAction() {
      super(getLabel("guiMenuSave"));
      putValue(Action.SHORT_DESCRIPTION, messages.getString("guiMenuSaveShortDesc"));
      putValue(Action.LONG_DESCRIPTION, messages.getString("guiMenuSaveLongDesc"));
      putValue(Action.MNEMONIC_KEY, getMnemonic("guiMenuSave"));
      putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
      putValue(Action.SMALL_ICON, getImageIcon("sc_save.png"));
      putValue(Action.LARGE_ICON_KEY, getImageIcon("lc_save.png"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      saveFile(false);
    }
  }

  class SaveAsAction extends AbstractAction {

    SaveAsAction() {
      super(getLabel("guiMenuSaveAs"));
      putValue(Action.SHORT_DESCRIPTION, messages.getString("guiMenuSaveAsShortDesc"));
      putValue(Action.LONG_DESCRIPTION, messages.getString("guiMenuSaveAsLongDesc"));
      putValue(Action.MNEMONIC_KEY, getMnemonic("guiMenuSaveAs"));
      putValue(Action.SMALL_ICON, getImageIcon("sc_saveas.png"));
      putValue(Action.LARGE_ICON_KEY, getImageIcon("lc_saveas.png"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      saveFile(true);
    }
  }

  class CheckClipboardAction extends AbstractAction {

    CheckClipboardAction() {
      super(getLabel("guiMenuCheckClipboard"));
      putValue(Action.MNEMONIC_KEY, getMnemonic("guiMenuCheckClipboard"));
      putValue(Action.ACCELERATOR_KEY, getMenuKeyStroke(KeyEvent.VK_Y));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      checkClipboardText();
    }
  }

  class TagTextAction extends AbstractAction {

    TagTextAction() {
      super(getLabel("guiTagText"));
      putValue(Action.MNEMONIC_KEY, getMnemonic("guiTagText"));
      putValue(Action.ACCELERATOR_KEY, getMenuKeyStroke(KeyEvent.VK_T));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      tagText();
    }
  }

  class AddRulesAction extends AbstractAction {

    AddRulesAction() {
      super(getLabel("guiMenuAddRules"));
      putValue(Action.MNEMONIC_KEY, getMnemonic("guiMenuAddRules"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      try {
        addLanguage();
      } catch (Exception ex) {
        Tools.showError(ex);
      }
    }
  }

  class OptionsAction extends AbstractAction {

    OptionsAction() {
      super(getLabel("guiMenuOptions"));
      putValue(Action.MNEMONIC_KEY, getMnemonic("guiMenuOptions"));
      putValue(Action.ACCELERATOR_KEY, getMenuKeyStroke(KeyEvent.VK_S));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      showOptions();
    }
  }

  class SelectFontAction extends AbstractAction {

    SelectFontAction() {
      super(getLabel("guiSelectFont"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      showSelectFontDialog();
    }
  }

  class SelectLFAction extends AbstractAction {

    private final UIManager.LookAndFeelInfo lf;

    SelectLFAction(UIManager.LookAndFeelInfo lf) {
      super(lf.getName());
      this.lf = lf;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      try {
        UIManager.setLookAndFeel(lf.getClassName());
        SwingUtilities.updateComponentTreeUI(frame);
        frame.pack();
        ltSupport.getConfig().setLookAndFeelName(lf.getName());
      } catch (ClassNotFoundException | InstantiationException
              | IllegalAccessException | UnsupportedLookAndFeelException ex) {
        Tools.showError(ex);
      }
    }
  }

  class HideAction extends AbstractAction {

    HideAction() {
      super(getLabel("guiMenuHide"));
      putValue(Action.MNEMONIC_KEY, getMnemonic("guiMenuHide"));
      putValue(Action.ACCELERATOR_KEY, getMenuKeyStroke(KeyEvent.VK_D));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      hideToTray();
    }
  }

  class QuitAction extends AbstractAction {

    QuitAction() {
      super(getLabel("guiMenuQuit"));
      putValue(Action.MNEMONIC_KEY, getMnemonic("guiMenuQuit"));
      putValue(Action.ACCELERATOR_KEY, getMenuKeyStroke(KeyEvent.VK_Q));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      quit();
    }
  }

  class AboutAction extends AbstractAction {

    AboutAction() {
      super(getLabel("guiMenuAbout"));
      putValue(Action.MNEMONIC_KEY, getMnemonic("guiMenuAbout"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      AboutDialog about = new AboutDialog(messages, getFrame());
      about.show();
    }
  }

  class CheckAction extends AbstractAction {

    CheckAction() {
      super(getLabel("checkText"));
      putValue(Action.SHORT_DESCRIPTION, messages.getString("checkTextShortDesc"));
      putValue(Action.LONG_DESCRIPTION, messages.getString("checkTextLongDesc"));
      putValue(Action.MNEMONIC_KEY, getMnemonic("checkText"));
      putValue(Action.SMALL_ICON, getImageIcon("sc_spelldialog.png"));
      putValue(Action.LARGE_ICON_KEY, getImageIcon("lc_spelldialog.png"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      checkTextAndDisplayResults();
    }
  }

  class AutoCheckAction extends AbstractAction {

    private boolean enable;

    AutoCheckAction(boolean initial) {
      super(getLabel("autoCheckText"));
      putValue(Action.SHORT_DESCRIPTION, messages.getString("autoCheckTextShortDesc"));
      putValue(Action.LONG_DESCRIPTION, messages.getString("autoCheckTextLongDesc"));
      putValue(Action.MNEMONIC_KEY, getMnemonic("autoCheckText"));
      putValue(Action.SMALL_ICON, getImageIcon("sc_spellonline.png"));
      putValue(Action.LARGE_ICON_KEY, getImageIcon("lc_spellonline.png"));
      enable = initial;
      putValue(Action.SELECTED_KEY, enable);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      enable = !enable;
      putValue(Action.SELECTED_KEY, enable);
      ltSupport.setBackgroundCheckEnabled(enable);
    }
  }

  class ClearTextAction extends AbstractAction {

    ClearTextAction() {
      super(getLabel("clearText"));
      putValue(Action.SHORT_DESCRIPTION, messages.getString("clearText"));
      putValue(Action.SMALL_ICON, getImageIcon("sc_closedoc.png"));
      putValue(Action.LARGE_ICON_KEY, getImageIcon("lc_closedoc.png"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      ltSupport.getTextComponent().setText("");
    }
  }

  private class SelectAllAction extends TextAction {

    private SelectAllAction() {
      super(getLabel("guiMenuSelectAll"));
      putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      JTextComponent component = getFocusedComponent();
      component.selectAll();
    }
  }

  private ImageIcon getImageIcon(String filename) {
    Image image = Toolkit.getDefaultToolkit().getImage(
            JLanguageTool.getDataBroker().getFromResourceDirAsUrl(filename));
    return new ImageIcon(image);
  }

}
