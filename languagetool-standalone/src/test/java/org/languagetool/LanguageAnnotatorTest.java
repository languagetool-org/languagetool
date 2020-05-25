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
package org.languagetool;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.*;

public class LanguageAnnotatorTest {

  private final Language en = Languages.getLanguageForShortCode("en-US");
  private final Language de = Languages.getLanguageForShortCode("de-DE");
  private final List<Language> deList = Arrays.asList(de);
  private final List<Language> enList = Arrays.asList(en);
  private final List<Language> enDe = Arrays.asList(en, de);

  @Test
  public void testGetTokensWithPotentialLanguages() {
    LanguageAnnotator annotator = new LanguageAnnotator();
    List<LanguageAnnotator.TokenWithLanguages> tokens = annotator.getTokensWithPotentialLanguages("Der große Haus.", en, deList);
    assertThat(tokens.toString(), is("[Der/de-DE,  , große,  , Haus/de-DE, .]"));   // TODO: why no lang for 'große'?
    List<LanguageAnnotator.TokenWithLanguages> tokens2 = annotator.getTokensWithPotentialLanguages("This is a new bicycle.", en, deList);
    assertThat(tokens2.toString(), is("[This/en-US,  , is/en-US,  , a/en-US/de-DE,  , new/en-US,  , bicycle/en-US, .]"));
  }

  @Test
  public void testDetectLanguages() {
    LanguageAnnotator annotator = new LanguageAnnotator();
    assertThat(annotator.detectLanguages("This is an English test. Hier kommt ein deutscher Satz.", en, deList).toString(),
                                      is("[| en-US: This is an English test. |, | de-DE:  Hier kommt ein deutscher Satz. |]"));
    assertThat(annotator.detectLanguages("This is an English test. Nun kommt ein deutscher Satz.", en, deList).toString(),  // "Nun" is also English
                                      is("[| en-US: This is an English test. |, | de-DE:  Nun kommt ein deutscher Satz. |]"));
    assertThat(annotator.detectLanguages("Nun kommt ein deutscher Satz. This is an English test.", en, deList).toString(),  // "Nun" is also English
                                      is("[| en-US:  |, | de-DE: Nun kommt ein deutscher Satz. |, | en-US:  This is an English test. |]"));  // TODO: empty "en-US"
    assertThat(annotator.detectLanguages("Hier steht was auf Deutsch. This is English text one. " +
                                         "Hier steht was auf Deutsch. \"This is English text two.\"", en, deList).toString(),
                                      is("[| en-US:  |, | de-DE: Hier steht was auf Deutsch. |, | en-US:  This is English text one. |, " +
                                          "| de-DE:  Hier steht was auf Deutsch. |, | en-US:  \"This is English text two.\" |]"));  // TODO: empty "en-US"
    assertThat(annotator.detectLanguages("Hier steht was auf Deutsch. This is English text one. " +
                                         "Hier steht was auf Deutsch. \"This is English text two.\"", de, enList).toString(),
                                      is("[| de-DE: Hier steht was auf Deutsch. |, | en-US:  This is English text one. |, " +
                                          "| de-DE:  Hier steht was auf Deutsch. |, | en-US:  \"This is English text two.\" |]"));
    assertThat(annotator.detectLanguages("Nutzen Sie es auf unserer Webseite foobar.com?", de, enList).toString(),
                                      is("[| de-DE: Nutzen Sie es auf unserer Webseite foobar.com? |]"));
    // TODO: sentences with typos
  }
  
  @Test
  public void testGetTokenRanges() {
    LanguageAnnotator annotator = new LanguageAnnotator();
    List<List<LanguageAnnotator.TokenWithLanguages>> tokenRanges = annotator.getTokenRanges(Arrays.asList(
      token("This", en), token(" ", en),
      token("is", de), token(" ", en),
      token("a", de), token(" ", en),
      token("test", en),
      token("!", en),
      token("Hier", de), token(" ", de),
      token("geht", de), token(" ", de),
      token("es", de), token(" ", de),
      token("weiter", de),
      token(".", de)
    ));
    assertThat(getTokenRangeAsString(tokenRanges), is("[This is a test!][Hier geht es weiter.]"));
    assertThat(annotator.getTokenRangesWithLang(tokenRanges, en, deList).toString(),
      is("[en-US: [This,  , is,  , a,  , test, !], de-DE: [Hier,  , geht,  , es,  , weiter, .]]"));

    List<List<LanguageAnnotator.TokenWithLanguages>> tokenRanges2 = annotator.getTokenRanges(Arrays.asList(
      token("This", en), token(" ", en),
      token("is", de), token(" ", en),
      token("a", de), token(" ", en),
      token("test", en),
      token("!", en),
      token("Hier", de), token(" ", de),
      token("geht", de), token(" ", de),
      token("es", de), token(" ", de),
      token("weiter", de)
    ));
    assertThat(getTokenRangeAsString(tokenRanges2), is("[This is a test!][Hier geht es weiter]"));
    assertThat(annotator.getTokenRangesWithLang(tokenRanges2, en, deList).toString(),
      is("[en-US: [This,  , is,  , a,  , test, !], de-DE: [Hier,  , geht,  , es,  , weiter]]"));
  }

  @Test
  public void testGetTokenRangesAmbiguous() {
    LanguageAnnotator annotator = new LanguageAnnotator();
    List<List<LanguageAnnotator.TokenWithLanguages>> tokenRanges = annotator.getTokenRanges(Arrays.asList(
      token("Hi", enDe), token(","),
      token("\n"),
      token("\n"),
      token("this", en), token(" ", en),
      token("is", de), token(" ", en),
      token("a", de), token(" ", en),
      token("test", en),
      token(".", en),
      token("Hier", de), token(" ", de),
      token("geht", de), token(" ", de),
      token("es", de), token(" ", de),
      token("weiter", de),
      token(".", de)
    ));
    assertThat(annotator.getTokenRangesWithLang(tokenRanges, de, enList).toString(),
      is("[en-US: [Hi, ,, \n], en-US: [\n], en-US: [this,  , is,  , a,  , test, .], de-DE: [Hier,  , geht,  , es,  , weiter, .]]"));
  }

  @Test
  public void testGetTokenRanges2() {
    LanguageAnnotator annotator = new LanguageAnnotator();
    List<List<LanguageAnnotator.TokenWithLanguages>> tokenRanges3 = annotator.getTokenRanges(Arrays.asList(
      t("This"), t(" "),
      t("is"), t(" "),
      t("a"), t(" "),
      t("test"),
      t("."),
      t("\""),
      t("Hier"), t(" "),
      t("geht"), t(" "),
      t("es"), t(" "),
      t("weiter"),
      t("\"")
    ));
    assertThat(getTokenRangeAsString(tokenRanges3), is("[This is a test.][\"Hier geht es weiter\"]"));
  }

  @Test
  public void testGetTokenRanges3() {
    LanguageAnnotator annotator = new LanguageAnnotator();
    List<List<LanguageAnnotator.TokenWithLanguages>> tokenRanges3 = annotator.getTokenRanges(Arrays.asList(
      t("This"), t(" "),
      t("is"), t(" "),
      t("a"), t(" "),
      t("test"),
      t("."),
      t("\""),
      t("Hier"), t(" "),
      t("geht"), t(" "),
      t("es"), t(" "),
      t("weiter"),
      t("."),
      t("\"")
    ));
    assertThat(getTokenRangeAsString(tokenRanges3), is("[This is a test.][\"Hier geht es weiter.][\"]"));
  }

  @NotNull
  private String getTokenRangeAsString(List<List<LanguageAnnotator.TokenWithLanguages>> tokenRanges) {
    StringBuilder sb = new StringBuilder();
    for (List<LanguageAnnotator.TokenWithLanguages> tokenRange : tokenRanges) {
      sb.append('[');
      for (LanguageAnnotator.TokenWithLanguages tokenWithLanguage : tokenRange) {
        sb.append(tokenWithLanguage.getToken());
      }
      sb.append(']');
    }
    return sb.toString();
  }

  @NotNull
  private LanguageAnnotator.TokenWithLanguages token(String s, Language... langs) {
    return new LanguageAnnotator.TokenWithLanguages(s, langs);
  }

  @NotNull
  private LanguageAnnotator.TokenWithLanguages token(String s, List<Language> langs) {
    return new LanguageAnnotator.TokenWithLanguages(s, langs);
  }

  @NotNull
  private LanguageAnnotator.TokenWithLanguages t(String s) {
    return new LanguageAnnotator.TokenWithLanguages(s, en);
  }

}
