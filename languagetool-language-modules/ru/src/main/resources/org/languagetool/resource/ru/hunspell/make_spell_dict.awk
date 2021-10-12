BEGIN {FS="\t"}
{if ($3~/.*(Name|Fam|Patr).*/) {print (toupper(substr($1,0,1)))(substr($1,2))} else print($1) }
