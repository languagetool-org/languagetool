#!/usr/bin/python
# A probabilistc part-of-speech tagger (see the QTag paper), 2002-06-15
# (c) 2002 Daniel Naber <daniel.naber@t-online.de>

# TODO: remove/ignore non-word characters
# fixme(?): needs to recognize 100% if trained on the same text (problem: special characters)
# Usage:
# 1) ./tag.py -build /data/bnc/A/A0/A00.xml (bzw. /data/bnc/A/A0/A0*.xml)
# 2) ./tag.py -tag /data/bnc/test/AY/AYJ.xml (bzw. kürzer: /data/bnc/A/A0/S.xml)

# ### Speed comparison for "database":

# BeeDict:
# time ./tag.py -build /data/bnc/A/A0/A0[0-9].xml	28.00s 2,4MB + 0,3MB
# time ./tag.py -tag test.txt >/dev/null			 0.17s

# pickle:
# time ./tag.py -build /data/bnc/A/A0/A0[0-9].xml	35.00s 2,2MB
# time ./tag.py -tag test.txt >/dev/null			18.00s (!!!)

# cPickle (binary):
# time ./tag.py -build /data/bnc/A/A0/A0[0-9].xml	18.00s 1,3MB
# time ./tag.py -tag test.txt >/dev/null			 1.50s

import Tagger

import re
import sys
import string
		
class Controller:
	"Main program."

	def __init__(self):
		return
		
	def usage(self):
		print "Usage: ./tagger.py <-build|-tag> <filename>"
		return
	
	def run(self):
		# fixme: add real options
		if len(sys.argv) <= 2:
			prg.usage()
			sys.exit()
		option = sys.argv[1]
		filename = sys.argv[2]
		if option == "-build":
			tagger = Tagger.Tagger()
			# ugly hack reset the data (useful for testing) (TODO):
			tagger.deleteData()
			tagger.bindData()
			filenames = sys.argv[2:]
			for filename in filenames:
				tagger.buildData(filename)
			tagger.commitData()
		elif option == "-tag":
			tagger = Tagger.Tagger()
			tagger.bindData()
			xml = tagger.tagFile(filename)
			print xml
			print >> sys.stderr, "Done."
			### sanity check: all <w>...</w> together == original file?
			words = re.compile("<w.*?>(.*?)</w>", re.DOTALL).findall(xml)
			word_list = []
			for word in words:
				word_list.append(word)
			words_string = string.join(word_list, "")
			# load original file:
			f = open(filename)
			orig_contents = f.read()
			f.close()
			if orig_contents != words_string:
				print >> sys.stderr, "*** Warning: joined output doesn't match original file!"
				print >> sys.stderr, "*** (can be ignored if the file is a BNC file)"
		else:
			print "Unknown option '%s'" % option
			self.usage()
		return

### Main program

prg = Controller()
prg.run()
