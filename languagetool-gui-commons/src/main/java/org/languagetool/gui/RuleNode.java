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
import org.languagetool.rules.Rule;

/**
 * @author Panagiotis Minos
 * @since 2.6
 */
class RuleNode extends DefaultMutableTreeNode {

  private final Rule rule;
  private boolean enabled;

  RuleNode(Rule rule, boolean enabled) {
    super(rule);
    this.rule = rule;
    this.enabled = enabled;
  }

  Rule getRule() {
    return rule;
  }

  boolean isEnabled() {
    return enabled;
  }

  void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public String toString() {
    return rule.getDescription();
  }
}
