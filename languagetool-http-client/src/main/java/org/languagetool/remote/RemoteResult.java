/* LanguageTool, a natural language style checker
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.remote;

import org.jetbrains.annotations.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The result of checking text on a remote LanguageTool server.
 * @since 3.4
 */
public class RemoteResult {

  private final String language;
  private final String languageCode;
  private final String languageDetectedCode;
  private final String languageDetectedName;
  private final List<RemoteRuleMatch> matches;
  private final RemoteServer remoteServer;

  RemoteResult(String language, String languageCode, @Nullable String languageDetectedCode, @Nullable String languageDetectedName, List<RemoteRuleMatch> matches, RemoteServer remoteServer) {
    this.language = Objects.requireNonNull(language);
    this.languageCode = Objects.requireNonNull(languageCode);
    this.languageDetectedCode = languageDetectedCode;
    this.languageDetectedName = languageDetectedName;
    this.matches = Collections.unmodifiableList(Objects.requireNonNull(matches));
    this.remoteServer = Objects.requireNonNull(remoteServer);
  }

  public List<RemoteRuleMatch> getMatches() {
    return matches;
  }

  public String getLanguage() {
    return language;
  }

  public String getLanguageCode() {
    return languageCode;
  }

  public RemoteServer getRemoteServer() {
    return remoteServer;
  }

  /**
   * @since 4.3
   */
  @Nullable
  public String getLanguageDetectedCode() {
    return languageDetectedCode;
  }

  /**
   * @since 4.3
   */
  @Nullable
  public String getLanguageDetectedName() {
    return languageDetectedName;
  }

  @Override
  public String toString() {
    return matches.toString();
  }

}
