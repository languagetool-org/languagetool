#!/usr/bin/python
# -*- coding: iso-8859-1 -*-
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

import unittest
import Tagger

import os

class TaggerTest(unittest.TestCase):

	FILENAME_WORDS = os.path.join("data", "tag_test_words")
	FILENAME_SEQ = os.path.join("data", "tag_test_sequences")
	
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
		tagger = Tagger.Tagger(self.FILENAME_WORDS, self.FILENAME_SEQ)
		tagger.deleteData()
		tagger.bindData()
		tagger.buildDataFromString(learn_text)
		tagger.commitData()
		tagger = None

		# tag text:
		tagger2 = Tagger.Tagger(self.FILENAME_WORDS, self.FILENAME_SEQ)
		tagger2.bindData()
		res = tagger2.tagText(text)
		res = self.cleanList(res)
		tagger2.deleteData()

		return res

	def testTagList(self):
		text = Tagger.Text()

		# only one element:
		t = {}
		text.addTagList(['NN0'], t)
		expected = {
			(None, None, 'NN0'): 1.0,
			(None, 'NN0', None): 1.0,
			('NN0', None, None): 1.0
			}
		self.assertEqual(t, expected)

		# only two elements:
		t = {}
		text.addTagList(['NN0', 'XX'], t)
		expected = {
			(None, None, 'NN0'): 1.0,
			(None, 'NN0', 'XX'): 1.0,
			('NN0', 'XX', None): 1.0,
			('XX', None, None): 1.0
			}
		self.assertEqual(t, expected)

		# three element:
		t = {}
		text.addTagList(['NN0', 'AV0', 'NP1'], t)
		expected = {
			(None, None, 'NN0'): 1.0,
			(None, 'NN0', 'AV0'): 1.0,
			('NN0', 'AV0', 'NP1'): 1.0,
			('AV0', 'NP1', None): 1.0,
			('NP1', None, None): 1.0
			}
		self.assertEqual(t, expected)

		# four element and elements with two tags:
		t = {}
		text.addTagList(['NN0', 'AV0', 'NP1-YY', 'XX'], t)
		expected = {
			(None, None, 'NN0'): 1.0,
			(None, 'NN0', 'AV0'): 1.0,
			('NN0', 'AV0', 'NP1'): 0.5,
			('NN0', 'AV0', 'YY'): 0.5,
			('AV0', 'NP1', 'XX'): 0.5,
			('AV0', 'YY', 'XX'): 0.5,
			('NP1', 'XX', None): 0.5,
			('YY', 'XX', None): 0.5,
			('XX', None, None): 1.0
			}
		self.assertEqual(t, expected)
		return
	
	def testExpandEntities(self):
		tagger = Tagger.Text()
		r = tagger.expandEntities("")
		self.assertEqual(r, "")
		r = tagger.expandEntities("bla &amp;&amp;")
		self.assertEqual(r, "bla &&")
		#r = tagger.expandEntities("bla &#xA3;")
		#self.assertEqual(r, u"bla £")
		return
		
	def testGuess(self):
		tagger = Tagger.Tagger(self.FILENAME_WORDS, self.FILENAME_SEQ)
		tagger.deleteData()
		tagger.bindData()
		tagger.buildDataFromString("")		# don't learn at all!
		tagger.commitData()

		tag = tagger.guessTagTest("")
		self.assertEqual(tag, None)

		# numbers = CRD:
		tag = tagger.guessTagTest("0")
		self.assertEqual(tag, 'CRD')
		tag = tagger.guessTagTest("3123.1312")
		self.assertEqual(tag, 'CRD')
		tag = tagger.guessTagTest("00,99")
		self.assertEqual(tag, 'CRD')
		tag = tagger.guessTagTest("00/99")
		self.assertEqual(tag, 'CRD')
		tag = tagger.guessTagTest("1-99")
		self.assertEqual(tag, 'CRD')

		# BNC Sampler tags "$xx" as NNU, which is mapped to NN0 (same for £):
		tag = tagger.guessTagTest("$31.12")
		self.assertEqual(tag, 'NN0')

		tag = tagger.guessTagTest("HIV")
		self.assertEqual(tag, 'NN0')

		tag = tagger.guessTagTest("8.55pm")
		self.assertEqual(tag, 'AV0')
		
		tag = tagger.guessTagTest("10.10pm")
		self.assertEqual(tag, 'AV0')

		tag = tagger.guessTagTest(u"Großekathöfer")
		self.assertEqual(tag, 'NP0')

		tag = tagger.guessTagTest("jackerfoodom")
		self.assertEqual(tag, 'NN1')

		tag = tagger.guessTagTest("testious")
		self.assertEqual(tag, 'AJ0')

		tag = tagger.guessTagTest("testize")
		self.assertEqual(tag, 'VVI')

		tag = tagger.guessTagTest("foofooly")
		self.assertEqual(tag, 'AV0')

		tag = tagger.guessTagTest("unguessablexxx")
		self.assertEqual(tag, None)
		tag = tagger.guessTagTest("verboten")
		self.assertEqual(tag, None)
		return

	def testLearningAndTagging(self):
	
		r = self.tag("The/AT0 fat/AJ0 man/NN1", "The big man")
		self.assertEqual(r, [('The', 'AT0'), ('big', 'unknown'), ('man', 'NN1')])

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
			"A fat man, he is fat.")
		self.assertEqual(r, [('A', 'unknown'), ('fat', 'AJ0'), ('man', 'NN'),
			(', ', None), ('he', 'unknown'), ('is', 'VB'), ('fat', 'AJ0')])
		
		return

	def testApplyConstraints(self):
	
		r = self.tag("A/X bla/X demodemo/AA demodemo/AA demodemo/BB bla/X bla/X", \
			"demodemo")
		self.assertEqual(r, [('demodemo', 'BB')])

		return

if __name__ == "__main__":
	unittest.main()
