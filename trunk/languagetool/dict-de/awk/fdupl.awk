#fdupl.awk
   { split($0,a,"/");
      if(szo[a[1]]) print "duplikat: " a[1];
	  szo[a[1]] = "1";
   }  
