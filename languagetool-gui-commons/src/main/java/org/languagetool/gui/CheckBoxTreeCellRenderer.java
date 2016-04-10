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

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

/**
 *
 * @author Panagiotis Minos
 * @since 2.6
 */
class CheckBoxTreeCellRenderer extends JPanel implements TreeCellRenderer {

  private final DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
  private final JCheckBox checkBox = new JCheckBox();
  
  private Component defaultComponent;

  CheckBoxTreeCellRenderer() {
    setLayout(new BorderLayout());
    setOpaque(false);
    checkBox.setOpaque(false);
    renderer.setLeafIcon(null);
    add(checkBox, BorderLayout.WEST);
  }

  @Override
  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    Component component = renderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

    if (value instanceof CategoryNode) {
      if (defaultComponent != null) {
        remove(defaultComponent);
      }
      defaultComponent = component;
      add(component, BorderLayout.CENTER);
      CategoryNode node = (CategoryNode) value;
      checkBox.setSelected(node.isEnabled());
      return this;
    }

    if (value instanceof RuleNode) {
      if (defaultComponent != null) {
        remove(defaultComponent);
      }
      defaultComponent = component;
      add(component, BorderLayout.CENTER);
      RuleNode node = (RuleNode) value;
      checkBox.setSelected(node.isEnabled());
      return this;
    }

    return component;
  }
}
