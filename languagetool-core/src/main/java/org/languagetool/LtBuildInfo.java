/*
 * LanguageTool, a natural language style checker
 * Copyright (c) 2024.  Stefan Viol (https://stevio.de)
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  USA
 */

package org.languagetool;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public enum LtBuildInfo {

  OS("/git.properties"),
  PREMIUM("/git-premium.properties");

  private final Logger logger = LoggerFactory.getLogger(LtBuildInfo.class);

  @Getter
  @Nullable
  private final String buildDate;
  @Getter
  @Nullable
  private final String shortGitId;
  @Getter
  @Nullable
  private final String version;

  LtBuildInfo(String gitPropertiesFilePath) {
    InputStream in = JLanguageTool.getDataBroker().getAsStream(gitPropertiesFilePath);
    Properties gitProperties = null;
    if (in != null) {
      gitProperties = new Properties();
      try {
        gitProperties.load(in);
      } catch (IOException e) {
        logger.warn("Failed to read {}", gitPropertiesFilePath, e);
      }
    }

    if (gitProperties != null) {
      OffsetDateTime offsetDateTime = OffsetDateTime.parse(gitProperties.getProperty("git.build.time"), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXX"));
      this.buildDate = offsetDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z"));
      this.shortGitId = gitProperties.getProperty("git.commit.id.abbrev");
      this.version = gitProperties.getProperty("git.build.version");
    } else {
      this.buildDate = null;
      this.shortGitId = null;
      this.version = null;
    }
  }
}
