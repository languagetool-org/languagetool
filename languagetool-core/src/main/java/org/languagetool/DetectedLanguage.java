/* LanguageTool, a natural language style checker
 * Copyright (C) 2018 Daniel Naber (http://www.danielnaber.de)
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
 *
 */
package org.languagetool;

/**
 * @since 4.2
 */
public class DetectedLanguage {

  private final Language givenLanguage;
  private final Language detectedLanguage;
  private final float detectionConfidence;

  public DetectedLanguage(Language givenLanguage, Language detectedLanguage) {
    this(givenLanguage, detectedLanguage, 1.0f);
  }

  /**
   * @since 4.4
   */
  public DetectedLanguage(Language givenLanguage, Language detectedLanguage, float detectionConfidence) {
    this.givenLanguage = givenLanguage;
    this.detectedLanguage = detectedLanguage;
    this.detectionConfidence = detectionConfidence;
  }

  public Language getGivenLanguage() {
    return givenLanguage;
  }

  public Language getDetectedLanguage() {
    return detectedLanguage;
  }

  /**
   * @since 4.4
   */
  public float getDetectionConfidence() {
    return detectionConfidence;
  }
  
  @Override
  public String toString() {
    return detectedLanguage.getShortCodeWithCountryAndVariant();
  }
}
