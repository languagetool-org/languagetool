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

class LanguageTest(unittest.TestCase):

	def _check(self, sentence, expectedErrors):
		(rule_matches, output, tagged_text) = self.checker.check(sentence)
		rule_matches.sort()
		if expectedErrors == None:
			if len(rule_matches) != 0:
				print "Expected no errors, found %d" % len(rule_matches)
				print "Sentence: %s" % sentence
				self.fail()
		elif isinstance(expectedErrors, list):
			if len(rule_matches) != len(expectedErrors):
				print "Expected %d errors, found %d" % (len(expectedErrors), len(rule_matches))
				print "Sentence: %s" % sentence
				self.fail()
			i = 0
			for expError in expectedErrors:
				self._checkError(sentence, rule_matches[i], expError)
				i = i + 1
		else:
			if len(rule_matches) != 1:
				print "Expected 1 error, found %d" % len(rule_matches)
				print "Sentence: %s" % sentence
				self.fail()
			self._checkError(sentence, rule_matches[0], expectedErrors)
		return

	def _checkError(self, sentence, rule_match, expectedError):
		self.assertEqual(rule_match.id, expectedError.error_type)
		if rule_match.from_pos != expectedError.from_pos or \
			rule_match.to_pos != expectedError.to_pos:
			print "Expected error from %d to %d, found error from %d to %d" % \
				(expectedError.from_pos, expectedError.to_pos, rule_match.from_pos, \
				rule_match.to_pos)
			print "Sentence: %s" % sentence
			self.fail()
		return

class ExpMatch:

	def __init__(self, error_type, from_pos, to_pos):
		self.error_type = error_type
		self.from_pos = from_pos
		self.to_pos = to_pos
		return
