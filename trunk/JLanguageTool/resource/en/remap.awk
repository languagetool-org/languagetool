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

#adjectives
/A:/ && !/'/ {
gsub(/{.*}/,"")
gsub(/,/,"")
gsub(/\|/,"")
gsub(/[0-9]/,"")
mark=0
total=0
for (i=3;i<=NF;i++) 
	{
	if ($i~/\?|</) mark++
	total++
	}
if (mark==total && "BEGIN"$1!~/BEGIN[A-Z]/) {
	print $1 "\t" $1 "\tJJ"
	mark=0
	total=0
	} else {
if ($1"_END"!~/ly_END/) print $1 "\t" $1 "\tJJ"; else print $1 "\t" $1 "\tRB"
for (i=2;i<=NF;i++) {
	if ($i"_END"~/er_END/) 
		{print $i "\t" $1 "\tJJR"
		JJR[$i]=$1}
	if ($i"_END"~/est_END/) 
		{print $i "\t" $1 "\tJJS"
		JJS[$i]=$1
		}
	}
}
}

/N:|N\?:/ && !/'/ {if ($1!~/[A-Z][a-z]/) print $1 "\t" $1 "\tNN"; else print $1 "\t" $1 "\tNNP"
gsub(/,/,"")
gsub(/{.*}/,"")
gsub(/[0-9]\.[0-9]/,"")
gsub(/[0-9]+ /,"")
for (i=3;i<=NF;i++) {
	if ($i!~/[\?\~\!<]/ && "PFX"$i"SFX"!~/PFX([0-9]+|\|)SFX/)
	if ($i!~/[A-Z][a-z]/) print $i "\t" $1 "\tNNS"; else print $i "\t" $1 "\tNNPS"
}
}

/V:/ && !/be V:/ && !/'/ {
gsub(/,/,"")
gsub(/{.*}/,"")
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
"BEGIN"$2"END"~/BEGINAEND/ { if (JJR[$1]=="" && JJS[$1]=="") print $1 "\t" $1 "\t"map[$2]}
"BEGIN"$2"END"~/BEGINANEND/ { if (JJR[$1]=="" && JJS[$1]=="") print $1 "\t" $1 "\tJJ"}


/\tvA/ {print $1 "\t" $1 "\t"map["v"]
	print $1 "\t" $1 "\t"map["A"]}
	
/\tPCv/ {print $1 "\t" $1 "\t"map["P"]
	print $1 "\t" $1 "\t"map["C"]
	print $1 "\t" $1 "\t"map["v"]}

/\tCv/ {print $1 "\t" $1 "\t"map["C"]
	print $1 "\t" $1 "\t"map["v"]}

/\tANtV/ {print $1 "\t" $1 "\t"map["A"]
		print $1 "\t" $1 "\t"map["N"]
		print $1 "\t" $1 "\t"map["t"]
		print $1 "\t" $1 "\t"map["V"]}
/\tAN/ && !/'/ {print $1 "\t" $1 "\t"map["A"]
		print $1 "\t" $1 "\t"map["N"]
	 }
/\tN$/ && !/[ ']/ {if ($1!~/[A-Z][a-z]/) print $1 "\t" $1 "\tNN"; else print $1 "\t" $1 "\tNNP"
	 }
/\tA$/ && !/[ ']/{
	if (JJR[$1]=="" && JJS[$1]=="")  
			print $1 "\t" $1 "\t"map["A"]
	 }