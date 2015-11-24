/* LanguageTool, a natural language style checker
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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

import org.junit.Test;

public class VersionTest {
    
    @Test
    public void printVersion() {
        // As Maven/Jenkins on cloudbees.com doesn't seem to log the Java version being
        // used, we log it here manually:
        System.out.println("Java version: " + System.getProperty("java.version") + ", " + System.getProperty("java.vendor"));
        System.out.println("OS: " + System.getProperty("os.arch") + ", " + System.getProperty("os.name") + ", " + System.getProperty("os.version"));
        System.out.println("user.language: " + System.getProperty("user.language"));
        System.out.println("user.country: " + System.getProperty("user.country"));
        System.out.println("file.encoding: " + System.getProperty("file.encoding"));
        /*Properties properties = System.getProperties();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }*/
    }
    
}
