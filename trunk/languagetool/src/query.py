#!/usr/bin/python
# Query BNC data files in XML format
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

# for debugging only
import cgitb
cgitb.enable()

#import profile

import cPickle
import cgi
import os
import re
import re
import sys
import time

os.chdir(sys.path[0])
sys.path.append(sys.path[0])
import TagInfo

data_dir = "/data/bnc/xml_data"
context = 4
limit = 30
tags_str = "AJ0,AJC,AJS,AT0,AV0,AVP,AVQ,CJC,CJS,CJT,\
CRD,DPS,DT0,DTQ,EX0,ITJ,NN0,NN1,NN2,NP0,ORD,PNI,PNP,\
PNQ,PNX,POS,PRF,PRP,PUL,PUN,PUQ,PUR,TO0,UNC,VBB,VBD,\
VBG,VBI,VBN,VBZ,VDB,VDD,VDG,VDI,VDN,VDZ,VHB,VHD,VHG,\
VHI,VHN,VHZ,VM0,VVB,VVD,VVG,VVI,VVN,VVZ,XX0,ZZ0"

tags = re.split(",", tags_str)
sentence_count = 0
word_count = 0
matches = 0
regex = re.compile("(<S.*?</S>)", re.DOTALL)
words_regex = re.compile("(<[WC].*?</[WC]>)", re.DOTALL)
type_regex = re.compile("TYPE=\"(.*?)\"")
word_regex = re.compile(">(.*?)</[WC]>")

def query(search_tokens, filename):
	global sentence_count
	global word_count
	global limit
	global matches
	global tags
	t1 = time.time()
	tokens = buildList(filename)
	#print "T=%.2f<br>" % (time.time()-t1)
	t1 = time.time()
	#print tokens
	match_pos = 0
	pos = 0
	for word,tag in tokens:
		if tag == 'S_BEGIN':
			sentence_count = sentence_count + 1
		word_count = word_count + 1
		if tags.count(search_tokens[match_pos]) > 0:
			compare = tag
		else:
			compare = word
		if compare == search_tokens[match_pos] or search_tokens[match_pos] == '_':
			match_pos = match_pos + 1
		else:
			match_pos = 0
		#print match_pos
		if match_pos == len(search_tokens):
			if matches+1 > limit:
				return None
			print "%d." % (matches+1)
			print niceFormat(tokens[pos-context:pos+context], \
				context-len(search_tokens)+1, len(search_tokens))
			sys.stdout.flush()
			matches = matches + 1
			match_pos = 0
		pos = pos + 1
	#print "T2=%.2f<br>" % (time.time()-t1)
	return 1

def niceFormat(tokens, rel_pos, match_len):
	l = []
	count = 0
	for word,tag in tokens:
		if count >= rel_pos and count < rel_pos+match_len:
			l.append('<b>%s<span class="tag">/%s</span></b>' % (word,tag))
		elif tag == 'PUN':
			l.append(word)
		else:
			l.append('%s<span class="tag">/%s</span>' % (word,tag))
		count = count + 1
	return str.join(' ', l) + "<br>"

def buildList(filename):
	# Speed up:
	pickle_filename = "%s.pickle" % filename
	if os.path.exists(pickle_filename):
		#print "Loading pickled data from %s<br>" % pickle_filename
		t1 = time.time()
		tokens = cPickle.load(open(pickle_filename))
		#print "Tpickle=%.2f<br>" % (time.time()-t1)
		return tokens

	f = open(filename)
	content = f.read()
	f.close()
	global regex
	global words_regex
	global type_regex
	global word_regex
	
	sentences = regex.findall(content)
	tokens = []
	for s in sentences:
		#print "X"
		words = words_regex.findall(s)
		tokens.append(('', 'S_BEGIN'))
		for w in words:
			w = w.replace("\n", " ")
			#print w
			type_match = type_regex.search(w)
			if not type_match:
				print "*** no type_match!?"
				continue
			type_str = type_match.group(1)
			word_match = word_regex.search(w)
			word = word_match.group(1).strip()
			#print "%s/%s" % (word, type_str)
			tokens.append((word, type_str))
		tokens.append(('', 'S_END'))
	# Prepare speed up for next search:
	cPickle.dump(tokens, open(pickle_filename, 'w'), 1)
	return tokens

def queryFiles(tokens, dir_name):
	os.chdir(dir_name)
	dir_contents = os.listdir(".")
	dir_contents.sort()
	c = 0
	for filename in dir_contents:
		if filename.endswith(".xml"):
			c = c + 1
	print "Found %d *.xml files in %s<br>" % (c, dir_name)
	w = 0
	s = 0
	m = 0
	f_count = 1
	for name in dir_contents:
		if os.path.isdir(name):
			queryFiles(tokens, name)
		elif name.endswith(".xml"):
			print "<strong>%.3d. %s</strong>, so far %d words, %d sentences<br>" % (f_count, name, word_count, sentence_count)
			res = query(tokens, name)
			if not res:
				return
			#global_file_count = global_file_count + 1
			#print "<hr />"
			sys.stdout.flush()
			f_count = f_count + 1
		# for profiling
		#if word_count > 200000:
		#	return
	os.chdir("..")
	return

def displayForm():
	taginfo = TagInfo.TagInfo()
	print "Content-Type: text/html\n\n"
	print """
		<html><head>
		<title>BNC Query</title></head>
		<body>
		<h1>BNC Query</h1>

		<form action="query.py" method="get">
		<table border="0" cellspacing="0" cellpadding="0">
		<tr>
			<td>Word/tag sequence:</td>
			<td>Context:</td>
			<td>Max. results:</td>
		</tr>
		<tr>
			<td><input type="text" name="tokens"></td>
			<td><select name="context">
				<option value="4">4&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</option>
				<option>6</option>
				<option>8</option>
				<option>10</option>
			</select></td>
			<td><input type="text" name="limit" value="30" size="6" /></input>
			<td>&nbsp;</td>
			<td><input type="submit" value="Query" /></td>
		</tr>
		</table>
		</form>
		<br />
		_ (underline) matches any word
		%s
		</body>
		</html>""" % taginfo.getHTMLCode()
	return

def main():
	global limit
	global context
	form = cgi.FieldStorage()
	if not form.getvalue("tokens"):
		displayForm()
		return
	if form.getvalue("context"):
		context = int(form.getvalue("context"))
	if form.getvalue("limit"):
		limit = int(form.getvalue("limit"))
	print "Content-Type: text/html\n\n"
	token_display = cgi.escape(form.getvalue("tokens"), 1)
	print """<html><head>
		<title>BNC query result for '%s'</title>
		<style rel="stylesheet">
		<!--
		.tag { color:#999999; }
		-->
		</style></head>
		<body>
		<h1>BNC query result for '%s'</h1>""" % (token_display, token_display)
	tokens = re.split("\s+", form.getvalue("tokens"))
	queryFiles(tokens, data_dir)
	print '<p>Queried %d words in %d sentences.' % (word_count, \
		sentence_count)
	print '</body></html>'
	#print '<pre>'	# profiling
	return

main()
#profile.run('main()')
