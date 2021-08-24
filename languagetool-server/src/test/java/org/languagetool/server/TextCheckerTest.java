/* LanguageTool, a natural language style checker
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.server;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.markup.AnnotatedTextBuilder;

import java.io.*;
import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class TextCheckerTest {

  private final String english = "This is clearly an English text, should be easy to detect.";
  private final TextChecker checker = new V2TextChecker(new HTTPServerConfig(), false, null, new RequestCounter());
  private final String unsupportedCzech = "V současné době je označením Linux míněno nejen jádro operačního systému, ale zahrnuje do něj též veškeré programové vybavení";

  @Test
  public void testJSONP() throws Exception {
    Map<String, String> params = new HashMap<>();
    params.put("text", "not used");
    params.put("language", "en");
    params.put("callback", "myCallback");
    HTTPServerConfig config1 = new HTTPServerConfig(HTTPTestTools.getDefaultPort());
    TextChecker checker = new V2TextChecker(config1, false, null, new RequestCounter());
    FakeHttpExchange httpExchange = new FakeHttpExchange();
    checker.checkText(new AnnotatedTextBuilder().addText("some random text").build(), httpExchange, params, null, null);
    assertTrue(httpExchange.getOutput().startsWith("myCallback("));
    assertTrue(httpExchange.getOutput().endsWith(");"));
  }
  
  @Test
  public void testMaxTextLength() throws Exception {
    Map<String, String> params = new HashMap<>();
    params.put("text", "not used");
    params.put("language", "en");
    HTTPServerConfig config1 = new HTTPServerConfig(HTTPTestTools.getDefaultPort());
    config1.setMaxTextLengthAnonymous(10);
    TextChecker checker = new V2TextChecker(config1, false, null, new RequestCounter());
    try {
      checker.checkText(new AnnotatedTextBuilder().addText("longer than 10 chars").build(), new FakeHttpExchange(), params, null, null);
      fail();
    } catch (TextTooLongException ignore) {}
    try {
      params.put("token", "invalid");
      checker.checkText(new AnnotatedTextBuilder().addText("longer than 10 chars").build(), new FakeHttpExchange(), params, null, null);
      fail();
    } catch (RuntimeException ignore) {}
    String validToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczpcL1wvbGFuZ3VhZ2V0b29scGx1cy5jb20iLCJpYXQiOjE1MDQ4NTY4NTQsInVpZCI6MSwibWF4VGV4dExlbmd0aCI6MTAwfQ._-8qpa99IWJiP_Zx5o-yVU11neW8lrxmLym1DdwPtIc";
    try {
      params.put("token", validToken);
      checker.checkText(new AnnotatedTextBuilder().addText("longer than 10 chars").build(), new FakeHttpExchange(), params, null, null);
      fail();
    } catch (RuntimeException expected) {
      // server not configured to accept tokens
    }
    try {
      config1.secretTokenKey = "foobar";
      checker.checkText(new AnnotatedTextBuilder().addText("longer than 10 chars").build(), new FakeHttpExchange(), params, null, null);
      fail();
    } catch (SignatureVerificationException ignore) {}

    config1.secretTokenKey = "foobar";
    // see test below for how to create a token:
    params.put("token", "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vZm9vYmFyIiwiaWF0IjoxNTA0ODU3NzAzLCJtYXhUZXh0TGVuZ3RoIjozMH0.2ijjMEhSyJPEc0fv91UtOJdQe8CMfo2U9dbXgHOkzr0");
    checker.checkText(new AnnotatedTextBuilder().addText("longer than 10 chars").build(), new FakeHttpExchange(), params, null, null);

    try {
      config1.secretTokenKey = "foobar";
      checker.checkText(new AnnotatedTextBuilder().addText("now it's even longer than 30 chars").build(), new FakeHttpExchange(), params, null, null);
      fail();
    } catch (TextTooLongException expected) {
      // too long even with claim from token, which allows 30 characters
    }
  }
  
  @Test
  @Ignore("use to create JWT test tokens for the other tests")
  public void makeToken() {
    Algorithm algorithm = Algorithm.HMAC256("foobar");
    String token = JWT.create()
            .withIssuer("http://foobar")
            .withIssuedAt(new Date())
            .withClaim("maxTextLength", 5000)
            .withClaim("premium", true)
            .withClaim("dictCacheSize", 10000L)
            .withClaim("uid", 42L)
            //.withClaim("skipLimits", true)
            //.withExpiresAt(new Date());
            .sign(algorithm);
    System.out.println(token);
  }

  @Test
  public void testInvalidAltLanguages() throws Exception {
    Map<String, String> params = new HashMap<>();
    params.put("text", "not used");
    params.put("language", "en");
    HTTPServerConfig config1 = new HTTPServerConfig(HTTPTestTools.getDefaultPort());
    TextChecker checker = new V2TextChecker(config1, false, null, new RequestCounter());
    try {
      params.put("altLanguages", "en");
      checker.checkText(new AnnotatedTextBuilder().addText("something").build(), new FakeHttpExchange(), params, null, null);
      fail();
    } catch (BadRequestException ignore) {
    }
    try {
      params.put("altLanguages", "xy");
      checker.checkText(new AnnotatedTextBuilder().addText("something").build(), new FakeHttpExchange(), params, null, null);
      fail();
    } catch (BadRequestException ignore) {
    }
    
    params.put("language", "en");
    params.put("altLanguages", "de-DE");
    checker.checkText(new AnnotatedTextBuilder().addText("something").build(), new FakeHttpExchange(), params, null, null);

    params.put("language", "en-US");
    params.put("altLanguages", "en-US");  // not useful, but not forbidden
    checker.checkText(new AnnotatedTextBuilder().addText("something").build(), new FakeHttpExchange(), params, null, null);
  }

  @Test
  public void testDetectLanguageOfString() {
    List<String> e = Collections.emptyList();
    List<String> preferredLangs = Collections.emptyList();
    assertThat(checker.detectLanguageOfString("", "en", Arrays.asList("en-GB"), e, preferredLangs, false)
      .getDetectedLanguage().getShortCodeWithCountryAndVariant(), is("en-GB"));

    // fallback language does not work anymore, now detected as ca-ES, ensure that at least the probability is low
    //assertThat(checker.detectLanguageOfString("X", "en", Arrays.asList("en-GB"), e)
    //  .getDetectedLanguage().getShortCodeWithCountryAndVariant(), is("en-GB"));
    //assertThat(checker.detectLanguageOfString("X", "en", Arrays.asList("en-ZA"), e)
    //  .getDetectedLanguage().getShortCodeWithCountryAndVariant(), is("en-ZA"));
    assertThat(checker.detectLanguageOfString("X", "en", Arrays.asList("en-GB"), e, preferredLangs, false)
      .getDetectedLanguage().getShortCodeWithCountryAndVariant(), is("ca-ES"));
    assertTrue(checker.detectLanguageOfString("X", "en", Arrays.asList("en-GB"), e, preferredLangs, false)
      .getDetectionConfidence() < 0.5);

    assertThat(checker.detectLanguageOfString(english, "de", Arrays.asList("en-GB", "de-AT"), e, preferredLangs, false)
      .getDetectedLanguage().getShortCodeWithCountryAndVariant(), is("en-GB"));
    assertThat(checker.detectLanguageOfString(english, "de", Arrays.asList(), e, preferredLangs, false)
      .getDetectedLanguage().getShortCodeWithCountryAndVariant(), is("en-US"));
    assertThat(checker.detectLanguageOfString(english, "de", Arrays.asList("de-AT", "en-ZA"), e, preferredLangs, false)
      .getDetectedLanguage().getShortCodeWithCountryAndVariant(), is("en-ZA"));
    String german = "Das hier ist klar ein deutscher Text, sollte gut zu erkennen sein.";
    assertThat(checker.detectLanguageOfString(german, "fr", Arrays.asList("de-AT", "en-ZA"), e, preferredLangs, false)
      .getDetectedLanguage().getShortCodeWithCountryAndVariant(), is("de-AT"));
    assertThat(checker.detectLanguageOfString(german, "fr", Arrays.asList("de-at", "en-ZA"), e, preferredLangs, false)
      .getDetectedLanguage().getShortCodeWithCountryAndVariant(), is("de-AT"));
    assertThat(checker.detectLanguageOfString(german, "fr", Arrays.asList(), e, preferredLangs, false)
      .getDetectedLanguage().getShortCodeWithCountryAndVariant(), is("de-DE"));
    assertThat(checker.detectLanguageOfString(unsupportedCzech, "en", Arrays.asList(), e, preferredLangs, false)
      .getDetectedLanguage().getShortCodeWithCountryAndVariant(), is("sk-SK"));  // misdetected because it's not supported
  }

  @Test
  @Ignore("requires fastText (binary and model) installed locally")
  public void testDetectLanguageOfStringWithFastText() {
    HTTPServerConfig config = new HTTPServerConfig();
    config.setFasttextBinary(new File("/prg/fastText-0.1.0/fasttext"));
    config.setFasttextModel(new File("/prg/fastText-0.1.0/data/lid.176.bin"));
    //config.setFasttextBinary(new File("/home/fabian/Documents/fastText/fasttext"));
    //config.setFasttextModel(new File("/home/fabian/Documents/fastText/lid.176.bin"));
    TextChecker checker = new V2TextChecker(config, false, null, new RequestCounter());
    assertThat(checker.detectLanguageOfString(unsupportedCzech, "en", Arrays.asList(), Arrays.asList("foo", "cs"), Collections.emptyList(), false).
            getDetectedLanguage().getShortCodeWithCountryAndVariant(), is("zz"));  // cs not supported but mapped to noop language
  }

  @Test(expected = RuntimeException.class)
  public void testInvalidPreferredVariant() {
    checker.detectLanguageOfString(english, "de", Arrays.asList("en"), Collections.emptyList(), Collections.emptyList(), false);  // that's not a variant
  }

  @Test(expected = RuntimeException.class)
  public void testInvalidPreferredVariant2() {
    checker.detectLanguageOfString(english, "de", Arrays.asList("en-YY"), Collections.emptyList(), Collections.emptyList(), false);  // variant doesn't exist
  }

}
