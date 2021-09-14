BEGIN {FS="\t"
filter_file="filter-archaic.txt"; #filtered out file
while ((getline < filter_file)  > 0){
	if ($0!~/^#/) 
		archaic[$0]="archaic"		
}
}
#exclude archaic forms
{if (archaic[$0]=="") print $2"|"$3"\t"$1}
