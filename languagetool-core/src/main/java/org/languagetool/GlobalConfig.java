/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Objects;

/**
 * @since 4.6
 */
public class GlobalConfig {
  
  private String grammalecteServer;
  private String grammalecteUser;
  private String grammalectePassword;
  private File beolingusFile;
  private String nerUrl;

  public void setGrammalecteServer(String serverUrl) {
    grammalecteServer = serverUrl;
  }
  
  public void setGrammalecteUser(String user) {
    grammalecteUser = user;
  }
  
  public void setGrammalectePassword(String password) {
    grammalectePassword = password;
  }

  public void setBeolingusFile(File beolingusFile) {
    this.beolingusFile = beolingusFile;
  }

  /** External named entity recognizer service. */
  public void setNERUrl(String nerUrl) {
    this.nerUrl = nerUrl;
  }

  @Nullable
  public String getGrammalecteServer() {
    return grammalecteServer;
  }

  @Nullable
  public String getGrammalecteUser() {
    return grammalecteUser;
  }

  @Nullable
  public String getGrammalectePassword() {
    return grammalectePassword;
  }

  public File getBeolingusFile() {
    return beolingusFile;
  }

  @Nullable
  public String getNerUrl() {
    return nerUrl;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    GlobalConfig that = (GlobalConfig) o;
    return Objects.equals(grammalecteServer, that.grammalecteServer) &&
      Objects.equals(grammalecteUser, that.grammalecteUser) &&
      Objects.equals(grammalectePassword, that.grammalectePassword) &&
      Objects.equals(beolingusFile, that.beolingusFile) && Objects.equals(nerUrl, that.nerUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(grammalecteServer, beolingusFile, nerUrl);
  }
}
