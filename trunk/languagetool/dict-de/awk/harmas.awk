#harmas.awk
function is_harom(s)
{
  len = length(s);
  elem = "";
  oldelem = "";
  egyezik = 0;
  for(i = 1; i <=len; i++){
    elem = substr(s,i,1);
	 if(elem == oldelem) ++egyezik;
	   else egyezik = 0;
	 if(egyezik ==2)
	   return 1;
	oldelem = elem;
  }
  return 0;
}
function makeketto(s)
{
  len = length(s);
  elem = "";
  oldelem = "";
  egyezik = 0;
  for(i = 1; i <=len; i++){
    elem = substr(s,i,1);
	 if(elem == oldelem) ++egyezik;
	   else egyezik = 0;
	 if(egyezik ==2){
	   elem = substr(s, 0,i-1);
		elem1 = substr(s,i+1, len);
#		print elem"!!"elem1;
	   return elem elem1;
	 }
	oldelem = elem;
  }
  return "";
}

 
  { if(is_harom($0)){
     print $0;
	  ketto[j++] = makeketto($0);
	 }
  }
END { print "------------------------";
      for(i = 0; i < j; i++)
         print ketto[i];
	 }
   