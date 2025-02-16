/*
 * LanguageTool, a natural language style checker
 * Copyright (C) 2025 Stefan Viol (https://stevio.de)
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

package org.languagetool.tools.grpc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.rules.RuleMatch;

public class ProtoHelper {

  @NotNull
  public static String nullAsEmpty(@Nullable String s) {
    return s != null ? s : "";
  }

  @Nullable
  public static String emptyAsNull(String s) {
    if (s != null && s.isEmpty()) {
      return null;
    }
    return s;
  }

  public static String getUrl(RuleMatch m) {
    // URL can be attached to Rule or RuleMatch (or both); in Protobuf, only to RuleMatch
    // prefer URL from RuleMatch, default to empty string
    if (m.getUrl() != null) {
      return m.getUrl().toString();
    }
    if (m.getRule().getUrl() != null) {
      return m.getRule().getUrl().toString();
    }
    return "";
  }

}
