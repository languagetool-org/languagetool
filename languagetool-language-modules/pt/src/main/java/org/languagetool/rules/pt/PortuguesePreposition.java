/* LanguageTool, a natural language style checker
 * Copyright (C) 2023 Pedro Goulart
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

import java.util.Objects;

public class PortuguesePreposition {
  String value;
  String contractedOnset;

  PortuguesePreposition(String fullForm) {
    this.value = parseContraction(fullForm);
    switch(value) {
      case "em": contractedOnset = "n"; break;
      case "de": contractedOnset = "d"; break;
      case "a": contractedOnset = "a"; break;
      default: contractedOnset = value + " ";
    }
  }

  @Override
  public String toString() {
    return value;
  }

  private String parseContraction(String fullForm) {
    if (fullForm.startsWith("d")) {
      return "de";
    }
    if (fullForm.equals("em") || fullForm.startsWith("n")) {
      return "em";
    }
    if (fullForm.startsWith("a") || fullForm.startsWith("à")) {
      return "a";
    }
    return fullForm;
  }

  public String contractWith(String article) {
    if (Objects.equals(article, "0")) {
      return value;
    }
    String contracted = contractedOnset + article;
    contracted = contracted.replace("aa", "à");
    return contracted;
  }
}
