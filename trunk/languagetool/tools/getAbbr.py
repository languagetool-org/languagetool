#!/usr/bin/python
# Daniel Naber <daniel.naber@t-online.de>, 2003-05-14
#
# Search the BNC SGML files for potential abbreviations,
# i.e. words that end in a period. Sentence periods
# should make use of a different markup, but this isn't
# always the case, so one gets quite some false positives.
#
# The list is sorted by order, most common potential 
# abbreviations at the end of the list.

import os
import re

DIR = "/data/bnc/A/"
words = {}

def workDir(directory):
	# sgml:
	regex = re.compile("<w[^>]*>(.*?)<", re.DOTALL)
	files = os.listdir(directory)
	for file in files:
		filename = os.path.join(directory, file)
		if os.path.isdir(filename):
			#print filename
			workDir(filename)
		elif os.path.isfile(filename):
			#print "FILE=%s" % file
			f = open(filename)
			s = f.read()
			f.close()
			matches = regex.findall(s)
			for match in matches:
				w = match.strip()
				if w.endswith("."):
					if words.has_key(w):
						words[w] = words[w] + 1
					else:
						words[w] = 1
		#break
	return

def main():
	workDir(DIR)		
	l = []
	for w in words.keys():
		e = (words[w], w)
		l.append(e)
		print e
	l.sort()
	for e in l:
		print e[1]
	return

main()
