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
package org.languagetool.rules.pt;

import org.languagetool.rules.AbstractCompoundRule;
import org.languagetool.rules.CompoundRuleData;

import java.io.IOException;
import java.util.ResourceBundle;

/**
 * Checks that compounds (if in the list) are not written as separate words.
 * @since 2.6
 */
public class PostReformPortugueseCompoundRule extends AbstractCompoundRule {

  private static final CompoundRuleData compoundData = new CompoundRuleData("/pt/post-reform-compounds.txt");

  public PostReformPortugueseCompoundRule(ResourceBundle messages) throws IOException {    
    super(messages,
            "Esta palavra é hifenizada.",
            "Esta palavra é escrita em conjunto.",
            "Esta palavra é uma palavra ou com um hífen.",
            "Juntos grafias de palavras");
  }

  @Override
  public String getId() {
    return "PT_COMPOUNDS_PRE_REFORM";
  }

  @Override
  public String getDescription() {
    return "Juntos ortografia de palavras, por exemplo 'CD-ROM' em vez de 'CD ROM'";
  }

  @Override
  protected CompoundRuleData getCompoundRuleData() {
    return compoundData;
  }
}
