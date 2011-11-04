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
package de.danielnaber.languagetool.databroker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.Permission;

import de.danielnaber.languagetool.JLanguageTool;

/**
 * Responsible for getting any items from the grammar checker's resource
 * directories. This default data broker assumes that they are accessible
 * directly via class-path and the directory names are like specified in:
 *
 * <ul style="list-type: circle">
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
 * <ul style="list-type: circle">
 * <li>{@code /res/grammarchecker/rulesdirname}</li>
 * <li>{@code /res/grammarchecker/resourcedirname}</li>
 * </ul>
 *
 * In this case you have to invoke the methods
 * {@link ResourceDataBroker#setRulesDir(String)} and
 * {@link ResourceDataBroker#setResourceDir(String)} with following arguments:
 *
 * <ul style="list-type: circle">
 * <li>{@code /res/grammarchecker/rulesdirname}</li>
 * <li>{@code /res/grammarchecker/resourcedirname}</li>
 * </ul>
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
 * @see ResourceDataBroker
 * @author PAX
 * @since 1.0.1
 */
public class DefaultResourceDataBroker implements ResourceDataBroker {

  /**
   * The directory's name of the grammar checker's resource directory. The
   * default value equals {@link ResourceDataBroker#RESOURCE_DIR}.
   */
  protected String resourceDir;

  /**
   * The directory's name of the grammar checker's rules directory. The
   * default value equals {@link ResourceDataBroker#RULES_DIR}.
   */
  protected String rulesDir;

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
   * @param resourceDir
   *            The directory's name of the grammar checker's resource
   *            directory. The default value equals
   *            {@link ResourceDataBroker#RESOURCE_DIR}.
   * @param rulesDir
   *            The directory's name of the grammar checker's rules directory.
   *            The default value equals
   *            {@link ResourceDataBroker#RULES_DIR}.
   */
  public DefaultResourceDataBroker(final String resourceDir, final String rulesDir) {
    this.setResourceDir(resourceDir);
    this.setRulesDir(rulesDir);
  }

  /**
   * See:
   * {@link ResourceDataBroker#getFromResourceDirAsStream(java.lang.String)}
   *
   * @param path
   *            The relative path to the item inside of the {@code /resource}
   *            directory. Please start your path information with {@code /}
   *            because it will be concatenated with the directory's name:
   *            /resource<b>/yourpath</b>.
   * @return An {@link InputStream} object to the requested item or {@code
   *         null} if it wasn't found.
   */
  @Override
  public InputStream getFromResourceDirAsStream(final String path) {
    final String completePath = this.getCompleteResourceUrl(path);
    final InputStream resourceAsStream = ResourceDataBroker.class.getResourceAsStream(completePath);
    assertNotNull(resourceAsStream, path, completePath);
    return resourceAsStream;
  }

  /**
   * See:
   * {@link ResourceDataBroker#getFromResourceDirAsUrl(java.lang.String)}
   *
   * @param path
   *            The relative path to the item inside of the {@code /resource}
   *            directory. Please start your path information with {@code /}
   *            because it will be concatenated with the directory's name:
   *            /resource<b>/yourpath</b>.
   * @return An {@link URL} object to the requested item or {@code null} if it
   *         wasn't found.
   */
  @Override
  public URL getFromResourceDirAsUrl(final String path) {
    final String completePath = this.getCompleteResourceUrl(path);
    final URL resource = ResourceDataBroker.class.getResource(completePath);
    assertNotNull(resource, path, completePath);
    return getFixedJarURL(resource);
  }

  /**
   * Concatenates the passed resource path with the currently set {@code
   * resource} directory path.
   *
   * @param path
   *            The relative path to a resource item inside of the {@code
   *            resource} directory.
   * @return The full relative path to the resource including the path to the
   *         {@code resource} directory.
   */
  private String getCompleteResourceUrl(final String path) {
    final StringBuilder completePath = new StringBuilder(this.getResourceDir());

    if (!this.getResourceDir().endsWith("/") && !(path.charAt(0)=='/')) {
      completePath.append('/');
    }

    if (this.getResourceDir().endsWith("/") && (path.charAt(0)=='/')
            && path.length() > 1) {
      completePath.append(path.substring(1));
    } else {
      completePath.append(path);
    }

    return completePath.toString();
  }

  /**
   * See:
   * {@link ResourceDataBroker#getFromRulesDirAsStream(java.lang.String)}
   *
   * @param path
   *            The relative path to the item inside of the {@code /rules}
   *            directory. Please start your path information with {@code /}
   *            because it will be concatenated with the directory's name:
   *            /rules<b>/yourpath</b>.
   * @return An {@link InputStream} object to the requested item or {@code
   *         null} if it wasn't found.
   */
  @Override
  public InputStream getFromRulesDirAsStream(final String path) {
    final String completePath = this.getCompleteRulesUrl(path);
    final InputStream resourceAsStream = ResourceDataBroker.class.getResourceAsStream(completePath);
    assertNotNull(resourceAsStream, path, completePath);
    return resourceAsStream;
  }

  /**
   * See: {@link ResourceDataBroker#getFromRulesDirAsUrl(java.lang.String)}
   *
   * @param path
   *            The relative path to the item inside of the {@code /rules}
   *            directory. Please start your path information with {@code /}
   *            because it will be concatenated with the directory's name:
   *            /rules<b>/yourpath</b>.
   * @return An {@link URL} object to the requested item or {@code null} if it
   *         wasn't found.
   */
  @Override
  public URL getFromRulesDirAsUrl(final String path) {
    final String completePath = this.getCompleteRulesUrl(path);
    final URL resource = ResourceDataBroker.class.getResource(completePath);
    assertNotNull(resource, path, completePath);
    return getFixedJarURL(resource);
  }

  private void assertNotNull(Object object, String path, String completePath) {
    if (object == null) {
      throw new RuntimeException("Path " + path + " not found in class path at " + completePath);
    }
  }

  /**
   * Concatenates the passed resource path with the currently set {@code
   * rules} directory path.
   *
   * @param path
   *            The relative path to a resource item inside of the {@code
   *            rules} directory.
   * @return The full relative path to the resource including the path to the
   *         {@code rules} directory.
   */
  private String getCompleteRulesUrl(final String path) {
    final StringBuilder completePath = new StringBuilder(this.getRulesDir());

    if (!this.getRulesDir().endsWith("/") && !(path.charAt(0)=='/')) {
      completePath.append('/');
    }

    if (this.getRulesDir().endsWith("/") && (path.charAt(0)=='/') && path.length() > 1) {
      completePath.append(path.substring(1));
    } else {
      completePath.append(path);
    }

    return completePath.toString();
  }

  /**
   * @return The directory's name of the grammar checker's resource directory.
   *         The default value equals
   *         {@link ResourceDataBroker#RESOURCE_DIR}.
   */
  @Override
  public String getResourceDir() {
    return this.resourceDir;
  }

  /**
   * @param resourceDir
   *            The directory's name of the grammar checker's resource
   *            directory. The default value was
   *            {@link ResourceDataBroker#RESOURCE_DIR}. Please let this
   *            string start with {@code '/'} and use this character as path
   *            separator. Don't set this character to the string's end. Valid
   *            example value: {@code /subdir/furtherdir/resourcedir}.
   */
  @Override
  public void setResourceDir(final String resourceDir) {
    this.resourceDir = (resourceDir == null) ? "" : resourceDir;
  }

  /**
   * @return The directory's name of the grammar checker's rules directory.
   *         The default value equals {@link ResourceDataBroker#RULES_DIR}.
   */
  @Override
  public String getRulesDir() {
    return this.rulesDir;
  }

  /**
   * @param rulesDir
   *            The directory's name of the grammar checker's rules directory.
   *            The default value was {@link ResourceDataBroker#RULES_DIR}.
   *            Please let this string start with {@code '/'} and use this
   *            character as path separator. Don't set this character to the
   *            string's end. Valid example value: {@code
   *            /subdir/furtherdir/rulesdir}.
   */
  @Override
  public void setRulesDir(final String rulesDir) {
    this.rulesDir = (rulesDir == null) ? "" : rulesDir;
  }

  /**
   * Fixes the getResource bug if you want to obtain any resource from a JAR file under Java
   * 1.5.0_16 Webstart. (Workaround by {@code mevanclark} from http://forums.sun.com)
   *
   * @param url The {@link URL} to be fixed.
   * @return The fixed version if necessary.
   */
  private static URL getFixedJarURL(URL url) {
    if (url == null) {
      return url;
    }

    final String originalURLProtocol = url.getProtocol();
    if (!"jar".equalsIgnoreCase(originalURLProtocol)) {
      return url;
    }

    final String originalURLString = url.toString();
    final int bangSlashIndex = originalURLString.indexOf("!/");
    if (bangSlashIndex > -1) {
      return url;
    }

    final String originalURLPath = url.getPath();
    final URLConnection urlConnection;
    try {
      urlConnection = url.openConnection();
      if (urlConnection == null) {
        throw new IOException("urlConnection is null");
      }
    } catch (IOException e) {
      return url;
    }

    final Permission urlConnectionPermission;
    try {
      urlConnectionPermission = urlConnection.getPermission();
      if (urlConnectionPermission == null) {
        throw new IOException("urlConnectionPermission is null");
      }
    } catch (IOException e) {
      return url;
    }

    final String urlConnectionPermissionName = urlConnectionPermission.getName();
    if (urlConnectionPermissionName == null) {
      return url;
    }

    final File file = new File(urlConnectionPermissionName);
    if (!file.exists()) {
      return url;
    }

    try {
      final String newURLStr = "jar:" + file.toURI().toURL().toExternalForm() + "!/" + originalURLPath;
      url = new URL(newURLStr);
    } catch (MalformedURLException e) {
      return url;
    }

    return url;
  }

}
