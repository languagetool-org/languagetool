/* LanguageTool, a natural language style checker
 * Copyright (C) 2011 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.openoffice;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;

import javax.swing.JOptionPane;

import org.languagetool.JLanguageTool;
import org.languagetool.tools.Tools;

/**
 * Writes Messages to screen or log-file
 * @since 4.3
 * @author Fred Kruse, Marcin Miłkowski
 */
class MessageHandler {
  
  private static final String logLineBreak = System.getProperty("line.separator");  //  LineBreak in Log-File (MS-Windows compatible)
  
  private static String homeDir;
  private static String logFileName;
  
  private static boolean testMode;
  
  MessageHandler(String homeDir, String logFileName) {
    MessageHandler.homeDir = homeDir;
    MessageHandler.logFileName = logFileName;
    initLogFile();
  }

  /**
   * Initialize log-file
   */
  private static void initLogFile() {
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(getLogPath()))) {
      Date date = new Date();
      bw.write("LT office integration log from " + date.toString() + logLineBreak);
    } catch (Throwable t) {
      showError(t);
    }
  }
  
  static void init(String homeDir, String logFileName) {
    MessageHandler.homeDir = homeDir;
    MessageHandler.logFileName = logFileName;
    initLogFile();
  }

  static void showError(Throwable e) {
    if (testMode) {
      throw new RuntimeException(e);
    }
    String msg = "An error has occurred in LanguageTool "
        + JLanguageTool.VERSION + " (" + JLanguageTool.BUILD_DATE + "):\n" + e + "\nStacktrace:\n";
    msg += Tools.getFullStackTrace(e);
    String metaInfo = "OS: " + System.getProperty("os.name") + " on "
        + System.getProperty("os.arch") + ", Java version "
        + System.getProperty("java.version") + " from "
        + System.getProperty("java.vm.vendor");
    msg += metaInfo;
    DialogThread dt = new DialogThread(msg);
    e.printStackTrace();
    dt.start();
  }

  /**
   * write to log-file
   */
  static void printToLogFile(String str) {
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(getLogPath(), true))) {
      bw.write(str + logLineBreak);
    } catch (Throwable t) {
      showError(t);
    }
  }

  /** 
   * Prints Exception to log-file  
   */
  static void printException (Throwable t) {
   printToLogFile(Tools.getFullStackTrace(t));
  }

  private static String getLogPath() {
    String xdgDataHome = System.getenv().get("XDG_DATA_HOME");
    String logHome = xdgDataHome != null ? xdgDataHome + "/LanguageTool" : homeDir;
    String path = logHome + "/" + logFileName;
    File parentDir = new File(path).getParentFile();
    if (parentDir != null && !testMode) {
      if(!parentDir.exists()) {
        boolean success = parentDir.mkdirs();
        if(!success) {
          showMessage("Can't create directory: " + parentDir.toString());
        }
      }
    }
    return path;
  }
  
  /**
   * Will throw exception instead of showing errors as dialogs - use only for test cases.
   */
  static void setTestMode(boolean mode) {
    testMode = mode;
  }

  /**
   * Shows a message in a dialog box
   * @param txt message to be shown
   */
  static void showMessage(String txt) {
    DialogThread dt = new DialogThread(txt);
    dt.run();
  }

  private static class DialogThread extends Thread {
    private final String text;

    DialogThread(String text) {
      this.text = text;
    }

    @Override
    public void run() {
      JOptionPane.showMessageDialog(null, text);
    }
  }
  
}
