/* LanguageTool, a natural language style checker 
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tools;

import java.io.File;

public class DictionaryBuilderTestHelper {

  protected File exportDictionaryContents(File file) throws Exception {
    File outputFile = File.createTempFile(DictionaryExporter.class.getSimpleName(), ".export");
    
    String infoFile = file.toString().replaceAll("\\.dict$", ".info");
    String[] buildOptions = {
        "--exit", "false",
        "-i", file.toString(), 
        "-o", outputFile.toString(),
        "-info", infoFile 
    };
    DictionaryExporter.main(buildOptions);
    return outputFile;
  }
}
