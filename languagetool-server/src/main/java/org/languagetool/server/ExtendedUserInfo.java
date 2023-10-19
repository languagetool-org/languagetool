/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Fabian Richter
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.sql.Date;
import java.sql.Timestamp;

/**
  Used via Jackson-databind + myBatis -&gt; return info from DB via JSON in /users/me route
 @see ApiV2
 */
public class ExtendedUserInfo {
  public Long id;

  public String addon_token;
  public String api_key;
  public String email;
  public String name;
  public Date premium_from;
  public Date premium_to;
  public Timestamp cancel_date;
  public Long subscription_months;
  public String geo_ip_country;
  public Long managed_accounts;

  // for jackson-databind deserialization
  public ExtendedUserInfo() {}

  // for myBatis deserialization
  public ExtendedUserInfo(String addon_token, String api_key, String email, String name, Date premium_from, Date premium_to, Timestamp cancel_date, Long subscription_months, String geo_ip_country, Long managed_accounts) {

    this.addon_token = addon_token;
    this.api_key = api_key;
    this.email = email;
    this.name = name;
    this.premium_from = premium_from;
    this.premium_to = premium_to;
    this.cancel_date = cancel_date;
    this.subscription_months = subscription_months;
    this.geo_ip_country = geo_ip_country;
    this.managed_accounts = managed_accounts;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
      .append("id", id)
      .append("addon_token", addon_token)
      .append("api_key", api_key)
      .append("email", email)
      .append("name", name)
      .append("premium_from", premium_from)
      .append("premium_to", premium_to)
      .append("cancel_date", cancel_date)
      .append("subscription_months", subscription_months)
      .append("geo_ip_country", geo_ip_country)
      .append("managed_accounts", managed_accounts)
      .toString();
  }


  // getters for tests (i.e. org.hamcrest.Matchers.hasProperty)

  public Long getId() {
    return id;
  }

  public String getAddon_token() {
    return addon_token;
  }

  public String getApi_key() {
    return api_key;
  }

  public String getEmail() {
    return email;
  }

  public String getName() {
    return name;
  }

  public Date getPremium_from() {
    return premium_from;
  }

  public Date getPremium_to() {
    return premium_to;
  }

  public Timestamp getCancel_date() {
    return cancel_date;
  }

  public Long getSubscription_months() {
    return subscription_months;
  }

  public String getGeo_ip_country() {
    return geo_ip_country;
  }

  public Long getManaged_accounts() {
    return managed_accounts;
  }
}
