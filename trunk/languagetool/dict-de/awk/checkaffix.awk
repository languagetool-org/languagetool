#
# check_affix.awk
#  check affix file for Oo myspell
#
function is_fx(s)
{
   if(s == "PFX")
	  return 1;
   if(s == "SFX")
	  return 2;
	 if(s == "REP")
	  return 3;
   return 0;
}
BEGIN {linenr = 0; nr = 0;}

    { ++linenr;
	   if(nr){
		  if(sorszam++ == nr){
		     sorszam = 0;
			  nr = 0;
		
			  if(NF > 1 && substr($0,1,1) != "#" && ((is_fx($1) < 3) && (is_fx($1) > 0)  && ($4 + 0) == 0)){
		        print "2. error in line " linenr " line:" $0;
#			  next;
           }
		  }  
		  else if($1 != veg || ((fxtyp < 3) && (($2 != typ) || (NF < 5)) )){
		    print "1. error in line " linenr" line:" $0;
		     sorszam = 0;
			  nr = 0;
			  next;		 
		 }
		  else
		    next;
		 }
	   while(!(ret = is_fx($1)))next;
		 veg = $1;
		 typ = $2;
		 fxtyp = ret;
		 if(ret < 3) nr = $4 + 0;
		    else nr = $2 + 0;
       sorszam = 0;	
#		 print "---1 " $0" nr:"nr;		
		}
