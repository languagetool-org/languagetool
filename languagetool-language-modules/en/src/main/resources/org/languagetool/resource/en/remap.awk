BEGIN {#FS=":|\|"
#Noun                        	
map["N"]="NN"
#Plural                      
map["p"]="NNS"
#Noun Phrase		
map["h"]="" #delete this in FSA
#Verb (usu participle)    
map["V"]="VBG"
#Verb (transitive)     	
map["t"]="VB"
#Verb (intransitive)  	
map["i"]="VB" #no difference here! use a new tag?
#Adjective                     
map["A"]="JJ"
#Adverb                    	
map["v"]="RB"
#Conjunction             	
map["C"]="CC"
#Preposition             	
map["P"]="IN"
#Interjection            	
map["!"]="UH"
#Pronoun                    	
map["r"]="PRP"
#Definite Article       	
map["D"]="DT"
#Indefinite Article     	
map["I"]="DT"
#Nominative               	
map["o"]="" # no such tags in Moby?
#Technical mark
map["\|"]=""	#delete this, unusable
map["\?"]=""	#as above

#exceptions to -ly adverbs:
adj["early"]="yes"
adj["wily"]="yes"
adj["churchly"]="yes"
adj["elderly"]="yes"
adj["priestly"]="yes"
adj["costly"]="yes"
adj["uncousinly"]="yes"
adj["sly"]="yes"
adj["oily"]="yes"
adj["steely"]="yes"
adj["lonely"]="yes"
adj["goodly"]="yes"
adj["godly"]="yes"
adj["ungodly"]="yes"
adj["heavenly"]="yes"
adj["earthly"]="yes"
adj["homely"]="yes"
adj["unholy"]="yes"
adj["dastardly"]="yes"
adj["spangly"]="yes"
adj["girly"]="yes"
adj["portly"]="yes"
adj["trickly"]="yes"
adj["unruly"]="yes"
adj["smelly"]="yes"
adj["measly"]="yes"
adj["bubbly"]="yes"
adj["burly"]="yes"
adj["sparkly"]="yes"
adj["fly"]="yes"

}
#additional rules: JJ & "er"_END_OF_WORD=JJR
#additional rules: JJ & "est"_END_OF_WORD=JJS
#NN & Uppercase = NNP
#NNS & Uppercase = NNPS
#verbs: case of 5 fields
#give V: gave | given | giving | gives
# VBP     VBD    VBN     VBG      VBZ
#in case of 4 fields: VBD=VBN.
#PRP$, WDT, WP, WP$, WRB only by enumeration
#MD by enumeration

#kill single-letter entries:
/^[a-zA-Z][\t ]/ {$0=""}

#adjectives
/A:|A\?:/ && !/'/ {
gsub(/{[a-zA-Z:0-9_]+}/,"")
gsub(/,/,"")
gsub(/[0-9]/,"")
split ($0, adjective, /\|/)
#print "1" adjective[1]
#print "2" adjective[2]
split(adjective[1], jjr,":")
split(jjr[2], jjr_forms)
split(adjective[2],jjs_forms)
gsub(/\|/,"")
mark=0
total=0
if ($1"_END"!~/ly_END/ || adj[$1]=="yes") print $1 "\t" $1 "\tJJ"; else print $1 "\t" $1 "\tRB"
for (n in jjr_forms) {
	#if ($i"_END"!~/er_END/) 
	if (jjr_forms[n]!~/\?|<|\.|!/)
		{print jjr_forms[n] "\t" $1 "\tJJR"
		JJR[jjr_forms[n]]=$1
		set++
		}
	}
for (n in jjs_forms) {	
	if (jjs_forms[n]!~/\?|<|\.|!/)
		{print jjs_forms[n] "\t" $1 "\tJJS"
		JJS[jjs_forms[n]]=$1
		set++
		}
	}
}

/N:|N\?:/ && !/'/ {
if ($1~/less/ && $2~/N\?:/ && NF==3) {
 print $1 "\t" $1 "\tJJ"
}
else {
if ($1!~/[A-Z]/) print $1 "\t" $1 "\tNN"; else print $1 "\t" $1 "\tNNP"
gsub(/[,<]/,"")
gsub(/{[a-zA-Z:0-9_]+}/,"")
gsub(/[0-9]\.[0-9]/,"")
gsub(/[0-9]+ /,"")
#systematic error in infl.txt for some gerunds:
$0 = gensub(/(ling N\?: [a-z]+ling$)/, "\\1s", "g", $0)
for (i=3;i<=NF;i++) {
#	print ">>" $i
#	print "NF=" NF, "string is:" $0
	if ($i!~/[\?\~\!]/ && "PFX"$i"SFX"!~/PFX([0-9]+|\|)SFX/)
	if ($i!~/[A-Z]/) {print $i "\t" $1 "\tNNS"
				nns[$i]=$1
				}
	else 
		print $i "\t" $1 "\tNNPS"
}
}
}

/V:/ && !/'/ {
gsub(/,/,"")
gsub(/{[a-zA-Z:0-9_]+}/,"")
gsub(/[0-9]\.[0-9]/,"")
gsub(/[0-9]+ /,"")
split($0,verb_fields,/\||:/)
#for (kk=1;kk<=10;kk++) {
#	print kk verb_fields[kk]
#	}
if (verb_fields[5]=="") {
	print $1 "\t" $1 "\tVB"
	print $1 "\t" $1 "\tVBP"
	split (verb_fields[2], VBD, " ")
	for (n in VBD) {
	if (VBD[n]!~/[\?\~\!<]/ && "PFX"VBD[n]"SFX"!~/PFX([0-9]+|\|)SFX/) print VBD[n] "\t" $1 "\tVBD"
	if (VBD[n]!~/[\?\~\!<]/ && "PFX"VBD[n]"SFX"!~/PFX([0-9]+|\|)SFX/) print VBD[n] "\t" $1 "\tVBN"
	}
	split (verb_fields[3], VBG, " ")
	for (n in VBG) {
	if (VBG[n]!~/[\?\~\!<]/ && "PFX"VBG[n]"SFX"!~/PFX([0-9]+|\|)SFX/) print VBG[n] "\t" $1 "\tVBG"
	}
	split (verb_fields[4], VBZ, " ")
	for (n in VBZ) {
	if (VBZ[n]!~/[\?\~\!<]/ && "PFX"VBZ[n]"SFX"!~/PFX([0-9]+|\|)SFX/) print VBZ[n] "\t" $1 "\tVBZ"
	}
	}
if (verb_fields[5]!="") {
	print $1 "\t" $1 "\tVB"
	print $1 "\t" $1 "\tVBP"
	split (verb_fields[2], VBD, " ")
	for (n in VBD) {
		if (VBD[n]!~/[\?\~\!<]/ && "PFX"VBD[n]"SFX"!~/PFX([0-9]+|\|)SFX/) print VBD[n] "\t" $1 "\tVBD"
	}
	split (verb_fields[3], VBN, " ")
	for (n in VBN) {
		if (VBN[n]!~/[\?\~\!<]/ && "PFX"VBN[n]"SFX"!~/PFX([0-9]+|\|)SFX/) print VBN[n] "\t" $1 "\tVBN"
	}
	split (verb_fields[4], VBG, " ")
	for (n in VBG) {
		if (VBG[n]!~/[\?\~\!<]/ && "PFX"VBG[n]"SFX"!~/PFX([0-9]+|\|)SFX/) print VBG[n] "\t" $1 "\tVBG"
	}
	split (verb_fields[5], VBZ, " ")
	for (n in VBZ) {
		if (VBZ[n]!~/[\?\~\!<]/ && "PFX"VBZ[n]"SFX"!~/PFX([0-9]+|\|)SFX/) print VBZ[n] "\t" $1 "\tVBZ"
	}
	}
}


"BEGIN"$2"END"~/BEGINCEND/ { print $1 "\t" $1 "\tCC"}
"BEGIN"$2"END"~/BEGINPEND/ { print $1 "\t" $1 "\t"map[$2]}
"BEGIN"$2"END"~/BEGIN\!END/ { print $1 "\t" $1 "\t"map[$2]}
#aaaa "BEGIN"$2"END"~/BEGINDEND/ { print $1 "\t" $1 "\t"map[$2]} #its as determiner!
"BEGIN"$2"END"~/BEGINIEND/ && !/ /{ print $1 "\t" $1 "\t"map[$2]} 
"BEGIN"$2"END"~/BEGINrEND/ { print $1 "\t" $1 "\t"map[$2]}
"BEGIN"$2"END"~/BEGINvEND/ { print $1 "\t" $1 "\t"map[$2]}
#"BEGIN"$2"END"~/BEGINANEND/ { if (JJR[$1]=="" && JJS[$1]=="") print $1 "\t" $1 "\tJJ"}

#interjections
/!/ && !/[ ']/ {
if ($1!~/!/) {
	print $1 "\t" $1 "\t"map["!"]
	gsub(/!/,"")
	}
	}


/\t(vA|Av)$/ && !/[ ']/ {print $1 "\t" $1 "\t"map["v"]
	print $1 "\t" $1 "\t"map["A"]}

/\tvAN$/ && !/[ ']/ {print $1 "\t" $1 "\t"map["v"]
	print $1 "\t" $1 "\t"map["A"]
	print $1 "\t" $1 "\t"map["N"]
	}

/\tvAtV$/ && !/[ ']/ {print $1 "\t" $1 "\t"map["v"] 
gsub(/\tvAtV/,"\tAVt")
}

/\tv$/ && !/[ ']/ {print $1 "\t" $1 "\t"map["v"]}

/\tvP$/ && !/[ ']/ {print $1 "\t" $1 "\t"map["v"]
print $1 "\t" $1 "\t"map["P"]
}

/\tvN$/ && !/[ ']/ {print $1 "\t" $1 "\t"map["v"]
print $1 "\t" $1 "\t"map["N"]
}

/\t(\|v|vP)/ && !/[ ']/ {
	if (JJR[$1]!="") {
	print $1 "\t" $1 "\tRBR"}
	if (JJS[$1]!="") {
	print $1 "\t" $1 "\tRBS"}
	if (JJS[$1]=="" && JJS[$1]=="") {
	print $1 "\t" $1 "\t"map["v"]}
	}

{gsub(/\tvPC/, "\tPCv")
 gsub(/\tvC/,"\tCv/")
}

/\tPCv/ {print $1 "\t" $1 "\t"map["P"]
	print $1 "\t" $1 "\t"map["C"]
	print $1 "\t" $1 "\t"map["v"]}

/\tCv/ {print $1 "\t" $1 "\t"map["C"]
	print $1 "\t" $1 "\t"map["v"]}

/\tANtV/ {if (JJR[$1]=="" && JJS[$1]=="")  
			if ($1"_END"!~/ly_END/ || adj[$1]=="yes") print $1 "\t" $1 "\tJJ"; else print $1 "\t" $1 "\tRB"
		if (nns[$1]=="" || $1"_END"~/ics_END/) {
			if ($1!~/[A-Z]/) print $1 "\t" $1 "\tNN"; else print $1 "\t" $1 "\tNNP"
		}
		print $1 "\t" $1 "\t"map["t"]
		print $1 "\t" $1 "\t"map["V"]
		}
/\tvNA$/ && !/'/  {
print $1 "\t" $1 "\t"map["v"]
gsub(/\tvNA/,"\tNA")
}

/\t[AN][AN]/ && !/'/ {
		if (JJR[$1]=="" && JJS[$1]=="" && $1"_END"!~/ism_END/) print $1 "\t" $1 "\tJJ" 
		if ($1!~/[A-Z]/) print $1 "\t" $1 "\tNN"; else print $1 "\t" $1 "\tNNP"
	 }
/\tN$/ && !/[ ']/ {
	if (nns[$1]=="" || $1"_END"~/ics_END/) {
	if ($1!~/[A-Z]/) print $1 "\t" $1 "\tNN"; else print $1 "\t" $1 "\tNNP"
	}
	 }
/\t\|N$/ && !/[ ']/ && !/^[0-9\.]+\t/{
	if (nns[$1]=="" || $1"_END"~/ics_END/) {
	if ($1!~/[A-Z]/) print $1 "\t" $1 "\tNN"; else print $1 "\t" $1 "\tNNP"
	}
	 }
/\t[N!][N!]/ && !/[ ']/ {
	if (nns[$1]=="" || $1"_END"~/ics_END/) {
	if ($1!~/[A-Z]/) print $1 "\t" $1 "\tNN"; else print $1 "\t" $1 "\tNNP"
	}
	print $1 "\t" $1 "\t"map["!"]
	 }
	
/\tA$/ && !/[ ']/{	
	if (JJR[$1]=="" && JJS[$1]=="")  {
	if ($1"_END"!~/ly_END/ || adj[$1]=="yes") print $1 "\t" $1 "\tJJ"; else print $1 "\t" $1 "\tRB"}
			#print $1 "\t" $1 "\t"map["A"]
	 }

/\t\|NA$/ && !/[ ']/ && !/^[0-9\.]+\t/{
	if (JJR[$1]=="" && JJS[$1]=="")  
			if ($1"_END"!~/ly_END/ || adj[$1]=="yes") print $1 "\t" $1 "\tJJ"; else print $1 "\t" $1 "\tRB"
	 }

/\tpN$/ && !/[ ']/ {if (nns[$1]=="") {if ($1!~/[A-Z]/) print $1 "\t" $1 "\tNNS"; else print $1 "\t" $1 "\tNNPS"}
	 }
/\tp$/ && !/[ ']/ {if (nns[$1]=="") {if ($1~/[A-Z]/) print $1 "\t" $1 "\tNNPS"}
	 }
/\tDA$/ && !/[ ']/{
#	if (JJR[$1]=="" && JJS[$1]=="")  
#			print $1 "\t" $1 "\t"map["A"]
	print $1 "\t" $1 "\t"map["D"]
	 }
/\tAv$/ && !/[ ']/{
	if (JJR[$1]=="" && JJS[$1]=="")  
			if ($1"_END"!~/ly_END/ || adj[$1]=="yes") print $1 "\t" $1 "\tJJ"; else print $1 "\t" $1 "\tRB"
#	print $1 "\t" $1 "\t"map["v"]
}
/\t(AV|AVti|AVt)$/ && !/[ ']/{ #AVti - only two words: articulate, foliate
	if (JJR[$1]=="" && JJS[$1]=="")  
			if ($1"_END"!~/ly_END/ || adj[$1]=="yes") print $1 "\t" $1 "\tJJ"; else print $1 "\t" $1 "\tRB"
#	print $1 "\t" $1 "\t"map["V"]
#this is wrong, actually V = VBG & VBN
}


/\t\|A$/ && !/[ ']/{
	if (JJR[$1]=="" && JJS[$1]=="")  
			if ($1"_END"!~/ly_END/ || adj[$1]=="yes") print $1 "\t" $1 "\tJJ"; else print $1 "\t" $1 "\tRB"
	 }

/\t\|Av$/ && !/[ ']/{
	if (JJR[$1]=="" && JJS[$1]=="")  
			if ($1"_END"!~/ly_END/ || adj[$1]=="yes") print $1 "\t" $1 "\tJJ"; else print $1 "\t" $1 "\tRB"
	
	 }

/\tAtNV$/ && !/[ ']/ {
	if (JJR[$1]=="" && JJS[$1]=="")  
			if ($1"_END"!~/ly_END/ || adj[$1]=="yes") print $1 "\t" $1 "\tJJ"; else print $1 "\t" $1 "\tRB"
	if (nns[$1]=="" || $1"_END"~/ics_END/) {
		if ($1!~/[A-Z]/) print $1 "\t" $1 "\tNN"; else print $1 "\t" $1 "\tNNP"
		}
	print $1 "\t" $1 "\t"map["t"]
	print $1 "\t" $1 "\t"map["V"]
	}

/\t\!$/ && !/[ ']/{
		print $1 "\t" $1 "\t"map["!"]
	 }
