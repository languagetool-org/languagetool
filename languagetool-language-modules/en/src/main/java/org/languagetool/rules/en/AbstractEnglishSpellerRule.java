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
package org.languagetool.rules.en;

import org.languagetool.AnalyzedToken;
import org.languagetool.Language;
import org.languagetool.rules.Example;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;
import org.languagetool.synthesis.en.EnglishSynthesizer;

import java.io.IOException;
import java.util.*;

public abstract class AbstractEnglishSpellerRule extends MorfologikSpellerRule {

  private final EnglishSynthesizer synthesizer = new EnglishSynthesizer();

  public AbstractEnglishSpellerRule(ResourceBundle messages, Language language) throws IOException {
    super(messages, language);
    setCheckCompound(true);
    addExamplePair(Example.wrong("This <marker>sentenc</marker> contains a spelling mistake."),
                   Example.fixed("This <marker>sentence</marker> contains a spelling mistake."));
  }

  @Override
  protected List<RuleMatch> getRuleMatches(String word, int startPos) throws IOException {
    List<RuleMatch> ruleMatches = super.getRuleMatches(word, startPos);
    if (ruleMatches.size() > 0) {
      // so 'word' is misspelled: 
      IrregularForms irregularForms = getIrregularFormsOrNull(word);
      if (irregularForms != null) {
        RuleMatch match = new RuleMatch(this, startPos, startPos + word.length(), 
                "'" + irregularForms.baseform + "' is an irregular " + irregularForms.type + ".");
        match.setSuggestedReplacements(irregularForms.forms);
        ruleMatches = Collections.singletonList(match);
      }
    }
    return ruleMatches;
  }

  @SuppressWarnings({"ReuseOfLocalVariable", "ControlFlowStatementWithoutBraces"})
  private IrregularForms getIrregularFormsOrNull(String word) {
    IrregularForms irregularFormsOrNull = getIrregularFormsOrNull(word, "ed", Arrays.asList("ed"), "VBD", "verb");
    if (irregularFormsOrNull != null) return irregularFormsOrNull;
    irregularFormsOrNull = getIrregularFormsOrNull(word, "ed", Arrays.asList("d" /* e.g. awaked */), "VBD", "verb");
    if (irregularFormsOrNull != null) return irregularFormsOrNull;
    irregularFormsOrNull = getIrregularFormsOrNull(word, "s", Arrays.asList("s"), "NNS", "noun");
    if (irregularFormsOrNull != null) return irregularFormsOrNull;
    irregularFormsOrNull = getIrregularFormsOrNull(word, "es", Arrays.asList("es"/* e.g. 'analysises' */), "NNS", "noun");
    if (irregularFormsOrNull != null) return irregularFormsOrNull;
    irregularFormsOrNull = getIrregularFormsOrNull(word, "er", Arrays.asList("er"/* e.g. 'farer' */), "JJR", "adjective");
    if (irregularFormsOrNull != null) return irregularFormsOrNull;
    irregularFormsOrNull = getIrregularFormsOrNull(word, "est", Arrays.asList("est"/* e.g. 'farest' */), "JJS", "adjective");
    return irregularFormsOrNull;
  }

  private IrregularForms getIrregularFormsOrNull(String word, String wordSuffix, List<String> suffixes, String posTag, String posName) {
    try {
      for (String suffix : suffixes) {
        if (word.endsWith(wordSuffix)) {
          String baseForm = word.substring(0, word.length() - suffix.length());
          String[] forms = synthesizer.synthesize(new AnalyzedToken(word, null, baseForm), posTag);
          List<String> result = new ArrayList<>();
          result.addAll(Arrays.asList(forms));
          // the internal dict might contain forms that the spell checker doesn't accept (e.g. 'criterions'),
          // but we trust the spell checker in this case:
          result.remove(word);
          result.remove("badder");  // non-standard usage
          result.remove("baddest");  // non-standard usage
          result.remove("spake");  // can be removed after dict update
          if (forms.length > 0) {
            return new IrregularForms(baseForm, posName, result);
          }
        }
      }
      return null;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static class IrregularForms {
    String baseform;
    String type;
    List<String> forms;
    private IrregularForms(String baseform, String type, List<String> forms) {
      this.baseform = baseform;
      this.type = type;
      this.forms = forms;
    }
  }
}
