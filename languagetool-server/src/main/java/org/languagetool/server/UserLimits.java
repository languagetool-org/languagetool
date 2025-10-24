/* LanguageTool, a natural language style checker
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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

import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Objects;

/**
 * @since 4.0
 */
class UserLimits {

  private static final Logger logger = LoggerFactory.getLogger(UserLimits.class);
  
  @Nullable
  private final UserInfoEntry account;

  private final int maxTextLength;
  private final long maxCheckTimeMillis;
  // number of words from custom dictionaries that are cached, enabled only for selected users
  private final boolean hasPremium;
  private final Long dictionaryCacheSize;
  private final Long premiumUid;
  private boolean skipLimits;

  private final Long requestsPerDay;
  private final LimitEnforcementMode limitEnforcementMode;

  @Getter
  private final JwtContent jwtContent;

  static UserLimits getDefaultLimits(HTTPServerConfig config) {
    if (config.premiumAlways) {
      return new UserLimits(config.getMaxTextLengthPremium(), config.getMaxCheckTimeMillisAnonymous(), 1L, true);
    } else {
      return new UserLimits(config.getMaxTextLengthAnonymous(), config.getMaxCheckTimeMillisAnonymous(), null, false);
    }
  }

  /**
   * @deprecated Use getLimitsByApiKey() instead
   */
  @Deprecated
  static UserLimits getLimitsFromUserAccount(HTTPServerConfig config, @NotNull String username, @NotNull String password) {
    UserInfoEntry entry = DatabaseAccess.getInstance().getUserInfoWithPassword(username, password);
    if (entry == null) { // transparent fallback to anonymous user if DB down
      return getUserLimitsFromWhitelistOrDefault(config, username);
    } if (entry.hasPremium() || config.isPremiumAlways()) {
      logger.info("Access via username/password for " + username);
      return new UserLimits(config.getMaxTextLengthPremium(), config.getMaxCheckTimeMillisPremium(), entry.getUserId(), true, entry.getUserDictCacheSize(), entry.getRequestsPerDay(), entry.getLimitEnforcement(), entry, JwtContent.NONE);
    } else {
      logger.info("Non-premium access via username/password for " + username);
      return new UserLimits(config.getMaxTextLengthLoggedIn(), config.getMaxCheckTimeMillisLoggedIn(), entry.getUserId(), false, entry.getUserDictCacheSize(), entry.getRequestsPerDay(), entry.getLimitEnforcement(), entry, JwtContent.NONE);
    }
  }

  /**
   * Get limits from the api key itself, database access is needed.
   */
  public static UserLimits getLimitsByApiKey(HTTPServerConfig config, String username, String apiKey) {
    DatabaseAccess db = DatabaseAccess.getInstance();
    UserInfoEntry data = db.getUserInfoWithApiKey(username, apiKey);
    if (data == null) { // transparent fallback to anonymous user if DB down
      return getUserLimitsFromWhitelistOrDefault(config, username);
    } else if (data.hasPremium() || config.isPremiumAlways()) {
      return new UserLimits(config.getMaxTextLengthPremium(), config.getMaxCheckTimeMillisPremium(), data.getUserId(), true, data.getUserDictCacheSize(), data.getRequestsPerDay(), data.getLimitEnforcement(), data, JwtContent.NONE);
    } else {
      return new UserLimits(config.getMaxTextLengthLoggedIn(), config.getMaxCheckTimeMillisLoggedIn(), data.getUserId(), false, data.getUserDictCacheSize(), data.getRequestsPerDay(), data.getLimitEnforcement(), data, JwtContent.NONE);
    }
  }

  /**
   * Get limits from the addon token, needs DB access
   */
  public static UserLimits getLimitsByAddonToken(HTTPServerConfig config, String username, String addonToken) {
    DatabaseAccess db = DatabaseAccess.getInstance();
    UserInfoEntry data = db.getUserInfoWithAddonToken(username, addonToken);
    if (data == null) { // transparent fallback to anonymous user if DB down
      return getUserLimitsFromWhitelistOrDefault(config, username);
    } if (data.hasPremium() || config.isPremiumAlways()) {
      return new UserLimits(config.getMaxTextLengthPremium(), config.getMaxCheckTimeMillisPremium(), data.getUserId(), true, data.getUserDictCacheSize(), data.getRequestsPerDay(), data.getLimitEnforcement(), data, JwtContent.NONE);
    } else {
      return new UserLimits(config.getMaxTextLengthLoggedIn(), config.getMaxCheckTimeMillisLoggedIn(), data.getUserId(), false, data.getUserDictCacheSize(), data.getRequestsPerDay(), data.getLimitEnforcement(), data, JwtContent.NONE);
    }
  }

  public static UserLimits getLimitsWithJwtToken(HTTPServerConfig config, String authHeader, String username, String addonToken) {

    DatabaseAccess db = DatabaseAccess.isReady() ? DatabaseAccess.getInstance() : null;
    UserInfoEntry data = (db != null && !Strings.isNullOrEmpty(username) && !Strings.isNullOrEmpty(addonToken)) ? db.getUserInfoWithAddonToken(username, addonToken) : null;
    JwtContent jwtContent = db != null ? db.validateAuthHeader(authHeader) : JwtContent.NONE;

    // User is premium
    if (data != null && (data.hasPremium() || config.isPremiumAlways())) {
      return new UserLimits(config.getMaxTextLengthPremium(), config.getMaxCheckTimeMillisPremium(), data.getUserId(), true, data.getUserDictCacheSize(), data.getRequestsPerDay(), data.getLimitEnforcement(), data, jwtContent);
    }

    // User send an invalid premium-token
    if (!jwtContent.isValid() && jwtContent.isPremium()) {
      Long userId = (data != null) ? data.getUserId() : 0L;
      if (userId == 0) {
        return username != null ? getUserLimitsFromWhitelistOrDefault(config, username) : getDefaultLimits(config);
      }
      // we don't want to block access for anyone based on the token
      return getDefaultLimits(config);
    }

    // At this point we already know
    // 1. User is not premium in our DB
    // 2. User has not sent an invalid premium token

    // User is not premium in our DB but has a token that decides if user is premium
    if (data != null) {
      if (jwtContent.isPremium()) {
        return new UserLimits(config.getMaxTextLengthPremium(), config.getMaxCheckTimeMillisPremium(), data.getUserId(), true, data.getUserDictCacheSize(), data.getRequestsPerDay(), data.getLimitEnforcement(), data, jwtContent);
      } else {
        return new UserLimits(config.getMaxTextLengthLoggedIn(), config.getMaxCheckTimeMillisLoggedIn(), data.getUserId(), false, data.getUserDictCacheSize(), data.getRequestsPerDay(), data.getLimitEnforcement(), data, jwtContent);
      }
    }

    // Anonymous user with valid token
    if (jwtContent.isValid()) {
      return new UserLimits(config.getMaxTextLengthPremium(), config.getMaxCheckTimeMillisPremium(), null, jwtContent.isPremium(), null, null, null, null, jwtContent);
    }
    return username != null ? getUserLimitsFromWhitelistOrDefault(config, username) : getDefaultLimits(config);
  }

  private static UserLimits getUserLimitsFromWhitelistOrDefault(HTTPServerConfig config, String username) {
    if (config.getRequestLimitWhitelistUsers() == null) {
      return getDefaultLimits(config);
    }
    String mailDomain = username.substring(username.indexOf('@') + 1);
    boolean isWhitelisted = false;
    for (String whitelisted : config.getRequestLimitWhitelistUsers()) {
      if (whitelisted.equals(username) || whitelisted.equals(mailDomain)) {
        isWhitelisted = true;
        break;
      }
    }
    if (isWhitelisted) {
      return new UserLimits(config.getMaxTextLengthPremium(), config.getMaxCheckTimeMillisPremium(), null, true);
    } else {
      return getDefaultLimits(config);
    }
  }


  private UserLimits(int maxTextLength, long maxCheckTimeMillis, Long premiumUid, boolean hasPremium) {
    this(maxTextLength, maxCheckTimeMillis, premiumUid, hasPremium, null, null, null);
  }

  private UserLimits(int maxTextLength, long maxCheckTimeMillis, Long premiumUid, boolean hasPremium, Long dictCacheSize, Long requestsPerDay, LimitEnforcementMode limitEnforcement) {
    this(maxTextLength, maxCheckTimeMillis, premiumUid, hasPremium, dictCacheSize, requestsPerDay, limitEnforcement, null, new JwtContent(false, false, Collections.emptyMap()));
  }

  private UserLimits(int maxTextLength, long maxCheckTimeMillis, Long premiumUid, boolean hasPremium, Long dictCacheSize, Long requestsPerDay, LimitEnforcementMode limitEnforcement, UserInfoEntry account, JwtContent jwtContent) {
    this.maxTextLength = maxTextLength;
    this.maxCheckTimeMillis = maxCheckTimeMillis;
    this.premiumUid = premiumUid;
    this.hasPremium = hasPremium;
    this.dictionaryCacheSize = dictCacheSize;
    this.requestsPerDay = requestsPerDay;
    this.limitEnforcementMode = limitEnforcement != null ? limitEnforcement : LimitEnforcementMode.DISABLED;
    this.account = account;
    this.jwtContent = jwtContent;
  }

  /**
   * Special case for internal use to skip all limits.
   */
  UserLimits(boolean skipLimits) {
    this(0, 0, -1L, true);
    this.skipLimits = skipLimits;
  }

  int getMaxTextLength() {
    return maxTextLength;
  }

  long getMaxCheckTimeMillis() {
   return maxCheckTimeMillis;
  }

  boolean getSkipLimits() {
    return skipLimits;
  }

  @Nullable
  Long getPremiumUid() {
    return premiumUid;
  }

  public boolean hasPremium() {
    return hasPremium;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
      .append("premiumUid", premiumUid)
      .append("maxTextLength", maxTextLength)
      .append("maxCheckTimeMillis", maxCheckTimeMillis)
      .append("dictCacheSize", dictionaryCacheSize)
      .append("requestsPerDay", requestsPerDay)
      .append("limitEnforcement", limitEnforcementMode)
      .build();
  }

  public Long getDictCacheSize() {
    return dictionaryCacheSize;
  }

  public Long getRequestsPerDay() {
    return requestsPerDay;
  }

  public LimitEnforcementMode getLimitEnforcementMode() {
    return limitEnforcementMode;
  }

  @Nullable
  UserInfoEntry getAccount() {
    return account;
  }

  static class Account {

    private final String username;
    private final String password;

    Account(String username, String password) {
      this.username = Objects.requireNonNull(username);
      this.password = Objects.requireNonNull(password);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Account account = (Account) o;
      return Objects.equals(username, account.username) && Objects.equals(password, account.password);
    }

    @Override
    public int hashCode() {
      return Objects.hash(username, password);
    }
  }

}
