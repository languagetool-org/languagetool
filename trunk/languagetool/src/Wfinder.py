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
#
# Speed up 9 times by helding different 
#  append endings in different arrays  3.July, 2004
#
#  Speed improvement by 30% by doing the above
#   also with the prefixes, and by helding
#   affixes and prefixes in different lists. 4. July, 2004
#

import array
import codecs
import os
import Tagger
import Wfdeu
import Wfhun
from string import *
import time
import sys


#aff_file = "dtest.aff"
#dic_file = "dtest.dic"
#test_file = "test.txt"
yesno = {}
comment = "#"
condlist = []
condlist1 = []
alfab_conddic = {}
palfab_conddic = {}
alfab_condlist_group = []
alfab2_condlist_group = []
alfab2_conddic = {}
palfab2_conddic = {}
alfab2_condlist_group = []
szodic = {}
typdic = {}

class Wfinder:

	encoding = "latin1"
	doubleflags = ""
	doubleflagList=""
	
	def __init__(self, textlanguage):
#		print time.strftime('%X %x %Z')
		self.is_initialized = 0
		self.is_secondflag = 0
		self.textlanguage = textlanguage
		self.wfdeu = Wfdeu.Wfdeu()
		self.wfhun = Wfhun.Wfhun()
		return

	def aff_read(self):
	 	self.aff_file = os.path.join(sys.path[0], "data", Tagger.affFile)
		condlist = []
		alfab_condlist_group = []
		alfab2_condlist_group = []
		faff = codecs.open(self.aff_file, "r", self.encoding)
		l = " "
		for i in range(0,256,1):
			alfab_conddic[i] = []
			palfab_conddic[i] = []
			alfab2_conddic[i] = []
			palfab2_conddic[i] = []
		while l != "":
  			l = faff.readline()
  			ll =  l.split()
  			if len(ll) <= 1:
  				continue
  			if ll[0][0] in comment:
				continue
			if ll[0][1:3] == "FX":
				arrname = ll[1]
				prefix = 0
				if ll[0][0] == 'P':
					prefix = 1
				yesno[arrname] = ll[2]
				for i in range(0, int(ll[3])):
					l = faff.readline()
					bb = l.split()
#					print "%s %d" %(bb,len(bb))
#					print "l:%s bb[2]:%s arrname:%s" %(l,bb[2], arrname)
					strip = bb[2]
					if bb[2] == '0':
						strip = '';
					appnd = bb[3]
					if bb[3] == '0':
						appnd = ''
						appnd_last = '0'
					else:
						if prefix == 0:
							appnd_last = appnd[-1]
						else:
							appnd_last = appnd[0]
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
					secondflag = ""
					if len(bb) >= 7:
						secondflag = bb[6]
						self.is_secondflag = 1
						if find(self.doubleflags,arrname) == -1:
							self.doubleflags = self.doubleflags+arrname
						for elem in secondflag:
							if find(self.doubleflagList,elem) == -1:
								self.doubleflagList = self.doubleflagList+elem
#						print "is_sec:%d" % self.is_secondflag
						alfab2_condlist_group.append(condlist)
						alfab2_condlist_group.append(strip)
						alfab2_condlist_group.append(appnd)
						alfab2_condlist_group.append(arrname)
						alfab2_condlist_group.append(secondflag)
						if prefix == 0:
							alfab2_conddic[ord(appnd_last)].append(alfab2_condlist_group)
						else:
							palfab2_conddic[ord(appnd_last)].append(alfab2_condlist_group)
					alfab_condlist_group.append(condlist)
					alfab_condlist_group.append(strip)
					alfab_condlist_group.append(appnd)
					alfab_condlist_group.append(arrname)
					if prefix == 0:
						alfab_conddic[ord(appnd_last)].append(alfab_condlist_group)
					else:
						palfab_conddic[ord(appnd_last)].append(alfab_condlist_group)
#					print "appended %s to  %s %d" %(appnd.encode('latin1'), appnd_last.encode('latin1'), ord(appnd_last))
					condlist = []
					alfab_condlist_group = []
					alfab2_condlist_group = []
		faff.close()
#		print self.doubleflags
#		for i in range (0,255,1):
#		  print len(alfab_conddic[i])
#		print alfab_conddic[ord('a')]

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

	def do_keytest(self,l):
		if l == "":
			return ""
		if szodic.has_key(l):
			return "+ %s" %l
		else:
			return "- %s" %l

	def suffix2_search(self, l, oarrname, oword):
		retval = ""
		found = 0
		for windex in ord(l[-1]), ord('0'):
			for elem in alfab2_conddic[windex]:
			# elem0: condlist, elem1: strip elem2 = append, elem3 = arrname 
#				print "s2_s l:%s oarr:%s elem[4]:%s  app:%s strip:%s" % (l, oarrname, elem[4],elem[2],elem[1] )
				if found:
					return retval
				if find(elem[4], oarrname) == -1:
					continue
			#
			#  search first only suffixes
			#  since prefix is optional
			#
				appnd    = elem[2]
				if len(appnd):
					if l[-len(appnd):] != appnd:
						continue
#				if len(appnd):
					restoredWord = l[0:len(l)-len(appnd)]
				else:
					restoredWord = l
				condlist = elem[0]
				strip    = elem[1]
				if len(strip):
					restoredWord = restoredWord + strip
				break_it = 0
				if len(condlist) > 0 and len(restoredWord) >= len(condlist): #tktk
					substr = restoredWord[-len(condlist):]
					for i in range(0, len(condlist), 1): #tktk
						if condlist[i][ord(substr[i])] != 1:
							break_it = 1
							break
					if break_it:
						continue
					
				if szodic.has_key(restoredWord):
					flags = szodic[restoredWord]
#					print "s22_s: %s %d %s %s %s %s %s"  % (restoredWord,szodic.has_key(restoredWord),elem[3], oarrname, elem[4], oarrname, flags)
					if flags == "": # tktk
						continue
					else:
						if find(flags, elem[3]) == -1:
							continue  
					retval = "++ %s %s" %(oword,restoredWord)
					found = 1
					return retval
		return retval
	

	def suffix_search(self, l, oldl, oarrname):
		retval = ""
		found = 0
		for windex in ord(l[-1]), ord('0'):
			for elem in alfab_conddic[windex]:
			# elem0: condlist, elem1: strip elem2 = append, elem3 = arrname 
				if found:
					return retval
			#
			#  search first only suffixes
			#  since prefix is optional
			#
				appnd    = elem[2]
				if len(appnd):
					if l[-len(appnd):] != appnd:
						continue
					restoredWord = l[0:len(l)-len(appnd)]
				else:
					restoredWord = l
				condlist = elem[0]
				strip    = elem[1]
				if len(strip):
					restoredWord = restoredWord + strip
				break_it = 0
#				print "%s %s %s %s" %(restoredWord,appnd,strip, elem[3])
				if len(condlist) > 0 and len(restoredWord) >= len(condlist): #tktk
					substr = restoredWord[-len(condlist):]
					for i in range(0, len(condlist), 1): #tktk
						if condlist[i][ord(substr[i])] != 1:
							break_it = 1
							break
					if break_it:
						continue
				if szodic.has_key(restoredWord):
					flags = szodic[restoredWord]
					if flags == "": # tktk
						continue
					else:
						if find(flags, elem[3]) == -1:
							continue
						if oarrname != "" and find(flags, oarrname) == -1:
							continue  
					if oldl != "":
						retval = "+++ %s %s %s" %(oldl, l,restoredWord)
					else: 
						retval = "++ %s %s" %(l,restoredWord)
					found = 1
					return retval
 #		print windex
		return retval
	
	def suffix22_search(self, l, oldl, oarrname):
		retval = ""
		found = 0
		for windex in ord(l[-1]), ord('0'):
			for elem in alfab_conddic[windex]:
			# elem0: condlist, elem1: strip elem2 = append, elem3 = arrname 
#				print "s.d:%s e3:%s app:%s str:%s" % (self.doubleflags, elem[3], elem[2],elem[1]) 
				if find(self.doubleflagList, elem[3]) == -1:
					continue
				if found:
					return retval
			#
			#  search first only suffixes
			#  since prefix is optional
			#
#				print "s22x l:%s oldl:%s oarrname:%s appnd:%s strip:%s" % (l, oldl, oarrname, elem[2], elem[1])
				appnd    = elem[2]
				if len(appnd):
					if l[-len(appnd):] != appnd:
						continue
					restoredWord = l[0:len(l)-len(appnd)]
				else:
					restoredWord = l
				condlist = elem[0]
				strip    = elem[1]
				if len(strip):
					restoredWord = restoredWord + strip
				break_it = 0
#				print "s22: %s %s %s %s" %(restoredWord,appnd,strip, elem[3])
				if len(condlist) > 0 and len(restoredWord) >= len(condlist): #tktk
					substr = restoredWord[-len(condlist):]
					for i in range(0, len(condlist), 1): #tktk
						if condlist[i][ord(substr[i])] != 1:
							break_it = 1
							break
					if break_it:
						continue
#				print "s->s2, rw:%s e3:%s" % (restoredWord, elem[3])
				rval = self.suffix2_search(restoredWord, elem[3], l)
				if rval != "":
					found = 1
					retval = rval
					return rval
 #		print windex
		return retval
		
	def prefix_search(self, l):
		found = 0
		retval = ""
		for windex in ord(l[0]), ord('0'):
			for elem in palfab_conddic[windex]:
				if found:
					return retval
				appnd    = elem[2]
				if appnd == l[:len(appnd)]:  # cut the matching prefix
					l1 = l[len(appnd):]
				else:
					continue
				condlist = elem[0]
				strip    = elem[1]
				if len(strip):
					l1 = strip + l1
				break_it = 0
				if len(condlist) > 0 and len(l1) >= len(condlist): #tktk
					substr = l1[0:len(condlist)]
					for i in range(0, len(condlist), 1): #tktk
						if condlist[i][ord(substr[i])] != 1:
							break_it = 1
							break
					if break_it:
						continue
			#
			# prefix without suffix
			#
				arrname = elem[3]
				if szodic.has_key(l1):
					flags1 = szodic[l1]
					if flags1 != "":
						if find(flags1, arrname) == -1:
							continue
						retval = "++ %s  %s" %(l,l1)
						found = 1
						return retval
						
				if lower(yesno[arrname]) == 'n':
					continue
#
#			check if this unprefixed word 
#				is a valid suffixed one
#
				retval = self.suffix_search(l1, l, arrname)
				if retval != "":
					found = 1
					return retval
		return retval
	
	def prefix22_search(self, l):
		found = 0
		retval = ""
		for windex in ord(l[0]), ord('0'):
			for elem in palfab_conddic[windex]:
				if found:
					return retval
#				print "str:%s app:%s e3:%s dfl:%s df:%s" % (elem[1],elem[2], elem[3],self.doubleflagList,self.doubleflags)
				if find(self.doubleflagList, elem[3]) == -1 and find(self.doubleflags, elem[3]) == -1:
					continue
				appnd    = elem[2]
				if appnd == l[:len(appnd)]:  # cut the matching prefix
					l1 = l[len(appnd):]
				else:
					continue
				condlist = elem[0]
				strip    = elem[1]
				if len(strip):
					l1 = strip + l1
				break_it = 0
				if len(condlist) > 0 and len(l1) >= len(condlist): #tktk
					substr = l1[0:len(condlist)]
					for i in range(0, len(condlist), 1): #tktk
						if condlist[i][ord(substr[i])] != 1:
							break_it = 1
							break
					if break_it:
						continue
			#
			# prefix without suffix
			#
				arrname = elem[3]
#				print "p22->s2 l1:%s e3:%s l:%s" %(l1,elem[3],l)
				rval = self.suffix2_search(l1, elem[3],l)
				if rval != "":
					found = 1
					retval = rval
					return rval
						
				if lower(yesno[arrname]) == 'n':
					continue
#
#			check if this unprefixed word 
#				is a valid suffixed one
#
#				print "ps l1:%s l:%s arrn:%s" % (l1, l, arrname)
				retval = self.suffix22_search(l1, "", "")
				if retval != "":
					found = 1
					return retval
		return retval

		
	def do_test(self,l):
		if l == "":
			return ""
		else:
			oldword = l
			found = 0
#			print "ss l:%s" %l
			retval = self.suffix_search(l, "", "")
			if retval != "":
				found = 1
				return retval
#
# searched all suffixes and not found
# now try to combine all prefixes with all suffixes
# that allow combinations
#
#			print "sp l:%s" %l
			retval = self.prefix_search(l)
			if retval != "":
				found = 1
				return retval
			
			if self.is_secondflag:
#				print "s22 l:%s" %l
				retval = self.suffix22_search(l, "", "")
				if retval != "":
					found = 1
					return retval
#				print "p22 l:%s" %l
				retval = self.prefix22_search(l)
				if retval != "":
					found = 1
					return retval
						
			return "- %s" % oldword

	def test_it(self,l):
		if self.is_initialized == 0:
			self.aff_read()
			self.dic_read()
			self.is_initialized = 1
		lcasetest = 0
		result = self.do_keytest(l)
		if result[0] == '-':
			lu = l[0]
			if lu != lu.lower():
				l1 = lu[0].lower()+l[1:]
				if l1 != l:
					lcasetest = 1;
					result = self.do_keytest(l1)
					#
					# in languages not German more likely to find
					# a lower case word than an uppercase
					#
					if result[0] == '-' and self.textlanguage != 'de':
						tmp = l1
						l1 = l
						l = tmp
		if result[0] == '-':
			result = self.do_test(l)
		if result[0] == '-' and lcasetest == 1:
			result = self.do_test(l1)
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
			if self.textlanguage == 'de':
				typ = self.wfdeu.getTyp(typ, oword, word)
			elif self.textlanguage == 'hu':
#				print word+" "+oword+" "+typ
				typ = self.wfhun.getTyp(typ, oword, word)
#
# end of language specific rules for new languages
#
#			print typ
			result = result + " " + typ
#		print result
		return result


