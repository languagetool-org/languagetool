/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2019 Fabian Richter
 *  * All rights reserved - not part of the Open Source edition
 *
 */

package org.languagetool.server;

import org.junit.Test;

import java.util.Calendar;
import java.sql.Date;

import static org.junit.Assert.*;

public class UserInfoEntryTest {

  private void setToStartOfDay(Calendar c) {
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);
  }

  public Date date(Calendar calendar) {
    return new Date(calendar.getTime().getTime());
  }

  @Test
  public void hasPremium() {
    Date premiumStart = new Date(0L);
    Calendar premiumEndToday = Calendar.getInstance();
    Calendar premiumEndYesterday = Calendar.getInstance();
    premiumEndYesterday.add(Calendar.DATE, -1);
    Calendar premiumEndTomorrow = Calendar.getInstance();
    premiumEndTomorrow.add(Calendar.DATE, 1);

    setToStartOfDay(premiumEndToday);
    setToStartOfDay(premiumEndYesterday);
    setToStartOfDay(premiumEndTomorrow);
    Long userGroup = null;
    UserInfoEntry userEndToday = new UserInfoEntry(1L, null, null, null, null, null, premiumStart, date(premiumEndToday), "foo", "foo", userGroup);
    UserInfoEntry userEndYesterday = new UserInfoEntry(1L, null, null, null,null,  null, premiumStart, date(premiumEndYesterday), "foo", "foo", userGroup);
    UserInfoEntry userEndTomorrow = new UserInfoEntry(1L, null, null,null,  null, null, premiumStart, date(premiumEndTomorrow), "foo", "foo", userGroup);

    assertTrue(userEndTomorrow.hasPremium());
    assertTrue(userEndToday.hasPremium());
    assertFalse(userEndYesterday.hasPremium());
  }
}
