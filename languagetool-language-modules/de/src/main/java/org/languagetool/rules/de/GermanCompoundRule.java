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
package org.languagetool.rules.de;

import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.UserConfig;
import org.languagetool.language.GermanyGerman;
import org.languagetool.rules.AbstractCompoundRule;
import org.languagetool.rules.Categories;
import org.languagetool.rules.CompoundRuleData;
import org.languagetool.rules.Example;
import org.languagetool.rules.patterns.PatternTokenBuilder;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static org.languagetool.rules.patterns.PatternRuleBuilderHelper.token;
import static org.languagetool.rules.patterns.PatternRuleBuilderHelper.tokenRegex;

/**
 * Checks that compounds are not written as separate words. The supported compounds are loaded
 * from {@code /de/compounds.txt} and {@code /de/compounds-cities.txt} in the resource directory.
 * 
 * @author Daniel Naber
 */
public class GermanCompoundRule extends AbstractCompoundRule {

  private static final Language GERMAN = Languages.getLanguageForShortCode("de-DE");
  private static final List<DisambiguationPatternRule> ANTI_PATTERNS = makeAntiPatterns(Arrays.asList(
    Arrays.asList(  // "Die Bürger konnten an die 900 Meter Kabel in Eigenregie verlegen."
      tokenRegex("an|um"),
      token("die"),
      tokenRegex("\\d+")
    ),
    Arrays.asList(  // "Lohnt sich die Werbung vom ausgegebenen Euro aus gedacht?"
      new PatternTokenBuilder().tokenRegex("von|vom").setSkip(5).build(),
      token("aus"),
      token("gedacht")
    ),
    Arrays.asList(  // "Die Bürger konnten an die 900 Meter Kabel in Eigenregie verlegen."
      tokenRegex("rund|etwa|zirka|cirka|ungefähr|annähernd|grob|wohl|gegen|schätzungsweise"),
      tokenRegex("\\d+")
    ),
    Arrays.asList(  // "Die Bürger konnten ca. 900 Meter Kabel in Eigenregie verlegen."
      token("ca"),
      token("."),
      tokenRegex("\\d+")
    )
  ), GERMAN);

  private static volatile CompoundRuleData compoundData;
  
  public GermanCompoundRule(ResourceBundle messages, Language lang, UserConfig userConfig) throws IOException {
    super(messages, lang, userConfig,
            "Dieses Wort wird mit Bindestrich geschrieben.",
            "Dieses Wort wird zusammengeschrieben.",
            "Diese Wörter werden zusammengeschrieben oder mit Bindestrich getrennt.",
            "Zusammenschreibung von Wörtern");
    super.setCategory(Categories.COMPOUNDING.getCategory(messages));
    addExamplePair(Example.wrong("Wenn es schlimmer wird, solltest Du zum <marker>HNO Arzt</marker> gehen."),
                   Example.fixed("Wenn es schlimmer wird, solltest Du zum <marker>HNO-Arzt</marker> gehen."));
  }

  @Override
  public String getId() {
    return "DE_COMPOUNDS";
  }

  @Override
  public String getDescription() {
    return "Zusammenschreibung von Wörtern, z. B. 'CD-ROM' statt 'CD ROM'";
  }

  @Override
  public CompoundRuleData getCompoundRuleData() {
    CompoundRuleData data = compoundData;
    if (data == null) {
      synchronized (GermanCompoundRule.class) {
        data = compoundData;
        if (data == null) {
          compoundData = data = new CompoundRuleData("/de/compounds.txt", "/de/compound-cities.txt");
        }
      }
    }
    return data;
  }
  
  @Override
  public boolean isMisspelled(String word) throws IOException {
    return GermanyGerman.INSTANCE.getDefaultSpellingRule().isMisspelled(word);
  }

  @Override
  public List<DisambiguationPatternRule> getAntiPatterns() {
    return ANTI_PATTERNS;
  }

}
