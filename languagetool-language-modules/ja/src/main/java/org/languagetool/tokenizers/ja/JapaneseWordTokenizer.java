/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tokenizers.ja;

import org.apache.lucene.analysis.ja.JapaneseTokenizer;
import org.apache.lucene.analysis.ja.tokenattributes.BaseFormAttribute;
import org.apache.lucene.analysis.ja.tokenattributes.PartOfSpeechAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.languagetool.tokenizers.Tokenizer;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;


public class JapaneseWordTokenizer implements Tokenizer {

  @Override
  public List<String> tokenize(String text) {
    List<String> ret = new ArrayList<>();

    try (JapaneseTokenizer tokenizer = new JapaneseTokenizer(null, false, JapaneseTokenizer.Mode.NORMAL)) {
      tokenizer.setReader(new StringReader(text));

      CharTermAttribute termAtt = tokenizer.addAttribute(CharTermAttribute.class);
      PartOfSpeechAttribute posAtt = tokenizer.addAttribute(PartOfSpeechAttribute.class);
      BaseFormAttribute baseFormAtt = tokenizer.addAttribute(BaseFormAttribute.class);

      tokenizer.reset();
      while (tokenizer.incrementToken()) {
        String surface = termAtt.toString();
        String baseForm = baseFormAtt.getBaseForm();
        if (baseForm == null || baseForm.equals("*")) {
          baseForm = surface;
        }
        String pos = posAtt.getPartOfSpeech();
        if (pos == null) {
          pos = "";
        } else {
          int separateIndex = pos.indexOf("-");
          if (separateIndex != -1) {
            pos = pos.substring(0, separateIndex);
          }
        }
        ret.add(surface + " " + pos + " " + baseForm);
      }
    } catch (Exception e) {
      return ret;
    }
    return ret;
  }
}
