# nodupsw.awk
# eliminate duplicated switches
#  word/aab -> word/ab
#
   { split($1, a, "/");
      if(length(a[2])){
	     c = substr(a[2], 1,1);
		 out = c;
	     for(i = 2; i <= length(a[2]);i++){
		    d = substr(a[2],i,1);
		    if(c != d)
			   out = out d;
			c = d;
          }
		 printf("%s/%s\n", a[1],out);
        }
        else print $0;
     }


