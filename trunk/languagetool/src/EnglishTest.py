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
import LanguageTest
from LanguageTest import ExpMatch

class EnglishTestCase(LanguageTest.LanguageTest):

	def setUp(self):
		self.checker = TextChecker.TextChecker(grammar=None, falsefriends=None, \
			words=None, builtin=None, textlanguage="en", mothertongue="de", \
			max_sentence_length=20, debug_mode=0)
		return
		
	def testSomeRules(self):
		"""Some English rule checks. Requires a trained tagger."""

		self._check("A sentence without problems.", None)
		self._check("This is bigger then blah.", ExpMatch("COMP_THAN", 15, 19))
		self._check("English/German false friend: my chef", ExpMatch("CHEF", 32, 36))
		self._check("Whitespace,here it's lacking.", ExpMatch("WHITESPACE", 11, 12))
		
		self._check("he good good.", ExpMatch("WORD_REPEAT", 7, 12))

		self._check("I ask you because of him.", None)
		self._check("Of cause not.", ExpMatch("OF_CAUSE", 3, 8))
		self._check("he is nice.", None)
		
		self._check("This is a stoopid test.", None)
		# TODO: error not detected:
		self._check("The baseball team are established.", None)
		
		self._check("I definitely think is should be less than four years.", 
			ExpMatch("IS_SHOULD", 19, 21))
			
		self._check("Peter's car is bigger then mine, and this isa spelling error.",
			ExpMatch("COMP_THAN", 22, 26))

		self._check("Peter's car is bigger then mine, and and a word repeat.",
			[ExpMatch("COMP_THAN", 22, 26), ExpMatch("WORD_REPEAT", 34, 38)])

		return

if __name__ == "__main__":
	unittest.main()
