BEGIN {FS=";"}
BEGIN  {print "<wordlist locale=\"sr\" description=\"Српски\" version=\"3\">"}
{print  " <w f=\""$3"\" flags=\"\">"$1"</w>"  }
END {print "</wordlist>"}