# -*- coding: iso-8859-1 -*-
#!/usr/bin/python
# Copyright (C) 2003 Daniel Naber <daniel.naber@t-online.de>

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

import SentenceSplitter
import unittest

class SentenceSplitterTest(unittest.TestCase):

	def testSplit(self):
		self.s = SentenceSplitter.SentenceSplitter()

		l = self.s.split(None)
		self.assertEqual(len(l), 0)

		self.doTest("")
		self.doTest("This is a sentence.")
		self.doTest("This is a sentence. #And this is another one.")
		self.doTest("This is a sentence. #Isn't it? #Yes, it is.")
		self.doTest("This is e.g. Mr. Smith, who talks slowly... #But this is another sentence.")
		self.doTest("Chanel no. 5 is groovy.")
		self.doTest("Mrs. Jones gave Peter $4.5, to buy Chanel No 5. #He never came back.")
		self.doTest("On p. 6 there's nothing. #Another sentence.")
		self.doTest("Leave me alone!, he yelled. #Another sentence.")
		self.doTest("\"Leave me alone!\", he yelled.")
		self.doTest("'Leave me alone!', he yelled. #Another sentence.")
		self.doTest("'Leave me alone,' he yelled. #Another sentence.")
		self.doTest("This works on the phrase level, i.e. not on the word level.")
		self.doTest("Let's meet at 5 p.m. in the main street.")
		self.doTest("James comes from the U.K. where he worked as a programmer.")
		self.doTest("Don't split strings like U.S.A. please.")
		self.doTest("Don't split strings like U. S. A. either.")
		self.doTest("Don't split... #Well you know. #Here comes more text.")
		self.doTest("Don't split... well you know. #Here comes more text.")
		self.doTest('The "." should not be a delimiter in quotes.')
		self.doTest('"Here he comes!" she said.')
		self.doTest('"Here he comes!", she said.')
		self.doTest('"Here he comes." #But this is another sentence.')
		self.doTest('"Here he comes!". #That\'s what he said.')
		self.doTest('The sentence ends here. #(Not me.)')
		self.doTest("He won't. #Really.")
		self.doTest("He won't say no. #Not really.")
		self.doTest("He won't say no. 5 is better. #Not really.")
		self.doTest("They met at 5 p.m. on Thursday.")
		self.doTest("They met at 5 p.m. #It was Thursday.")
		self.doTest("This is it: a test.")
		# known not to work:
		#self.doTest("This is it: #A final test.")
		# two returns -> paragraph -> new sentence:
		self.doTest("He won't\n\n#Really.")
		# Some people make two spaces after sentence end:
		self.doTest("This is a sentence.  #And this is another one.")
		# Missing space after sentence end:
		self.doTest("James is from the Ireland!#He lives in Spain now.")
		# From the abbreviation list:
		self.doTest("Jones Bros. have built a succesful company.")
		# Doesn't work:
		#self.doTest("James is from the U.K. #He lives in Spain now.")

		return

	def doTest(self, s):
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
		#print "correct_result=%s" % correct_result
		#print "l=%s" % l
		self.assertEqual(l, correct_result)
		return

if __name__ == "__main__":
	unittest.main()
