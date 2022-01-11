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
package org.languagetool.rules.ca;

import org.languagetool.rules.AbstractDateCheckFilter;

import java.util.Calendar;

/**
 * Catalan localization of {@link AbstractDateCheckFilter}.
 * @since 2.7
 */
public class DateCheckFilter extends AbstractDateCheckFilter {
  
  private final DateFilterHelper dateFilterHelper = new DateFilterHelper();

  @Override
  protected int getMonth(String localizedMonth) {
    return dateFilterHelper.getMonth(localizedMonth);
  }

  @Override
  protected Calendar getCalendar() {
    return dateFilterHelper.getCalendar();
  }
  
  protected int getDayOfWeek(String dayStr) {
    return dateFilterHelper.getDayOfWeek(dayStr);
  }

  @Override
  protected String getDayOfWeek(Calendar date) {
    return dateFilterHelper.getDayOfWeek(date);
  }
}
