package org.languagetool.gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ResourceBundle;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.languagetool.JLanguageTool;

public class PopUpRemoveLanguage extends JPopupMenu {

  private final ResourceBundle messages;
  private final JMenuItem removeOption;
  private final LanguageAdapter languageAdapter;

  public PopUpRemoveLanguage() {
    languageAdapter = (LanguageAdapter) LanguageComboBoxModel.languageComboBoxModel.getSelectedItem();
    messages = JLanguageTool.getMessageBundle(languageAdapter.getLanguage());
    removeOption = new JMenuItem(messages.getString("guiRemoveOptionText"));
    add(removeOption);
    removeOption.addMouseListener(new OnClickListener());
  }
}

class OnClickListener implements MouseListener {
  @Override
  public void mousePressed(MouseEvent e) {
    LanguageComboBoxModel comboBoxModel = LanguageComboBoxModel.languageComboBoxModel;
    LanguageAdapter languageAdapter = (LanguageAdapter) comboBoxModel.getSelectedItem();
    ResourceBundle messages = JLanguageTool.getMessageBundle(languageAdapter.getLanguage());
    int itemCount = comboBoxModel.getSize();
    Object selectedElement = comboBoxModel.getSelectedItem();

    int index = comboBoxModel.getIndexOf(selectedElement);
    // If only one language is left,we cannot remove it

    if (itemCount == 1) {
      JOptionPane.showMessageDialog(null, "<html>" + messages.getString("guiRemoveWarningMsg") + "</html>", "",
          JOptionPane.WARNING_MESSAGE);
    } else if (index == 0) {
      int newIndex = comboBoxModel.getIndexOf(selectedElement) + 1;
      Object newElement = comboBoxModel.getElementAt(newIndex);
      comboBoxModel.setSelectedItem(newElement);
      comboBoxModel.removeElement(selectedElement);

    } else if (index - 1 >= 0) {
      int newIndex = comboBoxModel.getIndexOf(selectedElement) - 1;
      Object newElement = comboBoxModel.getElementAt(newIndex);
      comboBoxModel.setSelectedItem(newElement);
      comboBoxModel.removeElement(selectedElement);

    }

  }

  @Override
  public void mouseClicked(MouseEvent arg0) {

  }

  @Override
  public void mouseEntered(MouseEvent arg0) {

  }

  @Override
  public void mouseExited(MouseEvent arg0) {

  }

  @Override
  public void mouseReleased(MouseEvent e) {

  }
}