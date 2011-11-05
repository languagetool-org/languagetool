package org.languagetool.openoffice;

import junit.framework.TestCase;

public class KhmerDetectorTest extends TestCase {
  
  public void testIsKhmer() {
    final KhmerDetector detector = new KhmerDetector();
    
    assertTrue(detector.isKhmer("ប៉ុ"));
    assertTrue(detector.isKhmer("ប៉ុន្តែ​តើ"));
    assertTrue(detector.isKhmer("ហើយដោយ​ព្រោះ​"));
    
    assertFalse(detector.isKhmer("Hallo"));
    assertFalse(detector.isKhmer("öäü"));

    assertFalse(detector.isKhmer(""));
    try {
      assertFalse(detector.isKhmer(null));
      fail();
    } catch (NullPointerException expected) {}
  }
  
}
