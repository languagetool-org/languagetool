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

import re
import unittest

import Chunker

class LocalRules:

	def __init__(self, rule_list):
		self.rules = rule_list
		return

class ChunkerTestCase(unittest.TestCase):

	def testChunking(self):
		c = Chunker.Chunker()
		r1 = Chunker.Rule("NP1: AT0 NN1 NN1")
		r2 = Chunker.Rule("NP2: AT0 NN1")
		rules = LocalRules([r1, r2])
		c.setRules(rules)

		tagged_text = self._makeList("Blah/XX the/AT0 house/NN1 foo/YY")
		chunks = c.chunk(tagged_text)
		self.assertEqual(chunks, [(2, 4, 'NP2')])
		
		tagged_text = self._makeList("Blah/XX house/NN1 foo/YY")
		chunks = c.chunk(tagged_text)
		self.assertEqual(chunks, [])

		tagged_text = self._makeList("the/AT0 summer/NN1 house/NN1 foo/YY2")
		chunks = c.chunk(tagged_text)
		self.assertEqual(chunks, [(0, 4, 'NP1')])
	
		# more than one chunk:

		tagged_text = self._makeList("the/AT0 summer/NN1 is/VB a/AT0 hit/NN1")
		chunks = c.chunk(tagged_text)
		self.assertEqual(chunks, [(0, 2, 'NP2'), (6, 8, 'NP2')])

		tagged_text = self._makeList("the/AT0 summer/NN1 a/AT0 hit/NN1")
		chunks = c.chunk(tagged_text)
		self.assertEqual(chunks, [(0, 2, 'NP2'), (4, 6, 'NP2')])

		return

	def _makeList(self, s):
		parts = re.split("(\s+)", s)
		l = []
		for part in parts:
			word = None
			word_norm = None
			tag = None
			pair = re.split("/", part)
			if len(pair) == 2:
				word, tag = pair
				word_norm = word
			else:
				word = pair[0]
			l.append((word, word_norm, tag))
		return l
