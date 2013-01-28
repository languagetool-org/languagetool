package org.languagetool.gui;

import org.languagetool.JLanguageTool;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(JLanguageTool.MESSAGE_BUNDLE);
  
	private Messages() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
