# -*- coding: iso-8859-1 -*-
# LanguageTool -- A Rule-Based Style and Grammar Checker
# Copyright (C) 2004 ....
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

# usage python stem.py
#
#  file test.txt contains are for example:
#   carried
#   worked
#    play
#
#  example aff file (dtest.aff)
# SFX D Y 4
# SFX D 0 e d             # abate->abated
# SFX D y ied [^aeiou]y   # carry -> carried
# SFX D 0 ed [^ey]        # work -> worked
# SFX D 0 ed [aeiuu]y     # play -> played
#
#  example dic file (dtest.dic)
# 3
# carry/D
# work/D
# play/D
#
# reads words from the file test.txt

import array
import codecs
import os
import Tagger
from string import *
import time
import sys

arrtype = {}
#aff_file = "dtest.aff"
#dic_file = "dtest.dic"
test_file = "test.txt"
yesno = {}
count = {}
maxllen = {}
maxveg = {}
comment = "#"
condlist = []
condlist1 = []
condlist_group = []
conddic = {}
conddic1 = {}
condglob = {}
szodic = {}
typdic = {}

class Wfinder:

	encoding = "latin1"

	def __init__(self):
#		print time.strftime('%X %x %Z')
		self.is_initialized = 0
		return

	def aff_read(self):
	 	self.aff_file = os.path.join(sys.path[0], "data", Tagger.affFile)
		condlist = []
		condlist_group = []
		conddic = {}
		faff = codecs.open(self.aff_file, "r", self.encoding)
		l = " "
		while l != "":
  			l = faff.readline().encode('latin1')
  			ll =  l.split()
  			if len(ll) <= 1:
  				continue
  			if ll[0][0] in comment:
				continue
			if ll[0][1:3] == "FX":
				arrname = ll[1]
				arrtype[arrname] = ll[0][0]
				yesno[arrname] = ll[2]
				count[arrname] = int(ll[3]);
				maxllen[arrname] = 1;
				maxveg[arrname] = 1;
				for i in range(0, count[arrname]):
#				for i in range(0, 10):
					l = faff.readline()
					bb = l.split()
#					print "l:%s bb[2]:%s arrname:%s" %(l,bb[2], arrname)

					if len(bb[2]) > maxllen[arrname]:
						maxllen[arrname] = len(bb[3])
					if len(bb[3]) > maxveg[arrname]:
						maxveg[arrname] = len(bb[3])
					strip = bb[2]
					if bb[2] == '0':
						strip = '';
					appnd = bb[3]
					if bb[3] == '0':
						appnd = ''
					numc = 0
					if bb[4] != '.':
						jj = 0
						while(jj < len(bb[4])):
							condarr = array.array('B',range(256))
							insbit = 1;
							for iii in range(0,256,1):
								condarr[iii] = 0
							if bb[4][jj] == '[':
								kk = 0;
								jj = jj + 1
								if bb[4][jj] == '^':
									jj = jj+1
									insbit = 0;
									for iii in range(0,256,1):
										condarr[iii] = 1
								while bb[4][jj] != ']':
									condarr[ord(bb[4][jj])] = insbit;
									jj = jj + 1
								if bb[4][jj] == ']':
									jj = jj +1
							else:
								condarr[ord(bb[4][jj])] = insbit;
								jj = jj +1
							condlist.append(condarr)
							++numc
					condlist_group.append(condlist)
					condlist_group.append(strip)
					condlist_group.append(appnd)
					conddic[i] = condlist_group
					condlist = []
					condlist_group = []
				condglob[arrname] = conddic
				conddic = {}
		faff.close()

#
# Now read the dictionary
#
	def dic_read(self):
	 	self.dic_file = os.path.join(sys.path[0], "data", Tagger.dicFile)
		szoszam = 0;
		fdic = codecs.open(self.dic_file, "r", self.encoding)
		l = " "
		szolista = []
		ujlista = []
		l = fdic.readline()
		szoszam = int(l)
		while l != "":
			l = fdic.readline()
			szolista = l.split("/")
			for szo in szolista:
				szo = szo.strip('\n \t')
				ujlista.append(szo)
			if len(ujlista) > 1:
				szodic[ujlista[0]] = ujlista[1]
			else:
				szodic[ujlista[0]] = ""
			if len(ujlista) > 2:
				typdic[ujlista[0]] = ujlista[2]
			else:
				typdic[ujlista[0]] = ""
			ujlista = []
		fdic.close()

	def do_test(self,l):
		if l == "":
			return ""
		if szodic.has_key(l):
			return "+ %s" %l
		else:
#			print "not found %s" %l
			oldword = l
			found = 0
			for key in condglob.keys():
#				print "key: %s" %key
				if found:
					break
			#
			#  search first only suffixes
			#  since prefix is optional
			#
				if arrtype[key] == 'P':
					continue;
				conddic = condglob[key]
				for k2 in conddic.keys():
#					print "k2:%s" %k2
					break_it = 0
					condlist = conddic[k2][0]
					strip    = conddic[k2][1]
					appnd    = conddic[k2][2]
					if len(appnd):
						if l[-len(appnd):] != appnd:
							continue
					if len(appnd):
						restoredWord = l[0:len(l)-len(appnd)]
					else:
						restoredWord = l
					if len(strip):
						restoredWord = restoredWord + strip
					if len(condlist) > 0 and len(restoredWord) >= len(condlist): #tktk
						substr = restoredWord[-len(condlist):]
#						print str(k2) + " " + oldword + " " + restoredWord
#						print condlist
						for i in range(0, len(condlist)-1, 1):
#							print "i=%d" %i
#							print condlist[i]
#							print ord(substr[i])
							if condlist[i][ord(substr[i])] != 1:
								break_it = 1
								break
						if break_it:
							continue
					else:
						if appnd == l[:len(appnd)]:
							restoredWord = l[len(appnd):]
						else:
							continue
					if szodic.has_key(restoredWord):
						flags = szodic[restoredWord]
						if flags == "": # tktk
							continue
						else:
							if find(flags, key) == -1:
								continue
						return "++ %s %s" %(l,restoredWord)
						found = 1
						break
#
# searched all suffixes and not found
# now try to combine all prefixes with all suffixes
# that allow combinations
#
		if found:
			return "+found %s" %oldword
		for key in condglob.keys():
			if found:
				break
			if lower(yesno[key]) == 'n':
				continue
			if arrtype[key] != 'P':
				continue
			conddic = condglob[key]
			for k2 in conddic.keys():
				break_it = 0
				condlist = conddic[k2][0]
				strip    = conddic[k2][1]
				appnd    = conddic[k2][2]
				if appnd == l[:len(appnd)]:  # cut the matching prefix
					l1 = l[len(appnd):]
				else:
					continue
			#
			# prefix without suffix
			#
				if szodic.has_key(l1):
					flags1 = szodic[l1]
					if flags1 != "":
						if find(flags1, key) == -1:
							continue
						return "++ %s  %s" %(l,l1)
						found = 1
						break
				for key1 in condglob.keys():
					if found:
						break
					if lower(yesno[key1]) == 'n':
						continue
					if arrtype[key1] == 'P':
						continue
					conddic1 = condglob[key1]
					for k21 in conddic1.keys():
						break_it = 0
						condlist1 = conddic1[k21][0]
						strip1    = conddic1[k21][1]
						appnd1    = conddic1[k21][2]
#						print "k:%s k1:%s k21:%s str:%s app:%s l:%s l1:%s" %(key,key1,k21, strip1,appnd1, l,l1)
						if len(appnd1):
							if l[-len(appnd1):] != appnd1:
								continue
						if len(appnd1):
							restoredWord1 = l1[0:len(l1)-len(appnd1)]
						else:
							restoredWord1 = l1
						if len(strip1):
							restoredWord1 = restoredWord1 + strip1
						if len(condlist1) > 0 and len(restoredWord1) >= len(condlist1): #tktk
							substr = restoredWord1[-len(condlist1):]
							for i1 in range(0, len(condlist1)-1, 1):
								if condlist1[i1][ord(substr[i1])] != 11:
									break_it = 1
									break
							if break_it:
								continue
					#
					#  prefix and suffix
					#
						if szodic.has_key(restoredWord1):
							flags1 = szodic[restoredWord1]
							if flags1 == "": # tktk
								continue
							else:
								if find(flags1, key1) == -1:
									continue
								if find(flags1, key) == -1:
									continue
							return "+++ %s %s %s" %(l,l1,restoredWord1)
							found = 1
							break
		if found == 0:
			return "- %s" % oldword

	def test_it(self,l):
		if self.is_initialized == 0:
			self.aff_read()
			self.dic_read()
			self.is_initialized = 1
		result = self.do_test(l)
		if result[0] == '-':
			lu = l[0]
			if lu != lu.lower():
				l1 = lu[0].lower()+l[1:]
				if l1.encode('iso-8859-1') != l:			# ???
#					print "------------- checking %s" %l1
					result = self.do_test(l1.encode('iso-8859-1'))
		typ = ''
		if result[0] != '-':
			src = result.split()
			word = src[len(src) - 1]
			oword = src[1]
			typ =  typdic[word]
#			print typ + " " + oword[-1:] + " " +oword[-2:]
#
# Here are the language specific rules of each language
#
			if Tagger.textlanguage == 'de':
				if typ != "":
					if typ == 'V':
						if oword[-1:] == 'e':
							typ = 'V11'
						elif oword[-2:] == 'st':
							typ = 'V12'
						elif oword[-2:] == 'en':
							typ = 'V14'
						elif oword[-2:] == 'et':
							typ = 'V15'
						elif oword[-1:] == 't':
							typ = 'V13'
					elif typ == 'HV':
						if oword[-1:] == 'e':
							typ = 'HV11'
						elif oword[-2:] == 'st':
							typ = 'HV12'
						elif oword[-2:] == 'en':
							typ = 'HV14'
						elif oword[-2:] == 'et':
							typ = 'HV15'
						elif oword[-1:] == 't':
							typ = 'HV13'
					elif typ == 'ADJ':
						if oword[-2:] == 'er':
							typ = 'ADJER'
						elif oword[-2:] == 'en':
							typ = 'ADJEN'
						elif oword[-2:] == 'em':
							typ = 'ADJEM'
						elif oword[-2:] == 'es':
							typ = 'ADJES'
						elif oword[-1:] == 'e':
							typ = 'ADJE'
					elif typ[0] == 'N':
						if word != oword and typ[-1:] == 'S':
							typ = typ[0:-1]
#
# end of language specific rules for new languages
#
#			print typ
			result = result + " " + typ
#		print result
		return result


