#dupl.awk
# eliminate duplicates like word1/F1
#                           word1/F2
#          result:          word1/F1F2
#
   { #print "++" $0;
      n = split($0, a, "/");
      if( a[1] == sav) {
	     if(length(sav) || n > 1){
	         print sav "/" sav1 a[2];
			 sav = ""; sav1 = "";
			 next;
        }
#		   else print sav;
      }
	  else { # a1 != sav
	     if(sav1 != "" && sav != ""){
	         print sav"/" sav1;
			 }
          else if(sav != ""){
		     print sav;
			 }
     }
	  sav = a[1];
	  sav1 = a[2];
   }
END {
	     if(sav1 != "" && sav != ""){
	         print sav"/" sav1;
			 }
          else if(sav != ""){
		     print sav;
			 }
    }
