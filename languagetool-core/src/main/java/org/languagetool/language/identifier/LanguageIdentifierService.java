/*
 * LanguageTool, a natural language style checker 
 * Copyright (c) 2022.  Stefan Viol (https://stevio.de)
 *  
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *  
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  USA
 */

package org.languagetool.language.identifier;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import org.languagetool.Languages;

import java.io.File;
import java.util.List;

@Slf4j
public enum LanguageIdentifierService {

  INSTANCE;

  private LanguageIdentifier defaultIdentifier = null;
  private LanguageIdentifier simpleIdentifier = null;

  /**
   * @param maxLength          - the maximum of characters that will be considered - can help with performance.
   *                           If 0 the default value of 1000 is used.
   *                           Don't use values between 1-100, as this would decrease accuracy.
   * @param ngramLangIdentData - the ngramLangIdentData file, if {@code null} ngram will not be initialized.
   * @param fasttextBinary     - the fasttext binary file, if {@code null} fasttext will not be initialized.
   * @param fasttextModel      - the fasttext model file, if {@code null} fasttext will not be initialized.
   * @return new {@code LanguageIdentifier} or existing if already initialized.
   * @since 5.8
   */
  public synchronized LanguageIdentifier getDefaultLanguageIdentifier(int maxLength,
                                                                      @Nullable File ngramLangIdentData,
                                                                      @Nullable File fasttextBinary,
                                                                      @Nullable File fasttextModel) {
    if (defaultIdentifier == null) {
      DefaultLanguageIdentifier defaultIdentifier = maxLength > 0 ? new DefaultLanguageIdentifier(maxLength) : new DefaultLanguageIdentifier();
      defaultIdentifier.enableNgrams(ngramLangIdentData);
      defaultIdentifier.enableFasttext(fasttextBinary, fasttextModel);
      this.defaultIdentifier = defaultIdentifier;
    }
    return this.defaultIdentifier;
  }

  /**
   * @param preferredLangCodes - a list of language codes for that a spellchecker will be initialized.
   *                           If {@code null} all spellcheckers will be used.
   * @return new {@code LanguageIdentifier} or existing if already initialized.
   * @since 5.8
   */
  public synchronized LanguageIdentifier getSimpleLanguageIdentifier(@Nullable List<String> preferredLangCodes) {
    if (simpleIdentifier == null) {
      if (preferredLangCodes == null) {
        this.simpleIdentifier = new SimpleLanguageIdentifier();
      } else {
        this.simpleIdentifier = new SimpleLanguageIdentifier(preferredLangCodes);
      }
    }
    return this.simpleIdentifier;
  }

  /**
   * Try to get an already initialized LanguageIdentifier
   * @return defaultIdentifier or if null simpleIdentifier or null
   */
  @Nullable
  public LanguageIdentifier getInitialized() {
    if (defaultIdentifier != null) {
      return this.defaultIdentifier;
    } else if (simpleIdentifier != null) {
      return this.simpleIdentifier;
    } else {
      return null;
    }
  }

  public boolean canLanguageBeDetected(String langCode, List<String> additionalLanguageCodes) {
    return Languages.isLanguageSupported(langCode) || additionalLanguageCodes.contains(langCode);
  }

  /**
   * @param type - option: "default", "simpel, or both to clear the identifiers
   * @return {@code LanguageIdentifierService} instance
   */
  @TestOnly
  public LanguageIdentifierService clearLanguageIdentifier(String type) {
    switch (type) {
      case "default":
        this.defaultIdentifier = null;
        break;
      case "simple":
        this.simpleIdentifier = null;
        break;
      case "both":
        this.simpleIdentifier = null;
        this.defaultIdentifier = null;
        break;
      default:
        break;
    }
    return this;
  }
}
