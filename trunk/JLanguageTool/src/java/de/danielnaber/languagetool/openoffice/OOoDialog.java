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
package de.danielnaber.languagetool.openoffice;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.frame.XController;
import com.sun.star.frame.XModel;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.text.XParagraphCursor;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextRange;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.text.XTextViewCursorSupplier;
import com.sun.star.uno.UnoRuntime;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.gui.Configuration;
import de.danielnaber.languagetool.gui.ConfigurationDialog;
import de.danielnaber.languagetool.gui.Tools;
import de.danielnaber.languagetool.rules.Rule;
import de.danielnaber.languagetool.rules.RuleMatch;

public class OOoDialog implements ActionListener {

  private static final String CHANGE_BUTTON = "Change";
  private static final String IGNORE_BUTTON = "Ignore";
  private static final String IGNORE_ALL_BUTTON = "Ignore All";
  private static final String OPTIONS_BUTTON = "Options...";
  private static final String CLOSE_BUTTON = "Close";
  
  private static final String COMPLETE_CHECK = "LanguageTool check is complete.";
  private static final String COMPLETE_CHECK_OF_SELECTION = "LanguageTool check of selected text is complete.";
  private static final String FONT_TAG = "<font face=\"Sans-Serif\">";
  private static final int CONTEXT_SIZE = 60;
  private static final String REPLACE_WITH_OTHER_TEXT = "<other text>";
  
  private List<Rule> rules = null;
  private JDialog dialog = null;

  private JTextPane contextArea = null;
  private JButton ignoreButton = null;
  private JButton ignoreAllButton = null;
  private JTextPane messageArea = null;
  private JList suggestionList = null;
  private JButton changeButton = null;
  private JButton optionsButton = null;
  private JButton closeButton = null;
  
  private XTextDocument xTextDoc = null;
  private List<CheckedParagraph> checkedParagraphs = null;

  private RuleMatch currentRuleMatch = null;
  private Set<String> rulesToIgnore = new HashSet<String>();
  private int prevMatchParagraph = -1;
  private int currentRuleMatchPos = 0;
  private int replacementCorrection = 0;
  private XTextViewCursor xViewCursor = null;
  private XTextViewCursor myViewCursor = null;
  private XTextRange startTextRange = null;

  private Configuration configuration = null;

  private TextToCheck textToCheck = null;
  
  /**
   * Use this constructor to check the selected text only. 
   */
  OOoDialog(Configuration configuration, List<Rule> rules, XTextDocument xTextDoc, 
      List<CheckedParagraph> checkedParagraphs,
      XTextViewCursor xViewCursor, TextToCheck textToCheck) {
    this.rules = rules;
    this.xTextDoc = xTextDoc;
    this.checkedParagraphs = checkedParagraphs;
    this.configuration = configuration;
    this.xViewCursor = xViewCursor;
    this.textToCheck = textToCheck;
  }

  /**
   * Use this constructor to check the complete document. 
   */
  OOoDialog(Configuration configuration, List<Rule> rules, XTextDocument xTextDoc, 
      List<CheckedParagraph> checkedParagraphs) {
    this(configuration, rules, xTextDoc, checkedParagraphs, null, null);
  }
  
  void show() {
    if (checkedParagraphs.size() == 0) {
      complete();
      return;
    }
    dialog = new JDialog();
    // close when user presses Escape key:
    bindKey(dialog, KeyEvent.VK_ESCAPE, new EscapeActionListener(dialog));
    
    dialog.setTitle("LanguageTool Version " + JLanguageTool.VERSION);
    Container contentPane = dialog.getContentPane();
    contentPane.setLayout(new GridBagLayout());

    GridBagConstraints cons = new GridBagConstraints();
    cons.anchor = GridBagConstraints.NORTHWEST;
    cons.insets = new Insets(4, 4, 4, 4);

    contextArea = new JTextPane();
    cons.fill = GridBagConstraints.BOTH;
    cons.weightx = 6.0f;
    cons.weighty = 6.0f;
    cons.gridx = 0;
    cons.gridy = 0;
    cons.gridheight = 3;
    contextArea.setContentType("text/html");
    contextArea.setEditable(false);
    contentPane.add(new JScrollPane(contextArea), cons);
    cons.gridheight = 1;
    cons.weightx = 1.0f;
    cons.weighty = 1.0f;

    cons.gridx = 1;
    cons.gridy = 0;
    cons.fill = GridBagConstraints.HORIZONTAL;
    ignoreButton = new JButton(IGNORE_BUTTON);
    ignoreButton.addActionListener(this);
    contentPane.add(ignoreButton, cons);
    
    cons.gridx = 1;
    cons.gridy = 1;
    ignoreAllButton = new JButton(IGNORE_ALL_BUTTON);
    ignoreAllButton.addActionListener(this);
    contentPane.add(ignoreAllButton, cons);

    cons.gridx = 1;
    cons.gridy = 2;
    optionsButton = new JButton(OPTIONS_BUTTON);
    optionsButton.addActionListener(this);
    contentPane.add(optionsButton, cons);

    messageArea = new JTextPane();
    messageArea.setContentType("text/html");
    messageArea.setEditable(false);
    cons.fill = GridBagConstraints.BOTH;
    cons.weightx = 8.0f;
    cons.weighty = 8.0f;
    cons.gridx = 0;
    cons.gridy = 3;
    contentPane.add(new JScrollPane(messageArea), cons);

    suggestionList = new JList();
    cons.fill = GridBagConstraints.BOTH;
    cons.gridheight = 2;
    cons.gridx = 0;
    cons.gridy = 4;
    cons.weightx = 2.0f;
    cons.weighty = 2.0f;
    contentPane.add(new JScrollPane(suggestionList), cons);
    cons.weightx = 1.0f;
    cons.weighty = 1.0f;
    cons.gridheight = 1;
    suggestionList.addMouseListener(new DoubleClickListener());

    cons.gridx = 1;
    cons.gridy = 4;
    cons.gridwidth = 1;
    cons.weightx = 1.0f;
    cons.weighty = 1.0f;
    cons.fill = GridBagConstraints.HORIZONTAL;
    changeButton = new JButton(CHANGE_BUTTON);
    changeButton.addActionListener(this);
    contentPane.add(changeButton, cons);

    cons.gridx = 1;
    cons.gridy = 5;
    cons.anchor = GridBagConstraints.SOUTH;
    closeButton = new JButton(CLOSE_BUTTON);
    closeButton.addActionListener(this);
    contentPane.add(closeButton, cons);

    try {
      showError(0);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    dialog.pack();
    dialog.setModal(true);
    dialog.setSize(500, 380);
    centerDialog(dialog);
    dialog.setVisible(true);
    // FIXME: close via "X" in the window must behave like close via "close" button
  }
  
  static void bindKey(JDialog dialog, int keyCode, ActionListener actionListener) {
    KeyStroke stroke = KeyStroke.getKeyStroke(keyCode, 0);
    JRootPane rootPane = dialog.getRootPane();
    rootPane.registerKeyboardAction(actionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
  }

  static void centerDialog(Container dialog) {
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = dialog.getSize();
    dialog.setLocation(screenSize.width/2 - (frameSize.width/2), screenSize.height/2 - (frameSize.height/2));
  }
  
  private int getTotalRuleMatches() {
    int total = 0;
    for (CheckedParagraph checkedParagraph : checkedParagraphs) {
      total += checkedParagraph.getRuleMatches().size(); 
    }
    return total;
  }
  
  private void showError(int i) throws NoSuchElementException, WrappedTargetException, UnknownPropertyException {
    RuleMatch match = null;
    int count = 0;
    int paragraphNumber = -1;
    for (CheckedParagraph checkedParagraph : checkedParagraphs) {
      for (RuleMatch ruleMatch : checkedParagraph.getRuleMatches()) {
        if (count >= i && !rulesToIgnore.contains(ruleMatch.getRule().getId())) {
          match = ruleMatch;
          paragraphNumber = checkedParagraph.getParagraphNumber();
          break;
        }
        count++;
      }
      if (match != null) {
        break;
      }
    }
    if (match == null || paragraphNumber == -1) {
      complete();
      return;
    }
    currentRuleMatch = match;
    currentRuleMatchPos = count;
    String msg = match.getMessage();
    msg = msg.replaceAll("<suggestion>", "<b>");
    msg = msg.replaceAll("</suggestion>", "</b>");
    StringBuilder sb = new StringBuilder();
    int totalMatches = getTotalRuleMatches();
    if (totalMatches == 1)
      sb.append(totalMatches + " match in total");         //TODO: i18n
    else
      sb.append(totalMatches + " matches in total");
    sb.append("<br>\n<br>\n<b>" +(i+1)+ ".</b> ");
    sb.append("<b>Match:</b> ");
    sb.append(msg);
    sb.append("<br>\n");
    messageArea.setText(FONT_TAG + sb.toString());
    setSuggestions();
    // Place visible cursor on the error:
    if (xTextDoc != null) {
      XModel xModel = (XModel)UnoRuntime.queryInterface(XModel.class, xTextDoc); 
      XController xController = xModel.getCurrentController(); 
      XTextViewCursorSupplier xViewCursorSupplier = 
        (XTextViewCursorSupplier)UnoRuntime.queryInterface(XTextViewCursorSupplier.class, xController); 
      int errorLength = currentRuleMatch.getToPos() - currentRuleMatch.getFromPos();
      if (xViewCursor == null) {
        // working on complete text:
        // get an invisible cursor:
        XTextCursor cursor = xTextDoc.getText().createTextCursor();
        XParagraphCursor xParagraphCursor = (XParagraphCursor)
          UnoRuntime.queryInterface(XParagraphCursor.class, cursor);
        boolean hasNextParagraph = true;
        boolean foundError = false;
        int paraCount = 0;
        while (hasNextParagraph) {
          xParagraphCursor.gotoStartOfParagraph(true);
          if (paraCount == paragraphNumber) {
            if (paragraphNumber != prevMatchParagraph)
              replacementCorrection = 0;

            // get the string to display in context area:
            String contentForDisplay = null;
            com.sun.star.container.XEnumerationAccess xParaAccess = (com.sun.star.container.XEnumerationAccess) UnoRuntime
              .queryInterface(com.sun.star.container.XEnumerationAccess.class, xTextDoc.getText());
            int innerParaCount = 0;
            for (com.sun.star.container.XEnumeration xParaEnum = xParaAccess.createEnumeration(); xParaEnum.hasMoreElements();) {
              Object para = xParaEnum.nextElement();
              if (innerParaCount == paragraphNumber) {
                contentForDisplay = Main.getParagraphContent(para);
                break;
              }
              innerParaCount++;
            }
            contextArea.setText(FONT_TAG + Tools.getContext(match.getFromPos()-replacementCorrection,
                match.getToPos()-replacementCorrection, contentForDisplay, CONTEXT_SIZE));

            // place cursor:
            xParagraphCursor.gotoStartOfParagraph(false);
            xParagraphCursor.goRight((short)(currentRuleMatch.getFromPos()-replacementCorrection), false);
            xParagraphCursor.goRight((short)errorLength, true);
            // now place the visible cursor:
            myViewCursor = xViewCursorSupplier.getViewCursor();
            myViewCursor.gotoRange(xParagraphCursor.getStart(), false);
            myViewCursor.gotoRange(xParagraphCursor.getEnd(), true);
            prevMatchParagraph = paragraphNumber;
            foundError = true;
            break;
          }
          hasNextParagraph = xParagraphCursor.gotoNextParagraph(false);
          paraCount++;
        }
        if (!foundError)
          throw new IllegalStateException("No position found in text that should be marked (paragraph="+paragraphNumber+")");

      } else {
        // working on selected text only:
        if (startTextRange == null) {
          startTextRange = xViewCursor.getStart();
        }
        // get the text to show in our dialog:
        int j = 0;
        String contentForDisplay = null;
        for (Iterator iter = textToCheck.paragraphs.iterator(); iter.hasNext();) {
          String para = (String) iter.next();
          if (paragraphNumber == j) {
            contentForDisplay = para;
            break;
          }
          j++;
        }
        contextArea.setText(FONT_TAG + Tools.getContext(match.getFromPos(),
            match.getToPos(), contentForDisplay, CONTEXT_SIZE));
        // FIXME: throws java.lang.reflect.UndeclaredThrowableException at $Proxy17.gotoRange(Unknown Source
        // if previous error occured at start of text and was replaced (e.g. "Die die" -> "die"):
        xViewCursor.gotoRange(startTextRange, false);
        xViewCursor.goRight((short)(currentRuleMatch.getFromPos()-replacementCorrection), false);
        xViewCursor.goRight((short)errorLength, true);
      }
    }
  }
  
  private void setSuggestions() {
    List<String> suggestions = currentRuleMatch.getSuggestedReplacements();
    suggestions.add(REPLACE_WITH_OTHER_TEXT);
    suggestionList.setListData(suggestions.toArray());
    suggestionList.setSelectedIndex(0);
  }

  private void changeText() {
    String replacement = (String)suggestionList.getSelectedValue();
    if (REPLACE_WITH_OTHER_TEXT.equals(replacement)) {
      ReplaceTextDialog replaceDialog = new ReplaceTextDialog();
      String replacementTextTmp = replaceDialog.getText();
      if (replacementTextTmp != null) {
        replacement = replacementTextTmp;
      } else {
        return;
      }
    }
    // FIXME: what if cast fails?
    short errorLength = (short)(currentRuleMatch.getToPos()-currentRuleMatch.getFromPos());
    if (xViewCursor == null) {
      myViewCursor.setString(replacement);
    } else {
      // working on selected text only:
      if (startTextRange == null) {
        startTextRange = xViewCursor.getStart();
      }
      xViewCursor.gotoRange(startTextRange, false);
      xViewCursor.goRight((short)(currentRuleMatch.getFromPos()-replacementCorrection), false);
      xViewCursor.goRight((short)errorLength, true);
      xViewCursor.setString(replacement);
    }
    replacementCorrection += errorLength - replacement.length();
    gotoNextMatch();
  }

  private void gotoNextMatch() {
    if (currentRuleMatchPos >= getTotalRuleMatches()-1) {
      complete();
    } else {
      currentRuleMatchPos++;
      try {
        showError(currentRuleMatchPos);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void complete() {
    if (xViewCursor == null)
      JOptionPane.showMessageDialog(null, COMPLETE_CHECK);
    else
      JOptionPane.showMessageDialog(null, COMPLETE_CHECK_OF_SELECTION);
    close();
  }
  
  private void close() {
    try {
      configuration.saveConfiguration();
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    dialog.setVisible(false);       // FIXME: does this really close the dialog?
  }

  /**
   * Ignore all matches of the current rule for the rest of this document.
   */
  private void ignoreAll() {
    rulesToIgnore.add(currentRuleMatch.getRule().getId());
  }

  public void actionPerformed(ActionEvent event) {
    if (event.getActionCommand().equals(CHANGE_BUTTON)) {
      changeText();
    } else if (event.getActionCommand().equals(IGNORE_BUTTON)) {
      gotoNextMatch();
    } else if (event.getActionCommand().equals(IGNORE_ALL_BUTTON)) {
      ignoreAll();
      gotoNextMatch();
    } else if (event.getActionCommand().equals(OPTIONS_BUTTON)) {
      ConfigurationDialog cfgDialog = new ConfigurationDialog(true);
      cfgDialog.setMotherTongue(configuration.getMotherTongue());
      cfgDialog.setDisabledRules(configuration.getDisabledRuleIds());
      cfgDialog.show(rules);
      configuration.setDisabledRuleIds(cfgDialog.getDisabledRuleIds());
      configuration.setMotherTongue(cfgDialog.getMotherTongue());
    } else if (event.getActionCommand().equals(CLOSE_BUTTON)) {
      close();
    } else {
      System.err.println("Unknown action: " + event);
    }
  }

  /** Testing only.
   */
/*  public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
    JLanguageTool lt = new JLanguageTool(Language.ENGLISH);
    lt.activateDefaultPatternRules();
    Configuration config = new Configuration(new File("/tmp"));
    for (Iterator<String> iter = config.getDisabledRuleIds().iterator(); iter.hasNext();) {
      String id = iter.next();
      lt.disableRule(id);
    }
    //String text = "and a hour ago. this is a test, I thing that's a good idea.";
    //String text = "i thing that's a good idea. This is an test.";
    String text = "There was to much snow.";
    List<RuleMatch> ruleMatches = lt.check(text);
    OOoDialog prg = new OOoDialog(config, lt.getAllRules(), null, ruleMatches, text, null);

    prg.show();
  }
*/

  class DoubleClickListener implements MouseListener {

    public void mouseClicked(MouseEvent e) {
      if (e.getClickCount() == 2) {
        changeText();
      }
    }

    public void mouseEntered(@SuppressWarnings("unused")MouseEvent e) {}
    public void mouseExited(@SuppressWarnings("unused")MouseEvent e) {}
    public void mousePressed(@SuppressWarnings("unused")MouseEvent e) {}
    public void mouseReleased(@SuppressWarnings("unused")MouseEvent e) {}
    
  }

}

class EscapeActionListener implements ActionListener {
  
  private JDialog dialog = null;
  
  EscapeActionListener(JDialog dialog) {
    this.dialog = dialog;
  }

  @SuppressWarnings("unused")
  public void actionPerformed(ActionEvent actionEvent) {
    dialog.setVisible(false);
   }

}
