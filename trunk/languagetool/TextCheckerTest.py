#!/usr/bin/python
# Copyright (C) 2003 Daniel Naber <daniel.naber@t-online.de>
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or (at
# your option) any later version.
#
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
# USA

import TextChecker

import unittest

class TextCheckerTest(unittest.TestCase):

	def testSomeRules(self):
		checker = TextChecker.TextChecker(grammar=None, falsefriends=None, \
			words=None, builtin=None, textlanguage="en", mothertongue="de", \
			max_sentence_length=20)
		err_count = 0

		sentence = "A sentence without problems."
		(rule_matches, xml_err, tagged_text) = checker.check(sentence)
		self.assertEqual(len(rule_matches), 0)

		sentence = "This is bigger then blah."
		(rule_matches, xml_err, tagged_text) = checker.check(sentence)
		self.assertEqual(len(rule_matches), 1)
		self.assertEqual(rule_matches[0].id, "COMP_THAN")

		sentence = "English/German false friend: my chef"
		(rule_matches, xml_err, tagged_text) = checker.check(sentence)
		self.assertEqual(len(rule_matches), 1)
		self.assertEqual(rule_matches[0].id, "CHEF")

		sentence = "Whitespace,here it's lacking."
		(rule_matches, xml_err, tagged_text) = checker.check(sentence)
		self.assertEqual(len(rule_matches), 1)
		self.assertEqual(rule_matches[0].id, "WHITESPACE")

		return

if __name__ == "__main__":
	unittest.main()
