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
package org.languagetool.broker;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.JLanguageTool;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Responsible for getting any items from LanguageTool's resource
 * directories. This default data broker assumes that they are accessible
 * directly via class-path and the directory names are like specified in:
 *
 * <ul>
 * <li>{@link ResourceDataBroker#RESOURCE_DIR}</li>
 * <li>{@link ResourceDataBroker#RULES_DIR}</li>
 * </ul>
 * <p>
 *
 * If you'd like to determine another resource directory location this default
 * data broker provides proper methods.
 * Assuming your {@code /rules} and {@code /resource} directories are accessible
 * via class-path with following path information:
 *
 * <ul>
 * <li>{@code /res/grammarchecker/rulesdirname}</li>
 * <li>{@code /res/grammarchecker/resourcedirname}</li>
 * </ul>
 *
 * In this case you have to use the constructor with the following arguments:
 *
 * <ul>
 * <li>{@code /res/grammarchecker/rulesdirname}</li>
 * <li>{@code /res/grammarchecker/resourcedirname}</li>
 * </ul>
 * <p>
 *
 * Make sure that you never obtain any LanguageTool resources by calling
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
 * passed because its name might have changed. The same usage does apply for the
 * {@code /resource} directory.
 *
 * @see ResourceDataBroker
 * @author PAX
 * @since 1.0.1
 */
public class DefaultResourceDataBroker implements ResourceDataBroker {

  /**
   * The directory's name of LanguageTool's resource directory. The
   * default value equals {@link ResourceDataBroker#RESOURCE_DIR}.
   */
  private final String resourceDir;

  /**
   * The directory's name of LanguageTool's rules directory. The
   * default value equals {@link ResourceDataBroker#RULES_DIR}.
   */
  private final String rulesDir;

  /**
   * Instantiates this data broker with the default resource directory names
   * as specified in:
   *
   * <ul>
   * <li>{@link ResourceDataBroker#RESOURCE_DIR}</li>
   * <li>{@link ResourceDataBroker#RULES_DIR}</li>
   * </ul>
   */
  public DefaultResourceDataBroker() {
    this(ResourceDataBroker.RESOURCE_DIR, ResourceDataBroker.RULES_DIR);
  }

  /**
   * Instantiates this data broker with the passed resource directory names.
   *
   * @param resourceDir The directory's name of LanguageTool's resource
   *  directory. The default value equals {@link ResourceDataBroker#RESOURCE_DIR}.
   * @param rulesDir The directory's name of LanguageTool's rules directory.
   *  The default value equals {@link ResourceDataBroker#RULES_DIR}.
   */
  public DefaultResourceDataBroker(String resourceDir, String rulesDir) {
    this.resourceDir = (resourceDir == null) ? "" : resourceDir;
    this.rulesDir = (rulesDir == null) ? "" : rulesDir;
  }

  /**
   * See:
   * {@link ResourceDataBroker#getFromResourceDirAsStream(String)}
   *
   * @param path
   *            The relative path to the item inside of the {@code /resource}
   *            directory. Please start your path information with {@code /}
   *            because it will be concatenated with the directory's name:
   *            /resource<b>/yourpath</b>.
   * @return An {@link InputStream} object to the requested item
   * @throws RuntimeException if path cannot be found
   */
  @NotNull
  @Override
  public InputStream getFromResourceDirAsStream(String path) {
    String completePath = getCompleteResourceUrl(path);
    InputStream resourceAsStream = getAsStream(completePath);
    assertNotNull(resourceAsStream, path, completePath);
    return resourceAsStream;
  }

  /**
   * See:
   * {@link ResourceDataBroker#getFromResourceDirAsStream(String)}
   *
   * @param path The relative path to the item inside of the {@code /resource}, e.g. {@code /xx/filename}
   * @return An list of strings, one per line
   * @throws RuntimeException if path cannot be found
   * @since 4.9
   */
  @NotNull
  public List<String> getFromResourceDirAsLines(String path) {
    List<String> lines = new ArrayList<>();
    try (InputStream stream = getFromResourceDirAsStream(path);
         InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
         BufferedReader br = new BufferedReader(reader)
    ) {
      String line;
      while ((line = br.readLine()) != null) {
        lines.add(line);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return lines;
  }

  /**
   * {@inheritDoc}
   */
  @Nullable
  @Override
  public InputStream getAsStream(String path) {
    return ResourceDataBroker.class.getResourceAsStream(path);
  }

  /**
   * {@inheritDoc}
   */
  @Nullable
  @Override
  public URL getAsURL(String path) {
    return ResourceDataBroker.class.getResource(path);
  }

  /**
   * {@inheritDoc}
   */
  @Nullable
  @Override
  public List<URL> getAsURLs(String path) {
    try {
      return Collections.list(ResourceDataBroker.class.getClassLoader().getResources(path));
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * See:
   * {@link ResourceDataBroker#getFromResourceDirAsUrl(String)}
   * @param path The relative path to the item inside of the {@code /resource}
   *            directory. Please start your path information with {@code /}
   *            because it will be concatenated with the directory's name:
   *            /resource<b>/yourpath</b>.
   * @return An {@link URL} object to the requested item
   * @throws RuntimeException if path cannot be found
   */
  @NotNull
  @Override
  public URL getFromResourceDirAsUrl(String path) {
    String completePath = getCompleteResourceUrl(path);
    URL resource = getAsURL(completePath);
    assertNotNull(resource, path, completePath);
    return resource;
  }

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  public List<URL> getFromResourceDirAsUrls(String path) {
    String completePath = getCompleteResourceUrl(path);
    // remove leading slash character for successful result because we are searching resource from classloader
    List<URL> resources = getAsURLs(completePath.substring(1));
    assertNotNull(resources, path, completePath);
    return resources;
  }

  /**
   * Concatenates the passed resource path with the currently set {@code
   * resource} directory path.
   * @param path The relative path to a resource item inside of the {@code resource} directory.
   * @return The full relative path to the resource including the path to the
   *         {@code resource} directory.
   */
  private String getCompleteResourceUrl(String path) {
    return appendPath(resourceDir, path);
  }

  /**
   * See:
   * {@link ResourceDataBroker#getFromRulesDirAsStream(String)}
   *
   * @param path The relative path to the item inside of the {@code /rules}
   *  directory. Please start your path information with {@code /} because it
   *  will be concatenated with the directory's name: /rules<b>/yourpath</b>.
   * @return An {@link InputStream} object to the requested item
   * @throws RuntimeException if path cannot be found
   */
  @NotNull
  @Override
  public InputStream getFromRulesDirAsStream(String path) {
    String completePath = getCompleteRulesUrl(path);
    InputStream resourceAsStream = getAsStream(completePath);
    assertNotNull(resourceAsStream, path, completePath);
    return resourceAsStream;
  }

  /**
   * See: {@link ResourceDataBroker#getFromRulesDirAsUrl(String)}
   * @param path The relative path to the item inside of the {@code /rules}
   *  directory. Please start your path information with {@code /} because it
   *  will be concatenated with the directory's name: /rules<b>/yourpath</b>.
   * @return An {@link URL} object to the requested item
   * @throws RuntimeException if path cannot be found
   */
  @NotNull
  @Override
  public URL getFromRulesDirAsUrl(String path) {
    String completePath = getCompleteRulesUrl(path);
    URL resource = getAsURL(completePath);
    assertNotNull(resource, path, completePath);
    return resource;
  }

  private void assertNotNull(Object object, String path, String completePath) {
    if (object == null) {
      throw new RuntimeException("Path " + path + " not found in class path at " + completePath);
    }
  }

  /**
   * Concatenates the passed resource path with the currently set {@code
   * rules} directory path.
   * @param path The relative path to a resource item inside of the {@code rules} directory.
   * @return The full relative path to the resource including the path to the {@code rules} directory.
   */
  private String getCompleteRulesUrl(String path) {
    return appendPath(rulesDir, path);
  }

  private String appendPath(String baseDir, String path) {
    StringBuilder completePath = new StringBuilder(baseDir);
    if (!this.rulesDir.endsWith("/") && !path.startsWith("/")) {
      completePath.append('/');
    }
    if (this.rulesDir.endsWith("/") && path.startsWith("/") && path.length() > 1) {
      completePath.append(path.substring(1));
    } else {
      completePath.append(path);
    }
    return completePath.toString();
  }

  /**
   * See: {@link ResourceDataBroker#resourceExists(String)}
   * 
   * Checks if a resource in LanguageTool's {@code /resource} exists.
   * @param path Path to an item from the {@code /resource} directory.
   * @return {@code true} if the resource file exists.
   */
  @Override
  public boolean resourceExists(String path) {
    String completePath = getCompleteResourceUrl(path);
    return getAsURL(completePath) != null;
  }
  
  /**
   * See: {@link ResourceDataBroker#ruleFileExists(String)}
   * 
   * Checks if a resource in LanguageTool's {@code /rules} exists.
   * @param path Path to an item from the {@code /rules} directory.
   * @return {@code true} if the resource file exists.
   */
  @Override
  public boolean ruleFileExists(String path) {
    String completePath = getCompleteRulesUrl(path);
    return getAsURL(completePath) != null;
  }

  /**
   * @return The directory's name of LanguageTool's resource directory.
   *         The default value equals {@link ResourceDataBroker#RESOURCE_DIR}.
   */
  @Override
  public String getResourceDir() {
    return resourceDir;
  }

  /**
   * @return The directory's name of the grammar checker's rules directory.
   *         The default value equals {@link ResourceDataBroker#RULES_DIR}.
   */
  @Override
  public String getRulesDir() {
    return rulesDir;
  }

  /**
   * {@inheritDoc}
   * @exception NullPointerException if <code>baseName</code> or <code>locale</code> is <code>null</code>
   * @exception MissingResourceException if no resource bundle for the specified base name can be found
   */
  @Override
  public ResourceBundle getResourceBundle(String baseName, Locale locale) {
    return ResourceBundle.getBundle(baseName, locale);
  }
}
