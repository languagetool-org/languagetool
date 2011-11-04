package de.danielnaber.languagetool.gui;

import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 * A resource bundle that uses its fallback resource bundle if the
 * value from the original bundle is null or empty.
 */
public class ResourceBundleWithFallback extends ResourceBundle {

  private final ResourceBundle bundle;
  private final ResourceBundle fallbackBundle;

  public ResourceBundleWithFallback(ResourceBundle bundle, ResourceBundle fallbackBundle) {
    this.bundle = bundle;
    this.fallbackBundle = fallbackBundle;
  }

  @Override
  public Object handleGetObject(String key) {
    final String string = bundle.getString(key);
    if (string.trim().isEmpty()) {
      return fallbackBundle.getString(key);
    }
    return string;
  }

  @Override
  public Enumeration<String> getKeys() {
    return bundle.getKeys();
  }

}
