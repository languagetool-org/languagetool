/* JLanguageTool, a natural language style checker 
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.sun.star.frame.XController;
import com.sun.star.frame.XModel;
import com.sun.star.text.XText;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.text.XTextViewCursorSupplier;
import com.sun.star.uno.UnoRuntime;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.gui.Configuration;
import de.danielnaber.languagetool.gui.ConfigurationDialog;
import de.danielnaber.languagetool.gui.Tools;
import de.danielnaber.languagetool.rules.RuleMatch;

public class OOoDialog implements ActionListener {

  private final static String CHANGE_BUTTON = "Change";
  private static final String IGNORE_BUTTON = "Ignore";
  private static final String IGNORE_ALL_BUTTON = "Ignore All";
  private static final String OPTIONS_BUTTON = "Options...";
  private static final String CLOSE_BUTTON = "Close";
  
  private static final String COMPLETE_TEXT = "JLanguageTool check is complete.";
  
  private List rules = null;
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
  private List ruleMatches = null;
  private String text = null;

  private RuleMatch currentRuleMatch = null;
  private int currentRuleMatchPos = 0;
  
  private Configuration configuration = null; 

  OOoDialog(Configuration configuration, List rules, XTextDocument xTextDoc, List ruleMatches, String text) {
    this.rules = rules;
    this.xTextDoc = xTextDoc;
    this.ruleMatches = ruleMatches;
    this.text = text;
    this.configuration = configuration;
  }
  
  void show() {
    if (ruleMatches.size() == 0) {
      JOptionPane.showMessageDialog(null, COMPLETE_TEXT);
      return;
    }
    dialog = new JDialog();
    //dialog.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
    dialog.setTitle("JLanguageTool/OOo ALPHA Version");
    Container contentPane = dialog.getContentPane();
    contentPane.setLayout(new GridBagLayout());

    GridBagConstraints cons = new GridBagConstraints();
    cons.anchor = GridBagConstraints.NORTHWEST;
    cons.insets = new Insets(4, 4, 4, 4);

    contextArea = new JTextPane();
    cons.fill = GridBagConstraints.BOTH;
    cons.weightx = 8.0f;
    cons.weighty = 8.0f;
    cons.gridx = 0;
    cons.gridy = 0;
    cons.gridheight = 2;
    contextArea.setContentType("text/html");
    contextArea.setEditable(false);
    contentPane.add(new JScrollPane(contextArea), cons);
    cons.gridheight = 1;
    cons.weightx = 1.0f;
    cons.weighty = 1.0f;

    cons.gridx = 1;
    cons.gridy = 0;
    cons.fill = GridBagConstraints.NONE;
    ignoreButton = new JButton(IGNORE_BUTTON);
    ignoreButton.addActionListener(this);
    contentPane.add(ignoreButton, cons);
    
    cons.gridx = 1;
    cons.gridy = 1;
    ignoreAllButton = new JButton(IGNORE_ALL_BUTTON);
    ignoreAllButton.addActionListener(this);
    contentPane.add(ignoreAllButton, cons);

    messageArea = new JTextPane();
    messageArea.setContentType("text/html");
    messageArea.setEditable(false);
    cons.fill = GridBagConstraints.BOTH;
    cons.weightx = 8.0f;
    cons.weighty = 8.0f;
    cons.gridx = 0;
    cons.gridy = 2;
    contentPane.add(new JScrollPane(messageArea), cons);

    suggestionList = new JList();
    cons.fill = GridBagConstraints.BOTH;
    cons.gridheight = 2;
    cons.gridx = 0;
    cons.gridy = 3;
    contentPane.add(new JScrollPane(suggestionList), cons);
    cons.gridheight = 1;

    cons.gridx = 1;
    cons.gridy = 3;
    cons.gridwidth = 1;
    cons.weightx = 1.0f;
    cons.weighty = 1.0f;
    cons.fill = GridBagConstraints.NONE;
    changeButton = new JButton(CHANGE_BUTTON);
    changeButton.addActionListener(this);
    contentPane.add(changeButton, cons);

    cons.gridx = 0;
    cons.gridy = 5;
    optionsButton = new JButton(OPTIONS_BUTTON);
    optionsButton.addActionListener(this);
    contentPane.add(optionsButton, cons);

    cons.gridx = 1;
    cons.gridy = 5;
    closeButton = new JButton(CLOSE_BUTTON);
    closeButton.addActionListener(this);
    contentPane.add(closeButton, cons);

    showError(0);

    dialog.pack();
    //dialog.setModal(true);
    dialog.setSize(500, 500);
    dialog.setVisible(true);
    // FIXME: close via "X" in the window must behave like close via "close" button
  }
  
  private void showError(int i) {
    RuleMatch match = (RuleMatch) ruleMatches.get(i);
    currentRuleMatch = match;
    currentRuleMatchPos = i;
    String msg = match.getMessage();
    StringBuffer sb = new StringBuffer();
    if (ruleMatches.size() == 1)
      sb.append(ruleMatches.size() + " match");
    else
      sb.append(ruleMatches.size() + " matches");
    sb.append("<br>\n<br>\n<b>" +(i+1)+ ".</b> ");
    sb.append("<b>Match:</b> ");
    sb.append(msg);
    sb.append("<br>\n");
    contextArea.setText(Tools.getContext(match.getFromPos(), match.getToPos(), text));
    messageArea.setText(sb.toString());
    setSuggestions();
    // Place visible cursor on the error:
    if (xTextDoc != null) {
      XModel xModel = (XModel)UnoRuntime.queryInterface(XModel.class, xTextDoc); 
      XController xController = xModel.getCurrentController(); 
      XTextViewCursorSupplier xViewCursorSupplier = (XTextViewCursorSupplier)UnoRuntime.queryInterface(XTextViewCursorSupplier.class, xController); 
      XTextViewCursor xViewCursor = xViewCursorSupplier.getViewCursor();
      xViewCursor.gotoStart(false);
      int errorLength = currentRuleMatch.getToPos() - currentRuleMatch.getFromPos();
      xViewCursor.goRight((short)currentRuleMatch.getFromPos(), false);
      xViewCursor.goRight((short)errorLength, true);
    }
  }
  
  private void setSuggestions() {
    List suggestions = currentRuleMatch.getSuggestedReplacements();
    if (suggestions.size() == 0) {
      System.err.println("No suggested replacement found");
      changeButton.setEnabled(false);
    } else {
      changeButton.setEnabled(true);
    }
    suggestionList.setListData(suggestions.toArray());
    suggestionList.setSelectedIndex(0);
  }

  public void actionPerformed(ActionEvent event) {
    if (event.getActionCommand().equals(CHANGE_BUTTON)) {
      String replacement = (String)suggestionList.getSelectedValue();
      XText text = xTextDoc.getText();
      XTextCursor cursor = text.createTextCursor();
      cursor.gotoStart(false);
      cursor.goRight((short)currentRuleMatch.getFromPos(), false);
      // FIXME: what if cast fails?
      short errorLength = (short)(currentRuleMatch.getToPos()-currentRuleMatch.getFromPos());
      //System.err.println(text.getString().replaceAll("\n", "#\n"));
      //System.err.println(currentRuleMatch.getFromPos() + ", len="+errorLength);
      cursor.goRight(errorLength, true);
      cursor.setString(replacement);
      gotoNextMatch();
      // FIXME: correct position of replacements for upcoming errors!
      //int correction = errorLength - replacement.length();
      //System.err.println("corr=" + correction);
    } else if (event.getActionCommand().equals(IGNORE_BUTTON)) {
      gotoNextMatch();
    } else if (event.getActionCommand().equals(IGNORE_ALL_BUTTON)) {
      JOptionPane.showMessageDialog(null, "fixme: not yet implemented");        //FIXME
    } else if (event.getActionCommand().equals(OPTIONS_BUTTON)) {
      ConfigurationDialog cfgDialog = new ConfigurationDialog();
      cfgDialog.setDisabledRules(configuration.getDisabledRuleIds());
      cfgDialog.show(rules);
      configuration.setDisabledRuleIds(cfgDialog.getDisabledRuleIds());
    } else if (event.getActionCommand().equals(CLOSE_BUTTON)) {
      close();
    } else {
      System.err.println("Unknown action: " + event);
    }
  }

  private void gotoNextMatch() {
    if (currentRuleMatchPos >= ruleMatches.size()-1) {
      complete();
    } else {
      currentRuleMatchPos++;
      showError(currentRuleMatchPos);
    }
  }

  private void complete() {
    JOptionPane.showMessageDialog(null, COMPLETE_TEXT);
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

  /** Testing only.
   */
  public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
    JLanguageTool lt = new JLanguageTool(Language.ENGLISH);
    lt.activateDefaultPatternRules();
    Configuration config = new Configuration();
    for (Iterator iter = config.getDisabledRuleIds().iterator(); iter.hasNext();) {
      String id = (String) iter.next();
      lt.disableRule(id);
    }
    //String text = "and a hour ago. this is a test, I thing that's a good idea.";
    String text = "i thing that's a good idea. This is an test.";
    List ruleMatches = lt.check(text);
    OOoDialog prg = new OOoDialog(config, lt.getAllRules(), null, ruleMatches, text);
    prg.show();
  }

}
