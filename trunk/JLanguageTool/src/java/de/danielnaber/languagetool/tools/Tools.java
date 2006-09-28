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
package de.danielnaber.languagetool.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public final class Tools {
  
  private Tools() {
    // cannot construct, static methods only
  }

  public static InputStream getInputStream(final String resourcePath) throws IOException {
    try {
      // try the URL first.
      URL url = new URL(resourcePath);
      // success, load the resource.
      InputStream is = url.openStream();
      return is;
    } catch (MalformedURLException e) {
      // no luck. Fallback to class loader paths.
    }

    // try file path
    File f = new File(resourcePath);
    if (f.exists() && f.isFile() && f.canRead()) {
      return new FileInputStream(f);
    } else
      throw new IOException("Could not open input stream from URL/ resource/ file: " + resourcePath);
  }

}
