# -*- coding: iso-8859-1 -*-
# Copyright (C) 2003,2004 Daniel Naber <daniel.naber@t-online.de>

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

import os

import SentenceSplitter
import unittest

class SentenceSplitterTestCase(unittest.TestCase):

	def testSplit(self):
		self.s = SentenceSplitter.SentenceSplitter()

		l = self.s.split(None)
		self.assertEqual(len(l), 0)

		self._doTest("")
		self._doTest("This is a sentence.")
		self._doTest("This is a sentence. #And this is another one.")
		self._doTest("This is a sentence. #Isn't it? #Yes, it is.")
		self._doTest("This is e.g. Mr. Smith, who talks slowly... #But this is another sentence.")
		self._doTest("Chanel no. 5 is groovy.")
		self._doTest("Mrs. Jones gave Peter $4.5, to buy Chanel No 5. #He never came back.")
		self._doTest("On p. 6 there's nothing. #Another sentence.")
		self._doTest("Leave me alone!, he yelled. #Another sentence.")
		self._doTest("\"Leave me alone!\", he yelled.")
		self._doTest("'Leave me alone!', he yelled. #Another sentence.")
		self._doTest("'Leave me alone,' he yelled. #Another sentence.")
		self._doTest("This works on the phrase level, i.e. not on the word level.")
		self._doTest("Let's meet at 5 p.m. in the main street.")
		self._doTest("James comes from the U.K. where he worked as a programmer.")
		self._doTest("Don't split strings like U.S.A. please.")
		self._doTest("Don't split strings like U. S. A. either.")
		self._doTest("Don't split... #Well you know. #Here comes more text.")
		self._doTest("Don't split... well you know. #Here comes more text.")
		self._doTest('The "." should not be a delimiter in quotes.')
		self._doTest('"Here he comes!" she said.')
		self._doTest('"Here he comes!", she said.')
		self._doTest('"Here he comes." #But this is another sentence.')
		self._doTest('"Here he comes!". #That\'s what he said.')
		self._doTest('The sentence ends here. #(Not me.)')
		self._doTest("He won't. #Really.")
		self._doTest("He won't say no. #Not really.")
		self._doTest("He won't say no. 5 is better. #Not really.")
		self._doTest("They met at 5 p.m. on Thursday.")
		self._doTest("They met at 5 p.m. #It was Thursday.")
		self._doTest("This is it: a test.")
		# known not to work:
		#self._doTest("This is it: #A final test.")
		# two returns -> paragraph -> new sentence:
		self._doTest("He won't\n\n#Really.")
		# Some people make two spaces after sentence end:
		self._doTest("This is a sentence.  #And this is another one.")
		# Missing space after sentence end:
		self._doTest("James is from the Ireland!#He lives in Spain now.")
		# From the abbreviation list:
		self._doTest("Jones Bros. have built a succesful company.")
		# Doesn't work:
		#self._doTest("James is from the U.K. #He lives in Spain now.")

		return

	def _doTest(self, s):
		s_copy = s.replace("#", "")
		l = self.s.split(s_copy)
		correct_result = s.split("#")
		# ignore leading/trailing whitespace differences:
		i = 0
		for item in l:
			l[i] = l[i].strip()
			i = i + 1
		i = 0
		for item in correct_result:
			correct_result[i] = correct_result[i].strip()
			i = i + 1
		self.assertEqual(l, correct_result)
		return
