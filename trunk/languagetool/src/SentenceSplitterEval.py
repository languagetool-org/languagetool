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

import sys
import re

import Entities
import SentenceSplitter

class SentenceSplitterEval:

	def __init__(self):
		return

	def findSentence(self, real_boundary, bnc_sentences):
		sent = None
		sent_disp = None
		l = 0
		i = 0
		for s in bnc_sentences:
			l = l + len(s)
			if l == real_boundary:
				sent = s
				next_sent_start = ""
				try:
					next_sent_start = bnc_sentences[i+1][0:20]
				except IndexError:
					pass
				sent_disp = "%s###%s..." % (s, next_sent_start)
				break
			i = i + 1
		return sent, sent_disp

	def run(self, bnc_string):
		self.s = SentenceSplitter.SentenceSplitter()

		# manual testing:
		#bnc_string = "<s n=0000>This a test. Sentence.</s> <s n=1111>Another one.</s>"
		#bnc_string = "<s n=0000>This a Sentence</s> <s n=1111>Another one.</s>"

		bnc_paras = re.compile("<p>(.*?)</p>", re.DOTALL).findall(bnc_string)
		bnc_paras_str = str.join(' ', bnc_paras)
		bnc_sentences = re.compile("<s\s.*?>(.*?)</s>", re.DOTALL).findall(bnc_paras_str)
		bnc_boundaries = []
		l = 0
		i = 0
		for s in bnc_sentences:
			s = bnc_sentences[i]
			s = Entities.Entities.cleanEntities(s)
			s = re.compile("<.*?>").sub("", s)
			s = s.strip()
			if not s.endswith(" "):
				# TODO: is this fair?
				s = s + " "
			bnc_sentences[i] = s
			l = l + len(s)
			bnc_boundaries.append(l)
			i = i + 1
		###print bnc_sentences
		bnc_sentences_str = str.join('', bnc_sentences)
		#print bnc_sentences_str

		detected_sentences = self.s.split(bnc_sentences_str)
		###print detected_sentences 
		detected_boundaries = []
		l = 0
		for s in detected_sentences:
			l = l + len(s)
			detected_boundaries.append(l)

		sent_count = 0
		# recall = how many of the sentence boundaries have been detected?
		recall_count = 0
		for real_boundary in bnc_boundaries:
			if real_boundary in detected_boundaries:
				recall_count = recall_count + 1
				#print "Found: '%s'" % s
			else:
				pass
				(s, s_disp) = self.findSentence(real_boundary, bnc_sentences)
				print "Not found: '%s'" % s_disp
			sent_count = sent_count + 1
		recall = 0
		if len(bnc_boundaries) > 0:
			recall = float(recall_count) / float(len(bnc_boundaries))

		# precision = how many of detected boundaries are real sentence boundaries?
		precision_count = 0
		for detected_boundary in detected_boundaries:
			if detected_boundary in bnc_boundaries:
				precision_count = precision_count + 1
		precision = 0
		if len(detected_boundaries) > 0:
			precision = float(precision_count) / float(len(detected_boundaries))
		
		print "Real sentences = %d" % sent_count
		print "Recall = %.3f" % recall
		print "Precision = %.3f" % precision
		return

if __name__ == "__main__":
	prg = SentenceSplitterEval()
	if len(sys.argv) <= 1:
		print "Usage: ./SentenceSplitterEval.py <bnc_sampler_files>"
	else:
		for filename in sys.argv[1:]:
			print filename
			f = open(filename)
			bnc_string = f.read()
			f.close()
			prg.run(bnc_string)
