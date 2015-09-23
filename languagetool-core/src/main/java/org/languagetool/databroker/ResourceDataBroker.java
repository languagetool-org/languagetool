/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.databroker;

import java.io.InputStream;
import java.net.URL;

import org.languagetool.JLanguageTool;

/**
 * Is responsible for getting the necessary resources for the grammar checker
 * library. Following directories are currently needed by a couple of classes:
 *
 * <ul style="list-type: circle">
 * <li>{@code /resource}</li>
 * <li>{@code /rules}</li>
 * </ul>
 *
 * This interface determines methods to obtain any contents from these
 * directories.
 * <p>
 *
 * Make sure that you never obtain any grammar checker resources by calling
 * {@code Object.class.getResource(String)} or {@code
 * Object.class.getResourceAsStream(String)} directly. If you would like to
 * obtain something from these directories do always use
 * {@link JLanguageTool#getDataBroker()} which provides proper methods for
 * reading the directories above.
 * <p>
 *
 * For example, if you want to get the {@link URL} of {@code
 * /rules/de/grammar.xml} just invoke
 * {@link ResourceDataBroker#getFromRulesDirAsUrl(String)} and pass {@code
 * /de/grammar.xml} as a string. Note: The {@code /rules} directory's name isn't
 * passed, because its name might have changed. The same usage does apply for the
 * {@code /resource} directory.
 *
 * @author PAX
 * @since 1.0.1
 */
public interface ResourceDataBroker {

  /**
   * The directory name of the {@code /resource} directory.
   */
  String RESOURCE_DIR = "/org/languagetool/resource";

  /**
   * The directory name of the {@code /rules} directory.
   */
  String RULES_DIR = "/org/languagetool/rules";

  /**
   * Gets any resource from the grammar checker's {@code /resource} directory.
   *
   * @param path Path to an item from the {@code /resource} directory.
   * @return An {@link URL} object to the requested item
   * @throws RuntimeException if path cannot be found
   */
  URL getFromResourceDirAsUrl(String path);
  
  /**
   * Checks if a resource in the grammar checker's {@code /resource} exists.
   * @param path Path to an item from the {@code /resource} directory.
   * @return {@code true} if the resource file exists.
   */
  boolean resourceExists(String path);
  
  /**
   * Checks if a resource in the grammar checker's {@code /rules} exists.
   * @param path Path to an item from the {@code /rules} directory.
   * @return {@code true} if the resource file exists.
   */
  boolean ruleFileExists(String path);
  
   /**
   * Gets any resource from the grammar checker's {@code /resource} directory.
   *
   * @param path Path to an item from the {@code /resource} directory.
   * @return An {@link InputStream} object to the requested item
   * @throws RuntimeException if path cannot be found
   */
   InputStream getFromResourceDirAsStream(String path);

  /**
   * Gets any resource from the grammar checker's {@code /rules} directory.
   *
   * @param path
   *            Path to an item from the {@code /rules} directory.
   * @return An {@link URL} object to the requested item
   * @throws RuntimeException if path cannot be found
   */
  URL getFromRulesDirAsUrl(String path);

  /**
   * Gets any resource from the grammar checker's {@code /rules} directory.
   *
   * @param path Path to an item from the {@code /rules} directory.
   * @return An {@link InputStream} object to the requested item
   * @throws RuntimeException if path cannot be found
   */
  InputStream getFromRulesDirAsStream(String path);

  /**
   * @return The currently set resource directory path as a string. Make sure
   *         that you comply with the following format when setting this value:
   *         <p>
   *         {@code /subdir/furtherdir/resourcedir}
   */
  String getResourceDir();

  /**
   * @return The currently set rules directory path as a string. Make sure
   *         that you comply with the following format when setting this value:
   *         <p>
   *         {@code /subdir/furtherdir/rulesdir}
   */
  String getRulesDir();

}
