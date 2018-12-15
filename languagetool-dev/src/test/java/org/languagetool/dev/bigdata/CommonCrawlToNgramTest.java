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
package org.languagetool.dev.bigdata;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.language.GermanyGerman;

public class CommonCrawlToNgramTest {
  
  @Test
  @Ignore("Interactive use only, has not assertions")
  public void testIndexing() throws IOException {
    File tempDir = new File(FileUtils.getTempDirectory(), "common-crawl-test");
    try {
      tempDir.mkdir();
      String filename = CommonCrawlToNgramTest.class.getResource("/org/languagetool/dev/bigdata/ngram-input.txt.xz").getFile();
      try (CommonCrawlToNgram prg = new CommonCrawlToNgram(new GermanyGerman(), new File(filename), tempDir, null)) {
        prg.setCacheLimit(1);
        prg.indexInputFile();
      }
    } finally {
      FileUtils.deleteDirectory(tempDir);
    }
  }

}