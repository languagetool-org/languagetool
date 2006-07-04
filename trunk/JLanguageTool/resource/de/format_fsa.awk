BEGIN {FS=" "
short_tag["A"]="A"
short_tag["ABK"]="A"
short_tag["ADJ"]="J"	#ADJ
short_tag["ADV"]="A"
short_tag["AKK"]="A"	#AKK
short_tag["ALG"]="A"
short_tag["ART"]="T"	#ART
short_tag["AUX"]="A"
short_tag["B"]="B"
short_tag["B/S"]="B"
short_tag["BEG"]="B"
short_tag["CAU"]="C"
short_tag["COU"]="C"
short_tag["DAT"]="D"	#DAT
short_tag["DAT,AKK"]="D"
short_tag["DEF"]="D"
short_tag["DEM"]="D"
short_tag["EIG"]="E"	#EIG
short_tag["FEM"]="F"	#FEM
short_tag["GEB"]="G"
short_tag["GEN"]="G"	#GEN
short_tag["GEN,DAT"]="G"
short_tag["GEO"]="G"
short_tag["GRU"]="G"
short_tag["IMP"]="I"
short_tag["IND"]="I"
short_tag["INF"]="I"
short_tag["INJ"]="I"
short_tag["INR"]="I"
short_tag["INR,PRO"]="I"
short_tag["KJ1"]="K"
short_tag["KJ2"]="K"
short_tag["KOM"]="K"
short_tag["LOK"]="L"
short_tag["LOK,CAU"]="L"
short_tag["LOK,MOD"]="L"
short_tag["LOK,PRO"]="L"
short_tag["MAS"]="M"	#MAS
short_tag["MOD"]="M"
short_tag["MOD,INR"]="M"
short_tag["MOD,PRO"]="M"
short_tag["MOD,TMP"]="M"
short_tag["MOD,TMP,LOK"]="M"
short_tag["MOU"]="M"
short_tag["NAC"]="N"
short_tag["NEB"]="N"
short_tag["NEG"]="N"
short_tag["NEU"]="N"
short_tag["NIL"]="N"
short_tag["NOG"]="O"	#NOG
short_tag["NOM"]="N"	#NOM
short_tag["NOM,AKK"]="N"
short_tag["NOM,DAT,AKK"]="N"
short_tag["NON"]="N"
short_tag["PA1"]="P"
short_tag["PA2"]="P"
short_tag["PAR"]="P"
short_tag["PER"]="P"
short_tag["PLU"]="P"	#PLU
short_tag["POS"]="P"
short_tag["PRD"]="P"
short_tag["PRI"]="P"
short_tag["PRO"]="O"	#PRO
short_tag["PRO,CAU"]="P"
short_tag["PRO,TMP"]="P"
short_tag["PRP"]="P"
short_tag["PRT"]="P"
short_tag["PRÄ"]="P"
short_tag["REF"]="R"
short_tag["RIN"]="R"
short_tag["SFT"]="S"
short_tag["SIN"]="S"	#SIN
short_tag["SOL"]="S"
short_tag["STD"]="S"
short_tag["STV"]="S"
short_tag["SUB"]="S"	#SUB
short_tag["SUP"]="S"
short_tag["TMP"]="T"
short_tag["TMP,LOK,MOD"]="T"
short_tag["TMP,MOD"]="T"
short_tag["UNT"]="U"
short_tag["VER"]="V"
short_tag["VGL"]="V"
short_tag["VOR"]="V"
short_tag["WAT"]="W"
short_tag["ZAL"]="Z"
short_tag["ZUS"]="Z"
}
#getting lemma
#format:
#form
#lemma POS_tags base lemma (optional)
#@
#
!/\*/{if (NF==1 && $1!="@") wyraz = $1

if (NF>1) {	
	printf wyraz"\t"$1"\t"
	gsub(",","+") #DAT,AKK
	tags=""
	for (i=2;i<=(NF-1);i++)
		tags=tags $i":"
	if (toupper($NF)==$NF && $NF!~/[0-9]/) tags = tags $NF
	#trailing :
	gsub(/:$/,"", tags)
	printf tags "\n"
	}
}