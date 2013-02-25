# Etiqueta formes verbals
# C=català central, V=valencià, B=Balear
# X=C+V, Y=C+B, Z=V+B

use strict;
use warnings;
use autodie;

my $f1 = "freeling_utf8.txt";
my $out = "freeling_utf8_verbs_reetiquetats.txt";
my $out2 = "verbs_descartats.txt";
my $paraula = "";
my $arrel = "";
my $postag = "";
my $printed=0;

open FILE1, "$f1" or die "Could not open file: $! \n";
open (OUTFILE, ">$out") or die "Cannot open $out for writing \n";
open (OUTFILE2, ">$out2") or die "Cannot open $out for writing \n";
while(my $line = <FILE1>)
{  
	 $printed=0;
	 chomp($line);
	 if ($line =~ /^([^ ]+)([aeiï]ra|[aeiï]res|[àéí]rem|[àéí]reu|[aeiï]ren) ([^ ]+) (VMSI...).?$/)
	 { 
       print OUTFILE "$1$2 $3 $4";
       print OUTFILE "V\n";
       $printed=1;
   }
   elsif ($line =~ /^(.*ten[cgd].*) (.*tenir) (V......).?$/)
	 { 
       print OUTFILE "$1 $2 $3";
       print OUTFILE "B\n";
       $printed=1;
   }
   elsif ($line =~ /^(.*ven[cgd].*) (.*venir) (V......).?$/)
	 { 
       print OUTFILE "$1 $2 $3";
       print OUTFILE "B\n";
       $printed=1;
   }
   elsif ($line =~ /^(vén[cg].*) (venir) (V......).?$/)
	 { 
       print OUTFILE "$1 $2 $3";
       print OUTFILE "B\n";
       $printed=1;
   }
   elsif ($line =~ /^([^ ]+)(e) ([^ ]+) (V.[^N]....).?$/)
	 { 	
	 		$paraula="$1$2";
	 		$arrel="$3";
	 		$postag="$4";
	 		if (($paraula !~ /^(he|ve|vine)$/) 
 	 		   && ($arrel !~ /^.*obrir|.*omplir|.*córrer$/)) #atenció: maniobre
	 		{	
       print OUTFILE "$paraula $arrel $postag";
       print OUTFILE "V\n";
       $printed=1;
      } 
      elsif ($line =~ /^(.*(omple|obre)) ([^ ]+) (VMM02S0).?$/)   
	 		{	
	       print OUTFILE "$paraula $arrel $postag";
	       print OUTFILE "Y\n"; #català - balear omple-li
	       $printed=1;
      } 
   } 
   elsif ($line =~ /^([^ ]+)(a|en?) ([^ ]+) (V.SP...).?$/)
	 { 	
	 		$paraula="$1$2";
	 		if ($paraula!~ /^(s|c)àpig(a|uen)$/)
	 		{	
       print OUTFILE "$1$2 $3 $4";
       print OUTFILE "V\n";
       $printed=1;
      } 
   } 
   elsif ($line =~ /^([^ ]+)(es) ([^ ]+) (V.SP...).?$/)
	 { 	
	 		$paraula="$1$2";
	 		if ($paraula!~ /^(s|c)àpigues|fes$/)
	 		{	
       print OUTFILE "$1$2 $3 $4";
       print OUTFILE "V\n";
       $printed=1;
      } 
   } 
   elsif ($line =~ /^([^ ]+)(en?) ([^ ]+) (V.M....).?$/)
	 { 	
	 		$paraula="$1$2";
	 		if ($paraula!~ /^(s|c)àpiguen|vine|.*obre|.*omple|pren|ven|fen|.*corre$/)
	 		{	
       print OUTFILE "$1$2 $3 $4";
       print OUTFILE "V\n";
       $printed=1;
      } 
   } 
   elsif ($line =~ /^([^ ]+)(a) ([^ ]+) (V.M03S0).?$/)
	 { 	
	 		$paraula="$1$2";
	 		if ($paraula!~ /^(s|c)àpiga$/)
	 		{	
       print OUTFILE "$1$2 $3 $4";
       print OUTFILE "V\n";
       $printed=1;
      } 
   }
   elsif ($line =~ /^([^ ]+)(asses|àssem|àsseu|assen|esses|éssem|ésseu|essen|isses|íssem|ísseu|issen) ([^ ]+) (V.SI...).?$/)
	 { 	
	 		$paraula="$1$2";
	 		if ($paraula!~ /^$/)
	 		{	
       print OUTFILE "$1$2 $3 $4";
       print OUTFILE "V\n";
       $printed=1;
      } 
   }
   elsif ($line =~ /^(.+(assis|àssim|àssiu|assin)) ([^ ]+) (V.SI...).?$/)
	 { 	
	 		$paraula="$1";
	 		$arrel="$3"; 
	 		#if ($arrel!~ /^.+escar$/)
	 		{	
       print OUTFILE "$1 $3 $4";
       print OUTFILE "B\n";
       $printed=1;
      } 
   }
   elsif ($line =~ /^([^ ]+)(essis|éssim|éssiu|essin|issis|íssim|íssiu|issin) ([^ ]+) (V.SI...).?$/)
	 { 	
	 		$paraula="$1$2";
	 		if ($paraula!~ /^$/)
	 		{	
       print OUTFILE "$1$2 $3 $4";
       print OUTFILE "Y\n";
       $printed=1;
      } 
   }
   elsif ($line =~ /^([^ ]+)(.*[iï](sc|sca|sques|squem|squeu|squen)|.+e(sca|sques|squen)) ([^ ]+) (V.SI...).?$/)
	 { 	
	 		$paraula="$1$2";
	 		$arrel="$5"; 
	 		if (($paraula!~ /^.*(visc|visquem|visqueu)$/) && ($arrel!~ /^.*[ei]scar$/))
	 		{	
       print OUTFILE "$1$2 $5 $6";
       print OUTFILE "V\n";
       $printed=1;
      } 
   }
   elsif ($line =~ /^([^ ]*)(òmplic|òbric|córrec) ([^ ]+) (V......).?$/)
	 { 		 		
	 		{	
       print OUTFILE "$1$2 $3 $4";
       print OUTFILE "V\n";
       $printed=1;
      } 
   }
   elsif ($line =~ /^([^ ]*)((tra|ja|ve|cre)e(m|nt|u)) ([^ ]+) (V......).?$/)
	 { 	
	 		$paraula="$1$2";
	 		$arrel="$5"; 
	 		if ($arrel!~ /^(.*crear|desvear)$/)
	 		{	
       print OUTFILE "$1$2 $5 $6";
       print OUTFILE "V\n";
       $printed=1;
      } 
   }
   elsif ($line =~ /^(.+(esqui|esquis|esquin)|.*fé) ([^ ]+) (V......).?$/)
	 { 	
	 		$paraula="$1";
	 		$arrel="$3"; 
	 		if ($arrel!~ /^.+escar$/)
	 		{	
       print OUTFILE "$1 $3 $4";
       print OUTFILE "B\n";
       $printed=1;
      } 
   }
   elsif ($line =~ /^([^ ]+) ([^ ]+) (VMIP1S0).?$/)
	 { 	
	 		$paraula="$1";
	 		$arrel="$2"; 
	 		$postag="$3";
	 		if (($paraula !~ /^(.+[eo]|.+scric|.*tr[ea]c|.*faig|.*[vt]inc|.*[ïie]sc|.*córrec|acut|tix|tiny|pertanc|planc|complanc|guard|absolc|adic|aparec|aprehenc|aprenc|assec|atenc|bec|benveig|caic|calc|carvenc|cloc|coc|colc|commoc|comparec|complac|componc|comprenc|concloc|condolc|conec|confonc|contenc|contradic|corfonc|corprenc|corresponc|crec|dec|decaic|defenc|depenc|desaparec|desaprenc|desatenc|descloc|descomplac|descomponc|desconec|descrec|desdic|desentenc|despenc|desplac|desponc|desprenc|dic|difonc|dissolc|distenc|dolc|duc|embec|emprenc|encenc|encloc|enduc|enfonc|entenc|entredic|entreploc|entreveig|equivalc|escaic|estenc|estic|excloc|expenc|exsolc|fonc|incloc|infonc|interdic|llec|maldic|malentenc|malprenc|malveig|malvenc|malvull|mamprenc|marfonc|menyscrec|moc|molc|noc|ofenc|olc|parec|plac|ploc|ponc|predic|prenc|pretenc|prevalc|preveig|promoc|puc|rac|reabsolc|reaparec|rebec|recaic|recloc|recoc|recomponc|reconec|redic|refonc|remoc|remolc|reprenc|resolc|responc|retonc|retrovenc|reveig|ric|romanc|salprenc|sec|sobreentenc|sobreprenc|sobresec|sobrevalc|solc|somoc|somric|sorprenc|suspenc|sé|tolc|tonc|transfonc|ullprenc|vaig|valc|veig|venc|vull|ajac|ajec|hac|hec|jac|jec)$/)
	 		    && ($arrel !~ /^.*sentir$/))
	 		{	
       print OUTFILE "$paraula $arrel $postag";
       print OUTFILE "B\n";
       $printed=1;
      } 
      elsif ($paraula =~ /^(desatrac|plovisc|nevisc|envisc|entreobr|reompl|tenc|ompl|obr|vénc|abstenc|advenc|avenc|captenc|cartenc|contrafaç|contravenc|convenc|desavenc|desconvenc|desfaç|detenc|entretenc|entrevenc|esdevenc|estrafaç|intervenc|mantenc|menystenc|obtenc|obvenc|perfaç|pervenc|prevenc|provenc|rarefaç|reconvenc|refaç|retenc|revenc|satisfaç|sobrevenc|sostenc|subvenc|viltenc|faç)$/)
	 		{	
       print OUTFILE "$paraula $arrel $postag";
       print OUTFILE "B\n";
       $printed=1;
      } 
      elsif ($paraula =~ /^(.+esc)$/) #valencià+balear
	 		{	
	       print OUTFILE "$paraula $arrel $postag";
	       print OUTFILE "Z\n";
	       $printed=1;
      } 
      elsif ($paraula =~ /^.+[qg]uo$/)         
	 		{	
	       print OUTFILE "$paraula $arrel $postag";
	       print OUTFILE "Y\n"; #català+balear
	       $printed=1;
      } 
      elsif (($paraula =~ /^.+([^e]i|ï)(xo)$/) 
      			&& ($arrel !~ /^.+eixir|.+uixir|.+uixar|.+aixar|.+oixar|.+àixer|.*néixer$/))
       #elimina: fornixo, aclameïxo
	 		{	
	 			 print OUTFILE2 "$paraula $arrel $postag";
	       print OUTFILE2 "Y\n"; #català+balear
	       $printed=1;
      }
      elsif ($paraula =~ /^.+o$/)         
	 		{	
	       print OUTFILE "$paraula $arrel $postag";
	       print OUTFILE "C\n"; #català
	       $printed=1;
      }
      elsif ($paraula =~ /^(acut|complanc|pertanc|planc|isc|desisc|reïsc|sobreïsc|tix|tiny)$/)
		  {	
	       print OUTFILE "$paraula $arrel $postag";
	       print OUTFILE "Z\n"; #valencià+balear
	       $printed=1;
	    }
	    elsif (($paraula =~ /^.*sent$/) && ($arrel =~/^.*sentir$/))
		  {	
	       print OUTFILE "$paraula $arrel $postag";
	       print OUTFILE "Z\n"; #valencià+balear
	       $printed=1;
	    }
      elsif ($paraula =~ /^.+[ïi]sc$/)         
	 		{	
	       print OUTFILE "$paraula $arrel $postag";
	       print OUTFILE "V\n"; #valencià
	       $printed=1;
      }
      elsif ($paraula =~ /^.*[vt]inc$/)         
	 		{	
	       print OUTFILE "$paraula $arrel $postag";
	       print OUTFILE "X\n"; #valencià+català
	       $printed=1;
      }
   }
   elsif ($line =~ /^(.+a[um]) ([^ ]+) (VM[MI].[12]P0).?$/)
	 { 	
	 		$paraula="$1";
	 		$arrel="$2"; 
	 		if (($paraula!~ /^va[um]$/) && ($arrel!~ /^.*caure$/))
	 		{	
       print OUTFILE "$1 $2 $3";
       print OUTFILE "B\n";
       $printed=1;
      } 
   }
   elsif ($line =~ /^(.*corrs?) ([^ ]+) (V......).?$/)
	 { 	
	 		$paraula="$1";
	 		$arrel="$2"; 
	 		#if (($paraula!~ /^va[um]$/) && ($arrel!~ /^.*caure$/))
	 		{	
       print OUTFILE "$1 $2 $3";
       print OUTFILE "B\n";
       $printed=1;
      } 
   }
   elsif ($line =~ /^(.+às) ([^ ]+) (V.SI...).?$/)
	 { 	
	 		$paraula="$1";
	 		$arrel="$2"; 
	 		#if ($arrel!~ /^.+escar$/)
	 		{	
       print OUTFILE "$1 $2 $3";
       print OUTFILE "Z\n"; #valencià+balear
       $printed=1;
      } 
   }
   elsif ($line =~ /^(.+eu) ([^ ]+) (VMM02P0).?$/)
	 { 	
	 		$paraula="$1";
	 		$arrel="$2"; 
	 		if ($arrel!~ /^.+(er|re)$/)
	 		{	
       print OUTFILE "$1 $2 $3";
       print OUTFILE "X\n"; #català+valencià
       $printed=1;
      } 
   }
   elsif ($line =~ /^(.+([^e]i|ï)(x|xes|xen)) ([^ ]+) (V.IP...|VMM02S0).?$/)
	 { 	
	 		$paraula="$1";
	 		$arrel="$4"; 
	 		$postag="$5";
	 		if ($arrel !~ /^.+eixir|.+uixir|.+uixar|.+aixar|.+oixar|.+àixer|.*néixer$/)
	 		{	
       print OUTFILE "$paraula $arrel $postag";
       print OUTFILE "V\n"; #valencià
       $printed=1;
      } 
   }
   elsif ($line =~ /^(.+[iï][ns]?) ([^ ]+) (V.SP...).?$/)
	 { 	
	 		$paraula="$1";
	 		$arrel="$2"; 
	 		$postag="$3";
	 		if ($paraula !~ /^tixi|tixis|tixin|tinyi|tinyis|tinyin$/)
	 		{	
	       print OUTFILE "$paraula $arrel $postag";
	       print OUTFILE "Y\n"; #català balear 
	       $printed=1;
      } 
      else
      {	
	       print OUTFILE "$paraula $arrel $postag";
	       print OUTFILE "B\n"; #balear 
	       $printed=1;
      } 
      
   }
   elsif ($line =~ /^(.+[iï]n?) ([^ ]+) (V.M03[SP].).?$/)
	 { 	
	 		$paraula="$1";
	 		$arrel="$2"; 
	 		$postag="$3";
	 		if ($paraula !~ /^tixi|tixis|tixin|tinyi|tinyis|tinyin$/)
	 		{	
	       print OUTFILE "$paraula $arrel $postag";
	       print OUTFILE "Y\n"; #català balear 
	       $printed=1;
      } 
      else
      {	
	       print OUTFILE "$paraula $arrel $postag";
	       print OUTFILE "B\n"; #balear 
	       $printed=1;
      }
   }
   # excepcions omplir/obrir. Omple/obre present d'indicatiu val per a tots els casos. 
   elsif ($line =~ /^(.*(ompli|obri)[ns]?) ([^ ]+) (VMIP...).?$/)
	 { 	
	 		$paraula="$1";
	 		$arrel="$3"; 
	 		$postag="$4";
	 		#if ($arrel !~ /^.*obrir|.*omplir$/)
	 		{	
       print OUTFILE "$paraula $arrel $postag";
       print OUTFILE "Z\n"; #valencià-balear present d'indicatiu: obri, ompli
       $printed=1;
      } 
   } 
   elsif ($line =~ /^(.*(ompli|obri)) ([^ ]+) (VMM02S0).?$/)
	 { 	
	 		$paraula="$1";
	 		$arrel="$3"; 
	 		$postag="$4";
	 		#if ($arrel !~ /^.*obrir|.*omplir$/)
	 		{	
       print OUTFILE "$paraula $arrel $postag";
       print OUTFILE "V\n"; #valencià present d'indicatiu: obri, ompli
       $printed=1;
      } 
   }
     
   # Més excepcions: tix, tiny, vist, deim i semblants Imperatius: dis, obtín, obtén, etc. 
   
   elsif ($line =~ /^(tix|tixes|tixen|tiny|tinys|tinyen) ([^ ]+) (V.IP...|VMM....).?$/)
	 { 	
	 		$paraula="$1";
	 		$arrel="$2"; 
	 		$postag="$3";
	 		{	
       print OUTFILE "$paraula $arrel $postag";
       print OUTFILE "Z\n"; #valencià balear
       $printed=1;
      } 
   }
   elsif ($line =~ /^(.*[fdv]eim|.*[fdv]eis) ([^ ]+) (V.IP...).?$/)
	 { 	
	 		$paraula="$1";
	 		$arrel="$2"; 
	 		$postag="$3";
	 		{	
       print OUTFILE "$paraula $arrel $postag";
       print OUTFILE "B\n"; # balear
       $printed=1;
      } 
   }
   elsif ($line =~ /^(.*facem|.*faceu|.*feis) ([^ ]+) (V.M....|V.SP...).?$/)
	 { 	
	 		$paraula="$1";
	 		$arrel="$2"; 
	 		$postag="$3";
	 		{	
       print OUTFILE "$paraula $arrel $postag";
       print OUTFILE "B\n"; # balear
       $printed=1;
      } 
   }
   elsif ($line =~ /^(.+[ií]s) (.*dir) (VMM02S0).?$/)
	 { 	
	 		$paraula="$1";
	 		$arrel="$2"; 
	 		$postag="$3";
	 		{	
       print OUTFILE "$paraula $arrel $postag";
       print OUTFILE "V\n"; # valencià
       $printed=1;
      } 
   }
   elsif ($line =~ /^(.*tín|tin) (.*tenir) (VMM02S0).?$/)
	 { 	
	 		$paraula="$1";
	 		$arrel="$2"; 
	 		$postag="$3";
	 		{	
       print OUTFILE "$paraula $arrel $postag";
       print OUTFILE "V\n"; # valencià
       $printed=1;
      } 
   }
   elsif ($line =~ /^(.*igue[mu]) (.+ir) (V......).?$/)
	 {	 	
	 		$paraula="$1";
	 		$arrel="$2";
	 		$postag="$3";
	 		if ($arrel!~ /^(dir|desdir|maldir|adir|contradir|entredir|interdir|predir|redir)$/)
	 		{
	       print OUTFILE "$paraula $arrel $postag";
	       print OUTFILE "B\n";
	       $printed=1;
      }
   }
   
 
   
   if (!$printed)
   {
   		if ($line =~ /^([^ ]+) ([^ ]+) (V......).?$/)
   		{
	   	  print OUTFILE "$1 $2 $3";
	   	  print OUTFILE "0\n";
	   	}
	   	else
	   	{
	   		print OUTFILE "$line\n";
	   	}
   }
   
}
close(FILE1); 
close(OUTFILE);
close(OUTFILE2);

