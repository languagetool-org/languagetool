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
package org.languagetool.rules.pt;

import org.languagetool.rules.AbstractWordCoherencyRule;
import org.languagetool.rules.Example;
import org.languagetool.rules.WordCoherencyDataLoader;

import java.io.IOException;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Portuguese version of {@link AbstractWordCoherencyRule}.
 * 
 * @author Tiago F. Santos
 * @since 3.8
 */
public class PortugueseWordCoherencyRule extends AbstractWordCoherencyRule {

  private static final Map<String, String> wordMap = new WordCoherencyDataLoader().loadWords("/pt/coherency.txt");

  public PortugueseWordCoherencyRule(ResourceBundle messages) throws IOException {
    super(messages);
    addExamplePair(Example.wrong("Foi um período duradouro. Tão marcante e <marker>duradoiro</marker> dificilmente será esquecido."),
                   Example.fixed("Foi um período duradouro. Tão marcante e <marker>duradouro</marker> dificilmente será esquecido."));
  }

  @Override
  protected Map<String, String> getWordMap() {
    return wordMap;
  }

  @Override
  protected String getMessage(String word1, String word2) {
    return "Não deve utilizar formas distintas de palavras com dupla grafia no mesmo texto. Escolha entre '" + word1 + "' e '" + word2 + "'.";
  }
  
  @Override
  public String getId() {
    return "PT_WORD_COHERENCY";
  }

  @Override
  public String getDescription() {
    return "Verificação de consistência para palavras com múltiplas grafia correctas";
  }

}
