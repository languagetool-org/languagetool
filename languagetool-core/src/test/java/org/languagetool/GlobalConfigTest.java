package org.languagetool;

import org.junit.Test;
import java.io.File;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals ;

public class GlobalConfigTest {

  /* This tests the equals method */
  @Test
  public void testEquals() {
    //Initialise the objects
    GlobalConfig config1 = new GlobalConfig();
    GlobalConfig config2 = new GlobalConfig();

    // Both objects are initially empty and should be equal
    assertEquals(config1, config2);

    config1.setGrammalecteServer("server1");
    config2.setGrammalecteServer("server2");

    //Check if they are different servers
    assertNotEquals(config1, config2);

    //Set config1 to the same server
    config1.setGrammalecteServer("server2");

    //Check for same server
    assertEquals(config1, config2);

    config1.setBeolingusFile(new File("file1"));
    assertNotEquals(config1, config2);
  }

  /* This tests the hashCode method */
  @Test
  public void testHashCode() {
    GlobalConfig config1 = new GlobalConfig();
    GlobalConfig config2 = new GlobalConfig();

    // Initially, both objects are empty, so their hash codes should be equal.
    assertEquals(config1.hashCode(), config2.hashCode());

    config1.setGrammalecteServer("server1");
    config2.setGrammalecteServer("server2");

    // After setting different servers, their hash codes should not be equal.
    assertNotEquals(config1.hashCode(), config2.hashCode());

    config1.setGrammalecteServer("server2");

    // When both objects have the same server, their hash codes should be equal again.
    assertEquals(config1.hashCode(), config2.hashCode());

    config1.setBeolingusFile(new File("file1"));

    // After setting different Beolingus files, their hash codes should not be equal.
    assertNotEquals(config1.hashCode(), config2.hashCode());
  }

}
