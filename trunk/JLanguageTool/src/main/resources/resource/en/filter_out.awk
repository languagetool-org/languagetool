BEGIN {oldFS=FS; FS=" "
IGNORECASE=1
filter_file="filter-minus.txt"; #filtered out file
while ((getline < filter_file)  > 0){
		#split($0,pos," ")
		if (!filter_list[tolower($1)])
			filter_list[tolower($1)]=$2
		else 
		      filter_list[tolower($1)]=filter_list[tolower($1)]"+"$2
	#	print $2
		}
		
FS=oldFS}
{if ($1!~/[ \']/) {
	if (filter_list[tolower($1)]) {
	split (filter_list[tolower($1)], possible_tags, "+")
	found = 0
	for (n in possible_tags)
		if (possible_tags[n]==$2) 
			{#print possible_tags[n]
			#print $1
			found++
			}
  	if (found==0) print #$1 " aaa " $2 " bbb " $3 " ccc " $4
	#print filter_list[tolower($1)]
	#print any[$1FS$1]
	}
	else print
}	
}