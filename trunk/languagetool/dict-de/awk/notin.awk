#
#  notin.awk
#  input  wln.txt in form wd {r}
#                                     wd2 {e}
#  input  dbe9.txt in form wd {r}
#                                     wd2 {e}
#  input wl.txt in form wd1 {r} bed1/bed2/bed3
# output  y wd1 {korr nem} bed1/bed2/bed3
#   output all words of the read  file which are not contained in the reference file
#
BEGIN { 
	while ( (getline < "x.txt") > 0){ # reference file
	   split($0, a, "/");
#	    szo[$1] = $2;
	    szo[a[1]] = "1";
                   ++imax;
	}
            }
# read file
   { ++szavak;
     split($0, b, "/");
    if(szo[b[1]] == "") {
#        print "talalt", szo[$1];
        print $0; 
     ++nemtalalt;
      next;	
    }
    else ++talalt;
   }
#END {printf("beolvastam %d azonos szot talaltam %d-t nem azonos:%d osszesen:%d\n",imax,talalt, nemtalalt, szavak);}
