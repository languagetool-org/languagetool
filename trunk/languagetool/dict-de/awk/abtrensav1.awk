#
#abtren.awk
#
function Is_ab(s)
{
  if(index(s,"a") || index(s,"b"))
    return 1;
  return 0;
}
function get_abd(s)
{
  result = "";
  if(index(s,"D")) 
    result = "D";
  if(index(s,"a")) 
    result = result "a";
  if(index(s,"b")) 
    result = result "b";
#  print "---get_abd---"result;
  return result;
}
function get_noabd(s)
{
  result = "";
  if(index(s,"D")) {
    split(s, c, "D");
    result = c[1]c[2];
  } else
    result = s;
#  print "--1--" result;
   if(index(result,"a")){
    split(result, c, "a");
#	 print c[1]"+++"c[2];
    result = c[1]c[2];
  }
#   print "--2--" result;
  if(index(result,"b")){
    split(result, c, "b");
    result = c[1]c[2];
  }
#  print "--3--"result;
  return result;
}
    {
#	  print "---$0---"$0;
	  split($0, a, "/");
	  if(Is_ab(a[2])){
	    abd = get_abd(a[2]);
		 noabd = get_noabd(a[2]);
		 if(abd != "")
	      print a[1]"/"abd;
		 if(noabd != "")
	      print a[1]"/"noabd;
		}# else
#		  print $0;
	 }
		    
