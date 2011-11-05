package org.languagetool.openoffice;

/**
 * Helps detecting Khmer strings by their Unicode range.
 */
class KhmerDetector {
  
  private static final int MAX_CHECK_LENGTH = 100;
  
  boolean isKhmer(String str) {
    final int maxCheckLength = Math.min(str.length(), MAX_CHECK_LENGTH);
    for (int i = 0; i < maxCheckLength; i++) {
      final char ch = str.charAt(i);
      final int numericValue = ch;
      if (numericValue >= 6016 && numericValue <= 6143) {
        return true;
      }
    }
    return false;
  }
  
}
