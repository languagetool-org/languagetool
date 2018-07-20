package org.languagetool.rules.de;

import java.util.Calendar;
import java.util.Locale;

class DateFilterHelper {
    public DateFilterHelper() {
    }

    protected Calendar getCalendar() {
        return Calendar.getInstance(Locale.GERMANY);
    }


    @SuppressWarnings("ControlFlowStatementWithoutBraces")
    protected int getDayOfWeek(String dayStr) {
        String day = dayStr.toLowerCase();
        if (day.startsWith("sonnabend")) return Calendar.SATURDAY;
        if (day.startsWith("so")) return Calendar.SUNDAY;
        if (day.startsWith("mo")) return Calendar.MONDAY;
        if (day.startsWith("di")) return Calendar.TUESDAY;
        if (day.startsWith("mi")) return Calendar.WEDNESDAY;
        if (day.startsWith("do")) return Calendar.THURSDAY;
        if (day.startsWith("fr")) return Calendar.FRIDAY;
        if (day.startsWith("sa")) return Calendar.SATURDAY;
        throw new RuntimeException("Could not find day of week for '" + dayStr + "'");
    }

    protected String getDayOfWeek(Calendar date) {
        return date.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.GERMAN);
    }

    @SuppressWarnings({"ControlFlowStatementWithoutBraces", "MagicNumber"})
    protected int getMonth(String monthStr) {
        String mon = monthStr.toLowerCase();
        if (mon.startsWith("jän")) return 1;
        if (mon.startsWith("jan")) return 1;
        if (mon.startsWith("feb")) return 2;
        if (mon.startsWith("mär")) return 3;
        if (mon.startsWith("apr")) return 4;
        if (mon.startsWith("mai")) return 5;
        if (mon.startsWith("jun")) return 6;
        if (mon.startsWith("jul")) return 7;
        if (mon.startsWith("aug")) return 8;
        if (mon.startsWith("sep")) return 9;
        if (mon.startsWith("okt")) return 10;
        if (mon.startsWith("nov")) return 11;
        if (mon.startsWith("dez")) return 12;
        throw new RuntimeException("Could not find month '" + monthStr + "'");
    }
}