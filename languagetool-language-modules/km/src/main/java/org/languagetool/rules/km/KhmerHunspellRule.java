/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Sohaib Afifi, Taha Zerrouki
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

package org.languagetool.rules.km;

import org.languagetool.UserConfig;
import org.languagetool.language.Khmer;
import org.languagetool.rules.spelling.hunspell.HunspellRule;

import java.util.ResourceBundle;

/**
 * @since 4.9
 */
public final class KhmerHunspellRule extends HunspellRule {

  public KhmerHunspellRule(ResourceBundle messages, UserConfig userConfig) {
    super(messages, new Khmer(), userConfig);
  }

  public KhmerHunspellRule(ResourceBundle messages) {
    this(messages, null);
  }
  
  @Override
  protected boolean isLatinScript() {
    return false;
  }

}