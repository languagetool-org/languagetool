#!/usr/bin/python
# A frontend to a probabilistc part-of-speech tagger (see the QTag paper)
# (c) 2003 Daniel Naber <daniel.naber@t-online.de>
# Usage examples:
# 1) ./tag.py -b /data/bnc_sampler/train/*
#    -> produces ~3MB in data, 469.501 words, 35.129 different words
# 2) ./tag.py --tag /data/bnc/test/AY/AYJ.xml (e.g. /data/bnc/A/A0/S.xml)

# TODO: remove/ignore non-word characters
# fixme(?): needs to recognize 100% if trained on the same text (problem: special characters)

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
		print >> sys.stderr, " -s, --seqtag  probability for any 3-tag-sequence"
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
				#xml = tagger.tagFile(filename)
				content = Entities.Entities.cleanEntities(content)
				xml = tagger.tagTexttoXML(content)
				self.sanityCheck(filename, xml)
				###
				#print xml
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
			key = (rest[0], rest[1], rest[2])
			prob = tagger.tagSeq(key)
			print prob
		return

### Main program

prg = Controller()
prg.run()
#profile.run('prg.run()', 'fooprof')
