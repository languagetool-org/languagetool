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

class GermanTestCase(LanguageTest.LanguageTest):

	def setUp(self):
		self.checker = TextChecker.TextChecker(grammar=None, falsefriends=None, \
			words=None, builtin=None, textlanguage="de", mothertongue="de", \
			max_sentence_length=20, debug_mode=0)
		return
		
	def testSomeRules(self):
		"""Some English rule checks. Requires a trained tagger."""

		self._check(u"Ich gehe daß er sieht", ExpMatch("DASS", 4, 12))
		self._check(u"Ich gehe.", None)
		self._check(u"Ich gehst.", ExpMatch("ICH", 0, 9))
		return

if __name__ == "__main__":
	unittest.main()
