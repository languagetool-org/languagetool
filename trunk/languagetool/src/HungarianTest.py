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

class HungarianTestCase(LanguageTest.LanguageTest):

	def setUp(self):
		self.checker = TextChecker.TextChecker(grammar=None, falsefriends=None, \
			words=None, builtin=None, textlanguage="hu", mothertongue="de", \
			max_sentence_length=20, debug_mode=0)
		return
		
	def testSomeRules(self):
		"""Some English rule checks. Requires a trained tagger."""
		self._check(u"Én mész moziba", ExpMatch("EN", 0, 7))
		self._check(u"Õk soha nem fogják megtanulni.", None)
		return

if __name__ == "__main__":
	unittest.main()
