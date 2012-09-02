# called from prepare_fsa_format.sh
BEGIN {
 FS=" "
}
#getting lemma
#format:
#form
#lemma POS_tags base lemma (optional)
#@
#
!/\*/{if (NF==1 && $1!="@") fullform = $1

if (NF>1) {	
	printf fullform"\t"$1"\t"
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
