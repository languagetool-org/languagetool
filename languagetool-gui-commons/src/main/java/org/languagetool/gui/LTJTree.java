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

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class LTJTree extends JTree {

  private static TreePath lastPath;
  private static boolean state;
  private static final long serialVersionUID = 1L;

  public LTJTree(DefaultTreeModel treeModel) {
    super(treeModel);
  }

  public void setState() {
    if (lastPath != null) {
      setExpandedState(lastPath, state);
    }
  }

  public void expandPath(TreePath path) {
    // Only expand if not leaf!
    TreeModel model = getModel();
    state = true;
    ConfigurationDialog.lastTree = this;
    if (path != null && model != null && !model.isLeaf(path.getLastPathComponent())) {
      lastPath = path;
    } else {
      lastPath = null;
    }
  }

  public void collapsePath(TreePath path) {
    ConfigurationDialog.lastTree = this;
    state = false;
    lastPath = path;
  }
}
