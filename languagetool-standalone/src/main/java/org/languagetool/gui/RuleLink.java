/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.rules.Rule;

/**
 * A link for enabling or disabling a rule.
 */
class RuleLink {

  private static final String DEACTIVATE_URL = "http://languagetool.org/deactivate/";
  private static final String REACTIVATE_URL = "http://languagetool.org/reactivate/";

  private final String urlPrefix;
  private final String id;

  private RuleLink(String urlPrefix, String id) {
    this.urlPrefix = urlPrefix;
    this.id = id;
  }

  static RuleLink buildDeactivationLink(Rule rule) {
    return new RuleLink(DEACTIVATE_URL, rule.getId());
  }

  static RuleLink buildReactivationLink(Rule rule) {
    return new RuleLink(REACTIVATE_URL, rule.getId());
  }

  static RuleLink getFromString(String ruleLink) {
    String id;
    if (ruleLink.startsWith(DEACTIVATE_URL)) {
      id = ruleLink.substring(DEACTIVATE_URL.length());
      return new RuleLink(DEACTIVATE_URL, id);
    } else if (ruleLink.startsWith(REACTIVATE_URL)) {
      id = ruleLink.substring(REACTIVATE_URL.length());
      return new RuleLink(REACTIVATE_URL, id);
    } else {
      throw new RuntimeException("Unknown link prefix: " + ruleLink);
    }
  }

  String getId() {
    return id;
  }

  @Override
  public String toString() {
    return urlPrefix + id;
  }

}
