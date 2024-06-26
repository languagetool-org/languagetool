/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.openoffice.aisupport;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ToolTipManager;

import org.languagetool.gui.Configuration;
import org.languagetool.openoffice.DocumentCache;
import org.languagetool.openoffice.MessageHandler;
import org.languagetool.openoffice.MultiDocumentsHandler;
import org.languagetool.openoffice.OfficeTools;
import org.languagetool.openoffice.SingleDocument;
import org.languagetool.openoffice.ViewCursorTools;
import org.languagetool.openoffice.DocumentCache.TextParagraph;
import org.languagetool.openoffice.MultiDocumentsHandler.WaitDialogThread;
import org.languagetool.openoffice.OfficeTools.DocumentType;

import com.sun.star.lang.Locale;
import com.sun.star.lang.XComponent;

/**
 * Dialog to change paragraphs by AI
 * @since 6.5
 * @author Fred Kruse
 */
public class AiDialog extends Thread implements ActionListener {
  
  private final static String AI_INSTRUCTION_FILE_NAME = "LT_AI_Instructions.dat";
  private final static int MAX_INSTRUCTIONS = 40;
  private final static int dialogWidth = 640;
  private final static int dialogHeight = 600;

  private boolean debugMode = true;
  private boolean debugModeTm = true;
  
  private final ResourceBundle messages;
  private final JDialog dialog;
  private final Container contentPane;
  private final JLabel instructionLabel;
  private final JComboBox<String> instruction;
  private final JLabel paragraphLabel;
  private final JTextPane paragraph;
  private final JLabel resultLabel;
  private final JTextPane result;
  private final JButton execute; 
  private final JButton copyResult; 
  private final JButton reset; 
  private final JButton clear; 
  private final JButton undo;
  private final JButton overrideParagraph; 
  private final JButton addToParagraph; 
  private final JButton help; 
  private final JButton close;
  private JProgressBar checkProgress;
  private final Image ltImage;
  
  private SingleDocument currentDocument;
  private MultiDocumentsHandler documents;
  private ViewCursorTools viewCursor;
  private Configuration config;
  
  private int dialogX = -1;
  private int dialogY = -1;
  private String docId = null;
  private List<String> instructionList = new ArrayList<>();
  private String saveText;
  private String saveResult;
  private String paraText;
  private String resultText;
  private Locale locale;
  private boolean atWork;
  private boolean focusLost;

  /**
   * the constructor of the class creates all elements of the dialog
   */
  public AiDialog(SingleDocument document, WaitDialogThread inf, ResourceBundle messages) {
    this.messages = messages;
    documents = document.getMultiDocumentsHandler();
    config = documents.getConfiguration();
    long startTime = 0;
    if (debugModeTm) {
      startTime = System.currentTimeMillis();
    }
    ltImage = OfficeTools.getLtImage();
    if (!documents.isJavaLookAndFeelSet()) {
      documents.setJavaLookAndFeel();
    }
    
    currentDocument = document;
    docId = document.getDocID();
    
    dialog = new JDialog();
    contentPane = dialog.getContentPane();
    instructionLabel = new JLabel(messages.getString("loAiDialogInstructionLabel"));
    instruction = new JComboBox<String>();
    paragraphLabel = new JLabel(messages.getString("loAiDialogParagraphLabel"));
    paragraph = new JTextPane();
    resultLabel = new JLabel(messages.getString("loAiDialogResultLabel"));
    result = new JTextPane();
    execute = new JButton (messages.getString("loAiDialogExecuteButton")); 
    copyResult = new JButton (messages.getString("loAiDialogcopyResultButton")); 
    reset = new JButton (messages.getString("loAiDialogResetButton")); 
    clear = new JButton (messages.getString("loAiDialogClearButton")); 
    undo = new JButton (messages.getString("loAiDialogUndoButton")); 
    overrideParagraph = new JButton (messages.getString("loAiDialogOverrideButton")); 
    addToParagraph = new JButton (messages.getString("loAiDialogaddToButton")); 
    help = new JButton (messages.getString("loAiDialogHelpButton")); 
    close = new JButton (messages.getString("loAiDialogCloseButton")); 
    
    checkProgress = new JProgressBar(0, 100);

    try {
      if (debugMode) {
        MessageHandler.printToLogFile("CheckDialog: LtCheckDialog: LtCheckDialog called");
      }

      
      if (dialog == null) {
        MessageHandler.printToLogFile("CheckDialog: LtCheckDialog: LtCheckDialog == null");
      }
      String dialogName = messages.getString("loAiDialogTitle");
      dialog.setName(dialogName);
      dialog.setTitle(dialogName + " (LanguageTool " + OfficeTools.getLtInformation() + ")");
      dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      ((Frame) dialog.getOwner()).setIconImage(ltImage);

      Font dialogFont = instructionLabel.getFont();
      instructionLabel.setFont(dialogFont);

      instruction.setFont(dialogFont);
      instruction.setEditable(true);
      instructionList = readInstructions();
      for (String instr : instructionList) {
        instruction.addItem(instr);
      }
/*
      instruction.addItemListener(e -> {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          String selectedLang = (String) language.getSelectedItem();
          if (!lastLang.equals(selectedLang)) {
            changeLanguage.setEnabled(true);
          }
        }
      });
*/
      if (debugModeTm) {
        long runTime = System.currentTimeMillis() - startTime;
//        if (runTime > OfficeTools.TIME_TOLERANCE) {
          MessageHandler.printToLogFile("CheckDialog: Time to initialise Languages: " + runTime);
//        }
          startTime = System.currentTimeMillis();
      }
      if (inf.canceled()) {
        return;
      }

      paragraphLabel.setFont(dialogFont);
      paragraph.setFont(dialogFont);
      JScrollPane paragraphPane = new JScrollPane(paragraph);
      paragraphPane.setMinimumSize(new Dimension(0, 30));

      resultLabel.setFont(dialogFont);
      result.setFont(dialogFont);
/*      
      result.getDocument().addDocumentListener(new DocumentListener() {
        @Override
        public void changedUpdate(DocumentEvent e) {
          if (!blockSentenceError) {
            if (!change.isEnabled()) {
              change.setEnabled(true);
            }
            if (changeAll.isEnabled()) {
              changeAll.setEnabled(false);
            }
            if (autoCorrect.isEnabled()) {
              autoCorrect.setEnabled(false);
            }
          }
        }
        @Override
        public void insertUpdate(DocumentEvent e) {
          changedUpdate(e);
        }
        @Override
        public void removeUpdate(DocumentEvent e) {
          changedUpdate(e);
        }
      });
*/
      JScrollPane resultPane = new JScrollPane(result);
      resultPane.setMinimumSize(new Dimension(0, 30));
      
      if (debugModeTm) {
        long runTime = System.currentTimeMillis() - startTime;
//        if (runTime > OfficeTools.TIME_TOLERANCE) {
          MessageHandler.printToLogFile("CheckDialog: Time to initialise suggestions, etc.: " + runTime);
//        }
          startTime = System.currentTimeMillis();
      }
      if (inf.canceled()) {
        return;
      }
      
      execute.setFont(dialogFont);
      execute.addActionListener(this);
      execute.setActionCommand("execute");
      
      copyResult.setFont(dialogFont);
      copyResult.addActionListener(this);
      copyResult.setActionCommand("copyResult");
      
      reset.setFont(dialogFont);
      reset.addActionListener(this);
      reset.setActionCommand("reset");
      
      clear.setFont(dialogFont);
      clear.addActionListener(this);
      clear.setActionCommand("clear");
      
      undo.setFont(dialogFont);
      undo.addActionListener(this);
      undo.setActionCommand("undo");
      
      overrideParagraph.setFont(dialogFont);
      overrideParagraph.addActionListener(this);
      overrideParagraph.setActionCommand("overrideParagraph");
      
      addToParagraph.setFont(dialogFont);
      addToParagraph.addActionListener(this);
      addToParagraph.setActionCommand("addToParagraph");
      
      help.setFont(dialogFont);
      help.addActionListener(this);
      help.setActionCommand("help");
      
      close.setFont(dialogFont);
      close.addActionListener(this);
      close.setActionCommand("close");

      dialog.addWindowFocusListener(new WindowFocusListener() {
        @Override
        public void windowGainedFocus(WindowEvent e) {
          if (focusLost) {
            try {
              Point p = dialog.getLocation();
              dialogX = p.x;
              dialogY = p.y;
              if (debugMode) {
                MessageHandler.printToLogFile("CheckDialog: LtCheckDialog: Window Focus gained: Event = " + e.paramString());
              }
              setAtWorkButtonState(atWork);
              currentDocument = getCurrentDocument();
              if (currentDocument == null) {
                closeDialog();
                return;
              }
              if (debugMode) {
                MessageHandler.printToLogFile("CheckDialog: LtCheckDialog: Window Focus gained: new docType = " + currentDocument.getDocumentType());
              }
              setText();
              focusLost = false;
            } catch (Throwable t) {
              MessageHandler.showError(t);
              closeDialog();
            }
          }
        }
        @Override
        public void windowLostFocus(WindowEvent e) {
          try {
            if (debugMode) {
              MessageHandler.printToLogFile("CheckDialog: LtCheckDialog: Window Focus lost: Event = " + e.paramString());
            }
            setAtWorkButtonState(atWork);
            dialog.setEnabled(true);
            focusLost = true;
          } catch (Throwable t) {
            MessageHandler.showError(t);
            closeDialog();
          }
        }
      });

      dialog.addWindowListener(new WindowListener() {
        @Override
        public void windowOpened(WindowEvent e) {
        }
        @Override
        public void windowClosing(WindowEvent e) {
          closeDialog();
        }
        @Override
        public void windowClosed(WindowEvent e) {
        }
        @Override
        public void windowIconified(WindowEvent e) {
        }
        @Override
        public void windowDeiconified(WindowEvent e) {
        }
        @Override
        public void windowActivated(WindowEvent e) {
        }
        @Override
        public void windowDeactivated(WindowEvent e) {
        }
      });
      
      checkProgress.setStringPainted(true);
      checkProgress.setIndeterminate(false);
/*
      //  set selection background color to get compatible layout to LO
      Color selectionColor = UIManager.getLookAndFeelDefaults().getColor("ProgressBar.selectionBackground");
      suggestions.setSelectionBackground(selectionColor);
      setJComboSelectionBackground(language, selectionColor);
      setJComboSelectionBackground(changeLanguage, selectionColor);
      setJComboSelectionBackground(addToDictionary, selectionColor);
      setJComboSelectionBackground(activateRule, selectionColor);
*/
      if (debugModeTm) {
        long runTime = System.currentTimeMillis() - startTime;
//        if (runTime > OfficeTools.TIME_TOLERANCE) {
          MessageHandler.printToLogFile("CheckDialog: Time to initialise Buttons: " + runTime);
//        }
          startTime = System.currentTimeMillis();
      }
      if (inf.canceled()) {
        return;
      }
      
      //  Define panels

      //  Define 1. right panel
      JPanel rightPanel1 = new JPanel();
      rightPanel1.setLayout(new GridBagLayout());
      GridBagConstraints cons21 = new GridBagConstraints();
      cons21.insets = new Insets(2, 0, 2, 0);
      cons21.gridx = 0;
      cons21.gridy = 0;
      cons21.anchor = GridBagConstraints.NORTHWEST;
      cons21.fill = GridBagConstraints.BOTH;
      cons21.weightx = 1.0f;
      cons21.weighty = 0.0f;
      cons21.gridy++;
      rightPanel1.add(copyResult, cons21);
      cons21.gridy++;
      rightPanel1.add(reset, cons21);
      cons21.gridy++;
      rightPanel1.add(clear, cons21);

      //  Define 2. right panel
      JPanel rightPanel2 = new JPanel();
      rightPanel2.setLayout(new GridBagLayout());
      GridBagConstraints cons22 = new GridBagConstraints();
      cons22.insets = new Insets(2, 0, 2, 0);
      cons22.gridx = 0;
      cons22.gridy = 0;
      cons22.anchor = GridBagConstraints.NORTHWEST;
      cons22.fill = GridBagConstraints.BOTH;
      cons22.weightx = 1.0f;
      cons22.weighty = 0.0f;
      cons22.gridy++;
      cons22.gridy++;
      rightPanel2.add(undo, cons22);
      cons22.gridy++;
      rightPanel2.add(addToParagraph, cons22);
      cons22.gridy++;
      rightPanel2.add(overrideParagraph, cons22);
      
      //  Define main panel
      JPanel mainPanel = new JPanel();
      mainPanel.setLayout(new GridBagLayout());
      GridBagConstraints cons1 = new GridBagConstraints();
      cons1.insets = new Insets(4, 4, 4, 4);
      cons1.gridx = 0;
      cons1.gridy = 0;
      cons1.anchor = GridBagConstraints.NORTHWEST;
      cons1.fill = GridBagConstraints.BOTH;
      cons1.weightx = 1.0f;
      cons1.weighty = 0.0f;
      mainPanel.add(instructionLabel, cons1);
      cons1.gridy++;
      mainPanel.add(instruction, cons1);
      cons1.weightx = 0.0f;
      cons1.gridx++;
      mainPanel.add(execute, cons1);
      cons1.gridx = 0;
      cons1.gridy++;
      cons1.weightx = 1.0f;
      cons1.weighty = 0.0f;
      mainPanel.add(paragraphLabel, cons1);
      cons1.gridy++;
      cons1.weighty = 2.0f;
      mainPanel.add(paragraphPane, cons1);
      cons1.gridx++;
      cons1.weightx = 0.0f;
      cons1.weighty = 0.0f;
      mainPanel.add(rightPanel1, cons1);
      cons1.gridx = 0;
      cons1.gridy++;
      cons1.weightx = 1.0f;
      mainPanel.add(resultLabel, cons1);
      cons1.gridy++;
      cons1.weighty = 2.0f;
      mainPanel.add(resultPane, cons1);
      cons1.gridx++;
      cons1.weightx = 0.0f;
      cons1.weighty = 0.0f;
      mainPanel.add(rightPanel2, cons1);

      //  Define general button panel
      JPanel generalButtonPanel = new JPanel();
      generalButtonPanel.setLayout(new GridBagLayout());
      GridBagConstraints cons3 = new GridBagConstraints();
      cons3.insets = new Insets(4, 4, 4, 4);
      cons3.gridx = 0;
      cons3.gridy = 0;
      cons3.anchor = GridBagConstraints.SOUTHEAST;
      cons3.fill = GridBagConstraints.HORIZONTAL;
      cons3.weightx = 1.0f;
      cons3.weighty = 0.0f;
      generalButtonPanel.add(help, cons3);
      cons3.gridx++;
      generalButtonPanel.add(close, cons3);
      
      //  Define check progress panel
      JPanel checkProgressPanel = new JPanel();
      checkProgressPanel.setLayout(new GridBagLayout());
      GridBagConstraints cons4 = new GridBagConstraints();
      cons4.insets = new Insets(4, 4, 4, 4);
      cons4.gridx = 0;
      cons4.gridy = 0;
      cons4.anchor = GridBagConstraints.NORTHWEST;
      cons4.fill = GridBagConstraints.HORIZONTAL;
      cons4.weightx = 4.0f;
      cons4.weighty = 0.0f;
      checkProgressPanel.add(checkProgress, cons4);

      contentPane.setLayout(new GridBagLayout());
      GridBagConstraints cons = new GridBagConstraints();
      cons.insets = new Insets(8, 8, 8, 8);
      cons.gridx = 0;
      cons.gridy = 0;
      cons.anchor = GridBagConstraints.NORTHWEST;
      cons.fill = GridBagConstraints.BOTH;
      cons.weightx = 1.0f;
      cons.weighty = 1.0f;
      contentPane.add(mainPanel, cons);
      cons.gridy++;
      cons.weighty = 0.0f;
      contentPane.add(generalButtonPanel, cons);
      cons.gridy++;
      contentPane.add(checkProgressPanel, cons);

      if (debugModeTm) {
        long runTime = System.currentTimeMillis() - startTime;
//        if (runTime > OfficeTools.TIME_TOLERANCE) {
          MessageHandler.printToLogFile("CheckDialog: Time to initialise panels: " + runTime);
//        }
          startTime = System.currentTimeMillis();
      }
      if (inf.canceled()) {
        return;
      }

      dialog.pack();
      // center on screen:
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension frameSize = new Dimension(dialogWidth, dialogHeight);
      dialog.setSize(frameSize);
      dialog.setLocation(screenSize.width / 2 - frameSize.width / 2,
          screenSize.height / 2 - frameSize.height / 2);
      dialog.setLocationByPlatform(true);
      
      ToolTipManager.sharedInstance().setDismissDelay(30000);
      if (debugModeTm) {
        long runTime = System.currentTimeMillis() - startTime;
//        if (runTime > OfficeTools.TIME_TOLERANCE) {
          MessageHandler.printToLogFile("CheckDialog: Time to initialise dialog size: " + runTime);
//        }
      }
    } catch (Throwable t) {
      MessageHandler.showError(t);
      closeDialog();
    }
  }
  
  @Override
  public void run() {
    try {
      show();
    } catch (Throwable e) {
      MessageHandler.showError(e);
    }
  }

  /**
   * Set the selection color to a combo box
   *//*
  private void setJComboSelectionBackground(JComboBox<String> comboBox, Color color) {
    Object context = comboBox.getAccessibleContext().getAccessibleChild(0);
    BasicComboPopup popup = (BasicComboPopup)context;
    JList<Object> list = popup.getList();
    list.setSelectionBackground(color);
  }
*/
  private void errorReturn() {
    MessageHandler.showMessage(messages.getString("loBusyMessage"));
    closeDialog();
  }

  /**
   * show the dialog
   * @throws Throwable 
   */
  public void show() throws Throwable {
    if (currentDocument == null || currentDocument.getDocumentType() != DocumentType.WRITER) {
      return;
    }
    if (debugMode) {
      MessageHandler.printToLogFile("CheckDialog: show: Goto next Error");
    }
    if (dialogX < 0 || dialogY < 0) {
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension frameSize = dialog.getSize();
      dialogX = screenSize.width / 2 - frameSize.width / 2;
      dialogY = screenSize.height / 2 - frameSize.height / 2;
    }
    dialog.setLocation(dialogX, dialogY);
    dialog.setAutoRequestFocus(true);
    dialog.setVisible(true);
    docId = currentDocument.getDocID();
    setText();
  }

  /**
   * Get the current document
   * Wait until it is initialized (by LO/OO)
   */
  private SingleDocument getCurrentDocument() {
    SingleDocument currentDocument = documents.getCurrentDocument();
    if (currentDocument != null && currentDocument.getDocumentType() != DocumentType.WRITER) {
      return null;
    }
    return currentDocument;
  }

  /**
   * Initialize the cursor / define the range for check
   * @throws Throwable 
   */
  private void setText() throws Throwable {
    if (currentDocument.getDocumentType() == DocumentType.WRITER) {
      XComponent xComponent = currentDocument.getXComponent();
      ViewCursorTools viewCursor = new ViewCursorTools(xComponent);
      TextParagraph tPara = viewCursor.getViewCursorParagraph();
      DocumentCache docCache = currentDocument.getDocumentCache();
      paraText = docCache.getTextParagraph(tPara);
      locale = docCache.getTextParagraphLocale(tPara);
      paragraph.setText(paraText);
    } else {
      paraText = "";
      locale = null;
      paragraph.setText(paraText);
    }
  }

  /**
   * Initial button state
   */
  private void setAtWorkButtonState(boolean work) {
    checkProgress.setIndeterminate(work);
    instruction.setEnabled(!work);
    paragraph.setEnabled(!work);
    result.setEnabled(!work);
    execute.setEnabled(!work);
    copyResult.setEnabled(!work);
    reset.setEnabled(!work);
    clear.setEnabled(!work);
    undo.setEnabled(saveText == null ? false : !work);
    overrideParagraph.setEnabled(resultText == null ? false : !work);
    addToParagraph.setEnabled(resultText == null ? false : !work);
    help.setEnabled(!work);
    close.setEnabled(true);
    contentPane.revalidate();
    contentPane.repaint();
    dialog.setEnabled(!work);
    atWork = work;
  }
  
  /**
   * execute AI request
   */
  private void execute() {
    try {
      if (!documents.isEnoughHeapSpace()) {
        closeDialog();
        return;
      }
      if (debugMode) {
        MessageHandler.printToLogFile("AiDialog: execute: start AI request");
      }
      String instructionText = (String) instruction.getSelectedItem();
      if (!instructionList.contains(instructionText)) {
        instruction.insertItemAt(instructionText, 0);
        instructionList.add(0, instructionText);
        if (instructionList.size() > MAX_INSTRUCTIONS) {
          instructionList.remove(instructionList.size() - 1);
          instruction.removeItemAt(instruction.getItemCount() - 1);
        }
        writeInstructions(instructionList);
      }
      String text = paragraph.getText();
      setAtWorkButtonState(true);
      AiRemote aiRemote = new AiRemote(config);
      if (debugMode) {
        MessageHandler.printToLogFile("AiParagraphChanging: runInstruction: instruction: " + instructionText + ", text: " + text);
      }
      String output = aiRemote.runInstruction(instructionText, text, locale, false);
      if (debugMode) {
        MessageHandler.printToLogFile("AiParagraphChanging: runAiChangeOnParagraph: output: " + output);
      }
      result.setEnabled(true);
      result.setText(output);
      resultText = output;
    } catch (Throwable t) {
      MessageHandler.showError(t);
      closeDialog();
    }
    setAtWorkButtonState(false);
  }

  /**
   * Actions of buttons
   */
  @Override
  public void actionPerformed(ActionEvent action) {
    if (!atWork) {
      try {
        if (debugMode) {
          MessageHandler.printToLogFile("CheckDialog: actionPerformed: Action: " + action.getActionCommand());
        }
        if (action.getActionCommand().equals("close")) {
          closeDialog();
        } else if (action.getActionCommand().equals("help")) {
          MessageHandler.showMessage(messages.getString("Not implemented yet"));
        } else if (action.getActionCommand().equals("execute")) {
          execute();
        } else if (action.getActionCommand().equals("copyResult")) {
          copyResult();
        } else if (action.getActionCommand().equals("reset")) {
          resetText();
        } else if (action.getActionCommand().equals("clear")) {
          clearText();
        } else if (action.getActionCommand().equals("undo")) {
          undo();
        } else if (action.getActionCommand().equals("overrideParagraph")) {
          writeToParagraph(true);
        } else if (action.getActionCommand().equals("addToParagraph")) {
          writeToParagraph(false);
        } else {
          MessageHandler.showMessage("Action '" + action.getActionCommand() + "' not supported");
        }
      } catch (Throwable e) {
        MessageHandler.showError(e);
        closeDialog();
      }
    }
  }
  
  private void copyResult() {
    saveText = paraText;
    saveResult = resultText;
    paraText = resultText;
    paragraph.setText(paraText);
  }

  private void resetText() throws Throwable {
    saveText = paraText;
    saveResult = resultText;
    setText();
  }

  private void clearText() throws Throwable {
    saveText = paraText;
    saveResult = resultText;
    paraText = "";
    paragraph.setText(paraText);
  }

  private void undo() {
    if (saveText != null) {
      paraText = saveText;
      resultText = saveResult;
      saveText = null;
      paragraph.setText(paraText);
      result.setText(resultText);
    }
  }

  private void writeToParagraph(boolean override) throws Throwable {
    AiParagraphChanging.insertText(resultText, currentDocument.getXComponent(), override);
  }
  
  
  
  private List<String> readInstructions() {
    String dir = OfficeTools.getLOConfigDir().getAbsolutePath();
    File file = new File(dir, AI_INSTRUCTION_FILE_NAME);
    if (!file.canRead() || !file.isFile()) {
      return new ArrayList<>();
    }
    List<String> instructions = new ArrayList<>();
    BufferedReader in = null;
    try {
      in = new BufferedReader(new FileReader(file.getAbsoluteFile()));
      String row = null;
      int n = 0;
      while ((row = in.readLine()) != null) {
        instructions.add(row);
        n++;
        if (n > MAX_INSTRUCTIONS) {
          break;
        }
      }
    } catch (Throwable e) {
      MessageHandler.showError(e);
    } finally {
      if (in != null)
        try {
            in.close();
        } catch (IOException e) {
        }
    }
    return instructions;
  }

  private void writeInstructions(List<String> instructions) {
    String dir = OfficeTools.getLOConfigDir().getAbsolutePath();
    File file = new File(dir, AI_INSTRUCTION_FILE_NAME);
    PrintWriter pWriter = null;
    try {
      pWriter = new PrintWriter(new FileWriter(file.getAbsoluteFile()));
      for (String inst : instructions) {
        pWriter.println(inst);
      }
    } catch (Throwable e) {
      MessageHandler.showError(e);
    } finally {
      if (pWriter != null) {
        pWriter.flush();
        pWriter.close();
      }
    }
  }

  /**
   * closes the dialog
   */
  public void closeDialog() {
    dialog.setVisible(false);
    if (debugMode) {
      MessageHandler.printToLogFile("AiDialog: closeDialog: Close AI Dialog");
    }
    atWork = false;
  }
  

}
