/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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

import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileBuilder;
import com.optimaize.langdetect.profiles.LanguageProfileWriter;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Train the language detector for a language not known yet.
 * @since 2.9
 */
final class LanguageDetectionTrainer {

  public static void main(String[] args) throws IOException {
    if (args.length != 3) {
      System.out.println("Usage: " + LanguageDetectionTrainer.class.getName() + " <languageCode> <plainTextFile> <minimalFrequency>");
      System.exit(1);
    }
    String langCode = args[0];
    String fileName = args[1];
    int minimalFrequency = Integer.parseInt(args[2]);
    String text = IOUtils.toString(new FileReader(fileName));
    TextObjectFactory textObjectFactory = CommonTextObjectFactories.forIndexingCleanText();
    TextObject inputText = textObjectFactory.create().append(text);
    LanguageProfile languageProfile = new LanguageProfileBuilder(langCode)
            .ngramExtractor(NgramExtractors.standard())
            .minimalFrequency(minimalFrequency)
            .addText(inputText)
            .build();
    File outputDir = new File(System.getProperty("user.dir"));  // current dir
    new LanguageProfileWriter().writeToDirectory(languageProfile, outputDir);
    System.out.println("Language profile written to " + new File(outputDir, langCode).getAbsolutePath());
  }

}
