/* LanguageTool, a natural language style checker
 * Copyright (C) 2018 Fred Kruse
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
package org.languagetool.rules.de;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.TestTools;

import java.io.IOException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * @author Fred Kruse
 */
public class CompoundInfinitivRuleTest {
  
  @Test
  public void testRule() throws IOException {
    Language german = Languages.getLanguageForShortCode("de");
    CompoundInfinitivRule rule = new CompoundInfinitivRule(TestTools.getMessages("de"), german, null);
    JLanguageTool lt = new JLanguageTool(german);

    assertThat(rule.match(lt.getAnalyzedSentence("Ich brachte ihn dazu, mein Zimmer sauber zu machen.")).length, is(1));
    assertThat(rule.match(lt.getAnalyzedSentence("Du brauchst nicht bei mir vorbei zu kommen.")).length, is(1));
    assertThat(rule.match(lt.getAnalyzedSentence("Ich ging zur Seite, um die alte Dame vorbei zu lassen.")).length, is(1));

    assertThat(rule.match(lt.getAnalyzedSentence("Seine Frau gab vor zu schlafen.")).length, is(0));
    assertThat(rule.match(lt.getAnalyzedSentence("Mein Herz hörte auf zu schlagen.")).length, is(0));
    assertThat(rule.match(lt.getAnalyzedSentence("Den Sonnenaufgang von einem Berggipfel aus zu sehen, ist eine Wonne.")).length, is(0));
    assertThat(rule.match(lt.getAnalyzedSentence("Hör auf zu schreien")).length, is(0));
    assertThat(rule.match(lt.getAnalyzedSentence("Sie riss sich zusammen und fing wieder an zu reden.")).length, is(0));
    assertThat(rule.match(lt.getAnalyzedSentence("Fang an zu zählen.")).length, is(0));
    assertThat(rule.match(lt.getAnalyzedSentence("Er hatte nichts weiter zu sagen")).length, is(0));
    assertThat(rule.match(lt.getAnalyzedSentence("Sie strengte sich an zu schwimmen.")).length, is(0));
    assertThat(rule.match(lt.getAnalyzedSentence("Tom stand auf und fing an, auf und ab zu gehen.")).length, is(0));
    assertThat(rule.match(lt.getAnalyzedSentence("Aber um auf Nummer sicher zu gehen, schrieb er es auf.")).length, is(0));
  }

}
