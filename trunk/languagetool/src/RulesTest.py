#!/usr/bin/python
# Test cases for Rule.py
#$rcs = ' $Id: RulesTest.py,v 1.4 2004-07-12 20:49:58 dnaber Exp $ ' ;
#
# LanguageTool -- A Rule-Based Style and Grammar Checker
# Copyright (C) 2002,2003,2004 Daniel Naber <daniel.naber@t-online.de>
#
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 2.1 of the License, or (at your option) any later version.
#
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with this library; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

import unittest
import Rules
import os
import sys

sys.path.append(os.path.join("python_rules"))
import allSentenceLengthRule
import enWordRepeatRule
import enAvsAnRule

class RuleTestCase(unittest.TestCase):

    def setUp(self):
		self.rule = Rules.PatternRule(None)
		self.rule.setVars("TEST1", '"word" (VB|TST)', "Test message 1.", 0, 0, \
			"Good example.", "Bad example.", 0, 5, "en")
		# negation:
		self.rule2 = Rules.PatternRule(None)
		self.rule2.setVars("TEST2", '"word" ^(VB|TST)', "Test message 2.", 0, 0, \
			"Good example.", "Bad example.", 0, 5, "en")
		# negation at the beginning:
		self.rule3 = Rules.PatternRule(None)
		self.rule3.setVars("TEST3", '^"word" (VB|TST)', "Test message 3.", 0, 0, \
			"Good example.", "Bad example.", 0, 5, "en")
		return

    def testConstructor(self):
		self.assertEqual(self.rule.rule_id, "TEST1")
		self.assertEqual(len(self.rule.tokens), 2)
		self.assertEqual(self.rule2.rule_id, "TEST2")
		self.assertEqual(len(self.rule.tokens), 2)
		self.assertEqual(self.rule3.rule_id, "TEST3")
		self.assertEqual(len(self.rule.tokens), 2)
		return

    def testSentenceLengthRule(self):
		r = allSentenceLengthRule.allSentenceLengthRule()
		r.setMaxLength(3)

		# just below the limit:
		warnings = r.match([('x','x','T'),('x','x','T'),('x','x','T')])
		self.assertEqual(len(warnings), 0)

		# just on the limit:
		warnings = r.match([('x','x','T'),('x','x','T'),('x','x','T'),('x','x','T')])
		self.assertEqual(len(warnings), 1)
		assert(warnings[0].toXML().startswith('<error from="3" to="4">'))
		r.setMaxLength(60)
		warnings = r.match([('x','x','T'),('x','x','T'),('x','x','T'),('x','x','T')])
		self.assertEqual(len(warnings), 0)
		r.setMaxLength(3)

		# whitespace is okay:
		warnings = r.match([('  ',None,None),('x','x','T'),('x','x','T'),('x','x','T')])
		self.assertEqual(len(warnings), 0)

		# much longer than the limit:
		warnings = r.match([('x','x','T'),('x','x','T'),('x','x','T'),('x','x','T'),\
			('x','x','T'),('x','x','T'),('x','x','T')])
		self.assertEqual(len(warnings), 1)

		return

    def testAvsAnRule(self):
		r = enAvsAnRule.enAvsAnRule()
		# okay:
		warnings = r.match([('A','A','DET'),(' ',None,None),('test','test','NN')], [])
		self.assertEqual(len(warnings), 0)
		warnings = r.match([('a','a','DET'),(' ',None,None),('test','test','NN')], [])
		self.assertEqual(len(warnings), 0)
		warnings = r.match([('an','an','DET'),(' ',None,None),('idea','idea','NN')], [])
		self.assertEqual(len(warnings), 0)

		# okay (exceptions list):
		warnings = r.match([('a','a','DET'),(' ',None,None),('university','university','NN')], [])
		self.assertEqual(len(warnings), 0)
		warnings = r.match([('an','an','DET'),(' ',None,None),('hour','hour','NN')], [])
		self.assertEqual(len(warnings), 0)

		# wrong:
		warnings = r.match([('An','An','DET'),(' ',None,None),('test','test','NN')], [])
		self.assertEqual(len(warnings), 1)
		warnings = r.match([('an','an','DET'),(' ',None,None),('test','test','NN')], [])
		self.assertEqual(len(warnings), 1)
		warnings = r.match([('a','a','DET'),(' ',None,None),('idea','idea','NN')], [])
		self.assertEqual(len(warnings), 1)

		# wrong (exceptions list):
		warnings = r.match([('an','an','DET'),(' ',None,None),('university','university','NN')], [])
		self.assertEqual(len(warnings), 1)
		warnings = r.match([('a','a','DET'),(' ',None,None),('hour','hour','NN')], [])
		self.assertEqual(len(warnings), 1)

		return
		
    def testWhitespaceRule(self):
		r = Rules.WhitespaceRule()
	
		# okay:
		warnings = r.match([('blah','blah','XX'),('?',None,None)])
		self.assertEqual(len(warnings), 0)
		warnings = r.match([('3.14','3.14','XX'),('?',None,None)])
		self.assertEqual(len(warnings), 0)

		# error - whitespace before punctuation:
		warnings = r.match([('blah','blah','XX'),(' ',None,None),('.',None,None)])
		self.assertEqual(len(warnings), 1)
		warnings = r.match([('blah','blah','XX'),(' ',None,None),('?',None,None)])
		self.assertEqual(len(warnings), 1)
		warnings = r.match([('blah','blah','XX'),(' ',None,None),('...',None,None)])
		self.assertEqual(len(warnings), 1)
		warnings = r.match([('blah','blah','XX'),(' ',None,None),('?!',None,None)])
		self.assertEqual(len(warnings), 1)

		# both errors
		warnings = r.match([('blah','blah','XX'),(' ',None,None),(',',None,None),('blah','blah','XX')])
		self.assertEqual(len(warnings), 2)

		# okay:
		warnings = r.match([('blah','blah','XX'),('?',None,None),(None,None,'SENT_END')])
		self.assertEqual(len(warnings), 0)

		# error - no whitespace after punctuation:
		warnings = r.match([('blah','blah','XX'),('?',None,None),('foo','foo','YY')])
		self.assertEqual(len(warnings), 1)

		return

    def testWordRepeat(self):
		r = enWordRepeatRule.enWordRepeatRule()
	
		warnings = r.match([('blah','blah','XX'),(' ',None,None),('blahbla','blahbla','YY')], [])
		self.assertEqual(len(warnings), 0)
		
		warnings = r.match([('blah','blah','XX'),(' ',None,None),('blah','blah','YY')], [])
		self.assertEqual(len(warnings), 1)
		warnings = r.match([('blah','blah','XX'),(' ',None,None),('BLAH','BLAH','XX')], [])
		self.assertEqual(len(warnings), 1)

		return

    def testPatternRuleMatch(self):

		# rule 1:
		
		res_list = self.rule.match([('', None, 'SENT_START'),
			('word', 'word', 'XX'),(' ', None, None),('bla', 'bla', 'VB')], 0)
		self.assertEqual(len(res_list), 1)
		self.assertEqual(res_list[0].toXML(), '<error from="0" to="8">Test message 1.</error>')

		res_list = self.rule.match([('no', 'no', 'XX'),('foo', 'foo', 'VB')], 0)
		self.assertEqual(len(res_list), 0)

		res_list = self.rule.match([], 0)
		self.assertEqual(len(res_list), 0)

		res_list = self.rule.match([('word', 'word', 'XX')], 0)
		self.assertEqual(len(res_list), 0)
		
		# rule 2:
		
		res_list = self.rule2.match([('word', 'word', 'XX'),('', None, None),('xxx', 'xxx', 'VBX')], 0)
		self.assertEqual(len(res_list), 1)

		# rule 3:
		
		res_list = self.rule3.match([('foo', 'foo', 'XX'),(' ', None, None),('xxx', 'xxx', 'VB')], 0)
		self.assertEqual(len(res_list), 1)
		return

class RuleMatchTestCase(unittest.TestCase):

	def testCompare(self):
		r1 = Rules.RuleMatch("ONE", 1, 2, 0, 0, "fake1", 0)
		r2 = Rules.RuleMatch("ONE", 2, 3, 0, 0, "fake2", 0)
		assert(r1 < r2)
		r3 = Rules.RuleMatch("ONE", 1, 3, 0, 0, "fake3", 0)
		assert(r1 == r3)
		assert(r2 > r3)
		return

class TokenTestCase(unittest.TestCase):

	def testToken(self):

		token = Rules.Token('NN')
		self.assertEqual(token.token, "NN")
		assert(not token.negation)
		assert(token.is_tag)
		assert(not token.is_word)
		assert(not token.is_chunk)
		assert(token.simple_token)

		token = Rules.Token('"word"')
		self.assertEqual(token.token, "word")
		assert(not token.negation)
		assert(not token.is_tag)
		assert(token.is_word)
		assert(not token.is_chunk)
		assert(token.simple_token)

		token = Rules.Token("^(NN)")
		self.assertEqual(token.token, "(NN)")
		assert(token.negation)
		assert(token.is_tag)
		assert(not token.is_word)
		assert(not token.is_chunk)
		assert(not token.simple_token)		# b/c of the parenthesis

		token = Rules.Token('^"word"')
		self.assertEqual(token.token, "word")
		assert(token.negation)
		assert(not token.is_tag)
		assert(token.is_word)
		assert(not token.is_chunk)
		assert(token.simple_token)

		token = Rules.Token('_NP')
		self.assertEqual(token.token, "NP")
		assert(not token.negation)
		assert(not token.is_tag)
		assert(not token.is_word)
		assert(token.is_chunk)
		assert(token.simple_token)

		token = Rules.Token("(AA|BB|CC)")
		self.assertEqual(token.token, "(AA|BB|CC)")
		assert(not token.negation)
		assert(token.is_tag)
		assert(not token.is_word)
		assert(not token.is_chunk)
		assert(not token.simple_token)		# b/c of the parenthesis
		return

if __name__ == "__main__":
    unittest.main()
