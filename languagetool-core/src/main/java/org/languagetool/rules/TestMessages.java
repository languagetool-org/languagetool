package org.languagetool.rules;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.languagetool.tools.Tools;

public class TestMessages {
  
  private static final String VENDOR_ID = "languagetool.org";
  private static final String APPLICATION_ID = "LanguageTool";
  private static final String LOG_FILE_NAME = "lt_test.log";
  private static final String logLineBreak = System.getProperty("line.separator");  //  LineBreak in Log-File (MS-Windows compatible)
  private static File logFile = null;

  /**
   * write to log-file
   * @throws IOException 
   */
  static void printToLogFile(String str) {
    if(logFile == null) {
      logFile = getLogFile();
    }
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true))) {
      bw.write(str + logLineBreak);
    } catch (IOException e) {
    }
  }

  /** 
   * Prints Exception to log-file  
   * @throws IOException 
   */
  static void printException (Throwable t) {
   printToLogFile(Tools.getFullStackTrace(t));
  }


  
  private static File getLogFile() {
    String userHome = null;
    File directory;
    File file;
    try {
      userHome = System.getProperty("user.home");
    } catch (SecurityException ex) {
    }
    if (userHome == null) {
      throw new RuntimeException("Could not get home directory");
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
    file = new File(directory.toString() + "/" + LOG_FILE_NAME);
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
      Date date = new Date();
      bw.write("LT office integration log from " + date.toString() + logLineBreak);
    } catch (IOException e) {
    }
    return file;
}



}
