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
#
import array
import codecs
import os
from string import *
import sys

class Wfhun:

	encoding = "latin1"
	
	def __init__(self):
		return
	
	def getTyp(self,typ, oword, word):
		dif = len(oword) - len(word)
		if (typ[0] == 'V' or typ[0:2] == 'SI') and word != oword:
			ik = ''
			telo = 'SI'
			if typ[0] == 'V':
				telo = 'V'
			if oword[0:2] != word[0:2]:
				ik = 'IK'
			if oword[-3:]  in (u'iük','iuk', 'nak', 'nek','tak', 'tek') or oword[-2:] in (u'ák', u'ék'):
				typ = ik + telo + '6'
			elif oword[-3:]  in ('tok','tek', u'tök'):
				typ = ik + telo + '5'
			elif oword[-3:]  in (u'ünk','unk', u'ánk', u'énk') or oword[-2:] in ('uk', u'ük'):
				typ = ik + telo + '4'
			elif oword[-2:]  in ('sz','od', 'ed', u'öd',u'éd','ad',u'ád'):
				typ = ik + telo + '2'
			elif oword[-2:]  in ('ok','ek',u'ök','om','em',u'öm', u'ám', u'ém', 'am'):
				typ = ik + telo + '1'
			elif oword[-2:] in ('va', 've') or oword[-3:] in (u'ván', u'vén'):
				typ = 'ADV'
			elif oword[-2:]  == 'ni':
				typ = 'INF'
			else:
				typ = ik + telo + '3'
		elif typ[0:3] == 'PP4':
			if oword != 'mi':
				typ = 'ADV'
		elif typ[0:3] == 'ADJ':
			if oword[-2:]  in ('ek','ok', 'ak', u'ék', u'ák') and dif > 0 and (dif < 3 or ((word[0:1] != oword[0:1]) and dif < 9)):
				typ = 'ADJP'
			elif oword[-1:]  in (u'é',u'á') and dif > 0 and (dif < 5 or ((word[0:1] != oword[0:1]) and dif < 12)):
				typ = 'ADV'
			elif oword[-2:] in ('an', 'en', 'bb','ul',u'ül') and dif == 2:
				typ = 'ADV'
			elif dif != 0:
				typ = 'ADV'
		elif typ[0] == 'N':
			if oword[-1] == 'k' and oword[-2] in ('a',u'á', 'e',u'é','i',u'í','o',u'ó',u'ö',u'õ','u',u'ú',u'ü',u'û') and dif > 0 and dif < 3 :
				typ = 'NP'
			elif oword[-1:] == 'i' and dif == 1:
				typ = 'DNA'
			elif (oword[-1:] in(u'ú', u'û') and dif == 1) or (oword[-2:] in (u'jú', u'jû')  and dif == 2):
				typ = 'ADJS'
			elif typ == 'N':
				if oword[-1] == 'k' and oword == word:
					typ = 'NP'
				else:
					typ = 'NS'
			elif  dif >= 2:
				typ = 'N'
		if typ[0] == 'N' and oword == word and word[-1] != 'k':
				typ = typ+'N'
		return typ		
					
	

