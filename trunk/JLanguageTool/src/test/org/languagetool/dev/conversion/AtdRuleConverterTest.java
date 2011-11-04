package de.danielnaber.languagetool.dev.conversion;

import junit.framework.TestCase;

import java.util.HashMap;

public class AtdRuleConverterTest extends TestCase {

	public void testParseRule() {
		AtdRuleConverter converter = new AtdRuleConverter();
		converter.setFileType("avoid");
		HashMap<String,String> rule = converter.parseRule("bad phrase	don't use this phrase");
		assertEquals(rule.get("pattern"), "bad phrase");
		assertEquals(rule.get("explanation"), "don't use this phrase");
		converter.setFileType("default");
		rule = converter.parseRule("my pants is::word=my pants are");
		assertEquals(rule.get("pattern"),"my pants is");
		assertEquals(rule.get("word"),"my pants are");
	}
	
	
}