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

import java.util.Objects;
import java.util.Optional;

/**
 * Information about the remote server as returned by the server's XML response.
 * @since 3.4
 */
public class RemoteServer {
  
  private final String software;
  private final String version;
  private final String buildDate;
  
  RemoteServer(String software, String version, String buildDate) {
    this.software = Objects.requireNonNull(software);
    this.version = Objects.requireNonNull(version);
    this.buildDate = buildDate;
  }

  /**
   * @return the software running on the server, usually {@code LanguageTool}
   */
  public String getSoftware() {
    return software;
  }

  /**
   * @return the version running on the server, might be something like {@code 3.4-SNAPSHOT} or {@code 3.4}
   */
  public String getVersion() {
    return version;
  }

  /**
   * @return the build date of the version or null (in case this isn't a real build but runs in an IDE etc.)
   */
  public Optional<String> getBuildDate() {
    return Optional.ofNullable(buildDate);
  }

  @Override
  public String toString() {
    return software + "/" + version + "/" + buildDate;
  }

}
