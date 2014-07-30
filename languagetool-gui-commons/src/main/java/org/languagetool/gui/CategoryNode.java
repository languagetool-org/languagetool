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

import javax.swing.tree.DefaultMutableTreeNode;
import org.languagetool.rules.Category;

/**
 *
 * @author Panagiotis Minos
 * @since 2.6
 */
class CategoryNode extends DefaultMutableTreeNode {

  private final Category category;
  private boolean enabled;

  CategoryNode(Category category, boolean enabled) {
    super(category);
    this.category = category;
    this.enabled = enabled;
  }

  Category getCategory() {
    return category;
  }

  boolean isEnabled() {
    return enabled;
  }

  void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public String toString() {
    int children = this.getChildCount();
    int selected = 0;
    for (int i = 0; i < children; i++) {
      RuleNode child = (RuleNode) this.getChildAt(i);
      if (child.isEnabled()) {
        selected++;
      }
    }
    return String.format("%s (%d/%d)", category.getName(), selected, children);
  }

}
