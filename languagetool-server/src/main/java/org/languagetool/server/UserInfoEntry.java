/* LanguageTool, a natural language style checker
 * Copyright (C) 2018 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.server;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;

/**
 * An item from our users table, with some information about the user/limits/etc.
 * @since 4.3
 */
class UserInfoEntry {

  private final long id;
  private final String email;
  private final byte[] passwordHash;
  private final String addonToken;
  @Nullable
  private final String apiKey;
  private final Long userDictCacheSize;
  private final Long requestsPerDay;
  private final LimitEnforcementMode limitEnforcement;
  @Nullable
  private final Long managedAccounts;

  private final Date premiumFrom;
  private final Date premiumTo;

  @Nullable
  private final Long userGroup;


  UserInfoEntry(long id, String email, @Nullable Long userDictCacheSize, @Nullable Long requestsPerDay, @Nullable Integer limitEnforcement, @Nullable Long managedAccounts,
                @Nullable String passwordHash, @Nullable java.sql.Date premiumFrom, @Nullable java.sql.Date premiumTo, String addonToken, String apiKey, @Nullable Long userGroup) {
    this.id = id;
    this.email = email;
    this.addonToken = addonToken;
    this.apiKey = apiKey;
    this.userDictCacheSize = userDictCacheSize;
    this.requestsPerDay = requestsPerDay;
    this.managedAccounts = managedAccounts;
    this.limitEnforcement = LimitEnforcementMode.parse(limitEnforcement);
    this.passwordHash = passwordHash != null ? passwordHash.getBytes(StandardCharsets.UTF_8) : null;
    this.premiumFrom = premiumFrom;
    this.premiumTo = premiumTo;
    this.userGroup = userGroup;
  }

  @Nullable
  Date getPremiumFrom() {
    return premiumFrom;
  }

  @Nullable
  Date getPremiumTo() {
    return premiumTo;
  }


  @Nullable
  Long getUserDictCacheSize() {
    return userDictCacheSize;
  }

  long getUserId() {
    return id;
  }

  String getEmail() {
    return email;
  }

  @Nullable
  public Long getRequestsPerDay() {
    return requestsPerDay;
  }

  @NotNull
  public LimitEnforcementMode getLimitEnforcement() {
    return limitEnforcement;
  }

  @Nullable
  public byte[] getPasswordHash() {
    return passwordHash;
  }

  public boolean hasPremium() {
    Calendar startOfToday = Calendar.getInstance();
    startOfToday.set(Calendar.HOUR_OF_DAY, 0);
    startOfToday.set(Calendar.MINUTE, 0);
    startOfToday.set(Calendar.SECOND, 0);
    startOfToday.set(Calendar.MILLISECOND, 0);
    // premiumTo in database is inclusive, saved as date only -> time part set to zero
    return premiumFrom != null && (premiumTo == null || premiumTo.compareTo(startOfToday.getTime()) >= 0);
  }

  public String getAddonToken() {
    return addonToken;
  }

  @Nullable
  public String getApiKey() {
    return apiKey;
  }

  @Nullable
  public Long getManagedAccounts() {
    return managedAccounts;
  }

  @Nullable
  public Long getUserGroup() {
    return userGroup;
  }
}
