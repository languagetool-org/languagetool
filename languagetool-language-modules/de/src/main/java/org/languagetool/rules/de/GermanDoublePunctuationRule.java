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
package org.languagetool.rules.de;

import java.util.ResourceBundle;

import org.languagetool.rules.DoublePunctuationRule;
import org.languagetool.rules.Example;
import org.languagetool.tools.Tools;

/**
 * Double punctuation rule with German-specific error message.
 */
public class GermanDoublePunctuationRule extends DoublePunctuationRule {
  
  public GermanDoublePunctuationRule(ResourceBundle messages) {
    super(messages);
    setUrl(Tools.getUrl("http://www.canoonet.eu/services/GermanSpelling/Amtlich/Interpunktion/pgf101-105.html#pgf103"));
    addExamplePair(Example.wrong("Sein Vater ist Regierungsrat <marker>a. D..</marker>"),
                   Example.fixed("Sein Vater ist Regierungsrat <marker>a. D.</marker>"));
  }
  
  @Override
  public String getId() {
    return "DE_DOUBLE_PUNCTUATION";
  }

  @Override
  protected String getDotMessage() {
    return "Zwei aufeinander folgende Punkte. Auch wenn ein Satz mit einer Abkürzung endet, " +
            "endet er nur mit einem Punkt (§103 Regelwerk).";
  }
    
}
