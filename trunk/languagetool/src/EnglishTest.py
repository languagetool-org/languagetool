# -*- coding: iso-8859-1 -*-
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

import TextChecker

import unittest

class EnglishTestCase(unittest.TestCase):

	def setUp(self):
		self.checker = TextChecker.TextChecker(grammar=None, falsefriends=None, \
			words=None, builtin=None, textlanguage="en", mothertongue="de", \
			max_sentence_length=20)
		return
		
	def testSomeRules(self):
		"""Some rule checks. Requires a trained tagger."""
		err_count = 0

		self._check("A sentence without problems.", None)
		self._check("This is bigger then blah.", "COMP_THAN")
		self._check("English/German false friend: my chef", "CHEF")
		self._check("Whitespace,here it's lacking.", "WHITESPACE")
		return

	def _check(self, sentence, expectedError):
		(rule_matches, output, tagged_text) = self.checker.check(sentence)
		if expectedError == None:
			self.assertEqual(len(rule_matches), 0)
		else:
			self.assertEqual(len(rule_matches), 1)
			self.assertEqual(rule_matches[0].id, expectedError)
		return
