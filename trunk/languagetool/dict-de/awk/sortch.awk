# sortch.awk
# sort flags word/acb -> word/abc
#
function sortchars(a)
{
   len = length(a);
   out = "";
   for(i = 1; i <= len; i++){
      c = substr(a,i,1);  # to be added
	  if( length(out) < 1)  {out = out c; }
	  else  if(length(out) == 1){
	     if(c > out) out = out c;
		 else out = c out;
#		 print "2" out;
      }
#	  print c".."substr(out,1,1)".."substr(out, length(out), 1);
	  if(c < substr(out,1,1)){
	    out = c out;
		continue;
     }
	 else if( c > substr(out, length(out), 1)){
	   out = out c;
	   continue;
	 }
	  else if(length(out) > 1){
	     lg2 = length(out);
	     for(k = 1; k < lg2; k++){
               c1 = substr(out,k,1);
               c2 = substr(out, k+1, 1);
			   eleje = substr(out, 1, k);
			   hatulja = substr(out, k+1, lg2 - k);
#			   print "eleje:" eleje " hatulja:" hatulja " c:" c" c1:"c1 "  k="k"  lg2:"lg2;
			   if((c  == c1) || (c == c2)) break;
			   if(c < c1){
#			       print c " kisebb mint " c1 "---";
			       out = c eleje hatulja;
#				   print "1 " out;
				   break;
			   }
			   else if(c > c1) {
			           if(c < c2) {
				         out = eleje c hatulja;
#					     print "2" out;
					     break;
			          }
			          else if( k == lg2 -1){
#					    out = eleje hatulja c;
#						print "3" out;
						break;
			         }
			  }
			    else continue;
         }# for k
       }# out not null length
	  }# for i
	  return out;
}
      { n = split($0, a, "/");
	     if(n > 1){
		    print a[1]"/" sortchars(a[2]);
        }
		else print $0;
     }
