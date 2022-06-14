/* LanguageTool, a natural language style checker
 * Copyright (C) 2018 Fabian Richter
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
package org.languagetool.rules.ar;

import org.languagetool.tools.StringTools;

import java.util.Calendar;
import java.util.Locale;

/**
 * @since 4.3
 */
class DateFilterHelper {

  protected Calendar getCalendar() {
    return Calendar.getInstance(Locale.UK);
  }

  @SuppressWarnings("ControlFlowStatementWithoutBraces")
  protected int getDayOfWeek(String dayStr) {

     switch (dayStr) {
      case "السبت": return Calendar.SATURDAY;
      case "الأحد": return Calendar.SUNDAY;
      case "الإثنين": return Calendar.MONDAY;
      case "الاثنين": return Calendar.MONDAY;
      case "الثلاثاء": return Calendar.TUESDAY;
      case "الأربعاء": return Calendar.WEDNESDAY;
      case "الخميس": return Calendar.THURSDAY;
      case "الجمعة": return Calendar.FRIDAY;
    }
    throw new RuntimeException("لا يمكن إيجاد اسم يوم لـ" + dayStr + "'");
  }


  @SuppressWarnings({"ControlFlowStatementWithoutBraces", "MagicNumber"})
  protected int getMonth(String monthStr) {
      String mon = StringTools.trimSpecialCharacters(monthStr);
      switch (mon)
      {
        // الأشهر العربية بالسريانية
        case "كانون الثاني": return 1;
        case "كانون ثاني": return 1;
        case "شباط": return 2;
        case "آذار": return 3;
        case "نيسان": return 4;
        case "أيار": return 5;
        case "حزيران": return 6;
        case "تموز": return 7;
        case "آب": return 8;
        case "أيلول": return 9;
        case "تشرين الأول": return 10;
        case "تشرين الثاني": return 11;
        case "كانون الأول":return 12;
        case "تشرين ثاني": return 11;
        case "كانون أول": return 12;
        // الأشهر المعربة عن الإنجليزية
        case "يناير": return 1;
        case "فبراير": return 2;
        case "مارس": return 3;
        case "أبريل": return 4;
        case "مايو": return 5;
        case "يونيو": return 6;
        case "يوليو": return 7;
        case "أغسطس": return 8;
        case "سبتمبر": return 9;
        case "أكتوبر": return 10;
        case "نوفمبر": return 11;
        case "ديسمبر": return 12;
        // الأشهر المعربة عن الفرنسية
        case "جانفي": return 1;
        case "جانفييه": return 1;
        case "فيفري": return 2;
//      case "مارس": return 3;
        case "أفريل": return 4;
        case "ماي": return 5;
        case "جوان": return 6;
        case "جويلية": return 7;
        case "أوت": return 8;
        //مكررة
//      case "سبتمبر": return 9;
//      case "أكتوبر": return 10;
//      case "نوفمبر": return 11;
//      case "ديسمبر": return 12;


      }
      throw new RuntimeException("لا اسم شهر لـ '" + monthStr + "'");
    }


  /* get day of week name */
  protected String getDayOfWeekName(int day) {
    switch (day)
    {
      case Calendar.SATURDAY: return "السبت";
      case Calendar.SUNDAY: return "الأحد";
      case Calendar.MONDAY: return "الإثنين";
      case Calendar.TUESDAY: return "الثلاثاء";
      case Calendar.WEDNESDAY: return "الأربعاء";
      case Calendar.THURSDAY: return "الخميس";
      case Calendar.FRIDAY: return "الجمعة";
      default: return "غير محدد";
    }

  }
}
