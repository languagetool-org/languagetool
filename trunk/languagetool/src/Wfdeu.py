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

class Wfdeu:

	encoding = "latin1"
	
	def __init__(self):
		return
	
	def getTyp(self,typ, oword, word):
		if typ != "":
			if typ == 'V' or typ == 'HV':
				if oword[-4:] == 'ende' or oword[-5:-1] == 'ende':
					typ = 'ADJV'
			if typ == 'V' or typ == 'HV':
				if oword[-1:] == 'e':
					typ =  typ + '11'
				elif oword[-2:] == 'st':
					typ = typ + '12'
				elif oword[-2:] == 'en':
					typ = typ + '14'
				elif oword[-2:] == 'et':
					typ = typ + '15'
				elif oword[-1:] == 't':
					typ = typ + '13'
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
			elif typ == 'NMS':
				if oword[-2:] == 'in':
					typ = 'NFS'
				elif oword[-5:] == 'innen':
					typ = 'NF'
			if typ[0] == 'N':
				if word != oword and typ[-1:] == 'S':
					typ = typ[0:-1]
		return typ
					
	

