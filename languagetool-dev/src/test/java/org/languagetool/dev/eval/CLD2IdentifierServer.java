/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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

//import org.commoncrawl.langdetect.cld2.Cld2;
//import org.commoncrawl.langdetect.cld2.Result;
//import org.languagetool.DetectedLanguage;
//import org.languagetool.Language;
//import org.languagetool.Languages;

public class CLD2IdentifierServer /*extends DefaultLanguageIdentifier*/ {

  /*
  public static void main(String[] args) {
    Result result = Cld2.detect("Die in peace");
    System.out.println(result);
  }

  private final List<String> ltSupportedCodes;
  
  CLD2Identifier() {
    ltSupportedCodes = Languages.get().stream().map(Language::getShortCode).collect(toList());
  }

  @Override
  public DetectedLanguage detectLanguage(String text, List<String> noopLangs) {
    Result result = Cld2.detect(text);
    //Result result = Cld2.detect(text, null, Flags.kCLDFlagBestEffort, true);
    //System.out.println("->"+result.getLanguageCode() + " for " + text);
    if (result.getLanguageCode().equals("un")) {  // unknown
      return null;
    }
    if (!ltSupportedCodes.contains(result.getLanguageCode())) {
      //System.out.println("->"+ Arrays.toString(result.getLanguageCodes()));
      return null;
    }
    Language language = Languages.getLanguageForShortCode(result.getLanguageCode());
    return new DetectedLanguage(null, language);
  }
  */
}
