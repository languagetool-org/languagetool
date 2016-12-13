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
import org.languagetool.rules.Categories;
import org.languagetool.rules.Example;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.WordRepeatBeginningRule;

/**
 * Adds a list Portuguese adverbs to {@link WordRepeatBeginningRule}.
 * 
 * @since 3.6
 * localized by @author Tiago F. Santos from the german version
 */
public class PortugueseWordRepeatBeginningRule extends WordRepeatBeginningRule {
  
  private static final Set<String> ADVERBS = new HashSet<>(Arrays.asList(
          "Abaixo", "Acaso", "Acima", "Acolá",
          "Ademais", "Adentro", "Adiante", "Adicionalmente",
          "Afinal", "Afora", "Agora", "Aí", "Ainda",
          "Além", "Algures", "Ali", "Aliás",
          "Amanhã", "Amiúde", "Antigamente", "Aonde",
          "Apenas", "Apesar", "Aquém", "Aqui", "Assaz",
          "Assim", "Até", "Atrás", "Bastante", "Bem",
          "Bondosamente", "Breve", "Cá", "Casualmente",
          "Cedo", "Certamente", "Certo", "Constantemente",
          "Cuidadosamente", "Dantes", "Debaixo", "Debalde",
          "Decerto", "Defronte", "Demais", "Demasiado",
          "Dentro", "Depois", "Depressa", "Detrás", "Devagar",
          "Doravante", "E", "Efetivamente", "Embaixo",
          "Embora", "Enfim", "Então", "Entrementes",
          "Exclusivamente", "Externamente", "Fora",
          "Frequentemente", "Generosamente", "Hoje",
          "Imediatamente", "Inclusivamente", "Inda", "Já",
          "Jamais", "Lá", "Logo", "Longe", "Mais", "Mal",
          "Mas", "Melhor", "Menos", "Mesmo", "Muito", "Não",
          "Nem", "Nenhures", "Nunca", "Onde", "Ontem", "Ora",
          "Ou", "Outra", "Outro", "Outrora", "Outrossim",
          "Perto", "Pior", "Porventura", "Possivelmente",
          "Pouco", "Primeiramente", "Primeiro",
          "Principalmente", "Provavelmente", "Provisoriamente",
          "Quanto", "Quão", "Quase", "Quiçá", "Realmente",
          "Salvo", "Seguidamente", "Sempre", "Senão", "Será",
          "Sim", "Simplesmente", "Só", "Sobremaneira",
          "Sobremodo", "Sobretudo", "Somente", "Sucessivamente",
          "Talvez", "Também", "Tampouco", "Tanto", "Tão",
          "Tarde", "Ultimamente", "Unicamente"
  ));

  public PortugueseWordRepeatBeginningRule(ResourceBundle messages, Language language) {
    super(messages, language);
    super.setCategory(Categories.STYLE.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Style);
    addExamplePair(Example.wrong("Além disso, a rua é quase completamente residêncial. <marker>Além</marker> disso, foi chamada em nome de um poeta."),
                   Example.fixed("Além disso, a rua é quase completamente residêncial. <marker>Foi</marker> chamada em nome de um poeta."));
  }
  
  @Override
  public String getId() {
    return "PORTUGUESE_WORD_REPEAT_BEGINNING_RULE";
  }
  
  @Override
  protected boolean isAdverb(AnalyzedTokenReadings token) {
    return ADVERBS.contains(token.getToken());
  }

}
