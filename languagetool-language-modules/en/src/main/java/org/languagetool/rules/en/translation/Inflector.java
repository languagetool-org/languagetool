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
package org.languagetool.rules.en.translation;

import org.jetbrains.annotations.NotNull;
import org.languagetool.AnalyzedToken;
import org.languagetool.Languages;
import org.languagetool.synthesis.Synthesizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Inflects English words according to a German POS tag.
 */
class Inflector {

  private final Synthesizer enSynth = Languages.getLanguageForShortCode("en").getSynthesizer();

  Inflector() {
  }

  /**
   * @param enToken base form of English token (from BeoLingus)
   */
  List<String> inflect(String enToken, String dePosTag) {
    List<String> parts = Arrays.asList(enToken.replaceFirst("to ", "").split(" "));
    List<String> lastPartForms = inflectSingleWord(parts.get(parts.size() - 1), dePosTag);
    String startParts = parts.size() > 1 ? String.join(" ", parts.subList(0, parts.size()-1)) : "";
    ArrayList<String> result = new ArrayList<>();
    for (String lastPartForm : lastPartForms) {
      // only the last part is inflected, e.g. "tire pump" -> "tire pumps"
      result.add((startParts + " " + lastPartForm).trim());
    }
    return result;
  }

  List<String> inflectSingleWord(String enToken, String dePosTag) {
    List<String> forms = new ArrayList<>();
    if (dePosTag == null) {
      forms.add(enToken);
      return forms;
    }
    if (dePosTag.matches("SUB.*PLU.*")) {
      forms.addAll(getForms(enToken, "NNP?S"));
    } else if (dePosTag.matches("VER:3:SIN:PRÄ.*")) {
      forms.addAll(getForms(enToken, "VBZ"));
    } else if (dePosTag.matches("VER:3:SIN:PRT:.*")) {
      forms.addAll(getForms(enToken, "VBD"));
    } else if (dePosTag.matches("PA1:PRD:GRU:VER")) {
      forms.addAll(getForms(enToken, "VBG"));
    } else if (dePosTag.matches("PA2:PRD:GRU:VER")) {
      forms.addAll(getForms(enToken, "VBN"));
    } else if (dePosTag.matches("ADJ:PRD:KOM|ADJ:.*:KOM.*")) {
      forms.addAll(getForms(enToken, "JJR"));
    } else if (dePosTag.matches("ADJ:.*:SUP.*")) {
      forms.addAll(getForms(enToken, "JJS"));
    } else {
      forms.add(enToken);
    }
    return forms;
  }

  @SuppressWarnings("ConstantConditions")
  @NotNull
  private List<String> getForms(String enToken, String posTagRegex) {
    try {
      return Arrays.asList(enSynth.synthesize(new AnalyzedToken(enToken, "fake-value", enToken), posTagRegex, true));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
