#!/usr/bin/python
# A server that uses TextChecker.py to check text for style 
# and grammar errors
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

import TextChecker

import ConfigParser
import os
import re
import socket
import sys
import time

sys.path.append(os.path.join(sys.path[0], "snakespell-1.01"))
from scriptfoundry.snakespell import iSpell 

server_name = "127.0.0.1"
server_port = 50100
configfile = os.path.join(os.getenv('HOME'), ".kde/share/config/languagetool")

def makeChecker(grammar_cfg=None, falsefriends_cfg=None, words_cfg=None, \
		builtin_cfg=None, textlanguage=None, mothertongue=None, \
		max_sentence_length=None):
	"""Create a new TextChecker object and return it."""
	checker = TextChecker.TextChecker(grammar_cfg, falsefriends_cfg, words_cfg, \
		builtin_cfg, textlanguage, mothertongue, max_sentence_length)
	return checker

def loadOptionList(config, enable_name, option_name):
	val = None
	if config.has_option("General", enable_name) and \
		config.getboolean("General", enable_name):
		if config.has_option("General", option_name):
			val = re.split(',', config.get("General", option_name))
	else:
		val = ["NONE"]
	return val

def loadOptionBoolean(config, option_name):
	if config.has_option("General", option_name) and config.getboolean("General", option_name):
		return 1
	return None

def loadOptionString(config, option_name, default):
	val = default
	if config.has_option("General", option_name):
		val = config.get("General", option_name)
	return val
	
def readConfig():
	"""Read the checker config from a KDE config file (INI style).
	Return a checker which uses that config."""
	config = ConfigParser.ConfigParser()
	try:
		config.readfp(open(configfile))
	except IOError:
		print "Couldn't load config file '%s', using defaults..." % configfile
	grammar = loadOptionList(config, "EnableGrammar", "GrammarRules")
	falsefriends = loadOptionList(config, "EnableFalseFriends", "FalseFriendsRules")
	words = loadOptionList(config, "EnableWords", "WordsRules")
	builtin = []
	if loadOptionBoolean(config, "EnableWhitespaceCheck"):
		builtin.append("WHITESPACE")
	if len(builtin) == 0:
		builtin = None
	textlanguage = loadOptionString(config, "TextLanguage", "en")
	mothertongue = loadOptionString(config, "MotherTongue", "en")
	sentence_length = 0
	if loadOptionBoolean(config, "EnableSentenceLength"):
		if config.has_option("General", "MaxSentenceLength"):
			sentence_length = config.getint("General", "MaxSentenceLength")
	checker = makeChecker(grammar, falsefriends, words, builtin, \
		textlanguage, mothertongue, sentence_length)
	return checker

def getConfig(data):
	"""Get a new config in pseudo XML format from the client.
	It needs to be at the beginning of the string that comes
	from the client and must be of form <config ... />.
	Returns a tuple with the a checker based on this config and 
	the 'data' string with the config section removed."""
	print "Receiving new config..."
	line_end_pos = data.find("/>")
	cfg_str = data[:line_end_pos]
	data = data[line_end_pos+3:]
	grammar = getConfigValue(cfg_str, "grammar")
	falsefriends = getConfigValue(cfg_str, "falsefriends")
	words = getConfigValue(cfg_str, "words")
	builtin = getConfigValue(cfg_str, "builtin")
	textlanguage = getConfigValue(cfg_str, "textlanguage")
	if textlanguage:
		textlanguage = textlanguage[0]
	mothertongue = getConfigValue(cfg_str, "mothertongue")
	if mothertongue:
		mothertongue = mothertongue[0]
	sentence_length = getConfigValue(cfg_str, "max-sentence-length")
	if not sentence_length:
		sentence_length = 0
	else:
		sentence_length = int(sentence_length[0])
	checker = makeChecker(grammar, falsefriends, words, builtin, \
		textlanguage, mothertongue, sentence_length)
	return (checker, data)

def getConfigValue(cfg_str, val):
	m = re.compile('%s="(.*?)"' % val).search(cfg_str)
	if not m:
		return None
	s = m.group(1)
	l = re.split(',', s)
	return l
	
def main():
	print "Binding to '%s:%d'..." % (server_name, server_port)
	s.bind((server_name, server_port))
	print "Listening..."
	s.listen(1)
	print "Setting up Checker..."
	checker = readConfig()
	print "Ready..."
	while 1:
		conn, addr = s.accept()
		if addr[0] != "127.0.0.1":		# security
			print "Connection by '%s' refused" % addr[0]
			conn.close()
			continue
		else:
			print "Connected by '%s'" % addr[0]

		l = []
		limit = 1024
		while 1:
			data = conn.recv(limit)
			l.append(data)
			#FIXME: need to look for separator, not just < limit!
			if not data or len(data) < limit:
				break
		data = str.join('', l)

		print "Received '%s'" % data
		if data.find("<config") != -1:
			del checker
			(checker, data) = getConfig(data)
			print "New config activated"
		t1 = time.time()
		check_result = checkWords(checker, data)
		t2 = time.time()-t1
		print "Replying (%.2fs) '%s'" % (t2, check_result.encode('utf8'))
		#print "Replying (%.2fs)" % t2
		conn.send(check_result.encode('utf8'))

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
	result = u'<result>'

	### Spelling:
	ispell = iSpell()
	words = words.replace("\n", " ")		# iSpell works line by line
	r = ispell.check(words)
	if r > 0:
		# fixme: escape word
		for mistake in ispell.getMistakes():
			# TODO: make faster
			pos = []
			for p in mistake.getPositions():
				result = u'%s<error from="%d" to="%d" word="%s" corrections="%s"/>' % \
					(result, p, p+len(mistake.getWord()), \
					unicode(mistake.getWord(), 'latin1'), \
					unicode(str.join(',', mistake.corrections), ('latin1')))

	### Grammar + Style:
	(rule_matches, res, tags) = checker.check(words)
	# FIXME: only if there's no overlap?!
	result = result + res
		
	result = result + '</result>\n'
	return result

try:
	s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	os.chdir(sys.path[0])
	main()
except KeyboardInterrupt:
	# TODO: close explicitely, unfortunately we still get an 
	# 'Address already in use' error if we restart immediately:
	s.shutdown(2)
	s.close()
	print "Stopped."
