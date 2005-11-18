#
# nfonbes.awk
#
# Read a list of German substantives
#  find the right affices for each substantive using deutsch.dic (Oo format)
#   print as a result a word list of substantives/affix switches
#
#
BEGIN { 
   tmpfile= "/tmp/x";
}

      { 
        if(length($1) < 2){
#	   print $1;
	   next;
	}
	gsub("-","_", $1);
	elso = tolower(substr($1,1,1));
	if(elso == substr($1,1,1)){  # no lower case words here
	   next;
	}
	for(i = 2; i <= length($1)-2; i++){
	   szo = substr($1, i, length($1)-i +1);
	   modszo = toupper(substr(szo,1,1)) substr(szo, 2, length(szo)-1);
	   gstr = "grep "modszo"/ deutsch.dic >"tmpfile;
	   close(tmpfile);
#	   print gstr;
	   system(gstr);
#	   system ("cat "tmpfile);
	   if(getline x < tmpfile > 0){
#	     print "x:" x;
	     n = split(x,a,"/");
	     gsub("_", "-",$1);
	     if(n > 1)
	       print $1"/"a[2];
	     else
	      print $1;
	     next;
	   } #else
#	     print "x:" x"++"tmpfile;
	}
       }
	   
