/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.eval;

import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.dev.dumpcheck.SentenceSourceChecker;

import java.io.File;
import java.io.IOException;

/**
 * See https://gist.github.com/danielnaber/79907f27e272fa43a4fa23b400a9fbd4
 * for how to extract the files from Tatoeba.
 *
 * Use `grep "Rule ID" /tmp/out | sed 's/.*Rule ID: //' | sort | uniq -c | sort -rn`
 * to get to rules (when only running on a single language).
 */
public class TatoebaEvaluator {

  private final static String template = "/Users/sohaib.lafifi/Code/Arabic/tatoeba/sentences-LANG-20191014-top1000.txt";

  private void run() throws IOException {
    for (Language lang : Languages.get()) {
      if (!lang.getShortCode().equals("ar")) {
        continue;
      }
      File file = new File(template.replaceFirst("LANG", lang.getShortCode()));
      if (!file.exists() || file.length() == 0) {
        System.err.println("File not found or empty, skipping: " + file);
        continue;
      }
      SentenceSourceChecker.main(new String[]{"-l", lang.getShortCode(), "-f", file.getAbsolutePath()});
    }
  }

  public static void main(String[] args) throws IOException {
    new TatoebaEvaluator().run();
  }
}
