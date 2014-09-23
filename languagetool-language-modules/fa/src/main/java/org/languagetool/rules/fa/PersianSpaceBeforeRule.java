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
package org.languagetool.rules.fa;

import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.languagetool.Language;
import org.languagetool.rules.AbstractSpaceBeforeRule;

/**
 * A Persian rule that checks if there is a missing space before some conjunctions.
 * 
 * @since 2.7
 */
public class PersianSpaceBeforeRule extends AbstractSpaceBeforeRule {

  private static final Pattern CONJUNCTIONS = Pattern.compile("و|به|با|تا|زیرا|چون|بنابراین|چونکه");

  public PersianSpaceBeforeRule(ResourceBundle messages, Language language) {
    super(messages, language);
  }

  @Override
  protected Pattern getConjunctions() {
    return CONJUNCTIONS;
  }

  @Override
  public String getId() {
    return "FA_SPACE_BEFORE_CONJUNCTION";
  }

  @Override
  public String getDescription() {
    return "بررسی‌کردن فاصله قبل از حرف ربط";
  }

  @Override
  protected String getShort() {
    return "فاصلهٔ حذف‌شده";
  }

  @Override
  protected String getSuggestion() {
    return "فاصلهٔ قبل از حرف ربط حذف شده‌است";
  }

}
