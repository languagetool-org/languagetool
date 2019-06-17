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

import java.util.Arrays;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.AbstractFillerWordsRule;

/**
 * A rule that gives hints on the use of Portuguese filler words.
 * The hints are only given when the percentage of filler words per paragraph exceeds the given limit.
 * A limit of 0 shows all used filler words. Direct speech or citation is excluded otherwise. 
 * This rule detects no grammar error but gives stylistic hints (default off).
 * @author Fred Kruse
 * @since 4.2
 */
public class PortugueseFillerWordsRule extends AbstractFillerWordsRule {

  private static final Set<String> fillerWords = new HashSet<>(Arrays.asList( "abundante", "acrescentou", "acrescidamente", 
      "adição", "agora", "ainda", "além", "algo", "algum", "alguma", "algumas", "alguns", "aparecer", 
      "aparentemente", "apenas", "apesar", "aproximadamente", "assim", "atrás", "atualmente", "automaticamente", 
      "bem", "bonito", "certamente", "certo", "claramente", "claro", "completam", "completamente", "completo", 
      "comumente", "consequentemente", "consistentemente", "continuamente", "contra", "contraste", "contudo", 
      "cuidado", "curto", "dependendo", "depois", "desigual", "determinado", "deve", "dever", "difícil", 
      "direito", "dúvida", "embora", "enquanto", "entanto", "ergo", "especial", "estranhamente", "eventualmente", 
      "evidentemente", "expressar", "extremamente", "fácil", "famoso", "feio", "felizmente", "francamente", 
      "frequência", "frequentemente", "geralmente", "graças", "impressionante", "impronunciável", "incomum", 
      "indizível", "infelizmente", "irrelevante", "irrelevantes", "já", "justo", "lento", "longo", "lugares", 
      "maior", "mais", "mas", "melhor", "mesmo", "muita", "muitas", "muito", "muitos", "múltipla", "nada", "não", 
      "natural", "naturalmente", "natureza", "nehumas", "nenhum", "nenhuma", "nenhuns", "nomeadamente", 
      "normalmente", "novo", "número", "nunca", "óbvio", "ocasionalmente", "outra", "outros", "para", "parente", 
      "particularmente", "pessoa", "pode", "poderia", "pois", "porém", "porque", "portanto", "possível", 
      "possivelmente", "pouca", "poucas", "pouco", "poucos", "prático", "precisas", "principalmente", "provável", 
      "provavelmente", "quaisquer", "qualquer", "quase", "rápido", "raramente", "razoavelmente", "realmente", 
      "recentemente", "relativamente", "repente", "sempre", "senão", "sentida", "sentidas", "sentido", "sentidos", 
      "siga", "significativo", "sim", "simples", "simplesmente", "sobre", "sozinho", "suave", "suavemente", 
      "substancialmente", "suficientemente", "tipo", "tornar", "tornaram", "tornou", "total", "totalmente", 
      "toda", "todas", "todo", "todos", "tudo", "ultrajante", "velho", "verdade", "vez", "vezes", "volta"
  ));
  
  public PortugueseFillerWordsRule(ResourceBundle messages, Language lang, UserConfig userConfig) {
    super(messages, lang, userConfig);
  }

  @Override
  public String getId() {
    return RULE_ID + "_PT";
  }

  @Override
  protected boolean isFillerWord(String token) {
    return fillerWords.contains(token);
  }

  @Override
  public boolean isException(AnalyzedTokenReadings[] tokens, int num) {
    if ("mas".equals(tokens[num].getToken()) && num >= 1 && ",".equals(tokens[num - 1].getToken())) {
      return true;
    }
    return false;
  }
  
}
