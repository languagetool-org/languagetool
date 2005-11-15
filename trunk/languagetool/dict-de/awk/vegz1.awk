#
# vegz1.awk
#
# given a word and the ispell flags
# We are looking for the valid words according to that
#
# e.g.. üzérkedik/EX
#
# usage  awk -f vegz1.awk < x1 (where x1 is a file containing the line üzérkedik/EX)
#  or awk -f vegz1.awk <x1 > x2
# the  hu_HU.aff file must be in the same directory where this awk file is. 
#  for prefixes only the ground word set will be built, since that is enogh for test
#
#  read in the aff file first
#
BEGIN { 
        room = "   \t\t\t\t";
	while (getline < "deutsch.aff"  > 0){
		if(NF < 1) continue;
		if (substr($0,1,1) == "#") continue;
		if(substr($0,2,3) == "FX "){
				split($0, a);
				arrname = a[2];
				arrtype[arrname] = substr(a[1],1,1);
				yesno[arrname] = a[3];
				count[arrname] = a[4] + 0;
				for(i = 1; i <=count[arrname]; i++){
					getline < "deutsch.aff";
					lines[arrname, i] = $0;
				}
		}
	}
}
#
#  the functions
#
function template_match(szo, template)
{
	if(length(template) > length(szo)){
#	   print "tm0:"szo"--"template;
	   return 0;
	}
	alszo = substr(szo, length(szo) - length(template) + 1, length(template));
	for(ii = 1; ii <= length(template);ii++){
		c1 = substr(template, ii,1);
		if(c1 != "." && c1 != substr(alszo,ii,1)){
#		  print "tm0:"szo"--"template;
		  return 0; 
		}
       }
       kk = 1;
	for(ii = 1; ii <= length(template);ii++){
		c1 = substr(template, ii,1);
		if(c1 == "." ){
			c2 = substr(alszo, ii,1);
			temp = zartarr[kk];
			if(substr(temp,1,1) == "^"){
				for(jj = 2; jj <= length(temp); jj++){
				   if(substr(temp, jj,1) == c2){ 
#		  		     print "tm01:"szo"--"template;
				     return 0;
				   }
				   
			       }
		   	} else{
				for(jj = 1; jj <= length(temp); jj++){
				   if(substr(temp, jj,1) == c2) break;
				   if(jj == length(temp)) {
#		  		     print "tm02:"szo"--"template;
				     return 0;
				   }
				}
		    }
		    ++kk;		
		} # c1 == "."
       } # for ii
#	print "tm1:"szo"--"template;
	return 1;
}
function ptemplate_match(szo, template)
{
	if(length(template) > length(szo)) return 0;
	alszo = substr(szo, 1, length(template));
	for(ii = 1; ii <= length(template);ii++){
		c1 = substr(template, ii,1);
		if(c1 != "." && c1 != substr(alszo,ii,1))
		  return 0; 
       }
       kk = 1;
	for(ii = 1; ii <= length(template);ii++){
		c1 = substr(template, ii,1);
		if(c1 == "." ){
			c2 = substr(alszo, ii,1);
			temp = zartarr[kk];
			if(substr(temp,1,1) == "^"){
				for(jj = 2; jj <= length(temp); jj++){
				   if(substr(temp, jj,1) == c2) return 0;
				   
			       }
		   	} else{
				for(jj = 1; jj <= length(temp); jj++){
				   if(substr(temp, jj,1) == c2) break;
				   if(jj == length(temp)) return 0;
				}
		    }
		    ++kk;		
		} # c1 == "."
       } # for ii
	return 1;
}

function  find_template(c)
{
	 zartak = 1;
	 template = "";
                      for(i1 = 1; i1 <=n1; i1++){
                      	  if(c[i1] == "") continue;
                      	  if(index(c[i1], "]")){
                      	  	split(c[i1],d,"]");
                      	  	if(d[1] != ""){
                      	  		zartarr[zartak++] = d[1];
                      	  		template = template "." d[2];
                      	  	} else
                      	  		template = template c[i1];
                      	  }else
                      	      template = template c[i1];
                   } # for i1
                   return zartak;
}

function pbuild_words(szo,cut_it, ending, sfx_sor)
{
       if(length(cut_it) > length(szo)) return 0;
	if(cut_it != "0")
	  ujszo = substr(szo, length(cut_it)+1, length(szo)-length(cut-it)); 
	 else ujszo = szo;
	 if(ending != "0")
	    print ending ujszo room sfx_sor;
	 else
	    print ujszo room sfx_sor;
	 return 1;
}
function build_words(szo,cut_it, ending, sfx_sor)
{
       if(length(cut_it) > length(szo)) return 0;
	if(cut_it != "0")
	  ujszo = substr(szo, 1, length(szo) - length(cut_it)); 
	 else ujszo = szo;
	 if(ending != "0")
	    print ujszo ending room sfx_sor;
	 else
	    print ujszo room sfx_sor;
	 return 1;
}
function pbuild_word_without_template(a,b)
{
                if(b[5] == "." || (b[5] == substr(a[1], 1, length(b[5]))) ){
#print a[1]"--"b[3];
                  	if(b[3] != "0") { 
                  	      ujszo = substr(a[1], length(b[3]) +1, length(a[1]) - length( b[3]) );
#print ujszo;           
			}
                        else
                  	           ujszo = a[1];
		   if(b[4] != "0")
		     ujszo = b[4] ujszo;
                  print  ujszo room lines[flag,j];
                  return 1;
                } else
                   return 0;
}              	           
function build_word_without_template(a,b)
{
                if(b[5] == "." || (b[5] == substr(a[1], length(a[1])-length(b[5]) +1, length(b[5]))) ){
                  	if(b[3] != "0") { 
                  	      ujszo = substr(a[1],1, length(a[1]) -length(b[3]));
                  	}
                        else
                  	           ujszo = a[1] ;
		  if(b[4] != "0")
		    ujszo = ujszo b[4];
                  print  ujszo room lines[flag,j];
                  return 1;
                } else
                   return 0;
}              	           
#
#
#  here we read in the wordlist, e.g üzérkedik/EX
#
#
   { n = split($0,a,"/");
# a1 a szo
# a2 a flagek
      for(i = 1; i <= length(a[2]); i++){
      	  flag = substr(a[2], i,1);
      	  if(arrtype[flag] == "S"){
              for(j = 1; j <= count[flag]; j++){         
#
# SFX  E 0 ük [dk]e[lm]a   data from lines to b   	
#
                  m = split( lines[flag,j], b);
                  if(index(b[5], "[") == 0){
                  	build_word_without_template(a,b);
                  } else { # van benne [
                      n1 = split(b[5], c, "[");
                      
                       template = "";
                      zartak = find_template(c); # used in find_template
                   #
#                   for(kk = 1; kk < zartak; kk++)  # debug
#                      print zartarr[kk];
#                     print template   
                     #    
                     # illik a template a szora?
                     #
#		     print a[1]"++"template;
                     if(template_match(a[1], template)){
                     	 build_words(a[1], b[3], b[4], lines[flag,j]);
                   }
                              
	            } # van benne [                
      	      } # for j
      	  } # arrtype flag = S
      	  else { #PFX flag
               for(j = 1; j <= count[flag]; j++){         
     	  	  m = split(lines[flag,j], b);
		       if(index(b[5], "[") == 0){
#		        if(pmatch(
                  	pbuild_word_without_template(a,b);
                  } else { # van benne [
                      n1 = split(b[5], c, "[");
                      
                       template = "";
                      zartak = find_template(c); # used in find_template
                   #
#                   for(kk = 1; kk < zartak; kk++)  # debug
#                      print zartarr[kk];
#                     print template   
                     #    
                     # illik a template a szora?
                     #
                     if(ptemplate_match(a[1], template)){
                     	 pbuild_words(a[1], b[3], b[4], lines[flag,j]);
                   }                       
	            } # van benne [                
 #    	  	  print pflag[4] a[1] room lines[flag,j];  
     	     } # for j
     	  } # arrtype P	
     } # for i (minden flag) 	 
    }# end of reading in the wordlist
				        
				        
					
		