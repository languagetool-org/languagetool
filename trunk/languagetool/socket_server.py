#!/usr/bin/python
# A server that uses TextChecker.py to check text for style 
# and grammar errors
# Copyright (C) 2002,2003 Daniel Naber <daniel.naber@t-online.de>
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

import TextChecker
import config

import os
import re
import socket
import sys
import time

sys.path.append(os.path.join(config.BASEDIR, "snakespell-1.01"))
from scriptfoundry.snakespell import iSpell 

name = "127.0.0.1"
port = 50100

def makeChecker():
	grammar_cfg = []			# empty list = activate all rules (?)
	falsefriends_cfg = []
	words_cfg = []
	builtin = []
	###fixme:
	textlanguage = None
	mothertongue = None
	max_sentence_length = None
	checker = TextChecker.TextChecker(grammar_cfg, falsefriends_cfg, words_cfg, builtin, \
		textlanguage, mothertongue, max_sentence_length)
	return checker
	
def main():
	print "Binding to '%s:%d'..." % (name, port)
	s.bind((name,port))
	print "Listening..."
	s.listen(1)
	print "Setting up Checker..."
	checker = makeChecker()
	while 1:
		conn, addr = s.accept()
		if addr[0] != "127.0.0.1":		# security
			print "Connection by '%s' refused" % addr[0]
			conn.close()
			continue
		else:
			print "Connected by '%s'" % addr[0]
		
		data = conn.recv(1024)
		print "Received '%s'" % data
		t1 = time.time()
		check_result = checkWords(checker, data)
		#if not data:
		#	break
		t2 = time.time()-t1
		print "Replying (%.2fs) '%s'" % (t2, check_result)
		conn.send(check_result)

		conn.close()
	s.close()
	return

def checkWordsTEST(words):
	"""Just for testing. Marks 'working' as incorrect."""
	words = re.split("\s+", words)
	s = '<result>'
	for w in words:	
		if w == "working":
			s = s + '\t<error word="working" pos="5" corrections="Bohlen,Didda"/>'
	s = s + '</result>'
	return s

def checkWords(checker, words):
	result = '<result>'

	### Spelling:
	ispell = iSpell()
	r = ispell.check(words)
	if r > 0:
		# fixme: escape word
		for mistake in ispell.getMistakes():
			#print mistake
			#print mistake.getWord()
			# TODO: make faster
			pos = []
			for p in mistake.getPositions():
				result = '%s<error from="%d" to="%d" word="%s" corrections="%s"/>' % \
					(result, p, p+len(mistake.getWord()), mistake.getWord(), str.join(',', mistake.corrections))

	### Grammar + Style:
	#tx = time.time()
	(rule_matches, res, tags) = checker.check(words)
	#print "=>%.2f" % (time.time()-tx)
	# FIXME: only if there's no overlap?!
	result = result + res
		
	result = result + '</result>\n'
	return result

try:
	s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	os.chdir(config.BASEDIR)
	main()
except KeyboardInterrupt:
	# TODO: close explicitely, unfortunately we still get an 
	# 'Address already in use' error if we restart immediately:
	s.shutdown(2)
	s.close()
	print "Stopped."
