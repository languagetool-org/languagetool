#!/usr/bin/python
# -*- coding: iso-8859-1 -*-
# A frontend to a probabilistc part-of-speech tagger (see the QTag paper)
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
#
# Usage examples:
# 1) ./tag.py -b /data/bnc_sampler/train/*
# 2) ./tag.py -t /data/bnc_sampler/test/fcf

import re
import sys
import string
import getopt
import profile

import Tagger
import Entities
		
class Controller:
	"Main program."

	TAG = 0
	BUILD = 1
	TAGWORD = 2
	TAGSEQ = 3
	
	def __init__(self):
		return
		
	def usage(self):
		print >> sys.stderr, "Usage: ./tagger.py <--build|--tag|--tagword> <filename...>"
		print >> sys.stderr, " -h, --help    this help information"
		print >> sys.stderr, " -t, --tag     tag any text files"
		print >> sys.stderr, " -b, --build   train the tagger using BNC XML files"
		print >> sys.stderr, " -w, --wordtag tag any word"
		print >> sys.stderr, " -s, --seqtag  probability for any 2-tag-sequence"
		# TODO: better help (e.g. 'build' adds to existing index (?))
		return
	
	def sanityCheck(self, filename, xml):
		"""Sanity check: all <w>...</w> together == original file?"""
		words = re.compile("<w.*?>(.*?)</w>", re.DOTALL).findall(xml)
		words_string = string.join(words, "")
		# Load original file:
		f = open(filename)
		orig_contents = f.read()
		f.close()
		if orig_contents != words_string:
			print >> sys.stderr, "*** Warning: joined output doesn't match original file!"
			print >> sys.stderr, "*** (can be ignored if the file is a BNC file)"
		return

	def run(self):
		try:
			(options, rest) = getopt.getopt(sys.argv[1:], 'htbws',
				['help', 'build', 'tag', 'wordtag', 'seqtag'])
		except getopt.GetoptError, e:
			print >> sys.stderr, "Error: %s" % e
			self.usage()
			sys.exit(1)
		mode = self.TAG
		for o, a in options:
			if o in ("-h", "--help"):
				self.usage()
				sys.exit(0)
			elif o in ("-t", "--tag"):
				mode = self.TAG
			elif o in ("-b", "--build"):
				mode = self.BUILD
			elif o in ("-w", "--wordtag"):
				mode = self.TAGWORD
			elif o in ("-s", "--seqtag"):
				mode = self.TAGSEQ
		if not rest:
			self.usage()
			sys.exit(1)

		if mode == self.BUILD:
			tagger = Tagger.Tagger()
			tagger.bindData()
			tagger.buildData(rest)
			tagger.commitData()
		elif mode == self.TAG:
			tagger = Tagger.Tagger()
			tagger.bindData()
			for filename in rest:
				f = open(filename)
				content = f.read()
				f.close()
				content = Entities.Entities.cleanEntities(content)
				xml = tagger.tagTexttoXML(content)
				self.sanityCheck(filename, xml)
				print xml
			print >> sys.stderr, "Done."
		elif mode == self.TAGWORD:
			tagger = Tagger.Tagger()
			tagger.bindData()
			for word in rest:
				r = tagger.tagWord(word)
				print r
		elif mode == self.TAGSEQ:
			tagger = Tagger.Tagger()
			tagger.bindData()
			if len(rest) > 1 and rest[1] != '*':
				key = (rest[0], rest[1])
				prob = tagger.tagSeq(key)
				print prob
			else:
				# TODO: don't duplicate code from query.py:
				tags_str = "AJ0,AJC,AJS,AT0,AV0,AVP,AVQ,CJC,CJS,CJT,"
				tags_str = tags_str + "CRD,DPS,DT0,DTQ,EX0,ITJ,NN0,NN1,NN2,NP0,ORD,PNI,PNP,"
				tags_str = tags_str + "PNQ,PNX,POS,PRF,PRP,PUL,PUN,PUQ,PUR,TO0,UNC,VBB,VBD,"
				tags_str = tags_str + "VBG,VBI,VBN,VBZ,VDB,VDD,VDG,VDI,VDN,VDZ,VHB,VHD,VHG,"
				tags_str = tags_str + "VHI,VHN,VHZ,VM0,VVB,VVD,VVG,VVI,VVN,VVZ,XX0,ZZ0,"
				# these are not in query.py:
				tags_str = tags_str + "YBL,YBR,YCOL,YCOM,YDSH,YEX,YLIP,YQUE,YQUO,YSCOL,YSTP"
				tags = re.split(",", tags_str)
				sum = 0
				items = 0
				for tag in tags:
					key = (rest[0], tag)
					prob = tagger.tagSeq(key)
					prob2 = tagger.tagSeq2(key)
					if prob > 0 or prob2 > 0:
						sum = sum + prob
						print "%s followed by %s -> %.10f" % (key[0], key[1], prob)
						print "%s follows     %s -> %.10f" % (key[0], key[1], prob2)
						items = items + 1
				print "items=%d, sum=%.5f" % (items, sum)
		return

### Main program

prg = Controller()
prg.run()
#profile.run('prg.run()', 'fooprof')
