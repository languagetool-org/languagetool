#!/usr/bin/python
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

import unittest
import Tagger

import os
import sys

class TaggerTestCase(unittest.TestCase):

	FILENAME_WORDS = os.path.join(sys.path[0], "data", "tag_test_words")
	FILENAME_SEQ1 = os.path.join(sys.path[0], "data", "tag_test_sequences1")
	FILENAME_SEQ2 = os.path.join(sys.path[0], "data", "tag_test_sequences2")
	
	def cleanList(self, l):
		"""Return a copy of the list with 'None' elements (e.g. whitespace)
		removed. Also, only the first and last element of each triple is 
		copied."""
		new_list = []
		for el in l:
			if el[1]:
				new_list.append((el[0], el[2]))
		return new_list
			
	def cleanListAll(self, l):
		"""Return a copy of the list with 'None' elements (e.g. whitespace)
		removed. Also, only the last element of each triple is copied."""
		new_list = []
		for el in l:
			if el[1]:
				new_list.append(el[2])
		return new_list

	def tag(self, learn_text, text):

		# build data:
		tagger = Tagger.Tagger("en", self.FILENAME_WORDS, self.FILENAME_SEQ1, self.FILENAME_SEQ2)
		tagger.deleteData()
		tagger.bindData()
		tagger.buildDataFromString(learn_text)
		tagger.commitData()
		tagger = None

		# tag text:
		tagger2 = Tagger.Tagger("en", self.FILENAME_WORDS, self.FILENAME_SEQ1, self.FILENAME_SEQ2)
		tagger2.bindData()
		res = tagger2.tagText(text)
		res = self.cleanList(res)
		tagger2.deleteData()

		return res

	def testExpandEntities(self):
		tagger = Tagger.Text("en", None)
		r = tagger.expandEntities("")
		self.assertEqual(r, "")
		r = tagger.expandEntities("bla &amp;&amp;")
		self.assertEqual(r, "bla &&")
		#r = tagger.expandEntities("bla &#xA3;")
		#self.assertEqual(r, u"bla £")
		return
		
	def testGuess(self):
		tagger = Tagger.Tagger("en", self.FILENAME_WORDS, self.FILENAME_SEQ1, self.FILENAME_SEQ2)
		tagger.deleteData()
		tagger.bindData()
		tagger.buildDataFromString("")		# don't learn at all!
		tagger.commitData()

		tag = tagger.guessTagTest("")
		self.assertEqual(tag, None)

		# numbers = CRD:
		self.assertEqual(tagger.guessTagTest("0"), 'CRD')
		self.assertEqual(tagger.guessTagTest("3123.1312"), 'CRD')
		self.assertEqual(tagger.guessTagTest("00,99"), 'CRD')
		self.assertEqual(tagger.guessTagTest("00/99"), 'CRD')
		self.assertEqual(tagger.guessTagTest("1-99"), 'CRD')

		# BNC Sampler tags "$xx" as NNU, which is mapped to NN0 (same for £):
		self.assertEqual(tagger.guessTagTest("$31.12"), 'NN0')
		self.assertEqual(tagger.guessTagTest("HIV"), 'NN0')
		self.assertEqual(tagger.guessTagTest("8.55pm"), 'AV0')
		self.assertEqual(tagger.guessTagTest("10.10pm"), 'AV0')
		self.assertEqual(tagger.guessTagTest(u"Großekathöfer"), 'NP0')
		self.assertEqual(tagger.guessTagTest("jackerfoodom"), 'NN1')
		self.assertEqual(tagger.guessTagTest("testious"), 'AJ0')
		self.assertEqual(tagger.guessTagTest("testize"), 'VVI')
		self.assertEqual(tagger.guessTagTest("foofooly"), 'AV0')
		self.assertEqual(tagger.guessTagTest("unguessablexxx"), None)
		self.assertEqual(tagger.guessTagTest("verboten"), None)
		return

	def testLearningAndTagging(self):
	
		print "###########1"
		
		#FIXME: doesn't work:
		r = self.tag("The/AT0 fat/AJ0 man/NN1", "The big man")
		self.assertEqual(r, [('The', 'AT0'), ('big', 'unknown'), ('man', 'NN1')])

		print "###########2"
		return		#FIXME

		r = self.tag("The/AT0 fat/AJ0 man/NN1", "the xxx")
		# the/unknown because the tagger is case sensitive:
		self.assertEqual(r, [('the', 'unknown'), ('xxx', 'unknown')])

		r = self.tag("The/AT0 fat/AJ0 man/NN1", "The fat man")
		self.assertEqual(r, [('The', 'AT0'), ('fat', 'AJ0'), ('man', 'NN1')])

		r = self.tag("A/DET cool/AJ0 large/AJ0 car/NN1", "A cool car")
		self.assertEqual(r, [('A', 'DET'), ('cool', 'AJ0'), ('car', 'NN1')])
		
		# fat occurs 2 times as NN1 and 1 time as AJ0, but context decides:
		r = self.tag("""The/DET fat/NN1 is/VB hot/AJ0
			The/DET fat/AJ0 guy/NN1
			A/DET man/NN1 used/VBD fat/NN1""",
			"A fat man")
		self.assertEqual(r, [('A', 'DET'), ('fat', 'AJ0'), ('man', 'NN1')])

		# fat occurs 3 times as NN1 and 0 times as AJ0 -> tagged as NN1 of course:
		r = self.tag("""The/DET fat/NN1 is/VB hot/AJ0
			A/DET fat/NN1 man/NN1 . 
			He/PP used/VBD fat/NN1""", "A fat man")
		self.assertEqual(r, [('A', 'DET'), ('fat', 'NN1'), ('man', 'NN1')])

		# fat occurs 1 times as NN1 and 2 times as AJ0 -> tagged as AJ0
		r = self.tag("""The/DET fat/AJ0 is/VB hot/AJ0
			A/DET fat/AJ0 man/NN1 . 
			He/PP used/VBD fat/NN1""", "A fat man")
		self.assertEqual(r, [('A', 'DET'), ('fat', 'AJ0'), ('man', 'NN1')])

		r = self.tag("""The/DET fat/AJ0 man/NN is/VB fat/AJ0 ./PP""",
			"A fat man he is fat.")
		self.assertEqual(r, [('A', 'unknown'), ('fat', 'AJ0'), ('man', 'NN'),
			('he', 'unknown'), ('is', 'VB'), ('fat', 'AJ0')])
		
		return

	#FIXME
	#def testApplyConstraints(self):
	#	r = self.tag("A/X bla/X demodemo/AA demodemo/AA demodemo/BB bla/X bla/X", \
	#		"demodemo")
	#	self.assertEqual(r, [('demodemo', 'BB')])
	#
	#	return

if __name__ == "__main__":
	unittest.main()
