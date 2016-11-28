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
package org.languagetool.gui;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

/**
 * Helper class to store configuration
 *
 * @author Panagiotis Minos
 * @since 3.4
 */
class LocalStorage {

  private static final String VENDOR_ID = "languagetool.org";
  private static final String APPLICATION_ID = "LanguageTool";
  
  private final File directory;

  LocalStorage() {
    String userHome = null;
    try {
      userHome = System.getProperty("user.home");
    } catch (SecurityException ex) {
    }
    if (userHome == null) {
      directory = null;
    } else if (SystemUtils.IS_OS_WINDOWS) {
      File appDataDir = null;
      try {
        String appData = System.getenv("APPDATA");
        if (!StringUtils.isEmpty(appData)) {
          appDataDir = new File(appData);
        }
      } catch (SecurityException ex) {
      }
      if (appDataDir != null && appDataDir.isDirectory()) {
        String path = VENDOR_ID + "\\" + APPLICATION_ID + "\\";
        directory = new File(appDataDir, path);
      } else {
        String path = "Application Data\\" + VENDOR_ID + "\\" + APPLICATION_ID + "\\";
        directory = new File(userHome, path);
      }
    } else if (SystemUtils.IS_OS_LINUX) {
      File appDataDir = null;
      try {
        String xdgConfigHome = System.getenv("XDG_CONFIG_HOME");
        if (!StringUtils.isEmpty(xdgConfigHome)) {
          appDataDir = new File(xdgConfigHome);
          if (!appDataDir.isAbsolute()) {
            //https://specifications.freedesktop.org/basedir-spec/basedir-spec-latest.html
            //All paths set in these environment variables must be absolute.
            //If an implementation encounters a relative path in any of these
            //variables it should consider the path invalid and ignore it.
            appDataDir = null;
          }
        }
      } catch (SecurityException ex) {
      }
      if (appDataDir != null && appDataDir.isDirectory()) {
        String path = APPLICATION_ID + "/";
        directory = new File(appDataDir, path);
      } else {
        String path = ".config/" + APPLICATION_ID + "/";
        directory = new File(userHome, path);
      }
    } else if (SystemUtils.IS_OS_MAC_OSX) {
      String path = "Library/Application Support/" + APPLICATION_ID + "/";
      directory = new File(userHome, path);
    } else {
      String path = "." + APPLICATION_ID + "/";
      directory = new File(userHome, path);
    }
    if (directory != null && !directory.exists()) {
      directory.mkdirs();
    }
  }

  void saveProperty(String name, Object obj) {
    if (directory == null) {
      return;
    }
    synchronized(directory) {
      try (XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(
            new FileOutputStream(new File(directory, name))))) {
        encoder.writeObject(obj);
      } catch (FileNotFoundException ex) {
        Tools.showError(ex);
      }
    }
  }

  <T> T loadProperty(String name, Class<T> clazz) {
    if (directory == null) {
      return null;
    }
    synchronized(directory) {
      try (XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(
            new FileInputStream(new File(directory, name))))) {
        try {
          return clazz.cast(decoder.readObject());
        } catch (ClassCastException ex) {
          Tools.showError(ex);
          return null;
        } catch (Exception ex) {
          //probably user messed up with files
          Tools.showError(ex);
          return null;
        }
      } catch (FileNotFoundException ex) {
        //ignore, we have not saved yet a property with this name
      }
    }
    return null;
  }

}
